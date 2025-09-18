package com.hawk.game.module.lianmengfgyl.march.module;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.CollectionUtils;
import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.delay.HawkDelayAction;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;

import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.module.lianmengfgyl.battleroom.msg.FGYLQuitRoomMsg;
import com.hawk.game.module.lianmengfgyl.march.cfg.FGYLConstCfg;
import com.hawk.game.module.lianmengfgyl.march.data.FGYLActivityStateData;
import com.hawk.game.module.lianmengfgyl.march.data.FGYLGuildJoinData;
import com.hawk.game.module.lianmengfgyl.march.data.FGYLRoomData;
import com.hawk.game.module.lianmengfgyl.march.entity.FGYLPlayerEntity;
import com.hawk.game.module.lianmengfgyl.march.msg.FGYLSignUpInvoker;
import com.hawk.game.module.lianmengfgyl.march.service.FGYLConst.FGYLActivityState;
import com.hawk.game.module.lianmengfgyl.march.service.FGYLMatchService;
import com.hawk.game.msg.GuildJoinMsg;
import com.hawk.game.msg.GuildQuitMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.FGYLWar.PBFGYLSignUpReq;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.warcollege.WarCollegeInstanceService;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.gamelib.GameConst;
import com.hawk.gamelib.GameConst.MsgId;

public class PlayerFGYLWarModule extends PlayerModule {

	public PlayerFGYLWarModule(Player player) {
		super(player);
	}
	
	
	@Override
	protected boolean onPlayerLogin() {
		FGYLMatchService.getInstance().syncWarInfo(player);
		return false;
	}

	
	@ProtocolHandler(code = HP.code2.FGYL_WAR_STATE_INFO_REQ_VALUE)
	public void infoDate(HawkProtocol protocol){
		FGYLMatchService.getInstance().syncWarInfo(player);
	}
	
	
	@ProtocolHandler(code = HP.code2.FGYL_WAR_SIGN_REQ_VALUE)
	public void signupWar(HawkProtocol protocol){
		PBFGYLSignUpReq req = protocol.parseProtocol(PBFGYLSignUpReq.getDefaultInstance());
		int level = req.getLevel();
		int timeIndex = req.getTimeIndex();
		player.msgCall(MsgId.FGYL_WAR_SIGN_UP, FGYLMatchService.getInstance(), new FGYLSignUpInvoker(player, protocol.getType(),level,timeIndex));
	}
	
	
	
	@ProtocolHandler(code = HP.code2.FGYL_WAR_TERM_RANK_REQ_VALUE)
	public void syncWarTermRank(HawkProtocol protocol){
		FGYLMatchService.getInstance().syncWarTermRank(player);
	}
	
	

