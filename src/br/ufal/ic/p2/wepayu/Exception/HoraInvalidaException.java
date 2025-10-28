package br.ufal.ic.p2.wepayu.Exception;

public class HoraInvalidaException extends RuntimeException {
    public HoraInvalidaException () {
        super("Horas devem ser positivas.");
    }
}