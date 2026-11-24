package modelo;

/**
 * Tipos de sala comum do IComp.
 */
public enum TipoSala {
    REUNIAO("Sala de Reuniões"),
    SEMINARIO("Sala de Seminários"),
    LABORATORIO("Laboratório de Graduação");

    private final String descricao;

    TipoSala(String descricao) {
        this.descricao = descricao;
    }

    /**
     * @return descrição legível do tipo, usada na interface e nos relatórios
     */
    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
