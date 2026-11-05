package com.sbde.jpa.helpers.persistence;

public interface Auditable {

    Audit getAudit();

    void setAudit(Audit audit);
}
