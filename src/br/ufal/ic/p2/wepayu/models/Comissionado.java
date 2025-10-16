package br.ufal.ic.p2.wepayu.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Comissionado extends Empregado {
    private double salarioBase;
    private double comissao;
    private List<Venda> vendas;

    public Comissionado(String nome, String endereco, String salario, String comissao) {
        super(nome, endereco, "comissionado");
        this.salarioBase = Double.parseDouble(salario.replace(",", "."));
        this.comissao = Double.parseDouble(comissao.replace(",", ".")) / 100;
        this.vendas = new ArrayList<>();
    }

    public void setVendas(LocalDate data, double valor) {
        this.vendas.add(new Venda(data, valor));
    }

    public List<Venda> getVendas() {
        return vendas;
    }

    @Override
    public double getSalario() {
        return salarioBase;
    }

    public double getComissao() {
        return comissao;
    }

    public double getSalario(LocalDate data) {
        return getFixo(data) + getComissaoTotal(data);
    }

    public double getFixo(LocalDate data) {
        return salarioBase;
    }

    public double getTotalVendas(LocalDate data) {
        double total = 0;
        LocalDate inicio = data.minusDays(30);
        for (Venda venda : vendas) {
            if (!venda.getData().isBefore(inicio) && !venda.getData().isAfter(data)) {
                total += venda.getValor();
            }
        }
        return total;
    }

    public double getComissaoTotal(LocalDate data) {
        return getTotalVendas(data) * comissao;
    }

    @Override
    public double getSalarioBruto(LocalDate data) {
        return getSalario(data);
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
}