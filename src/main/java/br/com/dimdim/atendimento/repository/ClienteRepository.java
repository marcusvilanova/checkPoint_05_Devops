package br.com.dimdim.atendimento.repository;

import br.com.dimdim.atendimento.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> { }
