package com.smwas.util;

import java.io.IOException;
import java.util.logging.Logger;

import com.smwas.monitoring.FileHandler;


public class LOGCAT {
	// isEnable false : 로그캣 사용안함, true : 로그캣 사용
	private static final boolean isEnable = true;
	// isException false : PrintStacTrace 사용안함, true : PrintStacTrace 사용
	private static final boolean isException = true;
	// Logger 클래스
	private static final Logger log = Logger.getGlobal();
	
	
	/**
	 * Information Log
	 * 
	 * @param strTag
	 * @param strMsg
	 */
	public static void i(String strTag, String strMsg) {
		if (isEnable) {
			
			// 콘솔용 LOGCAT
			log.info(String.format("[%s] %s", strTag, strMsg));
			
			// 로그용 LOGCAT
			LOGCATCollector.getInstance().saveLogcat("INFO", strTag, strMsg);	// 로그 수집
			try {																
				FileHandler.getInstance().writeLog(strTag);							// 로그 쓰기
			} catch (IOException e) {
				e.printStackTrace();
			}					
		}
	}

	/**
	 * Warning Log
	 * 
	 * @param strTag
	 * @param strMsg
	 * @throws IOException 
	 */
	public static void w(String strTag, String strMsg) {
		if (isEnable) {
			
			// 콘솔용 LOGCAT
			log.warning(String.format("[%s] %s", strTag, strMsg));
			
			// 로그용 LOGCAT
			LOGCATCollector.getInstance().saveLogcat("WARN", strTag, strMsg);	// 로그 수집
			try {																
				FileHandler.getInstance().writeLog(strTag);							// 로그 쓰기
			} catch (IOException e) {
				e.printStackTrace();
			}					
		}
	}
			
			

	/**
	 * Severe Log
	 * 
	 * @param strTag
	 * @param strMsg
	 * @throws IOException 
	 */
	public static void s(String strTag, String strMsg) {
		if (isEnable) {

			// 콘솔용 LOGCAT
			log.severe(String.format("[%s] %s", strTag, strMsg));
			
			// 로그용 LOGCAT
			LOGCATCollector.getInstance().saveLogcat("SEVERE", strTag, strMsg);	// 로그 수집
			try {																
				FileHandler.getInstance().writeLog(strTag);							// 로그 쓰기
			} catch (IOException e) {
				e.printStackTrace();
			}					
		}
	}
			
			
	/**
	 * Exception PrintStackTrace
	 * 
	 * @param e
	 */
	public static void printStackTrace(Exception e) {
		if (isException) {
			e.printStackTrace();
		}
	}
	
}