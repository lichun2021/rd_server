package com.hawk.game.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;

import com.hawk.game.player.manhattan.PlayerManhattanModule;
import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.type.impl.cakeShare.CakeShareActivity;
import com.hawk.activity.type.impl.cakeShare.cfg.CakeShareKVCfg;
import com.hawk.activity.type.impl.drogenBoatFestival.gift.DragonBoatGiftActivity;
import com.hawk.activity.type.impl.drogenBoatFestival.gift.cfg.DragonBoatGiftKVCfg;
import com.hawk.activity.type.impl.overlordBlessing.OverlordBlessingActivity;
import com.hawk.activity.type.impl.planetexploration.PlanetExploreActivity;
import com.hawk.activity.type.impl.planetexploration.cfg.PlanetExploreKVCfg;
import com.hawk.game.GsConfig;
import com.hawk.game.city.CityManager;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.CrossConstCfg;
import com.hawk.game.config.DressCfg;
import com.hawk.game.config.FoggyFortressCfg;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.config.MarchEmoticonCfg;
import com.hawk.game.config.MarchEmoticonProperty;
import com.hawk.game.config.NationConstCfg;
import com.hawk.game.config.NationConstructionBaseCfg;
import com.hawk.game.config.PushLangZhCNCfg;
import com.hawk.game.config.ResTreasureCfg;
import com.hawk.game.config.ShopCfg;
import com.hawk.game.config.TreasureHuntResCfg;
import com.hawk.game.config.WorldChristmasWarBossCfg;
import com.hawk.game.config.WorldEnemyCfg;
import com.hawk.game.config.WorldGundamCfg;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.config.WorldMarchConstProperty;
import com.hawk.game.config.WorldNianCfg;
import com.hawk.game.config.WorldPylonCfg;
import com.hawk.game.config.WorldResourceCfg;
import com.hawk.game.config.WorldStrongpointCfg;
import com.hawk.game.crossactivity.CActivityInfo;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.crossactivity.resourcespree.ResourceSpreeBoxWorldPoint;
import com.hawk.game.crossfortress.CrossFortressService;
import com.hawk.game.crossfortress.IFortress;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.DressEntity;
import com.hawk.game.entity.NationBuildQuestEntity;
import com.hawk.game.entity.PlayerMonsterEntity;
import com.hawk.game.entity.item.DressItem;
import com.hawk.game.entity.item.GuildFormationCell;
import com.hawk.game.entity.item.GuildFormationObj;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.guild.guildrank.GuildRankMgr;
import com.hawk.game.guild.manor.AbstractBuildable;
import com.hawk.game.guild.manor.GuildManorObj;
import com.hawk.game.guild.manor.ManorMarchEnum;
import com.hawk.game.guild.manor.building.GuildDragonTrap;
import com.hawk.game.invoker.MarchSpeedInvoker;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.item.MarchSpeedItem;
import com.hawk.game.lianmengxzq.XZQService;
import com.hawk.game.lianmengxzq.worldpoint.XZQWorldPoint;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.lianmengtaiboliya.TBLYConst.TBLYState;
import com.hawk.game.module.spacemecha.SpaceMechaService;
import com.hawk.game.module.spacemecha.module.PlayerSpaceMechaModule;
import com.hawk.game.module.spacemecha.worldpoint.SpaceWorldPoint;
import com.hawk.game.module.spacemecha.worldpoint.StrongHoldWorldPoint;
import com.hawk.game.msg.AtkPlayerAfterWarMsg;
import com.hawk.game.msg.DefPlayerAfterWarMsg;
import com.hawk.game.msg.GuildQuitMsg;
import com.hawk.game.nation.NationService;
import com.hawk.game.nation.construction.NationConstruction;
import com.hawk.game.nation.construction.model.NationalBuildQuestModel;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.skill.talent.Skill10104;
import com.hawk.game.player.skill.talent.TalentSkillContext;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.president.PresidentFightService;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.Armour.ArmourSuitType;
import com.hawk.game.protocol.Army.ArmyChangeCause;
import com.hawk.game.protocol.Army.ArmySoldierPB;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Const.PushMsgType;
import com.hawk.game.protocol.Const.TerritoryType;
import com.hawk.game.protocol.Consume.ConsumeItem;
import com.hawk.game.protocol.Consume.SyncAttrInfo;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.GuildWar.PushGuildWarBuyItems;
import com.hawk.game.protocol.GuildWar.PushQuarteredMarchBuyItems;
import com.hawk.game.protocol.GuildWar.PushQuarteredMarchType;
import com.hawk.game.protocol.GuildWar.WorldMassMarchBuyExtraItemsReq;
import com.hawk.game.protocol.GuildWar.WorldQuarteredMarchBuyExtraItemsReq;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.MassFormation.FormationOperType;
import com.hawk.game.protocol.MassFormation.MassFormationIndex;
import com.hawk.game.protocol.National.NationBuildQuestType;
import com.hawk.game.protocol.National.NationRedDot;
import com.hawk.game.protocol.National.NationbuildingType;
import com.hawk.game.protocol.Player.CrossPlayerStruct;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponPeriod;
import com.hawk.game.protocol.World.FetchInviewWorldPoint;
import com.hawk.game.protocol.World.InviteMassReq;
import com.hawk.game.protocol.World.KickSnowballDirection;
import com.hawk.game.protocol.World.MarchArmyDetailInfo;
import com.hawk.game.protocol.World.MarchArmyDetailReq;
import com.hawk.game.protocol.World.MarchArmyDetailResp;
import com.hawk.game.protocol.World.MassCardInfo;
import com.hawk.game.protocol.World.MassCardInfo.Builder;
import com.hawk.game.protocol.World.MassCardInfoReq;
import com.hawk.game.protocol.World.MassCardInfoResp;
import com.hawk.game.protocol.World.MonsterType;
import com.hawk.game.protocol.World.SnowballAttackReq;
import com.hawk.game.protocol.World.UseMarchEmoticonReq;
import com.hawk.game.protocol.World.WorldMarchLoginPush;
import com.hawk.game.protocol.World.WorldMarchPB;
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
import com.hawk.game.rank.RankService;
import com.hawk.game.service.ArmyService;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.PushService;
import com.hawk.game.service.WarFlagService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.flag.FlagCollection;
import com.hawk.game.service.mail.GuildMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.service.mail.YuriMailService;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventGenOldMonsterMarch;
import com.hawk.game.service.mssion.event.EventKillSolider;
import com.hawk.game.service.warFlag.IFlag;
import com.hawk.game.superweapon.SuperWeaponService;
import com.hawk.game.superweapon.weapon.IWeapon;
import com.hawk.game.util.AlgorithmPoint;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.ScoreType;
import com.hawk.game.util.GsConst.SysFunctionModuleId;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.WorldScene;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.march.impl.AttackPlayerMarch;
import com.hawk.game.world.march.submarch.IPassiveAlarmTriggerMarch;
import com.hawk.game.world.march.submarch.IReportPushMarch;
import com.hawk.game.world.march.submarch.MassMarch;
import com.hawk.game.world.object.CakeShareInfo;
import com.hawk.game.world.object.DragonBoatInfo;
import com.hawk.game.world.object.MapBlock;
import com.hawk.game.world.object.Point;
import com.hawk.game.world.service.WorldChristmasWarService;
import com.hawk.game.world.service.WorldFoggyFortressService;
import com.hawk.game.world.service.WorldGundamService;
import com.hawk.game.world.service.WorldNianService;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.service.WorldRobotService;
import com.hawk.game.world.service.WorldSnowballService;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;
import com.hawk.game.world.thread.tasks.MarchCallbackTask;
import com.hawk.game.world.thread.tasks.MarchCallbackWithPointTask;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.log.LogConst.ArmyChangeReason;
import com.hawk.log.LogConst.ArmySection;
import com.hawk.log.Source;
import com.hawk.sdk.msdk.entity.PayItemInfo;

/**
 * 玩家登陆模块
 *
 * @author hawk
 */
public class PlayerMarchModule extends PlayerModule {

	static Logger logger = LoggerFactory.getLogger("Server");

	/**
	 * 玩家可采集、掠夺的资源对应大本等级 默认可以掠夺金矿和石油
	 */
	private final int[] RES_TYPE = GsConst.RES_TYPE;
	/**
	 * 前一次tick的时间
	 */
	private long lastTickTime;

	/**
	 * 构造函数
	 *
	 * @param player
	 */
	public PlayerMarchModule(Player player) {
		super(player);
	}

	/**
	 * 玩家组装完成, 主要用来后期数据同步
	 */
	@Override
	protected boolean onPlayerAssemble() {
		return true;
	}

