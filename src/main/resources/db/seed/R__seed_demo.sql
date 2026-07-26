-- Jeu de démonstration (profil dev uniquement). Migration répétable et idempotente.
-- Les nations proviennent du référentiel de V2 ; on les référence par leur code.

INSERT INTO discipline (id, name) VALUES
    (1, 'Athlétisme'),
    (2, 'Natation')
ON CONFLICT DO NOTHING;

INSERT INTO epreuve (id, label, discipline_id, event_date) VALUES
    (1, '100 m',            1, DATE '2024-08-04'),
    (2, '200 m',            1, DATE '2024-08-06'),
    (3, '100 m nage libre', 2, DATE '2024-08-05')
ON CONFLICT DO NOTHING;

INSERT INTO athlete (id, last_name, first_name, gender, birth_date, country_id, discipline_id, height_cm, weight_kg) VALUES
    (1, 'Bolt',      'Usain',   'MALE',   DATE '1986-08-21', (SELECT id FROM country WHERE code = 'JAM'), 1, 195, 94),
    (2, 'Blake',     'Yohan',   'MALE',   DATE '1989-12-26', (SELECT id FROM country WHERE code = 'JAM'), 1, 180, 75),
    (3, 'Gatlin',    'Justin',  'MALE',   DATE '1982-02-10', (SELECT id FROM country WHERE code = 'USA'), 1, 185, 80),
    (4, 'Thompson',  'Elaine',  'FEMALE', DATE '1992-06-28', (SELECT id FROM country WHERE code = 'JAM'), 1, 167, 57),
    (5, 'Ledecky',   'Katie',   'FEMALE', DATE '1997-03-17', (SELECT id FROM country WHERE code = 'USA'), 2, 183, 70),
    (6, 'Manaudou',  'Florent', 'MALE',   DATE '1990-11-12', (SELECT id FROM country WHERE code = 'FRA'), 2, 199, 99)
ON CONFLICT DO NOTHING;

INSERT INTO resultat (id, epreuve_id, athlete_id, rank_position, medal) VALUES
    (1, 1, 1, 1, 'OR'),
    (2, 1, 2, 2, 'ARGENT'),
    (3, 1, 3, 3, 'BRONZE'),
    (4, 2, 1, 1, 'OR'),
    (5, 3, 5, 1, 'OR'),
    (6, 3, 6, 2, 'ARGENT')
ON CONFLICT DO NOTHING;

-- Réaligne les séquences d'identité après insertion d'identifiants explicites,
-- afin que les créations ultérieures via l'API n'entrent pas en collision.
SELECT setval(pg_get_serial_sequence('discipline', 'id'), (SELECT MAX(id) FROM discipline));
SELECT setval(pg_get_serial_sequence('epreuve', 'id'),    (SELECT MAX(id) FROM epreuve));
SELECT setval(pg_get_serial_sequence('athlete', 'id'),    (SELECT MAX(id) FROM athlete));
SELECT setval(pg_get_serial_sequence('resultat', 'id'),   (SELECT MAX(id) FROM resultat));
