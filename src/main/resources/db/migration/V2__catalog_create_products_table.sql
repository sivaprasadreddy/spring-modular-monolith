SET search_path TO catalog;

create sequence product_id_seq start with 100 increment by 50;

create table products
(
    id          bigint  not null default nextval('catalog.product_id_seq'),
    code        text    not null unique,
    name        text    not null,
    image_url   text,
    description text,
    price       numeric not null,
    primary key (id)
);
