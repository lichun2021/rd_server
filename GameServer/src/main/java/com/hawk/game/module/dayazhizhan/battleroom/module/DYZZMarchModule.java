package com.hawk.game.module.dayazhizhan.battleroom.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.hawk.game.protocol.Activity;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.game.config.BuildingCfg;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.config.ShopCfg;
import com.hawk.game.config.WorldMarchConstProperty;
import com.hawk.game.entity.item.GuildFormationCell;
import com.hawk.game.entity.item.GuildFormationObj;
import com.hawk.game.invoker.MarchSpeedInvoker;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.PlayerMarchModule;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZBattleRoom;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZProtocol;
import com.hawk.game.module.dayazhizhan.battleroom.IDYZZWorldPoint;
import com.hawk.game.module.dayazhizhan.battleroom.invoker.DYZZItemConsumeInvoker;
import com.hawk.game.module.dayazhizhan.battleroom.player.DYZZPlayer;
import com.hawk.game.module.dayazhizhan.battleroom.player.IDYZZPlayer;
import com.hawk.game.module.dayazhizhan.battleroom.worldmarch.IDYZZWorldMarch;
import com.hawk.game.module.dayazhizhan.battleroom.worldmarch.submarch.IDYZZMassMarch;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.DYZZBase;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.IDYZZBuilding;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.IDYZZFuelBank;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.skill.talent.ITalentSkill;
import com.hawk.game.player.skill.talent.TalentSkillContext;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Armour.ArmourSuitType;
import com.hawk.game.protocol.Army.ArmyChangeCause;
import com.hawk.game.protocol.Army.ArmySoldierPB;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.GuildWarehouse.HPYuriMonsterQueryHelpReq;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.World.AppointedCaptain;
import com.hawk.game.protocol.World.InviteMassReq;
import com.hawk.game.protocol.World.MarchArmyDetailInfo;
import com.hawk.game.protocol.World.MarchArmyDetailReq;
import com.hawk.game.protocol.World.MarchArmyDetailResp;
import com.hawk.game.protocol.World.MassCardInfo;
import com.hawk.game.protocol.World.MassCardInfoReq;
import com.hawk.game.protocol.World.MassCardInfoResp;
import com.hawk.game.protocol.World.RepatriateManorMarch;
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
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.ModuleType;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.sdk.msdk.entity.PayItemInfo;

public class DYZZMarchModule extends DYZZBattleRoomModule {
	public DYZZMarchModule(DYZZBattleRoom appObj) {
		super(appObj);
	}

	/**
	 * 使用行军表情
	 */
	@ProtocolHandler(code = HP.code.MARCH_EMOTICON_USE_C_VALUE)
	private boolean onUseMarchEmoticon(DYZZProtocol protocol) {
		UseMarchEmoticonReq req = protocol.parseProtocol(UseMarchEmoticonReq.getDefaultInstance());
		if (req.hasMarchId() && !HawkOSOperator.isEmptyString(req.getMarchId())) {
			marchEmoticonUse(protocol, req.getEmoticonId(), req.getMarchId());
		} else {
			cityEmoticonUse(protocol, req.getEmoticonId(), req.getMarchId());
		}

		return true;
	}

	/**
	 * 对城点使用行军表情判断
	 */
	public boolean cityEmoticonUse(DYZZProtocol protocol, int emoticonId, String marchId) {
		if (!useMarchEmoticon(protocol, emoticonId, marchId)) {
			return false;
		}
		IDYZZPlayer worldPoint = protocol.getPlayer();
		worldPoint.setEmoticon(emoticonId);
		worldPoint.setEmoticonUseTime(getParent().getCurTimeMil());
		getParent().worldPointUpdate(worldPoint);
		worldPoint.responseSuccess(protocol.getType());
		return true;
	}

	/**
	 * 对行军使用行军表情判断
	 * 
	 */
	public boolean marchEmoticonUse(DYZZProtocol protocol, int emoticonId, String marchId) {
		IDYZZPlayer player = protocol.getPlayer();
		IDYZZWorldMarch march = getParent().getMarch(marchId);
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
			IWorldMarch massMarch = getParent().getMarch(march.getMarchEntity().getTargetId()); // 获取集结的队长行军
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
		march.getMarchEntity().setEmoticonUseTime(getParent().getCurTimeMil());

		march.updateMarch();

		if (march.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MARCH_VALUE
				&& march.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
			IDYZZWorldPoint worldPoint = getParent().getWorldPoint(march.getTerminalId()).get();
			if (worldPoint instanceof IDYZZBuilding && ((IDYZZBuilding) worldPoint).getLeaderMarch() != null) {
				WorldMarch firstMarch = ((IDYZZBuilding) worldPoint).getLeaderMarch().getMarchEntity();
				firstMarch.setEmoticon(emoticonId);
				firstMarch.setEmoticonUseTime(HawkTime.getMillisecond());
			}
			getParent().worldPointUpdate(worldPoint);
		}
		
		player.responseSuccess(protocol.getType());
		return true;
	}

