package br.com.dimdim.atendimento.service;

public class ClienteSelecionadoInvalidoException extends RuntimeException {
    public ClienteSelecionadoInvalidoException() {
        super("O cliente selecionado não existe. Escolha um cliente cadastrado.");
    }
}
