package com.hawk.game.module.obelisk.service.mission.type;

import java.util.Objects;

import org.hawk.log.HawkLog;

import com.hawk.game.config.ObeliskCfg;
import com.hawk.game.entity.ObeliskEntity;
import com.hawk.game.module.obelisk.service.ObeliskService;
import com.hawk.game.module.obelisk.service.mission.ObeliskMission;
import com.hawk.game.module.obelisk.service.mission.ObeliskMissionType;
import com.hawk.game.module.obelisk.service.mission.data.ObeliskMissionItem;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Obelisk;
import com.hawk.game.protocol.Obelisk.PBObelisk.Builder;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.event.EventAttackFoggy;

/**
 * 所在联盟共击杀N个X级幽灵基地
 * 累积型
 * @author hf
 */
@ObeliskMission(missionType = ObeliskMissionType.GUIlD_KILL_FOGGY)
public class GuildKillFoggyMission implements IObeliskMission {
	private String lastMarhId;
	@Override
	public Class<? extends MissionEvent> getEventClassType() {
		return EventAttackFoggy.class;
	}

	@Override
	public ObeliskMissionType getObeliskMissionType() {
		return IObeliskMission.super.getObeliskMissionType();
	}

	@Override
	public void onTick(ObeliskMissionItem missionItem) {
		if (missionItem.getState() == Obelisk.PBObeliskMissionState.NOTOPEN) {
			// 如果任务成功开启
			boolean start = startMission(missionItem);
			if (start) {
				onMissionStart(missionItem);
				ObeliskService.getInstance().noticeNewPoint();
			}
		}
		if (missionItem.getState() == Obelisk.PBObeliskMissionState.OPEN) {
			// 检查任务关闭 通知红点.
			boolean close = closeMission(missionItem);
			if (close) {
				// 更新联盟玩家相关数据,,联盟任务都是以结束节点为准
				updateGuildMemberObeliskState(missionItem);
				// 红点
				ObeliskService.getInstance().noticeNewPoint();
			}
		}

	}

	@Override
	public void onMissionStart(ObeliskMissionItem missionItem) {
		boolean isCheck = checkOverTimeGuildMission(missionItem, true);
		// 校验是否直接完成
		if (isCheck) {
			return;
		}
	}

	@Override
	public void refreshMission(Player player, ObeliskMissionItem missionItem, MissionEvent missionEvent) {
		EventAttackFoggy eventAttackFoggy = (EventAttackFoggy) missionEvent;
		if (!eventAttackFoggy.isWin()) {
			return;
		}
		ObeliskCfg cfg = missionItem.getObeliskCfg();
		int foggyLv = eventAttackFoggy.getFoggyLvl();
		if (foggyLv < cfg.getPara1()) {
			return;
		}
		String guildId = GuildService.getInstance().getPlayerGuildId(player.getId());
		if (guildId.isEmpty()) {
			return;
		}
		// 满足条件数量加一
		if (!Objects.equals(lastMarhId, eventAttackFoggy.getMarchId())) { // 集结会多算
			missionItem.addGuildValue(guildId, 1);
			lastMarhId = eventAttackFoggy.getMarchId();
		}
		// 处理相关贡献度计算,,累计型的相关问题
		calculateContribution(player, cfg);

		HawkLog.logPrintln("GuildKillFoggyMission refreshMission playerId:{}, guildId:{}, foggyLv:{}", player.getId(), guildId, foggyLv);
	}

	@Override
	public void calculateContribution(Player player, ObeliskCfg cfg) {
		// 计算玩家贡献度
		ObeliskEntity obeliskEntity = player.getData().getObeliskByCfgIdOrCreate(cfg.getId());
		if (obeliskEntity.getContribution() == -1) {
			obeliskEntity.setContribution(1);
		} else {
			obeliskEntity.addContribution(1);
		}
		HawkLog.logPrintln("GuildKillFoggyMission calculateContribution playerId:{},afterContribution:{}", player.getId(), obeliskEntity.getContribution());
	}

	@Override
	public Builder buildPbToPlayer(Player player, ObeliskMissionItem missionItem) {
		Obelisk.PBObelisk.Builder obeliskPb = Obelisk.PBObelisk.newBuilder();
		obeliskPb.setCfgId(missionItem.getCfgId());
		obeliskPb.setStartTime(missionItem.getStartTime());
		obeliskPb.setEndTime(missionItem.getEndTime());
		// 有联盟才有进度 . 这里是联盟进度
		if (player.hasGuild()) {
			int guildNumber = missionItem.getGuildValue(player.getGuildId());
			obeliskPb.setNum(guildNumber);
		}
		if (missionItem.getState() == Obelisk.PBObeliskMissionState.NOTOPEN) {
			// 未开始
			obeliskPb.setState(Obelisk.PBObeliskPlayerState.NOT_OPEN);
		} else if (missionItem.getState() == Obelisk.PBObeliskMissionState.OPEN) {
			// 进行中
			obeliskPb.setState(Obelisk.PBObeliskPlayerState.OPENING);
		} else if (missionItem.getState() == Obelisk.PBObeliskMissionState.CLOSED) {
			ObeliskCfg cfg = missionItem.getObeliskCfg();
			// 任务已结束. 结算玩家数据, 记录贡献度等
			ObeliskEntity obeliskEntity = player.getData().getObeliskByCfgIdOrCreate(cfg.getId());
			// 玩家还没有结算过
			if (obeliskEntity.getState().getNumber() < Obelisk.PBObeliskPlayerState.FAILED.getNumber()) {
				// 玩家还没有结算过, 针对超时过期的任务进行特殊处理
				if (checkOverTimeComplete(missionItem, true)) {
					obeliskEntity.setState(Obelisk.PBObeliskPlayerState.FINISHED);
				} else {
					// 排行榜任务结束时统一结算修改该盟的玩家状态,,,此逻辑还未结算的,认定为当时不在联盟,所以设置失败状态
					obeliskEntity.setState(Obelisk.PBObeliskPlayerState.FAILED);
				}

				HawkLog.logPrintln("GuildKillFoggyMission buildPbToPlayer end playerId:{},state:{}", player.getId(), obeliskEntity.getState().getNumber());
			}
			obeliskPb.setContribution(obeliskEntity.getContribution());
			obeliskPb.setState(obeliskEntity.getState());
		}
		return obeliskPb;
	}

}
