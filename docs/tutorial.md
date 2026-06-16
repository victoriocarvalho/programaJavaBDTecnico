# Cadastro de Empregados com Java

Este tutorial conduz a criacao de um programa Java em camadas. A primeira versao usa uma lista em memoria. Depois, a camada de persistencia sera substituida por acesso a banco de dados com JDBC.

## Objetivo da primeira versao

Nesta etapa, o sistema cadastra empregados em memoria. Para cada empregado, guardamos:

- nome;
- CPF;
- salario.

A interface permite apenas:

- inserir empregado;
- listar empregados cadastrados.

Ainda nao teremos edicao, exclusao ou banco de dados.

## Camadas do projeto

O programa foi separado em quatro partes:

- `modelo`: representa os dados do sistema.
- `persistencia`: guarda e recupera os dados.
- `aplicacao`: contem as regras do cadastro.
- `ui`: contem a interface grafica em Swing.

Essa divisao prepara o projeto para trocar a persistencia em memoria por JDBC sem reescrever a interface.

## Classe de modelo

A classe `Empregado` representa um empregado:

```java
public class Empregado {
    private String nome;
    private String cpf;
    private BigDecimal salario;
}
```

O salario usa `BigDecimal`, que e uma escolha melhor para valores monetarios do que `double`.

## Persistencia em memoria

A interface `EmpregadoRepository` define o que a aplicacao precisa da persistencia:

```java
void inserir(Empregado empregado);
List<Empregado> listar();
boolean existeCpf(String cpf);
```

A classe `EmpregadoRepositoryMemoria` implementa essas operacoes usando um `ArrayList`.

## Regras da aplicacao

A classe `EmpregadoService` recebe os dados vindos da interface e aplica as regras:

- nome, CPF e salario sao obrigatorios;
- salario nao pode ser negativo;
- CPF nao pode ser duplicado;
- salario precisa ser numerico.

Somente depois dessas validacoes o empregado e enviado para a persistencia.

## Interface Swing

A classe `EmpregadoFrame` monta uma tela simples com:

- campos para nome, CPF e salario;
- botao Salvar;
- tabela com todos os empregados cadastrados.

A interface nao manipula diretamente o `ArrayList`. Ela chama a classe de aplicacao, que chama a persistencia.

## Execucao

Para obter exatamente esta versao do projeto, mesmo depois que o repositorio tiver novas versoes, use:

```bash
git clone https://github.com/victoriocarvalho/programaJavaBD.git
cd programaJavaBD
git checkout v1-arraylist
```

Compile e execute a classe principal:

```bash
javac -d target/classes src/main/java/br/com/cadastroempregados/App.java src/main/java/br/com/cadastroempregados/modelo/Empregado.java src/main/java/br/com/cadastroempregados/persistencia/EmpregadoRepository.java src/main/java/br/com/cadastroempregados/persistencia/EmpregadoRepositoryMemoria.java src/main/java/br/com/cadastroempregados/aplicacao/EmpregadoService.java src/main/java/br/com/cadastroempregados/ui/EmpregadoFrame.java
java -cp target/classes br.com.cadastroempregados.App
```

No VS Code, tambem e possivel executar pelo menu `Terminal > Run Task...` e selecionar a tarefa `Executar`.

## Proximo passo

Na proxima etapa, poderemos melhorar a organizacao do tutorial e preparar a camada de persistencia para ser substituida por uma implementacao JDBC usando o banco de dados no Aiven.

## Segunda Versão: persistencia com PostgreSQL no Aiven

Agora estamos iniciando a segunda parte do projeto. O objetivo desta etapa e substituir a persistencia em memoria, feita com `ArrayList`, por persistencia em banco de dados PostgreSQL no Aiven.

A interface, o modelo e a classe de aplicacao continuam com a mesma responsabilidade. A mudanca principal acontece na camada de persistencia: em vez de guardar os empregados em uma lista, vamos inserir e consultar os dados na tabela `empregado` usando JDBC.

## Biblioteca necessaria

Como o projeto nao usa Maven nem Gradle, precisamos acrescentar manualmente apenas a biblioteca estritamente necessaria:

- driver JDBC do PostgreSQL.

Crie uma pasta chamada `lib` na raiz do projeto e coloque nela o arquivo `.jar` do driver PostgreSQL. Nesta versao, usamos este arquivo:

```text
https://jdbc.postgresql.org/download/postgresql-42.7.11.jar
```

Com isso, os comandos de compilacao e execucao passam a incluir `lib/*` no classpath.

