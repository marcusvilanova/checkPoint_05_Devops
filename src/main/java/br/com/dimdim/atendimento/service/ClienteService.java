package br.com.dimdim.atendimento.service;

import br.com.dimdim.atendimento.entity.Atendimento;
import br.com.dimdim.atendimento.entity.Cliente;
import br.com.dimdim.atendimento.repository.AtendimentoRepository;
import br.com.dimdim.atendimento.repository.ClienteRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ClienteService {
    private final ClienteRepository clientes;
    private final AtendimentoRepository atendimentos;

    public ClienteService(ClienteRepository clientes, AtendimentoRepository atendimentos) {
        this.clientes = clientes;
        this.atendimentos = atendimentos;
    }

    public List<Cliente> listar() { return clientes.findAll(Sort.by("nome", "id")); }
    public long contar() { return clientes.count(); }

    public Cliente buscar(Long id) {
        return clientes.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado."));
    }

    public List<Atendimento> listarAtendimentos(Long clienteId) {
        buscar(clienteId);
        return atendimentos.findByClienteIdOrderByDataAberturaDescIdDesc(clienteId);
    }

    @Transactional
    public Cliente salvar(Long id, Cliente dados) {
        Cliente cliente = id == null ? new Cliente() : buscar(id);
        cliente.setNome(dados.getNome().trim());
        cliente.setEmail(dados.getEmail().trim());
        cliente.setTelefone(dados.getTelefone() == null ? null : dados.getTelefone().trim());
        return clientes.save(cliente);
    }

    @Transactional
    public void excluir(Long id) {
        Cliente cliente = buscar(id);
        if (atendimentos.existsByClienteId(id)) {
            throw new RegraNegocioException(
                    "Este cliente possui atendimentos. Exclua os atendimentos antes de excluir o cliente.");
        }
        clientes.delete(cliente);
    }
}
