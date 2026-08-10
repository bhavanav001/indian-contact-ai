package com.contactai.indian_contact_ai.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "clauses")
public class Clause {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Column(name = "clause_text", nullable = false, columnDefinition = "TEXT")
    private String clauseText;

    @Column(name = "clause_type", length = 100)
    private String clauseType;

    @Column(name = "clause_number")
    private Integer clauseNumber;

    @Column(name = "legal_score", precision = 5, scale = 2)
    private BigDecimal legalScore;

    @Column(name = "reg_score", precision = 5, scale = 2)
    private BigDecimal regScore;

    @Column(name = "llm_explanation", columnDefinition = "TEXT")
    private String llmExplanation;

    @Column(name = "suggestion", columnDefinition = "TEXT")
    private String suggestion;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
/* The Clause class is a JPA entity that represents individual clauses of a contract and maps to the clauses table. It maintains a many-to-one relationship with the Contract entity, meaning multiple clauses belong to a single contract. Each clause stores its text, type, and order, along with analysis results like legal and regulatory risk scores using BigDecimal for precision. Additionally, it includes AI-generated explanations and suggestions, making it useful for intelligent contract analysis systems. JPA annotations handle the database mapping, while Lombok simplifies the code by generating boilerplate methods.”*/

