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
@Table(name = "inquire_asking_price_exp_ccn")
@IdClass(PK_inquire.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InquireAskPrice implements Serializable {

	@Id
	@Column(name = "FID_COND_MRKT_DIV_CODE", columnDefinition="VARCHAR(2)")
	private String mrktType; // FID 조건 시장 분류 코드 J: 주식, ETF, ETN Length : 2
	
	@Id
	@Column(name = "FID_INPUT_ISCD", columnDefinition="VARCHAR(12)")
	private String jmCode; // FID 입력 종목코드 종목번호 (6자리)ETN의 경우, Q로 시작 (EX. Q500001) Length : 12

	@Column(name = "output1")
	private String output1; // 응답상세1
	
	@Column(name = "output2")
	private String output2; // 응답상세1

}
