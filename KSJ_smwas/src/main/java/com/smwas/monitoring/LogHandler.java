package com.smwas.monitoring;

import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


import com.smwas.client.ClientService;
import com.smwas.util.LOGCAT;

import jakarta.servlet.http.HttpServletRequest;

public class LogHandler {
	public static final String TAG = LogHandler.class.getSimpleName();
//	private static final Logger logger = LogManager.getLogger(DailyLog.class);
	
	/**
	 * 로그인 관련 로그
	 * 로그인 성공 여부 및 로그인을 시도한 클라이언트 IP 표출
	 */
	public void checkLogin (HttpServletRequest request) {

		// 로그인 실패 횟수 담을 map
		ConcurrentHashMap<String, AtomicInteger> loginFailureCount = new ConcurrentHashMap<>();
		
		// 로그인 성공 여부 판단
		boolean isLoginSuccess = ClientService.getInstance().checkLogin(ClientService.getFixedId(), ClientService.getFixedPw());
			
		String clientIp;
		try {
			clientIp = ClientService.getInstance().getClientIp(request);			
			
			AtomicInteger failCount = loginFailureCount.get(clientIp);
		
			if (isLoginSuccess) {
				LOGCAT.i(TAG, "로그인 성공 - IP: " + clientIp);
				loginFailureCount.remove(clientIp);						// 로그인 성공 시 실패 횟수 초기화
			} else {													// 로그인 실패 시 실패 횟수 계산
				if (failCount == null) {							
					failCount = new AtomicInteger();				
					loginFailureCount.put(clientIp, failCount);
				}
				failCount.incrementAndGet();							// 로그인 횟수 1씩 증가
				
				int totalFailCount = failCount.incrementAndGet();
				
				LOGCAT.w(TAG, "로그인 실패 - IP: " + clientIp + "에서 로그인이 " + totalFailCount + "회 실패하였습니다.");
			}
		} catch (UnknownHostException e) {
			LOGCAT.i(TAG, "IP 정보 없음");
		}
	}

	
	
}
