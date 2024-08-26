SET search_path TO orders;

create sequence order_id_seq start with 100 increment by 50;

create table orders
(
    id               bigint default nextval('orders.order_id_seq') not null,
    order_number     text                                   not null unique,
    customer_id      text                                   not null,
    delivery_address text                                   not null,
    product_code     text                                   not null,
    product_name     text                                   not null,
    product_price    text                                   not null,
    quantity         int                                    not null,
    status           text                                   not null,
    comments         text,
    created_at       timestamp                              not null,
    updated_at       timestamp,
    primary key (id)
);
