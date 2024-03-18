package com.smwas.db;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.smwas.comm.CommApi;
import com.smwas.dbio.DailyIndex;
import com.smwas.dbio.DailyItem;
import com.smwas.dbio.DailyPrice;
import com.smwas.dbio.InquireAskPrice;
import com.smwas.dbio.InquireCcnl;
import com.smwas.dbio.InquireInvestor;
import com.smwas.dbio.InquireItemChart;
import com.smwas.dbio.InquireMember;
import com.smwas.dbio.InquirePrice;
import com.smwas.dbio.PK_chart;
import com.smwas.dbio.PK_dailyIndex;
import com.smwas.dbio.PK_dailyItem;
import com.smwas.dbio.PK_dailyPrice;
import com.smwas.dbio.PK_inquire;
import com.smwas.dbio.PK_time;
import com.smwas.dbio.TimeItem;
import com.smwas.dbrepo.DailyIndexRepository;
import com.smwas.dbrepo.DailyItemRepository;
import com.smwas.dbrepo.DailyPriceRepository;
import com.smwas.dbrepo.InquireAskPriceRepository;
import com.smwas.dbrepo.InquireCcnlRepository;
import com.smwas.dbrepo.InquireInvestorRepository;
import com.smwas.dbrepo.InquireItemChartRepository;
import com.smwas.dbrepo.InquireMemberRepository;
import com.smwas.dbrepo.InquirePriceRepository;
import com.smwas.dbrepo.TimeItemRepository;
import com.smwas.io.ResultTrData;
import com.smwas.io.SendTrData;
import com.smwas.tr.ErrorFile;
import com.smwas.tr.TranFile;
import com.smwas.util.CommonUtil;
import com.smwas.util.LOGCAT;

import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;

/**
 * 실제 DB 상호작용 담당 하나의 서비스 단위 commit, rollback 되어야할 기능들 정의
 */
@Service
@EnableJpaRepositories("com.smwas.dbrepo")
@Transactional(readOnly = true) // 별도의 설정이 없을 경우, readOnly 로 동작됨
public class DatabaseService {
	
	@Autowired
	private InquirePriceRepository inquirePriceRepository;
	@Autowired
	private InquireMemberRepository inquireMemberRepository;
	@Autowired
	private InquireInvestorRepository inquireInvestorRepository;
	@Autowired
	private InquireCcnlRepository inquireCcnlRepository;
	@Autowired
	private InquireAskPriceRepository inquireAskPriceRepository;
	@Autowired
	private DailyIndexRepository dailyIndexRepository;
	@Autowired
	private DailyItemRepository dailyItemRepository;
	@Autowired
	private DailyPriceRepository dailyPriceRepository;
	@Autowired
	private TimeItemRepository timeItemRepository;
	@Autowired
	private InquireItemChartRepository inquireChartRepository;
	
//	private final InquirePriceRepository inquirePriceRepository;
//    private final InquireMemberRepository inquireMemberRepository;
//    private final InquireInvestorRepository inquireInvestorRepository;
//    private final InquireCcnlRepository inquireCcnlRepository;
//    private final InquireAskPriceRepository inquireAskPriceRepository;
//    private final DailyIndexRepository dailyIndexRepository;
//    private final DailyItemRepository dailyItemRepository;
//    private final DailyPriceRepository dailyPriceRepository;
//    private final TimeItemRepository timeItemRepository;
//    private final InquireItemChartRepository inquireChartRepository;
//    @Autowired
//    public DatabaseService(
//            InquirePriceRepository inquirePriceRepository,
//            InquireMemberRepository inquireMemberRepository,
//            InquireInvestorRepository inquireInvestorRepository,
//            InquireCcnlRepository inquireCcnlRepository,
//            InquireAskPriceRepository inquireAskPriceRepository,
//            DailyIndexRepository dailyIndexRepository,
//            DailyItemRepository dailyItemRepository,
//            DailyPriceRepository dailyPriceRepository,
//            TimeItemRepository timeItemRepository,
//            InquireItemChartRepository inquireChartRepository) {
//        this.inquirePriceRepository = inquirePriceRepository;
//        this.inquireMemberRepository = inquireMemberRepository;
//        this.inquireInvestorRepository = inquireInvestorRepository;
//        this.inquireCcnlRepository = inquireCcnlRepository;
//        this.inquireAskPriceRepository = inquireAskPriceRepository;
//        this.dailyIndexRepository = dailyIndexRepository;
//        this.dailyItemRepository = dailyItemRepository;
//        this.dailyPriceRepository = dailyPriceRepository;
//        this.timeItemRepository = timeItemRepository;
//        this.inquireChartRepository = inquireChartRepository;
//    }
	
