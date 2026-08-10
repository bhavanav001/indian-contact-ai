package com.contactai.indian_contact_ai.repository;


import com.contactai.indian_contact_ai.model.ChatHistory;
import com.contactai.indian_contact_ai.model.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Integer> {
    List<ChatHistory> findByContractOrderBySentAt(Contract contract);
}