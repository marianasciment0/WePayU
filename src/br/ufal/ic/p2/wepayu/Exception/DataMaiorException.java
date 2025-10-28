package br.ufal.ic.p2.wepayu.Exception;

public class DataMaiorException extends RuntimeException {
    public DataMaiorException () {
        super("Data inicial nao pode ser posterior aa data final.");
    }
}
