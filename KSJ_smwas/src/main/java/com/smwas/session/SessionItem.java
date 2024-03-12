
package com.smwas.session;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smwas.comm.CommApi;
import com.smwas.comm.ResponseWorker;
import com.smwas.dbio.RealtimeChe;
import com.smwas.dbio.RealtimeHo;
import com.smwas.header.CommHeader;
import com.smwas.io.SendRealData;
import com.smwas.util.CommonUtil;
import com.smwas.util.LOGCAT;
import com.smwas.util.ReadProperties;
import com.smwas.web.SessionHandler;
import com.smwas.web.SessionHandler.ConnectHandler;
import com.smwas.web.SessionHandler.MessageHandler;

/**
 * 고유 키 값, 웹 소켓, 소켓 채널 정보를 가진 클래스
 */
@Component
public class SessionItem {
	public static final String TAG = SessionItem.class.getSimpleName();
	// endpoint
	SessionHandler endPoint = null; 
	String mKey = "";
	String wsUri = null;
	String wsPort = null;
	// 연결한 websocket List
	private List<WebSocketSession> cSessionList = new ArrayList<>(); 
	/* 클라이언트가 요청한 종목 
	*  String : websocket ID , Map - String */ 
	private Map<String, Map<String, Set<String>>> cGetJmList = new HashMap<>(); 
	
	/* 클라이언트가 요청한 종목 
	*  String : websocket ID , String - "false", "true" */ 
	private Map<String, String> crushList = new HashMap<>();
	
	/* 모든 클라이언트가 요청한 종목 
	*  String : 호가, 체결 List<String> : 호가, 체결, List<String> : 종목 */
	private Map<String, List<String>> allJmCodeList = new HashMap<>(); 
	
	// WAS - Server 연결 상태 확인
	private boolean isSvrConnected = false; 
	private boolean isRushConnected = false;
	// 러시테스트 스케줄러
	private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
//	private Map<WebSocketSession, ScheduledExecutorService> sessionExcuList = new ConcurrentHashMap<>();
	
	/**
	 * 서버 실행하자마자 증권사 웹소켓 연결
	 * 
	 * @param key
	 */
	public void init(String key) {
		ReadProperties readProperties = new ReadProperties(); // 프로퍼티 파일 읽기
		Properties prop = readProperties.readProperties("ops.properties");

		this.mKey = key;
		this.wsUri = prop.getProperty("url_ws"); // 웹소켓 URL: ws://ops.koreainvestment.com
		this.wsPort = prop.getProperty("port_ws"); // 웹소켓 포트번호: 31000(모의)
	}
	 // WebSocketSession을 추가하고 해당 세션에 대한 ScheduledExecutorService를 생성하여 할당
//    public void addWebSocketSession(WebSocketSession session) {
//        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
//        sessionExcuList.put(session, executorService);
//    }
// // WebSocketSession과 해당하는 ScheduledExecutorService를 제거
//    public void removeWebSocketSession(WebSocketSession session) {
//        ScheduledExecutorService executorService = sessionExcuList.remove(session);
//        if (executorService != null) {
//            executorService.shutdown(); // ExecutorService 종료
//        }
//    }
    
	/**
	 * socket id 를 통해 websession 가져오기 ( C - W )
	 * 
	 * @param websocketId
	 * @return
	 */
	public WebSocketSession getWebsocket(String websocketId) {
		for (WebSocketSession websocket : cSessionList) {
			if (websocket.getId().equals(websocketId)) {
				return websocket;
			}
		}
		return null;
	}
	
