CREATE TABLE appointment_series (
    id UUID PRIMARY KEY,
    cabinet_id UUID NOT NULL REFERENCES cabinets(id),
    patient_id UUID NOT NULL REFERENCES patients(id),
    practitioner_membership_id UUID NOT NULL REFERENCES memberships(id),
    care_type_id UUID REFERENCES care_types(id),
    occurrences INTEGER NOT NULL,
    interval_days INTEGER NOT NULL,
    created_by UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT appointment_series_occurrences_positive CHECK (occurrences > 0),
    CONSTRAINT appointment_series_interval_positive CHECK (interval_days > 0)
);

ALTER TABLE appointments ADD COLUMN series_id UUID REFERENCES appointment_series(id);
ALTER TABLE appointments ADD COLUMN occurrence_number INTEGER;
CREATE INDEX appointment_series_cabinet_idx ON appointment_series(cabinet_id);
CREATE INDEX appointments_series_idx ON appointments(cabinet_id, series_id);

ALTER TABLE appointment_series ENABLE ROW LEVEL SECURITY;
ALTER TABLE appointment_series FORCE ROW LEVEL SECURITY;
CREATE POLICY appointment_series_tenant_isolation ON appointment_series
    USING (cabinet_id = current_setting('app.current_cabinet_id', true)::uuid);