package gui;

import dao.ConexaoBD;

import javax.swing.JOptionPane;
import java.awt.Color;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.sql.SQLException;

/**
 * Ponto de entrada do sistema de reserva de salas.
 */
public final class Main {

    private Main() {
    }

    /**
     * Configura a aparência, cria as tabelas do banco e abre a janela principal.
     *
     * @param args não utilizado
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            configurarAparencia();
            try {
                ConexaoBD.criarTabelas();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null,
                        "Não foi possível acessar o banco de dados:\n" + e.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
            new TelaPrincipal().setVisible(true);
        });
    }

    private static void configurarAparencia() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Mantém a aparência padrão do sistema caso o Nimbus não esteja disponível.
        }
        // Paleta do sistema: azul-marinho (base), teal (seleção) e fundo claro.
        UIManager.put("nimbusBase", new Color(0x1F, 0x3A, 0x5F));
        UIManager.put("nimbusSelectionBackground", new Color(0x2A, 0x9D, 0x8F));
        UIManager.put("nimbusSelectedText", Color.WHITE);
        UIManager.put("control", new Color(0xF4, 0xF6, 0xF8));
    }
}
