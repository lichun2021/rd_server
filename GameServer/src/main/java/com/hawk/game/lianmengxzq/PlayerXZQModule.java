package com.hawk.game.lianmengxzq;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.config.XZQAwardCfg;
import com.hawk.game.config.XZQConstCfg;
import com.hawk.game.config.XZQPointCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.lianmengxzq.task.XZQFoceColorSetCancelInvoker;
import com.hawk.game.lianmengxzq.task.XZQGiveUpInvoker;
import com.hawk.game.lianmengxzq.task.XZQSignupCancelInvoker;
import com.hawk.game.lianmengxzq.task.XZQSignupInvoker;
import com.hawk.game.lianmengxzq.worldpoint.XZQWorldPoint;
import com.hawk.game.msg.GuildJoinMsg;
import com.hawk.game.msg.GuildQuitMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.XZQ.PBXZQCancelSignupReq;
import com.hawk.game.protocol.XZQ.PBXZQForceColorSetReq;
import com.hawk.game.protocol.XZQ.PBXZQGiftRecodReq;
import com.hawk.game.protocol.XZQ.PBXZQGiveUpReq;
import com.hawk.game.protocol.XZQ.PBXZQQuarterInfoReq;
import com.hawk.game.protocol.XZQ.PBXZQSendGiftPlayerInfo;
import com.hawk.game.protocol.XZQ.PBXZQSendGiftReq;
import com.hawk.game.protocol.XZQ.PBXZQSignupReq;
import com.hawk.game.protocol.XZQ.PBXZQStatus;
import com.hawk.game.protocol.XZQ.XZQAllGiftReceiveCountResp;
import com.hawk.game.protocol.XZQ.XZQGiftReceiveCountReq;
import com.hawk.game.protocol.XZQ.XZQGiftReceiveCountResp;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.GameUtil;
import com.hawk.gamelib.GameConst.MsgId;

public class PlayerXZQModule extends PlayerModule{

	public PlayerXZQModule(Player player) {
		super(player);
	}
	
	@Override
	protected boolean onPlayerLogin() {
		//推送一下
		XZQService.getInstance().onShowPage(player);
		XZQService.getInstance().syncXZQEffectVal(this.player);
		return true;
	}
	
	
	@ProtocolHandler(code = HP.code.XZQ_GIVE_UP_C_VALUE)
	public void onXZQGiveup(HawkProtocol protocol){
		PBXZQGiveUpReq req = protocol.parseProtocol(PBXZQGiveUpReq.getDefaultInstance());
		int buildId = req.getBuidingId();
		XZQPointCfg cfg = HawkConfigManager.getInstance().getConfigByKey(XZQPointCfg.class,buildId);
		if(cfg == null){
			return;
		}
		int pos = GameUtil.combineXAndY(cfg.getX(), cfg.getY());
		XZQService.getInstance().dealMsg(MsgId.XZQ_GICE_UP, new XZQGiveUpInvoker(this.player,pos,req));
	}
	
