package com.hawk.game.module.obelisk.service.mission.type;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkTime;

import com.hawk.game.GsConfig;
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

/**
 * 所在服务器占领 非本服的跨服盟总
 * @author hf
 */
@ObeliskMission(missionType = ObeliskMissionType.GUIlD_CROSS_PRESIDENT)
public class GuildCrossPresidentMission implements IObeliskMission{

	@Override
	public Class<? extends MissionEvent> getEventClassType(){
		return null;
	}

	@Override
	public ObeliskMissionType getObeliskMissionType() {
		return IObeliskMission.super.getObeliskMissionType();
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
			// 检测跨服盟总的数据
			// 本服主服serverId
			String serverId = GsConfig.getInstance().getServerId();
			int daySign = HawkTime.getYyyyMMddIntVal();
			String pserverId = ObeliskService.getInstance().getServerObeliskMission(this.getObeliskMissionType(), serverId, daySign + "");
			if (StringUtils.isNotEmpty(pserverId) && missionItem.getNum() == 0) {
				missionItem.setNum(NumberUtils.toInt(pserverId));
			}
			// 如果任务成功关闭
			boolean close = closeMission(missionItem);
			if (close) {
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
	public Builder buildPbToPlayer(Player player, ObeliskMissionItem missionItem) {
		Builder obeliskPb = Obelisk.PBObelisk.newBuilder();
		obeliskPb.setCfgId(missionItem.getCfgId());
		obeliskPb.setStartTime(missionItem.getStartTime());
		obeliskPb.setEndTime(missionItem.getEndTime());
		int obeliskNum = missionItem.getNum();
		obeliskPb.setNum(obeliskNum);
		if (missionItem.getState() == Obelisk.PBObeliskMissionState.NOTOPEN) {
			obeliskPb.setState(Obelisk.PBObeliskPlayerState.NOT_OPEN);
		} else if (missionItem.getState() == Obelisk.PBObeliskMissionState.OPEN) {
			obeliskPb.setState(Obelisk.PBObeliskPlayerState.OPENING);
		} else if (missionItem.getState() == Obelisk.PBObeliskMissionState.CLOSED) {
			ObeliskCfg cfg = missionItem.getObeliskCfg();
			// 任务已结束. 结算玩家数据, 记录贡献度等
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
				HawkLog.logPrintln("GuildCrossPresidentMission buildPbToPlayer end change state playerId:{},obeliskNum:{},state:{}", player.getId(), obeliskNum,obeliskEntity.getState().getNumber());
			}
			obeliskPb.setContribution(obeliskEntity.getContribution());
			obeliskPb.setState(obeliskEntity.getState());
		}
		return obeliskPb;
	}

}
