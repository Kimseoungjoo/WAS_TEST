package com.smwas.db;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.smwas.dbio.RealtimeChe;
import com.smwas.dbio.RealtimeHo;
import com.smwas.dbrepo.RealtimeCheRepository;
import com.smwas.dbrepo.RealtimeHoRepository;
import com.smwas.io.ResultTrData;
import com.smwas.tr.TranFile;
import com.smwas.util.CommonUtil;

@Service
@Transactional(readOnly = true)
public class RushTestService {
	@Autowired
	private RealtimeCheRepository realtimeCheRepository;
	@Autowired
	private RealtimeHoRepository realtimeHoRepository;

	
	String trCode = "";	// endpoint
	String trKey = "";	// 종목 코드
	String output = "";	// 결과
	
	
	/**
	 * INSERT (for rush test)
	 * @param realResult
	 * @return
	 * @throws JsonProcessingException
	 */
	@Transactional
	public boolean saveDB(ResultTrData realResult) throws JsonProcessingException {
		
		boolean flag = true;
		
		trCode = realResult.getTrCode();
		trKey = (String) realResult.getOutRecMap().get("MKSC_SHRN_ISCD");
		output = CommonUtil.objectToString(realResult);
		
		switch(trCode) {
		case TranFile.REAL_CHE -> {		//  체결
			RealtimeChe entityChe = new RealtimeChe(trKey, output);
			realtimeCheRepository.save(entityChe);
		}
		case TranFile.REAL_HO -> {		// 호가
			RealtimeHo entityHo = new RealtimeHo(trKey, output);
			realtimeHoRepository.save(entityHo);
		}
		default -> {
			flag = false;
			break;
		}
		}
		
		return flag;
	}
	

	/**
	 * SELECT ALL
	 * 1) 실시간 체결가
	 * 2) 실시간 호가
	 * @return
	 */
	public List<RealtimeChe> selectAllDbChe() {
		List<RealtimeChe> list = realtimeCheRepository.findAll();
		return list;
	}
	public List<RealtimeHo> selectAllDbHo() {
		List<RealtimeHo> list = realtimeHoRepository.findAll();
		return list;
	}
	
	

	/**
	 * DELETE ALL
	 * 1) 실시간 체결가
	 * 2) 실시간 호가
	 */
	@Transactional
	public void deleteAllDbChe() {
		realtimeCheRepository.deleteAll();
		realtimeCheRepository.resetAutoIncrement();	// auto_increment 초기화 (1000->1)
		
	}
	@Transactional
	public void deleteAllDbHo() {
		realtimeHoRepository.deleteAll();
		realtimeHoRepository.resetAutoIncrement();
	}	


}
