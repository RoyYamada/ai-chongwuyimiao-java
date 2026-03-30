alter table reminder add column sent boolean default false;
alter table reminder add column sent_at timestamp;
alter table reminder add column send_error text;