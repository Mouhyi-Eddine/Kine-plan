CREATE TABLE care_types (
    id UUID PRIMARY KEY,
    cabinet_id UUID NOT NULL REFERENCES cabinets(id),
    name VARCHAR(200) NOT NULL,
    default_duration_minutes INTEGER NOT NULL,
    display_color VARCHAR(20),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (cabinet_id, name),
    CONSTRAINT care_types_duration_positive CHECK (default_duration_minutes > 0)
);

ALTER TABLE care_types ENABLE ROW LEVEL SECURITY;
ALTER TABLE care_types FORCE ROW LEVEL SECURITY;
CREATE POLICY care_types_tenant_isolation ON care_types
    USING (cabinet_id = current_setting('app.current_cabinet_id', true)::uuid);

ALTER TABLE appointments ADD COLUMN care_type_id UUID REFERENCES care_types(id);
CREATE INDEX care_types_cabinet_idx ON care_types(cabinet_id);
CREATE INDEX appointments_care_type_idx ON appointments(cabinet_id, care_type_id);