	public boolean useMarchEmoticon(DYZZProtocol protocol, int emoticonId, String marchId) {
		IDYZZPlayer player = protocol.getPlayer();
		PlayerMarchModule module = player.getModule(GsConst.ModuleType.WORLD_MARCH_MODULE);
		return module.useMarchEmoticon(protocol, emoticonId, marchId);
	}

	/**
	 * 邀请集结
	 */
	@ProtocolHandler(code = HP.code.INVITE_MASS_REQ_VALUE)
	private boolean onInviteMass(DYZZProtocol protocol) {
		IDYZZPlayer player = protocol.getPlayer();
		InviteMassReq req = protocol.parseProtocol(InviteMassReq.getDefaultInstance());

		IDYZZWorldMarch march = getParent().getMarch(req.getMarchId());
		if (march == null) {
			player.sendError(protocol.getType(), Status.Error.MASS_ERR_MARCH_NOT_EXIST);
			return false;
		}

		if (!march.isMassMarch()) {
			player.sendError(protocol.getType(), Status.Error.MASS_ERR_MARCH_NOT_EXIST);
			return false;
		}

		if (getParent().getCurTimeMil() - player.getData().getLastInviteMassTime() < WorldMarchConstProperty.getInstance().getInviteMassCD()) {
			player.sendError(protocol.getType(), Status.Error.INVITE_MASS_CD);
			return false;
		}

		Optional<IDYZZWorldPoint> worldPoint = getParent().getWorldPoint(march.getMarchEntity().getTerminalId());
		if (worldPoint.isPresent()) {
			sendNotice(worldPoint.get(), march);
		}

		player.getData().setLastInviteMassTime(getParent().getCurTimeMil());
		player.getPush().syncPlayerInfo();
		player.responseSuccess(protocol.getType());

		return true;
	}

	/**
	 * 发送公告
	 */
	public void sendNotice(IDYZZWorldPoint point, IDYZZWorldMarch march) {
		IDYZZPlayer player = march.getParent();
		if (point instanceof IDYZZPlayer) {
			ChatParames parames = ChatParames.newBuilder().setPlayer(player).setChatType(ChatType.CHAT_FUBEN_TEAM).setKey(NoticeCfgId.DYZZ_MASS_START_PLAYER)
					.addParms(((IDYZZPlayer) point).getName())
					.addParms(march.getMarchId()).addParms(player.getX()).addParms(player.getY()).addParms(0).addParms("").build();
			getParent().addWorldBroadcastMsg(parames);
		} else if (point instanceof IDYZZBuilding) {
			ChatParames parames = ChatParames.newBuilder().setPlayer(player).setChatType(ChatType.CHAT_FUBEN_TEAM).setKey(NoticeCfgId.DYZZ_MASS_START_BUILD)
					.addParms(point.getX())
					.addParms(point.getY())
					.addParms(march.getMarchId()).addParms(player.getX()).addParms(player.getY()).addParms(0).addParms("").build();
			getParent().addWorldBroadcastMsg(parames);
		}
	}

	/** 请求行军支援 */
	@ProtocolHandler(code = HP.code.YURI_MONSTER_QUERY_HELP_VALUE)
	private void onYuriMonsterQueryHelp(DYZZProtocol protocol) {
		IDYZZPlayer player = protocol.getPlayer();
		HPYuriMonsterQueryHelpReq req = protocol.parseProtocol(HPYuriMonsterQueryHelpReq.getDefaultInstance());
		// 行军已不存在
		IDYZZWorldMarch worldMarch = getParent().getMarch(req.getMarhcId());
		if (worldMarch == null) {
			return;
		}

		Optional<IDYZZWorldPoint> point = getParent().getWorldPoint(worldMarch.getTerminalId());
		int pointType = point.isPresent() ? point.get().getPointType().getNumber() : 0;
		ChatParames parames = ChatParames.newBuilder().setPlayer(player).setChatType(ChatType.CHAT_FUBEN_TEAM).setKey(NoticeCfgId.DYZZ_MARCH_QUERY_HELP)
				.addParms(worldMarch.getTerminalX())
				.addParms(worldMarch.getTerminalY())
				.addParms(pointType)
				.build();
		getParent().addWorldBroadcastMsg(parames);

		player.responseSuccess(protocol.getType());

	}

