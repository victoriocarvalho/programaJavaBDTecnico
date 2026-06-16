package br.com.cadastroempregados.persistencia;

import br.com.cadastroempregados.modelo.Empregado;

import java.util.List;

public interface EmpregadoRepository {
    void inserir(Empregado empregado);

    List<Empregado> listar();

    boolean existeCpf(String cpf);
}
