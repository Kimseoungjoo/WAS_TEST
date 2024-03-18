package com.smwas.dbrepo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.smwas.dbio.InquireItemChart;
import com.smwas.dbio.PK_chart;


@Repository
//@Component
public interface InquireItemChartRepository extends JpaRepository<InquireItemChart, PK_chart>{
	
	@Query(value = "SELECT s FROM InquireItemChart s where jmCode = :jmcode AND date1 > :to_date AND date1 < :from_date")
	List<InquireItemChart> selectChartData(@Param("jmcode") String jmcode, @Param("to_date") String to_date, @Param("from_date") String from_date);
	
}
