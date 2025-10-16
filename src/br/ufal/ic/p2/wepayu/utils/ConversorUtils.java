package br.ufal.ic.p2.wepayu.utils;

import br.ufal.ic.p2.wepayu.exceptions.DataInvalidaException;
import java.time.LocalDate;

public class ConversorUtils {

    public static LocalDate stringToDate(String data, String tipo) throws DataInvalidaException {
        try {
            String[] partes = data.split("/");
            int dia = Integer.parseInt(partes[0]);
            int mes = Integer.parseInt(partes[1]);
            int ano = Integer.parseInt(partes[2]);

            // Validações básicas de data
            if (dia < 1 || dia > 31 || mes < 1 || mes > 12 || ano < 1) {
                throw new DataInvalidaException();
            }

            // Validações específicas por mês
            if ((mes == 4 || mes == 6 || mes == 9 || mes == 11) && dia > 30) {
                throw new DataInvalidaException();
            }

            // Fevereiro - simplificado (não considera anos bissextos)
            if (mes == 2 && dia > 28) {
                throw new DataInvalidaException();
            }

            return LocalDate.of(ano, mes, dia);
        } catch (Exception e) {
            throw new DataInvalidaException("Data " + data + " invalida.");
        }
    }

    public static String converteSalario(double salario) {
        double arredondado = Math.round(salario * 100.0) / 100.0;
        return String.format("%.2f", arredondado).replace(".", ",");
    }

    public static String formataData(LocalDate data) {
        return String.format("%02d/%02d/%04d", data.getDayOfMonth(), data.getMonthValue(), data.getYear());
    }
}