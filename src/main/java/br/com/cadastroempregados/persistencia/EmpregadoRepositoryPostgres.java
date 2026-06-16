package br.com.cadastroempregados.persistencia;

import br.com.cadastroempregados.modelo.Empregado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class EmpregadoRepositoryPostgres implements EmpregadoRepository {
    private final ConexaoFactory conexaoFactory;

    public EmpregadoRepositoryPostgres(ConexaoFactory conexaoFactory) {
        this.conexaoFactory = conexaoFactory;
        criarTabelaSeNaoExistir();
    }

    @Override
    public void inserir(Empregado empregado) {
        String sql = "insert into empregado (nome, cpf, salario) values (?, ?, ?)";

        try (Connection conexao = conexaoFactory.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, empregado.getNome());
            comando.setString(2, empregado.getCpf());
            comando.setBigDecimal(3, empregado.getSalario());
            comando.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Nao foi possivel inserir o empregado.", e);
        }
    }

    @Override
    public List<Empregado> listar() {
        String sql = "select nome, cpf, salario from empregado order by nome";
        List<Empregado> empregados = new ArrayList<>();

        try (Connection conexao = conexaoFactory.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {
            while (resultado.next()) {
                Empregado empregado = new Empregado(
                        resultado.getString("nome"),
                        resultado.getString("cpf"),
                        resultado.getBigDecimal("salario")
                );
                empregados.add(empregado);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Nao foi possivel listar os empregados.", e);
        }

        return empregados;
    }

    @Override
    public boolean existeCpf(String cpf) {
        String sql = "select 1 from empregado where cpf = ?";

        try (Connection conexao = conexaoFactory.conectar();
             PreparedStatement comando = conexao.prepareStatement(sql)) {
            comando.setString(1, cpf);

            try (ResultSet resultado = comando.executeQuery()) {
                return resultado.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Nao foi possivel consultar o CPF.", e);
        }
    }

    private void criarTabelaSeNaoExistir() {
        String sql = """
                create table if not exists empregado (
                    id serial primary key,
                    nome varchar(100) not null,
                    cpf varchar(20) not null unique,
                    salario numeric(12, 2) not null
                )
                """;

        try (Connection conexao = conexaoFactory.conectar();
             Statement comando = conexao.createStatement()) {
            comando.executeUpdate(sql);
        } catch (SQLException e) {
            throw new IllegalStateException("Nao foi possivel criar a tabela de empregados.", e);
        }
    }
}
