package br.ufal.ic.p2.wepayu.exceptions;

public class ValorNaoNuloException extends Exception {
    public ValorNaoNuloException() {
        super("Valor nao pode ser nulo.");
    }

    public ValorNaoNuloException(String message) {
        super(message);
    }
}