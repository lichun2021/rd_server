package com.hawk.game.lianmengjunyan.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

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
import com.hawk.game.lianmengjunyan.ILMJYWorldPoint;
import com.hawk.game.lianmengjunyan.LMJYBattleRoom;
import com.hawk.game.lianmengjunyan.LMJYProtocol;
import com.hawk.game.lianmengjunyan.entity.LMJYMarchEntity;
import com.hawk.game.lianmengjunyan.player.ILMJYPlayer;
import com.hawk.game.lianmengjunyan.player.LMJYPlayer;
import com.hawk.game.lianmengjunyan.worldmarch.ILMJYWorldMarch;
import com.hawk.game.lianmengjunyan.worldmarch.LMJYAssistanceSingleMarch;
import com.hawk.game.lianmengjunyan.worldmarch.LMJYAttackPlayerMarch;
import com.hawk.game.lianmengjunyan.worldmarch.LMJYMassJoinSingleMarch;
import com.hawk.game.lianmengjunyan.worldmarch.LMJYMassSingleMarch;
import com.hawk.game.lianmengjunyan.worldmarch.LMJYSpyMarch;
import com.hawk.game.lianmengjunyan.worldmarch.submarch.ILMJYMassMarch;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.PlayerMarchModule;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.skill.talent.ITalentSkill;
import com.hawk.game.player.skill.talent.TalentSkillContext;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Armour.ArmourSuitType;
import com.hawk.game.protocol.Army.ArmySoldierPB;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.MechaCore.MechaCoreSuitType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.World.MarchArmyDetailInfo;
import com.hawk.game.protocol.World.MarchArmyDetailReq;
import com.hawk.game.protocol.World.MarchArmyDetailResp;
import com.hawk.game.protocol.World.MarchEvent;
import com.hawk.game.protocol.World.UseMarchEmoticonReq;
import com.hawk.game.protocol.World.WorldMarchRelation;
import com.hawk.game.protocol.World.WorldMarchReq;
import com.hawk.game.protocol.World.WorldMarchResp;
import com.hawk.game.protocol.World.WorldMarchServerCallBackReq;
import com.hawk.game.protocol.World.WorldMarchServerCallBackResp;
import com.hawk.game.protocol.World.WorldMarchSpeedUpReq;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointMarchCallBackReq;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.ArmyService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.ModuleType;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.sdk.msdk.entity.PayItemInfo;

public class LMJYMarchModule extends ILMJYBattleRoomModule {
	public LMJYMarchModule(LMJYBattleRoom appObj) {
		super(appObj);
	}
	
