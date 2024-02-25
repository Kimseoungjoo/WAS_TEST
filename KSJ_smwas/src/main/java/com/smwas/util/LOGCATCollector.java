package com.smwas.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LOGCATCollector {
	String logcatLog;
	
	private static final LOGCATCollector instance = new LOGCATCollector();
	
	public LOGCATCollector() {}
	
    public static LOGCATCollector getInstance() {
        return instance;
    }
	
	
    // 로그 저장
    public String saveLogcat(String level, String strTag, String strMsg) {
    	String formattedLevel = String.format("%-6s", level);
    	String formattedTag = String.format("%-20s", strTag);
    	
        logcatLog = "| " + getCurrentDateTime() + " | " + formattedLevel + " | " + formattedTag + " | " + strMsg + " | ";
        return logcatLog;
    }
    
	// 저장된 로그 리턴
	public String getLogcat() {
        return logcatLog;
	}
    
    
	// 저장된 로그 삭제
	public void clearLogcat() {
		logcatLog = null;
	}

	
	// LOGCAT 메세지에 삽입할 날짜/시간 구하기
	public String getCurrentDateTime() {
		LocalDateTime now = LocalDateTime.now();
		String formattedNow = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSS"));
		
		return formattedNow;
	}
	

}

