package br.com.dimdim.atendimento.service;

import br.com.dimdim.atendimento.entity.Cliente;
import br.com.dimdim.atendimento.repository.AtendimentoRepository;
import br.com.dimdim.atendimento.repository.ClienteRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {
    @Mock private ClienteRepository clientes;
    @Mock private AtendimentoRepository atendimentos;
    private ClienteService service;

    @BeforeEach
    void preparar() { service = new ClienteService(clientes, atendimentos); }

    @Test
    void cadastrarIgnoraIdDoFormularioENormalizaEspacos() {
        Cliente dados = dados("  Cliente demonstração  ", "  cliente@example.com  ");
        dados.setId(999L);
        dados.setTelefone("  (11) 99999-0000  ");
        when(clientes.save(any(Cliente.class))).thenAnswer(invocation -> {
            Cliente novo = invocation.getArgument(0);
            assertNull(novo.getId());
            novo.setId(1L);
            return novo;
        });

        Cliente salvo = service.salvar(null, dados);

        assertEquals(1L, salvo.getId());
        assertEquals("Cliente demonstração", salvo.getNome());
        assertEquals("cliente@example.com", salvo.getEmail());
        assertEquals("(11) 99999-0000", salvo.getTelefone());
    }

    @Test
    void editarAtualizaRegistroExistentePreservandoIdentidade() {
        Cliente existente = dados("Nome anterior", "antigo@example.com");
        existente.setId(7L);
        when(clientes.findById(7L)).thenReturn(Optional.of(existente));
        when(clientes.save(existente)).thenReturn(existente);
        Cliente dados = dados("Nome atualizado", "novo@example.com");
        dados.setId(999L);

        Cliente salvo = service.salvar(7L, dados);

        assertSame(existente, salvo);
        assertEquals(7L, salvo.getId());
        assertEquals("Nome atualizado", salvo.getNome());
        assertEquals("novo@example.com", salvo.getEmail());
    }

    @Test
    void editarIdInexistenteNaoCriaNovoRegistro() {
        when(clientes.findById(90L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> service.salvar(90L, dados("Nome", "nome@example.com")));

        verify(clientes, never()).save(any());
    }

    @Test
    void excluirClienteComAtendimentosPreservaRelacionamento() {
        Cliente cliente = dados("Cliente", "cliente@example.com");
        cliente.setId(3L);
        when(clientes.findById(3L)).thenReturn(Optional.of(cliente));
        when(atendimentos.existsByClienteId(3L)).thenReturn(true);

        assertThrows(RegraNegocioException.class, () -> service.excluir(3L));

        verify(clientes, never()).delete(any());
    }

    @Test
    void excluirClienteSemAtendimentosRemoveRegistro() {
        Cliente cliente = dados("Cliente", "cliente@example.com");
        cliente.setId(4L);
        when(clientes.findById(4L)).thenReturn(Optional.of(cliente));
        when(atendimentos.existsByClienteId(4L)).thenReturn(false);

        service.excluir(4L);

        verify(clientes).delete(cliente);
    }

    @Test
    void buscarOuExcluirIdInexistenteInformaAusencia() {
        when(clientes.findById(9L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscar(9L));
        assertThrows(RecursoNaoEncontradoException.class, () -> service.excluir(9L));
        verify(clientes, never()).delete(any());
    }

    private Cliente dados(String nome, String email) {
        Cliente cliente = new Cliente();
        cliente.setNome(nome);
        cliente.setEmail(email);
        return cliente;
    }
}
