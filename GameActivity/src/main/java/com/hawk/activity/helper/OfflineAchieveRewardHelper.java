package com.hawk.activity.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.os.HawkException;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;

/***
 * 连续多期活动成就达成离线奖励
 * @author yang.rao
 *
 */
public class OfflineAchieveRewardHelper {
	
	private static final String obj_sep = "\\|";
	
	private static final String obj_join = "|";
	
	private static final String fie_sep = "_";
	
	//玩家操作对象锁
	private static volatile Map<String, Object> locks = new HashMap<String, Object>();
	
	/***
	 * 添加一个玩家离线补发领取成就奖励到哪一期
	 * @param playerId
	 * @param itemId 离线补发到多少期
	 * @param activityId 活动id
	 */
	public static void addAchieveRewardData(String playerId, int itemId, int activityId){
		ensureLock(playerId);
		Object lock = locks.get(playerId);
		synchronized (lock) {
			List<AchieveValues> curlist = getOfflineAchieveReward(playerId);
			if(curlist == null){
				curlist = new ArrayList<>();
			}
			boolean contain = false;
			for(AchieveValues av : curlist){
				if(av.getActivityId() == activityId){
					contain = true;
					break;
				}
			}
			if(contain){
				throw new RuntimeException("add achieve reward data, but redis contain this activity");
			}
			AchieveValues av = new AchieveValues(activityId, itemId);
			curlist.add(av);
			String values = List2String(curlist);
			ActivityLocalRedis.getInstance().hset(ActivityRedisKey.getOFFLINE_ACHIEVE_REWARD_KEY(), playerId, values);
		}
	}	
	
	/***
	 * 更新itemId
	 * @param playerId
	 * @param itemId
	 * @param activityId
	 */
	public static void updateAchieveRewardData(String playerId, int itemId, int activityId){
		ensureLock(playerId);
		Object lock = locks.get(playerId);
		synchronized (lock) {
			List<AchieveValues> curlist = getOfflineAchieveReward(playerId);
			if(curlist == null){
				curlist = new ArrayList<>();
			}
			boolean updata = false;
			for(AchieveValues av : curlist){
				if(av.activityId == activityId){
					av.setItemId(itemId);
					updata = true;
					break;
				}
			}
			if(updata){
				String values = List2String(curlist);
				ActivityLocalRedis.getInstance().hset(ActivityRedisKey.getOFFLINE_ACHIEVE_REWARD_KEY(), playerId, values);
			}else{
				throw new RuntimeException("updata not contain activity item data");
			}
		}
	}
	
	/***
	 * 获取活动离线补发奖励到多少期
	 * @param playerId
	 * @param activityId
	 * @return -1表示无记录
	 */
	public static int getAchieveRewardItem(String playerId, int activityId){
		List<AchieveValues> curlist = getOfflineAchieveReward(playerId);
		if(curlist != null){
			for(AchieveValues av : curlist){
				if(av.getActivityId() == activityId){
					return av.getItemId();
				}
			}
		}
		return -1;
	}
	
	public static void removeAll(String filed){
		ActivityLocalRedis.getInstance().hDel(ActivityRedisKey.getOFFLINE_ACHIEVE_REWARD_KEY(), filed);
	}
	
	private static void ensureLock(String playerId){
		Object obj = locks.get(playerId);
		if(obj == null){
			synchronized (OfflineAchieveRewardHelper.class) {
				obj = locks.get(playerId);
				if(obj == null){
					obj = new Object();
					locks.put(playerId, obj);
				}
			}
		}
	}
	
	/***
	 * 从redis获取玩家离线成就奖励数据
	 * @param playerId
	 * @return
	 */
	private static List<AchieveValues> getOfflineAchieveReward(String playerId){
		String values = ActivityLocalRedis.getInstance().hget(ActivityRedisKey.getOFFLINE_ACHIEVE_REWARD_KEY(), playerId);
		if(values != null){
			return parseAchieveValues(values);
		}
		return null;
	}
	
	private static List<AchieveValues> parseAchieveValues(String values){
		try {
			String[] src = values.split(obj_sep);
			List<AchieveValues> list = new ArrayList<>();
			for(String val : src){
				String [] v = val.split(fie_sep);
				int activityId = Integer.parseInt(v[0]);
				int itemId = Integer.parseInt(v[1]);
				AchieveValues av = new AchieveValues(activityId, itemId);
				list.add(av);
			}
			return list;
		} catch (Exception e) {
			HawkException.catchException(e);
			return null;
		}
	}
	
	private static String List2String(List<AchieveValues> list) {
		StringBuilder sbuild = new StringBuilder();
		for(AchieveValues va : list){
			sbuild.append(va.toString())
			.append(obj_join);
		}
		return sbuild.toString();
	}
	
	public static class AchieveValues{
		//活动Id
		private int activityId;		
		//活动期数
		private int itemId;
		
		public AchieveValues(int activityId, int itemId){
			this.activityId = activityId;
			this.itemId = itemId;
		}
		public int getActivityId() {
			return activityId;
		}
		public int getItemId() {
			return itemId;
		}
		public void setItemId(int itemId) {
			this.itemId = itemId;
		}
		@Override
		public String toString() {
			return activityId + fie_sep + itemId;
		}		
	}
}
