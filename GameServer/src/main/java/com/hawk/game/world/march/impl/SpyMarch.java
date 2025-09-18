package com.hawk.game.world.march.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.xid.HawkXID;

import com.hawk.game.battle.NpcPlayer;
import com.hawk.game.battle.TemporaryMarch;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.config.SuperWeaponSoldierCfg;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.crossfortress.CrossFortressService;
import com.hawk.game.crossfortress.IFortress;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.guild.manor.GuildManorObj;
import com.hawk.game.lianmengxzq.worldpoint.XZQWorldPoint;
import com.hawk.game.player.Player;
import com.hawk.game.president.PresidentFightService;
import com.hawk.game.president.PresidentTower;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Mail.DetectMail;
import com.hawk.game.protocol.Mail.MailArmyInfo;
import com.hawk.game.protocol.Mail.MailPlayerInfo;
import com.hawk.game.protocol.Mail.PBDetectCapital;
import com.hawk.game.protocol.Mail.PBDetectFoggy;
import com.hawk.game.protocol.Mail.PBDetectManor;
import com.hawk.game.protocol.Mail.PBDetectStrongpoint;
import com.hawk.game.protocol.Mail.PBDetectTHRes;
import com.hawk.game.protocol.Mail.PBDetectWarFlag;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.President.PresidentPeriod;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponPeriod;
import com.hawk.game.protocol.World;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.WarFlagService;
import com.hawk.game.service.flag.FlagCollection;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.warFlag.IFlag;
import com.hawk.game.superweapon.SuperWeaponService;
import com.hawk.game.superweapon.weapon.IWeapon;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.MailBuilderUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.march.PassiveMarch;
import com.hawk.game.world.march.submarch.BasedMarch;
import com.hawk.game.world.march.submarch.IReportPushMarch;
import com.hawk.game.world.service.WorldMonsterService;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;

/**
 * 侦查行军
 * 
 * @author zhenyu.shang
 * @since 2017年8月28日
 */
public class SpyMarch extends PassiveMarch implements BasedMarch, IReportPushMarch {

