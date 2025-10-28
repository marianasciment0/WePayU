package br.ufal.ic.p2.wepayu.Exception;

public class MembroInexistenteException extends RuntimeException {
    public MembroInexistenteException () {
        super("Membro nao existe.");
    }
}