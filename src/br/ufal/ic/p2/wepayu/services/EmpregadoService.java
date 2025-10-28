package br.ufal.ic.p2.wepayu.services;

import br.ufal.ic.p2.wepayu.Exception.*;
import br.ufal.ic.p2.wepayu.models.*;
import br.ufal.ic.p2.wepayu.utils.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

public class EmpregadoService {
    ArrayList<Empregado> empregados;
    Map<String, String> dadosSindicais;

    public static Empregado getEmpregadoPorId (String emp, ArrayList<Empregado> empregados)
            throws EmpregadoNaoExisteException {
        if (emp == null || emp.isEmpty()) {
            if (empregados.isEmpty()) {
                throw new EmpregadoNaoExisteException();
            } else {
                throw new IllegalStateException("Identificacao do empregado nao pode ser nula.");
            }
        }
        for (Empregado e : empregados) {
            String id = String.valueOf(e.getId());
            if (id.equals(emp)) {
                return e;
            }
        }
        throw new EmpregadoNaoExisteException();
    }

    public static boolean recebeEmBanco (String emp, ArrayList<Empregado> empregados)
            throws EmpregadoNaoExisteException {
        Empregado e = getEmpregadoPorId(emp, empregados);
        if (e.getMetodoPagamento().equals(("banco"))) {
            return true;
        }
        return false;
    }

    public static Empregado criarEmpregado (String nome, String endereco, String tipo, String salario)
            throws AtributoNaoNuloException {
        //cria assalariado e horista
        Empregado e;
        if (tipo.equals("assalariado")) {
            e = new Assalariado(nome, endereco, tipo, salario);
            ValidacaoUtils.verificarEmpregado(e);
        } else if (tipo.equals("horista")) {
            e = new Horista(nome, endereco, tipo, salario);
            ValidacaoUtils.verificarEmpregado(e);
        } else if (tipo.equals("comissionado")) {
            throw new IllegalStateException("Tipo nao aplicavel.");
        } else {
            throw new IllegalStateException("Tipo invalido.");
        }

        return e;
    }

    public static Empregado criarEmpregado (String nome, String endereco, String tipo, String salario,
                                            String comissao) {
        //cria comissionado
        ValidacaoUtils.verificarSalario(salario);
        if (tipo.equals("comissionado")) {
            ValidacaoUtils.verificarComissao(comissao);
            Comissionado c = new Comissionado(nome, endereco, tipo, salario, comissao);
            return c;
        } else if (tipo.equals("assalariado") || tipo.equals("horista")) {
            throw new IllegalStateException("Tipo nao aplicavel.");
        } else {
            throw new IllegalStateException("Tipo invalido.");
        }
    }

