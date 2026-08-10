package com.contactai.indian_contact_ai.service;

import com.contactai.indian_contact_ai.model.Clause;
import com.contactai.indian_contact_ai.model.Contract;
import com.contactai.indian_contact_ai.repository.ClauseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ClauseSegmenterService {

    @Autowired
    private ClauseRepository clauseRepository;

    private static final int MIN_LENGTH = 80;
    private static final int MAX_LENGTH = 1500;

    // ─────────────────────────────────────────────
    // PUBLIC — called from ContractController
    // ─────────────────────────────────────────────

    public List<Clause> segmentAndSave(Contract contract, String rawText) {
        List<String> segments = segmentText(rawText);
        List<Clause> clauses = new ArrayList<>();

        int clauseNumber = 1;
        for (String segment : segments) {
            String cleaned = segment.trim();

            if (cleaned.length() < MIN_LENGTH) continue;

            if (cleaned.length() > MAX_LENGTH) {
                cleaned = cleaned.substring(0, MAX_LENGTH);
            }

            Clause clause = new Clause();
            clause.setContract(contract);
            clause.setClauseText(cleaned);
            clause.setClauseNumber(clauseNumber++);
            clauseRepository.save(clause);
            clauses.add(clause);
        }

        System.out.println("[Segmenter] Total clauses saved: " + clauses.size());
        return clauses;
    }

    // ─────────────────────────────────────────────
    // PRIVATE — segmentation logic
    // ─────────────────────────────────────────────

    private List<String> segmentText(String text) {

        // Strategy 1 — numbered sections: "1.", "1.1", "1.1.1", "SECTION 1", "Article 1"
        // FIX: use \\s* before ^ to handle leading whitespace, and MULTILINE + case-insensitive
        List<String> result = tryStrategy1(text);
        if (result.size() > 4) {
            System.out.println("[Segmenter] Strategy 1 (numbered) produced " + result.size() + " segments");
            return result;
        }

        // Strategy 2 — ALL CAPS headings on their own line (e.g. "CONFIDENTIALITY", "PAYMENT TERMS")
        result = tryStrategy2(text);
        if (result.size() > 4) {
            System.out.println("[Segmenter] Strategy 2 (ALL CAPS headings) produced " + result.size() + " segments");
            return result;
        }

        // Strategy 3 — sentence-boundary split for dense contracts with no clear structure
        result = tryStrategy3(text);
        if (result.size() > 4) {
            System.out.println("[Segmenter] Strategy 3 (sentence blocks) produced " + result.size() + " segments");
            return result;
        }

        // Strategy 4 — fallback: paragraph split on any blank line
        result = tryStrategy4(text);
        System.out.println("[Segmenter] Strategy 4 (paragraphs) produced " + result.size() + " segments");
        return result;
    }

    // ── Strategy 1: numbered sections ────────────────────────────────────────
    // Matches: "1.", "2.1", "1.1.1", "Section 3", "Article 4", "SECTION 3"
    // Key fix: (?i) for case-insensitive, \\s* to handle indented text
    private List<String> tryStrategy1(String text) {
        // Split just before a numbered section start
        String[] parts = text.split(
                "(?m)(?=^\\s*(\\d{1,2}\\.\\d{0,2}\\s+[A-Z]|\\d{1,2}\\.\\s+[A-Z]|(?i)section\\s+\\d+|(?i)article\\s+\\d+))"
        );
        List<String> out = new ArrayList<>();
        for (String p : parts) out.add(p);
        return out;
    }

    // ── Strategy 2: ALL CAPS headings ────────────────────────────────────────
    // Matches lines that are all uppercase words like "CONFIDENTIALITY" or "PAYMENT TERMS"
    // Key fix: trim each line before checking, use lookahead split
    private List<String> tryStrategy2(String text) {
        // Normalize: replace sequences of 4+ newlines with double newline
        String normalized = text.replaceAll("\\n{3,}", "\n\n");

        String[] parts = normalized.split(
                "(?m)(?=^\\s*[A-Z][A-Z\\s\\.]{3,}\\s*$)"
        );
        List<String> out = new ArrayList<>();
        for (String p : parts) out.add(p);
        return out;
    }

    // ── Strategy 3: fixed-size sentence blocks ───────────────────────────────
    // For contracts with no structural markers — group sentences into ~500 char blocks
    private List<String> tryStrategy3(String text) {
        // Split on sentence boundaries (period/semicolon followed by space + capital)
        String[] sentences = text.split("(?<=[.;])\\s+(?=[A-Z])");

        List<String> out = new ArrayList<>();
        StringBuilder block = new StringBuilder();

        for (String sentence : sentences) {
            block.append(sentence).append(" ");
            if (block.length() >= 500) {
                out.add(block.toString().trim());
                block.setLength(0);
            }
        }
        if (block.length() > MIN_LENGTH) {
            out.add(block.toString().trim());
        }
        return out;
    }

    // ── Strategy 4: paragraph fallback ───────────────────────────────────────
    private List<String> tryStrategy4(String text) {
        String[] parts = text.split("\\n\\s*\\n");
        List<String> out = new ArrayList<>();
        for (String p : parts) out.add(p);
        return out;
    }
}