	public static final String TAG = DatabaseService.class.getSimpleName();
	
	PK_inquire pk_inquire;
	PK_dailyIndex pk_dailyIndex;
	PK_dailyItem pk_dailyItem;
	PK_dailyPrice pk_dailyPrice;
	PK_time pk_time;
	PK_chart pk_chart;

	String mrktType = "";
	String jmCode = "";
	String output = "";
	String date1 = "";
	String date2 = "";
	String periodCode = "";
	String orgAdj = "";
	String output1 = "";
	String output2 = "";
	String hour = "";

	/**
	 * INSERT or UPDATE
	 * 
	 * @param (ResultTrData)apiResult
	 * @throws JsonProcessingException
	 */
	@Transactional
	public boolean saveDB(ResultTrData apiResult) throws JsonProcessingException {

		boolean flag = true;

		String url = CommonUtil.getUrlLastSegment(apiResult.getTrCode());
		Map<String, Object> header = apiResult.getHeaders();
		Map<String, Object> outrec = apiResult.getOutRecMap();

		mrktType = (String) header.get("FID_COND_MRKT_DIV_CODE");
		jmCode = (String) header.get("FID_INPUT_ISCD");

		switch (url) {
		case TranFile.INQUIRE_PRICE -> {
			output = CommonUtil.objectToString((Object) outrec.get("output"));
			InquirePrice row1 = new InquirePrice(mrktType, jmCode, output);
			inquirePriceRepository.save(row1);
		}
		case TranFile.INQUIRE_MEMBER -> {
			output = CommonUtil.objectToString((Object) outrec.get("output"));
			InquireMember row2 = new InquireMember(mrktType, jmCode, output);
			inquireMemberRepository.save(row2);
		}
		case TranFile.INQUIRE_INVESTOR -> {
			output = CommonUtil.objectToString((Object) outrec.get("output"));
			InquireInvestor row3 = new InquireInvestor(mrktType, jmCode, output);
			inquireInvestorRepository.save(row3);
		}
		case TranFile.INQUIRE_CCNL -> {
			output = CommonUtil.objectToString((Object) outrec.get("output"));
			InquireCcnl row4 = new InquireCcnl(mrktType, jmCode, output);
			inquireCcnlRepository.save(row4);
		}
		case TranFile.INQUIRE_ASK_PRICE_EXP -> {
			output1 = CommonUtil.objectToString((Object) outrec.get("output1"));
			output2 = CommonUtil.objectToString((Object) outrec.get("output2"));
			InquireAskPrice row5 = new InquireAskPrice(mrktType, jmCode, output1, output2);
			Map<String, String> checkOutput1 =  (Map<String,String>) outrec.get("output1");
//			if(checkOutput1.get("askp1").equals("0") && checkOutput1.get("bidp1").equals("0")) {
//				LOGCAT.i(TAG, "["+url+"] - 장시간이 아닙니다.");
//				flag = false;
//			}else {
//				LOGCAT.i(TAG, "["+url+"] - 장시간 입니다.");
				inquireAskPriceRepository.save(row5);
//			}
		}
		case TranFile.INQUIRE_DAILY_INDEX -> {
			date1 = (String) header.get("FID_INPUT_DATE_1");
			date2 = (String) header.get("FID_INPUT_DATE_2");
			periodCode = (String) header.get("FID_PERIOD_DIV_CODE");
			output1 = CommonUtil.objectToString((Object) outrec.get("output1"));
			output2 = CommonUtil.objectToString((Object) outrec.get("output2"));
			DailyIndex row6 = new DailyIndex(mrktType, jmCode, date1, date2, periodCode, output1, output2);
			dailyIndexRepository.save(row6);
		}
		case TranFile.INQUIRE_DAILY_ITEM -> {
			date1 = (String) header.get("FID_INPUT_DATE_1");
			date2 = (String) header.get("FID_INPUT_DATE_2");
			periodCode = (String) header.get("FID_PERIOD_DIV_CODE");
			orgAdj = (String) header.get("FID_ORG_ADJ_PRC");
			
			
			output1 = CommonUtil.objectToString((Object) outrec.get("output1"));
			output2 = CommonUtil.objectToString((Object) outrec.get("output2"));
			
			if (outrec.get("output2") instanceof List  && periodCode.equals("D")) {
				LOGCAT.i(TAG, "[DB SAVE] - " +jmCode + " OUTPUT - [ "+ output2.toString() + " ] " );
				List<Map<String, String>> outrecList =  (List<Map<String, String>>)outrec.get("output2");
				if( outrecList.size() > 0) {
					LOGCAT.i(TAG, "[ chart Data SAVE DB (inquire_itemchartprice)]");
					for (Map<String, String> outputObj : outrecList) {
						String chartData = CommonUtil.objectToString(outputObj);
						String stck_bsop_date = outputObj.get("stck_bsop_date");
						if(!outputObj.isEmpty()) {
							InquireItemChart row10 = new InquireItemChart(jmCode, stck_bsop_date, chartData);
							inquireChartRepository.save(row10);
						}
						// TODO:: 데이터 여부에 따라 처리 .
//					else {
//						InquireItemChart row10 = new InquireItemChart(jmCode, date1, output1);
//						inquireChartRepository.save(row10);
//					}
					}
					
				}
			} else {
				InquireItemChart row10 = new InquireItemChart(jmCode, date1, output1);
				inquireChartRepository.save(row10);
			}

			DailyItem row7 = new DailyItem(mrktType, jmCode, date1, date2, periodCode, orgAdj, output1, output2);
			dailyItemRepository.save(row7);
		}
		case TranFile.INQUIRE_DAILY_PRICE -> {
			periodCode = (String) header.get("FID_PERIOD_DIV_CODE");
			orgAdj = (String) header.get("FID_ORG_ADJ_PRC");
			output = CommonUtil.objectToString((Object) outrec.get("output"));
			DailyPrice row8 = new DailyPrice(mrktType, jmCode, periodCode, orgAdj, output);
			dailyPriceRepository.save(row8);
		}
		case TranFile.INQUIRE_TIME_ITEM -> {
			hour = (String) header.get("FID_INPUT_HOUR_1");
			output1 = CommonUtil.objectToString((Object) outrec.get("output1"));
			output2 = CommonUtil.objectToString((Object) outrec.get("output2"));
			TimeItem row9 = new TimeItem(mrktType, jmCode, hour, output1, output2);
			timeItemRepository.save(row9);
		}
		default -> {
			flag = false;
			break;
		}
		}

		return flag;

	}

