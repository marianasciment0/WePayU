package br.ufal.ic.p2.wepayu.services;

import java.time.LocalDate;

import br.ufal.ic.p2.wepayu.Exception.*;
import br.ufal.ic.p2.wepayu.models.Comissionado;
import br.ufal.ic.p2.wepayu.models.Empregado;
import br.ufal.ic.p2.wepayu.utils.ConversorUtils;

public class ComissionadoService {

    public static String getVendasRealizadas (Empregado e, String dataInicial, String dataFinal)
            throws EmpregadoNaoExisteException, DataInvalidaException {
        if (!(e instanceof Comissionado)) {
            throw new IllegalStateException("Empregado nao eh comissionado.");
        }
        Comissionado com = (Comissionado) e;
        LocalDate dataI = ConversorUtils.stringToDate(dataInicial, "inicial");
        LocalDate dataF = ConversorUtils.stringToDate(dataFinal, "final");
        if (dataI.isAfter(dataF)) {
            throw new DataMaiorException();
        }
        double totalVendas = 0;
        for (LocalDate date = dataI; date.isBefore(dataF); date = date.plusDays(1)) {
            totalVendas += com.getVendas(date);
        }
        return ConversorUtils.converteSalario(totalVendas);
    }

}