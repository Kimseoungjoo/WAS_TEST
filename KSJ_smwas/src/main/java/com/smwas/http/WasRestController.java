package com.smwas.http;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smwas.client.ClientData;
import com.smwas.client.ClientService;
import com.smwas.comm.CommApi;
import com.smwas.db.DatabaseService;
import com.smwas.db.RushTestService;
import com.smwas.dbio.RealtimeChe;
import com.smwas.dbio.RealtimeHo;
import com.smwas.io.ResultTrData;
import com.smwas.io.SendTrData;
import com.smwas.monitoring.MonitoringService;
import com.smwas.session.SessionItem;
import com.smwas.session.SessionManager;
import com.smwas.tr.TranFile;
import com.smwas.util.LOGCAT;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("")
public class WasRestController {
	public static final String TAG = WasRestController.class.getSimpleName();

	private ClientService clientService = new ClientService(); // 로그인
	private MonitoringService monitoringService = MonitoringService.getInstance(); // 모니터링

	@Autowired
	private DatabaseService databaseService; // 조회 DB
	@Autowired
	private RushTestService rushtestService; // 실시간 Rush Test DB

	/**
	 * 로그인
	 * 
	 * @param sendData
	 * @param httpServletRequest
	 * @return
	 * @throws JsonMappingException
	 * @throws JsonProcessingException
	 * @throws UnknownHostException
	 */
	@PostMapping("/login")
	public ModelMap login(@RequestBody String sendData, HttpServletRequest httpServletRequest)
			throws JsonMappingException, JsonProcessingException, UnknownHostException {

		// input 값에서 id 와 pw 추출
		ObjectMapper mapper = new ObjectMapper();
		JsonNode data = mapper.readTree(sendData).get("objCommInput");
		String loginID = data.get("id").asText();
		String loginPW = data.get("pw").asText();

		ClientService.getInstance();
		String clientIpAddress = clientService.getClientIp(httpServletRequest);
		String httpSessionKey = null;

		// 고정 id/pw 와 비교
		if (clientService.checkLogin(loginID, loginPW)) {
			HttpSession session = httpServletRequest.getSession(); // session 있으면 가져오고 없으면 session 생성해서 return
			httpSessionKey = session.getId();

			if (session.isNew()) { // 새로 로그인했다면
				// Map 추가
				ClientData client = new ClientData(httpSessionKey, loginID, clientIpAddress);
				clientService.addClientToMap(clientIpAddress, client);

				// session 설정
				session.setAttribute(clientIpAddress, httpSessionKey);
				session.setMaxInactiveInterval(1800); // session 유효 시간 설정 (1800초 = 30분)

				LOGCAT.i(TAG, "clientSession - " + session.getAttribute(clientIpAddress).toString());
			} else {
				clientService.getClientFromMap(clientIpAddress);
			}
		}

		ModelMap map = new ModelMap();
		map.addAttribute("sessionKey", httpSessionKey);

		return map;
	}

	/**
	 * 로그아웃
	 * 
	 * @param httpServletRequest
	 * @throws UnknownHostException
	 */

	@GetMapping("/logout")
	public void Logout(HttpServletRequest httpServletRequest) throws UnknownHostException {
		String clientIpAddress = clientService.getClientIp(httpServletRequest);
		HttpSession session = httpServletRequest.getSession(false);
		String httpSessionKey = session.getId();
		ClientService.getInstance();

		// session 유효성 및 sessionKey 비교
		if (clientService.compareSessionKey(httpSessionKey, httpServletRequest)) {

			clientService.removeClientFromMap(clientIpAddress);
			session.invalidate();
			LOGCAT.i(TAG, "로그아웃되었습니다.");

		} else {
			LOGCAT.i(TAG, "세션이 유효하지 않거나 sessionKey가 일치하지 않습니다.");
		}

	}

	/**
	 * 모니터링 페이지
	 * 
	 * @return
	 */