	/**
	 * SELECT
	 * 
	 * @param data
	 * @return
	 */
	public ResultTrData selectDB(SendTrData data) {

		String url = CommonUtil.getUrlLastSegment(data.getTrCode());

		Map<String, Object> response = new HashMap<>();

		mrktType = data.getObjCommInput().get("FID_COND_MRKT_DIV_CODE");
		jmCode = data.getObjCommInput().get("FID_INPUT_ISCD");

		switch (url) {
		case TranFile.INQUIRE_PRICE -> {
			pk_inquire = new PK_inquire(mrktType, jmCode);
			Optional<InquirePrice> dto1 = inquirePriceRepository.findById(pk_inquire);
			if (dto1.isPresent()) {
				response.put("output", CommonUtil.stringToMap(dto1.get().getOutput()));
			} else {
				response = null;
			}
		}
		case TranFile.INQUIRE_MEMBER -> {
			pk_inquire = new PK_inquire(mrktType, jmCode);
			Optional<InquireMember> dto2 = inquireMemberRepository.findById(pk_inquire);
			if (dto2.isPresent()) {
				response.put("output", CommonUtil.stringToMap(dto2.get().getOutput()));
			} else {
				response = null;
			}
		}
		case TranFile.INQUIRE_INVESTOR -> {
			pk_inquire = new PK_inquire(mrktType, jmCode);
			Optional<InquireInvestor> dto3 = inquireInvestorRepository.findById(pk_inquire);
			if (dto3.isPresent()) {
				response.put("output", CommonUtil.stringToList(dto3.get().getOutput()));
			} else {
				response = null;
			}
		}
		case TranFile.INQUIRE_CCNL -> {
			pk_inquire = new PK_inquire(mrktType, jmCode);
			Optional<InquireCcnl> dto4 = inquireCcnlRepository.findById(pk_inquire);
			if (dto4.isPresent()) {
				response.put("output", CommonUtil.stringToList(dto4.get().getOutput()));
			} else {
				response = null;
			}
		}
		case TranFile.INQUIRE_ASK_PRICE_EXP -> {
			pk_inquire = new PK_inquire(mrktType, jmCode);
			Optional<InquireAskPrice> dto5 = inquireAskPriceRepository.findById(pk_inquire);
			if (dto5.isPresent()) {
				response.put("output1", CommonUtil.stringToMap(dto5.get().getOutput1()));
				response.put("output2", CommonUtil.stringToMap(dto5.get().getOutput2()));
			} else {
				response = null;
			}
		}
//		case TranFile.INQUIRE_DAILY_INDEX -> {
//			date1 = data.getObjCommInput().get("FID_INPUT_DATE_1");
//			date2 = data.getObjCommInput().get("FID_INPUT_DATE_2");
//			periodCode = data.getObjCommInput().get("FID_PERIOD_DIV_CODE");
//			pk_dailyIndex = new PK_dailyIndex(mrktType, jmCode, date1, date2, periodCode);
//
//			Optional<DailyIndex> dto6 = dailyIndexRepository.findById(pk_dailyIndex);
//			if (dto6.isPresent()) {
//				response.put("output1", CommonUtil.stringToMap(dto6.get().getOutput1()));
//				response.put("output2", CommonUtil.stringToList(dto6.get().getOutput2()));
//			} else {
//				response = null;
//			}
//
//		}
		case TranFile.INQUIRE_DAILY_ITEM -> {
			date1 = data.getObjCommInput().get("FID_INPUT_DATE_1");
			date2 = data.getObjCommInput().get("FID_INPUT_DATE_2");
			periodCode = data.getObjCommInput().get("FID_PERIOD_DIV_CODE");
			orgAdj = data.getObjCommInput().get("FID_ORG_ADJ_PRC");
			
//			List<String> dateRange = getDateRange(date1, date2);
//			LOGCAT.i(TAG,  jmCode + " : [Date Range] - "+dateRange.toString() );
			List<Object> output2 = new ArrayList<>();
			if(periodCode.equals("D")) {
//				if(dateRange.size() < 200) {
//					for (String date3 : dateRange) {
//						pk_chart = new PK_chart(jmCode, date3);
//						Optional<InquireItemChart> dto = inquireChartRepository.findById(pk_chart);
//						try {
//							if(dto.isPresent()) {
//								output2.add(CommonUtil.objectToString(dto.get().getChartData()));
//							}
//						} catch (JsonProcessingException e) {
//							LOGCAT.i(TAG, "[ERROR] - " +e.toString());
//						}
//					}
//				}
				List<InquireItemChart> dto = inquireChartRepository.selectChartData(jmCode,date1, date2);
				try {
					if(dto != null && dto.size() > 0) {
						for(InquireItemChart chartData : dto) {
								output2.add(CommonUtil.objectToString(chartData.getChartData()));
							
						}
					}
				} catch (JsonProcessingException e) {
					LOGCAT.i(TAG, "[ERROR] - " +e.toString());
					
				}
				
				if (output2 == null || output2.isEmpty()) {
					LOGCAT.i(TAG, "Output - 2 없음 ");
					response = null;
				} else {
					try {
						List<Object> reverseOutput = new ArrayList<>();
						Collections.reverse(output2);
						response.put("output1", CommonUtil.objecttoMap(output2.get(output2.size()-1)));
					} catch (JsonProcessingException e) {
						e.printStackTrace();
					}
					response.put("output2", output2);
				}
				
				
				
				
			}else{
				pk_dailyItem = new PK_dailyItem(mrktType, jmCode, date1, date2, periodCode, orgAdj);
				
				Optional<DailyItem> dto7 = dailyItemRepository.findById(pk_dailyItem);
				if (dto7.isPresent()) {
					response.put("output1", CommonUtil.stringToMap(dto7.get().getOutput1()));
					response.put("output2", CommonUtil.stringToList(dto7.get().getOutput2()));
				} else {
					response = null;
				}
			}
		}
		case TranFile.INQUIRE_DAILY_PRICE -> {
			date1 = data.getObjCommInput().get("FID_INPUT_DATE_1");
			date2 = data.getObjCommInput().get("FID_INPUT_DATE_2");
			periodCode = data.getObjCommInput().get("FID_PERIOD_DIV_CODE");
			orgAdj = data.getObjCommInput().get("FID_ORG_ADJ_PRC");
			pk_dailyPrice = new PK_dailyPrice(mrktType, jmCode, periodCode, orgAdj);

			Optional<DailyPrice> dto8 = dailyPriceRepository.findById(pk_dailyPrice);
			if (dto8.isPresent()) {
				response.put("output", CommonUtil.stringToList(dto8.get().getOutput()));
			} else {
				response = null;
			}
		}
		case TranFile.INQUIRE_TIME_ITEM -> {
			hour = data.getObjCommInput().get("FID_INPUT_HOUR_1");
			;
			pk_time = new PK_time(mrktType, jmCode, hour);

			Optional<TimeItem> dto9 = timeItemRepository.findById(pk_time);
			if (dto9.isPresent()) {
				response.put("output1", CommonUtil.stringToMap(dto9.get().getOutput1()));
				response.put("output2", CommonUtil.stringToList(dto9.get().getOutput2()));
				if( response.get("output2") == null || response.get("output1") == null ) {
					response = null;
				}
			} else {
				response = null;
			}
		}
		default -> response = null;
		}

		Map<String, String> stringMap = data.getObjCommInput(); // String 타입의 Map
		Map<String, Object> objectMap = new HashMap<>(); // Object 타입의 Map
		// Map 형변환
		for (Map.Entry<String, String> entry : stringMap.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			Object objectValue = (Object) value;
			objectMap.put(key, objectValue);
		}

		Boolean dataFlag = (response != null);

		// msg_cd( 메세지 코드 ) ,msg1( 응답 메세지 ) , rt_cd( 성공 실패 여부 )
		if (dataFlag) {
			response.put("msg_cd", "MCA00000");
			response.put("msg1", ErrorFile.getInstance().getErrorMsg("MCA00000"));
			response.put("rt_cd", "0");
		} else {
			response = new HashMap<>(); // response가 null이면 새 HashMap을 생성
			response.put("msg_cd", "EGW00101");
			response.put("msg1", ErrorFile.getInstance().getErrorMsg("EGW00101"));
			response.put("rt_cd", "1");
		}

		ResultTrData resultData = new ResultTrData(data.getTrCode(), null, objectMap, response, dataFlag);

		return resultData;
	}

