package com.smwas.dbrepo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smwas.dbio.PK_time;
import com.smwas.dbio.TimeItem;


@Repository
public interface TimeItemRepository extends JpaRepository<TimeItem, PK_time>{

	
}