	@GetMapping("/monitoring")
	public ModelMap getMonitoringInfo() {

		double cpu = monitoringService.getCPUProcess();
		double Heapsize = monitoringService.getHeapSize();
		double usedMemory = monitoringService.getUsedMemory();
		double totalDisk = monitoringService.getDiskTotalSpace();
		double usableDisk = monitoringService.getDiskUsableSpace();
		double usingDisk = totalDisk - usableDisk;
		List<Map<String, String>> sessionList = SessionManager.getInstance().getSessionList();

		ModelMap map = new ModelMap();
		map.addAttribute("cpu", cpu);
		map.addAttribute("Heapsize", Heapsize);
		map.addAttribute("usedMemory", usedMemory);
		map.addAttribute("usingDisk", usingDisk);
		map.addAttribute("usableDisk", usableDisk);
		map.addAttribute("sessionList", sessionList);
		return map;
	}

	/**
	 * 조회 API (client -> DB -(없으면)-> Server)
	 * 
	 * @param sendData
	 * @param request
	 * @return
	 * @throws IOException
	 */

	@PostMapping("/request")
	public ResponseEntity<String> DbRequest(@RequestBody String sendData, HttpServletRequest request)
			throws IOException {

		String clientIp = request.getRemoteAddr(); // 요청한 IP 가져오기
		LOGCAT.i(TAG, "[REQUEST] [요청IP:" + clientIp + "]" + "\n" + sendData);

		try {
			ObjectMapper mapper = new ObjectMapper();
			SendTrData data = mapper.readValue(sendData, SendTrData.class);

			// Input 검증
			Map<String, Object> inputCheckResult = CommApi.checkRequest(data);
			boolean flag = (boolean) inputCheckResult.get("flag");

			if (flag) { // input 검증 완료
				// DB 조회
				ResultTrData dbResult = databaseService.selectDB(data);
				String result = mapper.writeValueAsString(dbResult); // Java Object -> JSONString 으로 직렬화

				// DB가 비어있으면
				if (dbResult.getOutRecMap().get("msg_cd").equals("EGW00101")) {
					// 한투 API 요청
					ResultTrData apiResult = CommApi.getInstance().callApiForDB(data);
					String msg_cd = (String) apiResult.getOutRecMap().get("msg_cd");

					// 요청 결과 DB 저장
					if (apiResult.getHeaders() != null && msg_cd.equals("MCA00000") && apiResult.getOutRecMap() != null
							&& apiResult.getDataFlag()) {
						if (databaseService.saveDB(apiResult)) {
							LOGCAT.i(TAG, "INSERT SUCCEED");
						} else {
							LOGCAT.i(TAG, "INSERT FAILED");
						}
						result = mapper.writeValueAsString(apiResult);
						return ResponseEntity.status(HttpStatus.OK).body(result);
					} else {
						LOGCAT.i(TAG, "한투에 없음 - " + result);
						result = mapper.writeValueAsString(apiResult);
						return ResponseEntity.status(500).body(result);
					}

				} else {
					LOGCAT.i(TAG, "DB - result  : " + result);
					return ResponseEntity.status(HttpStatus.OK).body(result);
				}
			} else { // input 부적절
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Input값을 확인하세요.");
			}

		} catch (Exception e) {
			LOGCAT.printStackTrace(e);
			LOGCAT.s(TAG, e.toString()); // 에러 로그 쓰기
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("error");
		}
	}

	/**
	 * 조회 API (WAS -> Server -> DB)
	 * 
	 * @param sendData
	 * @param request
	 */

	@PostMapping("/stacking")
	public String requestForStacking(@RequestBody String sendData, HttpServletRequest request) {

		String clientIp = request.getRemoteAddr(); // 요청한 IP 가져오기
		LOGCAT.i(TAG, "[REQUEST] [요청IP:" + clientIp + "]" + "\n" + sendData);

		try {
			ObjectMapper mapper = new ObjectMapper();
			SendTrData data = mapper.readValue(sendData, SendTrData.class); // JSONString -> Java Object 로 역직렬화 (set)
			data.setmClientIP(clientIp);

			// 한투 API 요청
			ResultTrData apiResult = CommApi.getInstance().callApiForDB(data);
			String msg_cd = null;
			if (apiResult.getOutRecMap() != null) {
				msg_cd = (String) apiResult.getOutRecMap().get("msg_cd");
			}

			// 요청 결과 DB 저장
			if (apiResult.getHeaders() != null && msg_cd.equals("MCA00000") && apiResult.getDataFlag()) {
				if (databaseService.saveDB(apiResult)) {
					LOGCAT.i(TAG, "INSERT SUCCEED");
				} else {
					LOGCAT.i(TAG, "INSERT FAILED");
				}
			} else {
				LOGCAT.i(TAG, "INSERT FAILED");
			}

			// apipage로 결과 리턴
			String result = mapper.writeValueAsString(apiResult);
			return result;
		} catch (Exception e) {
			LOGCAT.printStackTrace(e);
			LOGCAT.s(TAG, e.toString()); // 에러 로그 쓰기
			return null;
		}
	}

