package com.contactai.indian_contact_ai.repository;



import com.contactai.indian_contact_ai.model.Contract;
import com.contactai.indian_contact_ai.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContractRepository extends JpaRepository<Contract, Integer> {
    List<Contract> findByUserOrderByCreatedAtDesc(User user);
}