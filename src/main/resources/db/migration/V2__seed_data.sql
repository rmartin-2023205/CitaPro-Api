-- Negocio demo
INSERT INTO businesses (id, name, slug, timezone, phone, email, address, active)
VALUES (
    'b0000000-0000-0000-0000-000000000001',
    'CitaPro Barber & Studio',
    'citapro-studio',
    'America/Guatemala',
    '+502 2345-6789',
    'contacto@citapro.com',
    'Zona 10, Ciudad de Guatemala',
    true
) ON CONFLICT (slug) DO NOTHING;

-- Servicios iniciales
INSERT INTO services (id, business_id, name, description, duration_minutes, price, active)
VALUES
    ('s0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000001', 'Corte Clásico', 'Corte personalizado con tijera y máquina, lavado y peinado con cera mate.', 30, 50.00, true),
    ('s0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000001', 'Afeitado Tradicional y Perfilado', 'Afeitado clásico a navaja con toallas calientes y bálsamo hidratante.', 30, 40.00, true),
    ('s0000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000001', 'Corte + Barba Completo', 'Experiencia completa: corte de cabello con perfilado y arreglo de barba.', 60, 80.00, true),
    ('s0000000-0000-0000-0000-000000000004', 'b0000000-0000-0000-0000-000000000001', 'Tratamiento Capilar e Hidratación', 'Tratamiento profundo para revitalizar el cuero cabelludo y reparar fibras secas.', 45, 110.00, true)
ON CONFLICT (id) DO NOTHING;

-- Profesionales iniciales
INSERT INTO professionals (id, business_id, display_name, bio, active)
VALUES
    ('p0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000001', 'Carlos Gómez', 'Master Barber con más de 8 años de experiencia en cortes clásicos y diseño de barba.', true),
    ('p0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000001', 'Andrea Morales', 'Estilista profesional especializada en cuidado del cabello, texturas y asesoría de imagen.', true)
ON CONFLICT (id) DO NOTHING;

-- Relación profesional-servicio
INSERT INTO professional_services (professional_id, service_id)
VALUES
    -- Carlos hace cortes y barba
    ('p0000000-0000-0000-0000-000000000001', 's0000000-0000-0000-0000-000000000001'),
    ('p0000000-0000-0000-0000-000000000001', 's0000000-0000-0000-0000-000000000002'),
    ('p0000000-0000-0000-0000-000000000001', 's0000000-0000-0000-0000-000000000003'),
    -- Andrea hace corte, combo y tratamiento capilar
    ('p0000000-0000-0000-0000-000000000002', 's0000000-0000-0000-0000-000000000001'),
    ('p0000000-0000-0000-0000-000000000002', 's0000000-0000-0000-0000-000000000003'),
    ('p0000000-0000-0000-0000-000000000002', 's0000000-0000-0000-0000-000000000004')
ON CONFLICT (professional_id, service_id) DO NOTHING;

-- Horarios del negocio (Lunes a Sábado de 08:00 a 18:00, Domingo cerrado)
INSERT INTO business_hours (id, business_id, day_of_week, opens_at, closes_at, closed)
VALUES
    ('h0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000001', 1, '08:00', '18:00', false),
    ('h0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000001', 2, '08:00', '18:00', false),
    ('h0000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000001', 3, '08:00', '18:00', false),
    ('h0000000-0000-0000-0000-000000000004', 'b0000000-0000-0000-0000-000000000001', 4, '08:00', '18:00', false),
    ('h0000000-0000-0000-0000-000000000005', 'b0000000-0000-0000-0000-000000000001', 5, '08:00', '18:00', false),
    ('h0000000-0000-0000-0000-000000000006', 'b0000000-0000-0000-0000-000000000001', 6, '08:00', '18:00', false),
    ('h0000000-0000-0000-0000-000000000007', 'b0000000-0000-0000-0000-000000000001', 7, '08:00', '18:00', true)
ON CONFLICT (id) DO NOTHING;

