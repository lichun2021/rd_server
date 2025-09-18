package com.hawk.ms.thread;

public class ThreadStringBuilder extends ThreadLocal<StringBuilder> {

	
	@Override
	public StringBuilder initialValue() {
		return new StringBuilder(1024);
	}
	
	static ThreadLocal<StringBuilder> staticInstance = new ThreadStringBuilder();
	public static StringBuilder getStringBuilder(){
		StringBuilder sb =  staticInstance.get();
		if (sb.length() >= 0) {
			sb.delete(0, sb.length());
		} 
		
		return sb;
	}
}
