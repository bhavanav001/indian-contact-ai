//
//package com.contactai.indian_contact_ai.service;
//
//import com.contactai.indian_contact_ai.model.ComplianceFlag;
//import com.contactai.indian_contact_ai.model.Clause;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//public class RuleEngineService {
//
//    // ---------------------------------------------------------------
//    // MAIN ENTRY POINT — call this for every clause
//    // Returns a list of violations found (empty list = no violations)
//    // ---------------------------------------------------------------
//    public List<ComplianceFlag> checkClause(Clause clause) {
//        List<ComplianceFlag> flags = new ArrayList<>();
//        String text = clause.getClauseText().toLowerCase();
//
//        flags.addAll(checkDPDP(clause, text));
//        flags.addAll(checkGST(clause, text));
//        flags.addAll(checkICA(clause, text));
//
//        return flags;
//    }
//
//    // ---------------------------------------------------------------
//    // DPDP ACT 2023 — Digital Personal Data Protection
//    // ---------------------------------------------------------------
//    private List<ComplianceFlag> checkDPDP(Clause clause, String text) {
//        List<ComplianceFlag> flags = new ArrayList<>();
//
//        // Rule 1: Data sharing without consent mechanism (Section 6)
//        if (containsDataSharing(text) && !containsConsent(text)) {
//            ComplianceFlag flag = new ComplianceFlag();
//            flag.setClause(clause);
//            flag.setLawName("DPDP Act 2023");
//            flag.setSectionNum("Section 6");
//            flag.setViolation("Data sharing clause lacks explicit consent mechanism. " +
//                    "Under Section 6, personal data processing requires free, specific, informed, and unconditional consent.");
//            flag.setSeverity(ComplianceFlag.Severity.CRITICAL);
//            flags.add(flag);
//        }
//
//        // Rule 2: Irrevocable consent (Section 7)
//        if (containsConsent(text) && containsIrrevocable(text)) {
//            ComplianceFlag flag = new ComplianceFlag();
//            flag.setClause(clause);
//            flag.setLawName("DPDP Act 2023");
//            flag.setSectionNum("Section 7");
//            flag.setViolation("Consent cannot be irrevocable under Indian law. " +
//                    "Section 7 gives the data principal the right to withdraw consent at any time.");
//            flag.setSeverity(ComplianceFlag.Severity.HIGH);
//            flags.add(flag);
//        }
//
//        // Rule 3: No data retention / deletion timeline (Section 8(7))
//        if (containsDataStorage(text) && !containsRetentionPeriod(text)) {
//            ComplianceFlag flag = new ComplianceFlag();
//            flag.setClause(clause);
//            flag.setLawName("DPDP Act 2023");
//            flag.setSectionNum("Section 8(7)");
//            flag.setViolation("Data retention period not defined. " +
//                    "Section 8(7) requires data to be erased once the purpose is served. " +
//                    "Specify a retention timeline or deletion trigger.");
//            flag.setSeverity(ComplianceFlag.Severity.HIGH);
//            flags.add(flag);
//        }
//
//        // Rule 4: Cross-border data transfer (Section 16)
//        if (containsCrossBorder(text)) {
//            ComplianceFlag flag = new ComplianceFlag();
//            flag.setClause(clause);
//            flag.setLawName("DPDP Act 2023");
//            flag.setSectionNum("Section 16");
//            flag.setViolation("Cross-border data transfer detected. " +
//                    "Section 16 restricts transfer of personal data to countries approved by the Government of India. " +
//                    "Verify the destination country is on the whitelist.");
//            flag.setSeverity(ComplianceFlag.Severity.HIGH);
//            flags.add(flag);
//        }
//
//        // Rule 5: No breach notification obligation (Section 17)
//        if (containsDataSharing(text) || containsDataStorage(text)) {
//            if (!containsBreachNotification(text)) {
//                ComplianceFlag flag = new ComplianceFlag();
//                flag.setClause(clause);
//                flag.setLawName("DPDP Act 2023");
//                flag.setSectionNum("Section 17");
//                flag.setViolation("No data breach notification obligation defined. " +
//                        "Section 17 requires the data fiduciary to notify the Board and affected persons in case of a breach.");
//                flag.setSeverity(ComplianceFlag.Severity.HIGH);
//                flags.add(flag);
//            }
//        }
//
//        // Rule 6: Children's data without age verification (Section 9)
//        if (containsMinorData(text) && !containsAgeVerification(text)) {
//            ComplianceFlag flag = new ComplianceFlag();
//            flag.setClause(clause);
//            flag.setLawName("DPDP Act 2023");
//            flag.setSectionNum("Section 9");
//            flag.setViolation("Clause may involve minors' data but no age verification or parental consent mechanism is defined. " +
//                    "Section 9 requires verifiable parental consent before processing children's personal data.");
//            flag.setSeverity(ComplianceFlag.Severity.CRITICAL);
//            flags.add(flag);
//        }
//
//        return flags;
//    }
//
//    // ---------------------------------------------------------------
//    // GST ACT 2017
//    // ---------------------------------------------------------------
//    private List<ComplianceFlag> checkGST(Clause clause, String text) {
//        List<ComplianceFlag> flags = new ArrayList<>();
//
//        // Rule 1: Payment clause with no GST mention (Section 9)
//        if (containsPayment(text) && !containsGST(text)) {
//            ComplianceFlag flag = new ComplianceFlag();
//            flag.setClause(clause);
//            flag.setLawName("GST Act 2017");
//            flag.setSectionNum("Section 9");
//            flag.setViolation("Payment clause does not mention GST applicability. " +
//                    "Section 9 makes GST applicable on all commercial supplies. " +
//                    "Specify whether quoted price is inclusive or exclusive of GST and who bears the tax.");
//            flag.setSeverity(ComplianceFlag.Severity.HIGH);
//            flags.add(flag);
//        }
//
//        // Rule 2: Invoice timeline missing (Rule 47)
//        if (containsInvoice(text) && !containsInvoiceTimeline(text)) {
//            ComplianceFlag flag = new ComplianceFlag();
//            flag.setClause(clause);
//            flag.setLawName("GST Act 2017");
//            flag.setSectionNum("Rule 47");
//            flag.setViolation("Invoice timeline not specified. " +
//                    "GST Rule 47 requires invoices to be issued within 30 days of supply (45 days for banking/insurance). " +
//                    "Add a specific invoice issuance timeline to this clause.");
//            flag.setSeverity(ComplianceFlag.Severity.MEDIUM);
//            flags.add(flag);
//        }
//
//        // Rule 3: Reverse Charge Mechanism missing (Section 2(98))
//        if (containsRCMServices(text) && !containsRCM(text)) {
//            ComplianceFlag flag = new ComplianceFlag();
//            flag.setClause(clause);
//            flag.setLawName("GST Act 2017");
//            flag.setSectionNum("Section 2(98)");
//            flag.setViolation("Clause involves services subject to Reverse Charge Mechanism (RCM) " +
//                    "but no RCM clause is present. " +
//                    "Under RCM, the buyer is liable to pay GST directly to the government for specified services " +
//                    "(legal, transport, import). Add an explicit RCM clause.");
//            flag.setSeverity(ComplianceFlag.Severity.HIGH);
//            flags.add(flag);
//        }
//
//        // Rule 4: No GSTIN mentioned in payment clause (Section 31)
//        if (containsPayment(text) && !containsGSTIN(text)) {
//            ComplianceFlag flag = new ComplianceFlag();
//            flag.setClause(clause);
//            flag.setLawName("GST Act 2017");
//            flag.setSectionNum("Section 31");
//            flag.setViolation("GSTIN (GST registration number) not referenced in payment/invoice clause. " +
//                    "Section 31 requires a valid tax invoice with the supplier's GSTIN for Input Tax Credit eligibility.");
//            flag.setSeverity(ComplianceFlag.Severity.LOW);
//            flags.add(flag);
//        }
//
//        return flags;
//    }
//
//    // ---------------------------------------------------------------
//    // INDIAN CONTRACT ACT 1872 — Void / Voidable Clauses
//    // ---------------------------------------------------------------
//    private List<ComplianceFlag> checkICA(Clause clause, String text) {
//        List<ComplianceFlag> flags = new ArrayList<>();
//
//        // Rule 1: Restraint of trade / non-compete (Section 27)
//        if (containsRestraintOfTrade(text)) {
//            ComplianceFlag flag = new ComplianceFlag();
//            flag.setClause(clause);
//            flag.setLawName("Indian Contract Act 1872");
//            flag.setSectionNum("Section 27");
//            flag.setViolation("Non-compete or restraint of trade clause detected. " +
//                    "Section 27 makes agreements in restraint of trade void. " +
//                    "Non-competes must be limited in time and geography to have any enforceability. " +
//                    "Unlimited or blanket non-competes are void under Indian law.");
//            flag.setSeverity(ComplianceFlag.Severity.HIGH);
//            flags.add(flag);
//        }
//
//        // Rule 2: Excessive / unlimited penalty (Section 74)
//        if (containsPenalty(text)) {
//            ComplianceFlag flag = new ComplianceFlag();
//            flag.setClause(clause);
//            flag.setLawName("Indian Contract Act 1872");
//            flag.setSectionNum("Section 74");
//            flag.setViolation("Penalty or liquidated damages clause detected. " +
//                    "Section 74 allows Indian courts to reduce penalties that are unreasonable or unconscionable. " +
//                    "Ensure the penalty amount is proportionate to the actual loss. " +
//                    "Cap penalties at a reasonable amount.");
//            flag.setSeverity(ComplianceFlag.Severity.MEDIUM);
//            flags.add(flag);
//        }
//
//        // Rule 3: Restricting legal proceedings / ouster of Indian courts (Section 28)
//        if (containsJurisdictionOuster(text)) {
//            ComplianceFlag flag = new ComplianceFlag();
//            flag.setClause(clause);
//            flag.setLawName("Indian Contract Act 1872");
//            flag.setSectionNum("Section 28");
//            flag.setViolation("Clause restricts or ousts Indian court jurisdiction. " +
//                    "Section 28 makes agreements that restrict legal proceedings void. " +
//                    "You cannot contract out of Indian courts entirely. " +
//                    "Exclusive arbitration outside India without Indian law governing is risky.");
//            flag.setSeverity(ComplianceFlag.Severity.HIGH);
//            flags.add(flag);
//        }
//
//        // Rule 4: Agreement against public policy (Section 23)
//        if (containsPublicPolicyViolation(text)) {
//            ComplianceFlag flag = new ComplianceFlag();
//            flag.setClause(clause);
//            flag.setLawName("Indian Contract Act 1872");
//            flag.setSectionNum("Section 23");
//            flag.setViolation("Clause may be against public policy and therefore void under Section 23. " +
//                    "Agreements to suppress complaints, waive FIR rights, or obstruct justice are unlawful. " +
//                    "Review and remove any such language.");
//            flag.setSeverity(ComplianceFlag.Severity.CRITICAL);
//            flags.add(flag);
//        }
//
//        // Rule 5: Zero or no consideration (Section 25)
//        if (containsNoConsideration(text)) {
//            ComplianceFlag flag = new ComplianceFlag();
//            flag.setClause(clause);
//            flag.setLawName("Indian Contract Act 1872");
//            flag.setSectionNum("Section 25");
//            flag.setViolation("Clause appears to have no or nominal consideration. " +
//                    "Section 25 makes agreements without consideration void (with limited exceptions). " +
//                    "Ensure there is valid and lawful consideration for all obligations.");
//            flag.setSeverity(ComplianceFlag.Severity.MEDIUM);
//            flags.add(flag);
//        }
//
//        // Rule 6: Force majeure — COVID / pandemic not listed (Section 56)
//        if (containsForceMajeure(text) && !containsPandemic(text)) {
//            ComplianceFlag flag = new ComplianceFlag();
//            flag.setClause(clause);
//            flag.setLawName("Indian Contract Act 1872");
//            flag.setSectionNum("Section 56");
//            flag.setViolation("Force majeure clause does not explicitly include pandemic or epidemic events. " +
//                    "Post-COVID, Indian courts have scrutinized force majeure clauses closely. " +
//                    "Explicitly list pandemic, epidemic, government lockdown as force majeure events.");
//            flag.setSeverity(ComplianceFlag.Severity.MEDIUM);
//            flags.add(flag);
//        }
//
//        return flags;
//    }
//
//    // ================================================================
//    // KEYWORD HELPER METHODS
//    // ================================================================
//
//    // --- DPDP helpers ---
//    private boolean containsDataSharing(String text) {
//        return text.contains("share data") || text.contains("data sharing") ||
//                text.contains("transfer data") || text.contains("personal data") ||
//                text.contains("disclose data") || text.contains("third party data") ||
//                text.contains("data transfer") || text.contains("transmit data");
//    }
//
//    private boolean containsConsent(String text) {
//        return text.contains("consent") || text.contains("agree") ||
//                text.contains("permission") || text.contains("authorize") ||
//                text.contains("opt-in") || text.contains("opt in");
//    }
//
//    private boolean containsIrrevocable(String text) {
//        return text.contains("irrevocable") || text.contains("cannot be withdrawn") ||
//                text.contains("non-revocable") || text.contains("permanent consent");
//    }
//
//    private boolean containsDataStorage(String text) {
//        return text.contains("store data") || text.contains("data storage") ||
//                text.contains("retain data") || text.contains("data retention") ||
//                text.contains("keep data") || text.contains("maintain records") ||
//                text.contains("personal information");
//    }
//
//    private boolean containsRetentionPeriod(String text) {
//        return text.contains("retention period") || text.contains("deleted after") ||
//                text.contains("erased after") || text.contains("purged after") ||
//                text.contains("retain for") || text.contains("stored for") ||
//                text.contains("days of") || text.contains("months of") ||
//                text.contains("years of") || text.contains("upon termination");
//    }
//
//    private boolean containsCrossBorder(String text) {
//        return text.contains("outside india") || text.contains("international transfer") ||
//                text.contains("cross-border") || text.contains("cross border") ||
//                text.contains("foreign country") || text.contains("overseas") ||
//                text.contains("outside the country") || text.contains("abroad");
//    }
//
//    private boolean containsBreachNotification(String text) {
//        return text.contains("breach notification") || text.contains("notify") ||
//                text.contains("data breach") || text.contains("security incident") ||
//                text.contains("inform in case") || text.contains("report breach");
//    }
//
//    private boolean containsMinorData(String text) {
//        return text.contains("minor") || text.contains("child") ||
//                text.contains("children") || text.contains("under 18") ||
//                text.contains("underage") || text.contains("below 18");
//    }
//
//    private boolean containsAgeVerification(String text) {
//        return text.contains("age verification") || text.contains("parental consent") ||
//                text.contains("guardian consent") || text.contains("verify age") ||
//                text.contains("date of birth") || text.contains("18 years");
//    }
//
//    // --- GST helpers ---
//    private boolean containsPayment(String text) {
//        return text.contains("payment") || text.contains("invoice") ||
//                text.contains("fee") || text.contains("charge") ||
//                text.contains("price") || text.contains("amount") ||
//                text.contains("consideration") || text.contains("pay");
//    }
//
//    private boolean containsGST(String text) {
//        return text.contains("gst") || text.contains("goods and services tax") ||
//                text.contains("tax") || text.contains("igst") ||
//                text.contains("cgst") || text.contains("sgst");
//    }
//
//    private boolean containsInvoice(String text) {
//        return text.contains("invoice") || text.contains("bill") ||
//                text.contains("tax invoice") || text.contains("proforma");
//    }
//
//    private boolean containsInvoiceTimeline(String text) {
//        return text.contains("within") || text.contains("days of") ||
//                text.contains("30 days") || text.contains("45 days") ||
//                text.contains("invoice date") || text.contains("billing cycle");
//    }
//
//    private boolean containsRCMServices(String text) {
//        return text.contains("legal service") || text.contains("advocate") ||
//                text.contains("transport") || text.contains("import") ||
//                text.contains("freight") || text.contains("insurance") ||
//                text.contains("gta") || text.contains("goods transport");
//    }
//
//    private boolean containsRCM(String text) {
//        return text.contains("reverse charge") || text.contains("rcm") ||
//                text.contains("recipient shall pay") || text.contains("buyer shall pay tax");
//    }
//
//    private boolean containsGSTIN(String text) {
//        return text.contains("gstin") || text.contains("gst number") ||
//                text.contains("gst registration") || text.contains("gst no");
//    }
//
//    // --- ICA helpers ---
//    private boolean containsRestraintOfTrade(String text) {
//        return text.contains("non-compete") || text.contains("non compete") ||
//                text.contains("not compete") || text.contains("restraint of trade") ||
//                text.contains("not engage in") || text.contains("shall not work") ||
//                text.contains("shall not be employed") || text.contains("not carry on business");
//    }
//
//    private boolean containsPenalty(String text) {
//        return text.contains("penalty") || text.contains("liquidated damages") ||
//                text.contains("forfeit") || text.contains("forfeiture") ||
//                text.contains("deduct") || text.contains("fine") ||
//                text.contains("damages of") || text.contains("pay a sum");
//    }
//
//    private boolean containsJurisdictionOuster(String text) {
//        return text.contains("no legal action") || text.contains("waive right to sue") ||
//                text.contains("exclusive jurisdiction outside") ||
//                text.contains("courts of") && text.contains("only") ||
//                text.contains("arbitration outside india") ||
//                text.contains("foreign arbitration") || text.contains("shall not approach court");
//    }
//
//    private boolean containsPublicPolicyViolation(String text) {
//        return text.contains("suppress") || text.contains("not file") ||
//                text.contains("waive fir") || text.contains("not report") ||
//                text.contains("obstruct") || text.contains("not disclose to authorities") ||
//                text.contains("silence") && text.contains("complaint");
//    }
//
//    private boolean containsNoConsideration(String text) {
//        return text.contains("free of charge") || text.contains("no payment") ||
//                text.contains("gratuitous") || text.contains("nominal consideration") ||
//                text.contains("one rupee") || text.contains("₹1") ||
//                text.contains("without any fee");
//    }
//
//    private boolean containsForceMajeure(String text) {
//        return text.contains("force majeure") || text.contains("act of god") ||
//                text.contains("beyond control") || text.contains("unforeseen circumstances") ||
//                text.contains("circumstances beyond");
//    }
//
//    private boolean containsPandemic(String text) {
//        return text.contains("pandemic") || text.contains("epidemic") ||
//                text.contains("covid") || text.contains("lockdown") ||
//                text.contains("quarantine") || text.contains("public health emergency");
//    }
//}
package com.contactai.indian_contact_ai.service;