	/**
	 * socket id 를 통해 websession 가져오기 ( C - W )
	 * 
	 * @param websocketId
	 * @return
	 */
	public Map<String, Set<String>> getJmCode(String websocketId) {
		Map<String, Set<String>> jmList = cGetJmList.get(websocketId);
		if (!jmList.isEmpty()) {
			LOGCAT.i(TAG, "[Client Jmcode List] - " +jmList.toString());
			return jmList;
		}
		return null;
	}
	/**
	 * Rush test 사용중인 ip 가져오기 
	 * 
	 * @param websocketId
	 * @return
	 */
	public String getUseRush() {
		WebSocketSession useSession;
		 // Map 반복문
        for (Map.Entry<String, String> entry : crushList.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if(value.equals("true")) {
            	useSession = getWebsocket(key);
            	String clientIpAddress = "";
            	if(useSession != null) {
            		InetSocketAddress remoteAddress = (InetSocketAddress) useSession.getRemoteAddress();
            		clientIpAddress= remoteAddress.getAddress().getHostAddress();
            		
            	}
            	return clientIpAddress;
            }
            LOGCAT.i(TAG,"Key: " + key + ", Value: " + value);
        }
		return null;
	}
	
	/**
	 * 종목 추가
	 * 
	 * @param websocketId
	 * @param jmType
	 * @param jmcode
	 */
	public boolean addJmCode(String websocketId, String jmType ,String jmcode) {
		Map<String, Set<String>> jmList = cGetJmList.get(websocketId);
		boolean addFlag  = false;
		boolean jmFlag = false; // 전체 종목에 추가 여부 
		// 고객 종목 추가 
		if(jmList != null && !jmList.isEmpty()) {
			if ((jmList.get(jmType) == null || jmList.get(jmType).isEmpty())) {
				Set<String> codelist = new HashSet<>();
				codelist.add(jmcode);
				jmList.put(jmType, codelist);

				LOGCAT.i(TAG, "addJmCode - 기존 데이터에 새로운 타입, 데이터 추가 "+jmList.toString());
			}else {
				jmList.get(jmType).add(jmcode);
				LOGCAT.i(TAG, "addJmCode - 기존 데이터만 추가 "+jmList.toString());
				if(jmList.get(jmType).contains(jmcode)) {
					jmFlag = true;
				}
				addFlag = true;
				
			}
		}else {
			Set<String> codelist = new HashSet<>();
			codelist.add(jmcode);
			jmList.put(jmType, codelist);
			LOGCAT.i(TAG, "addJmCode - 새로 추가 WebsocketID : " + websocketId + " jmList - [ "+ jmList.toString() + " ]");
			
		}
		if(!jmFlag) {
			// 전체 종목 추가
			if(!allJmCodeList.isEmpty() && allJmCodeList.get(jmType) != null) {
				addFlag = !allJmCodeList.get(jmType).contains(jmcode);
				allJmCodeList.get(jmType).add(jmcode);
			}else {
				List<String> addJm = new ArrayList<>();
				addJm.add(jmcode);
				allJmCodeList.put(jmType, addJm);
			}
		}
		return addFlag;
	}
	
	/**
	 * 요청한 종목이 있는 소켓 조회 
	 * socketid 를 통해 websession 가져오기 ( C - W )
	 * 
	 * @param jmType
	 * @param jmCode
	 * @return
	 */
	public List<WebSocketSession> getJmCodeWebsocket(String jmType, String jmCode) {
		List<WebSocketSession> sessionList = new ArrayList<>(); // 요청한 Client Session LIST
		String getWebsocket = "";
		for (WebSocketSession websocket : cSessionList) {
			
			if(cGetJmList.get(websocket.getId()) != null && 
					!cGetJmList.get(websocket.getId()).isEmpty() ) { // 고객이 요청한 데이터가 있을 경우 
//				LOGCAT.i(TAG, "webSocket ID - [ "+websocket.getId() + " ] \n 전체 종목 리스트 - [ "+allJmCodeList.toString()+ " ] \n"
//						+ " 종목 리스트 - [ "+cGetJmList.get(websocket.getId()).toString()+ " ]");
				if(cGetJmList.get(websocket.getId()).get(jmType) != null ) { // 
					if(cGetJmList.get(websocket.getId()).get(jmType).contains(jmCode)) {
						getWebsocket += websocket.getId() + ", ";
						sessionList.add(websocket);
					}
				}
			}
		}
//		LOGCAT.i(TAG, "[Socket with item] - " +getWebsocket);
		return sessionList;
	}

