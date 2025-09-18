package com.hawk.activity.type.impl.allianceCarnival.entity;

import java.util.Optional;

import org.hawk.os.HawkTime;
import org.hawk.redis.HawkRedisSession;
import org.hawk.uuid.HawkUUIDGenerator;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.allianceCarnival.cfg.AllianceCarnivalAchieveCfg;
import com.hawk.game.protocol.Activity.ACMissionState;

/**
 * 联盟总动员任务信息 alliance carnival mission info
 * @author golden
 *
 */
public class ACMInfo {

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
	 * 任务uuid
	 */
	public String uuid;

	/**
	 * 任务id
	 */
	public int achieveId;

	/**
	 * 任务刷新出来的时间
	 */
	public long refreshTime;
	
	/**
	 * 
	 * @param acmInfo
	 */
	public ACMInfo(String acmInfo) {
		String[] info = acmInfo.split("_");
		this.uuid = info[0];
		this.achieveId = Integer.parseInt(info[1]);
		this.refreshTime = Long.parseLong(info[2]);
	}
	
	/**
	 * 构造 刷新初始任务的时候调用
	 */
	public ACMInfo(AllianceCarnivalAchieveCfg cfg) {
		this.uuid = HawkUUIDGenerator.genUUID();
		this.achieveId = cfg.getAchieveId();
		this.refreshTime = HawkTime.getMillisecond();
	}

	/**
	 * 构造 领取任务刷新的时候调用
	 */
	public ACMInfo(AllianceCarnivalAchieveCfg cfg, long refreshDelay) {
		this.uuid = HawkUUIDGenerator.genUUID();
		this.achieveId = cfg.getAchieveId();
		this.refreshTime = HawkTime.getMillisecond() + refreshDelay;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public int getAchieveId() {
		return achieveId;
	}

	public void setAchieveId(int achieveId) {
		this.achieveId = achieveId;
	}

	public long getRefreshTime() {
		return refreshTime;
	}

	public void setRefreshTime(long refreshTime) {
		this.refreshTime = refreshTime;
	}

	public ACMissionState getState() {
		if (HawkTime.getMillisecond() - refreshTime > 0) {
			return ACMissionState.AC_CAN_RECEIVE;
		} else {
			return ACMissionState.AC_REFRESHING;
		}
	}
	
	public String toString() {
		return 	uuid + "_" + achieveId + "_" + refreshTime;
	}
	
	/**
	 * 删除任务
	 */
	public void remove(String guildId) {
		Optional<ActivityBase> activityOp = ActivityManager.getInstance().getActivity(ActivityType.ALLIANCE_CARNIVAL.intValue());
		if (!activityOp.isPresent()) {
			return;
		}
		
		int termId = activityOp.get().getActivityTermId();
		
		HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
		redisSession.hDel(getACMKey(termId, guildId), uuid);
	}
	
	/**
	 * redis的key
	 */
	public String getACMKey(int termId, String guildId) {
		return String.format(ACM_INFO, termId, guildId);
	}
}
