package com.contactai.indian_contact_ai.service;

import com.contactai.indian_contact_ai.model.Clause;
import com.contactai.indian_contact_ai.model.ComplianceFlag;
import com.contactai.indian_contact_ai.model.Contract;
import com.contactai.indian_contact_ai.repository.ClauseRepository;
import com.contactai.indian_contact_ai.repository.ComplianceFlagRepository;
import com.contactai.indian_contact_ai.repository.ContractRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class RiskCalculatorService {

    @Autowired
    private LLMService llmService;

    @Autowired
    private RuleEngineService ruleEngineService;

    @Autowired
    private ClauseRepository clauseRepository;

    @Autowired
    private ComplianceFlagRepository complianceFlagRepository;

    @Autowired
    private ContractRepository contractRepository;

    // ---------------------------------------------------------------
    // MAIN METHOD — call this after clauses are saved to DB
    // It processes every clause of a contract end to end
    // ---------------------------------------------------------------
    public void analyzeContract(Contract contract, List<Clause> clauses) {

        double totalLegalScore = 0;
        double totalRegScore   = 0;
        int    clauseCount     = clauses.size();

        // STEP 0 — Analyze ALL clauses in batched calls (e.g. 10 clauses/call)
        // instead of one API call per clause. For a 25-clause contract this
        // turns 25 Gemini calls into 3, which is what actually keeps a free
        // tier usable — see LLMService.analyzeClausesBatch for details.
        java.util.Map<Integer, LLMService.ClauseAnalysisResult> llmResults =
                llmService.analyzeClausesBatch(clauses);

        for (Clause clause : clauses) {

            // STEP 1 — Pull this clause's result out of the batch response
            LLMService.ClauseAnalysisResult llmResult =
                    llmResults.getOrDefault(clause.getClauseNumber(),
                            LLMService.ClauseAnalysisResult.fallback("Missing from batch response"));

            // STEP 2 — Save LLM results into the clause
            clause.setClauseType(llmResult.clauseType);
            clause.setLegalScore(BigDecimal.valueOf(llmResult.legalRiskScore));
            clause.setRegScore(BigDecimal.valueOf(llmResult.regRiskScore));
            clause.setLlmExplanation(llmResult.legalRiskReason + " | " + llmResult.plainEnglish);
            clause.setSuggestion(llmResult.suggestion);
            clauseRepository.save(clause);

            // STEP 3 — Run rule engine on this clause
            List<ComplianceFlag> flags = ruleEngineService.checkClause(clause);

            // STEP 4 — Save each compliance flag to DB
            for (ComplianceFlag flag : flags) {
                flag.setClause(clause);
                flag.setContract(contract);   // set contract so nullable=false is satisfied
                complianceFlagRepository.save(flag);
            }

            // STEP 5 — Accumulate scores for contract-level average
            totalLegalScore += llmResult.legalRiskScore;

            // Regulatory score = average of LLM reg score + rule engine boost
            double ruleBoost = calculateRuleBoost(flags);
            double clauseRegScore = Math.min(100,
                    (llmResult.regRiskScore * 0.5) + (ruleBoost * 0.5));
            totalRegScore += clauseRegScore;
        }

        // STEP 6 — Compute contract-level 2D risk scores and save
        if (clauseCount > 0) {
            double avgLegal = totalLegalScore / clauseCount;
            double avgReg   = totalRegScore   / clauseCount;

            contract.setLegalRisk(
                    BigDecimal.valueOf(avgLegal).setScale(2, RoundingMode.HALF_UP));
            contract.setRegRisk(
                    BigDecimal.valueOf(avgReg).setScale(2, RoundingMode.HALF_UP));
        } else {
            contract.setLegalRisk(BigDecimal.ZERO);
            contract.setRegRisk(BigDecimal.ZERO);
        }

        contract.setStatus(Contract.Status.done);
        contract.setTotalClauses(clauseCount);
        contractRepository.save(contract);
    }

    // ---------------------------------------------------------------
    // HELPER — converts rule engine flags into a 0-100 boost score
    // More severe flags = higher regulatory risk boost
    // ---------------------------------------------------------------
    private double calculateRuleBoost(List<ComplianceFlag> flags) {
        if (flags.isEmpty()) return 0;

        double boost = 0;
        for (ComplianceFlag flag : flags) {
            switch (flag.getSeverity()) {
                case CRITICAL -> boost += 40;
                case HIGH     -> boost += 25;
                case MEDIUM   -> boost += 15;
                case LOW      -> boost += 5;
            }
        }
        // Cap at 100
        return Math.min(100, boost);
    }
}


//What this file does — step by step:
//
//Calls Gemini ONCE per batch of clauses (not once per clause) → gets clause type,
//legal score, reg score, explanation, suggestion for every clause in the batch
//Saves all that into the clauses table
//Runs rule engine → gets list of compliance flags
//Saves each flag to compliance_flags table with both clause and contract set
//Calculates contract-level average legal + regulatory scores
//Saves final scores to the contracts table, sets status to done