package com.smwas.db;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * DB 테스트용 컨트롤러 
 */
@RestController
@CrossOrigin(origins = "*") // 모든 ip로부터 요청 받기 허용 (cors 해결)
@RequestMapping("/db")
public class DatabaseController {
	
//	@Autowired
//	private ConnectMysql connectMysql;
//
//
//	@GetMapping("/insert")
//	public void tryInsert() {
//		Map<String, String> response = new HashMap<>();
//		response.put("mrktType", "J");
//		response.put("jmCode", "005930");
//		response.put("output", "hello??");
//		connectMysql.tryInsert("inquire-price", response);
//	}
//	
////	@GetMapping("/select")
////	public Map<String, Object> trySelect() {
////		Map<String, String> request = new HashMap<>();
////		request.put("mrktType", "J");
////		request.put("jmCode", "005930");
////		
//////		return connectMysql.trySelect("inquire-price", request);
////		return connectMysql.trySelect("inquire-price", request);
////	}
//	
//	@GetMapping("/delete")
//	public void tryDelete() {
//		Map<String, String> request = new HashMap<>();
//		request.put("mrktType", "J");
//		request.put("jmCode", "005930");
//		connectMysql.tryDelete("inquire-price", request);
//	}
//	
//	@GetMapping("/deleteAll")
//	public void tryDeleteAll() {
//		connectMysql.tryDeleteAll("inquire-price");
//	}

}
