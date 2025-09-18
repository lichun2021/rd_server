package com.hawk.game.module.lianmengfgyl.battleroom.player.module;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.game.config.ItemCfg;
import com.hawk.game.config.ShopCfg;
import com.hawk.game.config.WorldMarchConstProperty;
import com.hawk.game.entity.item.GuildFormationCell;
import com.hawk.game.entity.item.GuildFormationObj;
import com.hawk.game.invoker.MarchSpeedInvoker;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.GuildFormationModule;
import com.hawk.game.module.PlayerMarchModule;
import com.hawk.game.module.lianmengfgyl.battleroom.IFGYLWorldPoint;
import com.hawk.game.module.lianmengfgyl.battleroom.cfg.FGYLBattleCfg;
import com.hawk.game.module.lianmengfgyl.battleroom.invoker.FGYLItemConsumeInvoker;
import com.hawk.game.module.lianmengfgyl.battleroom.player.IFGYLPlayer;
import com.hawk.game.module.lianmengfgyl.battleroom.player.module.guildformation.FGYLGuildFormationObj;
import com.hawk.game.module.lianmengfgyl.battleroom.worldmarch.IFGYLWorldMarch;
import com.hawk.game.module.lianmengfgyl.battleroom.worldmarch.submarch.IFGYLMassJoinMarch;
import com.hawk.game.module.lianmengfgyl.battleroom.worldmarch.submarch.IFGYLMassMarch;
import com.hawk.game.module.lianmengfgyl.battleroom.worldpoint.FGYLMonster;
import com.hawk.game.module.lianmengfgyl.battleroom.worldpoint.IFGYLBuilding;
import com.hawk.game.module.lianmengfgyl.march.cfg.FGYLConstCfg;
import com.hawk.game.module.lianmenxhjz.battleroom.worldmarch.IXHJZWorldMarch;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Armour.ArmourSuitType;
import com.hawk.game.protocol.Army.ArmyChangeCause;
import com.hawk.game.protocol.Army.ArmySoldierPB;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MassFormation.FormationOperType;
import com.hawk.game.protocol.MassFormation.MassFormationIndex;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.World.AppointedCaptain;
import com.hawk.game.protocol.World.InviteMassReq;
import com.hawk.game.protocol.World.MarchArmyDetailInfo;
import com.hawk.game.protocol.World.MarchArmyDetailReq;
import com.hawk.game.protocol.World.MarchArmyDetailResp;
import com.hawk.game.protocol.World.UseMarchEmoticonReq;
import com.hawk.game.protocol.World.WorldMarchDeletePush;
import com.hawk.game.protocol.World.WorldMarchRelation;
import com.hawk.game.protocol.World.WorldMarchReq;
import com.hawk.game.protocol.World.WorldMarchResp;
import com.hawk.game.protocol.World.WorldMarchServerCallBackReq;
import com.hawk.game.protocol.World.WorldMarchServerCallBackResp;
import com.hawk.game.protocol.World.WorldMarchSpeedUpReq;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldMassDissolveReq;
import com.hawk.game.protocol.World.WorldMassRepatriateReq;
import com.hawk.game.protocol.World.WorldMassRepatriateResp;
import com.hawk.game.protocol.World.WorldPointMarchCallBackReq;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.ArmyService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.ModuleType;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.sdk.msdk.entity.PayItemInfo;

public class FGYLMarchModule extends PlayerModule {
	private IFGYLPlayer player;
	public FGYLMarchModule(IFGYLPlayer player) {
		super(player);
		this.player = player;
	}
	
	/**
	 * 使用行军表情
	 */
	@ProtocolHandler(code = HP.code.MARCH_EMOTICON_USE_C_VALUE)
	private boolean onUseMarchEmoticon(HawkProtocol protocol) {
		UseMarchEmoticonReq req = protocol.parseProtocol(UseMarchEmoticonReq.getDefaultInstance());
		if (req.hasMarchId() && !HawkOSOperator.isEmptyString(req.getMarchId())) {
			marchEmoticonUse( protocol, req.getEmoticonId(), req.getMarchId());
		} else {
			cityEmoticonUse(protocol, req.getEmoticonId(), req.getMarchId());
		}
		
		return true;
	}
	
	/**
	 * 对城点使用行军表情判断
	 */
	public boolean cityEmoticonUse(HawkProtocol protocol, int emoticonId, String marchId) {
		if (!useMarchEmoticon(protocol, emoticonId, marchId)) {
			return false;
		}
		IFGYLPlayer worldPoint = player;
		worldPoint.setEmoticon(emoticonId);
		worldPoint.setEmoticonUseTime(HawkTime.getMillisecond());
		worldPoint.worldPointUpdate();
		worldPoint.responseSuccess(protocol.getType());
		return true;
	}
	
