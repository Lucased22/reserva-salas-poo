package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Gerencia a conexão com o banco SQLite local ({@code salas.db}) e a criação
 * das tabelas do sistema.
 */
public final class ConexaoBD {

    private static final String URL = "jdbc:sqlite:salas.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "Driver SQLite não encontrado. Coloque sqlite-jdbc-*.jar na pasta lib/ (veja o README).", e);
        }
    }

    private static final String[] TABELAS = {
        """
        CREATE TABLE IF NOT EXISTS salas (
            id         INTEGER PRIMARY KEY AUTOINCREMENT,
            nome       TEXT NOT NULL,
            tipo       TEXT NOT NULL,
            capacidade INTEGER NOT NULL CHECK (capacidade > 0)
        )
        """,
        """
        CREATE TABLE IF NOT EXISTS usuarios (
            id    INTEGER PRIMARY KEY AUTOINCREMENT,
            nome  TEXT NOT NULL,
            tipo  TEXT NOT NULL,
            email TEXT NOT NULL
        )
        """,
        """
        CREATE TABLE IF NOT EXISTS reservas (
            id          INTEGER PRIMARY KEY AUTOINCREMENT,
            sala_id     INTEGER NOT NULL REFERENCES salas (id),
            usuario_id  INTEGER NOT NULL REFERENCES usuarios (id),
            data        TEXT NOT NULL,
            hora_inicio TEXT NOT NULL,
            hora_fim    TEXT NOT NULL,
            finalidade  TEXT NOT NULL
        )
        """
    };

    private ConexaoBD() {
    }

    /**
     * Abre uma nova conexão com o banco, com chaves estrangeiras habilitadas.
     *
     * @return conexão aberta; deve ser fechada pelo chamador
     * @throws SQLException se o banco não puder ser acessado
     */
    public static Connection abrir() throws SQLException {
        Connection conexao = DriverManager.getConnection(URL);
        try (Statement st = conexao.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }
        return conexao;
    }

    /**
     * Cria as tabelas do sistema caso ainda não existam.
     *
     * @throws SQLException se houver erro ao criar as tabelas
     */
    public static void criarTabelas() throws SQLException {
        try (Connection conexao = abrir(); Statement st = conexao.createStatement()) {
            for (String sql : TABELAS) {
                st.execute(sql);
            }
        }
    }
}
