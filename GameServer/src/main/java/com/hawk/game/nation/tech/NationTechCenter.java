package com.hawk.game.nation.tech;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.tuple.HawkTuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.cfgElement.EffectObject;
import com.hawk.game.config.NationConstCfg;
import com.hawk.game.config.NationTechCfg;
import com.hawk.game.entity.NationConstructionEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.nation.NationalBuilding;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.National.NationBuildingState;
import com.hawk.game.protocol.National.NationbuildingType;
import com.hawk.game.util.LogUtil;
import com.hawk.gamelog.GameLog;
import com.hawk.gamelog.LogParam;
import com.hawk.log.LogConst.LogInfoType;

/**
 * 国家科技中心
 * @author Golden
 *
 */
public class NationTechCenter extends NationalBuilding {

	/**
	 * 日志
	 */
	public static final Logger logger = LoggerFactory.getLogger("Server");
	
	/**
	 * 国家科技信息<techId, level>
	 */
	private Map<Integer, Integer> nationTech;
	
	/**
	 * 国家科技技能信息<techId，useSkillTime>
	 */
	private Map<Integer, Long> nationTechSkill;
	
	/**
	 * 研究值
	 */
	private int techValue;
	
	/**
	 * 每日标记
	 */
	private int dayMark;
	
	/**
	 * 每日科技增加
	 */
	private int dailyTechAdd;
	
	/**
	 * 国家科技研究信息
	 */
	private NationTechResearchTemp nationTechResearch;
	
	/**
	 * 作用号
	 */
	Map<EffType, Integer> effMap = new ConcurrentHashMap<>();
	
	/**
	 * 跨服科技信息存储
	 */
	Map<String, NationTechCrossCache> crossCache = new ConcurrentHashMap<>();
	
	public NationTechCenter(NationConstructionEntity entity, NationbuildingType buildType) {
		super(entity, buildType);
	}

	@Override
	public boolean init() {
		nationTech = RedisProxy.getInstance().getNationTechMap();
		nationTechSkill = RedisProxy.getInstance().getNationTechSkillMap();
		nationTechResearch = RedisProxy.getInstance().getNationTechResearchInfo();
		techValue = RedisProxy.getInstance().getNationTechValue(GsConfig.getInstance().getServerId());
		HawkTuple2<Integer, Integer> nationTechDailyInfo = RedisProxy.getInstance().getNationTechDailyInfo();
		dayMark = nationTechDailyInfo.first;
		dailyTechAdd = nationTechDailyInfo.second;
		calcEffect();
		return true;
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
		// 检测科技升级
		checkLevelUp();
		
		// 检测跨天
		checkCrossDay();

		if (nationTechResearch == null && getBuildState() == NationBuildingState.RUNNING) {
			exitRunningState();
			boardcastBuildState();
		}
	}

	/**
	 * 检测跨天
	 */
	private void checkCrossDay() {
		int day = HawkTime.getYearDay();
		if (nationTechResearch != null && nationTechResearch.getDayMark() != day) {
			nationTechResearch.setDayMark(day);
			nationTechResearch.setHelpTime(0L);
		}
		
		if (day != this.dayMark) {
			this.dailyTechAdd = 0;
			this.dayMark = day;
			RedisProxy.getInstance().updateNationTechDaily(this.dayMark, this.dailyTechAdd);
		}
	}
	
	/**
	 * 检测科技升级
	 */
	private void checkLevelUp() {
		if (nationTechResearch != null && nationTechResearch.getEndTime() < HawkTime.getMillisecond()) {
			
			tlogFinish(nationTechResearch.getTechCfgId(), nationTechResearch.getTarLevel(), getLevel());
			
			// 升级
			updateNationTechInfo(nationTechResearch.getTechCfgId(), nationTechResearch.getTarLevel());
			
			updateNationTechResearchInfo(null);
			
			// 重置作用号
			calcEffect();
			
			exitRunningState();
			
			boardcastBuildState();
		}
	}
	
	/**
	 * 更新国家科技信息
	 * @param techId
	 * @param level
	 */
	public void updateNationTechInfo(int techId, int level) {
		nationTech.put(techId, level);
		RedisProxy.getInstance().updateNationTech(techId, level);
	}

	/**
	 * 更新国家科技技能信息
	 * @param techId
	 * @param useSkillTime
	 */
	public void updateNationTechSkillInfo(int techId, long useSkillTime) {
		nationTechSkill.put(techId, useSkillTime);
		RedisProxy.getInstance().updateNationTechSkill(techId, useSkillTime);
	}
	
