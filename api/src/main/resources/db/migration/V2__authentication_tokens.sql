CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    token_hash VARCHAR(128) NOT NULL UNIQUE,
    user_id UUID NOT NULL REFERENCES users(id),
    cabinet_id UUID NOT NULL REFERENCES cabinets(id),
    membership_id UUID NOT NULL REFERENCES memberships(id),
    issued_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMPTZ NOT NULL,
    rotated_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ,
    replaced_by UUID REFERENCES refresh_tokens(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT refresh_tokens_expiry_after_issue CHECK (expires_at > issued_at)
);

CREATE INDEX refresh_tokens_user_idx ON refresh_tokens(user_id);
CREATE INDEX refresh_tokens_cabinet_idx ON refresh_tokens(cabinet_id);
CREATE INDEX refresh_tokens_active_idx ON refresh_tokens(token_hash, expires_at)
    WHERE revoked_at IS NULL AND rotated_at IS NULL;

CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    token_hash VARCHAR(128) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT password_reset_tokens_expiry_after_creation CHECK (expires_at > created_at)
);

CREATE INDEX password_reset_tokens_active_idx ON password_reset_tokens(token_hash, expires_at)
    WHERE used_at IS NULL;

ALTER TABLE cabinets NO FORCE ROW LEVEL SECURITY;
ALTER TABLE cabinets DISABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS cabinets_tenant_isolation ON cabinets;

ALTER TABLE memberships NO FORCE ROW LEVEL SECURITY;
ALTER TABLE memberships DISABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS memberships_tenant_isolation ON memberships;