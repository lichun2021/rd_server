package com.hawk.game.nation.mission;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.GsApp;
import com.hawk.game.config.NationConstCfg;
import com.hawk.game.config.NationMissionTaskCfg;
import com.hawk.game.entity.NationConstructionEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.nation.NationalBuilding;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.National.NationbuildingType;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.RandomUtil;
import com.hawk.gamelog.GameLog;
import com.hawk.gamelog.LogParam;
import com.hawk.log.LogConst.LogInfoType;

/**
 * 国家任务中心
 * 
 * @author Golden
 *
 */
public class NationMissionCenter extends NationalBuilding {

	/**
	 * 日志
	 */
	public static final Logger logger = LoggerFactory.getLogger("Server");

	/**
	 * 国家任务集合
	 */
	private Map<String, NationMissionTemp> nationMissions = new ConcurrentHashMap<>();

	/**
	 * 国家任务检测增加时间(每隔x秒,增加一个任务)
	 */
	private long nationMissonCheckAddTime = 0L;

	/**
	 * 哪天刷新的(用于跨天检测)
	 */
	private int nationMissionRefreshDay = 0;

	/**
	 * 缓存落地时间
	 */
	private long flushRedisTime = 0;

	/**
	 * 构造方法
	 * 
	 * @param entity
	 * @param buildType
	 */
	public NationMissionCenter(NationConstructionEntity entity, NationbuildingType buildType) {
		super(entity, buildType);
	}

	@Override
	public boolean init() {
		nationMissions = RedisProxy.getInstance().getNationMissions();
		nationMissonCheckAddTime = RedisProxy.getInstance().getNationMissionCheckAddTime();
		nationMissionRefreshDay = RedisProxy.getInstance().getNationMissionRefreshDay();
		flushRedisTime = HawkTime.getMillisecond();
		return true;
	}

	@Override
	public void levelupStart() {

	}

	@Override
	public void levelupOver() {
		// 检测任务刷新
		checkMissionRefresh();
	}

	@Override
	public void buildingTick(long nowTime) {
		if (!GsApp.getInstance().isInitOK()) {
			return;
		}

		// 检测
		doCheck();
	}

	/**
	 * 检测
	 */
	public void doCheck() {
		// 检测任务刷新
//		checkMissionRefresh();

		// 检测任务增加
		checkMissionAdd();

		// 定时落地缓存信息
		tickFlushToRedis();

		// 检测任务消失
		checkMissionDisapper();
	}

	/**
	 * 检测任务重置(初始化刷新/跨天刷新)
	 */
	private void checkMissionRefresh() {

		if (getLevel() != 1) {
			return;
		}

		int yearDay = HawkTime.getYearDay();
		if (yearDay == nationMissionRefreshDay) {
			return;
		}
		nationMissionRefreshDay = yearDay;

		// 重置任务增加时间
		nationMissonCheckAddTime = HawkTime.getMillisecond();

		// 刷新任务
		Map<String, NationMissionTemp> newMissions = new ConcurrentHashMap<>();
		// 刷新任务数量
		int refreshCount = NationConstCfg.getInstance().getMissionInitCount();

		boolean hasTimeLimitMission = false;
		
		// 刷新任务
		List<NationMissionTaskCfg> collect = getRandMissionList();
		for (int i = 0; i < refreshCount; i++) {
			NationMissionTaskCfg randomCfg = RandomUtil.random(collect);
			if (randomCfg.getTimeLimit() > 0) {
				hasTimeLimitMission = true;
			}
			NationMissionTemp missionItem = new NationMissionTemp(randomCfg.getMissionId());
			newMissions.put(missionItem.getUuid(), missionItem);
		}
		this.nationMissions = newMissions;

		// 通知刷新
		notifyRefresh();
		
		if (hasTimeLimitMission) {
			noticeTimeLimitMissionRefresh();
		}
	}

