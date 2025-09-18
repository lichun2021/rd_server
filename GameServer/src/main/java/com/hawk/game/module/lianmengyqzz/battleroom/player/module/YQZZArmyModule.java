package com.hawk.game.module.lianmengyqzz.battleroom.player.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;

import com.alibaba.fastjson.JSONObject;
import com.googlecode.protobuf.format.JsonFormat;
import com.hawk.game.config.AllianceSignCfg;
import com.hawk.game.config.GuildConstProperty;
import com.hawk.game.config.OfficerCfg;
import com.hawk.game.crossproxy.CrossProxy;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.entity.QueueEntity;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.PlayerArmyModule;
import com.hawk.game.module.PlayerQueueModule;
import com.hawk.game.module.lianmengtaiboliya.worldpoint.ITBLYBuilding;
import com.hawk.game.module.lianmengyqzz.battleroom.IYQZZWorldPoint;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZConst.YQZZState;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZGuildBaseInfo;
import com.hawk.game.module.lianmengyqzz.battleroom.extra.YQZZNation;
import com.hawk.game.module.lianmengyqzz.battleroom.order.YQZZOrder;
import com.hawk.game.module.lianmengyqzz.battleroom.order.YQZZOrderCollection;
import com.hawk.game.module.lianmengyqzz.battleroom.player.IYQZZPlayer;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.IYQZZBuilding;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZBuildState;
import com.hawk.game.msg.CalcDeadArmy;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.president.PresidentOfficier;
import com.hawk.game.protocol.Army.HPAddSoldierReq;
import com.hawk.game.protocol.CHP;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.GuildManager.GuildAddSignReq;
import com.hawk.game.protocol.GuildManager.GuildRemoveSignReq;
import com.hawk.game.protocol.GuildManager.GuildSign;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Player.MsgCategory;
import com.hawk.game.protocol.President.OfficerType;
import com.hawk.game.protocol.Queue.QueueSpeedUpReq;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.World.YQZZDeclareWar;
import com.hawk.game.protocol.YQZZ.PBYQZZDeclearWar;
import com.hawk.game.protocol.YQZZ.PBYQZZFirstControlBuildMail;
import com.hawk.game.protocol.YQZZ.PBYQZZOrderUseReq;
import com.hawk.game.protocol.YQZZ.YQZZDeclareWarUseResp;
import com.hawk.game.protocol.YQZZ.YQZZGiveupBuildReq;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.MailService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.mail.GuildMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.pushgift.AbstractPushGiftCondition;
import com.hawk.game.service.pushgift.PushGiftConditionEnum;
import com.hawk.game.service.pushgift.PushGiftManager;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.tsssdk.GameTssService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;

public class YQZZArmyModule extends PlayerModule {
	private IYQZZPlayer player;

	public YQZZArmyModule(IYQZZPlayer player) {
		super(player);
		this.player = player;
	}

	/** 放弃建筑 */
	@ProtocolHandler(code = HP.code2.YQZZ_GIVEUP_BUILD_REQ_VALUE)
	private void onGiveUpBuild(HawkProtocol protocol) {
		YQZZGiveupBuildReq req = protocol.parseProtocol(YQZZGiveupBuildReq.getDefaultInstance());
		YQZZNation nation = player.getParent().getNationInfo(player.getCamp());
		if (!Objects.equals(nation.getPresidentId(), player.getId())) {
			player.sendError(protocol.getType(), Status.YQZZError.YQZZ_NOT_PRESIDENT);
			return;
		}
		if (player.getGiveupBuildCd() > player.getParent().getCurTimeMil()) {
			player.sendError(protocol.getType(), Status.YQZZError.YQZZ_NOT_CD);
			return;
		}
		int x = req.getX();
		int y = req.getY();
		IYQZZWorldPoint ipoint = player.getParent().getWorldPoint(x, y).orElse(null);
		if (!(ipoint instanceof IYQZZBuilding)) {
			return;
		}
		IYQZZBuilding build = (IYQZZBuilding) ipoint;
		if (!build.underNationControl(player.getGuildId()) || build.getState() != YQZZBuildState.ZHAN_LING) {
			player.sendError(protocol.getType(), Status.YQZZError.YQZZ_NOT_NATIONCONTROL);
			return;
		}
		YQZZDeclareWar onwerguild = build.getOnwerGuild();
		player.setGiveupBuildCd(player.getParent().getCurTimeMil() + player.getParent().getCfg().getGiveupBuildCD() * 1000);
		build.cleanGuildMarch("");
		build.setOnwerGuild(YQZZDeclareWar.getDefaultInstance());
		build.worldPointUpdate();
		player.responseSuccess(protocol.getType());

		GuildMailService.getInstance().sendGuildMail(onwerguild.getGuildName(), MailParames.newBuilder()
						.setMailId(MailId.YURI_REVENGE_GUILD_RANK_REWARD)
						.addContents(0, 0));
		
		sendGiveupMail(onwerguild,nation,build);
		
		List<IYQZZPlayer> playerList = player.getParent().getPlayerList(YQZZState.GAMEING);
		for (IYQZZPlayer gamer : playerList) {
			if (!gamer.getSubareaBuilds().contains(build) || !Objects.equals(player.getMainServerId(), gamer.getMainServerId())) {
				continue;
			}

			boolean safe = gamer.getSubareaBuilds().stream().filter(stanBuild -> stanBuild.underNationControl(gamer.getGuildId())).findAny().isPresent();
			if (!safe) {
				int[] xy = gamer.getParent().getWorldPointService().randomBornPoint(gamer);
				// 迁城成功
				gamer.getParent().doMoveCitySuccess(gamer, xy);
//				gamer.sendProtocol(HawkProtocol.valueOf(HP.code.MOVE_CITY_NOTIFY_PUSH));
			}
		}
		
	}
	
