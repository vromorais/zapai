package br.com.zapai.atendimento.repositorio;

import br.com.zapai.atendimento.dominio.ContaOauth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ContaOauthRepositorio extends JpaRepository<ContaOauth, UUID> {
    Optional<ContaOauth> findByProvedorAndProfissionalId(String provedor, UUID profissionalId);
}
