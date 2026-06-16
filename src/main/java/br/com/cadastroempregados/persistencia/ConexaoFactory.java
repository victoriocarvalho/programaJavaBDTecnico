package br.com.cadastroempregados.persistencia;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConexaoFactory {
    private static final String ENV_URL = "APP_DB_URL";
    private static final String ENV_USUARIO = "APP_DB_USUARIO";
    private static final String ENV_SENHA = "APP_DB_SENHA";
    private final Map<String, String> configuracoes;

    public ConexaoFactory() {
        this.configuracoes = carregarEnv();
    }

    public Connection conectar() {
        String url = lerConfiguracaoObrigatoria(ENV_URL);
        String usuario = lerConfiguracaoObrigatoria(ENV_USUARIO);
        String senha = lerConfiguracaoObrigatoria(ENV_SENHA);

        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(url, usuario, senha);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Driver JDBC do PostgreSQL nao encontrado.", e);
        } catch (SQLException e) {
            throw new IllegalStateException("Nao foi possivel conectar ao banco de dados.", e);
        }
    }

    private String lerConfiguracaoObrigatoria(String nome) {
        String valor = configuracoes.get(nome);

        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalStateException("Configure " + nome + " no arquivo .env.");
        }

        return valor.trim();
    }

    private Map<String, String> carregarEnv() {
        Path caminho = Paths.get(".env");

        if (!Files.exists(caminho)) {
            throw new IllegalStateException("Arquivo .env nao encontrado na raiz do projeto.");
        }

        Map<String, String> valores = new HashMap<>();

        try {
            List<String> linhas = Files.readAllLines(caminho);

            for (String linha : linhas) {
                String linhaLimpa = linha.trim();

                if (linhaLimpa.isEmpty() || linhaLimpa.startsWith("#")) {
                    continue;
                }

                int posicaoIgual = linhaLimpa.indexOf('=');

                if (posicaoIgual <= 0) {
                    continue;
                }

                String chave = linhaLimpa.substring(0, posicaoIgual).trim();
                String valor = linhaLimpa.substring(posicaoIgual + 1).trim();
                valores.put(chave, removerAspas(valor));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Nao foi possivel ler o arquivo .env.", e);
        }

        return valores;
    }

    private String removerAspas(String valor) {
        if ((valor.startsWith("\"") && valor.endsWith("\"")) || (valor.startsWith("'") && valor.endsWith("'"))) {
            return valor.substring(1, valor.length() - 1);
        }

        return valor;
    }
}
