# ZapAI - Atendimento WhatsApp com IA

Plataforma SaaS multi-clínicas para atendimento inteligente via WhatsApp utilizando Spring Boot 3, PostgreSQL, Redis e integrações com OpenAI e Google Calendar.

## Visão Geral

O serviço recebe mensagens do WhatsApp, valida a assinatura Meta, guarda logs estruturados, mantém o contexto da conversa em Redis e consulta a OpenAI para sugerir um plano de ação padronizado. Com base no plano, o fluxo pode responder dúvidas, realizar small talk ou orquestrar operações de agenda (criação, reagendamento e cancelamento) sincronizadas com o Google Calendar.

### Principais Recursos

- **Multi-tenant** com identificação pelo cabeçalho `X-Tenant-Id` e contexto de sessão.
- **Multi-profissional** com cada agenda integrada ao Google Calendar.
- **Webhook Meta** com verificação HMAC SHA256 (`X-Hub-Signature-256`).
- **Outbox WhatsApp** com fila Redis e retry exponencial para envio de mensagens.
- **Integração OpenAI** para interpretação da conversa e geração de `PlanoAcao` em JSON.
- **Fluxo de agenda** com PostgreSQL + Flyway, status e reservas de disponibilidade.
- **Google OAuth** e criação de eventos no calendário do profissional.
- **Observabilidade** via Spring Actuator e logs estruturados com `MDC` do tenant.

## Arquitetura

```
whatsapp → webhook → validação assinatura → log inbound → sessão Redis → OpenAI
                                              ↓                              ↓
                                             plano ← mapeamento JSON ← resposta LLM
                                              ↓
                                   serviço de agenda/Google Calendar
                                              ↓
                                   resposta → outbox Redis → API Meta
```

### Camadas

- `api`: controladores REST (`WebhookWhatsappController`, `OauthGoogleController`).
- `servico`: orquestração de regras de negócio, integrações externas e infraestrutura.
- `dominio` e `repositorio`: entidades JPA e repositórios Spring Data.
- `config`: segurança, filtros multi-tenant e `WebClient` compartilhado.

## Modelagem de Dados

As migrações Flyway estão em `src/main/resources/db/migration/V1__criar_tabelas.sql` com as tabelas:

- `clinica`, `profissional`, `paciente`, `disponibilidade`, `agendamento`
- `log_mensagem` (jsonb para payload) e `conta_oauth`
- Índices para consultas por profissional e paciente.

Enums disponíveis:

- `StatusDisponibilidade {LIVRE, RESERVADO, AGENDADO}`
- `StatusAgendamento {AGENDADO, CANCELADO, REAGENDADO}`
- `TipoPlanoAcao {FAQ, SMALL_TALK, BOOK, RESCHEDULE, CANCEL}`

O contrato do LLM está representado em `PlanoAcao` (DTO), com `dados_agendamento` preenchidos pelo orquestrador.

## Executando o Projeto

### Pré-requisitos

- Java 17+
- Maven 3.9+
- PostgreSQL e Redis em execução
- Variáveis de ambiente configuradas: `WHATSAPP_TOKEN`, `WHATSAPP_SECRET`, `OPENAI_API_KEY`, `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `WHATSAPP_PHONE_ID`

### Passos

```bash
mvn clean package
java -jar target/atendimento-whatsapp-0.0.1-SNAPSHOT.jar
```

Endpoints principais:

- `POST /webhooks/whatsapp` – recebe mensagens Meta.
- `GET /oauth/google/authorize?state={profissionalId}` – redireciona para consentimento Google.
- `GET /oauth/google/callback` – persiste tokens por profissional.
- `GET /actuator/health` – verificação de saúde.

### Configuração Aplicação

Veja `src/main/resources/application.yaml` para URLs de banco, Redis e integrações externas. As credenciais sensíveis são lidas via variáveis de ambiente.

## Testes

```bash
mvn test
```

## Estrutura da Fila WhatsApp

- Lista Redis `outbox:whatsapp` armazena o JSON de envio.
- `ServicoWhatsapp.processarFila()` aplica `Retry.backoff` exponencial antes de reenfileirar.

## Licença

Uso interno ZapAI.
