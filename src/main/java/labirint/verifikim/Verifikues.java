package labirint.verifikim;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import labirint.algo.Zgjidhes;
import labirint.algo.ZgjidhesBFS;
import labirint.model.Labirint;
import labirint.model.Pozicion;
import labirint.model.Rezultat;

/**
 * Verifikues i saktesise pa asnje varesi te jashtme.
 *
 * Te njejtat kontrolle ekzistojne edhe si teste JUnit ne src/test/java. Ky verifikues
 * jepet qe testet te mund te ekzekutohen edhe pa Maven dhe pa internet:
 *   javac -d out $(find src/main/java -name "*.java")
 *   java -cp out labirint.verifikim.Verifikues
 *
 * Rezultati i pritshem nuk vjen nga implementimi qe testohet: gjatesia e rruges dhe
 * numri i qelizave te arritshme llogariten me nje permbytje BFS te pavarur me poshte.
 */
public final class Verifikues {

    private static int kaluan = 0;
    private static int deshtuan = 0;

    public static void main(String[] args) {
        Labirint ref = Labirint.referues();

        // T1 - struktura e labirintit te referimit
        kontrollo("T1a", "14 rreshta", ref.rreshta() == 14);
        kontrollo("T1b", "23 kolona", ref.kolona() == 23);
        kontrollo("T1c", "147 qeliza te lira", ref.numriQelizaveTeLira() == 147);
        kontrollo("T1d", "175 mure", ref.numriMureve() == 175);
        kontrollo("T1e", "135 qeliza te arritshme nga hyrja", qelizaTeArritshme(ref) == 135);

        // T2 - BFS gjen rrugen optimale me 35 qeliza
        Rezultat bfs = new ZgjidhesBFS().zgjidh(ref, true);
        kontrollo("T2a", "BFS e gjen daljen", bfs.uGjet());
        kontrollo("T2b", "rruga e BFS ka 35 qeliza", bfs.gjatesiaRruges() == 35);
        kontrollo("T2c", "rruga e BFS eshte e vlefshme", rrugaEVlefshme(ref, bfs.rruga()));

        // T4 - historiku i hapave eshte i perdorshem nga paneli
        kontrollo("T4a", "BFS regjistron hapa", !bfs.hapat().isEmpty());
        kontrollo("T4b", "pa regjistrim lista eshte boshe",
                new ZgjidhesBFS().zgjidh(ref, false).hapat().isEmpty());
        kontrollo("T4c", "cdo hap bie mbi nje qelize te lire",
                bfs.hapat().stream().allMatch(h -> ref.eshteLire(h.qeliza())));

        // T5 - labirint pa zgjidhje: dalja rrethohet teresisht me mure
        Labirint pa = paZgjidhje();
        Zgjidhes z = new ZgjidhesBFS();
        Rezultat rPa = z.zgjidh(pa, false);
        kontrollo("T5", "BFS raporton se nuk ka zgjidhje",
                !rPa.uGjet() && rPa.rruga().isEmpty());

        // T6 - validimi i hyrjes
        kontrollo("T6a", "hyrja mbi mur refuzohet", hedhGabim(() ->
                new Labirint(new int[][] { { 1, 1 }, { 1, 0 } }, new Pozicion(0, 0), new Pozicion(1, 1))));
        kontrollo("T6b", "rreshta me gjatesi te ndryshme refuzohen", hedhGabim(() ->
                new Labirint(new int[][] { { 0, 0 }, { 0 } }, new Pozicion(0, 0), new Pozicion(0, 1))));
        kontrollo("T6c", "vlere ndryshe nga 0 dhe 1 refuzohet", hedhGabim(() ->
                new Labirint(new int[][] { { 0, 2 }, { 0, 0 } }, new Pozicion(0, 0), new Pozicion(1, 1))));
        kontrollo("T6d", "hyrja e barabarte me daljen refuzohet", hedhGabim(() ->
                new Labirint(new int[][] { { 0, 0 }, { 0, 0 } }, new Pozicion(0, 0), new Pozicion(0, 0))));

        System.out.println();
        System.out.println("Kaluan: " + kaluan + ", deshtuan: " + deshtuan);
        if (deshtuan > 0) {
            System.exit(1);
        }
    }

    /** Nje labirint 7x7 ku qeliza e daljes eshte izoluar plotesisht me mure. */
    private static Labirint paZgjidhje() {
        int[][] m = new int[7][7];
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 7; j++) {
                m[i][j] = (i == 0 || j == 0 || i == 6 || j == 6) ? Labirint.MUR : Labirint.E_LIRE;
            }
        }
        m[5][4] = Labirint.MUR;
        m[4][4] = Labirint.MUR;
        m[4][5] = Labirint.MUR;
        return new Labirint(m, new Pozicion(1, 1), new Pozicion(5, 5));
    }

    /** Nje rruge eshte e vlefshme nese nis te hyrja, mbaron te dalja dhe cdo hap eshte fqinj i lire. */
    static boolean rrugaEVlefshme(Labirint l, List<Pozicion> rruga) {
        if (rruga.isEmpty()) return false;
        if (!rruga.get(0).equals(l.hyrja())) return false;
        if (!rruga.get(rruga.size() - 1).equals(l.dalja())) return false;
        Set<Pozicion> pare = new HashSet<>();
        for (int i = 0; i < rruga.size(); i++) {
            Pozicion p = rruga.get(i);
            if (!l.brendaKufijve(p) || !l.eshteLire(p)) return false;
            if (!pare.add(p)) return false;
            if (i > 0 && p.distancaManhattan(rruga.get(i - 1)) != 1) return false;
        }
        return true;
    }

    /** Numerimi i pavarur i qelizave te arritshme, pa nderprerje te hershme te kerkimit. */
    static int qelizaTeArritshme(Labirint l) {
        boolean[][] pare = new boolean[l.rreshta()][l.kolona()];
        Deque<Pozicion> radha = new ArrayDeque<>();
        radha.add(l.hyrja());
        pare[l.hyrja().rreshti()][l.hyrja().kolona()] = true;
        int n = 0;
        while (!radha.isEmpty()) {
            Pozicion p = radha.poll();
            n++;
            for (Pozicion f : l.fqinjet(p)) {
                if (!pare[f.rreshti()][f.kolona()]) {
                    pare[f.rreshti()][f.kolona()] = true;
                    radha.add(f);
                }
            }
        }
        return n;
    }

    private static boolean hedhGabim(Runnable r) {
        try {
            r.run();
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    private static void kontrollo(String id, String pershkrimi, boolean kushti) {
        if (kushti) {
            kaluan++;
        } else {
            deshtuan++;
            System.out.println("DESHTOI  " + id + "  " + pershkrimi);
        }
    }

    private Verifikues() {
    }
}
