package com.smwas.header;

/**
 * 공통 헤더, 파라메터
 */

public class CommHeader {

	/*
	 * 헤더
	 */
	// private static String authorization = "Bearer "; // (+ 접근토큰) String(40), 접근토큰
	// (한투에서 발급받은 접근토큰 앞에 "Bearer" 붙여서 호출)
	private static String authorization = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0b2tlbiIsImF1ZCI6IjM1MjMzNDE0LTkxMGQtNDk0Yy05ZGI1LWFiYTZlNDI5M2JjNiIsImlzcyI6InVub2d3IiwiZXhwIjoxNzA4Mzg0NzgwLCJpYXQiOjE3MDgyOTgzODAsImp0aSI6IlBTVURvNGRaSGpES1NJNTdOUzRmYjJ5T25ESlV3UXk3SGYxNSJ9.zVC9ee6D2WXJWa8wufxH4OANo1uaM3mrxOOQzGdRSIN_itTRKAZOJGnrK-J3bkRyw_JWuL5DwRSZzd5-0uV0rA"
			+ "";

//	private static String appkey = "PSUDo4dZHjDKSI57NS4fb2yOnDJUwQy7Hf15"; // 한투 APP KEY
//	private static String appsecret = "0Oi2jJuwLG3jsx+5qHVVI73YX9YEQKNkX5Q8X2S/Pg8wtVYdsXp0pYtwQqX/u51WOnIKJwLfF+00Yi44SVybqd9Ks+V0zwTl6K7rxszNh3TCAFQGJpHR+c9NJq5DhaGaKC3OLgcrLIHvlrXcWNEbZdoPIefBGmtxTmgAf3DW490MajtSuKY=";
	private static String appkey = "PSJeY9chYB8UgdUt9Me8LIMsdexaO6XKs1EK"; // 한투 APP KEY
	private static String appsecret = "Iiuock65BYo4WLDGlTb1ca9fNfOOlocaNwyAERNyO9+ZAwB4mEDDeIaCeOigAlI1J5l7OU/r52AaW3ZxcYbbDY1rZWpYQMKEddnzggH4F8x7g9V+rlhkNkmGHv7Uz+Mank0dh2qkGYaG0zye8skixXyEwMsjEFzeeLAD0RRVHPGc+crEOKg=";
	private static String custtype = "P"; // String(1) 고객타입 B:법인, P:개인
	private static String grantType = "client_credentials"; // String(18) 권한부여타입 -- 토큰 및 실시간 접속키 발급시 사용
//	private static String approval_key = "9e270fca-5909-4457-9e63-8d2b1f92948a"; // real key 
	private static String approval_key = "c2ed790f-e56d-4062-b70d-d7986232d8e6"; // real key 

	// 생성자
	public CommHeader() {
		// TODO Auto-generated constructor stub
	}

	// getter 메서드

	public static String getApproval_key() {
		return approval_key;
	}

	public static void setApproval_key(String approval_key) {
		CommHeader.approval_key = approval_key;
	}

	public static String getAuthorization() {
		return authorization;
	}

	public static void setAuthorization(String token) {
		authorization = "Bearer " + token;
	}

	public static String getAppkey() {
		return appkey;
	}

	public static void setAppkey(String appkey) {
		CommHeader.appkey = appkey;
	}

	public static String getAppsecret() {
		return appsecret;
	}

	public static void setAppsecret(String appsecret) {
		CommHeader.appsecret = appsecret;
	}

	public static String getCusttype() {
		return custtype;
	}

	public static void setCusttype(String custtype) {
		CommHeader.custtype = custtype;
	}

	public static String getGrantType() {
		return grantType;
	}

	public static void setGrantType(String grantType) {
		CommHeader.grantType = grantType;
	}

}
