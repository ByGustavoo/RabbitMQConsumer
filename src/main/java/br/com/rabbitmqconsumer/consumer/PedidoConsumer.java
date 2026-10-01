package br.com.rabbitmqconsumer.consumer;

import br.com.rabbitmqconsumer.model.event.PedidoCriadoEvent;
import br.com.rabbitmqconsumer.service.pedido.PedidoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class PedidoConsumer {

    private final PedidoService pedidoService;

    @RabbitListener(queues = "${rabbitmq.pedidos.fila}")
    public void receber(PedidoCriadoEvent pedidoCriadoEvent) {
        log.info("Mensagem recebida da fila! - Id: {}", pedidoCriadoEvent.id());

        pedidoService.processar(pedidoCriadoEvent);
    }
}