	/**
	 * 실시간 조회 API (client -> 한투 -> client)
	 * 
	 * @param sendWsData
	 * @return
	 */

	@PostMapping("/requestReal")
	public ResponseEntity<String> RealRequest(@RequestBody String sendWsData) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			SendTrData wsData = mapper.readValue(sendWsData, SendTrData.class);
			ResultTrData checkMsg = CommApi.getInstance().checkData(wsData);
			// data 여부 
			if(checkMsg.getOutRecMap().get("msg_cd").equals("MCA00000")) {
				if (CommApi.getInstance().callRealAPI(wsData)) {
					return ResponseEntity.status(HttpStatus.OK).body(sendWsData);
				} else {
					LOGCAT.i(TAG, "WAS - 한투 서버 연결 이상 / Data error [ " + sendWsData + " ]");
					return ResponseEntity.status(400).body(sendWsData);
				}
			}else {
				String result = mapper.writeValueAsString(checkMsg);
				return ResponseEntity.status(400).body(result);
			}

		} catch (Exception e) {
			LOGCAT.printStackTrace(e);
			return ResponseEntity.status(400).body(e.toString());
		}
	}

	/**
	 * 러시테스트
	 * 
	 * @param sendWsData
	 * @return
	 */
	@PostMapping("/rushtest")
	public ResponseEntity<String> Rushtest(@RequestBody String sendWsData) {

		try {
			LOGCAT.i(TAG, "DATA - [ " + sendWsData + " ]");
			ObjectMapper mapper = new ObjectMapper();
			SendTrData wsData = mapper.readValue(sendWsData, SendTrData.class);

			String trCode = wsData.getObjCommInput().get("tr_id");
			String trType = wsData.getHeader().get("tr_type");
			String websocketId = wsData.getHeader().get("sessionKey");

			List<RealtimeChe> cheList = new ArrayList<>();
			List<RealtimeHo> hoList = new ArrayList<>();
			LOGCAT.i(TAG, "DATA - [ " + cheList.toString() + " ] + [ " + hoList.toString() + " ]");
			// DB 조회
			switch (trCode) {
			case TranFile.REAL_CHE -> cheList = rushtestService.selectAllDbChe();
			case TranFile.REAL_HO -> hoList = rushtestService.selectAllDbHo();
			default -> {
				break;
			}
			}

			// 소켓으로 전송
			SessionItem item = SessionManager.getInstance().getPubSessionItem(websocketId);
			LOGCAT.i(TAG, "Rush item - " + Boolean.toString(item.isRushConnected()) + "  ID - " + item.getCrushList().get(websocketId));
			if (!item.isRushConnected() || item.getCrushList().get(websocketId).equals("true")) {
				if (trType.equals("1")) {
					item.startSendingRush(cheList, hoList, websocketId);
				} else {
					item.stopSendingRush(websocketId);
				}
			} else {
				String ip = item.getUseRush();
				if( ip != null) {
					return ResponseEntity.status(400).body("RushTest( "+ip+" )를 이미 진행중입니다.");
				}
				return ResponseEntity.status(400).body("RushTest를 이미 진행중입니다.");
			}

		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			LOGCAT.printStackTrace(e);
			return ResponseEntity.status(400).body(e.toString());
		}

		return ResponseEntity.status(HttpStatus.OK).body(sendWsData);

	}

	/**
	 * 실시간 조회 데이터 DB 적재 (for rush test)
	 * 
	 * @param realData
	 */

	@PostMapping("/stackingForRush")
	public void stackingForRushTest(@RequestBody String realData) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			ResultTrData data = mapper.readValue(realData, ResultTrData.class);
			if (rushtestService.saveDB(data)) {
				LOGCAT.i(TAG, "DB Insert Success ~~~~~~~~~");
			} else {
				LOGCAT.i(TAG, "DB Insert Fail");
			}

		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
