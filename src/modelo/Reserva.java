package modelo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Reserva de uma sala por um usuário em uma data e intervalo de horário.
 */
public class Reserva {
    private int id;
    private Sala sala;
    private Usuario usuario;
    private LocalDate data;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private String finalidade;

    /**
     * Cria uma reserva validando seus dados.
     *
     * @param id         identificador (0 quando a reserva ainda não foi salva)
     * @param sala       sala reservada, obrigatória
     * @param usuario    responsável pela reserva, obrigatório
     * @param data       data da reserva, obrigatória
     * @param horaInicio horário de início, obrigatório
     * @param horaFim    horário de término, deve ser posterior ao início
     * @param finalidade motivo da reserva, obrigatório
     * @throws IllegalArgumentException se algum dado for inválido
     */
    public Reserva(int id, Sala sala, Usuario usuario, LocalDate data,
                   LocalTime horaInicio, LocalTime horaFim, String finalidade) {
        if (sala == null) {
            throw new IllegalArgumentException("Selecione a sala.");
        }
        if (usuario == null) {
            throw new IllegalArgumentException("Selecione o responsável pela reserva.");
        }
        if (data == null) {
            throw new IllegalArgumentException("Informe a data da reserva.");
        }
        if (horaInicio == null || horaFim == null) {
            throw new IllegalArgumentException("Informe o horário de início e de término.");
        }
        if (!horaFim.isAfter(horaInicio)) {
            throw new IllegalArgumentException("O horário de término deve ser posterior ao de início.");
        }
        if (finalidade == null || finalidade.isBlank()) {
            throw new IllegalArgumentException("Informe a finalidade da reserva.");
        }
        this.id = id;
        this.sala = sala;
        this.usuario = usuario;
        this.data = data;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.finalidade = finalidade.trim();
    }

    /**
     * Verifica se esta reserva se sobrepõe a outra: mesma sala, mesma data e
     * intervalos de horário que se cruzam.
     *
     * @param outra reserva a comparar
     * @return {@code true} se houver conflito de horário
     */
    public boolean conflitaCom(Reserva outra) {
        return sala.getId() == outra.sala.getId()
                && data.equals(outra.data)
                && horaInicio.isBefore(outra.horaFim)
                && horaFim.isAfter(outra.horaInicio);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Sala getSala() {
        return sala;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public LocalDate getData() {
        return data;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public LocalTime getHoraFim() {
        return horaFim;
    }

    public String getFinalidade() {
        return finalidade;
    }

    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof Reserva)) {
            return false;
        }
        return id == ((Reserva) outro).id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return sala.getNome() + " (" + data.format(DateTimeFormatter.ofPattern("dd/MM/uuuu"))
                + ", " + horaInicio + "-" + horaFim + ")";
    }
}
