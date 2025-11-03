package br.com.zapai.atendimento.repositorio;

import br.com.zapai.atendimento.dominio.Agendamento;
import br.com.zapai.atendimento.dominio.enums.StatusAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AgendamentoRepositorio extends JpaRepository<Agendamento, UUID> {
    List<Agendamento> findByPacienteIdAndInicioEmAfter(UUID pacienteId, OffsetDateTime inicio);
    Optional<Agendamento> findByIdAndClinicaId(UUID id, UUID clinicaId);
    Optional<Agendamento> findFirstByPacienteIdAndStatusOrderByInicioEmDesc(UUID pacienteId, StatusAgendamento status);
}
