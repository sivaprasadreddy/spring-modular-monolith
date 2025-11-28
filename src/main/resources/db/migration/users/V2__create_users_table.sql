SET search_path TO users;

create sequence user_id_seq start with 100 increment by 50;

create table users
(
    id         bigint    not null default nextval('users.user_id_seq'),
    email      text      not null,
    password   text      not null,
    name       text      not null,
    role       text      not null,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp,
    primary key (id),
    constraint user_email_unique unique (email)
);