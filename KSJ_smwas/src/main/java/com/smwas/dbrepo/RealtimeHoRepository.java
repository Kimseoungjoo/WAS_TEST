package com.smwas.dbrepo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.smwas.dbio.RealtimeHo;


@Repository
@Transactional
public interface RealtimeHoRepository extends JpaRepository<RealtimeHo, Integer>{

	@Modifying
    @Query(value = "ALTER TABLE realtime_ho AUTO_INCREMENT = 1", nativeQuery = true)
    void resetAutoIncrement();
	
	List<RealtimeHo> findByTrKey(String trKey);
}

