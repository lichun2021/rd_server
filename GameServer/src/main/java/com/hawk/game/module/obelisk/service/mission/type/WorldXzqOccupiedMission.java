package com.hawk.game.module.obelisk.service.mission.type;

import com.google.common.collect.Table;
import com.hawk.game.config.ObeliskCfg;
import com.hawk.game.entity.ObeliskEntity;
import com.hawk.game.lianmengxzq.XZQService;
import com.hawk.game.module.obelisk.service.ObeliskService;
import com.hawk.game.module.obelisk.service.mission.ObeliskMission;
import com.hawk.game.module.obelisk.service.mission.ObeliskMissionType;
import com.hawk.game.module.obelisk.service.mission.data.ObeliskMissionItem;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Obelisk;
import com.hawk.game.protocol.Obelisk.PBObelisk.Builder;
import com.hawk.game.service.mssion.MissionEvent;
import org.hawk.log.HawkLog;

import java.util.Map;

/**
 * 全服有N个X级小战区被占领
 * @author hf
 */
@ObeliskMission(missionType = ObeliskMissionType.WORLD_XZQ_OCCUPIED_NUM)
public class WorldXzqOccupiedMission implements IObeliskMission{

	@Override
	public Class<? extends MissionEvent> getEventClassType(){
		return null;
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

		HawkLog.logPrintln("WorldXzqOccupiedMission startMission cfgId:{}, value:{}", cfg.getId(), missionItem.getNum());
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
			// 如果任务成功关闭
			boolean close = closeMission(missionItem);
			if (close) {
				ObeliskService.getInstance().noticeNewPoint();
				//更新联盟玩家相关数据,,联盟任务都是以结束节点为准
				updateGuildMemberObeliskState(missionItem);
			}
			//更新小战区数据
			updateXzqMissionItem(missionItem);
		}
	}

	/**
	 * 更新小战区数据
	 * @param missionItem
	 */
	public void updateXzqMissionItem(ObeliskMissionItem missionItem){
		ObeliskCfg cfg = missionItem.getObeliskCfg();
		missionItem.getGuildMap().clear();
		//等级条件
		int param = cfg.getPara1();
		//小战区占领信息
		Table<String, Integer, Integer> xzqControlTables =  XZQService.getInstance().getZXQGuildControls();
		int count = 0;
		for (Map.Entry<String, Map<Integer, Integer>> entry : xzqControlTables.rowMap().entrySet()) {
			Map<Integer, Integer> levelMap = entry.getValue();
			for (Map.Entry<Integer, Integer> entryLevel : levelMap.entrySet()) {
				int level = entryLevel.getKey();
				int num = entryLevel.getValue();
				if (level >= param){
					count += num;
				}
			}
		}
		missionItem.setNum(count);
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
//				if (checkOverTimeComplete(missionItem,false)){
//					obeliskEntity.setState(Obelisk.PBObeliskPlayerState.FINISHED);
//				}else {
//					// 之前计算出的进度值
//					int number = obeliskPb.getNum();
//					// 完成 后面可以领奖励
//					if (number >= cfg.getCount()) {
//						obeliskEntity.setState(Obelisk.PBObeliskPlayerState.FINISHED);
//					} else {
//						// 失败
//						obeliskEntity.setState(Obelisk.PBObeliskPlayerState.FAILED);
//					}
//				}
				//小战区相关的功能下架了，此任务就直接完成了
				obeliskEntity.setState(Obelisk.PBObeliskPlayerState.FINISHED);
				HawkLog.logPrintln("WorldXzqOccupiedMission buildPbToPlayer end change state playerId:{},number:{},state:{}", player.getId(), obeliskPb.getNum(),obeliskEntity.getState().getNumber());
			}
			obeliskPb.setContribution(obeliskEntity.getContribution());
			obeliskPb.setState(obeliskEntity.getState());
		}
		return obeliskPb;

	}
}
