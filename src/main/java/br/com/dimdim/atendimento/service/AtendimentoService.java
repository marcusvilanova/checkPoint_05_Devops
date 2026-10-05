package br.com.dimdim.atendimento.service;

import br.com.dimdim.atendimento.entity.Atendimento;
import br.com.dimdim.atendimento.entity.Cliente;
import br.com.dimdim.atendimento.entity.StatusAtendimento;
import br.com.dimdim.atendimento.repository.AtendimentoRepository;
import br.com.dimdim.atendimento.repository.ClienteRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AtendimentoService {
    private final AtendimentoRepository atendimentos;
    private final ClienteRepository clientes;

    public AtendimentoService(AtendimentoRepository atendimentos, ClienteRepository clientes) {
        this.atendimentos = atendimentos;
        this.clientes = clientes;
    }

    public List<Atendimento> listar() {
        return atendimentos.findAll(Sort.by(Sort.Direction.DESC, "dataAbertura", "id"));
    }

    public long contar() { return atendimentos.count(); }

    public Atendimento buscar(Long id) {
        return atendimentos.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Atendimento não encontrado."));
    }

    @Transactional
    public Atendimento salvar(Long id, String assunto, String descricao, StatusAtendimento status,
                              LocalDate dataAbertura, Long clienteId) {
        Atendimento atendimento = id == null ? new Atendimento() : buscar(id);
        Cliente cliente = clientes.findById(clienteId)
                .orElseThrow(ClienteSelecionadoInvalidoException::new);
        atendimento.setAssunto(assunto.trim());
        atendimento.setDescricao(descricao.trim());
        atendimento.setStatus(status);
        atendimento.setDataAbertura(dataAbertura);
        atendimento.setCliente(cliente);
        return atendimentos.save(atendimento);
    }

    @Transactional
    public void excluir(Long id) { atendimentos.delete(buscar(id)); }
}
