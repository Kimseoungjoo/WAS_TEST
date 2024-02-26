package com.smwas.dbrepo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smwas.dbio.DailyItem;
import com.smwas.dbio.InquireItemChart;
import com.smwas.dbio.PK_chart;
import com.smwas.dbio.PK_dailyItem;


@Repository
public interface InquireItemChartRepository extends JpaRepository<InquireItemChart, PK_chart>{

}
