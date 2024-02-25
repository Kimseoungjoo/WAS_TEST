package com.smwas.web;

import java.net.URI;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import com.smwas.util.LOGCAT;


@ClientEndpoint
public class SessionHandler {
	public static final String TAG = SessionHandler.class.getSimpleName();
	
	Session session = null;
	String sessionId = null;
	private MessageHandler messageHandler;
	private ConnectHandler connectHandler;
	
	
	/**
	 * 웹소켓 서버 연결 시도
	 * @param endpointURI: 웹소켓 연결 서버 주소
	 */
	public SessionHandler(URI endpointURI, MessageHandler mhd, ConnectHandler chd ) {
		try {
			if(mhd.toString() != "" || chd.toString() != "" ) {
				messageHandler = mhd;
				connectHandler = chd;
			}
			WebSocketContainer container = ContainerProvider.getWebSocketContainer();
			container.connectToServer(this, endpointURI);
//			LOGCAT.i(TAG, "# WebSocketHandler # 웹소켓 연결 성공");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}	
	
	
	/**
	 * 웹소켓 연결이 성공했을 때 호출
	 * @param session: 연결된 세션
	 * @return 
	 */
	@OnOpen
	public void onOpen(Session session) {
		LOGCAT.i(TAG, "# on Open # 세션값: " + session);
		this.session = session;
		this.sessionId = this.session.getId();
		if (this.connectHandler != null) {							// 수신 메세지를 처리할 로직이 없더라도 웹소켓 연결이 유지되도록 처리
	        this.connectHandler.handlerConnect(this.sessionId); 			
	    }
	}
	
	
	/**
	 * 웹소켓 연결 종료할 때 호출
	 * @param session: 연결된 세션
	 * @param reason: 연결 종료 원인
	 */
	@OnClose
	public void onClose(Session session, CloseReason reason) {	
		LOGCAT.i(TAG, "# onClose # 웹소켓 연결 종료: " + reason);
		this.session = null;
		if (this.connectHandler != null) {							// 웹소켓 연결 값 'false'로 설정
	        this.connectHandler.handlerConnect(null); 			
	    }
	}
	
	
	/**
	 * 웹소켓 연결 후 서버로부터 메세지를 수신할 때마다 호출
	 * @param message: 서버로부터 수신 된 메세지
	 */
	@OnMessage
	public void onMessage(String message) {
	    if (this.messageHandler != null) {				 // 수신 메세지를 처리할 로직이 없더라도 웹소켓 연결이 유지되도록 처리
	        this.messageHandler.handlerMessage(message); 			
	    }
	}
	
	
	/**
	 * 수신된 메시지를 처리할 MessageHandler를 설정, WebSocketConfig 클래스에서 호출되며 메시지 처리 방식을 정의
	 * @param msgHandler: MessageHandler 내 메서드
	 */
	public void addMessageHandler(MessageHandler msgHandler) {
		this.messageHandler = msgHandler;
	}
	
	/**
	 * 연결 여부를 체크하는 Handler
	 * @param conHandler: ConnectHandler 내 메서드
	 */
	public void addConnectHandler(ConnectHandler conHandler) {
		this.connectHandler = conHandler;
	}
	 
	
    /**
     * 웹소켓 연결 이후 WAS가 서버로 메세지 발신
     * @param message: 서버로 보내는 메세지
     */
    public void sendMessage(String message) {
    	LOGCAT.i(TAG, "# sendMessage # [SEND]: " + message);
        this.session.getAsyncRemote().sendText(message);
    }

    
    /**
     * 메세지 수신 시 처리 될 메서드 정의
     */
    public static interface MessageHandler {
        public void handlerMessage(String message);
    }
    
    
    /**
     * 메세지 수신 시 처리 될 메서드 정의
     */
    public static interface ConnectHandler {
        public void handlerConnect(String sessionId);
    }
}
