package br.com.topone.backend.infrastructure.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tb_suppliers", uniqueConstraints = {
        @UniqueConstraint(name = "uq_suppliers_tax_id", columnNames = "tax_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "tax_id", nullable = false, unique = true, length = 14)
    private String taxId;

    @Column(length = 255)
    private String email;

    @Column(length = 30)
    private String phone;

    @OneToOne(optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "address_id", nullable = false, foreignKey = @ForeignKey(name = "fk_suppliers_address"))
    private AddressEntity address;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinTable(
            name = "tb_supplier_contacts",
            joinColumns = @JoinColumn(name = "supplier_id", nullable = false, foreignKey = @ForeignKey(name = "fk_supplier_contacts_supplier")),
            inverseJoinColumns = @JoinColumn(name = "contact_id", nullable = false, foreignKey = @ForeignKey(name = "fk_supplier_contacts_contact"))
    )
    @Builder.Default
    private List<ContactEntity> contacts = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
