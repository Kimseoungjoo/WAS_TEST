package com.smwas.client;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.smwas.util.LOGCAT;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;


public class ClientService {
	public static final String TAG = ClientService.class.getSimpleName();
	private static volatile ClientService INSTANCE = new ClientService();
	
	private static final String FIXED_ID = "smwts";
	private static final String FIXED_PW = "smwts";
	// ConcurrentHashMap: 멀티스레드 환경에서 안전하게 데이터를 조작할 수 있는 맵 구현체.
	// List 와 비교했을 때 검색속도가 빠르고 CRUD 이후 동시성 문제도 해결됨
	// 다수의 클라이언트 정보를 빈번하게 검색하고 업데이트해야 하는 상황에 추천
	private Map<String, ClientData> clientMap = new ConcurrentHashMap<>();

	

	public static String getFixedId() {
		return FIXED_ID;
	}

	public static String getFixedPw() {
		return FIXED_PW;
	}

	/**
	 * 통신 객체
	 * 
	 * @return
	 */

	public ClientService() {
	}

	public static ClientService getInstance() {
		return INSTANCE;
	}

	/**
	 * 로그인 데이터 고정값과 비교
	 * 
	 * @param id
	 * @param pw
	 * @return
	 */

	public boolean checkLogin(String id, String pw) {
		return FIXED_ID.equals(id) && FIXED_PW.equals(pw);
	}

	/**
	 * 클라이언트 IP 주소
	 * 
	 * @param request
	 * @return
	 * @throws UnknownHostException
	 */

	public String getClientIp(HttpServletRequest request) throws UnknownHostException {
		// Client가 서버에 request를 보낼 때 request 헤더에 "X-Forwarded-For" 키로 IP를 담아 보냄
		String ip = request.getHeader("X-Forwarded-For");

		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("X-Real-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("X-RealIP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("REMOTE_ADDR");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}

		// 로컬에서 접속했을 경우 IPv4 또는 IPv6로 Client IP를 다르게 잡음.
		if (ip.equals("0:0:0:0:0:0:0:1") || ip.equals("127.0.0.1")) {
			InetAddress address = InetAddress.getLocalHost();
			ip = address.getHostAddress();
		}

		return ip;

	}

	/**
	 * client 객체 추가
	 * 
	 * @param clientIpAddress
	 * @param client
	 */

	public void addClientToMap(String clientIpAddress, ClientData client) {
		clientMap.put(clientIpAddress, client);
		LOGCAT.i(TAG, "clientMap 추가됨 --" + clientIpAddress);
	}

	/**
	 * client 객체 리턴
	 * 
	 * @param clientIpAddress
	 * @return
	 */

	public ClientData getClientFromMap(String clientIpAddress) {
		LOGCAT.i(TAG, "get " + clientMap.get(clientIpAddress).toString());
		
		return clientMap.get(clientIpAddress);
	}

	/**
	 * Map 에서 특정 client 객체 제거
	 * 
	 * @param clientIpAddress
	 */

	public void removeClientFromMap(String clientIpAddress) {
		clientMap.remove(clientIpAddress);
		LOGCAT.i(TAG, "remove");
	}

	/**
	 * Map 초기화
	 */

	public void cleanClientMap() {
		clientMap = new ConcurrentHashMap<>();
		LOGCAT.i(TAG, "ClientMap 초기화");
	}

	/**
	 * 세션 유효성 및 sessionKey를 비교합니다.
	 *
	 * @param sessionKey
	 * @param httpServletRequest
	 * @return
	 * @throws UnknownHostException
	 */
	public boolean compareSessionKey(String httpSessionKey, HttpServletRequest httpServletRequest)
	        throws UnknownHostException {
	    String clientIpAddress = getClientIp(httpServletRequest);
	    HttpSession session = httpServletRequest.getSession(false);
	    if(httpSessionKey == null) {
	    	return true;
	    }
	    if (session != null) { 
	        String httpSessionKeyInMap = getClientFromMap(clientIpAddress).getHttpSessionKey();
	        // 세션이 유효하고 Map의 sessionKey 와 일치하면 true 반환
	        return httpSessionKeyInMap.equals(httpSessionKey);
	        
	    } else {
	    	// 세션이 유효하지 않을 경우 Map 에서 해당 client 정보 삭제
	    	removeClientFromMap(clientIpAddress);
	    }

	    return false;
	}
	
	/**
	 * 로그에 찍을 IP
	 */
    public String getIp(HttpServletRequest request) {
    	String clientIp = null;
		try {
			clientIp = ClientService.getInstance().getClientIp(request);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
    	return clientIp;
    }

}
