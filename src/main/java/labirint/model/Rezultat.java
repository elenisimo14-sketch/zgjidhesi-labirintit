package labirint.model;

import java.util.Collections;
import java.util.List;

/**
 * Rezultati i plote i nje ekzekutimi: rruga, metrikat dhe historiku i hapave.
 *
 * Kjo klase eshte e vetmja gje qe nje algoritem kthen, dhe pikerisht permbajtja e saj
 * ishte vendimi me i rendesishem i projektit. Nese do te kthente vetem rrugen, procesi
 * i kerkimit nuk do te mund te vizualizohej, dhe vizualizimi i procesit eshte tema jone.
 * Prandaj permban edhe listen e hapave.
 *
 * Objekti eshte i pandryshueshem: listat kopjohen ne konstruktor dhe kthehen si te tilla,
 * prandaj askush nuk mund ta ndryshoje nje rezultat pasi eshte prodhuar.
 */
public final class Rezultat {

    private final String algoritmi;
    private final boolean uGjet;
    private final List<Pozicion> rruga;
    private final int qelizaTeVizituara;
    private final List<Hap> hapat;
    private final long kohaNano;

    public Rezultat(String algoritmi, boolean uGjet, List<Pozicion> rruga,
                    int qelizaTeVizituara, List<Hap> hapat, long kohaNano) {
        this.algoritmi = algoritmi;
        this.uGjet = uGjet;
        this.rruga = List.copyOf(rruga);
        this.qelizaTeVizituara = qelizaTeVizituara;
        this.hapat = hapat == null ? Collections.emptyList() : List.copyOf(hapat);
        this.kohaNano = kohaNano;
    }

    public String algoritmi() { return algoritmi; }

    public boolean uGjet() { return uGjet; }

    public List<Pozicion> rruga() { return rruga; }

    /**
     * Numri i qelizave te nxjerra nga kufiri dhe te zgjeruara.
     *
     * Ky eshte treguesi kryesor i projektit, i zgjedhur ne vend te kohes sepse eshte i
     * njejte ne cdo pajisje. Koha varet nga procesori dhe nga ngarkesa e makines
     * virtuale, prandaj mbetet tregues dytesor.
     */
    public int qelizaTeVizituara() { return qelizaTeVizituara; }

    public List<Hap> hapat() { return hapat; }

    public long kohaNano() { return kohaNano; }

    /**
     * Gjatesia e rruges e matur ne qeliza, duke perfshire hyrjen dhe daljen.
     *
     * Sherben si tregues cilesie: nje algoritem qe viziton pak qeliza por kthen rruge
     * te gjate nuk eshte me i mire, vetem me i shpejte ne gjetjen e nje zgjidhjeje.
     */
    public int gjatesiaRruges() { return rruga.size(); }

    public double kohaMs() { return kohaNano / 1_000_000.0; }

    @Override
    public String toString() {
        return String.format("%-6s  u gjet=%-5s  rruga=%4d  vizituar=%6d  koha=%8.3f ms",
                algoritmi, uGjet, gjatesiaRruges(), qelizaTeVizituara, kohaMs());
    }
}
