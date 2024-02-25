package com.smwas.dbrepo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smwas.dbio.PK_inquire;
import com.smwas.dbio.InquireAskPrice;


@Repository
public interface InquireAskPriceRepository extends JpaRepository<InquireAskPrice, PK_inquire>{

	
	
}
