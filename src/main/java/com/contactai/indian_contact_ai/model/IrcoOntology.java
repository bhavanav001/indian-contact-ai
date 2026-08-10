package com.contactai.indian_contact_ai.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "irco_ontology")
@Getter
@Setter
public class IrcoOntology {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "clause_type", nullable = false)
    private String clauseType;

    @Column(name = "law_name", nullable = false)
    private String lawName;

    @Column(name = "section_num", nullable = false)
    private String sectionNum;

    @Column(name = "check_criteria", columnDefinition = "TEXT", nullable = false)
    private String checkCriteria;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity_default")
    private ComplianceFlag.Severity severityDefault;

    @Column(name = "detection_keywords", columnDefinition = "TEXT")
    private String detectionKeywords;
}