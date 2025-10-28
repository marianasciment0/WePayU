package br.ufal.ic.p2.wepayu.services;

import java.time.LocalDate;

import br.ufal.ic.p2.wepayu.Exception.*;
import br.ufal.ic.p2.wepayu.models.Empregado;
import br.ufal.ic.p2.wepayu.models.Horista;
import br.ufal.ic.p2.wepayu.utils.ConversorUtils;

public class HoristaService {

    public static void lancaCartao (Empregado e, String data, String horas)
            throws EmpregadoNaoExisteException, DataInvalidaException {
        if (!(e instanceof Horista)) {
            throw new IllegalStateException("Empregado nao eh horista.");
        }
        Horista h = (Horista) e;
        LocalDate d = ConversorUtils.stringToDate(data, "data");
        double hrs = Double.parseDouble(horas.replace(",","."));
        if (hrs <= 0) {
            throw new HoraInvalidaException();
        }
        h.setHoras(d, hrs);
        if (h.getDataInicioD() == null) h.setDataInicio(d);
    }

    public static String getHorasNormaisTrabalhadas (Empregado e, String dataInicial, String dataFinal)
            throws EmpregadoNaoExisteException, DataInvalidaException {

        if (!(e instanceof Horista)) {
            throw new IllegalStateException("Empregado nao eh horista.");
        }
        LocalDate di = ConversorUtils.stringToDate(dataInicial, "inicial");
        LocalDate df = ConversorUtils.stringToDate(dataFinal, "final");
        if (di.isAfter(df)) {
            throw new DataMaiorException();
        }
        double horas = 0;
        Horista h = (Horista) e;
        for (LocalDate date = di; date.isBefore(df); date = date.plusDays(1)) {
            horas += h.getHorasNormais(date);
        }
        return ConversorUtils.formatarHoras(horas);
    }

    public static String getHorasExtrasTrabalhadas (Empregado e, String dataInicial, String dataFinal)
            throws EmpregadoNaoExisteException, DataInvalidaException {
        if (!(e instanceof Horista)) {
            throw new IllegalStateException("Empregado nao eh horista.");
        }
        LocalDate di = ConversorUtils.stringToDate(dataInicial, "inicial");
        LocalDate df = ConversorUtils.stringToDate(dataFinal, "final");
        if (di.isAfter(df)) {
            throw new DataMaiorException();
        }
        double horas = 0;
        Horista h = (Horista) e;
        for (LocalDate date = di; date.isBefore(df); date = date.plusDays(1)) {
            horas += h.getHorasExtras(date);
        }
        return ConversorUtils.formatarHoras(horas);
    }

}