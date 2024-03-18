package com.smwas.http;

import org.springframework.scheduling.annotation.Scheduled;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smwas.comm.CommApi;
import com.smwas.io.ResultTrData;
import com.smwas.io.SendTrData;
import com.smwas.util.LOGCAT;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.rules.Timeout;
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
import com.smwas.io.SendRealData;
import com.smwas.io.SendTrData;
import com.smwas.monitoring.MonitoringService;
import com.smwas.session.SessionItem;
import com.smwas.session.SessionManager;
import com.smwas.tr.TranFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
public class BatchScheduler {
	public static final String TAG = BatchScheduler.class.getSimpleName();

	// TODO :: 추후에 수정 필요 
	// 관심 종목 리스트에 따라 현재가, 호가  데이터 초기화 
	List<String> group_1 =  Arrays.asList("005930","000660","373220","005380","035420","035720","005490","247540","055550","066570"); 
	List<String> group_2 =  Arrays.asList("016360","105560","001500","005940","006800","078020","001510","030610","001200","000270"); 

	
	/**
	 * batch 7시 30분 기준 필요 데이터 
	 * 
	 */
//	@Scheduled(cron = "* 30 7 * * 1-5")
	public void refleshData() {
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		
		 // 실행할 작업을 Runnable 객체로 정의합니다.
        Runnable task = new Runnable() {
            public void run() {
                // 이 부분에 수행할 작업을 작성합니다.
                System.out.println("작업 수행");
            }
        };

        // for 루프 안에서 ScheduledExecutorService를 사용하여 작업을 일정한 시간 간격으로 수행합니다.
        for (int i = 0; i < 10; i++) {
            scheduler.schedule(task, 100, TimeUnit.MILLISECONDS); // 5초 간격으로 작업을 예약합니다.
        }
		
		for(String jmcode : group_1) {
		}
	}
	public void saveDB(String jmCode) {
		
	}
	
	
}
