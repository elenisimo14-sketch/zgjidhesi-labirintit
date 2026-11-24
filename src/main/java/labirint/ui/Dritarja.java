package labirint.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;

import labirint.algo.Zgjidhes;
import labirint.algo.ZgjidhesBFS;
import labirint.model.Labirint;
import labirint.model.Rezultat;

/**
 * Dritarja kryesore: kontrollet, paneli i labirintit dhe tabela e krahasimit.
 *
 * Kjo klase nuk di asgje per menyren si punon nje algoritem. Ajo thjesht i kerkon nje
 * zgjidhesi te zgjidhe labirintin dhe ia kalon rezultatin panelit. Prandaj shtimi i
 * DFS, Dijkstra dhe A* ne fazat e ardhshme kerkon vetem nje buton te ri: asnje rresht
 * i logjikes se kesaj klase nuk ndryshon.
 */
public final class Dritarja extends JFrame {

    private static final long serialVersionUID = 1L;

    private final PanelLabirinti panelLabirinti;
    private final PanelStatistikave panelStatistikave;
    private final JLabel shiritiStatusit;

    private final JButton butoniBFS = new JButton("Zgjidh me BFS");
    private final JButton butoniPastro = new JButton("Pastro");
    private final JSlider rreshqitesiShpejtesise = new JSlider(1, 60, 20);

    public Dritarja() {
        super("Zgjidhesi i labirintit - vizualizim i rruges dhe qelizave te vizituara");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        panelLabirinti = new PanelLabirinti(Labirint.referues());
        panelStatistikave = new PanelStatistikave();
        shiritiStatusit = new JLabel("Gati. Zgjidh nje algoritem.");
        shiritiStatusit.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        // Paneli nuk e njeh dritaren: ai vetem thërret dy funksione qe i jepen ketu.
        // Keshtu paneli mbetet i perdorshem edhe jashte kesaj dritareje.
        panelLabirinti.setRaportues(shiritiStatusit::setText);
        panelLabirinti.setKurPerfundon(() -> aktivizoKontrollet(true));
        panelLabirinti.setMsPerHap(rreshqitesiShpejtesise.getValue());

        setLayout(new BorderLayout());
        add(shiritiKontrolleve(), BorderLayout.NORTH);
        add(panelLabirinti, BorderLayout.CENTER);
        add(panelStatistikave, BorderLayout.EAST);
        add(shiritiStatusit, BorderLayout.SOUTH);

        lidhVeprimet();

        setMinimumSize(new Dimension(1000, 660));
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel shiritiKontrolleve() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        p.add(new JLabel("Zgjidh:"));
        p.add(butoniBFS);
        p.add(butoniPastro);
        p.add(new JLabel("   Shpejtesia (ms/hap):"));
        rreshqitesiShpejtesise.setPreferredSize(new Dimension(140, 24));
        rreshqitesiShpejtesise.setInverted(true);
        p.add(rreshqitesiShpejtesise);
        return p;
    }

    private void lidhVeprimet() {
        butoniBFS.addActionListener(e -> ekzekuto(new ZgjidhesBFS(), true));
        butoniPastro.addActionListener(e -> {
            panelLabirinti.pastro();
            panelStatistikave.pastro();
            shiritiStatusit.setText("Ekrani u pastrua.");
        });
        rreshqitesiShpejtesise.addChangeListener(
                e -> panelLabirinti.setMsPerHap(rreshqitesiShpejtesise.getValue()));
    }

    /**
     * Ekzekuton nje algoritem. Llogaritja behet e tera para animacionit, prandaj
     * nderfaqja nuk bllokohet gjate kerkimit.
     */
    private void ekzekuto(Zgjidhes z, boolean meAnimacion) {
        if (panelLabirinti.dukeLuajtur()) {
            return;
        }
        // Ketu ndodh e gjithe llogaritja, ne nje thirrje te vetme dhe ne nje cast te
        // vetem. Vetem pasi ajo ka perfunduar, animacioni nis.
        Rezultat r = z.zgjidh(panelLabirinti.labirint(), true);
        panelStatistikave.shto(r);

        if (!r.uGjet()) {
            panelLabirinti.shfaqMenjehere(r);
            shiritiStatusit.setText(z.emri() + ": dalja eshte e paarritshme.");
            JOptionPane.showMessageDialog(this,
                    "Dalja nuk eshte e arritshme nga hyrja.\nU zgjeruan "
                            + r.qelizaTeVizituara() + " qeliza pa e gjetur rrugen.",
                    "Pa zgjidhje", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (meAnimacion) {
            aktivizoKontrollet(false);
            shiritiStatusit.setText(z.emri() + " duke u luajtur...");
            panelLabirinti.luaj(r);
        } else {
            panelLabirinti.shfaqMenjehere(r);
        }
    }

    private void aktivizoKontrollet(boolean gjendje) {
        butoniBFS.setEnabled(gjendje);
    }

    public static void nis() {
        SwingUtilities.invokeLater(() -> new Dritarja().setVisible(true));
    }
}