	/**
	 * 玩家上线处理, 数据同步
	 *
	 * @return
	 */
	@Override
	protected boolean onPlayerLogin() {
		// 组装玩家自己的行军PB数据
		WorldMarchLoginPush.Builder builder = WorldMarchLoginPush.newBuilder();
		BlockingQueue<IWorldMarch> marchs = WorldMarchService.getInstance().getPlayerMarch(player.getId());
		WorldMarchPB.Builder marchBuilder = WorldMarchPB.newBuilder();
		for (IWorldMarch worldMarch : marchs) {
			WorldMarch march = worldMarch.getMarchEntity();
			builder.addMarchs(march.toBuilder(marchBuilder.clear(), WorldMarchRelation.SELF).build());
			
			if (march.getMarchType() == WorldMarchType.MASS_JOIN_VALUE) {
				IWorldMarch massMach = WorldMarchService.getInstance().getMarch(march.getTargetId());
				// 这里添加个空判断。 存在队长解散后，队员找不到队长行军。所以这里可能问空。
				if (massMach != null) {
					builder.addMarchs(massMach.getMarchEntity().toBuilder(marchBuilder.clear(), WorldMarchRelation.TEAM_LEADER).build());
				}
			}
			
			if(worldMarch instanceof IPassiveAlarmTriggerMarch){
				((IPassiveAlarmTriggerMarch) worldMarch).pullAttackReport(player.getId());
			}
		}

		// 通知客户端
		player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MARCHS_PUSH, builder));

		// 整理别的玩家向自己行军的信息，满足条件的要向对方发送行军报告
		BlockingQueue<IWorldMarch> worldMarchs = WorldMarchService.getInstance().getPlayerPassiveMarch(player.getId());
		worldMarchs.stream()
				.filter(m -> m instanceof IReportPushMarch)
				.map(m -> (IReportPushMarch) m)
				.forEach(m -> m.pushAttackReport(player.getId()));
		
		
		lastTickTime = HawkApp.getInstance().getCurrentTime();
		return true;
	}

	/**
	 * tick
	 */
	public boolean onTick() {
		long now = HawkApp.getInstance().getCurrentTime();
		if (now - lastTickTime < 1000) {
			return true;
		}
		
		lastTickTime = now;
		checkSuperVipSkinToMassMarch();
		return true;
	}
	
	/**
	 * 检测集结行军
	 */
	public void checkSuperVipSkinToMassMarch() {
		try {
			if (!player.getSuperVipObject().isSuperVipOpen() || player.getSuperVipSkinActivatedLevel() <= 0) {
				return;
			}
			List<IWorldMarch> marchs = WorldMarchService.getInstance().getPlayerMassMarch(player.getId());
			WorldPoint worldPoint = WorldPlayerService.getInstance().getPlayerWorldPoint(player.getId());
			OptionalLong optional = marchs.stream().filter(e -> e.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE)
					.mapToLong(e -> e.getMarchEntity().getStartTime()).max();
			long maxTime = Math.max(0, optional.orElse(0));
			if (worldPoint != null && maxTime != worldPoint.getSuperVipSkinEffEndTime()) {
				worldPoint.setSuperVipSkinEffEndTime(maxTime);
				worldPoint.setSuperVipSkinLevel(player.getSuperVipSkinActivatedLevel());
				WorldPointService.getInstance().getWorldScene().update(worldPoint.getAoiObjId());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 异常检测及修复
	 */
	public void exceptionCheckAndFix() {
		armysCheckAndFix();
	}
	
	/**
	 * 部队异常检测修复：玩家没有出征中的部队，并且有出征状态的armyEntity。则修复
	 */
	private void armysCheckAndFix() {
		if (GsConfig.getInstance().isRobotMode() || GsConfig.getInstance().isDebug()) {
			return;
		}
		
		// 玩家出征部队数量
		int marchCount = WorldMarchService.getInstance().getPlayerMarchCount(player.getId());
		if (marchCount > 0) {
			return;
		}

		List<Integer> armyIds = new ArrayList<Integer>();
		for (ArmyEntity armyEntity : player.getData().getArmyEntities()) {
			// 出征中的army数量
			int marchArmyCount = armyEntity.getMarch();
			if (marchArmyCount <= 0) {
				continue;
			}
			
			int armyId = armyEntity.getArmyId();
			armyIds.add(armyId);
			
			armyEntity.clearMarch();
			armyEntity.addFree(marchArmyCount);
			LogUtil.logArmyChange(player, armyEntity, marchArmyCount, ArmySection.FREE, ArmyChangeReason.MARCH_FIX);
			logger.error("armysCheckAndFix, playerId:{}, armyId:{}, marchArmyCount:{}, armyFree:{}", armyEntity.getPlayerId(), armyEntity.getId(), marchArmyCount, armyEntity.getFree());
		}

		if (!armyIds.isEmpty()) {
			player.getPush().syncArmyInfo(ArmyChangeCause.MARCH_BACK, armyIds.toArray(new Integer[armyIds.size()]));
		}
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
			logger.error("world march callback failed, marchId is required, playerId: {}", player.getId());
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return false;
		}

		// 检测行军是否存在
		IWorldMarch march = WorldMarchService.getInstance().getMarch(req.getMarchId());
		if (march == null) {
			logger.error("world march callback failed, march not exist, playerId: {}, marchId: {}", player.getId(), req.getMarchId());
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_NOT_EXISIT);
			return false;
		}

		// 不是自己的行军不能召回
		if (!player.getId().equals(march.getPlayerId())) {
			logger.error("world march callback failed, not own march, playerId:{}, marchId:{}, marchPlayerId:{}", player.getId(), req.getMarchId(), march.getPlayerId());
			return false;
		}
		
		// 返回途中不能召回
		if (march.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
			logger.error("world march callback failed, march cannot callback again in returning, playerId: {}, marchId: {}",player.getId(), req.getMarchId());
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_RETURNING);
			return false;
		}

		// 首都驻军的召回
		if ((march.isPresidentMarch() || march.isManorMarch())
				&& (march.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE || march.isManorMarchReachStatus())) {
			WorldThreadScheduler.getInstance().postWorldTask(new MarchCallbackWithPointTask(march, -1, -1));
			WorldMarchServerCallBackResp.Builder builder = WorldMarchServerCallBackResp.newBuilder();
			builder.setResult(true);
			protocol.response(HawkProtocol.valueOf(HP.code.WORLD_SERVER_CALLBACK_S, builder));
			return true;
		}

		// 集结类型的行军不能召回
		if (march.isMassMarch()) {
			logger.error("world march callback failed, mass march cannot callback, playerId: {}, march: {}", player.getId(), march);
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_MASS_CANNOT_CALLBACK);
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
				if (shopCfg == null) {
					logger.error("world march callback failed, item config error, playerId: {}, marchId: {}",
							player.getId(), march.getMarchId());
					sendError(protocol.getType(), Status.Error.ITEM_NOT_FOUND);
					return false;
				}
				consumeItems.addConsumeInfo(shopCfg.getPriceItemInfo(), false);
			}

			// 消耗掉
			if (!consumeItems.checkConsume(player, protocol.getType())) {
				return false;
			}
			consumeItems.consumeAndPush(player, Action.WORLD_MARCH_CALLBACK);
		}

		if (march != null && !HawkOSOperator.isEmptyString(march.getMarchId())) {
			logger.info("marchCallBack, marchId:{}", march.getMarchId());
		}
		
		logger.info("world march callback begin marchData:{}", march);
		long currentTime = HawkTime.getMillisecond();
		// 回复协议
		WorldMarchServerCallBackResp.Builder builder = WorldMarchServerCallBackResp.newBuilder();
		builder.setResult(true);

		// 不同类型召回不同处理
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(march.getMarchEntity().getTerminalX(), march.getMarchEntity().getTerminalY());
		//在行军召回的时候，每个行军可各自处理自己的召回
		WorldThreadScheduler.getInstance().postWorldTask(new MarchCallbackTask(march, currentTime, worldPoint));
		logger.info("world march callback success marchData:{}", march);

		// 回复
		protocol.response(HawkProtocol.valueOf(HP.code.WORLD_SERVER_CALLBACK_S, builder));

		// 士兵援助行军变化通知
		if (march.getMarchType() == WorldMarchType.ASSISTANCE) {
			Player targetPlayer = GlobalData.getInstance().makesurePlayer(march.getMarchEntity().getTargetId());
			WorldMarchService.getInstance().notifyAssistanceMarchChange(targetPlayer, march.getMarchId());
		}
		
		return true;

	}

	/**
	 * 行军召回,一定是已经在停留在目标点的行军
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.WORLD_POINT_MARCH_CALLBACK_C_VALUE)
	private boolean onPointMarchCallBack(HawkProtocol protocol) {
		// check param
		WorldPointMarchCallBackReq req = protocol.parseProtocol(WorldPointMarchCallBackReq.getDefaultInstance());
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(req.getX(), req.getY());
		if (worldPoint == null) {
			sendError(protocol.getType(), Status.Error.WORLD_POINT_NOT_EXIST); // 提示目标点没有援助行军
			return false;
		}

		if (worldPoint.getPointType() == WorldPointType.PLAYER_VALUE) {

			// 援助类型的行军
			if (HawkOSOperator.isEmptyString(worldPoint.getPlayerId())) {
				sendError(protocol.getType(), Status.Error.POINT_NO_MARCH_CALL_BACK); // 提示目标点没有援助行军
				return false;
			}

			// 获取是否有驻军信息
			IWorldMarch march = WorldMarchService.getInstance().getAssistanceMarch(player.getId(), worldPoint.getPlayerId());
			if (march == null) {
				sendError(protocol.getType(), Status.Error.POINT_NO_MARCH_CALL_BACK);// 提示目标点没有援助行军
				return false;
			}
			WorldThreadScheduler.getInstance().postWorldTask(new MarchCallbackWithPointTask(march, req.getX(), req.getY()));
			
			// 士兵援助行军变化通知
			Player targetPlayer = GlobalData.getInstance().makesurePlayer(march.getMarchEntity().getTargetId());
			WorldMarchService.getInstance().notifyAssistanceMarchChange(targetPlayer, march.getMarchId());
		} else if (worldPoint.getPointType() == WorldPointType.GUILD_TERRITORY_VALUE) {
			String playerId = req.getPlayerId();
			if (HawkOSOperator.isEmptyString(playerId)) {
				sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
				return false;
			}

			int pointId = GameUtil.combineXAndY(req.getX(), req.getY());
			List<IWorldMarch> marchs = GuildManorService.getInstance().getManorBuildMarch(pointId);
			
			List<IWorldMarch> callbckMarchs = new ArrayList<IWorldMarch>();
			for (IWorldMarch march : marchs) {
				if (march.getPlayerId().equals(playerId)) {
					callbckMarchs.add(march);
				}
			}
			WorldThreadScheduler.getInstance().postWorldTask(new MarchCallbackWithPointTask(callbckMarchs, req.getX(), req.getY()));
		} else if (worldPoint.getPointType() == WorldPointType.KING_PALACE_VALUE) {
			BlockingDeque<String> presidentMarchs = WorldMarchService.getInstance().getPresidentMarchs();
			BlockingQueue<IWorldMarch> playerMarch = WorldMarchService.getInstance().getPlayerMarch(player.getId());
			
			List<IWorldMarch> callbckMarchs = new ArrayList<IWorldMarch>();
			for (IWorldMarch march : playerMarch) {
				if (!presidentMarchs.contains(march.getMarchId())) {
					continue;
				}
				callbckMarchs.add(march);
			}
			WorldThreadScheduler.getInstance().postWorldTask(new MarchCallbackWithPointTask(callbckMarchs, req.getX(), req.getY()));
		} else if (worldPoint.getPointType() == WorldPointType.CAPITAL_TOWER_VALUE) {
			BlockingDeque<String> presidentMarchs = WorldMarchService.getInstance().getPresidentTowerMarchs(worldPoint.getId());
			BlockingQueue<IWorldMarch> playerMarch = WorldMarchService.getInstance().getPlayerMarch(player.getId());
			
			List<IWorldMarch> callbckMarchs = new ArrayList<IWorldMarch>();
			for (IWorldMarch march : playerMarch) {
				if (!presidentMarchs.contains(march.getMarchId())) {
					continue;
				}
				callbckMarchs.add(march);
			}
			WorldThreadScheduler.getInstance().postWorldTask(new MarchCallbackWithPointTask(callbckMarchs, req.getX(), req.getY()));
		} else if (worldPoint.getPointType() == WorldPointType.STRONG_POINT_VALUE) {
			IWorldMarch march = WorldMarchService.getInstance().getMarch(worldPoint.getMarchId());
			if (march != null && march.getPlayerId().equals(player.getId())) {
				WorldThreadScheduler.getInstance().postWorldTask(new MarchCallbackTask(march, HawkTime.getMillisecond(), worldPoint));
			}
		} else if (worldPoint.getPointType() == WorldPointType.SUPER_WEAPON_VALUE) {
			BlockingDeque<String> superWeaponMarchs = WorldMarchService.getInstance().getSuperWeaponMarchs(worldPoint.getId());
			BlockingQueue<IWorldMarch> playerMarch = WorldMarchService.getInstance().getPlayerMarch(player.getId());
			
			List<IWorldMarch> callbckMarchs = new ArrayList<IWorldMarch>();
			for (IWorldMarch march : playerMarch) {
				if (!superWeaponMarchs.contains(march.getMarchId())) {
					continue;
				}
				callbckMarchs.add(march);
			}
			WorldThreadScheduler.getInstance().postWorldTask(new MarchCallbackWithPointTask(callbckMarchs, req.getX(), req.getY()));
		} else if (worldPoint.getPointType() == WorldPointType.XIAO_ZHAN_QU_VALUE) {
			BlockingDeque<String> superWeaponMarchs = WorldMarchService.getInstance().getXZQMarchs(worldPoint.getId());
			BlockingQueue<IWorldMarch> playerMarch = WorldMarchService.getInstance().getPlayerMarch(player.getId());
			
			List<IWorldMarch> callbckMarchs = new ArrayList<IWorldMarch>();
			for (IWorldMarch march : playerMarch) {
				if (!superWeaponMarchs.contains(march.getMarchId())) {
					continue;
				}
				callbckMarchs.add(march);
			}
			WorldThreadScheduler.getInstance().postWorldTask(new MarchCallbackWithPointTask(callbckMarchs, req.getX(), req.getY()));
		}  else if (worldPoint.getPointType() == WorldPointType.CROSS_FORTRESS_VALUE) {
			BlockingDeque<String> superWeaponMarchs = WorldMarchService.getInstance().getFortressMarchs(worldPoint.getId());
			BlockingQueue<IWorldMarch> playerMarch = WorldMarchService.getInstance().getPlayerMarch(player.getId());
			
			List<IWorldMarch> callbckMarchs = new ArrayList<IWorldMarch>();
			for (IWorldMarch march : playerMarch) {
				if (!superWeaponMarchs.contains(march.getMarchId())) {
					continue;
				}
				callbckMarchs.add(march);
			}
			WorldThreadScheduler.getInstance().postWorldTask(new MarchCallbackWithPointTask(callbckMarchs, req.getX(), req.getY()));
		}  else if (worldPoint.getPointType() == WorldPointType.FOGGY_FORTRESS_VALUE) {
			BlockingDeque<String> fortressMarchs = WorldMarchService.getInstance().getFortressMarchs(worldPoint.getId());
			BlockingQueue<IWorldMarch> playerMarch = WorldMarchService.getInstance().getPlayerMarch(player.getId());
			
			List<IWorldMarch> callbckMarchs = new ArrayList<IWorldMarch>();
			for (IWorldMarch march : playerMarch) {
				if (!fortressMarchs.contains(march.getMarchId())) {
					continue;
				}
				callbckMarchs.add(march);
			}
			WorldThreadScheduler.getInstance().postWorldTask(new MarchCallbackWithPointTask(callbckMarchs, req.getX(), req.getY()));
		} else if (worldPoint.getPointType() == WorldPointType.WAR_FLAG_POINT_VALUE) {
			IFlag flag = FlagCollection.getInstance().getFlag(worldPoint.getGuildBuildId());
			List<IWorldMarch> marchs = WarFlagService.getInstance().getFlagPointMarch(flag);
			BlockingQueue<IWorldMarch> playerMarch = WorldMarchService.getInstance().getPlayerMarch(player.getId());
			
			List<IWorldMarch> callbckMarchs = new ArrayList<IWorldMarch>();
			for (IWorldMarch march : playerMarch) {
				if (!marchs.contains(march)) {
					continue;
				}
				callbckMarchs.add(march);
			}
			WorldThreadScheduler.getInstance().postWorldTask(new MarchCallbackWithPointTask(callbckMarchs, req.getX(), req.getY()));
		} else if (worldPoint.getPointType() == WorldPointType.SPACE_MECHA_MAIN_VALUE || worldPoint.getPointType() == WorldPointType.SPACE_MECHA_SLAVE_VALUE) {
			SpaceWorldPoint spacePoint = (SpaceWorldPoint) worldPoint;
			BlockingDeque<String> spaceDefMarchs = spacePoint.getDefMarchs();
			BlockingQueue<IWorldMarch> playerMarch = WorldMarchService.getInstance().getPlayerMarch(player.getId());
			
			List<IWorldMarch> callbckMarchs = new ArrayList<IWorldMarch>();
			for (IWorldMarch march : playerMarch) {
				if (!spaceDefMarchs.contains(march.getMarchId())) {
					continue;
				}
				callbckMarchs.add(march);
			}
			WorldThreadScheduler.getInstance().postWorldTask(new MarchCallbackWithPointTask(callbckMarchs, req.getX(), req.getY()));
		} else {
			String playerId = req.getPlayerId();
			if (HawkOSOperator.isEmptyString(playerId)) {
				sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
				return false;
			}
			Collection<IWorldMarch> worldPointMarchs = WorldMarchService.getInstance().getWorldPointMarch(req.getX(), req.getY());
			
			List<IWorldMarch> marchList = new ArrayList<IWorldMarch>();
			for (IWorldMarch march : worldPointMarchs) {
				if (!march.isReachAndStopMarch()) {
					continue;
				}
				if (!march.getPlayerId().equals(playerId)) {
					continue;
				}
				marchList.add(march);
			}
			
			WorldThreadScheduler.getInstance().postWorldTask(new MarchCallbackTask(marchList, HawkTime.getMillisecond(), worldPoint));
		}
		
		player.responseSuccess(protocol.getType());
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
			logger.error("march speed failed, playerId: {}, marchId: {}, marchId and item is required", player.getId(), req.hasMarchId() ? req.getMarchId() : "");
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return false;
		}
		
		// 道具id非法
		if (req.getItemId() != Const.ItemId.ITEM_WORLD_MARCH_SPEEDUPH_VALUE
				&& req.getItemId() != Const.ItemId.ITEM_WORLD_MARCH_SPEEDUPL_VALUE) {
			logger.error("march speed failed, consume item invalid, playerId: {}, marchId: {}", player.getId(),
					req.getMarchId());
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return false;
		}

		// 行军信息
		IWorldMarch march = WorldMarchService.getInstance().getPlayerMarch(player.getId(), req.getMarchId());
		if (march == null) {
			// 不是自己的行军，判断是否是队伍其他人的行军
			march = WorldMarchService.getInstance().getMarch(req.getMarchId());
			if (march == null) {
				logger.error("march speed failed, march not exist, playerId: {}, marchId: {}", player.getId(),
						req.getMarchId());
				sendError(protocol.getType(), Status.Error.WORLD_MARCH_NOT_EXISIT);
				return false;
			}
		}

		// 加入集结出征，给队长行军加速
		if (march.isMassJoinMarch() && march.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_JOIN_MARCH_VALUE) {
			march = WorldMarchService.getInstance().getMarch(march.getMarchEntity().getTargetId());
		}

		// 不在行进或者回程途中，加速无效
		if (!(march.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE
				|| march.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE)) {
			logger.error("march speed failed, march status can not speed, playerId: {}, march: {}", player.getId(), march);
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_NOT_EXISIT);
			return false;
		}

		// 行军状态发生了变化
		if (req.hasStatus() && req.getStatus() != WorldMarchStatus.valueOf(march.getMarchEntity().getMarchStatus())) {
			logger.error("march speed failed, march status changed, playerId: {}, march: {}", player.getId(), march);
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_STATUS_CHANGED);
			return false;
		}
		
		// 检测国家行军
		if (!canNationMarchSpeedUp(march, protocol)) {
			return false;
		}
		
		// 相加召唤相关的功能点行军，不让加速
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(march.getTerminalId());
		if (SpaceMechaService.getInstance().isSpaceMechaPoint(point)) {
			sendError(protocol.getType(), Status.Error.SPACE_MECHA_MARCH_SPEED_ERROR);
			return false;
		}
		
		// 召回消耗道具
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		// 加速消耗道具，记录进march 用于战斗异常返还道具
		List<MarchSpeedItem> speedConsume = new ArrayList<MarchSpeedItem>();

		if (player.getData().getItemNumByItemId(req.getItemId()) > 0) {
			consumeItems.addItemConsume(req.getItemId(), 1);
			speedConsume.add(new MarchSpeedItem(ItemType.TOOL_VALUE, req.getItemId(), 1, player.getId()));
		} else {
			ShopCfg shopCfg = ShopCfg.getShopCfgByItemId(req.getItemId());
			if (shopCfg == null) {
				logger.error("march speed failed, item config error, playerId: {}, marchId: {}", player.getId(),
						march.getMarchId());
				sendError(protocol.getType(), Status.Error.ITEM_NOT_FOUND);
				return false;
			}
			consumeItems.addConsumeInfo(shopCfg.getPriceItemInfo(), false);
		}

		// 判断消耗是否满足
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			return false;
		}
		
		int val = player.getEffect().getEffVal(EffType.MARCH_SPEED_ITEM_BACK_628);
		if (val > 0) {
			player.dealMsg(MsgId.WORLD_MARCH_SPEED, new MarchSpeedInvoker(player, req.getItemId()));
		}

		if (consumeItems.getBuilder().hasAttrInfo() && consumeItems.getBuilder().getAttrInfo().getDiamond() > 0) {
			ShopCfg shopCfg = ShopCfg.getShopCfgByItemId(req.getItemId());
			ItemInfo priceItem =  shopCfg.getPriceItemInfo();
			consumeItems.addPayItemInfo(new PayItemInfo(String.valueOf(req.getItemId()), (int)priceItem.getCount(), 1));
		}
		// 消耗掉
		consumeItems.consumeAndPush(player, Action.WORLD_MARCH_SPEEDUP);

		// caculate
		ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, req.getItemId());
		int timeReducePercent = itemCfg.getNum();
		int speedUpTimes = itemCfg.getMarchSpeedMultiple();

		logger.info("march speed success timeReducePercent:{},speedUpTimes:{}", timeReducePercent, speedUpTimes);

		// 加速
		String speedUpMarchId = march.getMarchId();
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.WORLD_MARCH_SPEED) {
			@Override
			public boolean onInvoke() {
				WorldMarchService.getInstance().marchSpeedUp(speedUpMarchId, timeReducePercent, speedUpTimes, speedConsume, player.getId());
				return false;
			}
		});
		return true;
	}

	/**
	 * 集结行军购买额外格子
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.WORLD_MASS_MARCH_BUY_ITEMS_C_VALUE)
	private boolean onMarchBuyItemTimes(HawkProtocol protocol) {
		WorldMassMarchBuyExtraItemsReq req = protocol
				.parseProtocol(WorldMassMarchBuyExtraItemsReq.getDefaultInstance());

		// 行军信息
		String marchId = req.getCellMarchId();
		if (HawkOSOperator.isEmptyString(marchId)) {
			logger.error("march buy items failed, marchId:{}", marchId);
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_NOT_EXISIT);
			return false;
		}

		// 行军不存在
		IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
		if (march == null) {
			logger.error("march buy items failed, march not exist");
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_NOT_EXISIT);
			return false;
		}

		// 集结参与者人数购买上限
		int buyLimit = WorldMarchConstProperty.getInstance().getTempAssemblyQueueUpper();
		if (march.getMarchEntity().getBuyItemTimes() >= buyLimit) {
			logger.error("march buy items failed, march.getBuyItemTimes():{} , buyLimit:{}", march.getMarchEntity().getBuyItemTimes(),
					buyLimit);
			sendError(protocol.getType(), Status.Error.GUILD_WAR_BUY_ITEMS_OVER_VALUE);
			return false;
		}

		// check 消耗
		int[] cost = WorldMarchConstProperty.getInstance().getTempAssemblyQueueCostArray();

		// 本次要消耗的钻石数目
		int price = cost[march.getMarchEntity().getBuyItemTimes()];

		// 召回消耗道具
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(PlayerAttr.GOLD, price);

		// 判断是否满足消耗
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			return false;
		}

		// 成功消耗掉 , 累计次数
		consumeItems.consumeAndPush(player, Action.WORLD_MARCH_BUY_ITEMS);
		WorldMarchService.getInstance().addMarchBuyItems(march.getMarchEntity());
		logger.info("march buy item success,playerId:{}, itemTimes:{} ", march.getPlayerId(), march.getMarchEntity().getBuyItemTimes());

		// 回复客户端
		PushGuildWarBuyItems.Builder resp = PushGuildWarBuyItems.newBuilder();
		resp.setCellMarchId(marchId);
		resp.setBuyItemTimes(march.getMarchEntity().getBuyItemTimes());

		HawkProtocol pushProtocol = HawkProtocol.valueOf(HP.code.GUILD_WAR_PUSH_BUY_ITEM_TIMES_VALUE, resp);
		GuildService.getInstance().broadcastProtocol(player.getGuildId(), pushProtocol);

		return true;
	}

	/**
	 * 驻军买额外格子
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.WORLD_QUARTERED_MARCH_BUY_ITEMS_C_VALUE)
	private boolean onQuarteredMarchBuyItemTimes(HawkProtocol protocol) {
		WorldQuarteredMarchBuyExtraItemsReq req = protocol
				.parseProtocol(WorldQuarteredMarchBuyExtraItemsReq.getDefaultInstance());

		// 行军信息
		String marchId = req.getMarchId();
		if (HawkOSOperator.isEmptyString(marchId)) {
			logger.error("quartered march buy item times failed, marchId:{}", marchId);
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_NOT_EXISIT);
			return false;
		}

		// 行军不存在
		IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
		if (march == null) {
			logger.error("quartered march buy item times failed, march is null");
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_NOT_EXISIT);
			return false;
		}

		// 不是自己的行军或者自己不是队长
		if (!march.getPlayerId().equals(player.getId()) && !march.getMarchEntity().getLeaderPlayerId().equals(march.getPlayerId())) {
			logger.error("quartered march buy item times failed, marchId:{}", marchId);
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_NOT_LEADER);
			return false;
		}

		// 找不到目标点，该点有可能是联盟堡垒或者超级发射平台
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(march.getMarchEntity().getTerminalId());
		if (worldPoint == null) {
			logger.error("quartered march buy item times failed, worldPoint:{}", worldPoint);
			sendError(protocol.getType(), Status.Error.WORLD_POINT_NOT_EXIST);
			return false;
		}

		PushQuarteredMarchType pushType = PushQuarteredMarchType.PUSH_QUARTERED_PRESIDENT;
		int manorId = 0;

		// 集结参与者人数购买上限
		int buyLimit = WorldMarchConstProperty.getInstance().getTempAssemblyQueueUpper();
		if (march.getMarchEntity().getBuyItemTimes() >= buyLimit) {
			logger.error("quartered march buy item times failed, buyItemTimes:{} , buyLimit:{}",
					march.getMarchEntity().getBuyItemTimes(), buyLimit);
			sendError(protocol.getType(), Status.Error.GUILD_WAR_BUY_ITEMS_OVER_VALUE);
			return false;
		}

		// check 消耗
		int[] cost = WorldMarchConstProperty.getInstance().getTempAssemblyQueueCostArray();

		// 本次要消耗的钻石数目
		int price = cost[march.getMarchEntity().getBuyItemTimes()];

		// 召回消耗道具
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(PlayerAttr.GOLD, price);

		// 检查消耗
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			return false;
		}

		// 消耗掉
		consumeItems.consumeAndPush(player, Action.WORLD_QUARTERED_MARCH_BUY_ITEMS);

		// 累计购买次数
		WorldMarchService.getInstance().addMarchBuyItems(march.getMarchEntity());
		logger.info("march buy item success marchData:{}", march);

		// 刷新给联盟所有人，因为所有人都有机会加入驻军
		PushQuarteredMarchBuyItems.Builder pushQuarteredMarchs = PushQuarteredMarchBuyItems.newBuilder();
		pushQuarteredMarchs.setPushType(pushType);
		pushQuarteredMarchs.setManorId(manorId);
		pushQuarteredMarchs.setGuildId(player.getGuildId());
		pushQuarteredMarchs.setBuyItemTimes(march.getMarchEntity().getBuyItemTimes());
		HawkProtocol allProtocol = HawkProtocol.valueOf(HP.code.PUSH_UPDATE_QUARTERED_MARCHS_BUY_ITEM_VALUE,
				pushQuarteredMarchs);
		GuildService.getInstance().broadcastProtocol(player.getGuildId(), allProtocol);

		return true;
	}

	/**
	 * 集结队长解散
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.WORLD_MASS_DISSOLVE_C_VALUE)
	protected boolean onWorldMassDissolve(HawkProtocol protocol) {
		return worldMassDissolve(protocol);
	}
	
	/**
	 * 集结队长遣返
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.WORLD_MASS_REPATRIATE_C_VALUE)
	protected boolean onWorldMassRepatriate(HawkProtocol protocol) {
		return worldMassRepatriate(protocol);
	}
	
	/**
	 * 集结队长解散
	 * 
	 * @param protocol
	 * @return
	 */
	protected boolean worldMassDissolve(HawkProtocol protocol) {
		WorldMassDissolveReq req = protocol.parseProtocol(WorldMassDissolveReq.getDefaultInstance());
		// check
		if (!req.hasMarchId()) {
			logger.error("world mass march dissolve failed, marchId:{}", req.getMarchId());
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return false;
		}
		// check
		IWorldMarch worldMarch = WorldMarchService.getInstance().getMarch(req.getMarchId());
		if (worldMarch == null) {
			logger.error("world mass march dissolve failed, march:{}", worldMarch);
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_NOT_EXISIT);
			return false;
		}
		
		// 国家战争集结,只能解散本服的
		if (worldMarch.isNationMassMarch() && !player.getMainServerId().equals(worldMarch.getPlayer().getMainServerId())) {
			return false;
		}
		
		WorldMarch march = worldMarch.getMarchEntity();
		// 集结目标点
		int terminalX = march.getTerminalX();
		int terminalY = march.getTerminalY();

		// 非集结行军不能解散
		if (!worldMarch.isMassMarch()) {
			logger.error("world mass march dissolve failed, march:{}", march);
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_NOT_MASS);
			return false;
		}

		// 不是队长不能解散
		if (!HawkOSOperator.isEmptyString(march.getPlayerId()) && !march.getPlayerId().equals(player.getId())) {
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_NOT_LEADER);
			logger.error("world mass march dissolve failed, playerId:{}", march.getPlayerId());
			return false;
		}

		// 队长的行军非等待状态不能解散
		if (march.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
			logger.error("world mass march disslove failed , not waitting status:{}", march.getMarchStatus());
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_ALREADY_MARCHING);
			return false;
		}
		
		// 其他人原点返回
		Set<IWorldMarch> otherMatchs = WorldMarchService.getInstance().getMassJoinMarchs(worldMarch, false);
		logger.info("world mass march disslove success, leaderMarch marchData:{}", march);
		if (otherMatchs != null && otherMatchs.size() > 0) {
			int icon = GuildService.getInstance().getGuildFlagByPlayerId(march.getPlayerId());
			for (IWorldMarch tempWorldMarch : otherMatchs) {
				WorldMarch tempMarch = tempWorldMarch.getMarchEntity();

				if (tempMarch.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
					AlgorithmPoint point = WorldUtil.getMarchCurrentPosition(tempMarch);
					tempWorldMarch.getMarchEntity().setCallBackX(point.getX());
					tempWorldMarch.getMarchEntity().setCallBackY(point.getY());
					WorldMarchService.getInstance().onMonsterRelatedMarchAction(tempWorldMarch);
					WorldMarchService.getInstance().onMarchReturn(tempWorldMarch, HawkTime.getMillisecond(), 0);
				} else {
					WorldMarchService.getInstance().onMonsterRelatedMarchAction(tempWorldMarch);
					WorldMarchService.getInstance().onPlayerNoneAction(tempWorldMarch, HawkTime.getMillisecond());
				}

				// 发送邮件---集结失败：发起者解散
				GuildMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(tempMarch.getPlayerId())
						.setMailId(MailId.MASS_FAILED_ORGANIZER_DISSOLVE)
						.addContents(terminalX, terminalY)
						.setIcon(icon)
						.build());
			}
		}

		// 行为日志
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_MARCH_DISSLOVE,
				Params.valueOf("marchData", march));

		// 自己立即返回
		WorldMarchService.getInstance().onMonsterRelatedMarchAction(worldMarch);
		WorldMarchService.getInstance().onMarchReturnImmediately(worldMarch, march.getArmys());

		// 发邮件: 野怪活动
		if (march.getMarchType() == WorldMarchType.MONSTER_MASS_VALUE) {
			YuriMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(march.getPlayerId()).setMailId(MailId.MASS_MONSTER_TEAM_DISSOLVE).build());
			for (IWorldMarch joinMarch : otherMatchs) {
				YuriMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(joinMarch.getMarchEntity().getPlayerId()).setMailId(MailId.MASS_MONSTER_TEAM_DISSOLVE).build());
			}
		}
		
		// 回复
		WorldMassRepatriateResp.Builder resp = WorldMassRepatriateResp.newBuilder();
		resp.setResult(true);
		protocol.response(HawkProtocol.valueOf(HP.code.WORLD_MASS_DISSOLVE_S_VALUE, resp));
		
		return true;
	}

	/**
	 * 集结队长遣返
	 * @param protocol
	 * @return
	 */
	private boolean worldMassRepatriate(HawkProtocol protocol) {
		WorldMassRepatriateReq req = protocol.parseProtocol(WorldMassRepatriateReq.getDefaultInstance());

		IWorldMarch worldMarch = WorldMarchService.getInstance().getMarch(req.getMarchId());
		if (worldMarch == null) {
			logger.error("world mass march repartiate error, world march is null, marchId:{}", req.getMarchId());
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_NOT_EXISIT);
			return false;
		}
		
		// 国家战争集结,只能遣返本服的
		if (worldMarch.isNationMassMarch() && !player.getMainServerId().equals(worldMarch.getPlayer().getMainServerId())) {
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_NOT_TEAM_MEMBER);
			return false;
		}
		
		WorldMarch march = worldMarch.getMarchEntity();

		// 援助部队的遣返
		if (march.getMarchType() == WorldMarchType.ASSISTANCE_VALUE) {
			return onAssistanceRepatriate(protocol, worldMarch);
		}
	
		if (!worldMarch.isMassJoinMarch()) {
			logger.error("mass march repartiate error, march is not join march, march:{}", march.toString());
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_NOT_TEAM_MEMBER);
			return false;
		}

		if (HawkOSOperator.isEmptyString(march.getTargetId())) {
			logger.error("mass march repartiate error, march have no target id, march:{}", march.toString());
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_NOT_LEADER);
			return false;
		}

		IWorldMarch leaderMarch = WorldMarchService.getInstance().getMarch(march.getTargetId());
		if (leaderMarch == null) {
			logger.error("mass march repartiate error, have no leader march, march:{}", march.toString());
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_NOT_LEADER);
			return false;
		}

		if (leaderMarch.getMarchEntity().getMarchStatus() != WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
			logger.error("mass march repartiate error, leader march not waitting status, leaderMarch:{}", leaderMarch.toString());
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_ALREADY_MARCHING);
			return false;
		}

		if (!leaderMarch.getPlayerId().equals(player.getId())) {
			logger.error("mass march repartiate error, not leader, leaderId:{}, playerId:{}", leaderMarch.getPlayerId(), player.getId());
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_NOT_LEADER);
			return false;
		}

		if (worldMarch.isReturnBackMarch()) {
			return false;
		}
		
		double backX = 0;
		double backY = 0;
		if (march.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
			AlgorithmPoint point = WorldUtil.getMarchCurrentPosition(march);
			if (point != null) {
				backX = point.getX();
				backY = point.getY();
			}
		}

		WorldMarchService.getInstance().onMonsterRelatedMarchAction(worldMarch);
		
		WorldMarchService.getInstance().onMarchReturn(worldMarch, HawkTime.getMillisecond(), march.getAwardItems(), march.getArmys(), backX, backY);
		
		// 回复
		WorldMassRepatriateResp.Builder resp = WorldMassRepatriateResp.newBuilder();
		resp.setResult(true);
		protocol.response(HawkProtocol.valueOf(HP.code.WORLD_MASS_REPATRIATE_S_VALUE, resp));

		// 发邮件: 野怪活动
		if (march.getMarchType() == WorldMarchType.MONSTER_MASS_JOIN_VALUE) {
			YuriMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(march.getPlayerId()).setMailId(MailId.MASS_MONSTER_HAS_BEEN_KICK_OUT).build());
		}
		
		// 参与者在队长未发车前离开
		if (leaderMarch != null && leaderMarch.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
			int icon = GuildService.getInstance().getGuildFlagByPlayerId(march.getPlayerId());
			Set<IWorldMarch> marchers = WorldMarchService.getInstance().getMassJoinMarchs(leaderMarch, false);
			if (marchers == null) {
				marchers = new HashSet<IWorldMarch>();
			}
			marchers.add(worldMarch);
			marchers.add(leaderMarch);
			for (IWorldMarch iworldMarch : marchers) {
				// 发邮件---发车前有人主动离开
				GuildMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(iworldMarch.getPlayerId())
						.setMailId(MailId.MASS_PLAYER_LEAVE)
						.addSubTitles(march.getPlayerName())
						.addContents(march.getPlayerName())
						.setIcon(icon)
						.build());
			}
		}

		// 行为日志
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_MASS_REPATRIATE, 
				Params.valueOf("operationPlayerId", player.getId()), Params.valueOf("marchData", march));
		
		player.responseSuccess(protocol.getType());
		return true;
	}

	/**
	 * 援助部队遣返
	 * @param protocol
	 * @param worldMarch
	 * @param march
	 * @return
	 */
	private boolean onAssistanceRepatriate(HawkProtocol protocol, IWorldMarch worldMarch) {
		
		WorldMarch march = worldMarch.getMarchEntity();
		
		if (march.getMarchType() != WorldMarchType.ASSISTANCE_VALUE) {
			logger.error("assistance repartiate error, not assistance type, march:{}", march.toString());
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_NOT_ASSIST);
			return false;
		}

		if (march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
			logger.error("assistance repartiate error, march is already return back, march:{}", march.toString());
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_ASSIST_BACK_HOME);
			return false;
		}

		if (HawkOSOperator.isEmptyString(march.getTargetId())) {
			logger.error("assistance repartiate error, have no targetId, march:{}", march.toString());
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_NOT_EXISIT);
			return false;
		}

		if (!march.getTargetId().equals(player.getId())) {
			logger.error("assistance repartiate error, targetId:{}, playerId:{}", march.getTargetId(), player.getId());
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_NOT_EXISIT);
			return false;
		}

		logger.info("assistance repartiate, playerId:{}, marchId:{}", player.getId(), march.getMarchId());
		
		if (march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE) {
			AlgorithmPoint point = WorldUtil.getMarchCurrentPosition(march);
			WorldMarchService.getInstance().onMarchReturn(worldMarch, HawkTime.getMillisecond(), march.getAwardItems(), march.getArmys(), point.getX(), point.getY());
		} else {
			WorldMarchService.getInstance().onMarchReturn(worldMarch, march.getArmys(), 0);
		}

		WorldMassRepatriateResp.Builder resp = WorldMassRepatriateResp.newBuilder();
		resp.setResult(true);
		protocol.response(HawkProtocol.valueOf(HP.code.WORLD_MASS_REPATRIATE_S_VALUE, resp));

		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_MASS_REPATRIATE, Params.valueOf("operationPlayerId", player.getId()), Params.valueOf("marchData", march));
		return true;
	}
	
	/**
	 * 开启驻扎
	 */
	@ProtocolHandler(code = HP.code.WORLD_QUARTERED_C_VALUE)
	private boolean onWorldQuarteredStart(HawkProtocol protocol) {
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		int posX = req.getPosX();
		int posY = req.getPosY();
		int pointId = GameUtil.combineXAndY(posX, posY);

		// 超出地图范围
		int worldMaxX = WorldMapConstProperty.getInstance().getWorldMaxX();
		int worldMaxY = WorldMapConstProperty.getInstance().getWorldMaxY();
		if (posX >= worldMaxX || posX <= 0 || posY >= worldMaxY || posY <= 0) {
			logger.error("world quartered failed, out or range, x:{}, y:{}", posX, posY);
			return false;
		}

		// 请求点为阻挡点
		if (MapBlock.getInstance().isStopPoint(pointId)) {
			logger.error("world quartered failed, is stop point, x:{}, y:{}", posX, posY);
			return false;
		}
		
		//检查驻扎点的有效性
		Point point = new Point(pointId);
		if(!point.canQuarteredSeat()){
			WorldMarchService.logger.error("world quartered failed, x , y error, x:{}, y:{}", posX, posY);
			return false;
		}

		WorldPoint targetPoint = WorldPointService.getInstance().getWorldPoint(pointId);

		// 请求驻扎点不为null && (不为驻扎点 || 驻扎点目标玩家为null || 驻扎点玩家和自己同盟)
		if (targetPoint != null &&

				(targetPoint.getPointType() != WorldPointType.QUARTERED_VALUE
						|| HawkOSOperator.isEmptyString(targetPoint.getPlayerId())
						|| GuildService.getInstance().isPlayerInGuild(player.getGuildId(), targetPoint.getPlayerId())

				)) {

			logger.error("world quartered failed, target point not null and not quarter point, targetPoint", targetPoint.toString());
			return false;
		}

		// 部队请求检查
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>();
		if (!checkMarchReq(req, protocol.getType(), armyList, targetPoint)) {
			logger.error("world quartered failed, checkMarchReq fail");
			return false;
		}

		// 扣兵
		if (!ArmyService.getInstance().checkArmyAndMarch(player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			logger.error("world quartered failed, armyList:{}", armyList);
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_ARMY_COUNT);
			return false;
		}

		// 目标信息 ,开启驻扎行军
		String targetId = String.valueOf(pointId);
		IWorldMarch march = WorldMarchService.getInstance().startMarch(player, WorldMarchType.ARMY_QUARTERED_VALUE, pointId, targetId, null, 0, new EffectParams(req, armyList));

		// 行为日志
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_START_QUARTERED, Params.valueOf("marchData", march));

		// 回复协议
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		protocol.response(HawkProtocol.valueOf(HP.code.WORLD_QUARTERED_S_VALUE, builder));

		// 推送行军开始的通知
