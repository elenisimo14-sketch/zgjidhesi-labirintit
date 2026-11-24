package labirint.algo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import labirint.model.Hap;
import labirint.model.Labirint;
import labirint.model.LlojHapi;
import labirint.model.Pozicion;
import labirint.model.Rezultat;

/**
 * Kerkimi ne gjeresi (Breadth-First Search).
 *
 * Ideja: zgjerohu ne valle koncentrike. Fillimisht shqyrto hyrjen, pastaj te gjithe
 * fqinjet e saj, pastaj te gjithe fqinjet e atyre, e keshtu me radhe. Kjo do te thote
 * se cdo qelize arrihet per here te pare permes rruges me te shkurter te mundshme.
 *
 * Pse eshte optimal: radha FIFO garanton se qelizat dalin ne rend jo-zvogelues te
 * distances nga hyrja. Kur dalja del nga radha, asnje rruge me e shkurter nuk mund
 * te ekzistoje, sepse do te ishte gjetur me pare.
 *
 * Koha O(V+E), memoria ndihmese O(V).
 */
public final class ZgjidhesBFS extends Zgjidhes {

    @Override
    public String emri() {
        return "BFS";
    }

    @Override
    public Rezultat zgjidh(Labirint labirint, boolean regjistro) {
        // Matja nis para cdo pune, qe te perfshihet edhe ndertimi i strukturave.
        long fillimi = System.nanoTime();

        // Varg dydimensional ne vend te nje HashSet: indekset i dime, prandaj qasja
        // eshte O(1) e vertete, pa llogaritur asnje hash.
        boolean[][] eZbuluar = new boolean[labirint.rreshta()][labirint.kolona()];

        // Per cdo qelize ruhet se nga cila qelize u arrit. Ne fund kjo harte na lejon
        // ta ndertojme rrugen duke ecur prapa nga dalja te hyrja.
        Map<Pozicion, Pozicion> prind = hartePrindi(labirint);

        // ArrayDeque perdoret si radhe FIFO: shtojme ne fund, heqim nga fillimi.
        // Eshte me i shpejte se LinkedList sepse nuk krijon nje objekt per cdo element.
        Deque<Pozicion> radha = new ArrayDeque<>();

        // Historiku mbahet vetem kur duhet animacion. Gjate matjeve te kohes kalohet
        // false, qe kostoja e regjistrimit te mos hyje brenda numrave.
        List<Hap> hapat = regjistro ? new ArrayList<>() : null;

        Pozicion hyrja = labirint.hyrja();
        Pozicion dalja = labirint.dalja();

        // Hapi fillestar: hyrja futet ne radhe dhe shenohet menjehere si e zbuluar.
        radha.addLast(hyrja);
        eZbuluar[hyrja.rreshti()][hyrja.kolona()] = true;
        if (regjistro) hapat.add(new Hap(hyrja, LlojHapi.NE_KUFI));

        int vizituar = 0;
        boolean uGjet = false;

        while (!radha.isEmpty()) {
            // pollFirst heq nga fillimi, pra qelizen me te vjeter ne radhe. Pikerisht
            // kjo sjellje FIFO e ben BFS-in te zgjerohet ne valle.
            Pozicion aktuale = radha.pollFirst();

            // "E vizituar" nenkupton te zgjeruar, pra te nxjerre nga radha dhe te
            // shqyrtuar. Ky eshte numri qe krahasohet me algoritmet e tjera.
            vizituar++;
            if (regjistro) hapat.add(new Hap(aktuale, LlojHapi.VIZITUAR));

            // Ndalim i hershem: sapo dalja del nga radha, rruga eshte e garantuar me e
            // shkurter. Pa kete ndalim BFS do te zgjeronte te gjitha qelizat e arritshme.
            if (aktuale.equals(dalja)) {
                uGjet = true;
                break;
            }

            for (Pozicion fqinj : labirint.fqinjet(aktuale)) {
                // Shenimi behet ne momentin e futjes ne radhe, jo te daljes. Keshtu nje
                // qelize nuk futet kurre dy here dhe prindi i saj mbetet ai i rruges
                // me te shkurter, sepse ajo arrihet e para pikerisht nga ajo rruge.
                if (!eZbuluar[fqinj.rreshti()][fqinj.kolona()]) {
                    eZbuluar[fqinj.rreshti()][fqinj.kolona()] = true;
                    prind.put(fqinj, aktuale);
                    radha.addLast(fqinj);
                    if (regjistro) hapat.add(new Hap(fqinj, LlojHapi.NE_KUFI));
                }
            }
        }

        // Nese radha u shter pa e gjetur daljen, labirinti nuk ka zgjidhje.
        List<Pozicion> rruga = uGjet
                ? ndertoRrugen(prind, hyrja, dalja)
                : Collections.emptyList();

        return new Rezultat(emri(), uGjet, rruga, vizituar, hapat, System.nanoTime() - fillimi);
    }
}