	/**
	 * 攻击怪物
	 */
	@ProtocolHandler(code = HP.code.WORLD_FIGHTMONSTER_C_VALUE)
	private boolean onWorldAttackMonsterStart(HawkProtocol protocol) {
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		int posX = req.getPosX();
		int posY = req.getPosY();

		// 目标点
		IFGYLWorldPoint targetPoint = player.getParent().getWorldPoint(posX, posY).orElse(null);

		// 目标点为空
		if (targetPoint == null) {
			player.sendError(protocol.getType(), Status.Error.POINT_NOT_EXIST_OR_TYPE_ERROR);
			return false;
		}

		// 目标点不是机甲
		if (targetPoint.getPointType() != WorldPointType.MONSTER) {
			player.sendError(protocol.getType(), Status.Error.POINT_NOT_EXIST_OR_TYPE_ERROR);
			return false;
		}

		// 检测出征的军队信息
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>(); // 出征的士兵列表
		if (!checkMarchReq(player, req, protocol.getType(), armyList, targetPoint, true)) {
			return false;
		}

		// 扣兵
		if (!checkArmyAndMarch(protocol.getType(), player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			return false;
		}

		// 开启行军,目标放怪物ID
		FGYLMonster monster = (FGYLMonster) targetPoint;
		String targetId = monster.getCfg().getMonsterId() + "";
		player.getParent().startMarch(player, player, targetPoint, WorldMarchType.ATTACK_MONSTER, targetId, 0, new EffectParams(req, armyList));

		// 返回
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		protocol.response(HawkProtocol.valueOf(HP.code.WORLD_FIGHTMONSTER_S_VALUE, builder));
		return true;
	}
	
	/**
	 * 对行军使用行军表情判断
	 * 
	 */
	public boolean marchEmoticonUse(HawkProtocol protocol, int emoticonId, String marchId) {
		IFGYLWorldMarch march = player.getParent().getMarch(marchId);
		if (march == null) {
			player.sendError(protocol.getType(), Status.Error.WORLD_MARCH_NOT_EXISIT);
			return false;
		}
		
		// 不是自己的行军
		if (!player.getId().equals(march.getPlayerId())) {
			player.sendError(protocol.getType(), Status.Error.WORLD_MARCH_NOT_SELF);
			return false;
		}
		
		// 是集结行军但不是队长
		if (march.isMassJoinMarch()) {
			IWorldMarch massMarch = player.getParent().getMarch(march.getMarchEntity().getTargetId()); // 获取集结的队长行军
			if (massMarch != null && massMarch.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE) {
				player.sendError(protocol.getType(), Status.Error.WORLD_MARCH_NOT_LEADER);
				return false;
			}
		}
		
		// 正处在集结等待或士兵援助中的行军, 联盟堡垒创建、修复或驻防中的行军
		int marchStatus = march.getMarchStatus(); 
		if (marchStatus == WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST_VALUE || marchStatus == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE
				|| marchStatus == WorldMarchStatus.MARCH_STATUS_MANOR_BUILD_VALUE || marchStatus == WorldMarchStatus.MARCH_STATUS_MANOR_REPAIR_VALUE
				|| marchStatus == WorldMarchStatus.MARCH_STATUS_MANOR_GARRASION_VALUE) {
			player.sendError(protocol.getType(), Status.Error.MARCH_STATUS_NOT_SUPPORT_EMOTICON);
			return false;
		}
		
		// 联盟资源矿采集时不可使用行军表情
		if (march.getMarchType() == WorldMarchType.MANOR_COLLECT && marchStatus == WorldMarchStatus.MARCH_STATUS_MARCH_COLLECT_VALUE) {
			player.sendError(protocol.getType(), Status.Error.MARCH_STATUS_NOT_SUPPORT_EMOTICON);
			return false;
		}
		
		if (!useMarchEmoticon(protocol, emoticonId, marchId)) {
			return false;
		}
		
		march.getMarchEntity().setEmoticon(emoticonId);
		march.getMarchEntity().setEmoticonUseTime(HawkTime.getMillisecond());
		
		march.updateMarch();
		
		if (march.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MARCH_VALUE 
				&& march.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
			IFGYLWorldPoint worldPoint = player.getParent().getWorldPoint(march.getTerminalId()).get();
			if (worldPoint instanceof IFGYLBuilding && ((IFGYLBuilding) worldPoint).getLeaderMarch() != null) {
				WorldMarch firstMarch = ((IFGYLBuilding) worldPoint).getLeaderMarch().getMarchEntity();
				firstMarch.setEmoticon(emoticonId);
				firstMarch.setEmoticonUseTime(HawkTime.getMillisecond());
			}
			worldPoint.worldPointUpdate();
		}
		
		player.responseSuccess(protocol.getType());
		return true;
	}
	
	public boolean useMarchEmoticon(HawkProtocol protocol, int emoticonId, String marchId) {
		PlayerMarchModule module = player.getModule(GsConst.ModuleType.WORLD_MARCH_MODULE);
		return module.useMarchEmoticon(protocol, emoticonId, marchId);
	}

	/**
	 * 邀请集结
	 */
	@ProtocolHandler(code = HP.code.INVITE_MASS_REQ_VALUE)
	private boolean onInviteMass(HawkProtocol protocol) {
		InviteMassReq req = protocol.parseProtocol(InviteMassReq.getDefaultInstance());

		IFGYLWorldMarch march = player.getParent().getMarch(req.getMarchId());
		if (march == null) {
			player.sendError(protocol.getType(), Status.Error.MASS_ERR_MARCH_NOT_EXIST);
			return false;
		}

		if (!march.isMassMarch()) {
			player.sendError(protocol.getType(), Status.Error.MASS_ERR_MARCH_NOT_EXIST);
			return false;
		}

		if (HawkTime.getMillisecond() - player.getData().getLastInviteMassTime() < WorldMarchConstProperty.getInstance().getInviteMassCD()) {
			player.sendError(protocol.getType(), Status.Error.INVITE_MASS_CD);
			return false;
		}

		Optional<IFGYLWorldPoint> worldPoint = player.getParent().getWorldPoint(march.getMarchEntity().getTerminalId());
		if (worldPoint.isPresent()) {
			FGYLGuildFormationObj formationObj = player.getParent().getGuildFormation(player.getGuildId());
			GuildFormationCell formation = formationObj.getGuildFormation(req.getMarchId());
			if (formation == null) {
				sendNotice(worldPoint.get(), march, 0, "");
			} else {
				sendNotice(worldPoint.get(), march, formation.getIndex().getNumber(), formation.getName());
			}
		}

		player.getData().setLastInviteMassTime(HawkTime.getMillisecond());
		player.getPush().syncPlayerInfo();
		player.responseSuccess(protocol.getType());

		return true;
	}

	/**
	 * 发送公告
	 */
	public void sendNotice(IFGYLWorldPoint point, IFGYLWorldMarch march, int formationIndex, String formationName) {
		IFGYLPlayer player = march.getParent();
		 if (point instanceof IFGYLBuilding) {
			ChatParames parames = ChatParames.newBuilder().setPlayer(player).setChatType(ChatType.CHAT_FUBEN_TEAM).setKey(NoticeCfgId.FGYL_MASS_START_BUILD)
					.addParms(march.getTerminalX())
					.addParms(march.getTerminalY())
					.addParms(march.getMarchId()).addParms(player.getX()).addParms(player.getY()).addParms(formationIndex)
					.addParms(formationName).build();
			player.getParent().addWorldBroadcastMsg(parames);
		}
	}


//	/**
//	 * 遣返联盟领地行军
//	 */
//	@ProtocolHandler(code = HP.code.REPATRIATE_MANOR_MARCH_C_VALUE)
//	private boolean onRepatriateManormarch(HawkProtocol protocol) {
//		if (!player.hasGuild()) {
//			return false;
//		}
//		RepatriateManorMarch req = protocol.parseProtocol(RepatriateManorMarch.getDefaultInstance());
//		// 世界点
//		IFGYLWorldPoint worldPoint = player.getParent().getWorldPoint(req.getX(), req.getY()).orElse(null);
//		if (worldPoint == null) {
//			player.sendError(protocol.getType(), Status.Error.WORLD_POINT_NOT_EXIST);
//			return false;
//		}
//
//		// 战区
//		if (worldPoint instanceof IFGYLBuilding) {
//			IFGYLBuilding building = (IFGYLBuilding) worldPoint;
//			building.repatriateMarch(player, req.getPlayerId());
//			building.syncQuarterInfo(player);
//		}
//
//		player.responseSuccess(HP.code.REPATRIATE_MANOR_MARCH_C_VALUE);
//		return true;
//	}

	/**
	 * 任命队长
	 */
	@ProtocolHandler(code = HP.code.APPOINTED_CAPTAIN_VALUE)
	private boolean onChangeQuarterLeader(HawkProtocol protocol) {
		if (!player.hasGuild()) {
			return false;
		}
		AppointedCaptain req = protocol.parseProtocol(AppointedCaptain.getDefaultInstance());
		// 世界点
		IFGYLWorldPoint worldPoint = player.getParent().getWorldPoint(req.getX(), req.getY()).orElse(null);
		if (worldPoint == null) {
			player.sendError(protocol.getType(), Status.Error.WORLD_POINT_NOT_EXIST);
			return false;
		}

		// 战区
		if (worldPoint instanceof IFGYLBuilding) {
			IFGYLBuilding building = (IFGYLBuilding) worldPoint;
			building.cheangeQuarterLeader(player, req.getPlayerId());
			building.syncQuarterInfo(player);
		}

		player.responseSuccess(protocol.getType());
		return true;
	}

	/**
	 * 集结队长解散
	 */
	@ProtocolHandler(code = HP.code.WORLD_MASS_DISSOLVE_C_VALUE)
	protected boolean onWorldMassDissolve(HawkProtocol protocol) {
		WorldMassDissolveReq req = protocol.parseProtocol(WorldMassDissolveReq.getDefaultInstance());
		// check
		IFGYLWorldMarch worldMarch = player.getParent().getMarch(req.getMarchId());
		if (worldMarch == null) {
			player.sendError(protocol.getType(), Status.Error.WORLD_MARCH_NOT_EXISIT);
			return false;
		}
		WorldMarch march = worldMarch.getMarchEntity();
		// 集结目标点
		// int terminalX = march.getTerminalX();
		// int terminalY = march.getTerminalY();

		// 非集结行军不能解散
		if (!worldMarch.isMassMarch()) {
			player.sendError(protocol.getType(), Status.Error.WORLD_MARCH_NOT_MASS);
			return false;
		}

		// 不是队长不能解散
		if (!HawkOSOperator.isEmptyString(march.getPlayerId()) && !march.getPlayerId().equals(player.getId())) {
			player.sendError(protocol.getType(), Status.Error.WORLD_MARCH_NOT_LEADER);
			return false;
		}

		// 队长的行军非等待状态不能解散
		if (march.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
			player.sendError(protocol.getType(), Status.Error.WORLD_MARCH_ALREADY_MARCHING);
			return false;
		}

		for (IFGYLWorldMarch jm : worldMarch.getMassJoinMarchs(true)) {
			jm.onMarchCallback();
			// // 发送邮件---集结失败：发起者解散
			// GuildMailService.getInstance().sendMail(
			// MailParames.newBuilder().setPlayerId(jm.getPlayerId()).setMailId(MailId.MASS_FAILED_ORGANIZER_DISSOLVE).addContents(terminalX, terminalY).setIcon(player.getGuildFlag()).build());
		}

		worldMarch.onMarchBack();
		// 回复
		WorldMassRepatriateResp.Builder resp = WorldMassRepatriateResp.newBuilder();
		resp.setResult(true);
		protocol.response(HawkProtocol.valueOf(HP.code.WORLD_MASS_DISSOLVE_S_VALUE, resp));
		return true;
	}

	/**
	 * 集结队长遣返
	 */
	@ProtocolHandler(code = HP.code.WORLD_MASS_REPATRIATE_C_VALUE)
	protected boolean onWorldMassRepatriate(HawkProtocol protocol) {
		WorldMassRepatriateReq req = protocol.parseProtocol(WorldMassRepatriateReq.getDefaultInstance());

		IFGYLWorldMarch worldMarch = player.getParent().getMarch(req.getMarchId());
		if (worldMarch == null) {
			player.sendError(protocol.getType(), Status.Error.WORLD_MARCH_NOT_EXISIT);
			return false;
		}

		WorldMarch march = worldMarch.getMarchEntity();

		// 援助部队的遣返
		if (march.getMarchType() == WorldMarchType.ASSISTANCE_VALUE) {
			worldMarch.onMarchCallback();
			// 回复
			WorldMassRepatriateResp.Builder resp = WorldMassRepatriateResp.newBuilder();
			resp.setResult(true);
			protocol.response(HawkProtocol.valueOf(HP.code.WORLD_MASS_REPATRIATE_S_VALUE, resp));

			return true;
		}

		if (!worldMarch.isMassJoinMarch()) {
			player.sendError(protocol.getType(), Status.Error.WORLD_MARCH_NOT_TEAM_MEMBER);
			return false;
		}

		IFGYLWorldMarch leaderMarch = player.getParent().getMarch(march.getTargetId());
		if (leaderMarch == null) {
			player.sendError(protocol.getType(), Status.Error.WORLD_MARCH_NOT_LEADER);
			return false;
		}

		if (leaderMarch.getMarchEntity().getMarchStatus() != WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
			player.sendError(protocol.getType(), Status.Error.WORLD_MARCH_ALREADY_MARCHING);
			return false;
		}

		if (!leaderMarch.getPlayerId().equals(player.getId())) {
			player.sendError(protocol.getType(), Status.Error.WORLD_MARCH_NOT_LEADER);
			return false;
		}

		if (worldMarch.isReturnBackMarch()) {
			return false;
		}

		worldMarch.onMarchCallback();

		// 回复
		WorldMassRepatriateResp.Builder resp = WorldMassRepatriateResp.newBuilder();
		resp.setResult(true);
		protocol.response(HawkProtocol.valueOf(HP.code.WORLD_MASS_REPATRIATE_S_VALUE, resp));

		return true;
	}

	/**
	 * FGYL_SINGLE_MARCH = 487; // 副本内特有建筑单人行军
	 */
	@ProtocolHandler(code = HP.code2.FGYL_SINGLE_MARCH_VALUE)
	private boolean onFGYL_SINGLE_MARCHStart(HawkProtocol protocol) {
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		IFGYLWorldPoint point = player.getParent().getWorldPoint(req.getPosX(), req.getPosY()).orElse(null);
		// 路点为空
		if (point == null) {
			player.sendError(protocol.getType(), Status.Error.WORLD_POINT_NOT_EXIST);
			return false;
		}
//		if (point.getProtectedEndTime() > player.getParent().getCurTimeMil()) {
//			player.sendError(protocol.getType(), Status.Error.FGYL_BUILD_PROCTED);
//			return false;
//		}
		if (HawkTime.getMillisecond() < point.getProtectedEndTime()) {
			return false;
		}
		// 联盟检查
		if (point.needJoinGuild() && !player.hasGuild()) {
			player.sendError(protocol.getType(), Status.Error.GUILD_NO_JOIN);
			return false;
		}
		WorldMarchType marchType = WorldMarchType.FGYL_BUILDING_SINGLE;

		// 带兵出征通用检查
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>(); // 出征的士兵列表 
		if (!checkMarchReq(player, req, protocol.getType(), armyList, point, true)) {
			return false;
		}

		// 扣兵
		if (!checkArmyAndMarch(protocol.getType(), player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			return false;
		}

		player.getParent().startMarch(player, player, point, marchType, "", 0, new EffectParams(req, armyList));
		
		// 回复协议
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		protocol.response(HawkProtocol.valueOf(HP.code2.FGYL_SINGLE_MARCH_S, builder));
		return true;

	}


	/** 行军详情(暂时只支持集结行军，传集结行军marchId) */
	@ProtocolHandler(code = HP.code.MARCH_ARMY_DETAIL_REQ_VALUE)
	private boolean onMarchArmyDetailInfo(HawkProtocol protocol) {
		MarchArmyDetailReq req = protocol.parseProtocol(MarchArmyDetailReq.getDefaultInstance());
		String marchId = req.getMarchId();

		// 参数错误
		if (HawkOSOperator.isEmptyString(marchId)) {
			player.sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			return false;
		}

		// 没有这条行军
		IFGYLWorldMarch march = player.getParent().getMarch(marchId);
		if (march == null) {
			player.sendError(protocol.getType(), Status.Error.WORLD_MARCH_NOT_EXISIT);
			return false;
		}

		// 不是集结行军(暂时只支持集结行军，传集结行军marchId)
		if (!march.isMassMarch()) {
			player.sendError(protocol.getType(), Status.Error.WORLD_MARCH_NOT_MASS);
			return false;
		}

		// 不是行军中
		if (!march.isMarchState()) {
			player.sendError(protocol.getType(), Status.Error.NOT_MARCH_STATE);
			return false;
		}

		Set<IFGYLWorldMarch> allMarch = new HashSet<>();
		allMarch.add(march);
		allMarch.addAll(march.getMassJoinMarchs(true));

		boolean canReq = false;

		// 是否有自己的行军
		for (IFGYLWorldMarch thisMarch : allMarch) {
			if (!player.getId().equals(thisMarch.getPlayerId())) {
				continue;
			}
			canReq = true;
			break;
		}

		// 没有自己的行军，不能查看
		if (!canReq) {
			player.sendError(protocol.getType(), Status.Error.HAVE_NO_OWEN_MARCH);
			return false;
		}

		MarchArmyDetailResp.Builder builder = MarchArmyDetailResp.newBuilder();
		for (IFGYLWorldMarch thisMarch : allMarch) {
			MarchArmyDetailInfo.Builder info = MarchArmyDetailInfo.newBuilder();
			IFGYLPlayer mp = thisMarch.getParent();
			if (mp == null || mp.getName() == null) {
				continue;
			}

			info.setPlayerName(mp.getName());
			for (PlayerHero hero : thisMarch.getHeros()) {
				info.addHeros(hero.toArmyHeroPb());
				info.addHeroList(hero.toPBobj());
			}

			for (ArmyInfo army : thisMarch.getArmys()) {
				info.addArmys(army.toArmySoldierPB(thisMarch.getParent()));
			}
			Optional<SuperSoldier> ssoldierOp = mp.getSuperSoldierByCfgId(thisMarch.getSuperSoldierId());
			if (ssoldierOp.isPresent()) {
				info.setSsoldier(ssoldierOp.get().toPBobj());
			}
			ArmourSuitType armourSuit = ArmourSuitType.valueOf(thisMarch.getMarchEntity().getArmourSuit());
			if (armourSuit != null) {
				info.setArmourSuit(armourSuit);
			}
			builder.addInfo(info);
		}
		protocol.response(HawkProtocol.valueOf(HP.code.MARCH_ARMY_DETAIL_RESP, builder));
		return true;
	}

	/**
	 * 行军加速
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.WORLD_MARCH_SPEEDUP_C_VALUE)
	private boolean onMarchSpeedUp(HawkProtocol protocol) {
		// check
		WorldMarchSpeedUpReq req = protocol.parseProtocol(WorldMarchSpeedUpReq.getDefaultInstance());
		if (!req.hasMarchId() || !req.hasItemId()) {
			player.sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			return false;
		}

//		// 道具id非法
//		if (req.getItemId() != Const.ItemId.ITEM_WORLD_MARCH_SPEEDUPH_VALUE && req.getItemId() != Const.ItemId.ITEM_WORLD_MARCH_SPEEDUPL_VALUE) {
//			player.sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
//			return false;
//		}

		// 行军信息
		IFGYLWorldMarch march = player.getParent().getMarch(req.getMarchId());
		if (march == null) {
			player.sendError(protocol.getType(), Status.Error.WORLD_MARCH_NOT_EXISIT);
			return false;
		}

		// 加入集结出征，给队长行军加速
		if (march.isMassJoinMarch() && march.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_JOIN_MARCH_VALUE) {
			march = player.getParent().getMarch(march.getMarchEntity().getTargetId());
		}

		// 不在行进或者回程途中，加速无效
		if (!(march.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE
				|| march.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE)) {
			player.sendError(protocol.getType(), Status.Error.WORLD_MARCH_NOT_EXISIT);
			return false;
		}

		// 行军状态发生了变化
		if (req.hasStatus() && req.getStatus() != WorldMarchStatus.valueOf(march.getMarchEntity().getMarchStatus())) {
			player.sendError(protocol.getType(), Status.Error.WORLD_MARCH_STATUS_CHANGED);
			return false;
		}

		// 召回消耗道具
		ConsumeItems consumeItems = ConsumeItems.valueOf();

		if (player.getData().getItemNumByItemId(req.getItemId()) > 0) {
			consumeItems.addItemConsume(req.getItemId(), 1);
		} else {
			ShopCfg shopCfg = ShopCfg.getShopCfgByItemId(req.getItemId());
			if (shopCfg == null) {
				player.sendError(protocol.getType(), Status.Error.ITEM_NOT_FOUND);
				return false;
			}
			consumeItems.addConsumeInfo(shopCfg.getPriceItemInfo(), false);
		}

		// 判断消耗是否满足
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			return false;
		}
		
		// caculate
		ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, req.getItemId());
		int timeReducePercent = itemCfg.getNum();
		int speedUpTimes = itemCfg.getMarchSpeedMultiple();
		// 加速
		if (march.speedUp(timeReducePercent, speedUpTimes)) {
			// 消耗掉
			player.dealMsg(MsgId.WORLD_MARCH, new FGYLItemConsumeInvoker(player, consumeItems, Action.WORLD_MARCH_SPEEDUP));
			int val = player.getEffect().getEffVal(EffType.MARCH_SPEED_ITEM_BACK_628);
			if (val > 0) {
				player.dealMsg(MsgId.WORLD_MARCH_SPEED, new MarchSpeedInvoker(player, req.getItemId()));
			}
		}
		return true;
	}

	/**
	 * 行军召回
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.WORLD_SERVER_CALLBACK_C_VALUE)
	private boolean onMarchCallBack(HawkProtocol protocol) {
		// check param
		WorldMarchServerCallBackReq req = protocol.parseProtocol(WorldMarchServerCallBackReq.getDefaultInstance());
		if (!req.hasMarchId()) {
			player.sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			return false;
		}
		// 检测行军是否存在
		IFGYLWorldMarch march = player.getParent().getPlayerMarch(player.getId(), req.getMarchId());
		if (march == null) {
			WorldMarchDeletePush.Builder builder = WorldMarchDeletePush.newBuilder();
			builder.setMarchId(req.getMarchId());
			builder.setRelation(WorldMarchRelation.SELF);
			player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MARCH_DELETE_PUSH_VALUE, builder));
			return false;
		}
		// 不是自己的行军不能召回
		if (!player.getId().equals(march.getPlayerId())) {
			return false;
		}

		// 返回途中不能召回
		if (march.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
			player.sendError(protocol.getType(), Status.Error.WORLD_MARCH_RETURNING);
			return false;
		}

		// 集结类型的行军不能召回
		if (march.isMassMarch()) {
			player.sendError(protocol.getType(), Status.Error.WORLD_MARCH_MASS_CANNOT_CALLBACK);
			return false;
		}

		// 非行军中的行军召回时不消耗道具
		if (march.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE) {
			// 召回消耗道具
			ConsumeItems consumeItems = ConsumeItems.valueOf();
			if (player.getData().getItemNumByItemId(Const.ItemId.ITEM_WORLD_MARCH_CALLBACK_VALUE) > 0) {
				consumeItems.addItemConsume(Const.ItemId.ITEM_WORLD_MARCH_CALLBACK_VALUE, 1);
			} else {
				ShopCfg shopCfg = ShopCfg.getShopCfgByItemId(Const.ItemId.ITEM_WORLD_MARCH_CALLBACK_VALUE);
				consumeItems.addConsumeInfo(shopCfg.getPriceItemInfo(), false);
			}

			// 消耗掉
			if (!consumeItems.checkConsume(player, protocol.getType())) {
				return false;
			}
			
			if (consumeItems.getBuilder().hasAttrInfo() && consumeItems.getBuilder().getAttrInfo().getDiamond() > 0) {
				ShopCfg shopCfg = ShopCfg.getShopCfgByItemId(Const.ItemId.ITEM_WORLD_MARCH_CALLBACK_VALUE);
				ItemInfo priceItem = shopCfg.getPriceItemInfo();
				consumeItems.addPayItemInfo(new PayItemInfo(String.valueOf(Const.ItemId.ITEM_WORLD_MARCH_CALLBACK_VALUE), (int)priceItem.getCount(), 1));
			}
			consumeItems.consumeAndPush(player, Action.WORLD_MARCH_CALLBACK);
		}

		// 召回
		march.onMarchCallback();

		// 回复协议
		WorldMarchServerCallBackResp.Builder builder = WorldMarchServerCallBackResp.newBuilder();
		builder.setResult(true);
		protocol.response(HawkProtocol.valueOf(HP.code.WORLD_SERVER_CALLBACK_S, builder));

		return true;

	}

	/** 行军召回,一定是已经在停留在目标点的行军 */
	@ProtocolHandler(code = HP.code.WORLD_POINT_MARCH_CALLBACK_C_VALUE)
	private boolean onPointMarchCallBack(HawkProtocol protocol) {
		// check param
		WorldPointMarchCallBackReq req = protocol.parseProtocol(WorldPointMarchCallBackReq.getDefaultInstance());
//		IFGYLWorldPoint worldPoint = player.getParent().getWorldPoint(req.getX(), req.getY()).orElse(null);
//		if (worldPoint == null) {
//			player.sendError(protocol.getType(), Status.Error.WORLD_POINT_NOT_EXIST); // 提示目标点没有援助行军
//			return false;
//		}
//		if (worldPoint instanceof IFGYLBuilding) {
//			// 获取是否有驻军信息
//			IFGYLWorldMarch march = player.getParent().getPointMarches(GameUtil.combineXAndY(req.getX(), req.getY()), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED).stream()
//					.filter(ma -> Objects.equals(ma.getPlayerId(), player.getId()))
//					.filter(ma -> Objects.equals(ma.getMarchId(), req.getMarchId()))
//					.findFirst().orElse(null);
//			if (march == null || march instanceof IFGYLMassJoinMarch) {
////				player.sendError(protocol.getType(), Status.Error.POINT_NO_MARCH_CALL_BACK);// 提示目标点没有援助行军
//				return false;
//			}
//			for (IFGYLWorldMarch jm : march.getMassJoinMarchs(true)) {
//				jm.onMarchCallback();
//			}
//			// 召回
//			march.onMarchCallback();
//		}
		
		IFGYLWorldMarch march = player.getParent().getPlayerMarch(player.getId(), req.getMarchId());
		if (march == null) {
			return false;
		}
		if (march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE) {
			return false;
		}
		if (march.getTerminalX() != req.getX() || march.getTerminalY() != req.getY()) {
			return false;
		}
		// 召回
		march.onMarchCallback();

		player.responseSuccess(protocol.getType());
		return true;
	}

	/**
	 * 是否有空闲出征队列
	 * 
	 * @return
	 */
	public boolean isHasFreeMarch(IFGYLPlayer player) {
		// 机器人不受行军数量限制
		if (player.isRobot()) {
			return true;
		}
		List<IFGYLWorldMarch> playerMarches = player.getParent().getPlayerMarches(player.getId());
		// 行军个数
		int marchCount = 0;
		for (IFGYLWorldMarch march : playerMarches) {
			if (!march.isExtraSpyMarch()) {
				marchCount++;
			}
		}
		if (marchCount >= player.getMaxMarchNum()) {
			return false;
		}
		return true;
	}

	/**
	 * 行军出发前通用检查：目标点类型、队列数量、各士兵数量
	 * 
	 * @param req
	 * @param hpCode
	 * @param armyList
	 * @param worldPoint
	 * @return
	 */
	public boolean checkMarchReq(IFGYLPlayer player, WorldMarchReq req, int hpCode, List<ArmyInfo> armyList, IFGYLWorldPoint worldPoint, boolean checkFreeMarch) {
		
		if(player.isAnchor()){
			return false;
		}
		if(player.getParent().maShangOver()){
			return false;
		}
		
		if (player.getParent().getStartTime() > HawkTime.getMillisecond()) {
			player.sendError(hpCode, Status.Error.TBLY_PRETIME_CANNOT_MARCH);
			return false;
		}

		boolean hasFreeMarch = isHasFreeMarch(player);
		if (hpCode == HP.code.WORLD_SPY_C_VALUE) {
			if (player.getParent().isExtraSpyMarchOpen(player) && !player.getParent().isExtraSypMarchOccupied(player)) {
				hasFreeMarch = true;
			}
		}
		
		if(hpCode == HP.code.WORLD_FIGHTMONSTER_C_VALUE){
			FGYLConstCfg ccfg = HawkConfigManager.getInstance().getKVInstance(FGYLConstCfg.class);
			if (player.getKillMonster() >= ccfg.getMonsterAttackMax()) {
				player.sendError(hpCode, Status.FGYLError.FGYL_MONSTER_MAX);
				return false;
			}
		}

		if (checkFreeMarch && !hasFreeMarch) {
			player.sendError(hpCode, Status.Error.WORLD_MARCH_MAX_LIMIT);
			return false;
		}
		if (hpCode == HP.code.WORLD_SPY_C_VALUE) {
			return true;
		}

		List<ArmySoldierPB> reqArmyList = req.getArmyInfoList();
		if ((req.getAssistantList() == null || req.getAssistantList().isEmpty()) && (reqArmyList == null || reqArmyList.size() == 0)) {
			player.sendError(hpCode, Status.Error.WORLD_MARCH_NO_ARMY);
			return false;
		}

		for (ArmySoldierPB marchArmy : reqArmyList) {
			if (marchArmy.getCount() <= 0) { // 防止刷兵
				player.sendError(hpCode, Status.Error.WORLD_MARCH_NO_ARMY);
				return false;
			}
		}
		if (!checkMarchDressReq(player, req.getMarchDressList())) {
			return false;
		}
		// 检查英雄出征
		if (!ArmyService.getInstance().heroCanMarch(player, req.getHeroIdList())) {
			return false;
		}	

		if (!ArmyService.getInstance().superSoldierCsnMarch(player, req.getSuperSoldierId())) {
			return false;
		}

		// 士兵总数
		int totalCnt = 0;
		for (ArmySoldierPB marchArmy : reqArmyList) {
			totalCnt += marchArmy.getCount();
			armyList.add(new ArmyInfo(marchArmy.getArmyId(), marchArmy.getCount()));
		}

		// 使用技能列表
		List<Integer> useSkills = req.getUseSkillIdList();

		// 不使用技能10104，走此兵力判断逻辑
		if (useSkills == null || !useSkills.contains(GsConst.SKILL_10104)) {
			int maxMarchSoldierNum = player.getMaxMarchSoldierNum(new EffectParams(req, new ArrayList<>()));
			if (totalCnt > maxMarchSoldierNum) {
				player.sendError(hpCode, Status.Error.WORLD_MARCH_ARMY_TOTALCOUNT);
				return false;
			}
		}
		return true;
	}

	/** 发起集结 */
	@ProtocolHandler(code = HP.code.WORLD_MASS_C_VALUE)
	private boolean onWorldMassStart(HawkProtocol protocol) {
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		int waitTime = req.getMassTime();
		// int marchType = req.getType().getNumber();
		// // 不是集结类型的行军
		// if (!WorldUtil.isMassMarch(marchType)) {
		// player.sendError(protocol.getType(),
		// Status.Error.WORLD_MARCH_REQ_TYPE_ERROR);
		// return false;
		// }

		IFGYLWorldPoint point = player.getParent().getWorldPoint(req.getPosX(), req.getPosY()).orElse(null);
		if (point == null) {
			player.sendError(protocol.getType(), Status.Error.WORLD_POINT_TYPE_ERROR);
			return false;
		}
		if (point.getProtectedEndTime() > player.getParent().getCurTimeMil()) {
			return false;
		}
		if (HawkTime.getMillisecond() < point.getProtectedEndTime()) {
			return false;
		}

		// 带兵出征通用检查
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>();
		if (!checkMarchReq(player, req, protocol.getType(), armyList, point, true)) {
			return false;
		}

		String targetId = "";
		WorldMarchType marchType = WorldMarchType.FGYL_BUILDING_MASS;

		// 扣兵
		if (!checkArmyAndMarch(protocol.getType(), player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			return false;
		}

		IFGYLWorldMarch march = player.getParent().startMarch(player, player, point, marchType, targetId, waitTime, new EffectParams(req, armyList));

		// 回复协议
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		protocol.response(HawkProtocol.valueOf(HP.code.WORLD_MASS_S_VALUE, builder));

		try {
			FGYLGuildFormationObj formationObj = player.getParent().getGuildFormation(player.getGuildId());
			// 行军id添加到集结编队信息中
			int formationIndex = req.getGuildFormation();
			String formationName = "";
			GuildFormationCell formation = formationObj.getFormation(MassFormationIndex.valueOf(formationIndex));
			if (formation != null) {
				formationObj.addFormationMarch(formationIndex, march.getMarchId());
				formationName = formation.getName();
				formationObj.notifyUpdate();
			}
			sendNotice(point, march, formationIndex, formationName);
			GuildFormationModule module = player.getModule(GsConst.ModuleType.GUILD_FORMATION);
			module.tlog(player, formation, FormationOperType.FORMATION_MASS, march.getMarchId());
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return true;
	}

	/** 加入集结 */
	@ProtocolHandler(code = HP.code.WORLD_MASS_JOIN_C_VALUE)
	private boolean onWorldMassJoinStart(HawkProtocol protocol) {
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		String massMarchId = req.getMarchId();
		IFGYLWorldMarch worldMarch = player.getParent().getMarch(massMarchId);
		// 取队长行军
		if (worldMarch == null) {
			player.sendError(protocol.getType(), Status.Error.MASS_ERR_MARCH_NOT_EXIST);
			return false;
		}
		if (!worldMarch.isMassMarch()) {
			return false;
		}
		int marchType = req.getType().getNumber();
		// 不能加入自己的集结
		IFGYLPlayer leader = worldMarch.getParent();
		String leaderId = leader.getId();
		if (player.getId().equals(leaderId)) {
			player.sendError(protocol.getType(), Status.Error.MASS_ERR_CANOT_JOIN_SELF);
			return false;
		}

		IFGYLMassMarch massMarch = (IFGYLMassMarch) worldMarch;
		// 集结行军类型和参与集结行军类型不一致
		int joinMarchType = massMarch.getJoinMassType().getNumber();
		if (marchType != joinMarchType) {
			player.sendError(protocol.getType(), Status.Error.MASS_JOIN_TYPE_ERROR);
			return false;
		}

		// 带兵出征通用检查
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>(); // 出征的士兵列表
		if (!checkMarchReq(player, req, protocol.getType(), armyList, leader, true)) {
			return false;
		}
		if (req.getFgylSkill() > 0) { // 使用技能
			FGYLBattleCfg cfg = HawkConfigManager.getInstance().getKVInstance(FGYLBattleCfg.class);
			if (player.getSkillOrder() < cfg.getUseHonor()) {
				player.sendError(protocol.getType(), Status.FGYLError.FGYL_SKILLORDER_LESS);
				return false;
			}
		}

		// 已经有加入这支部队的集结
		Set<IFGYLWorldMarch> massJoinMarchs = massMarch.getMassJoinMarchs(false);
		for (IWorldMarch massJoinMarch : massJoinMarchs) {
			if (massJoinMarch.getPlayerId().equals(player.getId())) {
				player.sendError(protocol.getType(), Status.Error.MASS_JOIN_REPEAT);
				return false;
			}
		}

		// 集结队伍是否已出发
		if (massMarch.getMarchEntity().getMarchStatus() != WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
			player.sendError(protocol.getType(), Status.Error.WORLD_MARCH_ALREADY_MARCHING);
			return false;
		}

		// 检查是否同盟
		if (!player.isInSameGuild(leader)) {
			player.sendError(protocol.getType(), Status.Error.MASS_ERR_NOT_SAME_GUILD);
			return false;
		}

		// 扣兵
		if (!checkArmyAndMarch(protocol.getType(), player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			return false;
		}

		// 回复协议
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		protocol.response(HawkProtocol.valueOf(HP.code.WORLD_MASS_JOIN_S_VALUE, builder));

		// 出发
		IFGYLWorldMarch march = player.getParent().startMarch(player, player, leader, req.getType(), massMarchId, 0, new EffectParams(req, armyList));
		if (req.getFgylSkill() > 0) { // 使用技能
			FGYLBattleCfg cfg = HawkConfigManager.getInstance().getKVInstance(FGYLBattleCfg.class);
			march.setFgylSkill(req.getFgylSkill());
			march.setUseHonor(cfg.getUseHonor());
			player.setSkillOrder(player.getSkillOrder()- cfg.getUseHonor());
			player.getPush().syncFGYLPlayerInfo();
		}

		return true;
	}
	
	

	public boolean checkArmyAndMarch(int protoType, IFGYLPlayer player, List<ArmyInfo> armyList, List<Integer> heroIdList, int superSoldierId) {
		player.getData().lockOriginalData();
		boolean result = ArmyService.getInstance().checkArmyAndMarch(player, armyList, heroIdList, superSoldierId);
		if (!result) {
			player.sendError(protoType, Status.Error.WORLD_MARCH_ARMY_COUNT);
			player.getPush().syncArmyInfo(ArmyChangeCause.DEFAULT);
		}
		return result;
	}
	
	public boolean checkMarchDressReq(IFGYLPlayer player, List<Integer> marchDressList) {
		try {
			PlayerMarchModule marckModule = player.getModule(ModuleType.WORLD_MARCH_MODULE);
			if (!marckModule.checkMarchDressReq(marchDressList)) {
				return false;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return true;
	}

	/**
	 * 集结卡信息请求
	 */
	@ProtocolHandler(code = HP.code2.CHAT_CARD_INFO_REQ_VALUE)
	public boolean onChatCardInfoReq(HawkProtocol protocol) {
		Activity.ChatCardInfoReq req = protocol.parseProtocol(Activity.ChatCardInfoReq.getDefaultInstance());
		if(req.getType() != Activity.ChatCardType.MASS_CARD){
			return false;
		}
		Activity.ChatCardInfoResp.Builder builder = Activity.ChatCardInfoResp.newBuilder();
		List<String> marchIdList = req.getIdsList();
		for (String marchId : marchIdList) {
			Activity.ChatMassCardInfo.Builder massCardInfo = getChatMassCardInfo(marchId, player);
			if (massCardInfo != null) {
				builder.addMassCardInfos(massCardInfo);
			}
		}
		builder.setType(req.getType());
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.CHAT_CARD_INFO_RESP_VALUE, builder));
		return true;
	}

	private Activity.ChatMassCardInfo.Builder getChatMassCardInfo(String marchId, IFGYLPlayer viewer) {
		try {
			Activity.ChatMassCardInfo.Builder builder = Activity.ChatMassCardInfo.newBuilder();
			builder.setMarchId(marchId);

			boolean isLeader = false;
			boolean hasFormation = false;
			boolean inFormation = false;
			boolean hasJoined = false;
			boolean massFull = false;
			boolean isMarch = false;
			boolean isEnd = false;

			IFGYLWorldMarch march = player.getParent().getMarch(marchId);
			if (march == null || march.getMarchEntity().isInvalid() || !march.isMassMarch() || !viewer.hasGuild()) {
				isEnd = true;
			} else {
				// 不是在行军中或者等待集结中,就认为结束了
				if (!march.isMarchState() && march.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
					isEnd = true;
				}
				isLeader = viewer.getId().equals(march.getPlayerId());

				GuildFormationObj guildFormationObj = player.getParent().getGuildFormation(viewer.getGuildId());
				GuildFormationCell guildFormationCell = guildFormationObj.getGuildFormation(marchId);

				// 是否有编队
				hasFormation = guildFormationCell != null && guildFormationCell.getIndex().getNumber() > 0;

				if (hasFormation) {
					// 是否在编队里面
					inFormation = guildFormationCell.fight(viewer.getId());
				}

				// 是否加入了集结
				Set<? extends IWorldMarch> massJoinMarchs = march.getMassJoinMarchs(false);
				for (IWorldMarch massJoinMarch : massJoinMarchs) {
					if (massJoinMarch.getPlayerId().equals(viewer.getId())) {
						hasJoined = true;
					}
				}

				// 编队满了
				Player leader = march.getParent();
				Set<? extends IWorldMarch> reachedMarchList = march.getMassJoinMarchs(true);
				int count = reachedMarchList != null ? reachedMarchList.size() : 0;
				if (count >= leader.getMaxMassJoinMarchNum(march) + march.getMarchEntity().getBuyItemTimes()) {
					massFull = true;
				}

				// 队伍是否已经出发了
				isMarch = march.getMarchEntity().getStartTime() < HawkTime.getMillisecond();
				builder.setMarchStartTime(march.getMarchEntity().getStartTime());
			}
			builder.setIsLeader(isLeader);
			builder.setHasFormation(hasFormation);
			builder.setInFormation(inFormation);
			builder.setHasJoined(hasJoined);
			builder.setMassFull(massFull);
			builder.setIsMarch(isMarch);
			builder.setIsEnd(isEnd);
			return builder;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return null;
	}
	
	/**集结出发*/
	@ProtocolHandler(code = HP.code2.FGYL_MARCH_MASS_GO_REQ_VALUE)
	private boolean onMassMarchGo(HawkProtocol protocol) {
		InviteMassReq req = protocol.parseProtocol(InviteMassReq.getDefaultInstance());
		String marchId = req.getMarchId();
		IFGYLWorldMarch march = player.getParent().getMarch(marchId);
		if (march == null || march.getParent() != player) {
			return false;
		}
		if(march.getMarchStatus()!= WorldMarchStatus.MARCH_STATUS_WAITING_VALUE){
			return false;
		}
		
		Set<IFGYLWorldMarch> reachJoinMarchs = march.getMassJoinMarchs(true);
		if (reachJoinMarchs.size() <= 0) {
			return false;
		}
		
		march.getMarchEntity().setStartTime(player.getParent().getCurTimeMil());
		player.responseSuccess(protocol.getType());
		return true;
	}
}
