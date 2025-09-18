package com.hawk.game.module.nation;

import java.util.List;
import java.util.Map;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;

import com.hawk.game.GsApp;
import com.hawk.game.config.NationConstCfg;
import com.hawk.game.config.NationMissionTaskCfg;
import com.hawk.game.entity.DailyDataEntity;
import com.hawk.game.entity.NationMissionEntity;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.item.NationMissionItem;
import com.hawk.game.msg.MissionMsg;
import com.hawk.game.nation.NationService;
import com.hawk.game.nation.mission.NationMissionCenter;
import com.hawk.game.nation.mission.NationMissionTemp;
import com.hawk.game.nation.tech.NationTechCenter;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.president.PresidentFightService;
import com.hawk.game.president.PresidentOfficier;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.National.MyNationMissionInfo;
import com.hawk.game.protocol.National.NationBuildQuestType;
import com.hawk.game.protocol.National.NationMissionInfo;
import com.hawk.game.protocol.National.NationMissionOperReq;
import com.hawk.game.protocol.National.NationMissionPageInfo;
import com.hawk.game.protocol.National.NationRedDot;
import com.hawk.game.protocol.National.NationbuildingType;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.mssion.MissionContext;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.type.IMission;
import com.hawk.game.util.GsConst.MissionState;
import com.hawk.log.Action;

/**
 * 国家任务
 * 
 * @author Golden
 *
 */
public class PlayerNationMissionModule extends PlayerModule {

	/**
	 * tick周期
	 */
	private static final long TICK_PERIOD = 1000L;
	
	/**
	 * 上次tick时间
	 */
	private long lastTickTime = 0L;
	
	public PlayerNationMissionModule(Player player) {
		super(player);
	}

	@Override
	public boolean onTick() {
		// 检测
		doCheck();
		// 检测小红点
		checkRD();
		return true;
	}

	/**
	 * 检测小红点
	 */
	private void checkRD() {
		NationMissionEntity entity = player.getData().getNationMissionEntity();
		NationMissionItem mission = entity.getMission();
		
		// 任务中心是否有奖励
		if (mission != null && mission.getState() == MissionState.STATE_FINISH) {
			player.updateNationRD(NationRedDot.MISSION_AWARD);
		} else {
			player.rmNationRD(NationRedDot.MISSION_AWARD);
		}
		
		// 任务中心闲置状态
		if (mission == null && entity.getRemainTimes() > 0) {
			player.updateNationRD(NationRedDot.MISSION_IDLE);
		} else {
			player.rmNationRD(NationRedDot.MISSION_IDLE);
		}
	}
	
	/**
	 * 检测
	 */
	private void doCheck() {
		// 服务器没初始化完成
		if (!GsApp.getInstance().isInitOK()) {
			return;
		}

		// 跨服不检测
		if (player.isCsPlayer()) {
			return;
		}

		// 控制下tick周期
		long currentTime = HawkApp.getInstance().getCurrentTime();
		if (currentTime - lastTickTime <= TICK_PERIOD) {
			return;
		}
		lastTickTime = currentTime;
		
		// 任务中心建筑
		NationMissionCenter missionCenter = (NationMissionCenter) NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_QUEST_CENTER);
		if (missionCenter == null || missionCenter.getLevel() <= 0) {
			return;
		}

		// 玩家任务中心数据
		NationMissionEntity nationMissionEntity = player.getData().getNationMissionEntity();

		boolean needRefresh = false;
		
		// 跨天检测
		long timeMark = nationMissionEntity.getTimeMark();
		if (!HawkTime.isSameDay(currentTime, timeMark)) {
			needRefresh = true;
			nationMissionEntity.setTimeMark(currentTime);
			
			if (timeMark == 0) {
				// 第一次进这个系统,给的次数
				nationMissionEntity.setRemainTimes(NationConstCfg.getInstance().getPlayerMissionInitCount());
			} else {
				// 跨天增加可完成任务次数
				int crossDays = HawkTime.getCrossDay(currentTime, timeMark, 0);
				// 最大可完成次数
				int canFinshTimes = missionCenter.getCurrentLevelCfg().getMissionFinishTimes();
				// 增加次数
				int addTimes = crossDays * missionCenter.getCurrentLevelCfg().getMissionRecoveryTimes();
				if (nationMissionEntity.getRemainTimes() < canFinshTimes) {
					addTimes = Math.min(addTimes, canFinshTimes - nationMissionEntity.getRemainTimes());
					nationMissionEntity.addRemainTimes(addTimes);
				}
			}
		}

