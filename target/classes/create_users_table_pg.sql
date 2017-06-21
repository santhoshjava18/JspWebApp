-- PostgreSQL syntax

CREATE TABLE users
(
  userid serial NOT NULL PRIMARY KEY,
  username varchar(20) NOT NULL UNIQUE,
  "password" varchar(20),
  first_name varchar(50),
  last_name varchar(50),
  phone varchar(20),
  salary numeric(10,2)
)