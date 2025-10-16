package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;
import java.time.LocalDate;

public class CartaoPonto implements Serializable {
    private LocalDate data;
    private double horas;

    public CartaoPonto(LocalDate data, String horas) {
        this.data = data;
        this.horas = Double.parseDouble(horas.replace(",", "."));
    }

    public LocalDate getData() { return data; }
    public double getHoras() { return horas; }
}