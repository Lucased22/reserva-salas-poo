package dao;

import modelo.Reserva;
import modelo.Sala;
import modelo.TipoSala;
import modelo.TipoUsuario;
import modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Acesso à tabela {@code reservas}, unida às tabelas de salas e usuários
 * para montar os objetos {@link Reserva} completos.
 */
public class ReservaDAO {

    private static final String SELECT = """
        SELECT r.id, r.data, r.hora_inicio, r.hora_fim, r.finalidade,
               s.id AS sala_id, s.nome AS sala_nome, s.tipo AS sala_tipo, s.capacidade AS sala_capacidade,
               u.id AS usuario_id, u.nome AS usuario_nome, u.tipo AS usuario_tipo, u.email AS usuario_email
        FROM reservas r
        JOIN salas s ON s.id = r.sala_id
        JOIN usuarios u ON u.id = r.usuario_id
        """;

    /**
     * @return todas as reservas, ordenadas por data e horário de início
     * @throws SQLException em caso de erro de banco
     */
    public List<Reserva> listarTodas() throws SQLException {
        return consultar(SELECT + " ORDER BY r.data, r.hora_inicio", ps -> { });
    }

    /**
     * Lista as reservas de uma sala em um intervalo de datas (inclusive).
     *
     * @param salaId identificador da sala
     * @param inicio primeira data do intervalo
     * @param fim    última data do intervalo
     * @return reservas da sala no período, ordenadas por data e horário
     * @throws SQLException em caso de erro de banco
     */
    public List<Reserva> listarPorSala(int salaId, LocalDate inicio, LocalDate fim) throws SQLException {
        String sql = SELECT + " WHERE r.sala_id = ? AND r.data BETWEEN ? AND ? ORDER BY r.data, r.hora_inicio";
        return consultar(sql, ps -> {
            ps.setInt(1, salaId);
            ps.setString(2, inicio.toString());
            ps.setString(3, fim.toString());
        });
    }

    /**
     * Insere uma reserva e atualiza o id do objeto com o valor gerado.
     *
     * @param reserva reserva a ser gravada
     * @throws SQLException em caso de erro de banco
     */
    public void inserir(Reserva reserva) throws SQLException {
        String sql = """
            INSERT INTO reservas (sala_id, usuario_id, data, hora_inicio, hora_fim, finalidade)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (Connection conexao = ConexaoBD.abrir();
             PreparedStatement ps = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, reserva.getSala().getId());
            ps.setInt(2, reserva.getUsuario().getId());
            ps.setString(3, reserva.getData().toString());
            ps.setString(4, reserva.getHoraInicio().toString());
            ps.setString(5, reserva.getHoraFim().toString());
            ps.setString(6, reserva.getFinalidade());
            ps.executeUpdate();
            try (ResultSet chaves = ps.getGeneratedKeys()) {
                if (chaves.next()) {
                    reserva.setId(chaves.getInt(1));
                }
            }
        }
    }

    /**
     * Remove (cancela) uma reserva pelo id.
     *
     * @param id identificador da reserva
     * @throws SQLException em caso de erro de banco
     */
    public void remover(int id) throws SQLException {
        try (Connection conexao = ConexaoBD.abrir();
             PreparedStatement ps = conexao.prepareStatement("DELETE FROM reservas WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * @param salaId identificador da sala
     * @return {@code true} se a sala possui ao menos uma reserva
     * @throws SQLException em caso de erro de banco
     */
    public boolean existeReservaParaSala(int salaId) throws SQLException {
        return contar("sala_id", salaId) > 0;
    }

    /**
     * @param usuarioId identificador do usuário
     * @return {@code true} se o usuário possui ao menos uma reserva
     * @throws SQLException em caso de erro de banco
     */
    public boolean existeReservaParaUsuario(int usuarioId) throws SQLException {
        return contar("usuario_id", usuarioId) > 0;
    }

    private int contar(String coluna, int valor) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservas WHERE " + coluna + " = ?";
        try (Connection conexao = ConexaoBD.abrir();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            ps.setInt(1, valor);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    @FunctionalInterface
    private interface Parametros {
        void definir(PreparedStatement ps) throws SQLException;
    }

    private List<Reserva> consultar(String sql, Parametros parametros) throws SQLException {
        List<Reserva> reservas = new ArrayList<>();
        try (Connection conexao = ConexaoBD.abrir();
             PreparedStatement ps = conexao.prepareStatement(sql)) {
            parametros.definir(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reservas.add(mapear(rs));
                }
            }
        }
        return reservas;
    }

    private Reserva mapear(ResultSet rs) throws SQLException {
        Sala sala = new Sala(
                rs.getInt("sala_id"),
                rs.getString("sala_nome"),
                TipoSala.valueOf(rs.getString("sala_tipo")),
                rs.getInt("sala_capacidade"));
        Usuario usuario = new Usuario(
                rs.getInt("usuario_id"),
                rs.getString("usuario_nome"),
                TipoUsuario.valueOf(rs.getString("usuario_tipo")),
                rs.getString("usuario_email"));
        return new Reserva(
                rs.getInt("id"),
                sala,
                usuario,
                LocalDate.parse(rs.getString("data")),
                LocalTime.parse(rs.getString("hora_inicio")),
                LocalTime.parse(rs.getString("hora_fim")),
                rs.getString("finalidade"));
    }
}
