package com.smwas.dbrepo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smwas.dbio.DailyPrice;
import com.smwas.dbio.PK_dailyPrice;


@Repository
public interface DailyPriceRepository extends JpaRepository<DailyPrice, PK_dailyPrice>{

	
}
