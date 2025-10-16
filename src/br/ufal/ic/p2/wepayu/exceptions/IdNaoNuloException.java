package br.ufal.ic.p2.wepayu.exceptions;

public class IdNaoNuloException extends Exception {
    public IdNaoNuloException() {
        super("Identificacao do sindicato nao pode ser nula.");
    }

    public IdNaoNuloException(String message) {
        super(message);
    }
}