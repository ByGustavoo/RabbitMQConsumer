package br.com.rabbitmqconsumer.consumer;

import br.com.rabbitmqconsumer.config.AbstractTest;
import br.com.rabbitmqconsumer.model.event.PedidoCriadoEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@SpringBootTest
class PedidoConsumerTest extends AbstractTest {

    @Autowired
    private PedidoConsumer pedidoConsumer;

    @Test
    void receberTest() {
        var pedidoCriadoEvent = new PedidoCriadoEvent(
                UUID.randomUUID(),
                "Ana Lima",
                "ana.lima@email.com",
                new BigDecimal("1200.00"),
                LocalDateTime.now());

        Assertions.assertDoesNotThrow(() -> pedidoConsumer.receber(pedidoCriadoEvent));
    }
}