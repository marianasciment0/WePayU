package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;
import java.util.Date;

public class Venda implements Serializable {
    private Date data;
    private double valor;

    // Construtor padrão para XMLEncoder
    public Venda() {
    }

    public Venda(Date data, double valor) {
        this.data = data;
        this.valor = valor;
    }

    // Getters e Setters
    public Date getData() { return data; }
    public void setData(Date data) { this.data = data; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }
}