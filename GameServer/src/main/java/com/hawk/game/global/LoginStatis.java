package com.hawk.game.global;

import java.util.concurrent.atomic.AtomicLong;

import org.hawk.log.HawkLog;

public class LoginStatis {
	private AtomicLong loginCount;
	private AtomicLong loginTime;
	
	private AtomicLong assembleCount;
	private AtomicLong assembleTime;
	
	private static LoginStatis instance;
	
	public static LoginStatis getInstance() {
		if (instance == null) {
			instance = new LoginStatis();
		}
		return instance;
	}
	
	private LoginStatis() {
		
	}
	
	public void init() {
		if (loginCount == null) {
			loginCount = new AtomicLong();
			loginTime = new AtomicLong();
			assembleCount = new AtomicLong();
			assembleTime = new AtomicLong();
		}
	}

	public AtomicLong getLoginCount() {
		return loginCount;
	}

	public AtomicLong getLoginTime() {
		return loginTime;
	}

	public AtomicLong getAssembleCount() {
		return assembleCount;
	}

	public AtomicLong getAssembleTime() {
		return assembleTime;
	}

	public void addLoginTime(long time) {
		loginCount.incrementAndGet();
		loginTime.addAndGet(time);
	}
	
	public void addAssembleTime(long time) {
		assembleCount.incrementAndGet();
		assembleTime.addAndGet(time);
	}
	
	public long getAvgLoginTime() {
		if (loginCount.get() > 0) {
			return loginTime.get() / loginCount.get();
		}
		return 0;
	}
	
	public long getAvgAssembleTime() {
		if (assembleCount.get() > 0) {
			return assembleTime.get() / assembleCount.get();
		}
		return 0;
	}
	
	public void logStatus() {
		if (loginCount.get() <= 0 || assembleCount.get() <= 0) {
			return;
		}
		
		HawkLog.logMonitor("loginStatis, loginCount: {}, loginTime: {}, avgLoginTime: {}, assembleCount: {}, assembleTime: {}, avgAssembleTime: {}",
				loginCount.get(), loginTime.get(), loginTime.get() / loginCount.get(), 
				assembleCount.get(), assembleTime.get(), assembleTime.get() / assembleCount.get());
	}
}
