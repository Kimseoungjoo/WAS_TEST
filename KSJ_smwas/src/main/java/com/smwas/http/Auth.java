package com.smwas.http;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.smwas.header.CommHeader;
import com.smwas.util.LOGCAT;
import com.smwas.util.ReadProperties;

/*
 * 접근 토큰 발급
 * 
 * */
public class Auth {
	public static final String TAG = Auth.class.getSimpleName();
	
	@SuppressWarnings("rawtypes")
    public void authenticate(String result) {
		// ops.properties 의 값을 가져온다.
		ReadProperties ops = new ReadProperties();
		Properties prop = ops.readProperties("ops.properties");
		
		// url
		String url = String.format("%s:%s%s", prop.getProperty("url_base"), prop.getProperty("uri_base_port"), prop.getProperty("url_token"));
        HttpHeaders headers = createJsonHeaders();		// 헤더
        Map<String, String> body = createRequestBody();	// 바디
        
        // 통신 
        ResponseEntity<Map> res = new RestTemplate().postForEntity(url, new HttpEntity<>(body, headers), Map.class);
        // TODO :: 접근 토큰 발급 잠시 후 다시 시도 > 
        if(result.contains("EGW001333")) {
        	authenticate(res.getBody().toString());
        }else {
        	String ACCESS_TOKEN = (String) res.getBody().get("access_token");
        	CommHeader.setAuthorization(ACCESS_TOKEN); // 발급받은 토큰 저장 
        	
        	LOGCAT.i(TAG, "TOKEN - " + ACCESS_TOKEN );
        }
    }


	/**
	 * 헤더 세팅
	 * 
	 * @return
	 */
	
    private static HttpHeaders createJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }


    /**
     * 바디 세팅
     * 
     * @return
     */
    
    private static Map<String, String> createRequestBody() {

        Map<String, String> body = new HashMap<>();
        body.put("grant_type", CommHeader.getGrantType());
        body.put("appkey", CommHeader.getAppkey());
        body.put("appsecret", CommHeader.getAppsecret());
        return body;
    }
	
}
