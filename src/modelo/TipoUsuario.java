package modelo;

/**
 * Categorias de usuários que podem reservar salas.
 */
public enum TipoUsuario {
    PROFESSOR("Professor"),
    FUNCIONARIO("Funcionário");

    private final String descricao;

    TipoUsuario(String descricao) {
        this.descricao = descricao;
    }

    /**
     * @return descrição legível do tipo de usuário
     */
    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
