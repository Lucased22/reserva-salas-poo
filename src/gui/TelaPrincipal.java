package gui;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

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

        JLabel titulo = new JLabel("Reserva de Salas - IComp");
        titulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        titulo.setForeground(Color.WHITE);
        JLabel subtitulo = new JLabel("Instituto de Computação - Universidade Federal do Amazonas");
        subtitulo.setForeground(new Color(0xCF, 0xE8, 0xE5));

        JPanel cabecalho = new JPanel();
        cabecalho.setLayout(new BoxLayout(cabecalho, BoxLayout.Y_AXIS));
        cabecalho.setBackground(new Color(0x1F, 0x3A, 0x5F));
        cabecalho.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        cabecalho.add(titulo);
        cabecalho.add(subtitulo);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(cabecalho, BorderLayout.NORTH);
        getContentPane().add(abas, BorderLayout.CENTER);
        setSize(1080, 720);
        setLocationRelativeTo(null);
    }
}
