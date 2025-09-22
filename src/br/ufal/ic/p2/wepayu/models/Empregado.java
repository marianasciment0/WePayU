package br.ufal.ic.p2.wepayu.models;

import java.beans.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Empregado implements Serializable {
    private String nome;
    private String endereco;
    private String tipo;
    private Double salario;
    private Boolean sindicalizado;
    private String idSindicato;
    private Double taxaSindical;
    private List<TaxaServico> taxasServico = new ArrayList<>();
    private Double comissao;
    private List<CartaoPonto> cartoesPonto; // Adicionado
    private List<Venda> vendas;
    private String metodoPagamento;
    private String banco;
    private String agencia;
    private String contaCorrente;
    private Date dataUltimoPagamento;
    private Date dataContrato;
    private String agendaPagamento;

    public Empregado() {
        this.sindicalizado = false;
        this.cartoesPonto = new ArrayList<>();
        this.vendas = new ArrayList<>();
        this.taxasServico = new ArrayList<>();
        this.metodoPagamento = "emMaos";
    }

    public Empregado(String nome, String endereco, String tipo, Double salario) {
        this.nome = nome;
        this.endereco = endereco;
        this.tipo = tipo;
        this.salario = salario;
        this.sindicalizado = false;
        this.cartoesPonto = new ArrayList<>();
        this.vendas = new ArrayList<>();
        this.taxasServico = new ArrayList<>();
        this.metodoPagamento = "emMaos";
    }


    public Empregado(String nome, String endereco, String tipo, Double salario, Double comissao) {
        this.nome = nome;
        this.endereco = endereco;
        this.tipo = tipo;
        this.salario = salario;
        this.comissao = comissao;
        this.sindicalizado = false;
        this.cartoesPonto = new ArrayList<>();
        this.vendas = new ArrayList<>();
        this.taxasServico = new ArrayList<>();
        this.metodoPagamento = "emMaos";
    }

    public Date getDataUltimoPagamento() {
        return dataUltimoPagamento;
    }

    public void setDataUltimoPagamento(Date dataUltimoPagamento) {
        this.dataUltimoPagamento = dataUltimoPagamento;
    }

    public Date getDataContrato() {
        return dataContrato;
    }

    public void setDataContrato(Date dataContrato) {
        this.dataContrato = dataContrato;
    }

    // Adicione os getters e setters para cartoesPonto
    public List<CartaoPonto> getCartoesPonto() { return cartoesPonto; }
    public void setCartoesPonto(List<CartaoPonto> cartoesPonto) { this.cartoesPonto = cartoesPonto; }
    public void addCartaoPonto(CartaoPonto cartao) { cartoesPonto.add(cartao); }

    public List<Venda> getVendas() { return vendas; }
    public void setVendas(List<Venda> vendas) { this.vendas = vendas; }
    public void addVenda(Venda venda) { vendas.add(venda); }

    public Boolean getSindicalizado() { return sindicalizado; }
    public void setSindicalizado(Boolean sindicalizado) { this.sindicalizado = sindicalizado; }

    public String getIdSindicato() { return idSindicato; }
    public void setIdSindicato(String idSindicato) { this.idSindicato = idSindicato; }

    public Double getTaxaSindical() { return taxaSindical; }
    public void setTaxaSindical(Double taxaSindical) { this.taxaSindical = taxaSindical; }

    public List<TaxaServico> getTaxasServico() {
        if (taxasServico == null) {
            taxasServico = new ArrayList<>();
        }
        return taxasServico;
    }

    public String getAgendaPagamento() {
        return agendaPagamento;
    }

    public void setAgendaPagamento(String agendaPagamento) {
        this.agendaPagamento = agendaPagamento;
    }

    public void setTaxasServico(List<TaxaServico> taxasServico) { this.taxasServico = taxasServico; }
    public void addTaxaServico(TaxaServico taxa) { taxasServico.add(taxa); }

    public String getMetodoPagamento() { return metodoPagamento; }
    public void setMetodoPagamento(String metodoPagamento) { this.metodoPagamento = metodoPagamento; }

    public String getBanco() { return banco; }
    public void setBanco(String banco) { this.banco = banco; }

    public String getAgencia() { return agencia; }
    public void setAgencia(String agencia) { this.agencia = agencia; }

    public String getContaCorrente() { return contaCorrente; }
    public void setContaCorrente(String contaCorrente) { this.contaCorrente = contaCorrente; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Double getSalario() { return salario; }
    public void setSalario(Double salario) { this.salario = salario; }

    public Double getComissao() { return comissao; }
    public void setComissao(Double comissao) { this.comissao = comissao; }

    @Transient
    public boolean isComissionado() {
        return "comissionado".equals(tipo);
    }
}