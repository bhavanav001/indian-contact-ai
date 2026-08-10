CREATE TABLE IF NOT EXISTS irco_ontology (
  id                 INT AUTO_INCREMENT PRIMARY KEY,
  clause_type        VARCHAR(100) NOT NULL,
  law_name           VARCHAR(100) NOT NULL,
  section_num        VARCHAR(50)  NOT NULL,
  check_criteria     TEXT NOT NULL,
  severity_default   ENUM('LOW','MEDIUM','HIGH','CRITICAL') NOT NULL,
  detection_keywords TEXT,
  created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO irco_ontology (clause_type, law_name, section_num, check_criteria, severity_default, detection_keywords) VALUES
-- DATA PROTECTION (DPDP Act 2023)
('data_sharing','DPDP Act 2023','Section 6','Is an explicit consent mechanism specified before data is shared?','CRITICAL','share data,data sharing,transfer data,disclose data,third party data'),
('data_sharing','DPDP Act 2023','Section 7','Can consent be withdrawn, or is it framed as irrevocable/permanent?','HIGH','irrevocable,cannot be withdrawn,non-revocable,permanent consent'),
('data_storage','DPDP Act 2023','Section 8(7)','Is a data retention or deletion timeline defined?','HIGH','store data,data storage,retain data,data retention,keep data'),
('data_storage','DPDP Act 2023','Section 8(3)','Does the clause shift data accuracy obligations entirely onto the user?','MEDIUM','accuracy,user responsible,user shall ensure correctness'),
('data_transfer','DPDP Act 2023','Section 16','Is cross-border data transfer restricted to government-approved countries?','HIGH','outside india,international transfer,cross-border,cross border,overseas,abroad'),
('data_breach','DPDP Act 2023','Section 17','Is a data breach notification obligation and timeline defined?','HIGH','breach notification,data breach,security incident,notify in case'),
('minors_data','DPDP Act 2023','Section 9','Is there age verification or parental consent for clauses involving minors?','CRITICAL','minor,child,children,under 18,underage'),
('data_processing','DPDP Act 2023','Section 4','Is the specific purpose of data processing clearly stated?','MEDIUM','process data,processing personal data,use of data'),
-- PAYMENT / TAX (GST Act 2017)
('payment','GST Act 2017','Section 9','Is GST applicability and who bears the tax explicitly stated?','HIGH','payment,fee,charge,price,amount,consideration'),
('invoice','GST Act 2017','Rule 47','Is an invoice issuance timeline (30/45 days) specified?','MEDIUM','invoice,bill,tax invoice,proforma'),
('payment','GST Act 2017','Section 2(98)','If services involve RCM categories (legal, transport, import), is RCM addressed?','HIGH','legal service,advocate,transport,import,freight,gta'),
('payment','GST Act 2017','Section 31','Is a GSTIN / GST registration number referenced?','LOW','gstin,gst number,gst registration'),
('payment','GST Act 2017','Section 15','Is the transaction value fully disclosed with no hidden adjustments?','MEDIUM','hidden charge,adjustment,additional cost not disclosed'),
('payment','GST Act 2017','Section 16','Could pay-later or retention clauses affect Input Tax Credit eligibility?','MEDIUM','retention,pay later,deferred payment,withhold payment'),
-- CONTRACT VALIDITY (Indian Contract Act 1872)
('termination','Indian Contract Act 1872','Section 39','Is a notice period specified for termination?','MEDIUM','terminate,termination,notice period'),
('termination','Indian Contract Act 1872','Section 73','Are damages on termination capped or left open-ended?','HIGH','damages,compensation on termination'),
('termination','Indian Contract Act 1872','Section 74','Are penalty/liquidated-damages amounts reasonable and not punitive?','HIGH','penalty,liquidated damages,forfeit,forfeiture,deduct,fine'),
('indemnity','Indian Contract Act 1872','Section 124','Is the scope of indemnity bounded, or unlimited/unconscionable?','HIGH','indemnify,indemnity,hold harmless'),
('indemnity','Indian Contract Act 1872','Section 23','Is liability capped, or does the clause impose zero-cap/void consideration?','CRITICAL','unlimited liability,no cap on liability,liability shall be unlimited'),
('confidentiality','Indian Contract Act 1872','Section 27','Is the NDA/non-compete bounded by duration and geography?','HIGH','non-compete,non compete,not engage in,restraint of trade'),
('confidentiality','Indian Contract Act 1872','Section 27','Is the confidentiality obligation's duration and scope clearly defined?','MEDIUM','confidential,non-disclosure,nda'),
('dispute','Indian Contract Act 1872','Section 28','Does the clause attempt to fully oust Indian courts'' jurisdiction?','HIGH','no legal action,waive right to sue,exclusive jurisdiction outside,foreign arbitration'),
('force_majeure','Indian Contract Act 1872','Section 32','Is the force majeure clause mutual, or one-sided in favour of one party?','MEDIUM','force majeure,act of god,beyond control'),
('force_majeure','Indian Contract Act 1872','Section 56','Does force majeure explicitly cover pandemic/regulatory-change scenarios?','LOW','pandemic,epidemic,covid,lockdown,regulatory change'),
('consideration','Indian Contract Act 1872','Section 25','Is the consideration adequate, or is it a zero/nominal-consideration clause?','MEDIUM','free of charge,gratuitous,nominal consideration,one rupee'),
('public_policy','Indian Contract Act 1872','Section 23','Does the clause attempt to suppress complaints, FIRs, or obstruct legal process?','CRITICAL','suppress,waive fir,not report,obstruct,not disclose to authorities'),
-- ARBITRATION
('dispute','Arbitration and Conciliation Act 1996','Section 7','Is there a valid, written arbitration agreement clause?','MEDIUM','arbitration,arbitrator,arbitral'),
('dispute','Arbitration and Conciliation Act 1996','Section 20','Is the seat of arbitration specified and located in India?','HIGH','seat of arbitration,venue of arbitration,arbitration outside india'),
('dispute','Arbitration and Conciliation Act 1996','Section 11','Is the arbitration ad hoc or institutional, and is this stated clearly?','LOW','ad hoc arbitration,institutional arbitration'),
-- GOVERNING LAW / JURISDICTION
('governing_law','Civil Procedure Code 1908','Section 9','Is Indian jurisdiction and governing law explicitly specified?','MEDIUM','governing law,jurisdiction,courts of'),
-- IP
('ip','Copyright Act 1957','Section 17','Is IP/copyright ownership on work product clearly assigned?','HIGH','copyright,intellectual property,work product,ownership of ip'),
('ip','Patents Act 1970','Section 18','If the contract involves inventions, is assignment of patent rights addressed?','MEDIUM','patent,invention,assign rights'),
-- EMPLOYMENT
('employment','Shops and Establishments Act','State-specific','Is it clear whether this is a contract of service vs contract for service?','MEDIUM','employee,employer,contract of service,contract for service'),
('employment','Information Technology Act 2000','Section 43A','If handling sensitive personal data at work, is a data-protection obligation defined?','MEDIUM','sensitive personal data,employee data'),
-- CONSUMER
('liability','Consumer Protection Act 2019','Section 2(47)','Does the clause contain unfair contract terms disproportionately favouring one party?','HIGH','sole discretion,unilateral right,no liability whatsoever'),
-- MISC/GENERAL
('assignment','Indian Contract Act 1872','Section 37','Is assignment/sub-contracting of obligations addressed and consented to?','LOW','assign,sub-contract,delegate obligations'),
('notice','Indian Contract Act 1872','Section 3','Is a clear notice mechanism (mode, address, deemed delivery) specified?','LOW','notice shall be,mode of communication,deemed received'),
('amendment','Indian Contract Act 1872','Section 62','Is the process for amending/varying the contract clearly defined?','LOW','amendment,modify this agreement,variation'),
('severability','Indian Contract Act 1872','Section 24','Is a severability clause present to protect the rest of the contract if one clause is void?','LOW','severability,severable,remainder of this agreement');