## Configuracao do acesso ao banco

Nao vamos gravar URL, usuario ou senha diretamente no codigo. Essas informacoes devem ficar em um arquivo local chamado `.env`.

O projeto possui um arquivo modelo chamado `.env.example`:

```text
APP_DB_URL=jdbc:postgresql://HOST:PORTA/NOME_DO_BANCO?sslmode=require
APP_DB_USUARIO=USUARIO
APP_DB_SENHA=SENHA
```

Cada aluno deve copiar esse arquivo para um novo arquivo chamado `.env` e trocar os placeholders pelos dados reais do seu banco.

No Aiven, o service URI costuma vir neste formato:

```text
postgres://USUARIO:SENHA@HOST:PORTA/NOME_DO_BANCO?sslmode=require
```

Para usar com JDBC, separamos as informacoes e montamos a URL neste formato:

```text
APP_DB_URL=jdbc:postgresql://HOST:PORTA/NOME_DO_BANCO?sslmode=require
APP_DB_USUARIO=USUARIO
APP_DB_SENHA=SENHA
```

Exemplo generico do arquivo `.env`:

```text
APP_DB_URL=jdbc:postgresql://HOST:PORTA/NOME_DO_BANCO?sslmode=require
APP_DB_USUARIO=USUARIO
APP_DB_SENHA=SENHA
```

O arquivo `.env` possui dados sensiveis e nao deve ser enviado para o Git. Por isso ele fica listado no `.gitignore`. Somente o `.env.example`, com dados ficticios, deve ser versionado.

## Estabelecendo a conexao

A classe `ConexaoFactory`, na camada de persistencia, fica responsavel por ler o arquivo `.env` e abrir a conexao:

```java
public Connection conectar() {
    String url = lerConfiguracaoObrigatoria("APP_DB_URL");
    String usuario = lerConfiguracaoObrigatoria("APP_DB_USUARIO");
    String senha = lerConfiguracaoObrigatoria("APP_DB_SENHA");

    Class.forName("org.postgresql.Driver");
    return DriverManager.getConnection(url, usuario, senha);
}
```

Antes disso, a propria `ConexaoFactory` carrega o `.env`, lendo linhas no formato `CHAVE=VALOR`. Assim, as demais classes nao precisam saber de onde vieram a URL, o usuario e a senha.

## Listando empregados pelo banco

Na nova classe `EmpregadoRepositoryPostgres`, o metodo `listar` consulta a tabela `empregado`:

```java
String sql = "select nome, cpf, salario from empregado order by nome";

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
}
```

O resultado do banco e transformado em objetos `Empregado`, que sao devolvidos para a aplicacao.

## Inserindo empregados no banco

O metodo `inserir` usa `PreparedStatement` para enviar os dados ao PostgreSQL:

```java
String sql = "insert into empregado (nome, cpf, salario) values (?, ?, ?)";

try (Connection conexao = conexaoFactory.conectar();
     PreparedStatement comando = conexao.prepareStatement(sql)) {
    comando.setString(1, empregado.getNome());
    comando.setString(2, empregado.getCpf());
    comando.setBigDecimal(3, empregado.getSalario());
    comando.executeUpdate();
}
```

O uso de `PreparedStatement` evita montar SQL por concatenacao de texto e deixa o codigo mais seguro e organizado.

## Compilando e executando a segunda versão

Para obter exatamente esta segunda versao do projeto, mesmo depois que o repositorio tiver novas versoes, use:

```bash
git clone https://github.com/victoriocarvalho/programaJavaBD.git
cd programaJavaBD
git checkout v2-persistenciaBD
```

Depois de criar o arquivo `.env` a partir do `.env.example`, ajustando os valores das variaveis, compile e execute:

```powershell
javac -d target/classes -cp "lib/*" src/main/java/br/com/cadastroempregados/App.java src/main/java/br/com/cadastroempregados/modelo/Empregado.java src/main/java/br/com/cadastroempregados/persistencia/EmpregadoRepository.java src/main/java/br/com/cadastroempregados/persistencia/ConexaoFactory.java src/main/java/br/com/cadastroempregados/persistencia/EmpregadoRepositoryPostgres.java src/main/java/br/com/cadastroempregados/aplicacao/EmpregadoService.java src/main/java/br/com/cadastroempregados/ui/EmpregadoFrame.java

java -cp "target/classes;lib/*" br.com.cadastroempregados.App
```

No VS Code, tambem e possivel executar pelo menu `Terminal > Run Task...` e selecionar a tarefa `Executar`.
