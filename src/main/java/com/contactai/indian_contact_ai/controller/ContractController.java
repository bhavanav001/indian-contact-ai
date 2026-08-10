package com.contactai.indian_contact_ai.controller;

import com.contactai.indian_contact_ai.model.*;
import com.contactai.indian_contact_ai.repository.*;
import com.contactai.indian_contact_ai.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractRepository       contractRepository;
    private final UserRepository           userRepository;
    private final ClauseRepository         clauseRepository;
    private final ComplianceFlagRepository complianceFlagRepository;
    private final ObligationRepository     obligationRepository;
    private final DocumentParserService    documentParserService;
    private final ClauseSegmenterService   clauseSegmenterService;
    private final RiskCalculatorService    riskCalculatorService;
    private final ObligationService        obligationService;
    private final ChatService              chatService;
    private final ContractProcessingService contractProcessingService;
    // ── Helper: never cast principal — always look up from DB ────────────────
    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ── POST /api/contracts/upload ───────────────────────────────────────────
    // ── POST /api/contracts/upload ───────────────────────────────────────────
    @PostMapping("/upload")
    public ResponseEntity<?> uploadContract(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = getCurrentUser(userDetails);

            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No file uploaded"));
            }
            if (!documentParserService.isAllowedFile(file)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Only PDF and DOCX files are supported"));
            }

            String savedPath = documentParserService.saveFile(file);
            String extractedText = documentParserService.extractText(file);

            Contract contract = new Contract();
            contract.setUser(user);
            contract.setFilename(file.getOriginalFilename());
            contract.setFilePath(savedPath);
            contract.setRawText(extractedText);
            contract.setStatus(Contract.Status.processing);
            contractRepository.save(contract);

            // Kick off the heavy pipeline in the background — request returns now.
            contractProcessingService.processAsync(contract, extractedText);

            return ResponseEntity.ok(Map.of(
                    "id",       contract.getId(),
                    "filename", contract.getFilename(),
                    "status",   contract.getStatus(),
                    "message",  "Contract uploaded. Analysis is running — poll GET /api/contracts/{id} for status."
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ── GET /api/contracts/history ───────────────────────────────────────────
    @GetMapping("/history")
    public ResponseEntity<?> getHistory(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        return ResponseEntity.ok(contractRepository.findByUserOrderByCreatedAtDesc(user));
    }

    // ── GET /api/contracts/{id} ──────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> getContract(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        return contractRepository.findById(id)
                .filter(c -> c.getUser().getId().equals(user.getId()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ── GET /api/contracts/{id}/clauses ──────────────────────────────────────
    @GetMapping("/{id}/clauses")
    public ResponseEntity<?> getClauses(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        return contractRepository.findById(id)
                .filter(c -> c.getUser().getId().equals(user.getId()))
                .map(contract -> ResponseEntity.ok(
                        clauseRepository.findByContractOrderByClauseNumber(contract)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ── GET /api/contracts/{id}/risks ────────────────────────────────────────
    @GetMapping("/{id}/risks")
    public ResponseEntity<?> getRisks(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        return contractRepository.findById(id)
                .filter(c -> c.getUser().getId().equals(user.getId()))
                .map(contract -> ResponseEntity.ok(Map.of(
                        "contractId",   contract.getId(),
                        "filename",     contract.getFilename(),
                        "legalRisk",    contract.getLegalRisk()   != null ? contract.getLegalRisk()   : 0,
                        "regRisk",      contract.getRegRisk()     != null ? contract.getRegRisk()     : 0,
                        "totalClauses", contract.getTotalClauses()!= null ? contract.getTotalClauses(): 0,
                        "clauses",      clauseRepository.findByContractOrderByClauseNumber(contract)
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    // ── GET /api/contracts/{id}/compliance ───────────────────────────────────
    @GetMapping("/{id}/compliance")
    public ResponseEntity<?> getCompliance(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        return contractRepository.findById(id)
                .filter(c -> c.getUser().getId().equals(user.getId()))
                .map(contract -> ResponseEntity.ok(
                        complianceFlagRepository.findByContract(contract)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ── GET /api/contracts/{id}/obligations ──────────────────────────────────
    @GetMapping("/{id}/obligations")
    public ResponseEntity<?> getObligations(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        return contractRepository.findById(id)
                .filter(c -> c.getUser().getId().equals(user.getId()))
                .map(contract -> ResponseEntity.ok(
                        obligationRepository.findByContract(contract)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ── PATCH /api/contracts/{id}/obligations/{obligationId}/fulfill ─────────
    @PatchMapping("/{id}/obligations/{obligationId}/fulfill")
    public ResponseEntity<?> fulfillObligation(
            @PathVariable Integer id,
            @PathVariable Integer obligationId,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = getCurrentUser(userDetails);

        Contract contract = contractRepository.findById(id).orElse(null);
        if (contract == null || !contract.getUser().getId().equals(user.getId())) {
            return ResponseEntity.notFound().build();
        }

        Obligation obligation = obligationRepository.findById(obligationId).orElse(null);
        if (obligation == null || !obligation.getContract().getId().equals(contract.getId())) {
            return ResponseEntity.notFound().build();
        }

        obligationService.markFulfilled(obligationId);

        return ResponseEntity.ok(Map.of(
                "obligationId", obligationId,
                "isFulfilled", true
        ));
    }

    // ── POST /api/contracts/{id}/chat ────────────────────────────────────────
    @PostMapping("/{id}/chat")
    public ResponseEntity<?> chat(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = getCurrentUser(userDetails);
            String userMessage = body.get("message");
            if (userMessage == null || userMessage.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Message cannot be empty"));
            }
            String aiResponse = chatService.chat(id, userMessage, user);
            return ResponseEntity.ok(Map.of("response", aiResponse));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ── GET /api/contracts/{id}/chat ─────────────────────────────────────────
    @GetMapping("/{id}/chat")
    public ResponseEntity<?> getChatHistory(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        return contractRepository.findById(id)
                .filter(c -> c.getUser().getId().equals(user.getId()))
                .map(contract -> ResponseEntity.ok(chatService.getHistory(contract)))
                .orElse(ResponseEntity.notFound().build());
    }
}