	/**
	 * 清除国家技能
	 */
	public void clearNationTechSkill() {
		nationTechSkill = new ConcurrentHashMap<>();
		RedisProxy.getInstance().clearNationTechSkill();
	}
	
	/**
	 * 更新国家科技研究信息
	 * @param researchInfo
	 */
	public void updateNationTechResearchInfo(NationTechResearchTemp researchInfo) {
		this.nationTechResearch = researchInfo;
		RedisProxy.getInstance().updateNationTechResearchInfo(nationTechResearch);
	}
	
	/**
	 * 尝试增加科技值
	 * @param changeValue(可能为负数)
	 */
	public synchronized boolean changeNationTechValue(int changeValue) {
		if (techValue + changeValue < 0) {
			return false;
		}
		techValue += changeValue;
		RedisProxy.getInstance().updateNationTechValue(techValue);
		return true;
	}
	
	/**
	 * 获取国家科技信息(只读的,拿出去别改)
	 * @return
	 */
	public Map<Integer, Integer> getNationTech() {
		return nationTech;
	}

	/**
	 * 获取国家科技等级
	 * @param techId
	 * @return
	 */
	public int getNationTechLevel(int techId) {
		return nationTech.getOrDefault(techId, 0);
	}
	
	/**
	 * 获取国家科技技能信息(只读的,拿出去别改)
	 * @return
	 */
	public Map<Integer, Long> getNationTechSkill() {
		return nationTechSkill;
	}

	/**
	 * 获取技能使用时间
	 * @param techId
	 * @return
	 */
	public long getNationTechSkill(int techId) {
		return nationTechSkill.getOrDefault(techId, 0L);
	}
	
	/**
	 * 获取国家科技研究信息(只读的,拿出去别改)
	 * @return
	 */
	public NationTechResearchTemp getNationTechResearch() {
		return nationTechResearch;
	}
	
	/**
	 * 获取国家科技研究值
	 * @return
	 */
	public int getTechValue() {
		return techValue;
	}
	
	/**
	 * 获取作用号值
	 * @param effType
	 * @return
	 */
	public int getEffValue(int effType) {
		return effMap.getOrDefault(EffType.valueOf(effType), 0);
	}
	
	/**
	 * 每日研究值增加
	 * @return
	 */
	public int getDailyTechAdd() {
		return dailyTechAdd;
	}

	/**
	 * 每日研究值增加
	 * @return
	 */
	public synchronized void addDailyTechAdd(int add) {
		dailyTechAdd += add;
		dailyTechAdd = Math.min(dailyTechAdd, NationConstCfg.getInstance().getMissionWeekLimit());
		RedisProxy.getInstance().updateNationTechDaily(dayMark, dailyTechAdd);;
	}
	
