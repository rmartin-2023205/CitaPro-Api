-- Usuarios iniciales con contraseñas encriptadas con BCrypt (Password: Password123!)
-- Hash BCrypt estándar de 60 caracteres: $2a$10$daU03B2g0iO88wQeSsqn6.KxU6p5fP0m2Zg4K.Gg/gJd6K9o1xHkS

-- 1. Administrador del negocio
INSERT INTO users (id, business_id, full_name, email, phone, password_hash, active)
VALUES (
    'u0000000-0000-0000-0000-000000000001',
    'b0000000-0000-0000-0000-000000000001',
    'Administrador CitaPro',
    'admin@citapro.com',
    '+502 5555-0001',
    '$2a$10$daU03B2g0iO88wQeSsqn6.KxU6p5fP0m2Zg4K.Gg/gJd6K9o1xHkS',
    true
) ON CONFLICT (email) DO NOTHING;

INSERT INTO user_roles (id, user_id, role)
VALUES (
    'r0000000-0000-0000-0000-000000000001',
    'u0000000-0000-0000-0000-000000000001',
    'ROLE_ADMIN'
) ON CONFLICT (user_id, role) DO NOTHING;

-- 2. Usuario Profesional - Carlos Gómez
INSERT INTO users (id, business_id, full_name, email, phone, password_hash, active)
VALUES (
    'u0000000-0000-0000-0000-000000000002',
    'b0000000-0000-0000-0000-000000000001',
    'Carlos Gómez',
    'carlos@citapro.com',
    '+502 5555-0002',
    '$2a$10$daU03B2g0iO88wQeSsqn6.KxU6p5fP0m2Zg4K.Gg/gJd6K9o1xHkS',
    true
) ON CONFLICT (email) DO NOTHING;

INSERT INTO user_roles (id, user_id, role)
VALUES (
    'r0000000-0000-0000-0000-000000000002',
    'u0000000-0000-0000-0000-000000000002',
    'ROLE_PROFESSIONAL'
) ON CONFLICT (user_id, role) DO NOTHING;

-- Vincular Carlos en tabla professionals
UPDATE professionals
SET user_id = 'u0000000-0000-0000-0000-000000000002'
WHERE id = 'p0000000-0000-0000-0000-000000000001';

-- 3. Usuario Profesional - Andrea Morales
INSERT INTO users (id, business_id, full_name, email, phone, password_hash, active)
VALUES (
    'u0000000-0000-0000-0000-000000000003',
    'b0000000-0000-0000-0000-000000000001',
    'Andrea Morales',
    'andrea@citapro.com',
    '+502 5555-0003',
    '$2a$10$daU03B2g0iO88wQeSsqn6.KxU6p5fP0m2Zg4K.Gg/gJd6K9o1xHkS',
    true
) ON CONFLICT (email) DO NOTHING;

INSERT INTO user_roles (id, user_id, role)
VALUES (
    'r0000000-0000-0000-0000-000000000003',
    'u0000000-0000-0000-0000-000000000003',
    'ROLE_PROFESSIONAL'
) ON CONFLICT (user_id, role) DO NOTHING;

-- Vincular Andrea en tabla professionals
UPDATE professionals
SET user_id = 'u0000000-0000-0000-0000-000000000003'
WHERE id = 'p0000000-0000-0000-0000-000000000002';

-- 4. Usuario Cliente demo
INSERT INTO users (id, business_id, full_name, email, phone, password_hash, active)
VALUES (
    'u0000000-0000-0000-0000-000000000004',
    'b0000000-0000-0000-0000-000000000001',
    'Mario López',
    'cliente@citapro.com',
    '+502 5555-0004',
    '$2a$10$daU03B2g0iO88wQeSsqn6.KxU6p5fP0m2Zg4K.Gg/gJd6K9o1xHkS',
    true
) ON CONFLICT (email) DO NOTHING;

INSERT INTO user_roles (id, user_id, role)
VALUES (
    'r0000000-0000-0000-0000-000000000004',
    'u0000000-0000-0000-0000-000000000004',
    'ROLE_CLIENT'
) ON CONFLICT (user_id, role) DO NOTHING;