    public static Empregado alteraEmpregado (String emp, String atributo, String valor,
                                             Empregado e, ArrayList<Empregado> empregados, List<String> agenda)
            throws EmpregadoNaoExisteException {

        if (atributo.equals("nome")) {
            if (valor == null || valor.isEmpty()) {
                throw new AtributoNaoNuloException("Nome");
            }
            e.setNome(valor);
        } else if (atributo.equals("endereco")) {
            if (valor == null || valor.isEmpty()) {
                throw new AtributoNaoNuloException("Endereco");
            }
            e.setEndereco(valor);
        } else if (atributo.equals("sindicalizado")) {
            if (!(valor.equals("true") || valor.equals("false"))) {
                throw new SindicatoException("Valor deve ser true ou false.");
            }
            boolean v = Boolean.parseBoolean(valor);
            e.setSindicalizado(v, null, 0);
        } else if (atributo.equals("comissao")) {
            if (valor == null || valor.isEmpty()) {
                throw new IllegalStateException("Comissao nao pode ser nula.");
            }
            if (!(e instanceof Comissionado)) {
                throw new IllegalStateException("Empregado nao eh comissionado.");
            }
            double v;
            try {
                v = Double.parseDouble(valor.replace(",", "."));
                if (v < 0) {
                    throw new IllegalStateException("Comissao deve ser nao-negativa.");
                }
                Comissionado c = (Comissionado) e;
                c.setComissao(v);
            } catch (NumberFormatException ex) {
                throw new IllegalStateException("Comissao deve ser numerica.");
            }
        } else if (atributo.equals("salario")) {
            if (valor == null || valor.isEmpty()) {
                throw new IllegalStateException("Salario nao pode ser nulo.");
            }
            double sal;
            try {
                sal = Double.parseDouble(valor.replace(",", "."));
            } catch (NumberFormatException ex) {
                throw new IllegalStateException("Salario deve ser numerico.");
            }
            if (sal < 0) {
                throw new IllegalStateException("Salario deve ser nao-negativo.");
            }
            e.setSalario(sal);
        } else if (atributo.equals("tipo")) {
            Empregado novo = EmpregadoUtils.mudaTipo(e, valor);
            novo.setId(e.getId());
            return novo;
        } else if (atributo.equals("metodoPagamento")) {
            if (!(valor.equals("emMaos") ||
                    valor.equals("dinheiro") ||
                    valor.equals("banco") ||
                    valor.equals("correios"))) {
                throw new IllegalStateException("Metodo de pagamento invalido.");
            }
            e.setMetodoPagamento(
                    valor,
                    e.getBanco(),
                    e.getAgencia(),
                    e.getContaCorrente()
            );
        } else if (atributo.equals("agendaPagamento")) {
            if (AgendaUtils.jaExiste(agenda, valor)) {
                e.setAgendaPagamento(valor);
            } else {
                throw new IllegalStateException("Agenda de pagamento nao esta disponivel");
            }
        } else {
            throw new IllegalStateException("Atributo nao existe.");
        }
        return e;
    }

    public static Empregado alteraEmpregado (String emp, String atributo, String valor,
                                             String comissao, Empregado e)
            throws EmpregadoNaoExisteException {
        if (atributo.equals("tipo")) {
            if (valor.equals("comissionado")) {
                Comissionado c = new Comissionado(e.getNome(), e.getEndereco(),
                        "comissionado", ConversorUtils.converteSalario(e.getSalario()), comissao);
                c.setId(e.getId());
                return c;
            } else if (valor.equals("horista")) {
                Horista h = new Horista  (
                        e.getNome(),
                        e.getEndereco(),
                        "horista",
                        comissao
                );
                h.setId(e.getId());
                return h;
            }
        }
        return null;
    }

    public static String getAtributoEmpregado (Empregado e, String atributo,
                                               Map<String, String> dadosSindicais, ArrayList<Empregado> empregados)
            throws EmpregadoNaoExisteException, NaoSindicalizadoException {
        String id = String.valueOf(e.getId());
        if (atributo.equals("nome")) {
            return e.getNome();
        } else if (atributo.equals("endereco")) {
            return e.getEndereco();
        } else if (atributo.equals("tipo")) {
            return e.getTipo();
        } else if (atributo.equals("salario")) {
            return ConversorUtils.converteSalario(e.getSalario());
        } else if (atributo.equals("sindicalizado")) {
            return ConversorUtils.converteSindicalizado(e.getSindicalizado());
        } else if (atributo.equals("comissao")) {
            if (e instanceof Comissionado) {
                Comissionado c = (Comissionado) e;
                return String.valueOf(c.getComissao()).replace(".", ",");
            } else {
                throw new IllegalStateException("Empregado nao eh comissionado.");
            }
        } else if (atributo.equals("metodoPagamento")) {
            return e.getMetodoPagamento();
        } else if (atributo.equals("banco")) {
            if (EmpregadoService.recebeEmBanco(id, empregados)) return e.getBanco();
            else throw new NaoBancoException();
        } else if (atributo.equals("agencia")) {
            if (EmpregadoService.recebeEmBanco(id, empregados)) return e.getAgencia();
            else throw new NaoBancoException();
        } else if (atributo.equals("contaCorrente")) {
            if (EmpregadoService.recebeEmBanco(id, empregados)) return e.getContaCorrente();
            else throw new NaoBancoException();
        } else if (atributo.equals("idSindicato")) {
            Empregado s = SindicatoService.getEmpSindicato(
                    e.getIdSindicato(), dadosSindicais, empregados
            );
            return s.getIdSindicato();
        } else if (atributo.equals("taxaSindical")) {
            Empregado s = SindicatoService.getEmpSindicato(
                    e.getIdSindicato(), dadosSindicais, empregados
            );
            String t = ConversorUtils.converteSalario(s.getTaxaSindical());
            return t;
        } else if (atributo.equals("agendaPagamento")) {
            return e.getAgendaPagamento();
        } else {
            throw new AtributoNaoExisteException();
        }
    }

