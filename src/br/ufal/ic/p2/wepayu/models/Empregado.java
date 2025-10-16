package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public abstract class Empregado implements Serializable {
    private static int nextId = 1;
    private int id;
    private String nome;
    private String endereco;
    private String tipo;
    private boolean sindicalizado;
    private String idSindicato;
    private double taxaSindical;
    private List<TaxaServico> taxasServico;
    private String metodoPagamento;
    private String banco;
    private String agencia;
    private String contaCorrente;
    private LocalDate ultimoPagamento;
    private String agendaPagamento;

    public Empregado(String nome, String endereco, String tipo) {
        this.id = nextId++;
        this.nome = nome;
        this.endereco = endereco;
        this.tipo = tipo;
        this.sindicalizado = false;
        this.taxasServico = new ArrayList<>();
        this.metodoPagamento = "emMaos";
        this.agendaPagamento = "mensal $";
    }

    // Getters e Setters
    public int getId() { return id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public boolean isSindicalizado() { return sindicalizado; }
    public String getIdSindicato() { return idSindicato; }
    public double getTaxaSindical() { return taxaSindical; }

    public List<TaxaServico> getTaxasServico() { return taxasServico; }

    public String getMetodoPagamento() { return metodoPagamento; }
    public String getBanco() { return banco; }
    public String getAgencia() { return agencia; }
    public String getContaCorrente() { return contaCorrente; }

    public LocalDate getUltimoPagamento() { return ultimoPagamento; }
    public void setUltimoPagamento(LocalDate ultimoPagamento) { this.ultimoPagamento = ultimoPagamento; }

    public String getAgendaPagamento() { return agendaPagamento; }
    public void setAgendaPagamento(String agendaPagamento) { this.agendaPagamento = agendaPagamento; }

    public void setSindicalizado(boolean sindicalizado, String idSindicato, double taxaSindical) {
        this.sindicalizado = sindicalizado;
        this.idSindicato = idSindicato;
        this.taxaSindical = taxaSindical;
    }

    public void setMetodoPagamento(String metodo, String banco, String agencia, String contaCorrente) {
        this.metodoPagamento = metodo;
        this.banco = banco;
        this.agencia = agencia;
        this.contaCorrente = contaCorrente;
    }

    public void addTaxaServico(TaxaServico taxa) {
        this.taxasServico.add(taxa);
    }

    // Métodos abstratos que devem ser implementados pelas subclasses
    public abstract double getSalario();
    public abstract double getSalarioBruto(LocalDate data);
    public abstract double getSalarioLiquido(LocalDate data);
    public abstract double getDescontos(LocalDate data);
}