	/**
	 * 遣返联盟领地行军
	 */
	@ProtocolHandler(code = HP.code.REPATRIATE_MANOR_MARCH_C_VALUE)
	private boolean onRepatriateManormarch(DYZZProtocol protocol) {
		IDYZZPlayer player = protocol.getPlayer();
		if (!player.hasGuild()) {
			return false;
		}
		RepatriateManorMarch req = protocol.parseProtocol(RepatriateManorMarch.getDefaultInstance());
		// 世界点
		IDYZZWorldPoint worldPoint = battleRoom.getWorldPoint(req.getX(), req.getY()).orElse(null);
		if (worldPoint == null) {
			player.sendError(protocol.getType(), Status.Error.WORLD_POINT_NOT_EXIST);
			return false;
		}

		// 战区
		if (worldPoint instanceof IDYZZBuilding) {
			IDYZZBuilding building = (IDYZZBuilding) worldPoint;
			building.repatriateMarch(player, req.getPlayerId());
			building.syncQuarterInfo(player);
		}

		player.responseSuccess(HP.code.REPATRIATE_MANOR_MARCH_C_VALUE);
		return true;
	}

	/**
	 * 任命队长
	 */
	@ProtocolHandler(code = HP.code.APPOINTED_CAPTAIN_VALUE)
	private boolean onChangeQuarterLeader(DYZZProtocol protocol) {
		IDYZZPlayer player = protocol.getPlayer();
		if (!player.hasGuild()) {
			return false;
		}
		AppointedCaptain req = protocol.parseProtocol(AppointedCaptain.getDefaultInstance());
		// 世界点
		IDYZZWorldPoint worldPoint = battleRoom.getWorldPoint(req.getX(), req.getY()).orElse(null);
		if (worldPoint == null) {
			player.sendError(protocol.getType(), Status.Error.WORLD_POINT_NOT_EXIST);
			return false;
		}

		// 战区
		if (worldPoint instanceof IDYZZBuilding) {
			IDYZZBuilding building = (IDYZZBuilding) worldPoint;
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
	protected boolean onWorldMassDissolve(DYZZProtocol protocol) {
		IDYZZPlayer player = protocol.getPlayer();
		WorldMassDissolveReq req = protocol.parseProtocol(WorldMassDissolveReq.getDefaultInstance());
		// check
		IDYZZWorldMarch worldMarch = getParent().getMarch(req.getMarchId());
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

		for (IDYZZWorldMarch jm : worldMarch.getMassJoinMarchs(true)) {
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
	protected boolean onWorldMassRepatriate(DYZZProtocol protocol) {
		IDYZZPlayer player = protocol.getPlayer();
		WorldMassRepatriateReq req = protocol.parseProtocol(WorldMassRepatriateReq.getDefaultInstance());

		IDYZZWorldMarch worldMarch = getParent().getMarch(req.getMarchId());
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

		IDYZZWorldMarch leaderMarch = getParent().getMarch(march.getTargetId());
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
	 * DYZZ_SINGLE_MARCH = 487; // 副本内特有建筑单人行军
	 */
	@ProtocolHandler(code = HP.code2.DYZZ_SINGLE_MARCH_VALUE)
	private boolean onDYZZ_SINGLE_MARCHStart(DYZZProtocol protocol) {
		IDYZZPlayer player = protocol.getPlayer();
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		IDYZZWorldPoint point = battleRoom.getWorldPoint(req.getPosX(), req.getPosY()).orElse(null);
		// 路点为空
		if (point == null) {
			player.sendError(protocol.getType(), Status.Error.WORLD_POINT_NOT_EXIST);
			return false;
		}
		if (point.getProtectedEndTime() > battleRoom.getCurTimeMil() || point instanceof DYZZBase) {
			player.sendError(protocol.getType(), Status.DYZZError.DYZZ_BUILD_PROCTED);
			return false;
		}
		// 联盟检查
		if (point.needJoinGuild() && !player.hasGuild()) {
			player.sendError(protocol.getType(), Status.Error.GUILD_NO_JOIN);
			return false;
		}
		WorldMarchType marchType = null;
		switch (point.getPointType()) {
		case DYZZ_TOWER:// 内外塔
			marchType = WorldMarchType.DYZZ_TOWER_SINGLE;
			break;
		case DYZZ_ENERGY_WELL: // = 244; // 能源井（祭坛)
			marchType = WorldMarchType.DYZZ_ENERGY_WELL_SINGLE;
			break;
			case DYZZ_HIGH_TOWER: //
			marchType = WorldMarchType.DYZZ_HIGH_TOWER_SINGLE;
			break;
		default:
			player.sendError(protocol.getType(), Status.Error.WORLD_MARCH_REQ_TYPE_ERROR);
			return false;
		}

		// 带兵出征通用检查
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>(); // 出征的士兵列表
		if (!checkMarchReq(player, req, protocol.getType(), armyList, point, true)) {
			return false;
		}

		// 扣兵
		if (!checkArmyAndMarch(protocol.getType(), player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			return false;
		}

		getParent().startMarch(player, player, point, marchType, "", 0, new EffectParams(req, armyList));

		// 回复协议
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		protocol.response(HawkProtocol.valueOf(HP.code2.DYZZ_SINGLE_MARCH_S, builder));
		return true;

	}

	/**
	 * 采集资源
	 */
	@ProtocolHandler(code = HP.code.WORLD_COLLECTRESOURCE_C_VALUE)
	private boolean onWorldCollectResourceStart(DYZZProtocol protocol) {
		IDYZZPlayer player = protocol.getPlayer();
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		IDYZZWorldPoint reqPoint = getParent().getWorldPoint(req.getPosX(), req.getPosY()).orElse(null);

		// check点数据
		if (reqPoint == null || !(reqPoint instanceof IDYZZFuelBank)) {
			player.sendError(protocol.getType(), Status.Error.WORLD_POINT_NOT_RESOURCE);
			return false;
		}
		IDYZZFuelBank point = (IDYZZFuelBank) reqPoint;

		// 带兵出征通用检查
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>(); // 出征的士兵列表
		if (!checkMarchReq(player, req, protocol.getType(), armyList, point, true)) {
			return false;
		}

		// 扣兵
		if (!checkArmyAndMarch(protocol.getType(), player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			return false;
		}

		getParent().startMarch(player, player, point, WorldMarchType.COLLECT_RESOURCE, point.getResourceId() + "", 0, new EffectParams(req, armyList));

		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		protocol.response(HawkProtocol.valueOf(HP.code.WORLD_COLLECTRESOURCE_S_VALUE, builder));

		return true;
	}

	/** 行军详情(暂时只支持集结行军，传集结行军marchId) */
	@ProtocolHandler(code = HP.code.MARCH_ARMY_DETAIL_REQ_VALUE)
	private boolean onMarchArmyDetailInfo(DYZZProtocol protocol) {
		IDYZZPlayer player = protocol.getPlayer();
		MarchArmyDetailReq req = protocol.parseProtocol(MarchArmyDetailReq.getDefaultInstance());
		String marchId = req.getMarchId();

		// 参数错误
		if (HawkOSOperator.isEmptyString(marchId)) {
			player.sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			return false;
		}

		// 没有这条行军
		IDYZZWorldMarch march = battleRoom.getMarch(marchId);
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

		Set<IDYZZWorldMarch> allMarch = new HashSet<>();
		allMarch.add(march);
		allMarch.addAll(march.getMassJoinMarchs(true));

		boolean canReq = false;

		// 是否有自己的行军
		for (IDYZZWorldMarch thisMarch : allMarch) {
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
		for (IDYZZWorldMarch thisMarch : allMarch) {
			MarchArmyDetailInfo.Builder info = MarchArmyDetailInfo.newBuilder();
			IDYZZPlayer mp = thisMarch.getParent();
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

	@ProtocolHandler(code = HP.code.WORLD_ATTACK_PLAYER_C_VALUE)
	private boolean onWorldAttackPlayerStart(DYZZProtocol protocol) {
		IDYZZPlayer player = protocol.getPlayer();
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		IDYZZWorldPoint point = battleRoom.getWorldPoint(req.getPosX(), req.getPosY()).orElse(null);
		// 路点为空
		if (point == null) {
			player.sendError(protocol.getType(), Status.Error.WORLD_POINT_NOT_EXIST);
			return false;
		}
		if (getParent().getCurTimeMil() < point.getProtectedEndTime()) {
			return false;
		}
		// 联盟检查
		if (point.needJoinGuild() && !player.hasGuild()) {
			player.sendError(protocol.getType(), Status.Error.GUILD_NO_JOIN);
			return false;
		}
		// 目标可能没有
		if (!(point instanceof IDYZZPlayer)) {
			player.sendError(protocol.getType(), Status.Error.WORLD_POINT_TYPE_ERROR);
			return false;
		}
		// 目标玩家和自己同盟
		IDYZZPlayer target = (IDYZZPlayer) point;
		if (Objects.equals(player.getDYZZGuildId(), target.getId())) {
			player.sendError(protocol.getType(), Status.Error.WORLD_POINT_TYPE_ERROR);
			return false;
		}
		// 攻打普通玩家
		String targetId = target.getId();

		// 带兵出征通用检查
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>(); // 出征的士兵列表
		if (!checkMarchReq(player, req, protocol.getType(), armyList, point, true)) {
			return false;
		}

		// 技能使用判断
		List<Integer> useSkills = req.getUseSkillIdList();
		if (useSkills != null && useSkills.contains(GsConst.SKILL_10104)) {
			ITalentSkill skill = TalentSkillContext.getInstance().getSkill(GsConst.SKILL_10104);
			boolean touchSkill = skill.touchSkill(player, targetId, ArmyService.getInstance().getArmysCount(armyList));
			// 不能触发技能
			if (!touchSkill) {
				player.sendError(protocol.getType(), Status.Error.TALENT_SKILL_USE_FAIL);
				return false;
			}
		}
		// 扣兵
		if (!checkArmyAndMarch(protocol.getType(), player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			return false;
		}

		getParent().startMarch(player, player, target, WorldMarchType.ATTACK_PLAYER, targetId, 0, new EffectParams(req, armyList));

		// 回复协议
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		protocol.response(HawkProtocol.valueOf(HP.code.WORLD_ATTACK_PLAYER_S_VALUE, builder));
		return true;
	}

	/**
	 * 行军加速
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.WORLD_MARCH_SPEEDUP_C_VALUE)
	private boolean onMarchSpeedUp(DYZZProtocol protocol) {
		IDYZZPlayer player = protocol.getPlayer();
		// check
		WorldMarchSpeedUpReq req = protocol.parseProtocol(WorldMarchSpeedUpReq.getDefaultInstance());
		if (!req.hasMarchId() || !req.hasItemId()) {
			player.sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			return false;
		}

		// // 道具id非法
		// if (req.getItemId() != Const.ItemId.ITEM_WORLD_MARCH_SPEEDUPH_VALUE && req.getItemId() != Const.ItemId.ITEM_WORLD_MARCH_SPEEDUPL_VALUE) {
		// player.sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
		// return false;
		// }

		// 行军信息
		IDYZZWorldMarch march = battleRoom.getMarch(req.getMarchId());
		if (march == null) {
			player.sendError(protocol.getType(), Status.Error.WORLD_MARCH_NOT_EXISIT);
			return false;
		}

		// 加入集结出征，给队长行军加速
		if (march.isMassJoinMarch() && march.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_JOIN_MARCH_VALUE) {
			march = battleRoom.getMarch(march.getMarchEntity().getTargetId());
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
			player.dealMsg(MsgId.WORLD_MARCH, new DYZZItemConsumeInvoker(player, consumeItems, Action.WORLD_MARCH_SPEEDUP));
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
	private boolean onMarchCallBack(DYZZProtocol protocol) {
		IDYZZPlayer player = protocol.getPlayer();
		// check param
		WorldMarchServerCallBackReq req = protocol.parseProtocol(WorldMarchServerCallBackReq.getDefaultInstance());
		if (!req.hasMarchId()) {
			player.sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			return false;
		}
		// 检测行军是否存在
		IDYZZWorldMarch march = battleRoom.getPlayerMarch(player.getId(), req.getMarchId());
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
				consumeItems.addPayItemInfo(new PayItemInfo(String.valueOf(Const.ItemId.ITEM_WORLD_MARCH_CALLBACK_VALUE), (int) priceItem.getCount(), 1));
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
	private boolean onPointMarchCallBack(DYZZProtocol protocol) {
		IDYZZPlayer player = protocol.getPlayer();
		// check param
		WorldPointMarchCallBackReq req = protocol.parseProtocol(WorldPointMarchCallBackReq.getDefaultInstance());
		IDYZZWorldPoint worldPoint = battleRoom.getWorldPoint(req.getX(), req.getY()).orElse(null);
		if (worldPoint == null) {
			player.sendError(protocol.getType(), Status.Error.WORLD_POINT_NOT_EXIST); // 提示目标点没有援助行军
			return false;
		}

		if (worldPoint.getPointType() == WorldPointType.PLAYER) {
			// 获取是否有驻军信息
			IDYZZWorldMarch march = battleRoom.getPointMarches(GameUtil.combineXAndY(req.getX(), req.getY()), WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST, WorldMarchType.ASSISTANCE)
					.stream()
					.filter(ma -> Objects.equals(ma.getPlayerId(), player.getId())).findFirst().orElse(null);
			if (march == null) {
				player.sendError(protocol.getType(), Status.Error.POINT_NO_MARCH_CALL_BACK);// 提示目标点没有援助行军
				return false;
			}
			// 召回
			march.onMarchCallback();
		} else if (worldPoint.getPointType() == WorldPointType.DYZZ_FUELBANK) {
			// 获取是否有驻军信息
			IDYZZWorldMarch march = battleRoom
					.getPointMarches(GameUtil.combineXAndY(req.getX(), req.getY()), WorldMarchStatus.MARCH_STATUS_MARCH_COLLECT, WorldMarchType.COLLECT_RESOURCE).stream()
					.filter(ma -> Objects.equals(ma.getPlayerId(), player.getId())).findFirst().orElse(null);
			if (march == null) {
				player.sendError(protocol.getType(), Status.Error.POINT_NO_MARCH_CALL_BACK);// 提示目标点没有援助行军
				return false;
			}
			// 召回
			march.onMarchCallback();
		} else if (worldPoint instanceof IDYZZBuilding) {
			// 获取是否有驻军信息
			IDYZZWorldMarch march = battleRoom.getPointMarches(GameUtil.combineXAndY(req.getX(), req.getY()), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED).stream()
					.filter(ma -> Objects.equals(ma.getPlayerId(), player.getId())).findFirst().orElse(null);
			if (march == null) {
				player.sendError(protocol.getType(), Status.Error.POINT_NO_MARCH_CALL_BACK);// 提示目标点没有援助行军
				return false;
			}
			// 召回
			march.onMarchCallback();
		}

		player.responseSuccess(protocol.getType());
		return true;
	}

	/** 世界侦查 */
	@ProtocolHandler(code = HP.code.WORLD_SPY_C_VALUE)
	private boolean onWorldSpyStart(DYZZProtocol protocol) {
		IDYZZPlayer player = protocol.getPlayer();
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());

		// 目标点
		IDYZZWorldPoint point = battleRoom.getWorldPoint(req.getPosX(), req.getPosY()).orElse(null);
		if (Objects.isNull(point)) {
			return false;
		}
		if (!checkMarchReq(player, req, protocol.getType(), Collections.emptyList(), point, true)) {
			return false;
		}
		// 消费check
		boolean useGold = req.hasSpyUseGold() && req.getSpyUseGold();
		if (useGold && player instanceof DYZZPlayer) {
			ConsumeItems consume = ConsumeItems.valueOf();
			consume.addConsumeInfo(WorldMarchConstProperty.getInstance().getInvestigationMarchCost(), useGold);
			if (!consume.checkConsume(player, protocol.getType())) {
				return false;
			}
			consume.consumeAndPush(player, Action.WORLD_SPY);
		}
		String targetId = "";
		if (point instanceof DYZZPlayer) {
			IDYZZPlayer targetPlayer = (IDYZZPlayer) point;
			// 普通玩家基地，放玩家的ID
			targetId = targetPlayer.getId();
		}
		// 创建行军
		getParent().startMarch(player, player, point, WorldMarchType.SPY, targetId, 0, new EffectParams());
		// 回复协议
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		protocol.response(HawkProtocol.valueOf(HP.code.WORLD_SPY_S_VALUE, builder));

		return true;
	}

	/** 援助玩家（火力支援） */
	@ProtocolHandler(code = HP.code.WORLD_ASSISTANCE_C_VALUE)
	private boolean onWorldAssistanceStart(DYZZProtocol protocol) {
		IDYZZPlayer player = protocol.getPlayer();
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		IDYZZWorldPoint point = battleRoom.getWorldPoint(req.getPosX(), req.getPosY()).orElse(null);

		// 世界点不存在
		if (point == null) {
			player.sendError(protocol.getType(), Status.Error.WORLD_POINT_NOT_EXIST);
			return false;
		}

		// 是否是可援助点
		if (point.getPointType() != WorldPointType.PLAYER) {
			player.sendError(protocol.getType(), Status.Error.NOT_YOUR_GUILD_MANOR);
			return false;
		}

		// 获取目标玩家
		final IDYZZPlayer tarPlayer = (IDYZZPlayer) point;

		// 检查玩家是否在同一联盟
		if (Objects.equals(player.getDYZZGuildId(), tarPlayer.getId())) {
			player.sendError(protocol.getType(), Status.Error.GUILD_NOT_MEMBER);
			return false;
		}

		// 被援助者和援助者都拥有建筑“大使馆”，才可被援助和援助。
		BuildingCfg myCfg = player.getData().getBuildingCfgByType(BuildingType.EMBASSY);
		BuildingCfg tarCfg = tarPlayer.getData().getBuildingCfgByType(BuildingType.EMBASSY);
		if (myCfg == null || tarCfg == null) {
			player.sendError(protocol.getType(), Status.Error.BUILDING_FRONT_NOT_EXISIT);
			return false;
		}

		// 检查玩家是否有同类型行军,一个玩家只能对同一个玩家援助一次
		long size = battleRoom.getPointMarches(point.getPointId(), req.getType()).stream().filter(m -> m.getPlayerId().equals(player.getId())).count();
		if (size > 0) {
			player.sendError(protocol.getType(), Status.Error.HAS_SAME_TYPE_MARCH);
			return false;
		}

		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>(); // 出征的士兵列表
		if (!checkMarchReq(player, req, protocol.getType(), armyList, point, true)) {
			return false;
		}

		// 扣兵
		if (!checkArmyAndMarch(protocol.getType(), player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			return false;
		}

		getParent().startMarch(player, player, point, WorldMarchType.ASSISTANCE, tarPlayer.getId(), 0, new EffectParams(req, armyList));

		// 回复协议
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		protocol.response(HawkProtocol.valueOf(HP.code.WORLD_ASSISTANCE_S_VALUE, builder));

		return true;

	}

	/**
	 * 是否有空闲出征队列
	 * 
	 * @return
	 */
	public boolean isHasFreeMarch(IDYZZPlayer player) {
		// 机器人不受行军数量限制
		if (player.isRobot()) {
			return true;
		}
		List<IDYZZWorldMarch> playerMarches = getParent().getPlayerMarches(player.getId());
		// 行军个数
		int marchCount = 0;
		for (IDYZZWorldMarch march : playerMarches) {
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
	public boolean checkMarchReq(IDYZZPlayer player, WorldMarchReq req, int hpCode, List<ArmyInfo> armyList, IDYZZWorldPoint worldPoint, boolean checkFreeMarch) {
		if (player.isAnchor()) {
			return false;
		}

		if (battleRoom.getCollectStartTime() > getParent().getCurTimeMil()) {
			player.sendError(hpCode, Status.DYZZError.DYZZ_PRETIME_CANNOT_MARCH);
			return false;
		}

		boolean hasFreeMarch = isHasFreeMarch(player);
		if (hpCode == HP.code.WORLD_SPY_C_VALUE) {
			if (getParent().isExtraSpyMarchOpen(player) && !getParent().isExtraSypMarchOccupied(player)) {
				hasFreeMarch = true;
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
	private boolean onWorldMassStart(DYZZProtocol protocol) {
		IDYZZPlayer player = protocol.getPlayer();
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		int waitTime = req.getMassTime();
		// int marchType = req.getType().getNumber();
		// // 不是集结类型的行军
		// if (!WorldUtil.isMassMarch(marchType)) {
		// player.sendError(protocol.getType(),
		// Status.Error.WORLD_MARCH_REQ_TYPE_ERROR);
		// return false;
		// }

		IDYZZWorldPoint point = battleRoom.getWorldPoint(req.getPosX(), req.getPosY()).orElse(null);
		if (point == null || point.getPointType() == WorldPointType .PLAYER) {
			player.sendError(protocol.getType(), Status.Error.WORLD_POINT_TYPE_ERROR);
			return false;
		}
		if (point.getProtectedEndTime() > battleRoom.getCurTimeMil() || point instanceof DYZZBase) {
			player.sendError(protocol.getType(), Status.DYZZError.DYZZ_BUILD_PROCTED);
			return false;
		}

		// 带兵出征通用检查
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>();
		if (!checkMarchReq(player, req, protocol.getType(), armyList, point, true)) {
			return false;
		}

		String targetId = "";
		WorldMarchType marchType = null;
		switch (point.getPointType()) {
		case PLAYER://
			marchType = WorldMarchType.MASS;
			IDYZZPlayer defPlayer = (IDYZZPlayer) point;
			// 目标id
			targetId = defPlayer.getId();
			break;
		case DYZZ_TOWER:// 铁幕装置
			marchType = WorldMarchType.DYZZ_TOWER_MASS;
			break;
		case DYZZ_ENERGY_WELL:// 核弹发射井
			marchType = WorldMarchType.DYZZ_ENERGY_WELL_MASS;
			break;
		case DYZZ_HIGH_TOWER://
			marchType = WorldMarchType.DYZZ_HIGH_TOWER_MASS;
			break;
		default:
			player.sendError(protocol.getType(), Status.Error.WORLD_MARCH_REQ_TYPE_ERROR);
			return false;
		}

		// 扣兵
		if (!checkArmyAndMarch(protocol.getType(), player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			return false;
		}

		IDYZZWorldMarch march = getParent().startMarch(player, player, point, marchType, targetId, waitTime, new EffectParams(req, armyList));

		// 回复协议
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		protocol.response(HawkProtocol.valueOf(HP.code.WORLD_MASS_S_VALUE, builder));

		sendNotice(point, march);

		return true;
	}

	/** 加入集结 */
	@ProtocolHandler(code = HP.code.WORLD_MASS_JOIN_C_VALUE)
	private boolean onWorldMassJoinStart(DYZZProtocol protocol) {
		IDYZZPlayer player = protocol.getPlayer();
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		String massMarchId = req.getMarchId();
		IDYZZWorldMarch worldMarch = battleRoom.getMarch(massMarchId);
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
		IDYZZPlayer leader = worldMarch.getParent();
		String leaderId = leader.getId();
		if (player.getId().equals(leaderId)) {
			player.sendError(protocol.getType(), Status.Error.MASS_ERR_CANOT_JOIN_SELF);
			return false;
		}

		// 带兵出征通用检查
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>(); // 出征的士兵列表
		if (!checkMarchReq(player, req, protocol.getType(), armyList, leader, true)) {
			return false;
		}

		IDYZZMassMarch massMarch = (IDYZZMassMarch) worldMarch;
		// 集结行军类型和参与集结行军类型不一致
		int joinMarchType = massMarch.getJoinMassType().getNumber();
		if (marchType != joinMarchType) {
			player.sendError(protocol.getType(), Status.Error.MASS_JOIN_TYPE_ERROR);
			return false;
		}

		// 已经有加入这支部队的集结
		Set<IDYZZWorldMarch> massJoinMarchs = massMarch.getMassJoinMarchs(false);
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
		getParent().startMarch(player, player, leader, req.getType(), massMarchId, 0, new EffectParams(req, armyList));

		return true;
	}

	public boolean checkArmyAndMarch(int protoType, IDYZZPlayer player, List<ArmyInfo> armyList, List<Integer> heroIdList, int superSoldierId) {
		player.getData().lockOriginalData();
		boolean result = ArmyService.getInstance().checkArmyAndMarch(player, armyList, heroIdList, superSoldierId);
		if (!result) {
			player.sendError(protoType, Status.Error.WORLD_MARCH_ARMY_COUNT);
			player.getPush().syncArmyInfo(ArmyChangeCause.DEFAULT);
		}
		return result;
	}
	
	public boolean checkMarchDressReq(IDYZZPlayer player, List<Integer> marchDressList) {
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
	public boolean onChatCardInfoReq(DYZZProtocol protocol) {
		IDYZZPlayer player = protocol.getPlayer();
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

	private Activity.ChatMassCardInfo.Builder getChatMassCardInfo(String marchId, IDYZZPlayer viewer) {
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

			IDYZZWorldMarch march = viewer.getParent().getMarch(marchId);
			if (march == null || march.getMarchEntity().isInvalid() || !march.isMassMarch() || !viewer.hasGuild()) {
				isEnd = true;
			} else {
				// 不是在行军中或者等待集结中,就认为结束了
				if (!march.isMarchState() && march.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
					isEnd = true;
				}
				isLeader = viewer.getId().equals(march.getPlayerId());

				GuildFormationObj guildFormationObj = viewer.getParent().getGuildFormation(viewer.getGuildId());
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

}
