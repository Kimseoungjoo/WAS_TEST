package com.smwas.util;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CommonUtil {
	
	/**
	 * URL 가져오기
	 * 
	 * @param url
	 */
	public static String getUrlLastSegment(String url) {
		if (url == null || url.isEmpty()) {
			return null;
		}

		String[] segments = url.split("/");
		if (segments.length > 0) {
			return segments[segments.length - 1];
		} else {
			return null;
		}
	}
	
	
	/**
	 * Object -> String
	 * 
	 * @param output
	 * @return
	 * @throws JsonProcessingException
	 */
	public static String objectToString(Object output) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		String result = mapper.writeValueAsString(output); // Java Object -> JSONString 으로 직렬화
		return result;
	}
	/**
	 * String -> Object
	 * 
	 * @param output
	 * @return
	 * @throws JsonProcessingException
	 */
	public static Object objectToString(String output) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		 Object result = mapper.readValue(output, Object.class); // Java Object -> JSONString 으로 직렬화
		return result;
	}
	/**
	 * Object -> Map<String, Object
	 * 
	 * @param output
	 * @return
	 * @throws JsonProcessingException
	 */
	public static Map<String, Object> objecttoMap(Object output) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> result = objectMapper.convertValue(output, Map.class);
		return result;
	}
	

	/**
	 * JSON -> Map	{}
	 * 
	 * @param strJson
	 * @return
	 */
	public static Map<String, Object> stringToMap(String strJson) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> map = mapper.readValue(strJson, new TypeReference<Map<String, Object>>() {
			});

			return map;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * JSON -> List  []
	 * 
	 * @param strJson
	 * @return
	 */
	public static List<Object> stringToList(String strJson) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			List<Object> list = mapper.readValue(strJson, new TypeReference<List<Object>>() {});
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
