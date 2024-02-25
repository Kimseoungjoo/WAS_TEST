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
@Table(name = "inquire_daily_price")
@IdClass(PK_dailyPrice.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyPrice implements Serializable {

	@Id
	@Column(name = "FID_COND_MRKT_DIV_CODE", columnDefinition="VARCHAR(2)")
	private String mrktType; // FID 조건 시장 분류 코드 J: 주식, ETF, ETN Length : 2
	
	@Id
	@Column(name = "FID_INPUT_ISCD", columnDefinition="VARCHAR(12)")
	private String jmCode; // FID 입력 종목코드 종목번호 (6자리)ETN의 경우, Q로 시작 (EX. Q500001) Length : 12
	
	@Id
	@Column(name = "FID_PERIOD_DIV_CODE", columnDefinition="VARCHAR(32)")
	private String periodCode;	// 기간분류코드 -- D:일봉 W:주봉, M:월봉, Y:년봉 (32)
	
	@Id
	@Column(name = "FID_ORG_ADJ_PRC", columnDefinition="VARCHAR(10)")
	private String orgAdj;		// 수정주가 원주가 가격 여부 -- 0:수정주가 1:원주가 (10)

	@Column(name = "output")
	private String output; // 응답상세


}
