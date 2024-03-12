package com.smwas.io;


import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonIgnoreProperties(ignoreUnknown = true) 
public class SendRealData {
	private String mTrCode;
//	private List<String> mJmList;
	private String mRqName;	 // 1 : 등록 , 2 : 해제 
	private Map<String, String> mHeader; // header 값
	private Map<String, Object> mObjCommInput; // body 값  tr_key : 종목 키 , tr_id : 호가 ( H0STASP0 ) / 체결(H0STCNT0) 

	public String getTrCode() {
		return mTrCode;
	}

	public void setTrCode(String trCode) {
		mTrCode = trCode;
	}
	
	public String getmRqName() {
		return mRqName;
	}

	public void setmRqName(String rqName) {
		mRqName = rqName;
	}


	public Map<String, String> getHeader() {
		return mHeader;
	}

	public void setHeader(Map<String, String> header) {
		this.mHeader = header;
	}

	public Map<String, Object> getObjCommInput() {
		return mObjCommInput;
	}

	public void setObjCommInput(Map<String, Object> objCommInput) {
		this.mObjCommInput = objCommInput;
	}
	
	
	@Override
	public String toString() {
		ObjectMapper objectmapper  = new ObjectMapper();
		try {
			String strHeader;
			String strObjInput;
			 strHeader = objectmapper.writeValueAsString(getHeader());
			 strObjInput = objectmapper.writeValueAsString(getObjCommInput());
			 return "SendRealData [getTrCode()=" + getTrCode() + ", getrqName()=" + getmRqName()
			 + ", getmHeader()=" + strHeader + ", getmObjCommInput()=" + strObjInput + "]";
		} catch (Exception e) {
			return "";
		}
	}
}
