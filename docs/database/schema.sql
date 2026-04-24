-- Contact Management System (initial schema)

CREATE TABLE users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    full_name NVARCHAR(120) NOT NULL,
    email NVARCHAR(150) UNIQUE NOT NULL,
    phone NVARCHAR(25) UNIQUE,
    password_hash NVARCHAR(255) NOT NULL,
    created_at DATETIME2 DEFAULT SYSDATETIME()
);

CREATE TABLE contacts (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    first_name NVARCHAR(100) NOT NULL,
    last_name NVARCHAR(100),
    title NVARCHAR(100),
    email NVARCHAR(150),
    phone NVARCHAR(25),
    address NVARCHAR(255),
    created_at DATETIME2 DEFAULT SYSDATETIME(),
    CONSTRAINT fk_contacts_user FOREIGN KEY (user_id) REFERENCES users(id)
);
