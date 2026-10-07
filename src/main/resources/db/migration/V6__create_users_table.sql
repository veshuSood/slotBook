CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY ,
    email VARCHAR(200) not null unique ,
    password varchar(255),
    name varchar(50),
    role varchar(10) not null ,
    auth_provider varchar(10) not null
);