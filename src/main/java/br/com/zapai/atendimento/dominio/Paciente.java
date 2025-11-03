package br.com.zapai.atendimento.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "paciente")
public class Paciente {

    @Id
    private UUID id;

    @Column(name = "nome_completo", nullable = false, length = 200)
    private String nomeCompleto;

    @Column(name = "telefone_e164", nullable = false, length = 20, unique = true)
    private String telefoneE164;

    @Column(length = 32)
    private String documento;

    @Column(length = 200)
    private String email;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public String getTelefoneE164() {
        return telefoneE164;
    }

    public void setTelefoneE164(String telefoneE164) {
        this.telefoneE164 = telefoneE164;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
