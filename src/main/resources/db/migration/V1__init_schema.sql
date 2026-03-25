create table if not exists category(
  id bigserial primary key,
  name varchar(100) not null,
  parent_id bigint
);

create table if not exists brand(
  id bigserial primary key,
  name varchar(100) not null
);

create table if not exists product(
  id bigserial primary key,
  sku_code varchar(64),
  name varchar(200) not null,
  category_id bigint,
  brand_id bigint,
  unit varchar(32),
  spec varchar(128),
  status varchar(16) default 'ENABLED',
  created_at timestamptz default now(),
  updated_at timestamptz default now()
);

create unique index if not exists idx_product_sku on product(sku_code);

create table if not exists product_barcode(
  id bigserial primary key,
  product_id bigint not null,
  barcode varchar(64) not null,
  is_primary boolean default false
);

create unique index if not exists uq_barcode on product_barcode(barcode);
create index if not exists idx_barcode_product on product_barcode(product_id);

create table if not exists store(
  id bigserial primary key,
  name varchar(100) not null,
  code varchar(64) not null,
  address varchar(255),
  status varchar(16) default 'ENABLED'
);

create unique index if not exists uq_store_code on store(code);

create table if not exists inventory(
  store_id bigint not null,
  product_id bigint not null,
  on_hand numeric(18,3) not null default 0,
  reserved numeric(18,3) not null default 0,
  updated_at timestamptz default now(),
  primary key(store_id, product_id)
);

create index if not exists idx_inventory_product on inventory(product_id);

create table if not exists inventory_ledger(
  id bigserial primary key,
  store_id bigint not null,
  product_id bigint not null,
  qty_change numeric(18,3) not null,
  type varchar(16) not null,
  ref_type varchar(32),
  ref_id varchar(64),
  occurred_at timestamptz default now()
);

create index if not exists idx_ledger_store_product on inventory_ledger(store_id, product_id);

