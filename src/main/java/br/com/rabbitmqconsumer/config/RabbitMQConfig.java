package br.com.rabbitmqconsumer.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.NestedExceptionUtils;

import java.nio.charset.StandardCharsets;

@Log4j2
@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue filaPedidos(FilaPedidosProperties filaPedidosProperties) {
        return QueueBuilder.durable(filaPedidosProperties.fila())
                .deadLetterExchange(filaPedidosProperties.exchangeDlq())
                .deadLetterRoutingKey(filaPedidosProperties.filaDlq())
                .build();
    }

    @Bean
    public DirectExchange exchangePedidos(FilaPedidosProperties filaPedidosProperties) {
        return new DirectExchange(filaPedidosProperties.exchange());
    }

    @Bean
    public Binding bindingPedidos(Queue filaPedidos, DirectExchange exchangePedidos, FilaPedidosProperties filaPedidosProperties) {
        return BindingBuilder.bind(filaPedidos)
                .to(exchangePedidos)
                .with(filaPedidosProperties.routingKey());
    }

    @Bean
    public Queue filaPedidosDlq(FilaPedidosProperties filaPedidosProperties) {
        return QueueBuilder.durable(filaPedidosProperties.filaDlq()).build();
    }

    @Bean
    public DirectExchange exchangePedidosDlq(FilaPedidosProperties filaPedidosProperties) {
        return new DirectExchange(filaPedidosProperties.exchangeDlq());
    }

    @Bean
    public Binding bindingPedidosDlq(Queue filaPedidosDlq, DirectExchange exchangePedidosDlq, FilaPedidosProperties filaPedidosProperties) {
        return BindingBuilder.bind(filaPedidosDlq)
                .to(exchangePedidosDlq)
                .with(filaPedidosProperties.filaDlq());
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public MessageRecoverer messageRecoverer() {
        return (message, cause) -> {
            log.error("Tentativas esgotadas! Enviando a mensagem para a DLQ... - Fila: {} - Erro: {} - Corpo: {}",
                    message.getMessageProperties().getConsumerQueue(),
                    NestedExceptionUtils.getMostSpecificCause(cause).getMessage(),
                    new String(message.getBody(), StandardCharsets.UTF_8));

            throw new AmqpRejectAndDontRequeueException("Tentativas esgotadas!", cause);
        };
    }
}