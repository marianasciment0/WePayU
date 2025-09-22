package br.ufal.ic.p2.wepayu;

import br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoExisteException;
import br.ufal.ic.p2.wepayu.models.CartaoPonto;
import br.ufal.ic.p2.wepayu.models.Empregado;
import br.ufal.ic.p2.wepayu.models.TaxaServico;
import br.ufal.ic.p2.wepayu.models.Venda;

import java.time.LocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.util.*;
import java.util.Map;
import java.util.HashMap;
import java.io.*;
import java.beans.XMLEncoder;
import java.beans.XMLDecoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.Stack;
import java.util.HashSet;
import java.util.Set;

public class Facade {
    private Map<String, Empregado> empregados = new HashMap<>();
    private int proximoId = 1;
    private static final String DATA_FILE = "sistema_data.xml";

    private Stack<Map<String, Object>> undoStack = new Stack<>();
    private Stack<Map<String, Object>> redoStack = new Stack<>();
    private Set<String> agendasDisponiveis = new HashSet<>();
    private boolean sistemaEncerrado = false;

    public Facade() {
        carregarDados();
        agendasDisponiveis.add("semanal 5");
        agendasDisponiveis.add("mensal $");
        agendasDisponiveis.add("semanal 2 5");
    }

    private void saveState() {
        if (sistemaEncerrado) return;

        Map<String, Object> state = new HashMap<>();
        state.put("empregados", new HashMap<>(empregados));
        state.put("proximoId", proximoId);
        state.put("agendasDisponiveis", new HashSet<>(agendasDisponiveis));
        undoStack.push(state);
        redoStack.clear();
    }

    public void undo() {
        if (sistemaEncerrado) {
            throw new Error("Nao pode dar comandos depois de encerrarSistema.");
        }
        if (undoStack.isEmpty()) {
            throw new Error("Nao ha comando a desfazer.");
        }

        redoStack.push(saveCurrentState());
        restoreState(undoStack.pop());
    }

    public void redo() {
        if (sistemaEncerrado) {
            throw new Error("Nao pode dar comandos depois de encerrarSistema.");
        }
        if (redoStack.isEmpty()) {
            throw new Error("Nao ha comando a refazer.");
        }

        undoStack.push(saveCurrentState());
        restoreState(redoStack.pop());
    }

    private Map<String, Object> saveCurrentState() {
        Map<String, Object> state = new HashMap<>();
        state.put("empregados", new HashMap<>(empregados));
        state.put("proximoId", proximoId);
        state.put("agendasDisponiveis", new HashSet<>(agendasDisponiveis));
        return state;
    }

    private void restoreState(Map<String, Object> state) {
        empregados = new HashMap<>((Map<String, Empregado>) state.get("empregados"));
        proximoId = (Integer) state.get("proximoId");
        agendasDisponiveis = new HashSet<>((Set<String>) state.get("agendasDisponiveis"));
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario) {
        saveState();
        validarNome(nome);
        validarEndereco(endereco);
        validarTipo(tipo);
        Double salarioDouble = validarSalarioString(salario);

        if (tipo.equals("comissionado")) {
            throw new Error("Tipo nao aplicavel.");
        }

        String id = gerarProximoId();
        Empregado emp = new Empregado(nome, endereco, tipo, salarioDouble);
        switch (tipo) {
            case "horista": emp.setAgendaPagamento("semanal 5"); break;
            case "assalariado": emp.setAgendaPagamento("mensal $"); break;
            case "comissionado": emp.setAgendaPagamento("semanal 2 5"); break;
        }
        empregados.put(id, emp);
        salvarDados();
        return id;
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario, String comissao) {
        saveState();
        validarNome(nome);
        validarEndereco(endereco);
        validarTipo(tipo);
        Double salarioDouble = validarSalarioString(salario);
        Double comissaoDouble = validarComissaoString(comissao);

        if (!tipo.equals("comissionado")) {
            throw new Error("Tipo nao aplicavel.");
        }

        String id = gerarProximoId();
        Empregado emp = new Empregado(nome, endereco, tipo, salarioDouble, comissaoDouble);
        empregados.put(id, emp);
        salvarDados();

        return id;
    }

    public String getEmpregadoPorNome(String nome, String indice) {
        if (nome == null || nome.isBlank()) {
            throw new Error("Nome nao pode ser nulo.");
        }

        Integer indiceInt;
        try {
            indiceInt = Integer.parseInt(indice);
        } catch (NumberFormatException e) {
            throw new Error("Indice deve ser numerico.");
        }

        if (indiceInt < 1) {
            throw new Error("Indice deve ser positivo.");
        }

        List<String> idsComNome = new ArrayList<>();
        for (Map.Entry<String, Empregado> entry : empregados.entrySet()) {
            if (entry.getValue().getNome().equals(nome)) {
                idsComNome.add(entry.getKey());
            }
        }

        if (indiceInt > idsComNome.size()) {
            throw new Error("Nao ha empregado com esse nome.");
        }

        return idsComNome.get(indiceInt - 1);
    }

