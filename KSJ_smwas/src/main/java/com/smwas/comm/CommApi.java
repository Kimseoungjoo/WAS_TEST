package com.smwas.comm;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smwas.header.CommHeader;
import com.smwas.http.Auth;
import com.smwas.io.RQRPData;
import com.smwas.io.ResultTrData;
import com.smwas.io.SendTrData;
import com.smwas.session.SessionItem;
import com.smwas.session.SessionManager;
import com.smwas.tr.ErrorFile;
import com.smwas.tr.TranFile;
import com.smwas.util.CommonUtil;
import com.smwas.util.LOGCAT;
import com.smwas.util.ReadProperties;
import com.smwas.web.ServerWebsocket;

public class CommApi {
	public static final String TAG = CommApi.class.getSimpleName();
	// 통신 객체
	private static volatile CommApi INSTANCE = new CommApi();

	private static Auth Auth = new Auth();
	// 통신 연결 타임아웃
	private static final int SOCKET_TIME_OUT = 2000;
	// 소켓에서 읽을 수 있는 최대 버퍼 사이즈
	public static final int MAX_BUFFER_SIZE = 1024 * 200;
	// 다중 채널 소켓 관련 변수
	private Selector mSelector = null;
	private SocketChannel mSocketChannel = null;

	// 통신 응답 Thread
	private ResponseWorker mResponseWorker;
	// 요청 큐
	private final ArrayBlockingQueue<RQRPData> mRequestQueue = new ArrayBlockingQueue<RQRPData>(50); // MAX_QUEUE_SIZE
	// 응답 큐
	private final ArrayBlockingQueue<ResultTrData> mResponseQueue = new ArrayBlockingQueue<ResultTrData>(50); // MAX_QUEUE_SIZE
	// 요청 가능 상태 플래그
	private AtomicBoolean isRequestable = new AtomicBoolean(false);
	// Send Index 태그
	private AtomicInteger sendIndex = new AtomicInteger(0);

	// 서버 IP(검증계)
	static final String SERVER_IP = "203.109.30.208";
	// 서버 PORT
	static final int SERVER_PORT = 21000;
	// Serversocket
	ServerWebsocket serverSocket = new ServerWebsocket();

	/**
	 * 통신 객체
	 * 
	 * @return
	 */
	private CommApi() {
		// private 생성자
	}

	public static CommApi getInstance() {

		return INSTANCE;
	}

	/*-------------------------------------------(조회 api 요청 START)-----------------------------------------------------*/

