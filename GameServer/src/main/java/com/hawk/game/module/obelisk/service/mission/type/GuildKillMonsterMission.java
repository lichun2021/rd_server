package com.hawk.game.module.obelisk.service.mission.type;

import org.apache.commons.lang.StringUtils;
import org.hawk.log.HawkLog;

import com.hawk.game.config.ObeliskCfg;
import com.hawk.game.entity.ObeliskEntity;
import com.hawk.game.module.obelisk.service.ObeliskService;
import com.hawk.game.module.obelisk.service.mission.ObeliskMission;
import com.hawk.game.module.obelisk.service.mission.ObeliskMissionType;
import com.hawk.game.module.obelisk.service.mission.data.ObeliskMissionItem;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Obelisk;
import com.hawk.game.protocol.Obelisk.PBObelisk;
import com.hawk.game.protocol.Obelisk.PBObeliskMissionState;
import com.hawk.game.protocol.Obelisk.PBObeliskPlayerState;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.event.EventMonsterAttack;

/**
 * 所在联盟 击杀N个 X级的野怪
 * @author hf
 */
@ObeliskMission(missionType = ObeliskMissionType.GUIlD_KILL_LEVEL_MONSTER)
public class GuildKillMonsterMission implements IObeliskMission {

	@Override
	public Class<? extends MissionEvent> getEventClassType() {
		return EventMonsterAttack.class;
	}

	@Override
	public ObeliskMissionType getObeliskMissionType() {
		return IObeliskMission.super.getObeliskMissionType();
	}

	@Override
	public void onTick(ObeliskMissionItem missionItem) {
		if (missionItem.getState() == PBObeliskMissionState.NOTOPEN) {
			boolean start = startMission(missionItem);
			if (start) {
				// 如果任务成功开启
				onMissionStart(missionItem);
				ObeliskService.getInstance().noticeNewPoint();
			}
		}
		if (missionItem.getState() == PBObeliskMissionState.OPEN) {
			// 如果任务成功关闭
			boolean close = closeMission(missionItem);
			if (close) {
				ObeliskService.getInstance().noticeNewPoint();
				//更新联盟玩家相关数据,,联盟任务都是以结束节点为准
				updateGuildMemberObeliskState(missionItem);
			}
		}
	}

	@Override
	public void onMissionStart(ObeliskMissionItem missionItem) {
		boolean isCheck = checkOverTimeGuildMission(missionItem, true);
		//校验是否直接完成
		if (isCheck){
			return;
		}
	}

	@Override
	public void refreshMission(Player player, ObeliskMissionItem missionItem, MissionEvent missionEvent) {

		EventMonsterAttack eventMonsterAttack = (EventMonsterAttack) missionEvent;
		if (!eventMonsterAttack.isWin()) {
			return;
		}
		ObeliskCfg cfg = missionItem.getObeliskCfg();
		if (eventMonsterAttack.getLevel() < cfg.getPara1()) {
			return;
		}
		String guildId = GuildService.getInstance().getPlayerGuildId(player.getId());
		if (StringUtils.isEmpty(guildId)) {
			return;
		}
		// 满足条件数量加一
		missionItem.addGuildValue(guildId, eventMonsterAttack.getAtkTimes());
		//处理相关贡献度计算,,累计型的相关问题
		for (int i = 0; i < eventMonsterAttack.getAtkTimes(); i++) {
			calculateContribution(player, cfg);
		}

		HawkLog.logPrintln("GuildKillMonsterMission refreshMission playerId:{}, guildId:{}, monsterLv:{}", player.getId(), guildId,eventMonsterAttack.getLevel());

	}
	@Override
	public void calculateContribution(Player player, ObeliskCfg cfg) {
		ObeliskEntity obeliskEntity = player.getData().getObeliskByCfgIdOrCreate(cfg.getId());
		if (obeliskEntity.getContribution() == -1){
			obeliskEntity.setContribution(1);
		}else{
			obeliskEntity.addContribution(1);
		}
		HawkLog.logPrintln("GuildKillMonsterMission calculateContribution playerId:{},afterContribution:{}", player.getId(), obeliskEntity.getContribution());

	}

	@Override
	public PBObelisk.Builder buildPbToPlayer(Player player, ObeliskMissionItem missionItem) {
		PBObelisk.Builder obeliskPb = PBObelisk.newBuilder();
		obeliskPb.setCfgId(missionItem.getCfgId());
		obeliskPb.setStartTime(missionItem.getStartTime());
		obeliskPb.setEndTime(missionItem.getEndTime());
		// 有联盟才有进度,这里是联盟进度
		if (player.hasGuild()) {
			int guildNumber = missionItem.getGuildValue(player.getGuildId());
			obeliskPb.setNum(guildNumber);
		}
		if (missionItem.getState() == PBObeliskMissionState.NOTOPEN) {
			obeliskPb.setState(PBObeliskPlayerState.NOT_OPEN);
		} else if (missionItem.getState() == PBObeliskMissionState.OPEN) {
			obeliskPb.setState(PBObeliskPlayerState.OPENING);
		} else if (missionItem.getState() == PBObeliskMissionState.CLOSED) {
			ObeliskCfg cfg = missionItem.getObeliskCfg();
			// 任务已结束. 结算玩家数据, 记录贡献度等
			ObeliskEntity obeliskEntity = player.getData().getObeliskByCfgIdOrCreate(cfg.getId());
			// 玩家还没有结算过
			if (obeliskEntity.getState().getNumber() < PBObeliskPlayerState.FAILED.getNumber()) {
				// 玩家还没有结算过, 针对超时过期的任务进行特殊处理
				if (checkOverTimeComplete(missionItem, true)){
					obeliskEntity.setState(Obelisk.PBObeliskPlayerState.FINISHED);
				}else {
					// 任务结束时统一结算修改该盟的玩家状态,,,此逻辑还未结算的,认定为当时不在联盟,所以设置失败状态
					obeliskEntity.setState(Obelisk.PBObeliskPlayerState.FAILED);
				}

				HawkLog.logPrintln("GuildKillMonsterMission buildPbToPlayer end playerId:{},state:{}", player.getId(), obeliskEntity.getState().getNumber());
			}
			obeliskPb.setContribution(obeliskEntity.getContribution());
			obeliskPb.setState(obeliskEntity.getState());
		}
		return obeliskPb;
	}

}
