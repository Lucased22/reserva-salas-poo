package dao;

import modelo.TipoUsuario;
import modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acesso à tabela {@code usuarios}.
 */
public class UsuarioDAO {

    private static final String SELECT = "SELECT id, nome, tipo, email FROM usuarios";

    /**
     * @return todos os usuários, ordenados pelo nome
     * @throws SQLException em caso de erro de banco
     */
    public List<Usuario> listarTodos() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection conexao = ConexaoBD.abrir();
             PreparedStatement ps = conexao.prepareStatement(SELECT + " ORDER BY nome");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                usuarios.add(mapear(rs));
            }
        }
        return usuarios;
    }

    /**
     * @param id identificador do usuário
     * @return o usuário encontrado, ou vazio se não existir
     * @throws SQLException em caso de erro de banco
     */
    public Optional<Usuario> buscarPorId(int id) throws SQLException {
        try (Connection conexao = ConexaoBD.abrir();
             PreparedStatement ps = conexao.prepareStatement(SELECT + " WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    /**
     * Insere um novo usuário e atualiza o id do objeto com o valor gerado.
     *
     * @param usuario usuário a ser cadastrado
     * @throws SQLException em caso de erro de banco
     */
    public void inserir(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuarios (nome, tipo, email) VALUES (?, ?, ?)";
        try (Connection conexao = ConexaoBD.abrir();
             PreparedStatement ps = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, usuario.getNome());
            ps.setString(2, usuario.getTipo().name());
            ps.setString(3, usuario.getEmail());
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    usuario.setId(chaves.getInt(1));
                }
            }
        }
    }

    /**
     * Atualiza os dados de um usuário existente.
     *
     * @param usuario usuário com id e dados novos
     * @throws SQLException em caso de erro de banco
     */
    public void atualizar(Usuario usuario) throws SQLException {
        String sql = "UPDATE usuarios SET nome = ?, tipo = ?, email = ? WHERE id = ?";
        try (Connection conexao = ConexaoBD.abrir();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, usuario.getNome());
            ps.setString(2, usuario.getTipo().name());
            ps.setString(3, usuario.getEmail());
            ps.setInt(4, usuario.getId());
            ps.executeUpdate();
        }
    }

    /**
     * Remove um usuário pelo id. Usuários com reservas são recusados pelo banco;
     * a verificação amigável fica em {@code ReservaService}.
     *
     * @param id identificador do usuário
     * @throws SQLException em caso de erro de banco
     */
    public void remover(int id) throws SQLException {
        try (Connection conexao = ConexaoBD.abrir();
             PreparedStatement ps = conexao.prepareStatement("DELETE FROM usuarios WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        return new Usuario(
                rs.getInt("id"),
                rs.getString("nome"),
                TipoUsuario.valueOf(rs.getString("tipo")),
                rs.getString("email"));
    }
}
