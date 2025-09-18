package com.hawk.game.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.obj.HawkObjBase;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple3;
import org.hawk.uuid.HawkUUIDGenerator;
import org.hawk.xid.HawkXID;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBiMap;
import com.hawk.activity.type.impl.honourHeroBefell.cfg.HonourHeroBefellRewardCfg;
import com.hawk.common.AccountRoleInfo;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.TemporaryMarch;
import com.hawk.game.battle.battleIncome.impl.PvpBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.BattleSoldierHonorCfg;
import com.hawk.game.config.BuildAreaCfg;
import com.hawk.game.config.BuildLimitCfg;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.config.SuperSoldierCfg;
import com.hawk.game.config.TechnologyCfg;
import com.hawk.game.config.XZQAwardCfg;
import com.hawk.game.config.XZQPointCfg;
import com.hawk.game.entity.ArmourEntity;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.entity.DressEntity;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.entity.HeroEntity;
import com.hawk.game.entity.SuperSoldierEntity;
import com.hawk.game.entity.TalentEntity;
import com.hawk.game.entity.TechnologyEntity;
import com.hawk.game.entity.item.DressItem;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.lianmengcyb.CYBORGExtraParam;
import com.hawk.game.lianmengcyb.CYBORGRoomManager;
import com.hawk.game.lianmengcyb.msg.CYBORGQuitReason;
import com.hawk.game.lianmengcyb.player.CYBORGPlayer;
import com.hawk.game.lianmengcyb.roomstate.CYBORGGameOver;
import com.hawk.game.lianmengstarwars.SWBattleRoom;
import com.hawk.game.lianmengstarwars.SWExtraParam;
import com.hawk.game.lianmengstarwars.SWRoomManager;
import com.hawk.game.lianmengstarwars.msg.SWQuitReason;
import com.hawk.game.lianmengstarwars.player.SWPlayer;
import com.hawk.game.lianmengstarwars.roomstate.SWGameOver;
import com.hawk.game.lianmengxzq.XZQGift;
import com.hawk.game.lianmengxzq.XZQRedisData;
import com.hawk.game.lianmengxzq.XZQService;
import com.hawk.game.lianmengxzq.worldpoint.XZQWorldPoint;
import com.hawk.game.lianmengxzq.worldpoint.data.XZQCommander;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZBattleRoom;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZRoomManager;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZRoomManager.DYZZCAMP;
import com.hawk.game.module.dayazhizhan.battleroom.cfg.DYZZBattleCfg;
import com.hawk.game.module.dayazhizhan.battleroom.extry.DYZZExtraParam;
import com.hawk.game.module.dayazhizhan.battleroom.extry.DYZZGamer;
import com.hawk.game.module.dayazhizhan.battleroom.msg.DYZZQuitReason;
import com.hawk.game.module.dayazhizhan.battleroom.player.DYZZPlayer;
import com.hawk.game.module.dayazhizhan.battleroom.roomstate.DYZZGameOver;
import com.hawk.game.module.dayazhizhan.playerteam.service.DYZZService;
import com.hawk.game.module.lianmengXianquhx.XQHXExtraParam;
import com.hawk.game.module.lianmengXianquhx.XQHXRoomManager;
import com.hawk.game.module.lianmengXianquhx.roomstate.XQHXGameOver;
import com.hawk.game.module.lianmengfgyl.battleroom.FGYLExtraParam;
import com.hawk.game.module.lianmengfgyl.battleroom.FGYLRoomManager;
import com.hawk.game.module.lianmengfgyl.battleroom.roomstate.FGYLGameOver;
import com.hawk.game.module.lianmengtaiboliya.TBLYBattleRoom;
import com.hawk.game.module.lianmengtaiboliya.TBLYExtraParam;
import com.hawk.game.module.lianmengtaiboliya.TBLYRoomManager;
import com.hawk.game.module.lianmengtaiboliya.msg.QuitReason;
import com.hawk.game.module.lianmengtaiboliya.player.ITBLYPlayer;
import com.hawk.game.module.lianmengtaiboliya.player.TBLYPlayer;
import com.hawk.game.module.lianmengtaiboliya.roomstate.TBLYGameOver;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZBattleRoom;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZRoomManager;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZ_CAMP;
import com.hawk.game.module.lianmengyqzz.battleroom.extra.YQZZExtraParam;
import com.hawk.game.module.lianmengyqzz.battleroom.extra.YQZZNation;
import com.hawk.game.module.lianmengyqzz.battleroom.msg.YQZZQuitReason;
import com.hawk.game.module.lianmengyqzz.battleroom.player.YQZZPlayer;
import com.hawk.game.module.lianmengyqzz.battleroom.roomstate.YQZZGameOver;
import com.hawk.game.module.lianmenxhjz.battleroom.XHJZExtraParam;
import com.hawk.game.module.lianmenxhjz.battleroom.XHJZRoomManager;
import com.hawk.game.module.lianmenxhjz.battleroom.roomstate.XHJZGameOver;
import com.hawk.game.module.soldierExchange.SoldierExchangeUtil;
import com.hawk.game.player.Player;
import com.hawk.game.player.cache.PlayerDataKey;
import com.hawk.game.player.cache.PlayerDataSerializer;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.president.PresidentCity;
import com.hawk.game.president.PresidentFightService;
import com.hawk.game.president.model.President;
import com.hawk.game.protocol.Army.ArmySoldierPB;
import com.hawk.game.protocol.Building.BuildingUpdateOperation;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.BattleSkillType;
import com.hawk.game.protocol.Const.BuildingStatus;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Player.CrossPlayerStruct;
import com.hawk.game.protocol.SuperSoldier.PBSuperSoldierState;
import com.hawk.game.protocol.World.PlayerPresetMarchInfo;
import com.hawk.game.protocol.World.PresetMarchInfo;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.BuildingService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.PlayerImageService;
import com.hawk.game.service.mail.DungeonMailType;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventUnlockGround;
import com.hawk.game.service.starwars.StarWarsConst;
import com.hawk.game.service.starwars.StarWarsConst.SWWarType;
import com.hawk.game.service.starwars.StarWarsOfficerService;
import com.hawk.game.service.xhjzWar.XHJZWarPlayerData;
import com.hawk.game.util.GsConst.XZQAwardType;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.object.Point;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.service.WorldRobotService;
import com.hawk.log.Action;
import com.hawk.log.Source;

public class CreateFubenUtil {

	private static String lastCommandPlayerId;

	public static void imTheKing(Player player, String chatMsg) {
		if (!Objects.equals("@sw king", chatMsg)) {
			return;
		}

		CrossPlayerStruct.Builder crossBuilder = BuilderUtil.buildCrossPlayer(player);
		RedisProxy.getInstance().updateCrossPlayerStruct(player.getId(), crossBuilder.build(), 3600 * 24 * 2);
		StarWarsOfficerService.getInstance().onFighterOver(player.getId(), 104, StarWarsConst.WORLD_PART, GsConst.StarWarsConst.TEAM_NONE);
		StarWarsOfficerService.getInstance().loadOrReloadOfficer();
	}

