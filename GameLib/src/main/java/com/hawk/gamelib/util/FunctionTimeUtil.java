package com.hawk.gamelib.util;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 
 * 为了尽力缩小测试的误差，该类的代码不会有任何的校验，请务必保证，先start　再end 否则会乱
 * 写个名字方便全文搜索
 * @author CodeJ
 *2016年7月11日
 */
public class FunctionTimeUtil extends ThreadLocal<FunctionTimeUtil> {

	static GarbageCollectorMXBean firstGc = null;
	static GarbageCollectorMXBean secondGc = null;

	static {
		List<GarbageCollectorMXBean> gcList = ManagementFactory.getGarbageCollectorMXBeans();

		firstGc = gcList.get(0);
		secondGc = gcList.get(1);
	}



	private static final FunctionTimeUtil ftu = new FunctionTimeUtil();
	public static final Map<Thread, FunctionTimeUtil> monitorMap = new ConcurrentHashMap<>();
	private Map<String, FunctionTimeDemain> demainMap = new HashMap<String, FunctionTimeDemain>();
	public static FunctionTimeUtil getInstance(){
		return ftu.get();
	}
	
	public static void start(String name){
		getInstance().realStart(name);
	}
	
	public  void realStart(String name){
		FunctionTimeDemain ftd = demainMap.get(name);
		if(ftd == null){
			ftd = new FunctionTimeDemain();
			demainMap.put(name, ftd);
		}

		ftd.setFirstGcTimes(firstGc.getCollectionCount());
		ftd.setSecondGcTimes(secondGc.getCollectionCount());
		ftd.setLastTime(System.nanoTime());
	}
	
	public static void end(String name){
		end(name, false);
	}
	
	public static void end(String name, boolean isPrint){
		getInstance().realEnd(name, isPrint);
	}
	
	public void realEnd(String name, boolean isPrint){
		FunctionTimeDemain ftd = demainMap.get(name);
		if(ftd.getFirstGcTimes() != firstGc.getCollectionCount() || ftd.getSecondGcTimes() != secondGc.getCollectionCount()) {
			return;
		}

		long curTime = System.nanoTime();
		ftd.setCallTimes(ftd.getCallTimes() + 1);
		long useTime = curTime - ftd.getLastTime();
		ftd.setUseTime(ftd.getUseTime() + useTime);
		if(isPrint){
			System.out.println(name+" 调用耗时===>"+(useTime /1000000));
		}
		 
	}
	
	public static void printUseTime(){
		printUseTime(true);
	}
	public static String printUseTime(boolean isMerge){
		StringBuilder sb = new StringBuilder();
		Map<String, FunctionTimeDemain> map = new HashMap<>();
		for(Entry<Thread, FunctionTimeUtil> entry : monitorMap.entrySet()){
			for(Entry<String,FunctionTimeDemain> entry1 : entry.getValue().demainMap.entrySet()){
				//时间转换为毫秒
				if(isMerge){
					FunctionTimeDemain ftd = map.get(entry1.getKey());
					if(ftd == null){
						ftd = entry1.getValue().clone();
						map.put(entry1.getKey(), ftd);
					}else{
						ftd.setCallTimes(ftd.getCallTimes() + entry1.getValue().getCallTimes());
						ftd.setUseTime(ftd.getUseTime() + entry1.getValue().getUseTime());
					}
				}else{
					String str = "thread=>"+entry.getKey().getName()+", name=>"+entry1.getKey()+" callTimes,useTime=>"+entry1.getValue().getCallTimes()+","+(entry1.getValue().getUseTime()/1000000);
					System.err.println(str);
					sb.append(str);
					sb.append("\r\n");
					
				}
				
			}
		}		
		if(isMerge){
			for(Entry<String, FunctionTimeDemain> entry : map.entrySet()){
				String str = "name=>"+entry.getKey()+" callTimes,useTime=>"+entry.getValue().getCallTimes()+","+(entry.getValue().getUseTime()/1000000);
				System.err.println(str);
				sb.append("\r\n");
				sb.append(str);
				
			}
		}
		
		return sb.toString();
	}
	@Override
	public FunctionTimeUtil initialValue(){
		FunctionTimeUtil ftu = new FunctionTimeUtil();
		monitorMap.put(Thread.currentThread(), ftu);
		return ftu;
	}
	
	class FunctionTimeDemain implements Cloneable{
		private int callTimes=0;
		private long useTime=0;
		private long lastTime=0;
		private long firstGcTimes=0;
		private long secondGcTimes=0;

		public long getFirstGcTimes() {
			return firstGcTimes;
		}

		public void setFirstGcTimes(long firstGcTimes) {
			this.firstGcTimes = firstGcTimes;
		}

		public long getSecondGcTimes() {
			return secondGcTimes;
		}

		public void setSecondGcTimes(long secondGcTimes) {
			this.secondGcTimes = secondGcTimes;
		}

		public int getCallTimes() {
			return callTimes;
		}
		public long getUseTime() {
			return useTime;
		}
		public long getLastTime() {
			return lastTime;
		}
		public void setCallTimes(int callTimes) {
			this.callTimes = callTimes;
		}
		public void setUseTime(long useTime) {
			this.useTime = useTime;
		}
		public void setLastTime(long lastTime) {
			this.lastTime = lastTime;
		}
		
		@Override
		public FunctionTimeDemain clone(){
			 
			FunctionTimeDemain ftd = new FunctionTimeDemain();
			ftd.callTimes = callTimes;
			ftd.useTime = useTime;
			ftd.firstGcTimes = firstGcTimes;
			ftd.secondGcTimes = secondGcTimes;
			return ftd;
		}
	}
}
