package br.com.dimdim.atendimento.entity;

public enum StatusAtendimento {
    ABERTO("Aberto"),
    EM_ANDAMENTO("Em andamento"),
    CONCLUIDO("Concluído");

    private final String descricao;

    StatusAtendimento(String descricao) { this.descricao = descricao; }
    public String getDescricao() { return descricao; }
}
