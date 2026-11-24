package modelo;

/**
 * Professor ou funcionário do instituto que pode fazer reservas.
 * Dois usuários são considerados iguais quando possuem o mesmo id.
 */
public class Usuario {
    private int id;
    private String nome;
    private TipoUsuario tipo;
    private String email;

    /**
     * Cria um usuário validando seus dados.
     *
     * @param id    identificador (0 quando o usuário ainda não foi salvo)
     * @param nome  nome completo, obrigatório
     * @param tipo  professor ou funcionário, obrigatório
     * @param email e-mail institucional, obrigatório e contendo '@'
     * @throws IllegalArgumentException se algum dado for inválido
     */
    public Usuario(int id, String nome, TipoUsuario tipo, String email) {
        this.id = id;
        setNome(nome);
        setTipo(tipo);
        setEmail(email);
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
            throw new IllegalArgumentException("O nome do usuário é obrigatório.");
        }
        this.nome = nome.trim();
    }

    public TipoUsuario getTipo() {
        return tipo;
    }

    public void setTipo(TipoUsuario tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("O tipo do usuário é obrigatório.");
        }
        this.tipo = tipo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Informe um e-mail válido (deve conter '@').");
        }
        this.email = email.trim();
    }

    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof Usuario)) {
            return false;
        }
        return id == ((Usuario) outro).id;
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
