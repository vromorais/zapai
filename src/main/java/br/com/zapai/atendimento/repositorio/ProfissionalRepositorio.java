package br.com.zapai.atendimento.repositorio;

import br.com.zapai.atendimento.dominio.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProfissionalRepositorio extends JpaRepository<Profissional, UUID> {
    Optional<Profissional> findByIdAndClinicaId(UUID id, UUID clinicaId);
}
