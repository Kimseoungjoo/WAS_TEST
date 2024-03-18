package com.smwas.dbrepo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smwas.dbio.DailyIndex;
import com.smwas.dbio.PK_dailyIndex;


//@Repository
public interface DailyIndexRepository extends JpaRepository<DailyIndex, PK_dailyIndex>{

	
}
