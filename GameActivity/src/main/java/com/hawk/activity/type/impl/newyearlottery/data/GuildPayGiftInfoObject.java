package com.hawk.activity.type.impl.newyearlottery.data;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.hawk.collection.ConcurrentHashSet;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class GuildPayGiftInfoObject {
	/**
	 * 所属联盟ID
	 */
	private String guildId;
	/**
	 * 礼包类型：金运礼包、银运礼包
	 */
	private int lotteryType;
	/**
	 * 时间
	 */
	private String dayTime;
	/**
	 * 联盟内购买人数
	 */
	private AtomicInteger payCountObj;
	/**
	 * 抽奖人数
	 */
	private int lotteryCount;
	/**
	 * 联盟内购买人数成就梯次达成时间
	 */
	private Map<Integer, Long> achieveTimeMap;
	/**
	 * 抽中大奖的序号：第几个抽中了
	 */
	private int lotteryLuckyNum;
	/**
	 * 抽中大奖的最大抽奖次数：如果前面一直没有抽中大奖，那么抽奖达到这个数时必中大奖
	 */
	private int lotteryLuckyMax;
	/**
	 * 购买礼包的成员
	 */
	private Set<String> payGiftMembers;
	/**
	 * 已抽奖的成员
	 */
	private Set<String> lotteryMembers;
	
	private int historyPayCount;
	private long nextCheckPayCountAchieveTime;
	
	private GuildPayGiftInfoObject(String guildId, int lotteryType, String dayTime) {
		this.guildId = guildId;
		this.lotteryType = lotteryType;
		this.dayTime = dayTime;
		this.payCountObj = new AtomicInteger(0);
		this.payGiftMembers = new ConcurrentHashSet<>();
		this.lotteryMembers = new ConcurrentHashSet<>();
		this.achieveTimeMap = new ConcurrentHashMap<Integer, Long>();
	}
	
	public static GuildPayGiftInfoObject valueOf(String guildId, int lotteryType, String dayTime) {
		GuildPayGiftInfoObject obj = new GuildPayGiftInfoObject(guildId, lotteryType, dayTime);
		obj.updateNextCheckPayCountAchieveTime();
		return obj;
	}

	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}

	public int getLotteryType() {
		return lotteryType;
	}

	public void setLotteryType(int lotteryType) {
		this.lotteryType = lotteryType;
	}

	public String getDayTime() {
		return dayTime;
	}

	public void setDayTime(String dayTime) {
		this.dayTime = dayTime;
	}

	public int getPayCount() {
		return payCountObj.get();
	}

	public int getLotteryCount() {
		return lotteryCount;
	}
	
	public void setPayCount(int payCount) {
		this.payCountObj.set(payCount);
	}
	
	public  void setLotteryCount(int lotteryCount) {
		this.lotteryCount = lotteryCount;
	}
	
	public Map<Integer, Long> getAchieveTimeMap() {
		return achieveTimeMap;
	}

	public int getLotteryLuckyNum() {
		return lotteryLuckyNum;
	}
	
	public void setLotteryLuckyNum(int lotteryLuckyNum) {
		this.lotteryLuckyNum = lotteryLuckyNum;
	}
	
	public Set<String> getPayMembers() {
		return payGiftMembers;
	}
	
	public Set<String> getLotteryMembers() {
		return lotteryMembers;
	}
	
	public int getLotteryLuckyMax() {
		return lotteryLuckyMax;
	}

	public void setLotteryLuckyMax(int lotteryLuckyMax) {
		this.lotteryLuckyMax = lotteryLuckyMax;
	}
	
	public int getHistoryPayCount() {
		return historyPayCount;
	}

	public void setHistoryPayCount(int historyPayCount) {
		this.historyPayCount = historyPayCount;
	}

	public long getNextCheckPayCountAchieveTime() {
		return nextCheckPayCountAchieveTime;
	}

	public void updateNextCheckPayCountAchieveTime() {
		this.nextCheckPayCountAchieveTime = HawkTime.getMillisecond() + randTime();
	}

	private static long randTime() {
		return HawkRand.randInt(5, 15) * 1000L;
	}
	
	public String toJSONString() {
		JSONObject json = new JSONObject();
		json.put("guildId", guildId);
		json.put("lotteryType", lotteryType);
		json.put("dayTime", dayTime);
		json.put("payCount", payCountObj.get());
		json.put("lotteryCount", lotteryCount);
		json.put("lotteryLuckyNum", lotteryLuckyNum);
		json.put("lotteryLuckyMax", lotteryLuckyMax);
		JSONArray array = new JSONArray();
		for (Entry<Integer, Long> entry : achieveTimeMap.entrySet()) {
			array.add(entry.getKey() + "_" + entry.getValue());
		}
		json.put("achieveTime", array);
		return json.toJSONString();
	}
	
	public static GuildPayGiftInfoObject toObject(String data) {
		JSONObject json = JSONObject.parseObject(data);
		String guildId = json.getString("guildId");
		int lotteryType = json.getIntValue("lotteryType");
		String dayTime = json.getString("dayTime");
		int payCount = json.getIntValue("payCount");
		int lotteryCount = json.getIntValue("lotteryCount");
		int lotteryLuckyNum = json.getIntValue("lotteryLuckyNum");
		int lotteryLuckyMax = json.getIntValue("lotteryLuckyMax");
		GuildPayGiftInfoObject obj = GuildPayGiftInfoObject.valueOf(guildId, lotteryType, dayTime);
		obj.setPayCount(payCount);
		obj.setLotteryCount(lotteryCount);
		obj.setLotteryLuckyNum(lotteryLuckyNum);
		obj.setLotteryLuckyMax(lotteryLuckyMax);
		JSONArray array = json.getJSONArray("achieveTime");
		for (int i = 0; i < array.size(); i++) {
			String keyval = array.getString(i);
			String[] split = keyval.split("_");
			int count = Integer.parseInt(split[0]);
			long time = Long.parseLong(split[1]);
			obj.getAchieveTimeMap().put(count, time);
		}
		
		obj.setHistoryPayCount(obj.getPayCount());
		return obj;
	}
	
}
