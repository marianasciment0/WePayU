package br.ufal.ic.p2.wepayu.Exception;

public class DataInvalidaException extends Exception {
    public DataInvalidaException (String data) {
        super(data.equals("data") ? "Data invalida." : "Data " + data + " invalida.");
    }
}