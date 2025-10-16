package br.ufal.ic.p2.wepayu.models;

import java.time.LocalDate;

public class Assalariado extends Empregado {
    private double salario;

    public Assalariado(String nome, String endereco, String salario) {
        super(nome, endereco, "assalariado");
        this.salario = Double.parseDouble(salario.replace(",", "."));
    }

    @Override
    public double getSalario() {
        return salario;
    }

    @Override
    public double getSalarioBruto(LocalDate data) {
        return salario;
    }

    @Override
    public double getSalarioLiquido(LocalDate data) {
        double descontos = getDescontos(data);
        return salario - descontos;
    }

    @Override
    public double getDescontos(LocalDate data) {
        double descontos = 0;
        if (isSindicalizado()) {
            descontos += getTaxaSindical();
            for (TaxaServico taxa : getTaxasServico()) {
                if (taxa.getData().isAfter(getUltimoPagamento()) &&
                        !taxa.getData().isAfter(data)) {
                    descontos += taxa.getValor();
                }
            }
        }
        return descontos;
    }
}