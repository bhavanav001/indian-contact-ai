package com.contactai.indian_contact_ai.model;




import jakarta.persistence.*;
        import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "obligations")
public class Obligation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @ManyToOne
    @JoinColumn(name = "clause_id")
    private Clause clause;

    @Column(length = 200)
    private String party;

    @Column(columnDefinition = "TEXT")
    private String action;

    @Column(length = 200)
    private String deadline;

    @Column(name = "is_fulfilled")
    private Boolean isFulfilled = false;

    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Priority {
        LOW, MEDIUM, HIGH
    }
}
/*The Obligation class is a JPA entity that maps to the obligations table and is used to store actionable responsibilities extracted from a contract. Each obligation is linked to a Contract (mandatory) and optionally to a specific Clause, representing who (party) needs to do what (action) and by when (deadline). It also tracks whether the task is completed (isFulfilled), assigns a priority level (low, medium, high), and records when the obligation was created (createdAt).

 What it does: It helps convert contract text into trackable tasks, enabling the system to monitor responsibilities, deadlines, and completion status for better contract management.*/