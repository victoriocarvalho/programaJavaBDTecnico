# Cadastro de Empregados com Java, JDBC e PostgreSQL

Este tutorial apresenta um programa Java para cadastro de empregados usando interface por linha de comando e persistencia em banco de dados PostgreSQL com JDBC.

O sistema permite:

- listar empregados cadastrados;
- cadastrar um novo empregado;
- validar os dados antes de salvar;
- gravar e consultar os dados em uma tabela no PostgreSQL.

Ao final, tambem veremos como trocar a interface de linha de comando por uma interface grafica simples em Swing.

## Organizacao do projeto

O projeto foi dividido em camadas para separar responsabilidades:

- `modelo`: representa os dados do sistema.
- `persistencia`: cuida da conexao e do acesso ao banco.
- `aplicacao`: contem as regras de negocio.
- `ui`: contem as interfaces do usuario.

Essa organizacao evita colocar tudo dentro da classe principal. A interface nao acessa o banco diretamente; ela conversa com a camada de aplicacao, que por sua vez usa a camada de persistencia.

## Modelo de dados

A classe `Empregado` representa um empregado do sistema:

```java
public class Empregado {
    private String nome;
    private String cpf;
    private BigDecimal salario;
}
```

O salario usa `BigDecimal`, que e mais adequado para valores monetarios do que `double`.

No banco de dados, criaremos uma tabela equivalente:

```sql
create table empregado (
    id serial primary key,
    nome varchar(100) not null,
    cpf varchar(20) not null unique,
    salario numeric(10, 2) not null
);
```

## Interface de persistencia

A interface `EmpregadoRepository` define as operacoes que a aplicacao precisa:

```java
void inserir(Empregado empregado);
List<Empregado> listar();
boolean existeCpf(String cpf);
```

A camada de aplicacao depende dessa interface, e nao diretamente de uma classe concreta. Neste projeto, a implementacao usada e `EmpregadoRepositoryPostgres`, que salva e consulta os dados no PostgreSQL usando JDBC.

## Configuracao do banco

As informacoes de conexao nao ficam escritas diretamente no codigo. Elas devem ser colocadas em um arquivo local chamado `.env`, criado a partir do arquivo `.env.example`.

O arquivo `.env.example` possui este formato:

```text
APP_DB_URL=jdbc:postgresql://HOST:PORTA/NOME_DO_BANCO?sslmode=require
APP_DB_USUARIO=USUARIO
APP_DB_SENHA=SENHA
```

Crie uma copia chamada `.env` e substitua os valores pelos dados reais do seu banco.

No Aiven, a URI do servico costuma aparecer neste formato:

```text
postgres://USUARIO:SENHA@HOST:PORTA/NOME_DO_BANCO?sslmode=require
```

Para usar com JDBC, separe as partes e monte o `.env` assim:

```text
APP_DB_URL=jdbc:postgresql://HOST:PORTA/NOME_DO_BANCO?sslmode=require
APP_DB_USUARIO=USUARIO
APP_DB_SENHA=SENHA
```

O arquivo `.env` contem dados sensiveis e nao deve ser enviado para o Git. Por isso ele esta listado no `.gitignore`. Apenas o `.env.example`, com valores ficticios, deve ser versionado.

## Conexao com JDBC

A classe `ConexaoFactory` le o arquivo `.env` e abre a conexao com o banco:

```java
public Connection conectar() {
    String url = lerConfiguracaoObrigatoria("APP_DB_URL");
    String usuario = lerConfiguracaoObrigatoria("APP_DB_USUARIO");
    String senha = lerConfiguracaoObrigatoria("APP_DB_SENHA");

    Class.forName("org.postgresql.Driver");
    return DriverManager.getConnection(url, usuario, senha);
}
```

Com isso, as outras classes nao precisam saber como as configuracoes foram carregadas. Elas apenas pedem uma conexao quando precisam executar comandos SQL.

## Inserindo empregados

Na classe `EmpregadoRepositoryPostgres`, o metodo `inserir` grava um empregado na tabela:

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

