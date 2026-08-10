package com.contactai.indian_contact_ai.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

    @Data
    @Entity
    @Table(name = "contracts")
    public class Contract {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;

        @ManyToOne
        @JoinColumn(name = "user_id", nullable = false)
        private User user;

        @Column(nullable = false, length = 255)
        private String filename;

        @Column(name = "file_path", nullable = false, length = 500)
        private String filePath;

        @Column(name = "raw_text", columnDefinition = "LONGTEXT")
        @JsonIgnore
        private String rawText;

        @Enumerated(EnumType.STRING)
        private Status status = Status.uploaded;

        @Column(name = "total_clauses")
        private Integer totalClauses = 0;

        @Column(name = "legal_risk", precision = 5, scale = 2)
        private BigDecimal legalRisk;

        @Column(name = "reg_risk", precision = 5, scale = 2)
        private BigDecimal regRisk;

        @Column(name = "created_at")
        private LocalDateTime createdAt = LocalDateTime.now();

        public enum Status {
            uploaded, processing, done, error
        }
    }
/*The Contract class is a JPA entity that represents a single record in the contracts database table, used to store and manage contract-related data in a Java application. It includes fields like an auto-generated primary key (id), file details (filename, filePath), and large contract content (rawText), along with a relationship mapping where multiple contracts belong to one user (@ManyToOne). It tracks the contract’s processing state using an enum (uploaded, processing, done, error), stores analysis results such as total clauses and risk scores (legalRisk, regRisk) using precise decimal values, and records the creation timestamp (createdAt). Annotations from JPA define how the class maps to the database, while Lombok (@Data) automatically generates common methods like getters and setters, making the code cleaner and more efficient.*/