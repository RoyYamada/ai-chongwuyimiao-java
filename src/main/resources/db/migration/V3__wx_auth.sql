create table if not exists wx_session(
  openid varchar(64) primary key,
  session_key varchar(128) not null,
  unionid varchar(64),
  expire_at timestamptz,
  updated_at timestamptz default now()
);

