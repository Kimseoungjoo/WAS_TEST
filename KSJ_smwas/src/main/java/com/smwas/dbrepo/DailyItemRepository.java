package com.smwas.dbrepo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smwas.dbio.DailyItem;
import com.smwas.dbio.PK_dailyItem;


@Repository
public interface DailyItemRepository extends JpaRepository<DailyItem, PK_dailyItem>{


}
