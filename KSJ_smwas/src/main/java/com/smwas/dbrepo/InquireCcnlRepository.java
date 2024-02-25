package com.smwas.dbrepo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smwas.dbio.PK_inquire;
import com.smwas.dbio.InquireCcnl;


@Repository
public interface InquireCcnlRepository extends JpaRepository<InquireCcnl, PK_inquire>{

	
	
}