		// 建筑等级提升检测
		int currentLevel = missionCenter.getLevel();
		int levelMark = nationMissionEntity.getConstructionLevelMark();
		if (currentLevel != levelMark) {
			needRefresh = true;
			nationMissionEntity.setConstructionLevelMark(currentLevel);

			if (levelMark == 0) {
				
			} else {
				// 检测建筑升级增加可完成任务次数
				// 最大可完成次数
				int canFinshTimes = missionCenter.getCurrentLevelCfg().getMissionFinishTimes();
				for (int i = 1; i <= currentLevel - levelMark; i++) {
					int level = levelMark + i;
					int addTimes = missionCenter.getLevelCfg(level).getMissionRecoveryTimes();
					if (nationMissionEntity.getRemainTimes() < canFinshTimes) {
						addTimes = Math.min(addTimes, canFinshTimes - nationMissionEntity.getRemainTimes());
						nationMissionEntity.addRemainTimes(addTimes);
					}
				}
			}
		}
		
		// 跨周检测
		int yearWeek = HawkTime.getYearWeek();
		if (yearWeek != nationMissionEntity.getWeekMark()) {
			needRefresh = true;
			nationMissionEntity.setWeekMark(yearWeek);
			
			// 科技值置为0
			nationMissionEntity.setTech(0);
		}
		
		// 任务删除
		NationMissionItem myMission = nationMissionEntity.getMission();
		if (myMission != null) {
			NationMissionTaskCfg cfg = HawkConfigManager.getInstance().getConfigByKey(NationMissionTaskCfg.class, myMission.getCfgId());
			if (myMission.getReceiveTime() + cfg.getFinishTime() < currentTime && myMission.getState() == MissionState.STATE_NOT_FINISH) {
				missionCenter.tlogFailed(player, myMission.getCfgId(), myMission.getValue());
				nationMissionEntity.setMission(null);
			}
		}
		
