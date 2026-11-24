package dao;

import modelo.Sala;
import modelo.TipoSala;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acesso à tabela {@code salas}.
 */
public class SalaDAO {

    private static final String SELECT = "SELECT id, nome, tipo, capacidade FROM salas";

    /**
     * @return todas as salas, ordenadas pelo nome
     * @throws SQLException em caso de erro de banco
     */
    public List<Sala> listarTodas() throws SQLException {
        List<Sala> salas = new ArrayList<>();
        try (Connection conexao = ConexaoBD.abrir();
             PreparedStatement ps = conexao.prepareStatement(SELECT + " ORDER BY nome");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                salas.add(mapear(rs));
            }
        }
        return salas;
    }

    /**
     * @param id identificador da sala
     * @return a sala encontrada, ou vazio se não existir
     * @throws SQLException em caso de erro de banco
     */
    public Optional<Sala> buscarPorId(int id) throws SQLException {
        try (Connection conexao = ConexaoBD.abrir();
             PreparedStatement ps = conexao.prepareStatement(SELECT + " WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    /**
     * Insere uma nova sala e atualiza o id do objeto com o valor gerado.
     *
     * @param sala sala a ser cadastrada
     * @throws SQLException em caso de erro de banco
     */
    public void inserir(Sala sala) throws SQLException {
        String sql = "INSERT INTO salas (nome, tipo, capacidade) VALUES (?, ?, ?)";
        try (Connection conexao = ConexaoBD.abrir();
             PreparedStatement ps = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, sala.getNome());
            ps.setString(2, sala.getTipo().name());
            ps.setInt(3, sala.getCapacidade());
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    sala.setId(chaves.getInt(1));
                }
            }
        }
    }

    /**
     * Atualiza os dados de uma sala existente.
     *
     * @param sala sala com id e dados novos
     * @throws SQLException em caso de erro de banco
     */
    public void atualizar(Sala sala) throws SQLException {
        String sql = "UPDATE salas SET nome = ?, tipo = ?, capacidade = ? WHERE id = ?";
        try (Connection conexao = ConexaoBD.abrir();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setString(1, sala.getNome());
            ps.setString(2, sala.getTipo().name());
            ps.setInt(3, sala.getCapacidade());
            ps.setInt(4, sala.getId());
            ps.executeUpdate();
        }
    }

    /**
     * Remove uma sala pelo id. Salas com reservas são recusadas pelo banco;
     * a verificação amigável fica em {@code ReservaService}.
     *
     * @param id identificador da sala
     * @throws SQLException em caso de erro de banco
     */
    public void remover(int id) throws SQLException {
        try (Connection conexao = ConexaoBD.abrir();
             PreparedStatement ps = conexao.prepareStatement("DELETE FROM salas WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Sala mapear(ResultSet rs) throws SQLException {
        return new Sala(
                rs.getInt("id"),
                rs.getString("nome"),
                TipoSala.valueOf(rs.getString("tipo")),
                rs.getInt("capacidade"));
    }
}
