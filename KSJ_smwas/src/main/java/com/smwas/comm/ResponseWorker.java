package com.smwas.comm;

import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smwas.io.ResultTrData;
import com.smwas.util.LOGCAT;

/**
 * 통신 응답 Thread
 * 실시간 데이터 변환
 * 
 */
public class ResponseWorker extends Thread {
	private static volatile ResponseWorker INSTANCE = new ResponseWorker();
	public static ResponseWorker getInstance() {
		return INSTANCE;
	}
	
	public ResponseWorker(){}
	public ResponseWorker(ArrayBlockingQueue<ResultTrData> queue) {
	}
	// 데이터 변환 
	public String  responseData (String data, String trCode)	{
		ResultTrData realResultData = new ResultTrData();
		
		if(data != "" || data != null) {
			String[] recvvalue = data.split("\\^");
			new HashMap<String,String>();
			realResultData.setTrCode(trCode);
//			LOGCAT.i("ResponseWorker", "실시간 응답 코드 : " + trCode);
			
			if(trCode.equals("H0STASP0") ) {
				realResultData.getOutRecMap().put("MKSC_SHRN_ISCD", recvvalue[0]); 			//	유가증권 단축 종목코드
				realResultData.getOutRecMap().put("BSOP_HOUR", recvvalue[1]);				//	영업 시간
				realResultData.getOutRecMap().put("HOUR_CLS_CODE", recvvalue[2]); 			//	시간 구분 코드
				realResultData.getOutRecMap().put("ASKP1", recvvalue[3]); 					//	매도호가1
				realResultData.getOutRecMap().put("ASKP2", recvvalue[4]); 					//	매도호가2
				realResultData.getOutRecMap().put("ASKP3", recvvalue[5]); 					//	매도호가3
				realResultData.getOutRecMap().put("ASKP4", recvvalue[6]); 					//	매도호가4
				realResultData.getOutRecMap().put("ASKP5", recvvalue[7]); 					//	매도호가5
				realResultData.getOutRecMap().put("ASKP6", recvvalue[8]); 					//	매도호가6
				realResultData.getOutRecMap().put("ASKP7", recvvalue[9]); 					//	매도호가7
				realResultData.getOutRecMap().put("ASKP8", recvvalue[10]); 					//	매도호가8
				realResultData.getOutRecMap().put("ASKP9", recvvalue[11]); 					//	매도호가9
				realResultData.getOutRecMap().put("ASKP10", recvvalue[12]); 				//	매도호가10
				
				realResultData.getOutRecMap().put("BIDP1", recvvalue[13]); 					//	매수호가1
				realResultData.getOutRecMap().put("BIDP2", recvvalue[14]); 					//	매수호가2
				realResultData.getOutRecMap().put("BIDP3", recvvalue[15]); 					//	매수호가3
				realResultData.getOutRecMap().put("BIDP4", recvvalue[16]); 					//	매수호가4
				realResultData.getOutRecMap().put("BIDP5", recvvalue[17]); 					//	매수호가5
				realResultData.getOutRecMap().put("BIDP6", recvvalue[18]); 					//	매수호가6
				realResultData.getOutRecMap().put("BIDP7", recvvalue[19]); 					//	매수호가7
				realResultData.getOutRecMap().put("BIDP8", recvvalue[20]); 					//	매수호가8
				realResultData.getOutRecMap().put("BIDP9", recvvalue[21]); 					//	매수호가9
				realResultData.getOutRecMap().put("BIDP10", recvvalue[22]); 				//	매수호가10
				
				realResultData.getOutRecMap().put("ASKP_RSQN1", recvvalue[23]); 			//	매도호가 잔량1		
				realResultData.getOutRecMap().put("ASKP_RSQN2", recvvalue[24]); 			//	매도호가 잔량2
				realResultData.getOutRecMap().put("ASKP_RSQN3", recvvalue[25]); 			//	매도호가 잔량3
				realResultData.getOutRecMap().put("ASKP_RSQN4", recvvalue[26]); 			//	매도호가 잔량4
				realResultData.getOutRecMap().put("ASKP_RSQN5", recvvalue[27]); 			//	매도호가 잔량5
				realResultData.getOutRecMap().put("ASKP_RSQN6", recvvalue[28]); 			//	매도호가 잔량6
				realResultData.getOutRecMap().put("ASKP_RSQN7", recvvalue[29]); 			//	매도호가 잔량7
				realResultData.getOutRecMap().put("ASKP_RSQN8", recvvalue[30]); 			//	매도호가 잔량8
				realResultData.getOutRecMap().put("ASKP_RSQN9", recvvalue[31]); 			//	매도호가 잔량9
				realResultData.getOutRecMap().put("ASKP_RSQN10", recvvalue[32]); 			//	매도호가 잔량10
				
				realResultData.getOutRecMap().put("BIDP_RSQN1", recvvalue[33]); 			//	매수호가 잔량1		
				realResultData.getOutRecMap().put("BIDP_RSQN2", recvvalue[34]); 			//	매수호가 잔량2
				realResultData.getOutRecMap().put("BIDP_RSQN3", recvvalue[35]); 			//	매수호가 잔량3
				realResultData.getOutRecMap().put("BIDP_RSQN4", recvvalue[36]); 			//	매수호가 잔량4
				realResultData.getOutRecMap().put("BIDP_RSQN5", recvvalue[37]); 			//	매수호가 잔량5
				realResultData.getOutRecMap().put("BIDP_RSQN6", recvvalue[38]); 			//	매수호가 잔량6
				realResultData.getOutRecMap().put("BIDP_RSQN7", recvvalue[39]); 			//	매수호가 잔량7
				realResultData.getOutRecMap().put("BIDP_RSQN8", recvvalue[40]); 			//	매수호가 잔량8
				realResultData.getOutRecMap().put("BIDP_RSQN9", recvvalue[41]); 			//	매수호가 잔량9
				realResultData.getOutRecMap().put("BIDP_RSQN10", recvvalue[42]); 			//	매수호가 잔량10
				
				realResultData.getOutRecMap().put("TOTAL_ASKP_RSQN",recvvalue[43]);			// 총매도호가 잔량
				realResultData.getOutRecMap().put("TOTAL_BIDP_RSQN",recvvalue[44]);			// 총매수호가 잔량
				realResultData.getOutRecMap().put("OVTM_TOTAL_ASKP_RSQN",recvvalue[45]);	// 시간외 총매도호가 잔량
				realResultData.getOutRecMap().put("OVTM_TOTAL_BIDP_RSQN",recvvalue[46]);	// 시간외 총매수호가 증감 >> 시간외 총 매수호가 잔량
				realResultData.getOutRecMap().put("ANTC_CNPR",recvvalue[47]);				// 예상 체결가
				realResultData.getOutRecMap().put("ANTC_CNQN",recvvalue[48]);				// 예상 체결량
				realResultData.getOutRecMap().put("ANTC_VOL",recvvalue[49]);				// 예상 거래량
				realResultData.getOutRecMap().put("ANTC_CNTG_VRSS	",recvvalue[50]);		// 예상체결 대비
				realResultData.getOutRecMap().put("ANTC_CNTG_VRSS_SIGN",recvvalue[51]);		// 예상 체결 대비 부호 1 : 상한 2 : 상승 3 : 보합 4 : 하한 5 : 하락
				realResultData.getOutRecMap().put("ANTC_CNTG_PRDY_CTRT",recvvalue[52]);		// 예상체결 전일대비율
				realResultData.getOutRecMap().put("ACML_VOL",recvvalue[53]);				// 누적거래량
				realResultData.getOutRecMap().put("TOTAL_ASKP_RSQN_ICDC",recvvalue[54]);	// 총매도호가 잔량 증감
				realResultData.getOutRecMap().put("TOTAL_BIDP_RSQN_ICDC",recvvalue[55]);	// 총매수호가 잔량 증감
				realResultData.getOutRecMap().put("OVTM_TOTAL_ASKP_ICDC",recvvalue[56]);	// 시간외 총 매도호가 증감
				realResultData.getOutRecMap().put("OVTM_TOTAL_BIDP_ICDC",recvvalue[57]);	// 시간외 총 매수호가 증감
				realResultData.getOutRecMap().put("STCK_DEAL_CLS_CODE",recvvalue[58]);		// 주식매매 구분코드
				realResultData.getOutRecMap().put("type", "realData");
			}else {
				
				//"유가증권단축종목코드|주식체결시간|주식현재가|전일대비부호|전일대비|전일대비율|가중평균주식가격|주식시가|주식최고가|주식최저가|매도호가1|매수호가1|체결거래량|누적거래량|누적거래대금|매도체결건수|매수체결건수|순매수체결건수|체결강도|총매도수량|총매수수량|체결구분|매수비율|전일거래량대비등락율|시가시간|시가대비구분|시가대비|최고가시간|고가대비구분|고가대비|최저가시간|저가대비구분|저가대비|영업일자|신장운영구분코드|거래정지여부|매도호가잔량|매수호가잔량|총매도호가잔량|총매수호가잔량|거래량회전율|전일동시간누적거래량|전일동시간누적거래량비율|시간구분코드|임의종료구분코드|정적VI발동기준가";
				String menulistJ = "MKSC_SHRN_ISCD|STCK_CNTG_HOUR|STCK_PRPR|PRDY_VRSS_SIGN|PRDY_VRSS|PRDY_CTRT|WGHN_AVRG_STCK_PRC|STCK_OPRC|STCK_HGPR|STCK_LWPR|"
						+ "ASKP1|BIDP1|CNTG_VOL|ACML_VOL|ACML_TR_PBMN|SELN_CNTG_CSNU|SHNU_CNTG_CSNU|NTBY_CNTG_CSNU|CTTR|SELN_CNTG_SMTN|SHNU_CNTG_SMTN|CCLD_DVSN|"
						+ "SHNU_RATE|PRDY_VOL_VRSS_ACML_VOL_RATE|OPRC_HOUR|OPRC_VRSS_PRPR_SIGN|OPRC_VRSS_PRPR|HGPR_HOUR|HGPR_VRSS_PRPR_SIGN|HGPR_VRSS_PRPR|LWPR_HOUR|LWPR_VRSS_PRPR_SIGN|"
						+ "LWPR_VRSS_PRPR|BSOP_DATE|NEW_MKOP_CLS_CODE|TRHT_YN|ASKP_RSQN1|BIDP_RSQN1|TOTAL_ASKP_RSQN|TOTAL_BIDP_RSQN|VOL_TNRT|PRDY_SMNS_HOUR_ACML_VOL|"
						+ "PRDY_SMNS_HOUR_ACML_VOL_RATE|HOUR_CLS_CODE|MRKT_TRTM_CLS_CODE|VI_STND_PRC";
				String[] arrMenu = menulistJ.split("\\|");
				for (int i=0; i<arrMenu.length;i++)	{
					realResultData.getOutRecMap().put(arrMenu[i], recvvalue[i]);
				}
				realResultData.getOutRecMap().put("type", "realData");
			}
			if(realResultData != null) {
				String realReqJson;
				try {
					realReqJson = new ObjectMapper().writeValueAsString(realResultData);
					return  realReqJson;
				} catch (JsonProcessingException e) {
					e.printStackTrace();
					return null;
				}
			}
		}
		return null;
		
		
		
	}
}
