package com.smwas.dbio;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@SuppressWarnings("serial")
@Entity
@Table(name = "inquire_itemchartprice")
@IdClass(PK_chart.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InquireItemChart implements Serializable {

	
	@Id
	@Column(name = "FID_INPUT_ISCD", columnDefinition="VARCHAR(12)")
	private String jmCode; // FID 입력 종목코드 종목번호 (6자리)ETN의 경우, Q로 시작 (EX. Q500001) Length : 12
	
	@Id
	@Column(name = "FID_INPUT_DATE", columnDefinition="VARCHAR(10)")
	private String date1; // FID 조건 시장 분류 코드 J: 주식, ETF, ETN Length : 2

	@Column(name = "output")
	private String chartData; // 한투 API 조회 값 JSON > String


}
