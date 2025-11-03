package br.com.zapai.atendimento.repositorio;

import br.com.zapai.atendimento.dominio.Clinica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClinicaRepositorio extends JpaRepository<Clinica, UUID> {
}
