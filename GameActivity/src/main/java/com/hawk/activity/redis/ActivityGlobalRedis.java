package com.hawk.activity.redis;

import java.util.*;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.redis.HawkRedisSession;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.entity.ActivityAccountRoleInfo;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

/**
 * 本服的redis操作 key的前缀采用 serverName:type:key eg: s1:chat:1
 * @author PhilChen
 *
 */
public class ActivityGlobalRedis {
	
	/**
	 * redis会话对象
	 */
	HawkRedisSession redisSession;
	
	/**
	 * 全局实例对象
	 */
	private static ActivityGlobalRedis instance = null;

	/**
	 * 获取实例对象
	 *
	 * @return
	 */
	public static ActivityGlobalRedis getInstance() {
		if (instance == null) {
			instance = new ActivityGlobalRedis();
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

	public long zadd(String key, double score, String member) {
		return redisSession.zAdd(key, score, member);
	}
	
	/**
	 * 移除有序集 key 中的一个或多个成员
	 * @param key
	 * @param members
	 */
	public void zrem(String key, String... members){
		redisSession.zRem(key,0, members);
	}
	
	/**
	 * 获取有序集 key 中成员 member 的排名。其中有序集成员按 score 值递减(从大到小)排序
	 * @param key
	 * @param member
	 * @return
	 */
	public long zrevrank(String key, String member) {
		return redisSession.zrevrank(key, member,0);
	}

	public RedisIndex zrevrankAndScore(String key, String member) {
		Long index = redisSession.zrevrank(key, member, 0);
		Double score = redisSession.zScore(key, member, 0);
		if (index != null && index >= 0 && Objects.nonNull(score)) {
			return new RedisIndex(index, score);
		}
		return null;
	}
	/**
	 * 向列表 key 的表头插入元素
	 * @param key
	 * @param values
	 * @return
	 */
	public Long lpush(String key, String... values) {
		return redisSession.lPush(key,0, values);
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
	
	/**
	 * 设置key的值
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean set(String key, byte[] value) {
		return redisSession.setBytes(key, value);
	}
	
	/**
	 * 设置key的值
	 * @param key
	 * @param value
	 * @param expireSeconds
	 * @return
	 */
	public boolean set(String key, String value, int expireSeconds) {
		return redisSession.setString(key, value, expireSeconds);
	}
	
	/**
	 * 设置key的值
	 * @param key
	 * @param value
	 * @param expireSeconds
	 * @return
	 */
	public boolean set(String key, byte[] value, int expireSeconds) {
		return redisSession.setBytes(key, value, expireSeconds);
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
	
	/**
	 * 设置哈希表中指定域的值，顺便设置过期时间
	 * @param key
	 * @param field
	 * @param value
	 * @param expireTime
	 */
	public void hset(String key, String field, String value, int expireTime) {
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
	 * 删除哈希表key中指定域对应的值
	 * @param key
	 * @param field
	 * @return
	 */
	public long hDel(String key, String field) {
		return redisSession.hDel(key, field);
		
	}
	
	/**
	 * 获取所有的成员.
	 * @param key
	 * @return
	 */
	public Set<String> sMembers(String key) {
		return redisSession.sMembers(key);
	}
	
	public Long sAdd(String key, int expireSeconds, String... members) {
		return redisSession.sAdd(key, expireSeconds, members);
	}
	
	/**
	 * 判断是否在set里面
	 * @param key
	 * @param member
	 * @return
	 */
	public boolean sIsMembers(String key, String member) {
		return redisSession.sIsmember(key, member);
	}
	
	/**
	 * 为key存储的数字值 + 1，
	 * key不存在则初始化0 在执行命令
	 * 存储的值不能解释给数字，返回错误
	 */
	public long increase(String key) {
		return redisSession.increase(key);
	}
	/***
	 * 获取平台所有角色信息{json<-->AccountRoleInfo}
	 * @param openId 帐号信息
	 * @return
	 */
	public List<String> getAccountRoleList(String openId){
		List<String> list = new ArrayList<>();
		String key = ActivityManager.getInstance().getDataGeter().getAccountRoleInfoKey(openId);
		Map<String, String> map = redisSession.hGetAll(key);
		for (String value : map.values()) {
			list.add(value);
		}
		return list;
	}
	
	/**
	 * 获取 activityAccountRoleInfo;
	 * @param openId
	 * @return
	 */
	public List<ActivityAccountRoleInfo> getActivityAccountRoleList(String openId) {
		List<String> roleList = getAccountRoleList(openId);
		List<ActivityAccountRoleInfo> activityRoleList = new ArrayList<>();
		for (String role : roleList) {
			try {
				ActivityAccountRoleInfo activityRole = JSON.parseObject(role, ActivityAccountRoleInfo.class);
				activityRoleList.add(activityRole);
			} catch (Exception e) {
				HawkException.catchException(e);
			}			
		}
		
		return activityRoleList;
	}
	
	/**
	 * 获取流失的玩家
	 * @param openId
	 * @return
	 */
	public List<ActivityAccountRoleInfo> getLostAccoutRoleList(Set<String> friendOpenId, long leaveTime, int cityLevel, int playerLevel, int vip) {
		List<ActivityAccountRoleInfo> roleInfoList = new ArrayList<>();
		List<Object> retObjs = null; 
		try (Jedis jedis = redisSession.getJedis(); Pipeline pip = jedis.pipelined()) {
			for (String openid : friendOpenId) {
				String key = ActivityManager.getInstance().getDataGeter().getAccountRoleInfoKey(openid);
				pip.hgetAll(key);
			}

			retObjs = pip.syncAndReturnAll();
		} catch (Exception e) {
			HawkException.catchException(e);
			return Collections.emptyList();
		}

		if (retObjs == null) {
			HawkLog.errPrintln("getLostAccoutRoleList failed, openid count: {}", friendOpenId.size());
			
			return roleInfoList;
		}
		
		try {			
			long curTime = HawkTime.getMillisecond();
			for (Object obj : retObjs) {
				@SuppressWarnings("unchecked")
				Map<String, String> map = (Map<String, String>) obj;
				List<ActivityAccountRoleInfo> sortList = new ArrayList<>();
				for (String roleStr : map.values()) {
					ActivityAccountRoleInfo activityRole = JSONObject.parseObject(roleStr, ActivityAccountRoleInfo.class);
					 //登录时间大于退出时间说明是在线的, 或者离线没有超过一定的期限.
					if (activityRole.getLoginTime() > activityRole.getLogoutTime() ||
							curTime - activityRole.getLogoutTime() < leaveTime) {
						sortList.clear();
						HawkLog.logPrintln("getLostAccoutRoleList failed, openId:{}, playerName:{}, loginTime:{}, logoutTime:{}  leaveTime:{} ",
								activityRole.getOpenId(), activityRole.getPlayerName(), activityRole.getLoginTime(), activityRole.getLogoutTime(), leaveTime);
						break;
					}
					
					if (activityRole.getCityLevel() < cityLevel) {
						continue;
					}
					
					if (activityRole.getPlayerLevel() < playerLevel) {
						continue;
					}
					
					if (activityRole.getVipLevel() < vip) {
						continue;
					}
					
					sortList.add(activityRole);
				}				
				
				if (sortList.isEmpty()) {
					continue;
				}
				
				Collections.sort(sortList);
				roleInfoList.add(sortList.get(0));
			} 
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return roleInfoList;
		
	}

	public HawkRedisSession getRedisSession() {
		return redisSession;
	}
	
	public boolean expire(String key, int expireSeconds){
		return redisSession.expire(key, expireSeconds);
	}

	public boolean hIncrBy(String key, String field, long value) {
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			boolean e;
			try {
				e = jedis.hincrBy(key, field, value).longValue() > 0L;
			} catch (Exception arg8) {
				HawkException.catchException(arg8, new Object[0]);
				return false;
			} finally {
				jedis.close();
			}

			return e;
		} else {
			return false;
		}
	}
}