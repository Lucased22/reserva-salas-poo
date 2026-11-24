package servico;

import dao.ReservaDAO;
import dao.SalaDAO;
import dao.UsuarioDAO;
import modelo.Reserva;
import modelo.Sala;
import modelo.Usuario;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Regras de negócio das reservas: impede conflitos de horário e a remoção
 * de salas ou usuários que ainda possuem reservas.
 */
public class ReservaService {

    private final ReservaDAO reservaDAO = new ReservaDAO();
    private final SalaDAO salaDAO = new SalaDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    /**
     * Reserva uma sala para um usuário, desde que não haja outra reserva
     * sobreposta na mesma sala e data.
     *
     * @param sala       sala a reservar
     * @param usuario    responsável pela reserva
     * @param data       data da reserva
     * @param inicio     horário de início
     * @param fim        horário de término
     * @param finalidade motivo da reserva
     * @return a reserva gravada, com id preenchido
     * @throws IllegalArgumentException se os dados forem inválidos
     * @throws RegraNegocioException    se o horário conflitar com outra reserva
     * @throws SQLException             em caso de erro de banco
     */
    public Reserva reservar(Sala sala, Usuario usuario, LocalDate data,
                            LocalTime inicio, LocalTime fim, String finalidade) throws SQLException {
        Reserva nova = new Reserva(0, sala, usuario, data, inicio, fim, finalidade);

        List<Reserva> existentes = reservaDAO.listarPorSala(sala.getId(), data, data);
        for (Reserva existente : existentes) {
            if (nova.conflitaCom(existente)) {
                throw new RegraNegocioException(String.format(
                        "A sala %s já está reservada por %s das %s às %s.",
                        sala.getNome(),
                        existente.getUsuario().getNome(),
                        existente.getHoraInicio(),
                        existente.getHoraFim()));
            }
        }

        reservaDAO.inserir(nova);
        return nova;
    }

    /**
     * Cancela (remove) uma reserva.
     *
     * @param reservaId identificador da reserva
     * @throws SQLException em caso de erro de banco
     */
    public void cancelarReserva(int reservaId) throws SQLException {
        reservaDAO.remover(reservaId);
    }

    /**
     * Remove uma sala, desde que ela não possua reservas.
     *
     * @param salaId identificador da sala
     * @throws RegraNegocioException se a sala possuir reservas
     * @throws SQLException          em caso de erro de banco
     */
    public void removerSala(int salaId) throws SQLException {
        if (reservaDAO.existeReservaParaSala(salaId)) {
            throw new RegraNegocioException(
                    "Esta sala possui reservas. Cancele-as antes de removê-la.");
        }
        salaDAO.remover(salaId);
    }

    /**
     * Remove um usuário, desde que ele não possua reservas.
     *
     * @param usuarioId identificador do usuário
     * @throws RegraNegocioException se o usuário possuir reservas
     * @throws SQLException          em caso de erro de banco
     */
    public void removerUsuario(int usuarioId) throws SQLException {
        if (reservaDAO.existeReservaParaUsuario(usuarioId)) {
            throw new RegraNegocioException(
                    "Este usuário possui reservas. Cancele-as antes de removê-lo.");
        }
        usuarioDAO.remover(usuarioId);
    }
}
