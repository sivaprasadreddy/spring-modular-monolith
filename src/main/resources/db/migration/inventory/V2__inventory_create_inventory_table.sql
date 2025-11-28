SET search_path TO inventory;

create sequence inventory_id_seq start with 100 increment by 50;

create table inventory
(
    id           bigint not null default nextval('inventory.inventory_id_seq'),
    product_code text   not null unique,
    quantity     bigint not null,
    primary key (id)
);

insert into inventory(product_code, quantity) values
('P100', 400),
('P101', 200),
('P102', 300),
('P103', 100),
('P104', 40),
('P105', 100),
('P106', 1400),
('P107', 4200),
('P108', 430),
('P109', 405),
('P110', 300),
('P111', 4100),
('P112', 4300),
('P113', 700),
('P114', 600)
;
