package com.hawk.game.module.obelisk.service.mission.type;

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
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.event.EventAttackFoggy;

/**
 * 全服共击杀N个X级幽灵基地
 * @author hf
 */
@ObeliskMission(missionType = ObeliskMissionType.WORLD_KILL_FOGGY)
public class WorldKillFoggyMission implements IObeliskMission {

	@Override
	public Class<? extends MissionEvent> getEventClassType(){
		return EventAttackFoggy.class;
	}

	@Override
	public ObeliskMissionType getObeliskMissionType() {
		return IObeliskMission.super.getObeliskMissionType();
	}

	@Override
	public void onMissionStart(ObeliskMissionItem missionItem) {
		boolean isCheck = checkOverTimeComplete(missionItem,  true);
		ObeliskCfg cfg = missionItem.getObeliskCfg();
		//校验是否直接完成
		if (isCheck){
			missionItem.addValue(cfg.getCount());
			//修改状态
			missionItem.setState(Obelisk.PBObeliskMissionState.CLOSED);
			return;
		}
		
	}

	@Override
	public void onTick(ObeliskMissionItem missionItem) {
		if (missionItem.getState() == Obelisk.PBObeliskMissionState.NOTOPEN) {
			boolean start = startMission(missionItem);
			if (start) {
				// 如果任务成功开启
				onMissionStart(missionItem);
				ObeliskService.getInstance().noticeNewPoint();
			}
		}
		if (missionItem.getState() == Obelisk.PBObeliskMissionState.OPEN) {
			boolean close = closeMission(missionItem);
			if (close) {
				ObeliskService.getInstance().noticeNewPoint();
			}
		}
	}

	@Override
	public void refreshMission(Player player,ObeliskMissionItem missionItem, MissionEvent missionEvent){
		EventAttackFoggy eventAttackFoggy = (EventAttackFoggy)missionEvent;
		if (!eventAttackFoggy.isWin()){
			return;
		}
		ObeliskCfg obeliskCfg = missionItem.getObeliskCfg();
		if(eventAttackFoggy.getFoggyLvl() < obeliskCfg.getPara1()){
			return;
		}
		//满足条件数量加一
		missionItem.addValue(1);
		//处理相关贡献度计算,,累计型的相关问题
		calculateContribution(player, obeliskCfg);

		HawkLog.logPrintln("WorldKillFoggyMission refreshMission playerId:{}, foggyLv:{}", player.getId(), eventAttackFoggy.getFoggyLvl());
	}

	@Override
	public void calculateContribution(Player player,ObeliskCfg cfg) {
		ObeliskEntity obeliskEntity = player.getData().getObeliskByCfgIdOrCreate(cfg.getId());
		if (obeliskEntity.getContribution() == -1){
			obeliskEntity.setContribution(1);
		}else{
			obeliskEntity.addContribution(1);
		}
		HawkLog.logPrintln("WorldKillFoggyMission calculateContribution playerId:{},afterContribution:{}", player.getId(), obeliskEntity.getContribution());
	}

	@Override
	public Builder buildPbToPlayer(Player player, ObeliskMissionItem missionItem) {
		Obelisk.PBObelisk.Builder obeliskPb = Obelisk.PBObelisk.newBuilder();
		obeliskPb.setCfgId(missionItem.getCfgId());
		int obeliskNum = missionItem.getNum();
		obeliskPb.setNum(obeliskNum);
		obeliskPb.setStartTime(missionItem.getStartTime());
		obeliskPb.setEndTime(missionItem.getEndTime());
		if (missionItem.getState() == Obelisk.PBObeliskMissionState.NOTOPEN){
			obeliskPb.setState(Obelisk.PBObeliskPlayerState.NOT_OPEN);
		}else if(missionItem.getState() == Obelisk.PBObeliskMissionState.OPEN){
			obeliskPb.setState(Obelisk.PBObeliskPlayerState.OPENING);
		}else if(missionItem.getState() == Obelisk.PBObeliskMissionState.CLOSED){
			ObeliskCfg cfg = missionItem.getObeliskCfg();
			ObeliskEntity obeliskEntity = player.getData().getObeliskByCfgIdOrCreate(cfg.getId());
			//玩家还未结算过
			if (obeliskEntity.getState().getNumber() < Obelisk.PBObeliskPlayerState.FAILED_VALUE){
				// 玩家还没有结算过, 针对超时过期的任务进行特殊处理
				if (checkOverTimeComplete(missionItem, false)){
					obeliskEntity.setState(Obelisk.PBObeliskPlayerState.FINISHED);
				}else {
					if (obeliskNum >= cfg.getCount()){
						obeliskEntity.setState(Obelisk.PBObeliskPlayerState.FINISHED);
					}else {
						obeliskEntity.setState(Obelisk.PBObeliskPlayerState.FAILED);
					}
				}
				HawkLog.logPrintln("WorldKillFoggyMission buildPbToPlayer end change state playerId:{},obeliskNum:{},state:{}", player.getId(), obeliskNum,obeliskEntity.getState().getNumber());
			}
			obeliskPb.setState(obeliskEntity.getState());
			obeliskPb.setContribution(obeliskEntity.getContribution());
		}
		return obeliskPb;
	}

}
