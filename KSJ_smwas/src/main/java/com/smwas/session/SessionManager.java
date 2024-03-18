package com.smwas.session;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.smwas.util.LOGCAT;
import com.smwas.web.SessionHandler;

public class SessionManager {
	private static final String TAG = SessionManager.class.getSimpleName();
	// 웹 소켓용 Session 개수
	private static final int SESSION_CAPACITY = 1;
	// HTTP용 Session 개수
//	private static final int EXTRA_CAPACITY = 1;
	// SessionManager 객체
	private static volatile SessionManager INSTANCE;
	// Session 리스트
	List<SessionItem> mSessionList = new ArrayList<SessionItem>();
	// Session Index
	private AtomicInteger mSessionIndex = new AtomicInteger(0);
	// Extra Index
	private AtomicInteger mExtraIndex = new AtomicInteger(0);
	// 중복 제거 종목 리스트 
	private List<String> jmList = new ArrayList<String>();
	
	/**
	 * SessionManager 객체
	 * 소켓 하나 수정완 
	 * @return
	 */
	public static SessionManager getInstance() {
		if (INSTANCE == null) {
			synchronized (SessionManager.class) {
				if (INSTANCE == null) {
					INSTANCE = new SessionManager();
				}
			}
		}
		return INSTANCE;
	}
	/**
	 * W - S 재연결 
	 * Sessionmanager .
	 */
	public void reConnect() {
		initializedSession();
//		if (!mSessionList.isEmpty()) {
//			SessionItem item = mSessionList.get(0);
//			String key = item.getKey();
//			
//			item.setcSessionList(new ArrayList<>());
//			item.setcGetJmList(new HashMap<>());
//			item.setCrushList(new HashMap<>());
//			item.setAllJmCodeList(new HashMap<>());
//			
//			item.init(key);
//			item.connectToServer(key,true);
//			LOGCAT.i(TAG, "통신 재연결");
//		}
	}
	/**
	 * SessionManager를 초기화한다.
	 * 소켓 하나 수정완 
	 */
	public void initializedSession() {
		mSessionList.removeAll(mSessionList);
		mSessionIndex.set(0);
		mExtraIndex.set(0);
		
		// Session item 생성 
		for (int index = 0; index < SESSION_CAPACITY; index++) {
			String key = TAG + "_" + (index + 1);
			SessionItem item = new SessionItem();

			item.init(key);
			// 통신 연결
			item.connectToServer(key,false);

			// Session 리스트에 넣기
			mSessionList.add(item);
			LOGCAT.i(TAG, "통신 초기화 작업 (" + key + " / " + (SESSION_CAPACITY ) + ")");
		}

		LOGCAT.i(TAG, "통신 초기화 작업 완료");
	}
	
	/**
	 * C - W 요청 종목 추가 
	 * 종목 추가 
	 * @param jmCode : C - W  요청 종목 
	 * 소켓 하나 수정완 
	 */
	public void addJmCode(String jmCode) {
		if(jmCode != null) {
			jmList.add(jmCode);
			// 중복 제거 
			HashSet<String> set = new HashSet<>(jmList);
			jmList = new ArrayList<>(set);
		}
	}
	
	/**
	 * C - W 요청 종목 제거 
	 * 종목 추가 
	 * @param jmCode : C - W  요청 종목 
	 * 소켓 하나 수정완 
	 */
	public void delJmCode(String jmCode) {
		
		if(jmCode != null) {
			jmList.add(jmCode);
			// 중복 제거 
			HashSet<String> set = new HashSet<>(jmList);
			jmList = new ArrayList<>(set);
		}
		
	}
	
