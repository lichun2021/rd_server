package com.hawk.activity.type.impl.allianceCarnival.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.redis.HawkRedisSession;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.allianceCarnival.cfg.AllianceCarnivalCfg;
import com.hawk.activity.type.impl.allianceCarnival.cfg.AllianceCarnivalLevelCfg;

/**
 * 联盟总动员联盟基础信息 alliance carnival base info
 * @author golden
 *
 */
public class ACBInfo {

	/**
	 * 联盟总动员任务信息
	 */
	public static final String ACM_INFO = "acm_info:%s:%s";
	
	/**
	 * 联盟总动员基础信息
	 */
	public static final String ACB_INFO = "acb_info:%s:%s";
	
	/**
	 * redis key 过期时间
	 */
	public static final int EXPIRESECONDS = 3600 * 24 * 30;
	
	/**
	 * 联盟id
	 */
	public String guildId;

	/**
	 * 总经验
	 */
	public AtomicInteger exp = new AtomicInteger();

	/**
	 * 基础奖励发放等级
	 */
	public int baseSendLevel;

	/**
	 * 进阶奖励发放等级
	 */
	public int advanceSendLevel;

	/**
	 * 参与人数
	 */
	public AtomicInteger joinCount = new AtomicInteger();

	public ACBInfo(String info, boolean init) {
		String[] infoArray = info.split("_");
		this.guildId = infoArray[0];
		this.exp = new AtomicInteger(Integer.parseInt(infoArray[1]));
		this.baseSendLevel = Integer.parseInt(infoArray[2]);
		this.advanceSendLevel = Integer.parseInt(infoArray[3]);
		this.joinCount = new AtomicInteger(Integer.parseInt(infoArray[4]));
	}

	public ACBInfo(String guildId) {
		this.guildId = guildId;
		this.exp = new AtomicInteger(AllianceCarnivalCfg.getInstance().getInitExp());
		this.joinCount = new AtomicInteger();
	}

	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}

	public int getExp() {
		return exp.get();
	}

	public int getBaseSendLevel() {
		return baseSendLevel;
	}

	public void setBaseSendLevel(int baseSendLevel) {
		this.baseSendLevel = baseSendLevel;
		notifyUpdate();
	}

	public int getAdvanceSendLevel() {
		return advanceSendLevel;
	}

	public void setAdvanceSendLevel(int advanceSendLevel) {
		this.advanceSendLevel = advanceSendLevel;
		notifyUpdate();
	}

	public int getJoinCount() {
		return joinCount.get();
	}

	public void addJoinCount(int addJoinCount) {
		this.joinCount.addAndGet(addJoinCount);
		notifyUpdate();
	}

	public void addExp(int addExp) {
		this.exp.addAndGet(addExp);
		notifyUpdate();
	}

	/**
	 * 获取当前等级
	 */
	public int getCurrentLevel() {

		int currentLevel = 0;
		ConfigIterator<AllianceCarnivalLevelCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(AllianceCarnivalLevelCfg.class);
		while (iterator.hasNext()) {
			
			AllianceCarnivalLevelCfg config = iterator.next();
			
			if (this.exp.get() >= config.getLevelUpExp()) {
				currentLevel = config.getLevel();
				
			} else {
				break;
			}
		}

		return currentLevel;
	}

	/**
	 * 获取当前经验
	 */
	public int getCurrentExp() {
		int currentExp = this.exp.get();

		ConfigIterator<AllianceCarnivalLevelCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(AllianceCarnivalLevelCfg.class);
		while (iterator.hasNext()) {
			
			AllianceCarnivalLevelCfg config = iterator.next();
			
			if (this.exp.get() >= config.getLevelUpExp()) {
				currentExp = this.exp.get() - config.getLevelUpExp();
			} else {
				break;
			}
		}

		return currentExp;
	}

	public Map<Integer, Integer> getLevelMap() {
		Map<Integer, Integer> levelMap = new HashMap<>();

		int allExp = 0;

		ConfigIterator<AllianceCarnivalLevelCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(AllianceCarnivalLevelCfg.class);
		while (iterator.hasNext()) {
			AllianceCarnivalLevelCfg config = iterator.next();
			allExp += config.getLevelUpExp();
			levelMap.put(config.getLevel(), allExp);
		}
		return levelMap;
	}

	public String toString() {
		return guildId + "_" + exp + "_" + baseSendLevel + "_" + advanceSendLevel + "_" + joinCount;
	}
	
	/**
	 * 通知更新
	 */
	public void notifyUpdate() {
		Optional<ActivityBase> activityOp = ActivityManager.getInstance().getActivity(ActivityType.ALLIANCE_CARNIVAL.intValue());
		if (!activityOp.isPresent()) {
			return;
		}
		
		int termId = activityOp.get().getActivityTermId();
		
		HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
		redisSession.setString(getACBKey(termId, guildId), toString(), EXPIRESECONDS);
	}
	
	/**
	 * redis的key
	 */
	public String getACBKey(int termId, String guildId) {
		return String.format(ACB_INFO, termId, guildId);
	}
}
