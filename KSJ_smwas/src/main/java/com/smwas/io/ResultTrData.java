package com.smwas.io;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResultTrData {
	private String mKey;
	private String mRqName = "";
	
	
	private Boolean dataFlag = false;	// output null 여부 (예외처리)
	private String mTrCode = "";		// "/uapi/domestic-stock/v1/quotations/inquire-daily-price"
	private String statusCode = "";		// 인풋값이 맞지 않으면 400, DB에 없으면 200 + output 빈값
	private Map<String, Object> headersMap = new HashMap<String, Object>();		
	private Map<String, Object> mOutRecMap = new HashMap<String, Object>();		// output

	/**
	 * 생성자
	 */
	public ResultTrData() {}
	
	public ResultTrData(String mTrCode, String statusCode, Map<String, Object> headersMap,
			Map<String, Object> mOutRecMap, Boolean dataFlag) {
		this.mTrCode = mTrCode;
		this.dataFlag = dataFlag;
		this.statusCode = statusCode;
		this.headersMap = headersMap;
		this.mOutRecMap = mOutRecMap;
	}

	

	@JsonProperty("RqName")
	public String getRqName() {
		return mRqName;
	}
	
	public void setRqName(String rqName) {
		mRqName = rqName;
	}
	
	public Boolean getDataFlag() {
		return dataFlag;
	}

	public void setDataFlag(Boolean dataFlag) {
		this.dataFlag = dataFlag;
	}

	@JsonProperty("TrCode")
	public String getTrCode() {
		return mTrCode;
	}

	public void setTrCode(String trCode) {
		mTrCode = trCode;
	}
	
	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	@JsonProperty("Header")
	public Map<String, Object> getHeaders() {
		return this.headersMap;
	}
	
	public void setHeaders(org.springframework.http.HttpHeaders httpHeaders) {
		
		 for (Map.Entry<String, List<String>> entry : httpHeaders.entrySet()) {
			 String headerName = entry.getKey();
			 List<String> headerValues = entry.getValue();
			 this.headersMap.put(headerName, headerValues.size() > 1 ? headerValues : headerValues.get(0));
		 }
	}
	
	@JsonProperty("Data")
	public Map<String, Object> getOutRecMap() {
		return mOutRecMap;
	}
	public void setmOutRecMap(Map<String, Object> mOutRecMap) {
		this.mOutRecMap = mOutRecMap;
	}
	public String toString() {
		return "SendTrData : [  mTrCode : "+getTrCode() + ", StatusCode : " + getStatusCode() + 
				" Header : "+getHeaders() + ", mObjCommOutput : " + getOutRecMap(); 
	}
	public String getmKey() {
		return mKey;
	}

	public void setmKey(String mKey) {
		this.mKey = mKey;
	}

	

	
}