//		WorldMarchService.getInstance().marchRelatedNotify(march, targetPoint, MarchNotifyType.MARCH_START);

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
		WorldPoint targetPoint = WorldPointService.getInstance().getWorldPoint(posX, posY);

		// 目标点为空
		if (targetPoint == null) {
			sendError(protocol.getType(), Status.Error.TARGET_MONSTER_DISAPPEAR);
			logger.error("world attack monster req failed, point is null, x:{}, y:{}", posX, posY);
			// 重新同步下视野内的点
			WorldScene.getInstance().move(player.getAoiObjId(), posX, posY, 0.0f);
			return false;
		}

		// 目标点不是怪物
		if (targetPoint.getPointType() != WorldPointType.MONSTER_VALUE && targetPoint.getPointType() != WorldPointType.ROBOT_VALUE) {
			sendError(protocol.getType(), Status.Error.WORLD_POINT_NOT_MONSTER);
			logger.error("world attack monster req failed, point not monster, x:{}, y:{}, pointType:{}", posX, posY, targetPoint.getPointType());
			return false;
		}

		// 专属怪, 不能被别人打
		if (!HawkOSOperator.isEmptyString(targetPoint.getOwnerId()) && !targetPoint.getOwnerId().equals(player.getId())) {
			logger.error("world attack monster failed, exclusive point, x:{}, y:{}, ownerId:{}, playerId:{}", posX, posY, targetPoint.getOwnerId(), player.getId());
			sendError(protocol.getType(), Status.Error.MONSTER_HAS_OWNER);
			return false;
		}

		// 野怪配置
		WorldEnemyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, targetPoint.getMonsterId());

		// 怪物配置错误
		if (cfg == null) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			logger.error("world attack monster failed, cfg null, monsterId：{}", targetPoint.getMonsterId());
			return false;
		}

		// 等级不足
		if (player.getLevel() < cfg.getLowerLimit()) {
			logger.error("world attack monster failed,playerLevel:{} ,lowerLimit:{}", player.getLevel(), cfg.getLowerLimit());
			sendError(protocol.getType(), Status.Error.PLAYER_LEVEL_NOT_ENOUGH);
			return false;
		}

		int vitCost = cfg.getCostPhysicalPower();
		int buff = player.getEffect().getEffVal(EffType.ATK_MONSTER_VIT_ADD, new EffectParams(req, new ArrayList<>()));
		vitCost = (int)(vitCost * (1 + buff * GsConst.EFF_PER));
		//体力减少
		int buffReduce =  player.getEffect().getEffVal(EffType.BACK_PRIVILEGE_ATK_MONSTER_VIT_REDUCE);
		vitCost = (int) (vitCost * (1 - buffReduce * GsConst.EFF_PER));
		vitCost = Math.max(vitCost, 1);
		
		// 体力消耗
		ConsumeItems consumeItems = ConsumeItems.valueOf(PlayerAttr.VIT, vitCost);

		// 体力不足
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			logger.error("world attack monster failed, vit not enough vit:{}", player.getVit());
			return false;
		}

		// 当前最大击杀等级
		int maxLevelKilled = player.getData().getMonsterEntity().getMaxLevel();

		// 不可攻击等级
		if (cfg.getLevel() > maxLevelKilled + 1) {
			logger.error("world attack monster failed, maxLevelKilled:{}, killLevel:{}", maxLevelKilled, cfg.getLevel());
			sendError(protocol.getType(), Status.Error.MONSTER_LEVEL_CAN_NOT_KILL_VALUE);
			return false;
		}

		// 检测出征的军队信息
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>(); // 出征的士兵列表
		if (!checkMarchReq(req, protocol.getType(), armyList, targetPoint)) {
			logger.error("world attack monster failed,armyList:{}", armyList);
			return false;
		}

		// 扣兵
		if (!ArmyService.getInstance().checkArmyAndMarch(player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			logger.error("world attack monster failed, deduct armyList:{}", armyList);
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_ARMY_COUNT);
			return false;
		}

		// 开启行军,目标放怪物ID
		String targetId = String.valueOf(targetPoint.getMonsterId());
		IWorldMarch march = WorldMarchService.getInstance().startMarch(player, WorldMarchType.ATTACK_MONSTER_VALUE, targetPoint.getId(), targetId, null, 0, new EffectParams(req, armyList));
		if (march == null) {
			return false;
		}
		march.getMarchEntity().setVitCost(vitCost);
		
		// 扣除体力
		consumeItems.consumeAndPush(player, Action.FIGHT_MONSTER);

		// 行为日志
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_START_FIGHT_MONSTER, Params.valueOf("marchData", march));

		// 返回
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		protocol.response(HawkProtocol.valueOf(HP.code.WORLD_FIGHTMONSTER_S_VALUE, builder));

		MissionManager.getInstance().postMsg(player, new EventGenOldMonsterMarch(cfg.getLevel()));
		return true;
	}

	/**
	 * 援助玩家（火力支援）
	 */
	@ProtocolHandler(code = HP.code.WORLD_ASSISTANCE_C_VALUE)
	private boolean onWorldAssistanceStart(HawkProtocol protocol) {
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(req.getPosX(), req.getPosY());

		// 世界点不存在
		if (point == null) {
			logger.error("world assistance failed, point:{},marchType:{}", point, req.getType());
			sendError(protocol.getType(), Status.Error.WORLD_POINT_NOT_EXIST);
			return false;
		}

		// 是否是可援助点
		if (!WorldUtil.isAssistantPoint(point.getPointType())) {
			logger.error("world assistance manor failed,playerGuild: {} , pointType:{}", player.getGuildId(),
					point.getPointType());
			sendError(protocol.getType(), Status.Error.NOT_YOUR_GUILD_MANOR_VALUE);
			return false;
		}

		int marchType = req.getType().getNumber();
		String targetId = "";

		// 客户端发来的玩家的行军类型
		if (point.getPointType() == WorldPointType.KING_PALACE_VALUE) {

			// 和平期不能攻击
			if (!PresidentFightService.getInstance().isFightPeriod()) {
				logger.error("world assistance king palace failed , period: {}",
						PresidentFightService.getInstance().isFightPeriod());
				sendError(protocol.getType(), Status.Error.WORLD_MARCH_PRESIDENT_NOT_FIGHT_TIME);
				return false;
			}

			// 是否是自己的盟
			String guildId = PresidentFightService.getInstance().getCurrentGuildId();
			if (!HawkOSOperator.isEmptyString(guildId)) {
				if (!GuildService.getInstance().isPlayerInGuild(guildId, player.getId())) {
					logger.error("world assistance king palace, playerGuildId: {}, fightGuildId: {}",
							player.getGuildId(), guildId);
					sendError(protocol.getType(), Status.Error.PRESIDENT_NOT_YOUR_GUILD_OCCPY);
					return false;
				}
				targetId = guildId;
			} else {
				targetId = "";
			}
			marchType = WorldMarchType.PRESIDENT_ASSISTANCE_VALUE;
		} else {
			// 获取目标玩家
			final Player tarPlayer = GlobalData.getInstance().makesurePlayer(point.getPlayerId());
			if (tarPlayer == null) {
				logger.error("world assistance player failed, tarPlayer: {} ", tarPlayer);
				sendError(protocol.getType(), Status.Error.WORLD_POINT_TYPE_ERROR);
				return false;
			}

			// 检查玩家是否在同一联盟
			if (!GuildService.getInstance().isPlayerInGuild(player.getGuildId(), tarPlayer.getId())) {
				logger.error("world assistance player failed, playerGuildId: {}, tarPlayerGuildId: {}",
						player.getGuildId(), tarPlayer.getGuildId());
				sendError(protocol.getType(), Status.Error.GUILD_NOT_MEMBER);
				return false;
			}
			
			if(tarPlayer.isInDungeonMap()){
				logger.error("world assistance player failed, tarPlayer in tiberiumWar, tarPlayer: {} ", tarPlayer.getId());
				sendError(protocol.getType(), Status.Error.TIBERIUM_MEMBER_IN_ROOM);
				return false;
			}
			
			//跨出玩家不可
			if(CrossService.getInstance().isEmigrationPlayer(point.getPlayerId())) {
				logger.error("target player is a emigration playyer id:{}", point.getPlayerId());
				sendError(protocol.getType(), Status.CrossServerError.CROSS_OPERATION_NOT_SUPPOERT_VALUE);
				
				return false;
			}
			
			// 被援助者和援助者都拥有建筑“大使馆”，才可被援助和援助。
			BuildingCfg myCfg = player.getData().getBuildingCfgByType(BuildingType.EMBASSY);
			BuildingCfg tarCfg = tarPlayer.getData().getBuildingCfgByType(BuildingType.EMBASSY);
			if (myCfg == null || tarCfg == null) {
				logger.error("world assisatnce player failed, myCfg: {} , tarCfg: {}", myCfg, tarCfg);
				sendError(protocol.getType(), Status.Error.BUILDING_FRONT_NOT_EXISIT);
				return false;
			}

			// 检查玩家是否有同类型行军,一个玩家只能对同一个玩家援助一次
			if (WorldMarchService.getInstance().hasSameMarch(player.getId(), point, marchType)) {
				logger.error("world assisatnce player failed, has same march in this point marchType: {}", marchType);
				sendError(protocol.getType(), Status.Error.HAS_SAME_TYPE_MARCH);
				return false;
			}

			targetId = point.getPlayerId();
			marchType = WorldMarchType.ASSISTANCE_VALUE;
		}

		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>(); // 出征的士兵列表
		if (!checkMarchReq(req, protocol.getType(), armyList, point)) {
			logger.error("world assisatnce failed ,marchType:{},armyList:{}", marchType, armyList);
			return false;
		}

		// 扣兵
		if (!ArmyService.getInstance().checkArmyAndMarch(player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			logger.error("world assisatnce failed ,marchType:{},armyList:{}", marchType, armyList);
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_ARMY_COUNT);
			return false;
		}

		// 回复协议
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		protocol.response(HawkProtocol.valueOf(HP.code.WORLD_ASSISTANCE_S_VALUE, builder));

		IWorldMarch march = WorldMarchService.getInstance().startMarch(player, marchType, point.getId(), targetId, null, 0, new EffectParams(req, armyList));
		// 行为日志
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_START_ASSISTANT, Params.valueOf("marchData", march));
		return true;
	}

	/**
	 * 资源援助玩家
	 */
	@ProtocolHandler(code = HP.code.WORLD_ASSISTANCE_RES_C_VALUE)
	private boolean onWorldAssistanceResPlayer(HawkProtocol protocol) {
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(req.getPosX(), req.getPosY());

		// 世界点不存在
		if (point == null) {
			logger.warn("world assistance res player failed ,point:{}", point);
			sendError(protocol.getType(), Status.Error.WORLD_POINT_NOT_EXIST);
			return false;
		}

		int marchType = WorldMarchType.ASSISTANCE_RES_VALUE;

		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>(); // 出征的士兵列表
		if (!checkMarchReq(req, protocol.getType(), armyList, point)) {
			logger.warn("world assistance res player failed , marchType:{} ,armyList:{} ,point:{}", marchType,
					armyList, point);
			return false;
		}

		// 同一个被援助者，每个援助者同时只能派遣一支部队前往。
		final Player tarPlayer = GlobalData.getInstance().makesurePlayer(point.getPlayerId());
		if (tarPlayer == null) {
			logger.warn("world assistance res player failed , tarPlayer is null");
			sendError(protocol.getType(), Status.Error.WORLD_POINT_TYPE_ERROR);
			return false;
		}
		if(tarPlayer.getTBLYState() == TBLYState.GAMEING){
			logger.error("world assistance res player failed, tarPlayer in tiberiumWar, tarPlayer: {} ", tarPlayer.getId());
			sendError(protocol.getType(), Status.Error.TIBERIUM_MEMBER_IN_ROOM);
			return false;
		}
		
		if(CrossService.getInstance().isEmigrationPlayer(point.getPlayerId())) {
			logger.error("target player is a emigration playyer id:{}", point.getPlayerId());
			sendError(protocol.getType(), Status.CrossServerError.CROSS_OPERATION_NOT_SUPPOERT_VALUE);
			
			return false;
		}
		
		// 玩家援助次数达到上限
		int playerAssResTimes = WorldMarchService.getInstance().getPlayerAssistanceResTimes(player);
		if (playerAssResTimes >= WorldMarchConstProperty.getInstance().getAssistanceResTimes()) {
			sendError(protocol.getType(), Status.Error.ASS_RES_TIMES_MAX);
			return false;
		}
		
		// 玩家被援助次数达到上限
		int playerBeAssResTimes = WorldMarchService.getInstance().getPlayerBeAssistanceResTimes(tarPlayer);
		if (playerBeAssResTimes >= WorldMarchConstProperty.getInstance().getBeAssistanceResTimes()) {
			sendError(protocol.getType(), Status.Error.BE_ASS_RES_TIMES_MAX);
			return false;
		}
		
		// 被援助者和援助者必须为同一联盟，且都拥有建筑“贸易中心”，才可被援助和援助。
		BuildingCfg myCfg = player.getData().getBuildingCfgByType(BuildingType.TRADE_CENTRE);
		BuildingCfg tarCfg = tarPlayer.getData().getBuildingCfgByType(BuildingType.TRADE_CENTRE);
		// 没有建造贸易中心
		if (myCfg == null) {
			logger.warn("world assistance res player failed , self have no TRADE_CENTER building");
			sendError(protocol.getType(), Status.Error.GUILD_SELF_NO_TRADE_CENTER);
			return false;
		}
		// 盟友没有建造贸易中心
		if (tarCfg == null) {
			logger.warn("world assistance res player failed , target has no TRADE_CENTER building");
			sendError(protocol.getType(), Status.Error.GUILD_MEMBER_NO_TRADE_CENTER);
			return false;
		}
		
		// 对方处于零收益状态不可援助对方
		if (tarPlayer.isZeroEarningState()) {
			logger.warn("world assistance res player failed , target player is in zeroearning state");
			sendError(protocol.getType(), Status.Error.TARGET_PLAYER_ZEROEARNING_STATE);
			return false;
		}

		int totalWeight = 0;
		int marketBurden = player.getMarketBurden();

		// check param
		List<RewardItem> assistant = req.getAssistantList();
		List<ItemInfo> itemInfos = new ArrayList<ItemInfo>();
		for (RewardItem rewardItem : assistant) {
			if (rewardItem == null) {
				continue;
			}
			if (rewardItem.getItemCount() <= 0) {
				continue;
			}
			if (rewardItem.getItemId() != PlayerAttr.GOLDORE_UNSAFE_VALUE
					&& rewardItem.getItemId() != PlayerAttr.OIL_UNSAFE_VALUE
					&& rewardItem.getItemId() != PlayerAttr.STEEL_UNSAFE_VALUE
					&& rewardItem.getItemId() != PlayerAttr.TOMBARTHITE_UNSAFE_VALUE) {
				return false;
			}
			
			ItemInfo itemInfo = new ItemInfo(rewardItem.getItemType(), rewardItem.getItemId(),
					(int) rewardItem.getItemCount());
			itemInfos.add(itemInfo);
			// 将资源数据转成资源重量
			totalWeight += itemInfo.getCount()
					* WorldMarchConstProperty.getInstance().getResWeightByType(itemInfo.getItemId());
		}

		// check resource num
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(itemInfos, false);
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			logger.warn("world assistance res player failed ,check consume failed");
			return false;
		}

		// 超出可援助上限
		if (totalWeight > marketBurden) {
			logger.warn("world assistance res player failed , totalWeight:{},marketBurden:{}", totalWeight,
					marketBurden);
			sendError(protocol.getType(), Status.Error.ASSISTANCE_RES_MAX);
			return false;
		}

		// 检查玩家是否在同一联盟
		if (!GuildService.getInstance().isPlayerInGuild(player.getGuildId(), tarPlayer.getId())) {
			logger.warn("world assistance res player failed , playerGuildId:{},tarPlayerGuildId:{}",
					player.getGuildId(), tarPlayer.getGuildId());
			sendError(protocol.getType(), Status.Error.GUILD_NOT_MEMBER);
			return false;
		}

		// 扣兵
		if (!ArmyService.getInstance().checkArmyAndMarch(player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			logger.warn("world assistance res player failed , armyList:{}", armyList);
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_ARMY_COUNT);
			return false;
		}

		// 支援方从自己家扣除需要援助的资源数量
		consumeItems.consumeAndPush(player, Action.ASSISTANT_RES);

		// 回复协议
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		protocol.response(HawkProtocol.valueOf(HP.code.WORLD_ASSISTANCE_RES_S_VALUE, builder));

		AwardItems awardItems = AwardItems.valueOf();
		awardItems.addItemInfos(itemInfos);
		IWorldMarch march = WorldMarchService.getInstance().startMarch(player, marchType, point.getId(), point.getPlayerId(), awardItems.toDbString(), 0, new EffectParams(req, armyList));
		// 行为日志
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_START_ASSISTANT_RES,
				Params.valueOf("marchData", march));
		return true;
	}

	/**
	 * 发起集结
	 */
	@ProtocolHandler(code = HP.code.WORLD_MASS_C_VALUE)
	private boolean onWorldMassStart(HawkProtocol protocol) {
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		int waitTime = req.getMassTime();
		int marchType = req.getType().getNumber();
		// 集结时间检查
		if (marchType == WorldMarchType.GUNDAM_MASS_VALUE) {
			if (!WorldMarchConstProperty.getInstance().checkGundamMassWaitTime(waitTime)) {
				logger.error("world mass march create failed , marchType:{} waitTime :{} ", req.getType(), waitTime);
				sendError(protocol.getType(), Status.Error.MASS_ERR_TIME);
				return false;
			}
		} else if (marchType == WorldMarchType.NIAN_MASS_VALUE) {
			if (!WorldMarchConstProperty.getInstance().checkNianMassWaitTime(waitTime)) {
				sendError(protocol.getType(), Status.Error.MASS_ERR_TIME);
				return false;
			}
		} else if (marchType == WorldMarchType.MONSTER_MASS_VALUE || marchType == WorldMarchType.TREASURE_HUNT_MONSTER_MASS_VALUE) {
			if (!WorldMarchConstProperty.getInstance().checkMonsterMassWaitTime(waitTime)) {
				sendError(protocol.getType(), Status.Error.MASS_ERR_TIME);
				return false;
			}
		} else if (marchType == WorldMarchType.CHRISTMAS_MASS_VALUE) {
			if (!WorldMarchConstProperty.getInstance().getChristmasMassTimeSet().contains(waitTime)) {
				sendError(protocol.getType(), Status.Error.MASS_ERR_TIME);
				
				return false;
			}
		} else if (marchType == WorldMarchType.WAR_FLAG_MASS_VALUE) {
			if (!WorldMarchConstProperty.getInstance().getWarFlagMassSet().contains(waitTime)) {
				sendError(protocol.getType(), Status.Error.MASS_ERR_TIME);
				
				return false;
			}
		} else if (marchType == WorldMarchType.SPACE_MECHA_ATK_STRONG_HOLD_MASS_VALUE || marchType == WorldMarchType.SPACE_MECHA_MAIN_MARCH_MASS_VALUE) {
			if (!WorldMarchConstProperty.getInstance().checkSpaceMechaMassWaitTime(waitTime)) {
				sendError(protocol.getType(), Status.Error.MASS_ERR_TIME);
				return false;
			}
		} else {	
			if (!WorldMarchConstProperty.getInstance().checkMassWaitTime(waitTime)) {
				logger.error("world mass march create failed , marchType:{} waitTime :{} ", req.getType(), waitTime);
				sendError(protocol.getType(), Status.Error.MASS_ERR_TIME);
				return false;
			}
		}  

		// 没有出战权限
//		if (marchType == WorldMarchType.PRESIDENT_MASS_VALUE) {
//			if (CrossActivityService.getInstance().isOpen()) {
//				String serverId = player.getMainServerId();
//				String guildId = player.getGuildId();
//				if (!RedisProxy.getInstance().isCrossFightGuild(serverId, guildId)) {
//					sendError(protocol.getType(), Status.CrossServerError.CROSS_HAVE_NO_FIGHT_PERM_VALUE);
//					return false;
//				}
//			}
//		}
		
		// 没有联盟不能集结行军
		if (!player.hasGuild()) {
			logger.error("world mass march create failed , marchType:{} ,playerGuildId :{} ", req.getType(), player.getGuildId());
			sendError(protocol.getType(), Status.Error.GUILD_NO_JOIN);
			return false;
		}

		// 没有卫星通信所, 不能集结
		if (player.getData().getBuildingMaxLevel(BuildingType.SATELLITE_COMMUNICATIONS_VALUE) <= 0) {
			logger.error("world mass march create failed , marchType:{} , CommunicationBuildingLevel :{} ", req.getType(), player.getData().getBuildingMaxLevel(BuildingType.SATELLITE_COMMUNICATIONS_VALUE));
			sendError(protocol.getType(), Status.Error.NON_SATELLITE_COMMUNICATIONS);
			return false;
		}

		// 不是集结类型的行军
		if (!WorldUtil.isMassMarch(marchType)) {
			logger.error("world mass march create failed , marchType:{}", marchType);
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_REQ_TYPE_ERROR);
			return false;
		}

		// 跨服行军类型拦截
		if (WorldMarchService.getInstance().isCrossMarchLimit(player, WorldMarchType.valueOf(marchType))) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}
		
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(req.getPosX(), req.getPosY());
		if (point == null) {
			logger.error("world mass march create failed , targetPoint is null, x:{}, y:{}", req.getPosX(), req.getPosY());
			sendError(protocol.getType(), Status.Error.WORLD_POINT_TYPE_ERROR);
			return false;
		}
		
		// 领地集结行军检查
		if (marchType == WorldMarchType.MANOR_MASS_VALUE || marchType == WorldMarchType.MANOR_ASSISTANCE_MASS_VALUE) {
			if(point.getBuildingId() != TerritoryType.GUILD_BASTION_VALUE){
				logger.error("world mass march create failed , targetPoint is not guild manor , x:{}, y:{}", req.getPosX(), req.getPosY());
				sendError(protocol.getType(), Status.Error.WORLD_POINT_TYPE_ERROR);
				return false;
			}
			ManorMarchEnum marchEnum = ManorMarchEnum.valueOf(marchType);
			if(marchEnum != null){
				int resCode = marchEnum.checkMarch(point, player, false);
				if (resCode != Status.SysError.SUCCESS_OK_VALUE) {
					sendError(protocol.getType(), resCode);
					return false;
				}
			}
		}
		//巨龙陷阱集结
		if (marchType == WorldMarchType.DRAGON_ATTACT_MASS_VALUE) {
			if(point.getBuildingId() != TerritoryType.GUILD_DRAGON_TRAP_VALUE){
				logger.error("world mass march create failed , targetPoint is not guild trap , x:{}, y:{}", req.getPosX(), req.getPosY());
				sendError(protocol.getType(), Status.Error.WORLD_POINT_TYPE_ERROR);
				return false;
			}
			ManorMarchEnum marchEnum = ManorMarchEnum.valueOf(marchType);
			if(marchEnum != null){
				int resCode = marchEnum.checkMarch(point, player, false);
				if (resCode != Status.SysError.SUCCESS_OK_VALUE) {
					sendError(protocol.getType(), resCode);
					return false;
				}
			}
		}
		
		// 星甲召唤舱体行军
		if (marchType == WorldMarchType.SPACE_MECHA_MAIN_MARCH_MASS_VALUE) {
			PlayerSpaceMechaModule module = player.getModule(GsConst.ModuleType.SPACE_MECHA_MODULE);
			if (!module.mainSpaceMassCheck(protocol, point)) {
				return false;
			}
		}
		
		// 星甲召唤据点集结行军
		if (marchType == WorldMarchType.SPACE_MECHA_ATK_STRONG_HOLD_MASS_VALUE) {
			PlayerSpaceMechaModule module = player.getModule(GsConst.ModuleType.SPACE_MECHA_MODULE);
			if (!module.strongholdMassCheck(protocol, point)) {
				return false;
			}
		}
		
		// 迷雾要塞行军判断
		if (marchType == WorldMarchType.FOGGY_FORTRESS_MASS_VALUE && point.getPointType() != WorldPointType.FOGGY_FORTRESS_VALUE) {
			return false;
		}
		if (marchType == WorldMarchType.SUPER_WEAPON_MASS_VALUE && point.getPointType() != WorldPointType.SUPER_WEAPON_VALUE) {
			return false;
		}
		if (marchType == WorldMarchType.XZQ_MASS_VALUE && point.getPointType() != WorldPointType.XIAO_ZHAN_QU_VALUE) {
			return false;
		}
		if (marchType == WorldMarchType.FORTRESS_MASS_VALUE && point.getPointType() != WorldPointType.CROSS_FORTRESS_VALUE) {
			return false;
		}
		if (marchType == WorldMarchType.GUNDAM_MASS_VALUE && point.getPointType() != WorldPointType.GUNDAM_VALUE) {
			return false;
		}
		if (marchType == WorldMarchType.NIAN_MASS_VALUE && point.getPointType() != WorldPointType.NIAN_VALUE) {
			return false;
		}
		if (marchType == WorldMarchType.CHRISTMAS_MASS_VALUE && point.getPointType() != WorldPointType.CHRISTMAS_BOSS_VALUE) {
			return false;
		}
		if (marchType == WorldMarchType.WAR_FLAG_MASS_VALUE && point.getPointType() != WorldPointType.WAR_FLAG_POINT_VALUE) {
			return false;
		}
		
		// 攻击机甲次数限制
		if (marchType == WorldMarchType.GUNDAM_MASS_VALUE && isAtkGundamTimesLimit(player.getId())) {
			sendError(protocol.getType(), Status.Error.ATK_GUNDAM_TIMES_LIMIT_VALUE);
			return false;
		}
		
		// 攻击年兽次数限制
		if (marchType == WorldMarchType.NIAN_MASS_VALUE && isAtkNianTimesLimit(player.getId())) {
			sendError(protocol.getType(), Status.Error.ATK_NIAN_TIMES_LIMIT_VALUE);
			return false;
		}
		
		if (marchType == WorldMarchType.CHRISTMAS_MASS_VALUE && this.isAtkChristmasTimesLimit(player.getId())) {
			sendError(protocol.getType(), Status.Error.CHRISTMAS_ATK_TIMES_LIMIT_VALUE);
			return false;
		}
		
		boolean isNationMarch = false;
		if (CrossActivityService.getInstance().isOpen()) {
			if (point.getPointType() == WorldPointType.KING_PALACE_VALUE || point.getPointType() == WorldPointType.CAPITAL_TOWER_VALUE) {
				isNationMarch = true;
			}
		}
		
		// 带兵出征通用检查
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>();
		if (!checkMarchReq(req, protocol.getType(), armyList, point, isNationMarch)) {
			logger.error("world mass march create failed , marchType:{},armyList:{}", marchType, armyList);
			return false;
		}

		// 目标id
		String targetId = point.getPlayerId();

		// 消耗
		ConsumeItems items = null;
		int vit = 0;
		
		if (point.getPointType() == WorldPointType.KING_PALACE_VALUE) {
			// 和平期不能攻击
			if (!PresidentFightService.getInstance().isFightPeriod()) {
				logger.error("world mass march create failed , president period : {} ", PresidentFightService.getInstance().getPresidentPeriodType());
				sendError(protocol.getType(), Status.Error.WORLD_MARCH_PRESIDENT_NOT_FIGHT_TIME);
				return false;
			}
			if (!HawkOSOperator.isEmptyString(PresidentFightService.getInstance().getCurrentGuildId())) {
				targetId = PresidentFightService.getInstance().getCurrentGuildId();
			} else {
				targetId = "";
			}
			marchType = WorldMarchType.PRESIDENT_MASS_VALUE;
			
		} else if (point.getPointType() == WorldPointType.CAPITAL_TOWER_VALUE) {
			// 和平期不能攻击
			if (!PresidentFightService.getInstance().isFightPeriod()) {
				logger.error("world mass march create failed , president period : {} ", PresidentFightService.getInstance().getPresidentPeriodType());
				sendError(protocol.getType(), Status.Error.WORLD_MARCH_PRESIDENT_NOT_FIGHT_TIME);
				return false;
			}
			targetId = "";
			marchType = WorldMarchType.PRESIDENT_TOWER_MASS_VALUE;
			
		} else if (point.getPointType() == WorldPointType.SUPER_WEAPON_VALUE) {
			if (SuperWeaponService.getInstance().getStatus() != SuperWeaponPeriod.WARFARE_VALUE) {
				return false;
			}
			// 报名检测
			IWeapon superWeapon = SuperWeaponService.getInstance().getWeapon(point.getId());
			if (!superWeapon.canAttack(player.getGuildId())) {
				return false;
			}
			targetId = "";
			marchType = WorldMarchType.SUPER_WEAPON_MASS_VALUE;
			
		} else if (point.getPointType() == WorldPointType.XIAO_ZHAN_QU_VALUE) {
			// 报名检测
			XZQWorldPoint superWeapon = (XZQWorldPoint) point;
			if (superWeapon.isPeace()) {
				return false;
			}
			//是否可以攻击
			int canAttackXZQWorldPoint = XZQService.getInstance().canAttackXZQWorldPoint(player, superWeapon);
			if (canAttackXZQWorldPoint > 0) {
				sendError(protocol.getType(), canAttackXZQWorldPoint);
				return false;
			}
			targetId = "";
			marchType = WorldMarchType.XZQ_MASS_VALUE;
			
		} else if (point.getPointType() == WorldPointType.CROSS_FORTRESS_VALUE) {
			if (CrossFortressService.getInstance().getCurrentState() != SuperWeaponPeriod.WARFARE_VALUE) {
				return false;
			}
			targetId = "";
			marchType = WorldMarchType.FORTRESS_MASS_VALUE;
			
		} else if (point.getPointType() == WorldPointType.MONSTER_VALUE || point.getPointType() == WorldPointType.TH_MONSTER_VALUE) {

			// 野怪id
			int monsterId = point.getMonsterId();

			// 野怪配置
			WorldEnemyCfg monsterCfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, monsterId);

			// 野怪配置为空
			if (monsterCfg == null) {
				sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
				logger.error("world monster mass march start failed, monster null, marchType:{}, monsterId:{}", marchType, monsterId);
				return false;
			}

			// type1和type2类型野怪只能单人打，不能集结
			if (monsterCfg.getType() == MonsterType.TYPE_1_VALUE || monsterCfg.getType() == MonsterType.TYPE_2_VALUE) {
				sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
				logger.error("world monster mass march start failed, type error, marchType:{}, monsterId:{}", marchType, monsterId);
				return false;
			}

			// 玩家等级不满足
			if (player.getLevel() < monsterCfg.getLowerLimit()) {
				sendError(protocol.getType(), Status.Error.PLAYER_LEVEL_NOT_ENOUGH);
				logger.error("world monster mass march start failed, level not enougth, level:{} ,lowerLimit:{}", player.getLevel(), monsterCfg.getLowerLimit());
				return false;
			}
			
			if(marchType != WorldMarchType.MONSTER_MASS_VALUE && marchType != WorldMarchType.TREASURE_HUNT_MONSTER_MASS_VALUE){
				sendError(protocol.getType(), Status.Error.WORLD_POINT_TYPE_ERROR);
				logger.error("world monster mass march start failed, point type and march type not equals, point:{} ,march:{}", point.getPointType(), marchType);
				return false;
			}

			// 判断体力是否足够
			items = ConsumeItems.valueOf(PlayerAttr.VIT, monsterCfg.getCostPhysicalPower());
			if (!items.checkConsume(player, protocol.getType())) {
				logger.error("world mass march create failed , vit not enough, marchType:{},playerVit:{}", marchType, player.getVit());
				return false;
			}
			vit = monsterCfg.getCostPhysicalPower();
			// 设置targetId
			targetId = String.valueOf(monsterId);

		} else if (point.getPointType() == WorldPointType.GUILD_TERRITORY_VALUE) {
			if(marchType != WorldMarchType.MANOR_MASS_VALUE && marchType != WorldMarchType.MANOR_ASSISTANCE_MASS_VALUE 
					&& marchType != WorldMarchType.DRAGON_ATTACT_MASS_VALUE){
				sendError(protocol.getType(), Status.Error.WORLD_POINT_TYPE_ERROR);
				logger.error("world guild mass march start failed, point type and march type not equals, point:{} ,march:{}", point.getPointType(), marchType);
				return false;
			}
			// 设置targetId
			targetId = point.getGuildBuildId();
			
		} else if (point.getPointType() == WorldPointType.FOGGY_FORTRESS_VALUE) {
			if(WorldFoggyFortressService.getInstance().checkPointIsInActive(point.getId())){
				sendError(protocol.getType(), Status.Error.FOGGY_CAN_NOT_ATTACK_VALUE);
				return false;
			}
			
			FoggyFortressCfg foggyFortressCfg = HawkConfigManager.getInstance().getConfigByKey(FoggyFortressCfg.class, point.getMonsterId());
			if(Objects.isNull(foggyFortressCfg)){
				sendError(protocol.getType(), Status.Error.FOGGY_CAN_NOT_ATTACK_VALUE);
				return false;
			}
			//如果幽灵基地大于1级，需要先打相应等级的野怪
			if(foggyFortressCfg.getLevel() > 1 && player.getData().getMonsterEntity().getMaxLevel() < foggyFortressCfg.getLevel()){
				sendError(protocol.getType(), Status.Error.FOGGY_CAN_NOT_ATTACK_VALUE);
				return false;
			}
			targetId = String.valueOf(point.getMonsterId());
			
		}  else if (point.getPointType() == WorldPointType.GUNDAM_VALUE) {
			// 高达id
			int monsterId = point.getMonsterId();

			// 高达配置
			WorldGundamCfg gundamCfg = HawkConfigManager.getInstance().getConfigByKey(WorldGundamCfg.class, monsterId);

			// 高达配置为空
			if (gundamCfg == null) {
				sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
				return false;
			}

			// 判断体力是否足够
			items = ConsumeItems.valueOf(PlayerAttr.VIT, gundamCfg.getCostPhysicalPower());
			if (!items.checkConsume(player, protocol.getType())) {
				logger.error("world mass march create failed , vit not enough, marchType:{},playerVit:{}", marchType, player.getVit());
				return false;
			}
			vit = gundamCfg.getCostPhysicalPower();
			// 设置targetId
			targetId = String.valueOf(monsterId);
		} else if (point.getPointType() == WorldPointType.GUNDAM_VALUE) {
			int monsterId = point.getMonsterId();
			targetId = String.valueOf(monsterId);
			
		}  else if (point.getPointType() == WorldPointType.NIAN_VALUE) {
			int monsterId = point.getMonsterId();
			targetId = String.valueOf(monsterId);
			
		} else if (point.getPointType() == WorldPointType.CHRISTMAS_BOSS_VALUE) {
			int monsterId = point.getMonsterId();
			targetId = String.valueOf(monsterId);
		} else if (point.getPointType() == WorldPointType.WAR_FLAG_POINT_VALUE) {
			targetId = point.getGuildBuildId();
		} else if (point.getPointType() == WorldPointType.SPACE_MECHA_MAIN_VALUE) {
			targetId = player.getGuildId();  // 星甲召唤集结主舱行军
		} else if(point.getPointType() == WorldPointType.SPACE_MECHA_STRONG_HOLD_VALUE) {
			StrongHoldWorldPoint worldPoint = (StrongHoldWorldPoint) point;
			targetId = String.valueOf(worldPoint.getStrongHoldId());
		} else {
			// 检查目标点玩家是否可被集结
			Player defPlayer = GlobalData.getInstance().makesurePlayer(point.getPlayerId());
			if (defPlayer == null) {
				logger.error("world mass march create failed , defPlayer:{}", defPlayer);
				sendError(protocol.getType(), Status.Error.MASS_ERR_TARCITY_LOW);
				return false;
			}

			if (GuildService.getInstance().isPlayerInGuild(player.getGuildId(), point.getPlayerId())) {
				logger.error("world mass march create failed , playerGuildId:{},pointPlayerId:{}", player.getGuildId(),
						point.getPlayerId());
				sendError(protocol.getType(), Status.Error.WORLD_POINT_TYPE_ERROR);
				return false;
			}

			// 对方保护状态不能被集结
			if (defPlayer.getData().getCityShieldTime() > HawkTime.getMillisecond()) {
				logger.error("world mass march create failed, defPlayer protected defTime:{},currTime:{}",
						defPlayer.getData().getCityShieldTime(), HawkTime.getMillisecond());
				sendError(protocol.getType(), Status.Error.CITY_UNDER_PROTECTED);
				return false;
			}

			// 没有联盟且等级低于6级的玩家
			if (HawkOSOperator.isEmptyString(defPlayer.getGuildId())
					&& defPlayer.getCityLv() < WorldMapConstProperty.getInstance().getStepCityLevel1()) {
				logger.error("world mass march create failed,defGuildId:{},defPlayerLevel:{}", defPlayer.getGuildId(),
						defPlayer.getCityLv());
				sendError(protocol.getType(), Status.Error.MASS_ERR_TARCITY_LOW);
				return false;
			}
			
			if (defPlayer.isCsPlayer() && !CrossActivityService.getInstance().isOpen()) {
				return false;
			}
			
			if (player.isCsPlayer() && !CrossActivityService.getInstance().isOpen()) {
				return false;
			}
		}

		// 扣兵
		if (!ArmyService.getInstance().checkArmyAndMarch(player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			logger.error("world mass march create failed , deduct army failed armyList:{}", armyList);
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_ARMY_COUNT);
			return false;
		}

		// 打怪扣除体力
		if (items != null) {
			items.consumeAndPush(player, Action.FIGHT_MONSTER);
		}

		// 开始行军
		IWorldMarch march = WorldMarchService.getInstance().startMarch(player, marchType, point.getId(), targetId, null, waitTime, new EffectParams(req, armyList));
		if (march == null) {
			return false;
		}
		
		march.getMarchEntity().setVitCost(vit);
		if (marchType == WorldMarchType.SPACE_MECHA_MAIN_MARCH_MASS_VALUE) {
			HawkLog.logPrintln("spaceMecha start space mass defMarch, guildId: {}, playerId: {}", player.getGuildId(), player.getId());
		}
		
		// 行为日志
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_START_MASS, Params.valueOf("marchData", march));

		// 回复协议
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(march == null ? false : true);
		builder.setType(req.getType());
		if(!HawkOSOperator.isEmptyString(targetId)){
			builder.setTargetId(targetId);
		}
		protocol.response(HawkProtocol.valueOf(HP.code.WORLD_MASS_S_VALUE, builder));

		// 推送被集结
		if (marchType == WorldMarchType.MASS_VALUE) {
			PushService.getInstance().pushMsg(point.getPlayerId(), PushMsgType.BE_MASSED_VALUE);
		}
		
		if (!req.hasGuildFormation()) {
			sendNotice(point, targetId, march, 0, "");
		} else {
			try {
				GuildFormationObj formationObj = GuildService.getInstance().getGuildFormation(player.getGuildId());
				
				// 行军id添加到集结编队信息中
				int formationIndex = req.getGuildFormation();
				String formationName = "";
				GuildFormationCell formation = formationObj.getFormation(MassFormationIndex.valueOf(formationIndex));
				if (formation != null) {
					formationObj.addFormationMarch(formationIndex, march.getMarchId());
					formationName = formation.getName();
					formationObj.notifyUpdate();
				}
				
				sendNotice(point, targetId, march, formationIndex, formationName);
				
				GuildFormationModule module = player.getModule(GsConst.ModuleType.GUILD_FORMATION);
				module.tlog(player, formation, FormationOperType.FORMATION_MASS, march.getMarchId());
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return true;
	}

	/**
	 * 发送公告
	 */
	@SuppressWarnings("deprecation")
	private void sendNotice(WorldPoint point, String targetId, IWorldMarch march, int formationIndex, String formationName) {
		int[] leaderPos = WorldPlayerService.getInstance().getPlayerPosXY(player.getId());
		String leaderPosX = String.valueOf(leaderPos[0]);
		String leaderPosY = String.valueOf(leaderPos[1]);
		// 推送联盟聊天消息
		if (point.getPointType() == WorldPointType.MONSTER_VALUE || point.getPointType() == WorldPointType.TH_MONSTER_VALUE) {
			int monsterId = point.getMonsterId();
			WorldEnemyCfg monsterCfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, monsterId);
			String name = monsterCfg.getName();
			PushLangZhCNCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PushLangZhCNCfg.class, name);
			pushMsg(PushMsgType.ALLIANCE_MASSED_BOSS_VALUE, cfg == null ? name : cfg.getText());
			ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.CHAT_ALLIANCE, Const.NoticeCfgId.MASS_FRO_YURI_BOSS, player, monsterCfg.getName(), march.getMarchId(), leaderPosX, leaderPosY, formationIndex, formationName);
		} else if (point.getPointType() == WorldPointType.GUILD_TERRITORY_VALUE) {
			AbstractBuildable buildable = GuildManorService.getInstance().getBuildable(point);
			TerritoryType type = TerritoryType.valueOf(point.getBuildingId());
			if(type == TerritoryType.GUILD_DRAGON_TRAP){
				GuildDragonTrap trap =  (GuildDragonTrap) buildable;
				String guildName = GuildService.getInstance().getGuildName(trap.getEntity().getGuildId());
				ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.CHAT_ALLIANCE, Const.NoticeCfgId.MASS_FOR_GUILD_DRAGON_TRAP,
						player, guildName, march.getMarchId(), leaderPosX, leaderPosY, formationIndex, formationName);
			}else{
				GuildManorObj obj = (GuildManorObj) buildable;
				String guildName = GuildService.getInstance().getGuildName(obj.getEntity().getGuildId());
				ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.CHAT_ALLIANCE, Const.NoticeCfgId.MASSFORGUILDMANOR, player, guildName, march.getMarchId(), leaderPosX, leaderPosY, formationIndex, formationName);
			}
		} else if (point.getPointType() == WorldPointType.PLAYER_VALUE) {
			String playerId = point.getPlayerId();
			String playerName = GlobalData.getInstance().getPlayerNameById(playerId);
			if (WorldRobotService.getInstance().isRobotId(playerId)) {
				Player robot = GlobalData.getInstance().makesurePlayer(playerId);
				if (!HawkOSOperator.isEmptyString(robot.getName())) {
					playerName = robot.getName();
				}
			}
			pushMsg(PushMsgType.ALLIANCE_MASSED_PLAYER_VALUE, playerName);
			ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.CHAT_ALLIANCE, Const.NoticeCfgId.MASSFORPLAYER, player, playerName, march.getMarchId(), leaderPosX, leaderPosY, formationIndex, formationName);
		} else if(point.getPointType() == WorldPointType.KING_PALACE_VALUE || point.getPointType() == WorldPointType.CAPITAL_TOWER_VALUE){
			ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.CHAT_ALLIANCE, Const.NoticeCfgId.PRESIDENT_MASS_ATTACK, player, point.getPointType(), march.getMarchId(), leaderPosX, leaderPosY, formationIndex, formationName);
		} else if (point.getPointType() == WorldPointType.FOGGY_FORTRESS_VALUE) {
			ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.CHAT_ALLIANCE, Const.NoticeCfgId.FOGGY_MASS, player, point.getMonsterId(), march.getMarchId(), leaderPosX, leaderPosY, formationIndex, formationName);
			Collection<String> members = GuildService.getInstance().getGuildMembers(player.getGuildId());
			String guildName = GuildService.getInstance().getGuildName(player.getGuildId());
			FoggyFortressCfg foggyCfg = HawkConfigManager.getInstance().getConfigByKey(FoggyFortressCfg.class, Integer.valueOf(targetId));
			for (String playerId : members) {
				PushService.getInstance().pushMsg(playerId, PushMsgType.ALLIANCE_MASSED_FOGGY_VALUE, guildName, String.valueOf(foggyCfg.getLevel()));
			}
		} else if (point.getPointType() == WorldPointType.SUPER_WEAPON_VALUE) {
			IWeapon weapon = SuperWeaponService.getInstance().getWeapon(point.getId());
			
			String guildId = weapon.getGuildId();
			Player leader = WorldMarchService.getInstance().getSuperWeaponLeader(point.getId());
			
			if (HawkOSOperator.isEmptyString(guildId) || leader == null) {
				ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.CHAT_ALLIANCE, Const.NoticeCfgId.MASS_FOR_SUPER_WEAPON, player, point.getX(), point.getY(), march.getMarchId(), leaderPosX, leaderPosY, formationIndex, formationName);
			} else {
				String guildTag = GuildService.getInstance().getGuildTag(guildId);
				String playerName = leader.getName();
				ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.CHAT_ALLIANCE, Const.NoticeCfgId.MASS_FOR_SUPER_WEAPON_HAVE_PLAYER, player, guildTag, playerName, point.getX(), point.getY(), march.getMarchId(), leaderPosX, leaderPosY, formationIndex, formationName);
			}
		} else if (point.getPointType() == WorldPointType.XIAO_ZHAN_QU_VALUE) {
			XZQWorldPoint weapon = (XZQWorldPoint) point;
			
			Player leader = WorldMarchService.getInstance().getXZQLeader(weapon.getId());
			if ( leader == null) {
				ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.CHAT_ALLIANCE, Const.NoticeCfgId.MASS_FOR_XZQ, player, point.getX(), point.getY(), march.getMarchId(), leaderPosX, leaderPosY, formationIndex, formationName);
			} else {
				String guildTag = leader.getGuildTag();
				String playerName = leader.getName();
				ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.CHAT_ALLIANCE, Const.NoticeCfgId.MASS_FOR_XZQ_HAVE_PLAYER, player, guildTag, playerName, point.getX(), point.getY(), march.getMarchId(), leaderPosX, leaderPosY, formationIndex, formationName);
			}
		}else if (point.getPointType() == WorldPointType.GUNDAM_VALUE) {
			ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.CHAT_ALLIANCE, Const.NoticeCfgId.WORLD_GUNDAM_MASS, player, point.getMonsterId(), point.getX(), point.getY(), march.getMarchId(), leaderPosX, leaderPosY, formationIndex, formationName);
		}  else if (point.getPointType() == WorldPointType.NIAN_VALUE) {
			
			boolean isGhost = WorldNianService.getInstance().isGhost();
			Const.NoticeCfgId noticeId = isGhost ? Const.NoticeCfgId.GHOST_6 : Const.NoticeCfgId.WORLD_NIAN_MASS;
			ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.CHAT_ALLIANCE, noticeId, player, point.getMonsterId(), point.getX(), point.getY(), march.getMarchId(), leaderPosX, leaderPosY, formationIndex, formationName);
			
		}	else if (point.getPointType() == WorldPointType.CROSS_FORTRESS_VALUE) {
			Player leader = WorldMarchService.getInstance().getFortressLeader(point.getId());
			
			if (leader == null || !leader.hasGuild()) {
				ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.CHAT_ALLIANCE, Const.NoticeCfgId.WORLD_MASS_CORSS_FORTRESS, player, point.getX(), point.getY(), march.getMarchId(), leaderPosX, leaderPosY, formationIndex, formationName);
			} else {
				String guildTag = GuildService.getInstance().getGuildTag(leader.getGuildId());
				String playerName = leader.getName();
				ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.CHAT_ALLIANCE, Const.NoticeCfgId.WORLD_MASS_CORSS_FORTRESS_HAVE_PLAYER, player, guildTag, playerName, point.getX(), point.getY(), march.getMarchId(), leaderPosX, leaderPosY, formationIndex, formationName);
			}
		} else if (point.getPointType() == WorldPointType.CHRISTMAS_BOSS_VALUE) {
			Const.NoticeCfgId noticeId = Const.NoticeCfgId.WORLD_CHRISTMAS_MASS;
			ChatParames chatParams = ChatParames.newBuilder().setChatType(Const.ChatType.CHAT_ALLIANCE)
					.setKey(noticeId)
					.setPlayer(player)
					.addParms(point.getMonsterId())
					.addParms(point.getX())
					.addParms(point.getY())
					.addParms(march.getMarchId())
					.addParms(leaderPosX)
					.addParms(leaderPosY)
					.addParms(formationIndex)
					.addParms(formationName)
					.build();
			ChatService.getInstance().addWorldBroadcastMsg(chatParams);
		} else if (point.getPointType() == WorldPointType.WAR_FLAG_POINT_VALUE) {
			String flagId = point.getGuildBuildId();
			IFlag flag = FlagCollection.getInstance().getFlag(flagId);
			if (flag == null) {
				return;
			}
			String guildTag = GuildService.getInstance().getGuildTag(flag.getOwnerId());
			if (flag.isCenter()) {
				ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.CHAT_ALLIANCE, Const.NoticeCfgId.WAR_FLAG_CENTER_MASS_NOTICE, player, guildTag, point.getX(), point.getY(), march.getMarchId(), leaderPosX, leaderPosY, formationIndex, formationName);
			} else {
				ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.CHAT_ALLIANCE, Const.NoticeCfgId.WAR_FLAG_MASS_NOTICE, player, guildTag, point.getX(), point.getY(), march.getMarchId(), leaderPosX, leaderPosY, formationIndex, formationName);
			}
		} else if (point.getPointType() == WorldPointType.SPACE_MECHA_MAIN_VALUE || point.getPointType() == WorldPointType.SPACE_MECHA_STRONG_HOLD_VALUE) {
			PlayerSpaceMechaModule module = player.getModule(GsConst.ModuleType.SPACE_MECHA_MODULE);
			module.sendMassNotice(point, march, leaderPosX, leaderPosY);
		}
	}
	
	/**
	 * 向盟友推送集结消息
	 * @param msgType
	 * @param enemyName
	 */
	private void pushMsg(int msgType, String enemyName) {
		Collection<String> guildMembers = GuildService.getInstance().getGuildMembers(player.getGuildId());
		for (String playerId : guildMembers) {
			if (!playerId.equals(player.getId())) {
				PushService.getInstance().pushMsg(playerId, msgType, enemyName);
			}
		}
	}

	/**
	 * 加入集结
	 */
	@ProtocolHandler(code = HP.code.WORLD_MASS_JOIN_C_VALUE)
	private boolean onWorldMassJoinStart(HawkProtocol protocol) {
		if (!player.hasGuild()) {
			sendError(protocol.getType(), Status.Error.MASS_JOIN_HAS_NO_GUILD_VALUE);
			return false;
		}
		
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(req.getPosX(), req.getPosY());
		if (point == null) {
			logger.warn("world mass join failed, targetPoint is null, x:{}, y:{}", req.getPosX(), req.getPosY());
			sendError(protocol.getType(), Status.Error.WORLD_POINT_TYPE_ERROR_VALUE);
			return false;
		}
		
		int marchType = req.getType().getNumber();

		// 攻击机甲次数限制
		if (marchType == WorldMarchType.GUNDAM_MASS_JOIN_VALUE && isAtkGundamTimesLimit(player.getId())) {
			sendError(protocol.getType(), Status.Error.ATK_GUNDAM_TIMES_LIMIT_VALUE);
			return false;
		}
		
		// 攻击年兽次数限制
		if (marchType == WorldMarchType.NIAN_MASS_JOIN_VALUE && isAtkNianTimesLimit(player.getId())) {
			sendError(protocol.getType(), Status.Error.ATK_NIAN_TIMES_LIMIT_VALUE);
			return false;
		}
		
		if (marchType == WorldMarchType.CHRISTMAS_MASS_JOIN_VALUE && isAtkChristmasTimesLimit(player.getId())) {
			sendError(protocol.getType(), Status.Error.CHRISTMAS_ATK_TIMES_LIMIT_VALUE);
			
			return false;
		}
		
		// 不能加入自己的集结
		String leaderId = point.getPlayerId();
		if (player.getId().equals(leaderId)) {
			logger.warn("world mass join failed, can not join self march, leaderId:{},playerId:{}", leaderId, player.getId());
			sendError(protocol.getType(), Status.Error.MASS_ERR_CANOT_JOIN_SELF);
			return false;
		}

		// 跨服行军类型拦截
		if (WorldMarchService.getInstance().isCrossMarchLimit(player, WorldMarchType.valueOf(marchType))) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}

		// 带兵出征通用检查
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>(); // 出征的士兵列表
		if (!checkMarchReq(req, protocol.getType(), armyList, point, false)) {
			logger.error("world mass join failed, marchType:{},point:{}", marchType, point);
			return false;
		}

		// 取队长行军
		String massMarchId = req.getMarchId();
		IWorldMarch worldMarch = WorldMarchService.getInstance().getPlayerMarch(leaderId, massMarchId);
		Player leader = GlobalData.getInstance().makesurePlayer(leaderId);
		if (worldMarch == null || leader == null) {
			logger.warn("world mass join failed, leaderMarch:{}, leader:{}", worldMarch, leader);
			sendError(protocol.getType(), Status.Error.MASS_ERR_MARCH_NOT_EXIST);
			return false;
		}

		if (!worldMarch.isMassMarch()) {
			logger.warn("world mass not a mass type, marchType:{},massMarch:{}", worldMarch, leader);
			return false;
		}
		MassMarch massMarch = (MassMarch) worldMarch;
		// 集结行军类型和参与集结行军类型不一致
		int joinMarchType = massMarch.getJoinMassType().getNumber();
		if (marchType != joinMarchType) {
			logger.warn("world mass join failed, marchType:{},joinMarchType:{}", marchType, joinMarchType);
			sendError(protocol.getType(), Status.Error.MASS_JOIN_TYPE_ERROR);
			return false;
		}
		
		// 已经有加入这支部队的集结
		Set<IWorldMarch> massJoinMarchs = WorldMarchService.getInstance().getMassJoinMarchs(worldMarch, false);
		for (IWorldMarch massJoinMarch : massJoinMarchs) {
			if (massJoinMarch.getPlayerId().equals(player.getId())) {
				sendError(protocol.getType(), Status.Error.MASS_JOIN_REPEAT);
				return false;
			}
		}

		ConsumeItems items = null;
		if (marchType == WorldMarchType.MONSTER_MASS_JOIN_VALUE || marchType == WorldMarchType.GUNDAM_MASS_JOIN_VALUE || marchType == WorldMarchType.TREASURE_HUNT_MONSTER_MASS_JOIN_VALUE) {
			
			// 判断体力够不够
			items = ConsumeItems.valueOf(PlayerAttr.VIT, massMarch.getMarchEntity().getVitCost());
			if (!items.checkConsume(player, protocol.getType())) {
				logger.warn("world mass join failed,VIT_NOT_ENOUGH, marchType:{},playerVit:{}", massMarch,
						player.getVit());
				return false;
			}
		}

		// 集结队伍是否已出发
		if (massMarch.getMarchEntity().getMarchStatus() != WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
			logger.warn("world mass join failed,massMarch already go, marchType:{},massMarchStatus:{}", massMarch,
					massMarch.getMarchEntity().getMarchStatus());
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_ALREADY_MARCHING);
			return false;
		}

		if (marchType == WorldMarchType.PRESIDENT_MASS_JOIN_VALUE || marchType == WorldMarchType.PRESIDENT_TOWER_MASS_JOIN_VALUE) {
			boolean open = CrossActivityService.getInstance().isOpen();
			if (open) {
				if (!player.getMainServerId().equals(leader.getMainServerId())) {
					sendError(protocol.getType(), Status.Error.MASS_ERR_NOT_SAME_GUILD);
					return false;
				}
			} else {
				if (!GuildService.getInstance().isPlayerInGuild(player.getGuildId(), leaderId)) {
					sendError(protocol.getType(), Status.Error.MASS_ERR_NOT_SAME_GUILD);
					return false;
				}
			}
		} else {
			// 检查是否同盟
			if (!GuildService.getInstance().isPlayerInGuild(player.getGuildId(), leaderId)) {
				logger.warn("world mass join failed, marchType:{},leaderGuildId:{},playerGuildId:{}", marchType,
						leader.getGuildId(), player.getGuildId());
				sendError(protocol.getType(), Status.Error.MASS_ERR_NOT_SAME_GUILD);
				return false;
			}
		}
		
		// 星甲召唤主舱参与集结
		if (marchType == WorldMarchType.SPACE_MECHA_MAIN_MARCH_MASS_JOIN_VALUE) {
			PlayerSpaceMechaModule module = player.getModule(GsConst.ModuleType.SPACE_MECHA_MODULE);
			if (!module.mainSpaceMassJoinCheck(protocol)) {
				return false;
			}
		}
		
		// 星甲召唤据点参与集结
		if (marchType == WorldMarchType.SPACE_MECHA_ATK_STRONG_HOLD_MASS_JOIN_VALUE) {
			PlayerSpaceMechaModule module = player.getModule(GsConst.ModuleType.SPACE_MECHA_MODULE);
			if (!module.strongholdMassJoinCheck(protocol, point)) {
				return false;
			}
		}

		// 扣兵
		if (!ArmyService.getInstance().checkArmyAndMarch(player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			logger.warn("world mass join failed, marchType:{},armyList:{}", marchType, armyList);
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_ARMY_COUNT);
			return false;
		}

		// 参与集结打怪，扣除体力
		if (items != null) {
			items.consumeAndPush(player, Action.FIGHT_MONSTER);
		}
		String targetId = massMarch.getMarchEntity().getTargetId();
		
		// 回复协议
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		builder.setType(req.getType());
		if(!HawkOSOperator.isEmptyString(targetId)){
			builder.setTargetId(targetId);
		}
		
		protocol.response(HawkProtocol.valueOf(HP.code.WORLD_MASS_JOIN_S_VALUE, builder));

		// 出发
		IWorldMarch march = WorldMarchService.getInstance().startMarch(player, marchType, point.getId(), massMarchId, null, 0, new EffectParams(req, armyList));
		
		if (march != null && 
				(marchType == WorldMarchType.MONSTER_MASS_JOIN_VALUE || marchType == WorldMarchType.GUNDAM_MASS_JOIN_VALUE || marchType == WorldMarchType.TREASURE_HUNT_MONSTER_MASS_JOIN_VALUE)) {
			march.getMarchEntity().setVitCost(massMarch.getMarchEntity().getVitCost());
		}
		
		// 行为日志
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_START_MASS_JOIN,
				Params.valueOf("marchData", march));
		return true;
	}
	
	/**
	 * 攻击玩家（包含玩家城点、驻扎点、联盟堡垒、王座等敌方玩家）
	 */
	@ProtocolHandler(code = HP.code.WORLD_ATTACK_PLAYER_C_VALUE)
	private boolean onWorldAttackPlayerStart(HawkProtocol protocol) {
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(req.getPosX(), req.getPosY());

		// 路点为空
		if (point == null || point.getShowProtectedEndTime() > HawkTime.getMillisecond()) {
			logger.error("world attack player failed, point:{}", point);
			sendError(protocol.getType(), Status.Error.WORLD_POINT_NOT_EXIST);
			return false;
		}

		// 联盟检查
		if (point.needJoinGuild() && !player.hasGuild()) {
			logger.error("world attack player failed,player not in guild");
			sendError(protocol.getType(), Status.Error.GUILD_NO_JOIN);
			return false;
		}

		int marchType = 0;

		// 目标可能没有
		if (HawkOSOperator.isEmptyString(point.getPlayerId())) {
			logger.error("world attack player failed,point.getPlayerId:{}", point.getPlayerId());
			sendError(protocol.getType(), Status.Error.WORLD_POINT_TYPE_ERROR);
			return false;
		}

		Player targetPlayer = GlobalData.getInstance().makesurePlayer(point.getPlayerId());
		if (targetPlayer.isCsPlayer() && !CrossActivityService.getInstance().isOpen()) {
			return false;
		}
		
		if (player.isCsPlayer() && !CrossActivityService.getInstance().isOpen()) {
			return false;
		}
		
		// 目标玩家和自己同盟
		if (GuildService.getInstance().isPlayerInGuild(player.getGuildId(), point.getPlayerId())) {
			sendError(protocol.getType(), Status.Error.WORLD_POINT_TYPE_ERROR);
			logger.error("world attack player failed,in same guild playerGuildId:{}", player.getGuildId());
			return false;
		}
		// 攻打普通玩家
		String targetId = point.getPlayerId();
		marchType = WorldMarchType.ATTACK_PLAYER_VALUE;

		// 带兵出征通用检查
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>(); // 出征的士兵列表
		if (!checkMarchReq(req, protocol.getType(), armyList, point)) {
			logger.error("world attack player failed,check march req failed,armyList:{}", armyList);
			return false;
		}

		// 技能使用判断
		List<Integer> useSkills = req.getUseSkillIdList();
//		List<Integer> useSkills = Arrays.asList(GsConst.SKILL_10104); // 
		String duelMark = ""; // 不要怪我, 我也是被逼的
		boolean castSkill10104 = false;
		if (useSkills != null && useSkills.contains(GsConst.SKILL_10104)) {
			PlayerTalentModule module = player.getModule(GsConst.ModuleType.TALENT_MODULE);
			castSkill10104 = module.castSkill(protocol, GsConst.SKILL_10104, Collections.emptyList());
			if(!castSkill10104){
				sendError(protocol.getType(), Status.Error.TALENT_SKILL_USE_FAIL_VALUE);
				return false;
			}
			duelMark = AttackPlayerMarch.DUEL_MARK;
		}

		// 扣兵
		if (!ArmyService.getInstance().checkArmyAndMarch(player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			logger.error("world attack player failed,deduct army failed, armyList:{}", armyList);
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_ARMY_COUNT);
			if(castSkill10104){ // 如果释放了
				Skill10104 skill = TalentSkillContext.getInstance().getSkill(GsConst.SKILL_10104);
				skill.forceCoolDown(player);
			}
			return false;
		}

		IWorldMarch march = WorldMarchService.getInstance().startMarch(player, marchType, point.getId(), targetId, duelMark, 0, new EffectParams(req, armyList));
		// 推送将被受到攻击的通知
		if (marchType == WorldMarchType.ATTACK_PLAYER_VALUE) {
			PushService.getInstance().pushMsg(targetId, PushMsgType.BE_ATTACKED_VALUE);
		}
		
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_START_ATTACK_PLAYER,
				Params.valueOf("marchData", march));

		// 回复协议
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		protocol.response(HawkProtocol.valueOf(HP.code.WORLD_ATTACK_PLAYER_S_VALUE, builder));
		
		return true;
	}
	
	/**
	 * 行军出发前通用检查：目标点类型、队列数量、各士兵数量
	 * @param req
	 * @param hpCode
	 * @param armyList
	 * @param worldPoint
	 * @return
	 */
	public boolean checkMarchReq(WorldMarchReq req, int hpCode, List<ArmyInfo> armyList, WorldPoint worldPoint) {
		return checkMarchReq(req, hpCode, armyList, worldPoint, true);
	}

	public boolean checkMarchReq(WorldMarchReq req, int hpCode, List<ArmyInfo> armyList, WorldPoint worldPoint,boolean checkFreeMarch) {
		return checkMarchReq(req, hpCode, armyList, worldPoint, checkFreeMarch, false);
	}
	
	/**
	 * 行军出发前通用检查：目标点类型、队列数量、各士兵数量
	 * @param req
	 * @param hpCode
	 * @param armyList
	 * @param worldPoint
	 * @return
	 */
	public boolean checkMarchReq(WorldMarchReq req, int hpCode, List<ArmyInfo> armyList, WorldPoint worldPoint,boolean checkFreeMarch, boolean isNationMarch) {
		if (player.isCsPlayer() && !CrossActivityService.getInstance().isOpen()) {
			return false;
		}
		
		if (GsConfig.getInstance().isRobotMode() && WorldMarchService.getInstance().getMarchsSize() >= GameConstCfg.getInstance().getMarchCountLimit()
				&& player.getOpenId().startsWith("robot_puid")) {
			HawkLog.debugPrintln("start march failed, world march count overhead");
			return false;
		}
				
		if (WorldUtil.isOwnPoint(player.getId(), worldPoint)) {
			sendError(hpCode, Status.Error.HAS_SAME_TYPE_MARCH);
			return false;
		}

		if (checkFreeMarch && !WorldMarchService.getInstance().isHasFreeMarch(player)) {
			sendError(hpCode, Status.Error.WORLD_MARCH_MAX_LIMIT);
			return false;
		}

		List<ArmySoldierPB> reqArmyList = req.getArmyInfoList();
		if ((req.getAssistantList() == null || req.getAssistantList().isEmpty()) && (reqArmyList == null || reqArmyList.size() == 0)) {
			sendError(hpCode, Status.Error.WORLD_MARCH_NO_ARMY);
			return false;
		}

		for (ArmySoldierPB marchArmy : reqArmyList) {
			if (marchArmy.getCount() <= 0) { // 防止刷兵
				sendError(hpCode, Status.Error.WORLD_MARCH_NO_ARMY);
				return false;
			}
		}
		
		if (req.getHeroIdCount() > 2) {
			return false;
		}
		
		if (!checkMarchDressReq(req.getMarchDressList())) {
			return false;
		}

		// 检查英雄出征
		if (!ArmyService.getInstance().heroCanMarch(player, req.getHeroIdList())) {
			return false;
		}	
		
		if (!ArmyService.getInstance().superSoldierCsnMarch(player, req.getSuperSoldierId())) {
			return false;
		}

		if (req.getSuperLab() != 0 && !player.isSuperLabActive(req.getSuperLab())) {
			return false;
		}
		if (req.getManhattan().getManhattanAtkSwId() > 0 || req.getManhattan().getManhattanDefSwId() > 0) {
			// 检测超武信息,判断是否解锁，
			PlayerManhattanModule manhattanModule = player.getModule(GsConst.ModuleType.MANHATTAN);
			if (!manhattanModule.checkMarchReq(req.getManhattan())) {
				return false;
			}
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
				logger.error("checkMarchReq failed, totalCnt:{}, maxMarchSoldierNum::{}, armyList:{}", totalCnt, maxMarchSoldierNum, armyList.toString());
				sendError(hpCode, Status.Error.WORLD_MARCH_ARMY_TOTALCOUNT);
				return false;
			}
		}
		
		return true;
	}

	public boolean checkMarchDressReq(List<Integer> marchDressList) {
		// 装扮只能带两个
		if (marchDressList.size() > 4) {
			logger.error("req march error, marchDressList error, playerId:{}, marchDressList:{}", player.getId(), Arrays.asList(marchDressList).toString());
			return false;
		}
		DressEntity dressEntity = player.getData().getDressEntity();
		// 装扮类型重复检测
		List<Integer> dressTypes = new ArrayList<>();
		for (int dress : marchDressList) {
			// 装扮不存在
			DressCfg dressCfg = HawkConfigManager.getInstance().getConfigByKey(DressCfg.class, dress);
			if (dressCfg == null) {
				logger.error("req march error, dressCfg null, playerId:{}, marchDressList:{}", player.getId(), Arrays.asList(marchDressList).toString());
				return false;
			}
			
			// 检测装扮类型重复
			if (dressTypes.contains(dressCfg.getDressType())) {
				logger.error("req march error, dressTypes error, playerId:{}, marchDressList:{}", player.getId(), Arrays.asList(marchDressList).toString());
				return false;
			}
			dressTypes.add(dressCfg.getDressType());
			
			// 玩家没有这个装扮
			DressItem dressInfo = dressEntity.getDressInfo(dressCfg.getDressType(), dressCfg.getModelType());
			if (dressInfo == null) {
				logger.error("req march error, dressInfo error, playerId:{}, marchDressList:{}", player.getId(), Arrays.asList(marchDressList).toString());
				return false;
			}
			
			// 装扮已经到期了
			if (HawkTime.getMillisecond() > dressInfo.getStartTime() + dressInfo.getContinueTime()) {
				logger.error("req march error, dressInfo time, playerId:{}, marchDressList:{}", player.getId(), Arrays.asList(marchDressList).toString());
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 采集资源
	 */
	@ProtocolHandler(code = HP.code.WORLD_COLLECTRESOURCE_C_VALUE)
	private boolean onWorldCollectResourceStart(HawkProtocol protocol) {
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(req.getPosX(), req.getPosY());

		// check点数据
		if (point == null || point.getPointType() != WorldPointType.RESOURCE_VALUE) {
			logger.warn("world collect resource failed, point:{}", point);
			sendError(protocol.getType(), Status.Error.WORLD_POINT_NOT_RESOURCE);
			return false;
		}

		// 新手专属资源
		if (!HawkOSOperator.isEmptyString(point.getOwnerId()) && !point.getOwnerId().equals(player.getId())) {
			logger.warn("world collect resource failed, pointOwnerId:{},playerId:{}", point.getOwnerId(),
					player.getId());
			sendError(protocol.getType(), Status.Error.RESOURCE_HAS_OWNER);
			return false;
		}

		// 带兵行军通用检查
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>();

		// 判断超级矿是否可以采集
		int marchType = WorldMarchType.COLLECT_RESOURCE_VALUE;
		String targetId = String.valueOf(point.getResourceId());
		int targetResType = 0;
		// 检查资源配置
		WorldResourceCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WorldResourceCfg.class,
				point.getResourceId());
		if (cfg == null) {
			logger.warn("world collect resource failed, no resource cfg,manorCfg:{}", cfg);
			sendError(protocol.getType(), Status.SysError.CONFIG_ERROR);
			return false;
		}
		targetResType = cfg.getResType();

		// check
		if (!checkMarchReq(req, protocol.getType(), armyList, point)) {
			logger.warn("world collect resource failed, check march req failed");
			return false;
		}

		// 检查资源是否可采
		if (targetResType <= 0) {
			logger.warn("world collect resource failed, no resource type,targetResType:{}", targetResType);
			sendError(protocol.getType(), Status.Error.CITY_LEVEL_NOT_ENOUGH);
			return false;
		}

		int[] resLv = WorldMapConstProperty.getInstance().getResLv();

		for (int i = 0; i < RES_TYPE.length; i++) {
			if (targetResType != RES_TYPE[i]) {
				continue;
			}
			if (player.getCityLv() < resLv[i]) {
				logger.warn("world collect resource failed, playerCityLv:{},resLevel:{}", player.getCityLv(),
						resLv[i]);
				sendError(protocol.getType(), Status.Error.CITY_LEVEL_NOT_ENOUGH);
				return false;
			}
		}

		// 扣兵
		if (!ArmyService.getInstance().checkArmyAndMarch(player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			logger.warn("world collect resource failed, deduct army failed armyList:{}", armyList);
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_ARMY_COUNT);
			return false;
		}

		IWorldMarch march = WorldMarchService.getInstance().startMarch(player, marchType, point.getId(), targetId, null, 0, new EffectParams(req, armyList));
		// 行为日志
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_START_COLLECT_RESOURCE,
				Params.valueOf("marchData", march));

		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		protocol.response(HawkProtocol.valueOf(HP.code.WORLD_COLLECTRESOURCE_S_VALUE, builder));

		return true;
	}
	
	/**
	 * 采集资源宝库(开仓放粮)
	 */
	@ProtocolHandler(code = HP.code.WORLD_COLL_TREASURE_C_VALUE)
	private boolean onWorldCollectTreasureResourceStart(HawkProtocol protocol) {
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(req.getPosX(), req.getPosY());

		// check点数据
		if (point == null || point.getPointType() != WorldPointType.RESOURC_TRESURE_VALUE) {
			logger.warn("world collect resource failed, point:{}", point);
			sendError(protocol.getType(), Status.Error.WORLD_POINT_NOT_RESOURCE);
			return false;
		}

		// 带兵行军通用检查
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>();

		String targetId = String.valueOf(point.getResourceId());
		// 检查资源配置
		ResTreasureCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ResTreasureCfg.class,
				point.getResourceId());
		if (cfg == null) {
			logger.warn("world collect resource failed, no resource cfg,manorCfg:{}", cfg);
			sendError(protocol.getType(), Status.SysError.CONFIG_ERROR);
			return false;
		}
		
		PlanetExploreKVCfg planetExploreCfg = HawkConfigManager.getInstance().getKVInstance(PlanetExploreKVCfg.class);
		if (cfg.getId() == planetExploreCfg.getRefreshTargetId() && !player.isCsPlayer()) {
			Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.PLANET_EXPLORE_347_VALUE);
			PlanetExploreActivity activity = (PlanetExploreActivity)opActivity.get();
			int result = activity.collectCheck(player.getId());
			if (result != 0) {
				logger.error("world collect resource of planet explore failed, playerId: {}, result: {}", player.getId(), result);
				sendError(protocol.getType(), result);
				return false;
			}
		}

		// check
		if (!checkMarchReq(req, protocol.getType(), armyList, point)) {
			logger.warn("world collect resource failed, check march req failed");
			return false;
		}

		// 扣兵
		if (!ArmyService.getInstance().checkArmyAndMarch(player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			logger.warn("world collect resource failed, deduct army failed armyList:{}", armyList);
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_ARMY_COUNT);
			return false;
		}

		IWorldMarch march = WorldMarchService.getInstance().startMarch(player, WorldMarchType.COLLECT_RES_TREASURE_VALUE, point.getId(), targetId, null, 0, new EffectParams(req, armyList));
		if (cfg.getId() == planetExploreCfg.getRefreshTargetId()) {
			march.getMarchEntity().setExtraInfo(String.valueOf(planetExploreCfg.getRefreshTargetId()));
		}
		// 行为日志
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_START_COLLECT_RESOURCE,
				Params.valueOf("marchData", march));

		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		protocol.response(HawkProtocol.valueOf(HP.code.WORLD_COLL_TREASURE_S, builder));

		return true;
	}
	
	/**
	 * 世界侦查
	 */
	@ProtocolHandler(code = HP.code.WORLD_SPY_C_VALUE)
	private boolean onWorldSpyStart(HawkProtocol protocol) {
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());

		// 目标点
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(req.getPosX(), req.getPosY());
		if (!worldSpyMarchCheck(req.getPosX(), req.getPosY(), protocol.getType())) {
			return false;
		}

		// 消费check
		boolean useGold = req.hasSpyUseGold() && req.getSpyUseGold();

		// 消耗
		List<ItemInfo> itemInfo = WorldMarchConstProperty.getInstance().getInvestigationMarchCost();
		for (ItemInfo item : itemInfo) {
			int eff = player.getEffect().getEffVal(EffType.SPY_CONSUM_REDUS, new EffectParams());
			int buffCost = (int)(item.getCount() * (1 - eff * GsConst.EFF_PER));
			item.setCount(Math.max(0, buffCost));
		}
		
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addConsumeInfo(itemInfo, useGold);
		if (!consume.checkConsume(player, protocol.getType())) {
			logger.error("world spy failed,consume check failed");
			return false;
		}
		consume.consumeAndPush(player, Action.WORLD_SPY);

		// 普通玩家基地，放玩家的ID
		String targetId = "";

		// 堡垒和据点放据点ID，超级发射平台的目标放发射平台的拥有联盟ID
		if (point.getPointType() == WorldPointType.KING_PALACE_VALUE) {
			// 国王战目标放要攻击的目标
			targetId = PresidentFightService.getInstance().getCurrentGuildId();
			if (HawkOSOperator.isEmptyString(targetId)) {
				targetId = "";
			}
		} else if (WorldUtil.isPresidentTowerPoint(point)) {
			// 国王战目标放要攻击的目标
			targetId = PresidentFightService.getInstance().getPresidentTowerGuild(point.getId());
			if (HawkOSOperator.isEmptyString(targetId)) {
				targetId = "";
			}
		} else if (point.getPointType() == WorldPointType.GUILD_TERRITORY_VALUE || point.getPointType() == WorldPointType.WAR_FLAG_POINT_VALUE) {
			targetId = point.getGuildBuildId();
		} else if (point.getPointType() == WorldPointType.SUPER_WEAPON_VALUE) {
			targetId = SuperWeaponService.getInstance().getWeapon(point.getId()).getGuildId();
			if (HawkOSOperator.isEmptyString(targetId)) {
				targetId = "";
			}
		}else if (point.getPointType() == WorldPointType.XIAO_ZHAN_QU_VALUE) {
			targetId = XZQService.getInstance().getXZQPoint(point.getId()).getGuildId();
			if (HawkOSOperator.isEmptyString(targetId)) {
				targetId = "";
			}
		} else if (point.getPointType() == WorldPointType.CROSS_FORTRESS_VALUE) {
			Player leader = WorldMarchService.getInstance().getFortressLeader(point.getId());
			if (leader == null || !leader.hasGuild()) {
				targetId = "";
			} else {
				targetId = leader.getGuildId();
			}
		} else {
			targetId = point.getPlayerId();
			if (HawkOSOperator.isEmptyString(targetId)) {
				logger.error("world spy failed,point playerId null");
				sendError(protocol.getType(), Status.Error.WORLD_POINT_TYPE_ERROR_VALUE);
				return false;
			}
		}

		// 创建行军
		IWorldMarch march = WorldMarchService.getInstance().startMarch(player, WorldMarchType.SPY_VALUE, point.getId(), targetId, null, 0, new EffectParams());

		// 推送被侦查
		if (point.getPointType() == WorldPointType.PLAYER_VALUE) {
			PushService.getInstance().pushMsg(targetId, PushMsgType.BE_DETECTED_VALUE);
		}

		// 回复协议
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		protocol.response(HawkProtocol.valueOf(HP.code.WORLD_SPY_S_VALUE, builder));
		
		// 记录侦查日志
		logDetectMarch(point, targetId);

		// 行为日志
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_START_SPY, Params.valueOf("marchData", march));
		return true;
	}
	
	/**
	 * 记录侦查日志
	 * 
	 * @param point
	 * @param targetId
	 */
	private void logDetectMarch(WorldPoint point, String targetId) {
		Player targetPlayer = GlobalData.getInstance().makesurePlayer(targetId);
		long pointArmyPower = 0;
		if (point.getPointType() == WorldPointType.PLAYER_VALUE) {
			pointArmyPower = targetPlayer.getData().getPowerElectric().getArmyBattlePoint();
		} else {
			List<IWorldMarch> playerMarchs = WorldMarchService.getInstance().getPlayerPointMarch(targetId, point.getId());
			for (IWorldMarch march : playerMarchs) {
				pointArmyPower += GameUtil.getArmyPower(march.getArmys());
			}
		}
		
		LogUtil.logDetectFlow(player, targetPlayer, point.getPointType(), pointArmyPower);
	}

	/**
	 * 侦查行军判断
	 * 
	 * @param targetPoint
	 * @param hpCode
	 * @return
	 */
	private boolean worldSpyMarchCheck(int x, int y, int hpCode) {
		WorldPoint targetPoint = WorldPointService.getInstance().getWorldPoint(x, y);
		
		if (targetPoint == null) {
			logger.error("world spy failed, targetPoint is null, playerId:{}, x:{}, y:{}", player.getId(), x, y);
			sendError(hpCode, Status.Error.WORLD_POINT_NOT_EXIST);
			return false;
		}

		// 判断是否可以开启行军
		if (!WorldMarchService.getInstance().isHasFreeMarch(player) 
				&& (!WorldMarchService.getInstance().isExtraSpyMarchOpen(player) || WorldMarchService.getInstance().isExtraSypMarchOccupied(player))) {
			logger.error("world spy failed, not have free march, playerId:{}", player.getId());
			sendError(hpCode, Status.Error.WORLD_MARCH_MAX_LIMIT);
			return false;
		}

		// 只有城点、资源点、驻扎点, 王城， 联盟领地允许侦查
		int pointType = targetPoint.getPointType();
		boolean canSpy = pointType == WorldPointType.PLAYER_VALUE 
				|| pointType == WorldPointType.RESOURCE_VALUE
				|| pointType == WorldPointType.QUARTERED_VALUE
				|| pointType == WorldPointType.KING_PALACE_VALUE
				|| pointType == WorldPointType.CAPITAL_TOWER_VALUE
				|| pointType == WorldPointType.GUILD_TERRITORY_VALUE
				|| pointType == WorldPointType.STRONG_POINT_VALUE
				|| pointType == WorldPointType.SUPER_WEAPON_VALUE
				|| pointType == WorldPointType.XIAO_ZHAN_QU_VALUE
				|| pointType == WorldPointType.TH_RESOURCE_VALUE
				|| pointType == WorldPointType.WAR_FLAG_POINT_VALUE
				|| pointType == WorldPointType.CROSS_FORTRESS_VALUE
				|| pointType == WorldPointType.PYLON_VALUE
				|| pointType == WorldPointType.CHRISTMAS_BOX_VALUE;
		if (!canSpy) {
			
			logger.error("world spy failed,pointType can not be spy, playerId:{}, pointType:{}", player.getId(), pointType);
			sendError(hpCode, Status.Error.WORLD_POINT_SPY_NOT_ALLOWED);
			return false;
		}

		// 据点类型的侦查
		if (pointType == WorldPointType.KING_PALACE_VALUE 
				|| pointType == WorldPointType.CAPITAL_TOWER_VALUE
				|| pointType == WorldPointType.GUILD_TERRITORY_VALUE
				|| pointType == WorldPointType.SUPER_WEAPON_VALUE
				|| pointType == WorldPointType.XIAO_ZHAN_QU_VALUE
				|| pointType == WorldPointType.WAR_FLAG_POINT_VALUE
				|| pointType == WorldPointType.CROSS_FORTRESS_VALUE) {
			return true;
		}

		// 没有玩家id不能侦查
		String targetPlayerId = targetPoint.getPlayerId();
		if (HawkOSOperator.isEmptyString(targetPlayerId) && pointType != WorldPointType.FOGGY_FORTRESS_VALUE) {
			sendError(hpCode, Status.Error.WORLD_POINT_SPY_NOT_ALLOWED);
			logger.error("world spy failed,point not have playerId, playerId:{}, point:{}", player.getId(), targetPoint.toString());
			return false;
		}

		// 判断是否可以侦查(受保护罩、反侦察保护时不可以侦查)
		if (pointType == WorldPointType.PLAYER_VALUE && HawkTime.getMillisecond() <= targetPoint.getShowProtectedEndTime()) {
			logger.error("world spy failed,target is protected, playerId:{}, point:{}", player.getId(), targetPoint.toString());
			sendError(hpCode, Status.Error.CITY_UNDER_PROTECTED);
			return false;
		}

		// 目标点是否有部队
		if (pointType == WorldPointType.RESOURCE_VALUE || pointType == WorldPointType.QUARTERED_VALUE) {
			String targetPointMarchId = targetPoint.getMarchId();
			IWorldMarch targetPointMarch = WorldMarchService.getInstance().getPlayerMarch(targetPlayerId, targetPointMarchId);
			if (targetPointMarch == null) {
				logger.error("world spy failed, point:{}", targetPoint.toString());
				player.sendError(hpCode, Status.Error.WORLD_POINT_WITHOUT_ARMY, 0);
				return false;
			}
		}
		return true;
	}

	/**
	 * 行军战斗结束，更新攻击方玩家数据
	 * 
	 * @param msg
	 */
	@MessageHandler
	private boolean onUpdateAtkPlayerAfterWar(AtkPlayerAfterWarMsg msg) {
		boolean isAtkWin = msg.isAtkWin();
		List<ArmyInfo> leftList = msg.getLeftList();
		int defMaxFactoryLvl = msg.getDefMaxFactoryLvl();
		doUpdateAtkPlayerAfterWar(isAtkWin, leftList, defMaxFactoryLvl);
		return true;
	}

	/**
	 * 更新战斗发起者的任务和统计数据
	 * 
	 * @param isAtkWin
	 *            战斗胜利
	 * @param armyList
	 *            战后部队信息
	 * @param constrFactorLvl
	 *            敌方大本等级
	 */
	public void doUpdateAtkPlayerAfterWar(final boolean isAtkWin, List<ArmyInfo> armyList, int constrFactorLvl) {
		// 统计部队击杀/死亡数据
		int killCnt = 0;
		int deadCnt = 0;
		// 统计玩家击杀战斗力/损失战斗力
		double destoryPower = 0;
		double lostPower = 0;
		if (armyList != null) {
			for (ArmyInfo info : armyList) {
				
				BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, info.getArmyId());
				if(null != cfg){
					lostPower += cfg.getPower() * (info.getDeadCount() + info.getWoundedCount());
				}
				//计算出来击杀的战斗力
				for( Map.Entry<Integer, Integer> entry : info.getKillInfo().entrySet()){
					BattleSoldierCfg killCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, entry.getKey());
					if(null != killCfg){
						destoryPower += killCfg.getPower() * entry.getValue();
					}
				}
				
				killCnt += info.getKillCount();
				deadCnt += info.getDeadCount();
			}
		}

		if (isAtkWin) {
			player.getData().getStatisticsEntity().addAtkWinCnt(1);
			player.getData().getStatisticsEntity().addWarWinCnt(1);
		} else {
			player.getData().getStatisticsEntity().addAtkLoseCnt(1);
			player.getData().getStatisticsEntity().addWarLoseCnt(1);
		}

		// 是否被封禁击杀排行
		final boolean beBan = RankService.getInstance().isBan(player.getId(), RankType.PLAYER_KILL_ENEMY_RANK);
		if (!beBan) {
			player.getData().getStatisticsEntity().addArmyKillCnt(killCnt);
			GameUtil.scoreBatch(player, ScoreType.KILL_ENEMY, player.getData().getStatisticsEntity().getArmyKillCnt());
		}
		GuildRankMgr.getInstance().onKilledSoldier(player.getId(), (long)Math.ceil(lostPower));
		GuildRankMgr.getInstance().onKillSoldier(player.getId(), (long)Math.ceil(destoryPower));
	
		player.getData().getStatisticsEntity().addArmyLoseCnt(deadCnt);

		final int killPopuFinal = killCnt;

		if (killPopuFinal > 0) {
			// 刷新领主击杀榜单
			final long killCount = player.getData().getStatisticsEntity().getArmyKillCnt();
			player.updateRankScore(MsgId.PLAYER_KILL_RANK_REFRESH, RankType.PLAYER_KILL_ENEMY_RANK, killCount);
			
			
			if (player.hasGuild() && !player.isZeroEarningState()) {
				GuildService.getInstance().changeGuildMemeberKillCount(player.getGuildId(), player.getId(), killCount);
			}
		}

	}

	/**
	 * 行军战斗结束，更新防守方玩家数据
	 * 
	 * @param msg
	 */
	@MessageHandler
	private boolean onUpdateDefPlayerAfterWar(DefPlayerAfterWarMsg msg) {
		boolean isAtkWin = msg.isAtkWin();
		List<ArmyInfo> leftArmyList = msg.getLeftArmyList();
		ConsumeItems consumeItems = msg.getConsumeItems();
		if (consumeItems != null) {
			defPlayerResLose(consumeItems);
		}

		int killCnt = 0;
		int deadCnt = 0;
		// 统计玩家击杀战斗力/损失战斗力
		double destoryPower = 0;
		double lostPower = 0;
		if (leftArmyList != null) {
			for (ArmyInfo armyInfo : leftArmyList) {
				
				killCnt += armyInfo.getKillCount();
				deadCnt += armyInfo.getDeadCount();
				//统计排行榜数据
				BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyInfo.getArmyId());
				if(null != cfg){
					lostPower += cfg.getPower() * (armyInfo.getDeadCount() + armyInfo.getWoundedCount());
				}
				//计算出来击杀的战斗力
				for( Map.Entry<Integer, Integer> entry : armyInfo.getKillInfo().entrySet()){
					BattleSoldierCfg killCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, entry.getKey());
					if(null != killCfg){
						destoryPower += killCfg.getPower() * entry.getValue();
					}
				}
			}
		}
		
		GuildRankMgr.getInstance().onKilledSoldier(player.getId(), (long)Math.ceil(lostPower));
		GuildRankMgr.getInstance().onKillSoldier(player.getId(), (long)Math.ceil(destoryPower));
		
		//资源掠夺
		if (isAtkWin) {
			player.getData().getStatisticsEntity().addWarLoseCnt(1);
			player.getData().getStatisticsEntity().addDefLoseCnt(1);
		} else {
			player.getData().getStatisticsEntity().addWarWinCnt(1);
			player.getData().getStatisticsEntity().addDefWinCnt(1);
		}
		
		// 是否被封禁击杀排行
		final boolean beBan = RankService.getInstance().isBan(player.getId(), RankType.PLAYER_KILL_ENEMY_RANK);
		if (!beBan) {
			player.getData().getStatisticsEntity().addArmyKillCnt(killCnt);
			GameUtil.scoreBatch(player, ScoreType.KILL_ENEMY, player.getData().getStatisticsEntity().getArmyKillCnt());
		}
		player.getData().getStatisticsEntity().addArmyLoseCnt(deadCnt);

		// 刷新领主击杀榜单
		final long armyKillCnt = player.getData().getStatisticsEntity().getArmyKillCnt();
		if (killCnt > 0) {
			player.updateRankScore(MsgId.PLAYER_KILL_RANK_REFRESH, RankType.PLAYER_KILL_ENEMY_RANK, armyKillCnt);
			// 刷新联盟成员击杀数据
			if (player.hasGuild() && !player.isZeroEarningState()) {
				GuildService.getInstance().changeGuildMemeberKillCount(player.getGuildId(), player.getId(), armyKillCnt);
			}
		}
		MissionManager.getInstance().postMsg(player, new EventKillSolider(killCnt));
		
		long resWeight = calcResWeight(consumeItems);
		if(resWeight > 0){
				GuildRankMgr.getInstance().onRobbedRes(player.getId(), resWeight );
		}
		
		return true;
	}
	
	/**
	 * 计算被抢夺资源负重
	 * @param consumeItems
	 * @return
	 */
	private long calcResWeight(ConsumeItems consumeItems) {
		if (consumeItems == null) {
			return 0;
		}
		long resWeight = 0;
		List<ConsumeItem> consumeItemList = consumeItems.getBuilder().getConsumeItemList();
		// 矿石
		int goldoreLost = (int) consumeItems.getBuilder().getAttrInfo().getGoldoreUnsafe();
		// 石油
		int oilLost = (int) consumeItems.getBuilder().getAttrInfo().getOilUnsafe();
		// 稀土
		int steelLost = (int) consumeItems.getBuilder().getAttrInfo().getSteelUnsafe();
		// 合金
		int tombarthiteLost = (int) consumeItems.getBuilder().getAttrInfo().getTombarthiteUnsafe();

		if (null != consumeItemList) {

			resWeight += (goldoreLost * WorldMarchConstProperty.getInstance().getResWeightByType(Const.PlayerAttr.GOLDORE_UNSAFE_VALUE));
			resWeight += (oilLost * WorldMarchConstProperty.getInstance().getResWeightByType(Const.PlayerAttr.OIL_UNSAFE_VALUE));
			resWeight += (steelLost * WorldMarchConstProperty.getInstance().getResWeightByType(Const.PlayerAttr.STEEL_UNSAFE_VALUE));
			resWeight += (tombarthiteLost * WorldMarchConstProperty.getInstance().getResWeightByType(Const.PlayerAttr.TOMBARTHITE_UNSAFE_VALUE));

			for (ConsumeItem item : consumeItemList) {
				int typeWeight = WorldMarchConstProperty.getInstance().getResWeightByType(item.getItemId());
				resWeight += item.getCount() * typeWeight;
			}
		}
		return resWeight;
	}
	
	/**
	 * 防守玩家资源损失计算
	 * 
	 * @param loseResources 资源损失量
	 */
	private void defPlayerResLose(ConsumeItems loseResources) {
		loseResources.checkConsume(player);
		SyncAttrInfo syncAttrInfo = loseResources.getBuilder().getAttrInfo();
		// 被掠夺总量
		long lostGoldore = syncAttrInfo.getGoldoreUnsafe() + syncAttrInfo.getGoldore() + syncAttrInfo.getGoldoreNotEnough();
		long lostOil = syncAttrInfo.getOilUnsafe() + syncAttrInfo.getOil() + syncAttrInfo.getOilNotEnough();
		long lostSteel = syncAttrInfo.getSteelUnsafe() + syncAttrInfo.getSteel() + syncAttrInfo.getSteelNotEnough();
		long lostTombarthite = syncAttrInfo.getTombarthiteUnsafe() + syncAttrInfo.getTombarthite() + syncAttrInfo.getTombarthiteNotEnough();
		
		if (lostGoldore <= 0 && lostOil <= 0 && lostSteel <= 0 && lostTombarthite <= 0) {
			return;
		}
		
		// 资源田已产出还未收取的量
		Map<Integer, Long> outputRes = player.getResBuildOutput();
		long storeGoldore = outputRes.get(PlayerAttr.GOLDORE_UNSAFE_VALUE);
		long storeOil = outputRes.get(PlayerAttr.OIL_UNSAFE_VALUE);
		long storeSteel = outputRes.get(PlayerAttr.STEEL_UNSAFE_VALUE);
		long storeTombarthite = outputRes.get(PlayerAttr.TOMBARTHITE_UNSAFE_VALUE);
		
		// 从已产出的资源中扣除的量
		long loststoreGoldore = Math.min(lostGoldore, storeGoldore);
		long loststoreOil = Math.min(lostOil, storeOil);
		long loststoreSteel = Math.min(lostSteel, storeSteel);
		long loststoreTombarthite = Math.min(lostTombarthite, storeTombarthite);
		// 更新产出收取时间
		if (loststoreGoldore > 0) {
			player.decResStoreByPercent(BuildingType.ORE_REFINING_PLANT, loststoreGoldore * 1D / storeGoldore);
		}
		if (loststoreOil > 0) {
			player.decResStoreByPercent(BuildingType.OIL_WELL, loststoreOil * 1D / storeOil);
		}
		if (loststoreSteel > 0) {
			player.decResStoreByPercent(BuildingType.STEEL_PLANT, loststoreSteel * 1D / storeSteel);
		}
		if (loststoreTombarthite > 0) {
			player.decResStoreByPercent(BuildingType.RARE_EARTH_SMELTER, loststoreTombarthite * 1D / storeTombarthite);
		}

		// 家里的非安全资源被掠夺量
		loseResources = ConsumeItems.valueOf();
		long lostUnsafeGoldore = Math.min(player.getResbyType(PlayerAttr.GOLDORE_UNSAFE), lostGoldore - loststoreGoldore);
		loseResources.addConsumeInfo(PlayerAttr.GOLDORE_UNSAFE, lostUnsafeGoldore);
		
		long lostUnsafeOil = Math.min(player.getResbyType(PlayerAttr.OIL_UNSAFE), lostOil - loststoreOil);
		loseResources.addConsumeInfo(PlayerAttr.OIL_UNSAFE, lostUnsafeOil);
		
		long lostUnsafeTombarthite = Math.min(player.getResbyType(PlayerAttr.TOMBARTHITE_UNSAFE), lostTombarthite - loststoreTombarthite);
		loseResources.addConsumeInfo(PlayerAttr.TOMBARTHITE_UNSAFE, lostUnsafeTombarthite);
		
		long lostUnsafeSteel = Math.min(player.getResbyType(PlayerAttr.STEEL_UNSAFE), lostSteel - loststoreSteel);
		loseResources.addConsumeInfo(PlayerAttr.STEEL_UNSAFE, lostUnsafeSteel);

		if (loseResources.checkConsume(player)) {
			loseResources.consumeAndPush(player, Action.ATTACKED_BY_PLAYER);
		}
	}
	
	/**
	 * 发起联盟领地行军
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILD_MANOR_MARCH_C_VALUE)
	private boolean onWorldManorMarch(HawkProtocol protocol) {
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(req.getPosX(), req.getPosY());
		// check点数据
		if (point == null || point.getPointType() != WorldPointType.GUILD_TERRITORY_VALUE) {
			GuildManorService.logger.error("[onWorldManorMarch]world march manor failed, point:{}", point);
			sendError(protocol.getType(), Status.Error.WORLD_POINT_NOT_MANOR_VALUE);
			return false;
		}
		int marchType = req.getType().getNumber();
		// 行军类型
		ManorMarchEnum manorMarch = ManorMarchEnum.valueOf(marchType);
		if (manorMarch == null) {
			GuildManorService.logger.error("[onWorldManorMarch]world march manor failed, type:{}", req.getType());
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_REQ_TYPE_ERROR);
			return false;
		}

		String guildBuildId = point.getGuildBuildId();
		// 不同类型的行军各自检查
		int resCode = manorMarch.checkMarch(point, player, false);
		if (resCode != Status.SysError.SUCCESS_OK_VALUE) {
			sendError(protocol.getType(), resCode);
			return false;
		}

		// 带兵行军通用检查
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>();
		// 常规检查
		if (!checkMarchReq(req, protocol.getType(), armyList, point)) {
			GuildManorService.logger.error("[onWorldManorMarch]world build manor failed,check march req failed,armyList:{}", armyList);
			return false;
		}

		// 扣兵
		if (!ArmyService.getInstance().checkArmyAndMarch(player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			GuildManorService.logger.error("[onWorldManorMarch]world build manor failed,deduct army failed, armyList:{}", armyList);
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_ARMY_COUNT);
			return false;
		}

		IWorldMarch march = WorldMarchService.getInstance().startMarch(player, marchType, point.getId(), guildBuildId, null, 0, new EffectParams(req, armyList));
		if (march == null) {
			return false;
		}
		
		// 添加行军
		manorMarch.addMarch(march, guildBuildId);
		// 行为日志
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_MANOR_MARCH_START,
				Params.valueOf("marchData", march));

		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		protocol.response(HawkProtocol.valueOf(HP.code.GUILD_MANOR_MARCH_S_VALUE, builder));

		return true;
	}

	/**
	 * 获取驻军列表，包括建设
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GET_WORLD_POINT_PB_C_VALUE)
	private boolean onGetWorldPointPB(HawkProtocol protocol) {
		FetchInviewWorldPoint req = protocol.parseProtocol(FetchInviewWorldPoint.getDefaultInstance());
		// 通知状态
		WorldPointService.getInstance().notifyPointUpdate(req.getX(), req.getY());
		return true;
	}

	/**
	 * 开始尤里探索行军
	 */
	@ProtocolHandler(code = HP.code.WORLD_YURI_EXPLORE_C_VALUE)
	private boolean onYuriExploreStart(HawkProtocol protocol) {
		sendError(protocol.getType(), Status.Error.EXPLORE_TIME_INVAILD);
		return false;
	}
	
	
	/**
	 * 开始藏兵行军
	 */
	@ProtocolHandler(code = HP.code.HIDEEN_MARCH_C_VALUE)
	private boolean onHidenMarchStart(HawkProtocol protocol) {
		// 判断玩家是否开启藏兵洞建筑
		if (player.getData().getBuildingEntityByType(BuildingType.SOLDIER_CAVE) == null) {
			return false;
		}
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		int waitTime = req.getMassTime();
		// 检查探索时间
		if (!ConstProperty.getInstance().checkExploreTime(waitTime)) {
			logger.error("hidden march create failed , marchType:{}  waitTime :{} ", req.getType(), waitTime);
			sendError(protocol.getType(), Status.Error.EXPLORE_TIME_INVAILD);
			return false;
		}
		// 检查是否已经有帝陵行军，有的话则不能再次发起
		BlockingQueue<IWorldMarch> playerMarchs = WorldMarchService.getInstance().getPlayerMarch(player.getId());
		for (IWorldMarch iWorldMarch : playerMarchs) {
			if(iWorldMarch.getMarchType() == WorldMarchType.HIDDEN_MARCH){
				sendError(protocol.getType(), Status.Error.EXPLORE_MARCH_LIMITED);
				return false;
			}
		} 
		// 部队请求检查
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>();
		if (!checkMarchReq(req, protocol.getType(), armyList, null)) {
			logger.error("world quartered failed, checkMarchReq fail");
			return false;
		}
		// 扣兵
		if (!ArmyService.getInstance().checkArmyAndMarch(player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			logger.error("world quartered failed, armyList:{}", armyList);
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_ARMY_COUNT);
			return false;
		}
		// 目标信息 ,开启探索行军
		IWorldMarch march = WorldMarchService.getInstance().startSelfMarch(player, WorldMarchType.HIDDEN_MARCH_VALUE, 0, new EffectParams(req, armyList));
		if (march == null) {
			return false;
		}
		
		// 此处设置resStartTime是为了节省字段，保存行军的探索时间，等到达之后取出对应的字段，在计算整个行军的持续时间
		march.getMarchEntity().setResStartTime(waitTime * 1000L);
		// 行为日志
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_START_YURI_EXPLORE, Params.valueOf("marchData", march));
		// 回复协议
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		protocol.response(HawkProtocol.valueOf(HP.code.HIDEEN_MARCH_S_VALUE, builder));

		LogUtil.logTibetanArmyHole(player, (long) GameUtil.getArmyPower(march.getArmys()), player.getData().getPowerElectric().getArmyBattlePoint(), waitTime, false);
		return true;
	}
	
	/**
	 * 开启国王战单人行军
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.PRESIDENT_SINGLE_MARCH_C_VALUE)
	private boolean onPresidentSingleMarch(HawkProtocol protocol) {
		// 非战斗时间
		if (!PresidentFightService.getInstance().isFightPeriod()) {
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_PRESIDENT_NOT_FIGHT_TIME);
			return false;
		}
		
		// 跨服行军类型拦截
		if (WorldMarchService.getInstance().isCrossMarchLimit(player, WorldMarchType.PRESIDENT_SINGLE)) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}
		
		// 玩家没有联盟
		if (!player.hasGuild()) {
			return false;
		}
		
		boolean isNationMarch = false;
//		if (CrossActivityService.getInstance().isOpen()) {
//			String serverId = player.getMainServerId();
//			String guildId = player.getGuildId();
//			if (!RedisProxy.getInstance().isCrossFightGuild(serverId, guildId)) {
//				sendError(protocol.getType(), Status.CrossServerError.CROSS_HAVE_NO_FIGHT_PERM_VALUE);
//				return false;
//			}
//			isNationMarch = true;
//		}
		
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		
		// 带兵出征通用检查
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>();
		if (!checkMarchReq(req, protocol.getType(), armyList, null, isNationMarch)) {
			return false;
		}
		
		// 扣兵
		if (!ArmyService.getInstance().checkArmyAndMarch(player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_ARMY_COUNT);
			return false;
		}
		
		// 王城坐标
		int pointId = WorldMapConstProperty.getInstance().getCenterPointId();
		
		IWorldMarch march = WorldMarchService.getInstance().startMarch(player, WorldMarchType.PRESIDENT_SINGLE_VALUE, pointId, "", null, 0, new EffectParams(req, armyList));

		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_PRESIDENT_SINGLE_MARCH, Params.valueOf("marchData", march));
		
		return true;
	}
	
	/**
	 * 开启国王战箭塔单人行军
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.PRESIDENT_TOWER_SINGLE_MARCH_C_VALUE)
	private boolean onPresidentTowerSingleMarch(HawkProtocol protocol) {
		// 非战斗时间
		if (!PresidentFightService.getInstance().isFightPeriod()) {
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_PRESIDENT_NOT_FIGHT_TIME);
			return false;
		}
		
		// 跨服行军类型拦截
		if (WorldMarchService.getInstance().isCrossMarchLimit(player, WorldMarchType.PRESIDENT_TOWER_SINGLE)) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}

		// 玩家没有联盟
		if (!player.hasGuild()) {
			return false;
		}
		
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		
		// 箭塔坐标
		int towerPointId = GameUtil.combineXAndY(req.getPosX(), req.getPosY());
		if (GsConst.PresidentTowerPointId.valueOf(towerPointId) == null) {
			return false;
		}
		
		boolean isNationMarch = false;
		if (CrossActivityService.getInstance().isOpen()) {
			isNationMarch = true;
		}
		
		// 带兵出征通用检查
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>();
		if (!checkMarchReq(req, protocol.getType(), armyList, null, isNationMarch)) {
			return false;
		}
		
		// 扣兵
		if (!ArmyService.getInstance().checkArmyAndMarch(player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_ARMY_COUNT);
			return false;
		}
		
		IWorldMarch march = WorldMarchService.getInstance().startMarch(player, WorldMarchType.PRESIDENT_TOWER_SINGLE_VALUE, towerPointId, "", null, 0, new EffectParams(req, armyList));

		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_PRESIDENT_TOWER_SINGLE_MARCH, Params.valueOf("marchData", march));
		return true;
	}
	
	/**
	 * 玩家据点行军
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.STRONGPOINT_MARCH_C_VALUE)
	private boolean onWorldStrongpointMarchStart(HawkProtocol protocol) {
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		
		// 目标点
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(req.getPosX(), req.getPosY());

		// 坐标点检查
		if (point == null || point.getPointType() != WorldPointType.STRONG_POINT_VALUE) {
			logger.error("gen strongpoint march failed, point null, playerId:{}, x:{}, y:{}", player.getId(), req.getPosX(), req.getPosY());
			sendError(protocol.getType(), Status.Error.WORLD_POINT_NOT_EXIST);
			return false;
		}

		// 需要体力值
		WorldStrongpointCfg strongPointCfg = HawkConfigManager.getInstance().getConfigByKey(WorldStrongpointCfg.class, point.getMonsterId());
		ConsumeItems consumeItems = ConsumeItems.valueOf(PlayerAttr.VIT, strongPointCfg.getStrongpointCost());

		// 体力判断
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			logger.error("gen strongpoint march failed, vit not enough, playerId:{}, vit:{}", player.getId(), player.getVit());
			return false;
		}
		
		// 带兵出征通用检查
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>();
		if (!checkMarchReq(req, protocol.getType(), armyList, point)) {
			logger.error("gen strongpoint march failed, check army req filed, playerId:{}, req:{}", player.getId(), req.toString());
			return false;
		}

		// 扣兵
		if (!ArmyService.getInstance().checkArmyAndMarch(player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			logger.error("gen strongpoint march failed, deduct army failed, player:{}, req:{}", player.getId(), req.toString());
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_ARMY_COUNT);
			return false;
		}

		// 生成行军
		IWorldMarch march = WorldMarchService.getInstance().startMarch(player, WorldMarchType.STRONGPOINT_VALUE, point.getId(), String.valueOf(point.getMonsterId()), null, 0, new EffectParams(req, armyList));
		
		// 扣除体力
		consumeItems.consumeAndPush(player, Action.WORLD_STRONGPOINT_MARCH);
		
		march.getMarchEntity().setVitCost(strongPointCfg.getStrongpointCost());
		
		// 回复协议
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		protocol.response(HawkProtocol.valueOf(HP.code.STRONGPOINT_MARCH_S_VALUE, builder));

		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_STRONGPOINT_MARCH, Params.valueOf("marchData", march));
		return true;
	}
	
	/**
	 * 退出联盟行军处理
	 * @return
	 */
	@MessageHandler
	private boolean onGuildQuitMsg(GuildQuitMsg msg) {
		try {
			BlockingQueue<IWorldMarch> playerMarchs = WorldMarchService.getInstance().getPlayerMarch(player.getId());
			for (IWorldMarch playerMarch : playerMarchs) {
				playerMarch.doQuitGuild(msg.getGuildId());
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return true;
	}
	
	/**
	 * 攻击新版怪物
	 */
	@ProtocolHandler(code = HP.code.WORLD_NEW_MONSTER_MARCH_C_VALUE)
	private boolean onWorlNewMonsterMarchStart(HawkProtocol protocol) {
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		int posX = req.getPosX();
		int posY = req.getPosY();
		int attackModel = req.getAttackModel();
		boolean atkOnce = attackModel == 0;

		// 多次攻击作用号判断
		int multAtkEff = player.getEffect().getEffVal(EffType.MULT_ATK_MONSTER, new EffectParams(req, new ArrayList<>()));
		if (!atkOnce && multAtkEff < 1 && !GameUtil.checkSysFunctionOpen(player, SysFunctionModuleId.MONSTERATKMORE)) {
			return false;
		}
		
		// 目标点
		WorldPoint targetPoint = WorldPointService.getInstance().getWorldPoint(posX, posY);

		// 目标点为空
		if (targetPoint == null) {
			sendError(protocol.getType(), Status.Error.TARGET_MONSTER_DISAPPEAR);
			WorldScene.getInstance().move(player.getAoiObjId(), posX, posY, 0.0f);
			return false;
		}

		// 目标点不是怪物
		if (targetPoint.getPointType() != WorldPointType.MONSTER_VALUE) {
			sendError(protocol.getType(), Status.Error.WORLD_POINT_NOT_MONSTER);
			logger.error("world attack monster req failed, point not monster, x:{}, y:{}, pointType:{}", posX, posY, targetPoint.getPointType());
			return false;
		}

		// 野怪配置
		WorldEnemyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, targetPoint.getMonsterId());
		if (cfg == null || cfg.getType() != MonsterType.TYPE_7_VALUE) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			logger.error("world attack monster failed, cfg null, monsterId：{}", targetPoint.getMonsterId());
			return false;
		}

		// 等级不足
		if (player.getLevel() < cfg.getLowerLimit()) {
			logger.error("world attack monster failed,playerLevel:{} ,lowerLimit:{}", player.getLevel(), cfg.getLowerLimit());
			sendError(protocol.getType(), Status.Error.PLAYER_LEVEL_NOT_ENOUGH);
			return false;
		}

		// 体力消耗
		int vitCost = cfg.getCostPhysicalPower();
		if (vitCost == 0) {
			logger.error("onWorlNewMonsterMarchStart, vitCost error, monsterId:{}", cfg.getId());
			vitCost = 1;
		}
		
		int atkTimes = 1;
		if (!atkOnce) {
			atkTimes = WorldMapConstProperty.getInstance().getNewMonsterAttackNumber();
			vitCost = vitCost * atkTimes;
			
		}
		
		ConsumeItems consumeItems = ConsumeItems.valueOf(PlayerAttr.VIT, vitCost);
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			logger.error("world attack monster failed, vit not enough vit:{}", player.getVit());
			return false;
		}

		// 检测出征的军队信息
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>(); // 出征的士兵列表
		if (!checkMarchReq(req, protocol.getType(), armyList, targetPoint)) {
			logger.error("world attack monster failed,armyList:{}", armyList);
			return false;
		}

		// 扣兵
		if (!ArmyService.getInstance().checkArmyAndMarch(player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			logger.error("world attack monster failed, deduct armyList:{}", armyList);
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_ARMY_COUNT);
			return false;
		}

		// 开启行军,目标放怪物ID
		String targetId = String.valueOf(targetPoint.getMonsterId());
		IWorldMarch march = WorldMarchService.getInstance().startMarch(player, WorldMarchType.NEW_MONSTER_VALUE, targetPoint.getId(), targetId, null, 0, new EffectParams(req, armyList));
		if (march == null) {
			// 这里只有机器人模式下才会为null
			return false;
		}
		march.getMarchEntity().setAttackTimes(atkTimes);
		march.getMarchEntity().setVitCost(vitCost);
		
		// 扣除体力
		consumeItems.consumeAndPush(player, Action.FIGHT_MONSTER);
		
		// 增加打新版野怪次数
		PlayerMonsterEntity monsterEntity = player.getData().getMonsterEntity();
		monsterEntity.addAttackNewMonsterTimes();
		player.getPush().syncMonsterKilled(cfg.getId(), true);
		
		// 行为日志
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_START_FIGHT_MONSTER, Params.valueOf("marchData", march));
		// 返回
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		protocol.response(HawkProtocol.valueOf(HP.code.WORLD_NEW_MONSTER_MARCH_S_VALUE, builder));

		return true;
	}
	
	/**
	 * 超级武器(名城)单人行军
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.SUPER_WEAPON_SINGLE_MARCH_C_VALUE)
	private boolean onSuperWeaponSingleMarch(HawkProtocol protocol) {
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		// 阶段开启检测
		if (SuperWeaponService.getInstance().getStatus() != SuperWeaponPeriod.WARFARE_VALUE) {
			return false;
		}
		
		// 跨服行军类型拦截
		if (WorldMarchService.getInstance().isCrossMarchLimit(player, WorldMarchType.SUPER_WEAPON_SINGLE)) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}
		
		// 联盟检测
		if (!player.hasGuild()) {
			return false;
		}
		// 坐标点检测
		int pointId = GameUtil.combineXAndY(req.getPosX(), req.getPosY());
		if (!SuperWeaponService.getInstance().isSuperWeaponPoints(pointId)) {
			return false;
		}
		
		// 报名检测
		IWeapon superWeapon = SuperWeaponService.getInstance().getWeapon(pointId);
		if (!superWeapon.canAttack(player.getGuildId()) && !GsConfig.getInstance().isRobotMode()) {
			sendError(protocol.getType(), Status.SuperWeaponError.SUPER_WEAPON_NOT_SIGN_UP);
			return false;
		}
		
		// 带兵出征通用检查
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>();
		if (!checkMarchReq(req, protocol.getType(), armyList, null)) {
			return false;
		}
		// 扣兵
		if (!ArmyService.getInstance().checkArmyAndMarch(player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_ARMY_COUNT);
			return false;
		}
		IWorldMarch march = WorldMarchService.getInstance().startMarch(player, WorldMarchType.SUPER_WEAPON_SINGLE_VALUE, pointId, "", null, 0, new EffectParams(req, armyList));
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_PRESIDENT_SINGLE_MARCH, Params.valueOf("marchData", march));
		return true;
	}
	
	
	/**
	 * 超级武器(名城)单人行军
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.XZQ_SINGLE_MARCH_C_VALUE)
	private boolean onXZQSingleMarch(HawkProtocol protocol) {
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		// 阶段开启检测
		int pointId = GameUtil.combineXAndY(req.getPosX(), req.getPosY());
		XZQWorldPoint superWeapon = XZQService.getInstance().getXZQPoint(pointId);
		if (superWeapon.isPeace()) {
			return false;
		}
		
		// 跨服行军类型拦截
		if (WorldMarchService.getInstance().isCrossMarchLimit(player, WorldMarchType.XZQ_SINGLE)) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}
		
		// 联盟检测
		if (!player.hasGuild()) {
			return false;
		}
		//是否可以攻击
		int canAttackXZQWorldPoint = XZQService.getInstance().canAttackXZQWorldPoint(player, superWeapon);
		if (canAttackXZQWorldPoint > 0) {
			sendError(protocol.getType(), canAttackXZQWorldPoint);
			return false;
		}
		// 带兵出征通用检查
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>();
		if (!checkMarchReq(req, protocol.getType(), armyList, null)) {
			return false;
		}
		// 扣兵
		if (!ArmyService.getInstance().checkArmyAndMarch(player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_ARMY_COUNT);
			return false;
		}
		WorldMarchService.getInstance().startMarch(player, WorldMarchType.XZQ_SINGLE_VALUE, pointId, "", null, 0, new EffectParams(req, armyList));
		//IWorldMarch march = WorldMarchService.getInstance().startMarch(player, WorldMarchType.XZQ_SINGLE_VALUE, pointId, "", null, 0, new EffectParams(req, armyList));
//		LogUtil.actionInfo(player, "marchId,marchType,marchStatus,playerId,leaderId,targetId,originId,terminalId,startTime", 
//				Action.WORLD_PRESIDENT_SINGLE_MARCH, 
//				march.getMarchId(), march.getMarchType(), march.getMarchStatus(), march.getPlayerId(),
//				march.getMarchEntity().getLeaderPlayerId(), march.getMarchEntity().getTargetId(),
//				march.getOrigionId(), march.getTerminalId(), march.getStartTime());
		return true;
	}
	
	/**
	 * 幽灵基地单人行军
	 */
	@ProtocolHandler(code = HP.code.WORLD_FOGGY_SIGNLE_C_VALUE)
	private boolean onWorldFoggySingleMarchStart(HawkProtocol protocol) {
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		int posX = req.getPosX();
		int posY = req.getPosY();
		WorldPoint targetPoint = WorldPointService.getInstance().getWorldPoint(posX, posY);
		
		// 目标点不存在
		if (targetPoint == null) {
			sendError(protocol.getType(), Status.Error.TARGET_MONSTER_DISAPPEAR);
			WorldScene.getInstance().move(player.getAoiObjId(), posX, posY, 0.0f);
			return false;
		}
		// 不是幽灵基地
		if (targetPoint.getPointType() != WorldPointType.FOGGY_FORTRESS_VALUE) {
			return false;
		}
		// 活动中，不能攻打
		if(WorldFoggyFortressService.getInstance().checkPointIsInActive(targetPoint.getId())){
			sendError(protocol.getType(), Status.Error.FOGGY_CAN_NOT_ATTACK_VALUE);
			return false;
		}
		// 检测出征的军队信息
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>(); // 出征的士兵列表
		if (!checkMarchReq(req, protocol.getType(), armyList, targetPoint)) {
			logger.error("world attack monster failed,armyList:{}", armyList);
			return false;
		}
		// 扣兵
		if (!ArmyService.getInstance().checkArmyAndMarch(player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			logger.error("world attack monster failed, deduct armyList:{}", armyList);
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_ARMY_COUNT);
			return false;
		}
		// 发起行军
		IWorldMarch march = WorldMarchService.getInstance().startMarch(player, WorldMarchType.FOGGY_SINGLE_VALUE, targetPoint.getId(), String.valueOf(targetPoint.getMonsterId()), null, 0, new EffectParams(req, armyList));
		
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(march != null);
		protocol.response(HawkProtocol.valueOf(HP.code.WORLD_FOGGY_SIGNLE_S, builder));
		return true;
	}
	
	/**
	 * 行军详情(暂时只支持集结行军，传集结行军marchId)
	 */
	@ProtocolHandler(code = HP.code.MARCH_ARMY_DETAIL_REQ_VALUE)
	private boolean onMarchArmyDetailInfo(HawkProtocol protocol) {
		MarchArmyDetailReq req = protocol.parseProtocol(MarchArmyDetailReq.getDefaultInstance());
		String marchId = req.getMarchId();
		
		// 参数错误
		if (HawkOSOperator.isEmptyString(marchId)) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return false;
		}
		
		// 没有这条行军
		IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
		if (march == null) {
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_NOT_EXISIT);
			return false;
		}
		
		// 不是集结行军(暂时只支持集结行军，传集结行军marchId)
		if (!march.isMassMarch()) {
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_NOT_MASS);
			return false;
		}
		
		// 不是行军中
		if (!march.isMarchState()) {
			sendError(protocol.getType(), Status.Error.NOT_MARCH_STATE);
			return false;
		}
		
		Set<IWorldMarch> allMarch = new HashSet<IWorldMarch>();
		allMarch.add(march);
		allMarch.addAll(WorldMarchService.getInstance().getMassJoinMarchs(march, true));

		boolean canReq = false;
		
		// 是否有自己的行军
		for (IWorldMarch thisMarch : allMarch) {
			if (!player.getId().equals(thisMarch.getPlayerId())) {
				continue;
			}
			canReq = true;
			break;
		}
		
		// 没有自己的行军，不能查看
		if (!canReq) {
			sendError(protocol.getType(), Status.Error.HAVE_NO_OWEN_MARCH);
			return false;
		}
		
		MarchArmyDetailResp.Builder builder = MarchArmyDetailResp.newBuilder();
		for (IWorldMarch thisMarch : allMarch) {
			MarchArmyDetailInfo.Builder info = MarchArmyDetailInfo.newBuilder();
			Player player = thisMarch.getPlayer();
			if (player == null || player.getName() == null) {
				continue;
			}
			
			info.setPlayerName(player.getName());
			for (PlayerHero hero : thisMarch.getHeros()) {
				info.addHeros(hero.toArmyHeroPb());
				info.addHeroList(hero.toPBobj());
			}
			
			for (ArmyInfo army : thisMarch.getArmys()) {
				info.addArmys(army.toArmySoldierPB(player));
			}
			Optional<SuperSoldier> ssoldierOp = player.getSuperSoldierByCfgId(thisMarch.getSuperSoldierId());
			if(ssoldierOp.isPresent()){
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
	 * 攻击机甲
	 */
	@ProtocolHandler(code = HP.code.WORLD_GUNDAM_SINGLE_MARCH_C_VALUE)
	private boolean onWorlGundamMarchStart(HawkProtocol protocol) {
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		int posX = req.getPosX();
		int posY = req.getPosY();
		
		// 目标点
		WorldPoint targetPoint = WorldPointService.getInstance().getWorldPoint(posX, posY);

		// 目标点为空
		if (targetPoint == null) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			WorldScene.getInstance().move(player.getAoiObjId(), posX, posY, 0.0f);
			return false;
		}

		// 目标点不是机甲
		if (targetPoint.getPointType() != WorldPointType.GUNDAM_VALUE) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return false;
		}

		// 跨服行军类型拦截
		if (WorldMarchService.getInstance().isCrossMarchLimit(player, WorldMarchType.GUNDAM_SINGLE)) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}

		// 攻击机甲次数限制
		if (isAtkGundamTimesLimit(player.getId())) {
			sendError(protocol.getType(), Status.Error.ATK_GUNDAM_TIMES_LIMIT_VALUE);
			return false;
		}
		
		// 机甲配置
		WorldGundamCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WorldGundamCfg.class, targetPoint.getMonsterId());
		if (cfg == null) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return false;
		}

		// 体力消耗
		int vitCost = cfg.getCostPhysicalPower();
		ConsumeItems consumeItems = ConsumeItems.valueOf(PlayerAttr.VIT, vitCost);
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			logger.error("world attack monster failed, vit not enough vit:{}", player.getVit());
			return false;
		}

		// 检测出征的军队信息
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>(); // 出征的士兵列表
		if (!checkMarchReq(req, protocol.getType(), armyList, targetPoint)) {
			logger.error("world attack monster failed,armyList:{}", armyList);
			return false;
		}

		// 扣兵
		if (!ArmyService.getInstance().checkArmyAndMarch(player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			logger.error("world attack monster failed, deduct armyList:{}", armyList);
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_ARMY_COUNT);
			return false;
		}

		// 开启行军,目标放怪物ID
		String targetId = String.valueOf(targetPoint.getMonsterId());
		IWorldMarch march = WorldMarchService.getInstance().startMarch(player, WorldMarchType.GUNDAM_SINGLE_VALUE, targetPoint.getId(), targetId, null, 0, new EffectParams(req, armyList));
		if (march == null) {
			return false;
		}
		march.getMarchEntity().setVitCost(vitCost);
		
		// 扣除体力
		consumeItems.consumeAndPush(player, Action.FIGHT_MONSTER);
		
		// 返回
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		protocol.response(HawkProtocol.valueOf(HP.code.WORLD_GUNDAM_SINGLE_MARCH_S_VALUE, builder));
		return true;
	}
	
	/**
	 * 圣诞boss
	 */
	@ProtocolHandler(code = HP.code.WORLD_CHRISTMAS_SINGLE_MARCH_C_VALUE)
	private boolean onWorldChristmasMarchStart(HawkProtocol protocol) {
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		int posX = req.getPosX();
		int posY = req.getPosY();
		
		// 目标点
		WorldPoint targetPoint = WorldPointService.getInstance().getWorldPoint(posX, posY);

		// 目标点为空
		if (targetPoint == null) {
			sendError(protocol.getType(), Status.Error.POINT_NOT_EXIST_OR_TYPE_ERROR_VALUE);
			WorldScene.getInstance().move(player.getAoiObjId(), posX, posY, 0.0f);
			
			return false;
		}

		// 目标点不是圣诞.
		if (targetPoint.getPointType() != WorldPointType.CHRISTMAS_BOSS_VALUE) {
			sendError(protocol.getType(), Status.Error.POINT_NOT_EXIST_OR_TYPE_ERROR_VALUE);
			
			return false;
		}

		// 跨服行军类型拦截
		if (WorldMarchService.getInstance().isCrossMarchLimit(player, WorldMarchType.CHRISTMAS_SINGLE)) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			
			return false;
		}

		// 攻击年兽次数限制
		if (isAtkChristmasTimesLimit(player.getId())) {
			sendError(protocol.getType(), Status.Error.CHRISTMAS_ATK_TIMES_LIMIT_VALUE);
			
			return false;
		}
		
		// 年兽配置
		WorldChristmasWarBossCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WorldChristmasWarBossCfg.class, targetPoint.getMonsterId());
		if (cfg == null) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return false;
		}

		// 检测出征的军队信息
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>(); // 出征的士兵列表
		if (!checkMarchReq(req, protocol.getType(), armyList, targetPoint)) {
			logger.error("world attack monster failed,armyList:{}", armyList);
			
			return false;
		}

		// 扣兵
		if (!ArmyService.getInstance().checkArmyAndMarch(player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_ARMY_COUNT);
			return false;
		}

		// 开启行军,目标放怪物ID
		String targetId = String.valueOf(targetPoint.getMonsterId());
		IWorldMarch march = WorldMarchService.getInstance().startMarch(player, WorldMarchType.CHRISTMAS_SINGLE_VALUE, targetPoint.getId(), targetId, null, 0, new EffectParams(req, armyList));
		if (march == null) {
			return false;
		}
		
		// 返回
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		protocol.response(HawkProtocol.valueOf(HP.code.WORLD_CHRISTMAS_SINGLE_MARCH_S_VALUE, builder));
		return true;
	}
	
	
	/**
	 * 攻击机甲
	 */
	@ProtocolHandler(code = HP.code.WORLD_NIAN_SINGLE_MARCH_C_VALUE)
	private boolean onWorlNianMarchStart(HawkProtocol protocol) {
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		int posX = req.getPosX();
		int posY = req.getPosY();
		
		// 目标点
		WorldPoint targetPoint = WorldPointService.getInstance().getWorldPoint(posX, posY);

		// 目标点为空
		if (targetPoint == null) {
			sendError(protocol.getType(), Status.Error.POINT_NOT_EXIST_OR_TYPE_ERROR_VALUE);
			WorldScene.getInstance().move(player.getAoiObjId(), posX, posY, 0.0f);
			return false;
		}

		// 目标点不是机甲
		if (targetPoint.getPointType() != WorldPointType.NIAN_VALUE) {
			sendError(protocol.getType(), Status.Error.POINT_NOT_EXIST_OR_TYPE_ERROR_VALUE);
			return false;
		}

		// 跨服行军类型拦截
		if (WorldMarchService.getInstance().isCrossMarchLimit(player, WorldMarchType.NIAN_SINGLE)) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}

		// 攻击年兽次数限制
		if (isAtkNianTimesLimit(player.getId())) {
			sendError(protocol.getType(), Status.Error.ATK_NIAN_TIMES_LIMIT_VALUE);
			return false;
		}
		
		// 年兽配置
		WorldNianCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WorldNianCfg.class, targetPoint.getMonsterId());
		if (cfg == null) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return false;
		}
		//是否可以单打
		if(cfg.getAllowWorldSolo() <= 0){
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return false;
		}

		// 检测出征的军队信息
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>(); // 出征的士兵列表
		if (!checkMarchReq(req, protocol.getType(), armyList, targetPoint)) {
			logger.error("world attack monster failed,armyList:{}", armyList);
			return false;
		}

		// 扣兵
		if (!ArmyService.getInstance().checkArmyAndMarch(player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_ARMY_COUNT);
			return false;
		}

		// 开启行军,目标放怪物ID
		String targetId = String.valueOf(targetPoint.getMonsterId());
		IWorldMarch march = WorldMarchService.getInstance().startMarch(player, WorldMarchType.NIAN_SINGLE_VALUE, targetPoint.getId(), targetId, null, 0, new EffectParams(req, armyList));
		if (march == null) {
			return false;
		}
		
		// 返回
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		protocol.response(HawkProtocol.valueOf(HP.code.WORLD_NIAN_SINGLE_MARCH_S_VALUE, builder));
		return true;
	}
	
	/**
	 * 攻击高达次数限制
	 * @param player
	 * @return
	 */
	private boolean isAtkGundamTimesLimit(String playerId) {
		String gundamRefreshUuid = WorldGundamService.getInstance().getGundamRefreshUuid();
		int atkGundamTimes = LocalRedis.getInstance().getAtkGundamTimes(gundamRefreshUuid, playerId);
		if (atkGundamTimes >= WorldMarchConstProperty.getInstance().getGundamAtkLimit()) {
			return true;
		}
		return false;
	}
	
	/**
	 * 攻击年兽次数限制
	 * @param player
	 * @return
	 */
	private boolean isAtkNianTimesLimit(String playerId) {
		String nianRefreshUuid = WorldNianService.getInstance().getNianRefreshUuid();
		int atkNianTimes = player.getAtkNianTimes(nianRefreshUuid);
		if (atkNianTimes >= WorldMarchConstProperty.getInstance().getNianAtkLimit()) {
			return true;
		}
		return false;
	}
	
	private boolean isAtkChristmasTimesLimit(String playerId) {
		String nianRefreshUuid = WorldChristmasWarService.getInstance().getRefreshUuid();
		int atkTimes = player.getAtkNianTimes(nianRefreshUuid);
		if (atkTimes >= WorldMarchConstProperty.getInstance().getChristmasAtkLimit()) {
			return true;
		}
		return false;
	}
	
	/**
	 * 霸主膜拜
	 * @param protocol
	 */
	@ProtocolHandler(code=HP.code.OVERLORD_BLESS_GO_REQ_VALUE)
	public void onOverlordBlessingStart(HawkProtocol protocol) {
		// 判断是否可以开启行军
		if (!WorldMarchService.getInstance().isHasFreeMarch(player)) {
			logger.error("overlord blessing failed, has no free march, playerId: {}", player.getId());
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_MAX_LIMIT);
			return;
		}
		
		List<IWorldMarch> marchs = WorldMarchService.getInstance().getPlayerMarch(player.getId(), WorldMarchType.OVERLORD_BLESSING_MARCH_VALUE);
		if (!marchs.isEmpty()) {
			sendError(protocol.getType(), Status.Error.TYPE_MARCH_EXIST_VALUE);
			return;
		}
		
		Optional<OverlordBlessingActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.OVERLORD_BLESS_VALUE);
		if (!opActivity.isPresent()) {
			sendError(protocol.getType(), Status.Error.ACTIVITY_NOT_OPEN_VALUE);
			return;
		}
		
		OverlordBlessingActivity activity = opActivity.get();
		int result = activity.overlordBlessingCheck(player.getId());
		if (result != 0) {
			sendError(protocol.getType(), result);
			return;
		}
		
		int[] coordinates = ConstProperty.getInstance().getStatueCoordinates();
		int terminalId = GameUtil.combineXAndY(coordinates[0], coordinates[1]);
		IWorldMarch march = WorldMarchService.getInstance().startMarch(player, WorldMarchType.OVERLORD_BLESSING_MARCH_VALUE, terminalId, "", null, 0, 0, 0, 0, new EffectParams());
		if (march != null) {
			player.responseSuccess(protocol.getType());
			BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.OVERLORD_BLESSING_START, Params.valueOf("marchData", march));
		}
	}
	
	/**
	 * 玩家寻宝资源点行军
	 */
	@ProtocolHandler(code = HP.code.TREASURE_HUNT_RES_C_VALUE)
	private boolean onWorldTreasureHuntResStart(HawkProtocol protocol) {
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		
		// 目标点
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(req.getPosX(), req.getPosY());

		// 坐标点检查
		if (point == null || point.getPointType() != WorldPointType.TH_RESOURCE_VALUE) {
			sendError(protocol.getType(), Status.Error.WORLD_POINT_NOT_EXIST);
			return false;
		}

		// 向一个点只能出发一条同类型行军
		Set<IWorldMarch> playerTypeMarchs = WorldMarchService.getInstance().getPlayerTypeMarchs(player.getId(), WorldMarchType.TREASURE_HUNT_RESOURCE_VALUE);
		for (IWorldMarch march : playerTypeMarchs) {
			if (march.isMarchState() && march.getTerminalId() == point.getId()) {
				return false;
			}
		}
		
		// 需要体力值
		TreasureHuntResCfg cfg = HawkConfigManager.getInstance().getConfigByKey(TreasureHuntResCfg.class, point.getResourceId());
		ConsumeItems consumeItems = ConsumeItems.valueOf(PlayerAttr.VIT, cfg.getStrongpointCost());

		// 体力判断
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			logger.error("gen strongpoint march failed, vit not enough, playerId:{}, vit:{}", player.getId(), player.getVit());
			return false;
		}

		// 带兵出征通用检查
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>();
		if (!checkMarchReq(req, protocol.getType(), armyList, point)) {
			logger.error("gen strongpoint march failed, check army req filed, playerId:{}, req:{}", player.getId(), req.toString());
			return false;
		}

		// 扣兵
		if (!ArmyService.getInstance().checkArmyAndMarch(player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			logger.error("gen strongpoint march failed, deduct army failed, player:{}, req:{}", player.getId(), req.toString());
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_ARMY_COUNT);
			return false;
		}

		// 生成行军
		IWorldMarch startMarch = WorldMarchService.getInstance().startMarch(player, WorldMarchType.TREASURE_HUNT_RESOURCE_VALUE, point.getId(), String.valueOf(point.getResourceId()), null, 0, new EffectParams(req, armyList));
		startMarch.getMarchEntity().setVitCost(cfg.getStrongpointCost());

		// 扣除体力
		consumeItems.consumeAndPush(player, Action.TREASURE_HUNT_RES_VIT_COST);

		// 回复协议
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		protocol.response(HawkProtocol.valueOf(HP.code.TREASURE_HUNT_RES_S_VALUE, builder));

		return true;
	}
	
	/**
	 * 战地旗帜行军
	 */
	@ProtocolHandler(code = HP.code.WAR_FLAG_MARCH_C_VALUE)
	private boolean onWarFlagMarchStart(HawkProtocol protocol) {
		
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(req.getPosX(), req.getPosY());
		if (point == null) {
			return false;
		}
		
		if (point.getPointType() != WorldPointType.WAR_FLAG_POINT_VALUE) {
			return false;
		}
		
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>();
		if (!checkMarchReq(req, protocol.getType(), armyList, point)) {
			return false;
		}
		
		if (!ArmyService.getInstance().checkArmyAndMarch(player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			return false;
		}
		
		// 生成行军
		WorldMarchService.getInstance().startMarch(player, WorldMarchType.WAR_FLAG_MARCH_VALUE, point.getId(), point.getGuildBuildId(), null, 0, new EffectParams(req, armyList));

		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		protocol.response(HawkProtocol.valueOf(HP.code.WAR_FLAG_MARCH_S_VALUE, builder));

		return true;
	}
	
	/**
	 * 航海远征单人行军
	 */
	@ProtocolHandler(code = HP.code.CROSS_FORTRESS_SINGLE_MARCH_C_VALUE)
	private boolean onFortressSingleMarch(HawkProtocol protocol) {
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		
		// 阶段开启检测
		if (CrossFortressService.getInstance().getCurrentState() != SuperWeaponPeriod.WARFARE_VALUE) {
			return false;
		}
		
		// 联盟检测
		if (!player.hasGuild()) {
			return false;
		}
		
		// 坐标点检测
		int pointId = GameUtil.combineXAndY(req.getPosX(), req.getPosY());
		IFortress fortress = CrossFortressService.getInstance().getFortress(pointId);
		if (fortress == null) {
			return false;
		}
		
		// 带兵出征通用检查
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>();
		if (!checkMarchReq(req, protocol.getType(), armyList, null)) {
			return false;
		}
		
		// 扣兵
		if (!ArmyService.getInstance().checkArmyAndMarch(player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_ARMY_COUNT);
			return false;
		}
		
		WorldMarchService.getInstance().startMarch(player, WorldMarchType.FORTRESS_SINGLE_VALUE, pointId, "", null, 0, new EffectParams(req, armyList));
		
		return true;
	}
	
	/**
	 * 邀请集结
	 */
	@ProtocolHandler(code = HP.code.INVITE_MASS_REQ_VALUE)
	private boolean onInviteMass(HawkProtocol protocol) {
		InviteMassReq req = protocol.parseProtocol(InviteMassReq.getDefaultInstance());
		
		IWorldMarch march = WorldMarchService.getInstance().getMarch(req.getMarchId());
		if (march == null) {
			sendError(protocol.getType(), Status.Error.MASS_ERR_MARCH_NOT_EXIST);
			return false;
		}
		
		if (!march.isMassMarch()) {
			sendError(protocol.getType(), Status.Error.MASS_ERR_MARCH_NOT_EXIST);
			return false;
		}
		
		if (HawkTime.getMillisecond() - player.getData().getLastInviteMassTime() < WorldMarchConstProperty.getInstance().getInviteMassCD()) {
			sendError(protocol.getType(), Status.Error.INVITE_MASS_CD);
			return false;
		}
		
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(march.getMarchEntity().getTerminalId());
		if (worldPoint != null) {
			String targetId = march.getMarchEntity().getTargetId();
			
			GuildFormationObj formationObj = GuildService.getInstance().getGuildFormation(player.getGuildId());
			GuildFormationCell formation = formationObj.getGuildFormation(req.getMarchId());
			if (formation == null) {
				sendNotice(worldPoint, targetId, march, 0, "");
			} else {
				sendNotice(worldPoint, targetId, march, formation.getIndex().getNumber(), formation.getName());
			}
		}
		
		player.getData().setLastInviteMassTime(HawkTime.getMillisecond());
		player.getPush().syncPlayerInfo();
		player.responseSuccess(protocol.getType());
		
		return true;
	}
	
	/**
	 * 圣诞宝箱.
	 */
	@ProtocolHandler(code = HP.code.WORLD_CHRISTMAS_BOX_MARCH_C_VALUE)
	private boolean onChristmasBoxMarch(HawkProtocol protocol) {
		
		WorldMarchConstProperty constProperty = WorldMarchConstProperty.getInstance();  
		int boxTimes = player.getReceivedChristmasBoxNumber();
		if (boxTimes >= constProperty.getChristmasBoxReceiveLimit()) {
			player.sendError(protocol.getType(), Status.Error.CHRISTMAS_BOX_TIMES_LIMIT_VALUE, 0);
			return true;
		}
		
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		
		int pointId = GameUtil.combineXAndY(req.getPosX(), req.getPosY());
		
		// 坐标点检测
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(pointId);
		if (worldPoint == null || worldPoint.getPointType() != WorldPointType.CHRISTMAS_BOX_VALUE) {
			sendError(protocol.getType(), Status.Error.WORLD_POINT_NOT_EXIST);
			return false;			
		}
		
		String playerId = worldPoint.getPlayerId();		
		if (!HawkOSOperator.isEmptyString(playerId)) {
			if (GuildService.getInstance().isInTheSameGuild(player.getId(), playerId)) {
				sendError(protocol.getType(), Status.Error.CHRISTMAS_BOX_GUILD_MEMBER_OWN_VALUE);
				
				return false;
			} 
		}
		
		// 跨服行军类型拦截
		if (WorldMarchService.getInstance().isCrossMarchLimit(player, WorldMarchType.CHRISTMAS_BOX_MARCH)) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			
			return false;
		}
		
		// 没有多余行军
		if (!WorldMarchService.getInstance().isHasFreeMarch(player)) {
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_MAX_LIMIT);
			return false;
		}
		
		// 检测出征的军队信息
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>(); // 出征的士兵列表
		
		if (!checkMarchReq(req, protocol.getType(), armyList, worldPoint)) {
			logger.error("world attack monster failed,armyList:{}", armyList);
			
			return false;
		}
		
		int count = armyList.stream().mapToInt(arm->arm.getTotalCount()).sum();
		if (count <= 0) {
			this.sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			
			return false;
		}

		// 扣兵
		if (!ArmyService.getInstance().checkArmyAndMarch(player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_ARMY_COUNT);
			return false;
		}
				
		
		WorldMarchService.getInstance().startMarch(player, WorldMarchType.CHRISTMAS_BOX_MARCH_VALUE, 
				pointId, String.valueOf(worldPoint.getMonsterId()), null, 0, new EffectParams(req, armyList));
		
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		protocol.response(HawkProtocol.valueOf(HP.code.WORLD_CHRISTMAS_BOX_MARCH_S_VALUE, builder));
		
		return true;
	}
	
	/**
	 * 机甲宝箱行军
	 */
	@ProtocolHandler(code = HP.code.WORLD_NIAN_BOX_MARCH_C_VALUE)
	private boolean onNianBoxMarch(HawkProtocol protocol) {
		
		int ghostBox = player.getData().getDailyDataEntity().getGhostBox();
		if (ghostBox >= WorldMapConstProperty.getInstance().getNianBoxReceiveLimit()) {
			player.sendError(HP.code.WORLD_NIAN_BOX_MARCH_C_VALUE, Status.Error.GHOST_BOX_TIMES_LIMIT_VALUE, 0);
			return true;
		}
		
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		
		int pointId = GameUtil.combineXAndY(req.getPosX(), req.getPosY());
		
		// 坐标点检测
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(pointId);
		if (worldPoint == null || worldPoint.getPointType() != WorldPointType.NIAN_BOX_VALUE) {
			sendError(protocol.getType(), Status.Error.WORLD_POINT_NOT_EXIST);
			return false;			
		}
		
		// 没有多余行军
		if (!WorldMarchService.getInstance().isHasFreeMarch(player)) {
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_MAX_LIMIT);
			return false;
		}
		
		WorldMarchService.getInstance().startMarch(player, WorldMarchType.NIAN_BOX_MARCH_VALUE, pointId, String.valueOf(worldPoint.getMonsterId()), null, 0, new EffectParams());
		
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		protocol.response(HawkProtocol.valueOf(HP.code.WORLD_NIAN_BOX_MARCH_S_VALUE, builder));
		
		return true;
	}
	
	/**
	 * 能量塔行军
	 */
	@ProtocolHandler(code = HP.code.WORLD_PYLON_MARCH_C_VALUE)
	private boolean onWorldPylonMarchStart(HawkProtocol protocol) {
		
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		
		// 目标点
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(req.getPosX(), req.getPosY());

		// 坐标点检查
		if (point == null || point.getPointType() != WorldPointType.PYLON_VALUE) {
			sendError(protocol.getType(), Status.Error.WORLD_POINT_NOT_EXIST);
			return false;
		}

		// 所需体力
		WorldPylonCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WorldPylonCfg.class, point.getResourceId());
		if (cfg == null) {
			return false;
		}
		ConsumeItems consumeItems = ConsumeItems.valueOf(PlayerAttr.VIT, cfg.getStrongpointCost());

		// 体力判断
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			sendError(protocol.getType(), Status.Error.VIT_NOT_ENOUGH);
			return false;
		}

		// 士兵总数
		int totalCnt = 0;
		for (ArmySoldierPB marchArmy : req.getArmyInfoList()) {
			totalCnt += marchArmy.getCount();
		}
		if (totalCnt < CrossConstCfg.getInstance().getCrossPylonArmyCountLimit()) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PYLON_MARCH_LIMIT);
			return false;
		}
		
		// 带兵出征通用检查
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>();
		if (!checkMarchReq(req, protocol.getType(), armyList, point)) {
			logger.error("gen pylon march failed, check army req filed, playerId:{}, req:{}", player.getId(), req.toString());
			return false;
		}

		// 扣兵
		if (!ArmyService.getInstance().checkArmyAndMarch(player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			logger.error("gen pylon march failed, deduct army failed, player:{}, req:{}", player.getId(), req.toString());
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_ARMY_COUNT);
			return false;
		}

		// 生成行军
		IWorldMarch startMarch = WorldMarchService.getInstance().startMarch(player, WorldMarchType.PYLON_MARCH_VALUE, point.getId(), String.valueOf(point.getResourceId()), null, 0, new EffectParams(req, armyList));
		startMarch.getMarchEntity().setVitCost(cfg.getStrongpointCost());
		
		// 扣除体力
		consumeItems.consumeAndPush(player, Action.WORLD_PYLON_MARCH_VIT_COST);

		// 回复协议
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		protocol.response(HawkProtocol.valueOf(HP.code.WORLD_PYLON_MARCH_S_VALUE, builder));

		LogUtil.logCrossActivtyPylonMarch(player, player.getGuildId(), totalCnt, 0, 1);
		return true;
	}
	
	/**
	 * 使用行军表情
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.MARCH_EMOTICON_USE_C_VALUE)
	private boolean onUseMarchEmoticon(HawkProtocol protocol) {
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
	 * @param protocol
	 * @param emoticonId
	 * @param marchId
	 * @return
	 */
	public boolean cityEmoticonUse(HawkProtocol protocol, int emoticonId, String marchId) {
		int[] posInfo = WorldPlayerService.getInstance().getPlayerPosXY(player.getId());
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(posInfo[0], posInfo[1]);
		if (worldPoint == null) {
			return false;
		}
		
		if (!useMarchEmoticon(protocol, emoticonId, marchId)) {
			return false;
		}
		
		worldPoint.setEmoticon(emoticonId);
		worldPoint.setEmoticonUseTime(HawkTime.getMillisecond());
		WorldPointService.getInstance().notifyPointUpdate(posInfo[0], posInfo[1]);
		player.responseSuccess(protocol.getType());
		return true;
	}
	
	/**
	 * 对行军使用行军表情判断
	 * 
	 * @param marchId
	 * @param protocol
	 * @return
	 */
	public boolean marchEmoticonUse(HawkProtocol protocol, int emoticonId, String marchId) {
		IWorldMarch march = WorldMarchService.getInstance().getPlayerMarch(player.getId(), marchId);
		if (march == null) {
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_NOT_EXISIT);
			logger.error("useMarchEmoticon failed, march not exist, playerId: {}, marchId: {}", player.getId(), marchId);
			return false;
		}
		
		// 不是自己的行军
		if (!player.getId().equals(march.getPlayerId())) {
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_NOT_SELF);
			logger.error("useMarchEmoticon failed, march not self march, playerId: {}, marchId: {}, march owner: {}", player.getId(), marchId, march.getPlayerId());
			return false;
		}
		
		// 是集结行军但不是队长
		if (march.isMassJoinMarch()) {
			IWorldMarch massMarch = WorldMarchService.getInstance().getMarch(march.getMarchEntity().getTargetId()); // 获取集结的队长行军
			if (massMarch != null && massMarch.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE) {
				sendError(protocol.getType(), Status.Error.WORLD_MARCH_NOT_LEADER);
				logger.error("useMarchEmoticon failed, mass march not leader, playerId: {}, marchId: {}, march leader: {}", player.getId(), marchId, march.getMarchEntity().getLeaderPlayerId());
				return false;
			}
		}
		
		// 正处在集结等待或士兵援助中的行军, 联盟堡垒创建、修复或驻防中的行军
		int marchStatus = march.getMarchStatus(); 
		if (marchStatus == WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST_VALUE || marchStatus == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE
				|| marchStatus == WorldMarchStatus.MARCH_STATUS_MANOR_BUILD_VALUE || marchStatus == WorldMarchStatus.MARCH_STATUS_MANOR_REPAIR_VALUE
				|| marchStatus == WorldMarchStatus.MARCH_STATUS_MANOR_GARRASION_VALUE) {
			sendError(protocol.getType(), Status.Error.MARCH_STATUS_NOT_SUPPORT_EMOTICON);
			logger.error("useMarchEmoticon failed, march status not support, playerId: {}, marchId: {}, status: {}", player.getId(), marchId, marchStatus);
			return false;
		}
		
		// 联盟资源矿采集时不可使用行军表情
		if (march.getMarchType() == WorldMarchType.MANOR_COLLECT && marchStatus == WorldMarchStatus.MARCH_STATUS_MARCH_COLLECT_VALUE) {
			sendError(protocol.getType(), Status.Error.MARCH_STATUS_NOT_SUPPORT_EMOTICON);
			logger.error("useMarchEmoticon failed, march status not support, playerId: {}, marchId: {}, status: {}", player.getId(), marchId, marchStatus);
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
			WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(march.getTerminalId());
			WorldMarch firstMarch = WorldMarchService.getInstance().getMarchByPoint(worldPoint);
			if (firstMarch != null) {
				firstMarch.setEmoticon(emoticonId);
				firstMarch.setEmoticonUseTime(HawkTime.getMillisecond());
			}
			WorldPointService.getInstance().notifyPointUpdate(worldPoint.getX(), worldPoint.getY());
		}
		
		player.responseSuccess(protocol.getType());
		return true;
	}
	
	/**
	 * 使用行军表情
	 * 
	 * @param protocol
	 * @param emoticonId
	 * @param marchId
	 * @return
	 */
	public boolean useMarchEmoticon(HawkProtocol protocol, int emoticonId, String marchId) {
		MarchEmoticonCfg emoticonCfg = HawkConfigManager.getInstance().getConfigByKey(MarchEmoticonCfg.class, emoticonId);
		// 道具ID参数不对
		if (emoticonCfg == null) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			logger.error("useMarchEmoticon failed, item config error, playerId: {}, marchId: {}, emoticonId: {}", player.getId(), marchId, emoticonId);
			return false;
		}
		
		// 判断表情包是否已解锁
		if (emoticonCfg.getDefaultUnlock() == 0 && !GameUtil.isMarchEmoticonBagUnlocked(player, emoticonCfg.getEmoticon())) {
			sendError(protocol.getType(), Status.Error.MARCH_EMOTICON_LOCKED_VALUE);
			logger.error("useMarchEmoticon failed, emoticon locked, playerId: {}, marchId: {}, emoticonId: {}", player.getId(), marchId, emoticonId);
			return false;
		}
		
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		boolean enough = false;
		ItemInfo itemInfo = MarchEmoticonProperty.getInstance().getItemInfo();
		if (player.getData().getItemNumByItemId(itemInfo.getItemId()) > 0) {
			enough = true;
			consumeItems.addConsumeInfo(itemInfo, false);
		} else {
			consumeItems.addConsumeInfo(MarchEmoticonProperty.getInstance().getPriceInfo(), false);
		}

		if (!consumeItems.checkConsume(player, protocol.getType())) {
			logger.error("useMarchEmoticon failed, consume error, playerId: {}, marchId: {}, emoticonId: {}, enough: {}", player.getId(), marchId, emoticonId, enough);
			return false;
		}
		
		if (consumeItems.getBuilder().hasAttrInfo() && consumeItems.getBuilder().getAttrInfo().getDiamond() > 0) {
			ItemInfo priceItem =  MarchEmoticonProperty.getInstance().getPriceInfo();
			consumeItems.addPayItemInfo(new PayItemInfo(String.valueOf(itemInfo.getItemId()), (int)priceItem.getCount(), 1));
		}
		consumeItems.consumeAndPush(player, Action.USE_MARCH_EMOTICON);
		
		logger.info("useMarchEmoticon success, playerId: {}, emoticonId: {}, enough: {}, marchId: {}", player.getId(), emoticonId, enough, marchId);
        LogUtil.logMarchEmotionUse(player, marchId, emoticonId, false, false, false);
		return true;
	}
	
	/**
	 * 踢雪球行军
	 */
	@ProtocolHandler(code = HP.code.WORLD_SNOWBALL_MARCH_C_VALUE)
	private boolean onWorldSnowballMarchStart(HawkProtocol protocol) {
		
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		
		// 目标点
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(req.getPosX(), req.getPosY());

		// 坐标点检查
		if (point == null || point.getPointType() != WorldPointType.SNOWBALL_VALUE) {
			sendError(protocol.getType(), Status.Error.WORLD_POINT_NOT_EXIST);
			return false;
		}

		// 带兵出征通用检查
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>();
		if (!checkMarchReq(req, protocol.getType(), armyList, point)) {
			logger.error("gen snowball march failed, check army req filed, playerId:{}, req:{}", player.getId(), req.toString());
			return false;
		}

		// 出征数量不足
		if (!WorldSnowballService.getInstance().canMarch(armyList)) {
			sendError(protocol.getType(), Status.Error.SNOWBALL_MARCH_COUNT_NOT_ENOUGTH);
			return false;
		}

		// 踢球方向
		KickSnowballDirection direction = req.getDirection();
		if (direction == null) {
			sendError(protocol.getType(), Status.Error.KICK_SNOWBALL_DIRECTION_ERROR);
			return false;
		}
		
		// 扣兵
		if (!ArmyService.getInstance().checkArmyAndMarch(player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			logger.error("gen snowball march failed, deduct army failed, player:{}, req:{}", player.getId(), req.toString());
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_ARMY_COUNT);
			return false;
		}

		// 生成行军
		IWorldMarch march = WorldMarchService.getInstance().startMarch(player, WorldMarchType.SNOWBALL_MARCH_VALUE, point.getId(), String.valueOf(point.getMonsterId()), null, 0, new EffectParams(req, armyList));
		if (march == null) {
			return false;
		}
		// 雪球行军这个字段用作踢球方向，不再新加字段了
		march.getMarchEntity().setAttackTimes(direction.getNumber());
		
		// 回复协议
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		protocol.response(HawkProtocol.valueOf(HP.code.WORLD_SNOWBALL_MARCH_S_VALUE, builder));

		return true;
	}
	
	/**
	 * 雪球攻击
	 */
	@ProtocolHandler(code = HP.code.WORLD_SNOWBALL_ATTACK_REQ_VALUE)
	private boolean onSnowballAttack(HawkProtocol protocol) {
		SnowballAttackReq req = protocol.parseProtocol(SnowballAttackReq.getDefaultInstance());
		int pointId = GameUtil.combineXAndY(req.getX(), req.getY());
		
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(pointId);
		
		// 目标点错误
		if (point == null || point.getPointType() != WorldPointType.PLAYER_VALUE) {
			sendError(protocol.getType(), Status.Error.WORLD_POINT_NOT_EXIST);
			return false;
		}
		
		// 不可以攻击同盟玩家
		if (GuildService.getInstance().isInTheSameGuild(player.getId(), point.getPlayerId())) {
			sendError(protocol.getType(), Status.Error.SNOWBALL_ATK_CAN_NOT_SAME_GUILD);
			return false;
		}
		
		// 返回协议
		int playerPointId = WorldPlayerService.getInstance().getPlayerPos(player.getId());
		int[] playerPos = GameUtil.splitXAndY(playerPointId);
		
		// 超出攻击范围
		double distance = WorldUtil.distance(playerPos[0], playerPos[1], point.getX(), point.getY());
		if (distance > WorldMapConstProperty.getInstance().getSnowballAtkDistance()) {
			sendError(protocol.getType(), Status.Error.SNOWBALL_ATK_DISTANCE_LIMIT);
			return false;
		}
		
		long currentTime = HawkTime.getMillisecond();
		long lastSnowballAtkTime = WorldMarchService.getInstance().getLastSnowballAtkTime(player.getId());
		if (currentTime - lastSnowballAtkTime < WorldMapConstProperty.getInstance().getSnowballAtkCd()) {
			sendError(protocol.getType(), Status.Error.SNOWBALL_ATK_IN_CD);
			return false;
		}
		WorldMarchService.getInstance().putLastSnowballAtkTime(player.getId(), currentTime);
		
		// 检测消耗
		ConsumeItems consume = ConsumeItems.valueOf();
		String cost = WorldMarchConstProperty.getInstance().getSnowballAtkCost();
		consume.addConsumeInfo(ItemInfo.valueListOf(cost));
		if (!consume.checkConsume(player, protocol.getType())) {
			return false;
		}
		consume.consumeAndPush(player, Action.WORLD_SONWBALL_ATTACK);
		
		WorldSnowballService.getInstance().noticeSnowballAttack(playerPos[0], playerPos[1], point.getX(), point.getY());
		
		
		Player tarPlayer = GlobalData.getInstance().makesurePlayer(point.getPlayerId());
		long snwballAtkFireTime = ConstProperty.getInstance().getSnwballAtkFireTime();
		CityManager.getInstance().cityOnFireNoLimit(tarPlayer, snwballAtkFireTime);
		
		// 邮件
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(player.getId())
				.addTitles(tarPlayer.getName())
				.addContents(tarPlayer.getName())
				.addContents(point.getX())
				.addContents(point.getY())
				.setMailId(MailId.SNOWBALL_MAIL_8)
				.build());
		
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(tarPlayer.getId())
				.setMailId(MailId.SNOWBALL_MAIL_9)
				.addTitles(player.getName())
				.addSubTitles(player.getName())
				.addContents(player.getName())
				.addContents(playerPos[0])
				.addContents(playerPos[1])
				.build());
		
		return true;
	}
	
	/**
	 * 间谍
	 */
	@ProtocolHandler(code = HP.code.WORLD_ESPIONAGE_MARCH_REQ_VALUE)
	private boolean onEspionageMarch(HawkProtocol protocol) {
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());

		// 没有多余的行军队列
		if (!WorldMarchService.getInstance().isHasFreeMarch(player)) {
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_MAX_LIMIT);
			return false;
		}
		
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(req.getPosX(), req.getPosY());
		
		// 目标点错误
		if (point == null || point.getPointType() != WorldPointType.PLAYER_VALUE) {
			sendError(protocol.getType(), Status.Error.WORLD_POINT_NOT_EXIST);
			return false;
		}
		
		// 不可以侦查同盟玩家
		if (GuildService.getInstance().isInTheSameGuild(player.getId(), point.getPlayerId())) {
			sendError(protocol.getType(), Status.Error.ESPIONAGE_CAN_NOT_SAME_GUILD);
			return false;
		}

		// 判断对方保护罩状态
		Player targetPlayer = GlobalData.getInstance().makesurePlayer(point.getPlayerId());
		boolean intShield = targetPlayer.getData().getCityShieldTime() > HawkTime.getMillisecond();
		if (!intShield && HawkTime.getMillisecond() > point.getShowProtectedEndTime()) {
			sendError(protocol.getType(), Status.Error.ESPIONAGE_HAVE_NO_PROTECT);
			return false;
		}
		
		// 检测消耗
		ConsumeItems consume = ConsumeItems.valueOf();
		String cost = WorldMarchConstProperty.getInstance().getEspionageCost();
		consume.addConsumeInfo(ItemInfo.valueListOf(cost));
		if (!consume.checkConsume(player, protocol.getType())) {
			return false;
		}
		consume.consumeAndPush(player, Action.WORLD_ESPIONAGE);

		// 创建行军
		String targetId = point.getPlayerId();
		WorldMarchService.getInstance().startMarch(player, WorldMarchType.ESPIONAGE_MARCH_VALUE, point.getId(), targetId, null, 0, new EffectParams());

		// 回复协议
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(true);
		protocol.response(HawkProtocol.valueOf(HP.code.WORLD_SNOWBALL_ATTACK_RESP_VALUE, builder));
		return true;
	}
	
	/**
	 * 幽灵工厂打怪行军
	 */
	@ProtocolHandler(code = HP.code.GHOST_TOWER_MARCH_C_VALUE)
	private boolean onWorldGhostTowerMarchStart(HawkProtocol protocol) {
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		int posX = req.getPosX();
		int posY = req.getPosY();
		WorldPoint targetPoint = WorldPointService.getInstance().getWorldPoint(posX, posY);
		
		// 目标点不存在
		if (targetPoint == null) {
			sendError(protocol.getType(), Status.Error.TARGET_MONSTER_DISAPPEAR);
			WorldScene.getInstance().move(player.getAoiObjId(), posX, posY, 0.0f);
			return false;
		}
		if(this.player.isCsPlayer()){
			logger.error("onWorldGhostTowerMarchStart failed, playrer is cross,playerId:{}", player.getId());
			return false;
		}
		// 不是幽灵工厂的怪
		if (targetPoint.getPointType() != WorldPointType.GHOST_TOWER_MONSTER_VALUE) {
			sendError(protocol.getType(), Status.Error.TARGET_MONSTER_DISAPPEAR);
			return false;
		}
		//不是自己的怪
		if(targetPoint.getGhostInfo() == null){
			sendError(protocol.getType(), Status.Error.GHOST_TOWER_MONSTER_NOT_YOURS);
			return false;
		}
		if(!targetPoint.getGhostInfo().getChallenger().equals(this.player.getId())){
			sendError(protocol.getType(), Status.Error.GHOST_TOWER_MONSTER_NOT_YOURS);
			return false;
		}
		// 没有多余行军
		if (!WorldMarchService.getInstance().isHasFreeMarch(player)) {
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_MAX_LIMIT);
			return false;
		}
		//同时只能有一个行军
		BlockingQueue<IWorldMarch> playerMarchs = WorldMarchService.getInstance().getPlayerMarch(player.getId());
		for (IWorldMarch iWorldMarch : playerMarchs) {
			if(iWorldMarch.getMarchType() == WorldMarchType.GHOST_TOWER_MARCH &&
					!iWorldMarch.isReturnBackMarch()){
				sendError(protocol.getType(), Status.Error.COUNTRY_QUEST_MARCH_LIMITED_VALUE);
				return false;
			}
		}
		// 检测出征的军队信息
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>(); // 出征的士兵列表
		if (!checkMarchReq(req, protocol.getType(), armyList, targetPoint)) {
			logger.error("world attack tower monster failed,armyList:{}", armyList);
			return false;
		}
		// 扣兵
		if (!ArmyService.getInstance().checkArmyAndMarch(player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			logger.error("world attack tower monster failed, deduct armyList:{}", armyList);
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_ARMY_COUNT);
			return false;
		}
		// 发起行军
		IWorldMarch march = WorldMarchService.getInstance().startMarch(player, WorldMarchType.GHOST_TOWER_MARCH_VALUE, targetPoint.getId(), String.valueOf(targetPoint.getMonsterId()), null, 0, new EffectParams(req, armyList));
		
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(march != null);
		protocol.response(HawkProtocol.valueOf(HP.code.GHOST_TOWER_MARCH_S, builder));
		return true;
	}
	
	/**
	 * 端午龙船行军
	 */
	@ProtocolHandler(code = HP.code.DRAGON_BOAT_MARCH_C_VALUE)
	public boolean onWorldDragonBoatMarchStart(HawkProtocol protocol) {
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		int posX = req.getPosX();
		int posY = req.getPosY();
		WorldPoint targetPoint = WorldPointService.getInstance().getWorldPoint(posX, posY);
		// 目标点不存在
		if (targetPoint == null) {
			sendError(protocol.getType(), Status.Error.DRAGON_BOAT_GIFT_HIDDEN_VALUE);
			WorldScene.getInstance().move(player.getAoiObjId(), posX, posY, 0.0f);
			return false;
		}
		if(this.player.isCsPlayer()){
			logger.error("onWorldDragonBoatMarchStart failed, playrer is cross,playerId:{}", player.getId());
			return false;
		}
		//活动关闭
		int termId = 0;
		Optional<ActivityBase> dbGiftboatActivityOP = ActivityManager.getInstance().getActivity(Activity.ActivityType.DRAGON_BOAT_GIFT_VALUE);
		if (dbGiftboatActivityOP.isPresent()) {
			DragonBoatGiftActivity activity = (DragonBoatGiftActivity) dbGiftboatActivityOP.get();
			boolean opening = activity.isOpening(player.getId());
			if(!opening){
				return false;
			}
			termId = activity.getActivityTermId();
		}
		if(this.player.isCsPlayer()){
			return false;
		}
		// 不是幽灵工厂的怪
		if (targetPoint.getPointType() != WorldPointType.DRAGON_BOAT_VALUE) {
			sendError(protocol.getType(), Status.Error.DRAGON_BOAT_GIFT_HIDDEN_VALUE);
			return false;
		}
		//同时只能有一个行军
		BlockingQueue<IWorldMarch> playerMarchs = WorldMarchService.getInstance().getPlayerMarch(player.getId());
		for (IWorldMarch iWorldMarch : playerMarchs) {
			if(iWorldMarch.getMarchType() == WorldMarchType.DRAGON_BOAT_MARCH){
				sendError(protocol.getType(), Status.Error.DRAGON_BOAT_MARCH_LIMITED_VALUE);
				return false;
			}
		}
		DragonBoatInfo info = targetPoint.getDragonBoatInfo();
		if(info == null){
			return false;
		}
		//已经领取过
		DragonBoatGiftKVCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(DragonBoatGiftKVCfg.class);
		int giftCount = info.getAwardRecordSize();
		if(info.isAward(player.getId()) ||
				giftCount >=  kvcfg.getGiftCount()){
			sendError(protocol.getType(), Status.Error.DRAGON_BOAT_GIFT_ACHIEVE_VALUE);
			return false;
		}
		
		// 没有多余行军
		if (!WorldMarchService.getInstance().isHasFreeMarch(player)) {
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_MAX_LIMIT);
			return false;
		}
		// 发起行军
		IWorldMarch march = WorldMarchService.getInstance().startMarch(player, WorldMarchType.DRAGON_BOAT_MARCH_VALUE, targetPoint.getId(), String.valueOf(targetPoint.getResourceId()), null, 0,  new EffectParams());
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(march != null);
		protocol.response(HawkProtocol.valueOf(HP.code.DRAGON_BOAT_MARCH_S, builder));
		//日志记录
		LogUtil.logDragonBoatGiftAchieve(player, termId, 1, info.getBoatId());
		logger.error("onWorldDragonBoatMarchStart sucess,playerId:{},boatId:{}", player.getId(), info.getBoatId());
		return true;
	}
	

	/**
	 * 共享蛋糕行军
	 */
	@ProtocolHandler(code = HP.code.CAKE_SHARE_MARCH_C_VALUE)
	public boolean onWorldCakeShareMarchStart(HawkProtocol protocol) {
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		int posX = req.getPosX();
		int posY = req.getPosY();
		WorldPoint targetPoint = WorldPointService.getInstance().getWorldPoint(posX, posY);
		// 目标点不存在
		if (targetPoint == null) {
			sendError(protocol.getType(), Status.Error.SHARE_CAKE_GIFT_HIDDEN_VALUE);
			WorldScene.getInstance().move(player.getAoiObjId(), posX, posY, 0.0f);
			return false;
		}
		if(this.player.isCsPlayer()){
			logger.error("onWorldCakeShareMarchStart failed, playrer is cross,playerId:{}", player.getId());
			return false;
		}
		//活动关闭
		int termId = 0;
		Optional<ActivityBase> cakeShareActivityOp = ActivityManager.getInstance().getActivity(Activity.ActivityType.CAKE_SHARE_VALUE);
		if (cakeShareActivityOp.isPresent()) {
			CakeShareActivity activity = (CakeShareActivity) cakeShareActivityOp.get();
			boolean opening = activity.isOpening(player.getId());
			if(!opening){
				return false;
			}
			//不在可领取的范围内
			boolean isCanRecTime = activity.isBetweenReceiveTime();
			if (!isCanRecTime) {
				sendError(protocol.getType(), Status.Error.SHARE_CAKE_MARCH_TIME_LIMITED_VALUE);
				return false;
			}
			termId = activity.getActivityTermId();
		}
		if(this.player.isCsPlayer()){
			return false;
		}
		// 不是幽灵工厂的怪
		if (targetPoint.getPointType() != WorldPointType.CAKE_SHARE_VALUE) {
			sendError(protocol.getType(), Status.Error.SHARE_CAKE_GIFT_HIDDEN_VALUE);
			return false;
		}
		//同时只能有一个行军
		BlockingQueue<IWorldMarch> playerMarchs = WorldMarchService.getInstance().getPlayerMarch(player.getId());
		for (IWorldMarch iWorldMarch : playerMarchs) {
			if(iWorldMarch.getMarchType() == WorldMarchType.CAKE_SHARE_MARCH){
				sendError(protocol.getType(), Status.Error.SHARE_CAKE_MARCH_LIMITED_VALUE);
				return false;
			}
		}
		CakeShareInfo info = targetPoint.getCakeShareInfo();
		if(info == null){
			return false;
		}
		//已经领取过
		CakeShareKVCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(CakeShareKVCfg.class);
		int giftCount = info.getAwardRecordSize();
		if(info.isAward(player.getId()) || giftCount >=  kvcfg.getGiftCount()){
			sendError(protocol.getType(), Status.Error.SHARE_CAKE_GIFT_ACHIEVE_VALUE);
			return false;
		}
		
		// 没有多余行军
		if (!WorldMarchService.getInstance().isHasFreeMarch(player)) {
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_MAX_LIMIT);
			return false;
		}
		// 发起行军
		IWorldMarch march = WorldMarchService.getInstance().startMarch(player, WorldMarchType.CAKE_SHARE_MARCH_VALUE, targetPoint.getId(), String.valueOf(targetPoint.getResourceId()), null, 0,  new EffectParams());
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(march != null);
		protocol.response(HawkProtocol.valueOf(HP.code.CAKE_SHARE_MARCH_S, builder));
		//日志记录
		LogUtil.logCakeShareRewardAchieve(player, termId, 1, info.getCakeId());
		logger.error("onWorldCakeShareMarchStart sucess,playerId:{},cakeId:{}", player.getId(), info.getCakeId());
		return true;
	}
	
	
	/**
	 * 开启国家建设任务行军
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.NATIONAL_BUILD_MARCH_C_VALUE)
	private boolean onNationalMarchStart(HawkProtocol protocol) {
		// 获取建设处
		NationConstruction construction = (NationConstruction) NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_BUILDING_CENTER);
		// 判断建筑状态
		if(construction == null || construction.getEntity().getLevel() < 1){
			return false;
		}
		// 跨服行军类型拦截
		if (WorldMarchService.getInstance().isCrossMarchLimit(player, WorldMarchType.NATIONAL_BUILDING_MARCH)) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_PROTOCOL_SHIELD);
			return false;
		}
		// 判断当前任务次数是否足够
		NationBuildQuestEntity nbq = player.getData().getNationalBuildQuestEntity();
		int hasQuest = nbq.calcAlreadyMarchCount()[0];
		int leftQuestTimes = nbq.getQuestTimes();
		// 当前剩余次数减去已经出征的队列数
		if(nbq == null || leftQuestTimes - hasQuest <= 0) {
			player.sendError(HP.code.NATIONAL_BUILD_MARCH_C_VALUE, Status.Error.QUEST_TIMES_NOT_ENOUGH, 0);
			return true;
		}
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		// 校验士兵数量是否足够
		int totalCnt = 0;
		for (ArmySoldierPB marchArmy : req.getArmyInfoList()) {
			totalCnt += marchArmy.getCount();
		}
		if(totalCnt < NationConstCfg.getInstance().getTaskSoldiers()){
			player.sendError(HP.code.NATIONAL_BUILD_MARCH_C_VALUE, Status.Error.ARMY_NOT_ENOUGH, 0);
			return true;
		}
		// 检查任务基础条件是否满足
		String questId = req.getNationQuestId();
		NationalBuildQuestModel model = nbq.getQuestsMap().get(questId);
		// 判断任务是否对，已经开始做的任务类型不能变
		int nopType = req.getNopType();
		NationBuildQuestType buildQuestType = NationBuildQuestType.valueOf(nopType);
		if(buildQuestType == null){
			return false;
		}
		// 已经开始做的任务 或者 当前任务进度大于0。这时候不能切任务类型
		if (nbq.getNationQuestType() != nopType) {
			for (NationalBuildQuestModel tm : nbq.getQuestsMap().values()) {
				if((tm.getMarchId() != null || tm.getCurrentProcess() > 0)) {
					player.sendError(HP.code.NATIONAL_BUILD_MARCH_C_VALUE, Status.Error.NATION_CANT_CHANGE_QUEST_TYPE_VALUE, 0);
					return true;
				}
			}
		}
		// 设置新的任务类型
		nbq.setNationQuestType(nopType);
		// 判断任务条件是否满足
		if(model == null || !model.checkQuestNeed(player.getId(), req.getHeroIdList())){
			player.sendError(HP.code.NATIONAL_BUILD_MARCH_C_VALUE, Status.Error.NATION_BUILD_NOT_ENOUGH, 0);
			return true;
		}
		// 带兵出征通用检查
		List<ArmyInfo> armyList = new ArrayList<ArmyInfo>();
		if (!checkMarchReq(req, protocol.getType(), armyList, null)) {
			return false;
		}
		// 扣兵
		if (!ArmyService.getInstance().checkArmyAndMarch(player, armyList, req.getHeroIdList(), req.getSuperSoldierId())) {
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_ARMY_COUNT);
			return false;
		}
		int buildId = model.getBuildId() * 100;
		// 获取坐标
		NationConstructionBaseCfg baseCfg = HawkConfigManager.getInstance().getConfigByKey(NationConstructionBaseCfg.class, buildId);
		if(baseCfg == null){
			return false;
		}
		int pointId = GameUtil.combineXAndY(baseCfg.getX(), baseCfg.getY());
		// 发起行军
		IWorldMarch march = WorldMarchService.getInstance().startMarch(player, WorldMarchType.NATIONAL_BUILDING_MARCH_VALUE, pointId, questId, null, 0, new EffectParams(req, armyList));
		// 任务存入行军
		model.setMarchId(march.getMarchId());
		// 保存数据
		nbq.notifyUpdate();
		
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(march != null);
		protocol.response(HawkProtocol.valueOf(HP.code.NATIONAL_BUILD_MARCH_S, builder));
		
		// 记录日志
		String[] ahLog = armyHeroToStr(req);
		LogUtil.logNationbuildQuestStart(player, model.getQuestCfgId(), model.getBuildId(), ahLog[0], ahLog[1], model.isAdvAward() ? 1 : 0, construction.getLevel());
		
		// 判断是否去掉红点
		if(!nbq.checkRd()) {
			player.rmNationRDAndNotify(NationRedDot.CONSTRUCTION_IDLE);
		}
		return true;
	}
	
	/**
	 * 资源狂欢宝箱行军
	 */
	@ProtocolHandler(code = HP.code2.RESOURCE_SPREE_BOX_MARCH_C_VALUE)
	public boolean onResourceSpreeBoxMarchStart(HawkProtocol protocol) {
		WorldMarchReq req = protocol.parseProtocol(WorldMarchReq.getDefaultInstance());
		int posX = req.getPosX();
		int posY = req.getPosY();
		boolean useGold = req.hasSpyUseGold() && req.getSpyUseGold();
		
		WorldPoint targetPoint = WorldPointService.getInstance().getWorldPoint(posX, posY);
		// 目标点不存在
		if (targetPoint == null) {
			sendError(protocol.getType(), Status.Error.WORLD_POINT_NOT_EXIST);
			WorldScene.getInstance().move(player.getAoiObjId(), posX, posY, 0.0f);
			return false;
		}
		
		// 不是宝箱点
		if (targetPoint.getPointType() != WorldPointType.RESOURCE_SPREE_BOX_VALUE) {
			sendError(protocol.getType(), Status.Error.WORLD_POINT_NOT_EXIST);
			return false;
		}
		String server = this.player.getMainServerId();
		ResourceSpreeBoxWorldPoint boxPoint = (ResourceSpreeBoxWorldPoint) targetPoint;
		if(!boxPoint.achiveServer(server)){
			sendError(protocol.getType(), Status.CrossServerError.CROSS_RESOURCE_SPREE_NOT_WINNER);
			return false;
		}
		// 消耗
		List<ItemInfo> itemInfo = WorldMarchConstProperty.getInstance().getInvestigationMarchCost();
		for (ItemInfo item : itemInfo) {
			int eff = player.getEffect().getEffVal(EffType.SPY_CONSUM_REDUS, new EffectParams());
			int buffCost = (int)(item.getCount() * (1 - eff * GsConst.EFF_PER));
			item.setCount(Math.max(0, buffCost));
		}
		
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addConsumeInfo(itemInfo, useGold);
		if (!consume.checkConsume(player, protocol.getType())) {
			logger.error("onResourceSpreeBoxMarchStart failed,consume check failed,playerId:{}",player.getId());
			return false;
		}
		// 没有多余行军
		if (!WorldMarchService.getInstance().isHasFreeMarch(player)) {
			sendError(protocol.getType(), Status.Error.WORLD_MARCH_MAX_LIMIT);
			return false;
		}
		//数量是否超标
		CActivityInfo crossInfo = CrossActivityService.getInstance().getActivityInfo();
		int termId = crossInfo.getTermId();
		int count = RedisProxy.getInstance().getCrossResourceBoxGetCount(this.player.getId(), termId);
		if(termId == 0 ||count >= CrossConstCfg.getInstance().getResBoxSoloMax()){
			sendError(protocol.getType(), Status.CrossServerError.CROSS_RESOURCE_SPREE_COUNT_LIMIT);
			return false;
		}
		//扣消耗
		consume.consumeAndPush(player, Action.RESOURCE_SPREE_MARCH);
		// 发起行军
		IWorldMarch march = WorldMarchService.getInstance().startMarch(player, WorldMarchType.RESOURCE_SPREE_BOX_MARCH_VALUE,
				targetPoint.getId(), String.valueOf(targetPoint.getResourceId()), null, 0,  new EffectParams());
		WorldMarchResp.Builder builder = WorldMarchResp.newBuilder();
		builder.setSuccess(march != null);
		protocol.response(HawkProtocol.valueOf(HP.code2.RESOURCE_SPREE_BOX_MARCH_S, builder));
		//日志记录
		logger.error("onResourceSpreeBoxMarchStart sucess,playerId:{},boxId:{}", player.getId(), 2);
		return true;
	}
	
	private String[] armyHeroToStr(WorldMarchReq req) {
		String[] info = new String[] {"", ""};
		int i = 0;
		StringBuffer heroLog = new StringBuffer();
		for (Integer heroId : req.getHeroIdList()) {
			if(i != 0) {
				heroLog.append(",");
			}
			heroLog.append(heroId);
			i++;
		}
		info[0] = heroLog.toString();
		i = 0;
		StringBuffer armyLog = new StringBuffer();
		for (ArmySoldierPB marchArmy : req.getArmyInfoList()) {
			if(i != 0) {
				armyLog.append(",");
			}
			armyLog.append(marchArmy.getArmyId());
			armyLog.append("_");
			armyLog.append(marchArmy.getCount());
			i++;
		}
		info[1] = armyLog.toString();
		return info;
	}
	
	/**
	 * 国家行军加速检测
	 */
	private boolean canNationMarchSpeedUp(IWorldMarch march, HawkProtocol protocol) {
		if (!march.isNationMassMarch()) {
			return true;
		}
		
		// 集结行军
		if (march.isMassMarch()) {
			// 战时司令联盟的盟主或者R4可以加速
			String crossFightPresident = RedisProxy.getInstance().getCrossFightPresident();
			CrossPlayerStruct fightPresidentInfo = RedisProxy.getInstance().getFightPresidentInfo(crossFightPresident);
			boolean fightGuild =player.getGuildId().equals(fightPresidentInfo.getGuildID());
			boolean guildAuthority = GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.ALLIANCE_MANOR_SET);
			if (fightGuild && guildAuthority) {
				return true;
			}
			
			// 参与集结的人可以加速
			List<String> authPlayerId = new ArrayList<>();
			Set<IWorldMarch> massJoinMarchs = WorldMarchService.getInstance().getMassJoinMarchs(march, true);
			for (IWorldMarch joinMarch : massJoinMarchs) {
				authPlayerId.add(joinMarch.getPlayerId());
			}
			authPlayerId.add(march.getPlayerId());
			if (authPlayerId.contains(player.getId())) {
				return true;
			}

			sendError(protocol.getType(), Status.CrossServerError.CROSS_MARCH_MASS_STAR_ERROR_VALUE);
			return false;
			
		// 加入集结的行军
		} else if (march.isMassJoinMarch()) {
			// 没有联盟不能加速
			if (!player.hasGuild()) {
				return false;
			}
			// 自己可以加速
			if (player.getId().equals(march.getPlayerId())) {
				return true;
			}
			// 出战联盟的人可以加速
			boolean isFightGuildPlayer = RedisProxy.getInstance().isCrossFightGuild(march.getPlayer().getMainServerId(), player.getGuildId());
			if (isFightGuildPlayer) {
				return true;
			}
			
			sendError(protocol.getType(), Status.CrossServerError.CROSS_MARCH_MASS_NOT_STAR_ERROR_VALUE);
			return false;
		// 单人行军
		} else {
			return true;
		}
	}

	/**
	 * 集结卡信息请求
	 */
	@ProtocolHandler(code = HP.code2.MASS_CARD_INFO_REQ_VALUE)
	public boolean onMassCardInfoReq(HawkProtocol protocol) {
		MassCardInfoReq req = protocol.parseProtocol(MassCardInfoReq.getDefaultInstance());
		MassCardInfoResp.Builder builder = MassCardInfoResp.newBuilder();
		List<String> marchIdList = req.getMarchIdList();
		for (String marchId : marchIdList) {
			Builder massCardInfo = getMassCardInfo(marchId, player);
			if (massCardInfo != null) {
				builder.addCardInfo(massCardInfo);
			}
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.MASS_CARD_INFO_RESP_VALUE, builder));
		return true;
	}
	
	private MassCardInfo.Builder getMassCardInfo(String marchId, Player viewer) {
		try {
			MassCardInfo.Builder builder = MassCardInfo.newBuilder();
			builder.setMarchId(marchId);
			
			boolean isLeader = false;
			boolean hasFormation = false;
			boolean inFormation  = false;
			boolean hasJoined  = false;
			boolean massFull  = false;
			boolean isMarch  = false;
			boolean isEnd = false;
			
			IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
			if (march == null || march.getMarchEntity().isInvalid() || !march.isMassMarch() || !viewer.hasGuild()) {
				isEnd = true;
			} else {
				// 不是在行军中或者等待集结中,就认为结束了
				if (!march.isMarchState() && march.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
					isEnd = true;	
				}
				
				isLeader = viewer.getId().equals(march.getPlayerId());
				
				GuildFormationObj guildFormationObj = GuildService.getInstance().getGuildFormation(viewer.getGuildId());
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
				Player leader = GlobalData.getInstance().makesurePlayer(march.getPlayerId());
				Set<IWorldMarch> reachedMarchList = WorldMarchService.getInstance().getMassJoinMarchs(march, true);
				int count = reachedMarchList != null ? reachedMarchList.size() : 0;
				if (count >= leader.getMaxMassJoinMarchNum(march) + march.getMarchEntity().getBuyItemTimes()) {
					massFull = true;
				}
				
				// 队伍是否已经出发了
				isMarch = march.getMarchEntity().getStartTime() < HawkTime.getMillisecond();
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

	private Activity.ChatMassCardInfo.Builder getChatMassCardInfo(String marchId, Player viewer) {
		try {
			Activity.ChatMassCardInfo.Builder builder = Activity.ChatMassCardInfo.newBuilder();
			builder.setMarchId(marchId);

			boolean isLeader = false;
			boolean hasFormation = false;
			boolean inFormation  = false;
			boolean hasJoined  = false;
			boolean massFull  = false;
			boolean isMarch  = false;
			boolean isEnd = false;

			IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
			if (march == null || march.getMarchEntity().isInvalid() || !march.isMassMarch() || !viewer.hasGuild()) {
				isEnd = true;
			} else {
				// 不是在行军中或者等待集结中,就认为结束了
				if (!march.isMarchState() && march.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
					isEnd = true;
				}

				isLeader = viewer.getId().equals(march.getPlayerId());

				GuildFormationObj guildFormationObj = GuildService.getInstance().getGuildFormation(viewer.getGuildId());
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
				Player leader = GlobalData.getInstance().makesurePlayer(march.getPlayerId());
				Set<IWorldMarch> reachedMarchList = WorldMarchService.getInstance().getMassJoinMarchs(march, true);
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