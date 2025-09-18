package com.hawk.game.module.obelisk.service.mission.type;

import java.math.BigInteger;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

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
import com.hawk.game.service.mssion.event.EventPowerCreate;

/**
 * 全服有X名玩家战力达到N
 * @author hf
 */
@ObeliskMission(missionType = ObeliskMissionType.WORLD_PLAYER_POWER)
public class WorldPowerMission implements IObeliskMission{

	@Override
	public Class<? extends MissionEvent> getEventClassType(){
		return EventPowerCreate.class;
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
	public void initMission(ObeliskMissionItem missionItem) {
		ObeliskCfg cfg = missionItem.getObeliskCfg();
		int conditionLv = cfg.getPara1();
		int count = 0;
		try {
			String sql = String.format("select count(*) from player where battlePoint >= %s limit 1", conditionLv);
			List<BigInteger> resultList = HawkDBManager.getInstance().executeQuery(sql, null);
			BigInteger countStr = resultList.get(0);
			count = countStr.intValue();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		missionItem.addValue((int) count);

		HawkLog.logPrintln("WorldPowerMission initMission cfgId:{}, value:{}", cfg.getId(), count);
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
				ObeliskService.getInstance().noticeNewPoint();
			}
		}
	}


	@Override
	public void refreshMission(Player player, ObeliskMissionItem missionItem, MissionEvent missionEvent){
		EventPowerCreate eventPowerCreate = (EventPowerCreate)missionEvent;
		long afterPower = eventPowerCreate.getAfterPower();
		long beforePower = eventPowerCreate.getBeforePower();
		// 最高战力改变
		ObeliskCfg cfg = missionItem.getObeliskCfg();
		int conditionPower = cfg.getPara1();
		
		if (player.getPower() >= conditionPower) {
			// 处理相关贡献度计算
			calculateContribution(player, cfg);
		}
		
		if(afterPower!= beforePower && afterPower == player.getEntity().getMaxBattlePoint()){
			//条件战力值
			//战力变化满足了任务条件,通过累计的数据,取出来
			if (beforePower < conditionPower && afterPower >= conditionPower){
				//更新
				missionItem.addValue(1);
				HawkLog.logPrintln("WorldPowerMission refreshMission playerId:{}, value:{},beforePower:{}, afterPower:{}, conditionPower:{}",
						player.getId(), missionItem.getNum(), beforePower, afterPower, conditionPower);
			}
		}
	}


	@Override
	public void calculateContribution(Player player,ObeliskCfg cfg) {
		// 计算玩家贡献度
		ObeliskEntity obeliskEntity = player.getData().getObeliskByCfgIdOrCreate(cfg.getId());
		if (obeliskEntity.getContribution() == -1){
			obeliskEntity.setContribution(1);
		}
		HawkLog.logPrintln("WorldPowerMission calculateContribution playerId:{},afterContribution:{}", player.getId(), obeliskEntity.getContribution());
	}
	@Override
	public Builder buildPbToPlayer(Player player, ObeliskMissionItem missionItem) {
		Obelisk.PBObelisk.Builder obeliskPb = Obelisk.PBObelisk.newBuilder();
		obeliskPb.setCfgId(missionItem.getCfgId());
		obeliskPb.setStartTime(missionItem.getStartTime());
		obeliskPb.setEndTime(missionItem.getEndTime());
		int num = missionItem.getNum();
		obeliskPb.setNum(num);
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
					// 之前计算出的进度值
					int number = obeliskPb.getNum();
					// 完成 后面可以领奖励
					if (number >= cfg.getCount()) {
						obeliskEntity.setState(Obelisk.PBObeliskPlayerState.FINISHED);
					} else {
						// 失败
						obeliskEntity.setState(Obelisk.PBObeliskPlayerState.FAILED);
					}
				}
				HawkLog.logPrintln("WorldPowerMission buildPbToPlayer end change state playerId:{},number:{},state:{}", player.getId(), obeliskPb.getNum(),obeliskEntity.getState().getNumber());
			}
			obeliskPb.setContribution(obeliskEntity.getContribution());
			obeliskPb.setState(obeliskEntity.getState());
		}
		return obeliskPb;

	}


}
