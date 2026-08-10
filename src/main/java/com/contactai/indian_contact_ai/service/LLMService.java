package com.contactai.indian_contact_ai.service;

import com.contactai.indian_contact_ai.model.Clause;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

@Service
public class LLMService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    // "gemini-flash-latest" always points Google's current recommended free-tier
    // Flash model, so you don't need to touch code every time a model is retired.
    // If you want the higher RPM free-tier variant, set gemini.model=gemini-2.5-flash-lite
    @Value("${gemini.model:gemini-flash-latest}")
    private String model;

    // Gemini free tier (2026): Flash ~10-15 RPM / 1,500 RPD / 250k TPM.
    // Flash-Lite ~30 RPM but a slightly weaker model. We throttle to a safe
    // default of ~13 RPM (4.6s/call) so a single user session never 429s.
    // Override with gemini.min-interval-ms if you switch to Flash-Lite.
    @Value("${gemini.min-interval-ms:4600}")
    private long minIntervalMs;

    // How many clauses go into ONE Gemini call. This is the single biggest
    // cost/rate-limit lever in this app: instead of 1 API call per clause
    // (e.g. 25 clauses = 25 calls), we send them in batches, so a 25-clause
    // contract costs ceil(25/10) = 3 calls instead of 25. ~85-90% fewer calls.
    @Value("${gemini.clause-batch-size:10}")
    private int clauseBatchSize;

    private static final String GEMINI_URL_TEMPLATE =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private volatile long lastCallAt = 0L;

    private static final String SYSTEM_PROMPT = """
        You are an expert Indian contract lawyer specializing in commercial law.
        You are familiar with the following Indian statutes:
        - Indian Contract Act, 1872 (ICA)
        - Digital Personal Data Protection Act, 2023 (DPDP Act)
        - Goods and Services Tax Act, 2017 (GST Act)
        - Arbitration and Conciliation Act, 1996
        - Information Technology Act, 2000 (IT Act)
        - Copyright Act, 1957
        - Consumer Protection Act, 2019

        When analyzing contract clauses, always:
        1. Identify which Indian law applies
        2. Flag clauses that would be void, voidable, or unenforceable under Indian law
        3. Note if a clause violates the DPDP Act 2023 or GST requirements
        4. Suggest India-specific alternatives for risky clauses
        5. Flag penalty clauses — Indian courts can reduce unconscionable penalties (Section 74 ICA)
        """;

    private static final List<String> VALID_CLAUSE_TYPES = List.of(
            "liability", "payment", "termination", "data_protection", "ip",
            "confidentiality", "dispute", "governing_law", "force_majeure",
            "indemnity", "other");

    // ─────────────────────────────────────────────
    // 1. CLAUSE ANALYSIS — BATCHED (was: 1 call per clause)
    // ─────────────────────────────────────────────

    /**
     * Analyzes ALL clauses of a contract using as few Gemini calls as possible.
     * Splits clauses into batches of {@code clauseBatchSize} and sends one
     * request per batch, asking Gemini to return a JSON array keyed by
     * clause_number so results can be mapped back to the right Clause object.
     *
     * Returns a map of clauseNumber -> ClauseAnalysisResult. Any clause that
     * Gemini didn't return a result for (rare, e.g. truncated output) gets a
     * fallback result so the pipeline never breaks.
     */
    public Map<Integer, ClauseAnalysisResult> analyzeClausesBatch(List<Clause> clauses) {
        Map<Integer, ClauseAnalysisResult> results = new HashMap<>();
        if (clauses == null || clauses.isEmpty()) return results;

        List<List<Clause>> batches = partition(clauses, clauseBatchSize);

        for (List<Clause> batch : batches) {
            String prompt = buildBatchPrompt(batch);
            String rawResponse = callGemini(prompt, buildBatchResponseSchema());
            Map<Integer, ClauseAnalysisResult> parsed = parseBatchResponse(rawResponse);
            results.putAll(parsed);

            // Fallback for any clause Gemini silently skipped
            for (Clause c : batch) {
                results.putIfAbsent(c.getClauseNumber(),
                        ClauseAnalysisResult.fallback("No result returned for this clause"));
            }
        }
        return results;
    }

    private String buildBatchPrompt(List<Clause> batch) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
            Analyze EACH of the following Indian contract clauses independently.
            Respond ONLY with a valid JSON array — one object per clause, in the
            same order, each object tagged with its clause_number.

            Required JSON format for each element:
            {
              "clause_number": <int, matches the number given below>,
              "clause_type": "<one of: liability, payment, termination, data_protection, ip, confidentiality, dispute, governing_law, force_majeure, indemnity, other>",
              "legal_risk_score": <integer 0-100>,
              "reg_risk_score": <integer 0-100>,
              "legal_risk_reason": "<one sentence explaining the legal risk>",
              "regulation_violated": "<Indian law name and section, or none>",
              "plain_english": "<what this clause means in simple terms>",
              "suggestion": "<improved version of the clause, or no change needed>"
            }

            Clauses:
            """);
        for (Clause c : batch) {
            String text = c.getClauseText();
            if (text != null && text.length() > 1500) {
                text = text.substring(0, 1500) + "...";
            }
            sb.append("\n---\nclause_number: ").append(c.getClauseNumber())
              .append("\ntext: ").append(text).append('\n');
        }
        return sb.toString();
    }

    // Structured-output schema so Gemini is constrained to return exactly the
    // shape we need (no markdown fences, no missing fields to guard against).
    private Map<String, Object> buildBatchResponseSchema() {
        Map<String, Object> clauseTypeEnum = Map.of(
                "type", "STRING",
                "enum", VALID_CLAUSE_TYPES
        );
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("clause_number", Map.of("type", "INTEGER"));
        properties.put("clause_type", clauseTypeEnum);
        properties.put("legal_risk_score", Map.of("type", "INTEGER"));
        properties.put("reg_risk_score", Map.of("type", "INTEGER"));
        properties.put("legal_risk_reason", Map.of("type", "STRING"));
        properties.put("regulation_violated", Map.of("type", "STRING"));
        properties.put("plain_english", Map.of("type", "STRING"));
        properties.put("suggestion", Map.of("type", "STRING"));

        Map<String, Object> itemSchema = new LinkedHashMap<>();
        itemSchema.put("type", "OBJECT");
        itemSchema.put("properties", properties);
        itemSchema.put("required", List.of("clause_number", "clause_type",
                "legal_risk_score", "reg_risk_score", "legal_risk_reason",
                "regulation_violated", "plain_english", "suggestion"));

        Map<String, Object> arraySchema = new LinkedHashMap<>();
        arraySchema.put("type", "ARRAY");
        arraySchema.put("items", itemSchema);
        return arraySchema;
    }

    private Map<Integer, ClauseAnalysisResult> parseBatchResponse(String rawResponse) {
        Map<Integer, ClauseAnalysisResult> out = new HashMap<>();
        if (rawResponse == null || rawResponse.isBlank()) return out;

        try {
            JsonNode arr = objectMapper.readTree(stripFences(rawResponse));
            if (!arr.isArray()) return out;

            for (JsonNode node : arr) {
                int clauseNumber = node.path("clause_number").asInt(-1);
                if (clauseNumber == -1) continue;

                String clauseType = node.path("clause_type").asText("other");
                if (!VALID_CLAUSE_TYPES.contains(clauseType)) clauseType = "other";

                ClauseAnalysisResult r = new ClauseAnalysisResult();
                r.clauseType         = clauseType;
                r.legalRiskScore     = clamp(node.path("legal_risk_score").asInt(50));
                r.regRiskScore       = clamp(node.path("reg_risk_score").asInt(50));
                r.legalRiskReason    = node.path("legal_risk_reason").asText("Unable to analyze");
                r.regulationViolated = node.path("regulation_violated").asText("none");
                r.plainEnglish       = node.path("plain_english").asText("Unable to parse");
                r.suggestion         = node.path("suggestion").asText("Manual review recommended");
                out.put(clauseNumber, r);
            }
        } catch (Exception e) {
            System.err.println("[LLMService] parseBatchResponse failed: " + e.getMessage()
                    + " | raw: " + rawResponse);
        }
        return out;
    }

    // Kept for backward-compat / single-clause use cases (e.g. re-analyzing
    // one clause after a manual edit). Internally just a batch of size 1.
    public ClauseAnalysisResult analyzeClause(Clause clause) {
        if (clause == null || clause.getClauseText() == null || clause.getClauseText().isBlank()) {
            return ClauseAnalysisResult.fallback("Empty clause text");
        }
        Map<Integer, ClauseAnalysisResult> result = analyzeClausesBatch(List.of(clause));
        return result.getOrDefault(clause.getClauseNumber(),
                ClauseAnalysisResult.fallback("No result returned"));
    }

    // ─────────────────────────────────────────────
    // 2. OBLIGATION EXTRACTION — already a single call for the whole contract
    // ─────────────────────────────────────────────

    public String extractObligations(String contractText) {
        if (contractText == null || contractText.isBlank()) {
            return "[]";
        }

        if (contractText.length() > 4000) {
            contractText = contractText.substring(0, 4000) + "...";
        }

        String prompt = """
            Extract all obligations from this Indian contract.
            Respond ONLY with a valid JSON array.

            Required JSON format:
            [
              {
                "party": "<who is obligated>",
                "action": "<what they must do>",
                "deadline": "<when — exact date or relative like '30 days from signing'>",
                "priority": "<low, medium, or high>"
              }
            ]

            Contract text:
            """ + contractText;

        String raw = callGemini(prompt, null);
        String extracted = extractJsonFromResponse(raw, '[', ']');
        return (extracted != null) ? extracted : "[]";
    }

    // ─────────────────────────────────────────────
    // 3. CHAT
    // ─────────────────────────────────────────────

    public String chat(String contractText, String userQuestion) {
        if (userQuestion == null || userQuestion.isBlank()) {
            return "Please ask a question about the contract.";
        }

        if (contractText != null && contractText.length() > 5000) {
            contractText = contractText.substring(0, 5000) + "...";
        }

        String prompt = """
            The user has uploaded an Indian contract. Answer their question based on the contract text below.
            Be concise, clear, and cite specific clauses or Indian law sections where relevant.

            Contract:
            """ + contractText + """

            User question: """ + userQuestion;

        String response = callGemini(prompt, null);
        if (response == null || response.isBlank()) {
            return "Sorry, I could not process your question. Please try again.";
        }
        return response;
    }

    // ─────────────────────────────────────────────
    // INTERNAL — HTTP call to Gemini generateContent
    // ─────────────────────────────────────────────

    private synchronized void throttle() {
        long now = System.currentTimeMillis();
        long wait = minIntervalMs - (now - lastCallAt);
        if (wait > 0) {
            try {
                Thread.sleep(wait);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
        lastCallAt = System.currentTimeMillis();
    }

    /**
     * @param responseSchema optional Gemini structured-output schema (see
     *                        buildBatchResponseSchema). Pass null for free-form
     *                        JSON-ish text (still asked for JSON in the prompt).
     */
    private String callGemini(String userPrompt, Map<String, Object> responseSchema) {
        try {
            throttle();

            Map<String, Object> generationConfig = new LinkedHashMap<>();
            generationConfig.put("temperature", 0.2);
            generationConfig.put("maxOutputTokens", 4000);
            generationConfig.put("responseMimeType", "application/json");
            if (responseSchema != null) {
                generationConfig.put("responseSchema", responseSchema);
            }

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("system_instruction", Map.of(
                    "parts", List.of(Map.of("text", SYSTEM_PROMPT))));
            body.put("contents", List.of(
                    Map.of("role", "user", "parts", List.of(Map.of("text", userPrompt)))));
            body.put("generationConfig", generationConfig);

            String requestBody = objectMapper.writeValueAsString(body);
            String url = String.format(GEMINI_URL_TEMPLATE, model, geminiApiKey);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 429) {
                System.err.println("[LLMService] Gemini rate limit hit, backing off and retrying once");
                try { Thread.sleep(4000); } catch (InterruptedException ignored) {}
                return callGeminiRetryOnce(url, requestBody);
            }

            if (response.statusCode() != 200) {
                System.err.println("[LLMService] Gemini HTTP error: "
                        + response.statusCode() + " | " + response.body());
                return null;
            }

            return extractTextFromCandidates(response.body());

        } catch (Exception e) {
            System.err.println("[LLMService] callGemini exception: " + e.getMessage());
            return null;
        }
    }

    private String callGeminiRetryOnce(String url, String requestBody) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("[LLMService] Gemini retry failed: "
                        + response.statusCode() + " | " + response.body());
                return null;
            }
            return extractTextFromCandidates(response.body());
        } catch (Exception e) {
            System.err.println("[LLMService] callGeminiRetryOnce exception: " + e.getMessage());
            return null;
        }
    }

    private String extractTextFromCandidates(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                System.err.println("[LLMService] No candidates in Gemini response: " + responseBody);
                return null;
            }
            JsonNode parts = candidates.get(0).path("content").path("parts");
            if (!parts.isArray() || parts.isEmpty()) return null;
            return parts.get(0).path("text").asText(null);
        } catch (Exception e) {
            System.err.println("[LLMService] Failed to parse Gemini response: " + e.getMessage());
            return null;
        }
    }

    // ─────────────────────────────────────────────
    // INTERNAL — helpers
    // ─────────────────────────────────────────────

    private String stripFences(String raw) {
        return raw.replaceAll("(?s)```json\\s*", "")
                   .replaceAll("(?s)```\\s*", "")
                   .trim();
    }

    private String extractJsonFromResponse(String raw, char openChar, char closeChar) {
        if (raw == null || raw.isBlank()) return null;
        String cleaned = stripFences(raw);
        int start = cleaned.indexOf(openChar);
        int end   = cleaned.lastIndexOf(closeChar);
        if (start == -1 || end == -1 || end <= start) return null;
        return cleaned.substring(start, end + 1);
    }

    private <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> out = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            out.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return out;
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    // ─────────────────────────────────────────────
    // RESULT HOLDER
    // ─────────────────────────────────────────────

    public static class ClauseAnalysisResult {
        public String clauseType;
        public int    legalRiskScore;
        public int    regRiskScore;
        public String legalRiskReason;
        public String regulationViolated;
        public String plainEnglish;
        public String suggestion;

        public static ClauseAnalysisResult fallback(String reason) {
            ClauseAnalysisResult r = new ClauseAnalysisResult();
            r.clauseType         = "other";
            r.legalRiskScore     = 50;
            r.regRiskScore       = 50;
            r.legalRiskReason    = reason;
            r.regulationViolated = "none";
            r.plainEnglish       = "Analysis unavailable";
            r.suggestion         = "Manual review recommended";
            return r;
        }
    }
}
