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
import com.hawk.game.protocol.Obelisk.PBObelisk;
import com.hawk.game.protocol.Obelisk.PBObeliskMissionState;
import com.hawk.game.protocol.Obelisk.PBObeliskPlayerState;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.event.EventMonsterAttack;

/**
 * 全服共击杀N支X级以上野外行军
 * @author hf
 */
@ObeliskMission(missionType = ObeliskMissionType.WORLD_KILL_LEVEL_MONSTER)
public class WorldKillMonsterMission implements IObeliskMission {

	@Override
	public Class<? extends MissionEvent> getEventClassType(){
		return EventMonsterAttack.class;
	}

	@Override
	public ObeliskMissionType getObeliskMissionType() {
		return IObeliskMission.super.getObeliskMissionType();
	}


	@Override
	public void onTick(ObeliskMissionItem missionItem) {
		if (missionItem.getState() == PBObeliskMissionState.NOTOPEN){
			boolean start = startMission(missionItem);
			if (start){
				onMissionStart(missionItem);
				ObeliskService.getInstance().noticeNewPoint();
			}
		}
		if (missionItem.getState() == PBObeliskMissionState.OPEN){
			//检查任务是否关闭
			boolean close = closeMission(missionItem);
			if (close){
				ObeliskService.getInstance().noticeNewPoint();
			}
		}
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
	public void refreshMission(Player player,ObeliskMissionItem missionItem, MissionEvent missionEvent){
		EventMonsterAttack eventMonsterAttack = (EventMonsterAttack)missionEvent;
		if (!eventMonsterAttack.isWin()){
			return;
		}
		ObeliskCfg obeliskCfg = missionItem.getObeliskCfg();
		if(eventMonsterAttack.getLevel() < obeliskCfg.getPara1()){
			return;
		}
		//满足条件数量加一
		missionItem.addValue(eventMonsterAttack.getAtkTimes());
		//处理相关贡献度计算,,累计型的相关问题
		for (int i = 0; i < eventMonsterAttack.getAtkTimes(); i++) {
			calculateContribution(player, obeliskCfg);
		}

		HawkLog.logPrintln("WorldKillMonsterMission refreshMission playerId:{}, monsterLv:{}", player.getId(), eventMonsterAttack.getLevel());
	}

	@Override
	public void calculateContribution(Player player,ObeliskCfg cfg) {
		ObeliskEntity obeliskEntity = player.getData().getObeliskByCfgIdOrCreate(cfg.getId());
		if (obeliskEntity.getContribution() == -1){
			obeliskEntity.setContribution(1);
		}else{
			obeliskEntity.addContribution(1);
		}
		HawkLog.logPrintln("WorldKillMonsterMission calculateContribution playerId:{},afterContribution:{}", player.getId(), obeliskEntity.getContribution());
	}

	@Override
	public PBObelisk.Builder buildPbToPlayer(Player player, ObeliskMissionItem missionItem) {
		PBObelisk.Builder obeliskPb = PBObelisk.newBuilder();
		obeliskPb.setCfgId(missionItem.getCfgId());
		int obeliskNum = missionItem.getNum();
		obeliskPb.setNum(obeliskNum);
		obeliskPb.setStartTime(missionItem.getStartTime());
		obeliskPb.setEndTime(missionItem.getEndTime());
		if (missionItem.getState() == PBObeliskMissionState.NOTOPEN){
			obeliskPb.setState(PBObeliskPlayerState.NOT_OPEN);
		}else if(missionItem.getState() == PBObeliskMissionState.OPEN){
			obeliskPb.setState(PBObeliskPlayerState.OPENING);
		}else if(missionItem.getState() == PBObeliskMissionState.CLOSED){
			ObeliskCfg cfg = missionItem.getObeliskCfg();
			ObeliskEntity obeliskEntity = player.getData().getObeliskByCfgIdOrCreate(cfg.getId());
			//玩家还未结算过
			if (obeliskEntity.getState().getNumber() < PBObeliskPlayerState.FAILED_VALUE){
				// 玩家还没有结算过, 针对超时过期的任务进行特殊处理
				if (checkOverTimeComplete(missionItem,false)){
					obeliskEntity.setState(Obelisk.PBObeliskPlayerState.FINISHED);
				}else {

					if (obeliskNum >= cfg.getCount()){
						obeliskEntity.setState(PBObeliskPlayerState.FINISHED);
					}else {
						obeliskEntity.setState(PBObeliskPlayerState.FAILED);
					}
				}
				HawkLog.logPrintln("WorldKillMonsterMission buildPbToPlayer end change state playerId:{},obeliskNum:{},state:{}", player.getId(), obeliskNum,obeliskEntity.getState().getNumber());
			}
			obeliskPb.setState(obeliskEntity.getState());
			obeliskPb.setContribution(obeliskEntity.getContribution());
		}
		return obeliskPb;
	}


}
