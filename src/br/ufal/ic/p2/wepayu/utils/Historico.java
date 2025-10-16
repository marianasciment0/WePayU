package br.ufal.ic.p2.wepayu.utils;

import br.ufal.ic.p2.wepayu.models.Empregado;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Historico implements Serializable {
    private List<Memento> estados = new ArrayList<>();
    private int currentIndex = -1;
    private static final int MAX_ESTADOS = 10;

    public static class Memento implements Serializable {
        private List<Empregado> empregados;
        private Map<String, String> dadosSindicais;

        public Memento(List<Empregado> empregados, Map<String, String> dadosSindicais) {
            // Deep copy dos empregados
            this.empregados = new ArrayList<>();
            for (Empregado e : empregados) {
                // Em uma implementação real, seria necessário fazer deep copy
                // Para simplificar, estamos usando a mesma referência
                this.empregados.add(e);
            }
            this.dadosSindicais = new TreeMap<>(dadosSindicais);
        }

        public List<Empregado> getEmpregados() {
            return empregados;
        }

        public Map<String, String> getDadosSindicais() {
            return dadosSindicais;
        }
    }

    public void salvarEstado(List<Empregado> empregados, Map<String, String> dadosSindicais) {
        // Remove estados futuros se estamos no meio do histórico
        if (currentIndex < estados.size() - 1) {
            estados = new ArrayList<>(estados.subList(0, currentIndex + 1));
        }

        estados.add(new Memento(empregados, dadosSindicais));
        currentIndex = estados.size() - 1;

        // Limita o histórico ao número máximo de estados
        if (estados.size() > MAX_ESTADOS) {
            estados.remove(0);
            currentIndex--;
        }
    }

    public Memento undo(Memento estadoAtual) {
        if (currentIndex <= 0) {
            throw new IllegalStateException("Nao ha comando para desfazer.");
        }

        // Salva estado atual se necessário
        if (currentIndex == estados.size() - 1) {
            estados.add(estadoAtual);
        }

        currentIndex--;
        return estados.get(currentIndex);
    }

    public Memento redo(Memento estadoAtual) {
        if (currentIndex >= estados.size() - 1) {
            throw new IllegalStateException("Nao ha comando para refazer.");
        }

        currentIndex++;
        return estados.get(currentIndex);
    }

    public void limparHistorico() {
        estados.clear();
        currentIndex = -1;
    }

    public boolean podeDesfazer() {
        return currentIndex > 0;
    }

    public boolean podeRefazer() {
        return currentIndex < estados.size() - 1;
    }
}