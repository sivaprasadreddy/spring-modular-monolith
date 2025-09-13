SET search_path TO users;

insert into users(id, email, password, name, role) values
(1,'admin@gmail.com','$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqaFS','SivaLabs', 'ROLE_ADMIN'),
(2,'siva@gmail.com','$2a$10$UFEPYW7Rx1qZqdHajzOnB.VBR3rvm7OI7uSix4RadfQiNhkZOi2fi','Siva', 'ROLE_AUTHOR');
