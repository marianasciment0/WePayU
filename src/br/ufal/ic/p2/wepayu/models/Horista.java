package br.ufal.ic.p2.wepayu.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Horista extends Empregado {
    private double salarioPorHora;
    private List<CartaoPonto> cartoes;

    public Horista(String nome, String endereco, String salario) {
        super(nome, endereco, "horista");
        this.salarioPorHora = Double.parseDouble(salario.replace(",", "."));
        this.cartoes = new ArrayList<>();
    }

    public void addCartao(CartaoPonto cartao) {
        this.cartoes.add(cartao);
    }

    public List<CartaoPonto> getCartoes() {
        return cartoes;
    }

    @Override
    public double getSalario() {
        return salarioPorHora;
    }

    @Override
    public double getSalarioBruto(LocalDate data) {
        LocalDate inicio = data.minusDays(6);
        double horasNormais = 0;
        double horasExtras = 0;

        for (CartaoPonto cartao : cartoes) {
            LocalDate dataCartao = cartao.getData();
            if (!dataCartao.isBefore(inicio) && !dataCartao.isAfter(data)) {
                double horas = cartao.getHoras();
                if (horas <= 8) {
                    horasNormais += horas;
                } else {
                    horasNormais += 8;
                    horasExtras += (horas - 8);
                }
            }
        }

        return (horasNormais * salarioPorHora) + (horasExtras * salarioPorHora * 1.5);
    }

    @Override
    public double getSalarioLiquido(LocalDate data) {
        double bruto = getSalarioBruto(data);
        double descontos = getDescontos(data);
        return bruto - descontos;
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

    public double getHnSemanal(LocalDate inicio, LocalDate fim) {
        double horasNormais = 0;
        for (CartaoPonto cartao : cartoes) {
            LocalDate dataCartao = cartao.getData();
            if (!dataCartao.isBefore(inicio) && !dataCartao.isAfter(fim)) {
                double horas = cartao.getHoras();
                if (horas <= 8) {
                    horasNormais += horas;
                } else {
                    horasNormais += 8;
                }
            }
        }
        return horasNormais;
    }

    public double getHxSemanal(LocalDate inicio, LocalDate fim) {
        double horasExtras = 0;
        for (CartaoPonto cartao : cartoes) {
            LocalDate dataCartao = cartao.getData();
            if (!dataCartao.isBefore(inicio) && !dataCartao.isAfter(fim)) {
                double horas = cartao.getHoras();
                if (horas > 8) {
                    horasExtras += (horas - 8);
                }
            }
        }
        return horasExtras;
    }
}