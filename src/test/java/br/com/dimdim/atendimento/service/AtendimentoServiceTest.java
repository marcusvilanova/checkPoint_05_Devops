package br.com.dimdim.atendimento.service;

import br.com.dimdim.atendimento.entity.Atendimento;
import br.com.dimdim.atendimento.entity.Cliente;
import br.com.dimdim.atendimento.entity.StatusAtendimento;
import br.com.dimdim.atendimento.repository.AtendimentoRepository;
import br.com.dimdim.atendimento.repository.ClienteRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtendimentoServiceTest {
    private static final LocalDate DATA = LocalDate.of(2026, 10, 5);
    @Mock private AtendimentoRepository atendimentos;
    @Mock private ClienteRepository clientes;
    private AtendimentoService service;

    @BeforeEach
    void preparar() { service = new AtendimentoService(atendimentos, clientes); }

    @Test
    void cadastrarAssociaClienteExistenteEGuardaSituacaoEData() {
        Cliente cliente = cliente(2L);
        when(clientes.findById(2L)).thenReturn(Optional.of(cliente));
        when(atendimentos.save(any(Atendimento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Atendimento salvo = service.salvar(null, "  Dúvida sobre cadastro  ", "  Cliente pediu orientação.  ",
                StatusAtendimento.ABERTO, DATA, 2L);

        assertSame(cliente, salvo.getCliente());
        assertEquals("Dúvida sobre cadastro", salvo.getAssunto());
        assertEquals("Cliente pediu orientação.", salvo.getDescricao());
        assertEquals(StatusAtendimento.ABERTO, salvo.getStatus());
        assertEquals(DATA, salvo.getDataAbertura());
    }

    @Test
    void clienteInexistenteImpedeCadastro() {
        when(clientes.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ClienteSelecionadoInvalidoException.class, () -> service.salvar(null,
                "Assunto", "Descrição", StatusAtendimento.ABERTO, DATA, 99L));

        verify(atendimentos, never()).save(any());
    }

    @Test
    void editarPreservaIdEPermiteAtualizarSituacaoECliente() {
        Atendimento existente = new Atendimento();
        existente.setId(8L);
        existente.setCliente(cliente(1L));
        Cliente novoCliente = cliente(2L);
        when(atendimentos.findById(8L)).thenReturn(Optional.of(existente));
        when(clientes.findById(2L)).thenReturn(Optional.of(novoCliente));
        when(atendimentos.save(existente)).thenReturn(existente);

        Atendimento salvo = service.salvar(8L, "Caso resolvido", "Orientação final enviada.",
                StatusAtendimento.CONCLUIDO, DATA, 2L);

        assertSame(existente, salvo);
        assertEquals(8L, salvo.getId());
        assertSame(novoCliente, salvo.getCliente());
        assertEquals(StatusAtendimento.CONCLUIDO, salvo.getStatus());
    }

    @Test
    void editarIdInexistenteNaoInsereNovoAtendimento() {
        when(atendimentos.findById(90L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.salvar(90L,
                "Assunto", "Descrição", StatusAtendimento.ABERTO, DATA, 2L));

        verify(atendimentos, never()).save(any());
        verify(clientes, never()).findById(any());
    }

    @Test
    void excluirRemoveAtendimentoSemApagarCliente() {
        Atendimento atendimento = new Atendimento();
        atendimento.setId(5L);
        atendimento.setCliente(cliente(2L));
        when(atendimentos.findById(5L)).thenReturn(Optional.of(atendimento));

        service.excluir(5L);

        verify(atendimentos).delete(atendimento);
        verify(clientes, never()).delete(any());
    }

    @Test
    void buscarOuExcluirIdInexistenteInformaAusencia() {
        when(atendimentos.findById(9L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscar(9L));
        assertThrows(RecursoNaoEncontradoException.class, () -> service.excluir(9L));
        verify(atendimentos, never()).delete(any());
    }

    private Cliente cliente(Long id) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setNome("Cliente " + id);
        cliente.setEmail("cliente" + id + "@example.com");
        return cliente;
    }
}
