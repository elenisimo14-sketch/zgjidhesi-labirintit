package labirint.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JPanel;
import javax.swing.Timer;

import labirint.model.Hap;
import labirint.model.Labirint;
import labirint.model.LlojHapi;
import labirint.model.Pozicion;
import labirint.model.Rezultat;

/**
 * Vizatimi i labirintit dhe riluajtja e historikut te hapave.
 *
 * Paneli nuk llogarit asgje: ai merr nje Rezultat te gatshem dhe e luan listen e
 * hapave me javax.swing.Timer. Kohematesi i Swing-ut e dergon cdo tik ne Event
 * Dispatch Thread, prandaj nderfaqja mbetet e pergjegjshme gjate animacionit.
 * Nje Thread.sleep brenda algoritmit do ta bllokonte EDT dhe dritarja do te ngrinte.
 */
public final class PanelLabirinti extends JPanel {

    private static final long serialVersionUID = 1L;

    /**
     * Gjendjet e mundshme te nje qelize gjate riluajtjes.
     *
     * Ruhen si byte ne nje varg dydimensional te vecante nga matrica e labirintit:
     * labirinti nuk ndryshon kurre, ndersa kjo rrjete rishkruhet ne cdo ekzekutim.
     */
    private static final byte E_LIRE = 0;
    private static final byte MUR = 1;
    private static final byte NE_KUFI = 2;
    private static final byte E_VIZITUAR = 3;
    private static final byte RRUGA = 4;

    private static final Color NGJYRA_MUR = new Color(33, 37, 43);
    private static final Color NGJYRA_E_LIRE = new Color(250, 250, 250);
    private static final Color NGJYRA_NE_KUFI = new Color(255, 214, 102);
    private static final Color NGJYRA_E_VIZITUAR = new Color(144, 190, 232);
    private static final Color NGJYRA_RRUGA = new Color(46, 160, 86);
    private static final Color NGJYRA_HYRJE = new Color(214, 72, 72);
    private static final Color NGJYRA_DALJE = new Color(126, 68, 173);
    private static final Color NGJYRA_RRJETE = new Color(220, 220, 220);

    private Labirint labirint;
    private byte[][] gjendja;

    private Rezultat rezultat;
    private List<Hap> hapat = List.of();
    private List<Pozicion> rruga = List.of();
    private int indeksiHapit;
    private int indeksiRruges;
    private boolean faza2;

    private Timer kohematesi;
    private int msPerHap = 20;
    private Runnable kurPerfundon;
    private Consumer<String> raportues;

    public PanelLabirinti(Labirint labirint) {
        setLabirint(labirint);
        setBackground(Color.WHITE);
    }

    /** Nderron labirintin e shfaqur dhe pershtat permasen e qelizes sipas tij. */
    public void setLabirint(Labirint labirint) {
        ndal();
        this.labirint = labirint;
        this.gjendja = new byte[labirint.rreshta()][labirint.kolona()];
        pastro();
        // Permasa e qelizes llogaritet qe labirinti te hyje ne rreth 700 piksele,
        // por kurre nen 8 piksele, perndryshe ngjyrat nuk dallohen me sy.
        int madhesia = Math.max(8, Math.min(32, 700 / Math.max(labirint.rreshta(), labirint.kolona())));
        setPreferredSize(new Dimension(labirint.kolona() * madhesia, labirint.rreshta() * madhesia));
        revalidate();
    }

    public Labirint labirint() {
        return labirint;
    }

    public void setMsPerHap(int ms) {
        this.msPerHap = Math.max(1, ms);
        if (kohematesi != null) {
            kohematesi.setDelay(this.msPerHap);
        }
    }

    public void setKurPerfundon(Runnable r) {
        this.kurPerfundon = r;
    }

    public void setRaportues(Consumer<String> r) {
        this.raportues = r;
    }

    /** Rikthen rrjeten ne gjendjen fillestare: vetem mure dhe qeliza te lira. */
    public void pastro() {
        ndal();
        for (int i = 0; i < labirint.rreshta(); i++) {
            for (int j = 0; j < labirint.kolona(); j++) {
                gjendja[i][j] = labirint.vlera(i, j) == Labirint.MUR ? MUR : E_LIRE;
            }
        }
        rezultat = null;
        hapat = List.of();
        rruga = List.of();
        indeksiHapit = 0;
        indeksiRruges = 0;
        faza2 = false;
        repaint();
    }

    public void ndal() {
        if (kohematesi != null && kohematesi.isRunning()) {
            kohematesi.stop();
        }
    }

    public boolean dukeLuajtur() {
        return kohematesi != null && kohematesi.isRunning();
    }

    /**
     * Nis riluajtjen e nje rezultati te llogaritur me pare.
     *
     * Vini re se nuk merr nje labirint dhe nje algoritem, por nje Rezultat te gatshem:
     * llogaritja ka perfunduar tashme, dhe ketu vetem riluhet ajo qe ndodhi.
     */
    public void luaj(Rezultat rezultat) {
        pastro();
        this.rezultat = rezultat;
        this.hapat = rezultat.hapat();
        this.rruga = rezultat.rruga();
        this.indeksiHapit = 0;
        this.indeksiRruges = 0;
        this.faza2 = false;

        // Timer i Swing-ut, jo nje Thread: cdo tik vjen ne Event Dispatch Thread,
        // prandaj nuk duhet asnje sinkronizim dhe nderfaqja mbetet e pergjegjshme.
        kohematesi = new Timer(msPerHap, e -> tik());
        kohematesi.start();
    }

