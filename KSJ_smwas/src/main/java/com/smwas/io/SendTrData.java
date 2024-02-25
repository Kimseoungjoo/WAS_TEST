package com.smwas.io;

import java.util.Map;

/*
 * TrBuilder에서 전송되는 값 (그대로 쓰기)
 * */
public class SendTrData {
	private String mTrCode;
	private String mClientIP;

	private String mRqName;
	private Map<String, String> mHeader; // header 값
	private Map<String, String> mObjCommInput; // body 값 


	public String getmClientIP() {
		return mClientIP;
	}
	
	public void setmClientIP(String mClientIP) {
		this.mClientIP = mClientIP;
	}
	public String getTrCode() {
		return mTrCode;
	}

	public void setTrCode(String trCode) {
		mTrCode = trCode;
	}

	public String getRqName() {
		return mRqName;
	}

	public void setRqName(String rqName) {
		mRqName = rqName;
	}

	public Map<String, String> getHeader() {
		return mHeader;
	}

	public void setHeader(Map<String, String> header) {
		mHeader = header;
	}

	public Map<String, String> getObjCommInput() {
		return mObjCommInput;
	}

	public void setObjCommInput(Map<String, String> objCommInput) {
		mObjCommInput = objCommInput;
	}
	public String toString() {
		return "SendTrData : [  mTrCode : "+getTrCode() + ", mClientIP : " + getmClientIP() + 
				" mHeader : "+getHeader() + ", mObjCommInput : " + getObjCommInput();
	}
}
