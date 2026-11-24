package gui;

import dao.ReservaDAO;
import dao.SalaDAO;
import dao.UsuarioDAO;
import modelo.Reserva;
import modelo.Sala;
import modelo.Usuario;
import servico.RegraNegocioException;
import servico.ReservaService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Aba de reservas: cria novas reservas (com verificação de conflito),
 * lista as reservas existentes, consulta e cancela.
 */
public class PainelReservas extends JPanel implements Atualizavel {

    private static final long serialVersionUID = 1L;

    private final SalaDAO salaDAO = new SalaDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final ReservaDAO reservaDAO = new ReservaDAO();
    private final ReservaService servico = new ReservaService();

    private final JComboBox<Sala> cmbSala = new JComboBox<>();
    private final JComboBox<Usuario> cmbUsuario = new JComboBox<>();
    private final JTextField txtData = new JTextField(10);
    private final JTextField txtInicio = new JTextField("08:00", 6);
    private final JTextField txtFim = new JTextField("09:00", 6);
    private final JTextField txtFinalidade = new JTextField(24);
    private final JTextField txtBusca = new JTextField(24);

    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"ID", "Data", "Início", "Término", "Sala", "Responsável", "Finalidade"}, 0) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int linha, int coluna) {
            return false;
        }
    };
    private final JTable tabela = new JTable(modelo);
    private final TableRowSorter<DefaultTableModel> ordenacao = new TableRowSorter<>(modelo);

    private List<Reserva> reservas = new ArrayList<>();

    /**
     * Monta o formulário de reserva, a busca e a tabela de reservas.
     */
    public PainelReservas() {
        super(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        txtData.setText(LocalDate.now().format(Utilitarios.FORMATO_DATA));

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(BorderFactory.createTitledBorder("Nova reserva"));
        Utilitarios.adicionarLinha(formulario, 0, "Sala:", cmbSala);
        Utilitarios.adicionarLinha(formulario, 1, "Responsável:", cmbUsuario);
        Utilitarios.adicionarLinha(formulario, 2, "Data (dd/mm/aaaa):", txtData);
        Utilitarios.adicionarLinha(formulario, 3, "Início (HH:mm):", txtInicio);
        Utilitarios.adicionarLinha(formulario, 4, "Término (HH:mm):", txtFim);
        Utilitarios.adicionarLinha(formulario, 5, "Finalidade:", txtFinalidade);

        JButton btnReservar = new JButton("Reservar");
        JButton btnCancelar = new JButton("Cancelar reserva selecionada");
        btnReservar.addActionListener(e -> reservar());
        btnCancelar.addActionListener(e -> cancelar());
        Utilitarios.adicionarLinhaCompleta(formulario, 6,
                Utilitarios.painelBotoes(btnReservar, btnCancelar));

        JPanel centro = new JPanel(new BorderLayout(6, 6));
        JPanel barraBusca = new JPanel(new BorderLayout(6, 0));
        barraBusca.add(new JLabel("Buscar (qualquer campo):"), BorderLayout.WEST);
        barraBusca.add(txtBusca, BorderLayout.CENTER);
        centro.add(barraBusca, BorderLayout.NORTH);
        centro.add(new JScrollPane(tabela), BorderLayout.CENTER);

        tabela.setRowSorter(ordenacao);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getColumnModel().getColumn(0).setMaxWidth(60);
        Utilitarios.aoMudarTexto(txtBusca, this::aplicarFiltro);

        add(formulario, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        atualizar();
    }

    @Override
    public void atualizar() {
        try {
            Sala salaAtual = (Sala) cmbSala.getSelectedItem();
            Usuario usuarioAtual = (Usuario) cmbUsuario.getSelectedItem();

            cmbSala.removeAllItems();
            for (Sala s : salaDAO.listarTodas()) {
                cmbSala.addItem(s);
            }
            cmbUsuario.removeAllItems();
            for (Usuario u : usuarioDAO.listarTodos()) {
                cmbUsuario.addItem(u);
            }
            if (salaAtual != null) {
                cmbSala.setSelectedItem(salaAtual);
            }
            if (usuarioAtual != null) {
                cmbUsuario.setSelectedItem(usuarioAtual);
            }

            reservas = reservaDAO.listarTodas();
            modelo.setRowCount(0);
            for (Reserva r : reservas) {
                modelo.addRow(new Object[]{
                        r.getId(),
                        r.getData().format(Utilitarios.FORMATO_DATA),
                        r.getHoraInicio().format(Utilitarios.FORMATO_HORA),
                        r.getHoraFim().format(Utilitarios.FORMATO_HORA),
                        r.getSala().getNome(),
                        r.getUsuario().getNome(),
                        r.getFinalidade()});
            }
        } catch (SQLException e) {
            Utilitarios.mostrarErro(this, e);
        }
    }

    private void reservar() {
        try {
            Sala sala = (Sala) cmbSala.getSelectedItem();
            Usuario usuario = (Usuario) cmbUsuario.getSelectedItem();
            if (sala == null || usuario == null) {
                throw new IllegalArgumentException(
                        "Cadastre ao menos uma sala e um usuário antes de fazer reservas.");
            }
            servico.reservar(
                    sala,
                    usuario,
                    Utilitarios.lerData(txtData.getText(), "Data"),
                    Utilitarios.lerHora(txtInicio.getText(), "Início"),
                    Utilitarios.lerHora(txtFim.getText(), "Término"),
                    txtFinalidade.getText());
            Utilitarios.mostrarInfo(this, "Reserva realizada com sucesso.");
            txtFinalidade.setText("");
            atualizar();
        } catch (IllegalArgumentException | RegraNegocioException | SQLException e) {
            Utilitarios.mostrarErro(this, e);
        }
    }

    private void cancelar() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            Utilitarios.mostrarInfo(this, "Selecione uma reserva na tabela para cancelar.");
            return;
        }
        Reserva reserva = reservas.get(tabela.convertRowIndexToModel(linha));
        if (!Utilitarios.confirmar(this, "Cancelar a reserva de " + reserva + "?")) {
            return;
        }
        try {
            servico.cancelarReserva(reserva.getId());
            atualizar();
        } catch (SQLException e) {
            Utilitarios.mostrarErro(this, e);
        }
    }

    private void aplicarFiltro() {
        ordenacao.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(txtBusca.getText()), -1));
    }
}
