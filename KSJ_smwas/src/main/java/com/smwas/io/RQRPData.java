package com.smwas.io;

import java.util.Map;

import org.springframework.http.HttpHeaders;


public class RQRPData {
	private String mParams; // RQ 데이터 
	private String trCode;
	private String ClientIP; // client IP
	private Map<String, String> mSendTrData; // RQ, RP API 정보 데이터 (?) 
	private Map<String, String> mSendRealData; // 실시간 데이터 
	private HttpHeaders mBaseHeader; // 해당 RQ,RP HEADER
	
	public String getClientIP() {
		return ClientIP;
	}
	public void setClientIP(String clientIP) {
		ClientIP = clientIP;
	}
	public String getTrCode() {
		return trCode;
	}
	public void setTrCode(String trCode) {
		this.trCode = trCode;
	}
	public String getParams() {
		return mParams;
	}
	public void setmParams(String mParams) {
		this.mParams = mParams;
	}
	public Map<String, String> getSendTrData() {
		return mSendTrData;
	}
	public void setmSendTrData(Map<String, String> mSendTrData) {
		this.mSendTrData = mSendTrData;
	}
	public Map<String, String> getSendRealData() {
		return mSendRealData;
	}
	public void setmSendRealData(Map<String, String> mSendRealData) {
		this.mSendRealData = mSendRealData;
	}
	public HttpHeaders getBaseHeader() {
		return mBaseHeader;
	}
	public void setmBaseHeader(HttpHeaders httpHeaders) {
		this.mBaseHeader = httpHeaders;
	}
	
	
}
