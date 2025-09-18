package com.hawk.game.module;

import java.util.List;
import java.util.Objects;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.LmjyGetRewardEvent;
import com.hawk.activity.helper.PlayerAcrossDayLoginMsg;
import com.hawk.game.config.WarCollegeInstanceCfg;
import com.hawk.game.entity.PlayerWarCollegeEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.invoker.warcollege.WarCollegeInstanceEnterRpcInvoker;
import com.hawk.game.invoker.warcollege.WarCollegeTeamCreateRpcInvoker;
import com.hawk.game.invoker.warcollege.WarCollegeTeamDissolveRpcInvoker;
import com.hawk.game.invoker.warcollege.WarCollegeTeamJoinRpcInvoker;
import com.hawk.game.invoker.warcollege.WarCollegeTeamKickPlayerRpcInvoker;
import com.hawk.game.invoker.warcollege.WarCollegeTeamQuickJoinRpcInvoker;
import com.hawk.game.invoker.warcollege.WarCollegeTeamQuitRpcInvoker;
import com.hawk.game.invoker.warcollege.WarCollegeTeamReqRpcInvoker;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.lianmengjunyan.msg.LMJYGameOverMsg;
import com.hawk.game.lianmengjunyan.msg.LMJYQuitRoomMsg;
import com.hawk.game.lianmengjunyan.msg.LMJYQuitRoomMsg.QuitReason;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.WarCollege.TeamPlayerOper;
import com.hawk.game.protocol.WarCollege.WarCollegeInfo;
import com.hawk.game.protocol.WarCollege.WarCollegeInstanceExterminateReq;
import com.hawk.game.protocol.WarCollege.WarCollegeSpecialTerm;
import com.hawk.game.protocol.WarCollege.WarCollegeTeamCreateReq;
import com.hawk.game.protocol.WarCollege.WarCollegeTeamJoinReq;
import com.hawk.game.protocol.WarCollege.WarCollegeTeamKickPlayerReq;
import com.hawk.game.service.MailService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.util.GsConst;
import com.hawk.game.warcollege.LMJYExtraParam;
import com.hawk.game.warcollege.WarCollegeInstanceService;
import com.hawk.game.warcollege.model.WarCollegeTeam;
import com.hawk.gamelib.GameConst;

