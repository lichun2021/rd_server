package com.hawk.game.module.obelisk.service.mission.type;

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
import com.hawk.game.service.mssion.event.EventGuildMemberChange;
import org.apache.commons.lang.StringUtils;
import org.hawk.log.HawkLog;

import java.util.List;

/**
 * 全服有N个联盟达到X人
 * @author hf
 */
@ObeliskMission(missionType = ObeliskMissionType.GUILD_MEMBER)
public class WorldGuildMemberMission implements IObeliskMission {

	@Override
	public Class<? extends MissionEvent> getEventClassType() {
		return EventGuildMemberChange.class;
	}

	@Override
	public ObeliskMissionType getObeliskMissionType() {
		return IObeliskMission.super.getObeliskMissionType();
	}

	@Override
	public void onTick(ObeliskMissionItem missionItem) {
		if (missionItem.getState() == PBObeliskMissionState.NOTOPEN) {
			// 如果任务成功开启
			boolean start = startMission(missionItem);
			if (start) {
				onMissionStart(missionItem);
				ObeliskService.getInstance().noticeNewPoint();
			}
		}
		if (missionItem.getState() == PBObeliskMissionState.OPEN) {
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
			return;
		}
	}

	@Override
	public void initMission(ObeliskMissionItem missionItem) {
		List<String> guildList = GuildService.getInstance().getGuildIds();
		ObeliskCfg cfg = missionItem.getObeliskCfg();
		int mini = cfg.getPara1();
		for (String guildId : guildList) {
			//联盟当前人数
			int guildMemberNum = GuildService.getInstance().getGuildMemberNum(guildId);
			if (guildMemberNum >= mini){
				missionItem.addGuildValue(guildId, guildMemberNum);
				HawkLog.logPrintln("WorldGuildMemberMission initMission guildId:{}, cfgId:{}, value:{} ",guildId, cfg.getId(), guildMemberNum);
			}
		}
	}


	@Override
	public void refreshMission(Player player, ObeliskMissionItem missionItem, MissionEvent missionEvent) {
		EventGuildMemberChange eventGuildMemberChange = (EventGuildMemberChange) missionEvent;
		String guildId = eventGuildMemberChange.getGuildId();
		if (StringUtils.isEmpty(guildId)) {
			return;
		}
		int oldCnt = missionItem.getGuildValue(guildId);
		ObeliskCfg cfg = missionItem.getObeliskCfg();
		int mini = cfg.getPara1();
		//联盟当前人数
		int memberNum = eventGuildMemberChange.getMemberNum();
		if (memberNum >= mini && memberNum > oldCnt) {
			missionItem.addGuildValue(guildId, memberNum);

			HawkLog.logPrintln("WorldGuildMemberMission refreshMission playerId:{}, memberNum:{}", player.getId(), memberNum);
		}
	}


	@Override
	public PBObelisk.Builder buildPbToPlayer(Player player, ObeliskMissionItem missionItem) {
		PBObelisk.Builder obeliskPb = PBObelisk.newBuilder();
		obeliskPb.setCfgId(missionItem.getCfgId());
		obeliskPb.setStartTime(missionItem.getStartTime());
		obeliskPb.setEndTime(missionItem.getEndTime());
		ObeliskCfg cfg = missionItem.getObeliskCfg();
		//全服联盟满足条件的个数
		long guildNumber = missionItem.getGuildMap().values().stream().filter(memberNum -> memberNum >= cfg.getPara1()).count();
		obeliskPb.setNum((int)guildNumber);

		if (missionItem.getState() == PBObeliskMissionState.NOTOPEN) {
			// 未开始
			obeliskPb.setState(PBObeliskPlayerState.NOT_OPEN);
		} else if (missionItem.getState() == PBObeliskMissionState.OPEN) {
			// 进行中
			obeliskPb.setState(PBObeliskPlayerState.OPENING);
		} else if (missionItem.getState() == PBObeliskMissionState.CLOSED) {
			// 任务已结束. 结算玩家数据, 记录贡献度等
			ObeliskEntity obeliskEntity = player.getData().getObeliskByCfgIdOrCreate(cfg.getId());
			// 玩家还没有结算过
			if (obeliskEntity.getState().getNumber() < PBObeliskPlayerState.FAILED.getNumber()) {
				// 玩家还没有结算过, 针对超时过期的任务进行特殊处理
				if (checkOverTimeComplete(missionItem,false)){
					obeliskEntity.setState(Obelisk.PBObeliskPlayerState.FINISHED);
				}else {
					// 之前计算出的进度值
					int guildNum = obeliskPb.getNum();
					// 完成 后面可以领奖励
					if (guildNum >= cfg.getCount()) {
						obeliskEntity.setState(PBObeliskPlayerState.FINISHED);
						//贡献度在这处理
						int myGuildNumber = missionItem.getGuildMap().getOrDefault(player.getGuildId(), 0);
						if (myGuildNumber >= cfg.getPara1() && obeliskEntity.getContribution() == -1) {
							obeliskEntity.setContribution(1);
						}
					} else {
						// 失败
						obeliskEntity.setState(PBObeliskPlayerState.FAILED);
					}
				}
				HawkLog.logPrintln("WorldGuildMemberMission buildPbToPlayer end change state playerId:{},guildNum:{},state:{}", player.getId(), obeliskPb.getNum(),obeliskEntity.getState().getNumber());
			}
			obeliskPb.setContribution(obeliskEntity.getContribution());
			obeliskPb.setState(obeliskEntity.getState());
		}
		return obeliskPb;
	}

}
