SET search_path TO orders;

create sequence order_id_seq start with 100 increment by 50;

create table orders
(
    id               bigint    not null default nextval('orders.order_id_seq'),
    order_number     text      not null unique,
    customer_name    text      not null,
    customer_email   text      not null,
    customer_phone   text      not null,
    delivery_address text      not null,
    product_code     text      not null,
    product_name     text      not null,
    product_price    text      not null,
    quantity         int       not null,
    status           text      not null,
    comments         text,
    created_at       timestamp not null,
    updated_at       timestamp,
    primary key (id)
);