	/**
	 * 小站区报名
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.XZQ_WAR_SIGN_UP_C_VALUE)
	public void onXZQSignup(HawkProtocol protocol){
		boolean open = XZQConstCfg.getInstance().isOpen();
		if(!open){
			return;
		}
		PBXZQSignupReq req = protocol.parseProtocol(PBXZQSignupReq.getDefaultInstance());
		int buildId = req.getBuidingId();
		XZQPointCfg cfg = HawkConfigManager.getInstance().getConfigByKey(XZQPointCfg.class,buildId);
		if(cfg == null){
			return;
		}
		int pos = GameUtil.combineXAndY(cfg.getX(), cfg.getY());
		if(player.isCsPlayer()){
			return;
		}
		XZQService.getInstance().dealMsg(MsgId.XZQ_SIGN_UP, new XZQSignupInvoker(this.player,pos,req));
		
	}
	
	
	/**
	 * 小站区取消报名
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.XZQ_WAR_SIGN_UP_CANCEL_C_VALUE)
	public void onXZQSignupCancel(HawkProtocol protocol){
		PBXZQCancelSignupReq req = protocol.parseProtocol(PBXZQCancelSignupReq.getDefaultInstance());
		int buildId = req.getBuidingId();
		XZQPointCfg cfg = HawkConfigManager.getInstance().getConfigByKey(XZQPointCfg.class,buildId);
		if(cfg == null){
			return;
		}
		int pos = GameUtil.combineXAndY(cfg.getX(), cfg.getY());
		XZQService.getInstance().dealMsg(MsgId.XZQ_CANCEL_SIGN_UP, new XZQSignupCancelInvoker(this.player,pos,req));
	}
	
	
	
	/**
	 * 小站区设置势力颜色
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.XZQ_MAP_FORCE_COLOR_SET_C_VALUE)
	public void onXZQForceColorSet(HawkProtocol protocol){
		PBXZQForceColorSetReq req = protocol.parseProtocol(PBXZQForceColorSetReq.getDefaultInstance());
		int color = req.getColor();
		XZQService.getInstance().dealMsg(MsgId.XZQ_FORCE_COLOR_SET, new XZQFoceColorSetCancelInvoker(this.player,color));
	}
	
	
	
	@ProtocolHandler(code = HP.code.XZQ_GUILD_WAR_DTAIL_C_VALUE)
	public void onXZQGuilldWarInfo(HawkProtocol protocol){
		XZQService.getInstance().onXZQGuildWarInfo(player);
	}
	
	
	/**
	 * 小站区礼包记录
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.XZQ_GIFT_RECORD_C_VALUE)
	private void onXZQGiftRecord(HawkProtocol protocol) {
		if (!player.hasGuild()) {
			return;
		}
		PBXZQGiftRecodReq req = protocol.parseProtocol(PBXZQGiftRecodReq.getDefaultInstance());
		XZQGift.getInstance().syncXZQGiftSendRecord(player,req.getBuildId());
	}
	
	/**
	 * 小战区礼包信息
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.XZQ_GIFT_INFO_C_VALUE)
	private void onXZQGiftInfo(HawkProtocol protocol) {
		if (!player.hasGuild()) {
			return;
		}
		XZQGift.getInstance().syncXZQGiftInfo(player);
	}
	
	/**
	 * 小战区礼包接收数量
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.XZQ_GIFT_RECEIVE_COUNT_C_VALUE)
	private void onXZQGiftReceiveCountReq(HawkProtocol protocol) {
		XZQGiftReceiveCountReq req = protocol.parseProtocol(XZQGiftReceiveCountReq.getDefaultInstance());
		String playerId = req.getPlayerId();
		int giftId = req.getGiftId();
		int buildId = req.getBuildId();
		
		XZQGiftReceiveCountResp.Builder builder = XZQGiftReceiveCountResp.newBuilder();
		XZQAwardCfg config = HawkConfigManager.getInstance().getConfigByKey(XZQAwardCfg.class, giftId);
		int limitCount = config.getNumberLimit();
		
		int turnCount = XZQService.getInstance().getXZQTermId();
		int receiveCount = XZQRedisData.getInstance().
				getXZQPlayerReceiveCount(turnCount, buildId, playerId, giftId);
		builder.setReceiveCount(receiveCount);
		builder.setLimitCount(limitCount);
		builder.setPlayerId(playerId);
		builder.setGiftId(giftId);
		builder.setBuildId(buildId);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.XZQ_GIFT_RECEIVE_COUNT_S, builder));
	}
	
	/**
	 * 小战区礼包接收数量
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code2.XZQ_GIFT_ALL_RECEIVE_COUNT_C_VALUE)
	private void onAllXZQGiftReceiveCountReq(HawkProtocol protocol) {
		XZQGiftReceiveCountReq req = protocol.parseProtocol(XZQGiftReceiveCountReq.getDefaultInstance());
		int giftId = req.getGiftId();
		int buildId = req.getBuildId();
		
		
		XZQAwardCfg config = HawkConfigManager.getInstance().getConfigByKey(XZQAwardCfg.class, giftId);
		int limitCount = config.getNumberLimit();
		
		int turnCount = XZQService.getInstance().getXZQTermId();
		Map<String, String> receiveMap = XZQRedisData.getInstance().getAllXZQPlayerReceiveCount(turnCount, buildId, giftId);
		XZQAllGiftReceiveCountResp.Builder builder = XZQAllGiftReceiveCountResp.newBuilder();
		for (Entry<String, String> sendInfo : receiveMap.entrySet()) {
			XZQGiftReceiveCountResp.Builder sendBuilder = XZQGiftReceiveCountResp.newBuilder();
			sendBuilder.setReceiveCount(Integer.valueOf(sendInfo.getValue()));
			sendBuilder.setLimitCount(limitCount);
			sendBuilder.setPlayerId(sendInfo.getKey());
			sendBuilder.setGiftId(giftId);
			sendBuilder.setBuildId(buildId);
			builder.addSendInfo(sendBuilder);
		}
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.XZQ_GIFT_ALL_RECEIVE_COUNT_S_VALUE, builder));
	}
	
	/**
	 * 小战区颁发礼包
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.XZQ_SENT_GIFT_C_VALUE)
	private void onSendGift(HawkProtocol protocol) {
		boolean open = XZQConstCfg.getInstance().isOpen();
		if(!open){
			return;
		}
		PBXZQSendGiftReq req = protocol.parseProtocol(PBXZQSendGiftReq.getDefaultInstance());
		int pointId = req.getBuildId();
		XZQPointCfg cfg = HawkConfigManager.getInstance().getConfigByKey(XZQPointCfg.class, pointId);
		if(cfg ==null){
			return;
		}
		if (!player.hasGuild()) {
			return;
		}
		if(XZQService.getInstance().getState() != PBXZQStatus.XZQ_HIDDEN){
			sendError(protocol.getType(), Status.XZQError.XZQ_GIFT_NOT_HAVE_VALUE);
			return;
		}
		// 建筑不存在
		XZQWorldPoint point = XZQService.getInstance().getXZQPoint(GameUtil.combineXAndY(cfg.getX(), cfg.getY()));
		if (point == null) {
			return;
		}
		int giftId = req.getGiftId();
		XZQAwardCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(XZQAwardCfg.class, giftId);
		if (giftCfg == null) {
			return;
		}
		// 颁发玩家无效
		List<PBXZQSendGiftPlayerInfo> playerInfos = req.getPlayerInfoList();
		if (playerInfos == null || playerInfos.isEmpty()) {
			return;
		}
		//调整发放列表
		int needSendCount = 0;
		Map<String,Integer> sendList = new HashMap<>();
		for(PBXZQSendGiftPlayerInfo playerInfo: playerInfos){
			sendList.put(playerInfo.getPlayerId(), playerInfo.getCount());
			needSendCount += playerInfo.getCount();
		}
		
		// 不能给不是本盟的玩家颁发
		for(PBXZQSendGiftPlayerInfo playerInfo : playerInfos){
			if (!GuildService.getInstance().isInTheSameGuild(player.getId(), playerInfo.getPlayerId())) {
				sendError(protocol.getType(), Status.Error.GUILD_PLAYER_HASNOT_GUILD);
				return;
			}
		}
		// 不是盟主不能颁发
		if (!GuildService.getInstance().isGuildLeader(player.getId())) {
			sendError(protocol.getType(), Status.XZQError.XZQ_SEND_GIFT_AUTH_LIMIT_VALUE);
			return;
		}
		// 礼包发放数据校验
		int termId = XZQService.getInstance().getXZQTermId();
		String giftInfo = XZQRedisData.getInstance().getXZQGiftInfo(termId, player.getGuildId(),point.getXzqCfg().getId(), giftId);
		if (HawkOSOperator.isEmptyString(giftInfo)) {
			sendError(protocol.getType(), Status.XZQError.XZQ_GIFT_NOT_HAVE_VALUE);
			return;
		}
		String[] giftInfoSplit = giftInfo.split("_");
		int sendNum = Integer.parseInt(giftInfoSplit[0]);
		int totalNum = Integer.parseInt(giftInfoSplit[1]);
		if (!XZQGift.getInstance().sendGiftCheck(needSendCount, totalNum, sendNum)) {
			return;
		}
		if (XZQGift.getInstance().isOnePlayerCountLimit(point.getXzqCfg().getId(), giftId, sendList)) {
			sendError(protocol.getType(), Status.XZQError.XZQ_SEND_GIFT_ONE_LIMIT_VALUE);
			return;
		}
		// 颁发是否成功
		for(Entry<String, Integer> entry : sendList.entrySet()){
			XZQGift.getInstance().sendGift(termId,pointId, entry.getKey(), giftId,entry.getValue(), player);
			Player recievePlayer = GlobalData.getInstance().makesurePlayer(entry.getKey());
			if(Objects.nonNull(recievePlayer)){
				//Tlog打点
				XZQTlog.XZQGfitSend(recievePlayer, termId, player.getGuildId(),point.getXzqCfg().getId(), player.getId(), giftId, entry.getValue());
			}
		}
		// 礼包个数更新
		XZQRedisData.getInstance().updateXZQGiftInfo(termId, player.getGuildId(),point.getXzqCfg().getId(),giftId, 
				sendNum + needSendCount, totalNum);
		// 通用成功返回
		player.responseSuccess(protocol.getType());
		// 礼包信息刷新
		XZQGift.getInstance().syncXZQGiftInfo(player);
	}

	
	
	/**
	 * 获取超级武器驻军列表
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.XZQ_QUARTER_INFO_C_VALUE)
	private void getXZQBuildQuarterInfo(HawkProtocol protocol) {
		PBXZQQuarterInfoReq req = protocol.parseProtocol(PBXZQQuarterInfoReq.getDefaultInstance());
		int pointId = GameUtil.combineXAndY(req.getPosX(), req.getPosY());
		XZQWorldPoint xzqPoint = XZQService.getInstance().getXZQPoint(pointId);
		if (xzqPoint == null) {
			return;
		}
		xzqPoint.sendXZQQuarterInfo(player);
		return;
	}
	
	
	@MessageHandler
	private void onPlayerQuitGuild(GuildQuitMsg msg){
		boolean open = XZQConstCfg.getInstance().isOpen();
		if(!open){
			return;
		}
		String guildId = msg.getGuildId();
		XZQService.getInstance().syncQuitGuildXZQEffectVal(player,guildId);
	}
	
	@MessageHandler
	private void onPlayerJoinGuild(GuildJoinMsg msg){
		boolean open = XZQConstCfg.getInstance().isOpen();
		if(!open){
			return;
		}
		String guildId = msg.getGuildId();
		XZQService.getInstance().syncJoinGuildXZQEffectVal(player, guildId);
	}
}
