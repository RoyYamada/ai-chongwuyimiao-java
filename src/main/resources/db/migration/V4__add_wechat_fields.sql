alter table owner add column if not exists avatar varchar(255);
alter table owner add column if not exists openid varchar(128) unique;
alter table owner add column if not exists unionid varchar(128);
