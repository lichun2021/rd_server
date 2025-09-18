package com.hawk.game.nation.construction.model;

import java.util.List;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.config.NationConstructionQuestCfg;
import com.hawk.game.heroTrial.HeroTrialCheckerFactory;
import com.hawk.game.heroTrial.mission.IHeroTrialChecker;
import com.hawk.game.nation.NationService;

/**
 * 玩家当前的任务信息
 * @author zhenyu.shang
 * @since 2022年3月31日
 */
public class NationalBuildQuestModel {
	
	private String playerId;
	
	private String questId;
	
	private int questCfgId;
	
	private String marchId;
	
	private int buildId;
	
	private boolean advAward;
	
	private int currentProcess;
	
	
	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}
	
	public String getQuestId() {
		return questId;
	}

	public void setQuestId(String questId) {
		this.questId = questId;
	}

	public int getQuestCfgId() {
		return questCfgId;
	}

	public void setQuestCfgId(int questCfgId) {
		this.questCfgId = questCfgId;
	}

	public String getMarchId() {
		return marchId;
	}

	public void setMarchId(String marchId) {
		this.marchId = marchId;
	}

	public int getBuildId() {
		return buildId;
	}

	public void setBuildId(int buildId) {
		this.buildId = buildId;
	}

	public boolean isAdvAward() {
		return advAward;
	}

	public void setAdvAward(boolean advAward) {
		this.advAward = advAward;
	}

	public int getCurrentProcess() {
		return currentProcess;
	}
	
	public void addCurrentProcess(int currentProcess) {
		this.currentProcess += currentProcess;
	}

	public void setCurrentProcess(int currentProcess) {
		this.currentProcess = currentProcess;
	}

	/**
	 * 检查英雄条件(同英雄试炼)
	 * @param playerId
	 * @param heroIds
	 * @param condition
	 * @return
	 */
	public boolean checkHero(String playerId, List<Integer> heroIds, List<Integer> condition) {
		if (HawkOSOperator.isEmptyString(playerId)) {
			return false;
		}
		if (condition.size() <= 0) {
			return false;
		}
		if (heroIds.size() <= 0) {
			return false;
		}
		int type = condition.get(0);
		IHeroTrialChecker checker = HeroTrialCheckerFactory.getInstance().getChecker(type);
		if (checker == null) {
			return false;
		}
		return checker.touchMission(playerId, heroIds, condition);
	}
	
	/**
	 * 检查任务条件
	 * @param playerId
	 * @param heroIds
	 * @return
	 */
	public boolean checkQuestNeed(String playerId, List<Integer> heroIds){
		// 已经正在做了
		if(this.marchId != null){
			return false;
		}
		// 检查英雄是否满足条件
		NationConstructionQuestCfg cfg = HawkConfigManager.getInstance().getConfigByKey(NationConstructionQuestCfg.class, this.questCfgId);
		if(cfg == null){
			NationService.logger.error("nation quest can not find quest cfg , cfg:", questCfgId);
			return false;
		}
		// 检查基础条件
		for (List<Integer> condition : cfg.getBaseConditionList()) {
			if (!this.checkHero(playerId, heroIds, condition)) {
				return false;
			}
		}
		
		this.advAward = true;
		// 检查进阶条件并设置
		for (List<Integer> condition : cfg.getAdvConditionList()) {
			if (!this.checkHero(playerId, heroIds, condition)) {
				this.advAward = false;
				break;
			}
		}

		return true;
	}
}
