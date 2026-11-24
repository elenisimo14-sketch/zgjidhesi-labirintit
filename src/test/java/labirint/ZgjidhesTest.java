package labirint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import labirint.algo.ZgjidhesBFS;
import labirint.model.Labirint;
import labirint.model.Pozicion;
import labirint.model.Rezultat;

/**
 * Testet e saktesise.
 *
 * Rezultati i pritshem nuk vjen nga implementimi qe testohet: numri 135 i qelizave te
 * arritshme llogaritet me nje permbytje BFS te pavarur brenda vete testit, dhe
 * optimaliteti i DFS krahasohet me BFS.
 */
class ZgjidhesTest {

    private static final Labirint REF = Labirint.referues();

    @Test
    @DisplayName("T1 - labirinti i referimit ka permasat dhe numrat e pritur")
    void strukturaELabirintit() {
        assertEquals(14, REF.rreshta());
        assertEquals(23, REF.kolona());
        assertEquals(147, REF.numriQelizaveTeLira());
        assertEquals(175, REF.numriMureve());
        assertEquals(new Pozicion(4, 0), REF.hyrja());
        assertEquals(new Pozicion(13, 21), REF.dalja());
        assertEquals(135, qelizaTeArritshme(REF),
                "12 nga 147 qelizat e lira ndodhen ne xhepa te izoluar");
    }

    @Test
    @DisplayName("T2 - BFS gjen rrugen optimale me 35 qeliza")
    void bfsGjenRrugenOptimale() {
        Rezultat r = new ZgjidhesBFS().zgjidh(REF, true);
        assertTrue(r.uGjet());
        assertEquals(35, r.gjatesiaRruges());
        assertTrue(rrugaEVlefshme(REF, r.rruga()));
    }

    @Test
    @DisplayName("T4 - historiku regjistrohet vetem kur kerkohet")
    void historikuIHapave() {
        Rezultat me = new ZgjidhesBFS().zgjidh(REF, true);
        Rezultat pa = new ZgjidhesBFS().zgjidh(REF, false);
        assertFalse(me.hapat().isEmpty());
        assertTrue(pa.hapat().isEmpty());
        assertTrue(me.hapat().stream().allMatch(h -> REF.eshteLire(h.qeliza())));
    }

    @Test
    @DisplayName("T5 - labirint pa zgjidhje raportohet qarte")
    void labirintPaZgjidhje() {
        int[][] m = new int[7][7];
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 7; j++) {
                m[i][j] = (i == 0 || j == 0 || i == 6 || j == 6) ? Labirint.MUR : Labirint.E_LIRE;
            }
        }
        m[5][4] = Labirint.MUR;
        m[4][4] = Labirint.MUR;
        m[4][5] = Labirint.MUR;
        Labirint pa = new Labirint(m, new Pozicion(1, 1), new Pozicion(5, 5));

        Rezultat r = new ZgjidhesBFS().zgjidh(pa, false);
        assertFalse(r.uGjet(), "BFS nuk duhet te gjeje rruge");
        assertTrue(r.rruga().isEmpty(), "BFS duhet te ktheje rruge boshe");
    }

    @Test
    @DisplayName("T6 - hyrja e pavlefshme refuzohet me mesazh te qarte")
    void validimiIHyrjes() {
        assertThrows(IllegalArgumentException.class, () ->
                new Labirint(new int[][] { { 1, 1 }, { 1, 0 } }, new Pozicion(0, 0), new Pozicion(1, 1)));
        assertThrows(IllegalArgumentException.class, () ->
                new Labirint(new int[][] { { 0, 0 }, { 0 } }, new Pozicion(0, 0), new Pozicion(0, 1)));
        assertThrows(IllegalArgumentException.class, () ->
                new Labirint(new int[][] { { 0, 2 }, { 0, 0 } }, new Pozicion(0, 0), new Pozicion(1, 1)));
        assertThrows(IllegalArgumentException.class, () ->
                new Labirint(new int[][] { { 0, 0 }, { 0, 0 } }, new Pozicion(0, 0), new Pozicion(0, 0)));
    }

    // ---------- ndihmesit e testit, te pavarur nga kodi qe testohet ----------

    private static boolean rrugaEVlefshme(Labirint l, List<Pozicion> rruga) {
        if (rruga.isEmpty()) {
            return false;
        }
        if (!rruga.get(0).equals(l.hyrja()) || !rruga.get(rruga.size() - 1).equals(l.dalja())) {
            return false;
        }
        Set<Pozicion> pare = new HashSet<>();
        for (int i = 0; i < rruga.size(); i++) {
            Pozicion p = rruga.get(i);
            if (!l.brendaKufijve(p) || !l.eshteLire(p) || !pare.add(p)) {
                return false;
            }
            if (i > 0 && p.distancaManhattan(rruga.get(i - 1)) != 1) {
                return false;
            }
        }
        return true;
    }

    private static int qelizaTeArritshme(Labirint l) {
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
}