	private void sendGiveupMail(YQZZDeclareWar onwerguild,YQZZNation nation,IYQZZBuilding build) {
		// TODO 邮件 通知 玩家XXX放弃了XXX联盟对XXX建筑的控制权
		ChatParames paramesAtk = ChatParames.newBuilder()
				.setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST)
				.setKey(NoticeCfgId.YQZZ_GIVEUP_BUILD)
				.addParms(nation.getPresidentName())
				.addParms(onwerguild.getGuildName())
				.addParms(build.getX())
				.addParms(build.getY())
				.build();
		player.getParent().addWorldBroadcastMsg(paramesAtk);
		

		String guildId = onwerguild.getGuildId();
		List<String> send = new ArrayList<>();
		for (IYQZZPlayer gamer : player.getParent().getPlayerList(YQZZState.GAMEING)) {
			if (!Objects.equals(gamer.getGuildId(), guildId)) {
				continue;
			}
			MailParames parames = MailParames.newBuilder().setPlayerId(gamer.getId())
					.addTitles(build.getX(), build.getY())
					.addSubTitles(build.getX(), build.getY())
					.setMailId(MailId.YQZZ_GIVEUP_BUILD_MAIL)
					.addContents(player.getName())
					.addContents(onwerguild.getGuildName())
					.addContents(build.getX())
					.addContents(build.getY())
					.build();
			MailService.getInstance().sendMail(parames);
			send.add(gamer.getId());
		}

