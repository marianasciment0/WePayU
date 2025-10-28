package br.ufal.ic.p2.wepayu.Exception;

public class IdRepetidoException extends RuntimeException {
    public IdRepetidoException () {
        super("Ha outro empregado com esta identificacao de sindicato");
    }
}
