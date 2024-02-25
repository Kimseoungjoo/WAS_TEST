package com.smwas.dbrepo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.smwas.dbio.RealtimeChe;


@Repository
@Transactional
public interface RealtimeCheRepository extends JpaRepository<RealtimeChe, Integer>{
	
	@Modifying
    @Query(value = "ALTER TABLE realtime_che AUTO_INCREMENT = 1", nativeQuery = true)
    void resetAutoIncrement();
	
	List<RealtimeChe> findByTrKey(String trKey);
	
}