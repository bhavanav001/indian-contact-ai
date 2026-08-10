package com.contactai.indian_contact_ai.repository;


import com.contactai.indian_contact_ai.model.ComplianceFlag;
import com.contactai.indian_contact_ai.model.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ComplianceFlagRepository extends JpaRepository<ComplianceFlag, Integer> {
    List<ComplianceFlag> findByContract(Contract contract);
}