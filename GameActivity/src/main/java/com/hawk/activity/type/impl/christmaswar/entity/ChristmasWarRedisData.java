package com.hawk.activity.type.impl.christmaswar.entity;

import java.util.concurrent.atomic.AtomicInteger;

import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.alibaba.fastjson.JSON;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;

/**
 * 存储圣诞大战的一些信息.
 * @author jm
 *
 */
public class ChristmasWarRedisData {
	/**
	 * 击杀小怪的数量
	 */
	private AtomicInteger killMonsterNum = new AtomicInteger();
	/**
	 * 击杀圣诞野怪的数量.
	 */
	private AtomicInteger killChristmasMonsterNum = new AtomicInteger();
	/**
	 * 已经召唤的圣诞怪物数量.
	 */
	private AtomicInteger summonedMonsterNum = new AtomicInteger();
	/**
	 * 上一次重置的时间.
	 */
	private long lastResetTime;
	
	private transient volatile int updateNum;
	
	private transient  int termId;
	
	public AtomicInteger getKillMonsterNum() {
		return killMonsterNum;
	}
	public void setKillMonsterNum(AtomicInteger killMonsterNum) {
		this.killMonsterNum = killMonsterNum;
	}
	public AtomicInteger getKillChristmasMonsterNum() {
		return killChristmasMonsterNum;
	}
	public void setKillChristmasMonsterNum(AtomicInteger killChristmasMonsterNum) {
		this.killChristmasMonsterNum = killChristmasMonsterNum;
	}
	public AtomicInteger getSummonMonsterNum() {
		return summonedMonsterNum;
	}
	public void setSummonMonsterNum(AtomicInteger summonMonsterNum) {
		this.summonedMonsterNum = summonMonsterNum;
	}
	
	public void update() {
		this.updateNum++;
		if (updateNum < 10) {
			return;
		}
		
		updateNum = 0;
		this.saveToRedis();
	} 
	
	/**
	 * 保存到redis里面.
	 */
	public void saveToRedis() {
		String key = getKey(this.termId);
		ActivityLocalRedis.getInstance().set(key, JSON.toJSONString(this));
	}
	
	public static  ChristmasWarRedisData load(int termId) {
		ChristmasWarRedisData redisData = null; 
		String key = getKey(termId);
		String str = ActivityLocalRedis.getInstance().get(key);
		if (HawkOSOperator.isEmptyString(str)) {
			redisData = new ChristmasWarRedisData();
			redisData.setLastResetTime(HawkTime.getMillisecond());
		} else {
			redisData = JSON.parseObject(str, ChristmasWarRedisData.class);
		}
		
		redisData.termId = termId;
		
		return redisData;
	} 
	
	private static String getKey(int termId) {
		return ActivityRedisKey.CHRISTMAS_WAR_DATA + ":" + termId;
	}
	
	public int addKillMonsterNum(int number) {
		int afterNum = this.killMonsterNum.addAndGet(number);
		this.update();
		
		return afterNum;
	}
	
	public int addKillChristmasNum(int number) {
		int afterNum = this.killChristmasMonsterNum.addAndGet(number);
		this.update();
		
		return afterNum;
	}
	
	/**
	 * 添加
	 * @param number
	 * @return
	 */
	public int addSummonedChristmasNum(int number) {
		int afterNum = this.summonedMonsterNum.addAndGet(number);
		this.update();
		
		return afterNum;
	}
	public long getLastResetTime() {
		return lastResetTime;
	}
	public void setLastResetTime(long lastResetTime) {
		this.lastResetTime = lastResetTime;
	}
	
	public boolean needReset() {
		return !HawkTime.isSameDay(lastResetTime, HawkTime.getMillisecond());
	}
	
	public void reset() {
		this.lastResetTime = HawkTime.getMillisecond();
		this.killMonsterNum = new AtomicInteger();
		this.killChristmasMonsterNum = new AtomicInteger();
		this.summonedMonsterNum = new AtomicInteger();
		
		this.saveToRedis();
	}
}
 