package com.smwas.client;

/*
 * 로그인 시 저장되는 데이터 -- 실시간 사용자 관리 시 key값으로 사용
 * session
 * */
public class ClientData {
	private String httpSessionKey; // HttpSession key 
	private String clientID;	// Client ID
	private String clientIpAddress; // Client IP 

	public ClientData(String httpSessionKey, String clientID, String clientIPAddress) {
		super();
		this.httpSessionKey = httpSessionKey;
		this.clientID = clientID;
		this.clientIpAddress = clientIPAddress;
	}


	public String getHttpSessionKey() {
		return httpSessionKey;
	}
	

	public void setHttpSessionKey(String httpSessionKey) {
		this.httpSessionKey = httpSessionKey;
	}


	public String getClientID() {
		return clientID;
	}


	public void setClientID(String clientID) {
		this.clientID = clientID;
	}


	public String getClientIPAddress() {
		return clientIpAddress;
	}


	public void setClientIPAddress(String clientIPAddress) {
		this.clientIpAddress= clientIPAddress;
	}


	@Override
	public String toString() {
		return "ClientData [httpSessionKey=" + httpSessionKey + ", clientID=" + clientID
				+ ", clientIP=" + clientIpAddress + "]";
	}
	
	



	


}
