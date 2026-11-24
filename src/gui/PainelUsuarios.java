package gui;

import dao.UsuarioDAO;
import modelo.TipoUsuario;
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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Aba de cadastro, alteração, remoção e consulta de usuários
 * (professores e funcionários).
 */
public class PainelUsuarios extends JPanel implements Atualizavel {

    private static final long serialVersionUID = 1L;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final ReservaService servico = new ReservaService();

    private final JTextField txtNome = new JTextField(24);
    private final JComboBox<TipoUsuario> cmbTipo = new JComboBox<>(TipoUsuario.values());
    private final JTextField txtEmail = new JTextField(24);
    private final JTextField txtBusca = new JTextField(24);

    private final DefaultTableModel modelo = new DefaultTableModel(
            new Object[]{"ID", "Nome", "Tipo", "E-mail"}, 0) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int linha, int coluna) {
            return false;
        }
    };
    private final JTable tabela = new JTable(modelo);
    private final TableRowSorter<DefaultTableModel> ordenacao = new TableRowSorter<>(modelo);

    private List<Usuario> usuarios = new ArrayList<>();
    private Integer idEmEdicao = null;

    /**
     * Monta o formulário, a busca e a tabela de usuários.
     */
    public PainelUsuarios() {
        super(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(BorderFactory.createTitledBorder("Dados do usuário"));
        Utilitarios.adicionarLinha(formulario, 0, "Nome:", txtNome);
        Utilitarios.adicionarLinha(formulario, 1, "Tipo:", cmbTipo);
        Utilitarios.adicionarLinha(formulario, 2, "E-mail:", txtEmail);

        JButton btnSalvar = new JButton("Salvar");
        JButton btnNovo = new JButton("Novo / Limpar");
        JButton btnRemover = new JButton("Remover selecionado");
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
                carregarSelecionado();
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
            usuarios = usuarioDAO.listarTodos();
            modelo.setRowCount(0);
            for (Usuario u : usuarios) {
                modelo.addRow(new Object[]{
                        u.getId(), u.getNome(), u.getTipo().getDescricao(), u.getEmail()});
            }
        } catch (SQLException e) {
            Utilitarios.mostrarErro(this, e);
        }
    }

    private void salvar() {
        try {
            Usuario usuario = new Usuario(
                    idEmEdicao == null ? 0 : idEmEdicao,
                    txtNome.getText(),
                    (TipoUsuario) cmbTipo.getSelectedItem(),
                    txtEmail.getText());
            if (idEmEdicao == null) {
                usuarioDAO.inserir(usuario);
                Utilitarios.mostrarInfo(this, "Usuário cadastrado com sucesso.");
            } else {
                usuarioDAO.atualizar(usuario);
                Utilitarios.mostrarInfo(this, "Usuário alterado com sucesso.");
            }
            limparFormulario();
            atualizar();
        } catch (IllegalArgumentException | SQLException e) {
            Utilitarios.mostrarErro(this, e);
        }
    }

    private void remover() {
        Usuario usuario = usuarioSelecionado();
        if (usuario == null) {
            Utilitarios.mostrarInfo(this, "Selecione um usuário na tabela para remover.");
            return;
        }
        if (!Utilitarios.confirmar(this, "Remover o usuário \"" + usuario.getNome() + "\"?")) {
            return;
        }
        try {
            servico.removerUsuario(usuario.getId());
            limparFormulario();
            atualizar();
        } catch (RegraNegocioException | SQLException e) {
            Utilitarios.mostrarErro(this, e);
        }
    }

    private void carregarSelecionado() {
        Usuario usuario = usuarioSelecionado();
        if (usuario == null) {
            return;
        }
        txtNome.setText(usuario.getNome());
        cmbTipo.setSelectedItem(usuario.getTipo());
        txtEmail.setText(usuario.getEmail());
        idEmEdicao = usuario.getId();
    }

    private void limparFormulario() {
        txtNome.setText("");
        cmbTipo.setSelectedIndex(0);
        txtEmail.setText("");
        idEmEdicao = null;
        tabela.clearSelection();
    }

    private Usuario usuarioSelecionado() {
        int linha = tabela.getSelectedRow();
        if (linha < 0) {
            return null;
        }
        return usuarios.get(tabela.convertRowIndexToModel(linha));
    }

    private void aplicarFiltro() {
        ordenacao.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(txtBusca.getText()), 1));
    }
}
