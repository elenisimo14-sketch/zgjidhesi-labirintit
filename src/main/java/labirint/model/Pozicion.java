package labirint.model;

/**
 * Koordinatat e nje qelize ne rrjete. Rreshti rritet poshte, kolona djathtas.
 *
 * Eshte shkruar si record sepse eshte vetem nje cift vlerash: Java gjeneron vete
 * equals dhe hashCode, gje qe eshte e domosdoshme meqe Pozicion perdoret si celes
 * ne hartat e prinderve.
 */
public record Pozicion(int rreshti, int kolona) {

    /**
     * Distanca Manhattan deri te nje pozicion tjeter: sa qeliza lart-poshte plus sa
     * majtas-djathtas, sikur te mos kishte asnje mur.
     *
     * Ne kete faze perdoret vetem per te verifikuar se dy qeliza te njepasnjeshme te
     * nje rruge jane vertet fqinje, pra distanca midis tyre eshte 1. Me vone do te
     * sherbeje si heuristike per A*.
     */
    public int distancaManhattan(Pozicion tjetri) {
        return Math.abs(this.rreshti - tjetri.rreshti) + Math.abs(this.kolona - tjetri.kolona);
    }

    @Override
    public String toString() {
        return "(" + rreshti + "," + kolona + ")";
    }
}
