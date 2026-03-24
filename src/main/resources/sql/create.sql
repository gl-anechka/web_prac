-- тип товара
CREATE TABLE product_type (
    id_type SERIAL PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT
);

-- товар
CREATE TABLE product (
    id_product SERIAL PRIMARY KEY,
    title VARCHAR(300) NOT NULL,
    id_type INTEGER NOT NULL REFERENCES product_type(id_type),
    unit VARCHAR(10) NOT NULL CHECK (unit IN ('KG', 'PCS', 'L')),
    kg_per_unit REAL NOT NULL CHECK (kg_per_unit >= 0)
);

-- поставщик/потребитель, поле type определяет, кем является партнер:
-- поставщик, потребитель или и тот и другой
CREATE TABLE partner (
    id_partner SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address TEXT,
    phone VARCHAR(12),
    email VARCHAR(100),
    type VARCHAR(20) NOT NULL DEFAULT 'BOTH'
        CHECK (type IN ('PROVIDER', 'CONSUMER', 'BOTH'))
);

-- поставка: 1 запись таблицы - поставка одного товара
CREATE TABLE supply (
    id_supply SERIAL PRIMARY KEY,
    id_provider INTEGER NOT NULL REFERENCES partner(id_partner),
    id_product INTEGER NOT NULL REFERENCES product(id_product),
    time TIMESTAMP NOT NULL,
    amount REAL NOT NULL CHECK (amount > 0)
);

-- получение: 1 строка - 1 заказ товара
-- атрибут completed показывает статус заказа: выполнен - true, обрабатывается - false
CREATE TABLE reception (
    id_reception SERIAL PRIMARY KEY,
    id_consumer INTEGER NOT NULL REFERENCES partner(id_partner),
    id_product INTEGER NOT NULL REFERENCES product(id_product),
    time TIMESTAMP NOT NULL,
    amount REAL NOT NULL CHECK (amount > 0),
    completed BOOLEAN NOT NULL DEFAULT FALSE
);

-- место товара на складе
-- полка в комнате, вместимость полки
CREATE TABLE place (
    id_place SERIAL PRIMARY KEY,
    room_num INTEGER NOT NULL,
    shelf_num INTEGER NOT NULL,
    kg_limit REAL NOT NULL CHECK (kg_limit > 0),
    CONSTRAINT unique_place UNIQUE (room_num, shelf_num)
);

-- складская запись
CREATE TABLE storehouse (
    id_storehouse SERIAL PRIMARY KEY,
    id_product INTEGER NOT NULL REFERENCES product(id_product),
    amount REAL NOT NULL CHECK (amount >= 0),
    id_place INTEGER NOT NULL REFERENCES place(id_place),
    id_supply INTEGER NOT NULL REFERENCES supply(id_supply),
    received_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'OK'
        CHECK (status IN ('OK', 'NEAR_EXPIRY', 'SPOILED')),
    id_reception INTEGER REFERENCES reception(id_reception)
);