	/**
	 * request input 검증 (예외처리)
	 * 
	 * @param data
	 * @return
	 */
	public static Map<String, Object> checkRequest(SendTrData data) {
		
		boolean flag = true;
		if(TranFile.getInstance().getTrId(data.getTrCode()) != null) {
			String url = CommonUtil.getUrlLastSegment(data.getTrCode());
			Map<String, String> inputValueMap = data.getObjCommInput();
			
			for (Map.Entry<String, String> entry : inputValueMap.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				
				
				// 자릿수 체크 & 대문자 변환 
				switch (key) {
				case "FID_COND_MRKT_DIV_CODE" -> {		// J, U
					if (value.length() != 1) {
						flag = false;
						LOGCAT.i(TAG, key + " 길이 다름");
					} else {
						value = value.toUpperCase();	// 대문자 변환
						if (!value.equals("J") && !value.equals("U")) {
							LOGCAT.i(TAG, key + "가 J, U가 아님");
							flag = false;
						} else {
							inputValueMap.put(key, value);
						}
					}
				}
				case "FID_INPUT_ISCD" -> {				// 005930
					if (value.length() > 6) {			// 0001(KODEX200), 201(KOSDAQ - KEPCO)
						flag = false;
						LOGCAT.i(TAG, key + " 길이 다름");
					}
				}
				case "FID_INPUT_DATE_1", "FID_INPUT_DATE_2" -> {	// 20220420, 20220430
					if (value.length() != 8) {
						flag = false;
						LOGCAT.i(TAG, key + " 길이 다름");
					}
				}
				case "FID_PERIOD_DIV_CODE" -> {			// D, W, M
					if (value.length() != 1) {
						flag = false;
						LOGCAT.i(TAG, key + " 길이 다름");
					} else {
						value = value.toUpperCase();	// 대문자 변환
						if (!value.equals("D") && !value.equals("W") && !value.equals("M")) {
							flag = false;
							LOGCAT.i(TAG, key + "가 D, W, M이 아님");
						} else {
							inputValueMap.put(key, value);
						}
					}
				}
				case "FID_ORG_ADJ_PRC" -> {				// 0, 1
					if (value.length() != 1) {
						flag = false;
						LOGCAT.i(TAG, key + " 길이 다름");
					} else {
						if (!value.equals("0") && !value.equals("1")) {
							flag = false;
							LOGCAT.i(TAG, key + "가 0, 1이 아님");
						}
					}
				}
				case "FID_INPUT_HOUR_1" -> {			// HHMMSS
					if (value.length() != 6) {
						flag = false;
						LOGCAT.i(TAG, key + " 길이 다름");
					}
				}
				default -> {
//				flag = false;
					break;
				}
				}
			}
			
			// 날짜? 기간 선후관계 체크
			if (url.equals(TranFile.INQUIRE_DAILY_INDEX) || url.equals(TranFile.INQUIRE_DAILY_ITEM)) {
				String date1 = inputValueMap.get("FID_INPUT_DATE_1");	// 과거여야함
				String date2 = inputValueMap.get("FID_INPUT_DATE_2");	// 미래여야함
				if (date1.length() == 8 && date2.length() == 8) {
					try {
						// 주어진 날짜 문자열을 LocalDate 객체로 변환
						LocalDate localDate1 = LocalDate.parse(date1, java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
						LocalDate localDate2 = LocalDate.parse(date2, java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
						
						// 날짜 비교
						int comparisonResult = localDate1.compareTo(localDate2);
						
						// 양수(미래, 과거), 0(동일), 음수(과거, 미래)
						if (comparisonResult > 0) {
							flag = false;
							LOGCAT.i(TAG, "날짜 선후관계가 맞지 않음");
						}
					} catch (Exception e) {
						flag = false;
						LOGCAT.printStackTrace(e);
						LOGCAT.i(TAG, "날짜 데이터 오류");
					}
				}
			}
			
		}
		
		Map<String, Object> checkingResult = new HashMap<>();
		checkingResult.put("flag", flag);
		checkingResult.put("data", data);
		
		return checkingResult;
	}
	
	
	/**
	 * DB Insert 위해 한투 API 조회
	 * 
	 * @param data
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ResultTrData callApiForDB(SendTrData data) {
		ResultTrData apiResult;

		String url = getUrl(data);
		HttpHeaders headers = createHeaders(data.getHeader()); // tr_id
		String params = createParams(data.getObjCommInput(), url); // FID_INPUT_ISCD 등 인풋값

		// HTTP 요청
		HttpEntity requestEntity = new HttpEntity<>(headers);
//		LOGCAT.i(TAG, "API Http 요청 - " + requestEntity);

		try {
			ResponseEntity<Map> response = new RestTemplate().exchange(params, HttpMethod.GET, requestEntity,
					Map.class);
//			LOGCAT.i(TAG, "짜잔" + response.getBody().get("output"));

			// 통신 결과에 따른 처리
			if (response.getStatusCode().equals(HttpStatus.OK) && "0".equals(response.getBody().get("rt_cd"))) {

//				LOGCAT.i(TAG, "짜잔" + (Map<String, Object>) response.getBody());
				// 형변환
				Map<String, String> stringMap = data.getObjCommInput(); // String 타입의 Map
				Map<String, Object> objectMap = new HashMap<>(); // Object 타입의 Map
				for (Map.Entry<String, String> entry : stringMap.entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();
					Object objectValue = (Object) value;
					objectMap.put(key, objectValue);
				}

				Map<String, Object> outlec = response.getBody();
				Boolean dataFlag = false;
				// 데이터 null 여부
				if (outlec.containsKey("output")) {
					if (outlec.get("output") instanceof ArrayList) {
						List outputList = (List) outlec.get("output");
						if (outputList != null && outputList.isEmpty()) {
							// outputList가 비어 있는 경우 처리
							dataFlag = false;
						} else {
							dataFlag = true;
						}
					} else if (outlec.get("output") instanceof Object) {
						System.out.println("Output list는 Object 자료형입니다.");
						if (outlec.get("output") == null) {
							// outputList가 비어 있는 경우 처리
							dataFlag = false;
						} else {
							dataFlag = true;
						}
					}
				} else if (outlec.containsKey("output1")) {
					if (outlec.get("output1") instanceof ArrayList) {
						List outputList = (List) outlec.get("output1");
						if (outputList != null && outputList.isEmpty()) {
							// outputList가 비어 있는 경우 처리
							dataFlag = false;
						} else {
							dataFlag = true;
						}
					} else if (outlec.get("output1") instanceof Object) {
						System.out.println("Output1 list는 Object 자료형입니다.");
						if (outlec.get("output1") == null) {
							// outputList가 비어 있는 경우 처리
							dataFlag = false;
						} else {
							// inquire-asking-price-exp-ccn URL -
							if (data.getTrCode().contains(TranFile.INQUIRE_ASK_PRICE_EXP)) {
								Map<String, String> test = (Map<String, String>) outlec.get("output1");
								if (test.get("aspr_acpt_hour").equals("000000")) {
									dataFlag = false;
								} else {
									dataFlag = true;
								}
							} else {
								dataFlag = true;
							}
						}
					}
				}

				apiResult = new ResultTrData(data.getTrCode(), response.getStatusCode().toString(), objectMap, outlec,
						dataFlag);

			} else {
				LOGCAT.w(TAG, "response Error: " + response.getStatusCode() + " | " + response.getBody());
				apiResult = new ResultTrData(data.getTrCode(), response.getStatusCode().toString(), null,
						response.getBody(), false);
			}

		} catch (HttpClientErrorException e) {
			// 클라이언트 오류 처리 (예: 4xx 오류)
			LOGCAT.w(TAG, "Client Error - " + e.toString());
			apiResult = new ResultTrData(data.getTrCode(), e.toString(), null, null, false);

		} catch (HttpServerErrorException e) {
			// 서버 오류 처리 (예: 5xx 오류)
			LOGCAT.w(TAG, "Server Error - " + e.toString());
			apiResult = new ResultTrData(data.getTrCode(), e.toString(), null, null, false);

			if (e.getResponseBodyAsString().contains("EGW00123")) {
				// 토큰 만료시 재인증 후 재시도
				Auth.authenticate(e.toString());
				return callApiForDB(data);
			} else if (e.getResponseBodyAsString().contains("EGW00201")) {
				// 초당 건수
				String responseBody = e.getResponseBodyAsString();
				ObjectMapper objectMapper = new ObjectMapper();
				Map<String, Object> responseMap = new HashMap<String, Object>();
				try {
					responseMap = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {
					});
				} catch (JsonMappingException e1) {
					e1.printStackTrace();
				} catch (JsonProcessingException e1) {
					e1.printStackTrace();
				}
				apiResult.setmOutRecMap(responseMap);
				System.out.println("HTTP 상태 코드: " + e.getStatusText());
			}

		} catch (Exception e) {
			// 기타 예외 처리
			LOGCAT.w(TAG, "Error occurred during API call -- " + e.toString());
			apiResult = new ResultTrData(data.getTrCode(), e.toString(), null, null, false);

		}
		return apiResult;

	}

	/**
	 * HTTP 통신 url 가져오기
	 * 
	 * @return
	 */
	public String getUrl(SendTrData data) {
		if (data != null) {
			ReadProperties ops = new ReadProperties(); // ops.properties 의 값을 가져온다.
			Properties prop = ops.readProperties("ops.properties");

			// URL 생성
			String url = String.format("%s:%s%s", prop.getProperty("url_base"), prop.getProperty("uri_base_port"),
					data.getTrCode());

			return url;
		}
		return null;
	}

	/**
	 * 헤더 세팅
	 * 
	 * @param header
	 * @return
	 */
	public static HttpHeaders createHeaders(Map<String, String> header) {

		// header 설정
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("authorization", CommHeader.getAuthorization());
		headers.set("appkey", CommHeader.getAppkey());
		headers.set("appsecret", CommHeader.getAppsecret());
		headers.set("tr_id", header.get("tr_id"));
		headers.set("custtype", CommHeader.getCusttype());

		return headers;
	}

	/**
	 * 파라메터 세팅 (TrBuilder input 값 그대로)
	 * 
	 * @param inputData
	 * @param url
	 * @return
	 */
	public static String createParams(Map<String, String> inputData, String url) {

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);

		// 각 키-값 쌍을 순회하면서 쿼리 파라미터를 추가
		for (Map.Entry<String, String> entry : inputData.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();

			builder.queryParam(key, value);
		}

		return builder.toUriString();
	}

	/*-------------------------------------------(조회 api 요청 END)-----------------------------------------------------*/

	/**
	 * 통신을 연결 합니다.
	 * 
	 * @return
	 */
	public synchronized boolean connectToServer() {
		// WAS 서버(S) - 한투 API 서버 단일 소켓 연결
		mResponseQueue.clear();
		mRequestQueue.clear();
		isRequestable.set(true);

		return true;
	}

	/**
	 * 통신을 연결 합니다.
	 * 
	 * @return
	 */
	public synchronized void connectToServer(SessionItem item) {
		SocketChannel socketChannel = null;

		try {
			socketChannel = SocketChannel.open();
			socketChannel.socket().setReceiveBufferSize(MAX_BUFFER_SIZE);
			socketChannel.socket().setReuseAddress(true);
			socketChannel.socket().setSoLinger(true, 0);
			socketChannel.socket().connect(new InetSocketAddress(SERVER_IP, SERVER_PORT), SOCKET_TIME_OUT);
			socketChannel.configureBlocking(false);
			socketChannel.register(mSelector, SelectionKey.OP_READ, item.getKey());
		} catch (Exception e) {
			socketChannel = null;
			LOGCAT.printStackTrace(e);
		}

	}

	/**
	 * 통신 연결을 끊습니다.
	 * 
	 * @return
	 */
	public synchronized boolean disconnectoToServer() {
		isRequestable.set(false);
		if (mSocketChannel == null)
			return true;

		try {
			mRequestQueue.clear();

			if (mSocketChannel != null) {
				mSocketChannel.close();
				mSocketChannel = null;
			}

			if (mSelector != null) {
				mSelector.close();
				mSelector = null;
			}

			if (mResponseWorker != null) {
				if (!mResponseWorker.isInterrupted()) {
					mResponseWorker.interrupt();
				}
				mResponseWorker = null;
			}

			sendIndex.set(0);
		} catch (Exception e) {
			LOGCAT.printStackTrace(e);
		}

		return true;
	}

	public synchronized boolean isSessionConnect() {
		if (mSocketChannel == null) {
			return false;
		}

		return mSocketChannel.isConnected();
	}

	/*-------------------------------------------(실시간 api 요청 START)-----------------------------------------------------*/

	
	/**
	 * 한투 Server 로 실시간 데이터 요청
	 * 
	 * @param data
	 * @return
	 */
	public boolean callRealAPI(SendTrData data) {

		boolean realFlag = false; // real 통신 flag
		new HashMap<>();
		LOGCAT.i(TAG + " - REAL REQUEST", data.toString());

		String sessionId = data.getHeader().get("sessionKey");
		boolean addFlag = false;
		boolean removeFlag = false;

		if (sessionId != null) {
			// 0. SessionId를 통해 sessionItem 가져오기
			SessionItem item = SessionManager.getInstance().getPubSessionItem(sessionId);
			WebSocketSession getSession = item.getWebsocket(sessionId);
			LOGCAT.i(TAG, "Session List - [ " + SessionManager.getInstance().getSessionString() + " ]");
			if(item.isSvrConnected()) {
				
				if (getSession != null && item.getKey() != null ) {
					// C -W 연결 Session
					
					String tr_id = data.getObjCommInput().get("tr_id");				// 호가, 체결 구분 변수
					String tr_key = data.getObjCommInput().get("tr_key");			// 종목 코드 값 
					// 종목을 등록하는 경우
					if (data.getHeader().get("tr_type").equals("1")) {
						LOGCAT.i(TAG, "Session add Data - [ ID : " + getSession.getId() + " JMCODE : "
								+ tr_key + " ]");
						addFlag = !item.addJmCode(sessionId, tr_id, tr_key);
					} else {
						LOGCAT.i(TAG, "Session remove Data - [ ID : " + getSession.getId() + " JMCODE : "
								+ tr_key + " ]");
						if(item.getAllJmCodeList() != null && item.getcGetJmList().get(sessionId) != null) {
							if(item.getAllJmCodeList().get(tr_id) != null && item.getJmCode(sessionId).get(tr_id) != null ) {
								item.getJmCode(sessionId).get(tr_id).remove(tr_key);
								item.getAllJmCodeList().get(tr_id).remove(tr_key);
								removeFlag = !item.getAllJmCodeList().get(tr_id).contains(tr_key);
							}
						}
					}
					Map<String, Object> rqData = new HashMap<String, Object>();
					
					if (createRealHeader(data) != null && data.getObjCommInput() != null) {
						rqData.put("header", createRealHeader(data));
						Map<String, Object> input = new HashMap<String, Object>();
						input.put("input", data.getObjCommInput());
						rqData.put("body", input);
					}
					if (rqData != null) {
						// RushTest 를 위한 코드 수정
						if (addFlag || removeFlag) {
							item.sendRealRq(rqData, sessionId);
						}
						realFlag = true;
						return realFlag;
					}
				}
			}else {
				// 서버 연결 끊어졌을 경우 
				SessionManager.getInstance().reConnect();
			}
			return realFlag;
		}
		return realFlag;
	}
	

	/**
	 * 리얼 헤더 생성
	 * 
	 * @param data
	 * @return
	 */
	public static Map<String, String> createRealHeader(SendTrData data) {
		Map<String, String> realHeader = new HashMap<String, String>();

		// data 없으면 return
		if (data != null) {
			realHeader.put("approval_key", CommHeader.getApproval_key());
			realHeader.put("custtype", CommHeader.getCusttype());
			realHeader.put("tr_type", data.getHeader().get("tr_type")); // 1: 등록, 2: 해제

			return realHeader;
		} else {

			return null;
		}
	}

	
	/**
	 * 리얼 해제 헤더 생성
	 * 
	 * @param jmType
	 * @param jmCode
	 * @return
	 */
	
	public Map<String, Object> createCancelReal(String jmType, String jmCode) {
		Map<String, Object> rqData = new HashMap<String, Object>();
		Map<String, String> realHeader = new HashMap<String, String>();

		// data 없으면 return
		realHeader.put("approval_key", CommHeader.getApproval_key());
		realHeader.put("custtype", CommHeader.getCusttype());
		realHeader.put("tr_type", "2"); // 1: 등록, 2: 해제

		rqData.put("header", realHeader);

		Map<String, String> objInput = new HashMap<String, String>();
		objInput.put("tr_key", jmCode);
		objInput.put("tr_id", jmType);

		Map<String, Object> input = new HashMap<String, Object>();

		input.put("input", objInput);
		rqData.put("body", input);

		return rqData;
	}

	
	/**
	 * 웹소켓 헤더 설정
	 * 
	 * @param header
	 * @return
	 */
	public static HttpHeaders createWsHeader(Map<String, String> header) {

		String approvalKey = CommHeader.getApproval_key();
		String custType = CommHeader.getCusttype();
		String trType = "1";
//		String contentType = prop.getProperty("contentType");

		HttpHeaders wsHeader = new HttpHeaders();
		wsHeader.set("approval_key", approvalKey); // 웹소켓 접속키
		wsHeader.set("custtype", custType); // 고객타입: 개인(P), 법인(B)
		wsHeader.set("tr_type", trType); // 거래타입: 등록(1), 해제(2)
//		wsHeader.set("content-type", contentType); // 컨텐츠타입: utf-8

		return wsHeader;
	}

	
	/**
	 * 웹소켓 바디 설정
	 * 
	 * @param inputData
	 * @param url
	 * @return
	 */
	public static String createWsBody(Map<String, String> inputData, String url) {
		Map<String, Object> wsBody = new HashMap<>();
		wsBody.put("input", inputData);

		Map<String, Object> wsRequest = new HashMap<>();
		wsRequest.put("body", wsBody);

		String jsonBody = "";

		ObjectMapper mapper = new ObjectMapper();
		try {
			jsonBody = mapper.writeValueAsString(wsRequest);
		} catch (Exception e) {
			throw new RuntimeException("JSON 변환 실패: " + e.getMessage());
		}

		return jsonBody;
	}

	
	/**
	 * data 확인 코드 
	 * 
	 * @return boolean
	 */
	public ResultTrData checkData(SendTrData checkData) {
		Map<String, Object>  response = new HashMap<>(); // response가 null이면 새 HashMap을 생성
		// data 없을 경우 
		if(checkData != null){
			if(checkData.getObjCommInput().get("tr_key") == null || checkData.getObjCommInput().get("tr_key").isEmpty()) {
				response.put("msg_cd", "OPSP9990");
				response.put("msg1", ErrorFile.getInstance().getErrorMsg("OPSP9990"));
				response.put("rt_cd", "1");
			}else {
				response.put("msg_cd", "MCA00000");
				response.put("msg1", ErrorFile.getInstance().getErrorMsg("MCA00000"));
				response.put("rt_cd", "1");
			}
		}else {
			// Input data not found
			response.put("msg_cd", "OPSP9991");
			response.put("msg1", ErrorFile.getInstance().getErrorMsg("OPSP9991"));
			response.put("rt_cd", "1");
		}
		ResultTrData resultData = new ResultTrData(checkData.getTrCode(), null, null, response, false);
		return resultData;
	}
	

}