package br.com.zapai.atendimento.repositorio;

import br.com.zapai.atendimento.dominio.LogMensagem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LogMensagemRepositorio extends JpaRepository<LogMensagem, UUID> {
}
