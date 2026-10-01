package br.com.rabbitmqconsumer.service.pedido;

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
class PedidoServiceTest extends AbstractTest {

    @Autowired
    private PedidoService pedidoService;

    @Test
    void processarTest() {
        var pedidoCriadoEvent = new PedidoCriadoEvent(
                UUID.randomUUID(),
                "João Pereira",
                "joao.pereira@email.com",
                new BigDecimal("89.90"),
                LocalDateTime.now());

        Assertions.assertDoesNotThrow(() -> pedidoService.processar(pedidoCriadoEvent));
    }
}