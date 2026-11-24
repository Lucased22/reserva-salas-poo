package gui;

import dao.SalaDAO;
import modelo.Sala;
import modelo.TipoSala;
import servico.RegraNegocioException;
import servico.ReservaService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Aba de cadastro, alteração, remoção e consulta de salas.
 */
public class PainelSalas extends JPanel implements Atualizavel {

    private static final long serialVersionUID = 1L;

    private final SalaDAO salaDAO = new SalaDAO();
    private final ReservaService servico = new ReservaService();

    private final JTextField txtNome = new JTextField(24);
    private final JComboBox<TipoSala> cmbTipo = new JComboBox<>(TipoSala.values());
    private final JSpinner spnCapacidade = new JSpinner(new SpinnerNumberModel(10, 1, 1000, 1));
    private final JTextField txtBusca = new JTextField(24);

    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"ID", "Nome", "Tipo", "Capacidade"}, 0) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int linha, int coluna) {
            return false;
        }
    };
    private final JTable tabela = new JTable(modelo);
    private final TableRowSorter<DefaultTableModel> ordenacao = new TableRowSorter<>(modelo);

    private List<Sala> salas = new ArrayList<>();
    private Integer idEmEdicao = null;

    /**
     * Monta o formulário, a busca e a tabela de salas.
     */
    public PainelSalas() {
        super(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(BorderFactory.createTitledBorder("Dados da sala"));
        Utilitarios.adicionarLinha(formulario, 0, "Nome:", txtNome);
        Utilitarios.adicionarLinha(formulario, 1, "Tipo:", cmbTipo);
        Utilitarios.adicionarLinha(formulario, 2, "Capacidade:", spnCapacidade);

        JButton btnSalvar = new JButton("Salvar");
        JButton btnNovo = new JButton("Novo / Limpar");
        JButton btnRemover = new JButton("Remover selecionada");
        btnSalvar.addActionListener(e -> salvar());
        btnNovo.addActionListener(e -> limparFormulario());
        btnRemover.addActionListener(e -> remover());
        Utilitarios.adicionarLinhaCompleta(formulario, 3,
                Utilitarios.painelBotoes(btnSalvar, btnNovo, btnRemover));

        JPanel centro = new JPanel(new BorderLayout(6, 6));
        JPanel barraBusca = new JPanel(new BorderLayout(6, 0));
        barraBusca.add(new JLabel("Buscar por nome:"), BorderLayout.WEST);
        barraBusca.add(txtBusca, BorderLayout.CENTER);
        centro.add(barraBusca, BorderLayout.NORTH);
        centro.add(new JScrollPane(tabela), BorderLayout.CENTER);

        Utilitarios.estilizarTabela(tabela);
        Utilitarios.destacar(btnSalvar);
        tabela.setRowSorter(ordenacao);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getColumnModel().getColumn(0).setMaxWidth(60);
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                carregarSelecionada();
            }
        });
        Utilitarios.aoMudarTexto(txtBusca, this::aplicarFiltro);

        add(formulario, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        atualizar();
    }

    @Override
    public void atualizar() {
        try {
            salas = salaDAO.listarTodas();
            modelo.setRowCount(0);
            for (Sala s : salas) {
                modelo.addRow(new Object[]{
                        s.getId(), s.getNome(), s.getTipo().getDescricao(), s.getCapacidade()});
            }
        } catch (SQLException e) {
            Utilitarios.mostrarErro(this, e);
        }
    }

    private void salvar() {
        try {
            Sala sala = new Sala(
                    idEmEdicao == null ? 0 : idEmEdicao,
                    txtNome.getText(),
                    (TipoSala) cmbTipo.getSelectedItem(),
                    (Integer) spnCapacidade.getValue());
            if (idEmEdicao == null) {
                salaDAO.inserir(sala);
                Utilitarios.mostrarInfo(this, "Sala cadastrada com sucesso.");
            } else {
                salaDAO.atualizar(sala);
                Utilitarios.mostrarInfo(this, "Sala alterada com sucesso.");
            }
            limparFormulario();
            atualizar();
        } catch (IllegalArgumentException | SQLException e) {
            Utilitarios.mostrarErro(this, e);
        }
    }

    private void remover() {
        Sala sala = salaSelecionada();
        if (sala == null) {
            Utilitarios.mostrarInfo(this, "Selecione uma sala na tabela para remover.");
            return;
        }
        if (!Utilitarios.confirmar(this, "Remover a sala \"" + sala.getNome() + "\"?")) {
            return;
        }
        try {
            servico.removerSala(sala.getId());
            limparFormulario();
            atualizar();
        } catch (RegraNegocioException | SQLException e) {
            Utilitarios.mostrarErro(this, e);
        }
    }

    private void carregarSelecionada() {
        Sala sala = salaSelecionada();
        if (sala == null) {
            return;
        }
        txtNome.setText(sala.getNome());
        cmbTipo.setSelectedItem(sala.getTipo());
        spnCapacidade.setValue(sala.getCapacidade());
        idEmEdicao = sala.getId();
    }

    private void limparFormulario() {
        txtNome.setText("");
        cmbTipo.setSelectedIndex(0);
        spnCapacidade.setValue(10);
        idEmEdicao = null;
        tabela.clearSelection();
    }

    private Sala salaSelecionada() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            return null;
        }
        return salas.get(tabela.convertRowIndexToModel(linha));
    }

    private void aplicarFiltro() {
        ordenacao.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(txtBusca.getText()), 1));
    }
}
