package com.hawk.game.data;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 渠道实时在线数据
 * 
 * @author lating
 *
 */
public class PfOnlineInfo {
	
	/**
	 * ios玩家在线人数
	 */
	private AtomicInteger iosOnlineCount = new AtomicInteger(0);
	
	/**
	 * android玩家在线人数
	 */
	private AtomicInteger androidOnlineCount = new AtomicInteger(0);
	/**
	 * 总注册人数数
	 */
	private AtomicInteger registerCount = new AtomicInteger(0);
	
	private AtomicInteger waitLoginCount = new AtomicInteger(0);
	
	public int addIosOnline() {
		return iosOnlineCount.incrementAndGet();
	}
	
	public int addAndroidOnline() {
		return androidOnlineCount.incrementAndGet();
	}
	
	public int removeIosOnline() {
		return iosOnlineCount.decrementAndGet();
	}
	
	public int removeAndroidOnline() {
		return androidOnlineCount.decrementAndGet();
	}
	
	public int getIosOnlineCnt() {
		return iosOnlineCount.get();
	}
	
	public int getAndroidOnlineCnt() {
		return androidOnlineCount.get();
	}
	
	public int addRegister(int count) {
		return registerCount.addAndGet(count);
	}
	
	public int getRegisterCount() {
		return registerCount.get();
	}

	public int getWaitLoginCount() {
		return waitLoginCount.get();
	}

	public int waitLoginAdd(int count) {
		return waitLoginCount.addAndGet(count);
	}
	
	public int waitLoginSub(int count) {
		return waitLoginCount.decrementAndGet();
	}
	
}
