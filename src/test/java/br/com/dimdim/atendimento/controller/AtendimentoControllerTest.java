package br.com.dimdim.atendimento.controller;

import br.com.dimdim.atendimento.entity.Atendimento;
import br.com.dimdim.atendimento.entity.Cliente;
import br.com.dimdim.atendimento.entity.StatusAtendimento;
import br.com.dimdim.atendimento.service.AtendimentoService;
import br.com.dimdim.atendimento.service.ClienteSelecionadoInvalidoException;
import br.com.dimdim.atendimento.service.ClienteService;
import br.com.dimdim.atendimento.service.RecursoNaoEncontradoException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(AtendimentoController.class)
class AtendimentoControllerTest {
    private static final LocalDate DATA = LocalDate.of(2026, 10, 5);
    @Autowired private MockMvc mvc;
    @MockBean private AtendimentoService atendimentos;
    @MockBean private ClienteService clientes;

    @Test
    void novoFormularioApresentaClientesCadastrados() throws Exception {
        when(clientes.listar()).thenReturn(List.of(cliente()));

        mvc.perform(get("/atendimentos/novo"))
                .andExpect(status().isOk())
                .andExpect(view().name("atendimentos/formulario"))
                .andExpect(content().string(containsString("Cliente teste (#2)")))
                .andExpect(content().string(containsString("Registrar atendimento")));
    }

    @Test
    void bindingInvalidoDeClienteDataESituacaoMostraErrosSemSalvar() throws Exception {
        when(clientes.listar()).thenReturn(List.of(cliente()));

        mvc.perform(post("/atendimentos").param("assunto", "Dúvida").param("descricao", "Orientação")
                        .param("clienteId", "abc").param("status", "INVALIDO").param("dataAbertura", "invalida"))
                .andExpect(status().isOk())
                .andExpect(view().name("atendimentos/formulario"))
                .andExpect(model().attributeHasFieldErrors("atendimento", "clienteId", "status", "dataAbertura"))
                .andExpect(content().string(containsString("Selecione um cliente cadastrado.")))
                .andExpect(content().string(containsString("Informe uma data válida.")));

        verify(atendimentos, never()).salvar(any(), any(), any(), any(), any(), any());
    }

    @Test
    void clienteInexistenteViraErroDoCampoNoFormulario() throws Exception {
        when(clientes.listar()).thenReturn(List.of(cliente()));
        when(atendimentos.salvar(isNull(), anyString(), anyString(), any(), any(), eq(99L)))
                .thenThrow(new ClienteSelecionadoInvalidoException());

        mvc.perform(post("/atendimentos").param("assunto", "Dúvida").param("descricao", "Orientação")
                        .param("clienteId", "99").param("status", "ABERTO").param("dataAbertura", "2026-10-05"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("atendimento", "clienteId"))
                .andExpect(content().string(containsString("O cliente selecionado não existe.")));
    }

    @Test
    void cadastroValidoSalvaViaFormularioERedirecionaAoDetalhe() throws Exception {
        when(atendimentos.salvar(isNull(), eq("Dúvida"), eq("Orientação"),
                eq(StatusAtendimento.ABERTO), eq(DATA), eq(2L))).thenReturn(atendimento());

        mvc.perform(post("/atendimentos").param("assunto", "Dúvida").param("descricao", "Orientação")
                        .param("clienteId", "2").param("status", "ABERTO").param("dataAbertura", "2026-10-05"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/atendimentos/5"));
    }

    @Test
    void detalheRenderizaSolicitacaoEClienteAssociado() throws Exception {
        when(atendimentos.buscar(5L)).thenReturn(atendimento());

        mvc.perform(get("/atendimentos/5"))
                .andExpect(status().isOk())
                .andExpect(view().name("atendimentos/detalhe"))
                .andExpect(content().string(containsString("Cliente teste")))
                .andExpect(content().string(containsString("05/10/2026")))
                .andExpect(content().string(containsString("Orientação")));
    }

    @Test
    void editarIdInexistenteRetorna404SemSalvar() throws Exception {
        when(atendimentos.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Ausente"));

        mvc.perform(post("/atendimentos/99").param("assunto", "").param("descricao", ""))
                .andExpect(status().isNotFound());

        verify(atendimentos, never()).salvar(any(), any(), any(), any(), any(), anyLong());
    }

    @Test
    void editarFormularioCarregaDataClienteESituacaoExistentes() throws Exception {
        when(atendimentos.buscar(5L)).thenReturn(atendimento());
        when(clientes.listar()).thenReturn(List.of(cliente()));

        mvc.perform(get("/atendimentos/5/editar"))
                .andExpect(status().isOk())
                .andExpect(view().name("atendimentos/formulario"))
                .andExpect(model().attribute("registroId", 5L))
                .andExpect(content().string(containsString("2026-10-05")))
                .andExpect(model().attribute("atendimento", hasProperty("descricao", equalTo("Orientação"))));
    }

    @Test
    void excluirUsaPostERedirecionaALista() throws Exception {
        mvc.perform(post("/atendimentos/5/excluir"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/atendimentos"));

        verify(atendimentos).excluir(5L);
    }

    private Cliente cliente() {
        Cliente cliente = new Cliente();
        cliente.setId(2L);
        cliente.setNome("Cliente teste");
        cliente.setEmail("teste@example.com");
        return cliente;
    }

    private Atendimento atendimento() {
        Atendimento atendimento = new Atendimento();
        atendimento.setId(5L);
        atendimento.setAssunto("Dúvida");
        atendimento.setDescricao("Orientação");
        atendimento.setStatus(StatusAtendimento.ABERTO);
        atendimento.setDataAbertura(DATA);
        atendimento.setCliente(cliente());
        return atendimento;
    }
}
