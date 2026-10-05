package br.com.dimdim.atendimento.repository;

import br.com.dimdim.atendimento.entity.Atendimento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AtendimentoRepository extends JpaRepository<Atendimento, Long> {
    boolean existsByClienteId(Long clienteId);
    List<Atendimento> findByClienteIdOrderByDataAberturaDescIdDesc(Long clienteId);
}
