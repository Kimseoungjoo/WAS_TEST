package com.smwas.dbrepo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smwas.dbio.PK_inquire;
import com.smwas.dbio.InquirePrice;


@Repository
public interface InquirePriceRepository extends JpaRepository<InquirePrice, PK_inquire>{

//	InquirePrice findByPK(String mrktType, String jmCode);		// select
	
//	void deleteByPK(String mrktType, String jmCode);				// delete
	
}
