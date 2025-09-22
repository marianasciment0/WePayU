package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;
import java.util.Date;

public class TaxaServico implements Serializable {
    private Date data;
    private double valor;

    public TaxaServico() {
    }

    public TaxaServico(Date data, double valor) {
        this.data = data;
        this.valor = valor;
    }

    public Date getData() { return data; }
    public void setData(Date data) { this.data = data; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }
}