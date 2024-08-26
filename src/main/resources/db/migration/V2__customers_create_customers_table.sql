SET search_path TO customers;

create sequence customer_id_seq start with 100 increment by 50;

create table customers
(
    id    bigint default nextval('customers.customer_id_seq') not null,
    name  text                                      not null,
    email text                                      not null unique,
    phone text                                      not null,
    primary key (id)
);
