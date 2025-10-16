package br.ufal.ic.p2.wepayu.exceptions;

public class NaoSindicalizadoException extends Exception {
    public NaoSindicalizadoException() {
        super("Empregado nao eh sindicalizado.");
    }

    public NaoSindicalizadoException(String message) {
        super(message);
    }
}