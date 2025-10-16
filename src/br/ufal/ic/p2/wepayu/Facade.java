package br.ufal.ic.p2.wepayu;

import br.ufal.ic.p2.wepayu.models.*;
import br.ufal.ic.p2.wepayu.services.*;
import br.ufal.ic.p2.wepayu.utils.*;
import br.ufal.ic.p2.wepayu.exceptions.*;

import java.beans.XMLEncoder;
import java.beans.XMLDecoder;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Facade implements Serializable {
    private List<Empregado> empregados = new ArrayList<>();
    private Map<String, String> dadosSindicais = new TreeMap<>(); // id -> idSindicato
    private Map<String, List<String>> folha = new TreeMap<>(); // data -> lista de IDs
    private Map<String, Map<String, String>> folhaPorTipo = new TreeMap<>(); // data -> (tipo -> total)
    private List<String> tiposAgenda = new ArrayList<>();
    private Historico historico = new Historico();

    private static final List<String> AGENDAS_PADRAO = Arrays.asList("mensal $", "semanal 5", "semanal 2 5");

    public Facade() {
        iniciarSistema();
    }

    // ========== MÉTODOS DE SISTEMA ==========

    public void iniciarSistema() {
        try {
            FileInputStream f = new FileInputStream("empregados.xml");
            XMLDecoder decoder = new XMLDecoder(f);
            List<Object> dados = (List<Object>) decoder.readObject();
            this.empregados = (ArrayList<Empregado>) dados.get(0);
            this.tiposAgenda = (List<String>) dados.get(1);
            decoder.close();
            f.close();
        } catch (FileNotFoundException e) {
            this.empregados = new ArrayList<>();
            this.tiposAgenda = new ArrayList<>(AGENDAS_PADRAO);
        } catch (IOException e) {
            this.empregados = new ArrayList<>();
            this.tiposAgenda = new ArrayList<>(AGENDAS_PADRAO);
        }
    }

    public void zerarSistema() {
        salvarEstado();
        empregados.clear();
        dadosSindicais.clear();
        folha.clear();
        folhaPorTipo.clear();
        tiposAgenda.clear();
        tiposAgenda.addAll(AGENDAS_PADRAO);
    }

    public void encerrarSistema() {
        try {
            FileOutputStream f = new FileOutputStream("empregados.xml");
            XMLEncoder encoder = new XMLEncoder(f);
            List<Object> dados = Arrays.asList(this.empregados, this.tiposAgenda);
            encoder.writeObject(dados);
            encoder.close();
            f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // ========== MÉTODOS DE EMPREGADOS ==========

    public String criarEmpregado(String nome, String endereco, String tipo, String salario)
            throws AtributoNaoNuloException {
        validarAtributosObrigatorios(nome, endereco, tipo, salario);
        ValidacaoUtils.verificarSalario(salario);

        Empregado empregado = EmpregadoService.criarEmpregado(nome, endereco, tipo, salario);
        salvarEstado();
        empregados.add(empregado);

        return String.valueOf(empregado.getId());
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario, String comissao)
            throws AtributoNaoNuloException {
        validarAtributosObrigatorios(nome, endereco, tipo, salario);
        if (comissao == null || comissao.isEmpty()) {
            throw new AtributoNaoNuloException("Comissao nao pode ser nula.");
        }
        ValidacaoUtils.verificarSalario(salario);

        Empregado empregado = EmpregadoService.criarEmpregado(nome, endereco, tipo, salario, comissao);
        salvarEstado();
        empregados.add(empregado);

        return String.valueOf(empregado.getId());
    }

    public String getAtributoEmpregado(String emp, String atributo)
            throws EmpregadoNaoExisteException, NaoSindicalizadoException {
        Empregado empregado = buscarEmpregadoPorId(emp);
        return EmpregadoService.getAtributoEmpregado(empregado, atributo, dadosSindicais, empregados);
    }

    public String getEmpregadoPorNome(String nome, String indice) {
        int index = Integer.parseInt(indice);
        int count = 0;

        for (Empregado empregado : empregados) {
            if (empregado.getNome().equals(nome)) {
                count++;
                if (count == index) {
                    return String.valueOf(empregado.getId());
                }
            }
        }

        throw new RuntimeException("Nao ha empregado com esse nome.");
    }

    public void removerEmpregado(String emp) throws EmpregadoNaoExisteException {
        Empregado empregado = buscarEmpregadoPorId(emp);
        salvarEstado();
        empregados.remove(empregado);

        // Remove do sindicato se estiver sindicalizado
        if (empregado.isSindicalizado()) {
            dadosSindicais.remove(emp);
        }
    }

    // ========== MÉTODOS DE ALTERAÇÃO DE EMPREGADOS ==========

    public void alteraEmpregado(String emp, String atributo, String valor)
            throws EmpregadoNaoExisteException {
        Empregado empregado = buscarEmpregadoPorId(emp);
        salvarEstado();

        Empregado novoEmpregado = EmpregadoService.alteraEmpregado(emp, atributo, valor, empregado, empregados, tiposAgenda);

        if (atributo.equals("tipo") && novoEmpregado != empregado) {
            empregados.remove(empregado);
            empregados.add(novoEmpregado);
        } else if (atributo.equals("agendaPagamento")) {
            EmpregadoService.mudaFolhaEmpregado(empregado, valor, folha);
        }
    }

    public void alteraEmpregado(String emp, String atributo, String valor, String comissao)
            throws EmpregadoNaoExisteException {
        Empregado empregado = buscarEmpregadoPorId(emp);
        salvarEstado();

        if (atributo.equals("tipo")) {
            Empregado novoEmpregado = EmpregadoService.alteraEmpregado(emp, atributo, valor, comissao, empregado);
            empregados.remove(empregado);
            empregados.add(novoEmpregado);
        }
    }

    public void alteraEmpregado(String emp, String atributo, String valor,
                                String idSindicato, String taxaSindical)
            throws EmpregadoNaoExisteException {
        Empregado empregado = buscarEmpregadoPorId(emp);

        if (atributo.equals("sindicalizado")) {
            // Verifica se o ID do sindicato já existe para outro empregado
            for (Map.Entry<String, String> entry : dadosSindicais.entrySet()) {
                if (idSindicato.equals(entry.getValue()) && !entry.getKey().equals(emp)) {
                    throw new IdIgualException();
                }
            }

            boolean sindicalizado = Boolean.parseBoolean(valor);
            validarTaxaSindical(taxaSindical);
            validarIdSindicato(idSindicato);

            double taxa = Double.parseDouble(taxaSindical.replace(",", "."));
            if (taxa < 0) {
                throw new RuntimeException("Taxa sindical deve ser nao-negativa.");
            }

            salvarEstado();
            empregado.setSindicalizado(sindicalizado, idSindicato, taxa);

            if (sindicalizado) {
                dadosSindicais.put(emp, idSindicato);
            } else {
                dadosSindicais.remove(emp);
            }
        }
    }

    public void alteraEmpregado(String emp, String atributo, String valor1,
                                String banco, String agencia, String contaCorrente)
            throws EmpregadoNaoExisteException {
        Empregado empregado = buscarEmpregadoPorId(emp);

        if (atributo.equals("metodoPagamento") && "banco".equals(valor1)) {
            validarDadosBancarios(banco, agencia, contaCorrente);
            salvarEstado();
            empregado.setMetodoPagamento(valor1, banco, agencia, contaCorrente);
        }
    }

    // ========== MÉTODOS DE CARTÃO DE PONTO ==========

    public void lancaCartao(String emp, String data, String horas)
            throws EmpregadoNaoExisteException, DataInvalidaException {
        Empregado empregado = buscarEmpregadoPorId(emp);
        salvarEstado();
        HoristaService.lancaCartao(empregado, data, horas);
    }

    public String getHorasNormaisTrabalhadas(String emp, String dataInicial, String dataFinal)
            throws EmpregadoNaoExisteException, DataInvalidaException {
        Empregado empregado = buscarEmpregadoPorId(emp);
        return HoristaService.getHorasNormaisTrabalhadas(empregado, dataInicial, dataFinal);
    }

    public String getHorasExtrasTrabalhadas(String emp, String dataInicial, String dataFinal)
            throws EmpregadoNaoExisteException, DataInvalidaException {
        Empregado empregado = buscarEmpregadoPorId(emp);
        return HoristaService.getHorasExtrasTrabalhadas(empregado, dataInicial, dataFinal);
    }

    // ========== MÉTODOS DE VENDAS ==========

    public void lancaVenda(String emp, String data, String valor)
            throws EmpregadoNaoExisteException, DataInvalidaException {
        Empregado empregado = buscarEmpregadoPorId(emp);

        if (!(empregado instanceof Comissionado)) {
            throw new RuntimeException("Empregado nao eh comissionado.");
        }

        Comissionado comissionado = (Comissionado) empregado;
        LocalDate dataVenda = ConversorUtils.stringToDate(data, "data");
        double valorVenda = Double.parseDouble(valor.replace(",", "."));

        if (valorVenda <= 0) {
            throw new ValorNaoNuloException();
        }

        salvarEstado();
        comissionado.setVendas(dataVenda, valorVenda);
    }

    public String getVendasRealizadas(String emp, String dataInicial, String dataFinal)
            throws EmpregadoNaoExisteException, DataInvalidaException {
        Empregado empregado = buscarEmpregadoPorId(emp);
        return ComissionadoService.getVendasRealizadas(empregado, dataInicial, dataFinal);
    }

    // ========== MÉTODOS DE SINDICATO ==========

    public void lancaTaxaServico(String membro, String data, String valor)
            throws EmpregadoNaoExisteException, DataInvalidaException,
            MembroNaoExisteException, IdNaoNuloException {
        SindicatoService.validarMembro(membro);
        Empregado empregado = SindicatoService.getEmpSindicato(membro, dadosSindicais, empregados);
        salvarEstado();
        SindicatoService.lancaTaxaServico(empregado, data, valor, dadosSindicais, empregados);
    }

    public String getTaxasServico(String emp, String dataInicial, String dataFinal)
            throws EmpregadoNaoExisteException, DataInvalidaException {
        Empregado empregado = buscarEmpregadoPorId(emp);
        return SindicatoService.getTaxasServico(empregado, dataInicial, dataFinal);
    }

    // ========== MÉTODOS DE FOLHA DE PAGAMENTO ==========

    public String totalFolha(String data) throws DataInvalidaException {
        double total = 0;
        double totalAssalariados = 0;
        double totalComissionados = 0;
        double totalHoristas = 0;

        LocalDate dataFolha = ConversorUtils.stringToDate(data, "data");

        for (Empregado empregado : empregados) {
            boolean recebeHoje = EmpregadoService.recebeHoje(dataFolha, empregado);

            if (recebeHoje) {
                if (empregado instanceof Assalariado) {
                    double salario = empregado.getSalarioBruto(dataFolha);
                    totalAssalariados += salario;
                    total += salario;
                } else if (empregado instanceof Comissionado) {
                    Comissionado comissionado = (Comissionado) empregado;
                    double salario = comissionado.getSalario(dataFolha);
                    totalComissionados += salario;
                    total += salario;
                } else if (empregado instanceof Horista) {
                    Horista horista = (Horista) empregado;
                    double salario = horista.getSalarioBruto(dataFolha);
                    totalHoristas += salario;
                    total += salario;
                }

                // Adiciona à folha do dia
                String id = String.valueOf(empregado.getId());
                folha.computeIfAbsent(data, k -> new ArrayList<>()).add(id);
            }
        }

        // Armazena totais por tipo
        Map<String, String> totaisPorTipo = new TreeMap<>();
        totaisPorTipo.put("assalariado", ConversorUtils.converteSalario(totalAssalariados));
        totaisPorTipo.put("comissionado", ConversorUtils.converteSalario(totalComissionados));
        totaisPorTipo.put("horista", ConversorUtils.converteSalario(totalHoristas));
        folhaPorTipo.put(data, totaisPorTipo);

        return ConversorUtils.converteSalario(total);
    }

    public void rodaFolha(String data, String saida)
            throws IOException, DataInvalidaException, EmpregadoNaoExisteException {
        LocalDate dataFolha = ConversorUtils.stringToDate(data, "data");
        salvarEstado();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(saida))) {
            writer.write("FOLHA DE PAGAMENTO DO DIA " + dataFolha);
            writer.newLine();
            writer.newLine();

            // Processa cada tipo de empregado
            processarHoristasFolha(writer, dataFolha);
            processarAssalariadosFolha(writer, dataFolha);
            processarComissionadosFolha(writer, dataFolha);

            // Atualiza última data de pagamento
            atualizarUltimoPagamento(dataFolha);
        }
    }

    public String getNumeroDeEmpregados() {
        return String.valueOf(empregados.size());
    }

    // ========== MÉTODOS DE UNDO/REDO ==========

    public void undo() {
        try {
            Historico.Memento estadoAtual = new Historico.Memento(
                    new ArrayList<>(this.empregados),
                    new TreeMap<>(this.dadosSindicais)
            );
            Historico.Memento estadoAnterior = historico.undo(estadoAtual);
            restaurarEstado(estadoAnterior);
        } catch (IllegalStateException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void redo() {
        try {
            Historico.Memento estadoAtual = new Historico.Memento(
                    new ArrayList<>(this.empregados),
                    new TreeMap<>(this.dadosSindicais)
            );
            Historico.Memento estadoFuturo = historico.redo(estadoAtual);
            restaurarEstado(estadoFuturo);
        } catch (IllegalStateException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    // ========== MÉTODOS DE AGENDA DE PAGAMENTOS ==========

    public void criarAgendaDePagamentos(String descricao) {
        AgendaUtils.verificaCriaAgenda(descricao, tiposAgenda);
    }

    // ========== MÉTODOS PRIVADOS AUXILIARES ==========

    private Empregado buscarEmpregadoPorId(String id) throws EmpregadoNaoExisteException {
        return EmpregadoService.getEmpregadoPorId(id, empregados);
    }

    private void salvarEstado() {
        historico.salvarEstado(empregados, dadosSindicais);
    }

    private void restaurarEstado(Historico.Memento memento) {
        this.empregados.clear();
        this.dadosSindicais.clear();
        this.empregados.addAll(memento.getEmpregados());
        this.dadosSindicais.putAll(memento.getDadosSindicais());
    }

    private void validarAtributosObrigatorios(String nome, String endereco, String tipo, String salario)
            throws AtributoNaoNuloException {
        if (nome == null || nome.isEmpty()) {
            throw new AtributoNaoNuloException("Nome nao pode ser nulo.");
        }
        if (endereco == null || endereco.isEmpty()) {
            throw new AtributoNaoNuloException("Endereco nao pode ser nulo.");
        }
        if (tipo == null || tipo.isEmpty()) {
            throw new AtributoNaoNuloException("Tipo nao pode ser nulo.");
        }
        if (salario == null || salario.isEmpty()) {
            throw new AtributoNaoNuloException("Salario nao pode ser nulo.");
        }
    }

    private void validarTaxaSindical(String taxaSindical) {
        if (taxaSindical == null || taxaSindical.isEmpty()) {
            throw new RuntimeException("Taxa sindical nao pode ser nula.");
        }
    }

    private void validarIdSindicato(String idSindicato) {
        if (idSindicato == null || idSindicato.isEmpty()) {
            throw new RuntimeException("Identificacao do sindicato nao pode ser nula.");
        }
    }

    private void validarDadosBancarios(String banco, String agencia, String contaCorrente) {
        if (banco == null || banco.isEmpty()) {
            throw new RuntimeException("Banco nao pode ser nulo.");
        }
        if (agencia == null || agencia.isEmpty()) {
            throw new RuntimeException("Agencia nao pode ser nulo.");
        }
        if (contaCorrente == null || contaCorrente.isEmpty()) {
            throw new RuntimeException("Conta corrente nao pode ser nulo.");
        }
    }

    private void processarHoristasFolha(BufferedWriter writer, LocalDate data) throws IOException {
        FolhaUtils.printaHoristas(writer, data);

        double totalHn = 0, totalHx = 0, totalBruto = 0, totalDescontos = 0, totalLiquido = 0;
        List<Horista> horistasDoDia = new ArrayList<>();

        List<String> idsDoDia = folha.get(data.toString());
        if (idsDoDia != null) {
            for (String id : idsDoDia) {
                try {
                    Empregado empregado = buscarEmpregadoPorId(id);
                    if (empregado instanceof Horista) {
                        horistasDoDia.add((Horista) empregado);
                    }
                } catch (EmpregadoNaoExisteException e) {
                    // Ignora empregados não encontrados
                }
            }
        }

        horistasDoDia.sort(Comparator.comparing(Horista::getNome));

        for (Horista horista : horistasDoDia) {
            LocalDate inicio = data.minusDays(6);
            double hn = horista.getHnSemanal(inicio, data);
            double hx = horista.getHxSemanal(inicio, data);
            double bruto = horista.getSalarioBruto(data);
            double descontos = horista.getDescontos(data);
            double liquido = horista.getSalarioLiquido(data);
            String metodo = FolhaUtils.formataMetodoPagamento(horista);

            if (bruto <= 0) {
                hn = 0;
                hx = 0;
            }

            FolhaUtils.printaValorHoristas(horista.getNome(), hn, hx, bruto, descontos, liquido, metodo, writer);

            totalHn += hn;
            totalHx += hx;
            totalBruto += bruto;
            totalDescontos += descontos;
            totalLiquido += liquido;

            if (bruto > 0) {
                horista.setUltimoPagamento(data);
            }
        }

        FolhaUtils.printaTotalHoristas(totalHn, totalHx, totalBruto, totalDescontos, totalLiquido, writer);
    }

    private void processarAssalariadosFolha(BufferedWriter writer, LocalDate data) throws IOException {
        FolhaUtils.printaAssalariados(writer, data);

        double totalBruto = 0, totalDescontos = 0, totalLiquido = 0;
        List<Assalariado> assalariadosDoDia = new ArrayList<>();

        List<String> idsDoDia = folha.get(data.toString());
        if (idsDoDia != null) {
            for (String id : idsDoDia) {
                try {
                    Empregado empregado = buscarEmpregadoPorId(id);
                    if (empregado instanceof Assalariado) {
                        assalariadosDoDia.add((Assalariado) empregado);
                    }
                } catch (EmpregadoNaoExisteException e) {
                    // Ignora empregados não encontrados
                }
            }
        }

        assalariadosDoDia.sort(Comparator.comparing(Assalariado::getNome));

        for (Assalariado assalariado : assalariadosDoDia) {
            double bruto = assalariado.getSalario();
            double liquido = assalariado.getSalarioLiquido(data);
            double descontos = assalariado.getDescontos(data);
            String metodo = FolhaUtils.formataMetodoPagamento(assalariado);

            assalariado.setUltimoPagamento(data);

            totalBruto += bruto;
            totalDescontos += descontos;
            totalLiquido += liquido;

            FolhaUtils.printaValorAssalariados(assalariado.getNome(), bruto, liquido, descontos, metodo, writer);
        }

        FolhaUtils.printaTotalAssalariados(totalBruto, totalDescontos, totalLiquido, writer);
    }

    private void processarComissionadosFolha(BufferedWriter writer, LocalDate data) throws IOException {
        FolhaUtils.printaComissionados(writer, data);

        double totalBruto = 0, totalDescontos = 0, totalLiquido = 0;
        double totalFixo = 0, totalVendas = 0, totalComissao = 0;
        List<Comissionado> comissionadosDoDia = new ArrayList<>();

        List<String> idsDoDia = folha.get(data.toString());
        if (idsDoDia != null) {
            for (String id : idsDoDia) {
                try {
                    Empregado empregado = buscarEmpregadoPorId(id);
                    if (empregado instanceof Comissionado) {
                        comissionadosDoDia.add((Comissionado) empregado);
                    }
                } catch (EmpregadoNaoExisteException e) {
                    // Ignora empregados não encontrados
                }
            }
        }

        comissionadosDoDia.sort(Comparator.comparing(Comissionado::getNome));

        for (Comissionado comissionado : comissionadosDoDia) {
            double fixo = comissionado.getFixo(data);
            double vendas = comissionado.getTotalVendas(data);
            double comissao = comissionado.getComissaoTotal(data);
            double bruto = comissionado.getSalario(data);
            double liquido = comissionado.getSalarioLiquido(data);
            double descontos = comissionado.getDescontos(data);
            String metodo = FolhaUtils.formataMetodoPagamento(comissionado);

            totalBruto += bruto;
            totalDescontos += descontos;
            totalLiquido += liquido;
            totalFixo += fixo;
            totalVendas += vendas;
            totalComissao += comissao;

            FolhaUtils.printaValorComissionados(comissionado.getNome(), fixo, vendas, comissao,
                    bruto, descontos, liquido, metodo, writer);

            comissionado.setUltimoPagamento(data);
        }

        FolhaUtils.printaTotalComissionados(totalFixo, totalVendas, totalComissao,
                totalBruto, totalDescontos, totalLiquido, writer);

        double totalGeral = totalBruto;
        String strTotal = ConversorUtils.converteSalario(totalGeral);
        writer.write("TOTAL FOLHA: " + strTotal);
        writer.newLine();
    }

    private void atualizarUltimoPagamento(LocalDate data) {
        List<String> idsDoDia = folha.get(data.toString());
        if (idsDoDia != null) {
            for (String id : idsDoDia) {
                try {
                    Empregado empregado = buscarEmpregadoPorId(id);
                    if (EmpregadoService.recebeHoje(data, empregado)) {
                        empregado.setUltimoPagamento(data);
                    }
                } catch (EmpregadoNaoExisteException e) {
                    // Ignora empregados não encontrados
                }
            }
        }
    }
}