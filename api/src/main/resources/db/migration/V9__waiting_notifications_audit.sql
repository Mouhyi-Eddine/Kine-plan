CREATE TABLE waiting_list_entries (
    id UUID PRIMARY KEY,
    cabinet_id UUID NOT NULL REFERENCES cabinets(id),
    patient_id UUID NOT NULL REFERENCES patients(id),
    care_type_id UUID REFERENCES care_types(id),
    practitioner_membership_id UUID REFERENCES memberships(id),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    proposed_appointment_id UUID REFERENCES appointments(id)
);

CREATE TABLE notification_deliveries (
    id UUID PRIMARY KEY,
    cabinet_id UUID NOT NULL REFERENCES cabinets(id),
    appointment_id UUID REFERENCES appointments(id),
    patient_id UUID REFERENCES patients(id),
    channel VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL,
    sent_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    cabinet_id UUID NOT NULL REFERENCES cabinets(id),
    user_id UUID NOT NULL REFERENCES users(id),
    membership_id UUID REFERENCES memberships(id),
    action VARCHAR(100) NOT NULL,
    resource VARCHAR(100) NOT NULL,
    resource_id UUID,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX waiting_list_cabinet_status_idx ON waiting_list_entries(cabinet_id, status, created_at);
CREATE INDEX notification_deliveries_appointment_idx ON notification_deliveries(cabinet_id, appointment_id, channel);
CREATE INDEX audit_logs_cabinet_time_idx ON audit_logs(cabinet_id, occurred_at);

ALTER TABLE waiting_list_entries ENABLE ROW LEVEL SECURITY;
ALTER TABLE notification_deliveries ENABLE ROW LEVEL SECURITY;
ALTER TABLE audit_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE waiting_list_entries FORCE ROW LEVEL SECURITY;
ALTER TABLE notification_deliveries FORCE ROW LEVEL SECURITY;
ALTER TABLE audit_logs FORCE ROW LEVEL SECURITY;
CREATE POLICY waiting_list_tenant_isolation ON waiting_list_entries
    USING (cabinet_id = current_setting('app.current_cabinet_id', true)::uuid);
CREATE POLICY notification_deliveries_tenant_isolation ON notification_deliveries
    USING (cabinet_id = current_setting('app.current_cabinet_id', true)::uuid);
CREATE POLICY audit_logs_tenant_isolation ON audit_logs
    USING (cabinet_id = current_setting('app.current_cabinet_id', true)::uuid);