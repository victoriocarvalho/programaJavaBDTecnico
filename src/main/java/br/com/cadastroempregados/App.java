package br.com.cadastroempregados;

import br.com.cadastroempregados.aplicacao.EmpregadoService;
import br.com.cadastroempregados.persistencia.ConexaoFactory;
import br.com.cadastroempregados.persistencia.EmpregadoRepositoryPostgres;
import br.com.cadastroempregados.ui.EmpregadoConsole;
import br.com.cadastroempregados.ui.EmpregadoFrame;

public class App {
    public static void main(String[] args) {
        EmpregadoRepositoryPostgres repository = new EmpregadoRepositoryPostgres(new ConexaoFactory());
        EmpregadoService service = new EmpregadoService(repository);
        EmpregadoConsole console = new EmpregadoConsole(service);
        console.iniciar();
        //EmpregadoFrame janela = new EmpregadoFrame(service);
        //janela.setVisible(true);
    }
}
