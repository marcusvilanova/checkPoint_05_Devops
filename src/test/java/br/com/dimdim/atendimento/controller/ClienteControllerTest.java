package br.com.dimdim.atendimento.controller;

import br.com.dimdim.atendimento.entity.Cliente;
import br.com.dimdim.atendimento.service.ClienteService;
import br.com.dimdim.atendimento.service.RecursoNaoEncontradoException;
import br.com.dimdim.atendimento.service.RegraNegocioException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {
    @Autowired private MockMvc mvc;
    @MockBean private ClienteService clientes;

    @Test
    void formularioInvalidoMostraErrosENaoSalva() throws Exception {
        mvc.perform(post("/clientes").param("nome", "   ").param("email", "invalido"))
                .andExpect(status().isOk())
                .andExpect(view().name("clientes/formulario"))
                .andExpect(model().attributeHasFieldErrors("cliente", "nome", "email"))
                .andExpect(content().string(containsString("Informe o nome do cliente.")))
                .andExpect(content().string(containsString("Informe um e-mail válido.")));

        verify(clientes, never()).salvar(any(), any());
    }

    @Test
    void cadastroUsaApenasCamposPermitidosDoFormulario() throws Exception {
        Cliente salvo = cliente(4L);
        when(clientes.salvar(isNull(), any())).thenReturn(salvo);

        mvc.perform(post("/clientes").param("id", "99").param("nome", "Cliente teste")
                        .param("email", "teste@example.com").param("telefone", "(11) 99999-0000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clientes/4"))
                .andExpect(flash().attribute("mensagem", "Cliente cadastrado com sucesso."));

        verify(clientes).salvar(isNull(), argThat(cliente -> cliente.getId() == null));
    }

    @Test
    void detalhesRenderizamClienteEListaDeAtendimentos() throws Exception {
        when(clientes.buscar(4L)).thenReturn(cliente(4L));
        when(clientes.listarAtendimentos(4L)).thenReturn(List.of());

        mvc.perform(get("/clientes/4"))
                .andExpect(status().isOk())
                .andExpect(view().name("clientes/detalhe"))
                .andExpect(content().string(containsString("Cliente teste")))
                .andExpect(content().string(containsString("Este cliente ainda não possui atendimentos.")));
    }

    @Test
    void atualizarIdAusenteRetorna404MesmoComFormularioInvalido() throws Exception {
        when(clientes.buscar(99L)).thenThrow(new RecursoNaoEncontradoException("Cliente ausente"));

        mvc.perform(post("/clientes/99").param("nome", "").param("email", ""))
                .andExpect(status().isNotFound())
                .andExpect(view().name("erro"))
                .andExpect(content().string(containsString("Registro não encontrado")));

        verify(clientes, never()).salvar(any(), any());
    }

    @Test
    void excluirClienteVinculadoExibeRegraDeNegocio() throws Exception {
        doThrow(new RegraNegocioException("Este cliente possui atendimentos."))
                .when(clientes).excluir(4L);

        mvc.perform(post("/clientes/4/excluir"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clientes/4"))
                .andExpect(flash().attribute("erro", "Este cliente possui atendimentos."));
    }

    @Test
    void falhaInesperadaRetorna500SemExporMensagemTecnica() throws Exception {
        when(clientes.listar()).thenThrow(new IllegalStateException("DETALHE_INTERNO_TESTE"));

        mvc.perform(get("/clientes"))
                .andExpect(status().isInternalServerError())
                .andExpect(view().name("erro"))
                .andExpect(content().string(containsString("Não foi possível concluir a operação")))
                .andExpect(content().string(not(containsString("DETALHE_INTERNO_TESTE"))));
    }

    @Test
    void enderecoSemControllerOuArquivoRetorna404() throws Exception {
        mvc.perform(get("/pagina-inexistente"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"))
                .andExpect(content().string(containsString("Página não encontrada")));
    }

    @Test
    void excluirPorGetRetorna405SemExecutarExclusao() throws Exception {
        mvc.perform(get("/clientes/4/excluir"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(view().name("erro"));

        verify(clientes, never()).excluir(any());
    }

    private Cliente cliente(Long id) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setNome("Cliente teste");
        cliente.setEmail("teste@example.com");
        return cliente;
    }
}
