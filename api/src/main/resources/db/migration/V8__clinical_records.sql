CREATE TABLE prescriptions (
    id UUID PRIMARY KEY,
    cabinet_id UUID NOT NULL REFERENCES cabinets(id),
    patient_id UUID NOT NULL REFERENCES patients(id),
    prescriber VARCHAR(200) NOT NULL,
    prescribed_at DATE NOT NULL,
    sessions_prescribed INTEGER NOT NULL,
    sessions_consumed INTEGER NOT NULL DEFAULT 0,
    expires_at DATE,
    CONSTRAINT prescriptions_sessions_positive CHECK (sessions_prescribed > 0),
    CONSTRAINT prescriptions_consumed_non_negative CHECK (sessions_consumed >= 0)
);

CREATE TABLE clinical_notes (
    id UUID PRIMARY KEY,
    cabinet_id UUID NOT NULL REFERENCES cabinets(id),
    patient_id UUID NOT NULL REFERENCES patients(id),
    appointment_id UUID REFERENCES appointments(id),
    author_membership_id UUID NOT NULL REFERENCES memberships(id),
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX prescriptions_patient_idx ON prescriptions(cabinet_id, patient_id);
CREATE INDEX clinical_notes_patient_idx ON clinical_notes(cabinet_id, patient_id, created_at);
ALTER TABLE prescriptions ENABLE ROW LEVEL SECURITY;
ALTER TABLE clinical_notes ENABLE ROW LEVEL SECURITY;
ALTER TABLE prescriptions FORCE ROW LEVEL SECURITY;
ALTER TABLE clinical_notes FORCE ROW LEVEL SECURITY;
CREATE POLICY prescriptions_tenant_isolation ON prescriptions
    USING (cabinet_id = current_setting('app.current_cabinet_id', true)::uuid);
CREATE POLICY clinical_notes_tenant_isolation ON clinical_notes
    USING (cabinet_id = current_setting('app.current_cabinet_id', true)::uuid);