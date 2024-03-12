package com.smwas.tr;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.smwas.util.LOGCAT;

public class TranFile {
	public static final String TAG = TranFile.class.getSimpleName();
	private static volatile TranFile INSTANCE = new TranFile();
	private Map<String, String> urlIdMap;
	private JSONArray trCodeMap;
	// DB TABLE / Server(한투) API URL
	public static final String INQUIRE_PRICE = "inquire-price"; // 주식현재가 시세
	public static final String INQUIRE_ASK_PRICE_EXP = "inquire-asking-price-exp-ccn"; // 주식현재가 호가/예상체결
	public static final String INQUIRE_CCNL = "inquire-ccnl"; // 주식현재가 체결
	public static final String INQUIRE_DAILY_INDEX = "inquire-daily-indexchartprice"; // 국내주식업종기간별시세(일/주/월/년)
	public static final String INQUIRE_DAILY_ITEM = "inquire-daily-itemchartprice"; // 국내주식기간별시세(일/주/월/년)
	public static final String INQUIRE_DAILY_PRICE = "inquire-daily-price"; // 주식현재가 일자별
	public static final String INQUIRE_INVESTOR = "inquire-investor"; // 주식현재가 투자자
	public static final String INQUIRE_MEMBER = "inquire-member"; // 주식현재가 회원사
	public static final String INQUIRE_TIME_ITEM = "inquire-time-itemconclusion"; // 주식현재가 당일시간대별체결

	// DB TABLE / Server(한투) REAR URL
	public static final String REAL_CHE = "H0STCNT0"; // 실시간 체결가
	public static final String REAL_HO = "H0STASP0"; // 실시간 호가
	
	
	// Header tr_id
	public static final String INQUIRE_PRICE_ID = "FHKST01010100"; // 주식현재가 시세
	public static final String INQUIRE_ASK_PRICE_EXP_ID = "FHKST01010200"; // 주식현재가 호가/예상체결
	public static final String INQUIRE_CCNL_ID = "FHKST01010300"; // 주식현재가 채결
	public static final String INQUIRE_DAILY_INDEX_ID = "FHKUP03500100"; // 국내주식업종기간별시세(일/주/월/년)
	public static final String INQUIRE_DAILY_ITEM_ID = "FHKST03010100"; // 국내주식기간별시세(일/주/월/년)
	public static final String INQUIRE_DAILY_PRICE_ID = "FHKST01010400"; // 주식현재가 일자별
	public static final String INQUIRE_INVESTOR_ID = "FHKST01010900"; // 주식현재가 투자자
	public static final String INQUIRE_MEMBER_ID = "FHKST01010600"; // 주식현재가 회원사
	public static final String INQUIRE_TIME_ITEM_ID = "FHPST01060000"; // 주식현재가 당일시간대별체결

	// DB CRUD
	public static final String INSERT = "insert"; // CREATE
	public static final String SELECT = "read"; // READ
	public static final String UPDATE = "update"; // UPDATE
	public static final String DELETE = "delete"; // DELETE

	public TranFile() {
		
		// 데이터가 없을 경우에만 조회..
//		if (!urlIdMap.isEmpty() || !this.trCodeMap.isEmpty()) {
			try {
				// Tran URL ID 맵 생성
				urlIdMap = new HashMap<>();
				urlIdMap.put(INQUIRE_PRICE, "FHKST01010100");
				urlIdMap.put(INQUIRE_ASK_PRICE_EXP, "FHKST01010200");
				urlIdMap.put(INQUIRE_CCNL, "FHKST01010300");
				urlIdMap.put(INQUIRE_DAILY_INDEX, "FHKUP03500100");
				urlIdMap.put(INQUIRE_DAILY_ITEM, "FHKST03010100");
				urlIdMap.put(INQUIRE_DAILY_PRICE, "FHKST01010400");
				urlIdMap.put(INQUIRE_INVESTOR, "FHKST01010900");
				urlIdMap.put(INQUIRE_MEMBER, "FHKST01010600");
				urlIdMap.put(INQUIRE_TIME_ITEM, "FHPST01060000");

				// JSON 파일을 리소스 경로에서 읽어오기
				InputStream inputStream = getClass().getClassLoader().getResourceAsStream("demomaster.json");
				// JSON 파일을 Map<String, String> 형태로 변환
				InputStreamReader reader = new InputStreamReader(inputStream);

				// JSONParser를 사용하여 JSON 데이터 파싱
				JSONParser parser = new JSONParser();
				trCodeMap = (JSONArray) parser.parse(reader);

			} catch (Exception e) {
				e.printStackTrace();
			}
//		}

	}

	// tr_id 반환
	public String getTrId(String url) {
		return urlIdMap.get(url);
	}

	// URL 반환
	public String getUrl(String url) {
		LOGCAT.i(TAG, Boolean.toString(urlIdMap.containsKey(url))) ;
		return urlIdMap.containsKey(url) ? url : null;
	}

	// 종목 유무
	public boolean getJmCode(String jmCode) {
		boolean jmCodeFlag = false;
		for (Object obj : trCodeMap) {
			JSONObject jsonObject = (JSONObject) obj;
			if (jsonObject.get("m_sCode").equals(jmCode)) {
				jmCodeFlag = true;
				break;
			}
		}
		return jmCodeFlag;
	}

	public static TranFile getInstance() {

		return INSTANCE;
	}
}