    /** Shfaq menjehere gjendjen perfundimtare, pa animacion. */
    public void shfaqMenjehere(Rezultat rezultat) {
        pastro();
        this.rezultat = rezultat;
        for (Hap h : rezultat.hapat()) {
            apliko(h);
        }
        for (Pozicion p : rezultat.rruga()) {
            gjendja[p.rreshti()][p.kolona()] = RRUGA;
        }
        repaint();
        if (kurPerfundon != null) {
            kurPerfundon.run();
        }
    }

    /**
     * Nje tik i kohematesit. Thirret automatikisht ne Event Dispatch Thread.
     *
     * Animacioni ka dy faza: e para luan historikun e kerkimit, e dyta vizaton rrugen
     * e gjetur mbi te. Kjo e ben rrugen te duket qarte mbi sfondin e qelizave te prekura.
     */
    private void tik() {
        if (!faza2) {
            if (indeksiHapit < hapat.size()) {
                apliko(hapat.get(indeksiHapit++));
            }
            if (indeksiHapit >= hapat.size()) {
                faza2 = true;
            }
        } else {
            if (indeksiRruges < rruga.size()) {
                Pozicion p = rruga.get(indeksiRruges++);
                gjendja[p.rreshti()][p.kolona()] = RRUGA;
            }
            if (indeksiRruges >= rruga.size()) {
                kohematesi.stop();
                if (raportues != null && rezultat != null) {
                    raportues.accept(pershkrimi(rezultat));
                }
                if (kurPerfundon != null) {
                    kurPerfundon.run();
                }
            }
        }
        repaint();
    }

    private String pershkrimi(Rezultat r) {
        if (!r.uGjet()) {
            return r.algoritmi() + ": dalja eshte e paarritshme. U zgjeruan "
                    + r.qelizaTeVizituara() + " qeliza.";
        }
        return r.algoritmi() + ": rruge me " + r.gjatesiaRruges() + " qeliza, "
                + r.qelizaTeVizituara() + " qeliza te vizituara, "
                + String.format("%.3f", r.kohaMs()) + " ms.";
    }

    /** Perkthen nje hap te kerkimit ne nje ndryshim ngjyre mbi rrjeten. */
    private void apliko(Hap h) {
        Pozicion p = h.qeliza();
        byte aktuale = gjendja[p.rreshti()][p.kolona()];
        if (aktuale == MUR) {
            return;
        }
        if (h.lloj() == LlojHapi.VIZITUAR) {
            // E vizituar fiton gjithmone mbi ne-kufi: nje qelize e zgjeruar nuk kthehet.
            gjendja[p.rreshti()][p.kolona()] = E_VIZITUAR;
        } else if (aktuale == E_LIRE) {
            // Vetem nje qelize ende e paprekur mund te hyje ne kufi.
            gjendja[p.rreshti()][p.kolona()] = NE_KUFI;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        int madhesia = Math.max(2, Math.min(getWidth() / labirint.kolona(), getHeight() / labirint.rreshta()));
        int gjeresia = madhesia * labirint.kolona();
        int lartesia = madhesia * labirint.rreshta();
        int x0 = (getWidth() - gjeresia) / 2;
        int y0 = (getHeight() - lartesia) / 2;

        for (int i = 0; i < labirint.rreshta(); i++) {
            for (int j = 0; j < labirint.kolona(); j++) {
                g2.setColor(ngjyraE(gjendja[i][j]));
                g2.fillRect(x0 + j * madhesia, y0 + i * madhesia, madhesia, madhesia);
            }
        }

        vizatoShenje(g2, labirint.hyrja(), NGJYRA_HYRJE, x0, y0, madhesia);
        vizatoShenje(g2, labirint.dalja(), NGJYRA_DALJE, x0, y0, madhesia);

        if (madhesia >= 10) {
            g2.setColor(NGJYRA_RRJETE);
            for (int i = 0; i <= labirint.rreshta(); i++) {
                g2.drawLine(x0, y0 + i * madhesia, x0 + gjeresia, y0 + i * madhesia);
            }
            for (int j = 0; j <= labirint.kolona(); j++) {
                g2.drawLine(x0 + j * madhesia, y0, x0 + j * madhesia, y0 + lartesia);
            }
        }
    }

    private void vizatoShenje(Graphics2D g2, Pozicion p, Color c, int x0, int y0, int madhesia) {
        g2.setColor(c);
        int zbritje = Math.max(1, madhesia / 5);
        g2.fillOval(x0 + p.kolona() * madhesia + zbritje, y0 + p.rreshti() * madhesia + zbritje,
                madhesia - 2 * zbritje, madhesia - 2 * zbritje);
    }

    private static Color ngjyraE(byte gjendje) {
        return switch (gjendje) {
            case MUR -> NGJYRA_MUR;
            case NE_KUFI -> NGJYRA_NE_KUFI;
            case E_VIZITUAR -> NGJYRA_E_VIZITUAR;
            case RRUGA -> NGJYRA_RRUGA;
            default -> NGJYRA_E_LIRE;
        };
    }

    static Color ngjyraMur() { return NGJYRA_MUR; }
    static Color ngjyraNeKufi() { return NGJYRA_NE_KUFI; }
    static Color ngjyraEVizituar() { return NGJYRA_E_VIZITUAR; }
    static Color ngjyraRruga() { return NGJYRA_RRUGA; }
    static Color ngjyraHyrje() { return NGJYRA_HYRJE; }
    static Color ngjyraDalje() { return NGJYRA_DALJE; }
}
