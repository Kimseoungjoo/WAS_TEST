package com.smwas.header;

public class RealHeader {
	
	private String rPersonalKey;		// 
	private String rTr_type; 			// 거래타입 1 : 등록, 2 : 해제  
	private String rCusttype;			// B : 법인 P : 개인
	private String rcontentType;		// B : 법인 P : 개인
	
	public RealHeader(String personalKey, String tr_type, String custtype, String contentType) {
		super();
		this.rPersonalKey = personalKey;
		this.rTr_type = tr_type;
		this.rCusttype = custtype;
		this.rcontentType = contentType;
	}
	
	public String getrPersonalKey() {
		return rPersonalKey;
	}
	public void setrPersonalKey(String personalKey) {
		rPersonalKey = personalKey;
	}
	public String getrTr_type() {
		return rTr_type;
	}
	public void setrTr_type(String tr_type) {
		rTr_type = tr_type;
	}
	public String getrCusttype() {
		return rCusttype;
	}
	public void setrCusttype(String custtype) {
		rCusttype = custtype;
	}
	public String getRcontentType() {
		return rcontentType;
	}
	public void setRcontentType(String contentType) {
		rcontentType = contentType;
	}
}
