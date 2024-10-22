SET search_path TO orders;

insert into orders(id, order_number, customer_name,customer_email,customer_phone, delivery_address, product_code, product_name, product_price, quantity, status, comments, created_at) values
(1, 'ORDER-100001', 'Siva','siva@gmail.com', '9911122233', 'Siva, Hyderabad, India', 'P100', 'The Hunger Games', 34.0, 1, 'NEW',null, CURRENT_TIMESTAMP),
(2, 'ORDER-100002', 'John','john@gmail.com', '9911122888', 'Prasad, Hyderabad, India', 'P101', 'To Kill a Mockingbird', 45.40, 3, 'NEW',null, CURRENT_TIMESTAMP),
(3, 'ORDER-100003', 'James','james@gmail.com', '9911122244', 'Ramu, Hyderabad, India', 'P102', 'The Chronicles of Narnia', 44.50, 2, 'NEW',null, CURRENT_TIMESTAMP)
;
