package com.hawk.game.module.lianmengfgyl.march.service.state;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.obj.HawkObjBase;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.uuid.HawkUUIDGenerator;
import org.hawk.xid.HawkXID;

import com.hawk.game.GsApp;
import com.hawk.game.entity.CommanderEntity;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.module.lianmengfgyl.battleroom.FGYLBattleRoom;
import com.hawk.game.module.lianmengfgyl.battleroom.FGYLExtraParam;
import com.hawk.game.module.lianmengfgyl.battleroom.FGYLRoomManager;
import com.hawk.game.module.lianmengfgyl.march.cfg.FGYLConstCfg;
import com.hawk.game.module.lianmengfgyl.march.cfg.FGYLTimeCfg;
import com.hawk.game.module.lianmengfgyl.march.data.FGYLActivityStateData;
import com.hawk.game.module.lianmengfgyl.march.data.FGYLGuildJoinData;
import com.hawk.game.module.lianmengfgyl.march.data.FGYLRoomData;
import com.hawk.game.module.lianmengfgyl.march.entity.FGYLPlayerEntity;
import com.hawk.game.module.lianmengfgyl.march.service.FGYLConst.FGYLActivityState;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.FGYLWar.PBFGYLWarState;
import com.hawk.game.protocol.FGYLWar.PBFGYLWarStateInfo;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;

public class FGYLState200Open extends IFGYLServiceState {
	
	private int yearday;
	
	
	@Override
	public void init() {
		//重置一下状态
		this.getDataManager().getStateData().setState(FGYLActivityState.OPEN);
		this.getDataManager().getStateData().saveRedis();
	}

	@Override
	public void tick() {
		FGYLActivityStateData data = this.calcInfo();
		//如果不在当前状态，则往下个状态推进
		FGYLActivityStateData curData = this.getDataManager().getStateData();
		if(curData.getTermId() != data.getTermId()
				|| curData.getState() != data.getState()){
			this.getService().updateState(FGYLActivityState.END);
			return;
		}
		//创建副本
		this.createFight();
		//检查副本,主要针对一些异常做下修复
		this.checkFight();
		//跨天广播
		this.checkCrossDay();
	}
	
	
	public void checkCrossDay(){
		int curDay = HawkTime.getYearDay();
		if(this.yearday <= 0){
			this.yearday = curDay;
			return;
		}
		if(this.yearday == curDay){
			return;
		}
		this.yearday = curDay;
		this.getService().boardAllPlayers();
	}

	
	public void checkFight(){
		Map<String, FGYLGuildJoinData> map = this.getDataManager().getAllFGYLGuildJoinData();
		if(map.size() <= 0){
			return;
		}
		long curTime = HawkTime.getMillisecond();
		List<FGYLGuildJoinData> list= new ArrayList<>();
		list.addAll(map.values());
		FGYLConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(FGYLConstCfg.class);
		
		for(FGYLGuildJoinData joinData : list){
			FGYLRoomData roomData = joinData.getGameRoom();
			if(Objects.isNull(roomData)){
				continue;
			}
			if(roomData.getEndTime() > 0){
				continue;
			}
			long createTime = roomData.getCreateTime();
			long endTimeLimit = createTime + constCfg.getBattleTime()*1000 + HawkTime.MINUTE_MILLI_SECONDS * 10;
			if(curTime < endTimeLimit){
				continue;
			}
			//副本一直不结束
			roomData.setEndTime(curTime);
			roomData.setRlt(0);
			roomData.setErr(1);
			joinData.saveRedis();
			this.getService().boardGuildPlayers(joinData.getGuildId());
		}
	}
	