    public String getAtributoEmpregado(String emp, String atributo) throws EmpregadoNaoExisteException {
        if (emp == null || emp.isEmpty()) {
            throw new Error("Identificacao do empregado nao pode ser nula.");
        }

        if (!empregados.containsKey(emp)) {
            throw new EmpregadoNaoExisteException();
        }

        Empregado empregado = empregados.get(emp);

        switch (atributo) {
            case "nome":
                return empregado.getNome();
            case "endereco":
                return empregado.getEndereco();
            case "tipo":
                return empregado.getTipo();
            case "salario":
                return formatarDouble(empregado.getSalario());
            case "sindicalizado":
                return Boolean.toString(empregado.getSindicalizado());
            case "comissao":
                if (!empregado.getTipo().equals("comissionado")) {
                    throw new Error("Empregado nao eh comissionado.");
                }
                return formatarDouble(empregado.getComissao());
            case "metodoPagamento":
                return empregado.getMetodoPagamento();
            case "banco":
                if (!"banco".equals(empregado.getMetodoPagamento())) {
                    throw new Error("Empregado nao recebe em banco.");
                }
                return empregado.getBanco();
            case "agencia":
                if (!"banco".equals(empregado.getMetodoPagamento())) {
                    throw new Error("Empregado nao recebe em banco.");
                }
                return empregado.getAgencia();
            case "contaCorrente":
                if (!"banco".equals(empregado.getMetodoPagamento())) {
                    throw new Error("Empregado nao recebe em banco.");
                }
                return empregado.getContaCorrente();
            case "idSindicato":
                if (!Boolean.TRUE.equals(empregado.getSindicalizado())) {
                    throw new Error("Empregado nao eh sindicalizado.");
                }
                return empregado.getIdSindicato();
            case "taxaSindical":
                if (!Boolean.TRUE.equals(empregado.getSindicalizado())) {
                    throw new Error("Empregado nao eh sindicalizado.");
                }
                return formatarDouble(empregado.getTaxaSindical());
            case "agendaPagamento":
                return empregado.getAgendaPagamento();
            default:
                throw new Error("Atributo nao existe.");
        }
    }

    public void removerEmpregado(String emp) throws EmpregadoNaoExisteException {
        saveState();
        if (emp == null || emp.isEmpty()) {
            throw new Error("Identificacao do empregado nao pode ser nula.");
        }

        if (!empregados.containsKey(emp)) {
            throw new EmpregadoNaoExisteException();
        }

        empregados.remove(emp);
        salvarDados();
    }

    private String gerarProximoId() {
        return String.valueOf(proximoId++);
    }

    private String formatarDouble(Double valor) {
        if (valor == null) return "0,00";
        return String.format("%.2f", valor).replace(".", ",");
    }

    public void lancaCartao(String emp, String data, String horas) throws EmpregadoNaoExisteException {
        saveState();
        if (emp == null || emp.isEmpty()) {
            throw new Error("Identificacao do empregado nao pode ser nula.");
        }

        if (!empregados.containsKey(emp)) {
            throw new EmpregadoNaoExisteException();
        }

        Empregado empregado = empregados.get(emp);
        if (!"horista".equals(empregado.getTipo())) {
            throw new Error("Empregado nao eh horista.");
        }

        String[] dataSeparada = data.split("/");
        Integer diaData = Integer.parseInt(dataSeparada[0]);
        Integer mesData = Integer.parseInt(dataSeparada[1]);
        Integer anoData = Integer.parseInt(dataSeparada[2]);

        if (dataSeparada == null || diaData > 31 || diaData < 1
                || mesData > 12 || mesData < 1 ||
                anoData > LocalDate.now().getYear()) {
            throw new Error("Data invalida.");
        }

        Double horasDouble = validarHoras(horas);

        if (horasDouble < 1) {
            throw new Error("Horas devem ser positivas");
        }

        Date dataDate = parseData(data);
        CartaoPonto cartao = new CartaoPonto(dataDate, horasDouble);
        empregado.getCartoesPonto().add(cartao);
        salvarDados();
    }

    public String getHorasNormaisTrabalhadas(String emp, String dataInicial, String dataFinal) throws EmpregadoNaoExisteException {
        validarConsultaHoras(emp, dataInicial, dataFinal);

        Empregado empregado = empregados.get(emp);

        Date dataIn = parseData(dataInicial);
        Date dataFi = parseData(dataFinal);

        validarData(dataInicial, dataFinal, dataIn, dataFi);

        if (dataIn.equals(dataFi)) {
            return "0";
        } else {
            if (dataIn.after(dataFi)) {
                throw new Error("Data inicial nao pode ser posterior aa data final.");
            }
        }

        Map<String, Double> horasPorDia = new HashMap<>();

        for (CartaoPonto cartao : empregado.getCartoesPonto()) {
            Date dataCartao = cartao.getData();
            if (dataCartao.compareTo(dataIn) >= 0 && dataCartao.compareTo(dataFi) < 0) {
                String dataKey = formatarData(dataCartao);
                horasPorDia.put(dataKey, horasPorDia.getOrDefault(dataKey, 0.0) + cartao.getHoras());
            }
        }

        Integer horasNormais = 0;
        for (var horasDia : horasPorDia.values()) {
            if (horasDia >= 8) {
                horasNormais += 8;
            } else {
                horasNormais += horasDia.intValue();
            }
        }

        return formatarHoras(horasNormais);
    }

    public String getHorasExtrasTrabalhadas(String emp, String dataInicial, String dataFinal) throws EmpregadoNaoExisteException {
        validarConsultaHoras(emp, dataInicial, dataFinal);

        Empregado empregado = empregados.get(emp);

        Date dataIn = parseData(dataInicial);
        Date dataFi = parseData(dataFinal);

        validarData(dataInicial, dataFinal, dataIn, dataFi);

        if (dataIn.equals(dataFi)) {
            return "0";
        } else {
            if (dataIn.after(dataFi)) {
                throw new Error("Data inicial nao pode ser posterior aa data final.");
            }
        }

        Map<String, Double> horasPorDia = new HashMap<>();

        for (CartaoPonto cartao : empregado.getCartoesPonto()) {
            Date dataCartao = cartao.getData();
            if (dataCartao.compareTo(dataIn) >= 0 && dataCartao.compareTo(dataFi) < 0) {
                String dataKey = formatarData(dataCartao);
                horasPorDia.put(dataKey, horasPorDia.getOrDefault(dataKey, 0.0) + cartao.getHoras());
            }
        }

        double horasExtras = 0;
        for (Double horasDia : horasPorDia.values()) {
            if (horasDia > 8) {
                horasExtras += (horasDia - 8);
            }
        }

        return formatarHoras(horasExtras);
    }

