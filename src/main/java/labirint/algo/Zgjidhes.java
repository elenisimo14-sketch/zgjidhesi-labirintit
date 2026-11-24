package labirint.algo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import labirint.model.Labirint;
import labirint.model.Pozicion;
import labirint.model.Rezultat;

/**
 * Kontrata e perbashket e te tre algoritmeve te kerkimit.
 *
 * Nje zgjidhes ekzekutohet i teri dhe kthen nje Rezultat qe permban historikun e
 * hapave. Ai nuk vizaton asgje dhe nuk thrret repaint(): kjo ndarje lejon qe
 * algoritmet te testohen pa hapur asnje dritare dhe qe paneli te mos ngrije
 * Event Dispatch Thread gjate kerkimit.
 */
public abstract class Zgjidhes {

    /** Emri i shkurter i algoritmit, i perdorur ne tabelen e krahasimit. */
    public abstract String emri();

    /**
     * Ekzekuton kerkimin.
     *
     * @param labirint labirinti qe zgjidhet
     * @param regjistro true kur duhet mbajtur historiku i hapave per vizualizim.
     *                  Per matjet e kohes kalohet false, qe kostoja e regjistrimit
     *                  te mos hyje ne mates.
     */
    public abstract Rezultat zgjidh(Labirint labirint, boolean regjistro);

    /** Varianti i perdorur nga nderfaqja: me historik te plote. */
    public Rezultat zgjidh(Labirint labirint) {
        return zgjidh(labirint, true);
    }

    /**
     * Ndertimi i rruges duke ndjekur prapa lidhjet prind, nga dalja te hyrja.
     * Kthen listen nga hyrja te dalja.
     */
    protected static List<Pozicion> ndertoRrugen(Map<Pozicion, Pozicion> prind, Pozicion hyrja, Pozicion dalja) {
        List<Pozicion> rruga = new ArrayList<>();

        // Nisemi nga dalja dhe ndjekim prapa zinxhirin e prinderve. Asnje algoritem nuk
        // e ndertoi rrugen gjate kerkimit: secili ruajti vetem se nga erdhi cdo qelize.
        Pozicion aktuale = dalja;
        while (aktuale != null) {
            rruga.add(aktuale);

            // Hyrja nuk ka prind, prandaj ketu ndalet ecja prapa.
            if (aktuale.equals(hyrja)) {
                break;
            }
            aktuale = prind.get(aktuale);
        }

        // Lista u ndertua nga dalja te hyrja, prandaj kthehet permbys.
        Collections.reverse(rruga);
        return rruga;
    }

    /** Harte prindi me kapacitet fillestar te pershtatur me madhesine e labirintit. */
    protected static Map<Pozicion, Pozicion> hartePrindi(Labirint l) {
        return new HashMap<>(Math.max(16, l.numriQelizaveTeLira() * 2));
    }
}
