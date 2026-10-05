package br.com.dimdim.atendimento.controller;

import br.com.dimdim.atendimento.entity.Atendimento;
import br.com.dimdim.atendimento.entity.StatusAtendimento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public class AtendimentoForm {
    @NotBlank(message = "Informe o assunto do atendimento.")
    @Size(max = 150, message = "O assunto deve ter até 150 caracteres.")
    private String assunto;

    @NotBlank(message = "Descreva a solicitação do cliente.")
    @Size(max = 1000, message = "A descrição deve ter até 1.000 caracteres.")
    private String descricao;

    @NotNull(message = "Selecione a situação do atendimento.")
    private StatusAtendimento status = StatusAtendimento.ABERTO;

    @NotNull(message = "Informe a data de abertura.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataAbertura = LocalDate.now();

    @NotNull(message = "Selecione o cliente.")
    @Positive(message = "Selecione um cliente cadastrado.")
    private Long clienteId;

    public static AtendimentoForm de(Atendimento atendimento) {
        AtendimentoForm form = new AtendimentoForm();
        form.setAssunto(atendimento.getAssunto());
        form.setDescricao(atendimento.getDescricao());
        form.setStatus(atendimento.getStatus());
        form.setDataAbertura(atendimento.getDataAbertura());
        form.setClienteId(atendimento.getCliente().getId());
        return form;
    }

    public String getAssunto() { return assunto; }
    public void setAssunto(String assunto) { this.assunto = assunto; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public StatusAtendimento getStatus() { return status; }
    public void setStatus(StatusAtendimento status) { this.status = status; }
    public LocalDate getDataAbertura() { return dataAbertura; }
    public void setDataAbertura(LocalDate dataAbertura) { this.dataAbertura = dataAbertura; }
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
}