import com.contactai.indian_contact_ai.model.ComplianceFlag;
import com.contactai.indian_contact_ai.model.Clause;
import com.contactai.indian_contact_ai.model.IrcoOntology;
import com.contactai.indian_contact_ai.repository.IrcoOntologyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RuleEngineService {

    @Autowired
    private IrcoOntologyRepository ircoOntologyRepository;

    // ---------------------------------------------------------------
    // ONTOLOGY LOOKUP HELPER
    // Pulls lawName / sectionNum / violation text / severity from the
    // irco_ontology table when a matching row exists. If the table is
    // empty or a row is missing, falls back to the hardcoded values
    // passed in, so nothing breaks if the ontology isn't seeded yet.
    // ---------------------------------------------------------------
    private void applyOntology(ComplianceFlag flag, String clauseType, String sectionNum,
                               String fallbackLawName, String fallbackViolation,
                               ComplianceFlag.Severity fallbackSeverity) {

        List<IrcoOntology> matches =
                ircoOntologyRepository.findByClauseTypeAndSectionNum(clauseType, sectionNum);

        if (!matches.isEmpty()) {
            IrcoOntology entry = matches.get(0);
            flag.setLawName(entry.getLawName());
            flag.setSectionNum(entry.getSectionNum());
            flag.setViolation(entry.getCheckCriteria() + " — " + fallbackViolation);
            flag.setSeverity(entry.getSeverityDefault() != null
                    ? entry.getSeverityDefault() : fallbackSeverity);
        } else {
            flag.setLawName(fallbackLawName);
            flag.setSectionNum(sectionNum);
            flag.setViolation(fallbackViolation);
            flag.setSeverity(fallbackSeverity);
        }
    }

    // ---------------------------------------------------------------
    // MAIN ENTRY POINT — call this for every clause
    // Returns a list of violations found (empty list = no violations)
    // ---------------------------------------------------------------
    public List<ComplianceFlag> checkClause(Clause clause) {
        List<ComplianceFlag> flags = new ArrayList<>();
        String text = clause.getClauseText().toLowerCase();

        flags.addAll(checkDPDP(clause, text));
        flags.addAll(checkGST(clause, text));
        flags.addAll(checkICA(clause, text));

        return flags;
    }

    // ---------------------------------------------------------------
    // DPDP ACT 2023 — Digital Personal Data Protection
    // ---------------------------------------------------------------
    private List<ComplianceFlag> checkDPDP(Clause clause, String text) {
        List<ComplianceFlag> flags = new ArrayList<>();

        // Rule 1: Data sharing without consent mechanism (Section 6)
        if (containsDataSharing(text) && !containsConsent(text)) {
            ComplianceFlag flag = new ComplianceFlag();
            flag.setClause(clause);
            applyOntology(flag, "data_sharing", "Section 6",
                    "DPDP Act 2023",
                    "Data sharing clause lacks explicit consent mechanism. " +
                            "Under Section 6, personal data processing requires free, specific, informed, and unconditional consent.",
                    ComplianceFlag.Severity.CRITICAL);
            flags.add(flag);
        }

        // Rule 2: Irrevocable consent (Section 7)
        if (containsConsent(text) && containsIrrevocable(text)) {
            ComplianceFlag flag = new ComplianceFlag();
            flag.setClause(clause);
            applyOntology(flag, "data_sharing", "Section 7",
                    "DPDP Act 2023",
                    "Consent cannot be irrevocable under Indian law. " +
                            "Section 7 gives the data principal the right to withdraw consent at any time.",
                    ComplianceFlag.Severity.HIGH);
            flags.add(flag);
        }

        // Rule 3: No data retention / deletion timeline (Section 8(7))
        if (containsDataStorage(text) && !containsRetentionPeriod(text)) {
            ComplianceFlag flag = new ComplianceFlag();
            flag.setClause(clause);
            applyOntology(flag, "data_storage", "Section 8(7)",
                    "DPDP Act 2023",
                    "Data retention period not defined. " +
                            "Section 8(7) requires data to be erased once the purpose is served. " +
                            "Specify a retention timeline or deletion trigger.",
                    ComplianceFlag.Severity.HIGH);
            flags.add(flag);
        }

        // Rule 4: Cross-border data transfer (Section 16)
        if (containsCrossBorder(text)) {
            ComplianceFlag flag = new ComplianceFlag();
            flag.setClause(clause);
            applyOntology(flag, "data_transfer", "Section 16",
                    "DPDP Act 2023",
                    "Cross-border data transfer detected. " +
                            "Section 16 restricts transfer of personal data to countries approved by the Government of India. " +
                            "Verify the destination country is on the whitelist.",
                    ComplianceFlag.Severity.HIGH);
            flags.add(flag);
        }

        // Rule 5: No breach notification obligation (Section 17)
        if (containsDataSharing(text) || containsDataStorage(text)) {
            if (!containsBreachNotification(text)) {
                ComplianceFlag flag = new ComplianceFlag();
                flag.setClause(clause);
                applyOntology(flag, "data_breach", "Section 17",
                        "DPDP Act 2023",
                        "No data breach notification obligation defined. " +
                                "Section 17 requires the data fiduciary to notify the Board and affected persons in case of a breach.",
                        ComplianceFlag.Severity.HIGH);
                flags.add(flag);
            }
        }

        // Rule 6: Children's data without age verification (Section 9)
        if (containsMinorData(text) && !containsAgeVerification(text)) {
            ComplianceFlag flag = new ComplianceFlag();
            flag.setClause(clause);
            applyOntology(flag, "minors_data", "Section 9",
                    "DPDP Act 2023",
                    "Clause may involve minors' data but no age verification or parental consent mechanism is defined. " +
                            "Section 9 requires verifiable parental consent before processing children's personal data.",
                    ComplianceFlag.Severity.CRITICAL);
            flags.add(flag);
        }

        return flags;
    }

    // ---------------------------------------------------------------
    // GST ACT 2017
    // ---------------------------------------------------------------
    private List<ComplianceFlag> checkGST(Clause clause, String text) {
        List<ComplianceFlag> flags = new ArrayList<>();

        // Rule 1: Payment clause with no GST mention (Section 9)
        if (containsPayment(text) && !containsGST(text)) {
            ComplianceFlag flag = new ComplianceFlag();
            flag.setClause(clause);
            applyOntology(flag, "payment", "Section 9",
                    "GST Act 2017",
                    "Payment clause does not mention GST applicability. " +
                            "Section 9 makes GST applicable on all commercial supplies. " +
                            "Specify whether quoted price is inclusive or exclusive of GST and who bears the tax.",
                    ComplianceFlag.Severity.HIGH);
            flags.add(flag);
        }

        // Rule 2: Invoice timeline missing (Rule 47)
        if (containsInvoice(text) && !containsInvoiceTimeline(text)) {
            ComplianceFlag flag = new ComplianceFlag();
            flag.setClause(clause);
            applyOntology(flag, "invoice", "Rule 47",
                    "GST Act 2017",
                    "Invoice timeline not specified. " +
                            "GST Rule 47 requires invoices to be issued within 30 days of supply (45 days for banking/insurance). " +
                            "Add a specific invoice issuance timeline to this clause.",
                    ComplianceFlag.Severity.MEDIUM);
            flags.add(flag);
        }

        // Rule 3: Reverse Charge Mechanism missing (Section 2(98))
        if (containsRCMServices(text) && !containsRCM(text)) {
            ComplianceFlag flag = new ComplianceFlag();
            flag.setClause(clause);
            applyOntology(flag, "payment", "Section 2(98)",
                    "GST Act 2017",
                    "Clause involves services subject to Reverse Charge Mechanism (RCM) " +
                            "but no RCM clause is present. " +
                            "Under RCM, the buyer is liable to pay GST directly to the government for specified services " +
                            "(legal, transport, import). Add an explicit RCM clause.",
                    ComplianceFlag.Severity.HIGH);
            flags.add(flag);
        }

        // Rule 4: No GSTIN mentioned in payment clause (Section 31)
        if (containsPayment(text) && !containsGSTIN(text)) {
            ComplianceFlag flag = new ComplianceFlag();
            flag.setClause(clause);
            applyOntology(flag, "payment", "Section 31",
                    "GST Act 2017",
                    "GSTIN (GST registration number) not referenced in payment/invoice clause. " +
                            "Section 31 requires a valid tax invoice with the supplier's GSTIN for Input Tax Credit eligibility.",
                    ComplianceFlag.Severity.LOW);
            flags.add(flag);
        }

        return flags;
    }

    // ---------------------------------------------------------------
    // INDIAN CONTRACT ACT 1872 — Void / Voidable Clauses
    // ---------------------------------------------------------------
    private List<ComplianceFlag> checkICA(Clause clause, String text) {
        List<ComplianceFlag> flags = new ArrayList<>();

        // Rule 1: Restraint of trade / non-compete (Section 27)
        if (containsRestraintOfTrade(text)) {
            ComplianceFlag flag = new ComplianceFlag();
            flag.setClause(clause);
            applyOntology(flag, "confidentiality", "Section 27",
                    "Indian Contract Act 1872",
                    "Non-compete or restraint of trade clause detected. " +
                            "Section 27 makes agreements in restraint of trade void. " +
                            "Non-competes must be limited in time and geography to have any enforceability. " +
                            "Unlimited or blanket non-competes are void under Indian law.",
                    ComplianceFlag.Severity.HIGH);
            flags.add(flag);
        }

        // Rule 2: Excessive / unlimited penalty (Section 74)
        if (containsPenalty(text)) {
            ComplianceFlag flag = new ComplianceFlag();
            flag.setClause(clause);
            applyOntology(flag, "termination", "Section 74",
                    "Indian Contract Act 1872",
                    "Penalty or liquidated damages clause detected. " +
                            "Section 74 allows Indian courts to reduce penalties that are unreasonable or unconscionable. " +
                            "Ensure the penalty amount is proportionate to the actual loss. " +
                            "Cap penalties at a reasonable amount.",
                    ComplianceFlag.Severity.MEDIUM);
            flags.add(flag);
        }

        // Rule 3: Restricting legal proceedings / ouster of Indian courts (Section 28)
        if (containsJurisdictionOuster(text)) {
            ComplianceFlag flag = new ComplianceFlag();
            flag.setClause(clause);
            applyOntology(flag, "dispute", "Section 28",
                    "Indian Contract Act 1872",
                    "Clause restricts or ousts Indian court jurisdiction. " +
                            "Section 28 makes agreements that restrict legal proceedings void. " +
                            "You cannot contract out of Indian courts entirely. " +
                            "Exclusive arbitration outside India without Indian law governing is risky.",
                    ComplianceFlag.Severity.HIGH);
            flags.add(flag);
        }

        // Rule 4: Agreement against public policy (Section 23)
        if (containsPublicPolicyViolation(text)) {
            ComplianceFlag flag = new ComplianceFlag();
            flag.setClause(clause);
            applyOntology(flag, "public_policy", "Section 23",
                    "Indian Contract Act 1872",
                    "Clause may be against public policy and therefore void under Section 23. " +
                            "Agreements to suppress complaints, waive FIR rights, or obstruct justice are unlawful. " +
                            "Review and remove any such language.",
                    ComplianceFlag.Severity.CRITICAL);
            flags.add(flag);
        }

        // Rule 5: Zero or no consideration (Section 25)
        if (containsNoConsideration(text)) {
            ComplianceFlag flag = new ComplianceFlag();
            flag.setClause(clause);
            applyOntology(flag, "consideration", "Section 25",
                    "Indian Contract Act 1872",
                    "Clause appears to have no or nominal consideration. " +
                            "Section 25 makes agreements without consideration void (with limited exceptions). " +
                            "Ensure there is valid and lawful consideration for all obligations.",
                    ComplianceFlag.Severity.MEDIUM);
            flags.add(flag);
        }

        // Rule 6: Force majeure — COVID / pandemic not listed (Section 56)
        if (containsForceMajeure(text) && !containsPandemic(text)) {
            ComplianceFlag flag = new ComplianceFlag();
            flag.setClause(clause);
            applyOntology(flag, "force_majeure", "Section 56",
                    "Indian Contract Act 1872",
                    "Force majeure clause does not explicitly include pandemic or epidemic events. " +
                            "Post-COVID, Indian courts have scrutinized force majeure clauses closely. " +
                            "Explicitly list pandemic, epidemic, government lockdown as force majeure events.",
                    ComplianceFlag.Severity.MEDIUM);
            flags.add(flag);
        }

        return flags;
    }

    // ================================================================
    // KEYWORD HELPER METHODS
    // ================================================================

    // --- DPDP helpers ---
    private boolean containsDataSharing(String text) {
        return text.contains("share data") || text.contains("data sharing") ||
                text.contains("transfer data") || text.contains("personal data") ||
                text.contains("disclose data") || text.contains("third party data") ||
                text.contains("data transfer") || text.contains("transmit data");
    }

    private boolean containsConsent(String text) {
        return text.contains("consent") || text.contains("agree") ||
                text.contains("permission") || text.contains("authorize") ||
                text.contains("opt-in") || text.contains("opt in");
    }

    private boolean containsIrrevocable(String text) {
        return text.contains("irrevocable") || text.contains("cannot be withdrawn") ||
                text.contains("non-revocable") || text.contains("permanent consent");
    }

    private boolean containsDataStorage(String text) {
        return text.contains("store data") || text.contains("data storage") ||
                text.contains("retain data") || text.contains("data retention") ||
                text.contains("keep data") || text.contains("maintain records") ||
                text.contains("personal information");
    }

    private boolean containsRetentionPeriod(String text) {
        return text.contains("retention period") || text.contains("deleted after") ||
                text.contains("erased after") || text.contains("purged after") ||
                text.contains("retain for") || text.contains("stored for") ||
                text.contains("days of") || text.contains("months of") ||
                text.contains("years of") || text.contains("upon termination");
    }

    private boolean containsCrossBorder(String text) {
        return text.contains("outside india") || text.contains("international transfer") ||
                text.contains("cross-border") || text.contains("cross border") ||
                text.contains("foreign country") || text.contains("overseas") ||
                text.contains("outside the country") || text.contains("abroad");
    }

    private boolean containsBreachNotification(String text) {
        return text.contains("breach notification") || text.contains("notify") ||
                text.contains("data breach") || text.contains("security incident") ||
                text.contains("inform in case") || text.contains("report breach");
    }

    private boolean containsMinorData(String text) {
        return text.contains("minor") || text.contains("child") ||
                text.contains("children") || text.contains("under 18") ||
                text.contains("underage") || text.contains("below 18");
    }

    private boolean containsAgeVerification(String text) {
        return text.contains("age verification") || text.contains("parental consent") ||
                text.contains("guardian consent") || text.contains("verify age") ||
                text.contains("date of birth") || text.contains("18 years");
    }

    // --- GST helpers ---
//    private boolean containsPayment(String text) {
//        return text.contains("payment") || text.contains("invoice") ||
//                text.contains("fee") || text.contains("charge") ||
//                text.contains("price") || text.contains("amount") ||
//                text.contains("consideration") || text.contains("pay");
//    }
    private boolean containsPayment(String text) {
        // Strong signals: currency symbols or explicit money-related terms
        boolean hasMoneySignal = text.contains("₹") || text.contains("rs.") ||
                text.contains("rupees") || text.contains("invoice") ||
                text.contains("gst") || text.contains("fee schedule") ||
                text.contains("due date") || text.contains("amount due") ||
                text.contains("payment schedule") || text.contains("payment terms") ||
                text.contains("payment due") || text.contains("billing");

        // Weak signals: generic words that need a nearby number/currency to count,
        // not just "payment"/"fee"/"charge" appearing anywhere in an unrelated sentence
        boolean hasGenericPaymentWord = text.contains("payment") || text.contains("fee") ||
                text.contains("charge") || text.contains("consideration");
        boolean hasNumberOrCurrency = text.matches(".*\\d.*") &&
                (text.contains("₹") || text.contains("rs") || text.contains("inr") ||
                        text.contains("month") || text.contains("day") || text.contains("%"));

        return hasMoneySignal || (hasGenericPaymentWord && hasNumberOrCurrency);
    }

    private boolean containsGST(String text) {
        return text.contains("gst") || text.contains("goods and services tax") ||
                text.contains("tax") || text.contains("igst") ||
                text.contains("cgst") || text.contains("sgst");
    }

    private boolean containsInvoice(String text) {
        return text.contains("invoice") || text.contains("bill") ||
                text.contains("tax invoice") || text.contains("proforma");
    }

    private boolean containsInvoiceTimeline(String text) {
        return text.contains("within") || text.contains("days of") ||
                text.contains("30 days") || text.contains("45 days") ||
                text.contains("invoice date") || text.contains("billing cycle");
    }

    private boolean containsRCMServices(String text) {
        return text.contains("legal service") || text.contains("advocate") ||
                text.contains("transport") || text.contains("import") ||
                text.contains("freight") || text.contains("insurance") ||
                text.contains("gta") || text.contains("goods transport");
    }

    private boolean containsRCM(String text) {
        return text.contains("reverse charge") || text.contains("rcm") ||
                text.contains("recipient shall pay") || text.contains("buyer shall pay tax");
    }

    private boolean containsGSTIN(String text) {
        return text.contains("gstin") || text.contains("gst number") ||
                text.contains("gst registration") || text.contains("gst no");
    }

    // --- ICA helpers ---
    private boolean containsRestraintOfTrade(String text) {
        return text.contains("non-compete") || text.contains("non compete") ||
                text.contains("not compete") || text.contains("restraint of trade") ||
                text.contains("not engage in") || text.contains("shall not work") ||
                text.contains("shall not be employed") || text.contains("not carry on business");
    }

    private boolean containsPenalty(String text) {
        return text.contains("penalty") || text.contains("liquidated damages") ||
                text.contains("forfeit") || text.contains("forfeiture") ||
                text.contains("deduct") || text.contains("fine") ||
                text.contains("damages of") || text.contains("pay a sum");
    }

    private boolean containsJurisdictionOuster(String text) {
        return text.contains("no legal action") || text.contains("waive right to sue") ||
                text.contains("exclusive jurisdiction outside") ||
                text.contains("courts of") && text.contains("only") ||
                text.contains("arbitration outside india") ||
                text.contains("foreign arbitration") || text.contains("shall not approach court");
    }

    private boolean containsPublicPolicyViolation(String text) {
        return text.contains("suppress") || text.contains("not file") ||
                text.contains("waive fir") || text.contains("not report") ||
                text.contains("obstruct") || text.contains("not disclose to authorities") ||
                text.contains("silence") && text.contains("complaint");
    }

    private boolean containsNoConsideration(String text) {
        return text.contains("free of charge") || text.contains("no payment") ||
                text.contains("gratuitous") || text.contains("nominal consideration") ||
                text.contains("one rupee") || text.contains("₹1") ||
                text.contains("without any fee");
    }

    private boolean containsForceMajeure(String text) {
        return text.contains("force majeure") || text.contains("act of god") ||
                text.contains("beyond control") || text.contains("unforeseen circumstances") ||
                text.contains("circumstances beyond");
    }

    private boolean containsPandemic(String text) {
        return text.contains("pandemic") || text.contains("epidemic") ||
                text.contains("covid") || text.contains("lockdown") ||
                text.contains("quarantine") || text.contains("public health emergency");
    }
}