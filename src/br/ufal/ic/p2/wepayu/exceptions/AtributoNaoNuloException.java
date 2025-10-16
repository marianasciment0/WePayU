package br.ufal.ic.p2.wepayu.exceptions;

public class AtributoNaoNuloException extends Exception {
    public AtributoNaoNuloException() {
        super("Atributo nao pode ser nulo.");
    }

    public AtributoNaoNuloException(String message) {
        super(message);
    }
}