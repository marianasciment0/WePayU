package br.ufal.ic.p2.wepayu.utils;

import br.ufal.ic.p2.wepayu.models.Empregado;
import java.io.BufferedWriter;
import java.io.IOException;

public class FolhaUtils {

    public static String formataMetodoPagamento(Empregado e) {
        if ("banco".equals(e.getMetodoPagamento())) {
            return "Banco " + e.getBanco() + " Ag " + e.getAgencia() + " CC " + e.getContaCorrente();
        } else {
            return "Em maos";
        }
    }

    public static void printaHoristas(BufferedWriter writer, java.time.LocalDate d) throws IOException {
        writer.write("HORISTAS:");
        writer.newLine();
        writer.write(String.format("%-40s %10s %10s %10s %10s %10s %-30s",
                "Nome", "Horas", "Extras", "Bruto", "Descontos", "Liquido", "Metodo"));
        writer.newLine();
    }

    public static void printaValorHoristas(String nome, double hn, double hx, double bruto,
                                           double descontos, double liquido, String metodo,
                                           BufferedWriter writer) throws IOException {
        writer.write(String.format("%-40s %10.1f %10.1f %10s %10s %10s %-30s",
                nome, hn, hx, ConversorUtils.converteSalario(bruto),
                ConversorUtils.converteSalario(descontos), ConversorUtils.converteSalario(liquido),
                metodo));
        writer.newLine();
    }

    public static void printaTotalHoristas(double hn, double hx, double bruto,
                                           double descontos, double liquido,
                                           BufferedWriter writer) throws IOException {
        writer.write(String.format("%-40s %10.1f %10.1f %10s %10s %10s",
                "TOTAL HORISTAS", hn, hx, ConversorUtils.converteSalario(bruto),
                ConversorUtils.converteSalario(descontos), ConversorUtils.converteSalario(liquido)));
        writer.newLine();
        writer.newLine();
    }

    public static void printaAssalariados(BufferedWriter writer, java.time.LocalDate d) throws IOException {
        writer.write("ASSALARIADOS:");
        writer.newLine();
        writer.write(String.format("%-40s %10s %10s %10s %-30s",
                "Nome", "Bruto", "Liquido", "Descontos", "Metodo"));
        writer.newLine();
    }

    public static void printaValorAssalariados(String nome, double bruto, double liquido,
                                               double descontos, String metodo,
                                               BufferedWriter writer) throws IOException {
        writer.write(String.format("%-40s %10s %10s %10s %-30s",
                nome, ConversorUtils.converteSalario(bruto),
                ConversorUtils.converteSalario(liquido),
                ConversorUtils.converteSalario(descontos), metodo));
        writer.newLine();
    }

    public static void printaTotalAssalariados(double bruto, double descontos, double liquido,
                                               BufferedWriter writer) throws IOException {
        writer.write(String.format("%-40s %10s %10s %10s",
                "TOTAL ASSALARIADOS", ConversorUtils.converteSalario(bruto),
                ConversorUtils.converteSalario(liquido),
                ConversorUtils.converteSalario(descontos)));
        writer.newLine();
        writer.newLine();
    }

    public static void printaComissionados(BufferedWriter writer, java.time.LocalDate d) throws IOException {
        writer.write("COMISSIONADOS:");
        writer.newLine();
        writer.write(String.format("%-40s %10s %10s %10s %10s %10s %10s %-30s",
                "Nome", "Fixo", "Vendas", "Comissao", "Bruto", "Descontos", "Liquido", "Metodo"));
        writer.newLine();
    }

    public static void printaValorComissionados(String nome, double fixo, double vendas,
                                                double comissao, double bruto, double descontos,
                                                double liquido, String metodo,
                                                BufferedWriter writer) throws IOException {
        writer.write(String.format("%-40s %10s %10s %10s %10s %10s %10s %-30s",
                nome, ConversorUtils.converteSalario(fixo), ConversorUtils.converteSalario(vendas),
                ConversorUtils.converteSalario(comissao), ConversorUtils.converteSalario(bruto),
                ConversorUtils.converteSalario(descontos), ConversorUtils.converteSalario(liquido),
                metodo));
        writer.newLine();
    }

    public static void printaTotalComissionados(double fixo, double vendas, double comissao,
                                                double bruto, double descontos, double liquido,
                                                BufferedWriter writer) throws IOException {
        writer.write(String.format("%-40s %10s %10s %10s %10s %10s %10s",
                "TOTAL COMISSIONADOS", ConversorUtils.converteSalario(fixo),
                ConversorUtils.converteSalario(vendas), ConversorUtils.converteSalario(comissao),
                ConversorUtils.converteSalario(bruto), ConversorUtils.converteSalario(descontos),
                ConversorUtils.converteSalario(liquido)));
        writer.newLine();
        writer.newLine();
    }
}