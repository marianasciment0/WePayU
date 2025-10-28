package br.ufal.ic.p2.wepayu.Exception;

public class IdNaoNuloException extends RuntimeException {
    public IdNaoNuloException () {
        super("Identificacao do membro nao pode ser nula.");
    }
}
