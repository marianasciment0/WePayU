package br.ufal.ic.p2.wepayu.services;

import br.ufal.ic.p2.wepayu.models.*;
import br.ufal.ic.p2.wepayu.exceptions.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class EmpregadoService {

    public static Empregado criarEmpregado(String nome, String endereco, String tipo, String salario) {
        switch (tipo.toLowerCase()) {
            case "assalariado":
                return new Assalariado(nome, endereco, salario);
            case "horista":
                return new Horista(nome, endereco, salario);
            default:
                throw new IllegalArgumentException("Tipo de empregado invalido: " + tipo);
        }
    }

    public static Empregado criarEmpregado(String nome, String endereco, String tipo, String salario, String comissao) {
        if ("comissionado".equalsIgnoreCase(tipo)) {
            return new Comissionado(nome, endereco, salario, comissao);
        } else {
            throw new IllegalArgumentException("Tipo de empregado invalido para comissao: " + tipo);
        }
    }

    public static Empregado getEmpregadoPorId(String id, List<Empregado> empregados)
            throws EmpregadoNaoExisteException {
        try {
            int empId = Integer.parseInt(id);
            for (Empregado e : empregados) {
                if (e.getId() == empId) {
                    return e;
                }
            }
        } catch (NumberFormatException e) {
            // ID inválido
        }
        throw new EmpregadoNaoExisteException();
    }

    public static String getAtributoEmpregado(Empregado e, String atributo,
                                              Map<String, String> dadosSindicais,
                                              List<Empregado> empregados)
            throws NaoSindicalizadoException {
        switch (atributo.toLowerCase()) {
            case "nome":
                return e.getNome();
            case "endereco":
                return e.getEndereco();
            case "tipo":
                return e.getTipo();
            case "salario":
                return ConversorUtils.converteSalario(e.getSalario());
            case "sindicalizado":
                return e.isSindicalizado() ? "true" : "false";
            case "idsindicato":
                if (!e.isSindicalizado()) throw new NaoSindicalizadoException();
                return e.getIdSindicato();
            case "taxasindical":
                if (!e.isSindicalizado()) throw new NaoSindicalizadoException();
                return ConversorUtils.converteSalario(e.getTaxaSindical());
            case "metodopagamento":
                return e.getMetodoPagamento();
            case "banco":
                return e.getBanco();
            case "agencia":
                return e.getAgencia();
            case "contacorrente":
                return e.getContaCorrente();
            case "agendapagamento":
                return e.getAgendaPagamento();
            case "comissao":
                if (e instanceof Comissionado) {
                    return ConversorUtils.converteSalario(((Comissionado) e).getComissao() * 100);
                }
                throw new IllegalArgumentException("Empregado nao eh comissionado.");
            default:
                throw new IllegalArgumentException("Atributo invalido: " + atributo);
        }
    }

    public static Empregado alteraEmpregado(String emp, String atributo, String valor,
                                            Empregado e, List<Empregado> empregados,
                                            List<String> tiposAgenda) {
        switch (atributo.toLowerCase()) {
            case "nome":
                e.setNome(valor);
                break;
            case "endereco":
                e.setEndereco(valor);
                break;
            case "tipo":
                return alterarTipoEmpregado(e, valor);
            case "salario":
                // A alteração de salário é tratada nas subclasses
                break;
            case "agendapagamento":
                if (AgendaUtils.validaAgenda(valor, tiposAgenda)) {
                    e.setAgendaPagamento(valor);
                } else {
                    throw new IllegalArgumentException("Agenda de pagamento invalida: " + valor);
                }
                break;
            case "metodopagamento":
                e.setMetodoPagamento(valor, null, null, null);
                break;
            default:
                throw new IllegalArgumentException("Atributo invalido para alteracao: " + atributo);
        }
        return e;
    }

    private static Empregado alterarTipoEmpregado(Empregado e, String novoTipo) {
        String nome = e.getNome();
        String endereco = e.getEndereco();
        String salario = ConversorUtils.converteSalario(e.getSalario());

        switch (novoTipo.toLowerCase()) {
            case "assalariado":
                return new Assalariado(nome, endereco, salario);
            case "horista":
                return new Horista(nome, endereco, salario);
            case "comissionado":
                return new Comissionado(nome, endereco, salario, "0");
            default:
                throw new IllegalArgumentException("Tipo de empregado invalido: " + novoTipo);
        }
    }

    public static Empregado alteraEmpregado(String emp, String atributo, String valor,
                                            String comissao, Empregado e) {
        if ("tipo".equalsIgnoreCase(atributo)) {
            String nome = e.getNome();
            String endereco = e.getEndereco();
            String salario = ConversorUtils.converteSalario(e.getSalario());

            if ("comissionado".equalsIgnoreCase(valor)) {
                return new Comissionado(nome, endereco, salario, comissao);
            } else if ("horista".equalsIgnoreCase(valor)) {
                return new Horista(nome, endereco, salario);
            }
        }
        return e;
    }

    public static boolean recebeHoje(LocalDate data, Empregado e) {
        // Implementação simplificada - em produção seria baseada na agenda de pagamento
        String agenda = e.getAgendaPagamento();

        if (agenda.startsWith("mensal")) {
            // Pagamento mensal no último dia útil
            return data.getDayOfMonth() == data.lengthOfMonth();
        } else if (agenda.startsWith("semanal")) {
            // Pagamento semanal - simplificado: toda sexta
            return data.getDayOfWeek().getValue() == 5; // 5 = sexta-feira
        }

        return false;
    }

    public static void mudaFolhaEmpregado(Empregado e, String valor, Map<String, List<String>> folha) {
        // Remove o empregado de todas as folhas antigas
        for (List<String> ids : folha.values()) {
            ids.remove(String.valueOf(e.getId()));
        }
    }
}