public class PlayerWarCollegeModule extends PlayerModule {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PlayerWarCollegeModule.class);
	
	public PlayerWarCollegeModule(Player player) {
		super(player);
	}
	
	@Override
	protected boolean onPlayerLogin() {
		this.syncWarCollege();
		return true;
	}
	
	/**
	 * 玩家跨天消息事件
	 * 
	 * @param msg
	 * @return
	 */
	@MessageHandler
	public boolean onPlayerAcreossDayLogin(PlayerAcrossDayLoginMsg msg) {
		this.syncWarCollege();
		return true;
	}
	
	
	
	/**
	 * 创建队伍,每個副本的每日可打一次，前置副本沒打過不能打後面的
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.WAR_COLLEGE_TEAM_CREATE_REQ_VALUE)
	private void onTeamCreate(HawkProtocol protocol) {
		if(player.lmjyCD > HawkTime.getMillisecond()){
			sendError(protocol.getType(), Status.Error.LMJY_CD_VALUE);
			return;
		}
		// 战争狂热
		if (player.getPlayerBaseEntity().getWarFeverEndTime() > HawkTime.getMillisecond()) {
			sendError(protocol.getType(), Status.Error.LMJY_WAR_FEVER_VALUE);
			return;
		}
		WarCollegeTeamCreateReq cparam = protocol.parseProtocol(WarCollegeTeamCreateReq.getDefaultInstance());		
		WarCollegeTeamCreateRpcInvoker invoker = new WarCollegeTeamCreateRpcInvoker(player, cparam.getInstanceId());
		player.rpcCall(GameConst.MsgId.WAR_COLLEGE_TEAM_CREATE, WarCollegeInstanceService.getInstance(), invoker);
	}
	
	/**
	 * 加入队伍
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.WAR_COLLEGE_TEAM_JOIN_REQ_VALUE)
	private void onTeamJoin(HawkProtocol protocol) {
		if(player.lmjyCD > HawkTime.getMillisecond()){
			sendError(protocol.getType(), Status.Error.LMJY_CD_VALUE);
			return;
		}
		if (player.getPlayerBaseEntity().getWarFeverEndTime() > HawkTime.getMillisecond()) {
			sendError(protocol.getType(), Status.Error.LMJY_WAR_FEVER_VALUE);
			return;
		}
		WarCollegeTeamJoinReq cparam = protocol.parseProtocol(WarCollegeTeamJoinReq.getDefaultInstance());
		WarCollegeTeamJoinRpcInvoker invoker = new WarCollegeTeamJoinRpcInvoker(player, cparam.getTeamId());
		player.rpcCall(GameConst.MsgId.WAR_COLLEGE_TEAM_JOIN, WarCollegeInstanceService.getInstance(), invoker);
	}
	
	/**
	 * 快速加入队伍
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.WAR_COLLEGE_TEAM_FAST_JOIN_REQ_VALUE)
	private void quickJoin(HawkProtocol protocol){
		if(player.lmjyCD > HawkTime.getMillisecond()){
			sendError(protocol.getType(), Status.Error.LMJY_CD_VALUE);
			return;
		}
		if (player.getPlayerBaseEntity().getWarFeverEndTime() > HawkTime.getMillisecond()) {
			sendError(protocol.getType(), Status.Error.LMJY_WAR_FEVER_VALUE);
			return;
		}
		WarCollegeTeamQuickJoinRpcInvoker invoker = new WarCollegeTeamQuickJoinRpcInvoker(player);
		player.rpcCall(GameConst.MsgId.WAR_COLLEGE_QUICK_JOIN_TEAM, WarCollegeInstanceService.getInstance(), invoker);
	}
	
	/**
	 * 请求队伍
	 * invoker里面会做区分逻辑.
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.WAR_COLLEGE_TEAM_REQ_VALUE)
	private void onTeamReq(HawkProtocol protocol) {
//		if(player.lmjyCD > HawkTime.getMillisecond()){
//			sendError(protocol.getType(), Status.Error.LMJY_CD_VALUE);
//			return;
//		}
//		// 战争狂热
//		if (player.getPlayerBaseEntity().getWarFeverEndTime() > HawkTime.getMillisecond()) {
//			sendError(protocol.getType(), Status.Error.LMJY_WAR_FEVER_VALUE);
//			return;
//		}
		WarCollegeTeamReqRpcInvoker invoker = new WarCollegeTeamReqRpcInvoker(player);
		player.rpcCall(GameConst.MsgId.WAR_COLLEGE_TEAM_REQ, WarCollegeInstanceService.getInstance(), invoker);
	}
	
	/**
	 * 剔出玩家
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.WAR_COLLEGE_TEAM_KICK_PLAYER_REQ_VALUE)
	private void onTeamKickMember(HawkProtocol protocol) {
		WarCollegeTeamKickPlayerReq cparam = protocol.parseProtocol(WarCollegeTeamKickPlayerReq.getDefaultInstance());
		if (HawkOSOperator.isEmptyString(cparam.getPlayerId()) || player.getId().equals(cparam.getPlayerId())) {
			this.sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return;
		}
		WarCollegeTeamKickPlayerRpcInvoker invoker = new WarCollegeTeamKickPlayerRpcInvoker(player, cparam.getPlayerId());
		player.rpcCall(GameConst.MsgId.WAR_COLLEGE_TEAM_KICK, WarCollegeInstanceService.getInstance(), invoker);
	}
	
	/**
	 * 退出队伍
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.WAR_COLLEGE_TEAM_QUIT_REQ_VALUE)
	private void onTeamQuit(HawkProtocol protocol) {
		WarCollegeTeamQuitRpcInvoker invoker = new WarCollegeTeamQuitRpcInvoker(player);
		player.rpcCall(GameConst.MsgId.WAR_COLLEGE_TEAM_QUIT, WarCollegeInstanceService.getInstance(), invoker);
	}
	
	/**
	 * 解散队伍
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.WAR_COLLEGE_TEAM_DISSOLVE_REQ_VALUE)
	private void onTeamDissolve(HawkProtocol protocol) {
		WarCollegeTeamDissolveRpcInvoker invoker = new WarCollegeTeamDissolveRpcInvoker(player);
		player.rpcCall(GameConst.MsgId.WAR_COLLEGE_TEAM_DISSOLVE, WarCollegeInstanceService.getInstance(), invoker);
	}
	
	/**
	 * 进入副本
	 * 这个需要调用战斗方面的协议
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.WAR_COLLEGE_ENTER_INSTANCE_REQ_VALUE)
	private void onInstanceEnter(HawkProtocol protocol) {
		if(player.lmjyCD > HawkTime.getMillisecond()){
			sendError(protocol.getType(), Status.Error.LMJY_CD_VALUE);
			return;
		}
		// 战争狂热
		if (player.getPlayerBaseEntity().getWarFeverEndTime() > HawkTime.getMillisecond()) {
			sendError(protocol.getType(), Status.Error.TIBERIUM_IN_WAR_FEVER_VALUE);
			return;
		}
		WarCollegeInstanceEnterRpcInvoker invoker = new WarCollegeInstanceEnterRpcInvoker(player);
		player.rpcCall(GameConst.MsgId.WAR_COLLEGE_ENTER_INSTANCE, WarCollegeInstanceService.getInstance(), invoker);
	}
	
	/**
	 * 邀请加入队伍.
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.WAR_COLLEGE_TEAM_INVITE_REQ_VALUE)
	private void onTeamInvite(HawkProtocol protocol) {
		WarCollegeInstanceService instanceService = WarCollegeInstanceService.getInstance(); 
		WarCollegeTeam team = instanceService.getWarCollegeTeamByPlayerId(player.getId());
		if (team == null){
			this.sendError(protocol.getType(), Status.Error.WAR_COLLEGE_NOT_HAVA_TEAM_VALUE);
			return;
		}
		if(team.inInstance()){
			this.sendError(protocol.getType(), Status.Error.WAR_COLLEGE_TEAM_STATE_NOT_WAIT_VALUE);
			return;
		}
		if(!instanceService.onPass(player.getId())){
			this.sendError(protocol.getType(), Status.Error.WAR_COLLEGE_SHARE_CD_VALUE);
			return;
		}
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("something", "war_college_instance");
		jsonObject.put("action", "onTeamInvite");
		jsonObject.put("playerId", player.getId());
		jsonObject.put("instanceId", team.getInstanceId());
		jsonObject.put("battleId", team.getBattleId());
		jsonObject.put("teamId", team.getTeamId());
		jsonObject.put("leader", team.getLeaderId());
		jsonObject.put("members", team.membersString());
		BehaviorLogger.log4Service(jsonObject.toString(), true);
		
		ChatParames chatParames = ChatParames.newBuilder()
				.setChatType(ChatType.GUILD_HREF)
				.setKey(Const.NoticeCfgId.WAR_COLLEGE_TEAM_INVITE)
				.setPlayer(player)
				.addParms(team.getInstanceId())
				.addParms(team.getTeamId())
				.build();
		ChatService.getInstance().addWorldBroadcastMsg(chatParames);
		player.responseSuccess(HP.code.WAR_COLLEGE_TEAM_INVITE_REQ_VALUE);
	}
	
	/**
	 * 扫荡副本
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.WAR_COLLEGE_EXTERMINATE_REQ_VALUE)
	private void onExterminate(HawkProtocol protocol){
		WarCollegeInstanceExterminateReq req = protocol.parseProtocol(WarCollegeInstanceExterminateReq.getDefaultInstance());
		int instanceId = req.getInstanceId();
		if (!WarCollegeInstanceService.getInstance().isOpen()) {
			this.sendError(protocol.getType(), Status.Error.WAR_COLLEGE_INSTANCE_NOT_OPEN_VALUE);
			return;
		}
		if (!player.hasGuild()) {
			this.sendError(protocol.getType(), Status.Error.WAR_COLLEGE_NOT_HAVE_GUILD_VALUE);
			return ;
		}
		WarCollegeInstanceCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WarCollegeInstanceCfg.class, instanceId);
		PlayerWarCollegeEntity entity = player.getData().getPlayerWarCollegeEntity();
		int exterminateDifficult = 0;
		WarCollegeInstanceCfg maxCfg = HawkConfigManager.getInstance().getConfigByKey(WarCollegeInstanceCfg.class, entity.getMaxInstanceId());
		if(maxCfg != null){
			exterminateDifficult = maxCfg.getMoppingupDifficult();
		}
		entity.checkOrReset();
		String awardInfo = cfg.getReward();
		ItemInfo itemInfo = ItemInfo.valueOf(awardInfo);
		double eff644 = player.getEffect().getEffVal(Const.EffType.LIFE_TIME_CARD_644) * 0.0001 + 1;
		itemInfo.setCount((long)(itemInfo.getCount() * eff644));
		awardInfo = itemInfo.toString();
		if((entity.getRewardCount(instanceId) < cfg.getDailyRewardTimes()) &&
				cfg.getDifficulty() <= exterminateDifficult &&
				!HawkOSOperator.isEmptyString(awardInfo)){
			entity.addInstanceExterminateCount(instanceId);
			entity.addRewardCount(instanceId);
			
			MailParames mailParames = MailParames.newBuilder()
					.setMailId(MailId.WAR_COLLEGE_INSTANCE)
					.setPlayerId(entity.getPlayerId())
					.setRewards(awardInfo)
					.setAwardStatus(MailRewardStatus.NOT_GET)
					.addContents(instanceId)
					.build();
			MailService.getInstance().sendMail(mailParames);
			ActivityManager.getInstance().postEvent(new LmjyGetRewardEvent(entity.getPlayerId()));
			//发首通奖励
			this.checkSendFristReward(entity, instanceId);
			//同步消息
			player.responseSuccess(HP.code2.WAR_COLLEGE_EXTERMINATE_REQ_VALUE);
		}
		//同步数据
		this.syncWarCollege();
	}
	
	
	/**
	 * 同步数据
	 */
	public void syncWarCollege(){
		PlayerWarCollegeEntity entity = player.getData().getPlayerWarCollegeEntity();
		if(entity == null){
			return;
		}
		entity.checkOrReset();
		List<HawkTuple2<Long, Long>> specialTerms = WarCollegeInstanceService.getInstance().getSpecialTerm();
		long specialEnd = WarCollegeInstanceService.getInstance().getSpecialTermOverTime();
		WarCollegeInfo.Builder builder = entity.genWarCollegeInfoBuilder();
		builder.setNormalTermStart(specialEnd);
		for(HawkTuple2<Long, Long> tuple : specialTerms){
			WarCollegeSpecialTerm.Builder spbuilder = WarCollegeSpecialTerm.newBuilder();
			spbuilder.setSpecialTimeStart(tuple.first);
			spbuilder.setSpecialTimeEnd(tuple.second);
			builder.addSpecialTerms(spbuilder);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.WAR_COLLEGE_INFO_RESP_VALUE, builder));
	}
	
	
	@MessageHandler
	private void onQuitRoom(LMJYQuitRoomMsg msg){
		try {
			QuitReason reason = msg.getReason();
			TeamPlayerOper oper = reason == QuitReason.FIREOUT ? TeamPlayerOper. TEAM_PLAYER_FIREOUT: 
				reason == QuitReason.LEAVE? TeamPlayerOper.TEAM_PLAYER_LEAVE : 
					reason == QuitReason.PREPAROVER?TeamPlayerOper.TEAM_PLAYER_PREPAROVER :null;
			WarCollegeInstanceService.getInstance().onTeamQuit(player,oper);
		} catch (Exception e) {
			LOGGER.error("onQuitRoom:"+msg.getBattleRoom().getExtParm(), e);
		}
	}
	
	@MessageHandler
	private void onGameOverMsg(LMJYGameOverMsg msg) {
		try {
			LMJYExtraParam json = msg.getExtParm();
			int instanceId = json.getInstanceId();
			String leaderId = json.getLeaderId();
			String rewardInfo = msg.getWinAward();
			List<ItemInfo> reItems = ItemInfo.valueListOf(rewardInfo);
			for(ItemInfo reward: reItems){
				if(reward.getItemId()== PlayerAttr.MILITARY_SCORE_VALUE){
					double eff644 = player.getEffect().getEffVal(Const.EffType.LIFE_TIME_CARD_644) * GsConst.EFF_PER + 1;
					reward.setCount((long) (reward.getCount() * eff644));
				}
			}
			rewardInfo = ItemInfo.toString(reItems);
			
			if(msg.isWin() && Objects.nonNull(rewardInfo)){
				PlayerWarCollegeEntity entity = player.getData().getPlayerWarCollegeEntity();
				// 记录攻打次数
				entity.addInstanceHitCount(instanceId);
				//发奖
				WarCollegeInstanceCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WarCollegeInstanceCfg.class, instanceId);
				if(entity.getRewardCount(instanceId) < cfg.getDailyRewardTimes()){
					entity.addRewardCount(instanceId);
					MailParames mailParames = MailParames.newBuilder()
							.setMailId(MailId.WAR_COLLEGE_INSTANCE)
							.setPlayerId(entity.getPlayerId())
							.setRewards(rewardInfo)
							.setAwardStatus(MailRewardStatus.NOT_GET)
							.addContents(instanceId)
							.build();
					MailService.getInstance().sendMail(mailParames);
					ActivityManager.getInstance().postEvent(new LmjyGetRewardEvent(entity.getPlayerId()));
				}
				//发首通奖励
				this.checkSendFristReward(entity,instanceId);
				//发带新人奖励
				this.checkSendHelpReward(entity,instanceId,leaderId);
			}
			
			WarCollegeInstanceService.getInstance().onTeamQuit(player,TeamPlayerOper.TEAM_PLAYER_OVER_INSTANCE);
			//同步数据
			syncWarCollege();
		} catch (Exception e) {
			LOGGER.error("onGameOverMsg:"+msg.getExtParm(),e);
		}
	}
	
	
	/**
	 * 发放首通奖励
	 */
	public void checkSendFristReward(PlayerWarCollegeEntity entity,int instanceId){
		long time = HawkTime.getMillisecond();
		int maxPassId = entity.getMaxInstanceId();
		WarCollegeInstanceCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WarCollegeInstanceCfg.class,instanceId);
		if(cfg.getId() >maxPassId){
			return;
		}
		boolean hasReward = entity.instanceHasFirstReward(instanceId);
		if(hasReward){
			return;
		}
		boolean add = entity.addFirstRewardRecord(cfg.getId(), time);
		if(!add){
			return;
		}
		String firstReward = cfg.getFirstReward();
		if(HawkOSOperator.isEmptyString(firstReward)){
			return;
		}
		//发邮件
		MailParames mailParames = MailParames.newBuilder()
				.setMailId(MailId.WAR_COLLEGE_FIRST_REWARD)
				.setPlayerId(entity.getPlayerId())
				.setRewards(firstReward)
				.setAwardStatus(MailRewardStatus.NOT_GET)
				.addSubTitles(instanceId)
				.addContents(instanceId)
				.build();
		MailService.getInstance().sendMail(mailParames);
		
	}
	

	/**
	 * 发放首通奖励
	 */
	public void checkSendHelpReward(PlayerWarCollegeEntity entity,int instanceId,String leaderId){
		WarCollegeInstanceCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WarCollegeInstanceCfg.class,instanceId);
		if(Objects.isNull(cfg)){
			return;
		}
		Player leader = GlobalData.getInstance().makesurePlayer(leaderId);
		if(Objects.isNull(leader)){
			return;
		}
		boolean has = WarCollegeInstanceService.getInstance().hasHelpReward(this.player);
		if(!has){
			return;
		}
		entity.addHelpRwardCount(1);
		String helpReward = cfg.getTeacherReward();
		if(HawkOSOperator.isEmptyString(helpReward)){
			return;
		}
		//发带新奖励
		MailParames mailParames = MailParames.newBuilder()
				.setMailId(MailId.WAR_COLLEGE_HELP_REWARD)
				.setPlayerId(entity.getPlayerId())
				.setRewards(helpReward)
				.setAwardStatus(MailRewardStatus.NOT_GET)
				.addContents(leader.getName())
				.addContents(instanceId)
				.build();
		MailService.getInstance().sendMail(mailParames);
	}
}

