package com.contactai.indian_contact_ai.service;

import com.contactai.indian_contact_ai.model.Clause;
import com.contactai.indian_contact_ai.model.Contract;
import com.contactai.indian_contact_ai.repository.ContractRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContractProcessingService {

    @Autowired private ClauseSegmenterService clauseSegmenterService;
    @Autowired private RiskCalculatorService riskCalculatorService;
    @Autowired private ObligationService obligationService;
    @Autowired private ContractRepository contractRepository;

    // Runs on a background thread so the HTTP request returns immediately.
    // Frontend polls GET /api/contracts/{id} until status == "done" or "error".
    @Async
    public void processAsync(Contract contract, String extractedText) {
        try {
            List<Clause> clauses = clauseSegmenterService.segmentAndSave(contract, extractedText);
            riskCalculatorService.analyzeContract(contract, clauses); // sets status=done internally
            obligationService.extractAndSave(contract);
        } catch (Exception e) {
            System.err.println("[ContractProcessingService] Pipeline failed for contract "
                    + contract.getId() + ": " + e.getMessage());
            e.printStackTrace();
            contract.setStatus(Contract.Status.error);
            contractRepository.save(contract);
        }
    }
}