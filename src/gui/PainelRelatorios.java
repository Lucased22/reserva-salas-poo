package gui;

import dao.ReservaDAO;
import dao.SalaDAO;
import modelo.Reserva;
import modelo.Sala;
import relatorio.RelatorioPorta;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.awt.print.PrinterException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Aba de geração do relatório de uma sala para impressão e afixação na porta.
 */
public class PainelRelatorios extends JPanel implements Atualizavel {

    private static final long serialVersionUID = 1L;

    private final SalaDAO salaDAO = new SalaDAO();
    private final ReservaDAO reservaDAO = new ReservaDAO();

    private final JComboBox<Sala> cmbSala = new JComboBox<>();
    private final JTextField txtInicio = new JTextField(10);
    private final JTextField txtFim = new JTextField(10);
    private final JTextArea areaRelatorio = new JTextArea();

    /**
     * Monta os controles de período e a área de visualização do relatório.
     */
    public PainelRelatorios() {
        super(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        LocalDate hoje = LocalDate.now();
        txtInicio.setText(hoje.format(Utilitarios.FORMATO_DATA));
        txtFim.setText(hoje.plusDays(6).format(Utilitarios.FORMATO_DATA));

        JPanel controles = new JPanel(new GridBagLayout());
        controles.setBorder(BorderFactory.createTitledBorder("Relatório da porta"));
        Utilitarios.adicionarLinha(controles, 0, "Sala:", cmbSala);
        Utilitarios.adicionarLinha(controles, 1, "De (dd/mm/aaaa):", txtInicio);
        Utilitarios.adicionarLinha(controles, 2, "Até (dd/mm/aaaa):", txtFim);

        JButton btnGerar = new JButton("Gerar");
        JButton btnImprimir = new JButton("Imprimir");
        JButton btnSalvar = new JButton("Salvar em arquivo .txt");
        btnGerar.addActionListener(e -> gerar());
        btnImprimir.addActionListener(e -> imprimir());
        btnSalvar.addActionListener(e -> salvarArquivo());
        Utilitarios.adicionarLinhaCompleta(controles, 3,
                Utilitarios.painelBotoes(btnGerar, btnImprimir, btnSalvar));

        areaRelatorio.setEditable(false);
        areaRelatorio.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        areaRelatorio.setMargin(new java.awt.Insets(8, 8, 8, 8));

        add(controles, BorderLayout.NORTH);
        add(new JScrollPane(areaRelatorio), BorderLayout.CENTER);
        atualizar();
    }

    @Override
    public void atualizar() {
        try {
            Sala atual = (Sala) cmbSala.getSelectedItem();
            cmbSala.removeAllItems();
            for (Sala s : salaDAO.listarTodas()) {
                cmbSala.addItem(s);
            }
            if (atual != null) {
                cmbSala.setSelectedItem(atual);
            }
        } catch (SQLException e) {
            Utilitarios.mostrarErro(this, e);
        }
    }

    private void gerar() {
        try {
            Sala sala = (Sala) cmbSala.getSelectedItem();
            if (sala == null) {
                throw new IllegalArgumentException("Cadastre uma sala antes de gerar o relatório.");
            }
            LocalDate inicio = Utilitarios.lerData(txtInicio.getText(), "De");
            LocalDate fim = Utilitarios.lerData(txtFim.getText(), "Até");
            if (fim.isBefore(inicio)) {
                throw new IllegalArgumentException("A data final deve ser igual ou posterior à inicial.");
            }
            List<Reserva> reservas = reservaDAO.listarPorSala(sala.getId(), inicio, fim);
            areaRelatorio.setText(RelatorioPorta.gerar(sala, reservas, inicio, fim));
            areaRelatorio.setCaretPosition(0);
        } catch (IllegalArgumentException | SQLException e) {
            Utilitarios.mostrarErro(this, e);
        }
    }

    private void imprimir() {
        if (areaRelatorio.getText().isBlank()) {
            gerar();
            if (areaRelatorio.getText().isBlank()) {
                return;
            }
        }
        try {
            areaRelatorio.print();
        } catch (PrinterException e) {
            Utilitarios.mostrarErro(this, e);
        }
    }

    private void salvarArquivo() {
        if (areaRelatorio.getText().isBlank()) {
            gerar();
            if (areaRelatorio.getText().isBlank()) {
                return;
            }
        }
        JFileChooser seletor = new JFileChooser();
        seletor.setSelectedFile(new File("relatorio-sala.txt"));
        if (seletor.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            Files.writeString(seletor.getSelectedFile().toPath(), areaRelatorio.getText(), StandardCharsets.UTF_8);
            Utilitarios.mostrarInfo(this, "Relatório salvo com sucesso.");
        } catch (IOException e) {
            Utilitarios.mostrarErro(this, e);
        }
    }
}
