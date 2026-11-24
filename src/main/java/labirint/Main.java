package labirint;

import labirint.algo.ZgjidhesBFS;
import labirint.model.Labirint;
import labirint.ui.Dritarja;

/**
 * Pika e hyrjes.
 *
 * Pa argumente nis nderfaqja grafike. Me argumentin "konsole" ekzekutohet BFS
 * mbi labirintin e referimit dhe rezultatet shtypen ne konsole, gje qe eshte e
 * dobishme kur nuk ka ekran ne dispozicion.
 */
public final class Main {

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("konsole")) {
            neKonsole();
        } else {
            Dritarja.nis();
        }
    }

    private static void neKonsole() {
        Labirint l = Labirint.referues();
        System.out.println("Labirinti i referimit: " + l.rreshta() + "x" + l.kolona()
                + ", " + l.numriQelizaveTeLira() + " qeliza te lira, " + l.numriMureve() + " mure.");
        System.out.println("Hyrja " + l.hyrja() + ", dalja " + l.dalja());
        System.out.println();
        System.out.println(new ZgjidhesBFS().zgjidh(l, false));
    }
}
