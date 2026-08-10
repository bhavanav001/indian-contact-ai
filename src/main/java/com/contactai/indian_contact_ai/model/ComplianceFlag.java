package com.contactai.indian_contact_ai.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "compliance_flags")
public class ComplianceFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "clause_id", nullable = false)
    private Clause clause;

    @ManyToOne
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Column(name = "law_name", length = 100)
    private String lawName;

    @Column(name = "section_num", length = 50)
    private String sectionNum;

    @Column(columnDefinition = "TEXT")
    private String violation;

    @Convert(converter = ComplianceFlag.SeverityConverter.class)
    private Severity severity;

    @Column(name = "flagged_at")
    private LocalDateTime flaggedAt = LocalDateTime.now();

    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL;

        @JsonCreator
        public static Severity fromValue(String value) {
            return Severity.valueOf(value.toUpperCase());
        }
    }

    @Converter
    public static class SeverityConverter
            implements AttributeConverter<Severity, String> {

        @Override
        public String convertToDatabaseColumn(Severity severity) {
            return severity == null ? null : severity.name();
        }

        @Override
        public Severity convertToEntityAttribute(String value) {
            return value == null ? null : Severity.valueOf(value.toUpperCase());
        }
    }
}