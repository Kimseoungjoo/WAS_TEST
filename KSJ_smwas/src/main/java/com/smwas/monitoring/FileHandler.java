package com.smwas.monitoring;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.smwas.util.LOGCATCollector;

@Component
public class FileHandler {
	public static final String TAG = FileHandler.class.getSimpleName();
	
	public static final List<String> REALTAG = new ArrayList<>(Arrays.asList("SessionItem","SessionManager"));
	
	private static final String LOG_DIR = "/home/smwas/test/log";						// 일반 로그 파일 저장 경로(서버)
//	private static final int MAX_SAVE_DAY = 7; 											// 최대 파일 보관 기한
	private static final int MAX_FILE_SIZE = 1024 * 1024 * 300; 						// 최대 파일 사이즈(300MB)
	private static final String LOG_BASIC_NAME = "smwas_log_"; // 일반 로그 파일명 양식
	private static final String PLOG_BASIC_NAME = "smwas_polling_log_"; // 폴링 로그 파일명 양식

	// 테스트
//	private static final String LOG_DIR = "C:\\work\\smwas\\logs"; // 일반 로그 파일 저장 경로(테스트)
	private static final int MAX_SAVE_DAY = 1; // 최대 파일 보관 기한
//	private static final int MAX_FILE_SIZE = 1024; // 최대 파일 사이즈(1KB)

	private static final FileHandler instance = new FileHandler();

	public FileHandler() {
	}

	public static FileHandler getInstance() {
		return instance;
	}

	/**
	 * 파일명에 삽입 될 날짜 구하기(파일 생성일)
	 */
	private String fileDate() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd"); // 날짜 표시 포맷
		Date today = new Date(); // 오늘 날짜 구하기
		return formatter.format(today); // 오늘 날짜를 yyyyMMdd 형식의 문자열로 변환하여 반환
	}

	/**
	 * 용량 초과 시 파일 분할하여 생성
	 */
	private String getFileName(String basedName, String extension) {
		int fileNum = 0;
		File file;
		String fileName = basedName + extension; // 파일명을 '기본 이름 + 확장자'로 구성

		while (true) {
			file = new File(fileName);

			if (!file.exists() || file.length() < MAX_FILE_SIZE) { // 파일이 존재하지 않을 경우, 파일의 최대 용량을 넘지 않을 경우에는 파일 생성 및 번호
																	// 증가 이뤄지지 않음
				break;
			}

			fileNum++;
			fileName = basedName + "_" + fileNum + extension;
		}
		return fileName;
	}

	/**
	 * Polling 로그인지 그 외 로그인지에 따라 구분해서 처리
	 */
	private boolean checkPolling() {
		boolean isPolling = false;

		String newLog = LOGCATCollector.getInstance().getLogcat(); 			// 수집된 로그를 가져옴

		if (newLog != null && newLog.contains("PINGPONG")) { 				// 로그 내용에 PINGPONG이 있는지 확인
			isPolling = true;
		}
		return isPolling;
	}

	/**
	 * 로그 파일 쓰기: LOGCAT 실행될 때마다 파일에 씀. 파일이 없다면 새로 생성
	 */
	public void writeLog(String strTag) throws IOException {
		String filePath = LOG_DIR + "/" + LOG_BASIC_NAME + fileDate();		 // 지정된 경로에 'smwas_log_오늘날짜.txt' 파일명으로 저장
		String p_filePath = LOG_DIR + "/" + PLOG_BASIC_NAME + fileDate(); 	 // 지정된 경로에 'smwas_polling_log_오늘날짜.txt' 파일명으로 저장
																			

		File file = new File(getFileName(filePath, ".txt"));
		File p_file = new File(getFileName(p_filePath, ".txt"));

		String newLog = LOGCATCollector.getInstance().getLogcat(); 			 // 저장한 로그 가져옴

		boolean isPollingData = checkPolling(); 							 // polling 로그인지 일반 로그인지 체크

		if (newLog != null && !newLog.isEmpty() && !REALTAG.contains(strTag) ) {
			File targetFile;

			if (isPollingData) { 											 // Polling 로그일 경우
				targetFile = p_file;
			} else { 														 // 일반 로그일 경우
				targetFile = file;
			}
			BufferedWriter bw = new BufferedWriter(new FileWriter(targetFile, true)); // 이어쓰기
			bw.write(newLog); 												// 새로 생성된 로그 쓰기
			bw.newLine();
			bw.close();
			LOGCATCollector.getInstance().clearLogcat(); 					// 저장한 로그 초기화
		}
	}
	
	/**
	 * 매일 자정에 1일 지난 파일 삭제 (테스트 기간 1일, 실서비스 7일 제한 예정)
	 */
	@Scheduled(cron = "0 0 10 * * *") 							// 매일 오전 10시에 실행
	public void logDelete() {
		long currentTimeMillis = System.currentTimeMillis(); 	// 현재 시간(밀리초 단위)
		long oneDayInMillis = 24 * 60 * 60 * 1000; 				// 하루의 밀리초

		File logDir = new File(LOG_DIR);

		File[] logFiles = logDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				// 파일 이름 패턴이 "smwas_log_YYYYMMDD.txt" 또는 "smwas_polling_log_YYYYMMDD.txt",
				// 그리고 선택적으로 "_숫자"가 붙은 파일도 포함
				return file.getName().matches("smwas(_polling)?_log_\\d{8}(_\\d+)?\\.txt");
			}
		});

		if (logFiles != null) {
			for (File file : logFiles) {
				long fileAgeInMillis = currentTimeMillis - file.lastModified(); // 파일이 생성된 후 경과한 시간 구하기 (현재 시간 - 파일이 마지막으로 수정된 시간)
				int fileAgeInDays = (int) (fileAgeInMillis / oneDayInMillis); 	// 이를 일 단위로 변환

				if (fileAgeInDays > MAX_SAVE_DAY && file.exists()) {			// 파일이 최대 보관 기간을 초과했는지 확인
					boolean deleted = file.delete(); 							// 파일 삭제
					if (deleted) {
						System.out.println(file.getName() + " 파일이 삭제되었습니다.");
					}
				}
			}
		}
	}
}
	


