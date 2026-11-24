package labirint.model;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Perfaqesimi i labirintit si graf i paponderuar. Kjo klase mban vetem te dhenat:
 * ajo nuk di asgje per Swing, per ngjyra dhe per algoritmet qe e perdorin.
 */
public final class Labirint {

    public static final int E_LIRE = 0;
    public static final int MUR = 1;

    /** Levizjet e lejuara: lart, poshte, majtas, djathtas. Pa diagonale. */
    private static final int[][] DREJTIMET = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };

    private final int[][] rrjeta;
    private final Pozicion hyrja;
    private final Pozicion dalja;

    public Labirint(int[][] rrjeta, Pozicion hyrja, Pozicion dalja) {
        if (rrjeta == null || rrjeta.length == 0) {
            throw new IllegalArgumentException("Rrjeta nuk mund te jete boshe.");
        }
        // Gjeresia merret nga rreshti i pare dhe pastaj kerkohet qe te gjithe rreshtat
        // ta kene te njejten. Nje matrice me rreshta te pabarabarte do te shkaktonte
        // gabime indeksi thelle brenda algoritmeve, larg nga shkaku i vertete.
        int gjeresia = rrjeta[0].length;

        // Matrica kopjohet, qe askush jashte kesaj klase te mos mund ta ndryshoje
        // labirintin pasi eshte ndertuar. Kjo e ben objektin praktikisht te pandryshueshem.
        int[][] kopje = new int[rrjeta.length][];
        for (int i = 0; i < rrjeta.length; i++) {
            if (rrjeta[i].length != gjeresia) {
                throw new IllegalArgumentException(
                        "Rreshti " + i + " ka " + rrjeta[i].length + " kolona, pritej " + gjeresia + ".");
            }
            for (int j = 0; j < gjeresia; j++) {
                int v = rrjeta[i][j];
                if (v != E_LIRE && v != MUR) {
                    throw new IllegalArgumentException(
                            "Vlere e palejuar " + v + " ne qelizen (" + i + "," + j + "). Lejohen vetem 0 dhe 1.");
                }
            }
            kopje[i] = rrjeta[i].clone();
        }
        this.rrjeta = kopje;
        this.hyrja = kerkoHyrjeTeVlefshme(hyrja, "Hyrja");
        this.dalja = kerkoHyrjeTeVlefshme(dalja, "Dalja");
        if (this.hyrja.equals(this.dalja)) {
            throw new IllegalArgumentException("Hyrja dhe dalja nuk mund te jene e njejta qelize.");
        }
    }

    private Pozicion kerkoHyrjeTeVlefshme(Pozicion p, String emri) {
        if (p == null) {
            throw new IllegalArgumentException(emri + " nuk mund te jete null.");
        }
        if (!brendaKufijve(p)) {
            throw new IllegalArgumentException(emri + " " + p + " eshte jashte kufijve te labirintit.");
        }
        if (!eshteLire(p)) {
            throw new IllegalArgumentException(emri + " " + p + " bie mbi nje mur.");
        }
        return p;
    }

    public int rreshta() { return rrjeta.length; }

    public int kolona() { return rrjeta[0].length; }

    public Pozicion hyrja() { return hyrja; }

    public Pozicion dalja() { return dalja; }

    public int vlera(int rreshti, int kolona) { return rrjeta[rreshti][kolona]; }

    public boolean brendaKufijve(Pozicion p) {
        return p.rreshti() >= 0 && p.rreshti() < rreshta()
                && p.kolona() >= 0 && p.kolona() < kolona();
    }

    public boolean eshteMur(Pozicion p) { return rrjeta[p.rreshti()][p.kolona()] == MUR; }

    public boolean eshteLire(Pozicion p) { return rrjeta[p.rreshti()][p.kolona()] == E_LIRE; }

    /**
     * Fqinjet ortogonale te lire te nje qelize.
     *
     * Kjo metode eshte vendi ku matrica kthehet ne graf. Qelizat e lira jane nyjet,
     * dhe cdo cift qelizash fqinje te lira eshte nje brinje. Asnje algoritem nuk e
     * shikon matricen drejtperdrejt: te gjithe e perdorin vetem kete metode, prandaj
     * nje ndryshim ne menyren si ruhet labirinti nuk prek asnje algoritem.
     *
     * Kthen nje liste me s'shumti kater elemente, sepse levizja eshte vetem ortogonale.
     */
    public List<Pozicion> fqinjet(Pozicion p) {
        List<Pozicion> rezultat = new ArrayList<>(4);
        for (int[] d : DREJTIMET) {
            Pozicion f = new Pozicion(p.rreshti() + d[0], p.kolona() + d[1]);
            if (brendaKufijve(f) && eshteLire(f)) {
                rezultat.add(f);
            }
        }
        return rezultat;
    }


    /**
     * Kostoja e hyrjes ne nje qelize. Aktualisht 1 per cdo qelize te lire, pra
     * grafi eshte i paponderuar dhe Dijkstra sillet njelloj si BFS.
     *
     * Metoda ekziston qe projekti te mbeshtese me vone terren me kosto te ndryshme,
     * si hartat e Moving AI ku ka baltë ose ujë: atehere mjafton te ndryshohet kjo
     * metode, pa prekur asnje algoritem.
     */
    public int kosto(Pozicion p) {
        return 1;
    }

    public int numriQelizaveTeLira() {
        int n = 0;
        for (int[] rresht : rrjeta) {
            for (int v : rresht) {
                if (v == E_LIRE) n++;
            }
        }
        return n;
    }

    public int numriMureve() { return rreshta() * kolona() - numriQelizaveTeLira(); }

    /**
     * Labirinti i referimit 14x23 i perdorur si rast testimi baze.
     *
     * Permban 147 qeliza te lira dhe 175 mure. Rruga me e shkurter nga hyrja (4,0) te
     * dalja (13,21) ka 35 qeliza, e verifikuar edhe me dore mbi matricen me poshte.
     * Dymbedhjete qeliza te lira ndodhen ne xhepa te izoluar: kjo nuk eshte defekt, por
     * na jep rast testimi per sjelljen e algoritmeve kur ka zona te paarritshme.
     */
    public static Labirint referues() {
        int[][] m = {
            { 1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1 },
            { 1,0,0,0,0,0,1,0,0,1,0,0,1,0,0,0,1,1,0,1,0,0,1 },
            { 1,0,1,0,1,0,1,1,0,1,0,1,1,0,1,0,1,1,1,1,0,1,1 },
            { 1,1,1,0,1,0,1,0,1,1,0,1,0,0,1,0,0,1,0,0,0,0,1 },
            { 0,0,1,0,0,0,0,0,1,0,0,1,0,1,0,1,0,1,0,1,0,0,1 },
            { 1,0,1,0,1,1,1,0,1,0,1,0,0,1,0,1,0,0,0,1,1,0,1 },
            { 1,0,0,1,0,0,0,0,1,0,1,1,0,0,1,0,0,0,0,1,0,0,1 },
            { 1,0,1,1,0,1,0,0,0,0,1,0,1,0,1,1,0,1,0,1,0,1,1 },
            { 1,0,0,0,0,1,0,1,1,0,0,0,0,0,1,0,1,1,0,0,0,0,1 },
            { 1,1,1,1,0,1,1,1,1,1,1,1,0,0,0,0,1,1,1,1,1,1,1 },
            { 1,0,1,0,0,1,1,0,1,0,1,0,0,1,0,1,0,1,0,0,1,0,1 },
            { 1,0,1,0,0,0,0,0,1,0,1,1,0,1,1,1,0,1,1,0,1,0,1 },
            { 1,0,1,0,1,0,1,0,1,0,1,0,0,0,0,0,0,0,0,0,0,0,1 },
            { 1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1 },
        };
        return new Labirint(m, new Pozicion(4, 0), new Pozicion(13, 21));
    }

    /**
     * Lexon nje labirint nga skedar teksti. Rreshti i pare mban kater numra:
     * rreshtiHyrjes kolonaHyrjes rreshtiDaljes kolonaDaljes. Rreshtat pasues
     * mbajne shifrat 0 dhe 1, pa ndarje.
     */
    public static Labirint ngaSkedari(Path rruga) throws IOException {
        List<String> rreshtat = new ArrayList<>();
        for (String r : Files.readAllLines(rruga, StandardCharsets.UTF_8)) {
            String t = r.trim();
            if (!t.isEmpty() && !t.startsWith("#")) {
                rreshtat.add(t);
            }
        }
        if (rreshtat.size() < 2) {
            throw new IOException("Skedari nuk permban as koordinatat as rrjeten.");
        }
        String[] koord = rreshtat.get(0).split("\\s+");
        if (koord.length != 4) {
            throw new IOException("Rreshti i pare duhet te kete kater numra, ka " + koord.length + ".");
        }
        int[][] m = new int[rreshtat.size() - 1][];
        for (int i = 1; i < rreshtat.size(); i++) {
            String r = rreshtat.get(i);
            m[i - 1] = new int[r.length()];
            for (int j = 0; j < r.length(); j++) {
                char c = r.charAt(j);
                if (c != '0' && c != '1') {
                    throw new IOException("Karakter i palejuar '" + c + "' ne rreshtin " + i + ".");
                }
                m[i - 1][j] = c - '0';
            }
        }
        return new Labirint(m,
                new Pozicion(Integer.parseInt(koord[0]), Integer.parseInt(koord[1])),
                new Pozicion(Integer.parseInt(koord[2]), Integer.parseInt(koord[3])));
    }

    /** Formati i pranuar nga ngaSkedari. */
    public String nePerfaqesimTekst() {
        StringBuilder sb = new StringBuilder();
        sb.append(hyrja.rreshti()).append(' ').append(hyrja.kolona()).append(' ')
          .append(dalja.rreshti()).append(' ').append(dalja.kolona()).append('\n');
        for (int[] rresht : rrjeta) {
            for (int v : rresht) {
                sb.append(v);
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
