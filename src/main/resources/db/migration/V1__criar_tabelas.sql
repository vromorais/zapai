create extension if not exists "uuid-ossp";

create table if not exists clinica (
    id uuid primary key,
    nome varchar(200) not null,
    fuso_horario varchar(64) not null default 'America/Sao_Paulo'
);

create table if not exists profissional (
    id uuid primary key,
    clinica_id uuid not null references clinica(id),
    nome varchar(200) not null,
    especialidades text,
    calendario_id varchar(255),
    minutos_slot_padrao int default 30
);

create index if not exists idx_profissional_clinica on profissional (clinica_id);

create table if not exists paciente (
    id uuid primary key,
    nome_completo varchar(200) not null,
    telefone_e164 varchar(20) unique not null,
    documento varchar(32),
    email varchar(200)
);

create table if not exists disponibilidade (
    id uuid primary key,
    profissional_id uuid not null references profissional(id),
    inicio_em timestamptz not null,
    fim_em timestamptz not null,
    status varchar(20) not null
);

create index if not exists idx_disponibilidade_profissional_inicio on disponibilidade (profissional_id, inicio_em);

create table if not exists agendamento (
    id uuid primary key,
    clinica_id uuid not null references clinica(id),
    profissional_id uuid not null references profissional(id),
    paciente_id uuid not null references paciente(id),
    inicio_em timestamptz not null,
    fim_em timestamptz not null,
    status varchar(20) not null,
    id_evento_google varchar(255),
    observacoes text
);

create index if not exists idx_agendamento_paciente_inicio on agendamento (paciente_id, inicio_em);

create table if not exists log_mensagem (
    id uuid primary key,
    canal varchar(20) not null,
    direcao varchar(10) not null,
    telefone_e164 varchar(20),
    payload jsonb,
    criado_em timestamptz not null default now(),
    id_sessao varchar(64),
    intent varchar(50),
    id_correlacao varchar(64)
);

create table if not exists conta_oauth (
    id uuid primary key,
    provedor varchar(32) not null,
    access_token text not null,
    refresh_token text,
    expira_em timestamptz,
    escopo text,
    profissional_id uuid not null references profissional(id)
);

create unique index if not exists uq_conta_oauth_provedor_profissional on conta_oauth (provedor, profissional_id);
