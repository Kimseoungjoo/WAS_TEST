package com.smwas.dbrepo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smwas.dbio.PK_inquire;
import com.smwas.dbio.InquireInvestor;


@Repository
public interface InquireInvestorRepository extends JpaRepository<InquireInvestor, PK_inquire>{

	
}
