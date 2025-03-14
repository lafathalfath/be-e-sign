package org.bh_foundation.e_sign.models;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.validator.constraints.UniqueElements;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "document")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title", nullable = false)
    @NotNull
    private String title;

    @Column(name = "url", nullable = false, unique = true)
    @NotNull
    @UniqueElements
    private String url;

    @Column(name = "order_sign", nullable = false)
    @NotNull
    private Boolean orderSign;

    @Column(name = "enabled", nullable = false)
    @NotNull
    private Boolean enabled;

    @Column(name = "request_count", nullable = false)
    @NotNull
    private Integer requestCount;

    @Column(name = "sign_count", nullable = false)
    @NotNull
    private Integer signedCount;

    // RELATIONS
    @ManyToOne
    @JoinColumn(name = "applicant_id", referencedColumnName = "id", nullable = false)
    @JsonIgnore
    private User applicant;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "p_sign_user", joinColumns = @JoinColumn(name = "document_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"))
    private Set<User> signers = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DocumentApproval> documentApprovals = new HashSet<>();

}
