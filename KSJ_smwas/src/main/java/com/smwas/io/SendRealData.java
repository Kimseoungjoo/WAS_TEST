package com.smwas.io;


import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonIgnoreProperties(ignoreUnknown = true) 
public class SendRealData {
	private String mTrCode;
//	private List<String> mJmList;
	private String mRqName;	 // 1 : 등록 , 2 : 해제 
	private Map<String, Object> mHeader; // header 값
	private Map<String, Object> mObjCommInput; // body 값  tr_key : 종목 키 , tr_id : 호가 ( H0STASP0 ) / 체결(H0STCNT0) 

	public String getTrCode() {
		return mTrCode;
	}

	public void setTrCode(String trCode) {
		mTrCode = trCode;
	}

//	public List<String> getJmList() {
//		return mJmList;
//	}
//
//	public void setJmList(List<String> jmList) {
//		mJmList = jmList;
//	}

	public String getmRqName() {
		return mRqName;
	}

	public void setmRqName(String rqName) {
		mRqName = rqName;
	}


	public Map<String, Object> getmHeader() {
		return mHeader;
	}

	public void setHeader(Map<String, Object> header) {
		this.mHeader = header;
	}

	public Map<String, Object> getmObjCommInput() {
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
			 strHeader = objectmapper.writeValueAsString(getmHeader());
			 strObjInput = objectmapper.writeValueAsString(getmObjCommInput());
			 return "SendRealData [getTrCode()=" + getTrCode() + ", getrqName()=" + getmRqName()
			 + ", getmHeader()=" + strHeader + ", getmObjCommInput()=" + strObjInput + "]";
		} catch (Exception e) {
			return "";
		}
	}
}
