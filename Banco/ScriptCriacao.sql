create table if not exists contatos (
  id bigserial primary key,
  whatsapp_id varchar(32) not null unique,   -- ex: 5511953789263
  nome text null,
  criado_em timestamptz not null default now(),
  atualizado_em timestamptz not null default now()
);

create table if not exists conversas (
  id bigserial primary key,
  contato_id bigint not null references contatos(id) on delete cascade,
  status varchar(16) not null default 'ABERTA', -- ABERTA / FECHADA
  ultima_mensagem_em timestamptz null,
  criado_em timestamptz not null default now(),
  atualizado_em timestamptz not null default now(),
  unique (contato_id)
);

ALTER TABLE conversas
ADD COLUMN operador_id BIGINT;

ALTER TABLE conversas
ADD CONSTRAINT fk_conversas_operador
FOREIGN KEY (operador_id)
REFERENCES operadores(id);

create index if not exists idx_conversas_ultima_mensagem
  on conversas (ultima_mensagem_em desc);

create table if not exists mensagens (
  id bigserial primary key,
  conversa_id bigint not null references conversas(id) on delete cascade,

  whatsapp_message_id text unique,            -- wamid...
  direcao varchar(3) not null,                 -- IN / OUT
  whatsapp_id_origem varchar(32) null,         -- quem enviou
  phone_number_id_destino text null,           -- seu número WhatsApp

  tipo varchar(32) not null default 'text',    -- text, image, etc
  texto text null,

  timestamp_whatsapp bigint null,              -- epoch que vem do webhook
  enviado_em timestamptz not null,              -- usado para ordenar
  criado_em timestamptz not null default now(),

  payload_raw jsonb null                       -- JSON bruto (opcional)
);

create index if not exists idx_mensagens_conversa_ordem
  on mensagens (conversa_id, enviado_em asc);

create table if not exists eventos_webhook (
  id bigserial primary key,
  recebido_em timestamptz not null default now(),
  tipo_objeto text null,                        -- whatsapp_business_account
  payload jsonb not null,
  processado_em timestamptz null,
  erro_processamento text null
);


CREATE TABLE operadores (
    id BIGSERIAL PRIMARY KEY,

    nome VARCHAR(120) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,

    senha_hash VARCHAR(255) NOT NULL,

    role VARCHAR(30) NOT NULL DEFAULT 'OPERADOR',
    ativo BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);


//crie para mim delete sem where em todas as tabelas 
DELETE FROM mensagens;
DELETE FROM conversas;
DELETE FROM contatos;
DELETE FROM eventos_webhook;
DELETE FROM operadores;
