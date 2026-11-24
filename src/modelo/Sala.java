package modelo;

/**
 * Sala comum do instituto (sala de reunião, seminário ou laboratório).
 * Duas salas são consideradas iguais quando possuem o mesmo id.
 */
public class Sala {
    private int id;
    private String nome;
    private TipoSala tipo;
    private int capacidade;

    /**
     * Cria uma sala validando seus dados.
     *
     * @param id         identificador (0 quando a sala ainda não foi salva)
     * @param nome       nome da sala, obrigatório
     * @param tipo       tipo da sala, obrigatório
     * @param capacidade número de pessoas, maior que zero
     * @throws IllegalArgumentException se algum dado for inválido
     */
    public Sala(int id, String nome, TipoSala tipo, int capacidade) {
        this.id = id;
        setNome(nome);
        setTipo(tipo);
        setCapacidade(capacidade);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome da sala é obrigatório.");
        }
        this.nome = nome.trim();
    }

    public TipoSala getTipo() {
        return tipo;
    }

    public void setTipo(TipoSala tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("O tipo da sala é obrigatório.");
        }
        this.tipo = tipo;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public void setCapacidade(int capacidade) {
        if (capacidade <= 0) {
            throw new IllegalArgumentException("A capacidade deve ser maior que zero.");
        }
        this.capacidade = capacidade;
    }

    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof Sala)) {
            return false;
        }
        return id == ((Sala) outro).id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return nome;
    }
}
