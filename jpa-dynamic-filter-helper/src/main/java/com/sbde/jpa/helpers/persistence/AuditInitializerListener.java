package com.sbde.jpa.helpers.persistence;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

/**
 * Spring Data auditing skips a null embedded {@link Audit} (builders, rows saved before auditing existed),
 * so it must be instantiated before {@code AuditingEntityListener} runs.
 */
public class AuditInitializerListener {

    @PrePersist
    @PreUpdate
    public void initAudit(Object entity) {
        if (entity instanceof Auditable auditable && auditable.getAudit() == null) {
            auditable.setAudit(new Audit());
        }
    }
}
