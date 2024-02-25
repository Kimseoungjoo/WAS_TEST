package com.smwas.tr;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ErrorFile {
	
	private static volatile ErrorFile INSTANCE = new ErrorFile();
	ObjectMapper objectMapper = new ObjectMapper();
	private Map<String, String> errCodeMap;
	 
	
	public ErrorFile() {
	    try {
	    	if(errCodeMap == null ) {
	    		// JSON 파일을 리소스 경로에서 읽어오기
	    		InputStream inputStream = getClass().getClassLoader().getResourceAsStream("errorMaster.json");
	    		
	    		// JSON 파일을 Map<String, String> 형태로 변환
	    		errCodeMap = objectMapper.readValue(inputStream, new TypeReference<Map<String, String>>() {});
	    	}
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	// tr_id 반환 
	public String getErrorMsg(String msgCode) {
		return errCodeMap.get(msgCode);
	}
	
	// URL 반환 
	public String getUrl(String errormsg) {
		return errCodeMap.containsKey(errormsg) ? errormsg : null;
	}
	
	public static ErrorFile getInstance() {
		
		return INSTANCE;
	}

}
