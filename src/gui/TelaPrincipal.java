package gui;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;

/**
 * Janela principal do sistema, com uma aba para cada área de trabalho.
 */
public class TelaPrincipal extends JFrame {

    private static final long serialVersionUID = 1L;

    /**
     * Monta a janela e suas abas. Os dados são recarregados ao trocar de aba.
     */
    public TelaPrincipal() {
        super("Reserva de Salas - IComp");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Salas", new PainelSalas());
        abas.addTab("Usuários", new PainelUsuarios());
        abas.addTab("Reservas", new PainelReservas());
        abas.addTab("Relatórios", new PainelRelatorios());

        abas.addChangeListener(e -> {
            if (abas.getSelectedComponent() instanceof Atualizavel atualizavel) {
                atualizavel.atualizar();
            }
        });

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(abas, BorderLayout.CENTER);
        setSize(1000, 660);
        setLocationRelativeTo(null);
    }
}