	@ProtocolHandler(code = HP.code2.FGYL_WAR_HONOR_RANK_REQ_VALUE)
	public void syncWarHonorRank(HawkProtocol protocol){
		FGYLMatchService.getInstance().syncWarHonorRank(player);
	}
	
	
	@ProtocolHandler(code = HP.code2.FGYL_WAR_ENTER_INSTANCE_REQ_VALUE)
	public void onEnterInstance(HawkProtocol hawkProtocol) {	
		if(player.isCsPlayer()){
			player.sendError(hawkProtocol.getType(),Status.CrossServerError.CROSS_ACTION_ILLEGAL_DURNING_OPEN_VALUE, 0);
			return;
		}
		FGYLActivityStateData stateData = FGYLMatchService.getInstance().getDataManger().getStateData();
		if(stateData.getState() != FGYLActivityState.OPEN){
			player.sendError(hawkProtocol.getType(),
					Status.FGYLError.FGYL_WAR_NOT_IN_OPEM_TIME, 0);
			return;
		}
		
		int errorCode = sourceCheckEnterInstance();
		if (errorCode != Status.SysError.SUCCESS_OK_VALUE) {
			this.sendError(hawkProtocol.getType(), errorCode);
			return;
		}			
		FGYLConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(FGYLConstCfg.class);
		WorldPoint worldPoint = WorldPlayerService.getInstance().getPlayerWorldPoint(player.getId());
		long shieldEndTime = HawkTime.getMillisecond()  + constCfg.getPeaceTime()*1000;  
		if (Objects.nonNull(worldPoint) && worldPoint.getShowProtectedEndTime() < shieldEndTime) {
			StatusDataEntity entity = player.addStatusBuff(GameConst.CITY_SHIELD_BUFF_ID, shieldEndTime);			
			if (entity != null) {
				player.getPush().syncPlayerStatusInfo(false, entity);
			}
		}
		
		//如果是本服的则处理.
		simulateCross();
		
	}
	
	
	private void simulateCross() {
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.FGYL_WAR_SIMULATE_CROSS_BEGIN_VALUE));
		player.addDelayAction(HawkRand.randInt(2000, 4000), new HawkDelayAction() {
			@Override
			protected void doAction() {
				player.sendProtocol(HawkProtocol.valueOf(HP.code2.FGYL_WAR_SIMULATE_CROSS_FINISH_VALUE));
				boolean rlt = FGYLMatchService.getInstance().joinRoom(player);
				Player.logger.info("playerId:{} enter local dyzz instance result:{}", player.getId(), rlt);
			}
		});
	}
	
	
	/**
	 * 检测是否可以进入副本
	 * @param serverId
	 * @return
	 */
	private int sourceCheckEnterInstance() {
		int termId = FGYLMatchService.getInstance().getDataManger().getStateData().getTermId();
		String guildId = player.getGuildId();
		if(HawkOSOperator.isEmptyString(guildId)){
			return Status.Error.GUILD_NO_JOIN_VALUE;
		}
		// 有行军
		BlockingQueue<IWorldMarch> marchs = WorldMarchService.getInstance().getPlayerMarch(player.getId());
		if (!marchs.isEmpty()) {
			HawkLog.logPrintln("FGYL sourceCheckEnterInstance,player has march,playerId:{}", player.getId());
			return Status.FGYLError.FGYL_HAS_MARCH_VALUE;
		}
		// 战争狂热
		if (player.getPlayerBaseEntity().getWarFeverEndTime() > HawkTime.getMillisecond()) {
			HawkLog.logPrintln("FGYL sourceCheckEnterInstance,player war fever,playerId:{}", player.getId());
			return Status.FGYLError.FGYL_IN_WAR_FEVER_VALUE;
		}
		// 城内有援助行军，不能进入泰伯利亚
		Set<IWorldMarch> marchList = WorldMarchService.getInstance().getPlayerPassiveMarchs(player.getId(), WorldMarchType.ASSISTANCE_VALUE,
				WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST_VALUE);
		if (!marchList.isEmpty()) {
			HawkLog.logPrintln("FGYL sourceCheckEnterInstance,player has assist march ,playerId:{}", player.getId());
			return Status.FGYLError.FGYL_HAS_ASSISTANCE_MARCH_VALUE;
		}

		// 有被动行军
		BlockingQueue<IWorldMarch> passiveMarchs = WorldMarchService.getInstance().getPlayerPassiveMarch(player.getId());
		if (!CollectionUtils.isEmpty(passiveMarchs)) {
			int playerPos = WorldPlayerService.getInstance().getPlayerPos(player.getId());
			for (IWorldMarch march : passiveMarchs) {
				if (march == null || march.getMarchEntity() == null || march.getMarchEntity().isInvalid()) {
					continue;
				}
				if (march.getTerminalId() != playerPos) {
					continue;
				}
				HawkLog.logPrintln("FGYL sourceCheckEnterInstance,player has passive march ,playerId:{}", player.getId());
				return Status.FGYLError.FGYL_HAS_PASSIVE_MARCH_VALUE;
			}
		}

		// 着火也不行
		if (player.getPlayerBaseEntity().getOnFireEndTime() > HawkTime.getMillisecond()) {
			HawkLog.logPrintln("FGYL sourceCheckEnterInstance,player on fire ,playerId:{}", player.getId());
			return Status.FGYLError.FGYL_ON_FIRE_VALUE;
		}
		if (player.isInDungeonMap()) {
			HawkLog.logPrintln("FGYL sourceCheckEnterInstance,player in dungeon ,playerId:{},dungeon:{}", player.getId(),player.getDungeonMap());
			return Status.Error.PLAYER_IN_INSTANCE_VALUE;
		}
		// 在联盟军演组队中不能.
		if (WarCollegeInstanceService.getInstance().isInTeam(player.getId())) {
			HawkLog.logPrintln("FGYL sourceCheckEnterInstance,player in lmjy team ,playerId:{}", player.getId());
			return Status.Error.LMJY_BAN_OP_VALUE;
		}

		// 已经在跨服中了.
		if (CrossService.getInstance().isCrossPlayer(player.getId())) {
			HawkLog.logPrintln("FGYL sourceCheckEnterInstance,player isCrossPlayer ,playerId:{}", player.getId());
			return Status.CrossServerError.CROSS_FIGHTERING_VALUE;
		}

		FGYLConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(FGYLConstCfg.class);
		FGYLGuildJoinData joinData = FGYLMatchService.getInstance().getDataManger().getFGYLGuildJoinData(guildId);
		if (joinData == null) {
			HawkLog.logPrintln("FGYL sourceCheckEnterInstance,FGYLGuildJoinData null ,playerId:{},guildId:{}", player.getId(),guildId);
			return Status.FGYLError.FGYL_HAS_NO_ROOM_VALUE;
		}
		FGYLRoomData roomData = joinData.getGameRoom();
		if (roomData == null) {
			HawkLog.logPrintln("FGYL sourceCheckEnterInstance,FGYLRoomData NULL err ,playerId:{},guildId:{}", player.getId(),guildId);
			return Status.FGYLError.FGYL_HAS_NO_ROOM_VALUE;
		}
		String roomId = roomData.getRoomId();
		if(roomData.getEndTime() > 0){
			HawkLog.logPrintln("FGYL sourceCheckEnterInstance,room state end ,playerId:{},guildId:{},roomId:{},endTim:{}", player.getId(),guildId,roomId,roomData.getEndTime());
			return Status.FGYLError.FGYL_BATTLE_NEAR_OVER_VALUE;
		}
		long nowTime = HawkTime.getMillisecond();
		long limitTime = roomData.getCreateTime() + constCfg.getBattleTime() * 1000 - TimeUnit.MINUTES.toMillis(2);
		if(nowTime >= limitTime){
			HawkLog.logPrintln("FGYL sourceCheckEnterInstance,room near end ,playerId:{},guildId:{},roomId:{},endTim:{}", player.getId(),guildId,roomId,roomData.getEndTime());
            return Status.FGYLError.FGYL_BATTLE_NEAR_OVER_VALUE;
        }
		FGYLPlayerEntity entity = player.getData().getCommanderEntity().getFgylPlayerEntity();
		if(entity == null){
			HawkLog.logPrintln("FGYL sourceCheckEnterInstance,FGYLPlayerEntity null ,playerId:{},guildId:{},gameId:{}", player.getId(),guildId,roomId);
			return  Status.SysError.PARAMS_INVALID_VALUE;
		}
		if(entity.rewardTerm(termId)){
			HawkLog.logPrintln("FGYL sourceCheckEnterInstance,FGYLPlayerEntity reward already err ,playerId:{},guildId:{},gameId:{},playerReward:{}",
					player.getId(),guildId,roomId,entity.getRewardTerm());
			return  Status.FGYLError.FGYL_WIN_PASS_VALUE;
		}
		if(entity.hasJoinRoom(roomId)){
			HawkLog.logPrintln("FGYL sourceCheckEnterInstance,FGYLPlayerEntity hasJoinRoom already err ,playerId:{},guildId:{},gameId:{},roomId:{}",
					player.getId(),guildId,roomId,entity.getJoinRoomId());
			return  Status.FGYLError.FGYL_HAS_JOINED_WAR_VALUE;
		}
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	

	@ProtocolHandler(code = HP.code2.FGYL_WAR_EXIT_INSTANCE_REQ_VALUE)
	public void onExitInstance(HawkProtocol hawkProtocol) {
		Player.logger.info("playerId:{} exit dyzz war from protocol", player.getId());
		if (player.isCsPlayer()) {
			return;
		}
		//本地操作.
		simulateExitCross();
		DungeonRedisLog.log(player.getId(), "simulateExitCross");
	}
	
	private void simulateExitCross() {
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.FGYL_WAR_SIMULATE_CROSS_BACK_BEGIN));
		player.addDelayAction(HawkRand.randInt(2000, 4000), new HawkDelayAction() {
			@Override
			protected void doAction() {
				player.sendProtocol(HawkProtocol.valueOf(HP.code2.FGYL_WAR_SIMULATE_CROSS_BACK_FINISH_VALUE));	
				Player.logger.info("playerId:{} exit local dyzz instance", player.getId());
			}
		});
	}

	
	
	/**
	 * 退出战场房间
	 * 
	 * @return
	 */
	@MessageHandler
	private boolean onQuitRoomMsg(FGYLQuitRoomMsg msg) {
		try {
			FGYLMatchService.getInstance().quitRoom(player,msg.getQuitReason());
		} catch (Exception e) {
			HawkException.catchException(e);
		}	
		return true;
	}
	
	
	@MessageHandler
	private void onPlayerQuitGuild(GuildQuitMsg msg){
		FGYLMatchService.getInstance().syncWarInfo(player);
	}
	
	@MessageHandler
	private void onPlayerJoinGuild(GuildJoinMsg msg){
		FGYLMatchService.getInstance().syncWarInfo(player);
	}
}
