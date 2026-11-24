package labirint.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import labirint.model.Rezultat;

/**
 * Tabela e krahasimit dhe legjenda e ngjyrave.
 *
 * Mban nje rresht per cdo algoritem. Ne kete faze shfaqet vetem BFS, por tabela eshte
 * ndertuar qe te prese disa rreshta, sepse qellimi i projektit eshte pikerisht krahasimi.
 */
public final class PanelStatistikave extends JPanel {

    private static final long serialVersionUID = 1L;

    private final DefaultTableModel modeli;
    private final JLabel etiketaPermbledhese;

    public PanelStatistikave() {
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setPreferredSize(new Dimension(330, 0));

        modeli = new DefaultTableModel(
                new Object[] { "Algoritmi", "Rruga", "Vizituar", "Koha (ms)" }, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int rreshti, int kolona) {
                return false;
            }
        };

        JTable tabela = new JTable(modeli);
        tabela.setRowHeight(24);
        tabela.getTableHeader().setReorderingAllowed(false);
        tabela.setFillsViewportHeight(true);

        JPanel siper = new JPanel();
        siper.setLayout(new BoxLayout(siper, BoxLayout.Y_AXIS));
        JLabel titulli = new JLabel("Krahasimi i algoritmeve");
        titulli.setFont(titulli.getFont().deriveFont(Font.BOLD, 14f));
        titulli.setAlignmentX(LEFT_ALIGNMENT);
        siper.add(titulli);
        siper.add(Box.createVerticalStrut(8));

        add(siper, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        JPanel poshte = new JPanel();
        poshte.setLayout(new BoxLayout(poshte, BoxLayout.Y_AXIS));
        poshte.add(legjenda());
        poshte.add(Box.createVerticalStrut(8));
        etiketaPermbledhese = new JLabel(" ");
        etiketaPermbledhese.setAlignmentX(LEFT_ALIGNMENT);
        poshte.add(etiketaPermbledhese);
        add(poshte, BorderLayout.SOUTH);
    }

    private JPanel legjenda() {
        JPanel p = new JPanel(new GridLayout(0, 1, 0, 2));
        p.setAlignmentX(LEFT_ALIGNMENT);
        p.setBorder(BorderFactory.createTitledBorder("Legjenda"));
        p.add(rreshtLegjende(PanelLabirinti.ngjyraNeKufi(), "Ne kufirin e kerkimit"));
        p.add(rreshtLegjende(PanelLabirinti.ngjyraEVizituar(), "E vizituar (e zgjeruar)"));
        p.add(rreshtLegjende(PanelLabirinti.ngjyraRruga(), "Rruga perfundimtare"));
        p.add(rreshtLegjende(PanelLabirinti.ngjyraMur(), "Mur"));
        p.add(rreshtLegjende(PanelLabirinti.ngjyraHyrje(), "Hyrja"));
        p.add(rreshtLegjende(PanelLabirinti.ngjyraDalje(), "Dalja"));
        return p;
    }

    private JPanel rreshtLegjende(Color ngjyra, String teksti) {
        JPanel rresht = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JPanel kuti = new JPanel();
        kuti.setBackground(ngjyra);
        kuti.setPreferredSize(new Dimension(16, 16));
        kuti.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        rresht.add(kuti);
        rresht.add(new JLabel(teksti));
        return rresht;
    }

    /**
     * Shton ose perditeson rreshtin e nje algoritmi.
     *
     * Nese algoritmi ekziston tashme ne tabele, rreshti i vjeter hiqet: keshtu nje
     * ekzekutim i dyte i te njejtit algoritem nuk krijon dy rreshta.
     */
    public void shto(Rezultat r) {
        for (int i = 0; i < modeli.getRowCount(); i++) {
            if (modeli.getValueAt(i, 0).equals(r.algoritmi())) {
                modeli.removeRow(i);
                break;
            }
        }
        modeli.addRow(new Object[] {
                r.algoritmi(),
                r.uGjet() ? r.gjatesiaRruges() : "-",
                r.qelizaTeVizituara(),
                String.format("%.3f", r.kohaMs())
        });
        perditesoPermbledhjen();
    }

    private void perditesoPermbledhjen() {
        Integer bfs = null;
        Integer astar = null;
        for (int i = 0; i < modeli.getRowCount(); i++) {
            Object emri = modeli.getValueAt(i, 0);
            Object vizituar = modeli.getValueAt(i, 2);
            if ("BFS".equals(emri)) bfs = (Integer) vizituar;
            if ("A*".equals(emri)) astar = (Integer) vizituar;
        }
        if (bfs != null && astar != null && bfs > 0) {
            double reduktimi = 100.0 * (bfs - astar) / bfs;
            etiketaPermbledhese.setText(String.format(
                    "<html>A* vizitoi %.1f%% me pak qeliza se BFS.</html>", reduktimi));
        } else {
            etiketaPermbledhese.setText(" ");
        }
    }

    public void pastro() {
        modeli.setRowCount(0);
        etiketaPermbledhese.setText(" ");
    }
}
