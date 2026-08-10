package com.contactai.indian_contact_ai.service;

import com.contactai.indian_contact_ai.model.ChatHistory;
import com.contactai.indian_contact_ai.model.Contract;
import com.contactai.indian_contact_ai.model.User;
import com.contactai.indian_contact_ai.repository.ChatHistoryRepository;
import com.contactai.indian_contact_ai.repository.ContractRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    @Autowired
    private LLMService llmService;

    @Autowired
    private ChatHistoryRepository chatHistoryRepository;

    @Autowired
    private ContractRepository contractRepository;

    // ---------------------------------------------------------------
    // MAIN METHOD — call this from ChatController
    // Saves user message, calls Gemini, saves AI response, returns it
    // ---------------------------------------------------------------
    public String chat(Integer contractId, String userMessage, User currentUser) {

        // STEP 1 — Load contract (we need raw text for context)
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found: " + contractId));

        // STEP 2 — Save user message to chat_history
        ChatHistory userEntry = new ChatHistory();
        userEntry.setContract(contract);
        userEntry.setUser(currentUser);
        userEntry.setRole(ChatHistory.Role.user);
        userEntry.setMessage(userMessage);
        chatHistoryRepository.save(userEntry);

        // STEP 3 — Call Gemini with contract text + user question
        String aiResponse = llmService.chat(contract.getRawText(), userMessage);

        // STEP 4 — Save AI response to chat_history
        ChatHistory aiEntry = new ChatHistory();
        aiEntry.setContract(contract);
        aiEntry.setUser(currentUser);
        aiEntry.setRole(ChatHistory.Role.assistant);
        aiEntry.setMessage(aiResponse);
        chatHistoryRepository.save(aiEntry);

        // STEP 5 — Return AI response to controller
        return aiResponse;
    }

    // ---------------------------------------------------------------
    // Get full chat history for a contract (used by GET endpoint)
    // ---------------------------------------------------------------
    public List<ChatHistory> getHistory(Contract contract) {
        return chatHistoryRepository.findByContractOrderBySentAt(contract);
    }
    }
