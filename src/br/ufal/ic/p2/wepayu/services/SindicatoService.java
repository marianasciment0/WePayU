package br.ufal.ic.p2.wepayu.services;

import br.ufal.ic.p2.wepayu.models.*;
import br.ufal.ic.p2.wepayu.exceptions.*;
import br.ufal.ic.p2.wepayu.utils.ConversorUtils;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class SindicatoService {

    public static void validarMembro(String membro) throws MembroNaoExisteException {
        if (membro == null || membro.isEmpty()) {
            throw new MembroNaoExisteException("Identificacao do membro nao pode ser nula.");
        }
    }

    public static Empregado getEmpSindicato(String membro, Map<String, String> dadosSindicais,
                                            List<Empregado> empregados) throws EmpregadoNaoExisteException {
        // Encontra o ID do empregado pelo ID do sindicato
        String empId = null;
        for (Map.Entry<String, String> entry : dadosSindicais.entrySet()) {
            if (membro.equals(entry.getValue())) {
                empId = entry.getKey();
                break;
            }
        }

        if (empId == null) {
            throw new EmpregadoNaoExisteException("Membro nao encontrado: " + membro);
        }

        return EmpregadoService.getEmpregadoPorId(empId, empregados);
    }

    public static void lancaTaxaServico(Empregado e, String data, String valor,
                                        Map<String, String> dadosSindicais,
                                        List<Empregado> empregados) throws DataInvalidaException {
        if (!e.isSindicalizado()) {
            throw new IllegalArgumentException("Empregado nao eh sindicalizado.");
        }

        LocalDate dataTaxa = ConversorUtils.stringToDate(data, "data");
        e.addTaxaServico(new TaxaServico(dataTaxa, valor));
    }

    public static String getTaxasServico(Empregado e, String dataInicial, String dataFinal)
            throws DataInvalidaException {
        if (!e.isSindicalizado()) {
            throw new NaoSindicalizadoException();
        }

        LocalDate inicio = ConversorUtils.stringToDate(dataInicial, "data inicial");
        LocalDate fim = ConversorUtils.stringToDate(dataFinal, "data final");

        if (inicio.isAfter(fim)) {
            throw new DataInvalidaException("Data inicial nao pode ser posterior a data final.");
        }

        double totalTaxas = 0;
        for (TaxaServico taxa : e.getTaxasServico()) {
            LocalDate dataTaxa = taxa.getData();
            if (!dataTaxa.isBefore(inicio) && !dataTaxa.isAfter(fim)) {
                totalTaxas += taxa.getValor();
            }
        }

        return ConversorUtils.converteSalario(totalTaxas);
    }
}