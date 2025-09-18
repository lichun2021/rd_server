package com.hawk.activity.redis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.hawk.os.HawkException;
import org.hawk.redis.HawkRedisSession;

import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.rewardOrder.cfg.RewardOrderCfg;

import redis.clients.jedis.Tuple;

/**
 * 本服的redis操作 key的前缀采用 serverName:type:key eg: s1:chat:1
 * @author PhilChen
 *
 */
public class ActivityLocalRedis {
	
	/**
	 * redis会话对象
	 */
	HawkRedisSession redisSession;
	
	/**
	 * 全局实例对象
	 */
	private static ActivityLocalRedis instance = null;

	/**
	 * 获取实例对象
	 *
	 * @return
	 */
	public static ActivityLocalRedis getInstance() {
		if (instance == null) {
			instance = new ActivityLocalRedis();
		}
		return instance;
	}

	/**
	 * 初始化活动redis对象
	 * 
	 * @param redisSession
	 */
	public void init(HawkRedisSession redisSession) {
		this.redisSession = redisSession;
	}
	
	
	
	public HawkRedisSession getRedisSession() {
		return redisSession;
	}

	/**
	 * 将member 元素及其 score 值加入到有序集 key当中
	 * @param key
	 * @param score
	 * @param member
	 * @return
	 */
	public long zadd(String key, double score, String member) {
		return redisSession.zAdd(key, score, member);
	}
	
	public long zaddWithExpire(String key, double score, String member, int expireSeconds){
		return redisSession.zAdd(key, score, member, expireSeconds);
	}
	
	/**
	 * 移除有序集 key 中的一个或多个成员
	 * @param key
	 * @param member
	 */
	public void zrem(String key, String... members){
		redisSession.zRem(key,0, members);
	}
	
	/**
	 * 获取有序集 key 中， score 值介于 max 和 min 之间(默认包括等于 max 或 min )的所有的成员。有序集成员按 score 值递减(从大到小)的次序排列
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 */
	public Set<Tuple> zrevrange(String key, long start, long end) {
		return redisSession.zRevrangeWithScores(key, start, end,0);
	}
	
	public Set<Tuple> zrevrangeWithExipre(String key, long start, long end, int expireTime){
		return redisSession.zRevrangeWithScores(key, start, end, expireTime);
	}
	
	public RedisIndex zrevrank(String key, String member) {
		Long index = redisSession.zrevrank(key, member, 0);
		Double score = redisSession.zScore(key, member, 0);
		if (index != null && index >= 0 && Objects.nonNull(score)) {
			return new RedisIndex(index, score);
		}
		return null;
	}
	
	/**
	 * 获取有序集 key 中成员 member 的排名信息。其中有序集成员按 score 值递减(从小到大)排序
	 * @param key
	 * @param member
	 * @return
	 */
	public RedisIndex zrank(String key, String member) {
		Long index = redisSession.zRank(key, member);
		Double score = redisSession.zScore(key, member,0);
		if (index != null && index >= 0 && Objects.nonNull(score)) {
			return new RedisIndex(index, score);
		}
		return null;
	}
	
	/**
	 * 为有序集key的成员member的score值加上增量increament
	 * @param key
	 * @param member
	 * @param increament
	 * @param expireSeconds
	 * @return
	 */
	public Double zIncrby(String key, String member, double increament){
		return redisSession.zIncrby(key, member, increament, 0);
	}
	
	public Double zIncrbyWithExpire(String key, String member, double increament, int expire){
		return redisSession.zIncrby(key, member, increament, expire);
	}
	
	/**
	 * 获取有序集 key 中，成员 member 的 score 值
	 * 
	 * @param key
	 * @param member
	 * @return
	 */
	public Double zScore(String key, String member){
		return redisSession.zScore(key, member, 0);
	}
	
	/**
	 * 向列表 key 的表头插入元素
	 * @param key
	 * @param value
	 * @return
	 */
	public long lpush(String key, String... value) {
		return redisSession.lPush(key, 0, value);
	}
	
	/***
	 * 插入数据，附带过期时间
	 * @param key
	 * @param expireTime
	 * @param value
	 * @return
	 */
	public long lpush(String key, int expireTime, String... value){
		return redisSession.lPush(key, expireTime, value);
	}
	
	public long lpush(byte[] key, int expireTime, byte[]... value){
		return redisSession.lPush(key, expireTime, value);
	}
	
	/**
	 * 获取列表 key 中指定区间内的元素
	 * @param key
	 * @return
	 */
	public List<String> lall(String key) {
		return redisSession.lRange(key, 0, Long.MAX_VALUE,0);
	}
	
	public List<byte[]> lall(byte[] key) {
		return redisSession.lRange(key, 0, Long.MAX_VALUE,0);
	}
	
	/**
	 * 删除key
	 * @param key
	 * @return
	 */
	public Long del(String key) {
		return redisSession.del(key);
	}
	
	/**
	 * 删除key
	 * @param key
	 * @return
	 */
	public Long del(byte[] key) {
		return redisSession.del(key);
	}
	
