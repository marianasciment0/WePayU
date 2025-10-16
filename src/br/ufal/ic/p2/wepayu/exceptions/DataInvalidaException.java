package br.ufal.ic.p2.wepayu.exceptions;

public class DataInvalidaException extends Exception {
    public DataInvalidaException() {
        super("Data invalida.");
    }

    public DataInvalidaException(String message) {
        super(message);
    }
}