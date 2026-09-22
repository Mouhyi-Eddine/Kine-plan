CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE cabinets (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    address VARCHAR(500),
    siret VARCHAR(14),
    timezone VARCHAR(64) NOT NULL DEFAULT 'Europe/Paris',
    status VARCHAR(20) NOT NULL,
    subscription_plan VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    suspended_at TIMESTAMPTZ,
    terminated_at TIMESTAMPTZ
);

CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(30),
    rpps_adeli VARCHAR(30),
    platform_admin BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL,
    last_login_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE memberships (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    cabinet_id UUID NOT NULL REFERENCES cabinets(id),
    role VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    invited_at TIMESTAMPTZ,
    accepted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, cabinet_id)
);

CREATE TABLE cabinet_settings (
    cabinet_id UUID PRIMARY KEY REFERENCES cabinets(id),
    reminder_delay_minutes INTEGER NOT NULL DEFAULT 1440,
    default_slot_duration_minutes INTEGER NOT NULL DEFAULT 30,
    notification_texts JSONB NOT NULL DEFAULT '{}'::jsonb
);

CREATE TABLE patients (
    id UUID PRIMARY KEY,
    cabinet_id UUID NOT NULL REFERENCES cabinets(id),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    birth_date DATE,
    phone VARCHAR(30),
    email VARCHAR(320),
    emergency_contact JSONB,
    sms_consent BOOLEAN NOT NULL DEFAULT FALSE,
    email_consent BOOLEAN NOT NULL DEFAULT FALSE,
    rgpd_consent BOOLEAN NOT NULL DEFAULT FALSE,
    archived_at TIMESTAMPTZ,
    anonymized_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE appointments (
    id UUID PRIMARY KEY,
    cabinet_id UUID NOT NULL REFERENCES cabinets(id),
    patient_id UUID NOT NULL REFERENCES patients(id),
    practitioner_membership_id UUID NOT NULL REFERENCES memberships(id),
    start_at TIMESTAMPTZ NOT NULL,
    end_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL,
    source VARCHAR(20) NOT NULL,
    cancellation_reason VARCHAR(500),
    created_by UUID NOT NULL REFERENCES users(id),
    updated_by UUID NOT NULL REFERENCES users(id),
    version BIGINT NOT NULL DEFAULT 0,
    deleted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT appointments_end_after_start CHECK (end_at > start_at)
);

ALTER TABLE appointments ADD CONSTRAINT appointments_no_overlap
    EXCLUDE USING gist (
        cabinet_id WITH =,
        practitioner_membership_id WITH =,
        tstzrange(start_at, end_at, '[)') WITH &&
    ) WHERE (status <> 'ANNULE' AND deleted_at IS NULL);

CREATE INDEX memberships_cabinet_idx ON memberships(cabinet_id);
CREATE INDEX patients_cabinet_idx ON patients(cabinet_id);
CREATE INDEX appointments_cabinet_start_idx ON appointments(cabinet_id, start_at);
CREATE INDEX appointments_practitioner_start_idx ON appointments(cabinet_id, practitioner_membership_id, start_at);

ALTER TABLE memberships ENABLE ROW LEVEL SECURITY;
ALTER TABLE cabinets ENABLE ROW LEVEL SECURITY;
ALTER TABLE cabinet_settings ENABLE ROW LEVEL SECURITY;
ALTER TABLE patients ENABLE ROW LEVEL SECURITY;
ALTER TABLE appointments ENABLE ROW LEVEL SECURITY;
ALTER TABLE memberships FORCE ROW LEVEL SECURITY;
ALTER TABLE cabinets FORCE ROW LEVEL SECURITY;
ALTER TABLE cabinet_settings FORCE ROW LEVEL SECURITY;
ALTER TABLE patients FORCE ROW LEVEL SECURITY;
ALTER TABLE appointments FORCE ROW LEVEL SECURITY;

CREATE POLICY cabinets_tenant_isolation ON cabinets
    USING (id = current_setting('app.current_cabinet_id', true)::uuid);

CREATE POLICY memberships_tenant_isolation ON memberships
    USING (cabinet_id = current_setting('app.current_cabinet_id', true)::uuid);
CREATE POLICY cabinet_settings_tenant_isolation ON cabinet_settings
    USING (cabinet_id = current_setting('app.current_cabinet_id', true)::uuid);
CREATE POLICY patients_tenant_isolation ON patients
    USING (cabinet_id = current_setting('app.current_cabinet_id', true)::uuid);
CREATE POLICY appointments_tenant_isolation ON appointments
    USING (cabinet_id = current_setting('app.current_cabinet_id', true)::uuid);