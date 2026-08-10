package com.contactai.indian_contact_ai.repository;

import com.contactai.indian_contact_ai.model.IrcoOntology;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IrcoOntologyRepository extends JpaRepository<IrcoOntology, Integer> {

    List<IrcoOntology> findByClauseTypeAndSectionNum(String clauseType, String sectionNum);

    List<IrcoOntology> findByLawName(String lawName);
}