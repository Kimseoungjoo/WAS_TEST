package com.smwas.monitoring;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

import com.sun.management.OperatingSystemMXBean;

public class MonitoringService {
	public static final String TAG = MonitoringService.class.getSimpleName();
	private static volatile MonitoringService INSTANCE = new MonitoringService();

	public MonitoringService() {
	}

	/**
	 * 통신 객체
	 * 
	 * @return
	 */
	public static MonitoringService getInstance() {
		return INSTANCE;
	}

	// 바이트 단위를 메가바이트 단위로 변환하는 메서드
	private double toMB(long size) {
		return Math.round(size / (1024 * 1024)); // 바이트 단위의 크기를 1024의 제곱(1MB)으로 나누어 MB 단위로 변환
	}

	/**
	 * 디스크 용량 체크 1) 총 디스크 공간 2) 사용 가능한 디스크 공간
	 * 
	 * @return
	 */
	public double getDiskTotalSpace() {
		File root = new File("/"); // 루트 디렉토리(/)를 나타내는 File 객체를 생성
		return toMB(root.getTotalSpace());
	}

	public double getDiskUsableSpace() {
		File root = new File("/"); // 루트 디렉토리(/)를 나타내는 File 객체를 생성
		return toMB(root.getUsableSpace());
	}

	/**
	 * CPU 사용량 체크
	 * 
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public double getCPUProcess() {
		OperatingSystemMXBean osbean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		return osbean.getSystemCpuLoad();
	}
	/**
	 * 메모리 사용량 체크 1) 할당된 메모리 2) 총 메모리
	 * 
	 * @return
	 */
	public double getUsedMemory() {
		MemoryMXBean membean = (MemoryMXBean) ManagementFactory.getMemoryMXBean();
		MemoryUsage heap = membean.getHeapMemoryUsage();
		return heap.getUsed();
	}

	public double getHeapSize() {
		MemoryMXBean membean = (MemoryMXBean) ManagementFactory.getMemoryMXBean();
		MemoryUsage heap = membean.getHeapMemoryUsage();
		return heap.getCommitted();
	}

}