	/**
	 * 获取key对应的值
	 * @param key
	 * @return
	 */
	public String get(String key) {
		return redisSession.getString(key);
	}
	
	/**
	 * 设置key的值
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean set(String key, String value) {
		return redisSession.setString(key, value);
	}
	
	public boolean set(String key, String value, int expire){
		return redisSession.setString(key, value, expire);
	}
	
	/**
	 * 获取哈希表key中指定域对应的值
	 * @param key
	 * @param field
	 * @return
	 */
	public String hget(String key, String field) {
		return redisSession.hGet(key, field);
	}
	
	/**
	 * 设值哈希表key中指定域的值
	 * @param key
	 * @param field
	 * @param value
	 */
	public void hset(String key, String field, String value) {
		redisSession.hSet(key, field, value);
	}
	
	public void hsetWithExpire(String key, String field, String value, int expireTime) {
		redisSession.hSet(key, field, value, expireTime);
	}
	
	public void hmset(String key, Map<String, String> map, int expireTime){
		redisSession.hmSet(key, map, expireTime);
	}
	
	/**
	 * 获取哈希表key中所有的域和对应的值
	 * @param key
	 * @return
	 */
	public Map<String, String> hgetAll(String key) {
		return redisSession.hGetAll(key);
	}
	
	/**
	 * 删除哈希表 key 中中指定的域
	 * @param key
	 * @param fileds
	 * @return
	 */
	public long hDel(String key, String... fileds){
		return redisSession.hDel(key, fileds);
	}
	
	public boolean hsetActivityAchieveCfg(int itemId, int activityId, List<AchieveConfig> cfgs, int expireSeconds) throws HawkException{
		String achieveKey = getAchieveKey(activityId, itemId);
		boolean exist = redisSession.exists(achieveKey);
		if(exist){
			throw new HawkException(String.format("hsetActivityAchieveCfg error, exist item: %d, activityId: %d", itemId, activityId));
		}
		Map<String, String> map = new HashMap<>();
		for(AchieveConfig cfg : cfgs){
			if(cfg instanceof RewardOrderCfg){
				map.put(String.valueOf(((RewardOrderCfg) cfg).getId()), cfg.getReward());
			}else{
				map.put(String.valueOf(cfg.getAchieveId()), cfg.getReward());
			}
		}
		boolean result = redisSession.hmSet(achieveKey, map,expireSeconds);
		if(result){
			return true;
		}
		return false;
	}

	public boolean hIncrBy(String key, String field, int value){
		return redisSession.hIncrBy(key, field, value);
	}

	public String getActivityAchieveRewards(int itemId, int activityId, int achieveId){
		return redisSession.hGet(getAchieveKey(activityId, itemId), String.valueOf(achieveId));
	}
	
	private String getAchieveKey(int activityId, int itemId){
		return String.format("%s:%s:%s", ActivityRedisKey.getACHIEVE_ACTIVITY_CFG_KEY(), activityId, itemId);
	}
	
	/**
	 * 将一个或多个 member 元素加入到集合 key 当中
	 * @param key
	 * @param members
	 * @return
	 */
	public Long sadd(String key, String... members){
		return redisSession.sAdd(key,0, members);
	}
	
	/**
	 * 获取集合 key 中的所有成员
	 * @param key
	 * @return
	 */
	public Set<String> sMembers(String key) {
		return redisSession.sMembers(key);
	}
	
	/**
	 * 移除集合 key 中的一个或多个 member 元素
	 * @param key
	 * @param members
	 * @return
	 */
	public long sRem(String key, String... members){
		return redisSession.sRem(key, members);
	}
	
	/**
	 * 将一个或多个 member 元素加入到集合 key 当中
	 * @param key
	 * @param member
	 * @return
	 */
	public boolean sIsmember(String key, String member){
		return redisSession.sIsmember(key, member);
	}
	
	/***
	 * 降序查询zset集合
	 * @param key
	 * @return
	 */
	public Set<Tuple> zRevrangeWithScores(String key){
		return redisSession.zRevrangeWithScores(key, 0, -1, 0);
	}
	
	public Set<String> zRange(String key, long start, long end, int expireTime){
		return redisSession.zRange(key, start, end, expireTime);
	}
	/**
	 * 用于为哈希表中不存在的字段赋值
	 * 不存在hset操作
	 * 已存在 操作无效
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 */
	public long hSetNx(String key, String field, String value){
		return redisSession.hSetNx(key, field, value);
	}
	
	public boolean expire(String key, int expireSeconds){
		return redisSession.expire(key, expireSeconds);
	}
	
	public boolean exists(String key){
		return redisSession.exists(key);
	}
	
	
	
	
	/**
	 * 将members 加入到key当中
	 * 
	 * @param key
	 * @param members
	 */
	public Long zadd(String key, Map<String, Double> members,int expireSeconds) {
		return this.redisSession.zAdd(key, members, expireSeconds);
	}
	
	
	/**
	 * 将members 加入到key当中
	 * 
	 * @param key
	 * @param members
	 */
	public Long sadd(String key, int expireSeconds,String... members) {
		return this.redisSession.sAdd(key, expireSeconds, members);
	}
	
	
}