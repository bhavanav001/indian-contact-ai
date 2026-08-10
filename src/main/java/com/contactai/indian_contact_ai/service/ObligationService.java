package com.contactai.indian_contact_ai.service;

import com.contactai.indian_contact_ai.model.Contract;
import com.contactai.indian_contact_ai.model.Obligation;
import com.contactai.indian_contact_ai.repository.ObligationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ObligationService {

    @Autowired
    private LLMService llmService;

    @Autowired
    private ObligationRepository obligationRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ---------------------------------------------------------------
    // MAIN METHOD — call this once per contract after analysis is done
    // ---------------------------------------------------------------
    public List<Obligation> extractAndSave(Contract contract) {
        List<Obligation> saved = new ArrayList<>();

        try {
            // STEP 1 — Ask Gemini to extract obligations from contract text
            String rawJson = llmService.extractObligations(contract.getRawText());

            // STEP 2 — Clean response (Gemini sometimes adds backticks)
            String cleaned = rawJson
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            // STEP 3 — Parse JSON array
            JsonNode array = objectMapper.readTree(cleaned);

            if (!array.isArray()) {
                System.err.println("Obligation extraction: expected JSON array, got: " + cleaned);
                return saved;
            }

            // STEP 4 — Save each obligation to DB
            for (JsonNode item : array) {
                Obligation obligation = new Obligation();
                obligation.setContract(contract);
                obligation.setParty(item.path("party").asText("Unknown"));
                obligation.setAction(item.path("action").asText(""));
                obligation.setDeadline(item.path("deadline").asText("Not specified"));
                obligation.setIsFulfilled(false);

                // Map priority string to enum
                String priorityStr = item.path("priority").asText("medium").toUpperCase();
                try {
                    obligation.setPriority(Obligation.Priority.valueOf(priorityStr));
                } catch (IllegalArgumentException e) {
                    obligation.setPriority(Obligation.Priority.MEDIUM);
                }

                obligationRepository.save(obligation);
                saved.add(obligation);
            }

        } catch (Exception e) {
            System.err.println("Obligation extraction failed: " + e.getMessage());
        }

        return saved;
    }

    // ---------------------------------------------------------------
    // Get all obligations for a contract (used by GET endpoint)
    // ---------------------------------------------------------------
    public List<Obligation> getByContract(Contract contract) {
        return obligationRepository.findByContract(contract);
    }

    // ---------------------------------------------------------------
    // Mark an obligation as fulfilled (used by frontend toggle button)
    // ---------------------------------------------------------------
    public void markFulfilled(Integer obligationId) {
        obligationRepository.findById(obligationId).ifPresent(ob -> {
            ob.setIsFulfilled(true);
            obligationRepository.save(ob);
        });
    }
}