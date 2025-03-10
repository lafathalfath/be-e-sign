package org.bh_foundation.e_sign.models;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
// import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// @Data
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "requested_document")
public class RequestedDocument {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "url", nullable = false, unique = true, columnDefinition = "TEXT")
    private String url;

    @Column(name = "order_sign", nullable = true)
    private boolean orderSign;

    @ManyToOne
    @JoinColumn(name = "applicant_id", referencedColumnName = "id", nullable = false)
    @JsonIgnore
    private User applicant;

    @JsonIgnore
    @ManyToMany(mappedBy = "requestDocuments", fetch = FetchType.LAZY)
    private Set<User> users = new HashSet<>();

}
