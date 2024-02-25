package com.smwas.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.smwas.comm.CommApi;
import com.smwas.io.ResultTrData;
import com.smwas.io.SendTrData;
import com.smwas.util.LOGCAT;



@Controller
public class IndexController {
	@GetMapping("/")
	public String index() {
		// src/main/resources/templates 에 있는 html 파일 실행
		return "monitoring";

	}

	@GetMapping("/postman")
	public String post() {
		// src/main/resources/templates 에 있는 html 파일 실행
		// 0. rank 관련 객체 생성 
		SendTrData data = new SendTrData();
		data.setTrCode("/uapi/domestic-stock/v1/quotations/volume-rank");
		Map<String, String> rankHeader  = new HashMap<>();
		Map<String, String> rankInput = new HashMap<>();
		rankInput.put("FID_BLNG_CLS_CODE", "0");
		rankInput.put("FID_COND_MRKT_DIV_CODE", "J");
		rankInput.put("FID_COND_SCR_DIV_CODE", "20171");
		rankInput.put("FID_DIV_CLS_CODE", "0");
		rankInput.put("FID_INPUT_DATE_1", "");
		rankInput.put("FID_INPUT_ISCD", "0000");
		rankInput.put("FID_INPUT_PRICE_1", "");
		rankInput.put("FID_INPUT_PRICE_2", "");
		rankInput.put("FID_TRGT_CLS_CODE", "111111111");
		rankInput.put("FID_TRGT_EXLS_CLS_CODE", "000000");
		rankInput.put("FID_VOL_CNT", "");
		rankHeader.put("tr_id", "FHPST01710000");
		data.setHeader(rankHeader);
		data.setObjCommInput(rankInput);
		
		// 1. 한투 RANK 관련 API 조회 
		ResultTrData apiResult = CommApi.getInstance().callApiForDB(data);
//		LOGCAT.i("랭크 응답 데이터 - 1", apiResult.toString());

		
		if(apiResult.getOutRecMap().get("output") != null) {
			
			// 2. RANK 종목 JSON 파일 수정 변경 
			String jsonFilePath = "static/jmCodeMaster.json";
//			ClassPathResource resource = new ClassPathResource("static/js/j")
			// ObjectMapper 생성
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
			
			try {
				InputStream inputStream = getClass().getClassLoader().getResourceAsStream(jsonFilePath);
				JsonNode rootNode = objectMapper.readTree(inputStream);
//				LOGCAT.i("기존 JSON 데이터 ", rootNode.toString());
				
				objectMapper.writerWithDefaultPrettyPrinter();
				Path path;
				try {
					path = Paths.get(getClass().getClassLoader().getResource(jsonFilePath).toURI());
					Files.write(path, objectMapper.writeValueAsBytes(apiResult.getOutRecMap().get("output")));
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				
				
				// JSON 파일 읽기
				InputStream inputStream2 = getClass().getClassLoader().getResourceAsStream(jsonFilePath);
				JsonNode rootNode2 = objectMapper.readTree(inputStream2);
//				LOGCAT.i("변경 JSON 데이터 ", rootNode2.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		return "apiPage";
	}
}