	/**
	 * WebSocket과 Session을 연결한다. -> C - W
	 * 소켓 하나 수정완 
	 * @param webSocket
	 */
	public void connectToSession(WebSocketSession webSocket) {
		SessionItem item = getSessionItem();

		if (item != null) {
			// Client 연결 session 추가 
			item.getcSessionList().add(webSocket);
			item.getcGetJmList().put(webSocket.getId(), new HashMap<>());
			item.getCrushList().put(webSocket.getId(), "false");
			LOGCAT.i(TAG, "Session 연결된 클라이언트 리스트 - " + item.getcSessionList().toString());
			LOGCAT.i(TAG, "Session 연결 완료 - " + item.getKey());
		} else {
			try {
				TextMessage errorMessage = new TextMessage("사용 가능한 Session이 없습니다.");
				webSocket.sendMessage(errorMessage);
				LOGCAT.i(TAG, "Session 연결 실패");
			} catch (IOException e) {
				LOGCAT.printStackTrace(e);
			}
		}
	}

	/**
	 * WebSocket과 Session의 연결을 해제한다. -> C - W 세션 제거
	 * 소켓 하나 수정완 
	 * @param webSocket
	 */
	public void disconnectToSession(WebSocketSession webSocket) {
		SessionItem item = getSessionItemById(webSocket);
		
		if (item.getcSessionList() != null) {
			// 통신 연결 해제
			LOGCAT.i(TAG, "Session 연결 해제 완료 - " + webSocket.getId());
			new TextMessage("Session 연결 해제.");
			item.removeSession(webSocket);

		} else {
			try {
				TextMessage errorMessage = new TextMessage("연결된 Session이 없습니다.");
				webSocket.sendMessage(errorMessage);
				LOGCAT.i(TAG, "Session 연결 해제 실패");
			} catch (IOException e) {
				LOGCAT.printStackTrace(e);
			}
		}
	}

	/**
	 * 사용 가능한 Session Item을 가져온다. session : W - S 세션
	 * 소켓 하나 수정완 
	 * @return
	 */
	private SessionItem getSessionItem() {
		
		// Session list 데이터 확인 
		if (mSessionList.isEmpty()) {
			return null;
		}
		else {
			SessionItem item = mSessionList.get(0);
			return item;
		}
	}

	
	/**
	 * 사용 가능한 Session Item을 가져온다.
	 * 
	 * @return
	 */
	public SessionItem getPubSessionItem(String mWebsocketId) {
		if (mSessionList.isEmpty()) {
			return null;
		}else {
			SessionItem item = mSessionList.get(0);
			return item;
		}
		
	}
	
	
	/**
	 * WebSocket과 SessionItem을 가져온다.
	 * 
	 * @param webSocket
	 * @return
	 */
	public SessionItem getSessionItemById(WebSocketSession webSocket) {
		if (mSessionList.isEmpty()) {
			return null;
		}
		else {
			SessionItem item = mSessionList.get(0);
			return item;
		}
	}

	/**
	 * WebSocket과 SessionItem을 가져온다.
	 * 
	 * @param webSocket
	 * @return
	 */
	public SessionItem getSessionItemById(String mWebSocketId) {
		if (mSessionList.isEmpty()) {
			return null;
		}else {
			SessionItem item = mSessionList.get(0);
			return item;
		}
	}

	/**
	 * monitoring 세션 리스트 조회 
	 * 
	 * @return
	 */
	public Map<String, Object> getSessionList() {
		int connection = 0;
		List<String> ipList = new ArrayList<>();

		if (!mSessionList.isEmpty()) {
			connection = 1;
			SessionItem item = getSessionItem();
			List<WebSocketSession> cSessionList = item.getcSessionList();
			for (WebSocketSession wsItem : cSessionList) {
				InetSocketAddress remoteAddress = (InetSocketAddress) wsItem.getRemoteAddress();
				String ipAddress = remoteAddress.getHostString(); // IP 주소 추출
				ipList.add(ipAddress);
			}

		}
		Map<String, Object> result = new HashMap<>();
		result.put("connection", connection);
		result.put("ipList", ipList);

		return result;
	}
	/**
	 * Session list 목록 to string
	 *  
	 */
	public String getSessionString() {
		if (mSessionList.isEmpty()) {
			return null;
		}
		LOGCAT.i(TAG, mSessionList.get(0).toString());
		
		
		return mSessionList.get(0).toString();
	}
	
	
}
