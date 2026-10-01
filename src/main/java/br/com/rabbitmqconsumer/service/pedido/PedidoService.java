package br.com.rabbitmqconsumer.service.pedido;

import br.com.rabbitmqconsumer.exceptions.PedidoInvalidoException;
import br.com.rabbitmqconsumer.model.event.PedidoCriadoEvent;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class PedidoService {

    public void processar(PedidoCriadoEvent pedidoCriadoEvent) {
        log.info("Processando o pedido... - Id: {} - Cliente: {} - Valor: {}",
                pedidoCriadoEvent.id(),
                pedidoCriadoEvent.cliente(),
                pedidoCriadoEvent.valor());

        validar(pedidoCriadoEvent);

        log.info("Enviando o e-mail de confirmação... - Id: {} - E-mail: {}", pedidoCriadoEvent.id(), pedidoCriadoEvent.email());
        log.info("Pedido processado! - Id: {}", pedidoCriadoEvent.id());
    }

    private void validar(PedidoCriadoEvent pedidoCriadoEvent) {
        if (pedidoCriadoEvent.email() == null || pedidoCriadoEvent.email().isBlank()) {
            log.warn("Pedido sem e-mail do cliente! - Id: {}", pedidoCriadoEvent.id());
            throw new PedidoInvalidoException("O pedido chegou sem o e-mail do cliente!");
        }

        if (pedidoCriadoEvent.valor() == null || pedidoCriadoEvent.valor().signum() <= 0) {
            log.warn("Pedido com valor inválido! - Id: {} - Valor: {}", pedidoCriadoEvent.id(), pedidoCriadoEvent.valor());
            throw new PedidoInvalidoException("O pedido chegou com um valor inválido!");
        }
    }
}