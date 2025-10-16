package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;
import java.time.LocalDate;

public class TaxaServico implements Serializable {
    private LocalDate data;
    private double valor;

    public TaxaServico(LocalDate data, String valor) {
        this.data = data;
        this.valor = Double.parseDouble(valor.replace(",", "."));
    }

    public LocalDate getData() { return data; }
    public double getValor() { return valor; }
}