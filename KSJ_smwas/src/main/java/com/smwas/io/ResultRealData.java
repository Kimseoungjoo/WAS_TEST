package com.smwas.io;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResultRealData {
	private String mTrCode = "";
	private List<String> mJmCode = new ArrayList<String>();
	private List<Object> mOutRecList = new ArrayList<Object>();
	private Map<String, String> mOutRec;


	@JsonProperty("TrCode")
	public String getTrCode() {
		return mTrCode;
	}

	public void setTrCode(String trCode) {
		mTrCode = trCode;
	}

	@JsonProperty("JmCode")
	public List<String> getJmCode() {
		return mJmCode;
	}
	
	
	
	
	
	public List<Object> getmOutRecList() {
		return mOutRecList;
	}

	public void setmOutRecList(List<Object> mOutRecList) {
		this.mOutRecList = mOutRecList;
	}

	public Map<String, String> getmOutRec() {
		return mOutRec;
	}

	public void setmOutRec(Map<String, String> mOutRec) {
		this.mOutRec = mOutRec;
	}

	@JsonProperty("Result")
	public Map<String,String> getOutRecList() {
		return mOutRec;
	}

	
	
//	public void addRealData(Object resultData) {
//		mOutRecList.add(resultData);
//	}
}
