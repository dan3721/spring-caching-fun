CREATE TABLE IF NOT EXISTS pokemon_set (
    id VARCHAR(64) PRIMARY KEY,
    name TEXT NOT NULL,
    series TEXT NOT NULL,
    printed_total INTEGER,
    total INTEGER,
    release_date DATE,
    updated_at TIMESTAMP,
    symbol_url TEXT,
    logo_url TEXT
);

WITH json_source AS (
    SELECT pg_read_file('/docker-entrypoint-initdb.d/set-data.json')::jsonb AS content
),
set_rows AS (
    SELECT jsonb_array_elements(content) AS set_item
    FROM json_source
)
INSERT INTO pokemon_set (
    id,
    name,
    series,
    printed_total,
    total,
    release_date,
    updated_at,
    symbol_url,
    logo_url
)
SELECT
    set_item ->> 'id',
    set_item ->> 'name',
    set_item ->> 'series',
    NULLIF(set_item ->> 'printedTotal', '')::INTEGER,
    NULLIF(set_item ->> 'total', '')::INTEGER,
    to_date(NULLIF(trim(set_item ->> 'releaseDate'), ''), 'YYYY/MM/DD'),
    to_timestamp(NULLIF(trim(set_item ->> 'updatedAt'), ''), 'YYYY/MM/DD HH24:MI:SS'),
    set_item -> 'images' ->> 'symbol',
    set_item -> 'images' ->> 'logo'
FROM set_rows
ON CONFLICT (id) DO NOTHING;
