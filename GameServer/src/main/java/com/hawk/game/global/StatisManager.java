package com.hawk.game.global;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.hawk.os.HawkOSOperator;

import com.hawk.game.GsConfig;

public class StatisManager {
	/**
	 * 每个类型entity的操作信息
	 */
	Map<String, AtomicInteger> statisRedisKey;

	/**
	 * 实例对象
	 */
	private static StatisManager instance;

	/**
	 * 取实例对象
	 * 
	 * @return
	 */
	public static StatisManager getInstance() {
		if (instance == null) {
			instance = new StatisManager();
		}

		return instance;
	}

	/**
	 * 私有构造
	 */
	private StatisManager() {

	}

	/**
	 * 初始化
	 * 
	 * @return
	 */
	public boolean init() {
		statisRedisKey = new ConcurrentHashMap<String, AtomicInteger>();
		return true;
	}

	/**
	 * 记录key
	 * 
	 * @param key
	 * @return
	 */
	public int incRedisKey(String key) {
		if (statisRedisKey != null && GsConfig.getInstance().isAnalyzerEnable() && !HawkOSOperator.isEmptyString(key)) {
			AtomicInteger count = statisRedisKey.get(key);
			if (count == null) {
				statisRedisKey.putIfAbsent(key, new AtomicInteger(0));
				count = statisRedisKey.get(key);
			}
			return count.incrementAndGet();
		}
		return 0;
	}

	/**
	 * 获取RedisKey统计信息
	 * 
	 * @return
	 */
	public Map<String, Integer> getRedisKeyStatis() {
		Map<String, Integer> countMap = new HashMap<String, Integer>();
		for (Entry<String, AtomicInteger> entry : statisRedisKey.entrySet()) {
			countMap.put(entry.getKey(), entry.getValue().get());
		}
		return countMap;
	}
}
