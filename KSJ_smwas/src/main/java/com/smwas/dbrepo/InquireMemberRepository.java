package com.smwas.dbrepo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smwas.dbio.PK_inquire;
import com.smwas.dbio.InquireMember;


@Repository
public interface InquireMemberRepository extends JpaRepository<InquireMember, PK_inquire>{

//	InquireMember findByPK(String mrktType, String jmCode);		// select
//	
//	void deleteByPK(String mrktType, String jmCode);				// delete
	
}
