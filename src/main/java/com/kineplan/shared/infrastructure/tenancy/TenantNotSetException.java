package com.kineplan.shared.infrastructure.tenancy;

public class TenantNotSetException extends RuntimeException {
    public TenantNotSetException() {
        super("A tenant context is required for this operation");
    }
}