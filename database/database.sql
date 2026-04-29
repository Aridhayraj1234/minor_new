CREATE DATABASE IF NOT EXISTS creditdb;
USE creditdb;

CREATE TABLE IF NOT EXISTS predictions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    age INT,
    income DOUBLE,
    credit_history INT,
    loan_amount DOUBLE,
    prediction INT,
    probability DOUBLE,
    timestamp DATETIME
);
