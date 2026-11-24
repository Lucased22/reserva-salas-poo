package relatorio;

import modelo.Reserva;
import modelo.Sala;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Gera o texto do relatório de uma sala para impressão e afixação na porta.
 * O texto tem largura fixa para ser impresso em fonte monoespaçada.
 */
public final class RelatorioPorta {

    private static final int LARGURA = 72;
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    private RelatorioPorta() {
    }

    /**
     * Monta o relatório da sala com as reservas do período informado.
     *
     * @param sala     sala do relatório
     * @param reservas reservas da sala no período, ordenadas por data e horário
     * @param inicio   primeira data do período
     * @param fim      última data do período
     * @return texto pronto para impressão
     */
    public static String gerar(Sala sala, List<Reserva> reservas, LocalDate inicio, LocalDate fim) {
        String separador = "=".repeat(LARGURA) + "\n";
        String traco = "-".repeat(LARGURA) + "\n";
        StringBuilder sb = new StringBuilder();

        sb.append(separador);
        sb.append(centralizar("INSTITUTO DE COMPUTAÇÃO - IComp")).append('\n');
        sb.append(centralizar("AGENDA DE RESERVAS DA SALA")).append('\n');
        sb.append(separador);
        sb.append(String.format("Sala:        %s%n", sala.getNome()));
        sb.append(String.format("Tipo:        %s%n", sala.getTipo().getDescricao()));
        sb.append(String.format("Capacidade:  %d pessoas%n", sala.getCapacidade()));
        sb.append(String.format("Período:     %s a %s%n", inicio.format(DATA), fim.format(DATA)));
        sb.append(traco);

        if (reservas.isEmpty()) {
            sb.append("Nenhuma reserva no período. Sala disponível.\n");
        } else {
            sb.append(String.format("%-12s %-13s %-22s %s%n", "DATA", "HORÁRIO", "RESPONSÁVEL", "FINALIDADE"));
            sb.append(traco);
            for (Reserva r : reservas) {
                sb.append(String.format("%-12s %-13s %-22s %s%n",
                        r.getData().format(DATA),
                        r.getHoraInicio() + " - " + r.getHoraFim(),
                        truncar(r.getUsuario().getNome(), 22),
                        r.getFinalidade()));
            }
        }

        sb.append(traco);
        sb.append(centralizar("Para reservar, consulte o sistema de reservas.")).append('\n');
        return sb.toString();
    }

    private static String centralizar(String texto) {
        int espacos = Math.max(0, (LARGURA - texto.length()) / 2);
        return " ".repeat(espacos) + texto;
    }

    private static String truncar(String texto, int maximo) {
        return texto.length() > maximo ? texto.substring(0, maximo - 1) + "." : texto;
    }
}