	/**
	 * 检测任务增加
	 */
	private void checkMissionAdd() {

		if (getLevel() <= 0) {
			return;
		}

		// 未到任务增加周期
		if (HawkTime.getMillisecond() - nationMissonCheckAddTime < NationConstCfg.getInstance()
				.getMissionRefreshTime()) {
			return;
		}
		// 任务数量达到上限
		if (nationMissions.size() >= NationConstCfg.getInstance().getMissionTaskLimit()) {
			return;
		}

		// 任务刷新数量
		long refreshCount = (HawkTime.getMillisecond() - nationMissonCheckAddTime)
				/ NationConstCfg.getInstance().getMissionRefreshTime();
		refreshCount = Math.min(refreshCount,
				NationConstCfg.getInstance().getMissionTaskLimit() - nationMissions.size());

		// 重置任务增加时间
		nationMissonCheckAddTime = HawkTime.getMillisecond();
		
		boolean hasTimeLimit = false;
		
		// 刷新任务
		List<NationMissionTaskCfg> collect = getRandMissionList();
		for (int i = 0; i < refreshCount; i++) {
			NationMissionTaskCfg randomCfg = RandomUtil.random(collect);
			if (randomCfg.getTimeLimit() > 0L) {
				hasTimeLimit = true;
			}
			NationMissionTemp missionItem = new NationMissionTemp(randomCfg.getMissionId());
			nationMissions.put(missionItem.getUuid(), missionItem);
		}

		// 通知刷新
		if (refreshCount > 0) {
			notifyRefresh();
			
			if (hasTimeLimit) {
				noticeTimeLimitMissionRefresh();
			}
		}
	}

