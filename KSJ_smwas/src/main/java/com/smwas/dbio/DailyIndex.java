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
@Table(name = "inquire_daily_indexchartprice")
@IdClass(PK_dailyIndex.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyIndex implements Serializable {

	@Id
	@Column(name = "FID_COND_MRKT_DIV_CODE", columnDefinition="VARCHAR(2)")
	private String mrktType; // FID 조건 시장 분류 코드 J: 주식, ETF, ETN Length : 2
	
	@Id
	@Column(name = "FID_INPUT_ISCD", columnDefinition="VARCHAR(12)")
	private String jmCode; // FID 입력 종목코드 종목번호 (6자리)ETN의 경우, Q로 시작 (EX. Q500001) Length : 12
	
	@Id
	@Column(name = "FID_INPUT_DATE_1", columnDefinition="VARCHAR(10)")
	private String date1;		// 조회 시작일자 (ex. 20220501) (10)
	
	@Id
	@Column(name = "FID_INPUT_DATE_2", columnDefinition="VARCHAR(10)")
	private String date2;		// 조회 종료일자 (ex. 20220530) (10)
	
	@Id
	@Column(name = "FID_PERIOD_DIV_CODE", columnDefinition="VARCHAR(32)")
	private String periodCode;	// 기간분류코드 -- D:일봉 W:주봉, M:월봉, Y:년봉 (32)

	@Column(name = "output1")
	private String output1; // 응답상세
	
	@Column(name = "output2")
	private String output2; // 조회 기간별 시세


}
