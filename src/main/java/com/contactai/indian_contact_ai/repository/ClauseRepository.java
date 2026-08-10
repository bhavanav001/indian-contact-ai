package com.contactai.indian_contact_ai.repository;

import com.contactai.indian_contact_ai.model.Clause;
import com.contactai.indian_contact_ai.model.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClauseRepository extends JpaRepository<Clause, Integer> {
    List<Clause> findByContractOrderByClauseNumber(Contract contract);
}
