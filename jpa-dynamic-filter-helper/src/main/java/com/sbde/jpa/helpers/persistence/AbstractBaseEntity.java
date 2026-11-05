package com.sbde.jpa.helpers.persistence;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@MappedSuperclass
@EntityListeners({AuditInitializerListener.class, AuditingEntityListener.class})
@NoArgsConstructor
public abstract class AbstractBaseEntity<ID extends Serializable> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "default_gen")
    protected ID id;

    protected AbstractBaseEntity(ID id) {
        this.id = id;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractBaseEntity)) {
            return false;
        }
        AbstractBaseEntity<ID> other = (AbstractBaseEntity<ID>) o;

        return id != null && id.equals(other.getId());
    }


    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
