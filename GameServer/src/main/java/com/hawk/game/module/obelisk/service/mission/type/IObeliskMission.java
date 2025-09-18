package com.hawk.game.module.obelisk.service.mission.type;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.helper.HawkAssert;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.config.ObeliskCfg;
import com.hawk.game.config.ObeliskConstCfg;
import com.hawk.game.entity.ObeliskEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.module.obelisk.service.ObeliskService;
import com.hawk.game.module.obelisk.service.mission.ObeliskMission;
import com.hawk.game.module.obelisk.service.mission.ObeliskMissionType;
import com.hawk.game.module.obelisk.service.mission.data.ObeliskMissionItem;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Obelisk;
import com.hawk.game.protocol.Obelisk.PBObelisk;
import com.hawk.game.protocol.Obelisk.PBObeliskMissionState;
import com.hawk.game.protocol.Obelisk.PBObeliskOpenType;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.mssion.MissionEvent;

/**方尖碑任务接口
 * @author hf
 */
public interface IObeliskMission {

	Logger logger = LoggerFactory.getLogger("Server");

	/**
	 * 事件类
	 * @return 任务类
	 */
	default Class<? extends MissionEvent> getEventClassType() {
		return null;
	}

	/**
	 * 定期检测
	 * @param missionItem 任务
	 */
	void onTick(ObeliskMissionItem missionItem);

	/**
	 * 刷新任务
	 * @param player 玩家
	 * @param missionItem 任务
	 * @param missionEvent 任务事件
	 */
	default void refreshMission(Player player, ObeliskMissionItem missionItem, MissionEvent missionEvent){

	}

	/**
	 * 检测任务开始 只有 未开始的 可以检测
	 * @param missionItem 任务
	 * @return 是否开启任务
	 */
	default boolean startMission(ObeliskMissionItem missionItem) {
		HawkAssert.isTrue(missionItem.getState() == PBObeliskMissionState.NOTOPEN);
		ObeliskCfg cfg = missionItem.getObeliskCfg();
		//时间开启类型
		if (cfg.getOpenType() == PBObeliskOpenType.OPEN_TIME_VALUE) {
			long startTime = cfg.getOpenTime() + ObeliskService.getInstance().getTermAM0Date();
			long nowTime = HawkTime.getMillisecond();
			if (nowTime >= startTime) {
				missionItem.setState(PBObeliskMissionState.OPEN);
				missionItem.setStartTime(startTime);
				long endTime = startTime + cfg.getDuration();
				missionItem.setEndTime(endTime);
				logger.info("IObeliskMission startMission success missionId:{}, startTime:{}, endTime:{}",cfg.getId(), startTime, endTime);
				return true;
			}
		} else if (cfg.getOpenType() == PBObeliskOpenType.OPEN_PRE_TASK_VALUE) {
			//前置条件开启类型
			int preTaskId = cfg.getUnlockTask();
			ObeliskMissionItem preMissItem = ObeliskService.getInstance().getObeliskMissionItem(preTaskId);
			if (preMissItem != null && preMissItem.getState() == PBObeliskMissionState.CLOSED) {
				missionItem.setState(PBObeliskMissionState.OPEN);
				missionItem.setStartTime(preMissItem.getEndTime());
				long endTime = preMissItem.getEndTime() + cfg.getDuration();
				// 结束时间, 可提前完成
				missionItem.setEndTime(endTime);
				logger.info("IObeliskMission startMission success missionId:{}, startTime:{}, endTime:{}",cfg.getId(), missionItem.getStartTime(), endTime);
				return true;
			} // 没有前置任务
			else if(preMissItem == null)  {
				missionItem.setState(PBObeliskMissionState.OPEN);
				long startTime = cfg.getOpenTime() + ObeliskService.getInstance().getTermAM0Date();
				missionItem.setStartTime(startTime);
				long endTime = startTime + cfg.getDuration();
				// 结束时间, 可提前完成
				missionItem.setEndTime(endTime);
				logger.info("IObeliskMission startMission success missionId:{}, startTime:{}, endTime:{}",cfg.getId(), startTime, endTime);
				return true;
			}
		}
		return false;
	}
	/**
	 * 检测任务完成
	 * @param missionItem 任务
	 * @return 是否完成
	 */
	default boolean closeMission(ObeliskMissionItem missionItem) {
		HawkAssert.isTrue(missionItem.getState() == PBObeliskMissionState.OPEN);
		ObeliskCfg cfg = missionItem.getObeliskCfg();
		//时间开启类型
		long nowTime = HawkTime.getMillisecond();
		if (cfg.getEndType() == Obelisk.PBObeliskCloseType.CLOSE_TIME_VALUE) {
			if (nowTime >= missionItem.getEndTime()) {
				missionItem.setState(PBObeliskMissionState.CLOSED);
				logger.info("IObeliskMission closeMission success missionId:{}, startTime:{}, endTime:{}",cfg.getId(), missionItem.getStartTime(), missionItem.getEndTime());
				return true;
			}
		//注意, 联盟类型的不允许配置为可提前关闭
		} else if (cfg.getEndType() == Obelisk.PBObeliskCloseType.CLOSE_NUM_FINISH_VALUE) {
			// 如果到达关闭时间 / 达到目标
			boolean isArriveEndTime = nowTime >= missionItem.getEndTime();
			boolean isArriveNum = missionItem.getNum() >= cfg.getCount();
			if (isArriveEndTime || isArriveNum) {
				missionItem.setState(PBObeliskMissionState.CLOSED);
				long endTime = Math.min(nowTime, missionItem.getEndTime());
				// 结束时间, 可提前完成
				missionItem.setEndTime(endTime);
				logger.info("IObeliskMission closeMission success missionId:{}, startTime:{}, endTime:{}",cfg.getId(), missionItem.getStartTime(), endTime);

				return true;
			}
		}
		return false;
	}

