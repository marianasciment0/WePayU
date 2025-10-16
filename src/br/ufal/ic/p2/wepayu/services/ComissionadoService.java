package br.ufal.ic.p2.wepayu.services;

import br.ufal.ic.p2.wepayu.models.*;
import br.ufal.ic.p2.wepayu.exceptions.*;
import br.ufal.ic.p2.wepayu.utils.ConversorUtils;
import java.time.LocalDate;

public class ComissionadoService {

    public static String getVendasRealizadas(Empregado e, String dataInicial, String dataFinal)
            throws DataInvalidaException {
        if (!(e instanceof Comissionado)) {
            throw new IllegalArgumentException("Empregado nao eh comissionado.");
        }

        Comissionado comissionado = (Comissionado) e;
        LocalDate inicio = ConversorUtils.stringToDate(dataInicial, "data inicial");
        LocalDate fim = ConversorUtils.stringToDate(dataFinal, "data final");

        if (inicio.isAfter(fim)) {
            throw new DataInvalidaException("Data inicial nao pode ser posterior a data final.");
        }

        double totalVendas = 0;
        for (Venda venda : comissionado.getVendas()) {
            LocalDate dataVenda = venda.getData();
            if (!dataVenda.isBefore(inicio) && !dataVenda.isAfter(fim)) {
                totalVendas += venda.getValor();
            }
        }

        return ConversorUtils.converteSalario(totalVendas);
    }
}