	/**
	 * 通知限时任务刷出来了
	 */
	private void noticeTimeLimitMissionRefresh() {
		try {
			ChatParames.Builder builder = ChatParames.newBuilder();
			builder.setKey(Const.NoticeCfgId.NATION_MISSION_NOTICE_368);
			builder.setChatType(Const.ChatType.SPECIAL_BROADCAST);
			ChatService.getInstance().addWorldBroadcastMsg(builder.build());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 检测任务消失
	 */
	public void checkMissionDisapper() {
		long currentTime = HawkTime.getMillisecond();
		List<String> removeList = new ArrayList<>();
		for (NationMissionTemp nation : nationMissions.values()) {
			NationMissionTaskCfg cfg = HawkConfigManager.getInstance().getConfigByKey(NationMissionTaskCfg.class,
					nation.getMissionId());
			if (cfg.getTimeLimit() > 0 && nation.getRefreshTime() + cfg.getTimeLimit() < currentTime) {
				removeList.add(nation.getUuid());
				tlogDisapper(nation.getMissionId(), cfg.getPickupTime() - nation.getPickUpTimes());
			}
		}

		for (String uuid : removeList) {
			nationMissions.remove(uuid);
		}
	}

	/**
	 * 获取可随机的任务列表
	 */
	private List<NationMissionTaskCfg> getRandMissionList() {
		List<NationMissionTaskCfg> retList = new ArrayList<>();
		ConfigIterator<NationMissionTaskCfg> cfgIter = HawkConfigManager.getInstance()
				.getConfigIterator(NationMissionTaskCfg.class);
		while (cfgIter.hasNext()) {
			NationMissionTaskCfg cfg = cfgIter.next();
			if (cfg.getMissionLevel() == getLevel()) {
				retList.add(cfg);
			}
		}
		return retList;
	}

	/**
	 * 获取下次增加任务的时间
	 * 
	 * @return
	 */
	public long getNextMissionAddTime() {
		if (nationMissions.size() >= NationConstCfg.getInstance().getMissionTaskLimit()) {
			return 0L;
		}
		checkMissionAdd();
		return nationMissonCheckAddTime + NationConstCfg.getInstance().getMissionRefreshTime();
	}

	/**
	 * 获取所有任务
	 * 
	 * @return
	 */
	public Map<String, NationMissionTemp> getAllMission() {
		return nationMissions;
	}

	/**
	 * 获取任务
	 * 
	 * @param uuid
	 * @return
	 */
	public NationMissionTemp getMission(String uuid) {
		return nationMissions.get(uuid);
	}

	/**
	 * 删除任务
	 * 
	 * @param uuid
	 */
	public void removeMission(String uuid) {
		if (nationMissions.size() == NationConstCfg.getInstance().getMissionTaskLimit()) {
			nationMissonCheckAddTime = HawkTime.getMillisecond();
		}
		// 重置任务增加时间
		nationMissions.remove(uuid);
	}

	/**
	 * 重置所有任务
	 * 
	 * @param uuid
	 */
	public void resetMission(Map<String, NationMissionTemp> nationMissions) {
		this.nationMissions = nationMissions;
		notifyRefresh();
	}

	/**
	 * 停服处理
	 */
	public void onShutdown() {
		flushToRedis();
	}

	/**
	 * 定时落地缓存信息(15min落地一次)
	 */
	private void tickFlushToRedis() {
		long currentTime = HawkTime.getMillisecond();
		if (currentTime - flushRedisTime < 15 * GsConst.MINUTE_MILLI_SECONDS) {
			return;
		}
		flushRedisTime = currentTime;

		flushToRedis();
	}

	/**
	 * 落地缓存信息
	 */
	private void flushToRedis() {
		RedisProxy.getInstance().resetNationMissions(nationMissions);
		RedisProxy.getInstance().updateNationMissionCheckAddTime(nationMissonCheckAddTime);
		RedisProxy.getInstance().updateNationMissionRefreshDay(nationMissionRefreshDay);
	}

	@Override
	public boolean checkStateCanBuild() {
		return true;
	}

	/**
	 * 通知刷新
	 */
	public void notifyRefresh() {
		Set<Player> onlinePlayers = GlobalData.getInstance().getOnlinePlayers();
		for (Player player : onlinePlayers) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.NATIONAL_MISSION_NOTIFY_REFRESH_VALUE));
		}
	}

	/**
	 * 接收任务
	 * 
	 * @param player
	 * @param missionId任务id
	 * @param remainTimes任务剩余可领取次数
	 * @param build任务中心建筑等级
	 */
	public void tlogRecevie(Player player, int missionId, int remainTimes, int buildLevel) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.nation_misison_receive);
			if (logParam != null) {
				logParam.put("missionId", missionId);
				logParam.put("remainTimes", remainTimes);
				logParam.put("buildLevel", buildLevel);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 放弃任务
	 * 
	 * @param player
	 * @param missionId任务id
	 * @param value任务进度
	 */
	public void tlogGiveUp(Player player, int missionId, long value) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.nation_misison_giveup);
			if (logParam != null) {
				logParam.put("missionId", missionId);
				logParam.put("value", value);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 任务失败
	 * 
	 * @param player
	 * @param missionId任务id
	 * @param value任务进度
	 */
	public void tlogFailed(Player player, int missionId, long value) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.nation_misison_failed);
			if (logParam != null) {
				logParam.put("missionId", missionId);
				logParam.put("value", value);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 任务完成
	 * 
	 * @param player
	 * @param missionId任务id
	 * @param limitTimes剩余可以完成任务次数
	 * @param city个人版/国家版
	 * @param dailyTech科技值当日上限进度
	 * @param tech科技值
	 * @param buildLevel任务中心建筑等级
	 */
	public void tlogFinish(Player player, int missionId, int limitTimes, int city, int dailyTech, int tech, int buildLevel) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.nation_misison_finish);
			if (logParam != null) {
				logParam.put("missionId", missionId);
				logParam.put("limitTimes", limitTimes);
				logParam.put("city", city);
				logParam.put("dailyTech", dailyTech);
				logParam.put("tech", tech);
				logParam.put("buildLevel", buildLevel);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 任务删除
	 * 
	 * @param player
	 * @param missionId任务id
	 * @param limitTimes剩余可以完成领取次数
	 */
	public void tlogDelete(Player player, int missionId, int limitTimes) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.nation_misison_delete);
			if (logParam != null) {
				logParam.put("missionId", missionId);
				logParam.put("limitTimes", limitTimes);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 购买任务次数
	 * 
	 * @param player
	 * @param limitTimes剩余可以完成任务次数
	 */
	public void tlogBuy(Player player, int limitTimes) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.nation_misison_buy);
			if (logParam != null) {
				logParam.put("limitTimes", limitTimes);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 任务消失
	 * 
	 * @param player
	 * @param missionId任务id
	 * @param limitTimes剩余可以完成领取次数
	 */
	public void tlogDisapper(int missionId, int limitTimes) {
		try {
			LogParam logParam = LogUtil.getNonPersonalLogParam(LogInfoType.nation_misison_disapper);
			if (logParam != null) {
				logParam.put("missionId", missionId);
				logParam.put("limitTimes", limitTimes);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 合服处理
	 */
	public void clearForMerge() {
		try {
			this.nationMissions = new ConcurrentHashMap<>();
			this.nationMissonCheckAddTime = 0L;
			this.nationMissionRefreshDay = 0;
			RedisProxy.getInstance().resetNationMissions(nationMissions);
			RedisProxy.getInstance().updateNationMissionCheckAddTime(nationMissonCheckAddTime);
			RedisProxy.getInstance().updateNationMissionRefreshDay(nationMissionRefreshDay);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
}
