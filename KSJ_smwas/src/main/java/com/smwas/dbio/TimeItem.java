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
@Table(name = "inquire_time_itemconclusion")
@IdClass(PK_time.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeItem implements Serializable {

	@Id
	@Column(name = "FID_COND_MRKT_DIV_CODE", columnDefinition="VARCHAR(2)")
	private String mrktType; // FID 조건 시장 분류 코드 J: 주식, ETF, ETN Length : 2
	
	@Id
	@Column(name = "FID_INPUT_ISCD", columnDefinition="VARCHAR(12)")
	private String jmCode; // FID 입력 종목코드 종목번호 (6자리)ETN의 경우, Q로 시작 (EX. Q500001) Length : 12

	@Id
	@Column(name = "FID_INPUT_HOUR_1", columnDefinition="VARCHAR(10)")
	private String hour;		// 조회 시작시간 -- 기준시간 (6자리; HH:MM:SS), ex) 155000 입력시 15시 50분 00초 기준 이전 체결 내역이 조회됨 (10)

	@Column(name = "output1")
	private String output1; // 기본 정보
	
	@Column(name = "output2")
	private String output2; // 시간별체결 정보


}
