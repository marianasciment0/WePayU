package br.ufal.ic.p2.wepayu.utils;

import java.util.List;

public class AgendaUtils {

    public static void verificaCriaAgenda(String descricao, List<String> tiposAgenda) {
        if (descricao == null || descricao.isEmpty()) {
            throw new RuntimeException("Descricao da agenda nao pode ser nula.");
        }

        if (!tiposAgenda.contains(descricao)) {
            tiposAgenda.add(descricao);
        }
    }

    public static boolean validaAgenda(String agenda, List<String> tiposAgenda) {
        return tiposAgenda.contains(agenda);
    }
}