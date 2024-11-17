SET search_path TO orders;

insert into orders(id, order_number, customer_name,customer_email,customer_phone, delivery_address, product_code, product_name, product_price, quantity, status, comments, created_at) values
(1, '16f69458-2f65-49ba-8779-bdaeafc7fa70', 'Siva','siva@gmail.com', '9911122233', 'Siva, Hyderabad, India', 'P100', 'The Hunger Games', 34.0, 1, 'NEW',null, CURRENT_TIMESTAMP),
(2, '594943a8-d209-40b7-958c-e1efdf72877f', 'John','john@gmail.com', '9911122888', 'Prasad, Hyderabad, India', 'P101', 'To Kill a Mockingbird', 45.40, 3, 'NEW',null, CURRENT_TIMESTAMP),
(3, '748de59b-a4e7-46f1-94aa-f2faba8bb8c3', 'James','james@gmail.com', '9911122244', 'Ramu, Hyderabad, India', 'P102', 'The Chronicles of Narnia', 44.50, 2, 'NEW',null, CURRENT_TIMESTAMP)
;