    private String formatarData(Date data) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(data);
    }

    private void validarData(String dataInicial, String dataFinal, Date dataIn, Date dataFi) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setLenient(false);

        try {
            sdf.parse(dataInicial);
        } catch (Exception e){
            throw new Error("Data inicial invalida.");
        }

        try {
            sdf.parse(dataFinal);
        } catch (Exception e){
            throw new Error("Data final invalida.");
        }
    }

    public void alteraEmpregado(String emp, String atributo, String valor) throws EmpregadoNaoExisteException {
        saveState();
        if (emp.isEmpty() || emp == null) {
            throw new Error("Identificacao do empregado nao pode ser nula.");
        }

        if (!empregados.containsKey(emp)) {
            throw new Error("Empregado nao existe.");
        }
        Empregado empreg = empregados.get(emp);
        alteraAtributoSimples(empreg, atributo, valor);
    }

    public void alteraEmpregado(String emp, String atributo, String valor, String valor2) {
        saveState();
        Empregado empreg = empregados.get(emp);
        if (atributo.equalsIgnoreCase("tipo")) {
            alteraAtributoSimples(empreg, "tipo", valor);
            switch (valor.toLowerCase()) {
                case "horista" -> alteraAtributoSimples(empreg, "salario", valor2);
                case "comissionado" -> alteraAtributoSimples(empreg, "comissao", valor2);
                case "assalariado" -> alteraAtributoSimples(empreg, "salario", valor2);
                default -> throw new Error("");
            }
        }
    }

    public String alteraEmpregado(String emp, String atributo, String valor, String idSindicato, String taxaSindical) throws EmpregadoNaoExisteException {
        saveState();
        return alteraEmpregadoImpl(emp, atributo, valor, null, idSindicato, taxaSindical, null);
    }

    public String alteraEmpregado(String emp, String atributo, String valor, String banco, String agencia, String contaCorrente) throws EmpregadoNaoExisteException {
        return alteraEmpregadoImpl(emp, atributo, valor, null, null, null, new String[]{banco, agencia, contaCorrente});
    }

    private String alteraEmpregadoImpl(String emp, String atributo, String valor, String comissao,
                                       String idSindicato, String taxaSindical, String[] dadosBanco) throws EmpregadoNaoExisteException {
        saveState();

        if (emp == null || emp.isEmpty()) {
            throw new Error("Identificacao do empregado nao pode ser nula.");
        }

        if (!empregados.containsKey(emp)) {
            throw new EmpregadoNaoExisteException();
        }

        Empregado empregado = empregados.get(emp);

        if (comissao != null) {
            if (!"tipo".equals(atributo) || !"comissionado".equals(valor)) {
                throw new Error("Parametros invalidos.");
            }
            return alteraTipoComissao(empregado, comissao);
        } else if (idSindicato != null && taxaSindical != null) {
            if (!"sindicalizado".equals(atributo) || !"true".equals(valor)) {
                throw new Error("Parametros invalidos.");
            }
            return alteraSindicalizado(empregado, idSindicato, taxaSindical);
        } else if (dadosBanco != null) {
            if (!"metodoPagamento".equals(atributo) || !"banco".equals(valor)) {
                throw new Error("Parametros invalidos.");
            }
            return alteraMetodoPagamentoBanco(empregado, dadosBanco[0], dadosBanco[1], dadosBanco[2]);
        } else {
            return alteraAtributoSimples(empregado, atributo, valor);
        }
    }

    private String alteraMetodoPagamentoBanco(Empregado emp, String banco, String agencia, String contaCorrente) {
        if (banco == null || banco.isEmpty()) {
            throw new Error("Banco nao pode ser nulo.");
        }
        if (agencia == null || agencia.isEmpty()) {
            throw new Error("Agencia nao pode ser nulo.");
        }
        if (contaCorrente == null || contaCorrente.isEmpty()) {
            throw new Error("Conta corrente nao pode ser nulo.");
        }

        emp.setMetodoPagamento("banco");
        emp.setBanco(banco);
        emp.setAgencia(agencia);
        emp.setContaCorrente(contaCorrente);

        salvarDados();
        return "true";
    }

    private String alteraAtributoSimples(Empregado emp, String atributo, String valor) {
        switch (atributo) {
            case "nome":
                validarNome(valor);
                emp.setNome(valor);
                break;
            case "endereco":
                validarEndereco(valor);
                emp.setEndereco(valor);
                break;
            case "salario":
                Double salario = validarSalarioString(valor);
                emp.setSalario(salario);
                break;
            case "comissao":
                if (!"comissionado".equals(emp.getTipo())) {
                    throw new Error("Empregado nao eh comissionado.");
                }
                Double comissao = validarComissaoString(valor);
                emp.setComissao(comissao);
                break;
            case "sindicalizado":
                if ("true".equals(valor) || "false".equals(valor)) {
                    if ("false".equals(valor)) {
                        emp.setSindicalizado(false);
                        emp.setIdSindicato(null);
                        emp.setTaxaSindical(null);
                    } else {
                        throw new Error("Identificacao do empregado nao pode ser nula.");
                    }
                } else {
                    throw new Error("Valor deve ser true ou false.");
                }
                break;
            case "metodoPagamento":
                validarMetodoPagamento(valor);
                emp.setMetodoPagamento(valor);
                if (!"banco".equals(valor)) {
                    emp.setBanco(null);
                    emp.setAgencia(null);
                    emp.setContaCorrente(null);
                }
                break;
            case "tipo":
                validarTipo(valor);
                emp.setTipo(valor);
                if (!"comissionado".equals(valor)) {
                    emp.setComissao(null);
                }
                break;
            case "agendaPagamento":
                if (!agendasDisponiveis.contains(valor)) {
                    throw new Error("Agenda de pagamento nao esta disponivel");
                }
                emp.setAgendaPagamento(valor);
                break;
            default:
                throw new Error("Atributo nao existe.");
        }
        salvarDados();
        return "true";
    }

    private String alteraTipoComissao(Empregado emp, String comissao) {
        Double comissaoDouble = validarComissaoString(comissao);
        emp.setTipo("comissionado");
        emp.setComissao(comissaoDouble);
        salvarDados();
        return "true";
    }

    private String alteraSindicalizado(Empregado emp, String idSindicato, String taxaSindical) {
        if (idSindicato == null || idSindicato.isEmpty()) {
            throw new Error("Identificacao do sindicato nao pode ser nula.");
        }

        if (taxaSindical == null || taxaSindical.isEmpty()) {
            throw new Error("Taxa sindical nao pode ser nula.");
        }

        for (Empregado e : empregados.values()) {
            if (e.getSindicalizado() && e.getIdSindicato() != null &&
                    e.getIdSindicato().equals(idSindicato) && !e.equals(emp)) {
                throw new Error("Ha outro empregado com esta identificacao de sindicato");
            }
        }

        Double taxaSindicalDouble = validarTaxaSindical(taxaSindical);
        emp.setSindicalizado(true);
        emp.setIdSindicato(idSindicato);
        emp.setTaxaSindical(taxaSindicalDouble);

        salvarDados();
        return "true";
    }

    public void criarAgendaDePagamentos(String descricao) {
        saveState();

        if (!descricao.matches("(semanal\\s+([1-7]|([1-4]?[0-9]|5[0-2])\\s+[1-7])|mensal\\s+([1-9]|[12][0-8]|\\$))")) {
            throw new Error("Descricao de agenda invalida");
        }

        if (agendasDisponiveis.contains(descricao)) {
            throw new Error("Agenda de pagamentos ja existe");
        }

        agendasDisponiveis.add(descricao);
        salvarDados();
    }

    private Double validarTaxaSindical(String taxa) {
        if (taxa == null || taxa.isEmpty()) {
            throw new Error("Taxa sindical nao pode ser nula.");
        }

        try {
            String taxaFormatada = taxa.replace(',', '.');
            double taxaDouble = Double.parseDouble(taxaFormatada);

            if (taxaDouble < 0) {
                throw new Error("Taxa sindical deve ser nao-negativa.");
            }

            return taxaDouble;
        } catch (NumberFormatException e) {
            throw new Error("Taxa sindical deve ser numerica.");
        }
    }

    private void validarMetodoPagamento(String metodo) {
        if (metodo == null || (!"emMaos".equals(metodo) && !"correios".equals(metodo) && !"banco".equals(metodo))) {
            throw new Error("Metodo de pagamento invalido.");
        }
    }

    private void validarDadosBancarios(String banco, String agencia, String contaCorrente) {
        if (banco == null || banco.isEmpty()) {
            throw new Error("Banco nao pode ser nulo.");
        }
        if (agencia == null || agencia.isEmpty()) {
            throw new Error("Agencia nao pode ser nulo.");
        }
        if (contaCorrente == null || contaCorrente.isEmpty()) {
            throw new Error("Conta corrente nao pode ser nulo.");
        }
    }

    private void validarTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            throw new Error("Tipo nao pode ser nulo");
        }

        List<String> opcoesValidas = Arrays.asList("horista", "assalariado", "comissionado");
        if (!opcoesValidas.contains(tipo)) {
            throw new Error("Tipo invalido.");
        }
    }

    public String lancaTaxaServico(String membro, String data, String valor) {
        saveState();
        if (membro == null || membro.isEmpty()) {
            throw new Error("Identificacao do membro nao pode ser nula.");
        }

        Empregado empregado = null;
        for (Empregado e : empregados.values()) {
            if (e.getSindicalizado() && e.getIdSindicato() != null &&
                    e.getIdSindicato().equals(membro)) {
                empregado = e;
                break;
            }
        }

        if (empregado == null) {
            throw new Error("Membro nao existe.");
        }

        Date dataSeparada = parseData(data);
        if (dataSeparada == null) {
            throw new Error("Data invalida.");
        }

        double valorDouble = validarValorTaxaServico(valor);

        TaxaServico taxa = new TaxaServico(dataSeparada, valorDouble);

        empregado.getTaxasServico().add(taxa);
        salvarDados();

        return "true";
    }

    public String getTaxasServico(String emp, String dataInicial, String dataFinal) throws EmpregadoNaoExisteException {
        validarConsultaTaxasServico(emp, dataInicial, dataFinal);

        Empregado empregado = empregados.get(emp);
        Date dataInicialDate = parseData(dataInicial);
        Date dataFinalDate = parseData(dataFinal);

        if (dataInicialDate.equals(dataFinalDate)) {
            return "0,00";
        }

        double totalTaxas = 0;
        for (TaxaServico taxa : empregado.getTaxasServico()) {
            Date dataTaxa = taxa.getData();
            if (!dataTaxa.before(dataInicialDate) && dataTaxa.before(dataFinalDate)) {
                totalTaxas += taxa.getValor();
            }
        }

        return formatarDouble(totalTaxas);
    }

    private void validarConsultaTaxasServico(String emp, String dataInicial, String dataFinal) throws EmpregadoNaoExisteException {
        if (emp == null || emp.isEmpty()) {
            throw new Error("Identificacao do empregado nao pode ser nula.");
        }

        if (!empregados.containsKey(emp)) {
            throw new EmpregadoNaoExisteException();
        }

        Empregado empregado = empregados.get(emp);
        if (!Boolean.TRUE.equals(empregado.getSindicalizado())) {
            throw new Error("Empregado nao eh sindicalizado.");
        }

        if (parseData(dataInicial) == null) {
            throw new Error("Data inicial invalida.");
        }

        if (parseData(dataFinal) == null) {
            throw new Error("Data final invalida.");
        }

        Date dataInicialDate = parseData(dataInicial);
        Date dataFinalDate = parseData(dataFinal);

        if (dataInicialDate.after(dataFinalDate)) {
            throw new Error("Data inicial nao pode ser posterior aa data final.");
        }
    }

    private double validarValorTaxaServico(String valor) {
        if (valor == null || valor.isEmpty()) {
            throw new Error("Valor deve ser positivo.");
        }

        try {
            String valorFormatado = valor.replace(',', '.');
            double valorDouble = Double.parseDouble(valorFormatado);

            if (valorDouble <= 0) {
                throw new Error("Valor deve ser positivo.");
            }

            return valorDouble;
        } catch (NumberFormatException e) {
            throw new Error("Valor deve ser numerica.");
        }
    }

    private void validarConsultaHoras(String emp, String dataInicial, String dataFinal) throws EmpregadoNaoExisteException {
        if (emp == null || emp.isEmpty()) {
            throw new Error("Identificacao do empregado nao pode ser nula.");
        }

        if (!empregados.containsKey(emp)) {
            throw new EmpregadoNaoExisteException();
        }

        Empregado empregado = empregados.get(emp);
        if (!"horista".equals(empregado.getTipo())) {
            throw new Error("Empregado nao eh horista.");
        }

        if (parseData(dataInicial) == null) {
            throw new Error("Data inicial invalida.");
        }

        if (parseData(dataFinal) == null) {
            throw new Error("Data final invalida.");
        }

        Date dataInicialDate = parseData(dataInicial);
        Date dataFinalDate = parseData(dataFinal);

        if (dataInicialDate.after(dataFinalDate)) {
            throw new Error("Data inicial nao pode ser posterior aa data final.");
        }
    }

    private double validarHoras(String horas) {
        if (horas == null || horas.isEmpty()) {
            throw new Error("Horas devem ser positivas.");
        }

        try {
            String horasFormatada = horas.replace(',', '.');
            double horasDouble = Double.parseDouble(horasFormatada);

            if (horasDouble <= 0) {
                throw new Error("Horas devem ser positivas.");
            }

            return horasDouble;
        } catch (NumberFormatException e) {
            throw new Error("Horas devem ser numericas.");
        }
    }

    private Date parseData(String data) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            sdf.setLenient(false);
            return sdf.parse(data);
        } catch (Exception e) {
            return null;
        }
    }

    private String formatarHoras(double horas) {
        if (horas == (int) horas) {
            return String.valueOf((int) horas);
        } else {
            return String.format("%.1f", horas).replace(".", ",");
        }
    }

    private void salvarDados() {
        try {
            Map<String, Object> dadosCompletos = new HashMap<>();
            dadosCompletos.put("empregados", empregados);
            dadosCompletos.put("proximoId", proximoId);
            dadosCompletos.put("agendasDisponiveis", agendasDisponiveis);

            try (XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(
                    new FileOutputStream(DATA_FILE)))) {
                encoder.writeObject(dadosCompletos);
            }
        } catch (IOException e) {
            System.err.println("Erro ao salvar dados: " + e.getMessage());
        }
    }

    private void carregarDados() {
        try {
            File dataFile = new File(DATA_FILE);
            if (dataFile.exists()) {
                try (XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(
                        new FileInputStream(dataFile)))) {
                    Object obj = decoder.readObject();
                    if (obj instanceof Map) {
                        Map<String, Object> dadosCompletos = (Map<String, Object>) obj;
                        empregados = (Map<String, Empregado>) dadosCompletos.get("empregados");
                        proximoId = (Integer) dadosCompletos.get("proximoId");
                        agendasDisponiveis = (Set<String>) dadosCompletos.get("agendasDisponiveis");
                        if (agendasDisponiveis == null) {
                            agendasDisponiveis = new HashSet<>();
                            agendasDisponiveis.add("semanal 5");
                            agendasDisponiveis.add("mensal $");
                            agendasDisponiveis.add("semanal 2 5");
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao carregar dados: " + e.getMessage());
            empregados = new HashMap<>();
            proximoId = 1;
        } catch (Exception e) {
            System.err.println("Erro ao decodificar dados: " + e.getMessage());
            empregados = new HashMap<>();
            proximoId = 1;
        }
    }

    public void zerarSistema() {
        saveState();
        empregados.clear();
        proximoId = 1;
        agendasDisponiveis.clear();
        agendasDisponiveis.add("semanal 5");
        agendasDisponiveis.add("mensal $");
        agendasDisponiveis.add("semanal 2 5");
        undoStack.clear();
        redoStack.clear();
        sistemaEncerrado = false;

        new File(DATA_FILE).delete();
    }

    public void encerrarSistema() {
        saveState();
        salvarDados();
        sistemaEncerrado = true;
    }

    private Double validarSalarioString(String salario) {
        if (salario == null || salario.isBlank()) {
            throw new Error("Salario nao pode ser nulo.");
        }

        if (!salario.matches("-?\\d+([.,]\\d{1,2})?")) {
            throw new Error("Salario deve ser numerico.");
        }

        String numeroFormatado = salario.replace(',', '.');
        double salarioDouble = Double.parseDouble(numeroFormatado);

        if (salarioDouble < 0) {
            throw new Error("Salario deve ser nao-negativo.");
        }

        return salarioDouble;
    }

    private Double validarComissaoString(String comissao) {
        if (comissao == null || comissao.isEmpty()) {
            throw new Error("Comissao nao pode ser nula.");
        }

        try {
            String numeroFormatado = comissao.replace(',', '.');
            double comissaoDouble = Double.parseDouble(numeroFormatado);

            if (comissaoDouble < 0) {
                throw new Error("Comissao deve ser nao-negativa.");
            }

            return comissaoDouble;
        } catch (NumberFormatException e) {
            throw new Error("Comissao deve ser numerica.");
        }
    }

    private void validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new Error("Nome nao pode ser nulo.");
        }

        for (char c : nome.toCharArray()) {
            if (!Character.isLetter(c) && c != ' ') {
                throw new Error("Nome deve conter apenas letras e espaços.");
            }
        }
    }

    private void validarEndereco(String endereco) {
        if (endereco == null || endereco.isBlank()) {
            throw new Error("Endereco nao pode ser nulo.");
        }
    }

    public String lancaVenda(String emp, String data, String valor) throws EmpregadoNaoExisteException {
        saveState();
        if (emp == null || emp.isEmpty()) {
            throw new Error("Identificacao do empregado nao pode ser nula.");
        }

        if (!empregados.containsKey(emp)) {
            throw new EmpregadoNaoExisteException();
        }

        Empregado empregado = empregados.get(emp);
        if (!"comissionado".equals(empregado.getTipo())) {
            throw new Error("Empregado nao eh comissionado.");
        }

        Date dataSeparada = parseData(data);
        if (dataSeparada == null) {
            throw new Error("Data invalida.");
        }

        double valorDouble = validarValorVenda(valor);

        Venda venda = new Venda(dataSeparada, valorDouble);
        empregado.getVendas().add(venda);
        salvarDados();

        return "true";
    }

    public String getVendasRealizadas(String emp, String dataInicial, String dataFinal) throws EmpregadoNaoExisteException {
        validarConsultaVendas(emp, dataInicial, dataFinal);

        Empregado empregado = empregados.get(emp);
        Date dataInicialDate = parseData(dataInicial);
        Date dataFinalDate = parseData(dataFinal);

        if (dataInicialDate.equals(dataFinalDate)) {
            return "0,00";
        }

        double totalVendas = 0;
        for (Venda venda : empregado.getVendas()) {
            Date dataVenda = venda.getData();
            if (!dataVenda.before(dataInicialDate) && dataVenda.before(dataFinalDate)) {
                totalVendas += venda.getValor();
            }
        }

        return formatarDouble(totalVendas);
    }

    private void validarConsultaVendas(String emp, String dataInicial, String dataFinal) throws EmpregadoNaoExisteException {
        if (emp == null || emp.isEmpty()) {
            throw new Error("Identificacao do empregado nao pode ser nula.");
        }

        if (!empregados.containsKey(emp)) {
            throw new EmpregadoNaoExisteException();
        }

        Empregado empregado = empregados.get(emp);
        if (!"comissionado".equals(empregado.getTipo())) {
            throw new Error("Empregado nao eh comissionado.");
        }

        if (parseData(dataInicial) == null) {
            throw new Error("Data inicial invalida.");
        }

        if (parseData(dataFinal) == null) {
            throw new Error("Data final invalida.");
        }

        Date dataInicialDate = parseData(dataInicial);
        Date dataFinalDate = parseData(dataFinal);

        if (dataInicialDate.after(dataFinalDate)) {
            throw new Error("Data inicial nao pode ser posterior aa data final.");
        }
    }

    private double validarValorVenda(String valor) {
        if (valor == null || valor.isEmpty()) {
            throw new Error("Valor deve ser positivo.");
        }

        try {
            String valorFormatado = valor.replace(',', '.');
            double valorDouble = Double.parseDouble(valorFormatado);

            if (valorDouble <= 0) {
                throw new Error("Valor deve ser positivo.");
            }

            return valorDouble;
        } catch (NumberFormatException e) {
            throw new Error("Valor deve ser numerico.");
        }
    }

    public String rodaFolha(String data, String saida) {
        Date dataSeparada = parseData(data);
        if (dataSeparada == null) {
            throw new Error("Data invalida.");
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(saida))) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            writer.println("FOLHA DE PAGAMENTO DO DIA " + sdf.format(dataSeparada));
            writer.println("=".repeat(36));
            writer.println();
            writer.println("=".repeat(127));
            writer.println();

            List<String> linhasHoristas = new ArrayList<>();
            List<String> linhasAssalariados = new ArrayList<>();
            List<String> linhasComissionados = new ArrayList<>();

            double totalHoristas = 0;
            double totalAssalariados = 0;
            double totalComissionados = 0;
            double totalFolha = 0;

            for (Empregado emp : empregados.values()) {
                double pagamento = calcularPagamentoEmpregado(emp, dataSeparada);
                if (pagamento > 0) {
                    String linha = gerarLinhaFolha(emp, pagamento);

                    switch (emp.getTipo()) {
                        case "horista":
                            linhasHoristas.add(linha);
                            totalHoristas += pagamento;
                            break;
                        case "assalariado":
                            linhasAssalariados.add(linha);
                            totalAssalariados += pagamento;
                            break;
                        case "comissionado":
                            linhasComissionados.add(linha);
                            totalComissionados += pagamento;
                            break;
                    }
                    totalFolha += pagamento;
                }
            }

            if (!linhasHoristas.isEmpty()) {
                writer.println("===================== HORISTAS ================================================================================================");
                writer.println();
                Collections.sort(linhasHoristas);
                for (String linha : linhasHoristas) {
                    writer.println(linha);
                }
                writer.println();
            }

            if (!linhasAssalariados.isEmpty()) {
                writer.println("===================== ASSALARIADOS ============================================================================================");
                writer.println();
                Collections.sort(linhasAssalariados);
                for (String linha : linhasAssalariados) {
                    writer.println(linha);
                }
                writer.println();
            }

            if (!linhasComissionados.isEmpty()) {
                writer.println("===================== COMISSIONADOS ============================================================================================");
                writer.println();
                Collections.sort(linhasComissionados);
                for (String linha : linhasComissionados) {
                    writer.println(linha);
                }
                writer.println();
            }

            writer.println("===============================================================================================================================");
            writer.println();

            if (totalHoristas > 0) {
                writer.println("TOTAL HORISTAS: " + formatarDouble(totalHoristas));
            }
            if (totalAssalariados > 0) {
                writer.println("TOTAL ASSALARIADOS: " + formatarDouble(totalAssalariados));
            }
            if (totalComissionados > 0) {
                writer.println("TOTAL COMISSIONADOS: " + formatarDouble(totalComissionados));
            }

            writer.println();
            writer.println("TOTAL: " + formatarDouble(totalFolha));

        } catch (IOException e) {
            throw new Error("Erro ao escrever arquivo de folha: " + e.getMessage());
        }

        return "true";
    }

    public String totalFolha(String data) {
        Date dataSep = parseData(data);
        Calendar cal = Calendar.getInstance();
        cal.setTime(dataSep);

        if (cal.get(Calendar.DAY_OF_WEEK) != 6) {
            return "0,00";
        }

        String[] dataSeparada = data.split("/");
        Integer diaData = Integer.parseInt(dataSeparada[0]);
        Integer mesData = Integer.parseInt(dataSeparada[1]);
        Integer anoData = Integer.parseInt(dataSeparada[2]);

        if (dataSeparada == null || diaData > 31 || diaData < 1 || mesData > 12 || mesData < 1 ||
                anoData > LocalDate.now().getYear()) {
            throw new Error("Data invalida.");
        }

        double total = 0;
        for (Empregado emp : empregados.values()) {
            total += calcularPagamentoEmpregado(emp, dataSep);
        }

        return formatarDouble(total);
    }

    private double calcularPagamentoEmpregado(Empregado emp, Date data) {
        if (!ehDiaDePagamento(emp, data)) {
            return 0;
        }

        switch (emp.getTipo()) {
            case "horista":
                return calcularPagamentoHorista(emp, data);
            case "assalariado":
                return calcularPagamentoAssalariado(emp, data);
            case "comissionado":
                return calcularPagamentoComissionado(emp, data);
            default:
                return 0;
        }
    }

    private boolean ehDiaDePagamento(Empregado emp, Date data) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(data);
        int diaSemana = cal.get(Calendar.DAY_OF_WEEK);

        if ("horista".equals(emp.getTipo())) {
            return diaSemana == Calendar.FRIDAY;
        } else if ("assalariado".equals(emp.getTipo())) {
            Calendar ultimo = Calendar.getInstance();
            ultimo.setTime(data);
            int ultimoDiaMes = ultimo.getActualMaximum(Calendar.DAY_OF_MONTH);
            ultimo.set(Calendar.DAY_OF_MONTH, ultimoDiaMes);

            int dow = ultimo.get(Calendar.DAY_OF_WEEK);
            if (dow == Calendar.SATURDAY) {
                ultimo.add(Calendar.DAY_OF_MONTH, -1);
            } else if (dow == Calendar.SUNDAY) {
                ultimo.add(Calendar.DAY_OF_MONTH, -2);
            }

            return cal.get(Calendar.YEAR) == ultimo.get(Calendar.YEAR)
                    && cal.get(Calendar.MONTH) == ultimo.get(Calendar.MONTH)
                    && cal.get(Calendar.DAY_OF_MONTH) == ultimo.get(Calendar.DAY_OF_MONTH);
        } else if ("comissionado".equals(emp.getTipo())) {
            if (diaSemana != Calendar.FRIDAY) return false;

            Date dataContrato = emp.getDataContrato();
            if (dataContrato == null) dataContrato = parseData("1/1/2005");

            long diasDesdeContrato = calcularDiasEntreDatas(dataContrato, data);
            return diasDesdeContrato % 14 == 0;
        }

        return false;
    }

    private double[] calcularHorasTrabalhadasPeriodo(String empId, Date dataInicio, Date dataFim) {
        if (!empregados.containsKey(empId)) {
            return new double[]{0, 0};
        }

        Empregado empregado = empregados.get(empId);

        // Se não for horista, retorna zeros
        if (!"horista".equals(empregado.getTipo())) {
            return new double[]{0, 0};
        }

        double horasNormais = 0;
        double horasExtras = 0;
        Map<String, Double> horasPorDia = new HashMap<>();

        // Coletar todas as horas do período
        for (CartaoPonto cartao : empregado.getCartoesPonto()) {
            Date dataCartao = cartao.getData();
            if (!dataCartao.before(dataInicio) && !dataCartao.after(dataFim)) {
                String dataKey = formatarData(dataCartao);
                horasPorDia.put(dataKey, horasPorDia.getOrDefault(dataKey, 0.0) + cartao.getHoras());
            }
        }

        // Calcular horas normais e extras
        for (Double horasDia : horasPorDia.values()) {
            horasNormais += Math.min(horasDia, 8);
            horasExtras += Math.max(0, horasDia - 8);
        }

        return new double[]{horasNormais, horasExtras};
    }

    private Double calcularPagamentoHorista(Empregado emp, Date data) {
        String empId = getEmpId(emp);
        if (empId == null || empId.isEmpty()) return 0.00;

        Date dataInicioPeriodo = getDataInicioPeriodo(emp, data);

        // Calcular horas do período
        double[] horas = calcularHorasTrabalhadasPeriodo(empId, dataInicioPeriodo, data);
        double horasNormais = horas[0];
        double horasExtras = horas[1];

        double salarioHora = emp.getSalario() != null ? emp.getSalario() : 0;
        double valorHorasNormais = horasNormais * salarioHora;
        double valorHorasExtras = horasExtras * salarioHora * 1.5;

        double totalBruto = valorHorasNormais + valorHorasExtras;

        // Deduzir taxas
        totalBruto = deduzirTaxasSindicais(emp, dataInicioPeriodo, data, totalBruto);
        totalBruto = deduzirTaxasServico(emp, dataInicioPeriodo, data, totalBruto);

        totalBruto = Math.max(0, totalBruto);
        emp.setDataUltimoPagamento(data);

        return arredondar(totalBruto);
    }

    private Date getInicioPeriodoQuinzenal(Empregado emp, Date dataFim) {
        // Retorna a data de início do período quinzenal
        Calendar cal = Calendar.getInstance();
        cal.setTime(dataFim);
        cal.add(Calendar.DAY_OF_MONTH, -14); // Período de 14 dias
        return cal.getTime();
    }

    private double calcularPagamentoAssalariado(Empregado emp, Date data) {
        // Verifica se é o dia de pagamento
        if (!ehDiaDePagamento(emp, data)) {
            return 0;
        }

        double salarioMensal = emp.getSalario() != null ? emp.getSalario() : 0;
        return salarioMensal; // Pagamento mensal fixo
    }

    private double calcularPagamentoComissionado(Empregado emp, Date data) {
        // Verifica se é o dia de pagamento (quinzenal)
        if (!ehDiaDePagamento(emp, data)) {
            return 0;
        }

        // Calcula vendas do período
        Date inicioPeriodo = getInicioPeriodoQuinzenal(emp, data);
        double totalVendas = calcularVendasPeriodo(emp, inicioPeriodo, data);
        double comissao = emp.getComissao() != null ? emp.getComissao() : 0;
        double valorComissao = totalVendas * comissao;

        // Salário base quinzenal
        double salarioBase = emp.getSalario() != null ? emp.getSalario() : 0;
        double salarioQuinzenal = (salarioBase * 12) / 24; // Divide o anual em 24 quinzenas

        return salarioQuinzenal + valorComissao;
    }

    private double calcularVendasPeriodo(Empregado emp, Date dataInicio, Date dataFim) {
        double totalVendas = 0;
        for (Venda venda : emp.getVendas()) {
            Date dataVenda = venda.getData();
            if (!dataVenda.before(dataInicio) && !dataVenda.after(dataFim)) {
                totalVendas += venda.getValor();
            }
        }
        return totalVendas;
    }

    private double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    private double parseDouble(String valor) {
        if (valor == null || valor.isEmpty()) return 0;
        return Double.parseDouble(valor.replace(',', '.'));
    }

    private Date getDataInicioPeriodo(Empregado emp, Date dataFim) {
        if (emp.getDataUltimoPagamento() != null) {
            return emp.getDataUltimoPagamento();
        }

        return getDataContrato(emp);
    }

    private Date getDataContrato(Empregado emp) {
        if (emp.getDataContrato() != null) {
            return emp.getDataContrato();
        }

        if ("horista".equals(emp.getTipo())) {
            return encontrarPrimeiroCartao(emp);
        } else {
            return parseData("1/1/2005");
        }
    }

    private double deduzirTaxasSindicais(Empregado emp, Date dataInicio, Date dataFim, double totalBruto) {
        if (Boolean.TRUE.equals(emp.getSindicalizado()) && emp.getTaxaSindical() != null) {
            long diasTrabalhados = calcularDiasEntreDatas(dataInicio, dataFim);
            double taxaSindicalTotal = emp.getTaxaSindical() * diasTrabalhados;
            totalBruto -= taxaSindicalTotal;
        }
        return totalBruto;
    }

    private double deduzirTaxasServico(Empregado emp, Date dataInicio, Date dataFim, double totalBruto) {
        if (Boolean.TRUE.equals(emp.getSindicalizado())) {
            double taxasServico = calcularTaxasServicoPeriodo(emp, dataInicio, dataFim);
            totalBruto -= taxasServico;
        }
        return totalBruto;
    }

    private double calcularTaxasServicoPeriodo(Empregado emp, Date dataInicio, Date dataFim) {
        double totalTaxas = 0;
        if (emp.getTaxasServico() != null) {
            for (TaxaServico taxa : emp.getTaxasServico()) {
                if (!taxa.getData().before(dataInicio) && !taxa.getData().after(dataFim)) {
                    totalTaxas += taxa.getValor();
                }
            }
        }
        return totalTaxas;
    }

    private String gerarLinhaFolha(Empregado emp, double pagamento) {
        String metodoPagamento = emp.getMetodoPagamento();

        if ("emMaos".equals(metodoPagamento)) {
            return String.format("%s\t%.2f", emp.getNome(), pagamento);
        } else if ("correios".equals(metodoPagamento)) {
            return String.format("%s\t%s\t%.2f", emp.getNome(), emp.getEndereco(), pagamento);
        } else if ("banco".equals(metodoPagamento)) {
            return String.format("%s\t%s\t%s\t%.2f", emp.getNome(), emp.getBanco(),
                    emp.getAgencia() + "/" + emp.getContaCorrente(), pagamento);
        }

        return String.format("%s\t%.2f", emp.getNome(), pagamento);
    }

    private String getEmpId(Empregado emp) {
        for (Map.Entry<String, Empregado> entry : empregados.entrySet()) {
            if (entry.getValue().equals(emp)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private Date encontrarPrimeiroCartao(Empregado emp) {
        if (emp.getCartoesPonto() != null && !emp.getCartoesPonto().isEmpty()) {
            List<CartaoPonto> cartoes = new ArrayList<>(emp.getCartoesPonto());
            cartoes.sort(Comparator.comparing(CartaoPonto::getData));
            return cartoes.get(0).getData();
        }
        return null;
    }

    private long calcularDiasEntreDatas(Date inicio, Date fim) {
        long diff = fim.getTime() - inicio.getTime();
        return diff / (24 * 60 * 60 * 1000) + 1;
    }

    public int getNumeroDeEmpregados() {
        return empregados.size();
    }
}