package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;
import java.util.Date;

public class CartaoPonto implements Serializable {
    private Date data;
    private double horas;

    public CartaoPonto() {
    }

    public CartaoPonto(Date data, double horas) {
        this.data = data;
        this.horas = horas;
    }

    public Date getData() { return data; }
    public void setData(Date data) { this.data = data; }

    public double getHoras() { return horas; }
    public void setHoras(double horas) { this.horas = horas; }
}