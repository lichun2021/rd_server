package com.hawk.game.module.obelisk.service.mission.type;

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
import com.hawk.game.service.mssion.event.EventControlSuperWeapon;
import org.apache.commons.lang.StringUtils;
import org.hawk.log.HawkLog;

/**
 * 所在联盟占领N个战区
 * 任务期间生效  非累计
 * @author hf
 */
@ObeliskMission(missionType = ObeliskMissionType.GUIlD_OCCUPIED_SUPER_WEAPON)
public class GuildSuperWeaponMission implements IObeliskMission{

	@Override
	public Class<? extends MissionEvent> getEventClassType(){
		return EventControlSuperWeapon.class;
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
			boolean close = closeMission(missionItem);
			if (close) {
				//更新联盟玩家相关数据,,联盟任务都是以结束节点为准
				updateGuildMemberObeliskState(missionItem);
				//红点
				ObeliskService.getInstance().noticeNewPoint();
			}
		}
	}

	@Override
	public void onMissionStart(ObeliskMissionItem missionItem) {
		boolean isCheck = checkOverTimeComplete(missionItem,  false);
		//校验是否直接完成
		if (isCheck){
			//修改状态
			missionItem.setState(Obelisk.PBObeliskMissionState.CLOSED);
		}
	}

	@Override
	public void refreshMission(Player player, ObeliskMissionItem missionItem, MissionEvent missionEvent){
		EventControlSuperWeapon eventControlSuperWeapon = (EventControlSuperWeapon) missionEvent;
		if (eventControlSuperWeapon.getPointId() == 0) {
			return;
		}
		String guildId = GuildService.getInstance().getPlayerGuildId(player.getId());
		if (StringUtils.isEmpty(guildId) || missionItem.getState() != Obelisk.PBObeliskMissionState.OPEN) {
			return;
		}
		// 满足条件数量加一
		missionItem.addGuildValue(guildId, 1);

		HawkLog.logPrintln("GuildSuperWeaponMission refreshMission playerId:{}, guildId:{}",player.getId(), guildId);
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
				if (checkOverTimeComplete(missionItem,false)){
					obeliskEntity.setState(Obelisk.PBObeliskPlayerState.FINISHED);
				}else {
					// 任务结束时统一结算修改该盟的玩家状态,,,此逻辑还未结算的,认定为当时不在联盟,所以设置失败状态
					obeliskEntity.setState(Obelisk.PBObeliskPlayerState.FAILED);
				}

				HawkLog.logPrintln("GuildSuperWeaponMission buildPbToPlayer end playerId:{},state:{}", player.getId(), obeliskEntity.getState().getNumber());
			}
			obeliskPb.setContribution(obeliskEntity.getContribution());
			obeliskPb.setState(obeliskEntity.getState());
		}
		return obeliskPb;

	}

}
