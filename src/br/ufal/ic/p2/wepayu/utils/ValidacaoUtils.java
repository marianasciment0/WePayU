package br.ufal.ic.p2.wepayu.utils;

import br.ufal.ic.p2.wepayu.exceptions.AtributoNaoNuloException;

public class ValidacaoUtils {

    public static void verificarSalario(String salario) throws AtributoNaoNuloException {
        if (salario == null || salario.isEmpty()) {
            throw new AtributoNaoNuloException("Salario nao pode ser nulo.");
        }
        try {
            double s = Double.parseDouble(salario.replace(",", "."));
            if (s <= 0) {
                throw new AtributoNaoNuloException("Salario deve ser positivo.");
            }
        } catch (NumberFormatException e) {
            throw new AtributoNaoNuloException("Salario deve ser numerico.");
        }
    }

    public static void verificarComissao(String comissao) throws AtributoNaoNuloException {
        if (comissao == null || comissao.isEmpty()) {
            throw new AtributoNaoNuloException("Comissao nao pode ser nula.");
        }
        try {
            double c = Double.parseDouble(comissao.replace(",", "."));
            if (c < 0) {
                throw new AtributoNaoNuloException("Comissao deve ser nao-negativa.");
            }
        } catch (NumberFormatException e) {
            throw new AtributoNaoNuloException("Comissao deve ser numerica.");
        }
    }

    public static void verificarHoras(String horas) throws AtributoNaoNuloException {
        if (horas == null || horas.isEmpty()) {
            throw new AtributoNaoNuloException("Horas nao podem ser nulas.");
        }
        try {
            double h = Double.parseDouble(horas.replace(",", "."));
            if (h < 0) {
                throw new AtributoNaoNuloException("Horas devem ser nao-negativas.");
            }
        } catch (NumberFormatException e) {
            throw new AtributoNaoNuloException("Horas devem ser numericas.");
        }
    }
}