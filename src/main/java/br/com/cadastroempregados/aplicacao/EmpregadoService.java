package br.com.cadastroempregados.aplicacao;

import br.com.cadastroempregados.modelo.Empregado;
import br.com.cadastroempregados.persistencia.EmpregadoRepository;

import java.math.BigDecimal;
import java.util.List;

public class EmpregadoService {
    private final EmpregadoRepository repository;

    public EmpregadoService(EmpregadoRepository repository) {
        this.repository = repository;
    }

    public void inserir(String nome, String cpf, String salarioTexto) {
        if (estaVazio(nome) || estaVazio(cpf) || estaVazio(salarioTexto)) {
            throw new IllegalArgumentException("Preencha nome, CPF e salario.");
        }

        BigDecimal salario = converterSalario(salarioTexto);

        if (salario.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O salario nao pode ser negativo.");
        }

        String cpfLimpo = cpf.trim();

        if (repository.existeCpf(cpfLimpo)) {
            throw new IllegalArgumentException("Ja existe um empregado com este CPF.");
        }

        Empregado empregado = new Empregado(nome.trim(), cpfLimpo, salario);
        repository.inserir(empregado);
    }

    public List<Empregado> listar() {
        return repository.listar();
    }

    private boolean estaVazio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private BigDecimal converterSalario(String salarioTexto) {
        try {
            return new BigDecimal(salarioTexto.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Informe um salario numerico valido.");
        }
    }
}
