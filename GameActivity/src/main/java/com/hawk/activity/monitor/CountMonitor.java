package com.hawk.activity.monitor;

import java.util.HashMap;
import java.util.Map;

public class CountMonitor {

	private Map<Object, Monitor> countMap = new HashMap<>();
	
	private Monitor getMonitor(Object key) {
		Monitor monitor = countMap.get(key);
		if (monitor == null) {
			monitor = new Monitor();
			countMap.put(key, monitor);
		}
		return monitor;
	}
	
	public void addCount1(Object key) {
		Monitor monitor = getMonitor(key);
		monitor.count = monitor.count + 1;
	}
	
	public void setMaxTime(Object key, long value) {
		Monitor monitor = getMonitor(key);
		if (monitor.maxTime < value) {
			monitor.maxTime = value;
		}
	}
	
	class Monitor {
		int count;
		long maxTime;
	}
}