	/**
	 * DELETE
	 * 
	 * @param url
	 * @param request
	 */
	@Transactional
	public void deleteRow(String url, Map<String, String> request) {

		mrktType = request.get("mrktType");
		jmCode = request.get("jmCode");

		switch (url) {
		case TranFile.INQUIRE_PRICE -> {
			pk_inquire = new PK_inquire(mrktType, jmCode);
			inquirePriceRepository.deleteById(pk_inquire);
		}
		case TranFile.INQUIRE_MEMBER -> {
			pk_inquire = new PK_inquire(mrktType, jmCode);
			inquireMemberRepository.deleteById(pk_inquire);
		}
		case TranFile.INQUIRE_INVESTOR -> {
			pk_inquire = new PK_inquire(mrktType, jmCode);
			inquireInvestorRepository.deleteById(pk_inquire);
		}
		case TranFile.INQUIRE_CCNL -> {
			pk_inquire = new PK_inquire(mrktType, jmCode);
			inquireCcnlRepository.deleteById(pk_inquire);
		}
		case TranFile.INQUIRE_ASK_PRICE_EXP -> {
			pk_inquire = new PK_inquire(mrktType, jmCode);
			inquireAskPriceRepository.deleteById(pk_inquire);
		}
		case TranFile.INQUIRE_DAILY_INDEX -> {
			date1 = request.get("date1");
			date2 = request.get("date2");
			periodCode = request.get("periodCode");
			pk_dailyIndex = new PK_dailyIndex(mrktType, jmCode, date1, date2, periodCode);
			dailyIndexRepository.deleteById(pk_dailyIndex);
		}
		case TranFile.INQUIRE_DAILY_ITEM -> {
			date1 = request.get("date1");
			date2 = request.get("date2");
			periodCode = request.get("periodCode");
			orgAdj = request.get("orgAdj");
			pk_dailyItem = new PK_dailyItem(mrktType, jmCode, date1, date2, periodCode, orgAdj);
			dailyItemRepository.deleteById(pk_dailyItem);
		}
		case TranFile.INQUIRE_DAILY_PRICE -> {
			date1 = request.get("date1");
			date2 = request.get("date2");
			periodCode = request.get("periodCode");
			orgAdj = request.get("orgAdj");
			pk_dailyPrice = new PK_dailyPrice(mrktType, jmCode, periodCode, orgAdj);
			dailyPriceRepository.deleteById(pk_dailyPrice);
		}
		case TranFile.INQUIRE_TIME_ITEM -> {
			hour = request.get("hour");
			pk_time = new PK_time(mrktType, jmCode, hour);
			timeItemRepository.deleteById(pk_time);
		}
		}
	}

