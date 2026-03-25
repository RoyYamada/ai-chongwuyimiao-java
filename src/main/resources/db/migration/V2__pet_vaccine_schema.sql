create table if not exists owner(
  id bigserial primary key,
  name varchar(100) not null,
  phone varchar(32),
  email varchar(120)
);

create table if not exists pet(
  id bigserial primary key,
  owner_id bigint not null,
  name varchar(100) not null,
  species varchar(40),
  breed varchar(80),
  gender varchar(16),
  birth_date date,
  weight_kg numeric(10,2),
  microchip varchar(64)
);
create index if not exists idx_pet_owner on pet(owner_id);

create table if not exists vaccine(
  id bigserial primary key,
  name varchar(120) not null,
  species varchar(40),
  doses_required int default 1,
  interval_days int default 0,
  valid_months int default 12
);

create table if not exists vaccination(
  id bigserial primary key,
  pet_id bigint not null,
  vaccine_id bigint not null,
  dose_number int not null,
  administered_at timestamptz not null default now(),
  lot_number varchar(64),
  clinic varchar(120),
  vet_name varchar(80),
  next_due_at timestamptz,
  status varchar(16) default 'RECORDED'
);
create index if not exists idx_vaccination_pet on vaccination(pet_id);
create index if not exists idx_vaccination_due on vaccination(next_due_at);

