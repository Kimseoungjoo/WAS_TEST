package com.smwas.dbio;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 복합키를 담고 있는 식별자 클래스
 */
@SuppressWarnings("serial")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PK_dailyPrice implements Serializable {
	private String mrktType;	// FID 조건 시장 분류 코드 J: 주식, ETF, ETN Length : 2
	private String jmCode;		// FID 입력 종목코드 종목번호 (6자리)ETN의 경우, Q로 시작 (EX. Q500001) Length : 12
	private String periodCode;	// 기간분류코드 -- D:일봉 W:주봉, M:월봉, Y:년봉 (32)
	private String orgAdj;		// 수정주가 원주가 가격 여부 -- 0:수정주가 1:원주가 (10)
}