	/**
	 * DELETE all
	 * 
	 * @param url
	 */
	@Transactional
	public void deleteAllRows(String url) {
		switch (url) {
		case TranFile.INQUIRE_PRICE -> inquirePriceRepository.deleteAll();
		case TranFile.INQUIRE_MEMBER -> inquireMemberRepository.deleteAll();
		case TranFile.INQUIRE_INVESTOR -> inquireInvestorRepository.deleteAll();
		case TranFile.INQUIRE_CCNL -> inquireCcnlRepository.deleteAll();
		case TranFile.INQUIRE_ASK_PRICE_EXP -> inquireAskPriceRepository.deleteAll();
		case TranFile.INQUIRE_DAILY_INDEX -> dailyIndexRepository.deleteAll();
		case TranFile.INQUIRE_DAILY_ITEM -> dailyItemRepository.deleteAll();
		case TranFile.INQUIRE_DAILY_PRICE -> dailyPriceRepository.deleteAll();
		case TranFile.INQUIRE_TIME_ITEM -> timeItemRepository.deleteAll();
		}
	}

	/**
	 * 날짜 범위 내의 모든 날짜 가져오는 메소드
	 * 
	 * @param url
	 */
	private static List<String> getDateRange(String startDateStr, String endDateStr) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		LocalDate startDate = LocalDate.parse(startDateStr, formatter);
		LocalDate endDate = LocalDate.parse(endDateStr, formatter);

		List<String> dateRange = new ArrayList();
		for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
			dateRange.add(date.format(formatter));
		}

		return dateRange;
	}

}
