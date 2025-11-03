package br.com.zapai.atendimento.repositorio;

import br.com.zapai.atendimento.dominio.Disponibilidade;
import br.com.zapai.atendimento.dominio.enums.StatusDisponibilidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DisponibilidadeRepositorio extends JpaRepository<Disponibilidade, UUID> {
    List<Disponibilidade> findByProfissionalIdAndStatusAndInicioEmAfter(UUID profissionalId, StatusDisponibilidade status, OffsetDateTime inicio);
    Optional<Disponibilidade> findFirstByProfissionalIdAndStatusAndInicioEmLessThanEqualAndFimEmGreaterThanEqual(
            UUID profissionalId, StatusDisponibilidade status, OffsetDateTime inicio, OffsetDateTime fim);
}