	public SpyMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.SPY;
	}

	@Override
	public void onMarchReach(Player player) {
		// 删除行军报告
		removeAttackReport();
		WorldMarch march = getMarchEntity();
		// 行军到达时判断是否可以侦查
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(march.getTerminalId());
		if (point == null) {
			// 行军返回
			WorldMarchService.getInstance().onPlayerNoneAction(this, march.getReachTime());
			return;
		}

		if (point.getPointType() == WorldPointType.KING_PALACE_VALUE) {
			doCapitalSpy(march, point, player);
			WorldMarchService.getInstance().onPlayerNoneAction(this, march.getReachTime());

		} else if (point.getPointType() == WorldPointType.CAPITAL_TOWER_VALUE) {
			doCapitalTowerSpy(march, point, player);
			WorldMarchService.getInstance().onPlayerNoneAction(this, march.getReachTime());
		} else if (point.getPointType() == WorldPointType.GUILD_TERRITORY_VALUE) {
			doGuildManorSpy(march, point, player);
			WorldMarchService.getInstance().onPlayerNoneAction(this, march.getReachTime());
		} else if (point.getPointType() == WorldPointType.STRONG_POINT_VALUE) {
			doStrongPointSpy(march, point, player);
			WorldMarchService.getInstance().onPlayerNoneAction(this, march.getReachTime());
		} else if (point.getPointType() == WorldPointType.TH_RESOURCE_VALUE) {
			doFoggyPointSpy(march, point, player);
			WorldMarchService.getInstance().onPlayerNoneAction(this, march.getReachTime());
		} else if (point.getPointType() == WorldPointType.CHRISTMAS_BOX_VALUE) {
			doChristmasBosPointSpy(march, point, player);
			WorldMarchService.getInstance().onPlayerNoneAction(this, march.getReachTime());
		} else if (point.getPointType() == WorldPointType.PYLON_VALUE) {
			doPlyonPointSpy(march, point, player);
			WorldMarchService.getInstance().onPlayerNoneAction(this, march.getReachTime());
		} else if (point.getPointType() == WorldPointType.SUPER_WEAPON_VALUE) {
			doSuperWeaponSpy(march, point, player);
			WorldMarchService.getInstance().onPlayerNoneAction(this, march.getReachTime());
		}else if (point.getPointType() == WorldPointType.XIAO_ZHAN_QU_VALUE) {
			doXzqSpy(march, point, player);
			WorldMarchService.getInstance().onPlayerNoneAction(this, march.getReachTime());
		}  else if (point.getPointType() == WorldPointType.WAR_FLAG_POINT_VALUE) {
			doWarFlagSpy(march, point, player);
			WorldMarchService.getInstance().onPlayerNoneAction(this, march.getReachTime());
		} else if (point.getPointType() == WorldPointType.CROSS_FORTRESS_VALUE) {
			doFortressSpy(march, point, player);
			WorldMarchService.getInstance().onPlayerNoneAction(this, march.getReachTime());
		} else {
			doPlayerSpy(march, point, player);
		}
	}

	private void doChristmasBosPointSpy(WorldMarch march, WorldPoint point, Player player) {
		Collection<IWorldMarch> quarteredMarchs = WorldMarchService.getInstance().getWorldPointMarch(point.getX(), point.getY());
		quarteredMarchs = quarteredMarchs.stream()
				.filter(m -> m.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE)
				.filter(m -> !Objects.equals(m.getPlayerId(), player.getId()))
				.collect(Collectors.toList());
		final int radarLevel = radaLevel(player);

		PBDetectTHRes.Builder builder = PBDetectTHRes.newBuilder()
				.setResourceId(point.getResourceId())
				.setLevel(radarLevel)
				.setPointX(point.getX())
				.setPointY(point.getY());
		if (radarLevel >= GsConst.RadarLevel.LV10) {
			for (IWorldMarch ma : quarteredMarchs) {
				MailArmyInfo.Builder defenceArmy = MailBuilderUtil.getDefenceArmy(radarLevel, ma, radarLevel < GsConst.RadarLevel.LV18);
				builder.addDefenceArmy(defenceArmy);
			}
		}
		if (radarLevel >= GsConst.RadarLevel.LV4 && radarLevel < GsConst.RadarLevel.LV10) {
			int assistTotal = quarteredMarchs.stream()
					.flatMap(m -> m.getArmys().stream())
					.mapToInt(a -> a.getFreeCnt())
					.sum();
			builder.setDefenceArmyAboutNum(GameUtil.getProbablyNum(assistTotal, GsConst.RADA_RANDOM_RANGE));
		}

		// 侦查邮件
		MailParames.Builder playerParamesBuilder = MailParames.newBuilder().setPlayerId(player.getId())
				.setMailId(MailId.CHRISTMAS_BOX_SPY)
				.addSubTitles(point.getX(), point.getY())
				.addTips(player.getName())
				.addContents(DetectMail.newBuilder().setDetectTHRes(builder));
		FightMailService.getInstance().sendMail(playerParamesBuilder.build());

		int[] playerPos = WorldPlayerService.getInstance().getPlayerPosXY(player.getId());
		for (IWorldMarch tempMarch : quarteredMarchs) {
			FightMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(tempMarch.getPlayerId())
					.setMailId(MailId.CHRISTMAS_BOX_SPYED)
					.addSubTitles(point.getX(), point.getY())
					.addMidTitles(player.getGuildTag(), player.getName())
					.addContents(player.getIcon(), player.getName(), player.getPower(), playerPos[0], playerPos[1], player.getPfIcon(), point.getX(), point.getY(),
							player.getGuildTag())
					.build());
		}
		// TH_RES_BE_SPY
		// 统计侦察次数，以到达目标为准
		player.getData().getStatisticsEntity().addSpyCnt(1);

	}

	/** 航海要塞 */
	private void doFortressSpy(WorldMarch march, WorldPoint point, Player player) {
		boolean spyFail = false;
		if (!player.hasGuild()) {
			spyFail = true;
		}
		// 目标点
		int pointId = point.getId();
		IFortress fortress = CrossFortressService.getInstance().getFortress(pointId);
		if (fortress == null) {
			spyFail = true;
		}
		if (CrossFortressService.getInstance().getCurrentState() != SuperWeaponPeriod.WARFARE_VALUE) {
			spyFail = true;
			return;
		}

		// 当前占领联盟
		String guildId = null;
		Player stayLeader = WorldMarchService.getInstance().getFortressLeader(pointId);
		if (stayLeader != null && stayLeader.hasGuild()) {
			guildId = stayLeader.getGuildId();
		}
		if (Objects.equals(guildId, player.getGuildId())) {
			spyFail = true;
		}
		if (spyFail) {
			FightMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(march.getPlayerId())
					.addSubTitles(point.getX(), point.getY())
					.addContents(point.getX(), point.getY())
					.setMailId(MailId.FORTRESS_SPY_FAIL)
					.addTips(point.getX())
					.addTips(point.getY())
					.addTips(stayLeader == null ? "" : stayLeader.getName())
					.build());
			return;
		}

		List<IWorldMarch> quarteredMarchs = null;
		if (fortress.hasNpc()) {
			int worldMaxLevel = WorldMonsterService.getInstance().getMaxCommonMonsterLvl();
			SuperWeaponSoldierCfg config = HawkConfigManager.getInstance().getConfigByKey(SuperWeaponSoldierCfg.class, worldMaxLevel);
			if (config == null) {
				int configSize = HawkConfigManager.getInstance().getConfigSize(SuperWeaponSoldierCfg.class);
				config = HawkConfigManager.getInstance().getConfigByIndex(SuperWeaponSoldierCfg.class, configSize - 1);
			}

			NpcPlayer npcPlayer = new NpcPlayer(HawkXID.nullXid());
			TemporaryMarch foggyMarch = new TemporaryMarch();
			foggyMarch.setArmys(config.getArmyList());
			foggyMarch.setPlayer(npcPlayer);
			foggyMarch.setHeros(npcPlayer.getHeroByCfgId(config.getHeroIdList()));

			quarteredMarchs = new ArrayList<>(1);
			quarteredMarchs.add(foggyMarch);
		} else {
			quarteredMarchs = WorldMarchService.getInstance().getFortressStayMarchs(pointId);
		}

		// 确认侦查类型
		final int radarLevel = radaLevel(player);

		PBDetectFoggy.Builder builder = PBDetectFoggy.newBuilder()
				.setLevel(radarLevel)
				.setPointX(point.getX())
				.setPointY(point.getY());
		if (radarLevel >= GsConst.RadarLevel.LV10) {
			for (IWorldMarch ma : quarteredMarchs) {
				MailArmyInfo.Builder defenceArmy = MailBuilderUtil.getDefenceArmy(radarLevel, ma, radarLevel < GsConst.RadarLevel.LV18);
				builder.addDefenceArmy(defenceArmy);
			}
		}
		if (radarLevel >= GsConst.RadarLevel.LV4 && radarLevel < GsConst.RadarLevel.LV10) {
			int assistTotal = quarteredMarchs.stream()
					.flatMap(m -> m.getMarchEntity().getArmys().stream())
					.mapToInt(a -> a.getFreeCnt())
					.sum();
			builder.setDefenceArmyAboutNum(GameUtil.getProbablyNum(assistTotal, GsConst.RADA_RANDOM_RANGE));
		}

		// 侦查邮件
		MailParames.Builder playerParamesBuilder = MailParames.newBuilder().setPlayerId(player.getId())
				.setMailId(MailId.FORTRESS_SPY_SUCCUS)
				.addSubTitles(point.getX(), point.getY())
				.addTips(point.getX())
				.addTips(point.getY())
				.addTips(stayLeader == null ? "" : stayLeader.getName())
				.addContents(DetectMail.newBuilder().setDetectFoggy(builder));
		MailParames mparames = playerParamesBuilder.build();
		FightMailService.getInstance().sendMail(mparames);
		WorldMarchService.getInstance().addSpyMark(player, point.getId(), mparames.getUuid(), "");
		for (IWorldMarch tempMarch : quarteredMarchs) {
			FightMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(tempMarch.getPlayerId())
					.setMailId(MailId.FORTRESS_SPY_ED)
					.addSubTitles(point.getX(), point.getY())
					.addMidTitles(player.getGuildTag(), player.getName())
					.addContents(point.getX(), point.getY())
					.addTips(point.getX())
					.addTips(point.getY())
					.addTips(player.getName())
					.build());
		}

		// 统计侦察次数，以到达目标为准
		player.getData().getStatisticsEntity().addSpyCnt(1);
	}

	/** 侦查旗帜 */
	private void doWarFlagSpy(WorldMarch march, WorldPoint flagPoint, Player player) {
		if (flagPoint.getPointType() != WorldPointType.WAR_FLAG_POINT_VALUE) {
			return;
		}

		String flagId = flagPoint.getGuildBuildId();
		if (HawkOSOperator.isEmptyString(flagId)) {
			return;
		}

		IFlag flag = FlagCollection.getInstance().getFlag(flagId);
		if (flag == null) {
			return;
		}

		// 首都上驻扎的行军
		List<IWorldMarch> quarteredMarchs = WarFlagService.getInstance().getFlagPointMarch(flag);

		// 确认侦查类型
		final int radarLevel = radaLevel(player);

		PBDetectWarFlag.Builder builder = PBDetectWarFlag.newBuilder()
				.setLevel(radarLevel)
				.setPointX(flagPoint.getX())
				.setPointY(flagPoint.getY());
		if (!quarteredMarchs.isEmpty()) {
			builder.setPlayer(MailBuilderUtil.createMailPlayerInfo(quarteredMarchs.get(0).getPlayer()));
		}

		if (radarLevel >= GsConst.RadarLevel.LV10) {
			for (IWorldMarch ma : quarteredMarchs) {
				MailArmyInfo.Builder defenceArmy = MailBuilderUtil.getDefenceArmy(radarLevel, ma, radarLevel < GsConst.RadarLevel.LV18);
				builder.addDefenceArmy(defenceArmy);
			}
		}
		if (radarLevel >= GsConst.RadarLevel.LV4 && radarLevel < GsConst.RadarLevel.LV10) {
			int assistTotal = quarteredMarchs.stream()
					.flatMap(m -> m.getMarchEntity().getArmys().stream())
					.mapToInt(a -> a.getFreeCnt())
					.sum();
			builder.setDefenceArmyAboutNum(GameUtil.getProbablyNum(assistTotal, GsConst.RADA_RANDOM_RANGE));
		}
		if (StringUtils.isNotEmpty(flag.getCurrentId())) {
			String guildId = flag.getCurrentId();
			builder.setGuildName(GuildService.getInstance().getGuildName(guildId));
			builder.setGuildTag(GuildService.getInstance().getGuildTag(guildId));
		}

		// 侦查邮件
		
		MailId mailId = MailId.SPY_WAR_FLAG_SUCESS;
		if (flag.isCenter()) {
			mailId = MailId.SPY_CENTER_FLAG;
		}
		
		MailParames.Builder playerParamesBuilder = MailParames.newBuilder().setPlayerId(player.getId())
				.setMailId(mailId);
		if (StringUtils.isNotEmpty(flag.getCurrentId())) {
			playerParamesBuilder.addSubTitles(GuildService.getInstance().getGuildTag(flag.getCurrentId()));
			playerParamesBuilder.addSubTitles(GuildService.getInstance().getGuildName(flag.getCurrentId()));
			playerParamesBuilder.addTips(GuildService.getInstance().getGuildTag(flag.getCurrentId()));
		}
		playerParamesBuilder.addSubTitles(flagPoint.getX(), flagPoint.getY());
		playerParamesBuilder.addContents(DetectMail.newBuilder().setDetectWarFlag(builder));
		MailParames mparames = playerParamesBuilder.build();
		FightMailService.getInstance().sendMail(mparames);
		WorldMarchService.getInstance().addSpyMark(player, flagPoint.getId(), mparames.getUuid(), "");
		
		MailId mailId2 = MailId.SPY_WAR_FLAG_SPYED;
		if (flag.isCenter()) {
			mailId2 = MailId.BE_SPY_CENTER_FLAG;
		}
		// 向守军推送被侦查
		int[] playerPos = WorldPlayerService.getInstance().getPlayerPosXY(player.getId());
		for (IWorldMarch tempMarch : quarteredMarchs) {
			FightMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(tempMarch.getPlayerId())
					.setMailId(mailId2)
					.addSubTitles(flagPoint.getX(), flagPoint.getY())
					.addMidTitles(player.getGuildTag(), player.getName())
					.addContents(player.getIcon(), player.getName(), player.getPower(), playerPos[0], playerPos[1], player.getPfIcon(), flagPoint.getX(), flagPoint.getY(),
							player.getGuildTag())
					.build());
		}

		// 统计侦察次数，以到达目标为准
		player.getData().getStatisticsEntity().addSpyCnt(1);
	}

	/** 侦查金猪点 */
	private void doFoggyPointSpy(WorldMarch march, WorldPoint point, Player player) {
		Collection<IWorldMarch> quarteredMarchs = WorldMarchService.getInstance().getWorldPointMarch(point.getX(), point.getY());
		quarteredMarchs = quarteredMarchs.stream()
				.filter(m -> m.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE)
				.filter(m -> !Objects.equals(m.getPlayerId(), player.getId()))
				.collect(Collectors.toList());
		final int radarLevel = radaLevel(player);

		PBDetectTHRes.Builder builder = PBDetectTHRes.newBuilder()
				.setResourceId(point.getResourceId())
				.setLevel(radarLevel)
				.setPointX(point.getX())
				.setPointY(point.getY());
		if (radarLevel >= GsConst.RadarLevel.LV10) {
			for (IWorldMarch ma : quarteredMarchs) {
				MailArmyInfo.Builder defenceArmy = MailBuilderUtil.getDefenceArmy(radarLevel, ma, radarLevel < GsConst.RadarLevel.LV18);
				builder.addDefenceArmy(defenceArmy);
			}
		}
		if (radarLevel >= GsConst.RadarLevel.LV4 && radarLevel < GsConst.RadarLevel.LV10) {
			int assistTotal = quarteredMarchs.stream()
					.flatMap(m -> m.getArmys().stream())
					.mapToInt(a -> a.getFreeCnt())
					.sum();
			builder.setDefenceArmyAboutNum(GameUtil.getProbablyNum(assistTotal, GsConst.RADA_RANDOM_RANGE));
		}

		// 侦查邮件
		MailParames.Builder playerParamesBuilder = MailParames.newBuilder().setPlayerId(player.getId())
				.setMailId(MailId.DETECT_THRES_SUCCESS)
				.addSubTitles(point.getX(), point.getY())
				.addTips(player.getName())
				.addContents(DetectMail.newBuilder().setDetectTHRes(builder));
		FightMailService.getInstance().sendMail(playerParamesBuilder.build());

		int[] playerPos = WorldPlayerService.getInstance().getPlayerPosXY(player.getId());
		for (IWorldMarch tempMarch : quarteredMarchs) {
			FightMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(tempMarch.getPlayerId())
					.setMailId(MailId.TH_RES_BE_SPY)
					.addSubTitles(point.getX(), point.getY())
					.addMidTitles(player.getGuildTag(), player.getName())
					.addContents(player.getIcon(), player.getName(), player.getPower(), playerPos[0], playerPos[1], player.getPfIcon(), point.getX(), point.getY(),
							player.getGuildTag())
					.build());
		}
		// TH_RES_BE_SPY
		// 统计侦察次数，以到达目标为准
		player.getData().getStatisticsEntity().addSpyCnt(1);

	}
	
	/** 侦查能量塔行军 */
	private void doPlyonPointSpy(WorldMarch march, WorldPoint point, Player player) {
		Collection<IWorldMarch> quarteredMarchs = WorldMarchService.getInstance().getWorldPointMarch(point.getX(), point.getY());
		quarteredMarchs = quarteredMarchs.stream()
				.filter(m -> m.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE)
				.filter(m -> !Objects.equals(m.getPlayerId(), player.getId()))
				.collect(Collectors.toList());
		final int radarLevel = radaLevel(player);

		PBDetectTHRes.Builder builder = PBDetectTHRes.newBuilder()
				.setResourceId(point.getResourceId())
				.setLevel(radarLevel)
				.setPointX(point.getX())
				.setPointY(point.getY());
		if (radarLevel >= GsConst.RadarLevel.LV10) {
			for (IWorldMarch ma : quarteredMarchs) {
				MailArmyInfo.Builder defenceArmy = MailBuilderUtil.getDefenceArmy(radarLevel, ma, radarLevel < GsConst.RadarLevel.LV18);
				builder.addDefenceArmy(defenceArmy);
			}
		}
		if (radarLevel >= GsConst.RadarLevel.LV4 && radarLevel < GsConst.RadarLevel.LV10) {
			int assistTotal = quarteredMarchs.stream()
					.flatMap(m -> m.getArmys().stream())
					.mapToInt(a -> a.getFreeCnt())
					.sum();
			builder.setDefenceArmyAboutNum(GameUtil.getProbablyNum(assistTotal, GsConst.RADA_RANDOM_RANGE));
		}

		// 侦查邮件
		MailParames.Builder playerParamesBuilder = MailParames.newBuilder().setPlayerId(player.getId())
				.setMailId(MailId.DETECT_PLYON_SUCCESS)
				.addSubTitles(point.getX(), point.getY())
				.addTips(player.getName())
				.addContents(DetectMail.newBuilder().setDetectTHRes(builder));
		FightMailService.getInstance().sendMail(playerParamesBuilder.build());

		int[] playerPos = WorldPlayerService.getInstance().getPlayerPosXY(player.getId());
		for (IWorldMarch tempMarch : quarteredMarchs) {
			FightMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(tempMarch.getPlayerId())
					.setMailId(MailId.DETECT_PLYON_BE)
					.addSubTitles(point.getX(), point.getY())
					.addMidTitles(player.getGuildTag(), player.getName())
					.addContents(player.getIcon(), player.getName(), player.getPower(), playerPos[0], playerPos[1], player.getPfIcon(), point.getX(), point.getY(),
							player.getGuildTag())
					.build());
		}
		// TH_RES_BE_SPY
		// 统计侦察次数，以到达目标为准
		player.getData().getStatisticsEntity().addSpyCnt(1);

	}

	/**
	 * 侦查据点
	 * 
	 * @param march
	 * @param point
	 * @return
	 */
	private void doStrongPointSpy(WorldMarch march, WorldPoint point, Player player) {
		// DETECT_STRONGPOINT_SUCCESS = 2012035; // 侦查据点成功
		// DETECT_STRONGPOINT_DETECTED = 2012036; // 据点被侦查
		// DETECT_STRONGPOINT_FAIL = 2012037; // 据点行军已撤离
		// DETECT_STRONGPOINT_SAMEG = 2012038; // 同盟
		IWorldMarch enemy = WorldMarchService.getInstance().getMarch(point.getMarchId());
		if (Objects.isNull(enemy)) {
			// 据点空
			FightMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(march.getPlayerId())
					.setMailId(MailId.DETECT_STRONGPOINT_FAIL)
					.addSubTitles(point.getX(), point.getY())
					.addContents(point.getX(), point.getY())
					.build());
			return;
		}
		Player leader = GlobalData.getInstance().makesurePlayer(enemy.getPlayerId());
		if (GuildService.getInstance().isInTheSameGuild(player.getId(), enemy.getPlayerId())) {
			// TODO 联盟改变
			FightMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(march.getPlayerId())
					.setMailId(MailId.DETECT_STRONGPOINT_SAMEG)
					.addSubTitles(point.getX(), point.getY())
					.addContents(point.getX(), point.getY())
					.build());
			return;
		}

		// 首都上驻扎的行军
		List<IWorldMarch> quarteredMarchs = new ArrayList<>();
		quarteredMarchs.add(enemy);

		// 确认侦查类型
		final int radarLevel = radaLevel(player);

		PBDetectStrongpoint.Builder builder = PBDetectStrongpoint.newBuilder()
				.setLevel(radarLevel)
				.setCfgId(point.getMonsterId())
				.setPointX(point.getX())
				.setPointY(point.getY());
		if (radarLevel >= GsConst.RadarLevel.LV10) {
			for (IWorldMarch ma : quarteredMarchs) {
				MailArmyInfo.Builder defenceArmy = MailBuilderUtil.getDefenceArmy(radarLevel, ma, radarLevel < GsConst.RadarLevel.LV18);
				builder.addDefenceArmy(defenceArmy);
			}
		}
		if (radarLevel >= GsConst.RadarLevel.LV4 && radarLevel < GsConst.RadarLevel.LV10) {
			int assistTotal = quarteredMarchs.stream()
					.flatMap(m -> m.getMarchEntity().getArmys().stream())
					.mapToInt(a -> a.getFreeCnt())
					.sum();
			builder.setDefenceArmyAboutNum(GameUtil.getProbablyNum(assistTotal, GsConst.RADA_RANDOM_RANGE));
		}

		// 侦查邮件
		MailParames.Builder playerParamesBuilder = MailParames.newBuilder().setPlayerId(player.getId())
				.setMailId(MailId.DETECT_STRONGPOINT_SUCCESS)
				.addSubTitles(point.getX(), point.getY())
				.addContents(DetectMail.newBuilder().setStrongpoint(builder))
				.addTips(leader.getName());
		MailParames mparames = playerParamesBuilder.build();
		FightMailService.getInstance().sendMail(mparames);
		WorldMarchService.getInstance().addSpyMark(player, point.getId(), mparames.getUuid(), enemy.getMarchId());

		// 向守军推送被侦查
		int[] playerPos = WorldPlayerService.getInstance().getPlayerPosXY(player.getId());
		for (IWorldMarch tempMarch : quarteredMarchs) {
			FightMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(tempMarch.getPlayerId())
					.setMailId(MailId.DETECT_STRONGPOINT_DETECTED)
					.addSubTitles(point.getX(), point.getY())
					.addMidTitles(player.getGuildTag(), player.getName())
					.addContents(player.getIcon(), player.getName(), player.getPower(), playerPos[0], playerPos[1], player.getPfIcon(), point.getX(), point.getY(),
							player.getGuildTag())
					.build());
		}

		// 统计侦察次数，以到达目标为准
		player.getData().getStatisticsEntity().addSpyCnt(1);
	}

	/**
	 * 侦查箭塔
	 */
	private void doCapitalTowerSpy(WorldMarch march, WorldPoint point, Player player) {
		// 不在国王战时期
		int period = PresidentFightService.getInstance().getPresidentPeriodType();

		// 大总统战结束
		// 如果首都的驻扎联盟是自己的，则侦查直接返回
		PresidentTower tower = PresidentFightService.getInstance().getPresidentTower(point.getId());
		String fightGuildId = tower.getGuildId();
		if (Objects.equals(fightGuildId, player.getGuildId())) {
			return;
		}
		if (period != PresidentPeriod.WARFARE_VALUE & period != PresidentPeriod.OVERTIME_VALUE) {
			FightMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(march.getPlayerId())
					.setMailId(MailId.DETECT_TOWER_FAIL)
					.addSubTitles(point.getX(), point.getY())
					.addContents(point.getX(), point.getY())
					.build());
			return;
		}

		// 箭塔上驻扎的行军
		List<IWorldMarch> quarteredMarchs = WorldMarchService.getInstance().getPresidentTowerStayMarchs(point.getId());

		final int radarLevel = radaLevel(player);

		PBDetectCapital.Builder builder = PBDetectCapital.newBuilder()
				.setLevel(radarLevel)
				.setPointX(point.getX())
				.setPointY(point.getY());
		if (radarLevel >= GsConst.RadarLevel.LV10) {
			for (IWorldMarch ma : quarteredMarchs) {
				MailArmyInfo.Builder defenceArmy = MailBuilderUtil.getDefenceArmy(radarLevel, ma, radarLevel < GsConst.RadarLevel.LV18);
				builder.addDefenceArmy(defenceArmy);
			}
		}
		if (radarLevel >= GsConst.RadarLevel.LV4 && radarLevel < GsConst.RadarLevel.LV10) {
			int assistTotal = quarteredMarchs.stream()
					.flatMap(m -> m.getMarchEntity().getArmys().stream())
					.mapToInt(a -> a.getFreeCnt())
					.sum();
			builder.setDefenceArmyAboutNum(GameUtil.getProbablyNum(assistTotal, GsConst.RADA_RANDOM_RANGE));
		}

		// 侦查邮件
		MailParames.Builder playerParamesBuilder = MailParames.newBuilder().setPlayerId(player.getId())
				.setMailId(MailId.DETECT_TOWER_SUCC)
				.addSubTitles(point.getX(), point.getY())
				.addContents(DetectMail.newBuilder().setDetectCapital(builder));
		MailParames mparames = playerParamesBuilder.build();
		FightMailService.getInstance().sendMail(mparames);
		WorldMarchService.getInstance().addSpyMark(player, point.getId(), mparames.getUuid(), "");
		// 向守军推送被侦查
		int[] playerPos = WorldPlayerService.getInstance().getPlayerPosXY(player.getId());
		for (IWorldMarch tempMarch : quarteredMarchs) {
			FightMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(tempMarch.getPlayerId())
					.setMailId(MailId.DETECT_TOWER_DETECTED)
					.addSubTitles(point.getX(), point.getY())
					.addMidTitles(player.getGuildTag(), player.getName())
					.addContents(player.getIcon(), player.getName(), player.getPower(), playerPos[0], playerPos[1], player.getPfIcon(), point.getX(), point.getY(),
							player.getGuildTag())
					.build());
		}

		// 统计侦察次数，以到达目标为准
		player.getData().getStatisticsEntity().addSpyCnt(1);

	}

	private void spyManorSpyFail(WorldMarch spymarch) {
		FightMailService.getInstance().sendMail(MailParames
				.newBuilder()
				.setPlayerId(spymarch.getPlayerId())
				.setMailId(MailId.DETECT_GUILD_BASTION_FAILED_TARGET_CHANGED)
				.addSubTitles(spymarch.getTerminalX(), spymarch.getTerminalY())
				.addContents(spymarch.getTerminalX(), spymarch.getTerminalY())
				.build());
	}

	/**
	 * 侦查王座的邮件处理
	 * 
	 * @param march
	 * @param point
	 * @return
	 */
	private void doCapitalSpy(WorldMarch march, WorldPoint point, Player player) {
		// 不在国王战时期
		int period = PresidentFightService.getInstance().getPresidentPeriodType();
		// 如果首都的驻扎联盟是自己的，则侦查直接返回
		String fightGuildId = PresidentFightService.getInstance().getCurrentGuildId();
		boolean saveGuild = Objects.equals(fightGuildId, player.getGuildId());
		if ((period != PresidentPeriod.WARFARE_VALUE & period != PresidentPeriod.OVERTIME_VALUE)
				|| saveGuild) {
			FightMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(march.getPlayerId())
					.addSubTitles(point.getX(), point.getY())
					.addContents(point.getX(), point.getY())
					.setMailId(saveGuild ? MailId.DETECT_CAPITAL_TARGET_CHANGE : MailId.DETECT_CAPITAL_FAILED)
					.build());
			return;
		}

		// 首都上驻扎的行军
		List<IWorldMarch> quarteredMarchs = WorldMarchService.getInstance().getPresidentQuarteredMarchs();

		// 确认侦查类型
		final int radarLevel = radaLevel(player);

		PBDetectCapital.Builder builder = PBDetectCapital.newBuilder()
				.setLevel(radarLevel)
				.setPointX(point.getX())
				.setPointY(point.getY());
		if (radarLevel >= GsConst.RadarLevel.LV10) {
			for (IWorldMarch ma : quarteredMarchs) {
				MailArmyInfo.Builder defenceArmy = MailBuilderUtil.getDefenceArmy(radarLevel, ma, radarLevel < GsConst.RadarLevel.LV18);
				builder.addDefenceArmy(defenceArmy);
			}
		}
		if (radarLevel >= GsConst.RadarLevel.LV4 && radarLevel < GsConst.RadarLevel.LV10) {
			int assistTotal = quarteredMarchs.stream()
					.flatMap(m -> m.getMarchEntity().getArmys().stream())
					.mapToInt(a -> a.getFreeCnt())
					.sum();
			builder.setDefenceArmyAboutNum(GameUtil.getProbablyNum(assistTotal, GsConst.RADA_RANDOM_RANGE));
		}

		// 侦查邮件
		MailParames.Builder playerParamesBuilder = MailParames.newBuilder().setPlayerId(player.getId())
				.setMailId(MailId.DETECT_CAPITAL_SUCC)
				.addSubTitles(point.getX(), point.getY())
				.addContents(DetectMail.newBuilder().setDetectCapital(builder));
		MailParames mparames = playerParamesBuilder.build();
		FightMailService.getInstance().sendMail(mparames);
		WorldMarchService.getInstance().addSpyMark(player, point.getId(), mparames.getUuid(), "");
		// 向守军推送被侦查
		int[] playerPos = WorldPlayerService.getInstance().getPlayerPosXY(player.getId());
		for (IWorldMarch tempMarch : quarteredMarchs) {
			FightMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(tempMarch.getPlayerId())
					.setMailId(MailId.CAPITAL_BEDETECT)
					.addSubTitles(point.getX(), point.getY())
					.addMidTitles(player.getGuildTag(), player.getName())
					.addContents(player.getIcon(), player.getName(), player.getPower(), playerPos[0], playerPos[1], player.getPfIcon(), point.getX(), point.getY(),
							player.getGuildTag())
					.build());
		}

		// 统计侦察次数，以到达目标为准
		player.getData().getStatisticsEntity().addSpyCnt(1);
	}

	/**
	 * 侦查超级武器的邮件处理
	 */
	private void doSuperWeaponSpy(WorldMarch march, WorldPoint point, Player player) {

		IWeapon weapon = SuperWeaponService.getInstance().getWeapon(point.getId());

		// 如果首都的驻扎联盟是自己的，则侦查直接返回
		boolean saveGuild = Objects.equals(weapon.getGuildId(), player.getGuildId());
		if (SuperWeaponService.getInstance().getStatus() != SuperWeaponPeriod.WARFARE_VALUE || saveGuild) {
			FightMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(march.getPlayerId())
					.addSubTitles(point.getX(), point.getY())
					.addContents(point.getX(), point.getY())
					.setMailId(MailId.DETECT_SUPER_WEAPON_FAILED)
					.build());
			return;
		}

		// 首都上驻扎的行军
		List<IWorldMarch> quarteredMarchs = WorldMarchService.getInstance().getSuperWeaponStayMarchs(point.getId());

		// 确认侦查类型
		final int radarLevel = radaLevel(player);

		PBDetectCapital.Builder builder = PBDetectCapital.newBuilder()
				.setLevel(radarLevel)
				.setPointX(point.getX())
				.setPointY(point.getY());
		if (radarLevel >= GsConst.RadarLevel.LV10) {
			for (IWorldMarch ma : quarteredMarchs) {
				MailArmyInfo.Builder defenceArmy = MailBuilderUtil.getDefenceArmy(radarLevel, ma, radarLevel < GsConst.RadarLevel.LV18);
				builder.addDefenceArmy(defenceArmy);
			}
		}
		if (radarLevel >= GsConst.RadarLevel.LV4 && radarLevel < GsConst.RadarLevel.LV10) {
			int assistTotal = quarteredMarchs.stream()
					.flatMap(m -> m.getMarchEntity().getArmys().stream())
					.mapToInt(a -> a.getFreeCnt())
					.sum();
			builder.setDefenceArmyAboutNum(GameUtil.getProbablyNum(assistTotal, GsConst.RADA_RANDOM_RANGE));
		}

		// 侦查邮件
		MailParames.Builder playerParamesBuilder = MailParames.newBuilder().setPlayerId(player.getId())
				.setMailId(MailId.DETECT_SUPER_WEAPON_SUCC)
				.addSubTitles(point.getX(), point.getY())
				.addContents(DetectMail.newBuilder().setDetectCapital(builder))
				.addTips(point.getX())
				.addTips(point.getY());

		MailParames mparames = playerParamesBuilder.build();
		FightMailService.getInstance().sendMail(mparames);
		WorldMarchService.getInstance().addSpyMark(player, point.getId(), mparames.getUuid(), "");
		// 向守军推送被侦查
		int[] playerPos = WorldPlayerService.getInstance().getPlayerPosXY(player.getId());
		for (IWorldMarch tempMarch : quarteredMarchs) {
			FightMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(tempMarch.getPlayerId())
					.setMailId(MailId.SUPER_WEAPON_BEDETECT)
					.addSubTitles(point.getX(), point.getY())
					.addMidTitles(player.getGuildTag(), player.getName())
					.addTitles(player.getName())
					.addContents(player.getIcon(),
							player.getName(),
							player.getPower(),
							playerPos[0],
							playerPos[1],
							player.getPfIcon(),
							point.getX(),
							point.getY(),
							player.getGuildTag())
					.build());
		}

		// 统计侦察次数，以到达目标为准
		player.getData().getStatisticsEntity().addSpyCnt(1);
	}
	
	/**
	 * 侦查超级武器的邮件处理
	 */
	private void doXzqSpy(WorldMarch march, WorldPoint point, Player player) {

		XZQWorldPoint weapon = (XZQWorldPoint) point;
		// 如果首都的驻扎联盟是自己的，则侦查直接返回
		boolean saveGuild = Objects.equals(weapon.getGuildId(), player.getGuildId());
		if (weapon.isPeace() || saveGuild) {
			FightMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(march.getPlayerId())
					.addSubTitles(point.getX(), point.getY())
					.addContents(point.getX(), point.getY())
					.setMailId(MailId.DETECT_XZQ_FAILED)
					.build());
			return;
		}
		// 首都上驻扎的行军
		List<IWorldMarch> quarteredMarchs = new ArrayList<>();
		if(weapon.hasNpc()){
			quarteredMarchs.add(weapon.getNpcMarch());
		}else{
			quarteredMarchs.addAll(WorldMarchService.getInstance().getXZQStayMarchs(point.getId()));
		}
		// 确认侦查类型
		final int radarLevel = radaLevel(player);

		PBDetectCapital.Builder builder = PBDetectCapital.newBuilder()
				.setLevel(radarLevel)
				.setPointX(point.getX())
				.setPointY(point.getY());
		if (radarLevel >= GsConst.RadarLevel.LV10) {
			for (IWorldMarch ma : quarteredMarchs) {
				MailArmyInfo.Builder defenceArmy = MailBuilderUtil.getDefenceArmy(radarLevel, ma, radarLevel < GsConst.RadarLevel.LV18);
				builder.addDefenceArmy(defenceArmy);
			}
		}
		if (radarLevel >= GsConst.RadarLevel.LV4 && radarLevel < GsConst.RadarLevel.LV10) {
			int assistTotal = quarteredMarchs.stream()
					.flatMap(m -> m.getMarchEntity().getArmys().stream())
					.mapToInt(a -> a.getFreeCnt())
					.sum();
			builder.setDefenceArmyAboutNum(GameUtil.getProbablyNum(assistTotal, GsConst.RADA_RANDOM_RANGE));
		}

		// 侦查邮件
		MailParames.Builder playerParamesBuilder = MailParames.newBuilder().setPlayerId(player.getId())
				.setMailId(MailId.DETECT_XZQ_SUCC)
				.addSubTitles(point.getX(), point.getY())
				.addContents(DetectMail.newBuilder().setDetectCapital(builder))
				.addTips(point.getX())
				.addTips(point.getY());

		MailParames mparames = playerParamesBuilder.build();
		FightMailService.getInstance().sendMail(mparames);
		WorldMarchService.getInstance().addSpyMark(player, point.getId(), mparames.getUuid(), "");
		// 向守军推送被侦查
		int[] playerPos = WorldPlayerService.getInstance().getPlayerPosXY(player.getId());
		for (IWorldMarch tempMarch : quarteredMarchs) {
			FightMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(tempMarch.getPlayerId())
					.setMailId(MailId.XZQ_BEDETECT)
					.addSubTitles(point.getX(), point.getY())
					.addMidTitles(player.getGuildTag(), player.getName())
					.addTitles(player.getName())
					.addContents(player.getIcon(),
							player.getName(),
							player.getPower(),
							playerPos[0],
							playerPos[1],
							player.getPfIcon(),
							point.getX(),
							point.getY(),
							player.getGuildTag())
					.build());
		}

		// 统计侦察次数，以到达目标为准
		player.getData().getStatisticsEntity().addSpyCnt(1);
	}

	/**
	 * 侦查联盟领地邮件处理
	 * 
	 * @param march
	 * @param point
	 * @return
	 */
	private void doGuildManorSpy(WorldMarch march, WorldPoint point, Player player) {

		String targetGuildId = point.getGuildId(); // 目标联盟建筑联盟id

		String pointLeaderPlayerId = GuildManorService.getInstance().getManorLeaderId(point.getId());
		if (StringUtils.isEmpty(pointLeaderPlayerId)) {
			pointLeaderPlayerId = GuildService.getInstance().getGuildLeaderId(targetGuildId);
		}

		if (GuildService.getInstance().isInTheSameGuild(player.getId(), pointLeaderPlayerId)) {
			spyManorSpyFail(march);
			return;
		}

		List<IWorldMarch> pointMarchs = GuildManorService.getInstance().getManorBuildMarch(point.getId());
		GuildManorObj manor = GuildManorService.getInstance().getGuildManorByPoint(point);
		if (Objects.isNull(manor)) {
			return;
		}

		final int radarLevel = radaLevel(player);
		String manorName = manor.getEntity().getManorName();
		PBDetectManor.Builder builder = PBDetectManor.newBuilder()
				.setLevel(radarLevel)
				.setManorName(manorName)
				.setManorX(point.getX())
				.setManorY(point.getY())
				.setManorState(manor.getEntity().getManorState())
				.setGuildTag(GuildService.getInstance().getGuildTag(manor.getGuildId()));
		if (radarLevel >= GsConst.RadarLevel.LV10) {
			for (IWorldMarch ma : pointMarchs) {
				MailArmyInfo.Builder defenceArmy = MailBuilderUtil.getDefenceArmy(radarLevel, ma, radarLevel < GsConst.RadarLevel.LV18);
				builder.addDefenceArmy(defenceArmy);
			}
		}
		if (radarLevel >= GsConst.RadarLevel.LV4 && radarLevel < GsConst.RadarLevel.LV10) {
			int assistTotal = pointMarchs.stream()
					.flatMap(m -> m.getMarchEntity().getArmys().stream())
					.mapToInt(a -> a.getFreeCnt())
					.sum();
			builder.setDefenceArmyAboutNum(GameUtil.getProbablyNum(assistTotal, GsConst.RADA_RANDOM_RANGE));
		}

		Player targetPlayer = GlobalData.getInstance().makesurePlayer(pointLeaderPlayerId);

		// 侦查邮件
		MailParames.Builder playerParamesBuilder = MailParames.newBuilder().setPlayerId(player.getId())
				.setMailId(MailId.DETECT_GUILD_BASTION_SUCC_TO_FROM)
				.addSubTitles(manorName, point.getX(), point.getY())
				.addContents(DetectMail.newBuilder().setDetectManor(builder))
				.setOppPfIcon(targetPlayer.getPfIcon())
				.addTips(manorName);
		MailParames mparames = playerParamesBuilder.build();
		FightMailService.getInstance().sendMail(mparames);
		WorldMarchService.getInstance().addSpyMark(player, point.getId(), mparames.getUuid(), "");
		// 被侦查邮件
		MailParames.Builder targetParamesBuilder = MailParames.newBuilder().setPlayerId(targetPlayer.getId())
				.addSubTitles(point.getX(), point.getY())
				.addMidTitles(player.getGuildTag(), player.getName())
				.setOppPfIcon(player.getPfIcon())
				.setMailId(MailId.DETECT_GUILD_BASTION_SUCC_TO_TARGET)
				.addContents(player.getIcon(), player.getName(), player.getPower(), point.getX(), point.getY(), player.getPfIcon(), player.getGuildTag())
				.addTips(player.getName());
		FightMailService.getInstance().sendMail(targetParamesBuilder.build());

		return;
	}

	/**
	 * 侦查目标是玩家
	 * 
	 * @param march
	 * @param reachTime
	 * @return
	 */
	private boolean doPlayerSpy(WorldMarch march, WorldPoint point, Player player) {
		// 行军到达时判断是否可以侦查
		Player targetPlayer = GlobalData.getInstance().makesurePlayer(march.getTargetId());
		if (!this.getSpyResult(march, point, targetPlayer, player)) {
			// 行军返回
			WorldMarchService.getInstance().onPlayerNoneAction(this, march.getReachTime());
			return false;
		}

		// 侦察成功邮件
		this.sendDetectMail(player, targetPlayer, point);

		player.getData().getStatisticsEntity().addSpyCnt(1); // 统计侦察次数，以到达目标为准

		// 添加交互信息
		long curTime = HawkTime.getMillisecond();
		LocalRedis.getInstance().addInteractivePlayer(player.getId(), targetPlayer.getId(), curTime);
		LocalRedis.getInstance().addInteractivePlayer(targetPlayer.getId(), player.getId(), curTime);

		// 行军返回
		WorldMarchService.getInstance().onPlayerNoneAction(this, march.getReachTime());
		return true;
	}

	/**
	 * 获取侦查结果
	 *
	 * @param march
	 * @param point
	 * @param targetPlayer
	 * @return
	 */
	private boolean getSpyResult(WorldMarch march, WorldPoint point, Player targetPlayer, Player player) {

		if (targetPlayer == null || march == null || point == null) {
			return false;
		}

		int playerCityLvl = player.getCityLv();
		int targetCityLvl = targetPlayer.getCityLv();

		// 目标点没东西 point 此形参在调用此方法之前已经判断过，这里肯定不为null
		if (HawkOSOperator.isEmptyString(point.getPlayerId())) {
			Object[] subTitle;
			Object[] content;
			MailId mailId;
			if (point.getPointType() == WorldPointType.PLAYER_VALUE) {
				// 发送邮件---侦查玩家基地失败，被侦查方高迁或被打飞（侦查方）
				subTitle = new Object[] { march.getTerminalX(), march.getTerminalY() };
				content = new Object[] { targetPlayer.getIcon(), targetPlayer.getName(), targetPlayer.getPower(),
						march.getTerminalX(), march.getTerminalY(), targetPlayer.getPfIcon(), playerCityLvl, targetCityLvl, targetPlayer.getGuildTag() };
				mailId = MailId.DETECT_BASE_FAILED_TARGET_MOVED;

			} else if (point.getPointType() == WorldPointType.RESOURCE_VALUE) {
				// 发送邮件---侦查玩家资源点失败，资源点消失（侦察方）
				subTitle = new Object[] { march.getTerminalX(), march.getTerminalY() };
				content = new Object[] { march.getTargetPointField(), march.getTerminalX(), march.getTerminalY(), playerCityLvl, targetCityLvl };
				mailId = MailId.DETECT_RES_FAILED_TARGET_DISAPPEAR;

			} else if (point.getPointType() == WorldPointType.QUARTERED_VALUE) {
				// 发送邮件---侦查玩家驻扎点失败，驻扎点消失（侦察方）
				subTitle = new Object[] { march.getTerminalX(), march.getTerminalY() };
				content = new Object[] { targetPlayer.getName(), march.getTerminalX(), march.getTerminalY(), playerCityLvl, targetCityLvl, targetPlayer.getGuildTag() };
				mailId = MailId.DETECT_CAMP_FAILED_TARGET_DISAPPEAR;
			} else {
				return false;
			}

			FightMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(march.getPlayerId())
					.setMailId(mailId).addSubTitles(subTitle).addContents(content).addTips(targetPlayer.getName()).build());

			return false;
		}

		// 目标点换人或属于同一联盟
		if (!point.getPlayerId().equals(targetPlayer.getId())
				|| GuildService.getInstance().isInTheSameGuild(player.getId(), point.getPlayerId())) {

			Object[] subTitle;
			Object[] content;
			MailId mailId;
			if (point.getPointType() == WorldPointType.PLAYER_VALUE) {
				// 发送邮件---侦查玩家基地失败，被侦查方状态改变（侦察方）
				Player pointPlayer = GlobalData.getInstance().makesurePlayer(point.getPlayerId());
				subTitle = new Object[] { march.getTerminalX(), march.getTerminalY() };
				content = new Object[] { pointPlayer.getIcon(), targetPlayer.getName(), pointPlayer.getPower(),
						march.getTerminalX(), march.getTerminalY(), targetPlayer.getPfIcon(), playerCityLvl, targetCityLvl, pointPlayer.getGuildTag() };
				mailId = MailId.DETECT_BASE_FAILED_TARGET_CHANGED;

			} else if (point.getPointType() == WorldPointType.RESOURCE_VALUE) {
				// 发送邮件---侦查玩家资源点失败，资源点状态改变（侦察方）
				subTitle = new Object[] { march.getTerminalX(), march.getTerminalY() };
				content = new Object[] { point.getResourceId(), march.getTerminalX(), march.getTerminalY(), playerCityLvl, targetCityLvl };
				mailId = MailId.DETECT_RES_FAILED_TARGET_CHANGED;

			} else if (point.getPointType() == WorldPointType.QUARTERED_VALUE) {
				// 发送邮件---侦查玩家驻扎点失败，驻扎点状态改变（侦察方）
				subTitle = new Object[] { march.getTerminalX(), march.getTerminalY() };
				content = new Object[] { targetPlayer.getName(), march.getTerminalX(), march.getTerminalY(), targetPlayer.getGuildTag(), playerCityLvl, targetCityLvl };
				mailId = MailId.DETECT_CAMP_FAILED_TARGET_CHANGED;
			} else {
				return false;
			}

			FightMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(march.getPlayerId())
					.setMailId(mailId).addSubTitles(subTitle).addContents(content).addTips(targetPlayer.getName()).build());
			return false;
		}

		int[] playerPos = WorldPlayerService.getInstance().getPlayerPosXY(player.getId());
		// 城点已进入保护状态，不让侦查
		if (point.getPointType() != World.WorldPointType.PLAYER_VALUE) {
			return true;
		}
		if (HawkTime.getMillisecond() <= point.getShowProtectedEndTime()) {
			// 发送邮件---侦查玩家基地失败，被侦查方开启保护（侦查方）
			FightMailService.getInstance()
					.sendMail(MailParames.newBuilder().setPlayerId(player.getId())
							.setMailId(MailId.DETECT_BASE_FAILED_PROTECTION_TO_FROM)
							.addSubTitles(targetPlayer.getName(), march.getTerminalX(), march.getTerminalY())
							.addContents(targetPlayer.getIcon(), targetPlayer.getName(), targetPlayer.getPower(),
									march.getTerminalX(), march.getTerminalY(), targetPlayer.getPfIcon(), playerCityLvl, targetCityLvl, targetPlayer.getGuildTag())
							.addTips(targetPlayer.getName())
							.addMidTitles(targetPlayer.getGuildTag(), targetPlayer.getName())
							.build());
			// 发送邮件---侦查玩家基地失败，被侦查方开启保护（被侦察方）
			FightMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(targetPlayer.getId())
					.setMailId(MailId.DETECT_BASE_FAILED_PROTECTION_TO_TARGET)
					.addContents(player.getIcon(), player.getName(), player.getPower(), playerPos[0], playerPos[1], player.getPfIcon(), targetCityLvl, playerCityLvl,
							player.getGuildTag())
					.addMidTitles(player.getGuildTag(), player.getName())
					.build());
			return false;

		}

		if (targetPlayer.getData().getEffVal(Const.EffType.CITY_SCORT_NO) > 0) {
			// 发送邮件---侦查玩家基地失败，被侦查方开启反侦察（侦查方）
			FightMailService.getInstance()
					.sendMail(MailParames.newBuilder().setPlayerId(player.getId())
							.setMailId(MailId.DETECT_BASE_FAILED_AGAINST_TO_FROM)
							.addSubTitles(targetPlayer.getName(), march.getTerminalX(), march.getTerminalY())
							.addContents(targetPlayer.getIcon(), targetPlayer.getName(), targetPlayer.getPower(),
									march.getTerminalX(), march.getTerminalY(), targetPlayer.getPfIcon(), playerCityLvl, targetCityLvl, targetPlayer.getGuildTag())
							.addTips(targetPlayer.getName())
							.addMidTitles(targetPlayer.getGuildTag(), targetPlayer.getName())
							.build());
			// 发送邮件---侦查玩家基地失败，被侦查方开启反侦察（被侦察方）
			FightMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(targetPlayer.getId())
					.setMailId(MailId.DETECT_BASE_FAILED_AGAINST_TO_TARGET)
					.addMidTitles(player.getGuildTag(), player.getName())
					.addContents(player.getIcon(), player.getName(), player.getPower(), playerPos[0], playerPos[1], player.getPfIcon(), targetCityLvl, playerCityLvl,
							player.getGuildTag())
					.build());
			return false;
		}

		int radarLevel = player.getData().getBuildingMaxLevel(BuildingType.RADAR_VALUE);
		int tarradarLevel = targetPlayer == null ? 0 : targetPlayer.getData().getBuildingMaxLevel(BuildingType.RADAR_VALUE);
		if (radarLevel + 10 < tarradarLevel && tarradarLevel >= GsConst.RadarLevel.LV32) {
			// 发送邮件---侦查玩家基地失败，被侦查方开启反侦察（侦查方）
			FightMailService.getInstance()
					.sendMail(MailParames.newBuilder().setPlayerId(player.getId())
							.setMailId(MailId.DETECT_CITY_FAILED10_TO_FROM)
							.addSubTitles(targetPlayer.getName(), march.getTerminalX(), march.getTerminalY())
							.addContents(targetPlayer.getIcon(), targetPlayer.getName(), targetPlayer.getPower(),
									march.getTerminalX(), march.getTerminalY(), targetPlayer.getPfIcon(), playerCityLvl, targetCityLvl, targetPlayer.getGuildTag())
							.addTips(targetPlayer.getName())
							.addMidTitles(targetPlayer.getGuildTag(), targetPlayer.getName())
							.build());
			// 发送邮件---侦查玩家基地失败，被侦查方开启反侦察（被侦察方）
			FightMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(targetPlayer.getId())
					.setMailId(MailId.DETECT_CITY_FAILED10_TO_TAR)
					.addContents(player.getIcon(), player.getName(), player.getPower(), playerPos[0], playerPos[1], player.getPfIcon(), targetCityLvl, playerCityLvl,
							player.getGuildTag())
					.addMidTitles(player.getGuildTag(), player.getName())
					.build());
			return false;
		}

		return true;
	}

	/**
	 * 发送侦察成功邮件
	 * 
	 * @param player
	 * @param targetPlayer
	 * @param point
	 * @return
	 */
	private boolean sendDetectMail(Player player, Player targetPlayer, WorldPoint point) {
		if (player == null || targetPlayer == null || point == null) {
			return false;
		}

		// 对方使用了部队镜像
		boolean isMirror = false;
		if (point.getPointType() == WorldPointType.PLAYER_VALUE
				&& targetPlayer.getData().getEffVal(EffType.CITY_SCORT_MIRROR) > 0) {
			isMirror = true;
		}

		BuildingBaseEntity buildingEntity = player.getData().getBuildingEntityByType(BuildingType.RADAR);
		if (buildingEntity == null) {
			return true;
		}
		BuildingCfg buildCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
		if (buildCfg == null) {
			return true;
		}
		DetectMail.Builder builder = MailBuilderUtil.createDetectMail(buildCfg.getLevel(), player, targetPlayer, point, null,
				isMirror);
		builder.setSelfPower(player.getPower());

		int[] pos = WorldPlayerService.getInstance().getPlayerPosXY(player.getId());
		MailPlayerInfo pbtplayer = builder.getDetectData().getPlayer();
		Object[] subTitle = new Object[] { pbtplayer.getName(), pbtplayer.getX(),
				pbtplayer.getY() };
		MailParames.Builder playerParamesBuilder = MailParames.newBuilder().setPlayerId(player.getId()).addSubTitles(subTitle)
				.addContents(builder).setOppPfIcon(targetPlayer.getPfIcon()).addTips(targetPlayer.getName())
				.addMidTitles(targetPlayer.getGuildTag(), targetPlayer.getName());
		MailParames.Builder targetParamesBuilder = MailParames.newBuilder().setPlayerId(targetPlayer.getId())
				.setOppPfIcon(player.getPfIcon()).addTips(player.getName())
				.addMidTitles(player.getGuildTag(), player.getName());
		int targetConstructionFactoryLevel = targetPlayer.getCityLevel();
		int constructionFactoryLevel = player.getCityLevel();
		if (point.getPointType() == WorldPointType.PLAYER_VALUE) {
			// 发送邮件---侦查玩家基地成功（侦察方）
			playerParamesBuilder.setMailId(MailId.DETECT_BASE_SUCC_TO_FROM);
			// 发送邮件---侦查玩家基地成功（被侦察方）
			targetParamesBuilder
					.setMailId(MailId.DETECT_BASE_SUCC_TO_TARGET)
					.addContents(player.getIcon())
					.addContents(player.getName())
					.addContents(player.getPower())
					.addContents(pos[0])
					.addContents(pos[1])
					.addContents(player.getPfIcon())
					.addContents(targetConstructionFactoryLevel)
					.addContents(constructionFactoryLevel)
					.addContents(player.getGuildTag());

		} else if (point.getPointType() == WorldPointType.RESOURCE_VALUE) {
			// 发送邮件---侦查玩家资源点成功（侦察方）
			playerParamesBuilder.setMailId(MailId.DETECT_RES_SUCC_TO_FROM);
			// 发送邮件---侦查玩家资源点成功（被侦察方）
			Object[] content = new Object[] { player.getIcon(), player.getName(), player.getPower(), pos[0], pos[1], player.getPfIcon(), targetConstructionFactoryLevel,
					constructionFactoryLevel, player.getGuildTag(), point.getX(), point.getY() };
			targetParamesBuilder.setMailId(MailId.DETECT_RES_SUCC_TO_TARGET).addContents(content);
		} else if (point.getPointType() == WorldPointType.QUARTERED_VALUE) {
			// 发送邮件---侦查玩家驻扎点成功（侦察方）
			playerParamesBuilder.setMailId(MailId.DETECT_CAMP_SUCC_TO_FROM);
			// 发送邮件---侦查玩家驻扎点成功（被侦察方）
			Object[] content = new Object[] { player.getIcon(), player.getName(), player.getPower(), pos[0], pos[1],
					player.getPfIcon(), targetConstructionFactoryLevel, constructionFactoryLevel, player.getGuildTag() };
			targetParamesBuilder.setMailId(MailId.DETECT_CAMP_SUCC_TO_TARGET).addContents(content);
		}
		MailParames mparames = playerParamesBuilder.build();
		FightMailService.getInstance().sendMail(mparames);
		WorldMarchService.getInstance().addSpyMark(player, point.getId(), mparames.getUuid(), builder.getDetectData().getLeaderMarchId());

		FightMailService.getInstance().sendMail(targetParamesBuilder.build());
		return true;
	}

	@Override
	public void onMarchStart() {
		this.pushAttackReport();
	}

	@Override
	public void onMarchReturn() {
		// 删除行军报告
		this.removeAttackReport();
	}

	@Override
	public void remove() {
		super.remove();
		// 删除行军报告
		this.removeAttackReport();
	}

	@Override
	public Set<String> attackReportRecipients() {
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(getMarchEntity().getAlarmPointId());
		if (Objects.nonNull(point) && point.getPointType() == WorldPointType.PLAYER_VALUE) {
			return ReportRecipients.TargetAndHisAssistance.attackReportRecipients(this);
		}
		if (Objects.nonNull(point) && point.getPointType() == WorldPointType.CROSS_FORTRESS_VALUE) {
			return WorldMarchService.getInstance().getFortressStayMarchs(point.getId()).stream().map(IWorldMarch::getPlayerId).collect(Collectors.toSet());
		}
		Set<String> result = alarmPointEnemyMarches().stream()
				.filter(march -> march.getMarchEntity().getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MARCH_VALUE)
				.map(IWorldMarch::getPlayerId)
				.filter(tid -> !Objects.equals(tid, this.getMarchEntity().getPlayerId()))
				.collect(Collectors.toSet());
		return result;
	}
	
	
	
	@Override
	public List<IWorldMarch> alarmPointMarches() {
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(getMarchEntity().getAlarmPointId());
		if (Objects.nonNull(point) && point.getPointType() == WorldPointType.KING_PALACE_VALUE) {
			return WorldMarchService.getInstance().getPresidentQuarteredMarchs();
		}
		return IReportPushMarch.super.alarmPointMarches();
	}

	@Override
	public boolean isNationMassMarch() {
//		if (CrossActivityService.getInstance().isOpen()) {
//			WorldPoint point = WorldPointService.getInstance().getWorldPoint(getMarchEntity().getAlarmPointId());
//			if (Objects.nonNull(point) && point.getPointType() == WorldPointType.KING_PALACE_VALUE) {
//				return true;
//			}
//		}
		return false;
	}

	private int radaLevel(Player player) {
		// 确认侦查类型
		BuildingBaseEntity buildingEntity = player.getData().getBuildingEntityByType(BuildingType.RADAR);
		if (Objects.isNull(buildingEntity)) {
			return 0;
		}
		BuildingCfg buildCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
		if (buildCfg == null) {
			return 0;
		}

		return buildCfg.getLevel();
	}
}