O `PreparedStatement` evita montar SQL por concatenacao de texto e ajuda a deixar o codigo mais seguro e organizado.

## Listando empregados

O metodo `listar` consulta os empregados cadastrados:

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

Cada linha retornada pelo banco e transformada em um objeto `Empregado`.

## Regras da aplicacao

A classe `EmpregadoService` recebe os dados vindos da interface e aplica as regras antes de salvar:

- nome, CPF e salario sao obrigatorios;
- salario precisa ser numerico;
- salario nao pode ser negativo;
- CPF nao pode ser duplicado.

Somente depois dessas validacoes o empregado e enviado para o repositorio.

## Interface por linha de comando

A classe `EmpregadoConsole` e a interface principal deste projeto. Ela exibe um menu simples:

```text
=== Cadastro de Empregados ===
1 - Listar empregados
2 - Cadastrar novo empregado
0 - Sair
Escolha uma opcao:
```

Quando o usuario escolhe cadastrar, a interface pede nome, CPF e salario. Depois ela chama o `EmpregadoService`, que valida os dados e salva no banco.

Quando o usuario escolhe listar, a interface chama o service, recebe os empregados cadastrados e mostra as informacoes no terminal.

## Classe principal

A classe `App` monta os objetos do sistema e inicia a interface de linha de comando:

```java
public class App {
    public static void main(String[] args) {
        EmpregadoRepositoryPostgres repository = new EmpregadoRepositoryPostgres(new ConexaoFactory());
        EmpregadoService service = new EmpregadoService(repository);
        EmpregadoConsole console = new EmpregadoConsole(service);
        console.iniciar();
    }
}
```

Essa montagem conecta as camadas:

- `ConexaoFactory` abre conexoes com o banco.
- `EmpregadoRepositoryPostgres` executa SQL.
- `EmpregadoService` aplica as regras.
- `EmpregadoConsole` conversa com o usuario pelo terminal.

## Compilando e executando

Como o projeto nao usa Maven nem Gradle, o driver JDBC do PostgreSQL fica na pasta `lib`.

Depois de criar o arquivo `.env` e a tabela `empregado` no banco, compile o projeto:

```powershell
javac -d target/classes -cp "lib/*" src/main/java/br/com/cadastroempregados/App.java src/main/java/br/com/cadastroempregados/modelo/Empregado.java src/main/java/br/com/cadastroempregados/persistencia/EmpregadoRepository.java src/main/java/br/com/cadastroempregados/persistencia/ConexaoFactory.java src/main/java/br/com/cadastroempregados/persistencia/EmpregadoRepositoryPostgres.java src/main/java/br/com/cadastroempregados/aplicacao/EmpregadoService.java src/main/java/br/com/cadastroempregados/ui/EmpregadoConsole.java src/main/java/br/com/cadastroempregados/ui/EmpregadoFrame.java
```

Execute a aplicacao:

```powershell
java -cp "target/classes;lib/*" br.com.cadastroempregados.App
```

No Linux ou macOS, o separador do classpath e `:` em vez de `;`:

```bash
java -cp "target/classes:lib/*" br.com.cadastroempregados.App
```

## Usando a interface Swing

O projeto tambem possui uma interface grafica simples em Swing, implementada pela classe `EmpregadoFrame`.

Para usar Swing no lugar da linha de comando, altere a classe `App`.

Troque:

```java
EmpregadoConsole console = new EmpregadoConsole(service);
console.iniciar();
```

Por:

```java
EmpregadoFrame janela = new EmpregadoFrame(service);
janela.setVisible(true);
```

Tambem confira os imports da classe `App`: se a interface de console nao for usada, o import de `EmpregadoConsole` pode ser removido. Se `EmpregadoFrame` ainda nao estiver importado, adicione:

```java
import br.com.cadastroempregados.ui.EmpregadoFrame;
```

Depois disso, compile e execute novamente. A aplicacao usara o mesmo banco de dados, as mesmas regras e a mesma camada de persistencia; apenas a interface do usuario sera diferente.
