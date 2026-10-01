<div align="center"> <br>
  <img align="center" alt="rabbitmqconsumer-rabbitmq" height="150" width="150" src="https://cdn.jsdelivr.net/gh/devicons/devicon@latest/icons/rabbitmq/rabbitmq-original.svg" />
</div>

<br>

<div align="center">
  Consumidor de mensagens em Spring Boot com RabbitMQ. Escuta a fila de pedidos alimentada pelo <a href="https://github.com/ByGustavoo/RabbitMQProducer">RabbitMQProducer</a>, converte cada mensagem JSON no evento PedidoCriado e processa o pedido, com retentativas, descarte de mensagens inválidas e logs em cada etapa.
</div>

<br> <br>

## 🚀 Ferramentas Utilizadas

* 📝 Log4j2

* 🔴 Lombok

* ☕️ Java 25

* 🧪 JUnit 5

* 🐇 RabbitMQ 4

* 🟢 Spring Boot 4.1.1

* 📨 Spring AMQP 4.1.1

* 🐘 Gradle 9.7.1 (Kotlin DSL)

<br>

## 🔎 Como Funciona

```
RabbitMQProducer ──► pedidos.exchange ──► pedidos.criados ──► PedidoConsumer ──► PedidoService
                     (routing key pedido.criado)
```

* **Escuta:** o `PedidoConsumer` usa `@RabbitListener` na fila `pedidos.criados` e recebe o `PedidoCriadoEvent` já convertido do JSON pelo `JacksonJsonMessageConverter`.

* **Processamento:** o `PedidoService` valida o pedido (e-mail presente e valor maior que zero) e simula o envio do e-mail de confirmação, registrando cada passo no log.

* **Topologia:** o `RabbitMQConfig` declara a mesma fila, exchange e binding do produtor. A declaração é idempotente, então qualquer um dos dois projetos pode subir primeiro.

* **Confirmação:** o ack é automático: a mensagem sai da fila quando o processamento termina sem erro.

* **Retentativas:** se o processamento falhar, a mensagem é tentada de novo até 3 vezes, com espera de 1 s, 2 s e 4 s.

* **Descarte:** esgotadas as retentativas, o `MessageRecoverer` registra um log de erro com o motivo e o corpo da mensagem e a rejeita sem recolocar na fila. Uma mensagem com JSON inválido segue o mesmo caminho.

* **Vazão:** cada consumidor busca até 10 mensagens por vez (`prefetch`), e o Spring sobe de 1 até 3 consumidores conforme a fila cresce.

<br>

## 📝 Logs

Configurados no `log4j2.xml`: console colorido em todos os perfis e arquivo diário em `/app/logs` no perfil `prod`.

| Momento | Nível | Mensagem |
|---|---|---|
| Mensagem chegou | `INFO` | `Mensagem recebida da fila!` |
| Início do processamento | `INFO` | `Processando o pedido...` |
| Pedido válido | `INFO` | `Enviando o e-mail de confirmação...` e `Pedido processado!` |
| Pedido inválido (a cada tentativa) | `WARN` | `Pedido sem e-mail do cliente!` ou `Pedido com valor inválido!` |
| Tentativas esgotadas | `ERROR` | `Tentativas esgotadas! Mensagem descartada...`, com a fila, o erro e o corpo |

Exemplo de um pedido processado:

```
23:15:38.069 [Container#0-1] INFO PedidoConsumer - Mensagem recebida da fila! - Id: 4adf7246-...
23:15:38.069 [Container#0-1] INFO PedidoService - Processando o pedido... - Id: 4adf7246-... - Cliente: Fernanda Rocha - Valor: 75.00
23:15:38.069 [Container#0-1] INFO PedidoService - Enviando o e-mail de confirmação... - Id: 4adf7246-... - E-mail: fernanda@email.com
23:15:38.069 [Container#0-1] INFO PedidoService - Pedido processado! - Id: 4adf7246-...
```

<br>

## ⚙️ Pré-requisitos

* JDK 25 instalada

* RabbitMQ acessível, como o do `docker-compose-rabbitmq.yml` do [RabbitMQProducer](https://github.com/ByGustavoo/RabbitMQProducer)

<br>

## 🔐 Variáveis de Ambiente

Todas são opcionais: sem elas, a aplicação usa os valores do RabbitMQ local do produtor.

| Variável | Descrição |
|---|---|
| `RABBITMQ_HOST` | Host do RabbitMQ (padrão `localhost`) |
| `RABBITMQ_PORT` | Porta AMQP (padrão `5672`) |
| `RABBITMQ_USER` | Usuário (padrão `rabbitmq`) |
| `RABBITMQ_PASSWORD` | Senha (padrão `rabbitmq`) |

<br>

## ▶️ Como Executar

🔹 RabbitMQ (na pasta do RabbitMQProducer)

```bash
# Sobe o RabbitMQ com o painel de gerenciamento (AMQP na 5672, painel na 15672)
docker compose -f docker-compose-rabbitmq.yml up -d
```

🔹 Consumidor

```bash
# Sobe o consumidor, que fica escutando a fila pedidos.criados
./gradlew bootRun
```

🔹 Produtor (na pasta do RabbitMQProducer)

```bash
# Sobe a API que publica os pedidos (porta 9019)
./gradlew bootRun --args="--spring.profiles.active=dev"

# Registra um pedido, que aparece no log do consumidor logo em seguida
curl -X POST http://localhost:9019/RabbitMQProducer/v1/pedidos \
  -H "Content-Type: application/json" \
  -d '{"cliente":"Maria Souza","email":"maria.souza@email.com","valor":249.90}'
```

O consumidor não tem porta HTTP: é um worker que só escuta a fila. As mensagens que chegaram antes
de ele subir ficam guardadas na fila durável e são processadas assim que ele se conecta.

<br>

## 🧪 Testes e Build

Os testes sobem o contexto completo contra o RabbitMQ real, então ele precisa estar no ar. O perfil
`test` escuta uma fila própria (`pedidos.criados.test`), separada da de desenvolvimento.

```bash
# Testes
./gradlew test

# Build sem testes
./gradlew clean build -x test
```

<br>

## 📁 Estrutura

```
src/main/java/br/com/rabbitmqconsumer
├── RabbitMQConsumerApplication.java   # Classe de inicialização
├── config                             # RabbitMQConfig (fila, exchange, binding, JSON e descarte) e FilaPedidosProperties
├── consumer                           # PedidoConsumer, o @RabbitListener da fila de pedidos
├── exceptions                         # PedidoInvalidoException
├── model/event                        # PedidoCriadoEvent, o corpo da mensagem
└── service/pedido                     # PedidoService, que valida e processa o pedido

src/main/resources
├── application.yaml                   # Conexão, prefetch, concorrência, retentativas e nomes da fila (padrão e test)
└── log4j2.xml                         # Configuração de logging
```

<br>

## ⚠️ Limitações

* Uma mensagem descartada some da fila: o log de erro guarda o corpo dela, mas não há dead letter queue. Adicionar uma exige declarar a fila com `x-dead-letter-exchange` nos dois projetos, porque o RabbitMQ recusa a mesma fila com argumentos diferentes.

* Um JSON inválido nunca vai dar certo, mas passa pelas mesmas retentativas antes do descarte.

<br>

## 🖥️ Desenvolvedor

### 🔵 LinkedIn: [Gustavo Correa](https://www.linkedin.com/in/gustavo-chauar-correa-946168269/)