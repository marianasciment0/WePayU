package br.ufal.ic.p2.wepayu.Exception;

public class AtributoNaoNuloException extends RuntimeException{
    public AtributoNaoNuloException(String atributo) {
        super(atributo + " nao pode ser nulo.");
    }
}
