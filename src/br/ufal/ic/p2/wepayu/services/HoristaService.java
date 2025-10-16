package br.ufal.ic.p2.wepayu.services;

import br.ufal.ic.p2.wepayu.models.*;
import br.ufal.ic.p2.wepayu.exceptions.*;
import br.ufal.ic.p2.wepayu.utils.ConversorUtils;
import java.time.LocalDate;

public class HoristaService {

    public static void lancaCartao(Empregado e, String data, String horas)
            throws DataInvalidaException {
        if (!(e instanceof Horista)) {
            throw new IllegalArgumentException("Empregado nao eh horista.");
        }

        Horista horista = (Horista) e;
        LocalDate dataCartao = ConversorUtils.stringToDate(data, "data");
        horista.addCartao(new CartaoPonto(dataCartao, horas));
    }

    public static String getHorasNormaisTrabalhadas(Empregado e, String dataInicial, String dataFinal)
            throws DataInvalidaException {
        if (!(e instanceof Horista)) {
            throw new IllegalArgumentException("Empregado nao eh horista.");
        }

        Horista horista = (Horista) e;
        LocalDate inicio = ConversorUtils.stringToDate(dataInicial, "data inicial");
        LocalDate fim = ConversorUtils.stringToDate(dataFinal, "data final");

        if (inicio.isAfter(fim)) {
            throw new DataInvalidaException("Data inicial nao pode ser posterior a data final.");
        }

        double horasNormais = 0;
        for (CartaoPonto cartao : horista.getCartoes()) {
            LocalDate dataCartao = cartao.getData();
            if (!dataCartao.isBefore(inicio) && !dataCartao.isAfter(fim)) {
                double horas = cartao.getHoras();
                if (horas <= 8) {
                    horasNormais += horas;
                } else {
                    horasNormais += 8;
                }
            }
        }

        return ConversorUtils.converteSalario(horasNormais);
    }

    public static String getHorasExtrasTrabalhadas(Empregado e, String dataInicial, String dataFinal)
            throws DataInvalidaException {
        if (!(e instanceof Horista)) {
            throw new IllegalArgumentException("Empregado nao eh horista.");
        }

        Horista horista = (Horista) e;
        LocalDate inicio = ConversorUtils.stringToDate(dataInicial, "data inicial");
        LocalDate fim = ConversorUtils.stringToDate(dataFinal, "data final");

        if (inicio.isAfter(fim)) {
            throw new DataInvalidaException("Data inicial nao pode ser posterior a data final.");
        }

        double horasExtras = 0;
        for (CartaoPonto cartao : horista.getCartoes()) {
            LocalDate dataCartao = cartao.getData();
            if (!dataCartao.isBefore(inicio) && !dataCartao.isAfter(fim)) {
                double horas = cartao.getHoras();
                if (horas > 8) {
                    horasExtras += (horas - 8);
                }
            }
        }

        return ConversorUtils.converteSalario(horasExtras);
    }
}