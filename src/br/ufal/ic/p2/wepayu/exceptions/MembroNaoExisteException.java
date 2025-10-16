package br.ufal.ic.p2.wepayu.exceptions;

public class MembroNaoExisteException extends Exception {
    public MembroNaoExisteException() {
        super("Membro nao existe.");
    }

    public MembroNaoExisteException(String message) {
        super(message);
    }
}