	/**
	 * 웹소켓 연결 & 한투 서버에서 데이터 응답
	 * 
	 * @param key
	 */
	public void connectToServer(String key, boolean reconFlag) {
		try {
			if (this.wsUri == null || wsPort == null) {
				init(key);
			}
			// 한투 서버에서 응답받는 곳
			MessageHandler msgHandle = new SessionHandler.MessageHandler() { // 웹소켓 연결 후 증권사로부터 수신 된 메세지를 처리하는 방식을 정의,
				// onMessege 메서드 구동
				public void handlerMessage(String message) {
					maintainWs(message);
				}
			};

			// 한투 서버 - WAS 서버 연결 확인
			ConnectHandler conHandler = new SessionHandler.ConnectHandler() { // 웹소켓 연결 여부
				public void handlerConnect(String sessionId) {
					if (sessionId == null) {
						isSvrConnected = false;
					} else {
						isSvrConnected = true;
					}
				}
			};
			endPoint = new SessionHandler(new URI(wsUri + ":" + wsPort), msgHandle, conHandler); // 증권사 서버로 연결 시도
			
			// 재연결 시 재 조회 
			if( !(this.allJmCodeList == null || this.allJmCodeList.isEmpty()) && reconFlag ) {
				SendRealData data = new SendRealData();
				data.setHeader(getCommHeader());
				for(Map.Entry<String, List<String>> entry : allJmCodeList.entrySet()){
					String type = entry.getKey();
					List<String> uniqueList = entry.getValue().stream().distinct().collect(Collectors.toList());
					if(uniqueList.size() > 0) {
						for(String jmcode : uniqueList) {
							data.getObjCommInput().put(type,jmcode);
							Map<String, Object> rqData = new HashMap<String, Object>();

							rqData.put("header", data.getHeader());
							Map<String, Object> input = new HashMap<String, Object>();
							input.put("input", data.getObjCommInput());
							rqData.put("body", input);
							String realReqJson = null;
							try {
								realReqJson = new ObjectMapper().writeValueAsString(rqData);
								if (isSvrConnected == true) {
									LOGCAT.i(TAG + " [재접속으로 인해 한투 서버에 보냄] : ", realReqJson);
									endPoint.sendMessage(realReqJson); // 메세지 발송하여 웹소켓 연결 끊기지 않게 함
								}
							} catch (JsonProcessingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}
					}
				}
			}
		} catch (URISyntaxException ex) {
			LOGCAT.i(TAG, "URISyntaxException exception: " + ex.getMessage());
		}
	}
	/**
	 * 한투 서버로 실시간 해더 
	 * 
	 */
	public Map<String, String> getCommHeader() {
		Map<String, String> realHeader = new HashMap<String, String>();
		realHeader.put("approval_key", CommHeader.getApproval_key());
		realHeader.put("custtype", CommHeader.getCusttype());
		realHeader.put("tr_type", "1"); // 1: 등록, 2: 해제
		return realHeader;
	}
	
	/**
	 * 한투 서버로 실시간 데이터 요청
	 * 
	 * @param data
	 * @param websocketId
	 */
	public void sendRealRq(Map<String, Object> data, String websocketId) {
		// data : callrealApi 에서 구조 맞춤
		try {
			LOGCAT.i(TAG + " - sendRealRQ", data.toString() + " Server connect - " + Boolean.toString(isSvrConnected));
			String realReqJson = null;
			if (!data.isEmpty()) {
				realReqJson = new ObjectMapper().writeValueAsString(data);
				if (isSvrConnected == true) {
					LOGCAT.i(TAG + " [한투 서버에 보냄] : ", realReqJson);
					endPoint.sendMessage(realReqJson); // 메세지 발송하여 웹소켓 연결 끊기지 않게 함
				}
			}
			Thread.sleep(1000);
		} catch (JsonProcessingException e) {
			LOGCAT.i(TAG + "[ ERROR ] : ", "실시간 요청 데이터 문제 - [ " + data.toString() + " ]");
		} catch (InterruptedException e) {
			LOGCAT.i(TAG + "Thread ", e.toString());
		}
	}
	
	/**
	 * 클라이언트에게 RushTest 데이터 전송
	 * 
	 * @param cheList
	 * @param hoList
	 * @param websocketId
	 */
	public void sendRush(List<RealtimeChe> cheList, List<RealtimeHo> hoList, String websocketId) {
		if(this.isRushConnected) {
			LOGCAT.i(TAG, "체결 ::" + cheList.size() + " 호가 :: "+hoList.size());
			List<Object> listToSend = new ArrayList<>();
			listToSend.addAll(hoList);
			listToSend.addAll(cheList);
			listToSend.forEach(entity -> {
				try {
					String str = CommonUtil.objectToString(entity);
					synchronized(getWebsocket(websocketId)) {
						getWebsocket(websocketId).sendMessage(new TextMessage(str));
					}
					
				} catch (IOException e) {
					e.printStackTrace();
					LOGCAT.i(TAG, e.toString());
				}
			});
			
		}else {
			scheduler.shutdownNow();
		}
//		if(this.isRushConnected) {
//			scheduler.execute(() -> sendRush(cheList, hoList, websocketId));
//		}else {
//			scheduler.shutdownNow();
//		}
		
	}
	
	/**
	 * RushTest 데이터 1초마다 재전송
	 * 
	 * @param cheList
	 * @param hoList
	 * @param websocketId
	 */
	public void startSendingRush(List<RealtimeChe> cheList, List<RealtimeHo> hoList, String websocketId) {
		if(!isRushConnected) {
			this.isRushConnected = true;
//			if(!this.crushList.get(websocketId).equals("true")) {
				this.crushList.put(websocketId, "true");
//			}
			
			if (!(cheList.size() > 0) && !( hoList.size() > 0 ) ) {
				LOGCAT.w(TAG, "TrCode 를 확인하세요.");
			} else {
				LOGCAT.i(TAG, "Rush Test START :: " + cheList.size());
//				scheduler.execute(() -> sendRush(cheList, hoList, websocketId));
				scheduler.scheduleWithFixedDelay(() -> {
					sendRush(cheList, hoList, websocketId);
				}, 0, 1, TimeUnit.SECONDS);
			}
		}
	}
	
	/**
	 * RushTest 중지
	 */
	public void stopSendingRush(String socketId) {
		this.isRushConnected = false;
		crushList.remove(socketId);
		try {
            // 스레드 풀 종료를 기다림 (모든 작업이 완료될 때까지)
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                // 시간 초과 시 강제 종료
                scheduler.shutdownNow();
                // 남은 작업을 취소하고 대기 중인 작업을 모두 제거
                scheduler.awaitTermination(60, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            // 대기 중에 인터럽트 발생 시 처리
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // 새로운 스레드 풀 생성
       scheduler = Executors.newScheduledThreadPool(5);
       LOGCAT.i(TAG, "Rush Test End");
	}

	/**
	 *  클라이언트에게 응답데이터 전송 OR 웹소켓 연결 무한 대기하도록 PINGPONG 메세지 전송
	 * 
	 * @param message
	 */
	void maintainWs(String message) {
		List<WebSocketSession> mWebSocket = new ArrayList<>();

		char firstStr = message.charAt(0); // 첫 데이터로 JSON 데이터 여부 체크. 0 혹은 1이 아닐 경우 JSON
		
		// 요청에 대한 응답데이터일 때 -> 클라이언트에게 데이터 웹소켓 전송
		if (firstStr == '0') {
			// 암호화 되지 않은 전문 처리
			String[] mData = message.split("\\|");
			String tr_id = mData[1];
			if (mData[0] != null) {
				String jmcode = mData[3].split("\\^")[0];
				mWebSocket = getJmCodeWebsocket(tr_id, jmcode);
			}

			switch (tr_id) {
			case "H0STASP0":	// 실시간 호가
			case "H0STCNT0":	// 실시간 체결
				
				if (mWebSocket != null) {
					message = ResponseWorker.getInstance().responseData(mData[3], tr_id);
					if (message != null) {
						TextMessage resultMessage = new TextMessage(message);
						
							for (int i = 0; i < mWebSocket.size(); i++) {
//								LOGCAT.i(TAG, "[Socket SIZE] - "+mWebSocket.size()+" :: [Message] - "  + message);
								try {
									// 세션이 이미 닫혀있으면 삭제 필요 
									if(mWebSocket.get(i).isOpen()) {
										synchronized(mWebSocket.get(i)) {
											mWebSocket.get(i).sendMessage(resultMessage);
										}
									}else {
										removeSession(mWebSocket.get(i));
									}
								} catch (IOException e) {
									e.printStackTrace();
									LOGCAT.i(TAG, e.toString());	// 에러 로그 쓰기
									removeSession(mWebSocket.get(i));
								}
							}
						
					}
				}
				break;
			default:
				break;
			}
			
		// 요청이 없을 때 웹소켓 무한 대기하도록 PINGPONG 메시지 전송
		} else if (firstStr != '0' && firstStr != '1') {
			JSONParser parser = new JSONParser();
			Object obj;
			JSONObject jsonObj;

			try {
				obj = parser.parse(message); // 한투 서버로 부터 수신한 메세지 JSON 파싱
				jsonObj = (JSONObject) obj;

				JSONObject header = (JSONObject) jsonObj.get("header"); // 위의 파싱 데이터에서 헤더 값 가져옴
				
				String tr_id = header.get("tr_id").toString(); // 헤더 값에서 TR_ID 추출

				if (tr_id.equals("PINGPONG")) { // TR_ID가 "PINGPONG"일 경우
					this.endPoint.sendMessage(message); // 메세지 발송하여 웹소켓 연결 끊기지 않게 함
					
				} else {
					
					if (mWebSocket != null) {
						TextMessage resultMessage = new TextMessage(message);
						for (int i = 0; i < mWebSocket.size(); i++) {
//							mWebSocket.get(i).sendMessage(resultMessage);
							try {
								// 세션이 이미 닫혀있으면 삭제 필요 
								if(mWebSocket.get(i).isOpen()) {
									synchronized(mWebSocket.get(i)) {
										mWebSocket.get(i).sendMessage(resultMessage);
									}
								}else {
									removeSession(mWebSocket.get(i));
								}
							} catch (IOException e) {
								e.printStackTrace();
								LOGCAT.i(TAG, e.toString());	// 에러 로그 쓰기
								removeSession(mWebSocket.get(i));
							}
						}
					}
				}
			} catch (ParseException e) {
				try {
					if(this.cSessionList != null) {
						for (WebSocketSession websocket : this.cSessionList) {
							websocket.close();
						}
					}
				} catch (IOException e1) {
					e1.printStackTrace();
					LOGCAT.i(TAG, e1.toString());	// 에러 로그 쓰기
				}
				mWebSocket = new ArrayList<>();
			} catch (Exception e) {
				try {
					LOGCAT.i(TAG, e.toString());	// 에러 로그 쓰기
					if(this.cSessionList != null) {
						for (WebSocketSession websocket : this.cSessionList) {
							websocket.close();
						}
					}
				} catch (IOException e1) {
					e1.printStackTrace();
					LOGCAT.i(TAG, e1.toString());	// 에러 로그 쓰기
				}
				mWebSocket = new ArrayList<>();
			}
		}
	}
	
	
	/**
	 * 초기 소켓(C-W) 연결 해제
	 * 
	 * @param socket
	 */
	public void removeSession(WebSocketSession socket) {
		// 1. 소켓 연결 해제 변수 선언 
		List<WebSocketSession> setList =  this.cSessionList;
		Map<String, List<String>> jmList =  this.allJmCodeList;
		Map<String,Set<String>> cJm = this.cGetJmList.get(socket.getId());
		
		// 러쉬 테스트 취소 
		if(this.crushList.get(socket.getId()) != null && crushList.get(socket.getId()).equals("true") ) {
			LOGCAT.i(TAG, "RemoveSession 러쉬테스트 종료 ");
			stopSendingRush(socket.getId());
		}
		
		if(cJm != null) {
			// 1-1. 고객이 종목을 등록했을 경우 
			if( !cJm.isEmpty() && setList.size() > 0 && !jmList.isEmpty() ) {
				LOGCAT.i(TAG, "RemoveSession 제거 전 전체 종목 리스트 - [ " + jmList.toString() + " ] 클라이언트 종목 리스트 - [ "+ cJm.toString()+ " ]");
				LOGCAT.i(TAG, "RemoveSession - 1");
				Set<String> cjmlist = new HashSet<>(); // 호가, 체결 여부 종목리스트 
				// 2. 세션 연결 해제 클라이언트의 종목 가져옴 
				for(String jmType : cJm.keySet()) {
					LOGCAT.i(TAG, "RemoveSession - 2 ITEM " + jmType );
					// 종목 타입에 따른 전체 종목 리스트 
					cjmlist = cJm.get(jmType);
					// 문자열과 개수를 저장할 Map 생성
					Map<String, Integer> stringCounts = new HashMap<>();
					
					// 3. 종목이 있을 경우 
					if( cjmlist.size() > 0 && jmList.get(jmType)!= null ) {
						// 4. 전체 종목
						// 4-1. 전체 종목 안에 해당 종목 갯수 파악, 종목 타입은 있는데 종목이 없을 경우 예외처리
						if(jmList.get(jmType).size() > 0) {
							for(String jmcode : jmList.get(jmType)) {
								if(cjmlist.contains(jmcode)) { 
									stringCounts.put(jmcode, stringCounts.getOrDefault(jmcode, 0) + 1);
								}
							}
						}
						// 4-2 전체 종목 안에 해당 종목 갯수 파악 후 유무 예외처리 
						if(stringCounts.size() > 0) {
							for (String str : stringCounts.keySet()) {
								// 해당 종목 코드가 2개보다 적을 경우 
								if(stringCounts.get(str) < 2) {
									jmList.get(jmType).remove(str);
									Map<String,Object> data =  CommApi.getInstance().createCancelReal(jmType, str);
									try {
										// 해당 종목 실시간 끊음
										String realReqJson = new ObjectMapper().writeValueAsString(data);
										this.endPoint.sendMessage(realReqJson);
									} catch (JsonProcessingException e) {
										e.printStackTrace();
									}
								}
								// 해당 종목 코드가 2개보다 많을 경우 전체 종목에서 하나만 제거한다 
								else {
									jmList.get(jmType).remove(str);
								}
							}
						}
						this.allJmCodeList = jmList;
					}
				}
				LOGCAT.i(TAG, "RemoveSession 제거 후 전체 종목 리스트 - [ " + jmList.toString() + " ]");
				// 3. session 종목 제거
				setList.remove(socket);
				this.cSessionList = setList;
				this.cGetJmList.remove(socket.getId());
			}else if(setList.size() > 1) {
				LOGCAT.i(TAG, "소켓 연결 2인 이상");
				setList.remove(socket);
				this.cSessionList = setList;
			}
			// 1-3. 소켓 연결한 클라이언트가 본인 한명일 경우 초기화  
			else {
				LOGCAT.i(TAG, "소켓 연결 1인");
				this.cSessionList = new ArrayList<>();
				this.allJmCodeList = new HashMap<>();
				this.cGetJmList = new HashMap<>();
			}
		}else {
			LOGCAT.i(TAG, "종목이 없을 경우, 소켓 삭제 ");
			setList.remove(socket);
			this.cSessionList = setList;
		}
	}
	
	public boolean isRushConnected() {
		return isRushConnected;
	}

	public void setRushConnected(boolean isRushConnected) {
		this.isRushConnected = isRushConnected;
	}

	/**
	 * 현재 Session이 사용 가능한지 확인합니다. C - W
	 * 
	 * @param websocketId
	 * @return true : 사용 가능, false : 사용 불가능
	 */
	public boolean isEnable(String websocketId) {
		if (this.isSvrConnected) {
			return true;
		}

		return false;
	}

	
	/**
	 * 초기 소켓(C-W) 연결 SET
	 * 
	 * @param socket
	 */
	public void setSession(WebSocketSession socket) {
		List<WebSocketSession> setList = this.cSessionList;
		setList.add(socket);
		this.cSessionList = setList;
		this.cGetJmList.put(socket.getId(), new HashMap<>());
		
	}

	
	/**
	 * 현재 Session이 사용 가능한지 확인합니다. C - W
	 * 
	 * @return true : 사용 가능, false : 사용 불가능
	 */
	public boolean isEnable() {
		if (this.isSvrConnected) {
			return true;
		}

		return false;
	}
	/**
	 * Session ID LIST  return String
	 * 
	 * @return String
	 */
	public String isListIdString() {
		List<String> sessionIdStr = new ArrayList<>();
		if (this.cSessionList.size() > 0 ) {
			for(WebSocketSession websocket : this.cSessionList) {
				sessionIdStr.add(websocket.getId());
			}
			return sessionIdStr.toString();
		}
		
		return "";
	}
	
	

	public List<WebSocketSession> getcSessionList() {
		return cSessionList;
	}

	public void setcSessionList(List<WebSocketSession> cSessionList) {
		this.cSessionList = cSessionList;
	}

	public Map<String, Map<String, Set<String>>> getcGetJmList() {
		return cGetJmList;
	}

	public void setcGetJmList(Map<String, Map<String, Set<String>>> cGetJmList) {
		this.cGetJmList = cGetJmList;
	}

	public Map<String, List<String>> getAllJmCodeList() {
		return allJmCodeList;
	}

	public void setAllJmCodeList(Map<String, List<String>> allJmCodeList) {
		this.allJmCodeList = allJmCodeList;
	}

	public SessionHandler getEndPoint() {
		return endPoint;
	}

	public void setEndPoint(SessionHandler endPoint) {
		this.endPoint = endPoint;
	}

	public boolean isSvrConnected() {
		return isSvrConnected;
	}

	public void setSvrConnected(boolean isSvrConnected) {
		this.isSvrConnected = isSvrConnected;
	}

	public String getKey() {
		return mKey;
	}

	public void setKey(String mKey) {
		this.mKey = mKey;
	}

	public String getWsUri() {
		return wsUri;
	}

	public Map<String, String> getCrushList() {
		return crushList;
	}

	public void setCrushList(Map<String, String> crushList) {
		this.crushList = crushList;
	}

	public void setWsUri(String wsUri) {
		this.wsUri = wsUri;
	}

	public String getWsPort() {
		return wsPort;
	}

	public void setWsPort(String wsPort) {
		this.wsPort = wsPort;
	}

	public String toString() {
		String sessionStr = "";
		if(!this.cSessionList.isEmpty() || this.cSessionList != null) {
			for(WebSocketSession session : this.cSessionList) {
				sessionStr += session.getId() + " ";
			}
		}
		return "SESSION ITEM - [ mWebSocketId : " + sessionStr + "]";
	}
}