	/**
	 * 计算大于条件的数值
	 * @param mini 条件值
	 * @param allMap 所有数据
	 * @return 值
	 */
	default int calGreatThanVal(int mini, Map<String, Integer> allMap) {
		// 击杀高于cfg.getPara1() 级 怪 的总数
		int value = 0;
		for (Entry<String, Integer> ent : allMap.entrySet()) {
			int level = NumberUtils.toInt(ent.getKey());
			if (level < mini) {
				continue;
			}
			value += ent.getValue();
		}
		return value;
	}


	/**
	 * 任务类型
	 * @return 任务类型
	 */
	default ObeliskMissionType getObeliskMissionType() {
		return getClass().getAnnotation(ObeliskMission.class).missionType();
	}

	/**
	 * 初始化任务 只在启动时调用 
	 * @param missionItem 任务
	 */
	default void initMission(ObeliskMissionItem missionItem) {

	}
	
	/**任务成功开启*/
	default void onMissionStart(ObeliskMissionItem missionItem) {

	}

	/**
	 * 检测是否直接完成
	 * @param missionItem 任务
	 * @param directComplete true 代表超过开始时间即完成
	 * @return 是否直接完成
	 */
	default boolean checkOverTimeComplete(ObeliskMissionItem missionItem, boolean directComplete) {
		//方尖碑上线时间
		long obeliskOnlineTime = ObeliskConstCfg.getInstance().getOnlineTimeValue();
		//超过开始时间直接完成
		boolean overStart = (directComplete && obeliskOnlineTime > missionItem.getStartTime());
		//超过结束时间直接完成
		boolean overEnd = obeliskOnlineTime > missionItem.getEndTime();
		return (overStart || overEnd);
	}
	/**联盟相关任务
	 * 针对任务开始 结束时间,在配置的时间抽,进行处理
	 * 功能开启时已结束处理方式 - 直接完成
	 * 功能开启时处于进行阶段的处理方式 - 部分直接完成,部分正常进行
	 * @param missionItem 任务
	 * @param directComplete 是否直接完成
	 * @return 是否超时
	 */
	default boolean checkOverTimeGuildMission(ObeliskMissionItem missionItem, boolean directComplete) {
		ObeliskCfg cfg = missionItem.getObeliskCfg();
		//如果 (开始时间 < 配置的上线时间点)(结束时间 < 配置的上线时间点), 拉满. 只会发生在启服时. 不用担心效率
		if (checkOverTimeComplete(missionItem, directComplete)) {
			int cfgValue = cfg.getPara1();
			List<String> guildList = GuildService.getInstance().getGuildIds();
			for (String guildId : guildList) {
				// 如果联盟完成数低于要求的数字
				if (missionItem.getGuildValue(guildId) < cfg.getCount()) {
					int left = cfg.getCount() - missionItem.getGuildValue(guildId);
					missionItem.addGuildValue(guildId, left);
					HawkLog.logPrintln("IObeliskMission checkOverTimeGuildMission fillUp guildId:{}, cfgId:{}, value:{}",guildId, cfg.getId(),left);
				}
			}
			//修改状态
			missionItem.setState(PBObeliskMissionState.CLOSED);
			return true;
		}
		return false;
	}

	/**
	 * 玩家每个任务的pb组装
	 * @param player 玩家
	 * @param missionItem 任务
	 * @return 单个任务的pb
	 */
	PBObelisk.Builder buildPbToPlayer(Player player, ObeliskMissionItem missionItem);


	/**
	 * 计算任务贡献度
	 * @param player 玩家
	 * @param cfg 配置
	 */
	default void calculateContribution(Player player, ObeliskCfg cfg){

	}


	/**
	 * 更新联盟任务中,玩家状态
	 * @param missionItem 任务
	 */
	default void updateGuildMemberObeliskState(ObeliskMissionItem missionItem){
		ObeliskCfg cfg = missionItem.getObeliskCfg();
		Map<String, Integer> guildMap = missionItem.getGuildMap();
		for (Map.Entry<String, Integer> entry: guildMap.entrySet()) {
			String guildId = entry.getKey();
			int num = entry.getValue();
			//联盟成员修改数据
			Collection<String> memberIds = GuildService.getInstance().getGuildMembers(guildId);
			for (String playerId:memberIds) {
				Player player = GlobalData.getInstance().makesurePlayer(playerId);
				if (player != null){
					ObeliskEntity entity = player.getData().getObeliskByCfgIdOrCreate(cfg.getId());
					if (num >= cfg.getCount()){
						entity.setState(Obelisk.PBObeliskPlayerState.FINISHED);
					}else {
						entity.setState(Obelisk.PBObeliskPlayerState.FAILED);
					}
					entity.notifyUpdate();
					logger.info("IObeliskMission updateGuildMemberObeliskState  success playerId:{}, guildId:{}, num:{}", player.getId(), guildId, num);
				}
			}
		}
	}

}
