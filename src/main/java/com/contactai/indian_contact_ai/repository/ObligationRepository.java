package com.contactai.indian_contact_ai.repository;
import com.contactai.indian_contact_ai.model.Contract;
import com.contactai.indian_contact_ai.model.Obligation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ObligationRepository extends JpaRepository<Obligation, Integer> {
    List<Obligation> findByContract(Contract contract);
}

