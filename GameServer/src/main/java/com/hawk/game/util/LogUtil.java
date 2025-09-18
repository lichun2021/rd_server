package com.hawk.game.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;
import org.hawk.uuid.HawkUUIDGenerator;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableList;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.christmaswar.cfg.ChristmasWarTaskCfg;
import com.hawk.activity.type.impl.rank.ActivityRankType;
import com.hawk.common.AccountRoleInfo;
import com.hawk.game.GsConfig;
import com.hawk.game.activity.impl.inherit.InheritNewService;
import com.hawk.game.battle.BattleLogHelper;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.config.HeroCfg;
import com.hawk.game.config.HeroOfficeCfg;
import com.hawk.game.config.SuperSoldierCfg;
import com.hawk.game.crossactivity.CrossTargetType;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.entity.PlantFactoryEntity;
import com.hawk.game.entity.StatisticsEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.guild.championship.GCConst.GCGuildGrade;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.lianmengcyb.player.ICYBORGPlayer;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.dayazhizhan.battleroom.player.IDYZZPlayer;
import com.hawk.game.module.lianmengtaiboliya.TBLYExtraParam;
import com.hawk.game.module.lianmengtaiboliya.msg.TBLYBilingInformationMsg;
import com.hawk.game.module.material.data.MTTruck;
import com.hawk.game.module.material.data.MTTruckGroup;
import com.hawk.game.module.nationMilitary.entity.NationMilitaryEntity;
import com.hawk.game.module.obelisk.service.mission.data.ObeliskMissionItem;
import com.hawk.game.module.plantfactory.tech.PlantTech;
import com.hawk.game.module.plantsoldier.strengthen.PlantSoldierSchool;
import com.hawk.game.nation.NationalConst;
import com.hawk.game.player.Player;
import com.hawk.game.player.PowerElectric;
import com.hawk.game.player.hero.HeroSkin;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.player.supersoldier.SuperSoldierSkillSlot;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.GachaType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Consume.SyncAttrInfo;
import com.hawk.game.protocol.Cross.CrossType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.PlotBattle;
import com.hawk.game.protocol.SimulateWar.WayType;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.RelationService;
import com.hawk.game.service.starwars.StarWarsConst.SWWarType;
import com.hawk.game.service.tiberium.logunit.TLWLeaguaGuildInfoUnit;
import com.hawk.game.service.tiberium.logunit.TLWWarResultLogUnit;
import com.hawk.game.service.tiberium.logunit.TWEloScoreLogUnit;
import com.hawk.game.service.tiberium.logunit.TWGuildScoreLogUnit;
import com.hawk.game.service.tiberium.logunit.TWPlayerScoreLogUnit;
import com.hawk.game.service.tiberium.logunit.TWPlayerSeasonScoreLogUnit;
import com.hawk.game.service.tiberium.logunit.TWSelfRewardLogUnit;
import com.hawk.game.service.warFlag.IFlag;
import com.hawk.game.util.GsConst.StatisticDataType;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.service.WorldChristmasWarService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.gamelib.GameConst;
import com.hawk.gamelog.GameLog;
import com.hawk.gamelog.LogParam;
import com.hawk.log.Action;
import com.hawk.log.LogConst;
import com.hawk.log.LogConst.ActivityClickType;
import com.hawk.log.LogConst.AddOrReduce;
import com.hawk.log.LogConst.ArmyChangeReason;
import com.hawk.log.LogConst.ArmySection;
import com.hawk.log.LogConst.Channel;
import com.hawk.log.LogConst.ChapterMissionOperType;
import com.hawk.log.LogConst.CityWallChangeType;
import com.hawk.log.LogConst.ClickEventType;
import com.hawk.log.LogConst.CrossStateType;
import com.hawk.log.LogConst.GiftType;
import com.hawk.log.LogConst.GuildAction;
import com.hawk.log.LogConst.GuildTechOperType;
import com.hawk.log.LogConst.GundamKillType;
import com.hawk.log.LogConst.HeroSkillOperType;
import com.hawk.log.LogConst.IMoneyType;
import com.hawk.log.LogConst.InheritCondType;
import com.hawk.log.LogConst.LogInfoType;
import com.hawk.log.LogConst.PowerChangeReason;
import com.hawk.log.LogConst.PowerType;
import com.hawk.log.LogConst.SoldierProtectEventType;
import com.hawk.log.LogConst.StoreHouseOperType;
import com.hawk.log.LogConst.TaskType;
import com.hawk.log.LogConst.WarFlagOwnChangeType;
import com.hawk.sdk.SDKManager;
import com.hawk.sdk.config.PlatformConstCfg;
import com.hawk.serialize.string.SerializeHelper;

/** 
 * tlog日志工具类
 *
 * @author lating 
 * 
 */
public final class LogUtil {
	
	/**
	 * 获取联盟相关或其它非给人数据的公共参数
	 * @param logType
	 * @return
	 */
	public static LogParam getNonPersonalLogParam(LogInfoType logType) {
		LogParam logParam = new LogParam(GsConfig.getInstance().isTlogEnable(), GsConfig.getInstance().getServerId());
		logParam.put("logType", logType.name());
		return logParam;
	}
	
	/**
	 * 获取个人相关的tlog日志公共参数
	 * 
	 * @param player
	 * @param logType
	 * @return
	 */
	public static LogParam getPersonalLogParam(Player player, LogInfoType logType) {
		// tlog打印开关没开，或在白名单内的玩家，不记tlog数据
		if (!GsConfig.getInstance().isTlogEnable() || GameUtil.isTlogPuidControlled(player.getOpenId())) {
			return null;
		}

		try {
			LogParam logParam = new LogParam(GsConfig.getInstance().isTlogEnable(), GsConfig.getInstance().getServerId());
			logParam.put("logType", logType.name())
					.put("puid", player.getOpenId())
					.put("iZoneAreaId", player.getEntity().getServerId())
					.put("playerId", player.getId())
					.put("playerLevel", player.getLevel())
					.put("vipLevel", player.getVipLevel())
					.put("platId", player.getPlatId())
					.put("cityLevel", player.getCityLevel())
					.put("gameAppId", player.getGameAppId())
					.put("dungeon", player.getDungeonMap());
			addSpecialParam(logType, player, logParam);
			return logParam;
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return null;
	}

	/**
	 * 注册、登录、登出添加特有的参数
	 * 
	 * @param logType
	 * @param player
	 * @param logParam
	 */
	private static void addSpecialParam(LogInfoType logType, Player player, LogParam logParam) {
		if (logType != LogInfoType.userlogin && logType != LogInfoType.userlogout && logType != LogInfoType.createuser && logType != LogInfoType.account_register) {
			return;
		}
		
		String appVersion = player.getEntity().getVersion();
		String phoneInfo = player.getClientHardware();
		String channelId = player.getChannelId();
		String telecomOper = player.getTelecomOper();
		String network = player.getNetwork();
		String deviceId = "";
		if (logType != LogInfoType.userlogin || !"android".equalsIgnoreCase(player.getPlatform())) {
			deviceId = player.getDeviceId();
			deviceId = HawkOSOperator.isEmptyString(deviceId) ? "" : deviceId;
		}
		
		if (logType == LogInfoType.userlogin && !"android".equalsIgnoreCase(player.getPlatform())) {
			deviceId = player.idfa;
			deviceId = HawkOSOperator.isEmptyString(deviceId) ? "" : deviceId;
		}
		
		logParam.put("deviceId", deviceId)
				.put("network", HawkOSOperator.isEmptyString(network) ? "NULL" : network)
				.put("phoneInfo", HawkOSOperator.isEmptyString(phoneInfo) ? "NULL" : phoneInfo)
				.put("channelId", HawkOSOperator.isEmptyString(channelId) ? "NULL" : channelId)
				.put("gversion", HawkOSOperator.isEmptyString(appVersion) ? "NULL" : appVersion)
				.put("telecomOper", HawkOSOperator.isEmptyString(telecomOper) ? "NULL" : telecomOper)
				.put("memory", 0)
				.put("density", 0)
				.put("screenWidth", 0)
				.put("screenHeight", 0)
				.put("glRender", "NULL")
				.put("glVersion", "NULL")
				.put("cpuHardware", "NULL")
				.put("systemSoftware", "NULL")
				.put("gamematrix", player.gamematrix);
	}

	/**
	 *  记录心跳日志
	 * @param activePlayerCnt
	 */
	public static void logHeart(int activePlayerCnt) {
		// 放到tlog打印开关判断前面，只主要是考虑GsConst.LOCAL_IP的全局性，为空时先给它赋值，其它地方引用不受影响
		if (HawkOSOperator.isEmptyString(GsConst.LOCAL_IP)) {
			GsConst.LOCAL_IP = HawkOSOperator.getLocalIp();
		}

		if (!GsConfig.getInstance().isTlogEnable()) {
			return;
		}

		LogParam logParam = getNonPersonalLogParam(LogInfoType.heart);
		logParam.put("activePlayer", activePlayerCnt)
				.put("vGameIp", GsConst.LOCAL_IP);
		GameLog.getInstance().info(logParam);
	}

	/**
	 * 记录玩家登陆日志
	 * @param player
	 */
	public static void logPlayerLogin(Player player, int regContinueLoing, int totalLogin) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.userlogin);
		if (logParam == null) {
			return;
		}

		try {
			String leaderId = GuildService.getInstance().getGuildLeaderId(player.getGuildId());
			PowerElectric powerElectic = player.getData().getPowerElectric();
			NationMilitaryEntity nationMilitaryEntity = player.getData().getNationMilitaryEntity();
			logParam.put("guildId", player.hasGuild() ? player.getGuildId() : "")
					.put("numGuildId", player.hasGuild() ? player.getGuildNumId() : "")
					.put("playerName", player.getNameEncoded())
					.put("friendCount", RelationService.getInstance().getFriendNum(player.getId()))
					.put("clientIp", player.getClientIp())
					.put("militaryLevel", player.getMilitaryRankLevel())
					
					.put("battlePoint", player.getPower())
					.put("heroPower", powerElectic.getHeroBattlePoint())
					.put("equipPower", powerElectic.getEquipBattlePoint())
					.put("techPower", powerElectic.getTechBattlePoint())
					.put("commanderPower", powerElectic.getPlayerBattlePoint())
					.put("armyPower", powerElectic.getArmyBattlePoint())
					.put("buildingPower", powerElectic.getBuildBattlePoint())
					.put("crossTechPower", powerElectic.getCrossTechBattlePoint())
					.put("trapPower", powerElectic.getTrapBattlePoint())
					.put("superSoldierPower", powerElectic.getSuperSoldierBattlePoint())
					.put("armourPower", powerElectic.getArmourBattlePoint())
					.put("plantTechPower", powerElectic.getPlantTechBattlePoint())
					.put("plantSciencePower", powerElectic.getPlantScienceBattlePoint())
					.put("plantSchoolPower", powerElectic.getPlantSchoolBattlePoint())
					.put("starExplorePower", powerElectic.getStarExploreBattlePoint())
					.put("manhattanBasePower", powerElectic.getManhattanBaseBattlePoint())
					.put("manhattanSWPower", powerElectic.getManhattanSWBattlePoint())
					.put("mechacoreTechPower", powerElectic.getMechacoreTechPower())
					.put("mechacoreModulePower", powerElectic.getMechacoreModulePower())
					.put("homeLandPower", powerElectic.getHomeLandPower())

					.put("gold", player.getGold())
					.put("diamond", player.getDiamonds())
					.put("saveAmt", player.getPlayerBaseEntity().getSaveAmtTotal())
					.put("goldore", player.getAllResByType(PlayerAttr.GOLDORE_VALUE))
					.put("oil", player.getAllResByType(PlayerAttr.OIL_VALUE))
					.put("steel", player.getAllResByType(PlayerAttr.STEEL_VALUE))
					.put("tombarthite", player.getAllResByType(PlayerAttr.TOMBARTHITE_VALUE))
					.put("heroSSChipCount", player.getData().getItemNumByItemId(Const.ItemId.ITEM_HERO_SS_CHIP_VALUE))
					.put("heroSSSChipCount", player.getData().getItemNumByItemId(Const.ItemId.ITEM_HERO_SSS_CHIP_VALUE))
					.put("crossServer", player.isCsPlayer() ? 1 : 0)
					.put("collegeId", HawkOSOperator.isEmptyString(player.getCollegeId()) ? "NULL" : player.getCollegeId())
					.put("collegeAuth", player.getCollegeAuth())
					.put("inviteComing", player.getEntity().isBeInvited() ? 1 : 0)
					.put("directName", player.getName())
					.put("guildName", HawkOSOperator.isEmptyString(player.getGuildName()) ? "NULL" : player.getGuildName())
					.put("platformusertag", GlobalData.getInstance().getPfAuthStatus(player.getId()))
					.put("clientIpv6", "")
					.put("oaid", !GameUtil.isAndroidAccount(player) ? "" : (HawkOSOperator.isEmptyString(player.oaidOrCaid) ? "" : player.oaidOrCaid))
					.put("caid", GameUtil.isAndroidAccount(player) ? "" : (HawkOSOperator.isEmptyString(player.oaidOrCaid) ? "" : player.oaidOrCaid))
					.put("useragent", HawkOSOperator.isEmptyString(player.useragent) ? "" : player.useragent)
					.put("regContinueLogin", regContinueLoing)
					.put("totalLogin", totalLogin)
					.put("nationMilitaryLv", nationMilitaryEntity.getNationMilitarLlevel())
					.put("skinPoint", player.getData().getSkinPoint());
			AccountRoleInfo leaderRoleInfo = null;
			if (!HawkOSOperator.isEmptyString(leaderId) && (leaderRoleInfo = GlobalData.getInstance().getAccountRoleInfo(leaderId)) != null) {
				logParam.put("leaderId", leaderRoleInfo.getPlayerId());
				logParam.put("leaderOpenId", leaderRoleInfo.getOpenId());
				logParam.put("guildPower", GuildService.getInstance().getGuildBattlePoint(player.getGuildId()));
			} else {
				logParam.put("leaderId", "");
				logParam.put("leaderOpenId", "");
				logParam.put("guildPower", 0);
			}
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 记录玩家登出日志
	 * @param player
	 */
	public static void logPlayerLogout(Player player) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.userlogout);
		if (logParam == null) {
			return;
		}

