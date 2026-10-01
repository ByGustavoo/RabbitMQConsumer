# RabbitMQConsumer

## O que é

Consumidor de mensagens de estudo em Spring Boot: escuta a fila `pedidos.criados`, alimentada pelo
`RabbitMQProducer` (projeto irmão em `../RabbitMQProducer`), converte o JSON em `PedidoCriadoEvent`,
valida e simula o processamento do pedido. Não tem banco nem porta HTTP: é um worker.

## Tipo

Worker de mensageria de estudo, sem consumidores externos. Release: commit numerado direto na
`main`, no formato `<n> - <descrição>`.

## Stack

- Java 25, Spring Boot 4.1.1, Spring AMQP 4.1.1, Gradle 9.7.1 (Kotlin DSL)
- `spring-boot-starter-jackson` declarado à parte, porque sem o starter web o Jackson não vem junto
- Log4j2 no lugar do Logback e Lombok

## Estrutura

- `config/` — `RabbitMQConfig` (fila, exchange, binding, conversor JSON e `MessageRecoverer`) e `FilaPedidosProperties` (`rabbitmq.pedidos.*`)
- `consumer/` — `PedidoConsumer`, o `@RabbitListener`
- `service/pedido/` — `PedidoService`, a validação e o processamento
- `model/event/` — `PedidoCriadoEvent`, cópia do record do produtor
- `exceptions/` — `PedidoInvalidoException`

## Comandos

| Objetivo | Comando |
|---|---|
| Subir o RabbitMQ | `docker compose -f docker-compose-rabbitmq.yml up -d`, na pasta do RabbitMQProducer |
| Rodar | `./gradlew bootRun` |
| Testar | `./gradlew test` (com o RabbitMQ no ar) |
| Build | `./gradlew clean build -x test` |

## Convenções

- Segue a skill `java-clean-architecture`: sem comentários, campos do menor para o maior, mensagens
  terminando em `!`, imports no padrão do IntelliJ.
- **Contrato com o produtor:** o `PedidoCriadoEvent` e os nomes em `rabbitmq.pedidos.*` precisam
  ficar iguais aos do RabbitMQProducer. A fila, a exchange e o binding também são declarados com os
  mesmos argumentos: o RabbitMQ recusa (`PRECONDITION_FAILED`) uma fila redeclarada com argumentos
  diferentes. Mudança em um lado exige a mesma mudança no outro.
- O conversor infere o tipo pelo parâmetro do listener; o cabeçalho `__TypeId__`, que aponta para o
  pacote do produtor, não é usado.
- Os testes seguem o modelo do PrismaAPI sem a parte de banco: `@SpringBootTest` contra o RabbitMQ
  real, sem mocks, com a fila `pedidos.criados.test` no perfil `test`.

## Pegadinhas

- No Spring Boot 4, `spring.rabbitmq.listener.simple.retry.max-attempts` foi removido e é ignorado em
  silêncio; a propriedade é `max-retries` (retentativas além da primeira tentativa).
- Sem dead letter queue, mensagem rejeitada é perdida; o corpo fica só no log de erro.
- O aviso `Error opening zip file ... byte-buddy-agent` no `./gradlew test` vem do caminho do usuário
  com acento e não afeta os testes.