	/**
	 * 使用行军表情
	 */
	@ProtocolHandler(code = HP.code.MARCH_EMOTICON_USE_C_VALUE)
	private boolean onUseMarchEmoticon(LMJYProtocol protocol) {
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
	public boolean cityEmoticonUse(LMJYProtocol protocol, int emoticonId, String marchId) {
		if (!useMarchEmoticon(protocol, emoticonId, marchId)) {
			return false;
		}
		ILMJYPlayer worldPoint = protocol.getPlayer();
		worldPoint.setEmoticon(emoticonId);
		worldPoint.setEmoticonUseTime(HawkTime.getMillisecond());
		getParent().worldPointUpdate(worldPoint);
		worldPoint.responseSuccess(protocol.getType());
		return true;
	}
	
	/**
	 * 对行军使用行军表情判断
	 * 
	 */
	public boolean marchEmoticonUse(LMJYProtocol protocol, int emoticonId, String marchId) {
		ILMJYPlayer player = protocol.getPlayer();
		ILMJYWorldMarch march = getParent().getMarch(marchId);
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
		march.getMarchEntity().setEmoticonUseTime(HawkTime.getMillisecond());
		
		march.updateMarch();
		
		if (march.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MARCH_VALUE 
				&& march.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
			ILMJYWorldPoint worldPoint = getParent().getWorldPoint(march.getTerminalId()).get();
			getParent().worldPointUpdate(worldPoint);
		}
		
		player.responseSuccess(protocol.getType());
		return true;
	}
	
	public boolean useMarchEmoticon(LMJYProtocol protocol, int emoticonId, String marchId) {
		ILMJYPlayer player = protocol.getPlayer();
		PlayerMarchModule module = player.getModule(GsConst.ModuleType.WORLD_MARCH_MODULE);
		return module.useMarchEmoticon(protocol, emoticonId, marchId);
	}

	/** 行军详情(暂时只支持集结行军，传集结行军marchId) */
	@ProtocolHandler(code = HP.code.MARCH_ARMY_DETAIL_REQ_VALUE)
	private boolean onMarchArmyDetailInfo(LMJYProtocol protocol) {
		ILMJYPlayer player = protocol.getPlayer();
		MarchArmyDetailReq req = protocol.parseProtocol(MarchArmyDetailReq.getDefaultInstance());
		String marchId = req.getMarchId();

		// 参数错误
		if (HawkOSOperator.isEmptyString(marchId)) {
			player.sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			return false;
		}

		// 没有这条行军
		ILMJYWorldMarch march = battleRoom.getMarch(marchId);
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

		Set<ILMJYWorldMarch> allMarch = new HashSet<>();
		allMarch.add(march);
		allMarch.addAll(march.getMassJoinMarchs(true));

		boolean canReq = false;

		// 是否有自己的行军
		for (ILMJYWorldMarch thisMarch : allMarch) {
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
		for (ILMJYWorldMarch thisMarch : allMarch) {
			MarchArmyDetailInfo.Builder info = MarchArmyDetailInfo.newBuilder();
			ILMJYPlayer mp = thisMarch.getParent();
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
	private boolean onWorldAttackPlayerStart(LMJYProtocol protocol) {
		ILMJYPlayer player = protocol.getPlayer();
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		ILMJYWorldPoint point = battleRoom.getWorldPoint(req.getPosX(), req.getPosY()).orElse(null);
		// 路点为空
		if (point == null) {
			player.sendError(protocol.getType(), Status.Error.WORLD_POINT_NOT_EXIST);
			return false;
		}
		// 联盟检查
		if (point.needJoinGuild() && !player.hasGuild()) {
			player.sendError(protocol.getType(), Status.Error.GUILD_NO_JOIN);
			return false;
		}
		// 目标可能没有
		if (!(point instanceof ILMJYPlayer)) {
			player.sendError(protocol.getType(), Status.Error.WORLD_POINT_TYPE_ERROR);
			return false;
		}
		// 目标玩家和自己同盟
		ILMJYPlayer target = (ILMJYPlayer) point;
		if (Objects.equals(player.getGuildId(), target.getId())) {
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
		if (!ArmyService.getInstance().checkArmyAndMarch(player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			player.sendError(protocol.getType(), Status.Error.WORLD_MARCH_ARMY_COUNT);
			return false;
		}

		startMarch(player, target, WorldMarchType.ATTACK_PLAYER, targetId, 0, new EffectParams(req, armyList));

		// 回复协议
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		protocol.response(HawkProtocol.valueOf(HP.code.WORLD_ATTACK_PLAYER_S_VALUE, builder));
		return true;
	}

	/** 行军加速
	 * 
	 * @param protocol
	 * @return */
	@ProtocolHandler(code = HP.code.WORLD_MARCH_SPEEDUP_C_VALUE)
	private boolean onMarchSpeedUp(LMJYProtocol protocol) {
		ILMJYPlayer player = protocol.getPlayer();
		// check
		WorldMarchSpeedUpReq req = protocol.parseProtocol(WorldMarchSpeedUpReq.getDefaultInstance());
		if (!req.hasMarchId() || !req.hasItemId()) {
			player.sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			return false;
		}

		// 道具id非法
		if (req.getItemId() != Const.ItemId.ITEM_WORLD_MARCH_SPEEDUPH_VALUE
				&& req.getItemId() != Const.ItemId.ITEM_WORLD_MARCH_SPEEDUPL_VALUE) {
			player.sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			return false;
		}

		// 行军信息
		ILMJYWorldMarch march = battleRoom.getPlayerMarch(player.getId(), req.getMarchId());
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
			if (consumeItems.getBuilder().hasAttrInfo() && consumeItems.getBuilder().getAttrInfo().getDiamond() > 0) {
				ShopCfg shopCfg = ShopCfg.getShopCfgByItemId(req.getItemId());
				ItemInfo priceItem = shopCfg.getPriceItemInfo();
				consumeItems.addPayItemInfo(new PayItemInfo(String.valueOf(req.getItemId()), (int)priceItem.getCount(), 1));
			}
			// 消耗掉
			consumeItems.consumeAndPush(player, Action.WORLD_MARCH_SPEEDUP);
			int val = player.getEffect().getEffVal(EffType.MARCH_SPEED_ITEM_BACK_628);
			if (val > 0) {
				player.dealMsg(MsgId.WORLD_MARCH_SPEED, new MarchSpeedInvoker(player, req.getItemId()));
			}
		}
		return true;
	}

	/** 行军召回
	 * 
	 * @param protocol
	 * @return */
	@ProtocolHandler(code = HP.code.WORLD_SERVER_CALLBACK_C_VALUE)
	private boolean onMarchCallBack(LMJYProtocol protocol) {
		ILMJYPlayer player = protocol.getPlayer();
		// check param
		WorldMarchServerCallBackReq req = protocol.parseProtocol(WorldMarchServerCallBackReq.getDefaultInstance());
		if (!req.hasMarchId()) {
			player.sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			return false;
		}
		// 检测行军是否存在
		ILMJYWorldMarch march = battleRoom.getPlayerMarch(player.getId(), req.getMarchId());
		if (march == null) {
			player.sendError(protocol.getType(), Status.Error.WORLD_MARCH_NOT_EXISIT);
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
	private boolean onPointMarchCallBack(LMJYProtocol protocol) {
		ILMJYPlayer player = protocol.getPlayer();
		// check param
		WorldPointMarchCallBackReq req = protocol.parseProtocol(WorldPointMarchCallBackReq.getDefaultInstance());
		ILMJYWorldPoint worldPoint = battleRoom.getWorldPoint(req.getX(), req.getY()).orElse(null);
		if (worldPoint == null) {
			player.sendError(protocol.getType(), Status.Error.WORLD_POINT_NOT_EXIST); // 提示目标点没有援助行军
			return false;
		}

		if (worldPoint.getPointType() == WorldPointType.PLAYER) {
			// 获取是否有驻军信息
			ILMJYWorldMarch march = battleRoom.getPointMarches(GameUtil.combineXAndY(req.getX(), req.getY()), WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST, WorldMarchType.ASSISTANCE)
					.stream()
					.filter(ma -> Objects.equals(ma.getPlayerId(), player.getId()))
					.findFirst().orElse(null);
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
	private boolean onWorldSpyStart(LMJYProtocol protocol) {
		ILMJYPlayer player = protocol.getPlayer();
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());

		// 目标点
		ILMJYWorldPoint point = battleRoom.getWorldPoint(req.getPosX(), req.getPosY()).orElse(null);
		if (Objects.isNull(point)) {
			return false;
		}
		if (!checkMarchReq(player, req, protocol.getType(), Collections.emptyList(), point, true)) {
			return false;
		}
		// 消费check
		boolean useGold = req.hasSpyUseGold() && req.getSpyUseGold();
		if (useGold && player instanceof LMJYPlayer) {
			ConsumeItems consume = ConsumeItems.valueOf();
			consume.addConsumeInfo(WorldMarchConstProperty.getInstance().getInvestigationMarchCost(), useGold);
			if (!consume.checkConsume(player, protocol.getType())) {
				return false;
			}
			consume.consumeAndPush(player, Action.WORLD_SPY);
		}

		ILMJYPlayer targetPlayer = (ILMJYPlayer) point;
		// 普通玩家基地，放玩家的ID
		String targetId = targetPlayer.getId();
		// 创建行军
		startMarch(player, targetPlayer, WorldMarchType.SPY, targetId, 0, new EffectParams());
		// 回复协议
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		protocol.response(HawkProtocol.valueOf(HP.code.WORLD_SPY_S_VALUE, builder));

		return true;
	}

	/** 援助玩家（火力支援） */
	@ProtocolHandler(code = HP.code.WORLD_ASSISTANCE_C_VALUE)
	private boolean onWorldAssistanceStart(LMJYProtocol protocol) {
		ILMJYPlayer player = protocol.getPlayer();
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		ILMJYWorldPoint point = battleRoom.getWorldPoint(req.getPosX(), req.getPosY()).orElse(null);

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
		final ILMJYPlayer tarPlayer = (ILMJYPlayer) point;

		// 检查玩家是否在同一联盟
		if (Objects.equals(player.getGuildId(), tarPlayer.getId())) {
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
		if (!ArmyService.getInstance().checkArmyAndMarch(player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			player.sendError(protocol.getType(), Status.Error.WORLD_MARCH_ARMY_COUNT);
			return false;
		}

		startMarch(player, point, WorldMarchType.ASSISTANCE, tarPlayer.getId(), 0, new EffectParams(req, armyList));

		// 回复协议
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		protocol.response(HawkProtocol.valueOf(HP.code.WORLD_ASSISTANCE_S_VALUE, builder));

		return true;

	}

	/** 是否有空闲出征队列
	 * 
	 * @return */
	public boolean isHasFreeMarch(ILMJYPlayer player) {
		// 机器人不受行军数量限制
		if (player.isRobot()) {
			return true;
		}
		List<ILMJYWorldMarch> playerMarches = getParent().getPlayerMarches(player.getId());
		// 行军个数
		int marchCount = 0;
		for (ILMJYWorldMarch march : playerMarches) {
			if (!march.isExtraSpyMarch()) {
				marchCount++;
			}
		}

		if (marchCount >= player.getMaxMarchNum()) {
			return false;
		}
		return true;
	}

	/** 行军出发前通用检查：目标点类型、队列数量、各士兵数量
	 * 
	 * @param req
	 * @param hpCode
	 * @param armyList
	 * @param worldPoint
	 * @return */
	public boolean checkMarchReq(ILMJYPlayer player, WorldMarchReq req, int hpCode, List<ArmyInfo> armyList, ILMJYWorldPoint worldPoint, boolean checkFreeMarch) {
		if (WorldMarchType.MASS != req.getType() && WorldMarchType.MASS_JOIN != req.getType()) {
			if (battleRoom.getStartTime() > HawkTime.getMillisecond()) {
				player.sendError(hpCode, Status.Error.LMJY_PREPARING);
				return false;
			}
		}
		
		boolean hasFreeMarch = isHasFreeMarch(player);
		if (hpCode == HP.code.WORLD_SPY_C_VALUE) {
			if (player.isExtraSpyMarchOpen() && !player.isExtraSypMarchOccupied()) {
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
		player.getData().lockOriginalData();
		return true;
	}

	public ILMJYWorldMarch startMarch(ILMJYPlayer player, ILMJYWorldPoint tPoint, WorldMarchType marchType, String targetId, int waitTime, EffectParams effParams) {
		// 生成行军
		ILMJYWorldMarch march = genMarch(player, tPoint, marchType, targetId, waitTime, effParams);
		List<PlayerHero> OpHero = player.getHeroByCfgId(march.getMarchEntity().getHeroIdList());
		for (PlayerHero hero : OpHero) {
			hero.goMarch(march);
		}
		
		Optional<SuperSoldier> sso = player.getSuperSoldierByCfgId(march.getMarchEntity().getSuperSoldierId());
		if (sso.isPresent()) {
			sso.get().goMarch(march);
		}
		// 行军上需要显示的作用号
		List<Integer> marchShowEffList = new ArrayList<>();
		int[] marchShowEffs = WorldMarchConstProperty.getInstance().getMarchShowEffArray();
		if (marchShowEffs != null) {
			for (int i = 0; i < marchShowEffs.length; i++) {
				int effVal = player.getEffect().getEffVal(EffType.valueOf(marchShowEffs[i]));
				if (effVal > 0) {
					marchShowEffList.add(marchShowEffs[i]);
				}
			}
		}

		if (!marchShowEffList.isEmpty()) {
			march.getMarchEntity().resetEffect(marchShowEffList);
		}

		// 保存行军并推送给对应的玩家
		battleRoom.addMarch(march);

		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WORLD_MARCH_ADD_PUSH_VALUE, march.toBuilder(WorldMarchRelation.SELF));
		player.sendProtocol(protocol);

		march.notifyMarchEvent(MarchEvent.MARCH_ADD); // 通知行军事件

		// 加入行军警报
		march.onMarchStart();

		tPoint.onMarchCome(march);
		player.onMarchStart(march);

		return march;
	}

	/** 发起集结 */
	@ProtocolHandler(code = HP.code.WORLD_MASS_C_VALUE)
	private boolean onWorldMassStart(LMJYProtocol protocol) {
		ILMJYPlayer player = protocol.getPlayer();
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		int waitTime = req.getMassTime();
		int marchType = req.getType().getNumber();
		// if (!WorldMarchConstProperty.getInstance().checkMassWaitTime(waitTime)) {
		// player.sendError(protocol.getType(), Status.Error.MASS_ERR_TIME);
		// return false;
		// }

		// 不是集结类型的行军
		if (!WorldUtil.isMassMarch(marchType)) {
			player.sendError(protocol.getType(), Status.Error.WORLD_MARCH_REQ_TYPE_ERROR);
			return false;
		}

		ILMJYWorldPoint point = battleRoom.getWorldPoint(req.getPosX(), req.getPosY()).orElse(null);
		if (point == null) {
			player.sendError(protocol.getType(), Status.Error.WORLD_POINT_TYPE_ERROR);
			return false;
		}

		// 带兵出征通用检查
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>();
		if (!checkMarchReq(player, req, protocol.getType(), armyList, point, true)) {
			return false;
		}

		// 消耗
		// 检查目标点玩家是否可被集结
		ILMJYPlayer defPlayer = (ILMJYPlayer) point;
		// 目标id
		String targetId = defPlayer.getId();

		// 扣兵
		if (!ArmyService.getInstance().checkArmyAndMarch(player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			player.sendError(protocol.getType(), Status.Error.WORLD_MARCH_ARMY_COUNT);
			return false;
		}

		ILMJYWorldMarch march = startMarch(player, defPlayer, WorldMarchType.MASS, targetId, waitTime, new EffectParams(req, armyList));

		// 回复协议
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		protocol.response(HawkProtocol.valueOf(HP.code.WORLD_MASS_S_VALUE, builder));

		if (player instanceof ILMJYPlayer) {
			ChatParames parames = ChatParames.newBuilder()
					.setPlayer(player)
					.setChatType(ChatType.CHAT_FUBEN)
					.setKey(NoticeCfgId.LMJY_MASSSINGE)
					.addParms(defPlayer.getName())
					.addParms(march.getMarchId())
					.addParms(player.getX())
					.addParms(player.getY())
					.addParms(0).addParms("")
					.build();
			battleRoom.addWorldBroadcastMsg(parames);
		}
		return true;
	}

	/** 加入集结 */
	@ProtocolHandler(code = HP.code.WORLD_MASS_JOIN_C_VALUE)
	private boolean onWorldMassJoinStart(LMJYProtocol protocol) {
		ILMJYPlayer player = protocol.getPlayer();
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		String massMarchId = req.getMarchId();
		ILMJYWorldMarch worldMarch = battleRoom.getMarch(massMarchId);
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
		ILMJYPlayer leader = worldMarch.getParent();
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

		ILMJYMassMarch massMarch = (ILMJYMassMarch) worldMarch;
		// 集结行军类型和参与集结行军类型不一致
		int joinMarchType = massMarch.getJoinMassType().getNumber();
		if (marchType != joinMarchType) {
			player.sendError(protocol.getType(), Status.Error.MASS_JOIN_TYPE_ERROR);
			return false;
		}

		// 已经有加入这支部队的集结
		Set<ILMJYWorldMarch> massJoinMarchs = massMarch.getMassJoinMarchs(false);
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
		if (!ArmyService.getInstance().checkArmyAndMarch(player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			player.sendError(protocol.getType(), Status.Error.WORLD_MARCH_ARMY_COUNT);
			return false;
		}

		// 回复协议
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		protocol.response(HawkProtocol.valueOf(HP.code.WORLD_MASS_JOIN_S_VALUE, builder));

		// 出发
		startMarch(player, leader, req.getType(), massMarchId, 0, new EffectParams(req, armyList));

		return true;
	}

	/** 生成一个行军对象 */
	public ILMJYWorldMarch genMarch(ILMJYPlayer player, ILMJYWorldPoint tPoint, WorldMarchType marchType, String targetId, int waitTime, EffectParams effParams) {
		long startTime = HawkTime.getMillisecond();

		LMJYMarchEntity march = new LMJYMarchEntity();
		march.setArmys(effParams.getArmys());
		march.setStartTime(startTime);
		march.setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_VALUE);
		march.setOrigionId(player.getPointId());
		march.setTerminalId(tPoint.getPointId());
		march.setAlarmPointId(tPoint.getPointId());
		march.setTargetId(targetId);
		march.setMarchType(marchType.getNumber());
		march.setSuperSoldierId(effParams.getSuperSoliderId());
		march.setPlayerId(player.getId());
		march.setPlayerName(player.getName());
		if (Objects.nonNull(effParams.getHeroIds())) {
			march.setHeroIdList(effParams.getHeroIds());
		}

		// 使用额外侦查行军队列
		if (marchType == WorldMarchType.SPY && !player.isExtraSypMarchOccupied() && player.isExtraSpyMarchOpen()) {
			march.setExtraSpyMarch(true);
		}
		
		if (effParams.getArmourSuit() != null) {
			int armourSuit = effParams.getArmourSuit().getNumber();
			if (armourSuit > 0 && armourSuit <= player.getEntity().getArmourSuitCount()) {
				march.setArmourSuit(armourSuit);
			}
		}
		MechaCoreSuitType mechaSuit = effParams.getMechacoreSuit();
		if (mechaSuit != null && player.getPlayerMechaCore().isSuitUnlocked(mechaSuit.getNumber())) {
			march.setMechacoreSuit(mechaSuit.getNumber());
		} else {
			march.setMechacoreSuit(player.getPlayerMechaCore().getWorkSuit());
		}
		
		if (effParams.getDressList().size() > 0) {
			march.setDressList(effParams.getDressList());
		}
		march.setTalentType(effParams.getTalent());
		march.setSuperLab(effParams.getSuperLab());
		
		march.setTargetPointType(tPoint.getPointType().getNumber());
		// 把个人编队记录下来
		if (effParams.getWorldmarchReq() != null) {
			march.setFormation(effParams.getWorldmarchReq().getFormation());
		}
		// 超武
		int atkId = effParams.getManhattanAtkSwId();
		if (atkId > 0) {
			march.setManhattanAtkSwId(atkId);
		}
		int defId = effParams.getManhattanDefSwId();
		if (defId > 0) {
			march.setManhattanDefSwId(defId);
		}
		ILMJYWorldMarch iWorldMarch;
		// TODO 有时间封装一下 组装
		switch (marchType) {
		case ATTACK_PLAYER:
			iWorldMarch = new LMJYAttackPlayerMarch(player);
			break;
		case SPY:
			iWorldMarch = new LMJYSpyMarch(player);
			break;
		case ASSISTANCE:
			iWorldMarch = new LMJYAssistanceSingleMarch(player);
			break;
		case MASS:
			iWorldMarch = new LMJYMassSingleMarch(player);
			break;
		case MASS_JOIN:
			iWorldMarch = new LMJYMassJoinSingleMarch(player);
			break;
		default:
			throw new UnsupportedOperationException("dont know what march it is!!!!!!!");
		}
		iWorldMarch.setMarchEntity(march);
		// marchId设置放在前面
		march.setMarchId(HawkOSOperator.randomUUID());

		// 行军时间
		long needTime = iWorldMarch.getMarchNeedTime();
		march.setEndTime(startTime + needTime);
		march.setMarchJourneyTime((int) needTime);

		// 集结等待时间
		if (waitTime > 0) {
			waitTime *= 1000;

			// 作用号618：集结所需时间减少 -> 实际集结时间 = 基础集结时间 * （1 - 作用值/10000）；向上取整；不得小于0
			waitTime *= 1 - player.getEffect().getEffVal(EffType.GUILD_MASS_TIME_REDUCE_PER, effParams) * GsConst.EFF_PER;
			waitTime = waitTime > 0 ? waitTime : 0;

			march.setMarchStatus(WorldMarchStatus.MARCH_STATUS_WAITING_VALUE);
			march.setMassReadyTime(march.getStartTime());
			march.setStartTime(march.getStartTime() + waitTime);
			march.setEndTime(march.getEndTime() + waitTime);
		}

		return iWorldMarch;
	}

	public boolean checkMarchDressReq(ILMJYPlayer player, List<Integer> marchDressList) {
		try {
			PlayerMarchModule marckModule = player.getModule(ModuleType.WORLD_MARCH_MODULE);
			if (marckModule != null && !marckModule.checkMarchDressReq(marchDressList)) {
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
	public boolean onChatCardInfoReq(LMJYProtocol protocol) {
		ILMJYPlayer player = protocol.getPlayer();
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

	private Activity.ChatMassCardInfo.Builder getChatMassCardInfo(String marchId, ILMJYPlayer viewer) {
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

			ILMJYWorldMarch march = viewer.getParent().getMarch(marchId);
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