	/** 模拟团战*/
	private static void testBattle(String chatMsg) {
		if (!chatMsg.startsWith("@testFight")) {
			return;
		}

		String[] args = chatMsg.split(" ");

		List<Player> atkPlayers = new ArrayList<>();
		// 防守方玩家
		List<Player> defPlayers = new ArrayList<>();
		// 进攻方行军
		List<IWorldMarch> atkMarchs = new ArrayList<>();
		// 防守方行军
		List<IWorldMarch> defMarchs = new ArrayList<>();

		for (String guildId : GuildService.getInstance().getGuildIds()) {
			String guildName = GuildService.getInstance().getGuildName(guildId);
			if (!Objects.equals(args[1], guildName) && !Objects.equals(args[2], guildName)) {
				continue;
			}
			Collection<String> guildMembers = GuildService.getInstance().getGuildMembers(guildId);
			List<Player> guildPlayers = new ArrayList<>();
			for (String playerId : guildMembers) {
				guildPlayers.add(GlobalData.getInstance().makesurePlayer(playerId));
			}
			Collections.sort(guildPlayers, new Comparator<Player>() {
				@Override
				public int compare(Player o1, Player o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});

			if (Objects.equals(args[1], guildName)) {
				for (Player player : guildPlayers) {
					buildMarch(player, atkPlayers, atkMarchs);
				}
			}

			if (Objects.equals(args[2], guildName)) {
				for (Player player : guildPlayers) {
					buildMarch(player, defPlayers, defMarchs);
				}
			}

		}

		// 战斗数据输入
		PvpBattleIncome battleIncome = BattleService.getInstance().initPVPBattleData(BattleConst.BattleType.ATTACK_PRESIDENT, 0, atkPlayers, defPlayers, atkMarchs, defMarchs,
				BattleSkillType.BATTLE_SKILL_NONE);
		battleIncome.getBattle().setDuntype(DungeonMailType.TBLY);
		if (args.length > 2 && args[3].equals("duel")) {
			try {
				Field f1 = HawkOSOperator.getClassField(battleIncome.getDefCalcParames(), "duel");
				f1.set(battleIncome.getDefCalcParames(), true);
				Field f2 = HawkOSOperator.getClassField(battleIncome.getAtkCalcParames(), "duel");
				f2.set(battleIncome.getAtkCalcParames(), true);

				Field f3 = HawkOSOperator.getClassField(battleIncome.getDefCalcParames(), "decDieBecomeInjury");
				f3.set(battleIncome.getDefCalcParames(), 1000000);

				Field f4 = HawkOSOperator.getClassField(battleIncome.getAtkCalcParames(), "decDieBecomeInjury");
				f4.set(battleIncome.getAtkCalcParames(), 1000000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// 战斗数据输出
		BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);
		FightMailService.getInstance().sendFightMail(WorldPointType.KING_PALACE_VALUE, battleIncome, battleOutcome, null);
	}

	private static IWorldMarch buildMarch(Player player, List<Player> atkPlayers, List<IWorldMarch> atkMarchs) {
		PlayerPresetMarchInfo.Builder infos = GameUtil.makeMarchPresetBuilder(player.getId());
		Optional<PresetMarchInfo> preSetInfo = infos.getMarchInfosList().stream().filter(pre -> pre.getIdx() == 1).findAny();
		if (!preSetInfo.isPresent()) {
			return null;
		}
		PresetMarchInfo info = preSetInfo.get();
		List<ArmyInfo> armys = new ArrayList<>();
		for (ArmySoldierPB arp : info.getArmyList()) {
			armys.add(new ArmyInfo(arp.getArmyId(), arp.getCount()));
		}

		TemporaryMarch atkMarch = new TemporaryMarch();
		atkMarch.setArmys(armys);
		atkMarch.setPlayer(player);
		atkMarch.getMarchEntity().setArmourSuit(info.getArmourSuit().getNumber());
		atkMarch.getMarchEntity().setMechacoreSuit(info.getMechacoreSuit().getNumber());
		atkMarch.getMarchEntity().setHeroIdList(info.getHeroIdsList());
		atkMarch.getMarchEntity().setSuperSoldierId(info.getSuperSoldierId());
		atkMarch.setHeros(player.getHeroByCfgId(info.getHeroIdsList()));
		atkMarch.getMarchEntity().setSuperLab(info.getSuperLab());
		atkMarch.getMarchEntity().setTalentType(info.getTalentType().getNumber());
		atkPlayers.add(atkMarch.getPlayer());
		atkMarchs.add(atkMarch);
		return atkMarch;
	}

	public static void addAllguildMembers(int fuben, String guildId) {
		int joinCnt = 0;
		Player from = GlobalData.getInstance().makesurePlayer(lastCommandPlayerId);
		for (String playerId : GuildService.getInstance().getGuildMembers(guildId)) {
			HawkOSOperator.osSleep(10);
			Player robot = GlobalData.getInstance().makesurePlayer(playerId);
			robot.getData().loadAll(false);
			if (!robot.isInDungeonMap()) {
				// copyArmy(from, robot);
				// if (robot.getLevel() < 20) {
				// CreateFubenUtil.doCopy(from, robot);
				// }

				AccountRoleInfo accountRoleInfo = GlobalData.getInstance().getAccountRoleInfo(robot.getId());
				if (Objects.isNull(accountRoleInfo)) {

					if (accountRoleInfo == null) {
						accountRoleInfo = AccountRoleInfo.newInstance().openId(robot.getOpenId()).playerId(robot.getId())
								.serverId(robot.getServerId()).platform(robot.getPlatform()).registerTime(robot.getCreateTime());
					}

					try {
						accountRoleInfo.playerName(robot.getName()).playerLevel(robot.getLevel()).cityLevel(robot.getCityLevel())
								.vipLevel(robot.getVipLevel()).battlePoint(robot.getPower()).activeServer(GsConfig.getInstance().getServerId())
								.icon(robot.getIcon()).loginWay(robot.getEntity().getLoginWay()).loginTime(HawkTime.getMillisecond())
								.logoutTime(robot.getLogoutTime());
						accountRoleInfo.pfIcon(PlayerImageService.getInstance().getPfIcon(robot));
					} catch (Exception e) {
						HawkException.catchException(e, robot.getId());
					}

					GlobalData.getInstance().addOrUpdateAccountRoleInfo(accountRoleInfo);

				}

				joinCnt++;
				if (fuben == 1) {
					TBLYRoomManager.getInstance().joinGame("abcd", robot);
					System.out.println("Join Tbly " + robot.getName() + " joinCnt:" + joinCnt);
				}
				if (fuben == 2) {
					CYBORGRoomManager.getInstance().joinGame("abcd", robot);
					System.out.println("Join Cyborg " + robot.getName() + " joinCnt:" + joinCnt);
					if (joinCnt >= 30) {
						break;
					}
				}
				if (fuben == 3) {
					SWRoomManager.getInstance().joinGame("tguyhnjko6", robot);
					System.out.println("Join Cyborg " + robot.getName() + " joinCnt:" + joinCnt);
				}
				if (fuben == 4) {
					YQZZRoomManager.getInstance().joinGame("abcd", robot);
					System.out.println("Join yqzz " + robot.getName() + " joinCnt:" + joinCnt);
				}
			}
		}
	}

	static void cyborg(Player player, String chatMsg) {
		try {

			if ("@cyborg join A".equals(chatMsg)) {
				System.out.println(player.getId() + " @TBLY JOIN A");
				if (!CYBORGRoomManager.getInstance().hasGame("abcd")) {
					CYBORGExtraParam extParm = new CYBORGExtraParam();
					extParm.setLeaguaWar(true);
					extParm.setCampAGuild(player.getGuildId());
					extParm.setCampAGuildName(player.getGuildName());
					extParm.setCampAGuildTag(player.getGuildTag());
					extParm.setCampAServerId(player.getMainServerId());
					extParm.setCampAguildFlag(player.getGuildFlag());
					/**-----------------------------------------------------------------------------------------------------------------------------------------------*/
					/**-----------------------------------------------------------------------------------------------------------------------------------------------*/
					List<String> guildIds = GuildService.getInstance().getGuildIds();
					guildIds.remove(player.getGuildId());

					extParm.setCampBGuild(guildIds.get(0));
					extParm.setCampBGuildName("eqwe");
					extParm.setCampBGuildTag("BigB");
					extParm.setCampBServerId(player.getMainServerId());
					extParm.setCampBguildFlag(12);

					extParm.setCampCGuild(guildIds.get(1));
					extParm.setCampCGuildName("fe2");
					extParm.setCampCGuildTag("fe2");
					extParm.setCampCServerId(player.getMainServerId());
					extParm.setCampCguildFlag(13);

					extParm.setCampDGuild(guildIds.get(2));
					extParm.setCampDGuildName("ooo");
					extParm.setCampDGuildTag("ooo");
					extParm.setCampDServerId(player.getMainServerId());
					extParm.setCampDguildFlag(8);
					CYBORGRoomManager.getInstance().creatNewBattle(HawkTime.getMillisecond(), HawkTime.getMillisecond() + 3000000, "abcd", extParm);

					// 1aas-sttg2-1 @@@ tag:fe2 name: fe2 flag: 13
					// 1aas-rovq7-1 @@@ tag:ooo name: oooo flag: 1
					// 1aas-ya9do-1 @@@ tag:sds name: sdsdas flag: 8
					// 1aas-17ehw7-1 @@@ tag:Bdd name: Bddd flag: 11
					// 7px-qz2wx-1 @@@ tag:eqw name: eqwe flag: 12
					// 7px-mi6x0-1 @@@ tag:DDD name: DDD flag: 2

					Thread.sleep(1000);
					// addAllguildMembers(2, extParm.getCampAGuild());
					// addAllguildMembers(2, extParm.getCampBGuild());
					// addAllguildMembers(2, extParm.getCampCGuild());
					// addAllguildMembers(2, extParm.getCampDGuild());
				}
				CYBORGRoomManager.getInstance().joinGame("abcd", player);
			}

			if ("@cyborg quit".equals(chatMsg)) {
				CYBORGPlayer tp = (CYBORGPlayer) CYBORGRoomManager.getInstance().makesurePlayer(player.getId());
				tp.getParent().quitWorld(tp, CYBORGQuitReason.LEAVE);
			}

			if ("@cyborg over".equals(chatMsg)) {
				CYBORGRoomManager.getInstance().findAllRoom().forEach(room -> room.setState(new CYBORGGameOver(room)));
				;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void xzqOccupy(Player player, String chatMsg) {
		if (!GsConfig.getInstance().isDebug()) {
			return;
		}

		if (chatMsg.startsWith("@xzqOccupy-")) {
			String id = chatMsg.split("-")[1];
			int xzqId = Integer.parseInt(id);
			XZQPointCfg pointcfg = HawkConfigManager.getInstance().getConfigByKey(XZQPointCfg.class, xzqId);
			if (pointcfg == null) {
				return;
			}
			String guildId = GuildService.getInstance().getPlayerGuildId(player.getId());
			if (HawkOSOperator.isEmptyString(guildId)) {
				return;
			}
			int lid = GameUtil.combineXAndY(pointcfg.getX(), pointcfg.getY());
			XZQWorldPoint point = XZQService.getInstance().getXZQPoint(lid);
			if (point == null) {
				return;
			}
			int turnCount = XZQService.getInstance().getXZQTermId();
			// 礼包
			List<XZQAwardCfg> leaderSendAwardCfgs = null;
			if (point.isInitOccupyed()) {
				leaderSendAwardCfgs = AssembleDataManager.getInstance().getXZQFirstAwards(point.getId(), XZQAwardType.ATTACK_LEADER_SEND_AWARD);
			} else {
				leaderSendAwardCfgs = AssembleDataManager.getInstance().getXZQAwards(point.getId(), XZQAwardType.ATTACK_LEADER_SEND_AWARD);
			}
			for (XZQAwardCfg cfg : leaderSendAwardCfgs) {
				String xzqGiftInfo = XZQRedisData.getInstance().getXZQGiftInfo(turnCount, guildId, point.getXzqCfg().getId(), cfg.getId());
				String afterInfo = null;
				if (HawkOSOperator.isEmptyString(xzqGiftInfo)) {
					int sendCount = 0;
					int totalCount = cfg.getTotalNumber();
					afterInfo = String.valueOf(sendCount) + "_" + totalCount;
				} else {
					String[] splitInfo = xzqGiftInfo.split("_");
					int sendCount = Integer.parseInt(splitInfo[0]);
					int totalCount = Integer.parseInt(splitInfo[1]) + cfg.getTotalNumber();
					afterInfo = String.valueOf(sendCount) + "_" + totalCount;
				}
				XZQRedisData.getInstance().updateXZQGiftInfo(turnCount, guildId, point.getXzqCfg().getId(), cfg.getId(), afterInfo);
			}
			point.addOccupuHistory(guildId);
			point.updateWorldScene();

		}
	}

	public static void xzqSignup(Player player, String chatMsg) {
		if (!GsConfig.getInstance().isDebug()) {
			return;
		}

		if (chatMsg.startsWith("@xzqSign-")) {
			String id = chatMsg.split("-")[1];
			int xzqId = Integer.parseInt(id);
			XZQPointCfg pointcfg = HawkConfigManager.getInstance().getConfigByKey(XZQPointCfg.class, xzqId);
			if (pointcfg == null) {
				return;
			}
			String guildId = GuildService.getInstance().getPlayerGuildId(player.getId());
			if (HawkOSOperator.isEmptyString(guildId)) {
				return;
			}
			int lid = GameUtil.combineXAndY(pointcfg.getX(), pointcfg.getY());
			XZQWorldPoint point = XZQService.getInstance().getXZQPoint(lid);
			if (point == null) {
				return;
			}
			point.signup(guildId);
			point.updateWorldScene();

		}
	}

	public static void xzqControl(Player player, String chatMsg) {
		if (!GsConfig.getInstance().isDebug()) {
			return;
		}

		if (chatMsg.startsWith("@xzqControl-")) {
			String id = chatMsg.split("-")[1];
			int xzqId = Integer.parseInt(id);
			XZQPointCfg pointcfg = HawkConfigManager.getInstance().getConfigByKey(XZQPointCfg.class, xzqId);
			if (pointcfg == null) {
				return;
			}
			String guildId = GuildService.getInstance().getPlayerGuildId(player.getId());
			if (HawkOSOperator.isEmptyString(guildId)) {
				return;
			}
			int lid = GameUtil.combineXAndY(pointcfg.getX(), pointcfg.getY());
			XZQWorldPoint point = XZQService.getInstance().getXZQPoint(lid);
			if (point == null) {
				return;
			}
			GuildInfoObject guildInfo = GuildService.getInstance().getGuildInfoObject(guildId);
			XZQCommander commander = new XZQCommander();
			commander.setPlayerId(player.getId());
			commander.setPlayerName(player.getName());
			commander.setPlayerGuildId(player.getGuildId());
			commander.setPlayerGuildName(guildInfo.getName());
			commander.setTermId(1);
			commander.setIcon(player.getIcon());
			commander.setPfIcon(player.getPfIcon());
			point.updateXZQCommander(commander);
			point.updateWorldScene();
			XZQGift.getInstance().sendControlAward(point);
			XZQService.getInstance().updateZXQEffect();
			XZQService.getInstance().updateXZQForceColor(true);
		}

	}

	public static void clearDyzzTeam(Player player, String chatMsg) {
		if (chatMsg.startsWith("@dyzzclear")) {
			DYZZService.getInstance().clear();
		}

	}

	public static void country(Player player, String chatMsg) {
		try {
			if (!GsConfig.getInstance().isDebug()) {
				return;
			}

			if ("@country setPresident".equals(chatMsg)) {
				System.out.println(player.getId() + " @country setPresident");
				if (HawkOSOperator.isEmptyString(player.getGuildId())) {
					System.out.println(player.getId() + "need join guild, @country setPresident");
					return;
				}
				// 判断当前有没有国王，没有国王就设置国王
				if (HawkOSOperator.isEmptyString(PresidentFightService.getInstance().getPresidentPlayerId())) {
					// 设置当前号为国王

					PresidentCity city = PresidentFightService.getInstance().getPresidentCity();
					if (city.getPresident() == null) {
						city.setPresident(new President());
					}
					city.chanagePresident(player);
				}
			}
			if ("@country clearPresident".equals(chatMsg)) {
				System.out.println(player.getId() + " @country clearPresident");
				// 清除国王
				PresidentFightService.getInstance().getPresidentCity().clearPresident();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void fuben(Player player, String chatMsg) {
		lastCommandPlayerId = player.getId();
		try {
			xqhx(player,chatMsg);
			fgyl(player,chatMsg);
			xhjz(player,chatMsg);
			testZhuanbingZhong(player, chatMsg);
			testBattle(chatMsg);
			testBattleChecker(player, chatMsg);
			imTheKing(player, chatMsg);
			cyborg(player, chatMsg);
			country(player, chatMsg);
			xzqSignup(player, chatMsg);
			xzqControl(player, chatMsg);
			xzqOccupy(player, chatMsg);
			// xlzj(player,chatMsg);
			dyzz(player, chatMsg);
			yqzz(player, chatMsg);
			armour(player, chatMsg);
			clearDyzzTeam(player, chatMsg);
			honourHeroLottery(player, chatMsg);
			if ("@tbly join A".equals(chatMsg)) {
				System.out.println(player.getId() + " @TBLY JOIN A");
				if (!TBLYRoomManager.getInstance().hasGame("abcd")) {
					TBLYExtraParam extParm = new TBLYExtraParam();
					extParm.setCampAGuild(player.getGuildId());
					extParm.setCampAGuildName(player.getGuildName());
					extParm.setCampAGuildTag(player.getGuildTag());
					extParm.setCampAServerId(player.getMainServerId());
					extParm.setCampAguildFlag(player.getGuildFlag());
					/**-----------------------------------------------------------------------------------------------------------------------------------------------*/
					/**-----------------------------------------------------------------------------------------------------------------------------------------------*/
					extParm.setCampBGuild("1aas-1wn6m3-1");
					extParm.setCampBGuildName("AAAA");
					extParm.setCampBGuildTag("AAA");
					extParm.setCampBServerId(player.getMainServerId());
					extParm.setCampBguildFlag(player.getGuildFlag());
					TBLYRoomManager.getInstance().creatNewBattle(HawkTime.getMillisecond(), HawkTime.getMillisecond() + 3600_000, "abcd", extParm);

					Thread.sleep(1000);
					// addAllguildMembers(1, player.getGuildId());
					// addAllguildMembers(1, "1aas-1wn6m3-1");
				}
				TBLYRoomManager.getInstance().joinGame("abcd", player);
			}

			if ("@tbly quit".equals(chatMsg)) {
				TBLYPlayer tp = (TBLYPlayer) TBLYRoomManager.getInstance().makesurePlayer(player.getId());
				tp.getParent().quitWorld(tp, QuitReason.LEAVE);
			}

			if ("@tbly over".equals(chatMsg)) {
				TBLYRoomManager.getInstance().findAllRoom().forEach(room -> room.setState(new TBLYGameOver(room)));
				;
			}

			if ("@tbly join Anchor".equals(chatMsg)) {
				final String battleId = "abcd";
				HawkXID roomXid = HawkXID.valueOf(GsConst.ObjType.TBLYAOGUAN_ROOM, battleId);
				HawkObjBase<HawkXID, HawkAppObj> roomObj = GsApp.getInstance().getObjMan(GsConst.ObjType.TBLYAOGUAN_ROOM).queryObject(roomXid);
				TBLYBattleRoom room = null;
				if (roomObj != null) {
					room = (TBLYBattleRoom) roomObj.getImpl();
				} else {
					throw new RuntimeException("game not fount id = " + battleId);
				}
				player.setTBLYRoomId(roomXid.getUUID());

				ITBLYPlayer hp = new TBLYPlayer(player);
				hp.setParent(room);
				room.anchorJoinRoom(hp);
			}

			if ("@sw join A".equals(chatMsg)) {
				String gid = "tguyhnjko6";
				if (!SWRoomManager.getInstance().hasGame(gid)) {
					SWExtraParam extParm = new SWExtraParam();
					extParm.setDebug(true);
					extParm.setWarType(SWWarType.FIRST_WAR);
					SWRoomManager.getInstance().creatNewBattle(HawkTime.getMillisecond(), HawkTime.getMillisecond() + 3600 * 1000, gid, extParm);
					SWRoomManager.getInstance().joinGame(gid, player);

					 HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
					 @Override
					 public Object run() {
					 int joinCnt = 0;
					 for (String guildiId : GuildService.getInstance().getGuildIds()) {
					 addAllguildMembers(3, guildiId);
					 joinCnt += GuildService.getInstance().getGuildMembers(guildiId).size();
					 if(joinCnt> 500){
					 break;
					 }
					 }
					 return null;
					 }
					 });

				} else {
					SWRoomManager.getInstance().joinGame(gid, player);
				}
			}

			if ("@sw quit".equals(chatMsg)) {
				SWPlayer tp = (SWPlayer) SWRoomManager.getInstance().makesurePlayer(player.getId());
				tp.getParent().quitWorld(tp, SWQuitReason.LEAVE);
			}

			if ("@sw over".equals(chatMsg)) {
				for (SWBattleRoom swroom : SWRoomManager.getInstance().findAllRoom()) {
					SWGameOver swstate = new SWGameOver(swroom);
					swstate.setKillGame(true);
					swroom.setState(swstate);
				}

			}
			if ("@sw make guild".equals(chatMsg)) {
				Set<String> playerIds = GlobalData.getInstance().getAllPlayerIds();
				int i = 0;
				for (String id : playerIds) {
					Player pl = GlobalData.getInstance().makesurePlayer(id);
					String guildid = GuildService.getInstance().getPlayerGuildId(id);
					String serverId = GsConfig.getInstance().getServerId();
					if (!pl.getServerId().equals(serverId)) {
						continue;
					}
					if (guildid == null || guildid.equals("")) {
						i++;
						HawkTuple2<Integer, String> rlt = GuildService.getInstance().onQuickJoinGuild(pl);
						System.out.println("guild join:" + id + ">" + rlt.first);
					}
				}
				System.out.println("guild join:" + i);
			}
			if ("@sw make robot".equals(chatMsg)) {
				for (String guildiId : GuildService.getInstance().getGuildIds()) {

					for (String playerId : GuildService.getInstance().getGuildMembers(guildiId)) {
						Player robot = GlobalData.getInstance().makesurePlayer(playerId);

						robot.getEntity().setVipExp(1000000000);
						robot.getEntity().setVipLevel(18);
						robot.getPlayerBaseEntity().setExp(186553636);
						robot.getPlayerBaseEntity().setLevel(50);
						// 科技满级
						techLevelUp(robot);

						// 建筑满级
						buildUpdate(robot);
					}
				}
			}

			if ("@mail create".equals(chatMsg)) {
				for (int i = 0; i < 100; i++) {
					Thread.sleep(1);
					MailParames.Builder mailParames = MailParames.newBuilder().setMailId(MailId.VIP_BENEFIT_BOX_REFRESH)
							.setPlayerId(player.getId()).setRewards("10000_1007_666").setAwardStatus(MailRewardStatus.NOT_GET);
					SystemMailService.getInstance().sendMail(mailParames.build());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	private static void xqhx(Player player, String chatMsg) {
		if (!chatMsg.startsWith("@xqhx")) {
			return;
		}
		
		if ("@xqhx join A".equals(chatMsg)) {
			System.out.println(player.getId() + " @xqhx JOIN A");
			if (!XQHXRoomManager.getInstance().hasGame("xqhx")) {
				XQHXExtraParam extParm = new XQHXExtraParam();
				extParm.setCampAGuild(player.getGuildId());
				extParm.setCampAGuildName(player.getGuildName());
				extParm.setCampAGuildTag(player.getGuildTag());
				extParm.setCampAServerId(player.getMainServerId());
				extParm.setCampAguildFlag(player.getGuildFlag());
				/**-----------------------------------------------------------------------------------------------------------------------------------------------*/
				/**-----------------------------------------------------------------------------------------------------------------------------------------------*/
				extParm.setCampBGuild("1aas-1wn6m3-1");
				extParm.setCampBGuildName("BBBBB");
				extParm.setCampBGuildTag("BB");
				extParm.setCampBServerId(player.getMainServerId());
				extParm.setCampBguildFlag(player.getGuildFlag());
				XQHXRoomManager.getInstance().creatNewBattle(HawkTime.getMillisecond(), HawkTime.getMillisecond() + 3000000, "xhjz", extParm);
			}
			XQHXRoomManager.getInstance().joinGame("xhjz", player);
		}

		if ("@xqhx over".equals(chatMsg)) {
			XQHXRoomManager.getInstance().findAllRoom().forEach(room -> room.setState(new XQHXGameOver(room)));
		}
		
	}
	private static void xhjz(Player player, String chatMsg) {
		if (!chatMsg.startsWith("@xhjz")) {
			return;
		}
		
		if ("@xhjz join A".equals(chatMsg)) {
			System.out.println(player.getId() + " @XHJZ JOIN A");
			if (!XHJZRoomManager.getInstance().hasGame("xhjz")) {
				XHJZExtraParam extParm = new XHJZExtraParam();
				extParm.setCampAGuild(player.getGuildId());
				extParm.setCampAGuildName(player.getGuildName());
				extParm.setCampAGuildTag(player.getGuildTag());
				extParm.setCampAServerId(player.getMainServerId());
				extParm.setCampAguildFlag(player.getGuildFlag());
				extParm.getCampACommonder().add(new XHJZWarPlayerData(player));
				extParm.setTeamAName("韩立");
				/**-----------------------------------------------------------------------------------------------------------------------------------------------*/
				/**-----------------------------------------------------------------------------------------------------------------------------------------------*/
				extParm.setCampBGuild("1aas-1wn6m3-1");
				extParm.setCampBGuildName("BBBBB");
				extParm.setCampBGuildTag("BB");
				extParm.setCampBServerId(player.getMainServerId());
				extParm.setCampBguildFlag(player.getGuildFlag());
				extParm.setTeamBName("南宫碗");
				XHJZRoomManager.getInstance().creatNewBattle(HawkTime.getMillisecond(), HawkTime.getMillisecond() + 3000000, "xhjz", extParm);

				// addAllguildMembers(1, player.getGuildId());
				// addAllguildMembers(1, "1aas-1wn6m3-1");
			}
			XHJZRoomManager.getInstance().joinGame("xhjz", player);
		}

//		if ("@tbly quit".equals(chatMsg)) {
//			TBLYPlayer tp = (TBLYPlayer) TBLYRoomManager.getInstance().makesurePlayer(player.getId());
//			tp.getParent().quitWorld(tp, QuitReason.LEAVE);
//		}

		if ("@xhjz over".equals(chatMsg)) {
			XHJZRoomManager.getInstance().findAllRoom().forEach(room -> room.setState(new XHJZGameOver(room)));
			;
		}
		
	}
	private static void fgyl(Player player, String chatMsg) {
		if (!chatMsg.startsWith("@fgyl")) {
			return;
		}
		
		if (chatMsg.startsWith("@fgyl join")) {
			System.out.println(player.getId() + " @fgyl JOIN A");
			if (!FGYLRoomManager.getInstance().hasGame("fgyl")) {
				String[] params = chatMsg.split(" ");
				int dif = NumberUtils.toInt(params[2], 1);
				FGYLExtraParam extParm = new FGYLExtraParam();
				extParm.setDifficult(dif);
				
				extParm.setCampAGuild(player.getGuildId());
				extParm.setCampAGuildName(player.getGuildName());
				extParm.setCampAGuildTag(player.getGuildTag());
				extParm.setCampAServerId(player.getMainServerId());
				extParm.setCampAguildFlag(player.getGuildFlag());
				extParm.setTeamAName("韩立");
				/**-----------------------------------------------------------------------------------------------------------------------------------------------*/
				/**-----------------------------------------------------------------------------------------------------------------------------------------------*/
				extParm.setCampBGuild("1aas-1wn6m3-1");
				extParm.setCampBGuildName("BBBBB");
				extParm.setCampBGuildTag("BB");
				extParm.setCampBServerId(player.getMainServerId());
				extParm.setCampBguildFlag(player.getGuildFlag());
				extParm.setTeamBName("南宫碗");
				FGYLRoomManager.getInstance().creatNewBattle(HawkTime.getMillisecond(), HawkTime.getMillisecond() + 1800000, "fgyl", extParm);

				// addAllguildMembers(1, player.getGuildId());
				// addAllguildMembers(1, "1aas-1wn6m3-1");
			}
			FGYLRoomManager.getInstance().joinGame("fgyl", player);
		}

//		if ("@tbly quit".equals(chatMsg)) {
//			TBLYPlayer tp = (TBLYPlayer) TBLYRoomManager.getInstance().makesurePlayer(player.getId());
//			tp.getParent().quitWorld(tp, QuitReason.LEAVE);
//		}

		if ("@fgyl over".equals(chatMsg)) {
			FGYLRoomManager.getInstance().findAllRoom().forEach(room -> room.setState(new FGYLGameOver(room)));
			;
		}
		
	}

	private static void testZhuanbingZhong(Player player, String chatMsg) {
		if (!chatMsg.startsWith("@bzzh")) {
			return;
		}
		String[] args = chatMsg.split(" ");

		SoldierType fromType = SoldierType.valueOf(Integer.valueOf(args[1]));
		SoldierType toType = SoldierType.valueOf(Integer.valueOf(args[2]));
		SoldierExchangeUtil util = SoldierExchangeUtil.create(player,fromType,toType);
		util.zhuanArmy(true);
		util.zhuanBuild(true);
		util.zhuanPlantSchool(true);
		util.zhuanSuperSoldier();
		util.zhuanArmour();
		util.zhuanMechaCore(player);
		util.zhuanPlantTech();
		util.zhuanBuild(false);
		util.zhuanPlantSchool(false);
		util.zhuanArmy(false);

	}

	private static void dyzz(Player player, String chatMsg) {
		if ("@dyzz join A".equals(chatMsg) || "@dyzz join B".equals(chatMsg)) {
			System.out.println(player.getId() + " @TBLY JOIN A");
			if (!DYZZRoomManager.getInstance().hasGame("abcd")) {
				DYZZExtraParam extParm = new DYZZExtraParam();
				extParm.setBattleId("abcd");

				adddyzzplayer(extParm, player, chatMsg);

				DYZZRoomManager.getInstance().creatNewBattle(HawkTime.getMillisecond(), extParm);

				// 1aas-sttg2-1 @@@ tag:fe2 name: fe2 flag: 13
				// 1aas-rovq7-1 @@@ tag:ooo name: oooo flag: 1
				// 1aas-ya9do-1 @@@ tag:sds name: sdsdas flag: 8
				// 1aas-17ehw7-1 @@@ tag:Bdd name: Bddd flag: 11
				// 7px-qz2wx-1 @@@ tag:eqw name: eqwe flag: 12
				// 7px-mi6x0-1 @@@ tag:DDD name: DDD flag: 2

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				HawkXID roomXid = HawkXID.valueOf(GsConst.ObjType.DYZZAOGUAN_ROOM, "abcd");
				HawkObjBase<HawkXID, HawkAppObj> roomObj = GsApp.getInstance().getObjMan(GsConst.ObjType.DYZZAOGUAN_ROOM).queryObject(roomXid);
				DYZZBattleRoom room = (DYZZBattleRoom) roomObj.getImpl();
				DYZZExtraParam extParm = room.getExtParm();
				adddyzzplayer(extParm, player, chatMsg);
			}
			DYZZRoomManager.getInstance().joinGame("abcd", player);
		}

		if ("@dyzz quit".equals(chatMsg)) {
			DYZZPlayer tp = (DYZZPlayer) DYZZRoomManager.getInstance().makesurePlayer(player.getId());
			tp.getParent().quitWorld(tp, DYZZQuitReason.LEAVE);
		}

		if ("@dyzz over".equals(chatMsg)) {
			for (DYZZBattleRoom room : DYZZRoomManager.getInstance().findAllRoom()) {
				room.setWinCamp(DYZZCAMP.A);
				room.setState(new DYZZGameOver(room));
			}
		}

		if ("@dyzz kda".equals(chatMsg)) {
			DYZZBattleCfg cfg = HawkConfigManager.getInstance().getKVInstance(DYZZBattleCfg.class);
			DYZZPlayer tp = (DYZZPlayer) DYZZRoomManager.getInstance().makesurePlayer(player.getId());

			HawkTuple3<Double, Double, Double> sp3 = cfg.getScoreparameter3();
			// return (int) (killCount * sp3.first + collectHonor * sp3.second + hurtTankCount * sp3.third);
			String str = "kad :  " + tp.getKillCount() + "*" + sp3.first + " + " + tp.getCollectHonor() + "*" + sp3.second + " + " + tp.getHurtTankCount() + " * " + sp3.third
					+ " = " + tp.getKda();
			tp.getParent().sendChatMsg(tp, str, "", 0, Const.ChatType.CHAT_FUBEN);
		}

	}

	private static void adddyzzplayer(DYZZExtraParam extParm, Player player, String chatMsg) {
		DYZZGamer gamer1 = new DYZZGamer();
		gamer1.setPlayerId(player.getId());
		if ("@dyzz join A".equals(chatMsg)) {
			gamer1.setCamp(DYZZCAMP.A);
		} else {
			gamer1.setCamp(DYZZCAMP.B);
		}

		int[] armys = new int[] { 100107, 100207, 100307, 100407, 100507, 100607, 100707, 100807 };
		for (int arid : armys) {
			ArmyInfo army = new ArmyInfo();
			army.setArmyId(arid);
			army.setTotalCount(arid * 100);
			gamer1.getArmys().add(army);
		}

		gamer1.getFoggyHeros().addAll(Arrays.asList(1001005, 1002005, 1017012, 1021003, 3000038));

		extParm.addGamer(gamer1);
	}

	// private static void xlzj(Player player, String chatMsg) {
	// if ("@xlzj join A".equals(chatMsg)) {
	// String gid = "tguyhnjko6";
	// if (!XLZJRoomManager.getInstance().hasGame(gid)) {
	// XLZJExtraParam extParm = new XLZJExtraParam();
	// List<XLZJGuildJoin> joinGuilds = new ArrayList<>(30);
	// for (String guildiId : GuildService.getInstance().getGuildIds()) {
	// GetGuildInfoResp ginfo = GuildService.getInstance().getGuildInfo(guildiId).build();
	// XLZJGuildJoin jg = new XLZJGuildJoin();
	// jg.setGuild(ginfo.getId());
	// jg.setGuildName(ginfo.getName());
	// jg.setGuildFlag(ginfo.getFlag());
	// jg.setGuildTag(ginfo.getTag());
	// jg.setServerId( GsConfig.getInstance().getServerId());
	//
	// joinGuilds.add(jg);
	//// private String guild = "";
	//// private String guildName = "";
	//// private String guildTag = "";
	//// private String serverId = "";
	//// private int guildFlag;
	// }
	// extParm.setJoinGuilds(joinGuilds);
	//
	// XLZJRoomManager.getInstance().creatNewBattle(HawkTime.getMillisecond(), HawkTime.getMillisecond() + 3600 * 1000, gid, extParm);
	//
	//// int joinCnt = 0;
	//// for (String guildiId : GuildService.getInstance().getGuildIds()) {
	//// addAllguildMembers(3, guildiId);
	//// joinCnt += GuildService.getInstance().getGuildMembers(guildiId).size();
	//// if(joinCnt> 700){
	//// break;
	//// }
	//// }
	// }
	// XLZJRoomManager.getInstance().joinGame(gid, player);
	// }
	//
	// if ("@xlzj quit".equals(chatMsg)) {
	// XLZJPlayer tp = (XLZJPlayer) XLZJRoomManager.getInstance().makesurePlayer(player.getId());
	// tp.getParent().quitWorld(tp, XLZJQuitReason.LEAVE);
	// }
	//
	// if ("@xlzj over".equals(chatMsg)) {
	// XLZJRoomManager.getInstance().findAllRoom().forEach(room -> room.setState(new XLZJGameOver(room)));;
	// }
	//
	// }

	public static void honourHeroLottery(Player player, String chatMsg) {
		if (!GsConfig.getInstance().isDebug()) {
			return;
		}
		if (chatMsg.startsWith("@HonourHeroLottery")) {
			String countStr = chatMsg.split("-")[1];
			int count = Integer.parseInt(countStr);

			Map<Integer, Integer> list = new HashMap<Integer, Integer>();
			List<HonourHeroBefellRewardCfg> cfgList = HawkConfigManager.getInstance().getConfigIterator(HonourHeroBefellRewardCfg.class).toList();
			for (int i = 1; i <= count; i++) {
				HonourHeroBefellRewardCfg cfg = HawkRand.randomWeightObject(cfgList);
				int perCount = list.getOrDefault(cfg.getId(), 0);
				list.put(cfg.getId(), perCount + 1);
			}

			for (int key : list.keySet()) {
				int value = list.get(key);
				HawkLog.logPrintln("HonourHeroLottery-test,config:{},count:{}", key, value);
			}
		}
	}

	private static void testBattleChecker(Player player, String chatMsg) {
		if (chatMsg.startsWith("@copyme")) {
			String[] arr = chatMsg.split(" ");
			int randX = NumberUtils.toInt(arr[1]);
			int randY = NumberUtils.toInt(arr[2]);
			int pointId = GameUtil.combineXAndY(randX, randY);
			Point point = WorldPointService.getInstance().getAreaPoint(randX, randY, true);

			Player robot = WorldRobotService.getInstance().createRobotPlayer(player.getId(), pointId);

			// 创建世界点对象
			WorldPoint worldPoint = new WorldPoint(point.getX(), point.getY(), point.getAreaId(), point.getZoneId(), WorldPointType.PLAYER_VALUE);
			worldPoint.setPlayerId(robot.getId());
			worldPoint.setPlayerName(robot.getName());
			worldPoint.setCityLevel(robot.getCityLevel());
			worldPoint.setPlayerIcon(robot.getIcon());
			worldPoint.setLifeStartTime(HawkTime.getMillisecond());
			worldPoint.setProtectedEndTime(0);
			worldPoint.setMonsterId(9);
			WorldPointService.getInstance().addPoint(worldPoint);

		}

		if ("@cksoldier".equals(chatMsg)) {
			ConfigIterator<BuildingCfg> it = HawkConfigManager.getInstance().getConfigIterator(BuildingCfg.class);
			for (BuildingCfg cfg : it) {
				if (cfg.getBuildType() == BuildingType.PRISM_TOWER_VALUE) {// 如果是尖塔 会有两个
					System.out.println(cfg.getId());
					BattleSoldierHonorCfg solderStarCfg = HawkConfigManager.getInstance().getCombineConfig(BattleSoldierHonorCfg.class, cfg.getBattleSoldierId(), cfg.getHonor(),
							0);
					Preconditions.checkNotNull(solderStarCfg, "BattleSoldierHonorCfg cfg can not be null soldierId = " + cfg.getBattleSoldierId() + " star = " + cfg.getHonor());
				}
			}

		}

	}

	public static String doCopy(Player from, Player to) {

		to.getEntity().setVipExp(1000000000);
		to.getEntity().setVipLevel(18);
		to.getPlayerBaseEntity().setExp(186553636);
		to.getPlayerBaseEntity().setLevel(50);

		// 复制英雄
		copyHero(from, to);

		// 复制装备
		// copyEquip(from, to);

		copyArmy(from, to);
		// copyDress(from, to);
		// copyItem(from, to);
		// copyTalent(from, to);
		// 玩家满级
		playerUp(to);
		copyDL(from, to);
		return "ok";
	}

	/**
	 * 获取玩家
	 */
	public Player getPlayer(String playerId, String playerName) {
		if (HawkOSOperator.isEmptyString(playerId) && !HawkOSOperator.isEmptyString(playerName)) {
			playerId = GameUtil.getPlayerIdByName(playerName);
		}
		return GlobalData.getInstance().scriptMakesurePlayer(playerId);
	}

	/**
	 * 复制英雄
	 */
	public static void copyHero(Player from, Player to) {

		try {

			// 序列化
			byte[] data = PlayerDataSerializer.serializeData(PlayerDataKey.HeroEntityList, from.getData().getDataCache().getData(PlayerDataKey.HeroEntityList));

			// 反序列化
			Object result = PlayerDataSerializer.unserializeData(PlayerDataKey.HeroEntityList, data, false);

			@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>) result;
			for (Object obj : list) {

				HeroEntity heroEntity = (HeroEntity) obj;
				heroEntity.setId(HawkUUIDGenerator.genUUID());
				heroEntity.setCreateTime(0);
				heroEntity.setUpdateTime(0);
				heroEntity.setPlayerId(to.getId());
				heroEntity.setPersistable(true);

				to.getData().getHeroEntityList().add(heroEntity);

				PlayerHero hero = PlayerHero.create(heroEntity);
				HawkDBManager.getInstance().create(heroEntity);
				hero.notifyChange();
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 复制装备
	 */
	public static void copyEquip(Player from, Player to) {

		try {
			// 序列化
			byte[] data = PlayerDataSerializer.serializeData(PlayerDataKey.ArmourEntities, from.getData().getDataCache().getData(PlayerDataKey.ArmourEntities));

			// 反序列化
			Object result = PlayerDataSerializer.unserializeData(PlayerDataKey.ArmourEntities, data, false);

			@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>) result;
			for (Object obj : list) {

				ArmourEntity armourEntity = (ArmourEntity) obj;
				armourEntity.setId(HawkUUIDGenerator.genUUID());
				armourEntity.setPlayerId(to.getId());
				armourEntity.setUpdateTime(0);
				armourEntity.setCreateTime(0);
				armourEntity.setPersistable(true);

				armourEntity.create();
				to.getData().getArmourEntityList().add(armourEntity);
				to.getPush().syncArmourInfo(armourEntity);
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 复制士兵
	 */
	public static void copyArmy(Player from, Player player) {

		try {
			player.getData().getArmyEntities().forEach(e -> e.delete());
			player.getData().getArmyEntities().clear();

			ConfigIterator<BattleSoldierCfg> armyit = HawkConfigManager.getInstance().getConfigIterator(BattleSoldierCfg.class);

			for (BattleSoldierCfg arCfg : armyit) {
				if (arCfg.isDefWeapon() || arCfg.getLevel() < 10) {
					continue;
				}
				ArmyEntity armyEntity = player.getData().getArmyEntity(arCfg.getId());
				if (armyEntity == null) {
					armyEntity = new ArmyEntity();
					armyEntity.setPlayerId(player.getId());
					armyEntity.setArmyId(arCfg.getId());
					armyEntity.addFree(2000000);
					if (HawkDBManager.getInstance().create(armyEntity)) {
						player.getData().addArmyEntity(armyEntity);
					}
				} else {
					armyEntity.addFree(2000000);
				}

			}

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 复制装扮
	 */
	public static void copyDress(Player from, Player to) {

		try {
			// 序列化
			byte[] data = PlayerDataSerializer.serializeData(PlayerDataKey.DressEntity, from.getData().getDataCache().getData(PlayerDataKey.DressEntity));

			// 反序列化
			Object result = PlayerDataSerializer.unserializeData(PlayerDataKey.DressEntity, data, false);

			DressEntity dressEntity = (DressEntity) result;
			for (DressItem dressItem : dressEntity.getDressInfo()) {
				to.getData().getDressEntity().addOrUpdateDressInfo(dressItem.getDressType(), dressItem.getModelType(), dressItem.getContinueTime());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 复制物品
	 */
	public static void copyItem(Player from, Player to) {

		// try {
		// // 序列化
		// byte[] data = PlayerDataSerializer.serializeData(PlayerDataKey.ItemCont, from.getData().getDataCache().getData(PlayerDataKey.ItemEntities));
		//
		// // 反序列化
		// Object result = PlayerDataSerializer.unserializeData(PlayerDataKey.ItemEntities, data, false);
		//
		// @SuppressWarnings("unchecked")
		// List<Object> list = (List<Object>) result;
		// for (Object obj : list) {
		//
		// ItemEntity itemEntity = (ItemEntity) obj;
		// itemEntity.setId(HawkUUIDGenerator.genUUID());
		// itemEntity.setPlayerId(to.getId());
		// itemEntity.setPersistable(true);
		//
		// itemEntity.create();
		// to.getData().getItemEntities().add(itemEntity);
		// to.getPush().syncItemInfo();
		// }
		//
		// } catch (Exception e) {
		// HawkException.catchException(e);
		// }
	}

	/**
	 * 复制天赋
	 */
	public static void copyTalent(Player from, Player to) {

		try {
			// 序列化
			byte[] data = PlayerDataSerializer.serializeData(PlayerDataKey.TalentEntities, from.getData().getDataCache().getData(PlayerDataKey.TalentEntities));

			// 反序列化
			Object result = PlayerDataSerializer.unserializeData(PlayerDataKey.TalentEntities, data, false);

			@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>) result;
			for (Object obj : list) {

				TalentEntity talentEntity = (TalentEntity) obj;
				talentEntity.setId(HawkUUIDGenerator.genUUID());
				talentEntity.setPlayerId(to.getId());
				talentEntity.setPersistable(true);

				talentEntity.create();

				to.getData().getTalentEntities().add(talentEntity);
				to.getPush().syncTalentInfo();
				to.getPush().syncTalentSkillInfo();
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 所有玩家满级
	 */
	public static void allPlayerUp() {
		String sql = String.format("select id from player");
		List<String> playerIds = HawkDBManager.getInstance().executeQuery(sql, null);
		for (String playerId : playerIds) {
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			playerUp(player);
		}
	}

	/**
	 * 玩家满级
	 */
	public static void playerUp(Player player) {
		// 科技满级
		techLevelUp(player);

		// 机甲满级
		unlockGanDaMuJiQiRen(player);

		// 建筑满级
		buildUpdate(player);
	}

	/**
	 * 科技升级
	 */
	public static void techLevelUp(Player player) {
		ConfigIterator<TechnologyCfg> cfgs = HawkConfigManager.getInstance().getConfigIterator(TechnologyCfg.class);
		for (TechnologyCfg cfg : cfgs) {
			int techId = cfg.getTechId();
			TechnologyEntity entity = player.getData().getTechEntityByTechId(techId);
			if (entity == null) {
				entity = player.getData().createTechnologyEntity(cfg);
			}

			player.getData().getPlayerEffect().addEffectTech(player, entity);
			entity.setLevel(cfg.getLevel());
			entity.setResearching(false);
		}
	}

	/**
	 * 解锁机甲
	 */
	public static void unlockGanDaMuJiQiRen(Player player) {
		ConfigIterator<SuperSoldierCfg> cfgit = HawkConfigManager.getInstance().getConfigIterator(SuperSoldierCfg.class);
		for (SuperSoldierCfg cfg : cfgit) {
			int soldierId = cfg.getSupersoldierId();
			if (player.getSuperSoldierByCfgId(soldierId).isPresent()) {// 已解锁
				continue;
			}
			SuperSoldierEntity newSso = new SuperSoldierEntity();
			newSso.setSoldierId(soldierId);
			newSso.setPlayerId(player.getId());
			newSso.setStar(6);
			newSso.setState(PBSuperSoldierState.SUPER_SOLDIER_STATE_FREE_VALUE);
			SuperSoldier hero = SuperSoldier.create(newSso);

			hero.getPassiveSkillSlots().forEach(slot -> slot.getSkill().addExp(3000));
			hero.getSkillSlots().forEach(slot -> slot.getSkill().addExp(3000));

			HawkDBManager.getInstance().create(newSso);

			player.getData().getSuperSoldierEntityList().add(newSso);
		}
	}

	/**
	 * 建筑升级
	 */
	public static void buildUpdate(Player player) {
		try {

			unlockArea(player);

			for (int buildType : getBuildTypeList()) {
				BuildingBaseEntity buildingEntity = getBuildingBaseEntity(player, buildType);
				if (buildingEntity == null && !BuildAreaCfg.isShareBlockBuildType(buildType)) {
					BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, (buildType * 100) + 1);
					buildingEntity = player.getData().createBuildingEntity(buildingCfg, "1", false);
					BuildingService.getInstance().createBuildingFinish(player, buildingEntity, BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY,
							HP.code.BUILDING_CREATE_PUSH_VALUE);
				}

				buildUpgrade(player, buildingEntity);
			}

			for (BuildingBaseEntity entity : player.getData().getBuildingEntities()) {
				BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, entity.getBuildingCfgId());
				if (buildingCfg == null || buildingCfg.getLevel() >= 30) {
					continue;
				}
				buildUpgrade(player, entity);
			}

			// player.refreshPowerElectric(PowerChangeReason.BUILD_LVUP);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 升级建筑
	 */
	public static void buildUpgrade(Player player, BuildingBaseEntity buildingEntity) {
		// 建筑满级
		while (HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId() + 1) != null) {
			BuildingService.getInstance().buildingUpgrade(player, buildingEntity, BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY);
		}

		if (buildingEntity.getBuildingCfgId() % 100 < 30) {
			return;
		}

		// 勋章满级
		BuildingCfg currCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
		BuildingCfg nextLevelCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, currCfg.getPostStage());
		while (nextLevelCfg != null) {
			BuildingService.getInstance().buildingUpgrade(player, buildingEntity, BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY);
			nextLevelCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, nextLevelCfg.getPostStage());
		}
	}

	/**
	 * 根据建筑cfgId获取建筑实体
	 * @param id
	 */
	public static BuildingBaseEntity getBuildingBaseEntity(Player player, int buildCfgId) {
		Optional<BuildingBaseEntity> op = player.getData().getBuildingEntities().stream()
				.filter(e -> e.getStatus() != BuildingStatus.BUILDING_CREATING_VALUE)
				.filter(e -> e.getBuildingCfgId() / 100 == buildCfgId)
				.findAny();
		if (op.isPresent()) {
			return op.get();
		}
		return null;
	}

	/**
	 * 获取需要升级至满级的建筑列表
	 */
	public static List<Integer> getBuildTypeList() {
		List<Integer> retList = new ArrayList<>();

		ConfigIterator<BuildingCfg> buildCfgIterator = HawkConfigManager.getInstance().getConfigIterator(BuildingCfg.class);
		while (buildCfgIterator.hasNext()) {
			BuildingCfg buildCfg = buildCfgIterator.next();
			if (buildCfg.getLevel() > 1) {
				continue;
			}
			BuildLimitCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BuildLimitCfg.class, buildCfg.getLimitType());
			if (cfg == null || cfg.getLimit(30) > 1) {
				continue;
			}
			retList.add(buildCfg.getBuildType());
		}
		return retList;
	}

	/**
	 * 解锁地块
	 */
	public static void unlockArea(Player player) {
		try {
			Set<Integer> unlockedAreas = player.getData().getPlayerBaseEntity().getUnlockedAreaSet();
			ConfigIterator<BuildAreaCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(BuildAreaCfg.class);

			List<Integer> areaList = new ArrayList<Integer>();
			while (iterator.hasNext()) {
				BuildAreaCfg areaCfg = iterator.next();
				int areaId = areaCfg.getId();
				if (unlockedAreas.contains(areaId)) {
					continue;
				}

				areaList.add(areaId);
			}

			areaList.stream().forEach(e -> {
				player.unlockArea(e);
				MissionManager.getInstance().postMsg(player, new EventUnlockGround(e));
				// 解锁地块任务
				BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.BUIDING_AREA_UNLOCK, Params.valueOf("buildAreaId", e));
			});

			player.getPush().synUnlockedArea();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	public static void copyDL(Player from, Player to) {
		String info = RedisProxy.getInstance().getPlayerPresetWorldMarch(from.getId());
		RedisProxy.getInstance().getRedisSession().hSet("world_preset_march", to.getId(), info);
	}

	private static void yqzz(Player player, String chatMsg) {
		if ("@yqzz join A".equals(chatMsg) || "@yqzz join B".equals(chatMsg)) {
			System.out.println(player.getId() + " @TBLY JOIN A");
			if (!YQZZRoomManager.getInstance().hasGame("abcd")) {
				YQZZExtraParam extParm = new YQZZExtraParam();
				extParm.setDebug(true);
				extParm.setBattleId("abcd");
				HashBiMap<String, YQZZNation> serverCamp = HashBiMap.create(6);
				YQZZNation nation = new YQZZNation();
				nation.setCamp(YQZZ_CAMP.B);
				nation.setServerId(player.getServerId());
				nation.setNationLevel(5);
				nation.setPresidentId("");
				nation.setPresidentName("");
				serverCamp.put(player.getServerId(), nation);
				extParm.setServerCamp(serverCamp);
				YQZZRoomManager.getInstance().creatNewBattle(HawkTime.getMillisecond(), HawkTime.getMillisecond() + 6*3600 * 1000, extParm);

				RedisProxy.getInstance().updateNationTechValue(12345);
				// 1aas-sttg2-1 @@@ tag:fe2 name: fe2 flag: 13
				// 1aas-rovq7-1 @@@ tag:ooo name: oooo flag: 1
				// 1aas-ya9do-1 @@@ tag:sds name: sdsdas flag: 8
				// 1aas-17ehw7-1 @@@ tag:Bdd name: Bddd flag: 11
				// 7px-qz2wx-1 @@@ tag:eqw name: eqwe flag: 12
				// 7px-mi6x0-1 @@@ tag:DDD name: DDD flag: 2

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				HawkXID roomXid = HawkXID.valueOf(GsConst.ObjType.YQZZAOGUAN_ROOM, "abcd");
				HawkObjBase<HawkXID, HawkAppObj> roomObj = GsApp.getInstance().getObjMan(GsConst.ObjType.YQZZAOGUAN_ROOM).queryObject(roomXid);
				YQZZBattleRoom room = (YQZZBattleRoom) roomObj.getImpl();
			}
			YQZZRoomManager.getInstance().joinGame("abcd", player);

			// for(String guildId : GuildService.getInstance().getGuildIds()){
			// addAllguildMembers(4, guildId);
			// }
		}

		if ("@yqzz quit".equals(chatMsg)) {
			YQZZPlayer tp = (YQZZPlayer) YQZZRoomManager.getInstance().makesurePlayer(player.getId());
			tp.getParent().quitWorld(tp, YQZZQuitReason.LEAVE);
		}

		if ("@yqzz over".equals(chatMsg)) {
			for (YQZZBattleRoom room : YQZZRoomManager.getInstance().findAllRoom()) {
				// room.setWinCamp(YQZZ_CAMP.A);
				room.setState(new YQZZGameOver(room));
				room.getState().onTick();
			}
		}

		if ("@yqzz army".equals(chatMsg)) {

			for (String pid : GlobalData.getInstance().getAllPlayerIds()) {
				if (player.getId().equals(pid)) {
					continue;
				}
				Player to = GlobalData.getInstance().makesurePlayer(pid);
				copyArmy(player, to);
			}
		}

		// if ("@dyzz kda".equals(chatMsg)){
		// DYZZBattleCfg cfg = HawkConfigManager.getInstance().getKVInstance(DYZZBattleCfg.class);
		// DYZZPlayer tp = (DYZZPlayer) DYZZRoomManager.getInstance().makesurePlayer(player.getId());
		//
		// HawkTuple3<Double, Double, Double> sp3 = cfg.getScoreparameter3();
		//// return (int) (killCount * sp3.first + collectHonor * sp3.second + hurtTankCount * sp3.third);
		// String str = "kad : "+ tp.getKillCount() +"*"+ sp3.first+" + " + tp.getCollectHonor() +"*"+ sp3.second +" + " + tp.getHurtTankCount() +" * " + sp3.third +" = " + tp.getKda();
		// tp.getParent().sendChatMsg(tp, str, "", 0, Const.ChatType.CHAT_FUBEN);
		// }
		//

	}

	private static void armour(Player player, String chatMsg) {
		if (chatMsg.startsWith("@armour")) {
			String[] param = chatMsg.split(" ");
			SoldierType fromType = SoldierType.valueOf(Integer.parseInt(param[1]));
			SoldierType toType = SoldierType.valueOf(Integer.parseInt(param[2]));
			SoldierExchangeUtil util = SoldierExchangeUtil.create(player,fromType,toType);
			util.zhuanArmour();
		}
	}

}
