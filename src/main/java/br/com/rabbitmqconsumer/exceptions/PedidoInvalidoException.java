package br.com.rabbitmqconsumer.exceptions;

public class PedidoInvalidoException extends RuntimeException {

    public PedidoInvalidoException(String mensagem) {
        super(mensagem);
    }
}