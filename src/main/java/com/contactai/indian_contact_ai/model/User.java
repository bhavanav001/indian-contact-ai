
// what is model?
/*  its a simple java class that mirrors  database table */
package com.contactai.indian_contact_ai.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;//java persistance api without this java wounld have idea how to map classes to database
import lombok.Data;//atomatically generates getters,setters,toString(),equals(),hashcode()
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")

public class User {

    @Id //private key
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 255)
    @JsonIgnore
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.user;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Role {
        user, admin
    }
}
/* This entity represents the clause-level abstraction of a contract, which is the fundamental unit for analysis in my system. It acts as the bridge between raw legal text and AI-driven insights by storing not just the clause content, but also evaluation metrics like legal and regulatory scores, explanations, and suggestions. This design enables scalable processing, fine-grained querying, and explainable AI, making it central to both the data model and the intelligence layer of the application*/