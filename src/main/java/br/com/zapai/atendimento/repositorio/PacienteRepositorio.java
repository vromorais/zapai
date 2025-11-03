package br.com.zapai.atendimento.repositorio;

import br.com.zapai.atendimento.dominio.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PacienteRepositorio extends JpaRepository<Paciente, UUID> {
    Optional<Paciente> findByTelefoneE164(String telefoneE164);
}
