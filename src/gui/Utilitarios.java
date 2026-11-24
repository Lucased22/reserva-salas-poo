package gui;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Funções auxiliares da interface: formatação de datas e horas, diálogos
 * e montagem de formulários.
 */
final class Utilitarios {

    static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/uuuu");
    static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private Utilitarios() {
    }

    /**
     * @param texto texto digitado no formato dd/mm/aaaa
     * @param campo nome do campo, usado na mensagem de erro
     * @return a data convertida
     * @throws IllegalArgumentException se o texto não for uma data válida
     */
    static LocalDate lerData(String texto, String campo) {
        try {
            return LocalDate.parse(texto.trim(), FORMATO_DATA);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Data inválida em \"" + campo + "\". Use o formato dd/mm/aaaa.");
        }
    }

    /**
     * @param texto texto digitado no formato HH:mm
     * @param campo nome do campo, usado na mensagem de erro
     * @return a hora convertida
     * @throws IllegalArgumentException se o texto não for uma hora válida
     */
    static LocalTime lerHora(String texto, String campo) {
        try {
            return LocalTime.parse(texto.trim(), FORMATO_HORA);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Horário inválido em \"" + campo + "\". Use o formato HH:mm.");
        }
    }

    /**
     * Aplica o padrão visual das tabelas do sistema: linhas mais altas,
     * cabeçalho em negrito e sem colunas verticais.
     */
    static void estilizarTabela(JTable tabela) {
        tabela.setRowHeight(24);
        tabela.setShowVerticalLines(false);
        tabela.setIntercellSpacing(new Dimension(0, 1));
        tabela.getTableHeader().setFont(tabela.getFont().deriveFont(Font.BOLD));
        tabela.getTableHeader().setReorderingAllowed(false);
    }

    /**
     * Destaca um botão principal de ação (texto em negrito).
     */
    static void destacar(JButton botao) {
        botao.setFont(botao.getFont().deriveFont(Font.BOLD));
    }

    static void mostrarErro(Component pai, Exception e) {
        JOptionPane.showMessageDialog(pai, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
    }

    static void mostrarInfo(Component pai, String mensagem) {
        JOptionPane.showMessageDialog(pai, mensagem, "Informação", JOptionPane.INFORMATION_MESSAGE);
    }

    static boolean confirmar(Component pai, String mensagem) {
        return JOptionPane.showConfirmDialog(pai, mensagem, "Confirmação",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    /**
     * Adiciona uma linha "rótulo: campo" a um painel com {@code GridBagLayout}.
     */
    static void adicionarLinha(JPanel painel, int linha, String rotulo, JComponent campo) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = linha;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(4, 6, 4, 10);
        painel.add(new javax.swing.JLabel(rotulo), g);

        g.gridx = 1;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        painel.add(campo, g);
    }

    /**
     * Adiciona um componente ocupando as duas colunas de um painel com {@code GridBagLayout}.
     */
    static void adicionarLinhaCompleta(JPanel painel, int linha, JComponent componente) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = linha;
        g.gridwidth = 2;
        g.anchor = GridBagConstraints.WEST;
        g.insets = new Insets(10, 6, 4, 6);
        painel.add(componente, g);
    }

    static JPanel painelBotoes(JButton... botoes) {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        for (JButton botao : botoes) {
            painel.add(botao);
        }
        return painel;
    }

    /**
     * Executa a ação sempre que o texto do campo mudar.
     */
    static void aoMudarTexto(JTextField campo, Runnable acao) {
        campo.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                acao.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                acao.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                acao.run();
            }
        });
    }
}