	/**
	 * 计算作用号
	 */
	private void calcEffect() {
		Map<EffType, Integer> effMap = new ConcurrentHashMap<>();
		for (Entry<Integer, Integer> tech : nationTech.entrySet()) {
			try {
				NationTechCfg cfg = AssembleDataManager.getInstance().getNationTech(tech.getKey(), tech.getValue());
				if (cfg == null) {
					logger.error("nationTechCenter calcEffect error, key:{}, val:{}", tech.getKey(), tech.getValue());
					continue;
				}
				List<EffectObject> effectList = cfg.getEffect();
				for (EffectObject effect : effectList) {
					int effVal = 0;
					if (effMap.containsKey(effect.getType())) {
						effVal = effMap.get(effect.getType()) + effect.getEffectValue();
					} else {
						effVal = effect.getEffectValue();
					}
					effMap.put(effect.getType(), effVal);
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		this.effMap = effMap;

		// reset作用号,抛到额外线程去处理
		if (effMap.size() > 0) {
			HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
				@Override
				public Object run() {
					Set<EffType> effTypeSet = effMap.keySet();
					EffType[] effTypeArray = effTypeSet.toArray(new EffType[effTypeSet.size()]);
					Set<Player> onlinePlayers = GlobalData.getInstance().getOnlinePlayers();
					for (Player player : onlinePlayers) {
						player.getPush().syncPlayerEffect(effTypeArray);
					}
					return null;
				}
			});
		}
	}
	
	@Override
	public void levelupOver() {
		
	}

	@Override
	public void levelupStart() {
		
	}

	@Override
	public boolean checkStateCanBuild() {
		return this.getBuildState() == NationBuildingState.IDLE || this.getBuildState() == NationBuildingState.INCOMPLETE;
	}
	
	@Override
	public long getRunningEndTime() {
		if (nationTechResearch == null) {
			return 0L;
		}
		return nationTechResearch.getEndTime();
	}
	
	@Override
	public long getRunningTotalTime() {
		if (nationTechResearch == null) {
			return 0L;
		}
		NationTechCfg nationTechCfg = AssembleDataManager.getInstance().getNationTech(nationTechResearch.getTechCfgId(), nationTechResearch.getTarLevel());
		return nationTechCfg.getTechTime();
	}

	/**
	 * 使用科技道具
	 * @param player
	 * @param tech科技值
	 * @param dailyTech当日上限进度
	 */
	public void tlogTools(Player player, int tech, int dailyTech) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.nation_tech_tool);
			if (logParam != null) {
				logParam.put("tech", tech);
				logParam.put("dailyTech", dailyTech);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 科技研究
	 * @param player
	 * @param techId科技id
	 * @param techLevel科技等级
	 * @param comsumeTech消耗科技值
	 * @param remainTech剩余科技值
	 * @param buildLevel建筑等级
	 */
	public void tlogResearch(Player player, int techId, int techLevel, int comsumeTech, int remainTech, int buildLevel) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.nation_tech_research);
			if (logParam != null) {
				logParam.put("techId", techId);
				logParam.put("techLevel", techLevel);
				logParam.put("comsumeTech", comsumeTech);
				logParam.put("remainTech", remainTech);
				logParam.put("buildLevel", buildLevel);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 取消研究
	 * @param player
	 * @param techId科技id
	 * @param techLevel科技等级
	 * @param rewardTech返还科技值
	 * @param remainTech剩余科技值
	 */
	public void tlogGiveUp(Player player, int techId, int techLevel, int rewardTech, int remainTech) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.nation_tech_giveup);
			if (logParam != null) {
				logParam.put("techId", techId);
				logParam.put("techLevel", techLevel);
				logParam.put("rewardTech", rewardTech);
				logParam.put("remainTech", remainTech);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 研究完成
	 * @param techId科技id
	 * @param techLevel科技等级
	 * @param buildLevel建筑等级
	 */
	public void tlogFinish(int techId, int techLevel, int buildLevel) {
		try {
			LogParam logParam = LogUtil.getNonPersonalLogParam(LogInfoType.nation_tech_finish);
			if (logParam != null) {
				logParam.put("techId", techId);
				logParam.put("techLevel", techLevel);
				logParam.put("buildLevel", buildLevel);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 科技助力
	 * @param player
	 * @param techId科技id
	 * @param techLevel科技等级
	 * @param remainTime剩余时间
	 */
	public void tlogHelp(Player player, int techId, int techLevel, long remainTime) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.nation_tech_help);
			if (logParam != null) {
				logParam.put("techId", techId);
				logParam.put("techLevel", techLevel);
				logParam.put("remainTime", (int)(remainTime / 1000));
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 释放技能
	 * @param player
	 * @param techId科技id
	 * @param techLevel科技等级
	 * @param comsumeTech消耗科技值
	 * @param remainTech剩余科技值
	 */
	public void tlogSkill(Player player, int techId, int techLevel, int comsumeTech, int remainTech) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.nation_tech_skill);
			if (logParam != null) {
				logParam.put("techId", techId);
				logParam.put("techLevel", techLevel);
				logParam.put("comsumeTech", comsumeTech);
				logParam.put("remainTech", remainTech);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 重置科技值(慎用)
	 * @param value
	 */
	public void resetTechValue(int value) {
		techValue = value;
		RedisProxy.getInstance().updateNationTechValue(techValue);
	}
	
	/**
	 * 重置科技(慎用)
	 * @param techMap
	 */
	public void resetTech(Map<String, String> techMap) {
		nationTech = new ConcurrentHashMap<>();
		for (Entry<String, String> info : techMap.entrySet()) {
			nationTech.put(Integer.valueOf(info.getKey()), Integer.valueOf(info.getValue()));
		}
		RedisProxy.getInstance().resetNationTech(techMap);
	}
	
	/**
	 * 获取跨服科技缓存
	 * @param serverId
	 * @return
	 */
	public NationTechCrossCache getCrossCache(String serverId) {
		NationTechCrossCache cache = crossCache.get(serverId);
		if (cache == null) {
			cache = new NationTechCrossCache(serverId);
			crossCache.putIfAbsent(serverId, cache);
		}
		return crossCache.get(serverId);
	}
	/**
	 * 获取跨服国家科技作用号值
	 * @param targetServerId
	 * @param effType
	 */
	public int getCrossEffValue(String targetServerId, int effType) {
		NationTechCrossCache crossCache = getCrossCache(targetServerId);
		if (crossCache == null) {
			return 0;
		}
		Map<EffType, Integer> effMap = crossCache.getEffMap();
		return effMap.getOrDefault(EffType.valueOf(effType), 0);
	}
}
