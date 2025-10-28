package br.ufal.ic.p2.wepayu.Exception;

public class NaoBancoException extends RuntimeException {
    public NaoBancoException () {
        super("Empregado nao recebe em banco.");
    }
}
