package br.com.cadastroempregados.ui;

import br.com.cadastroempregados.aplicacao.EmpregadoService;
import br.com.cadastroempregados.modelo.Empregado;

import java.math.RoundingMode;
import java.util.List;
import java.util.Scanner;

public class EmpregadoConsole {
    private final EmpregadoService service;
    private final Scanner scanner;

    public EmpregadoConsole(EmpregadoService service) {
        this.service = service;
        this.scanner = new Scanner(System.in);
    }

    public void iniciar() {
        boolean executando = true;

        while (executando) {
            exibirMenu();
            String opcao = scanner.nextLine().trim();

            switch (opcao) {
                case "1":
                    listarEmpregados();
                    break;
                case "2":
                    cadastrarEmpregado();
                    break;
                case "0":
                    executando = false;
                    System.out.println("Encerrando o sistema.");
                    break;
                default:
                    System.out.println("Opcao invalida. Tente novamente.");
                    break;
            }
        }
    }

    private void exibirMenu() {
        System.out.println();
        System.out.println("=== Cadastro de Empregados ===");
        System.out.println("1 - Listar empregados");
        System.out.println("2 - Cadastrar novo empregado");
        System.out.println("0 - Sair");
        System.out.print("Escolha uma opcao: ");
    }

    private void listarEmpregados() {
        List<Empregado> empregados = service.listar();

        if (empregados.isEmpty()) {
            System.out.println("Nenhum empregado cadastrado.");
            return;
        }

        System.out.println();
        System.out.println("Empregados cadastrados:");

        for (Empregado empregado : empregados) {
            System.out.println("----------------------------------------");
            System.out.println("Nome: " + empregado.getNome());
            System.out.println("CPF: " + empregado.getCpf());
            System.out.println("Salario: " + empregado.getSalario().setScale(2, RoundingMode.HALF_UP));
        }

        System.out.println("----------------------------------------");
    }

    private void cadastrarEmpregado() {
        System.out.println();
        System.out.println("Cadastro de novo empregado");

        System.out.print("Nome: ");
        String nome = scanner.nextLine();

        System.out.print("CPF: ");
        String cpf = scanner.nextLine();

        System.out.print("Salario: ");
        String salario = scanner.nextLine();

        try {
            service.inserir(nome, cpf, salario);
            System.out.println("Empregado cadastrado com sucesso.");
        } catch (IllegalArgumentException e) {
            System.out.println("Dados invalidos: " + e.getMessage());
        }
    }
}
