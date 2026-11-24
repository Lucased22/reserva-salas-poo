package servico;

/**
 * Erro de regra de negócio (ex.: conflito de horário ou remoção de sala com reservas).
 * Sua mensagem é exibida diretamente ao usuário.
 */
public class RegraNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * @param mensagem descrição do problema para o usuário
     */
    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
