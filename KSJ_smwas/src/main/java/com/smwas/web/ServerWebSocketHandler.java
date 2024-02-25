package com.smwas.web;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smwas.io.ResultTrData;
import com.smwas.session.SessionManager;
import com.smwas.util.LOGCAT;

@Component
public class ServerWebSocketHandler extends TextWebSocketHandler{
	private static String sendSessionInfoTR = "/getSessionInfo";
	
	public static final String TAG = ServerWebSocketHandler.class.getSimpleName();

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		LOGCAT.i(TAG, "데이터 연결 ");
		SessionManager.getInstance().getSessionItemById(session);
	}
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		
		LOGCAT.i(TAG, "클라이언트 접속");
		InetSocketAddress remoteAddress = (InetSocketAddress) session.getRemoteAddress();
		String ipAddress = remoteAddress.getAddress().getHostAddress();
		LOGCAT.i(TAG, "클라이언트 IP - [ "+ipAddress + " ] SESSION INFO - "+ session.toString());
		sendSessionkeyToClient(session);
		// Session 연결
		SessionManager.getInstance().connectToSession(session);
	}
	
	// Client session 접속 시 session key 반환 
	private void sendSessionkeyToClient(WebSocketSession session) {
		ResultTrData resultRealData = new ResultTrData();
		resultRealData.setTrCode(sendSessionInfoTR);
		resultRealData.getOutRecMap().put("type", "websocketkey");
		resultRealData.getOutRecMap().put("websocketkey", session.getId());
		
		// JSON 변환
        ObjectMapper objectMapper = new ObjectMapper();
        
        try {
            String json = objectMapper.writeValueAsString(resultRealData);
            LOGCAT.i(TAG + " - Socket Info", json);
            TextMessage textMessage = new TextMessage(json);
            try {
				session.sendMessage(textMessage);
			} catch (IOException e) {
				e.printStackTrace();
			}
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
		
		
	}
	
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		InetSocketAddress remoteAddress = (InetSocketAddress) session.getRemoteAddress();
		String ipAddress = remoteAddress.getAddress().getHostAddress();
		LOGCAT.i(TAG, "클라이언트 IP - [ "+ipAddress + " ] 클라이언트 해제");

		// Session 연결 해제
		SessionManager.getInstance().disconnectToSession(session);
	}
	
}
