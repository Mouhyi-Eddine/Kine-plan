CREATE TABLE working_hours (
    id UUID PRIMARY KEY,
    cabinet_id UUID NOT NULL REFERENCES cabinets(id),
    practitioner_membership_id UUID NOT NULL REFERENCES memberships(id),
    day_of_week SMALLINT NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    UNIQUE (cabinet_id, practitioner_membership_id, day_of_week, start_time),
    CONSTRAINT working_hours_day_valid CHECK (day_of_week BETWEEN 1 AND 7),
    CONSTRAINT working_hours_end_after_start CHECK (end_time > start_time)
);

CREATE TABLE practitioner_time_off (
    id UUID PRIMARY KEY,
    cabinet_id UUID NOT NULL REFERENCES cabinets(id),
    practitioner_membership_id UUID NOT NULL REFERENCES memberships(id),
    start_at TIMESTAMPTZ NOT NULL,
    end_at TIMESTAMPTZ NOT NULL,
    reason VARCHAR(500),
    CONSTRAINT practitioner_time_off_end_after_start CHECK (end_at > start_at)
);

CREATE TABLE cabinet_closures (
    id UUID PRIMARY KEY,
    cabinet_id UUID NOT NULL REFERENCES cabinets(id),
    start_at TIMESTAMPTZ NOT NULL,
    end_at TIMESTAMPTZ NOT NULL,
    reason VARCHAR(500),
    CONSTRAINT cabinet_closures_end_after_start CHECK (end_at > start_at)
);

CREATE INDEX working_hours_practitioner_idx ON working_hours(cabinet_id, practitioner_membership_id, day_of_week);
CREATE INDEX practitioner_time_off_range_idx ON practitioner_time_off(cabinet_id, practitioner_membership_id, start_at);
CREATE INDEX cabinet_closures_range_idx ON cabinet_closures(cabinet_id, start_at);

ALTER TABLE working_hours ENABLE ROW LEVEL SECURITY;
ALTER TABLE practitioner_time_off ENABLE ROW LEVEL SECURITY;
ALTER TABLE cabinet_closures ENABLE ROW LEVEL SECURITY;
ALTER TABLE working_hours FORCE ROW LEVEL SECURITY;
ALTER TABLE practitioner_time_off FORCE ROW LEVEL SECURITY;
ALTER TABLE cabinet_closures FORCE ROW LEVEL SECURITY;
CREATE POLICY working_hours_tenant_isolation ON working_hours
    USING (cabinet_id = current_setting('app.current_cabinet_id', true)::uuid);
CREATE POLICY practitioner_time_off_tenant_isolation ON practitioner_time_off
    USING (cabinet_id = current_setting('app.current_cabinet_id', true)::uuid);
CREATE POLICY cabinet_closures_tenant_isolation ON cabinet_closures
    USING (cabinet_id = current_setting('app.current_cabinet_id', true)::uuid);