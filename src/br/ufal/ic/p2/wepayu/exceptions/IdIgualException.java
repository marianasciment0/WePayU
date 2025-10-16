package br.ufal.ic.p2.wepayu.exceptions;

public class IdIgualException extends Exception {
    public IdIgualException() {
        super("Ha outro empregado com essa identificacao de sindicato.");
    }

    public IdIgualException(String message) {
        super(message);
    }
}