	public void createFight(){
		Map<String, FGYLGuildJoinData> map = this.getDataManager().getAllFGYLGuildJoinData();
		if(map.size() <= 0){
			return;
		}
		long curTime = HawkTime.getMillisecond();
		List<FGYLGuildJoinData> list= new ArrayList<>();
		list.addAll(map.values());
		for(FGYLGuildJoinData joinData : list){
			try {
				this.createBattleRoom(joinData, curTime);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	
	
	public void createBattleRoom(FGYLGuildJoinData joinData,long curTime){
		//已经成功创建了副本
		if(joinData.getCreateFightRlt() > 0){
			return;
		}
		//还不到时间
		if(curTime < joinData.getFightCreateTime()){
			return;
		}
		GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(joinData.getGuildId());
		if(Objects.isNull(guild)){
			return;
		}
		FGYLConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(FGYLConstCfg.class);
		String roomId = HawkUUIDGenerator.genUUID();
		FGYLExtraParam param = new FGYLExtraParam();
		param.setDifficult(joinData.getFightLevel());
		param.setBattleId(roomId);
		param.setCampAGuild(guild.getId());
		param.setCampAGuildName(guild.getName());
		param.setCampAGuildTag(guild.getTag());
		param.setCampAguildFlag(guild.getFlagId());
		param.setCampAServerId(guild.getServerId());
		boolean createRlt = FGYLRoomManager.getInstance().creatNewBattle(curTime, curTime + constCfg.getBattleTime() * 1000, roomId, param);
		if(createRlt){
			FGYLBattleRoom fightRoom = this.getFightRoom(roomId);
			if(Objects.nonNull(fightRoom)){
				FGYLRoomData room = new FGYLRoomData();
				room.setRoomId(roomId);
				room.setGuildId(guild.getId());
				room.setSignTime(joinData.getSignTime());
				room.setSignBattleIndex(joinData.getSignBattleIndex());
				room.setFightLevel(joinData.getFightLevel());
				room.setConfingStartTime(joinData.getFightCreateTime());
				room.setCreateTime(curTime);
				room.setRlt(0);
				joinData.setCreateFightRlt(1);
				joinData.recordFightRoom();
				joinData.setGameRoom(room);
				joinData.saveRedis();
				this.getService().boardGuildPlayers(guild.getId());
				LogUtil.logFGYLCreateRoom(joinData.getTermId(), guild.getId(), joinData.getFightLevel(), roomId);
			}
		}
	}
	
	
	
	/**
	 * 获取战斗房间
	 * @param roomId
	 * @return
	 */
	public FGYLBattleRoom getFightRoom(String roomId){
		HawkXID roomXid = HawkXID.valueOf(GsConst.ObjType.FGYLAOGUAN_ROOM, roomId);
		HawkObjBase<HawkXID, HawkAppObj> roomObj = GsApp.getInstance().getObjMan(GsConst.ObjType.FGYLAOGUAN_ROOM).queryObject(roomXid);
		if (roomObj != null) {
			FGYLBattleRoom room = (FGYLBattleRoom) roomObj.getImpl();
			return room;
		}
		return null;
	}
	

	@Override
	public void addBuilder(PBFGYLWarStateInfo.Builder builder, Player player) {
		FGYLActivityStateData stateData = this.getDataManager().getStateData();
		int termdId = stateData.getTermId();
		builder.setTermId(termdId);
		String guildId = player.getGuildId();
		
		FGYLConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(FGYLConstCfg.class);
		FGYLTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(FGYLTimeCfg.class, termdId);
		CommanderEntity entity = player.getData().getCommanderEntity();
	    FGYLPlayerEntity fdata = entity.getFgylPlayerEntity();
		int rewardTerm = 0;
		if(fdata.rewardTerm(termdId)){
			rewardTerm = 1;
		}
		builder.setShowTime(timeCfg.getShowTimeValue());
		builder.setStartTime(timeCfg.getStartTimeValue());
		builder.setEndTime(timeCfg.getEndTimeValue());
		builder.setHiddenTime(timeCfg.getHiddenTimeValue());
		builder.setWinReward(rewardTerm);
		//没联盟
		if(HawkOSOperator.isEmptyString(guildId)){
			builder.setState(PBFGYLWarState.FGYL_OPEN_FREE);
			return;
		}
		int passLevel = this.getDataManager().getGuildPassLevel(guildId);
		builder.setPassLevel(passLevel);
		//没有报名信息
		FGYLGuildJoinData joinData = this.getDataManager().getFGYLGuildJoinData(guildId);
		if(Objects.isNull(joinData)){
			builder.setState(PBFGYLWarState.FGYL_OPEN_FREE);
			builder.setSignCount(0);
			return;
		}
		FGYLRoomData room = joinData.getGameRoom();
		int signcount = joinData.getSignCount();
		builder.setSignCount(signcount);
		//如果已经胜利了一场
		if(joinData.getWinPassLevel() > 0){
			builder.setSignLevel(room.getFightLevel());
			builder.setBattleRlt(this.genFGYLWarBattleRltBuilder(room));
			builder.setState(PBFGYLWarState.FGYL_OPEN_WIN);
			return;
		}
		long curTime = HawkTime.getMillisecond();
		//如果今天已经报名了
		if(HawkTime.isSameDay(joinData.getSignTime(), curTime)){
			long fightTime = joinData.getFightCreateTime();
			builder.setSignLevel(joinData.getFightLevel());
			builder.setFigthTime(fightTime);
			//没有开副本，等待开启
			if(Objects.isNull(room)){
				builder.setState(PBFGYLWarState.FGYL_OPEN_SIGN);
				builder.setSignWarTimeIndex(joinData.getSignBattleIndex());
				return;
			}
			//已经结束
			if(room.getEndTime() > 0){
				builder.setState(PBFGYLWarState.FGYL_OPEN_FAIL);
				builder.setBattleRlt(this.genFGYLWarBattleRltBuilder(room));
				return;
			}
			//正在进行中
			boolean joined = fdata.hasJoinRoom(room.getRoomId());
			builder.setState(PBFGYLWarState.FGYL_OPEN_FIGHT);
			builder.setJoined(joined);
			builder.addAllStage(this.genFGYLWarStageBuilder(room));
			return;
		}
		//今天还没有报名
		if(signcount < constCfg.getChallengeNum()){
			//报名次数还够
			builder.setState(PBFGYLWarState.FGYL_OPEN_FREE);
			return;
		}
		//没有次数了  就结束失败状态
		builder.setState(PBFGYLWarState.FGYL_OPEN_FAIL);
		if(room.getRlt() > 0){
			builder.setBattleRlt(this.genFGYLWarBattleRltBuilder(room));
		}
	}

	
	

	


	
}