    public static void removerIdDatas (String id, Map<String, List<String>> folha) {
        folha.keySet().forEach(data -> {
            folha.computeIfPresent(data, (k, lista) -> {
                lista.remove(id);

                if (lista.isEmpty()) return null;
                else return lista;
            });
        });
    }

    public static void mudaFolhaEmpregado (Empregado e, String agendaNova,
                                           Map<String, List<String>> folha) {
        String id = String.valueOf(e.getId());
        removerIdDatas(id, folha);
    }

    public static boolean recebeHoje(LocalDate data, Empregado e) {
        String partes[] = (e.getAgendaPagamento()).split(" ");
        if (partes[0].equals("mensal")) {
            return EmpregadoUtils.verificaMensal(data, partes);
        } else if (partes[0].equals("semanal")) {
            if (partes.length == 2) {
                return EmpregadoUtils.verificaSemanal1(data, partes[1]);
            } else if (partes.length == 3) {
                if (e.getAgendaPagamento().equals("semanal 2 5")) {
                    if (data.getDayOfWeek() != DayOfWeek.FRIDAY) {
                        return false;
                    }
                    //data de inicio sempre 1/1/2005
                    LocalDate ultimo = e.getUltimoPagamentoD();
                    if (ultimo == null) {
                        final LocalDate inicio = LocalDate.of(2005, 1, 1);
                        ultimo = e.getDataInicioD();
                        if (inicio == null) return false;
                        long diasDesdeInicio = ChronoUnit.DAYS.between(inicio, data);
                        if (diasDesdeInicio < 13) return false;

                        long diasDesdePag = diasDesdeInicio - 13;
                        return diasDesdePag % 14 == 0;
                    } else {
                        long diasDesdeUltimoPag = ChronoUnit.DAYS.between(ultimo, data);
                        return diasDesdeUltimoPag == 14;
                    }
                } else {
                    long diasDesdePag;
                    int intervaloSemanas = ConversorUtils.stringToInt(partes[1]);
                    int diaDaSemana = ConversorUtils.stringToInt(partes[2]);

                    if (!(EmpregadoUtils.verificaSemanal1(data, partes[2]))) {
                        return false;
                    }
                    int intervaloDias = intervaloSemanas * 7;
                    LocalDate supostoPag = data.minusWeeks(intervaloSemanas);
                    DayOfWeek diaEsperado = DayOfWeek.of(diaDaSemana);

                    if (supostoPag.isBefore(LocalDate.of(2005, 1, 1))) {
                        supostoPag = e.getDataInicioD();
                        if (supostoPag == null) return false;
                        while (supostoPag.getDayOfWeek() != diaEsperado) {
                            supostoPag = supostoPag.plusDays(1);
                        }
                        if (data.isBefore(supostoPag)) return false;
                        diasDesdePag = ChronoUnit.DAYS.between(supostoPag, data);
                        return (diasDesdePag+7) % intervaloDias == 0;
                    } else {
                        diasDesdePag = ChronoUnit.DAYS.between(supostoPag, data);
                        return diasDesdePag % intervaloDias == 0;
                    }
                }
            }
        }
        return false;
    }

}