		try {
			PowerElectric powerElectic = player.getData().getPowerElectric();
			NationMilitaryEntity nationMilitaryEntity = player.getData().getNationMilitaryEntity();
			int onlineTime = (int) (player.getLogoutTime() - player.getLoginTime()) / 1000;
			logParam.put("onlineTime", onlineTime)
					.put("friendCount", RelationService.getInstance().getFriendNum(player.getId()))
					.put("militaryLevel", player.getMilitaryRankLevel())
					.put("battlePoint", player.getPower())
					.put("heroPower", powerElectic.getHeroBattlePoint())
					.put("equipPower", powerElectic.getEquipBattlePoint())
					.put("techPower", powerElectic.getTechBattlePoint())
					.put("commanderPower", powerElectic.getPlayerBattlePoint())
					.put("armyPower", powerElectic.getArmyBattlePoint())
					.put("buildingPower", powerElectic.getBuildBattlePoint())
					.put("gold", player.getGold())
					.put("diamond", player.getDiamonds())
					.put("goldore", player.getAllResByType(PlayerAttr.GOLDORE_VALUE))
					.put("oil", player.getAllResByType(PlayerAttr.OIL_VALUE))
					.put("steel", player.getAllResByType(PlayerAttr.STEEL_VALUE))
					.put("tombarthite", player.getAllResByType(PlayerAttr.TOMBARTHITE_VALUE))
					.put("heroSSChipCount", player.getData().getItemNumByItemId(Const.ItemId.ITEM_HERO_SS_CHIP_VALUE))
					.put("heroSSSChipCount", player.getData().getItemNumByItemId(Const.ItemId.ITEM_HERO_SSS_CHIP_VALUE))
					.put("crossServer", player.isCsPlayer() ? 1 : 0)
					.put("collegeId", HawkOSOperator.isEmptyString(player.getCollegeId()) ? "NULL" : player.getCollegeId())
					.put("collegeAuth", player.getCollegeAuth())
					.put("inviteComing", player.getEntity().isBeInvited() ? 1 : 0)
					.put("directName", player.getName())
					.put("guildName", HawkOSOperator.isEmptyString(player.getGuildName()) ? "NULL" : player.getGuildName())
					.put("guildId", player.hasGuild() ? player.getGuildId() : "")
					.put("numGuildId", player.hasGuild() ? player.getGuildNumId() : "")
					.put("skinPoint", player.getData().getSkinPoint())
					.put("nationMilitaryLv", nationMilitaryEntity.getNationMilitarLlevel())
					.put("platformusertag", GlobalData.getInstance().getPfAuthStatus(player.getId()));
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}

	}

	/** 记录玩家等级流水日志
	 * 
	 * @param expChange
	 * @param oldLevel
	 * @param newLevel
	 * @param costTime
	 * @param action
	 * @param subReason */
	public static void logPlayerExpFlow(Player player, int expChange, int oldLevel, int newLevel, int costTime, Action action, int subReason) {
		LogParam logParam = getPersonalLogParam(player, LogConst.LogInfoType.player_exp_flow);
		if (logParam == null) {
			return;
		}
		int reason = action.intExpVal();
		if (reason < 0) {
			reason = action.intItemVal();
		}
		logParam.put("expChange", expChange)
				.put("beforeLevel", oldLevel)
				.put("afterLevel", newLevel)
				.put("costTime", costTime) // 升级所用时间（秒）
				.put("reason", reason) // 经验流动一级原因
				.put("subReason", subReason);
		GameLog.getInstance().info(logParam);
	}

	/** 记录玩家等级流水日志 */
	public static void logPlayerExpFlow(Player player, int expChange, int oldLevel, int newLevel, int costTime, Action action) {
		logPlayerExpFlow(player, expChange, oldLevel, newLevel, costTime, action, 0);
	}

	/** 记录vip流水日志
	 * 
	 * @param player
	 * @param oldLevel
	 * @param oldVipExp
	 * @param newVipExp
	 * @param changeExp
	 * @param action */
	public static void logVipExpFlow(Player player, int oldLevel, int oldVipExp, int newVipExp, int changeExp, Action action) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.vip_level_flow);
		if (logParam != null) {
			logParam.put("oldLevel", oldLevel)
					.put("changeExp", changeExp)
					.put("beforeExp", oldVipExp)
					.put("afterExp", newVipExp)
					.put("reason", action.intItemVal())
					.put("superVipLevel", player.getSuperVipLevel());
			GameLog.getInstance().info(logParam);
		}
	}

	/** 记录联盟流水日志 */
	public static void logGuildFlow(Player player, int operType, String guildId, String leaderId) {
		if (HawkOSOperator.isEmptyString(leaderId)) {
			leaderId = GuildService.getInstance().getGuildLeaderId(guildId);
		}

		LogParam logParam = getPersonalLogParam(player, LogInfoType.guild_flow);
		if (logParam != null) {
			AccountRoleInfo roleInfo = HawkOSOperator.isEmptyString(leaderId) ? null : GlobalData.getInstance().getAccountRoleInfo(leaderId);
			Collection<String> members = GuildService.getInstance().getGuildMembers(guildId);
			logParam.put("guildOperType", operType)
					.put("guildId", guildId)
					.put("guildName", GuildService.getInstance().getGuildName(guildId))
					.put("numGuildId", HawkOSOperator.isEmptyString(guildId) ? "" : String.valueOf(HawkUUIDGenerator.strUUID2Long(guildId)))
					.put("guildLevel", GuildService.getInstance().getGuildLevel(guildId))
					.put("guildMemNum", members != null ? members.size() : 0)
					.put("leaderId", roleInfo != null ? roleInfo.getPlayerId() : "")
					.put("leaderOpenId", roleInfo != null ? roleInfo.getOpenId() :"");
			GameLog.getInstance().info(logParam);
		}
	}

	/** 记录金币消耗日志信息
	 * 
	 * @param player
	 * @param action
	 * @param logParamMap
	 * @param consumeGold */
	public static void logMoneyFlow(Player player, Action action, int subReason, LogInfoType logType, long moneyChange, int moneyType, String mailUUID) {
		LogParam logParam = getPersonalLogParam(player, logType);
		if (logParam == null) {
			return;
		}
		logParam.put("money", moneyChange)
				.put("afterMoney", moneyType == IMoneyType.MT_GOLD ? player.getGold() : player.getDiamonds())
				.put("reason", action.intItemVal())
				.put("subReason", subReason)
				.put("addOrReduce", LogInfoType.money_consume == logType ? AddOrReduce.REDUCE : AddOrReduce.ADD)
				.put("mailUUID", mailUUID)
				.put("moneyType", moneyType);
		GameLog.getInstance().info(logParam);
	}

	/** 记录金币消耗日志信息 */
	public static void logMoneyFlow(Player player, Action action, LogInfoType logType, long moneyChange, int moneyType) {
		logMoneyFlow(player, action, 0, logType, moneyChange, moneyType, "");
	}

	public static void logMoneyFlow(Player player, Action action, int subReason, LogInfoType logType, int moneyChange, int moneyType) {
		logMoneyFlow(player, action, subReason, logType, moneyChange, moneyType, "");
	}

	/** 记录道具流水日志
	 * 
	 * @param player
	 * @param action
	 * @param logInfoType
	 * @param itemType
	 * @param itemId
	 * @param count
	 * @param money */
	public static void logItemFlow(Player player, Action action, int subReason, LogInfoType logType, int itemType, int itemId,
			long count, int money, int moneyType, String mailUUID) {
		LogParam logParam = getPersonalLogParam(player, logType);
		if (logParam == null) {
			return;
		}

		logParam.put("goodsType", itemType)
				.put("goodsId", itemId)
				.put("count", count)
				.put("afterCount", player.getData().getItemNumByItemId(itemId))
				.put("reason", action.intItemVal())
				.put("subReason", subReason)
				.put("money", money)
				.put("moneyType", moneyType)
				.put("mailUUID", mailUUID)
				.put("addOrReduce", LogInfoType.goods_add == logType ? AddOrReduce.ADD : AddOrReduce.REDUCE);
		GameLog.getInstance().info(logParam);
	}

	/** 记录道具流水日志 */
	public static void logItemFlow(Player player, Action action, LogInfoType logType, int itemType, int itemId, long count, int money, int moneyType) {
		logItemFlow(player, action, 0, logType, itemType, itemId, count, money, moneyType, "");
	}

	public static void logItemFlow(Player player, Action action, int subReason, LogInfoType logType, int itemType, int itemId,
			int count, int money, int moneyType) {
		logItemFlow(player, action, subReason, logType, itemType, itemId, count, money, moneyType, "");
	}

	/** 玩家聊天信息日志记录
	 * 
	 * @param player
	 * @param toPlayerId
	 * @param message
	 * @param recNum */
	public static void logChatInfo(Player player, String toPlayerId, int snsType, String message, int recNum) {
		LogParam logParam = getPersonalLogParam(player, LogInfoType.sns_flow);
		if (logParam != null) {
			logParam.put("recNum", recNum) // 接收玩家个数
					.put("count", 1) // 发送的数量
					.put("subType", 0)
					.put("snsType", snsType) // 交互一级类型
					.put("saveAmt", player.getPlayerBaseEntity().getSaveAmtTotal());

			GameLog.getInstance().info(logParam);
		}
	}
	
	/**
	 * 记录建筑等级变化日志
	 * 
	 * @param player
	 * @param buildingEntity
	 * @param beforeLevel
	 * @param afterLevel
	 */
	public static void logBuildFlow(Player player, BuildingBaseEntity buildingEntity, int beforeLevel, int afterLevel) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.build_flow);
		BuildingCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
		if (logParam != null) {
			logParam.put("buildId", buildingEntity.getId())
					.put("buildType", buildingEntity.getType())
					.put("beforeLevel", beforeLevel)
					.put("afterLevel", afterLevel)
					.put("cfgId", cfg.getId())
					.put("honor", cfg.getHonor())
					.put("progress", cfg.getProgress());
			GameLog.getInstance().info(logParam);
		}

		HawkLog.logPrintln("building-level change, playerId: {}, openid: {}, buildId: {}, buildType: {}, beforeLevel: {}, afterLevel: {}, honor: {}, progress: {}",
				player.getId(), player.getOpenId(), buildingEntity.getId(), buildingEntity.getType(), beforeLevel, afterLevel, cfg.getHonor(), cfg.getProgress());
	}

	/**
	 * 记录礼包购买信息
	 * 
	 * @param player
	 * @param type
	 * @param giftId
	 * @param costMoney
	 * @param moneyType
	 * @param giftItemType
	 */
	public static void logGiftBagFlow(Player player, GiftType type, String giftId, int costMoney, int moneyType, int giftItemType) {
		logGiftBagFlow(player, type, giftId, costMoney, moneyType, giftItemType, 1);
	}
	
	/** 记录礼包购买信息
	 * 
	 * @param player
	 * @param type
	 * @param giftId
	 * @param costMoney 
	 * 
	 * */
	public static void logGiftBagFlow(Player player, GiftType type, String giftId, int costMoney, int moneyType, int giftItemType, int buyCount) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.gift_bag_flow);
		if (logParam != null) {
			logParam.put("giftType", type.intVal())
					.put("giftId", giftId)
					.put("costMoney", costMoney)
					.put("battlePoint", player.getPower())
					.put("moneyType", moneyType)
					.put("itemType", giftItemType)
					.put("buyCount", buyCount);
			GameLog.getInstance().info(logParam);
		}
	}

	/** 世界资源刷新统计
	 * 
	 * @param resCfgId
	 * @param resType
	 * @param resLevel
	 * @param x
	 * @param y
	 * @param areaId */
	public static void logWorldResourceRefrersh(int resCfgId, int resType, int resLevel, int x, int y, int areaId) {
		LogParam logParam = getNonPersonalLogParam(LogInfoType.world_resource_refresh_flow);
		logParam.put("resCfgId", resCfgId)
				.put("resType", resType)
				.put("resLevel", resLevel)
				.put("x", x)
				.put("y", y)
				.put("areaId", areaId);
		GameLog.getInstance().info(logParam);
	}

	/** 世界资源采集统计
	 * 
	 * @param player
	 * @param resCfgId
	 * @param resType
	 * @param resLevel
	 * @param collectNum
	 * @param collectTime 采集时长，单位：ms
	 * 
	 * */
	public static void logWorldCollect(Player player, int resCfgId, int resType, int resLevel, long collectNum, long collectTime) {
		if (collectNum <= 0) {
			return;
		}
		
		StatisticsEntity statisticsEntity = player.getData().getStatisticsEntity();
		statisticsEntity.addCommonStatisData(StatisticDataType.COLLECT_RES_TODAY, collectNum);
		
		LogParam logParam = getPersonalLogParam(player, LogInfoType.world_resource_collect_flow);
		if (logParam != null) {
			logParam.put("resCfgId", resCfgId)
					.put("resType", resType)
					.put("resLevel", resLevel)
					.put("collectNum", collectNum)
					.put("collectTime", collectTime);
			GameLog.getInstance().info(logParam);
		}
	}

	/** 世界击杀怪物统计
	 * 
	 * @param player
	 * @param monsterId
	 * @param isWin
	 * @param isFirstKill */
	public static void logAttackMonster(Player player, int posX, int posY, int monsterType, int monsterId, int monsterLevel, int atkTimes,
			int pracAtkTimes, int beforeBlood, int afterBlood, boolean isKill, boolean isFirstKill, boolean isLeader) {
		LogParam logParam = getPersonalLogParam(player, LogInfoType.world_attack_monster_flow);
		if (logParam == null) {
			return;
		}

		logParam.put("posX", posX)
				.put("posY", posY)
				.put("monsterType", monsterType)
				.put("monsterId", monsterId)
				.put("monsterLvl", monsterLevel)
				.put("atkTimes", atkTimes)
				.put("pracAtkTimes", pracAtkTimes)
				.put("beforeBlood", beforeBlood)
				.put("afterBlood", afterBlood)
				.put("isKill", isKill ? 1 : 0)
				.put("isFirstKill", isFirstKill ? 1 : 0)
				.put("isLeader", isLeader ? 1 : 0)
				.put("isWin", isKill ? 1 : 0)
				.put("dungeon", player.getDungeonMap());
		GameLog.getInstance().info(logParam);
	}

	/** 世界攻打迷雾要塞统计
	 * 
	 * @param player
	 * @param posX
	 * @param posY
	 * @param foggyId
	 * @param foggyLvl
	 * @param isLeader
	 * @param isWin
	 * @param playerNum */
	public static void logAttackFoggy(Player player, int posX, int posY, int foggyId, int foggyLvl, boolean isLeader, boolean isWin, int playerNum) {
		LogParam logParam = getPersonalLogParam(player, LogInfoType.world_attack_foggy);
		if (logParam == null) {
			return;
		}

		PowerElectric powerElectic = player.getData().getPowerElectric();
		logParam.put("posX", posX)
				.put("posY", posY)
				.put("foggyId", foggyId)
				.put("foggyLvl", foggyLvl)
				.put("isLeader", isLeader ? 1 : 0)
				.put("isWin", isWin ? 1 : 0)
				.put("playerNum", playerNum)
				.put("battlePoint", player.getPower())
				.put("heroPower", powerElectic.getHeroBattlePoint())
				.put("equipPower", powerElectic.getEquipBattlePoint())
				.put("techPower", powerElectic.getTechBattlePoint())
				.put("commanderPower", powerElectic.getPlayerBattlePoint())
				.put("armyPower", powerElectic.getArmyBattlePoint())
				.put("buildingPower", powerElectic.getBuildBattlePoint())
				.put("dungeon", player.getDungeonMap());

		GameLog.getInstance().info(logParam);
	}

	/** 联盟数据
	 * 
	 * @param guildAction */
	public static void logGuildAction(GuildAction guildAction, String guildId) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.guild_action);
			logParam.put("guildAction", guildAction.intVal()).put("guildId", guildId == null ? "NULL" : guildId);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/** 记录活动点击事件
	 * 
	 * @param player
	 * @param clickType
	 *            点击类型
	 * @param btnType
	 *            按钮类型
	 * @param activityId
	 *            活动Id
	 * @param btnId
	 *            活动子按钮cell Id */
	public static void logActivityClickFlow(Player player, ActivityClickType clickType, String... args) {
		LogParam logParam = getPersonalLogParam(player, LogInfoType.activity_click);
		if (logParam != null) {
			logParam.put("clickType", clickType.intVal())
					.put("btnType", args[0])
					.put("activityId", args.length > 1 ? args[1] : 0)
					.put("btnId", args.length > 2 ? args[2] : 0);
			GameLog.getInstance().info(logParam);
		}
	}

	/** 记录首充点击事件
	 * 
	 * @param player
	 * @param clickType
	 * @param args */
	public static void logFirstRechargeFlow(Player player, String arg) {
		LogParam logParam = getPersonalLogParam(player, LogInfoType.boundary_recharge_click);
		if (logParam != null) {
			logParam.put("clickType", arg);
			GameLog.getInstance().info(logParam);
		}
	}

	/** 记录超值礼包入口点击进入事件
	 * 
	 * @param player */
	public static void logEnterGift(Player player) {
		LogParam logParam = getPersonalLogParam(player, LogInfoType.enter_gift);
		if (logParam != null) {
			GameLog.getInstance().info(logParam);
		}
	}

	/** 记录真人视频打点信息
	 * 
	 * @param player
	 * @param stepId */
	public static void logGameVideo(Player player, String stepId, String state) {
		LogParam logParam = getPersonalLogParam(player, LogInfoType.video_flow);
		if (logParam != null) {
			logParam.put("step", stepId)
					.put("state", state);
			GameLog.getInstance().info(logParam);
		}
	}

	/** 资源流水日志
	 * 
	 * @param player
	 * @param action
	 * @param logType
	 * @param resourceType
	 * @param after
	 * @param count */
	public static void logResourceFlow(Player player, Action action, int subReason, LogInfoType logType, int resourceType, long after, long count, String mailUUID) {
		LogParam logParam = getPersonalLogParam(player, logType);
		if (logParam == null) {
			return;
		}

		logParam.put("resourceType", resourceType)
				.put("count", count)
				.put("after", after)
				.put("reason", action.intItemVal())
				.put("subReason", subReason)
				.put("mailUUID", mailUUID)
				.put("addOrReduce", LogInfoType.resource_add == logType ? AddOrReduce.ADD : AddOrReduce.REDUCE);
		GameLog.getInstance().info(logParam);
	}

	public static void logResourceFlow(Player player, Action action, int subReason, LogInfoType logType, int resourceType, long after, long count) {
		logResourceFlow(player, action, subReason, logType, resourceType, after, count, "");
	}

	/** 资源流水日志 */
	public static void logResourceFlow(Player player, Action action, LogInfoType logType, int resourceType, long after, long count) {
		logResourceFlow(player, action, 0, logType, resourceType, after, count, "");
	}

	/** 有奖励邮件发放 */
	public static void logRewardMailSendFlow(Player player, String mailUUID, int mailId, LogInfoType logType, String reward) {
		if (player == null) {
			return;
		}
		
		try {
			LogParam logParam = getPersonalLogParam(player, logType);
			if (logParam == null) {
				return;
			}
			
			logParam.put("mailId", mailId)
			.put("reward", reward)
			.put("mailUUID", mailUUID);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}

	}

	/** 记录购买月卡流水
	 * 
	 * @param player
	 * @param cardId
	 * @param renew */
	public static void logBuyMonthCardFlow(Player player, int cardId, boolean renew, long validEndTime) {
		LogParam logParam = getPersonalLogParam(player, LogInfoType.buy_month_card);
		if (logParam != null) {
			logParam.put("monthCardId", cardId)
					.put("renewBuy", renew ? 1 : 0)
					.put("validEndTime", validEndTime / 1000);
			GameLog.getInstance().info(logParam);
		}
	}

	/** 记录英雄属性变化日志 (注：参数列表中参数的类型不一定就是int，可根据实际情况修改类型，如果两个不同英雄拥有相同的heroId，可以添加一个英雄uuid以做区分)
	 * 
	 * @param player
	 *            英雄所属玩家
	 * @param action
	 *            变化来源
	 * @param hero
	 *            英雄
	 * @param heroPoint
	 *            战略点 */
	public static void logHeroAttrChange(Player player, Action action, int heroPoint, PlayerHero hero) {
		if (hero == null) {
			return;
		}

		LogParam logParam = getPersonalLogParam(player, LogInfoType.hero_change);
		if (logParam != null) {
			HeroCfg heroCfg = HawkConfigManager.getInstance().getConfigByKey(HeroCfg.class, hero.getCfgId());
			int heroType = heroCfg == null ? 0 : heroCfg.getHeroClass();
			HeroOfficeCfg officeCfg = HawkConfigManager.getInstance().getConfigByKey(HeroOfficeCfg.class, hero.getOffice());
			int buildType = officeCfg == null ? 0 : officeCfg.getUnlockBuildingType();
			
			logParam.put("heroId", hero.getCfgId())
					.put("heroStar", hero.getStar())
					.put("heroStep", hero.getStep())
					.put("heroLevel", hero.getLevel())
					.put("heroPower", hero.power())
					.put("heroPoint", heroPoint)
					.put("action", action.intItemVal())
					.put("heroType", heroType)
					.put("heroOffice", hero.getOffice())
					.put("buildType", buildType)
					.put("talent", hero.talentPowerStr())
					.put("dungeon", player.getDungeonMap())
					.put("archiveLevel", hero.getArchiveLevel())
					.put("soul", hero.getSoul().toString());
			GameLog.getInstance().info(logParam);
		}
	}

	/** 
	 * 英雄抽奖
	 * 
	 * @param player
	 * @param type  抽奖类型
	 *             NORMAL_ONE = 101;  // 英雄普通一次
	 *             NORMAL_TEN = 110;  // 英雄普通十次
	 *             ADVANCE_ONE = 201; // 英雄高级一次
	 *             ADVANCE_TEN = 210; // 英雄高级十次
	 *             SKILL_ONE = 301;   // 芯片普通一次
	 *             SKILL_TEN = 310;   // 芯片普通十次
	 * @param cost 抽奖消耗
	 * 
	 */
	public static void logGacha(Player player, int type, List<ItemInfo> cost) {
		LogInfoType logType = LogInfoType.gacha_flow;
		if (type == GachaType.ARMOUR_ONE_VALUE || type == GachaType.ARMOUR_TEN_VALUE || type == GachaType.ARMOUR_BOX_VALUE) {
			logType = LogInfoType.gacha_equip;
		}
		
		LogParam logParam = getPersonalLogParam(player, logType);
		if (logParam == null) {
			return;
		}

		logParam.put("gachaType", type);
		if (cost == null || cost.isEmpty()) {
			logParam.put("consumeType", 0)
					.put("consumeNum", 0);
			GameLog.getInstance().info(logParam);
		} else {
			for (ItemInfo itemInfo : cost) {
				logParam.put("consumeType", itemInfo.getItemId())
						.put("consumeNum", itemInfo.getCount());
				GameLog.getInstance().info(logParam);
			}
		}
	}

	/** 
	 * 记录抽卡获取的物品信息
	 * 
	 * @param player
	 * @param type
	 * @param gachaAwardItems 
	 * 
	 */
	public static void logGachaItemsFlow(Player player, int type, List<ItemInfo> gachaAwardItems) {
		LogInfoType logType = LogInfoType.gacha_item_flow;
		if (type == GachaType.ARMOUR_ONE_VALUE || type == GachaType.ARMOUR_TEN_VALUE || type == GachaType.ARMOUR_BOX_VALUE) {
			logType = LogInfoType.gacha_equip_cost;
		}
		
		LogParam logParam = getPersonalLogParam(player, logType);
		if (logParam == null) {
			return;
		}

		logParam.put("gachaType", type);
		for (ItemInfo itemInfo : gachaAwardItems) {
			logParam.put("itemType", itemInfo.getItemType().getNumber())
					.put("itemId", itemInfo.getItemId())
					.put("amount", itemInfo.getCount());
			GameLog.getInstance().info(logParam);
		}
	}

	/** 记录英雄技能的合成或分解信息
	 * 
	 * @param player
	 * @param type
	 * @param skillId
	 * @param levelBefore
	 * @param levelAfter
	 * @param operType */
	public static void logHeroSkillChange(Player player, int heroId, int index, int type, int skillId, int levelBefore, int levelAfter, HeroSkillOperType operType) {
		LogParam logParam = getPersonalLogParam(player, LogInfoType.hero_skill_change);
		if (logParam == null) {
			return;
		}

		logParam.put("act", operType.intVal())
				.put("type", type)
				.put("skillId", skillId)
				.put("levelBefore", levelBefore)
				.put("levelAfter", levelAfter)
				.put("heroId", heroId)
				.put("index", index);
		GameLog.getInstance().info(logParam);
	}

	/** 英雄技能技能安装、卸载
	 * 
	 * @param player
	 * @param type
	 * @param skillId
	 * @param skillLevel
	 * @param equip
	 * @param hero */
	public static void logHeroSkillEquip(Player player, int type, int skillId, int skillLevel, boolean equip, PlayerHero hero) {
		if (hero == null) {
			return;
		}

		LogParam logParam = getPersonalLogParam(player, LogInfoType.hero_skill_equip);
		if (logParam == null) {
			return;
		}

		logParam.put("act", equip ? 1 : 0)
				.put("type", type)
				.put("skillId", skillId)
				.put("skillLevel", skillLevel)
				.put("heroId", hero.getCfgId())
				.put("heroStar", hero.getStar())
				.put("heroLevel", hero.getLevel());
		GameLog.getInstance().info(logParam);
	}

	/** 记录装备属性变化日志 (注：参数列表中参数的类型不一定就是int，可根据实际情况修改类型，如果两件不同装备拥有相同的equipId，可以添加一个装备uuid以做区分)
	 * 
	 * @param player
	 * @param equipId
	 *            装备uid
	 * @param cfgId
	 *            装备配置id
	 * @param remove
	 *            是否是删除装备，true是false否
	 * @param posId
	 *            装备部位
	 * @param equipPower
	 *            装备战力
	 * @param equipQuality
	 *            装备品质
	 * @param equipLevel
	 *            装备等级 */
	public static void logEquipmentAttrChange(Player player, String equipId, int cfgId, boolean remove, int posId, int equipPower, int equipQuality, int equipLevel) {
		LogParam logParam = getPersonalLogParam(player, LogInfoType.equip_change);
		if (logParam != null) {
			logParam.put("equipId", equipId)
					.put("typeId", cfgId)
					.put("remove", remove ? 1 : 0)
					.put("equipPos", posId)
					.put("equipPower", equipPower)
					.put("equipQuality", equipQuality)
					.put("equipLevel", equipLevel);
			GameLog.getInstance().info(logParam);
		}
	}

	/** 记录充值流水
	 * 
	 * @param player
	 * @param rechargeType
	 * @param goodsId */
	public static void logRechargeFlow(Player player, int rechargeType, String goodsId, int costMoney) {
		LogParam logParam = getPersonalLogParam(player, LogInfoType.recharge_flow);
		if (logParam == null) {
			return;
		}

		PowerElectric powerElectic = player.getData().getPowerElectric();
		logParam.put("type", rechargeType)
				.put("goodsId", goodsId)
				.put("power", player.getPower())
				.put("heroPower", powerElectic.getHeroBattlePoint())
				.put("equipPower", powerElectic.getEquipBattlePoint())
				.put("techPower", powerElectic.getTechBattlePoint())
				.put("commanderPower", powerElectic.getPlayerBattlePoint())
				.put("armyPower", powerElectic.getArmyBattlePoint())
				.put("buildingPower", powerElectic.getBuildBattlePoint())
				.put("costMoney", costMoney)
				.put("collegeId", HawkOSOperator.isEmptyString(player.getCollegeId()) ? "NULL" : player.getCollegeId())
				.put("collegeAuth", player.getCollegeAuth())
				.put("inviteComing", player.getEntity().isBeInvited() ? 1 : 0)
				;
		GameLog.getInstance().info(logParam);
	}

	/** 军需处购买资源信息
	 * 
	 * @param player
	 * @param resourceType
	 * @param resourceCount
	 * @param costMoney */
	public static void logWishingCostFlow(Player player, int resourceType, int resourceCount, int costMoney) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.wishing_cost_flow);
		if (logParam != null) {
			logParam.put("resourceType", resourceType)
					.put("resourceCount", resourceCount)
					.put("costMoney", costMoney);
			GameLog.getInstance().info(logParam);
		}
	}

	/** 记录购买基金流水
	 * 
	 * @param player
	 * @param type */
	public static void logBuyFundFlow(Player player, ActivityType type) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.fund_active);
		if (logParam != null) {
			logParam.put("fundType", type.intValue());
			GameLog.getInstance().info(logParam);
		}
	}

	/** 记录最强指挥官积分变更
	 * 
	 * @param player
	 * @param rankType
	 *            排行类型(阶段/总排行)
	 * @param stageId
	 *            阶段id
	 * @param score
	 *            积分 */
	public static void logStrongestLeaderScoreFlow(Player player, ActivityRankType rankType, int stageId, long score) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.commander_score_refresh);
		if (logParam != null) {
			logParam.put("stageId", stageId)
					.put("scoreType", rankType == ActivityRankType.STRONGEST_TOTALL_RANK ? 0 : 1)
					.put("score", score);
			GameLog.getInstance().info(logParam);
		}
	}
	
	/** 记录王者联盟个人积分变更
	 * 
	 * @param player
	 * @param guildId
	 * @param rankType
	 *            排行类型(阶段/总排行)
	 * @param stageId
	 *            阶段id
	 * @param score
	 *            积分 */
	public static void logStrongestGuildPersonScoreFlow(Player player, int rankType, int termId, int stageId, long score) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.strongest_guild_person_score);
			if (logParam != null) {
				String guildId = player.getGuildId();
				logParam.put("termId", termId)
				.put("stageId", stageId)
				.put("scoreType", rankType)
				.put("score", score)
				.put("guildId", HawkOSOperator.isEmptyString(guildId) ? "NULL" : guildId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/** 记录黑市黑金礼包刷出信息
	 * 
	 * @param player
	 * @param giftId */
	public static void logTravelShopGiftRefresh(Player player, int giftId) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.travel_shop_gift_refresh);
		if (logParam != null) {
			logParam.put("giftId", giftId);
			GameLog.getInstance().info(logParam);
		}
	}

	/** 记录推送礼包刷出信息
	 * 
	 * @param player
	 * @param conditionType
	 * @param groupId */
	public static void logPushGiftRefresh(Player player, int conditionType, int groupId, int giftLevelId) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.push_gift_refresh);
		if (logParam != null) {
			logParam.put("conditionType", conditionType)
					.put("groupId", groupId)
					.put("giftLevelId", giftLevelId);
			GameLog.getInstance().info(logParam);
		}
	}

	/** 记录建筑升级操作日志
	 * 
	 * @param player
	 * @param buildingEntity
	 * @param beforeLevel
	 * @param immediate */
	public static void logBuildLvUpOperation(Player player, BuildingBaseEntity buildingEntity, int beforeLevel, boolean immediate) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.build_lv_up_flow);
		if (logParam != null) {
			logParam.put("buildId", buildingEntity.getId())
					.put("buildType", buildingEntity.getType())
					.put("beforeLevel", beforeLevel)
					.put("operType", immediate ? 1 : 0)
					.put("cfgId", buildingEntity.getBuildingCfgId());
			GameLog.getInstance().info(logParam);
		}
	}

	/** 记录训练兵种操作日志
	 * 
	 * @param player
	 * @param buildType
	 * @param soldierId
	 * @param soldierLv
	 * @param trainCount
	 * @param immediate */
	public static void logTrainOperation(Player player, int buildType, int soldierId, int soldierLv, int trainCount, boolean immediate) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.train_flow);
		if (logParam != null) {
			logParam.put("buildType", buildType)
					.put("soldierId", soldierId)
					.put("soldierLv", soldierLv)
					.put("trainCount", trainCount)
					.put("operType", immediate ? 1 : 0);
			GameLog.getInstance().info(logParam);
		}
	}

	/** 记录科技研究操作日志
	 * 
	 * @param player
	 * @param techId
	 * @param techLevel
	 * @param immediate */
	public static void logTechResearchOperation(Player player, int techId, int techLevel, boolean immediate) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.tech_research_flow);
		if (logParam != null) {
			logParam.put("techId", techId)
					.put("techLv", techLevel)
					.put("operType", immediate ? 1 : 0);
			GameLog.getInstance().info(logParam);
		}
	}

	/** 记录战斗部队信息日志
	 * 
	 * @param battleLogHelper */
	public static void logBattleFlow(Player player, BattleLogHelper battleLogHelper) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.battle_flow);
		if (logParam != null) {
			battleLogHelper.genLogParam(logParam);
			GameLog.getInstance().info(logParam);
		}
	}

	/** 记录RTS
	 * 
	 * @param player
	 * @param actionType
	 *            {@link PlotBattle.ActionType}
	 * @param levelId
	 *            rts的关卡ID */
	public static void logRts(Player player, int levelsId, int actionType) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.rts_flow);
		if (logParam != null) {
			logParam.put("levelsId", levelsId)
					.put("actionType", actionType);
			GameLog.getInstance().info(logParam);
		}
	}

	/** 记录超值礼包刷新
	 * 
	 * @param player
	 * @param giftId */
	public static void logGiftRefresh(Player player, int giftId) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.gift_refresh_flow);
		if (logParam != null) {
			logParam.put("giftId", giftId);
			GameLog.getInstance().info(logParam);
		}
	}

	/** 记录任务流水日志
	 * 
	 * @param player
	 * @param type
	 *            任务大类型：1主线任务，2剧情任务，3军衔任务
	 * @param taskType
	 *            二级任务类型
	 * @param taskId
	 *            任务ID
	 * @param state
	 *            任务状态
	 * @param chapterId
	 *            剧情任务和章节任务的章节ID，主线任务默认为0 */
	public static void logTaskFlow(Player player, TaskType type, int taskType, int taskId, int state, int chapterId) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.task_flow);
		if (logParam != null) {
			logParam.put("type", type.intVal())
					.put("taskType", taskType)
					.put("taskId", taskId)
					.put("state", state)
					.put("chapterId", chapterId);
			GameLog.getInstance().info(logParam);
		}
	}

	/** 章节类型任务变化记录
	 * 
	 * @param player
	 * @param taskType
	 * @param chapterId
	 * @param operType */
	public static void logChapterMissionFlow(Player player, TaskType taskType, int chapterId, ChapterMissionOperType operType) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.chapter_mission_flow);
		if (logParam != null) {
			logParam.put("missionType", taskType.intVal())
					.put("type", operType.intVal())
					.put("chapterId", chapterId);
			GameLog.getInstance().info(logParam);
		}
	}

	/** 记录城防数据变化
	 * 
	 * @param player
	 * @param source */
	public static void logCityWallDataChange(Player player, CityWallChangeType source) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.city_wall_data_flow);
		if (logParam != null) {
			try {
				int buildLevel = player.getData().getBuildingMaxLevel(BuildingType.CITY_WALL_VALUE); // 城墙等级
				int curCityDef = player.getData().getPlayerBaseEntity().getCityDefVal(); // 当前城防值
				int maxCityDef = player.getData().getRealMaxCityDef(); // 最大城防值
				int defWeaponCapacity = player.getData().getTrapCapacity(); // 城防武器容量
				long onFireEnd = player.getData().getPlayerBaseEntity().getOnFireEndTime();
				boolean onFire = onFireEnd > HawkTime.getMillisecond();
				logParam.put("buildLevel", buildLevel)
						.put("curCityDef", curCityDef)
						.put("maxCityDef", maxCityDef)
						.put("onFire", onFire ? 1 : 0)
						.put("capacity", defWeaponCapacity)
						.put("source", source.intVal());
				GameLog.getInstance().info(logParam);
				
				List<Integer> heroIdList = player.getAllHero().stream().map(PlayerHero::getCfgId).collect(Collectors.toList());
				EffectParams effParams = new EffectParams();
				effParams.setHeroIds(heroIdList);
				int speedAdd = player.getEffect().getEffVal(EffType.CITY_FIRE_SPD);
				int speedSub = player.getEffect().getEffVal(EffType.CITY_FIRE_SPEED_SLOW, effParams);
				HawkLog.logPrintln("logCityWallDataChange, playerId: {}, buildLevel: {}, curCityDef: {}, maxCityDef: {}, onFire: {}, source: {}, "
						+ "speedAdd: {}, speedSub: {}", player.getId(), buildLevel, curCityDef, maxCityDef, onFire, source.intVal(), speedAdd, speedSub);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}

	/** 记录盟军宝藏抽奖信息
	 * 
	 * @param player
	 * @param type
	 *            抽奖类型:1 免费, 2 单抽, 3 十连抽
	 * @param lucky
	 *            幸运值
	 * @param isMulti
	 *            是否处于翻倍
	 * @param causeMulti
	 *            是否触发翻倍
	 * @param results
	 *            抽奖结果: 1001,1002,1001```1012
	 * @param multis
	 *            奖励倍数: 1,3,2'''2,1 */
	public static void logLotteryDrawFlow(Player player, int type, int lucky, boolean isMulti, boolean causeMulti, String results, String multis) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.lottery_draw_flow);
		if (logParam != null) {
			logParam.put("type", type)
					.put("lucky", lucky)
					.put("multi", isMulti ? 1 : 0)
					.put("causeMulti", causeMulti ? 1 : 0)
					.put("results", results)
					.put("multis", multis);
			GameLog.getInstance().info(logParam);
		}
	}

	/** 玩家资源掠夺上限打点
	 * 
	 * @param player
	 * @param grabResLimit
	 *            当日可掠夺资源上限
	 * @param resId
	 *            资源id
	 * @param grabRes
	 *            当日实际掠夺的资源总量 */
	public static void logGrabResLimit(Player player, int resId, int grabResLimit, int grabRes) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.grab_res_limit);
		if (logParam != null) {
			logParam
					.put("resId", resId)
					.put("grabResLimit", grabResLimit)
					.put("grabRes", grabRes);
			GameLog.getInstance().info(logParam);
		}
	}

	/** 联盟宝藏数据打点记录
	 * 
	 * @param player
	 * @param uuid
	 *            宝藏uuid
	 * @param cfgId
	 *            宝藏配置id
	 * @param operType
	 *            操作类型
	 * @param cost
	 *            挖掘、加速、刷新登录操作，消耗的货币数量 */
	public static void logStoreHouseFlow(Player player, String uuid, int cfgId, StoreHouseOperType operType, int cost) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.store_house_flow);
		if (logParam != null) {
			logParam.put("uuid", uuid)
					.put("cfgId", cfgId)
					.put("operType", operType.intVal())
					.put("cost", cost);
			GameLog.getInstance().info(logParam);
		}
	}

	/** 联盟科技操作数据打点记录
	 * 
	 * @param player
	 * @param techId
	 * @param operType
	 * @param costType
	 * @param cost */
	public static void logGuildTechFlow(Player player, int techId, GuildTechOperType operType, int costType, int cost) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.guild_tech_flow);
		if (logParam != null) {
			logParam.put("techId", techId)
					.put("operType", operType.intVal())
					.put("costType", costType)
					.put("cost", cost);
			GameLog.getInstance().info(logParam);
		}
	}

	/** 士兵数量变化记录
	 * 
	 * @param player
	 * @param entity
	 * @param changeCount
	 * @param section
	 * @param reason */
	public static void logArmyChange(Player player, ArmyEntity entity, int changeCount, int advance, int advanceFinish, int diedCount, ArmySection section,
			ArmyChangeReason reason) {
		HawkLog.logPrintln("armyChangeFlow, playerId: {}, armyId: {}, count: {}, free: {}, march: {}, wounded: {}, cure: {}, "
				+ "cureFinish: {}, train: {}, trainFinish: {}, advance: {}, advanceFinish: {}, section: {}, reason: {}",
				player.getId(), entity.getArmyId(), changeCount, entity.getFree(), entity.getMarch(), entity.getWoundedCount(), entity.getCureCount(),
				entity.getCureFinishCount(), entity.getTrainCount(), entity.getTrainFinishCount(), advance, advanceFinish,
				section.name(), reason.name());

		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.army_change);
		if (logParam == null) {
			return;
		}
		if (advance == 0 && advanceFinish == 0 && (int) entity.getAdvancePower() > 0) {
			if (entity.getTrainCount() > 0) {
				advance = entity.getTrainCount();
			} else if (entity.getTrainFinishCount() > 0) {
				advanceFinish = entity.getTrainFinishCount();
			}
		}
		
		int effPer = 0;
		BattleSoldierCfg armyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, entity.getArmyId());
		if (reason == ArmyChangeReason.CURE && armyCfg != null) {
			if (armyCfg.isPlantSoldier()) {
				effPer = player.getData().getEffVal(Const.EffType.PLANT_SOLDIER_4127) + GameUtil.getCurePlantSpeedEffPerByArmyId(player, armyCfg.getId());
			} else {
				effPer = player.getData().getEffVal(Const.EffType.CITY_HURT_CRUT_SPD) + GameUtil.getCureSpeedEffPerByArmyId(player, armyCfg.getId());
			}
		}
		
		logParam.put("armyId", entity.getArmyId())
		.put("count", changeCount)
		.put("free", entity.getFree())
		.put("march", entity.getMarch())
		.put("wounded", entity.getWoundedCount())
		.put("cure", entity.getCureCount())
		.put("cureFinish", entity.getCureFinishCount())
		.put("taralabs", diedCount)
		.put("train", entity.getTrainCount())
		.put("trainFinish", entity.getTrainFinishCount())
		.put("advance", advance)
		.put("advanceFinish", advanceFinish)
		.put("section", section.intVal())
		.put("reason", reason.intVal())
		.put("deadCount1", entity.getNationalHospitalDeadCount())
		.put("recoveredCount1", entity.getNationalHospitalRecoveredCount())
		.put("deadCount2", entity.getTszzDeadCount())
		.put("recoveredCount2", entity.getTszzRecoveredCount())
		.put("cureEffPer", effPer);
		GameLog.getInstance().info(logParam);
	}

	/** 士兵数量变化记录 */
	public static void logArmyChange(Player player, ArmyEntity entity, int changeCount, ArmySection section, ArmyChangeReason reason) {
		logArmyChange(player, entity, changeCount, 0, 0, 0, section, reason);
	}

	/** 士兵数量变化记录, 记录死兵 */
	public static void logArmyChange(Player player, ArmyEntity entity, int changeCount, int dieCount, ArmySection section, ArmyChangeReason reason) {
		logArmyChange(player, entity, changeCount, 0, 0, dieCount, section, reason);
	}

	/** 士兵数量变化记录, 记录晋升相关的兵 */
	public static void logArmyChange(Player player, ArmyEntity entity, int changeCount, int advance, int advanceFinish, ArmySection section, ArmyChangeReason reason) {
		logArmyChange(player, entity, changeCount, advance, advanceFinish, 0, section, reason);
	}

	/** 记录战力变化
	 * 
	 * @param player
	 * @param powerAmount
	 * @param type 战力变化类型
	 * @param reason 战力变化原因 
	 */
	public static void logPowerFlow(Player player, int powerAmount, PowerType type, PowerChangeReason reason) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.power_flow);
		if (logParam != null) {
			PowerElectric powerElectic = player.getData().getPowerElectric();
			logParam.put("type", type.intVal())
					.put("amount", powerAmount)
					.put("add", powerAmount > 0 ? 0 : 1)
					.put("reason", reason.intVal())
					.put("battlePoint", player.getPower())
					.put("heroPower", powerElectic.getHeroBattlePoint())
					.put("equipPower", powerElectic.getEquipBattlePoint())
					.put("techPower", powerElectic.getTechBattlePoint())
					.put("commanderPower", powerElectic.getPlayerBattlePoint())
					.put("armyPower", powerElectic.getArmyBattlePoint())
					.put("buildingPower", powerElectic.getBuildBattlePoint())
					.put("crossTechPower", powerElectic.getCrossTechBattlePoint())
					.put("trapPower", powerElectic.getTrapBattlePoint())
					.put("superSoldierPower", powerElectic.getSuperSoldierBattlePoint())
					.put("armourPower", powerElectic.getArmourBattlePoint())
					.put("plantTechPower", powerElectic.getPlantTechBattlePoint())
					.put("plantSciencePower", powerElectic.getPlantScienceBattlePoint())
					.put("plantSchoolPower", powerElectic.getPlantSchoolBattlePoint())
					.put("starExplorePower", powerElectic.getStarExploreBattlePoint())
					.put("manhattanBasePower", powerElectic.getManhattanBaseBattlePoint())
					.put("manhattanSWPower", powerElectic.getManhattanSWBattlePoint())
					.put("mechacoreTechPower", powerElectic.getMechacoreTechPower())
					.put("mechacoreModulePower", powerElectic.getMechacoreModulePower())
					.put("homeLandPower", powerElectic.getHomeLandPower());
					if (player.hasGuild()) {
						long newGuildPower = GuildService.getInstance().getGuildBattlePoint(player.getGuildId()) + powerAmount;
						logParam.put("guildId", player.getGuildId());
						logParam.put("guildPower", newGuildPower);
					} else {
						logParam.put("guildId", "");
						logParam.put("guildPower", 0);
					}
			GameLog.getInstance().info(logParam);
		}
	}

	/** 记录侦查流水日志
	 * 
	 * @param player
	 * @param targetPlayer
	 * @param pointType */
	public static void logDetectFlow(Player player, Player targetPlayer, int pointType, long pointArmyPower) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.detect_flow);
		if (logParam != null) {
			logParam.put("type", pointType)
					.put("battlePoint", player.getPower())
					.put("armyPower", player.getData().getPowerElectric().getArmyBattlePoint());

			if (targetPlayer != null) {
				logParam.put("targetPuid", targetPlayer.getOpenId())
						.put("targetPlayerId", targetPlayer.getId())
						.put("targetPlayerLevel", targetPlayer.getLevel())
						.put("targetVipLevel", targetPlayer.getVipLevel())
						.put("targetCityLevel", targetPlayer.getCityLevel())
						.put("targetBattlePoint", targetPlayer.getPower())
						.put("targetArmyPower", targetPlayer.getData().getPowerElectric().getArmyBattlePoint())
						.put("targetPointArmyPower", pointArmyPower);
			} else {
				logParam.put("targetPuid", "NULL")
						.put("targetPlayerId", "NULL")
						.put("targetPlayerLevel", 0)
						.put("targetVipLevel", 0)
						.put("targetCityLevel", 0)
						.put("targetBattlePoint", 0)
						.put("targetArmyPower", 0)
						.put("targetPointArmyPower", pointArmyPower);
			}

			GameLog.getInstance().info(logParam);
		}
	}

	/** 藏兵洞
	 * 
	 * @param player
	 * @param hideArmyPower
	 * @param totalArmyPower */
	public static void logTibetanArmyHole(Player player, long hideArmyPower, long totalArmyPower, int hideTimeSec, boolean callback) {
		LogParam logParam = getPersonalLogParam(player, LogInfoType.tibetan_army_hole);
		if (logParam != null) {
			logParam.put("hideArmyPower", hideArmyPower)
					.put("totalArmyPower", totalArmyPower)
					.put("hideTime", hideTimeSec)
					.put("callback", callback ? 1 : 0);
			GameLog.getInstance().info(logParam);
		}
	}

	/** 记录实时在线数据
	 * 
	 * @param onlinecntIOS
	 * @param onlinecntAndroid */
	public static void logOnlinecnt(String channel, int onlinecntIOS, int onlinecntAndroid, int registerCnt, int waitLoginCnt) {
		if (!GsConfig.getInstance().isTlogEnable()) {
			return;
		}
		LogParam logParam = getNonPersonalLogParam(LogInfoType.onlineInfo);
		logParam.put("gameappid", SDKManager.getInstance().getAppId(channel))
				.put("timekey", HawkTime.getSeconds())
				.put("gsid", GsConfig.getInstance().getServerId())
				.put("zoneareaid", GsConfig.getInstance().getServerId())
				.put("onlinecntios", onlinecntIOS)
				.put("onlinecntandroid", onlinecntAndroid)
				.put("registernum", registerCnt)
				.put("queuesize", waitLoginCnt);
		GameLog.getInstance().info(logParam);
	}

	/** 记录Idip敏感日志
	 * 
	 * @param player
	 *            player为null时记录全局敏感日志，否则记录个人相关敏感日志
	 * @param request
	 * @param goodsId
	 *            道具Id
	 * @param value
	 *            道具的数量 */
	public static void logIdipSensitivity(Player player, JSONObject request, int goodsId, int value) {
		if (player == null) {
			logIdipSensitivity(request, goodsId, value);
			return;
		}

		try {
			LogParam logParam = getPersonalLogParam(player, LogConst.LogInfoType.idip_flow);
			if (logParam != null) {
				logParam.put("goodsId", goodsId).put("count", value)
				.put("serial", request.getJSONObject("body").getString("Serial"))
				.put("source", request.getJSONObject("body").getIntValue("Source"))
				.put("cmd", request.getJSONObject("head").getIntValue("Cmdid"));
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/** 记录Idip敏感日志(全局)
	 * 
	 * @param request
	 * @param goodsId
	 * @param value */
	private static void logIdipSensitivity(JSONObject request, int goodsId, int value) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.idip_flow);
			logParam.put("puid", "")
					.put("iZoneAreaId", GsConfig.getInstance().getServerId())
					.put("goodsId", goodsId)
					.put("count", value)
					.put("serial", request.getJSONObject("body").getString("Serial"))
					.put("source", request.getJSONObject("body").getIntValue("Source"))
					.put("cmd", request.getJSONObject("head").getIntValue("Cmdid"));
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/** 记录举报安全日志
	 * 
	 * @param player
	 *            举报人
	 * @param targetPlayer
	 *            被举报人
	 * @param reportScene
	 *            举报场景
	 * @param reportType
	 *            举报类型
	 * @param reportDesc
	 *            举报说明
	 * @param reportContent
	 *            举报内容
	 * @param reportPicUrls
	 *            举报图片url */
	public static void logReporting(Player player, Player targetPlayer, int reportScene, int reportType, String reportDesc, String reportContent, List<String> reportPicUrls) {
		LogParam logParam = getPersonalLogParam(player, LogInfoType.sec_reporting);
		if (logParam == null) {
			return;
		}

		String pfInconPrimitive = targetPlayer.getData().getPrimitivePfIcon();
		if (!targetPlayer.isActiveOnline()) {
			pfInconPrimitive = GameUtil.getPrimitivePfIcon(targetPlayer.getChannel(), targetPlayer.getPuid());
		}

		logParam.put("reportId", HawkOSOperator.randomUUID())
				.put("roleType", 0)
				.put("areaId", Channel.valueOf(player.getChannel().toUpperCase()).intVal())
				.put("playerPower", player.getPower())
				.put("playerName", player.getName())
				.put("gversion", player.getAppVersion())
				.put("targetOpenid", targetPlayer.getOpenId())
				.put("targetPlayerName", targetPlayer.getName())
				.put("targetPlayerId", targetPlayer.getId())
				.put("targetUserIp", targetPlayer.getClientIp())
				.put("targetGuildId", targetPlayer.hasGuild() ? targetPlayer.getGuildId() : "")
				.put("targetPicUrl", pfInconPrimitive == null ? "" : pfInconPrimitive)
				.put("targetPlatId", targetPlayer.getPlatId())
				.put("targetAreaId", Channel.valueOf(targetPlayer.getChannel().toUpperCase()).intVal())
				.put("targetZoneId", GsConfig.getInstance().getServerId())
				.put("reportScene", reportScene)
				.put("reportType", reportType)
				.put("reportDesc", GameUtil.stringFilter(reportDesc))
				.put("reportContent", GameUtil.stringFilter(reportContent));

		StringBuilder sb = new StringBuilder();
		for (String url : reportPicUrls) {
			sb.append(url).append(";");
		}

		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
		}

		logParam.put("reportPicUrls", sb.toString());
		GameLog.getInstance().info(logParam);
	}

	/** 记录操作流水安全日志信息
	 * 
	 * @param player
	 * @param protocol
	 * @param operationCount
	 * @param operationResult 
	 * */
	public static boolean logSecOperationFlow(Player player, HawkProtocol protocol, int operationCount, boolean operationResult, long startTime) {
		try {
			if (!GsConfig.getInstance().isSecTlogPrintEnable() || GameUtil.isTlogPuidControlled(player.getOpenId())) {
				return false;
			}
			int type = protocol.getType();
			HP.code code = HP.code.valueOf(type);
			HP.code2 code2 = HP.code2.valueOf(type);
			if (code == null && code2 == null) {
				return false;
			}
			if (type == HP.code.LOGIN_C_VALUE || type == HP.code.LOGIN_WAIT_C_VALUE || type == HP.code.CFG_CHECK_C_VALUE) {
				return false;
			}
			LogParam logParam = getPersonalLogParam(player, LogInfoType.sec_operation_flow);
			if (logParam != null) {
				long costtime = startTime <= 0 ? 0 : HawkTime.getMillisecond() - startTime;
				String desc = code != null ? code.name() : code2.name();
				logParam.put("time", HawkApp.getInstance().getCurrentTime() / 1000)
						.put("gversion", player.getAppVersion())
						.put("areaId", Channel.valueOf(player.getChannel().toUpperCase()).intVal())
						.put("operCount", operationCount + 1)
						.put("clientTime", HawkTime.formatTime(HawkApp.getInstance().getCurrentTime() + player.getData().getClientServerTimeSub() * 1000))
						.put("userName", player.getName())
						.put("userMoney", player.getGold())
						.put("useDiamonds", player.getDiamonds())
						.put("operId", type)
						.put("operDesc", desc.toLowerCase())
						.put("operType", 0) // 操作来源，0为玩家操作
						.put("operResult", operationResult ? 0 : 1)
						.put("operCosttime", costtime);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		
		return true;
	}

	/** 记录聊天相关安全日志信息
	 * 
	 * @param player
	 * @param recPlayer
	 * @param chatType
	 * @param title
	 * @param content */
	public static void logSecTalkFlow(Player player, String recPlayerId, int chatType, String title, String content) {
		if (!GsConfig.getInstance().isSecTlogPrintEnable()) {
			return;
		}

		try {
			LogParam logParam = getPersonalLogParam(player, LogInfoType.sec_talk_flow);
			if (logParam == null) {
				return;
			}

			String guildId = player.getGuildId();
			String pfIconPrimitive = player.getData().getPrimitivePfIcon();
			if (GlobalData.getInstance().isBanPortraitAccount(player.getOpenId())) {
				pfIconPrimitive = PlatformConstCfg.getInstance().getImage_def();
			}
			
			logParam.put("areaId", Channel.valueOf(player.getChannel().toUpperCase()).intVal())
					.put("senderName", player.getName())
					.put("senderId", player.getId())
					.put("senderLevel", player.getLevel())
					.put("senderBattlePoint", player.getEntity().getBattlePoint())
					.put("senderIp", player.getClientIp())
					.put("senderGuildId", HawkOSOperator.isEmptyString(guildId) ? 0 : guildId)
					.put("chatType", chatType)
					.put("title", GameUtil.stringFilter(title))
					.put("content", GameUtil.stringFilter(content)) // 对文本中的英文分隔符过滤
					.put("picUrl", pfIconPrimitive == null ? 0 : pfIconPrimitive)
					.put("saveAmt", player.getPlayerBaseEntity().getSaveAmtTotal());

			Player recPlayer = GlobalData.getInstance().makesurePlayer(recPlayerId);
			if (recPlayer != null) {
				logParam.put("recPuid", recPlayer.getOpenId())
						.put("recPlayerId", recPlayer.getId())
						.put("recPlayerLevel", recPlayer.getLevel());
			} else {
				logParam.put("recPuid", 0)
						.put("recPlayerId", 0)
						.put("recPlayerLevel", 0);
			}

			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/** 给主播刷礼物
	 * 
	 * @param player
	 * @param anchorId
	 *            主播id
	 * @param giftId
	 *            礼物id */
	public static void logSendGiftForAnchorFlow(Player player, String anchorId, int giftId) {
		LogParam logParam = getPersonalLogParam(player, LogInfoType.anchor_gift);
		if (logParam != null) {
			logParam.put("anchorId", anchorId)
					.put("giftId", giftId);
			GameLog.getInstance().info(logParam);
		}
	}

	/** 记录玩家战斗安全日志
	 * 
	 * @param attPlayer
	 *            进攻者
	 * @param defPlayer
	 *            防守者
	 * @param battleId
	 *            战斗ID
	 * @param defIdentity
	 *            防守者身份
	 * @param isWin
	 *            攻击是否胜利
	 * @param awardItems
	 *            进攻者掠夺的资源
	 * @param consumeItems
	 *            防守者损失的资源
	 * @param atkArmyLeft
	 *            进攻者部队信息
	 * @param defArmyLeft
	 *            防守者部队信息
	 * @param battleCastTime
	 *            战斗耗时
	 * @param march
	 *            战斗行军 */
	public static void logSecBattleFlow(Player attPlayer, Player defPlayer, String battleId, int defIdentity, boolean isWin, AwardItems awardItems,
			ConsumeItems consumeItems, List<ArmyInfo> atkArmyLeft, List<ArmyInfo> defArmyLeft, int battleCastTime, WorldMarch march, boolean isMassMarch) {
		if (!GsConfig.getInstance().isSecTlogPrintEnable()) {
			return;
		}

		try {
			LogParam logParam = getPersonalLogParam(attPlayer, LogInfoType.sec_battle_flow);
			if (logParam == null) {
				return;
			}

			int marchDistance = 0;
			if (march != null) {
				AlgorithmPoint origion = new AlgorithmPoint(march.getOrigionX(), march.getOrigionY());
				AlgorithmPoint terminal = new AlgorithmPoint(march.getTerminalX(), march.getTerminalY());
				marchDistance = (int) origion.distanceTo(terminal);
			}
			logParam.put("time", HawkApp.getInstance().getCurrentTime() / 1000)
					.put("gversion", attPlayer.getAppVersion())
					.put("areaId", Channel.valueOf(attPlayer.getChannel().toUpperCase()).intVal())
					.put("battleId", battleId)
					.put("result", isWin ? 1 : 0)
					.put("castTime", battleCastTime)
					.put("distance", marchDistance);
			// 进攻方信息
			fillAttakerParams(attPlayer, logParam, awardItems, atkArmyLeft);

			// 防守方信息
			fillDefenderParams(defPlayer, logParam, defIdentity, consumeItems, defArmyLeft);

			// 进攻玩家次数
			int attackerAtkWin = attPlayer.getData().getStatisticsEntity().getAtkWinCnt() + (isWin ? 1 : 0);
			int attackerAtkLose = attPlayer.getData().getStatisticsEntity().getAtkLoseCnt() + (isWin ? 0 : 1);
			int atkTotal = attackerAtkWin + attackerAtkLose;
			// 防御次数
			int attackerDefWin = attPlayer.getData().getStatisticsEntity().getDefWinCnt();
			int attackerDefLose = attPlayer.getData().getStatisticsEntity().getDefLoseCnt();
			int defTotal = attackerDefWin + attackerDefLose;

			// 历史信息
			logParam.put("attPlayerTotalAttackTimes", atkTotal) // 历史总进攻次数
					.put("attPlayerTotalWinTimes", (int) (attackerAtkWin * 1.0D / atkTotal * 1000)) // 历史进攻总胜率
					.put("attPlayerTotalDefTimes", defTotal) // 历史被攻打总次数
					.put("attPlayerTotalDefWinTimes", (int) (attackerDefWin * 1.0D / defTotal * 1000)); // 历史防御成功率

			if (defPlayer != null) {
				int defenderAtkWin = defPlayer.getData().getStatisticsEntity().getAtkWinCnt();
				int defenderAtkLose = defPlayer.getData().getStatisticsEntity().getAtkLoseCnt();
				atkTotal = defenderAtkWin + defenderAtkLose;
				int defenderDefWin = defPlayer.getData().getStatisticsEntity().getDefWinCnt() + (isWin ? 0 : 1);
				int defenderDefLose = defPlayer.getData().getStatisticsEntity().getDefLoseCnt() + (isWin ? 1 : 0);
				defTotal = defenderDefWin + defenderDefLose;
				// 历史信息
				logParam.put("defPlayerTotalAttackTimes", atkTotal) // 历史总进攻次数
						.put("defPlayerTotalWinTimes", (int) (defenderAtkWin * 1.0D / atkTotal * 1000)) // 历史进攻总胜率
						.put("defPlayerTotalDefTimes", defTotal) // 历史被攻打总次数
						.put("defPlayerTotalDefWinTimes", (int) (defenderDefWin * 1.0D / defTotal * 1000)) // 历史防御成功率
						.put("defGuildId", defPlayer.hasGuild() ? defPlayer.getGuildId() : "");
			} else {
				logParam.put("defPlayerTotalAttackTimes", -1)
						.put("defPlayerTotalWinTimes", -1)
						.put("defPlayerTotalDefTimes", -1)
						.put("defPlayerTotalDefWinTimes", -1);
			}

			logParam.put("isMassMarch", isMassMarch ? 1 : 0).put("atkGuildId", attPlayer.hasGuild() ? attPlayer.getGuildId() : "");
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/** 填充进攻方参数
	 * 
	 * @param attPlayer
	 * @param logParam
	 * @param awardItems
	 * @param atkArmyLeft */
	private static void fillAttakerParams(Player attPlayer, LogParam logParam, AwardItems awardItems, List<ArmyInfo> atkArmyLeft) {

		logParam.put("attPlayerBattlePoint", attPlayer.getData().getPlayerEntity().getBattlePoint())
				.put("attPlayerLevel", attPlayer.getLevel())
				.put("attPlayerClientTime", HawkTime.formatTime(HawkApp.getInstance().getCurrentTime() + attPlayer.getData().getClientServerTimeSub() * 1000))
				.put("attPlayerName", attPlayer.getName())
				.put("attPlayerDeviceId", attPlayer.getDeviceId())
				.put("attPlayerIp", attPlayer.getClientIp())
				.put("attPlayerDiamonds", attPlayer.getDiamonds())
				.put("attPlayerGold", attPlayer.getGold())
				.put("attPlayerGoldore", attPlayer.getGoldore() + attPlayer.getGoldoreUnsafe())
				.put("attPlayerOil", attPlayer.getOil() + attPlayer.getOilUnsafe())
				.put("attPlayerTombar", attPlayer.getTombarthite() + attPlayer.getTombarthiteUnsafe())
				.put("attPlayerAttr", attPlayer.getSteel() + attPlayer.getSteelUnsafe());

		if (atkArmyLeft != null) {
			StringBuilder atkArmyIds = new StringBuilder();
			StringBuilder atkArmyCounts = new StringBuilder();
			StringBuilder atkRemainArmyIds = new StringBuilder();
			StringBuilder atkRemainArmyCounts = new StringBuilder();
			for (ArmyInfo armyInfo : atkArmyLeft) {
				atkArmyIds.append(armyInfo.getArmyId()).append(",");
				atkArmyCounts.append(armyInfo.getTotalCount()).append(",");
				if (armyInfo.getFreeCnt() > 0) {
					atkRemainArmyIds.append(armyInfo.getArmyId()).append(",");
					atkRemainArmyCounts.append(armyInfo.getFreeCnt()).append(",");
				}
			}

			if (atkArmyIds.indexOf(",") >= 0) {
				atkArmyIds.deleteCharAt(atkArmyIds.length() - 1);
				atkArmyCounts.deleteCharAt(atkArmyCounts.length() - 1);
			}

			if (atkRemainArmyIds.indexOf(",") >= 0) {
				atkRemainArmyIds.deleteCharAt(atkRemainArmyIds.length() - 1);
				atkRemainArmyCounts.deleteCharAt(atkRemainArmyCounts.length() - 1);
			}

			String armyIds = atkArmyIds.toString();
			String armyCounts = atkArmyCounts.toString();
			String remainIds = atkRemainArmyIds.toString();
			String remainCounts = atkRemainArmyCounts.toString();
			logParam.put("attPlayerArmyIds", HawkOSOperator.isEmptyString(armyIds) ? "0" : armyIds)
					.put("attPlayerArmyCount", HawkOSOperator.isEmptyString(armyCounts) ? "0" : armyCounts)
					.put("attPlayerRemainArmyIds", HawkOSOperator.isEmptyString(remainIds) ? "0" : remainIds)
					.put("attPlayerRemainArmyCount", HawkOSOperator.isEmptyString(remainCounts) ? "0" : remainCounts);
		} else {
			logParam.put("attPlayerArmyIds", 0)
					.put("attPlayerArmyCount", 0)
					.put("attPlayerRemainArmyIds", 0)
					.put("attPlayerRemainArmyCount", 0);
		}

		if (awardItems != null) {
			Map<Integer, Long> attrItemsCount = awardItems.getAwardItemsCount();
			HawkTuple2<String, String> itemIdCounts = awardItems.getItemsInfo();
			logParam.put("attPlayerGetDiamonds", attrItemsCount.get(PlayerAttr.DIAMOND_VALUE) == null ? 0 : attrItemsCount.get(PlayerAttr.DIAMOND_VALUE))
					.put("attPlayerGetGold", attrItemsCount.get(PlayerAttr.GOLD_VALUE) == null ? 0 : attrItemsCount.get(PlayerAttr.GOLD_VALUE))
					.put("attPlayerGetGoldore", attrItemsCount.get(PlayerAttr.GOLDORE_UNSAFE_VALUE) == null ? 0 : attrItemsCount.get(PlayerAttr.GOLDORE_UNSAFE_VALUE))
					.put("attPlayerGetOil", attrItemsCount.get(PlayerAttr.OIL_UNSAFE_VALUE) == null ? 0 : attrItemsCount.get(PlayerAttr.OIL_UNSAFE_VALUE))
					.put("attPlayerGetTombar", attrItemsCount.get(PlayerAttr.TOMBARTHITE_UNSAFE_VALUE) == null ? 0 : attrItemsCount.get(PlayerAttr.TOMBARTHITE_UNSAFE_VALUE))
					.put("attPlayerGetAttr", attrItemsCount.get(PlayerAttr.STEEL_UNSAFE_VALUE) == null ? 0 : attrItemsCount.get(PlayerAttr.STEEL_UNSAFE_VALUE))
					.put("attPlayerGetItemIds", HawkOSOperator.isEmptyString(itemIdCounts.first) ? "0" : itemIdCounts.first)
					.put("attPlayerGetItemCounts", HawkOSOperator.isEmptyString(itemIdCounts.second) ? "0" : itemIdCounts.second);
		} else {
			logParam.put("attPlayerGetDiamonds", -1)
					.put("attPlayerGetGold", -1)
					.put("attPlayerGetGoldore", -1)
					.put("attPlayerGetOil", -1)
					.put("attPlayerGetTombar", -1)
					.put("attPlayerGetAttr", -1)
					.put("attPlayerGetItemIds", 0)
					.put("attPlayerGetItemCounts", 0);
		}
	}

	/** 填充防守方参数
	 * 
	 * @param defPlayer
	 * @param logParam
	 * @param defIdentity
	 * @param consumeItems
	 * @param defArmyLeft */
	private static void fillDefenderParams(Player defPlayer, LogParam logParam, int defIdentity,
			ConsumeItems consumeItems, List<ArmyInfo> defArmyLeft) {

		logParam.put("defPlayerIdentity", defIdentity);
		if (defPlayer != null) {
			logParam.put("defPlayerPuid", defPlayer.getOpenId())
					.put("defPlayerPlatId", GameUtil.changePlatform2Int(defPlayer.getPlatform()))
					.put("defPlayerAreaId", Channel.valueOf(defPlayer.getChannel().toUpperCase()).intVal())
					.put("defPlayerZoneId", defPlayer.getEntity().getServerId())
					.put("defPlayerBattlePoint", defPlayer.getData().getPlayerEntity().getBattlePoint())
					.put("defPlayerLevel", defPlayer.getLevel())
					.put("defPlayerClientTime", "")
					.put("defPlayerClientVersion", defPlayer.getAppVersion())
					.put("defPlayerName", defPlayer.getName())
					.put("defPlayerDeviceId", defPlayer.getDeviceId())
					.put("defPlayerIp", defPlayer.getClientIp())
					.put("defPlayerDiamonds", defPlayer.getDiamonds())
					.put("defPlayerGold", defPlayer.getGold())
					.put("defPlayerGoldore", defPlayer.getGoldore() + defPlayer.getGoldoreUnsafe())
					.put("defPlayerOil", defPlayer.getOil() + defPlayer.getOilUnsafe())
					.put("defPlayerTombar", defPlayer.getTombarthite() + defPlayer.getTombarthiteUnsafe())
					.put("defPlayerAttr", defPlayer.getSteel() + defPlayer.getSteelUnsafe());

			StringBuilder defArmyIds = new StringBuilder();
			StringBuilder defArmyCounts = new StringBuilder();
			StringBuilder defRemainArmyIds = new StringBuilder();
			StringBuilder defRemainArmyCounts = new StringBuilder();
			for (ArmyInfo armyInfo : defArmyLeft) {
				defArmyIds.append(armyInfo.getArmyId()).append(",");
				defArmyCounts.append(armyInfo.getTotalCount()).append(",");
				if (armyInfo.getFreeCnt() > 0) {
					defRemainArmyIds.append(armyInfo.getArmyId()).append(",");
					defRemainArmyCounts.append(armyInfo.getFreeCnt()).append(",");
				}
			}

			if (defArmyIds.indexOf(",") >= 0) {
				defArmyIds.deleteCharAt(defArmyIds.length() - 1);
				defArmyCounts.deleteCharAt(defArmyCounts.length() - 1);
			}

			if (defRemainArmyIds.indexOf(",") >= 0) {
				defRemainArmyIds.deleteCharAt(defRemainArmyIds.length() - 1);
				defRemainArmyCounts.deleteCharAt(defRemainArmyCounts.length() - 1);
			}

			String defArmyIdStr = defArmyIds.toString();
			String defArmyCountStr = defArmyCounts.toString();
			String remainIds = defRemainArmyIds.toString();
			String remainCounts = defRemainArmyCounts.toString();
			logParam.put("defPlayerArmyIds", HawkOSOperator.isEmptyString(defArmyIdStr) ? "0" : defArmyIdStr)
					.put("defPlayerArmyCount", HawkOSOperator.isEmptyString(defArmyCountStr) ? "0" : defArmyCountStr)
					.put("defPlayerRemainArmyIds", HawkOSOperator.isEmptyString(remainIds) ? "0" : remainIds)
					.put("defPlayerRemainArmyCount", HawkOSOperator.isEmptyString(remainCounts) ? "0" : remainCounts);
		} else {
			logParam.put("defPlayerPuid", 0)
					.put("defPlayerPlatId", -1)
					.put("defPlayerAreaId", -1)
					.put("defPlayerZoneId", -1)
					.put("defPlayerBattlePoint", -1)
					.put("defPlayerLevel", -1)
					.put("defPlayerClientTime", HawkTime.formatTime(0))
					.put("defPlayerClientVersion", 0)
					.put("defPlayerName", 0)
					.put("defPlayerDeviceId", 0)
					.put("defPlayerIp", 0)
					.put("defPlayerDiamonds", -1)
					.put("defPlayerGold", -1)
					.put("defPlayerGoldore", -1)
					.put("defPlayerOil", -1)
					.put("defPlayerTombar", -1)
					.put("defPlayerAttr", -1)
					.put("defPlayerArmyIds", 0)
					.put("defPlayerArmyCount", 0)
					.put("defPlayerRemainArmyIds", 0)
					.put("defPlayerRemainArmyCount", 0);
		}

		if (consumeItems != null) {
			consumeItems.checkConsume(defPlayer);
			SyncAttrInfo syncAttrInfo = consumeItems.getBuilder().getAttrInfo();
			HawkTuple2<String, String> loseItemIdCounts = consumeItems.getItemsInfo();
			logParam.put("defPlayerLoseDiamonds", syncAttrInfo.getDiamond())
					.put("defPlayerLoseGold", syncAttrInfo.getGold())
					.put("defPlayerLoseGoldore", syncAttrInfo.getGoldoreUnsafe())
					.put("defPlayerLoseOil", syncAttrInfo.getOilUnsafe())
					.put("defPlayerLoseTombar", syncAttrInfo.getTombarthiteUnsafe())
					.put("defPlayerLoseAttr", syncAttrInfo.getSteelUnsafe())
					.put("defPlayerLoseItemIds", HawkOSOperator.isEmptyString(loseItemIdCounts.first) ? "0" : loseItemIdCounts.first)
					.put("defPlayerLoseItemCounts", HawkOSOperator.isEmptyString(loseItemIdCounts.second) ? "0" : loseItemIdCounts.second);
		} else {
			logParam.put("defPlayerLoseDiamonds", -1)
					.put("defPlayerLoseGold", -1)
					.put("defPlayerLoseGoldore", -1)
					.put("defPlayerLoseOil", -1)
					.put("defPlayerLoseTombar", -1)
					.put("defPlayerLoseAttr", -1)
					.put("defPlayerLoseItemIds", 0)
					.put("defPlayerLoseItemCounts", 0);
		}
	}

	/** 潘多拉抽奖打点， num为0的时候代表免费抽奖.
	 * 
	 * @param player
	 * @param num */
	public static void logPandoraLotter(Player player, int num) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.pandora_lottery);
		if (logParam != null) {
			logParam.put("num", num);
			GameLog.getInstance().info(logParam);
		}
	}

	/** 记录潘多拉兑换
	 * 
	 * @param player
	 * @param cfgId
	 *            配置ID
	 * @param num */
	public static void logPandoraExchange(Player player, int cfgId, int num) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.pandora_exchange);
		if (logParam != null) {
			logParam.put("cfgId", cfgId);
			logParam.put("num", num);
			GameLog.getInstance().info(logParam);
		}
	}

	/*** 幸运星抽奖记录
	 * 
	 * @param player
	 * @param lotCnt
	 * @param itemList */
	public static void logLuckyStarLotter(Player player, int lotCnt) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.luckystar_lotter);
		if (logParam != null) {
			logParam.put("lotCnt", lotCnt);
			GameLog.getInstance().info(logParam);
		}
	}

	/** 伊娃热线相关事件打点记录
	 * 
	 * @param player
	 * @param eventType
	 * @param args */
	public static void logEvaHotlineEvent(Player player, int eventType, String args) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.eva_hotline);
		if (logParam != null) {
			logParam.put("eventType", eventType);
			logParam.put("eventObjId", HawkOSOperator.isEmptyString(args) ? 0 : Integer.parseInt(args));
			GameLog.getInstance().info(logParam);
		}
	}

	/** 英雄试炼 进入事件打点记录
	 * 
	 * @param player
	 * @param eventType
	 * @param args
	 *            进入类型 1超时空进入 2机甲进入 3道具获得进入 */
	public static void logWarriorKingEnterEvent(Player player, int eventType, String args) {
		if (args == null) {
			return;
		}
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.warrior_king);
		if (logParam != null) {
			logParam.put("eventType", eventType);
			logParam.put("enterType", HawkOSOperator.isEmptyString(args) ? 0 : Integer.parseInt(args));
			logParam.put("passTime", 0);
			logParam.put("passRet", 0);
			GameLog.getInstance().info(logParam);
		}
	}

	/** 英雄试炼结束事件打点记录
	 * 
	 * @param player
	 * @param eventType
	 * @param passTime
	 *            通关时间
	 * @param passRet
	 *            通关结果 1 成功 0 失败 */
	public static void logWarriorKingEndEvent(Player player, int eventType, String passTime, String passRet) {
		if (passTime == null || passRet == null) {
			return;
		}
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.warrior_king);
		if (logParam != null) {
			logParam.put("eventType", eventType);
			logParam.put("enterType", 0);
			logParam.put("passTime", HawkOSOperator.isEmptyString(passTime) ? 0 : Integer.parseInt(passTime));
			logParam.put("passRet", HawkOSOperator.isEmptyString(passRet) ? 0 : Integer.parseInt(passRet));
			GameLog.getInstance().info(logParam);
		}
	}

	/** 官职任命数据打点记录
	 * 
	 * @param appointPlayer
	 * @param beAppointPlayer
	 * @param officerId */
	public static void logAppointOfficer(Player appointPlayer, Player beAppointPlayer, int officerId) {
		LogParam logParam = LogUtil.getPersonalLogParam(beAppointPlayer, LogInfoType.office_appoint);
		if (logParam != null) {
			logParam.put("power", beAppointPlayer.getPower())
					.put("officerId", officerId)
					.put("appointerOpenid", appointPlayer.getOpenId())
					.put("appointerRoleID", appointPlayer.getId());
			GameLog.getInstance().info(logParam);
		}
	}

	/** 总统争夺战数据打点记录
	 * 
	 * @param player
	 * @param params */
	public static void logPresidentWar(Player player, Map<String, Object> params) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.president_war);
		if (logParam == null) {
			return;
		}

		try {
			@SuppressWarnings("unchecked")
			List<ArmyInfo> armyInfoList = (List<ArmyInfo>) params.get("armyInfo");
			StringBuilder armyIds = new StringBuilder();
			StringBuilder armyCounts = new StringBuilder();
			StringBuilder loseArmyIds = new StringBuilder();
			StringBuilder loseArmyCounts = new StringBuilder();
			for (ArmyInfo armyInfo : armyInfoList) {
				armyIds.append(armyInfo.getArmyId()).append(",");
				armyCounts.append(armyInfo.getTotalCount()).append(",");

				loseArmyIds.append(armyInfo.getArmyId()).append(",");
				loseArmyCounts.append(armyInfo.getTotalCount() - armyInfo.getFreeCnt()).append(",");
			}

			if (armyIds.indexOf(",") >= 0) {
				armyIds.deleteCharAt(armyIds.length() - 1);
				armyCounts.deleteCharAt(armyCounts.length() - 1);
			}

			if (loseArmyIds.indexOf(",") >= 0) {
				loseArmyIds.deleteCharAt(loseArmyIds.length() - 1);
				loseArmyCounts.deleteCharAt(loseArmyCounts.length() - 1);
			}

			String armyIdStr = armyIds.toString();
			String armyCountStr = armyCounts.toString();
			String loseArmyIdStr = loseArmyIds.toString();
			String loseArmyCountStr = loseArmyCounts.toString();

			logParam.put("turnCount", params.get("turnCount"))
					.put("battleId", params.get("battleId"))
					.put("allianceId", params.get("allianceId"))
					.put("identity", params.get("identity"))
					.put("warType", params.get("warType"))
					.put("pointType", params.get("pointType"))
					.put("result", params.get("result"))
					.put("power", player.getPower())
					.put("armyIDList", armyIdStr)
					.put("armyCount", armyCountStr)
					.put("loseArmyIDList", loseArmyIdStr)
					.put("loseArmyCount", loseArmyCountStr);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/** 机甲剧情打点记录
	 * 
	 * @param player
	 * @param type */
	public static void logMechaScenario(Player player, int type) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.mecha_scenario);
		if (logParam != null) {
			logParam.put("power", player.getPower())
					.put("type", type);
			GameLog.getInstance().info(logParam);
		}
	}

	/** 记录机甲变化日志 (注：参数列表中参数的类型不一定就是int，可根据实际情况修改类型，如果两个不同机甲拥有相同的mechaId)
	 * 
	 * @param player
	 *            机甲所属玩家
	 * @param action
	 *            变化来源
	 * @param mecha
	 *            机甲 */
	public static void logMechaAttrChange(Player player, Action action, SuperSoldier mecha) {
		try {
			if (mecha == null) {
				return;
			}

			SuperSoldierCfg mechaCfg = HawkConfigManager.getInstance().getConfigByKey(SuperSoldierCfg.class, mecha.getCfgId());
			if (mechaCfg == null) {
				return;
			}

			ImmutableList<SuperSoldierSkillSlot> skillSlots = mecha.getSkillSlots();
			int slot1Lv = 0;
			int slot2Lv = 0;
			int slot3Lv = 0;
			int slot4Lv = 0;
			for (SuperSoldierSkillSlot slot : skillSlots) {
				if (slot.getSkill() != null) {
					if (slot.getIndex() == 1) {
						slot1Lv = slot.getSkill().getLevel();
					} else if (slot.getIndex() == 2) {
						slot2Lv = slot.getSkill().getLevel();
					} else if (slot.getIndex() == 3) {
						slot3Lv = slot.getSkill().getLevel();
					} else if (slot.getIndex() == 4) {
						slot4Lv = slot.getSkill().getLevel();
					}
				}
			}

			LogParam logParam = getPersonalLogParam(player, LogInfoType.mecha_change);
			if (logParam != null) {
				int mechaType = mechaCfg.getSupersoldierClass();
				logParam.put("mechaId", mecha.getCfgId()).put("mechaStar", mecha.getStar())
						.put("mechaLevel", mecha.getLevel()).put("mechaPower", mecha.power())
						.put("action", action.intItemVal()).put("mechaType", mechaType)
						.put("slot1lv", slot1Lv).put("slot2lv", slot2Lv).put("slot3lv", slot3Lv)
						.put("slot4lv", slot4Lv).put("skin", mecha.getSkin());

				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/** 记录机甲参战(PVP) (注：参数列表中参数的类型不一定就是int，可根据实际情况修改类型，如果两个不同机甲拥有相同的mechaId)
	 * 
	 * @param player
	 *            机甲所属玩家
	 * @param mecha
	 *            机甲 */
	public static void logMechaMarch(Player player, int mechaId, int battleType) {
		try {
			SuperSoldierCfg mechaCfg = HawkConfigManager.getInstance().getConfigByKey(SuperSoldierCfg.class,
					mechaId);
			if (mechaCfg != null) {
				LogParam logParam = getPersonalLogParam(player, LogInfoType.mecha_march);
				if (logParam != null) {
					logParam.put("mechaId", mechaId).put("battleType", battleType);
					GameLog.getInstance().info(logParam);
				}
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	public static void logDailyMission(Player player) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.daily_mission);
		if (logParam != null) {
			GameLog.getInstance().info(logParam);
		}
	}

	/** 创退组队房间
	 * 
	 * @param teamId
	 * @param leaderId
	 * @param guildId
	 * @param instanceId
	 *            war_college_instance 配置id
	 * @param op */
	public static void logWarCollegeTeam(int teamId, String leaderId, String guildId, int instanceId, String op) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.war_college_team);
			logParam.put("teamId", teamId)
					.put("leaderId", leaderId)
					.put("guildId", guildId)
					.put("instanceId", instanceId)
					.put("op", op);
			GameLog.getInstance().info(logParam);
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 
	 * @param battleId
	 * @param op  0  开始  1 结束
	 * @param guildId
	 * @param cfgId
	 * @param playerCount
	 */
	public static void logLMJYGameState(String battleId, int op, String guildId, int cfgId, int playerCount, boolean win) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.war_college_high);
		    logParam.put("battleId", battleId)
					.put("guildId", guildId)
					.put("cfgId", cfgId)
					.put("playerCount", playerCount)
					.put("op", op)
					.put("win", win ? 1 : 0);
			GameLog.getInstance().info(logParam);
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 
	 * @param player
	 * @param op 0合成 , 1 分解
	 * @param xiaoHao 消耗
	 * @param houDe 获得
	 * @param totalCnt 折合1级总数目
	 */
	public static void logSuperLabItemOp(Player player,int op, String xiaoHao,String houDe, int totalCnt){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.super_lab_item_op);
			if (logParam != null) {
				logParam.put("opType", op)
				        .put("consume", xiaoHao)
				        .put("award", houDe)
				        .put("totalCnt", totalCnt);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 激活lab
	 * @param plaeyr
	 * @param op 0 激活 1 取消
	 * @param cfgId
	 */
	public static void logSuperLabOp(Player player,int op, int cfgId){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.super_lab_oper);
			if (logParam != null) {
				logParam.put("opType", op)
				        .put("cfgId", cfgId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 
	 * @param player 跨服的玩家 
	 * @param toServerId 跨到哪个区服
	 * @param state   该次操作的状态.
	 * @param crossType 跨服的类型 有跨服战,泰伯利亚,星球大战.
	 */
	public static void logPlayerCross(Player player, String toServerId, CrossStateType type, CrossType crossType) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.cross_server);
			if (logParam != null) {
				logParam.put("targetServer", toServerId)
				        .put("state", type.intVal())
				        .put("crossType", crossType.getNumber());
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 跨服个人积分流水
	 * 
	 * @param player
	 * @param serverId
	 *            所属区服id
	 * @param type
	 *            积分获取类型
	 * @param addScore
	 *            积分增加值
	 */

	public static void logCrossPlayerScore(Player player, String serverId, CrossTargetType type, long addScore) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.cross_player_score);
			if (logParam != null) {
				logParam.put("crossTargetType", type.getType())
				        .put("addScore", addScore);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 跨服联盟积分流水
	 * 
	 * @param guildId
	 * @param serverId
	 * @param type
	 * @param addScore
	 */
	public static void logCrossGuildScore(String guildId, String serverId, CrossTargetType type, long addScore) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.cross_guild_score);
		    logParam.put("guildId", guildId)
					.put("originServer", serverId)
					.put("crossTargetType", type.getType())
					.put("addScore", addScore);
			GameLog.getInstance().info(logParam);
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 跨服服务器积分流水
	 * 
	 * @param serverId
	 * @param type
	 * @param addScore
	 */
	public static void logCrossServerScore(String serverId, CrossTargetType type, long addScore) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.cross_server_score);
			logParam.put("originServer", serverId)
					.put("crossTargetType", type.getType())
					.put("addScore", addScore);
			GameLog.getInstance().info(logParam);
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/***
	 * 老玩家回归 回归大礼
	 * @param player
	 */
	public static void logRecieveComeBackGreatReward(Player player){
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.come_back_reward);
		GameLog.getInstance().info(logParam);
	}
	
	/***
	 * 老玩家回归 成就任务
	 * @param player
	 * @param achieveId
	 */
	public static void logComeBackAchieve(Player player, int achieveId){
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.come_back_achieve);
		if (logParam != null) {
			logParam.put("achieveId", achieveId); 
			GameLog.getInstance().info(logParam);
		}
	}
	
	/***
	 * 老玩家回归，兑换
	 * @param player
	 * @param cfgId
	 * @param num
	 */
	public static void logComeBackExchange(Player player, int cfgId, int num){
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.come_back_exchange);
		if (logParam != null) {
			logParam.put("cfgId", cfgId);
			logParam.put("num", num);
			GameLog.getInstance().info(logParam);
		}
	}
	
	/***
	 * 老玩家回归，低折回馈
	 * @param player
	 * @param cfgId
	 * @param num
	 */
	public static void logComeBackBuy(Player player, int cfgId, int num){
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.come_back_buy);
		if (logParam != null) {
			logParam.put("cfgId", cfgId);
			logParam.put("num", num);
			GameLog.getInstance().info(logParam);
		}
	}
	
	/**
	 * 新年寻宝，使用藏宝图
	 * @param player
	 */
	public static void logTreasureHuntToolUse(Player player) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.treasure_hunt_tool_use);
			if (logParam != null) {
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 新年寻宝，触发奖励
	 */
	public static void logTreasureHuntTouceReward() {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.treasure_hunt_touch_reward);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 新年寻宝，触发生成野怪
	 * @param addCount
	 * @param currentCount
	 */
	public static void logTreasureHuntTouceMonster(int addCount, int currentCount) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.treasure_hunt_touch_monster);
		    logParam.put("addCount", addCount);
			logParam.put("currentCount", currentCount);
			GameLog.getInstance().info(logParam);
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 新年寻宝，触发生成资源
	 * @param addCount
	 * @param currentCount
	 */
	public static void logTreasureHuntTouceResource(int addCount, int currentCount) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.treasure_hunt_touch_resource);
		    logParam.put("addCount", addCount);
			logParam.put("currentCount", currentCount);
			GameLog.getInstance().info(logParam);
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 新年寻宝，攻击野怪
	 * @param player
	 * @param guildId
	 * @param isKill
	 */
	public static void logTreasureHuntAtkMonster(Player player, String guildId, boolean isKill) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.treasure_hunt_attack_monster);
			if (logParam != null) {
				logParam.put("isKill", isKill ? 1 : 0);
				logParam.put("guildId", guildId == null ? "" : guildId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 新年寻宝，采集资源
	 * @param player
	 */
	public static void logTreasureHuntCollectResource(Player player) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.treasure_hunt_collect_resource);
			if (logParam != null) {
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 新年寻宝，采集点战斗
	 * @param player
	 * @param atkerBattlePoint
	 * @param defPlayerId
	 * @param deferBattlePoint
	 */
	public static void logTreasureHuntResourceBattle(Player player, long atkerBattlePoint, String defPlayerId, long deferBattlePoint) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.treasure_hunt_resource_battle);
			if (logParam != null) {
				logParam.put("atkerBattlePoint", atkerBattlePoint);
				logParam.put("defPlayerId", defPlayerId);
				logParam.put("deferBattlePoint", deferBattlePoint);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录联盟插旗
	 * 
	 * @param entity
	 */
	public static void logWarFlag(IFlag entity, String beforeGuildId, WarFlagOwnChangeType ownType) {
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(entity.getPointId());
		if (point == null) {
			return;
		}
		logWarFlag(entity, beforeGuildId, ownType, point.getZoneId());
	}
	
	/**
	 * 记录联盟插旗
	 * 
	 * @param entity
	 * @param beforeGuildId
	 * @param ownType
	 * @param zoneId
	 */
	public static void logWarFlag(IFlag entity, String beforeGuildId, WarFlagOwnChangeType ownType, int zoneId) {
		try {
			long power = GuildService.getInstance().getGuildBattlePoint(entity.getCurrentId());
			LogParam logParam = getNonPersonalLogParam(LogInfoType.war_flag);
		    logParam.put("placeGuildId", entity.getOwnerId())
			        .put("beforeGuildId", beforeGuildId)
			        .put("ownerGuildId", entity.getCurrentId())
			        .put("ownerGuildPower", power)
			        .put("zoneId", zoneId)
			        .put("ownType", ownType.intVal());
			GameLog.getInstance().info(logParam);
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 技能释放log
	 * 
	 * @param player
	 * @param skillId
	 * @param param1
	 */
	public static void logSkillCasting(Player player, int skillId, int param1, int param2, int param3, int param4, int param5) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.skill_cast);
			if (logParam != null) {
				logParam.put("skillId", skillId)
						.put("param1", param1)
						.put("param2", param2)
						.put("param3", param3)
						.put("param4", param4)
						.put("param5", param5);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录自动打野功能开启关闭
	 */
	public static void logAutoMonsterSwitch(Player player, boolean isOpen) {
		logAutoMonsterSwitch(player, isOpen, 0, 0, 0, "");
	}
	
	/**
	 * 记录自动打野功能开启关闭
	 * 
	 * @param isOpen
	 * @param minLevel
	 * @param maxLevel
	 * @param marchCount 开启行军队列数
	 */
	public static void logAutoMonsterSwitch(Player player, boolean isOpen, int minLevel, int maxLevel, int marchCount, String troopDetail) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.auto_monster);
			if (logParam != null) {
				logParam.put("switchVal", isOpen ? 1 : 0)
						.put("minLevel", minLevel)
						.put("maxLevel", maxLevel)
						.put("marchCount", marchCount)
						.put("troops", 0)
						.put("troopDetail", troopDetail);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录私人定制礼包购买
	 * 
	 * @param giftId
	 * @param rewardIds
	 */
	public static void logCustomGiftPurchase(Player player, String giftId, String rewardIds) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.custom_gift);
			if (logParam != null) {
				logParam.put("giftId", giftId)
						.put("rewardIds", rewardIds);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录创建军事学院
	 * @param player
	 * @param collegeId
	 */
	public static void logCollegeCreate(Player player, String collegeId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.college_create);
			if (logParam != null) {
				logParam.put("collegeId", collegeId)
						.put("memberCnt", 1)
						.put("operType", 0);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录解散联盟学院
	 * @param player
	 * @param collegeId
	 * @param memberCnt
	 */
	public static void logCollegeDismiss(Player player, String collegeId, int memberCnt){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.college_dismiss);
			if (logParam != null) {
				logParam.put("collegeId", collegeId)
				.put("memberCnt", memberCnt)
				.put("operType", 1);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录战争学院在线奖励领取
	 * @param player
	 * @param collegeId
	 * @param targetPlayerId
	 * @param 奖励id列表  collegeId, targetPlayerId, rewardIds
	 */
	public static void logCollegeOnlineRewarded(Player player, String collegeId, String targetPlayerId, int id) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.college_reward);
			if (logParam != null) {
				logParam.put("collegeId", collegeId)
						.put("targetPlayerId", targetPlayerId)
						.put("rewardIds", id);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录联盟语音房间进出
	 * @param player
	 * @param guildId 
	 * @param type 1: 进入 ,2: 退出
	 */
	public static void logVoiceRoom(Player player, String guildId, int type) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.voice_room);
			if (logParam != null) {
				logParam.put("guildId", guildId)
						.put("type", type);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 密友入口点击事件
	 * 
	 * @param player
	 * @param clickType 点击类型
	 */
	public static void logClickEvent(Player player, ClickEventType clickType) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.click_event);
			if (logParam != null) {
				logParam.put("clickType", clickType.intVal());
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 场景入口点击打点记录
	 * 
	 * @param player
	 * @param scenarioEntrance
	 */
	public static void logScenarioEntrance(Player player, int scenarioEntrance) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.scenario_entrance_click);
			if (logParam != null) {
				logParam.put("scenarioEntrance", scenarioEntrance);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录密友邀请任务完成情况
	 * 
	 * @param player
	 * @param taskId
	 */
	public static void logFinishedInviteTask(Player player, int taskId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.invite_task);
			if (logParam != null) {
				logParam.put("taskId", taskId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录红警战令经验购买情况
	 * @param player
	 * @param termId
	 * @param cycle
	 * @param expId
	 * @param exp
	 */
	public static void logBuyOrderExp(Player player, int termId, int cycle, int expId, int exp) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.order_exp_buy);
			if (logParam != null) {
				logParam.put("termId", termId)
				.put("cycle", cycle)
				.put("expId", expId)
				.put("exp", exp);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录红警战令进阶购买情况
	 * @param player
	 * @param termId
	 * @param cycle
	 * @param authId
	 */
	public static void logBuyOrderAuth(Player player, int termId, int cycle, int authId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.order_auth_buy);
			if (logParam != null) {
				logParam.put("termId", termId)
				.put("cycle", cycle)
				.put("authId", authId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录红警战令经验等级流水
	 * @param player
	 * @param termId
	 * @param cycle
	 * @param expAdd
	 * @param exp
	 * @param level
	 * @param reason	变更来源:0-初始化,1-完成任务,2-进阶,3-购买经验
	 * @param reasonId	来源id: 任务id/进阶礼包id/经验id
	 */
	 
	public static void logOrderExpChange(Player player, int termId, int cycle, int expAdd, int exp, int level, int reason, int reasonId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.order_exp_flow);
			if (logParam != null) {
				logParam.put("termId", termId)
				.put("cycle", cycle)
				.put("expAdd", expAdd)
				.put("exp", exp)
				.put("level", level)
				.put("reason", reason)
				.put("reasonId", reasonId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录红警战令任务完成情况
	 * @param player
	 * @param termId
	 * @param cycle
	 * @param orderId
	 * @param finishTimes
	 */
	public static void logOrderFinishId(Player player, int termId, int cycle, int orderId, int addTimes, int finishTimes){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.order_flow);
			if (logParam != null) {
				logParam.put("termId", termId)
				.put("cycle", cycle)
				.put("orderId", orderId)
				.put("addTimes", addTimes)
				.put("finishTimes", finishTimes);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 新兵救援记录
	 * 
	 * @param player
	 * @param flag
	 */
	public static void logProtectSoldierInfo(Player player, SoldierProtectEventType flag) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.proctect_soldier);
			if (logParam != null) {
				logParam.put("flag", flag.intVal());
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/** 赏金猎人打点 */
	public static void logBountyHunterHit(Player player, int termId, String boss, int bossHp,
			String costStr, String rewStr, boolean free, int rewardMutil, int lefState, String bigGift) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.bounty_hunter_hit);
		if (logParam != null) {
			logParam.put("termId", termId)
					.put("boss", boss)
					.put("bossHp", bossHp)
					.put("costStr", costStr)
					.put("rewStr", rewStr)
					.put("free", free ? 1 : 0)
					.put("rewardMutil", rewardMutil)
					.put("lefState", lefState)
					.put("bigGift", bigGift);
			GameLog.getInstance().info(logParam);
		}

	}

	/** 英雄天赋更换 */
	public static void logHeroTalentSelect(Player player,int heroId, int index, int talentId,int toTalentId,int exp, int power) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.hero_talent_select);
		if (logParam != null) {
			logParam.put("heroId", heroId)
					.put("index", index)
					.put("talentId", talentId)
					.put("toTalentId", toTalentId)
					.put("exp", exp)
					.put("power", power);
			GameLog.getInstance().info(logParam);
		}
	}
	
	public static void logHeroTalentStrengh(Player player,int heroId, int index, int talentId, String cost, int expAdd, int exp,int power) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.hero_talent_change);
		if (logParam != null) {
			logParam.put("heroId", heroId)
					.put("index", index)
					.put("talentId", talentId)
					.put("cost", cost)
					.put("expAdd", expAdd)
					.put("exp", exp)
					.put("power", power);
			GameLog.getInstance().info(logParam);
		}
	}
	
	/**
	 * 英雄天赋随机打点
	 * @param player
	 * @param heroId   英雄ID
	 * @param index    天赋栏ID
	 * @param poolId   天赋池ID
	 * @param talentId  天赋ID
	 * @param costType  消耗类型
	 * @param cost     消耗数量
	 */
	public static void logHeroTalentRandom(Player player,int heroId, int index,int poolId, int talentId, int costType, long cost) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.hero_talent_random);
		if (logParam != null) {
			logParam.put("heroId", heroId)
					.put("index", index)
					.put("poolId", poolId)
					.put("talentId", talentId)
					.put("costType", costType)
					.put("cost", cost);
			GameLog.getInstance().info(logParam);
		}
	}
	
	
	
	
	
	/**
	 * 源计划 抽奖打点 
	 * @param playerId
	 * @param termId 活动期数
	 * @param lotteryType 1 超级充能 :2磁暴充能
	 * @param score 获取积分
	 */
	public static void logPlanActivityLottery(Player player, int termId, int lotteryType, int score){
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.plan_activity_lottery);
		if (logParam != null) {
			logParam.put("termId", termId)
					.put("lotteryType", lotteryType)
					.put("score", score);
			GameLog.getInstance().info(logParam);
		}
	}
	/**
	 * 记录月签数据 
	 * @param player 
	 * @param termId 活动期数
	 * @param type 1 签到,2 补签
	 * @param dayIndex 第几天
	 * @param cost 补签消耗
	 */
	public static void logDailySignReward(Player player, int type, int termId, int dayIndex, String cost) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.daily_sign_sign);
			if(null == logParam){
				return;
			}
			logParam.put("type", type).put("termId", termId).put("day", dayIndex);			
			if(1 == type){
				logParam.put("cost", "");
			}else{
				logParam.put("cost",cost);
			}
			GameLog.getInstance().info(logParam);

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 领取返还兵力
	 * 
	 * @param player
	 * @param soldierId 兵种ID
	 * @param count     领取兵的数量
	 * @param type      领取类型，大R复仇领取或是新兵救援领取
	 */
	public static void logReceiveRevengeSoldier(Player player, int soldierId, int count, int type) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.receive_soldier);
			if (logParam != null) {
				logParam.put("soldierId", soldierId)
				        .put("count", count)
				        .put("type", type);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 触发限时商店上架时间
	 * 
	 * @param player
	 * @param triggerType
	 */
	public static void logTimeLimitStoreOnSell(Player player, int triggerType) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.time_limit_store);
			if (logParam != null) {
				logParam.put("conditionType", triggerType);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 记录泰伯利亚的跨服状态.
	 * @param player
	 * @param toServerId 跨到哪个区服
	 * @param type 操作行为 对应CrossStateType
	 */
	public static void logTimberiumCross(Player player, String toServerId, CrossStateType state) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.tiberium_cross);
			if (logParam != null) {
				logParam.put("targetServer", toServerId);
				logParam.put("state", state.intVal());
				
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录泰伯利亚报名信息
	 * @param guildId	报名联盟id
	 * @param guildName
	 * @param memberCnt	成员数量
	 * @param totalPowar 成员总战力
	 * @param serverId	区服id
	 * @param termId	期数
	 * @param timeIndex	战场时段角标
	 * @param playerId	报名玩家id
	 */
	public static void logTimberiumSignup(String guildId, String guildName, int memberCnt, long totalPowar, String serverId, int termId, int timeIndex, String playerId) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.tiberium_sign_up);
		    logParam.put("guildId", guildId)
					.put("guildName", guildName)
					.put("memberCnt", memberCnt)
					.put("totalPowar", totalPowar)
					.put("termId", termId)
					.put("timeIndex", timeIndex)
					.put("playerId", playerId);
			GameLog.getInstance().info(logParam);
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录泰伯利亚匹配信息
	 * @param roomId		房间id
	 * @param roomServer	房间所在服务器
	 * @param termId		期数
	 * @param timeIndex		战场时段角标
	 * @param guildA		联盟A
	 * @param serverA		联盟A所在服
	 * @param guildB		联盟B
	 * @param serverB		联盟B所在服
	 */
	 
	public static void logTimberiumMatchInfo(String roomId, String roomServer, int termId, int timeIndex, String guildA,long guildStrengthA, String serverA, String guildB, long guildStrengthB, String serverB) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.tiberium_match);
		    logParam.put("roomId", roomId)
			.put("roomServer", roomServer)
			.put("termId", termId)
			.put("timeIndex", timeIndex)
			.put("guildA", guildA)
			.put("guildStrengthA", guildStrengthA)
			.put("serverA", serverA)
			.put("guildB", guildB)
			.put("guildStrengthB", guildStrengthB)
			.put("serverB", serverB);
			GameLog.getInstance().info(logParam);
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录泰伯利亚参与匹配联盟信息
	 * @param guildId	
	 * @param guildName
	 * @param serverId
	 * @param memberCnt		参战人员数量
	 * @param totalPowar	参战人员总战力
	 * @param termId
	 * @param timeIndex		战场时段角标
	 */
	public static void logTimberiumMatcherInfo(String guildId, String guildName,String serverId, int memberCnt, long totalPowar, long guildStrength, int termId, int timeIndex) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.tiberium_match_guild);
		    logParam.put("guildId", guildId)
					.put("guildName", guildName)
					.put("memberCnt", memberCnt)
					.put("totalPowar", totalPowar)
					.put("guildStrength", guildStrength)
					.put("termId", termId)
					.put("timeIndex", timeIndex);
			GameLog.getInstance().info(logParam);
			HawkLog.logPrintln("LogUtil record TiberiumWar flushSignerInfo, guildId: {}, guildName: {} , serverId: {}, memberCnt: {}, totalPowar: {},guildStrength:{}, termId: {}, timeIndex: {}",
					guildId, guildName, serverId, memberCnt, totalPowar, guildStrength,termId, timeIndex);

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录泰伯利亚进入战场信息
	 * @param playerId
	 * @param termId
	 * @param roomId		房间id
	 * @param roomServer	房间所在区服
	 * @param guildId		玩家所在联盟
	 */
	public static void logTimberiumEnterInfo(String playerId, int termId, String roomId, String roomServer, String guildId, String serverId, long power,long guildStrength) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.tiberium_enter_room);
		    logParam.put("playerId", playerId)
					.put("serverId", serverId)
					.put("termId", termId)
					.put("roomId", roomId)
					.put("guildId", guildId)
					.put("roomServer", roomServer)
					.put("power", power)
					.put("guildStrength", guildStrength);
			GameLog.getInstance().info(logParam);
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录泰伯利亚退出战场信息
	 * @param playerId
	 * @param termId
	 * @param guildId		玩家所在联盟
	 * @param isMidaway		是否中途退出
	 */
	public static void logTimberiumQuitInfo(String playerId, int termId, String guildId, boolean isMidaway) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.tiberium_quit_room);
		    logParam.put("playerId", playerId)
			.put("termId", termId)
			.put("guildId", guildId)
			.put("isMidaway", isMidaway ? 1 : 0);
			GameLog.getInstance().info(logParam);
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录泰伯利亚个人结算积分信息
	 * @param playerId
	 * @param termId
	 * @param guildId
	 * @param score
	 */
	public static void logTimberiumPlayerScoreInfo(TWPlayerScoreLogUnit scoreLogUnit) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.tiberium_player_score);
			logParam.put("playerId", scoreLogUnit.getPlayerId())
			.put("termId", scoreLogUnit.getTermId())
			.put("guildId", scoreLogUnit.getGuildId())
			.put("score", scoreLogUnit.getScore());
			GameLog.getInstance().info(logParam);
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录泰伯利亚联盟结算积分信息
	 * @param logUnit
	 */
	public static void logTimberiumGuildScoreInfo(TWGuildScoreLogUnit logUnit) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.tiberium_guild_score);
		    logParam.put("guildId", logUnit.getGuildId())
			.put("guildName", logUnit.getGuildName())
			.put("termId", logUnit.getTermId())
			.put("serverId", logUnit.getServerId())
			.put("roomId", logUnit.getRoomId())
			.put("roomServer", logUnit.getRoomServer())
			.put("score", logUnit.getScore())
			.put("memberCnt", logUnit.getMemberCnt())
			.put("totalPower", logUnit.getTotalPower())
			.put("isWin", logUnit.isWin() ? 1: 0);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 记录泰伯利亚出战玩家信息
	 * @param playerId 玩家ID
	 * @param guildId 联盟ID
	 * @param selfPowar 玩家新战力
	 * @param termId 期数
	 */
	public static void logTimberiumPlayerWarInfo(String playerId, String guildId, long selfPowar, int termId) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.tiberium_player_war_info);
		    logParam.put("playerId", playerId)
			.put("guildId", guildId)
			.put("selfPowar", selfPowar)
			.put("termId", termId);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 装扮赠送打点
	 * 
	 * @param player
	 * @param type 1:赠送 ,2:索要
	 * @param dressId     装扮id
	 * @param tplayer     目标玩家id 
	 * @param item		      道具
	 */
	public static void logPlayerDressSend(Player player, int type, int dressId, String tPlayer, String itemStr) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.dress_send);
			if (logParam != null) {
				logParam.put("type", type)
				        .put("dressId", dressId)
				        .put("tPlayer", tPlayer)
						.put("item", itemStr);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 推广员活动玩家打点
	 * @param player
	 * @param openid 绑定的openid
	 * @param code  绑定的code
	 * @param charge 玩家充值数量
	 * @param exchange 字段为0 表示是登录数据，其他表示兑换的id
	 * @param count 兑换的数量
	 */
	public static void logPlayerSpreadLogin(Player player, String openid, String code, int charge, int exchange, int count){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.spread_log);
			if (logParam != null) {
				logParam.put("code", code)
						.put("openid", openid)
						.put("charge", charge)
						.put("exchange", exchange)
						.put("count", count);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 幸运折扣活动刷新奖池打点
	 * @param playerId
	 * @param refreshType 1 免费次数刷新, 2 使用道具刷新
	 * @param poolId 刷新到的奖池id
	 * @param discount 刷新到的折扣
	 */
	public static void logLuckyDiscountDraw(Player player, int refreshType, int poolId, String discount){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.lucky_discount_draw);
			if (logParam != null) {
				logParam.put("refreshType", refreshType)
						.put("poolId", poolId)
						.put("discount", discount);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}	
	}
	
	/**
	 * 幸运折扣活动购买商品打点
	 * @param playerId
	 * @param goods 购买商品
	 * @param price 购买的单价
	 * @param num  购买的数量
	 * @param discount 购买的折扣
	 * @param goodsId 购买商品的id
	 */
	public static void logLuckyDiscountBuy(Player player, String goods, String price, int num, String discount, int goodsId){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.lucky_discount_buy);
			if (logParam != null) {
				logParam.put("goods", goods)
						.put("price", price)
						.put("num", num)
						.put("discount", discount)
						.put("goodsId", goodsId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}	
	}
	/**tbly 中每分钟积分*/
	public static void logTBLYPlayerHonor(Player player, int honor) {
		try {
			if (!GsConfig.getInstance().isTlogEnable()) {
				return;
			}
			LogParam logParam = getPersonalLogParam(player, LogInfoType.tbly_player_honor);
			if (logParam != null) {
				logParam.put("honor", honor);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/** tbly 中每分钟积分 */
	public static void logTBLYGuildHonor(String roomId, String guildId, String guildName, int honor) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.tbly_guild_honor);
			logParam.put("guildId", guildId)
					.put("guildName", guildName)
					.put("roomId", roomId)
					.put("honor", honor);
			GameLog.getInstance().info(logParam);

		} catch (Exception e) {
			HawkException.catchException(e);
		}

	}
	
	
	/** 
	 * tbly 击杀异兽
	 * @param roomId
	 * @param guildId
	 * @param guildName
	 * @param honor
	 */
	public static void logTBLYKillMonster(Player player,String roomId, String guildId, String guildName, int monsterId, int orderPowerAdd) {
		try {
			LogParam logParam = getPersonalLogParam(player, LogInfoType.tbly_player_kill_monster);
			logParam.put("guildId", guildId)
					.put("guildName", guildName)
					.put("roomId", roomId)
					.put("monsterId", monsterId)
					.put("orderPowerAdd", orderPowerAdd);
			GameLog.getInstance().info(logParam);

		} catch (Exception e) {
			HawkException.catchException(e);
		}

	}
	
	
	/**
	 * tbly 使用号令
	 * @param roomId
	 * @param guildId
	 * @param guildName
	 * @param honor
	 */
	public static void logTBLYUseOrder(Player player,String roomId, String guildId, String guildName, int orderId,int orderPowerCost ) {
		try {
			LogParam logParam = getPersonalLogParam(player, LogInfoType.tbly_guild_use_order);
			logParam.put("guildId", guildId)
					.put("guildName", guildName)
					.put("roomId", roomId)
					.put("orderId", orderId)
					.put("orderPowerCost", orderPowerCost);
			GameLog.getInstance().info(logParam);

		} catch (Exception e) {
			HawkException.catchException(e);
		}

	}
	
	
	

	/**
	 * 投资理财记录
	 * 
	 * @param player
	 * @param productId 理财产品ID
	 * @param investAmount 投资额度
	 * @param addCustomer 购买时是否购买加成道具
	 * @param cancel      是否时取消投资
	 */
	public static void logInvest(Player player, int productId, int investAmount, boolean addCustomer, boolean cancel) {
		try {
			LogParam logParam = getPersonalLogParam(player, LogInfoType.invest);
			if (logParam != null) {
				logParam.put("productId", productId)
				        .put("investAmount", investAmount)
						.put("customer", addCustomer ? 1 : 0)
				        .put("investCancel", cancel ? 1 : 0);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 
	 * @param serverId 以队长的信息的主Id
	 * @param fightResult 战斗结果 赢了为1，输了为0
	 */
	public static void logCrossFortress(String serverId, boolean fightResult) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.cross_fortress_war);
		    logParam.put("fightResult", fightResult ? 1 : 0);
			logParam.put("originServer", serverId);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	public static void logCrossFortressOccupyTime(String serverId, int occupyTime) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.cross_fortress_occupy);
		    logParam.put("originServer", serverId)
					.put("occupyTime", occupyTime);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 英雄试炼接受任务
	 */
	public static void logHeroTrialReceive(Player player, int missionId) {
		try {
			LogParam logParam = getPersonalLogParam(player, LogInfoType.hero_trial_receive);
			if (logParam != null) {
				logParam.put("missionId", missionId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 英雄试炼刷新任务
	 */
	public static void logHeroTrialRefreshMission(Player player, int missionId) {
		try {
			LogParam logParam = getPersonalLogParam(player, LogInfoType.hero_trial_refresh_mission);
			if (logParam != null) {
				logParam.put("missionId", missionId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 英雄试炼完成任务
	 */
	public static void logHeroTrialComplete(Player player, int missionId) {
		try {
			LogParam logParam = getPersonalLogParam(player, LogInfoType.hero_trial_complete);
			if (logParam != null) {
				logParam.put("missionId", missionId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 英雄试炼金币刷新
	 */
	public static void logHeroTrialCostRefresh(Player player, String cost) {
		try {
			LogParam logParam = getPersonalLogParam(player, LogInfoType.hero_trial_cost_refresh);
			if (logParam != null) {
				logParam.put("cost", cost);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 英雄试炼金币加速
	 * @param player 
	 */
	public static void logHeroTrialCostSpeed(Player player, int cost) {
		try {
			LogParam logParam = getPersonalLogParam(player, LogInfoType.hero_trial_cost_complete);
			if (logParam != null) {
				logParam.put("cost", cost);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 复仇商店触发记录
	 * 
	 * @param player
	 * @param soldierCount     死兵数量
	 * @param circleStartTime  此周期开始时间
	 * @param login            是否是登录触发
	 */
	public static void logTriggerRevengeShop(Player player, int soldierCount, long circleStartTime, boolean login) {
		try {
			LogParam logParam = getPersonalLogParam(player, LogInfoType.revenge_shop);
			if (logParam != null) {
				logParam.put("soldierCount", soldierCount)
				        .put("login", login ? 1 : 0)
						.put("startTime", HawkTime.formatTime(circleStartTime));
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 英雄返场兑换道具打点
	 * 
	 * @param player
	 * @param activityId  活动ID
	 * @param exchangeId  兑换档位ID
	 * @param cost        兑换消耗道具
	 * @param gain        兑换所得道具
	 * @param exchangeCount  兑换次数
	 */
	public static void logHeroBackExchange(Player player, int activityId, int exchangeId, String cost, String gain, int exchangeCount) {
		try {
			LogParam logParam = getPersonalLogParam(player, LogInfoType.hero_back_exchange);
			if (logParam != null) {
				logParam.put("activityId", activityId)
				        .put("exchangeId", exchangeId)
				        .put("cost", cost)
				        .put("gain", gain)
						.put("exchangeCount", exchangeCount);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 英雄返场购买道具打点
	 * 
	 * @param player
	 * @param activityId 活动ID
	 * @param chestId    宝箱ID
	 * @param count      购买数量
	 * @param cost       货币消耗
	 * @param gain       获得道具
	 */
	public static void logHeroBackBuyChest(Player player, int activityId, int chestId, int count, String cost, String gain) {
		try {
			LogParam logParam = getPersonalLogParam(player, LogInfoType.hero_back_buy);
			if (logParam != null) {
				logParam.put("activityId", activityId)
		        .put("chestId", chestId)
		        .put("cost", cost)
		        .put("gain", gain)
				.put("count", count);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 黑科技刷新奖池
	 * @param playerId
	 * @param cost
	 * @param buffId
	 */
	public static void logBlackTechDraw(Player player, long cost, int buffId){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.black_tech_draw);
			if (logParam != null) {
				logParam.put("cost", cost)
						.put("buffId", buffId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}	
	}
	
	/**
	 * 黑科技激活buff
	 * @param playerId
	 * @param buffId
	 */
	public static void logBlackTechActive(Player player, int buffId){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.black_tech_active);
			if (logParam != null) {
				logParam.put("buffId", buffId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}		
	}
	
	/**
	 * 黑科技购买加持礼包
	 * @param playerId
	 * @param packageId
	 */
	public static void logBlackTechBuy(Player player, int packageId){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.black_tech_buy);
			if (logParam != null) {
				logParam.put("packageId", packageId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}	
	}
	
	/**
	 * 玩家城点保护罩打点
	 * 
	 * @param player
	 * @param openShield  开启或关闭
	 * @param endTime     结束时间
	 * @param autoEnd     是否是自动结束
	 */
	public static void logCityShieldChange(Player player, boolean openShield, long endTime, boolean autoEnd) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.city_shield);
			if (logParam != null) {
				logParam.put("openShield", openShield ? 1: 0)
						.put("endTime", HawkTime.formatTime(endTime))
						.put("auto", autoEnd ? 1: 0);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}	
	}
	
	/**
	 * 铠甲产出 
	 */
	public static void logArmourAdd(Player player, String uuid, int armourId, int quality) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.armour_add);
			if (logParam != null) {
				logParam.put("uuid", uuid);
				logParam.put("armourId", armourId);
				logParam.put("quality", quality);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 铠甲解锁套装
	 */
	public static void logArmourUnlockSuit(Player player, int suitId){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.armour_unlock_suit);
			if (logParam != null) {
				logParam.put("suitId", suitId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}	
	}
	
	/**
	 * 铠甲分解
	 */
	public static void logArmourResolve(Player player, int count){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.armour_resolve);
			if (logParam != null) {
				logParam.put("count", count);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}	
	}
	
	/**
	 * 强化
	 */
	public static void logArmourIntensify(Player player, String uuid, int armourId, int afterLevel){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.armour_intensify);
			if (logParam != null) {
				logParam.put("uuid", uuid);
				logParam.put("armourId", armourId);
				logParam.put("afterLevel", afterLevel);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}	
	}
	
	/**
	 * 传承
	 */
	public static void logArmourInherit(Player player, String uuid, String beUuid){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.armour_inherit);
			if (logParam != null) {
				logParam.put("uuid", uuid);
				logParam.put("beUuid", beUuid);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}	
	}
	
	/**
	 * 突破
	 */
	public static void logArmourBreakthrough(Player player, String uuid, int armourId, int afterQuality){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.armour_breakthrough);
			if (logParam != null) {
				logParam.put("uuid", uuid);
				logParam.put("armourId", armourId);
				logParam.put("afterQuality", afterQuality);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}	
	}
	
	/**
	 * 全副武装探测打点
	 * @param playerId
	 * @param searchId
	 */
	public static void logFullArmedSearch(Player player, int searchId ){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.fully_armed_search);
			if (logParam != null) {
				logParam.put("searchId", searchId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}	
	}
	
	/**
	 * 全副武装购买打点
	 * @param playerId
	 * @param cfgId
	 * @param count
	 */
	public static void logFullArmedBuy(Player player, int cfgId, int count){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.fully_armed_buy);
			if (logParam != null) {
				logParam.put("cfgId", cfgId)
				.put("count", count);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}			
	}
	
	/**
	 * 先锋豪礼参与购买打点
	 * 
	 * @param player
	 * @param termId
	 * @param type
	 * @param giftId
	 */
	public static void logPioneerGiftBuy(Player player, int termId, int type, int giftId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.pioneer_gift_buy);
			if (logParam != null) {
				logParam.put("termId", termId)
						.put("type", type)
						.put("giftId", giftId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}	
	}
	
	
	/**
	 * 时空轮盘抽奖
	 * @param playerId
	 * @param termId
	 * @param count    1 抽一次  10 十连抽
	 * @param buyCount    购买多少个
	 * @param itemSet 当前的四个设置
	 */
	public static void logRouletteActivityLottery(Player player, int termId, int count, int buyCount, String itemSet){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.roulette_lottery);
			if (logParam != null) {
				logParam.put("termId", termId)
						.put("count", count)
						.put("buyCount", buyCount)
						.put("itemSet", itemSet);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}	
	}
	
	/**
	 * 领取时空轮盘箱子奖励
	 * @param playerId
	 * @param termId
	 */
	public static void logRouletteActivityRewardBox(Player player, int termId){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.roulette_box);
			if (logParam != null) {
				logParam.put("termId", termId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}	
	}
	
	/**
	 * 记录锦标赛联盟参与信息
	 * @param guildId
	 * @param guildName
	 * @param termId 期数
	 * @param guildGrade 联盟段位
	 * @param signCnt 报名的成员数量
	 * @param joinCnt 参与的成员数量
	 */
	public static void logGCGuildInfo( String guildId, String guildName, int termId, GCGuildGrade guildGrade, int signCnt, int joinCnt) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.championship_guild);
			logParam.put("guildId", guildId)
			.put("guildName", guildName)
			.put("termId", termId)
			.put("guildGrade", guildGrade.getValue())
			.put("signCnt", signCnt)
			.put("joinCnt", joinCnt);
			GameLog.getInstance().info(logParam);
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	public static void logGCPlayerInfo(Player player , int termId){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.championship_player);
			if (logParam != null) {
					logParam.put("termId", termId);
					logParam.put("talentType", player.getData().getPlayerEntity().getTalentType());
					logParam.put("superLab", player.getData().getPlayerEntity().getSuperLab());
					int[] valArr = player.getEffect().getEffValArr(EffType.WAR_TROOP_ATK_PER,EffType.WAR_ALL_DEF,EffType.TROOP_STRENGTH_PER_ITEM);
					logParam.put("buffATK", valArr[0]);
					logParam.put("buffDEF", valArr[1]);
					logParam.put("buffSTR", valArr[2]);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录体力变化流水
	 * 
	 * @param player
	 * @param beforeValue
	 * @param afterValue
	 * @param amount
	 */
	public static void logVitChange(Player player, int beforeValue, int afterValue, int amount, int addOrReduce, Action action) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.vit_change);
			if (logParam != null) {
				logParam.put("beforeValue", beforeValue)
						.put("afterValue", afterValue)
						.put("amount", amount)
						.put("addOrReduce", addOrReduce)
						.put("reason", action.intItemVal());
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 每日任务（酒馆）积分变化记录
	 * 
	 * @param player
	 * @param beforeValue
	 * @param afterValue
	 * @param amount
	 * @param type
	 */
	public static void logDailyActiveScoreChange(Player player, int beforeValue, int afterValue, int amount, int type) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.daily_active_change);
			if (logParam != null) {
				logParam.put("beforeValue", beforeValue)
						.put("afterValue", afterValue)
						.put("amount", amount)
						.put("reason", type);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	

	/**
	 * 皮肤计划
	 * @param player
	 * @param addScore 本次加分
	 * @param afterScore 总分
	 * @param beforeScore 之前总分
	 */
	public static void logSkinPlan(Player player, int addScore, int afterScore, int beforeScore) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.skin_plan);
			if (logParam != null) {
				logParam.put("rollScore", addScore);
				logParam.put("beforeScore", beforeScore);
				logParam.put("afterScore", afterScore);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 英雄皮肤打点
	 * 
	 * @param player
	 * @param action
	 * @param itemsInfo
	 * @param skin
	 */
	public static void logHeroSkinChange(Player player, Action action, HawkTuple2<String, String> itemsInfo, HeroSkin skin) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.hero_skin_change);
			if (logParam != null) {
				logParam.put("reason", action.intItemVal())
						.put("itemIds", itemsInfo.first) // 消耗物品id
						.put("itemCounts", itemsInfo.second) // 消耗物品数量
						.put("skinId", skin.getCfgId()) // 皮肤id
						.put("step", skin.getStep()) // 阶
						.put("luck", skin.getLuck()); // 幸运值
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 今日累充活动信息打点记录
	 * 
	 * @param player
	 * @param id       成就ID或宝箱ID
	 * @param buyGift  是否购买宝箱，如果不是购买宝箱，就是领取成就奖励
	 */
	public static void logDailyRechargeInfo(Player player, int id, boolean buyGift) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.daily_recharge);
			if (logParam != null) {
				logParam.put("giftId", id)
				.put("buyGift", buyGift ? 1 : 0);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 今日累充新版购买礼包打点记录
	 * 
	 * @param player
	 * @param giftId	礼包ID
	 * @param rewardIds 礼包选择奖励ID集合
	 */
	public static void logDailyRechargeNewInfo(Player player, int giftId, String rewardIds) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.daily_recharge_new);
			if (logParam != null) {
				logParam.put("giftId", giftId)
				.put("rewardIds", rewardIds);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 
	 * @param player
	 * type 1 请求建立, 2建立, 3 删除关系.
	 */
	public static void logGuardRelation(Player player, String targetId, int type) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.guard_relation);
			if (logParam != null) {
				logParam.put("targetPlayerId", targetId);
				logParam.put("type", type);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录等级玩家.
	 * @param level
	 * @param num
	 */
	public static void logGuardPlayerLevel(int level, int num) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.guard_player_level);
			logParam.put("level", level)
					.put("num", num);
			GameLog.getInstance().info(logParam);			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	} 
	
	/** 记录机甲觉醒活动玩家总伤害变更
	 * 
	 * @param player
	 * @param activityId
	 * @param termId
	 * @param addScore
	 * @param totalScore
	 */
	public static void logMachineAwakePersonDamage(Player player, int activityId, int termId, int addScore, long totalScore) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.machine_awake_person_damage);
			if (logParam != null) {
				logParam.put("activityId", activityId)
				.put("termId", termId)
				.put("addScore", addScore)
				.put("totalScore", totalScore);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**中秋定点礼包购买打点
	 * @param player
	 * @param giftId 礼包id
	 * @param items 
	 */
	public static void logMidAutumnGift(Player player, int giftId, String items) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.mid_autumn_gift);
			if (logParam != null) {
				logParam.put("giftId", giftId);
				logParam.put("items", items);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**记录使用代金券购买礼包信息
	 * @param player
	 * @param type
	 * @param giftId
	 * @param costMoney
	 * @param moneyType
	 * @param giftItemType
	 * @param voucherId 代金券ID
	 * @param voucherValue 代金券面值金额
	 */
	public static void logGiftByVoucher(Player player, GiftType type, String giftId, int costMoney, int moneyType, int giftItemType, int voucherId, int voucherValue) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.gift_buy_voucher);
		if (logParam != null) {
			logParam.put("giftType", type.intVal())
					.put("giftId", giftId)
					.put("costMoney", costMoney)
					.put("battlePoint", player.getPower())
					.put("moneyType", moneyType)
					.put("itemType", giftItemType)
					.put("buyCount", 1)
					.put("voucherId", voucherId)
					.put("voucherValue", voucherValue);
			GameLog.getInstance().info(logParam);
		}
	}
	
	/**
	 * 接受任务 
	 */
	public static void logACMissionReceive(Player player, int achieveId, int guildMemberCount) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.ac_receive_mission);
			if (logParam != null) {
				logParam.put("achieveId", achieveId)
				.put("guildMemberCount", guildMemberCount);
			}
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 完成任务 
	 */
	public static void logACMissionFinish(Player player, int addExp, int guildMemberCount) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.ac_finish_mission);
			if (logParam != null) {
				logParam.put("addExp", addExp)
				.put("guildMemberCount", guildMemberCount);
			}
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 放弃任务
	 */
	public static void logACMissionAbandon(Player player, int achieveId, boolean outData) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.ac_abandon_mission);
			if (logParam != null) {
				logParam.put("achieveId", achieveId)
				.put("outData", outData ? 1 : 0);
			}
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 购买次数
	 */
	public static void logACMBuyTimes(Player player) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.ac_buy_times);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 击杀机甲
	 * 
	 * @param player
	 * @param x
	 * @param y
	 */
	public static void logOnceKillGundam(Player player, int x, int y) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.kill_gundam);  
			if (logParam != null) {
				logParam.put("posX", x).put("posY", y).put("type", GundamKillType.ONCE_KILL.intVal()); // 致命一击
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 击杀机甲
	 * 
	 * @param player
	 * @param x
	 * @param y
	 */
	public static void logKillGundam(Player player, int x, int y) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.kill_gundam); 
			if (logParam != null) {
				logParam.put("posX", x).put("posY", y).put("type", GundamKillType.KILL.intVal()); // 最终一击 
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录守护值的变化.
	 * @param player
	 * @param guardValue
	 */
	public static void logGuardValue(Player player, int guardValue) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.guard_value);			
			logParam.put("guardValue", guardValue);				
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录 显示掉落的单次掉落
	 * @param player
	 * @param dropNum 单次掉落的物品数量.
	 */
	public static void logTimeLimitDrop(Player player, int dropNum) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.time_limit_drop);			
			logParam.put("dropNum", dropNum);				
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * @param player
	 * @param num 勋章行动中,抽奖次数
	 */
	public static void logMedalTreasureLottery(Player player, int num) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.medal_treasure_lottery);
		if (logParam != null) {
			logParam.put("num", num);
			GameLog.getInstance().info(logParam);
		}
	}
	
	/**
	 * 记录地狱火全军动员 
	 * @param player
	 * @param type {{@link GameConst.HELL_FIRE_TYPE}
	 * @param score
	 */
	public static void logHellFire(Player player, int type, int score) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.hell_fire);			
			logParam.put("type", type);
			logParam.put("score", score);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	
	/***
	 * 庆典特惠 成就任务完成
	 * @param player
	 * @param achieveId 成就ID
	 */
	public static void logTravelShopAssistAchieveFinish(Player player, int achieveId){
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.ac_travel_shop_assist_achieve_finish);
		if (logParam != null) {
			logParam.put("achieveId", achieveId); 
			GameLog.getInstance().info(logParam);
		}
	}
	
	
	/**
	 * 特惠商人刷新消耗
	 * @param player  
	 * @param times 刷新次数
	 * @param cost  刷新消耗
	 */
	public static void logTravelShopRefreshCost(Player player,int times,int costType,int cost){
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.ac_travel_shop_refresh_cost);
		if (logParam != null) {
			logParam.put("times", times); 
			logParam.put("costType", costType); 
			logParam.put("cost", cost); 
			GameLog.getInstance().info(logParam);
		}
	}
	

	
	/**
	 *  玩家许愿消耗金币和获取的许愿点
	 * @param player 
	 * @param awardId   锦鲤大奖ID
	 * @param costType  消耗货币类型
	 * @param cost      消耗货币数量
	 * @param wishPoint 获取许愿点
	 * @param termId    活动期数ID
	 * @param turnId    开奖轮ID
	 */
	public static void redkoiPlayerCost(Player player, int awardId,int costType,long cost,
			int wishPoint,String termId,String turnId){
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.ac_redkoi_player_wish_cost);
		if (logParam != null) {
			logParam.put("awardId", awardId); 
			logParam.put("costType", costType); 
			logParam.put("cost", cost); 
			logParam.put("wishPoint", wishPoint); 
			logParam.put("termId", termId); 
			logParam.put("turnId", turnId); 
			GameLog.getInstance().info(logParam);
		}
	}
	
	
	/**
	 * 玩家被选中锦鲤
	 * @param player    
	 * @param awardId   锦鲤大奖ID
	 * @param turnId    开奖期数(也是 开奖时间)
	 */
	public static void redkoiAward(Player player, int awardId,String temId,String turnId){
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.ac_redkoi_award);
		if (logParam != null) {
			logParam.put("awardId", awardId); 
			logParam.put("temId", temId); 
			logParam.put("turnId", turnId); 
			GameLog.getInstance().info(logParam);
		}
	}
	
	
	/**
	 * 记录泰伯利亚联赛匹配信息
	 * @param roomId		房间id
	 * @param roomServer	房间所在服务器
	 * @param termId		期数
	 * @param timeIndex		战场时段角标
	 * @param guildA		联盟A
	 * @param serverA		联盟A所在服
	 * @param guildB		联盟B
	 * @param serverB		联盟B所在服
	 * @param warStartTime	战场开始时间
	 */
	public static void logTimberiumLeaguaMatchInfo(String roomId, String roomServer, int season, int termId, String guildA, String serverA, String guildB, String serverB, long warStartTime,int battleType,int serverType){
		logTimberiumLeaguaMatchInfo(roomId, roomServer, season, termId, guildA, serverA, guildB, serverB, warStartTime, battleType, serverType, 0);
	}

	public static void logTimberiumLeaguaMatchInfo(String roomId, String roomServer, int season, int termId, String guildA, String serverA, String guildB, String serverB, long warStartTime,int battleType,int serverType, int groupBattleType) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.tiberium_leagua_match);
		    logParam.put("roomId", roomId)
			.put("roomServer", roomServer)
			.put("season", season)
			.put("termId", termId)
			.put("guildA", guildA)
			.put("serverA", serverA)
			.put("guildB", guildB)
			.put("serverB", serverB)
			.put("warStartTime", warStartTime)
			.put("group", battleType)
			.put("serverType", serverType)
			.put("battleType", groupBattleType);;
			GameLog.getInstance().info(logParam);
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录泰伯利亚联赛入选正赛联盟信息
	 * @param zoneId
	 * @param season
	 * @param termId
	 * @param guildId
	 * @param guildName
	 * @param serverId
	 */
	public static void logTimberiumLeaguaJoinGuild(int zoneId, int season, String guildId, String guildName, String serverId, long initPower) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.tiberium_leagua_join);
		    logParam.put("zoneId", zoneId)
					.put("season", season)
					.put("guildId", guildId)
					.put("guildName", guildName)
					.put("serverId", serverId)
					.put("initPower", initPower);
			GameLog.getInstance().info(logParam);
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 记录泰伯利亚联赛参与匹配联盟信息
	 * @param guildId
	 * @param guildName
	 * @param serverId
	 * @param memberCnt
	 * @param totalPowar
	 * @param season
	 * @param termId
	 */
	public static void logTimberiumLeaguaGuildWarInfo(String guildId, String guildName,String serverId, int memberCnt, long totalPowar, int season, int termId, int group, int serverType) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.tiberium_leagua_guild_war_info);
			logParam.put("guildId", guildId)
					.put("guildName", guildName)
					.put("memberCnt", memberCnt)
					.put("totalPowar", totalPowar)
					.put("season", season)
					.put("termId", termId)
					.put("group", group)
					.put("serverType", serverType);
			GameLog.getInstance().info(logParam);
			HawkLog.logPrintln("LogUtil record TiberiumWar flushSignerInfo, guildId: {}, guildName: {} , serverId: {}, memberCnt: {}, totalPower: {}, season: {}, termId: {}, group: {}",
					guildId, guildName, serverId, memberCnt, totalPowar, season, termId);

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录泰伯利亚联赛锁定出战时的联盟信息
	 * @param roomId
	 * @param roomServer
	 * @param season
	 * @param termId
	 * @param guildA
	 * @param serverA
	 * @param guildAName
	 * @param guildAPower
	 * @param guildB
	 * @param serverB
	 * @param guildBName
	 * @param guildBPower
	 */
	public static void logTimberiumLeaguaManageEndInfo(String roomId, String roomServer, int season, int termId, String guildA, String serverA, String guildAName, long guildAPower, String guildB, String serverB, String guildBName, long guildBPower) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.tiberium_leagua_guild_manage_end);
		    logParam.put("roomId", roomId)
					.put("roomServer", roomServer)
					.put("season", season)
					.put("termId", termId)
					.put("guildA", guildA)
					.put("serverA", serverA)
					.put("guildAname", guildAName)
					.put("guildApower", guildAPower)
					.put("guildB", guildB)
					.put("serverB", serverB)
					.put("guildBname", guildBName)
					.put("guildBpower", guildBPower);
			GameLog.getInstance().info(logParam);

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录泰伯利亚联赛出战玩家信息
	 * @param playerId
	 * @param guildId
	 * @param serverId
	 * @param selfPowar
	 * @param season
	 * @param termId
	 * @param group
	 */
	public static void logTimberiumLeaguaPlayerWarInfo(String playerId, String guildId, String serverId, long selfPowar, int season, int termId, int group) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.tiberium_leagua_player_war_info);
		    logParam.put("playerId", guildId)
			.put("guildId", guildId)
			.put("selfPowar", selfPowar)
			.put("season", season)
			.put("termId", termId)
			.put("group", group);
			GameLog.getInstance().info(logParam);
			HawkLog.logPrintln("LogUtil record TiberiumWar flushSignerInfo, playerId:{}, guildId: {}, serverId: {}, selfPowar: {}, season: {}, termId: {}, group: {}", playerId,
					guildId, serverId, selfPowar, season, termId, group);
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录泰伯联赛参与玩家积分战力数据
	 * @param logUnit
	 */
	public static void logTimberiumLeaguaPlayerScore(TWPlayerSeasonScoreLogUnit logUnit) {
		try {
			if (!GsConfig.getInstance().isTlogEnable()) {
				return;
			}
			String playerId = logUnit.getPlayerId();
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			if(player == null){
				return;
			}
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.tiberium_leagua_player_score);
			if (logParam != null) {
				logParam.put("roleName", player.getName())
				.put("money", player.getRechargeTotal()/10)
				.put("registDay", player.getPlayerRegisterDays())
				.put("registTime", player.getCreateTime())
				.put("power", player.getPower())
				.put("roomId", logUnit.getRoomId())
				.put("roomServer", logUnit.getRoomServer())
				.put("guildId", logUnit.getGuildId())
				.put("guildName", logUnit.getGuildName())
				.put("score", logUnit.getScore())
				.put("result", logUnit.isWin() ? 1 : 0);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	/**
	 * 泰伯利亚联赛个人积分奖励发放记录
	 * @param logUnit
	 */
	public static void logTimberiumLeaguaSelfReward(TWSelfRewardLogUnit logUnit) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.tiberium_leagua_player_season_reward);
			logParam.put("playerId", logUnit.getPlayerId())
			.put("guildId", logUnit.getGuildId())
			.put("selfScore", logUnit.getSelfScore())
			.put("season", logUnit.getSeason())
			.put("termId", logUnit.getTermId())
			.put("rewardId", logUnit.getRewardId())
			.put("isLeagua", logUnit.isLeagua() ? 1 : 0);
			GameLog.getInstance().info(logParam);
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 泰伯利亚联赛联盟奖励积分发放记录
	 * @param playerId
	 * @param guildId
	 * @param serverId
	 * @param guildScore
	 * @param selfScore
	 * @param season
	 * @param termId
	 * @param rewardId
	 * @param isLeagua 是否联赛战场
	 */
	public static void logTimberiumLeaguaGuildReward(String playerId, String guildId, String serverId, long guildScore, long selfScore, int season, int termId, int rewardId, boolean isLeagua) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.tiberium_leagua_guild_season_reward);
		    logParam.put("playerId", playerId)
			.put("guildId", guildId)
			.put("guildScore", guildScore)
			.put("selfScore", selfScore)
			.put("season", season)
			.put("termId", termId)
			.put("rewardId", rewardId)
			.put("isLeagua", isLeagua ? 1 : 0);
			GameLog.getInstance().info(logParam);
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录泰伯利亚联赛进入战场信息
	 * @param playerId
	 * @param season
	 * @param termId
	 * @param roomId
	 * @param roomServer
	 * @param guildId
	 * @param serverId
	 * @param power
	 */
	public static void logTimberiumLeaguaEnterInfo(String playerId, int season, int termId, String roomId, String roomServer, String guildId, String serverId, long power) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.tiberium_leagua_enter_room);
		    logParam.put("playerId", playerId)
					.put("season", season)
					.put("termId", termId)
					.put("roomId", roomId)
					.put("roomServer", roomServer)
					.put("guildId", guildId)
					.put("serverId", serverId)
					.put("power", power);
			GameLog.getInstance().info(logParam);
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 记录泰伯利亚联赛退出战场信息
	 * @param playerId
	 * @param season
	 * @param termId
	 * @param guildId		玩家所在联盟
	 * @param isMidaway		是否中途退出
	 */
	public static void logTimberiumLeaguaQuitInfo(String playerId, int season, int termId, String guildId, boolean isMidaway) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.tiberium_leagua_quit_room);
		    logParam.put("playerId", playerId)
			.put("season", season)
			.put("termId", termId)
			.put("guildId", guildId)
			.put("isMidaway", isMidaway ? 1 : 0);
			GameLog.getInstance().info(logParam);
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录泰伯利亚联赛战斗结果
	 * @param logUnit
	 */
	public static void logTimberiumLeaguaWarResult(TLWWarResultLogUnit logUnit) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.tiberium_leagua_war_result);
		    logParam.put("roomId", logUnit.getRoomId())
			.put("season", logUnit.getSeason())
			.put("termId", logUnit.getTermId())
			.put("guildA", logUnit.getGuildA())
			.put("scoreA", logUnit.getScoreA())
			.put("guildB", logUnit.getGuildB())
			.put("scoreB", logUnit.getScoreB())
			.put("winner", logUnit.getWinner())
			.put("firstKill", logUnit.getFirstKill())
			.put("first5000", logUnit.getFirst5000())
			.put("firstOccupa", logUnit.getFirstOccupa())
			.put("group", logUnit.getBattleType())
			.put("serverType", logUnit.getServerType());
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录泰伯利亚联赛参与联盟信息
	 * @param logUnit
	 */
	public static void logTimberiumLeaguaGuildInfo(TLWLeaguaGuildInfoUnit logUnit) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.tiberium_leagua_guild_info);
		    logParam.put("season", logUnit.getSeason())
			.put("termId", logUnit.getTermId())
			.put("guildId", logUnit.getGuildId())
			.put("guildName", logUnit.getGuildName())
			.put("guildServer", logUnit.getGuildServer())
			.put("guildFlag", logUnit.getGuildFlag())
			.put("teamId", logUnit.getTeamId())
			.put("groupType", logUnit.getGroupType().getNumber())
			.put("isSeed", logUnit.isSeed() ? 1 : 0)
			.put("leaderId", logUnit.getLeaderId())
			.put("leaderName", logUnit.getLeaderName())
			.put("leaderOpenid", logUnit.getLeaderOpenid())
			.put("memberCnt", logUnit.getMemberCnt())
			.put("winCnt", logUnit.getWinCnt())
			.put("loseCnt", logUnit.getLoseCnt())
			.put("totalScore", logUnit.getTotalScore())
			.put("kickOutTerm", logUnit.getKickOutTerm())
			.put("serverType", logUnit.getServerType());
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	public static void logTimberiumLeaguaNewSignupOld(String playerId, String guildId) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.tiberium_leagua_new_signup_old);
			logParam.put("playerId", playerId)
					.put("guildId", guildId);
			GameLog.getInstance().info(logParam);

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 大帝战领建筑时长秒
	 * @param honor 该盟累计占领建筑时长
	 */
	public static void logSWGuildHonor(String roomId, String guildId, String guildName, long honor) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.sw_guild_honor);
		    logParam.put("guildId", guildId)
					.put("guildName", guildName)
					.put("roomId", roomId)
					.put("honor", honor);
			GameLog.getInstance().info(logParam);

		} catch (Exception e) {
			HawkException.catchException(e);
		}

	}
	/**
	 * 记录金币瓜分活动开红包奖励
	 * @param player
	 * @param goldNum  开红包，开出的金币数量
	 */
	public static void logOpenRedEnvelope(Player player,int goldNum) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.divide_gold_open_red_envelope);			
			logParam.put("goldNum", goldNum);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 玩家分享打点
	 * @param player
	 * @param shareType
	 */
	public static void playerShare(Player player,int shareType) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.player_share);
		if (logParam != null) {
			logParam.put("shareType", shareType);
			GameLog.getInstance().info(logParam);
		}
	}

	/**
	 * 玩家分享名人堂打点
	 * @param player
	 * @param shareType 分享类型，0.是分享，1.是领奖
	 * @param season 分享的赛季
	 */
	public static void playerShareFameHall(Player player,int shareType, int season) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.player_share_fame_hall);
		if (logParam != null) {
			logParam.put("shareType", shareType);
			logParam.put("season", season);
			GameLog.getInstance().info(logParam);
		}
	}
	
	

	
	/**
	 * 记录星球大战进入战场信息
	 * @param playerId	玩家id
	 * @param guildId	联盟id
	 * @param serverId	区服id
	 * @param termId	期数
	 * @param roomId	房间id
	 * @param roomServer	房间区服id
	 * @param warType	初赛1/决赛2
	 */
	public static void logSWEnterInfo(String playerId, String guildId, String serverId, int termId, String roomId, String roomServer, SWWarType warType) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.sw_enter_room);
		    logParam.put("playerId", playerId)
					.put("guildId", guildId)
					.put("serverId", serverId)
					.put("termId", termId)
					.put("roomId", roomId)
					.put("roomServer", roomServer)
					.put("warType", warType.getNumber());
			GameLog.getInstance().info(logParam);
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 记录星球大战退出战场信息
	 * @param playerId
	 * @param guildId	联盟id
	 * @param serverId	区服id
	 * @param termId	期数
	 * @param warType	初赛1/决赛2
	 * @param isMidaway 是否中途退出
	 */

	public static void logSWQuitInfo(String playerId, String guildId, String serverId, int termId, SWWarType warType, boolean isMidaway) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.sw_quit_room);
			logParam.put("playerId", playerId)
			.put("guildId", guildId)
			.put("serverId", serverId)
			.put("termId", termId)
			.put("warType", warType.getNumber())
			.put("isMidaway", isMidaway ? 1 : 0);
			GameLog.getInstance().info(logParam);
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录星球大战对战联盟信息
	 * @param roomId	房间id
	 * @param roomServer	房间区服id
	 * @param termId	期数
	 * @param warType	初赛1/决赛2
	 * @param guildId	联盟id
	 * @param guildServer	联盟区服id
	 */
	
	public static void logSWMatchInfo(String roomId, String roomServer, int termId, SWWarType warType,  String guildId, String guildServer) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.sw_join_guild);
		    logParam.put("roomId", roomId)
			.put("roomServer", roomServer)
			.put("termId", termId)
			.put("warType", warType.getNumber())
			.put("guildId", guildId)
			.put("guildServer", guildServer);
			GameLog.getInstance().info(logParam);
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 大世界上的能量塔，行军到达
	 * @param player
	 */
	public static void logPylonReach(Player player) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.pylon_march_reach);
			if (logParam != null) {
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 大世界上的能量塔，战斗事件
	 * @param player 攻击方玩家
	 * @param atkerBattlePoint 攻击方玩家战力
	 * @param defPlayerId 防御玩家id
	 * @param deferBattlePoint 防御玩家战力
	 */
	public static void logPylonBattle(Player player, long atkerBattlePoint, String defPlayerId, long deferBattlePoint) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.pylon_march_battle);
			if (logParam != null) {
				logParam.put("atkerBattlePoint", atkerBattlePoint);
				logParam.put("defPlayerId", defPlayerId);
				logParam.put("deferBattlePoint", deferBattlePoint);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 英雄进化奖池兑换
	 * 
	 * @param player
	 * @param level			奖池等级
	 * @param exchangeId	兑换ID
	 */
	public static void logEvolutionExchange(Player player, int level, int exchangeId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.evolution_exchange);
			if (logParam != null) {
				logParam.put("level", level)
						.put("exchangeId", exchangeId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 英雄进化积分变动
	 * 
	 * @param player
	 * @param exp		积分变化数		
	 * @param add		true增加false减少
	 * @param resourceId 增加或减少的产生来源  taskId或exchangeId
	 */
	public static void logEvolutionExpChange(Player player, int exp, boolean add, int resourceId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.evolution_exp_change);
			if (logParam != null) {
				logParam.put("score", exp)
				        .put("resourceId", resourceId)
					    .put("add", add ? 1: 0);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 英雄进化任务完成情况
	 * 
	 * @param playerId
	 * @param taskId		任务ID
	 * @param times			完成轮次
	 */
	public static void logEvolutionTask(Player player, int taskId, int times) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.evolution_task);
			if (logParam != null) {
				logParam.put("taskId", taskId)
					    .put("times", times);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 威龙商城商品兑换记录
	 * 
	 * @param player
	 * @param cfgId	 	兑换商品的配置ID
	 * @param itemId	兑换商品的物品ID
	 * @param itemNum	兑换商品的数量
	 * @param costNum   兑换消耗碎片的数量
	 * @param exchangeTimes 该商品已兑换次数 
	 */
	public static void logFlightPlanExchange(Player player, int cfgId, int itemId, int itemNum, int costNum, int exchangeTimes) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.flight_plan_exchange);
			if (logParam != null) {
				logParam.put("exchangeId", cfgId)
						.put("exchangeCount", itemNum)
					    .put("itemId", itemId)
					    .put("costNum", costNum)
					    .put("exchangeTimes", exchangeTimes);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 攻防模拟战记录报名信息
	 * @param player
	 * @param termId 攻防模拟战的期数
	 * @param guildId 玩家的工会ID
	 * @param wayType 攻防模拟战中的三路 {@link  WayType}
	 * @param optType 1为报名 2为解散.
	 */
	public static void logSimulateWarSign(Player player, int termId, String guildId, WayType wayType, int optType) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.simulate_war_sign);
			if (logParam != null) {
				logParam.put("termId", termId)
					    .put("guildId", guildId)
					    .put("wayType", wayType.getNumber())
					    .put("optType", optType);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录攻防模拟战的助威
	 * @param player
	 * @param termId
	 * @param guildId
	 */
	public static void logSimulateWarEncourage(Player player, int termId, String guildId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.simulate_war_encourage);
			if (logParam != null) {
				logParam.put("termId", termId)
					    .put("guildId", guildId);					    
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 攻防模拟战调整 
	 * @param player
	 * @param termId 期数ID
	 * @param guildId 工会ID
	 */
	public static void logSimulateWarAdjust(Player player, int termId,  String guildId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.simulate_war_adjust);
			if (logParam != null) {
				logParam.put("termId", termId)
					    .put("guildId", guildId);					    
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 指挥官学院礼包购买
	 * @param player  
	 * @param termId   活动期数
	 * @param stageId  活动阶段ID
	 * @param giftId   助力礼包ID
	 */
	public static void logCommandAcademyGiftBuy(Player player, int termId, int stageId,int giftId){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.command_academy_gift_buy);
			if (logParam != null) {
				logParam.put("termId", termId)
					    .put("stageId", stageId)
					    .put("giftId", giftId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 指挥官 学院排名
	 * @param player
	 * @param termId    活动期数
	 * @param stageId   活动阶段ID
	 * @param rankIndex 排行名次
	 */
	public static void logCommandAcademyRank(Player player, int termId, int stageId,int rankIndex){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.command_academy_rank);
			if (logParam != null) {
				logParam.put("termId", termId)
					    .put("stageId", stageId)
					    .put("rankIndex", rankIndex);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 指挥官学院团购人数
	 * @param termId    活动期数
	 * @param stageId   活动阶段ID
	 * @param buyCount  礼包真实购买人数
	 * @param assistCount 注水数量
	 */
	public static void logCommandAcademyBuyCount(int termId,int stageId,int buyCount,int assistCount){
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.command_academy_buy_count);
		    logParam.put("termId", termId)
					.put("stageId", stageId)
					.put("buyCount", buyCount)
					.put("assistCount", assistCount);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
	}
	
	/**
	 * 指挥官学院礼包购买
	 * @param player  
	 * @param termId   活动期数
	 * @param stageId  活动阶段ID
	 * @param giftId   助力礼包ID
	 */
	public static void logCommandAcademySimplifyGiftBuy(Player player, int termId, int stageId,int giftId){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.command_academy_simplify_gift_buy);
			if (logParam != null) {
				logParam.put("termId", termId)
					    .put("stageId", stageId)
					    .put("giftId", giftId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 指挥官 学院排名
	 * @param player
	 * @param termId    活动期数
	 * @param stageId   活动阶段ID
	 * @param rankIndex 排行名次
	 */
	public static void logCommandAcademySimplifyRank(Player player, int termId, int stageId,int rankIndex){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.command_academy_simplify_rank);
			if (logParam != null) {
				logParam.put("termId", termId)
					    .put("stageId", stageId)
					    .put("rankIndex", rankIndex);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 指挥官学院团购人数
	 * @param termId    活动期数
	 * @param stageId   活动阶段ID
	 * @param buyCount  礼包真实购买人数
	 * @param assistCount 注水数量
	 */
	public static void logCommandAcademySimplifyBuyCount(int termId,int stageId,int buyCount,int assistCount){
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.command_academy_simplify_buy_count);
		    logParam.put("termId", termId)
					.put("stageId", stageId)
					.put("buyCount", buyCount)
					.put("assistCount", assistCount);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
	}
	
	/**
	 * 装备黑市 精炼
	 * @param termId
	 * @param refineId 精炼公式ID
	 * @param rcount 精炼次数
	 */
	public static void logEquipBlackMarketRefine(Player player,int termId,int refineId,int count){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.equip_black_market_refine);
			if (logParam != null) {
				logParam.put("termId", termId)
					    .put("refineId", refineId)
					    .put("count", count);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	/**
	 * 全服完成的圣诞任务阶段.
	 * @param taskId 达成圣诞任务的ID
	 * @param num  全服击杀的怪物ID
	 */
	public static void logChristmasTask(int termId, int taskId, int num) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.christmas_task);
		    logParam.put("termId", termId)
					.put("taskId", taskId)
					.put("num", num);
			GameLog.getInstance().info(logParam);			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 圣诞玩家的领奖记录
	 * @param player 领取的玩家
	 * @param taskId 领取的是哪个任务id {@link ChristmasWarTaskCfg#getId()}
	 * @param termId  活动的期数ID
	 */
	public static void logChristmasTaskReceive(Player player, int termId, int taskId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.christmas_task_receive);
			if (logParam != null) {
				logParam.put("termId", termId)
					    .put("taskId", taskId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 圣诞宝箱的领取,占领记录.
	 * @param player
	 * @param termId
	 * @param boxId
	 * @param optType
	 */
	public static void logChristmasBox(Player player, int boxId, int optType) {
		try {
			int termId = WorldChristmasWarService.getInstance().getTermId();
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.christmas_box);
			if (logParam != null) {
				logParam.put("termId", termId)
					    .put("boxId", boxId)
					    .put("optType", optType);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录当前英雄总羁绊数量
	 * @param player
	 * @param collectActNum
	 */
	public static void logHeroCollect(Player player, int collectActNum) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.hero_collect);
			if (logParam != null) {
				logParam.put("collectActNum", collectActNum);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 使用行军表情
	 * 
	 * @param player
	 * @param marchId      行军ID
	 * @param emotionId    表情ID
	 * @param usedOnMassMarch    是否是队员对集结行军使用
	 * @param isFree       是否是免费使用
	 * @param usedOnCity   是否是在城点上使用
	 */
	public static void logMarchEmotionUse(Player player, String marchId, int emotionId, boolean usedOnMassMarch, boolean isFree, boolean usedOnCity) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.march_emotion);
			if (logParam != null) {
				logParam.put("marchId", HawkOSOperator.isEmptyString(marchId) ? "NULL" : marchId)
				.put("emotionId", emotionId)
				.put("isFree", isFree ? 1 : 0)
				.put("usedOnMassMarch", usedOnMassMarch ? 1 : 0)
				.put("usedOnCity", usedOnCity ? 1 : 0);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录泰伯利亚elo积分流水
	 * @param logUnit
	 */
	public static void logTimberiumEloScoreFlow(TWEloScoreLogUnit logUnit) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.tiberium_elo_score);
		    logParam.put("guildId", logUnit.getGuildId())
			.put("termId", logUnit.getTermId())
			.put("scoreBef", logUnit.getScoreBef())
			.put("scoreAft", logUnit.getScoreAft())
			.put("changeNum", logUnit.getChangeNum())
			.put("reason", logUnit.getReason().getNumber());
			GameLog.getInstance().info(logParam);
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * TBLY 战场结果
	 * @param extp
	 * @param winGuild
	 * @param campAScore 红方分数
	 * @param campBScore
	 * @param campABuildScore 建筑得分
	 * @param campBBuildScore
	 * @param campACollectScore 采集得分
	 * @param campBCollectScore
	 * @param campABomShot 红方导弹发射数
	 * @param campBBomShot
	 */
	public static void logTBLYResult(TBLYBilingInformationMsg extp, String winGuild , long campAScore, long campBScore,long campABuildScore,long campBBuildScore,long campACollectScore,long campBCollectScore, int campABomShot, int campBBomShot,int extryHonorA, int extryHonorB, int firstControlHonorA, int firstControlHonorB){
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.tiberium_battle_score);
		    logParam.put("gameId", extp.getRoomId())
					.put("isLeaguaWar", extp.isLeaguaWar() ? 1 : 0)
					.put("season", extp.getSeason())
					.put("winGuild", winGuild)
					.put("campAGuild", extp.getCampAGuild())
					.put("campBGuild", extp.getCampBGuild())
					
					.put("campABuildScore", campABuildScore)
					.put("campBBuildScore", campBBuildScore)
					
					.put("campACollectScore", campACollectScore)
					.put("campBCollectScore", campBCollectScore)
					
					.put("campAPlayerCnt", extp.getCampAPlayerCnt())
					.put("campBPlayerCnt", extp.getCampBPlayerCnt())
					.put("campAScore", campAScore)
					.put("campBScore", campBScore)
					.put("campABomShot", campABomShot)
					.put("campBBomShot", campBBomShot)
					.put("extryHonorA", extryHonorA)
					.put("extryHonorB", extryHonorB)
					.put("firstControlHonorA", firstControlHonorA)
					.put("firstControlHonorB", firstControlHonorB)
					;
			GameLog.getInstance().info(logParam);
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 发射核弹
	 * @param extp
	 * @param guildId 发射方
	 */
	public static void logTBLYNuclearShot(TBLYExtraParam extp, String guildId ){
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.tiberium_nuclear_shot);
		    logParam.put("gameId", extp.getBattleId())
					.put("isLeaguaWar", extp.isLeaguaWar() ? 1 : 0)
					.put("season", extp.getSeason())
					.put("guildId", guildId);
			GameLog.getInstance().info(logParam);
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 击杀机甲
	 * @param extp
	 * @param guildId 击杀方
	 */
	public static void logTBLYNianKill(TBLYExtraParam extp, String guildId ){
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.tiberium_nian_kill);
		    logParam.put("gameId", extp.getBattleId())
					.put("isLeaguaWar", extp.isLeaguaWar() ? 1 : 0)
					.put("season", extp.getSeason())
					.put("guildId", guildId);
			GameLog.getInstance().info(logParam);
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * TBLY 建筑得分, 控制时间 
	 * @param extp
	 * @param campAScore 从建筑中获取的积分
	 * @param campBScore
	 * @param campAControl 控制时长(秒)
	 * @param campBControl
	 * @param buildPos 建筑坐标
	 */
	public static void logTBLYBuilding(TBLYBilingInformationMsg extp, int buildPos, int campAScore, int campBScore, int campAControl, int campBControl){
		try {
			int[] p = GameUtil.splitXAndY(buildPos);
			LogParam logParam = getNonPersonalLogParam(LogInfoType.tiberium_build_score);
		    logParam.put("gameId", extp.getRoomId())
					.put("isLeaguaWar", extp.isLeaguaWar() ? 1 : 0)
					.put("season", extp.getSeason())
					.put("campAGuild", extp.getCampAGuild())
					.put("campBGuild", extp.getCampBGuild())
					.put("campAScore", campAScore)
					.put("campBScore", campBScore)
					.put("campAControl", campAControl)
					.put("campBControl", campBControl)
					.put("buildPos", Arrays.toString(p));
			GameLog.getInstance().info(logParam);
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 雪球大战进球
	 * @param player
	 * @param posX 坐标x
	 * @param posY 坐标y
	 */
	public static void logSnowballGoal(Player player, int posX, int posY) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.snowball_goal);
			if (logParam != null) {
				logParam.put("posX", posX).put("posY", posY);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/** cyborg 建筑控制流水 
	 * @param begin 攻占
	 * @param control 完全控制
	 * @param over 失去控制
	 */
	public static void logCYBORGBuildControl(String roomId, String guildId, String guildName, long begin, long control, long over) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.cyborg_build_control);
		    logParam.put("roomId", roomId)
			 .put("guildId", guildId)
			 .put("guildName", guildName)
			 .put("begin", begin)
			 .put("control", control)
			 .put("over", over);
			GameLog.getInstance().info(logParam);

		} catch (Exception e) {
			HawkException.catchException(e);
		}

	}
	
	/**
	 * cyborg 导弹伤害 
	 * @param roomId
	 * @param ptype
	 * @param x 目标点
	 * @param y
	 * @param guildId 攻方盟id
	 * @param guildName
	 * @param tguildId
	 * @param tguildName
	 * @param killStr sid_num
	 */
	public static void logCYBORGNuclearHit(String roomId, String ptype, int pointx, int pointy, String guildId, String guildName, String tguildId, String tguildName,
			String killStr) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.cyborg_nuclear_hit);
		    logParam.put("roomId", roomId)
					.put("ptype", ptype)
					.put("pointx", pointx)
					.put("pointy", pointy)
					.put("guildId", guildId)
					.put("guildName", guildName)
					.put("tguildId", tguildId)
					.put("tguildName", tguildName)
					.put("killStr", killStr);
			GameLog.getInstance().info(logParam);

		} catch (Exception e) {
			HawkException.catchException(e);
		}

	}
	
	/**
	 * 赛博之战报名信息
	 * @param teamId
	 * @param teamName
	 * @param guildId
	 * @param guildName
	 * @param memberCnt
	 * @param totalPowar
	 * @param serverId
	 * @param termId
	 * @param timeIndex
	 * @param playerId
	 */
	public static void logCyborgSignUp(String teamId, String teamName, String guildId, String guildName, int memberCnt, long totalPowar, String serverId, int termId, int timeIndex, String playerId) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.cyborg_sign_up);
			logParam.put("teamId", teamId)
					.put("teamName", teamName)
					.put("guildId", guildId)
					.put("guildName", guildName)
					.put("memberCnt", memberCnt)
					.put("totalPowar", totalPowar)
					.put("serverId", serverId)
					.put("termId", termId)
					.put("timeIndex", timeIndex)
					.put("playerId", playerId);
			GameLog.getInstance().info(logParam);

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 赛博之战匹配战场信息
	 * @param roomId
	 * @param roomServer
	 * @param termId
	 * @param timeIndex
	 * @param teamA
	 * @param guildA
	 * @param serverA
	 * @param teamB
	 * @param guildB
	 * @param serverB
	 * @param teamC
	 * @param guildC
	 * @param serverC
	 * @param teamD
	 * @param guildD
	 * @param serverD
	 */
	public static void logCyborgMatchRoom(String roomId, String roomServer, int termId, int timeIndex, String teamA, long teamPowerA, String guildA, String serverA, String teamB, long teamPowerB, String guildB,
			String serverB, String teamC, long teamPowerC, String guildC, String serverC, String teamD, long teamPowerD, String guildD, String serverD) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.cyborg_match_room);
		    logParam.put("roomId", roomId)
					.put("roomServer", roomServer)
					.put("termId", termId)
					.put("timeIndex", timeIndex)
					.put("teamA", teamA)
					.put("teamPowerA", teamPowerA)
					.put("guildA", guildA)
					.put("serverA", serverA)
					.put("teamB", teamB)
					.put("teamPowerB", teamPowerB)
					.put("guildB", guildB)
					.put("serverB", serverB)
					.put("teamC", teamC)
					.put("teamPowerC", teamPowerC)
					.put("guildC", guildC)
					.put("serverC", serverC)
					.put("teamD", teamD)
					.put("teamPowerD", teamPowerD)
					.put("guildD", guildD)
					.put("serverD", serverD);
			GameLog.getInstance().info(logParam);

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	/**
	 * 赛博之战匹配战队信息
	 * @param teamId
	 * @param teamName
	 * @param guildId
	 * @param guildName
	 * @param serverId
	 * @param memberCnt
	 * @param totalPowar
	 * @param termId
	 * @param timeIndex
	 */
	public static void logCyborgMatchTeam(String teamId, String teamName, String guildId, String guildName, String serverId, int memberCnt, long totalPowar, int termId, int timeIndex,long matchPower) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.cyborg_match_team);
			logParam.put("teamId", teamId)
					.put("teamName", teamName)
					.put("guildId", guildId)
					.put("guildName", guildName)
					.put("serverId", serverId)
					.put("memberCnt", memberCnt)
					.put("totalPowar", totalPowar)
					.put("termId", termId)
					.put("timeIndex", timeIndex)
					.put("matchPower", matchPower);
			GameLog.getInstance().info(logParam);

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 赛博之战匹配玩家信息
	 * @param playerId
	 * @param termId
	 * @param teamId
	 * @param guildId
	 * @param serverId
	 * @param power
	 */
	public static void logCyborgMatchPlayer(String playerId, int termId, String teamId, String guildId, String serverId, long power) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.cyborg_match_player);
		    logParam.put("playerId", playerId)
					.put("termId", termId)
					.put("teamId", teamId)
					.put("guildId", guildId)
					.put("serverId", serverId)
					.put("power", power);
			GameLog.getInstance().info(logParam);

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 赛博之战进入战场信息
	 * @param playerId
	 * @param termId
	 * @param roomId
	 * @param roomServer
	 * @param teamId
	 * @param guildId
	 * @param serverId
	 * @param power
	 */
	public static void logCyborgEnterRoom(String playerId, int termId, String roomId, String roomServer, String teamId, String guildId, String serverId, long power,long matchPower) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.cyborg_enter_room);
		    logParam.put("playerId", playerId)
					.put("termId", termId)
					.put("roomId", roomId)
					.put("roomServer", roomServer)
					.put("teamId", teamId)
					.put("guildId", guildId)
					.put("serverId", serverId)
					.put("power", power)
					.put("matchPower", matchPower);
			GameLog.getInstance().info(logParam);

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 赛博之战退出战场信息
	 * @param playerId
	 * @param termId
	 * @param teamId
	 * @param guildId
	 * @param isMidaway
	 */
	public static void logCyborgQuitRoom(String playerId, int termId, String teamId, String guildId, boolean isMidaway) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.cyborg_quit_room);
		    logParam.put("playerId", playerId)
					.put("termId", termId)
					.put("teamId", teamId)
					.put("guildId", guildId)
					.put("isMidaway", isMidaway ? 1 : 0);
			GameLog.getInstance().info(logParam);

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 赛博之战个人积分记录
	 * @param playerId
	 * @param termId
	 * @param teamId
	 * @param guildId
	 * @param score
	 * @param teamRank
	 */
	public static void logCyborgPlayerScore(String playerId, int termId, String teamId, String guildId, long score, int teamRank) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.cyborg_player_score);
		    logParam.put("playerId", playerId)
					.put("termId", termId)
					.put("teamId", teamId)
					.put("guildId", guildId)
					.put("score", score)
					.put("teamRank", teamRank);
			GameLog.getInstance().info(logParam);

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 赛博之战战队积分记录
	 * @param teamId
	 * @param teamName
	 * @param guildId
	 * @param termId
	 * @param serverId
	 * @param roomId
	 * @param roomServer
	 * @param score
	 * @param teamRank
	 */
	public static void logCyborgTeamScore(String teamId, String teamName, String guildId, int termId, String serverId, String roomId, String roomServer, long score, int teamRank) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.cyborg_team_score);
		    logParam.put("teamId", teamId)
					.put("teamName", teamName)
					.put("guildId", guildId)
					.put("termId", termId)
					.put("serverId", serverId)
					.put("roomId", roomId)
					.put("roomServer", roomServer)
					.put("score", score)
					.put("teamRank", teamRank);
			GameLog.getInstance().info(logParam);

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/** 机甲被击杀*/
	public static void logCYBORGNianKill(String roomId, String guildId, String guildName) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.cyborg_nian_kill);
		    logParam.put("roomId", roomId)
					.put("guildId", guildId)
					.put("guildName", guildName);
			GameLog.getInstance().info(logParam);

		} catch (Exception e) {
			HawkException.catchException(e);
		}

	}


	
	/**
	 * 资源保卫战 建造资源站
	 * @param player
	 * @param resType 资源站类型
	 */
	public static void logResourceDefenseBuild(Player player, int resType) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.resource_defense_build);
			if (logParam != null) {
				logParam.put("resType", resType);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 资源保卫战偷取
	 * @param player
	 * @param targetId 目标玩家id
	 */
	public static void logResourceDefenseSteal(Player player, String targetId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.resource_defense_steal);
			if (logParam != null) {
				logParam.put("targetId", targetId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 资源保卫战获取经验
	 * @param player
	 * @param addExp 增加经验
	 * @param afterExp 增加过后玩家总经验
	 * @param afterLevel 增加经验过后玩家等级
	 */
	public static void logResourceDefenseExp(Player player, int addExp, int afterExp, int afterLevel) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.resource_defense_exp);
			if (logParam != null) {
				logParam.put("addExp", addExp)
						.put("afterExp", afterExp)
						.put("afterLevel", afterLevel);;
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	

	/***
	 * 时空豪礼 成就任务完成
	 * @param player
	 * @param achieveId 成就ID
	 */
	public static void logChronoGiftTaskFinish(Player player,int termId,int taskId){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.chrono_gift_task_finish);
			if (logParam != null) {
				logParam.put("termId", termId); 
				logParam.put("taskId", taskId); 
				GameLog.getInstance().info(logParam);
			}
		
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/***
	 * 时空豪礼 成就任务完成
	 * @param player
	 * @param achieveId 成就ID
	 */
	public static void logChronoGiftUnlock(Player player,int termId,int giftId){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.chrono_gift_unlock);
			if (logParam != null) {
				logParam.put("termId", termId); 
				logParam.put("giftId", giftId); 
				GameLog.getInstance().info(logParam);
			}
		
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/***
	 * 时空豪礼 成就任务完成
	 * @param player
	 * @param achieveId 成就ID
	 */
	public static void logChronoGiftFreeAwardAchieve(Player player,int termId,int giftId){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.chrono_gift_achieve_free_award);
			if (logParam != null) {
				logParam.put("termId", termId); 
				logParam.put("giftId", giftId); 
				GameLog.getInstance().info(logParam);
			}
		
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 更换装扮外观打点记录
	 * 
	 * @param player
	 * @param dressType
	 * @param modelType
	 * @param showType
	 * @param showEndTime
	 */
	public static void logDressShowChange(Player player, int dressType, int modelType, int showType, int showEndTime) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.dress_show_change);
			if (logParam != null) {
				logParam.put("dressType", dressType) 
				        .put("modelType", modelType)
				        .put("showType", showType)
				        .put("showEndTime", showEndTime); 
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}


	/**
	 * 神话外观激活
	 * @param player
	 * @param dressId
	 * @param dressType
	 * @param modelType
	 * @param showStartTime
	 * @param showEndTime
	 */
	public static void logDressGodChange(Player player, int dressId, int dressType, int modelType, long showStartTime, long showEndTime) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.dress_god_change);
			if (logParam != null) {
				logParam.put("dressId", dressId)
						.put("dressType", dressType)
						.put("modelType", modelType)
						.put("showStartTime", showStartTime)
						.put("showEndTime", showEndTime);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 充值基金投资流水
	 * @param player
	 * @param termId
	 * @param giftId
	 */
	public static void logRechargeFundInvest(Player player, int termId, int giftId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.recharge_fund_invest);
			if (logParam != null) {
				logParam.put("termId", termId) 
				        .put("giftId", giftId); 
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 充值基金充值解锁信息
	 * @param player
	 * @param termId
	 * @param rechargeGold
	 * @param rechargeBef
	 * @param rechargeAft
	 * @param unlockCnt
	 */
	public static void logRechargeFundRecharge(Player player, int termId, int rechargeGold, int rechargeBef, int rechargeAft, int unlockCnt) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.recharge_fund_recharge);
			if (logParam != null) {
				logParam.put("termId", termId) 
				        .put("rechargeGold", rechargeGold)
				        .put("rechargeBef", rechargeBef)
				        .put("rechargeAft", rechargeAft)
				        .put("unlockCnt", unlockCnt); 
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 充值基金领奖
	 * @param player
	 * @param termId
	 * @param giftId
	 * @param rewardId
	 */
	public static void logRechargeFundReward(Player player, int termId, int giftId, int rewardId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.recharge_fund_reward);
			if (logParam != null) {
				logParam.put("termId", termId) 
				        .put("giftId", giftId)
				        .put("rewardId", rewardId); 
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 联盟商店补货流水
	 * @param player
	 * @param guildId
	 * @param itemId
	 * @param itemCnt
	 * @param costScore
	 * @param scoreBef
	 * @param scoreAft
	 */
	public static void logGuildShopAdd(Player player, String guildId, int itemId, int itemCnt, int costScore, long scoreBef, long scoreAft) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.guild_shop_add);
			if (logParam != null) {
				logParam.put("guildId", guildId) 
				        .put("itemId", itemId)
				        .put("itemCnt", itemCnt)
				        .put("costScore", costScore)
				        .put("scoreBef", scoreBef)
				        .put("scoreAft", scoreAft); 
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * SS装备属性改变日志
	 * 
	 * @param player 玩家
	 * @param armourCfgId 装备配置id
	 * @param level 等级
	 * @param quality 品质
	 * @param power 战力
	 * @param suit 套装
	 * @param extraAttr 额外属性
	 * @param skillAttr 特技属性
	 * @param reason 变化原因
	 */
	public static void logArmourChange(Player player, int armourCfgId, int level, int quality, int star, int quantum, int power, 
			String suit, String extraAttr, String skillAttr, String starAttr, int reason, String uuid) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.armour_change);
		if (logParam != null) {
			logParam.put("armourCfgId", armourCfgId)
					.put("level", level)
					.put("quality", quality)
					.put("star", star)
					.put("quantum", quantum)
					.put("power", power)
					.put("suit", suit)
					.put("extraAttr", extraAttr)
					.put("skillAttr", skillAttr)
					.put("starAttr", starAttr)
					.put("reason", reason)
					.put("armourUuid", uuid == null ? "" : uuid);
			GameLog.getInstance().info(logParam);
		}
	}
	
	/**
	 * 回归有礼抽奖记录
	 * @param player
	 * @param termId
	 * @param backCount
	 * @param isFree
	 * @param lotteryCount
	 */
	public static void logBackGiftLottery(Player player,int termId,int backCount,int isFree,int lotteryCount){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.back_gift_lottery);
			if (logParam != null) {
				logParam.put("termId", termId); 
				logParam.put("backCount", backCount); 
				logParam.put("isFree", isFree); 
				logParam.put("lotteryCount", lotteryCount); 
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 体力赠送，消息数量
	 * @param player
	 * @param termId
	 * @param backCount
	 * @param messageCount
	 * @param messageTotalCount
	 */
	public static void logPowerSendMessageCount(Player player,int termId,int backCount,int messageCount,int messageTotalCount){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.power_send_message_send);
			if (logParam != null) {
				logParam.put("termId", termId); 
				logParam.put("backCount", backCount); 
				logParam.put("messageCount", messageCount); 
				logParam.put("messageTotalCount", messageTotalCount); 
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 赛博之战赛季联盟积分记录
	 * @param guildId 联盟id
	 * @param teamId 战队id
	 * @param season 赛季
	 * @param termId 期数
	 * @param serverId 服务器id
	 * @param scoreChange 积分变化量
	 * @param score 变化后积分
	 */
	public static void logCyborgSeasonGuildScore(String guildId, String teamId, int season, int termId, String serverId, int scoreChange, long score) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.cyborg_season_guild_score);
		    logParam.put("guildId", guildId)
					.put("teamId", teamId)
					.put("season", season)
					.put("termId", termId)
					.put("serverId", serverId)
					.put("scoreChange", scoreChange)
					.put("score", score);
			GameLog.getInstance().info(logParam);

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 赛博之战赛季段位流水
	 * @param guildId 联盟id
	 * @param teamId 战队id
	 * @param season 赛季
	 * @param termId 期数
	 * @param serverId 服务器id
	 * @param starChange 积分变化量
	 * @param star 变化后段位
	 * @param reason 段位变化原因:1 赛季初初始化 2 赛季中初始化  3赛博战斗 4消极出战扣星 5匹配失败加星
	 * @param param 补充参数
	 */
	public static void logCyborgSeasonStarFlow(String guildId, String teamId, int season, int termId, String serverId, int starChange, int star, int reason, int param) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.cyborg_season_star);
			logParam.put("guildId", guildId)
					.put("teamId", teamId)
					.put("season", season)
					.put("termId", termId)
					.put("serverId", serverId)
					.put("starChange", starChange)
					.put("star", star)
					.put("reason", reason)
					.put("param", param);
			GameLog.getInstance().info(logParam);

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 赛博赛季发最后排位奖励
	 * @param guildId
	 * @param teamId
	 * @param season
	 * @param termId
	 * @param serverId
	 * @param rank
	 * @param playerId
	 */
	public static void logCyborgSeasonRankAward(String guildId, String teamId, int season, String serverId,int star, int rank,String playerId) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.cyborg_season_team_rank_award);
			logParam.put("guildId", guildId)
					.put("teamId", teamId)
					.put("season", season)
					.put("serverId", serverId)
					.put("star", star)
					.put("rank", rank)
					.put("playerId", playerId);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 赛博赛季结束段位奖励
	 * @param guildId
	 * @param teamId
	 * @param season
	 * @param serverId
	 * @param star
	 * @param division
	 * @param playerId
	 */
	public static void logCyborgSeasonStarAward(String guildId, String teamId, int season, String serverId,int star, int division,String playerId) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.cyborg_season_team_star_award);
			logParam.put("guildId", guildId)
					.put("teamId", teamId)
					.put("season", season)
					.put("serverId", serverId)
					.put("star", star)
					.put("division", division)
					.put("playerId", playerId);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 免费兑换装扮
	 * @param player
	 * @param termId
	 * @param addExp
	 * @param afterLevel
	 * @param afterExp
	 */
	public static void logExchangeDecorateLevel(Player player,int termId,int addExp,int afterLevel,int afterExp){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.exchange_decorate_level);
			if (logParam != null) {
				logParam.put("termId", termId); 
				logParam.put("addExp", addExp); 
				logParam.put("afterLevel", afterLevel); 
				logParam.put("afterExp", afterExp); 
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}	
	/**
	 * 免费兑换装扮任务
	 * @param player
	 * @param termId
	 * @param addExp
	 * @param afterLevel
	 * @param afterExp
	 */
	public static void logExchangeDecorateMission(Player player,int termId,int missionId){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.exchange_decorate_mission);
			if (logParam != null) {
				logParam.put("termId", termId); 
				logParam.put("missionId", missionId); 
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	public static void logGhostSecretDrewResult(Player player,int termId, int drewValue){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.ghost_secret_drew_result);
			if (logParam != null) {
				logParam.put("termId", termId); 
				logParam.put("drewValue", drewValue); 
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}	
	
	public static void logGhostSecretResetInfo(Player player,int termId, int drewedTimes){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.ghost_secret_reset_info);
			if (logParam != null) {
				logParam.put("termId", termId); 
				logParam.put("drewedTimes", drewedTimes); 
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}	
	
	public static void logGhostSecretRewardInfo(Player player,int termId, int rewardId){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.ghost_secret_reward_info);
			if (logParam != null) {
				logParam.put("termId", termId); 
				logParam.put("rewardId", rewardId); 
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}	
	/**
	 * 能源滚滚个人积分流水
	 * @param player
	 * @param termId 活动期数
	 * @param addScore 积分增量
	 * @param addType 积分来源 1召唤  2打怪
	 * @param afterScore
	 */
	public static void logEnergiesSelfScore(Player player, int termId, int addScore, int addType, int afterScore) {
		try {
			if (!GsConfig.getInstance().isTlogEnable()) {
				return;
			}
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.energies_self_score);
			if (logParam != null) {
				logParam.put("termId", termId) 
						.put("addScore", addScore)
						.put("addType", addType) 
						.put("afterScore", afterScore); 
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}	
	/**
	 * 能源滚滚联盟积分流水
	 * @param guildId
	 * @param termId 活动期数
	 * @param addScore 积分增量
	 * @param addType 积分来源 1召唤  2打怪
	 * @param afterScore
	 */
	public static void logEnergiesGuildScore(String guildId, int termId, int addScore, int addType, long afterScore) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.energies_guild_score);
			logParam.put("guildId", guildId)
					.put("termId", termId)
					.put("addScore", addScore)
					.put("addType", addType)
					.put("afterScore", afterScore);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}	
	
	/**
	 * 能源滚滚积分排行
	 * @param termId
	 * @param rankType 1-个人,2-联盟
	 * @param rankerId 排行id
	 * @param rank 排名
	 * @param score 积分
	 */
	public static void logEnergiesRank(int termId, int rankType, String rankerId, int rank, long score) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.energies_rank);
			logParam.put("termId", termId)
					.put("rankType", rankType)
					.put("rankerId", rankerId)
					.put("rank", rank)
					.put("score", score);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}	

	/** 虚拟实验室开拍
	 * @param player
	 * @param termId
	 * @param cardIndex  牌的位置
	 * @param cardValue	牌值
	 */
	public static void logVirtualLaboratoryOpenCard(Player player, int termId, int cardIndex, int cardIndexTwo, int cardValue) {
		try {
			if (!GsConfig.getInstance().isTlogEnable()) {
				return;
			}
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.virtual_laboratory_open);
			if (logParam != null) {
				logParam.put("termId", termId) 
						.put("cardIndex", cardIndex)
						.put("cardIndexTwo", cardIndexTwo)
						.put("cardValue", cardValue) ; 
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}	

	/**
	 * 装备科技升级
	 * 
	 * researchId：装备科技id
	 * level: 等级
	 */
	public static void logEquipResearchLevelUp(Player player, int researchId, int level) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.equip_research_level_up);
			if (logParam != null) {
				logParam.put("researchId", researchId); 
				logParam.put("level", level); 
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}		
	}

	
	/**
	 * 端午-联盟庆典升级发奖
	 * @param termId
	 * @param guildId
	 * @param level
	 * @param players
	 */
	public static void logDragonBoatCelebrateionLevelReward(int termId, String guildId,int level,String players) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.dragon_boat_celebration_level_reward);
			logParam.put("termId", termId)
					.put("guildId", guildId)
					.put("guildLevel",level )
					.put("players",players);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 端午-联盟庆典贡献经验
	 * @param player
	 * @param termId
	 * @param guildId
	 * @param donateType
	 * @param donateExp
	 */
	public static void logDragonBoatCelebrationDonate(Player player, int termId,String guildId, int donateType, int donateExp,int totalExp ) {
		try {
			if (!GsConfig.getInstance().isTlogEnable()) {
				return;
			}
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.dragon_boat_celebration_donate);
			if (logParam != null) {
				logParam.put("termId", termId) 
						.put("guildId", guildId)
						.put("donateType", donateType) 
						.put("donateExp", donateExp)
						.put("totalExp", totalExp); 
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 端午-道具兑换
	 * @param player
	 * @param termId
	 * @param exchangeId
	 * @param exchangeCount
	 */
	public static void logDragonBoatExchange(Player player, int termId, int exchangeId, int exchangeCount) {
		try {
			if (!GsConfig.getInstance().isTlogEnable()) {
				return;
			}
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.dragon_boat_exchange);
			if (logParam != null) {
				logParam.put("termId", termId) 
						.put("exchangeId", exchangeId)
						.put("exchangeCount", exchangeCount);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 端午-龙船送礼
	 * @param player
	 * @param termId
	 * @param type 1前往    2到达领奖
	 * @param boatId
	 */
	public static void logDragonBoatGiftAchieve(Player player, int termId, int type, long boatId) {
		try {
			if (!GsConfig.getInstance().isTlogEnable()) {
				return;
			}
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.dragon_boat_gift_achieve);
			if (logParam != null) {
				logParam.put("termId", termId) 
						.put("type", type)
						.put("boatId", boatId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 端午-打开福袋
	 * @param player
	 * @param termId
	 * @param openCount
	 */
	public static void logDragonBoatLuckyBagOpen(Player player, int termId, int openCount) {
		try {
			if (!GsConfig.getInstance().isTlogEnable()) {
				return;
			}
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.dragon_boat_lucky_bag_open);
			if (logParam != null) {
				logParam.put("termId", termId) 
						.put("openCount",openCount);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 端午-连续充值天数
	 * @param player
	 * @param termId
	 * @param days
	 */
	public static void logDragonBoatRechargeDays(Player player, int termId, int days) {
		try {
			if (!GsConfig.getInstance().isTlogEnable()) {
				return;
			}
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.dragon_boat_recharge_days);
			if (logParam != null) {
				logParam.put("termId", termId) 
						.put("days",days);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/** 虚拟实验室开拍
	 * @param player
	 * @param termId
	 * @param buyId  购买的勋章基金Id
	 * @param scoreInfo	积分详情 0:300|1:500  第一天300分,第二天500分  本活动的第几天
	 */
	public static void logMedalFundRewardScoreInfo(Player player, int termId, int buyId, String scoreInfo, int type) {
		try {
			if (!GsConfig.getInstance().isTlogEnable()) {
				return;
			}
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.medal_fund_reward_score);
			if (logParam != null) {
				logParam.put("termId", termId) 
						.put("buyId", buyId)
						.put("scoreInfo", scoreInfo)
						.put("type", type);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 超能核心操作
	 * @param player
	 * @param opration 1 随机 2 保存随机结果
	 * @param randomBlock 操作1随机结果
	 * @param powerCore 当前核心
	 * @param powerBlock 当前魔方
	 */
	public static void lotLaboratoryOP(Player player, String opration, String randomBlock, String powerCore, String powerBlock) {
		try {
			if (!GsConfig.getInstance().isTlogEnable()) {
				return;
			}
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.laboratory_op);
			if (logParam != null) {
				logParam.put("opration", opration) 
						.put("randomBlock", randomBlock)
						.put("powerCore", powerCore)
						.put("powerBlock", powerBlock);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}	
	
	

	
	/**
	 * 沙场点兵，翻雕像
	 * @param player
	 * @param termId
	 * @param stage 	
	 * @param quality 雕像品质ID
	 */
	public static void logArmiesMassOpenSculpture(Player player, int termId, int stage, int quality) {
		try {
			if (!GsConfig.getInstance().isTlogEnable()) {
				return;
			}
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.armies_mass_open_sculpture);
			if (logParam != null) {
				logParam.put("termId", termId) 
						.put("stage", stage)
						.put("quality", quality);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}	
	
	
	
	/** 能量源投资奖励记录
	 * @param player
	 * @param termId
	 * @param buyId  购买的勋章基金Id
	 * @param scoreInfo	积分详情 0:300|1:500  第一天300分,第二天500分  本活动的第几天
	 */
	public static void logEnergyInvestRewardScoreInfo(Player player, int termId, int buyId, String scoreInfo) {
		try {
			if (!GsConfig.getInstance().isTlogEnable()) {
				return;
			}
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.energy_invest_reward_score);
			if (logParam != null) {
				logParam.put("termId", termId) 
						.put("buyId", buyId)
						.put("scoreInfo", scoreInfo);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}	
	
	
	/** 机甲投资奖励记录
	 * @param player
	 * @param termId
	 * @param buyId  购买的勋章基金Id
	 * @param scoreInfo	积分详情 0:300|1:500  第一天300分,第二天500分  本活动的第几天
	 */
	public static void logSupersoldierInvestRewardScoreInfo(Player player, int termId, int buyId, String scoreInfo) {
		try {
			if (!GsConfig.getInstance().isTlogEnable()) {
				return;
			}
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.supersoldier_invest_reward_score);
			if (logParam != null) {
				logParam.put("termId", termId) 
						.put("buyId", buyId)
						.put("scoreInfo", scoreInfo);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}	
	
	
	/**霸主膜拜记录信息
	 * @param player
	 * @param termId
	 * @param blessRealNum 膜拜真实次数
	 * @param blessWaterNum 膜拜注水次数
	 */
	public static void logOverlordBlessingInfo(Player player, int termId, long blessRealNum, long blessWaterNum) {
		try {
			if (!GsConfig.getInstance().isTlogEnable()) {
				return;
			}
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.overlord_blessing_info);
			if (logParam != null) {
				logParam.put("termId", termId) 
						.put("blessRealNum", blessRealNum)
						.put("blessWaterNum", blessWaterNum);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}	
	
	
	
	
	
	/**
	 * 新服战令-战令经验购买
	 * @param player
	 * @param termId
	 * @param expId  经验道具ID
	 * @param exp   增加经验值
	 */
	public static void logNewBuyOrderExp(Player player, int termId, int expId, int exp) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.new_order_exp_buy);
			if (logParam != null) {
				logParam.put("termId", termId)
				.put("expId", expId)
				.put("exp", exp);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 新服战令-进阶直购
	 * @param player
	 * @param termId
	 * @param authId  进阶礼包ID
	 */
	public static void logNewBuyOrderAuth(Player player, int termId, int authId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.new_order_auth_buy);
			if (logParam != null) {
				logParam.put("termId", termId)
				.put("authId", authId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 新服战令-经验流水
	 * @param player
	 * @param termId
	 * @param expAdd    增加经验值
	 * @param totalExp  总经验值
	 * @param exp       当前等级经验值
	 * @param level		当前等级
	 * @param reason	变更来源:0-初始化,1-完成任务,2-进阶,3-购买经验
	 * @param reasonId	来源id: 任务id/进阶礼包id/经验id
	 */
	 
	public static void logNewOrderExpChange(Player player, int termId, int expAdd, int totalExp,int exp, int level, int reason, int reasonId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.new_order_exp_flow);
			if (logParam != null) {
				logParam.put("termId", termId)
				.put("expAdd", expAdd)
				.put("totalExp", totalExp)
				.put("exp", exp)
				.put("level", level)
				.put("reason", reason)
				.put("reasonId", reasonId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 新服战令-任务完成情况
	 * @param player
	 * @param termId
	 * @param orderId		新服战令任务ID
	 * @param addTimes		新完成次数
	 * @param finishTimes   总完成次数
	 */
	public static void logNewOrderFinishId(Player player, int termId, int orderId, int addTimes, int finishTimes){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.new_order_flow);
			if (logParam != null) {
				logParam.put("termId", termId)
				.put("orderId", orderId)
				.put("addTimes", addTimes)
				.put("finishTimes", finishTimes);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}


	/**
	 * 赛季战令-进阶直购
	 * @param player
	 * @param termId
	 * @param authId  进阶礼包ID
	 */
	public static void logSeasonBuyOrderAuth(Player player, int termId, int authId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.season_order_auth_buy);
			if (logParam != null) {
				logParam.put("termId", termId)
						.put("authId", authId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 赛季战令-经验流水
	 * @param player
	 * @param termId
	 * @param expAdd    增加经验值
	 * @param exp       当前等级经验值
	 * @param oldLevel  老等级
	 * @param level		当前等级
	 * @param reason	变更来源:0-初始化,1-完成任务,2-进阶,3-购买经验
	 * @param reasonId	来源id: 任务id/进阶礼包id/经验id
	 */

	public static void logSeasonOrderExpChange(Player player, int termId, int expAdd, int exp,int oldLevel, int level, int reason, int reasonId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.season_order_exp_flow);
			if (logParam != null) {
				logParam.put("termId", termId)
						.put("expAdd", expAdd)
						.put("exp", exp)
						.put("oldLevel", oldLevel)
						.put("level", level)
						.put("reason", reason)
						.put("reasonId", reasonId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 赛季战令-任务完成情况
	 * @param player
	 * @param termId
	 * @param orderId		新服战令任务ID
	 * @param addTimes		新完成次数
	 * @param finishTimes   总完成次数
	 */
	public static void logSeasonOrderFinishId(Player player, int termId, int orderId, int addTimes, int finishTimes){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.season_order_flow);
			if (logParam != null) {
				logParam.put("termId", termId)
						.put("orderId", orderId)
						.put("addTimes", addTimes)
						.put("finishTimes", finishTimes);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**双享豪礼参与购买打点
	 * @param player
	 * @param termId
	 * @param giftId
	 * @param rewardId
	 */
	public static void logDoubleGiftBuy(Player player, int termId, int giftId, int rewardId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.double_gift_buy);
			if (logParam != null) {
				logParam.put("termId", termId)
						.put("giftId", giftId)
						.put("rewardId", rewardId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}	
	}
	
	/**
	 * 中控飞堡记录
	 * 
	 * @param player
	 */
	public static void logZKRemoveCity(Player player) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.zk_remove_city);
			if (logParam != null) {
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}	
	}
	
	/**团购礼包购买打点
	 * @param player
	 * @param termId
	 * @param giftId
	 * @param rewardId
	 * @param realTimes
	 * @param waterTimes
	 */
	public static void logGroupBuy(Player player, int termId, int giftId, int rewardId, long realTimes, long waterTimes) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.group_buy);
			if (logParam != null) {
				logParam.put("termId", termId)
						.put("giftId", giftId)
						.put("rewardId", rewardId)
						.put("realTimes", realTimes)
						.put("waterTimes", waterTimes);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}	
	}
	
	
	/**
	 * 幽灵工厂行军记录
	 * @param player
	 * @param ghostId
	 * @param state
	 */
	public static void logGhostTowerAttack(Player player, int ghostId, int state) {
		try {
			if (!GsConfig.getInstance().isTlogEnable()) {
				return;
			}
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.ghost_tower_attack);
			if (logParam != null) {
				logParam.put("ghostId", ghostId) 
						.put("rlt",state);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 幽灵工厂挑战奖励发放
	 * @param player
	 * @param ghostId
	 * @param state
	 */
	public static void logGhostAwardSend(Player player, int ghostId, String reward) {
		try {
			if (!GsConfig.getInstance().isTlogEnable()) {
				return;
			}
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.ghost_tower_reward_email);
			if (logParam != null) {
				logParam.put("ghostId", ghostId) 
						.put("reward",reward);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	
	/**
	 * 周年庆-蛋糕分享
	 * @param player
	 * @param termId
	 * @param type 1前往    2到达领奖
	 * @param cakeId
	 */
	public static void logCakeShareRewardAchieve(Player player, int termId, int type, long cakeId) {
		try {
			if (!GsConfig.getInstance().isTlogEnable()) {
				return;
			}
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.cake_share_reward_achieve);
			if (logParam != null) {
				logParam.put("termId", termId) 
						.put("type", type)
						.put("cakeId", cakeId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}


	/**
	 * 军械要塞开奖
	 * @param termId
	 * @param player
	 * @param stage
	 * @param openId
	 * @param count
	 * @param cost
	 * @param rewardType
	 * @param rewardId
	 */
	public static void logOrdnanceFortressOpen(Player player, int termId, int stage, int openId, 
			int count, String cost, int rewardType, int rewardId) {
		try {
			if (!GsConfig.getInstance().isTlogEnable()) {
				return;
			}
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.ordnance_fortress_open);
			if (logParam != null) {
				logParam.put("termId", termId) 
						.put("stage", stage) 
						.put("openId",openId)
						.put("count",count)
						.put("cost",cost)
						.put("rewardType",rewardType)
						.put("rewardId",rewardId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 军械要塞
	 * @param termId
	 * @param player
	 * @param fromStage
	 * @param toStage
	 */
	public static void logOrdnanceFortressAdvance(Player player,int termId,  int fromStage, int toStage) {
		try {
			if (!GsConfig.getInstance().isTlogEnable()) {
				return;
			}
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.ordnance_fortress_advance);
			if (logParam != null) {
				logParam.put("termId", termId) 
						.put("fromStage", fromStage) 
						.put("toStage",toStage);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}


	/**
	 * 装备工匠词条变动
	 * @param player
	 * @param cfgId 装备词条配置id
	 * @param armourAddCfgId 装备属性配置id
	 * @param effectType 作用号类型
	 * @param effectValue 作用号值
	 * @param reason 变动原因 1 获取 2 放弃 3 被传承
	 * @param inheritArmourCfgId 传承的装备配置id
	 */
	public static void logEquipCarftsmanAttr(Player player, int cfgId, int armourAddCfgId, int effectType, int effectValue, int reason, int inheritArmourCfgId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.equip_carftsman_attr);
			if (logParam != null) {
				logParam.put("cfgId", cfgId);
				logParam.put("armourAddCfgId", armourAddCfgId);
				logParam.put("effectType", effectType);
				logParam.put("effectValue", effectValue);
				logParam.put("reason", reason);
				logParam.put("inheritArmourCfgId", inheritArmourCfgId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 周年红包打开
	 * @param player
	 * @param termId 活动期数
	 * @param stage  红包ID
	 * @param score  红包积分
	 */
	public static void logRedPackageOpen(Player player, int termId, int stage,int score) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.red_package_open);
			if (logParam != null) {
				logParam.put("termId", termId)
						.put("stage", stage)
						.put("score", score);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录活动成就任务数据
	 * 
	 * @param player
	 * @param activityId    活动Id
	 * @param termId        活动期数
	 * @param achieveId     成就Id
	 * @param achieveState  成就状态
	 * @param achieveData   成就数据
	 */
	public static void logActivityAchieve(Player player, int activityId, int termId, int achieveId, int achieveState, String achieveData) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.activity_achieve);
			if (logParam != null) {
				logParam.put("termId", termId)
						.put("activityId", activityId)
						.put("achieveId", achieveId)
						.put("achieveState", achieveState)
						.put("achieveData", achieveData);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录战地寻宝活动购买通行证
	 * 
	 * @param player
	 * @param termId
	 */
	public static void logBattleFieldBuyGift(Player player, int termId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.battle_field_buy_gift);
			if (logParam != null) {
				logParam.put("termId", termId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录战地寻宝活动骰子使用和购买情况
	 * 
	 * @param player
	 * @param termId
	 * @param add        增加或减少
	 * @param diceType   骰子类型
	 * @param count      购买或使用骰子的数量
	 * @param afterCount 购买或使用后骰子的余量
	 */
	public static void logBattleFieldDice(Player player, int termId, boolean add, int diceType, int count, int afterCount) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.battle_field_dice);
			if (logParam != null) {
				logParam.put("termId", termId)
						.put("addOrReduce", add ? 0 : 1)
						.put("diceType", diceType)
						.put("count", count)
						.put("afterCount", afterCount);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录 战地寻宝活动投掷骰子获得奖励
	 * 
	 * @param player
	 * @param termId
	 * @param awardType  奖励类型
	 * @param awardId    奖励ID
	 * @param cellId     停留的格子ID
	 */
	public static void logBattleFieldDiceReward(Player player, int termId, int awardType, int awardId, int cellId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.battle_field_dice_reward);
			if (logParam != null) {
				logParam.put("termId", termId)
						.put("awardType", awardType)
						.put("awardId", awardId)
						.put("cellId", cellId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}


	/**
	 * 周年庆烟花盛典点燃烟花 激活buff
	 * @param playerId
	 * @param buffId
	 */
	public static void logFireWorksForBuffActive(Player player, int termId, int buffId){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.fire_works_for_buff_active);
			if (logParam != null) {
				logParam.put("termId", termId)
						.put("buffId", buffId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 周年庆 庆典美食,蛋糕制作
	 * @param playerId
	 * @param level  制作蛋糕的等级
	 */
	public static void logCelebrationFoodMake(Player player, int termId, int level){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.celebration_food_make);
			if (logParam != null) {
				logParam.put("termId", termId)
						.put("level", level);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 记录装备战令经验购买情况
	 * @param player
	 * @param termId
	 * @param cycle
	 * @param expId
	 * @param exp
	 */
	public static void logBuyOrderEquipExp(Player player, int termId, int cycle, int expId, int exp) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.order_equip_exp_buy);
			if (logParam != null) {
				logParam.put("termId", termId)
				.put("cycle", cycle)
				.put("expId", expId)
				.put("exp", exp);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	
	/**
	 * 记录装备战令进阶购买情况
	 * @param player
	 * @param termId
	 * @param cycle
	 * @param authId
	 */
	public static void logBuyOrderEquipAuth(Player player, int termId, int cycle, int authId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.order_equip_auth_buy);
			if (logParam != null) {
				logParam.put("termId", termId)
				.put("cycle", cycle)
				.put("authId", authId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录装备战令经验等级流水
	 * @param player
	 * @param termId
	 * @param cycle
	 * @param expAdd
	 * @param exp
	 * @param level
	 * @param reason	变更来源:0-初始化,1-完成任务,2-进阶,3-购买经验
	 * @param reasonId	来源id: 任务id/进阶礼包id/经验id
	 */
	 
	public static void logOrderEquipExpChange(Player player, int termId, int cycle, int expAdd, int exp, int level, int reason, int reasonId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.order_equip_exp_flow);
			if (logParam != null) {
				logParam.put("termId", termId)
				.put("cycle", cycle)
				.put("expAdd", expAdd)
				.put("exp", exp)
				.put("level", level)
				.put("reason", reason)
				.put("reasonId", reasonId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录装备战令任务完成情况
	 * @param player
	 * @param termId
	 * @param cycle
	 * @param orderId
	 * @param finishTimes
	 */
	public static void logOrderEquipFinishId(Player player, int termId, int cycle, int orderId, int addTimes, int finishTimes){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.order_equip_flow);
			if (logParam != null) {
				logParam.put("termId", termId)
				.put("cycle", cycle)
				.put("orderId", orderId)
				.put("addTimes", addTimes)
				.put("finishTimes", finishTimes);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 回流拼图积分
	 * @param player
	 * @param termId
	 */
	public static void logReturnPuzzleScore(Player player, int termId, int score){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.return_puzzle_flow);
			if (logParam != null) {
				logParam.put("termId", termId)
				.put("score", score);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 军备
	 * @param playerId
	 */
	public static void logArmamentExchangeFirst(Player player, int termId){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.armament_exchange_first);
			if (logParam != null) {
				logParam.put("termId", termId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}	
	
	/**祝福语活动产出的道具使用后,聊天频道出炫酷图片效果
	 * @param player
	 * @param imageIndex 图片下标
	 */
	public static void logGreetUseItemImage(Player player, int imageIndex){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.greet_activity_item_index);
			if (logParam != null) {
				logParam.put("imageIndex", imageIndex);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}



	/**
	 * 双十一联盟欢庆活动联盟积分流水
	 * @param guildId
	 * @param termId 活动期数
	 * @param addScore 积分增量
	 * @param afterPlayerScore 增加后的个人积分
	 * @param afterGuildScore 增加后的联盟积分
	 */
	public static void logAllianceCelebrateScore(Player player, String guildId, int termId, int addScore, int afterPlayerScore,int afterGuildScore) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.alliance_celebrate_score);
			if (logParam != null) {
				logParam.put("termId", termId)
						.put("guildId", guildId)
						.put("addScore", addScore)
						.put("afterPlayerScore", afterPlayerScore)
						.put("afterGuildScore", afterGuildScore);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 双十一联盟欢庆活动领取奖励记录
	 * @param termId 活动期数
	 * @param level 领取奖励的等级
	 * @param index 领取奖励的第几列 1普通 和 2高级
	 */
	public static void logAllianceCelebrateReward(Player player, int termId, int level, int index) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.alliance_celebrate_reward);
			if (logParam != null) {
				logParam.put("termId", termId)
						.put("level", level)
						.put("index", index);;
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 超级折扣活动刷新奖池打点
	 * @param playerId
	 * @param refreshType 1 免费次数刷新, 2 使用道具刷新
	 * @param poolId 刷新到的奖池id
	 * @param discount 刷新到的折扣
	 */
	public static void logSuperDiscountDraw(Player player,int termId, int refreshType,int cfgId, int poolId, String discount){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.super_discount_draw);
			if (logParam != null) {
				logParam.put("termId", termId)
						.put("refreshType", refreshType)
						.put("cfgId", cfgId)
						.put("poolId", poolId)
						.put("discount", discount);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}	
	}
	
	/**
	 * 超级折扣活动购买商品打点
	 * @param playerId
	 * @param goods 购买商品
	 * @param price 购买的单价
	 * @param num  购买的数量
	 * @param discount 购买的折扣
	 * @param goodsId 购买商品的id
	 */
	public static void logSuperDiscountBuy(Player player,int termId, String goods, String price, int num, String discount, int goodsId,int voucherId){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.super_discount_buy);
			if (logParam != null) {
				logParam.put("termId", termId)
						.put("goods", goods)
						.put("price", price)
						.put("num", num)
						.put("discount", discount)
						.put("goodsId", goodsId)
						.put("voucherId", voucherId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}	
	}
	
	/**
	 * 玩家全服签到
	 * 
	 * @param player
	 * @param termId 期数
	 */
	public static void logPlayerGlobalSign(Player player, int termId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.global_sign);
			if (logParam != null) {
				logParam.put("termId", termId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 泰能工厂. 资源收取去查itemflow
	 * @param action 变化来源
	 * @param befId 改变前配置id
	 * @param afterId 改变后id
	 * @param msg 额外信息
	 */
	public static void logPlantFactoryChange(Player player, Action action, int befId, int afterId, PlantFactoryEntity entity) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.plant_factory_flow);
			if (logParam != null) {
				logParam.put("action", action.intItemVal());
				logParam.put("befId", befId);
				logParam.put("afterId", afterId);
				logParam.put("msg", entity.getPlantCfgId());
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
	}

	/**
	 * 升级泰能科技
	 * @param action 变化来源
	 * @param befId 升级前id
	 * @param afterId 升级后id
	 * @param msg 额外信息
	 */
	public static void logPlantTechChange(Player player, Action action, int befId, int afterId, PlantTech upfactory) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.plant_tech_flow);
			if (logParam != null) {
				logParam.put("action", action.intItemVal());
				logParam.put("befId", befId);
				logParam.put("afterId", afterId);
				logParam.put("msg", upfactory.toString());
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
	}

	/**
	 * 资源保卫战 特工能力刷新/激活 记录
	 * @param termId
	 * @param type 类型 1激活 , 2刷新
	 * @param skillInfo 技能信息
	 */
	public static void logResourceDefenseSkillRefreshAndActive(Player player, int termId, int type, String skillInfo) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.resource_defense_skill_refresh);
			if (logParam != null) {
				logParam.put("termId", termId)
						.put("type", type)
						.put("skillInfo", skillInfo);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}


	/**
	 * 资源保卫战 特工能力 生效
	 * @param player
	 * @param termId 活动期数
	 * @param skillId 生效技能的Id
	 */
	public static void logResourceDefenseAgentSkillEffect(Player player, int termId, int skillId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.resource_defense_skill_effect);
			if (logParam != null) {
				logParam.put("termId", termId)
						.put("skillId", skillId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 装扮投放系列活动三:重燃战火 领取宝箱每次一个
	 * @param player
	 * @param termId
	 */
	public static void logFireReigniteReceiveBox(Player player, int termId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.fire_reignite_receive_box);
			if (logParam != null) {
				logParam.put("termId", termId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}


	/**
	 * 军械要塞开奖
	 * @param termId  期数
	 * @param player  玩家
	 * @param stage   当前阶段
	 * @param openId  奖券ID
	 * @param count   开奖次数
	 * @param cost    消耗
	 * @param rewardType  奖励类型
	 * @param rewardId  奖励ID
	 */
	public static void logPlantFortressOpen(Player player, int termId, int stage, int openId, 
			int count, String cost, int rewardType, int rewardId) {
		try {
			if (!GsConfig.getInstance().isTlogEnable()) {
				return;
			}
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.plant_fortress_open);
			if (logParam != null) {
				logParam.put("termId", termId) 
						.put("stage", stage) 
						.put("openId",openId)
						.put("count",count)
						.put("cost",cost)
						.put("rewardType",rewardType)
						.put("rewardId",rewardId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 军械要塞进阶
	 * @param termId   期数
	 * @param player  玩家
	 * @param fromStage 当前阶段
	 * @param toStage 操作后阶段
	 */
	public static void logPlantFortressAdvance(Player player,int termId,  int fromStage, int toStage) {
		try {
			if (!GsConfig.getInstance().isTlogEnable()) {
				return;
			}
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.plant_fortress_advance);
			if (logParam != null) {
				logParam.put("termId", termId) 
						.put("fromStage", fromStage) 
						.put("toStage",toStage);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录平台信息授权变更
	 * 
	 * @param player
	 * @param event
	 */
	public static void logPlayerUsertagChange(Player player, int event) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.player_usertag_change);
			if (logParam != null) {
				logParam.put("eventTag", event);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 传承记录打点
	 * 
	 * @param player
	 * @param termId      期数
	 * @param oldPlayerId 被传承角色ID
	 * @param oldServerId 被传承角色注册区服ID
	 * @param sumGold     被传承角色充值额度
	 * @param rebetGold   返利额度
	 * @param sumVipExp   被传承角色贵族经验值
	 * @param rebetExp    贵族经验返利值
	 */
	public static void logAccountInherit(Player player, int termId, String oldPlayerId, String oldServerId, long sumGold, long rebetGold, long sumVipExp, long rebetExp) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.account_inherit_flow);
			if (logParam != null) {
				logParam.put("oldPlayerId", oldPlayerId)
						.put("oldServerId", oldServerId)
						.put("sumGold", sumGold)
						.put("rebetGold", rebetGold)
						.put("sumVipExp", sumVipExp)
						.put("rebetExp", rebetExp)
						.put("termId", termId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 传承条件判断结果打点
	 * @param player
	 * @param condType
	 * @param condParam
	 */
	public static void logInheritCondResult(Player player, InheritCondType condType, String condParam) {
		try {
			// 记录已被传承过的角色、还未被传承但充过值的角色信息
			StringJoiner sj1 = new StringJoiner(","),  sj2 = new StringJoiner(",");
			InheritNewService.getInstance().inheritCondDataCollect(player, sj1, sj2);
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.inherit_cond_result);
			if (logParam != null) {
				logParam.put("condType", condType.intVal())
						.put("condParam", condParam)
						.put("inheritedRoles", sj1.toString())
						.put("uninheritedRoles", sj2.toString());
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	
	/**
	 * 军事备战进阶奖励领取
	 * @param player
	 * @param termId  期数
	 * @param rewardId 成就任务ID
	 */
	public static void logMilitaryPrepareAdvancedReward(Player player, int termId, int  rewardId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.military_prepare_advanced_reward);
			if (logParam != null) {
				logParam.put("termId", termId)
						.put("rewardId", rewardId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}



	/**
	 * 装圣诞节系列活动二:冬日装扮活动 每次领取宝箱
	 * @param player
	 * @param termId
	 */
	public static void logFireReigniteReceiveBoxTwo(Player player, int termId, int boxId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.fire_reignite_receive_box_two);
			if (logParam != null) {
				logParam.put("termId", termId);
				logParam.put("boxId", boxId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	public static void logPeakHonourScore(Player player, String guildId, int getType, long addScore, long afterScore, int matchId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.peak_honour_score);
			if (logParam != null) {
				logParam.put("guildId", guildId);
				logParam.put("getType", getType);
				logParam.put("addScore", addScore);
				logParam.put("afterScore", afterScore);
				logParam.put("matchId", matchId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	public static void logTimeLimitBuy(Player player, int goodsId, int success) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.time_limit_buy);
			if (logParam != null) {
				logParam.put("goodsId", goodsId);
				logParam.put("success", success);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	public static void logTimeLimitBuyWater(int goodsId, int addCount) {
		try {
			LogParam logParam = LogUtil.getNonPersonalLogParam(LogInfoType.time_limit_buy_water);
			if (logParam != null) {
				logParam.put("goodsId", goodsId);
				logParam.put("addCount", addCount);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 圣诞节累计充值
	 * @param player
	 * @param termId
	 * @param rechargeDiamond
	 * @param totalRechargeDiamond
	 */
	public static void logChristmasRechargeDiamond(Player player, int termId, int rechargeDiamond, int totalRechargeDiamond) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.christmas_recharge);
			if (logParam != null) {
				logParam.put("termId", termId);
				logParam.put("rechargeDiamond", rechargeDiamond);
				logParam.put("totalRechargeDiamond", totalRechargeDiamond);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	
	/**
	 * 雄心壮志活动抽奖次数
	 * @param player
	 * @param termId
	 * @param boxCount
	 */
	public static void logCoreplateBox(Player player, int termId, int boxCount) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.coreplate_score_box);
			if (logParam != null) {
				logParam.put("termId", termId);
				logParam.put("boxCount",boxCount);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	


	/** 登录基金购买流水
	 * @param player
	 * @param termId  活动期数
	 * @param type	登录基金购买的类型,,1加速基金,2英雄基金, 3装备基金
	 * */
	public static void logBuyLoginFundTwoFlow(Player player, int termId, int type) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.login_fund_two_buy);
		if (logParam != null) {
			logParam.put("termId", termId);
			logParam.put("type", type);
			GameLog.getInstance().info(logParam);
		}
	}

	/**
	 * 洪福礼包 解锁log
	 * @param player
	 * @param termId 活动期数
	 * @param giftId 礼包id
	 */
	public static void logHongFuGiftUnlockFlow(Player player, int termId, int giftId) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.hong_fu_gift_unlock);
		if (logParam != null) {
			logParam.put("termId", termId);
			logParam.put("giftId", giftId);
			GameLog.getInstance().info(logParam);
		}
	}

	/**
	 * 洪福礼包 领取奖励log
	 * @param player
	 * @param termId 活动期数
	 * @param giftId 礼包id
	 * @param dayCount 累计领取几天的
	 * @param chooseRewardId 自选的奖励id
	 */
	public static void logHongFuGiftRecRewardFlow(Player player, int termId, int giftId, int dayCount, int chooseRewardId) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.hong_fu_gift_reward);
		if (logParam != null) {
			logParam.put("termId", termId);
			logParam.put("giftId", giftId);
			logParam.put("dayCount", dayCount);
			logParam.put("chooseRewardId", chooseRewardId);
			GameLog.getInstance().info(logParam);
		}
	}
	
	public static void logDailyStatistic(String serverId, String mergeServerIds, String serverOpenTime,
			long playerPowerOne, long playerPowerTen, long playerCityOne, long playerCityTen, long playerLevelOne,
			long playerLevelTen, long guildPowerOne, long guildPowerTen) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.server_statistic);
			logParam.put("serverId", serverId).put("mergeServerIds", mergeServerIds)
					.put("serverOpenTime", serverOpenTime).put("playerPowerOne", playerPowerOne)
					.put("playerPowerTen", playerPowerTen).put("playerCityOne", playerCityOne)
					.put("playerCityTen", playerCityTen).put("playerLevelOne", playerLevelOne)
					.put("playerLevelTen", playerLevelTen).put("guildPowerOne", guildPowerOne)
					.put("guildPowerTen", guildPowerTen);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}		
	}
	
	
	
	/**
	 * 情报中心升级
	 * @param player
	 * @param level
	 */
	public static void logPlayerAgencyLevelUp(Player player,int level){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.agency_levelup);
			if (logParam != null) {
				logParam.put("afterLevel", level);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 情报中心领奖
	 * @param player
	 * @param eventUUID
	 * @param eventCfgId
	 * @param specialEvent
	 * @param levelUpEvent
	 * @param itemEvent
	 * @param difficulity
	 */
	public static void logPlayerAgencyAward(Player player,String eventUUID,int eventCfgId,int specialEvent,int levelUpEvent,int itemEvent,int difficulity){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.agency_award);
			if (logParam != null) {
				logParam.put("eventUUID", eventUUID)
						.put("eventCfgId", eventCfgId)
						.put("specialEvent", specialEvent)
						.put("levelUpEvent", levelUpEvent)
						.put("itemEvent", itemEvent)
						.put("difficulity", difficulity);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	

	
	/**
	 * 情报中心刷新事件
	 * @param player
	 * @param eventUUID
	 * @param eventCfgId
	 * @param specialEvent
	 * @param levelUpEvent
	 * @param itemEvent
	 * @param difficulity
	 */
	public static void logPlayerAgencyRefresh(Player player,String eventUUID,int eventCfgId,int specialEvent,int levelUpEvent,int itemEvent,int difficulity){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.agency_refresh);
			if (logParam != null) {
				logParam.put("eventUUID", eventUUID)
						.put("eventCfgId", eventCfgId)
						.put("specialEvent", specialEvent)
						.put("levelUpEvent", levelUpEvent)
						.put("itemEvent", itemEvent)
						.put("difficulity", difficulity);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 情报中心箱子奖励
	 * @param player
	 * @param boxId
	 * @param boxExtLevel
	 * @param agencyLevel
	 */
	public static void logPlayerAgencyBox(Player player,int boxId,int boxExtLevel,int agencyLevel){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.agency_box);
			if (logParam != null) {
				logParam.put("boxId", boxId)
						.put("boxExtLevel", boxExtLevel)
						.put("agencyLevel", agencyLevel);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 情报中心完成事件
	 * @param player
	 * @param eventUUID
	 * @param eventCfgId
	 * @param specialEvent
	 * @param levelUpEvent
	 * @param itemEvent
	 * @param difficulity
	 */
	public static void logPlayerAgencyComplete(Player player,String eventUUID,int eventCfgId,int specialEvent,int levelUpEvent,int itemEvent,int difficulity){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.agency_complete);
			if (logParam != null) {
				logParam.put("eventUUID", eventUUID)
						.put("eventCfgId", eventCfgId)
						.put("specialEvent", specialEvent)
						.put("levelUpEvent", levelUpEvent)
						.put("itemEvent", itemEvent)
						.put("difficulity", difficulity);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 红蓝对决翻牌操作记录
	 * 
	 * @param player
	 * @param termId
	 * @param operType 操作类型：1翻牌，2重置，3开启翻牌
	 * @param pool 红蓝池子，0代表红色池子，1代表蓝色池子
	 * @param ticketId 牌ID
	 * @param rewardId 翻牌翻出的奖励ID
	 * @param refreshTimes 奖池刷新重置次数 
	 */
	public static void logRedbludTicketFlow(Player player, int termId, int operType, int pool, int ticketId,
			int rewardId, int refreshTimes) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.redblue_ticket_flow);
			if (logParam != null) {
				logParam.put("termId", termId)
						.put("operType", operType)
						.put("pool", pool)
						.put("ticketId", ticketId)
						.put("rewardId", rewardId)
						.put("refreshTimes", refreshTimes);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 升级泰能学院
	 * @param action 变化来源
	 * @param befId 升级前id
	 * @param afterId 升级后id
	 * @param msg 额外信息
	 */
	public static void logPlantSchoolChange(Player player, Action action, int befId, int afterId, PlantSoldierSchool upfactory) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.plant_soldier_flow);
			if (logParam != null) {
				logParam.put("action", action.intItemVal());
				logParam.put("befId", befId);
				logParam.put("afterId", afterId);
				logParam.put("msg", upfactory.toString());
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
	}
	
	
	
	/**
	 * 精装夺宝摇色子
	 * @param player
	 * @param termId 期数
	 * @param randomFirst 色子1随机数
	 * @param randomSecond 色子2随机值
	 * @param awardNumStart 中奖范围起始
	 * @param awardNumEnd  中奖范围结束
	 * @param randomId  随机配置ID
	 * @param awardId  中奖ID
	 * @param scoreAdd 增加兑换记分
	 * @param cost  消耗
	 */
	public static void logDressTreasureRandom(Player player,int termId,int randomFirst,int randomSecond,
			int awardNumStart,int awardNumEnd,int randomId,int awardId,String cost) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.dress_treasure_random);
			if (logParam != null) {
				logParam.put("termId", termId)
						.put("randomFirst", randomFirst)
						.put("randomSecond", randomSecond)
						.put("awardNumStart", awardNumStart)
						.put("awardNumEnd", awardNumEnd)
						.put("randomId", randomId)
						.put("awardId", awardId)
						.put("cost", cost);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 精装夺宝重置
	 * @param player
	 * @param termId 期数
	 * @param randomId 随机配置ID
	 * @param awardNumStart 中奖范围起始
	 * @param awardNumEnd 中奖范围结束
	 */
	public static void logDressTreasureRest(Player player,int termId,int randomId, int awardNumStart, int awardNumEnd) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.dress_treasure_reset);
			if (logParam != null) {
				logParam.put("termId", termId)
						.put("randomId", randomId)
						.put("awardNumStart", awardNumStart)
						.put("awardNumEnd", awardNumEnd);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	

	
	/**
	 * 限时抢购客户端打点记录
	 * @param player
	 * @param logType 1-触发金条不足 2-点击前往确认 3-进入商城点击充值 4-充值完成
	 * @param activityId 活动id
	 * @param goodsId 购买档位id：配置id
	 * @param diamonds 金条存量
	 * @param rechargeId 5. 充值档位
	 */
	public static void logTimeLimitClientLog(Player player, int type, int activityId, int goodsId, int diamonds, int rechargeId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.time_limit_client);
			if (logParam != null) {
				logParam.put("type", type);
				logParam.put("activityId", activityId);
				logParam.put("goodsId", goodsId);
				logParam.put("diamonds", diamonds);
				logParam.put("rechargeId", rechargeId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 玩家战力记录
	 * @param player
	 */
	public static void logPlayerPower(Player player) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.login_power);
			if (logParam != null) {
				PowerElectric powerElectic = player.getData().getPowerElectric();
				logParam.put("playerBattle", powerElectic.getPlayerBattlePoint());
				logParam.put("army", powerElectic.getArmyBattlePoint());
				logParam.put("build", powerElectic.getBuildBattlePoint());
				logParam.put("tech", powerElectic.getTechBattlePoint());
				logParam.put("trap", powerElectic.getTrapBattlePoint());
				logParam.put("hero", powerElectic.getHeroBattlePoint());
				logParam.put("superSoldier", powerElectic.getSuperSoldierBattlePoint());
				logParam.put("armour", powerElectic.getArmourBattlePoint());
				logParam.put("armourTech", powerElectic.getEquipResearchPoint());
				logParam.put("plantScool", powerElectic.getPlantTechBattlePoint());
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	
	
	/**
	 * 幸运转盘抽奖
	 * @param player
	 * @param termId
	 * @param group 多抽还是单抽
	 * @param randomCount  抽奖次数
	 * @param cellId  奖品格子ID
	 * @param rewardId 奖品ID
	 * @param canSelect 是否可以更换
	 * @param finish  是否是完结一抽
	 */
	public static void logLuckyBoxRandom(Player player,int termId,String group,int randomCount, int cellId, int rewardId,int canSelect,int finish) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.lucky_box_random);
			if (logParam != null) {
				logParam.put("termId", termId)
						.put("group", group)
						.put("randomCount", randomCount)
						.put("cellId", cellId)
						.put("rewardId", rewardId)
						.put("canSelect", canSelect)
						.put("finish", finish);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	
	/**
	 * 坊间杯玩家领取奖励
	 * @param player
	 * @param cfgId 任务id
	 * @param award 领取奖励
	 * @param num 全服数
	 * @param guildNum 联盟数
	 * @param contribution 玩家贡献
	 */
	public static void logPlayerObeliskAward(Player player, int cfgId, String award,int num ,int guildNum, int contribution) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.obelisk_award);
			if (logParam != null) {
				logParam.put("cfgId", cfgId);
				logParam.put("award", award);
				logParam.put("num", num);
				logParam.put("guildNum", guildNum);
				logParam.put("contribution", contribution);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 世界勋章活动奖励
	 * @param player
	 * @param award 奖励内容
	 * @param reason 奖励原因
	 */
	public static void logStarLightSignAward(Player player, String award, int reason) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.star_light_sign_award);
			if (logParam != null) {
				logParam.put("award", award);
				logParam.put("reason", reason);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 分数变化
	 * @param player
	 * @param before
	 * @param after
	 * @param add
	 */
	public static void logStarLightSignScore(Player player, int before, int after, int add) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.star_light_sign_score);
			if (logParam != null) {
				logParam.put("before", before);
				logParam.put("after", after);
				logParam.put("add", add);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 道具选择
	 * @param player
	 * @param choose 道具id
	 */
	public static void logStarLightSignChoose(Player player,int type, int rechargeType, int choose) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.star_light_sign_choose);
			if (logParam != null) {
				logParam.put("type", type);
				logParam.put("rechargeType", rechargeType);
				logParam.put("choose", choose);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 *  记录心跳日志
	 * @param activePlayerCnt
	 */
	public static void logObeliskState(ObeliskMissionItem missionItem) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.obelisk_mission);
			logParam.put("cfgId", missionItem.getCfgId())
					.put("serverOpen", GameUtil.getServerOpenTime())
					.put("num", missionItem.getNum())
					.put("guildNum", SerializeHelper.mapToString(missionItem.getGuildMap()))
					.put("state", missionItem.getState().name());
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/** 打压之战使用号令*/
	public static void logDYZZUseOrder(IDYZZPlayer player, String roomId, String guildId, String guildName, int orderId, int costPower) {
		try {
			LogParam logParam = getPersonalLogParam(player, LogInfoType.dyzz_guild_use_order);
			logParam.put("guildId", guildId)
					.put("guildName", guildName)
					.put("roomId", roomId)
					.put("orderId", orderId)
					.put("orderPowerCost", costPower);
			GameLog.getInstance().info(logParam);

		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
	}

	/** 打压之战结束*/
	public static void logDYZZResult(String roomId, String winGuild , int baseHPA, int baseHPB) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.dyzz_battle_score);
			logParam.put("gameId", roomId)
					.put("winGuild", winGuild)
					.put("baseHPA", baseHPA)
					.put("baseHPB", baseHPB);
			GameLog.getInstance().info(logParam);
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
	}
	
	/**
	 * 泰能机密操作打点
	 * 
	 * @param player
	 * @param operType  操作类型：1翻牌，2开箱
	 * @param openBoxCount：成功开启箱子的个数
	 * @param openCardTime：翻牌次数
	 * @param buyItemCount：购买翻牌道具的个数
	 * @param openBoxTimes：单个箱子的开启次数
	 * @param success：开箱操作结果
	 */
	public static void logPlantSecret(Player player, int termId, int operType, int openBoxCount, int openCardTime, int buyItemCount, 
			int openBoxTimes, boolean success, int serverNum, int clientNum) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.plant_secret);
			if (logParam != null) {
				logParam.put("termId", termId);
				logParam.put("operType", operType);
				logParam.put("openBoxCount", openBoxCount);
				logParam.put("openCardTime", openCardTime);
				logParam.put("buyItemCount", buyItemCount);
				logParam.put("openBoxTimes", openBoxTimes);
				logParam.put("boxResult", success ? 1 : 0);
				logParam.put("serverNum", serverNum);
				logParam.put("clientNum", clientNum);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 预流失活动激活打点
	 * @param player
	 * @param termId
	 */
	public static void logPrestressingLoss(Player player, int termId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.prestressing_loss);
			if (logParam != null) {
				logParam.put("termId", termId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	
	/**
	 * 结算
	 * @param termId  期数
	 * @param gameId  战场ID
	 * @param battleTime 战斗时长
	 * @param killCount  击杀数量
	 * @param deathCount 伤亡数量
	 * @param resoureCount 资源数量
	 */
	public static void dyzzBattleResult(String playerId,int termId,String gameId,long battleTime,long killCount,
			long deathCount,int resoureCount,int camp,int baseHp,int winCount,int lossCount){
		try {
			if (!GsConfig.getInstance().isTlogEnable()) {
				return;
			}
			LogParam logParam = LogUtil.getNonPersonalLogParam(LogInfoType.dyzz_battle_result);
			logParam.put("playerId", playerId)
					.put("termId", termId)
					.put("gameId", gameId)
					.put("battleTime", battleTime)
					.put("killCount", killCount)
					.put("deathCount", deathCount)
					.put("resoureCount", resoureCount)
					.put("camp", camp)
					.put("baseHp", baseHp)
					.put("winCount", winCount)
					.put("lossCount", lossCount);
			
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 大雅赛季积分变化
	 * @param playerId
	 * @param termId
	 * @param gameId
	 * @param seasonId
	 * @param scoreChange
	 * @param socreBefore
	 * @param socreAfter
	 */
	public static void dyzzSeasonScoreChange(String playerId,int termId,String gameId,int seasonId,int scoreChange,int socreBefore,int socreAfter){
		try {
			if (!GsConfig.getInstance().isTlogEnable()) {
				return;
			}
			LogParam logParam = LogUtil.getNonPersonalLogParam(LogInfoType.dyzz_season_score_change);
			logParam.put("playerId", playerId)
					.put("termId", termId)
					.put("gameId", gameId)
					.put("seasonId", seasonId)
					.put("scoreChange", scoreChange)
					.put("socreBefore", socreBefore)
					.put("socreAfter", socreAfter);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	
	/**
	 * 匹配时长
	 * @param termId 期数
	 * @param teamId 队伍ID
	 * @param gameId 战场ID
	 * @param matchTime 匹配时长
	 */
	public static void dyzzMatchResult(int termId,String teamId,String gameId,long matchTime){
		try {
			if (!GsConfig.getInstance().isTlogEnable()) {
				return;
			}
			
			LogParam logParam = LogUtil.getNonPersonalLogParam(LogInfoType.dyzz_match_time);
			logParam.put("termId", termId)
					.put("teamId", teamId)
					.put("gameId", gameId)
					.put("matchTime", matchTime);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	
	

	public static void logCYBORGKillMonster(Player player, String id, String guildId, String guildName, int cfgId, int guildOrder) {
		// TODO Auto-generated method stub
		
	}

	public static void logCYBORGUseOrder(ICYBORGPlayer player, String id, String guildId, String guildName, int orderId, int costPower) {
		// TODO Auto-generated method stub
		
	}
	

	
	/**
	 * 盟军祝福活动签到
	 * @param player
	 * @param termId 期数
	 * @param openPos 开放数字位置
	 * @param openNum 开放数字之
	 */
	public static void logAllianceWishSign(Player player,int termId,int signType, int openPos, int openNum) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.alliance_wish_sign);
			if (logParam != null) {
				logParam.put("termId", termId)
				.put("signType", signType)
				.put("openPos", openPos)
				.put("openNum", openNum);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 盟军祝福活动帮助
	 * @param player
	 * @param termId 期数
	 * @param guildMember  联盟玩家ID
	 */
	public static void logAllianceWishHelp(Player player,int termId, String guildMember) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.alliance_wish_help);
			if (logParam != null) {
				logParam.put("termId", termId)
						.put("guildMember", guildMember);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}


	/**
	 * 天降洪福支付
	 * @param player
	 * @param groupId 付费组
	 * @param level 付费档位
	 * @param payCount 付费次数
	 * @param choose 自定义选择
	 */
	public static void logHeavenBlessingPay(Player player, int groupId, int level, int payCount, int choose) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.heaven_blessing_pay);
			if (logParam != null) {
				logParam.put("groupId", groupId)
						.put("level", level)
						.put("payCount", payCount)
						.put("choose", choose);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 天降洪福激活
	 * @param player
	 * @param groupId 付费组
	 */
	public static void logHeavenBlessingActive(Player player, int groupId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.heaven_blessing_active);
			if (logParam != null) {
				logParam.put("groupId", groupId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 天降洪福领奖
	 * @param player
	 * @param groupId 付费组
	 * @param level 付费档位
	 * @param payCount 支付次数
	 * @param choose 自定义选择
	 */
	public static void logHeavenBlessingAward(Player player, int groupId, int level, int payCount, int choose) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.heaven_blessing_award);
			if (logParam != null) {
				logParam.put("groupId", groupId)
						.put("level", level)
						.put("payCount", payCount)
						.put("choose", choose);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}


	/**
	 * 天降洪福随机奖励
	 * @param player
	 * @param gain 获得奖励
	 */
	public static void logHeavenBlessingRandomAward(Player player, String gain) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.heaven_blessing_random_award);
			if (logParam != null) {
				logParam.put("gain", gain);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 天降洪福打开界面
	 * @param player
	 */
	public static void logHeavenBlessingOpen(Player player) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.heaven_blessing_open);
			if (logParam != null) {
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 活动通用记录打点
	 * @param player 玩家
	 * @param param 参数
	 */
	public static void logActivityCommon(Player player, LogInfoType logInfoType, Map<String, Object> param) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, logInfoType);
			if(logParam != null){
				for(Entry<String, Object> entry : param.entrySet()){
					logParam.put(entry.getKey(), entry.getValue());
				}
				GameLog.getInstance().info(logParam);
			}
		}catch (Exception e){
			HawkException.catchException(e);
		}
	}
	
	public static void logActivityCommon(LogInfoType logInfoType, Map<String, Object> param) {
		try {
			LogParam logParam = getNonPersonalLogParam(logInfoType);
			if(logParam != null){
				for(Entry<String, Object> entry : param.entrySet()){
					logParam.put(entry.getKey(), entry.getValue());
				}
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 感恩福利领奖打点
	 * @param player
	 * @param punchCount
	 * @param createDays
	 * @param help
	 * @param gold
	 */
	public static void logGrateBenefitsAward(Player player, int punchCount, int createDays, int help, int gold) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.grateful_benefits_award);
			if (logParam != null) {
				logParam.put("punchCount", punchCount);
				logParam.put("createDays", createDays);
				logParam.put("help", help);
				logParam.put("gold", gold);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**荣耀返利 购买
	 * @param player
	 * @param termId
	 * @param num 购买次数
	 */
	public static void logHonorRepayBuy(Player player, int termId, int num){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.honor_repay_buy_reward);
			if (logParam != null) {
				logParam.put("termId", termId)
						.put("num", num);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 陨晶成就完成
	 * @param player
	 */
	public static void logDYZZAchieveReach(Player player,int achieveId){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.dyzz_achieve_reach);
			if (logParam != null) {
				logParam.put("achieveId", achieveId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 陨晶成就领取
	 * @param player
	 */
	public static void logDYZZAchieveTake(Player player, int score, int count, int achieveId){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.dyzz_achieve_take);
			if (logParam != null) {
				logParam.put("score", score);
				logParam.put("count", count);
				logParam.put("achieveId", achieveId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**荣耀返利 领取返利奖励
	 * @param player
	 * @param termId
	 * @param buyTimes  返利购买次数
	 * @param type	类型 1-手动领取,2-系统补发
	 */
	public static void logHonorRepayReceiveReward(Player player, int termId, int buyTimes, int type){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.honor_repay_receive_reward);
			if (logParam != null) {
				logParam.put("termId", termId)
						.put("buyTimes", buyTimes)
						.put("type", type);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 国家状态改变日志
	 * @param status
	 */
	public static void logNationStatusChange(int status) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.nation_status_change);
		    logParam.put("status", status);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 国家重建捐献
	 * @param player
	 * @param beforeValue
	 * @param afterValue
	 */
	public static void logNationRebuild(Player player, int beforeValue, int afterValue) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.nation_rebuild);
			if (logParam != null) {
				logParam.put("beforeValue", beforeValue)
						.put("afterValue", afterValue);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 国家建筑升级开始
	 * @param buildingType
	 * @param buildingLevel
	 * @param leftValue
	 */
	public static void logNationBuildingUpgradeStart(int buildingType, int buildingLevel, int leftValue) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.nation_building_upgrade_start);
		    logParam.put("buildingType", buildingType)
		    	.put("buildingLevel", buildingLevel)
		    	.put("leftValue", leftValue);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 国家建筑升级结束
	 * @param buildingType
	 * @param buildingLevel
	 */
	public static void logNationBuildingUpgradeEnd(int buildingType, int buildingLevel) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.nation_building_upgrade_end);
			logParam.put("buildingType", buildingType)
		    	.put("buildingLevel", buildingLevel);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 国家捐献资助
	 * @param player
	 * @param buildingType
	 * @param afterBuildVal
	 * @param goldNum
	 */
	public static void logNationBuildSupport(Player player, int buildingType, int afterBuildVal, long goldNum) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.nation_build_support);
			if (logParam != null) {
				logParam.put("buildingType", buildingType)
						.put("afterBuildVal", afterBuildVal)
						.put("goldNum", goldNum);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 建设任务开始
	 * @param player
	 * @param questId
	 * @param buildingType
	 * @param heros
	 * @param armys
	 * @param isAdv
	 * @param buildingLevel
	 */
	public static void logNationbuildQuestStart(Player player, int questId, int buildingType, String heros, String armys, int isAdv, int buildingLevel) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.nation_build_quest_start);
			if (logParam != null) {
				logParam.put("questId", questId)
						.put("buildingType", buildingType)
						.put("heros", heros)
						.put("armys", armys)
						.put("isAdv", isAdv)
						.put("buildingLevel", buildingLevel);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 召回建设行军
	 * @param player
	 * @param questId
	 * @param alreadyTime
	 */
	public static void logNationbuildQuestCancel(Player player, int questId, long alreadyTime) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.nation_build_quest_cancel);
			if (logParam != null) {
				logParam.put("questId", questId)
						.put("alreadyTime", alreadyTime);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 任务完成
	 * @param player
	 * @param questId
	 * @param leftTimes
	 * @param buildingType
	 * @param questType
	 * @param beforbuildVal
	 * @param afterbuildVal
	 * @param dayVal
	 * @param buildingLevel
	 */
	public static void logNationbuildQuestOver(Player player, int questId, int leftTimes, int buildingType, int questType, int beforbuildVal, int afterbuildVal, String dayVal, int buildingLevel) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.nation_build_quest_over);
			if (logParam != null) {
				logParam.put("questId", questId)
						.put("leftTimes", leftTimes)
						.put("buildingType", buildingType)
						.put("questType", questType)
						.put("beforbuildVal", beforbuildVal)
						.put("afterbuildVal", afterbuildVal)
						.put("dayVal", dayVal)
						.put("buildingLevel", buildingLevel);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 刷新任务
	 * @param player
	 * @param costCoins
	 */
	public static void logNationbuildQuestRefresh(Player player, long costCoins) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.nation_build_quest_refresh);
			if (logParam != null) {
				logParam.put("costCoins", costCoins);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 使用加建设值道具
	 * @param player
	 * @param buildingType
	 * @param beforbuildVal
	 * @param afterbuildVal
	 * @param dayVal
	 */
	public static void logNationbuildItemUse(Player player, int buildingType, int beforbuildVal, int afterbuildVal, String dayVal) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.nation_build_item_use);
			if (logParam != null) {
				logParam.put("buildingType", buildingType)
				.put("beforbuildVal", beforbuildVal)
				.put("afterbuildVal", afterbuildVal)
				.put("dayVal", dayVal);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 飞船部件升级开始
	 * @param player
	 * @param componentId
	 * @param componentLevel
	 * @param consumeRes
	 * @param leftRes
	 * @param shipFactoryLevel
	 */
	public static void logNationShipUpgradeStart(Player player, int componentId, int componentLevel, String consumeRes, String leftRes, int shipFactoryLevel) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.nation_ship_upgrade_start);
			if (logParam != null) {
				logParam.put("componentId", componentId)
				.put("componentLevel", componentLevel)
				.put("consumeRes", consumeRes)
				.put("leftRes", leftRes)
				.put("shipFactoryLevel", shipFactoryLevel);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 飞船部件取消升级
	 * @param player
	 * @param componentId
	 * @param componentLevel
	 * @param returnRes
	 * @param leftRes
	 */
	public static void logNationShipUpgradeCancel(Player player, int componentId, int componentLevel, String returnRes, String leftRes) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.nation_ship_upgrade_cancel);
			if (logParam != null) {
				logParam.put("componentId", componentId)
				.put("componentLevel", componentLevel)
				.put("returnRes", returnRes)
				.put("leftRes", leftRes);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 飞船升级结束
	 * @param componentId
	 * @param componentLevel
	 * @param shipFactoryLevel
	 */
	public static void logNationShipUpgradeOver(int componentId, int componentLevel, int shipFactoryLevel) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.nation_ship_upgrade_over);
			logParam.put("componentId", componentId)
		    	.put("componentLevel", componentLevel)
		    	.put("shipFactoryLevel", shipFactoryLevel);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 飞船助力
	 * @param player
	 * @param componentId
	 * @param componentLevel
	 * @param beforTime
	 * @param afterTime
	 */
	public static void logNationShipUpgradeAssist(Player player, int componentId, int componentLevel, long beforTime, long afterTime) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.nation_ship_upgrade_assist);
			if (logParam != null) {
				logParam.put("componentId", componentId)
				.put("componentLevel", componentLevel)
				.put("beforTime", beforTime)
				.put("afterTime", afterTime);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/** 记录至尊vip激活
	 * 
	 * @param player
	 * @param score
	 * @param source 数据产生来源：1-升级，2-激活
	 * 
	 * */
	public static void logSuperVipActive(Player player, int score, int level, int source, int cfgId, long endTime) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.super_vip_active);
		if (logParam != null) {
			logParam.put("score", score)
			        .put("superVipLevel", level)
			        .put("source", source)
			        .put("cfgId", cfgId)
			        .put("endTime", endTime <= 0 ? endTime : HawkTime.formatTime(endTime));
			GameLog.getInstance().info(logParam);
		}
	}
	
	/** 记录科技研究操作日志
	 * 
	 * @param player
	 * @param techId
	 * @param techLevel
	 * @param action 1 升级  2 升级完成 */
	public static void logCrossTechResearchOperation(Player player, int techId, int techLevel, int action) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.cross_tech_research_flow);
		if (logParam != null) {
			logParam.put("techId", techId)
					.put("techLv", techLevel)
					.put("action", action);
			GameLog.getInstance().info(logParam);
		}
	}

	/**
	 * 七夕相遇结局
	 * @param player
	 * @param termId  期数
	 * @param endingId  结局ID
	 */
	public static void logLoverMeetEnding(Player player,int termId, int endingId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.lover_meet_ending);
			if (logParam != null) {
				logParam.put("termId", termId)
						.put("endingId", endingId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/** 记录泰能科技研究操作日志
	 * 
	 * @param player
	 * @param techId
	 * @param techLevel
	 * @param action 1 升级  2 升级完成 */
	public static void logPlantScienceResearchOperation(Player player, int techId, int techLevel, int action,int power) {
		LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.plant_science_research_flow);
		if (logParam != null) {
			logParam.put("techId", techId)
					.put("techLv", techLevel)
					.put("action", action)
					.put("power", power);
			GameLog.getInstance().info(logParam);
		}
	}
	
	
	
	 
	/**
	 * 跨服匹配，玩家新战力计算参数
	 * @param termId
	 * @param playerId
	 * @param rank
	 * @param playerStrength
	 * @param powerWeight
	 * @param addPower
	 */
	public static void logCrossActivityPlayerStrength(int termId,String playerId, int rank,long playerStrength, double powerWeight,double addPower) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.cross_activity_player_strength);
		    logParam.put("termId", termId)
		    .put("playerId", playerId)
		    .put("rank", rank)
			.put("playerStrength", playerStrength)
			.put("powerWeight", powerWeight)
			.put("addPower", addPower);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 跨服匹配，历史战绩计算参数
	 * @param termId
	 * @param historyTerm
	 * @param rank
	 * @param addParam
	 */
	public static void logCrossActivityTeamParam(int termId,int historyTerm, int rank, double addParam) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.cross_activity_team_param);
		    logParam.put("termId", termId)
		    .put("historyTerm", historyTerm)
		    .put("rank", rank)
			.put("addParam", addParam);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 跨服活动，匹配战力计算
	 * @param termId
	 * @param historyTerm
	 * @param battleVal
	 * @param matchPower
	 */
	public static void logCrossActivityMatchPower(int termId,long battleVal, double matchPower) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.cross_activity_match_power);
		    logParam.put("termId", termId)
		    .put("battleVal", battleVal)
			.put("matchPower", matchPower);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 跨服活动匹配
	 * @param termId
	 * @param crossId
	 * @param servers
	 */
	public static void logCrossActivityMatch(int termId,int crossId, String servers) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.cross_activity_match);
		    logParam.put("termId", termId)
		    .put("crossId", crossId)
		    .put("servers", servers);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 跨服开始记录
	 * @param termId
	 * @param matchPower
	 */
	public static void logCrossActivityStart(int termId,long matchPower) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.cross_activity_start);
		    logParam.put("termId", termId)
		    .put("matchPower", matchPower);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 跨服活动-积分增加
	 */
	public static void logCrossActivtyScoreAdd(Player player, String guildId, long before, long add, long after, int sourceType, int scoreType) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.cross_activity_score_add);
			logParam.put("guildId", guildId);
			logParam.put("before", before);
			logParam.put("add", add);
			logParam.put("after", after);
			logParam.put("sourceType", sourceType);
			logParam.put("scoreType", scoreType);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 跨服活动-领取积分宝箱
	 */
	public static void logCrossActivtyScoreBox(Player player, String guildId, int boxId, int type) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.cross_activity_score_box);
			logParam.put("guildId", guildId);
			logParam.put("boxId", boxId);
			logParam.put("type", type);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 跨服活动-个人积分排名
	 */
	public static void logCrossActivtySelfScoreRank(Player player, String guildId, long score, int rank, int rewardId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.cross_activity_self_score_rank);
			logParam.put("guildId", guildId);
			logParam.put("score", score);
			logParam.put("rank", rank);
			logParam.put("rewardId", rewardId);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 跨服活动-领取成就任务奖励
	 */
	public static void logCrossActivtyMission(Player player, String guildId, int missionId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.cross_activity_mission);
			logParam.put("guildId", guildId);
			logParam.put("missionId", missionId);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 跨服活动-获取国家战略点
	 */
	public static void logCrossActivtyTalent(Player player, String guildId, long before, long add, long after, int sourceType) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.cross_activity_talent);
			logParam.put("guildId", guildId);
			logParam.put("before", before);
			logParam.put("add", add);
			logParam.put("after", after);
			logParam.put("sourceType", sourceType);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 跨服活动-向能量塔发起行军
	 * 
	 * marchType 0空点 1有敌方
	 * type 0 发起 1 占领 2 收取
	 */
	public static void logCrossActivtyPylonMarch(Player player, String guildId, int armyCount, int marchType, int type) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.cross_activity_pylon_march);
			logParam.put("guildId", guildId);
			logParam.put("armyCount", armyCount);
			logParam.put("marchType", marchType);
			logParam.put("type", type);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 跨服活动-个人战略排名
	 */
	public static void logCrossActivtySelfTalentRank(Player player, String guildId, long talent, int rank, int rewardId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.cross_activity_self_talent_rank);
			logParam.put("guildId", guildId);
			logParam.put("talent", talent);
			logParam.put("rank", rank);
			logParam.put("rewardId", rewardId);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 跨服活动-攻占盟总建筑
	 */
	public static void logCrossActivtyOccupyPresident(Player player, String guildId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.cross_activity_occupy_president);
			logParam.put("guildId", guildId);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 跨服活动-控制盟总建筑
	 */
	public static void logCrossActivtyControlPresident(Player player, String guildId, int armyCount) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.cross_activity_control_president);
			logParam.put("guildId", guildId);
			logParam.put("armyCount", armyCount);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 跨服活动-分配礼包
	 */
	public static void logCrossActivtySendGift(Player player, String guildId, String targetId, int giftType, int giftId, int count) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.cross_activity_send_gift);
			logParam.put("guildId", guildId);
			logParam.put("targetId", targetId);
			logParam.put("giftType", giftType);
			logParam.put("giftId", giftId);
			logParam.put("count", count);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 跨服活动-领取宝箱
	 */
	public static void logCrossActivtyReceiveBox(Player player, String guildId, int boxId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.cross_activity_receive_box);
			logParam.put("guildId", guildId);
			logParam.put("boxId", boxId);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 跨服活动-联盟积分排名
	 */
	public static void logCrossActivityGuildScoreRank(String guildId, long score, int rank, int rewardId) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.cross_activity_guild_score_rank);
		    logParam.put("guildId", guildId);
		    logParam.put("score", score);
		    logParam.put("rank", rank);
		    logParam.put("rewardId", rewardId);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 跨服活动-联盟战略排名
	 */
	public static void logCrossActivityGuildTalentRank(String guildId, long score, int rank, int rewardId) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.cross_activity_guild_talent_rank);
		    logParam.put("guildId", guildId);
		    logParam.put("score", score);
		    logParam.put("rank", rank);
		    logParam.put("rewardId", rewardId);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 跨服活动-国家积分排名
	 */
	public static void logCrossActivityServerScoreRank(long score, int rank, int rewardId) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.cross_activity_server_score_rank);
		    logParam.put("score", score);
		    logParam.put("rank", rank);
		    logParam.put("rewardId", rewardId);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 跨服活动-爆仓
	 */
	public static void logCrossActivityGenBox(int resourceType, long before, long after, String tarServerId) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.cross_activity_gen_box);
		    logParam.put("resourceType", resourceType);
		    logParam.put("before", before);
		    logParam.put("after", after);
		    logParam.put("tarServerId", tarServerId);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 跨服活动-能量塔刷新
	 */
	public static void logCrossActivityPylonRefresh(long before, long after) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.cross_activity_pylon_refresh);
		    logParam.put("before", before);
		    logParam.put("after", after);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	public static void logMarchSpeedItemUse(Player player, int itemId, int afterCount) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.march_speed_item_use);
			logParam.put("itemId", itemId);
			logParam.put("afterCount", afterCount);
			logParam.put("dungeon", player.getDungeonMap());
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	 /**
	  * 删除死兵打点记录
	  * 
	  * @param type	       操作类型：1-预设删除，2-删除已存在的待恢复死兵
	  * @param soldierType 预设删除时的兵种类型
	  * @param level   预设删除时的兵种等级
	  * @param armyId  即时删除的兵种ID
	  * @param count   即时删除的兵数量
	  * @param oldCount 即时删除前原有的待恢复死兵数量
	  * @param hospitalType 类型：1-普通死兵，2-统帅之战死兵
	  */
	 public static void logNationalHospitalRemoveSolider(Player player, int type, int soldierType, int level, int armyId, int count, int oldCount, int hospitalType) {
		 try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.nation_hospital_del);
			if (logParam != null) {
				logParam.put("operType", type)
				.put("soldierType", soldierType)
				.put("soldierLevel", level)
				.put("armyId", armyId)
				.put("count", count)
				.put("oldCount", oldCount)
				.put("type", hospitalType);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	 }
	 
	 /**
	  * 国家医院死兵恢复加速打点
	  * 
	  * @param player
	  * @param oldMap
	  * @param newMap
	  * @param type 类型：1-普通死兵，2-统帅之战死兵
	  */
	 public static void logNationalHospitalSpeedRecover(Player player, Map<Integer, Integer> oldMap, Map<Integer, Integer> newMap, int type) {
		 for (Entry<Integer, Integer> entry : oldMap.entrySet()) {
			 try {
				 int armyId = entry.getKey();
				 int count = entry.getValue() - newMap.getOrDefault(armyId, 0);
				 if (count <= 0) {
					 continue;
				 }
				 ArmyEntity entity = player.getData().getArmyEntity(armyId);
				 LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.nation_speed_recover);
				 logParam.put("armyId", armyId)
				 .put("recoveredCount", count)  // 加速恢复的数量
				 .put("type", type);
				 if (type == NationalConst.NATION_HOSPITAL_SOLDIER) {
					 logParam.put("totalRecovered", entity.getNationalHospitalRecoveredCount());  // 已恢复待领取的总数
					 logParam.put("remainCount", entity.getNationalHospitalDeadCount()); // 待恢复的数量
				 } else {
					 logParam.put("totalRecovered", entity.getTszzRecoveredCount());  // 已恢复待领取的总数
					 logParam.put("remainCount", entity.getTszzDeadCount()); // 待恢复的数量
				 }
				 GameLog.getInstance().info(logParam);
			 } catch (Exception e) {
				 HawkException.catchException(e);
			 }
		 }
	 }
	 
	 /**
	  * 接收死兵打点记录
      * 
      * @param armyId	兵种ID
      * @param oldCount  国家医院中原有的待恢复死兵数量
      * @param comeinCount 进来的死兵数量
      * @param acceptCount 实际接收的死兵数量
      * @param type 类型：1-普通死兵，2-统帅之战死兵
      */
    public static void logNationalHospitalAcceptDeadSoldier(Player player, int armyId, int oldCount, int comeinCount, int acceptCount, int type, int buildLevel, int hospitalAccelerate) {
    	 try {
    		 LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.nation_accept_count);
    		 if (logParam != null) {
    			 logParam.put("oldCount", oldCount)
    			 .put("armyId", armyId)
    			 .put("comeinCount", comeinCount)
    			 .put("acceptCount", acceptCount)
    			 .put("type", type)
    			 .put("buildLevel", buildLevel)
    			 .put("hospitalAccelerate", hospitalAccelerate);
    			 GameLog.getInstance().info(logParam);
    		 }
    	 } catch (Exception e) {
    		 HawkException.catchException(e);
    	 }
    }
    
    /**
     * 死兵恢复时间变化打点记录
     * 
     * @param changeFrom 变化来源 1. 接收死兵后，2. 删除死兵后， 3. 加速复活后
     * 
     * @param startTime 队列开始时间
     * @param oldEndTime 旧的恢复完成时间
     * @param endTime 新的恢复完成时间
     * @param type 类型：1-普通死兵，2-统帅之战死兵
     */
    public static void logNationalHospitalDeadRecoverQueue(Player player, int changeFrom, long startTime, long oldEndTime, long endTime, int alreadyExist, int remainCap, int type) {
	   	 try {
	   		 LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.nation_recover_queue);
	   		 if (logParam != null) {
	   			 logParam.put("changeFrom", changeFrom)
	   			 .put("startTime", HawkTime.formatTime(startTime))
	   			 .put("oldEndTime", HawkTime.formatTime(oldEndTime))
	   			 .put("endTime", HawkTime.formatTime(endTime))
	   			 .put("remainCount", alreadyExist)
	   			 .put("remainCapacity", remainCap)
	   			 .put("type", type);
	   			 GameLog.getInstance().info(logParam);
	   		 }
	   	 } catch (Exception e) {
	   		 HawkException.catchException(e);
	   	 }
   }
    
    
    /**
	 * 英雄祈福，选择祈福ID
	 * @param player
	 * @param termId  期数
	 * @param choose  选择祈福ID
	 */
	public static void logHeroWishChoose(Player player,int termId, int chooseId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.hero_wish_choose);
			if (logParam != null) {
				logParam.put("termId", termId)
						.put("chooseId", chooseId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 盟军祝福礼包购买
	 * @param player
	 * @param termId  期数
	 * @param count  祝福值
	 * @param signCount  签到天数
	 */
	public static void logAllianceWishGiftBuy(Player player,int termId,int giftId, long count,int signCount) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.alliance_wish_gift_buy);
			if (logParam != null) {
				logParam.put("termId", termId)
						.put("giftId", giftId)
						.put("count", count)
						.put("signCount", signCount);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 盟军祝福礼包购买
	 * @param player
	 * @param termId  期数
	 * @param count  祝福值
	 * @param signCount  礼包ID
	 */
	public static void logAllianceWishAchieve(Player player,int termId, long count,int giftId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.alliance_wish_achieve);
			if (logParam != null) {
				logParam.put("termId", termId)
						.put("count", count)
						.put("giftId", giftId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 联盟操作详细日志
	 * @param player
	 * @param guildId 联盟信息
	 * @param changeInfo 根据reason不同含义不同
	 * @param reason
	 */
	public static void logGuildDetail(Player player, String guildId, String changeInfo, int reason) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.guild_detail);
			logParam.put("guildId", guildId)
					.put("changeInfo", changeInfo == null ? "" : changeInfo)
					.put("reason", reason);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录客户端日志
	 * @param player
	 * @param logType
	 * @param logInfo
	 */
	public static void logClientTlog(Player player, String logType, String logInfo) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.client_tlog);
			if (logParam != null) {
				logParam.put("clientTlogType", logType)
						.put("logInfo", logInfo);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 兵种战力日志
	 * @param player
	 * @param soldierStrength 各兵种战力
	 * @param strength 最高兵种战力
	 */
	public static void logStrength(Player player, String soldierStrength, long strength) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.strength);
			if (logParam != null) {
				logParam.put("soldierStrength", soldierStrength)
						.put("strength", strength);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 荣耀同享玩家捐献
	 * @param player
	 * @param termId
	 * @param itemId
	 * @param count
	 * @param guildId
	 * @param time 时间
	 * @param beforeEnergy 捐献前经验值
	 * @param afterEnergy 捐献后经验值
	 * @param curEnergyLevel 捐献后的A能量等级
	 */
	public static void logShareGloryDonate(Player player, int termId, int itemId,
										   int count, String guildId, int beforeEnergy,
										   int afterEnergy, int curEnergyLevel){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.share_glory_donate);
			if (logParam != null) {
				logParam.put("termId", termId)
						.put("itemId", itemId)
						.put("count", count)
						.put("guildId", guildId)
						.put("beforeEnergy", beforeEnergy)
						.put("afterEnergy", afterEnergy)
						.put("curEnergyLevel", curEnergyLevel)
				;
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 *  荣耀同享能量柱升级
	 * @param player
	 * @param termId
	 * @param guildId
	 * @param time
	 * @param curLevel
	 */
	public static void logShareGloryEnergyLevelup(Player player,int termId, String guildId,
										   int curLevel, int itemId){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.share_glory_energy_levelup);
			if (logParam != null) {
				logParam.put("termId", termId)
						.put("guildId", guildId)
						.put("curLevel", curLevel)
						.put("itemId", itemId);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	
	
	
	/**
	 * 荣耀英雄降临抽奖
	 * @param player
	 * @param termId 期数
	 * @param lotteryTpye 抽奖类型  单抽，10抽
	 * @param lotteryRlt 抽奖结果
	 */
	public static void logHonourHeroBefellLottery(Player player,int termId, int lotteryTpye,String lotteryRlt) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.honour_hero_befell_lottery);
			if (logParam != null) {
				logParam.put("termId", termId)
						.put("lotteryTpye", lotteryTpye)
						.put("lotteryRlt", lotteryRlt);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 跨服出战联盟打点
	 * @param player
	 * @param actionType
	 * @param tarGuildId
	 */
	public static void logCrossFightGuild(Player player, int actionType, String tarGuildId){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.cross_activity_fight_guild);
			logParam.put("actionType", actionType);
			logParam.put("tarGuildId", tarGuildId);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 双旦活动礼包购买数据上报
	 * @param player
	 * @param lotteryType 礼包类型
	 * @param selectId    自选奖励对应id
	 * @param randomReward 自选奖励对应的随机奖励
	 */
	public static void logLotteryGiftPay(Player player, int lotteryType, int selectId, String randomReward) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.newyear_lottery_paygift);
			logParam.put("lotteryType", lotteryType)
			        .put("selectId", selectId)
			        .put("randomReward", randomReward)
			        .put("guildId", player.getGuildId());
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 双旦活动领取联盟进度奖励数据上报
	 * @param player
	 * @param lotteryType 礼包类型
	 * @param achieveId 联盟进度档次ID
	 */
	public static void logLotteryTakeAchieveReward(Player player, int lotteryType, int achieveId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.newyear_lottery_achieve);
			logParam.put("lotteryType", lotteryType)
			        .put("achieveId", achieveId)
			        .put("guildId", player.getGuildId());
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 双旦活动头彩抽奖结果数据上报
	 * @param player
	 * @param lotteryType 礼包类型
	 * @param reward 抽中的奖励
	 */
	public static void logLotteryInfo(Player player, int lotteryType, String reward) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.newyear_lottery_result);
			logParam.put("lotteryType", lotteryType)
			        .put("reward", reward)
			        .put("guildId", player.getGuildId());
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录月球之战进入战场信息
	 * @param playerId
	 * @param termId
	 * @param roomId		房间id
	 * @param roomServer	房间所在区服
	 * @param guildId		玩家所在联盟
	 */
	public static void logYQZZEnterInfo(String playerId, int termId, String roomId, String roomServer, String guildId, String serverId, long power) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.yqzz_enter_room);
		    logParam.put("playerId", playerId)
					.put("serverId", serverId)
					.put("termId", termId)
					.put("roomId", roomId)
					.put("guildId", guildId)
					.put("roomServer", roomServer)
					.put("power", power);
			GameLog.getInstance().info(logParam);
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录月球之战退出战场信息
	 * @param playerId
	 * @param termId
	 * @param guildId		玩家所在联盟
	 * @param isMidaway		是否中途退出
	 */
	public static void logYQZZQuitInfo(String playerId, int termId, String roomId,String guildId, boolean isMidaway) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.yqzz_quit_room);
		    logParam.put("playerId", playerId)
			.put("termId", termId)
			.put("roomId", roomId)
			.put("guildId", guildId)
			.put("isMidaway", isMidaway ? 1 : 0);
			GameLog.getInstance().info(logParam);
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 月球之战宣战
	 * @param playerId 玩家ID
	 * @param playerName 玩家名称
	 * @param guildId 玩家联盟ID
	 * @param guildName 玩家联盟名称
	 * @param playerServer 玩家所在服务器ID
	 * @param buildId 建筑ID
	 * @param gameId 战局ID
	 */
	public static void logYQZZDeclareWarInfo(String playerId, String playerName, String guildId, 
			String guildName,String playerServer,int buildId,String gameId) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.yqzz_declare_war);
		    logParam.put("playerId", playerId)
					.put("playerName", playerName)
					.put("guildId", guildId)
					.put("guildName", guildName)
					.put("playerServer", playerServer)
					.put("buildId", buildId)
					.put("gameId", gameId);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 月球之战建筑控制变化
	 * @param guildIdBef 变化前控制联盟ID
	 * @param guildNameBef变化前控制联盟名称
	 * @param serverBef 变化前控制联盟所在服务器ID
	 * @param guildIdAft变化后控制联盟ID
	 * @param guildNameAft变化后控制联盟名称
	 * @param serverAft变化后控制联盟所在服务器ID
	 * @param buildId 建筑ID
	 * @param gameId 战局ID
	 */
	public static void logYQZZBuildControlChange(String guildIdBef,String guildNameBef,String serverBef,String guildIdAft,String guildNameAft,String serverAft,int buildId,String gameId) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.yqzz_build_control_change);
		    logParam.put("guildIdBef", guildIdBef)
					.put("guildNameBef", guildNameBef)
					.put("serverBef", serverBef)
					.put("guildIdAft", guildIdAft)
					.put("guildNameAft", guildNameAft)
					.put("serverAft", serverAft)
					.put("buildId", buildId)
					.put("gameId", gameId);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 联盟控制建筑个数
	 * @param guildId 联盟ID
	 * @param guildName 联盟猛踹
	 * @param buildType 建筑类型
	 * @param count 建筑个数
	 * @param stage 阶段ID
	 * @param gameId 战局ID
	 */
	public static void logYQZZBuildGuildControlCount(String guildId,String guildName,int buildType,int count,int stage,String gameId) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.yqzz_build_guild_control_count);
		    logParam.put("guildId", guildId)
					.put("guildName", guildName)
					.put("buildType", buildType)
					.put("count", count)
					.put("stage", stage)
					.put("gameId", gameId);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 月球之战玩家积分奖励
	 * @param termId 期数
	 * @param playerScore 玩家积分呢
	 * @param rank 排行
	 * @param awardId 奖励ID
	 */
	public static void logYQZZPlayerRankReward(Player player,int termId,long playerScore,int rank,int awardId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.yqzz_player_rank_reward);
		    logParam.put("termId", termId)
					.put("playerScore", playerScore)
					.put("rank", rank)
					.put("awardId", awardId);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 月球之战联盟排行奖励
	 * @param termId 期数
	 * @param guildId 联盟ID
	 * @param guildName 联盟名字
	 * @param guildScore 联盟积分
	 * @param rank 排行
	 * @param awardId 奖励ID
	 */
	public static void logYQZZGuildRankReward(int termId,String guildId,String guildName,long guildScore,int rank,int awardId) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.yqzz_guild_rank_reward);
		    logParam.put("termId", termId)
					.put("guildId", guildId)
					.put("guildName", guildName)
					.put("guildScore", guildScore)
					.put("rank", rank)
					.put("awardId", awardId);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 月球之战国家排行奖励
	 * @param termId 期数
	 * @param serverId 服务器ID
	 * @param serverScore 服务器积分
	 * @param rank 排行
	 * @param awardId 奖励ID
	 */
	public static void logYQZZCountryRankReward(int termId,String serverId,long serverScore,int rank,int awardId) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.yqzz_country_rank_reward);
		    logParam.put("termId", termId)
					.put("serverId", serverId)
					.put("serverScore", serverScore)
					.put("rank", rank)
					.put("awardId", awardId);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 月球之战任务领取奖励
	 * @param termId 期数
	 * @param achieveId 任务ID
	 */
	public static void logYQZZAchieveReward(Player player,int termId,int achieveId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.yqzz_achieve_reward);
		    logParam.put("termId", termId)
					.put("achieveId", achieveId);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 匹配结果
	 * @param termId
	 * @param serverId
	 * @param serverPower
	 * @param joinServer
	 * @param roomServer
	 */
	public static void logYQZZMatch(int termId, String serverId, long serverPower, String joinServer, String roomServer){
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.yqzz_match);
			logParam.put("termId", termId)
					.put("serverId", serverId)
					.put("serverPower", serverPower)
					.put("joinServer", joinServer)
					.put("roomServer", roomServer);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 匹配战力
	 * @param termId
	 * @param serverId
	 * @param guildId
	 * @param guildName
	 * @param guildPower
	 */
	public static void logYQZZMatchPower(int termId, String serverId, String guildId, String guildName, int rank, long guildPower){
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.yqzz_match_power);
			logParam.put("termId", termId)
					.put("serverId", serverId)
					.put("guildId", guildId)
					.put("guildName", guildName)
					.put("rank", rank)
					.put("guildPower", guildPower);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 战斗开始
	 * @param termId
	 * @param serverId
	 * @param serverPower
	 */
	public static void logYQZZBattleStartPower(int termId, String serverId, long serverPower){
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.yqzz_battle_start_power);
			logParam.put("termId", termId)
					.put("serverId", serverId)
					.put("serverPower", serverPower);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 机甲赋能
	 * @param player
	 * @param superSoldier
	 */
	public static void logSuperSoldierEnergy(Player player, SuperSoldier superSoldier) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.super_soldier_energy);
			logParam.put("soldierId", superSoldier.getCfgId())
			        .put("energyLevel", superSoldier.getSoldierEnergy().getLevel())
					.put("energy", superSoldier.getSoldierEnergy().serializ());
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 机甲研究所物品掉落
	 * @param player
	 * @param termId 期数
	 * @param dropType 掉落类型
	 * @param dropCount 掉落数量
	 */
	public static void logMachineLabDrop(Player player,int termId,int dropType,int dropCount) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.machine_lab_drop);
		    logParam.put("termId", termId)
					.put("dropType", dropType)
					.put("dropCount", dropCount);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 机甲研究所领奖
	 * @param player
	 * @param termId 期数
	 * @param orderType 类型 1 全服等级  2 玩家等级  3进阶等级
	 * @param levels 等级列表
	 */
	public static void logMachineLabOrderReward(Player player,int termId,int orderType,String levels) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.machine_lab_order_reward);
		    logParam.put("termId", termId)
					.put("orderType", orderType)
					.put("levels", levels);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 机甲研究所捐献
	 * @param player
	 * @param termId 期数
	 * @param count 数量
	 * @param giftMult 礼包倍数
	 * @param donatMult 暴击倍数
	 * @param serverExpAdd 服务器经验增加
	 * @param playerExpAdd 玩家经验增加
	 * @param stormingPointAdd 攻坚点增加
	 * @param stormingPointTotal 攻坚点累计数量
	 */
	public static void logMachineLabContribute(Player player,int termId, int count, int giftMult,
			int donatMult, int serverExpAdd, int playerExpAdd, int stormingPointAdd, int stormingPointTotal) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.machine_lab_contribute);
		    logParam.put("termId", termId)
					.put("count", count)
					.put("giftMult", giftMult)
					.put("donatMult", donatMult)
					.put("serverExpAdd", serverExpAdd)
					.put("playerExpAdd", playerExpAdd)
					.put("stormingPointAdd", stormingPointAdd)
					.put("stormingPointTotal", stormingPointTotal);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 机甲研究所兑换
	 * @param player
	 * @param termId 期数
	 * @param exchangeId 兑换ID
	 * @param exchangeCount 兑换数量
	 */
	public static void logMachineLabExchange(Player player,int termId,int exchangeId,int exchangeCount) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.machine_lab_exchange);
		    logParam.put("termId", termId)
					.put("exchangeId", exchangeId)
					.put("exchangeCount", exchangeCount);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/***
	 * 副本中人数统计
	 */
	public static void logYQZZRoomInfo(int playerAll, int playerCnt, int guildCnt,String roomId) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.yqzz_room_info);
			logParam.put("playerAll", playerAll)
					.put("playerCnt", playerCnt)
					.put("guildCnt", guildCnt)
					.put("roomId", roomId);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	public static void logCelebrationFundGiftBuy(Player player,int fundLevel) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.celebration_fund_gift_buy);
			if (logParam != null) {
				logParam.put("fundLevel", fundLevel) ;
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	public static void logCelebrationFundScoreBuy(Player player, int score, int cost) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.celebration_fund_score_buy);
			if (logParam != null) {
				logParam.put("buyScoreNum", score) 
				        .put("goldCost",cost);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	public static void logGoldBabyFindReward(Player player, String activityId, int isLockTopGrade, int poolId, int costCount, int rewardCount, int magnification) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.gold_baby_find_reward);
			if (logParam != null) {
				logParam.put("poolId", poolId) 
						.put("activityId", activityId)
						.put("isLockTopGrade", isLockTopGrade)
				        .put("costCount",costCount)
				        .put("rewardCount", rewardCount)
				        .put("magnification", magnification);			 
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 新兵作训打点
	 * @param player
	 * @param trainType 作训类型：1-英雄，2-装备
	 * @param times     此次操作消耗的作训次数
	 * @param remainTimes 剩余作训次数
	 * @param gachaTimes 抽卡次数转换成作训次数后剩余的零头次数
	 * @param gachaTimesTotal 本期活动内总抽卡次数
	 */
	public static void logNewbieTrain(Player player, int trainType, int times, int remainTimes, int gachaTimes, int gachaTimesTotal) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.newbie_train);
			if (logParam != null) {
				logParam.put("trainType", trainType) 
						.put("times", times)
						.put("remainTimes", remainTimes)
						.put("gachaTimes", gachaTimes)
						.put("gachaTimesTotal", gachaTimesTotal);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 *
	 * @param player 玩家
	 * @param chipId 升级的模块ID
	 * @param afterLevel 提升后该模块的等级
	 */
	public static void logPlantSoldierMilitaryUpgrade(Player player, int chipId, int afterLevel){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.plant_soldier_military_upgrade);
			if (logParam != null) {
				logParam.put("chipId", chipId);
				logParam.put("afterLevel", afterLevel);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 发起攻击性行军
	 * @param player
	 * @param marchType        行军类型
	 * @param targetPointType  目标点类型
	 */
	public static void logOffensiveMarch(Player player, int marchType, int targetPointType) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.offensive_march);
			if (logParam != null) {
				logParam.put("marchType", marchType);
				logParam.put("pointType", targetPointType);
				GameLog.getInstance().info(logParam);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	
	/**
	 * 中部培养计划  任务完成获得积分
	 * @param player
	 * @param termId
	 * @param achieveId 任务ID
	 * @param scoreAdd  积分增加数量
	 * @param scoreBef  积分变化前
	 * @param scoreAft  积分变化后
	 */
	public static void logGrowUpBoostAchieveScore(Player player,int termId,int achieveId, int scoreAdd, int scoreBef, int scoreAft) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.grow_up_boost_achieve_score);
		    logParam.put("termId", termId)
					.put("achieveId", achieveId)
					.put("scoreAdd", scoreAdd)
					.put("scoreBef", scoreBef)
					.put("scoreAft", scoreAft);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 中部培养计划  道具消耗获得积分
	 * @param player
	 * @param termId
	 * @param itemId 消耗道具ID
	 * @param itemNum 消耗道具数量
	 * @param scoreAdd  积分增加数量
	 * @param scoreBef  积分变化前
	 * @param scoreAft  积分变化后
	 */
	public static void logGrowUpBoostItemScore(Player player,int termId,int itemId,int itemNum,int scoreAdd,int scoreBef,int scoreAft) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.grow_up_boost_item_score);
		    logParam.put("termId", termId)
					.put("itemId", itemId)
					.put("itemNum", itemNum)
					.put("scoreAdd", scoreAdd)
					.put("scoreBef", scoreBef)
					.put("scoreAft", scoreAft);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 中部培养计划   领取积分任务奖励
	 * @param player
	 * @param termId
	 * @param achieveId 成就ID
	 * @param score 当前积分
	 */
	public static void logGrowUpBoostScoreAchieveRewardTake(Player player,int termId,int achieveId,int score) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.grow_up_boost_score_achieve_reward_take);
		    logParam.put("termId", termId)
					.put("achieveId", achieveId)
					.put("score", score);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 中部培养计划  任务刷新记录 
	 * @param player
	 * @param termId
	 * @param refreshCount 刷新次数
	 * @param curPage  当前任务页
	 */
	public static void logGrowUpBoostScoreAchievePageChange(Player player,int termId,int refreshCount,int curPage) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.grow_up_boost_score_achieve_page_change);
		    logParam.put("termId", termId)
					.put("refreshCount", refreshCount)
					.put("curPage", curPage);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 中部培养计划  兑换道具解锁层数 
	 * @param player
	 * @param termId
	 * @param exchangeId  兑换ID
	 * @param exchangeGroup 兑换组
	 * @param unlockGroupMaxBef 兑换前解锁最高兑换组
	 * @param unlockGroupMaxAft 兑换后解锁最高兑换组
	 */
	public static void logGrowUpBoostExchangeGroup(Player player,int termId,int exchangeId,int exchangeGroup,int unlockGroupMaxBef,int unlockGroupMaxAft) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.grow_up_boost_exchange_group);
		    logParam.put("termId", termId)
					.put("exchangeId", exchangeId)
					.put("exchangeGroup", exchangeGroup)
					.put("unlockGroupMaxBef", unlockGroupMaxBef)
					.put("unlockGroupMaxAft", unlockGroupMaxAft);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 中部培养计划  道具礼包购买 
	 * @param player
	 * @param termId
	 * @param buyId 购买ID
	 */
	public static void logGrowUpBoostBuyGift(Player player,int termId,int buyId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.grow_up_boost_buy_gift);
		    logParam.put("termId", termId)
					.put("buyId", buyId);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 中部培养计划  道具回收
	 * @param player
	 * @param termId 道具ID
	 * @param itemCount 数量
	 */
	public static void logGrowUpBoostItemRecover(Player player,int termId,int itemId,int itemCount) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.grow_up_boost_item_recover);
		    logParam.put("termId", termId)
		    		.put("itemId", itemId)
					.put("itemCount", itemCount);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 星海分配燃油
	 * @param playerId 玩家ID
	 * @param guildId 联盟ID
	 * @param guildOil 联盟燃油
	 * @param tarId 目标Id 
	 */
	public static void logXhjzXhjzFuelDistribute(String battleId, String playerId, String guildId, int guildOil, String tarId, int add, int after) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.xhjz_fuel_distribute);
			logParam
					.put("battleId", battleId)
					.put("playerId", playerId)
					.put("guildId", guildId)
					.put("guildOil", guildOil)
					.put("tarId", tarId)
					.put("add", add)
					.put("after", after);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	
	
	/**
	 * 反攻幽灵-报名
	 * @param player
	 * @param termId 期数
	 * @param guildId 联盟ID
	 * @param signLevel 难度等级
	 * @param timeIndex 开启时间段
	 * @param passLevel 联盟通过最高等级
	 */
	public static void logFGYLSignUp(Player player,int termId,String guildId,int signLevel,int timeIndex,int passLevel,int signCount) {
		try {
			
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.fgyl_sign_up);
		    logParam.put("termId", termId)
					.put("guildId", guildId)
					.put("signLevel", signLevel)
					.put("timeIndex", timeIndex)
					.put("passLevel", passLevel)
					.put("signCount", signCount);
		    
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 反攻幽灵-创建房间
	 * @param termId 期数
	 * @param guildId 联盟ID
	 * @param roomLevel 难度等级
	 * @param roomId 房间ID
	 */
	public static void logFGYLCreateRoom(int termId,String guildId, int roomLevel,String roomId) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.fgyl_create_room);
			logParam.put("termId", termId);
		    logParam.put("guildId", guildId);
		    logParam.put("roomLevel", roomLevel);
		    logParam.put("roomId", roomId);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 *  反攻幽灵-加入房间
	 * @param player
	 * @param termId 期数
	 * @param guildId 联盟ID
	 * @param roomId 房间ID
	 */
	public static void logFGYLJoinRoom(Player player,int termId,String guildId,String roomId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.fgyl_join_room);
		    logParam.put("termId", termId)
					.put("guildId", guildId)
					.put("roomId", roomId);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 反攻幽灵-退出房间
	 * @param player
	 * @param termId 期数
	 * @param guildId 联盟ID
	 * @param roomId 房间ID
	 */
	public static void logFGYLExitRoom(Player player,int termId,String guildId,String roomId, int exitReason) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.fgyl_exit_room);
		    logParam.put("termId", termId)
					.put("guildId", guildId)
					.put("roomId", roomId)
					.put("exitReason", exitReason);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 反攻幽灵-房间结束
	 * @param termId 期数
	 * @param guildId 联盟ID
	 * @param roomId 房间ID
	 * @param roomLevel 战斗难度
	 * @param rlt 战斗结果
	 * @param timeUse 耗时
	 * @param joinCounts 参战玩家数量
	 * @param signCount 第几次报名
	 */
	public static void logFGYLOverRoom(int termId,String guildId,String roomId,int roomLevel, int rlt,int timeUse,int joinCounts,int signCount) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.fgyl_over_room);
			logParam.put("termId", termId);
		    logParam.put("guildId", guildId);
		    logParam.put("roomId", roomId);
		    logParam.put("roomLevel", roomLevel);
		    logParam.put("rlt", rlt);
		    logParam.put("timeUse", timeUse);
		    logParam.put("joinCounts", joinCounts);
		    logParam.put("signCount", signCount);
			GameLog.getInstance().info(logParam);
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 反攻幽灵-房间战斗奖励
	 * @param player
	 * @param termId 期数
	 * @param guildId 联盟ID
	 * @param roomId 房间ID
	 * @param roomLevel 难度等级
	 */
	public static void logFGYLFightReward(Player player,int termId,String guildId,String roomId,int roomLevel) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.fgyl_fight_reward);
		    logParam.put("termId", termId)
					.put("guildId", guildId)
					.put("roomId", roomId)
					.put("roomLevel", roomLevel);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 反攻幽灵-排行榜奖励
	 * @param termId 期数
	 * @param guildId 联盟ID
	 * @param rank 排名
	 * @param rewardId  奖励ID
	 * @param roomLevel 难度等级
	 * @param timeUse  耗时
	 */
	public static void logFGYLRankReward(int termId,String guildId, int rank,int rewardId,int roomLevel,int timeUse) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.fgyl_rank_reward);
			logParam.put("termId", termId);
		    logParam.put("guildId", guildId);
		    logParam.put("rank", rank);
		    logParam.put("rewardId", rewardId);
		    logParam.put("roomLevel", roomLevel);
		    logParam.put("timeUse", timeUse);
		    GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 先驱回响 - 加入战斗
	 * @param player
	 * @param termId
	 * @param guildId 联盟ID
	 * @param teamId  队伍ID
	 * @param roomId  房间ID
	 */
	public static void logXqhxJoinBattle(Player player,int termId,String guildId,String teamId,String roomId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.xqhx_join_battle);
		    logParam.put("termId", termId)
					.put("guildId", guildId)
					.put("teamId", teamId)
					.put("roomId", roomId);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 先驱回响 - 退出战斗
	 * @param player
	 * @param termId
	 * @param guildId 联盟ID
	 * @param teamId  队伍ID
	 * @param roomId  房间ID
	 */
	public static void logXqhxExitBattle(Player player,int termId,String guildId,String teamId,String roomId) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.xqhx_exit_battle);
		    logParam.put("termId", termId)
					.put("guildId", guildId)
					.put("teamId", teamId)
					.put("roomId", roomId);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 先驱回响-管理小队成员 
	 * @param player
	 * @param guildId
	 * @param manageType 队伍类型+1出站  2停战
	 * @param targetId 被操作人ID
	 * @param befTeam 操作前队伍ID
	 * @param curTeam 当前队伍ID
	 */
	public static void logGuildTeamMamageMember(Player player,String guildId,int manageType,String targetId,String befTeam,String curTeam) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.guild_team_mamage_member);
		    logParam.put("guildId", guildId)
					.put("manageType", manageType)
					.put("targetId", targetId)
					.put("befTeam", befTeam)
					.put("curTeam", curTeam);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 先驱回响-报名
	 * @param player
	 * @param termId
	 * @param guildId 联盟ID
	 * @param teamId  队伍ID
	 * @param teamPower 队伍实力
	 * @param memberCnt 队伍人数
	 * @param signTimeIndex 战斗时间段
	 */
	public static void logXqhxSignUp(Player player,int termId,String guildId,String teamId,long teamPower,int memberCnt,int signTimeIndex) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.xqhx_sign_up);
		    logParam.put("termId", termId)
					.put("guildId", guildId)
					.put("teamId", teamId)
					.put("teamPower", teamPower)
					.put("memberCnt", memberCnt)
					.put("signTimeIndex", signTimeIndex);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	
	/**
	 * 先驱回响-战斗结束
	 * @param termId
	 * @param timeIndex
	 * @param guildA 联盟ID
	 * @param teamA  队伍ID
	 * @param teamPowerA 队伍实力
	 * @param teamMemberCntA 队伍人数
	 * @param teamJoinCntA 参战人数
	 * @param teamScoreA 队伍积分
	 * @param teamDataChipA 队伍数据碎片数量
	 * @param guildB
	 * @param teamB
	 * @param teamPowerB
	 * @param teamMemberCntB
	 * @param teamJoinCntB
	 * @param teamScoreB
	 * @param teamDataChipB
	 */
	public static void logXqhxBattleOver(int termId,int timeIndex,
			String guildA,String teamA,long teamPowerA,int teamMemberCntA,int teamJoinCntA,long teamScoreA,int teamDataChipA,
			String guildB,String teamB,long teamPowerB,int teamMemberCntB,int teamJoinCntB,long teamScoreB,int teamDataChipB) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.xqhx_battle_over);
			logParam.put("termId", termId)
					.put("timeIndex", timeIndex)
					.put("guildA", guildA)
					.put("teamA", teamA)
					.put("teamPowerA", teamPowerA)
					.put("teamMemberCntA", teamMemberCntA)
					.put("teamJoinCntA", teamJoinCntA)
					.put("teamScoreA", teamScoreA)
					.put("teamDataChipA", teamDataChipA)
					
					.put("guildB", guildB)
					.put("teamB", teamB)
					.put("teamPowerB", teamPowerB)
					.put("teamMemberCntB", teamMemberCntB)
					.put("teamJoinCntB", teamJoinCntB)
					.put("teamScoreB", teamScoreB)
					.put("teamDataChipB", teamDataChipB)
					;
			GameLog.getInstance().info(logParam);
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 先驱回响-发奖
	 * @param termId
	 * @param timeIndex  战斗时间段
	 * @param guildId    联盟ID
	 * @param teamId     队伍ID
	 * @param playerId   玩家ID
	 * @param roomId     房间ID
	 * @param battleRlt  胜利 1 ,失败 0
	 * @param playerScore玩家积分
	 * @param awardId    奖励ID
	 */
	public static void logXqhxSendAward(int termId,int timeIndex,String guildId,String teamId,String playerId,String roomId,int battleRlt,long playerScore,int awardId) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.xqhx_send_award);
			logParam.put("termId", termId)
					.put("timeIndex", timeIndex)
					.put("guildId", guildId)
					.put("teamId", teamId)
					.put("playerId", playerId)
					.put("roomId", roomId)
					.put("battleRlt", battleRlt)
					.put("playerScore", playerScore)
					.put("awardId", awardId);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	
	/**
	 * 巨龙来袭预约
	 * @param guildId  联盟ID
	 * @param appointmentTime  预约时间
	 * @param openTime 开始时间
	 */
	public static void logGuildDragonAttackAppoint(String guildId,long appointmentTime,long openTime) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.guild_dragon_attack_appoint);
			logParam.put("guildId", guildId)
					.put("appointmentTime", appointmentTime)
					.put("openTime", openTime);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 巨龙来袭开启
	 * @param guildId  联盟ID
	 * @param appointmentTime  预约时间
	 * @param openTime 开始时间
	 */
	public static void logGuildDragonAttackOpen(String guildId,long appointmentTime,long openTime) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.guild_dragon_attack_open);
			logParam.put("guildId", guildId)
					.put("appointmentTime", appointmentTime)
					.put("openTime", openTime);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 巨龙来袭结束
	 * @param guildId  联盟ID
	 * @param guildDamage  预约时间
	 * @param guildReward 开始时间
	 */
	public static void logGuildDragonAttackEnd(String guildId,long guildDamage,int guildReward) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.guild_dragon_attack_end);
			logParam.put("guildId", guildId)
					.put("guildDamage", guildDamage)
					.put("guildReward", guildReward);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	


	
	/**
	 * 巨龙来袭-攻击
	 * @param player
	 * @param termId 期数
	 * @param guildId 联盟ID
	 * @param battleId 战斗ID
	 * @param playerDamage 玩家伤害
	 * @param playerCreateTime 玩家创建时间
	 */
	public static void logGuildDragonAttackFight(Player player,long termId,String guildId, String battleId, long playerDamage, long playerCreateTime) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.guild_dragon_attack_fight);
		    logParam.put("termId", termId)
					.put("guildId", guildId)
					.put("battleId", battleId)
					.put("playerDamage", playerDamage)
					.put("playerCreateTime", playerCreateTime);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 巨龙来袭发奖
	 * @param guildId  联盟ID
	 * @param playerId  玩家ID
	 * @param playerDamage 玩家伤害
	 * @param guildDamage 联盟伤害
	 * @param rewardCd 奖励是否CD
	 * @param playerReward 玩家奖励
	 * @param guildReward 联盟奖励
	 */
	public static void logGuildDragonAttackReward(String guildId,String playerId,long playerDamage,long guildDamage,int rewardCd,int playerReward, int guildReward) {
		try {
			LogParam logParam = getNonPersonalLogParam(LogInfoType.guild_dragon_attack_reward);
			logParam.put("guildId", guildId)
					.put("playerId", playerId)
					.put("playerDamage", playerDamage)
					.put("guildDamage", guildDamage)
					.put("rewardCd", rewardCd)
					.put("playerReward", playerReward)
					.put("guildReward", guildReward);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	
	
	/**
	 * 自动集结-开启
	 * @param player
	 * @param settingInfo  设置参数
	 */
	public static void logAutoMassJionOpen(Player player,String settingInfo) {
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.auto_mass_jion_open);
		    logParam.put("settingInfo", settingInfo);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 自动集结-行军出发
	 * @param player
	 * @param marchId     行军ID
	 * @param marchType   行军类型
	 * @param autoMarchCnt 当前自动集结行军数量
	 * @param autoMarchFreeCnt 当前自动集结可用数量
	 * @param curOrder    当前队里排名
	 * @param leaderMarchId 队长行军ID
	 * @param leaderMarchType 队长行军类型
	 * @param leaderMarchTarget 队长行军目标
	 * @param marchDis 距离
	 */
	public static void logAutoMassJionStart(Player player,String marchId, int marchType,int autoMarchCnt,int autoMarchFreeCnt,int curOrder,String leaderMarchId,
			int leaderMarchType, String leaderMarchTarget, int marchDis){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.auto_mass_jion_start);
		    logParam.put("marchId", marchId)
		    		.put("marchType", marchType)
		    		.put("autoMarchCnt", autoMarchCnt)
		    		.put("autoMarchFreeCnt", autoMarchFreeCnt)
		    		.put("curOrder", curOrder)
		    		.put("leaderMarchId", leaderMarchId)
		    		.put("leaderMarchType", leaderMarchType)
		    		.put("leaderMarchTarget", leaderMarchTarget)
		    		.put("marchDis", marchDis);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 自动集结-行军加入成功
	 * @param player
	 * @param marchId     行军ID
	 * @param marchType   行军类型
	 * @param leaderMarchId 队长行军ID
	 * @param leaderMarchType 队长行军类型
	 */
	public static void logAutoMassJionSuc(Player player,String marchId, int marchType,String leaderMarchId,int leaderMarchType){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.auto_mass_jion_suc);
		    logParam.put("marchId", marchId)
		    		.put("marchType", marchType)
		    		.put("leaderMarchId", leaderMarchId)
		    		.put("leaderMarchType", leaderMarchType);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	

	/**
	 * 
	 * @param player
	 * @param operType 1 创建, 2 指定车头, 3 上车 , 4 抢夺, 5 到达, 6 刷新
	 * @param cost
	 * @param truck
	 */
	public static void logMaterialTransport(Player player,int  operType, MTTruck truck){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.material_transport);
			logParam.put("guildId", truck.getGuildId())
					.put("truckId", truck.getId())
					.put("truckType", truck.getType().getNumber())
					.put("operType", operType)
					.put("leaderId", truck.getLeader().getPlayerId())
					.put("robCnt", truck.getRobCnt())
					.put("trainReward", truck.getTrainReward())
					.put("group1", "")
					.put("group2", "")
					.put("group3", "")
					.put("group4", "");
			for (MTTruckGroup group : truck.getCompartments()) {
				logParam.put("group" + group.getIndex(), group.getGroupId() + "_" + ItemInfo.toString(group.getRewards()));
			}
		    
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 开启建筑第二队列免费试用
	 * @param player
	 * @param freeTime
	 */
	public static void logSecondaryBuildQueueFreeUse(Player player,int freeTime){
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.secondary_build_queue_free_use);
		    logParam.put("freeTime", freeTime);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	
}