		PBYQZZFirstControlBuildMail.Builder builder = PBYQZZFirstControlBuildMail.newBuilder();
		builder.setReward(player.getName());
		builder.setGuildId(guildId);
		builder.addAllExclude(send);
		builder.setX(build.getX());
		builder.setY(build.getY());
		CrossProxy.getInstance().sendNotify(HawkProtocol.valueOf(CHP.code.YQZZ_GIVEUP_BUILD_MAIL_REQ_VALUE, builder), player.getMainServerId(), "");
	}

	/** 使用号令 */
	@ProtocolHandler(code = HP.code2.YQZZ_ORDER_USE_C_VALUE)
	private boolean onUseOrder(HawkProtocol protocol) {
		PBYQZZOrderUseReq req = protocol.parseProtocol(PBYQZZOrderUseReq.getDefaultInstance());
		int officerId = PresidentOfficier.getInstance().getOfficerId(player.getId());
		if (CrossService.getInstance().isImmigrationPlayer(player.getId())) {
			officerId = 0;
			Set<Integer> oset = RedisProxy.getInstance().getPlayerOfficerIdSet(player.getId());
			List<OfficerCfg> clist = HawkConfigManager.getInstance().getConfigIterator(OfficerCfg.class).toList();
			for(OfficerCfg cfg :clist){
				//副官 和 跨服司令都不行
				if (cfg.getOfficeType() == 2 || 
						cfg.getId() == OfficerType.OFFICER_CROSS_PRESIDENT_VALUE) {
					continue;
				}
				if(!oset.contains(cfg.getId())){
					continue;
				}
				officerId = cfg.getId();
			}
		}
		OfficerCfg ofcfg = HawkConfigManager.getInstance().getConfigByKey(OfficerCfg.class, officerId);
		if (ofcfg == null || ofcfg.getOfficeType() == 2) {
			player.sendError(protocol.getType(), Status.Error.ONLY_OFFICER_TO_OPER);
			return false;
		}
		// 消耗
		YQZZOrderCollection orderCollection = player.getParent().getWorldPointService().getBaseByCamp(player.getCamp()).getOrderCollection();
		if (orderCollection == null) {
			return true;
		}
		int orderId = req.getOrderId();
		int rlt = orderCollection.canStartOrder(req, player);
		if (rlt > 0) {
			player.sendError(protocol.getType(), rlt, 0);
			return true;
		}
		YQZZOrder order = orderCollection.getOrder(orderId);

		int cost = order.getConfig().getTechCost();
		if (cost >= player.getBase().getNationTechValue()) {
			player.sendError(protocol.getType(), Status.YQZZError.YQZZ_NATIONTECH_BUGOU_VALUE, 0);
			return true;
		}
		order.startOrder(req, player);
		player.getBase().changeNationTechValue(-cost);

		orderCollection.notifyChange();
		player.getBase().updataNationSkill(order);
		player.responseSuccess(protocol.getType());
		return true;
	}

	/**
	 * 队列加速
	 *
	 * @param hpCode
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.QUEUE_SPEED_UP_C_VALUE)
	private boolean onQueueSpeedUp(HawkProtocol protocol) {
		QueueSpeedUpReq req = protocol.parseProtocol(QueueSpeedUpReq.getDefaultInstance());
		QueueEntity queueEntity = player.getData().getQueueEntity(req.getId());
		if (Objects.isNull(queueEntity)) {
			return false;
		}
		// // 指定队列可以加速
		// boolean canSpeedUp = queueEntity.getQueueType() == QueueType.SOILDER_QUEUE_VALUE
		// || queueEntity.getQueueType() == QueueType.CURE_QUEUE_VALUE
		// || queueEntity.getQueueType() == QueueType.SOLDIER_ADVANCE_QUEUE_VALUE;
		// if (!canSpeedUp) {
		// player.sendError(protocol.getType(), Status.Error.YQZZ_BAN_OP_VALUE, 0);
		// return true;
		// }
		PlayerQueueModule module = player.getModule(GsConst.ModuleType.QUEUE_MODULE);
		module.onQueueSpeedUp(protocol);

		return true;
	}

	/** 训练士兵 */
	@ProtocolHandler(code = HP.code.ADD_SOLDIER_C_VALUE)
	private boolean onCreateSoldier(HawkProtocol protocol) {
		if (player.getParent().maShangOver()) {
			player.responseSuccess(protocol.getType());
			return true;
		}
		HPAddSoldierReq req = protocol.parseProtocol(HPAddSoldierReq.getDefaultInstance());
		final boolean immediate = req.getIsImmediate();
		if (!immediate) {
			return false;
		}

		PlayerArmyModule armyModule = player.getModule(GsConst.ModuleType.ARMY_MODULE);
		if (armyModule == null) {
			return false; // haha 怎么可能
		}

		boolean success = armyModule.onCreateSoldier(protocol);
		if (success) {
			// int soldierCount = req.getSoldierCount();
			// int armyId = req.getArmyId();
			// RedisProxy.getInstance().jbsIncreaseCreateSoldier(player.getId(),
			// armyId, soldierCount);
		}

		return true;
	}

	/**
	 * 治疗伤兵
	 * 
	 * @return
	 */
	@ProtocolHandler(code = HP.code.CURE_SOLDIER_C_VALUE)
	private boolean onCureSoldier(HawkProtocol protocol) {

		PlayerArmyModule armyModule = player.getModule(GsConst.ModuleType.ARMY_MODULE);
		if (armyModule == null) {
			return false; // haha 怎么可能
		}
		boolean success = armyModule.onCureSoldier(protocol);
		if (success) {

		}

		return true;
	}

	/** 宣战*/
	@ProtocolHandler(code = HP.code2.YQZZ_DECLEAR_WAR_VALUE)
	private boolean declareWar(HawkProtocol protocol) {
		PBYQZZDeclearWar req = protocol.parseProtocol(PBYQZZDeclearWar.getDefaultInstance());
		final int x = req.getX();
		final int y = req.getY();

		boolean guildAuthority = GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.ALLIANCE_MANOR_SET);
		if (!guildAuthority) {
			player.sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return false;
		}

		Optional<IYQZZWorldPoint> buildOp = player.getParent().getWorldPoint(x, y);
		if (!buildOp.isPresent()) {
			return false;
		}
		IYQZZBuilding build = (IYQZZBuilding) buildOp.get();
		if (build.underNationControl(player.getGuildId())) {
			player.sendError(protocol.getType(), Status.YQZZError.YQZZ_SAME_NATION);
			return false;
		}

		YQZZGuildBaseInfo binfo = player.getParent().getCampBase(player.getGuildId());
		if (binfo.declareWarPoint < build.getBuildTypeCfg().getDeclareCost()) {
			player.sendError(protocol.getType(), Status.YQZZError.YQZZ_DECLARE_ITEM_LESS);
			return false;
		}

		for (int linkBuild : build.getCfg().getLinkList()) {
			IYQZZBuilding link = player.getParent().getWorldPointService().getBuildingByCfgId(linkBuild);
			if (link.underNationControl(player.getGuildId())) {
				build.declareWar(player);
				player.responseSuccess(protocol.getType());
				return true;
			}
		}
		player.sendError(protocol.getType(), Status.YQZZError.YQZZ_BUILD_NOT_LINK);

		return true;
	}

	/**查看宣战记录*/
	@ProtocolHandler(code = HP.code2.YQZZ_DECLARE_WAR_USE_C_VALUE)
	private boolean declareWarRecords(HawkProtocol protocol) {
		YQZZDeclareWarUseResp.Builder resp = YQZZDeclareWarUseResp.newBuilder();
		resp.addAllRecords(player.getParent().getCampBase(player.getGuildId()).declareWarRecords);
		protocol.response(HawkProtocol.valueOf(HP.code2.YQZZ_DECLARE_WAR_USE_S_VALUE, resp));
		return true;
	}

	/**
	 * 添加联盟标记
	 * 
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILD_ADD_SIGN_C_VALUE)
	public boolean onAddGuildSign(HawkProtocol protocol) {

		// 禁止输入文本
		if (!GameUtil.checkBanMsg(player)) {
			return false;
		}

		GuildAddSignReq req = protocol.parseProtocol(GuildAddSignReq.getDefaultInstance());

		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.ALLIANCE_SIGN)) {
			player.sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return true;
		}

		GuildSign guildSign = req.getSignInfo();
		int signId = guildSign.getId();
		// 检查id合法性
		AllianceSignCfg cfg = HawkConfigManager.getInstance().getConfigByKey(AllianceSignCfg.class, signId);
		if (cfg == null) {
			player.sendError(protocol.getType(), Status.Error.GUILD_SIGN_CFG_ERROR);
			return true;
		}

		String signInfo = guildSign.getInfo();
		// 标记信息超长
		if (signInfo.length() > GuildConstProperty.getInstance().getAllianceSignExplainLen()) {
			player.sendError(protocol.getType(), Status.Error.GUILD_SIGN_TO_LONG);
			return true;
		}

		JSONObject gameDataJson = new JSONObject();
		gameDataJson.put("alliance_id", player.hasGuild() ? player.getGuildId() : "");
		gameDataJson.put("param_id", String.valueOf(signId));

		String value = JsonFormat.printToString(req.getSignInfo());
		GameTssService.getInstance().wordUicChatFilter(player, signInfo,
				MsgCategory.GUILD_SIGN.getNumber(), GameMsgCategory.YQZZ_ADD_GUILD_SIGN,
				value, gameDataJson, protocol.getType());
		return true;
	}

	/**
	 * 移除联盟标记
	 * 
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILD_REMOVE_SIGN_C_VALUE)
	public boolean onRemoveGuildSign(HawkProtocol protocol) {
		GuildRemoveSignReq req = protocol.parseProtocol(GuildRemoveSignReq.getDefaultInstance());
		player.getParent().getCampBase(player.getGuildId()).getSignMap().remove(req.getSignId());
		player.responseSuccess(HP.code.GUILD_REMOVE_SIGN_C_VALUE);

		Set<IYQZZPlayer> tosend = new HashSet<>(player.getParent().getPlayerList(YQZZState.GAMEING));
		tosend = tosend.stream().filter(p -> Objects.equals(p.getGuildId(), player.getGuildId())).collect(Collectors.toSet());
		// 同步联盟信息
		for (IYQZZPlayer member : tosend) {
			member.getPush().syncGuildInfo();
			// //联盟红点
			// member.getPush().syncRedPoint(RedType.GUILD_FAVOURITE, "");
		}
		return true;
	}

	/**
	 * @param msg
	 */
	@MessageHandler
	private void onCalcDeadArmyMsg(CalcDeadArmy msg) {
		List<ArmyInfo> armyInfos = msg.getArmyDeadList();
		int deadcnt = armyInfos.stream().mapToInt(ArmyInfo::getDeadCount).sum();
		player.setPushGiftDeadCnt(player.getPushGiftDeadCnt() + deadcnt);

		AbstractPushGiftCondition condition = PushGiftManager.getInstance().getCondition(PushGiftConditionEnum.PUSH_GIFT_3700.getType());
		condition.handle(player.getData(), Collections.emptyList(), player.isActiveOnline());
	}

}
