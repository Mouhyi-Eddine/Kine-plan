CREATE TABLE membership_invitations (
    id UUID PRIMARY KEY,
    cabinet_id UUID NOT NULL REFERENCES cabinets(id),
    email VARCHAR(320) NOT NULL,
    role VARCHAR(30) NOT NULL,
    token_hash VARCHAR(128) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    accepted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT membership_invitations_expiry_after_creation CHECK (expires_at > created_at)
);

CREATE INDEX membership_invitations_cabinet_idx ON membership_invitations(cabinet_id);
CREATE INDEX membership_invitations_active_idx ON membership_invitations(token_hash, expires_at)
    WHERE accepted_at IS NULL;