		// 刷新
		if (needRefresh) {
			sendProtocol(HawkProtocol.valueOf(HP.code2.NATIONAL_MISSION_NOTIFY_REFRESH_VALUE));
		}
	}

	/**
	 * 国家任务界面信息请求
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.NATIONAL_MISSION_PAGE_INFO_REQ_VALUE)
	private void onPageInfoReq(HawkProtocol protocol) {
		// 获取国家建筑
		NationMissionCenter missionCenter = (NationMissionCenter) NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_QUEST_CENTER);
		if (missionCenter == null || missionCenter.getLevel() <= 0) {
			return;
		}

		// 同步界面信息
		syncPageInfo();
	}

	/**
	 * 同步界面信息
	 * 
	 * @param missionCenter
	 */
	private void syncPageInfo() {
		NationMissionCenter missionCenter = (NationMissionCenter) NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_QUEST_CENTER);
		if (missionCenter == null || missionCenter.getLevel() <= 0) {
			return;
		}
		missionCenter.doCheck();
		
		// 玩家国家任务数据
		NationMissionEntity missionEntity = player.getData().getNationMissionEntity();
		NationMissionPageInfo.Builder builder = NationMissionPageInfo.newBuilder();
		builder.setType(NationBuildQuestType.valueOf(missionEntity.getType()));
		builder.setRemainMissionTimes(missionEntity.getRemainTimes());
		builder.setCanFinishMissionTimes(missionCenter.getCurrentLevelCfg().getMissionFinishTimes());
		builder.setNextMissionAddTime(HawkTime.getNextAM0Date());
		builder.setNextMissionAddCount(missionCenter.getCurrentLevelCfg().getMissionRecoveryTimes());
		if (missionCenter.getNextMissionAddTime() > 0L) {
			builder.setNextRefreshTime(missionCenter.getNextMissionAddTime());
		}
		builder.setBuyTimes(player.getData().getDailyDataEntity().getNationMissionDayBuyTimes());
		
		NationTechCenter techCenter = (NationTechCenter)NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_TECH_CENTER);
		if (techCenter != null) {
			builder.setCurrentTech(techCenter.getDailyTechAdd());
		} else {
			builder.setCurrentTech(0);
		}
		
		long giveupTime = missionEntity.getGiveupTime();
		if (giveupTime > 0) {
			builder.setGiveupTime(giveupTime);
		}
		
		NationMissionItem myMission = missionEntity.getMission();
		if (myMission != null) {
			MyNationMissionInfo.Builder myMissionBuilder = MyNationMissionInfo.newBuilder();
			myMissionBuilder.setCfgId(myMission.getCfgId());
			myMissionBuilder.setCurrentValue((int) Math.min(Integer.MAX_VALUE - 1, myMission.getValue()));
			NationMissionTaskCfg cfg = HawkConfigManager.getInstance().getConfigByKey(NationMissionTaskCfg.class, myMission.getCfgId());
			myMissionBuilder.setConditionValue(cfg.getVal2());
			myMissionBuilder.setDisappearTime(myMission.getReceiveTime() + cfg.getFinishTime());
			myMissionBuilder.setUuid(myMission.getUuid());
			myMissionBuilder.setState(myMission.getState());
			builder.setMyMission(myMissionBuilder);
		}

		Map<String, NationMissionTemp> allMission = missionCenter.getAllMission();
		for (NationMissionTemp mission : allMission.values()) {
			NationMissionInfo.Builder missionBuilder = NationMissionInfo.newBuilder();
			missionBuilder.setUuid(mission.getUuid());
			missionBuilder.setCfgId(mission.getMissionId());
			NationMissionTaskCfg cfg = HawkConfigManager.getInstance().getConfigByKey(NationMissionTaskCfg.class, mission.getMissionId());
			missionBuilder.setRemainTimes(cfg.getPickupTime() - mission.getPickUpTimes());
			if (cfg.getTimeLimit() > 0L) {
				missionBuilder.setDisappearTime(mission.getRefreshTime() + cfg.getTimeLimit());
			}
			builder.addMissions(missionBuilder);
		}
		sendProtocol(HawkProtocol.valueOf(HP.code2.NATIONAL_MISSION_PAGE_INFO_RESP_VALUE, builder));
	}

	/**
	 * 接受任务
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.NATIONAL_MISSION_RECEIVE_VALUE)
	private void onMissionReceive(HawkProtocol protocol) {
		NationMissionOperReq req = protocol.parseProtocol(NationMissionOperReq.getDefaultInstance());
		String uuid = req.getUuid();
		NationBuildQuestType type = req.getType();
		
		// 获取国家建筑
		NationMissionCenter missionCenter = (NationMissionCenter) NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_QUEST_CENTER);
		if (missionCenter == null || missionCenter.getLevel() <= 0) {
			return;
		}

		// 任务不存在
		NationMissionTemp mission = missionCenter.getMission(uuid);
		if (mission == null) {
			syncPageInfo();
			sendError(protocol.getType(), Status.Error.NATION_MISSION_NONE);
			return;
		}

		// 任务配置不存在
		NationMissionTaskCfg missionCfg = HawkConfigManager.getInstance().getConfigByKey(NationMissionTaskCfg.class, mission.getMissionId());
		if (missionCfg == null) {
			return;
		}

		// 任务达到最大接取次数
		if (mission.getPickUpTimes() >= missionCfg.getPickupTime()) {
			missionCenter.removeMission(uuid);
			sendError(protocol.getType(), Status.Error.NATION_MISISON_PICK_LIMIT_VALUE);
			return;
		}

		// 玩家身上有任务
		NationMissionEntity missionEntity = player.getData().getNationMissionEntity();
		if (missionEntity.getMission() != null) {
			return;
		}

		// 剩余可以完成次数
		if (missionEntity.getRemainTimes() <= 0) {
			sendError(protocol.getType(), Status.Error.NATION_MISSION_NO_REMAIN);
			return;
		}

		// 玩家添加任务
		NationMissionItem nationMissionItem = new NationMissionItem(mission.getMissionId(), 0, MissionState.STATE_NOT_FINISH, HawkTime.getMillisecond(), uuid);
		missionEntity.setMission(nationMissionItem);
		missionEntity.setType(type.getNumber());
		
		// 添加任务完成次数
		mission.addPickUpTimes();
		if (mission.getPickUpTimes() >= missionCfg.getPickupTime()) {
			missionCenter.removeMission(uuid);
		}

		player.responseSuccess(protocol.getType());
		
		// 同步界面信息
		syncPageInfo();
		
		missionCenter.tlogRecevie(player, mission.getMissionId(), missionCfg.getPickupTime() - mission.getPickUpTimes(), missionCenter.getLevel());
	}

	/**
	 * 放弃任务
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.NATIONAL_MISSION_GIVEUP_VALUE)
	private void onMissionGiveUp(HawkProtocol protocol) {
		// 玩家身上有任务
		NationMissionEntity missionEntity = player.getData().getNationMissionEntity();
		NationMissionItem mission = missionEntity.getMission();
		if (mission == null) {
			return;
		}
		
		long currentTime = HawkTime.getMillisecond();
		if (currentTime - missionEntity.getGiveupTime() < NationConstCfg.getInstance().getMissionGiveupCd()) {
			return;
		}
		missionEntity.setGiveupTime(currentTime);
		
		int missionId = mission.getCfgId();
		long value = mission.getValue();
		
		// 删除任务
		missionEntity.setMission(null);
		
		player.responseSuccess(protocol.getType());
		
		// 同步界面信息
		syncPageInfo();
		
		NationMissionCenter missionCenter = (NationMissionCenter) NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_QUEST_CENTER);
		missionCenter.tlogGiveUp(player, missionId, value);
	}
	
	/**
	 * 删除任务
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.NATIONAL_MISSION_DELETE_VALUE)
	private void onMissionDelete(HawkProtocol protocol) {
		// 权限判断
		if (!PresidentFightService.getInstance().isPresidentPlayer(player.getId()) && !PresidentOfficier.getInstance().isMeritoriousOfficials(player.getId())) {
			sendError(protocol.getType(), Status.Error.ONLY_OFFICER_TO_OPER);
			return;
		}
		
		NationMissionOperReq req = protocol.parseProtocol(NationMissionOperReq.getDefaultInstance());
		String uuid = req.getUuid();
		
		// 任务不存在
		NationMissionCenter missionCenter = (NationMissionCenter) NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_QUEST_CENTER);
		NationMissionTemp mission = missionCenter.getMission(uuid);
		if (mission == null) {
			syncPageInfo();
			sendError(protocol.getType(), Status.Error.NATION_MISSION_NONE);
			return;
		}
		
		// 删除任务
		missionCenter.removeMission(uuid);
		
		player.responseSuccess(protocol.getType());
		
		// 同步界面信息
		syncPageInfo();
		
		NationMissionTaskCfg missionCfg = HawkConfigManager.getInstance().getConfigByKey(NationMissionTaskCfg.class, mission.getMissionId());		
		missionCenter.tlogDelete(player, mission.getMissionId(), missionCfg.getPickupTime() - mission.getPickUpTimes());
	}
	
	/**
	 * 领奖
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.NATIONAL_MISSION_REWARD_VALUE)
	private void onMissionReward(HawkProtocol protocol) {
		NationMissionEntity entity = player.getData().getNationMissionEntity();
		NationMissionItem mission = entity.getMission();
		if (mission == null || mission.getState() != MissionState.STATE_FINISH) {
			return;
		}
		int missionId = mission.getCfgId();
		entity.setMission(null);
		entity.reduceRemainTimes();
		
		// 是否是国家奖励
		boolean nationAward = (NationBuildQuestType.valueOf(entity.getType()) != NationBuildQuestType.PERSONAL);
		
		// 奖励
		AwardItems award = AwardItems.valueOf();
		NationMissionTaskCfg cfg = HawkConfigManager.getInstance().getConfigByKey(NationMissionTaskCfg.class, mission.getCfgId());
		if (nationAward) {
			award.addItemInfos(cfg.getNationalAwardItems());
		} else {
			award.addItemInfos(cfg.getPersonalAwardItems());
		}
		award.rewardTakeAffectAndPush(player, Action.NATIONAL_MISSION_AWARD, true);
		
		// 增加科技
		NationTechCenter techCenter = (NationTechCenter)NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_TECH_CENTER);
		if (techCenter != null) {
			int remainTechCanAdd = NationConstCfg.getInstance().getMissionWeekLimit() - techCenter.getDailyTechAdd();
			if (nationAward && remainTechCanAdd > 0) {
				int addTech = Math.min(cfg.getNationalTechAward(), remainTechCanAdd);
				entity.addTech(addTech);
			
				// 科技建筑增加科技值
				techCenter.changeNationTechValue(addTech);
				techCenter.addDailyTechAdd(addTech);
			}
		}
		
		player.responseSuccess(protocol.getType());
		
		// 同步界面信息
		syncPageInfo();
		
		NationMissionCenter missionCenter = (NationMissionCenter) NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_QUEST_CENTER);
		missionCenter.tlogFinish(player, missionId, entity.getRemainTimes(), nationAward ? 1 : 0, techCenter.getDailyTechAdd(), techCenter.getTechValue(), missionCenter.getLevel());
	}
	
	/**
	 * 购买次数
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.NATIONAL_MISSION_BUY_TIMES_VALUE)
	private void onBuyTimes(HawkProtocol protocol) {
		// 最大购买次数
		DailyDataEntity dailyDataEntity = player.getData().getDailyDataEntity();
		int nationMissionDayBuyTimes = dailyDataEntity.getNationMissionDayBuyTimes();
		if (nationMissionDayBuyTimes >= NationConstCfg.getInstance().getMissionDayBuyTimes()) {
			return;
		}

		// 最大次数
//		NationMissionCenter missionCenter = (NationMissionCenter) NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_QUEST_CENTER);
//		int canFinishTimes = missionCenter.getCurrentLevelCfg().getMissionFinishTimes();
		
		// 超上限了
		NationMissionEntity missionEntity = player.getData().getNationMissionEntity();
//		if (missionEntity.getRemainTimes() >= canFinishTimes) {
//			return;
//		}
		
		missionEntity.addRemainTimes(1);
		dailyDataEntity.addNationMissionDayBuyTimes();
		
		// 消耗
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(ItemInfo.valueListOf(NationConstCfg.getInstance().getMissionPrice()));
		if (!consumeItems.checkConsume(player)) {
			return;
		}
		consumeItems.consumeAndPush(player, Action.NATIONAL_MISSION_BUY_TIMES);
		
		player.responseSuccess(protocol.getType());
		
		// 界面信息
		syncPageInfo();
		
		NationMissionCenter missionCenter = (NationMissionCenter) NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_QUEST_CENTER);
		missionCenter.tlogBuy(player, missionEntity.getRemainTimes());
	}
	
	/**
	 * 任务刷新
	 * @param msg
	 */
	@MessageHandler
	private void onRefreshMission(MissionMsg msg) {
		if (player.isCsPlayer()) {
			return;
		}
		
		MissionEvent event = msg.getEvent();
		
		// 事件触发任务列表
		List<MissionType> touchMissions = event.touchMissions();
		if (touchMissions == null || touchMissions.isEmpty()) {
			return;
		}
		
		NationMissionEntity entity = player.getData().getNationMissionEntity();
		NationMissionItem mission = entity.getMission();
		if (mission == null) {
			return;
		}
		
		NationMissionTaskCfg cfg = HawkConfigManager.getInstance().getConfigByKey(NationMissionTaskCfg.class, mission.getCfgId());
		
		// 任务类型
		MissionType missionType = MissionType.valueOf(cfg.getType());
		
		// 不触发此类型任务
		if (!touchMissions.contains(missionType)) {
			return;
		}
		
		// 未完成的任务才处理
		if (mission.getState() != MissionState.STATE_NOT_FINISH) {
			return;
		}
		
		// 刷新任务
		IMission iMission = MissionContext.getInstance().getMissions(missionType);
		iMission.refreshMission(player.getData(), event, mission, cfg.getMissionCfgItem());
		
		// 设置任务状态(这里要设置一下，调用下entity的set方法，不然可能不会落地)
		entity.setMission(mission);
		
		// 通知刷新
		sendProtocol(HawkProtocol.valueOf(HP.code2.NATIONAL_MISSION_NOTIFY_REFRESH_VALUE));
	}
}
