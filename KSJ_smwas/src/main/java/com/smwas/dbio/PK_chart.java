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
public class PK_chart implements Serializable {
	private String jmCode;		// FID 입력 종목코드 종목번호 (6자리)ETN의 경우, Q로 시작 (EX. Q500001) Length : 12
	private String date1;		// 일별 날짜 (YYYYMMDD) 형식 저장 
}
