package com.hawk.game.world.march.submarch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;

import com.hawk.game.config.BuildingCfg;
import com.hawk.game.entity.TalentEntity;
import com.hawk.game.entity.TechnologyEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Armour.ArmourBriefInfo;
import com.hawk.game.protocol.Armour.ArmourSuitType;
import com.hawk.game.protocol.Army.ArmySoldierPB;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.TerritoryType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.AttackMarchReportPB;
import com.hawk.game.protocol.World.GeneralPB;
import com.hawk.game.protocol.World.MarchReportPB;
import com.hawk.game.protocol.World.MarchTargetPointType;
import com.hawk.game.protocol.World.TaltentPB;
import com.hawk.game.protocol.World.WorldMarchRefreshPB;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.flag.FlagCollection;
import com.hawk.game.service.warFlag.IFlag;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.Predicates;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.march.impl.YuriRevengeMonsterMarch;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;

/**
 * 
 * @author lwt
 * @date 2017年10月6日
 */
public interface IReportPushMarch extends IWorldMarch {
	enum ReportRecipients {
		/**
		 * 目标和他的盟友
		 */
		TargetAndHisAssistance {
			@Override
			public Set<String> attackReportRecipients(IReportPushMarch march) {
				WorldPoint point = WorldPointService.getInstance().getWorldPoint(march.getTerminalId());
				if (point == null || point.getPointType() != WorldPointType.PLAYER_VALUE) {
					return Collections.emptySet();
				}

				Set<String> allToNotify = new HashSet<>();
				String targetId = point.getPlayerId();
				allToNotify.add(targetId);

				BlockingQueue<IWorldMarch> helpMarchList = WorldMarchService.getInstance().getPlayerPassiveMarch(targetId);
				Predicate<IWorldMarch> isAssistanceMarch = Predicates.of((IWorldMarch m) -> m.getMarchType() == WorldMarchType.ASSISTANCE);
				helpMarchList.stream()
						.filter(isAssistanceMarch)
						.filter(m -> m.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE)
						.filter(m -> m.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MARCH_VALUE)
						.map(m -> m.getPlayerId()).forEach(allToNotify::add);
				return allToNotify;
			}
		},
		/**
		 * 联盟堡垒
		 */
		TargetManor {
			@Override
			public Set<String> attackReportRecipients(IReportPushMarch march) {
				// MARCH_STATUS_MANOR_BUILD = 8; // 领地建造状态
				// MARCH_STATUS_MANOR_REPAIR = 9; // 领地修复状态
				// MARCH_STATUS_MANOR_BREAK = 10; // 领地摧毁状态
				// MARCH_STATUS_MANOR_GARRASION = 11; // 领地驻防状态
				Predicate<IWorldMarch> zhuZhaZhong = Predicates
						.of((IWorldMarch tm) -> tm.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MANOR_BUILD_VALUE)
						.or((IWorldMarch tm) -> tm.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MANOR_REPAIR_VALUE)
						.or((IWorldMarch tm) -> tm.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MANOR_BREAK_VALUE)
						.or((IWorldMarch tm) -> tm.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MANOR_GARRASION_VALUE);

				return march.alarmPointMarches().stream()
						.filter(zhuZhaZhong)
						.map(IWorldMarch::getPlayerId)
						.filter(tid -> !Objects.equals(tid, march.getPlayerId()))
						.collect(Collectors.toSet());
			}
		},
		TargetPresident {
			@Override
			public Set<String> attackReportRecipients(IReportPushMarch march) {
				Predicate<IWorldMarch> zhuZhaZhong = Predicates
						.of((IWorldMarch tm) -> tm.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE);
				return WorldMarchService.getInstance().getPresidentQuarteredMarchs().stream()
						.filter(zhuZhaZhong)
						.map(IWorldMarch::getPlayerId)
						.filter(tid -> !Objects.equals(tid, march.getPlayerId()))
						.collect(Collectors.toSet());
			}
		},
		TargetPresidentTower {
			@Override
			public Set<String> attackReportRecipients(IReportPushMarch march) {
				Predicate<IWorldMarch> zhuZhaZhong = Predicates
						.of((IWorldMarch tm) -> tm.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE);
				return WorldMarchService.getInstance().getPresidentTowerStayMarchs(march.getAlarmPointId()).stream()
						.filter(zhuZhaZhong)
						.map(IWorldMarch::getPlayerId)
						.filter(tid -> !Objects.equals(tid, march.getPlayerId()))
						.collect(Collectors.toSet());
			}
		},
		TargetSuperWeapon {
			@Override
			public Set<String> attackReportRecipients(IReportPushMarch march) {
				Predicate<IWorldMarch> zhuZhaZhong = Predicates
						.of((IWorldMarch tm) -> tm.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE);
				return WorldMarchService.getInstance().getSuperWeaponStayMarchs(march.getAlarmPointId()).stream()
						.filter(zhuZhaZhong)
						.map(IWorldMarch::getPlayerId)
						.filter(tid -> !Objects.equals(tid, march.getPlayerId()))
						.collect(Collectors.toSet());
			}
		},
		
		TargetXzq {
			@Override
			public Set<String> attackReportRecipients(IReportPushMarch march) {
				Predicate<IWorldMarch> zhuZhaZhong = Predicates
						.of((IWorldMarch tm) -> tm.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE);
				return WorldMarchService.getInstance().getXZQStayMarchs(march.getAlarmPointId()).stream()
						.filter(zhuZhaZhong)
						.map(IWorldMarch::getPlayerId)
						.filter(tid -> !Objects.equals(tid, march.getPlayerId()))
						.collect(Collectors.toSet());
			}
		}
		;
		public abstract Set<String> attackReportRecipients(IReportPushMarch march);
	}

	public static boolean hasRedAlarm(String playerId) {
		WorldPoint pt = WorldPlayerService.getInstance().getPlayerWorldPoint(playerId);
		if (pt == null) {
			return false;
		}
		BlockingQueue<IWorldMarch> worldMarchs = WorldMarchService.getInstance().getPlayerPassiveMarch(playerId);
		for (IWorldMarch march : worldMarchs) {
			if (!(march instanceof IReportPushMarch)) {
				continue;
			}
			IReportPushMarch pm = (IReportPushMarch) march;
			if (!pm.attackReportRecipients().contains(playerId)) {
				continue;
			}

			if (WorldMarchService.getInstance().isOffensiveAction(playerId, march.getMarchType(), pt)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 发送攻击报告
	 */
	default void pushAttackReport() {
		try {
			removeAttackReport();

			if (!isEvident()) {
				return;
			}

			Set<String> attackReportRecipients = attackReportRecipients();
			reportSend().addAll(attackReportRecipients);

			final WorldPoint point = WorldPointService.getInstance().getWorldPoint(this.getTerminalX(), this.getTerminalY());
			Set<IWorldMarch> memberMarchs = WorldMarchService.getInstance().getMassJoinMarchs(this, true);
			for (String playerId : attackReportRecipients) {
				doSendReport(playerId, memberMarchs, point);
			}
		} catch (Exception e) {
			e.printStackTrace();
			HawkException.catchException(e);
		}
	}

	/**
	 * 发送攻击报告给特定玩家
	 */
	default void pushAttackReport(String playerId) {
		if (!isEvident()) { // 不发送战报
			return;
		}
		Set<String> attackReportRecipients = attackReportRecipients();
		if (!attackReportRecipients.contains(playerId)) { // 未达到接受条件
			return;
		}
		reportSend().add(playerId);

		final WorldPoint point = WorldPointService.getInstance().getWorldPoint(this.getTerminalX(), this.getTerminalY());
		Set<IWorldMarch> memberMarchs = WorldMarchService.getInstance().getMassJoinMarchs(this, true);
		doSendReport(playerId, memberMarchs, point);

	}

	default void doSendReport(String playerId, Set<IWorldMarch> memberMarchs, WorldPoint point) {
		try {
			Player to = GlobalData.getInstance().getActivePlayer(playerId);
			// 在线即推送给客户端
			if (to == null || !to.isActiveOnline() || to.isInDungeonMap()) {
				return;
			}

			AttackMarchReportPB.Builder attReportBuilder = this.assembleEnemyMarchInfo(to, memberMarchs);
			MarchReportPB.Builder marchReport = MarchReportPB.newBuilder();
			marchReport.setAttackReport(attReportBuilder);
			marchReport.setTargetType(getMarchTargetPointType(to, point).getNumber());
			to.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MARCH_REPORT_PUSH, marchReport));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 可见报告
	 * 
	 * @return
	 */
	default boolean isEvident() {
		return this.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE;
	}

	/**
	 * 攻击报告接受者列表
	 * 
	 * @return
	 */
	Set<String> attackReportRecipients();

	/**
	 * 删除上一次发出的攻击报告
	 */
	default void removeAttackReport() {
		for (String playerId : reportSend()) {
			Player to = GlobalData.getInstance().getActivePlayer(playerId);
			// 在线即推送给客户端
			if (to == null || !to.isActiveOnline()) {
				continue;
			}
			WorldMarchRefreshPB.Builder builder = WorldMarchRefreshPB.newBuilder();
			builder.setMarchId(getMarchId());
			to.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MARCH_END_PUSH, builder));
		}
		reportSend().clear();
	}

	/** 删除发送给指定玩家的报告 */
	default void removeAttackReport(String tarPlayerId) {
		if (!reportSend().contains(tarPlayerId)) {
			return;
		}
		reportSend().remove(tarPlayerId);
		Player to = GlobalData.getInstance().getActivePlayer(tarPlayerId);
		// 在线即推送给客户端
		if (to == null || !to.isActiveOnline()) {
			return;
		}
		WorldMarchRefreshPB.Builder builder = WorldMarchRefreshPB.newBuilder();
		builder.setMarchId(getMarchId());
		to.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MARCH_END_PUSH, builder));

	}

	/***
	 * 移除指定点其它行军发送给自己的警报
	 */
	default void removeAttackReportFromPoint(int x, int y) {
		for (IWorldMarch targetMarch : pointMarches(x, y)) {
			if (targetMarch instanceof IReportPushMarch) {
				((IReportPushMarch) targetMarch).removeAttackReport(getPlayerId());
			}
		}
	}

	/**
	 * 行军速度改变
	 */
	default void reachTimeChange() {
		for (String playerId : reportSend()) {
			Player to = GlobalData.getInstance().getActivePlayer(playerId);
			// 在线即推送给客户端
			if (to == null || !to.isActiveOnline()) {
				continue;
			}
			WorldMarchRefreshPB.Builder builder = WorldMarchRefreshPB.newBuilder();
			builder.setMarchId(getMarchId());
			builder.setEndTime(getMarchEntity().getEndTime());
			to.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MARCH_REFRESH_PUSH, builder));
		}
	}

	/**
	 * 获取行军目标点通知类型
	 * 
	 * @param march
	 * @return
	 */
	default MarchTargetPointType getMarchTargetPointType(Player player, WorldPoint point) {
		if (point == null) {
			return MarchTargetPointType.QUARTERED_POINT;
		}

		switch (WorldPointType.valueOf(point.getPointType())) {
		case PLAYER: {
			String pointPid = point.getPlayerId();
			boolean isSelfCity = Objects.equals(player.getId(), pointPid);
			return isSelfCity ? MarchTargetPointType.PLAYER_CITY_POINT : MarchTargetPointType.ALLY_CITY;
		}
		case RESOURCE:
			return MarchTargetPointType.RESOURCE_POINT;

		case QUARTERED:
			return MarchTargetPointType.QUARTERED_POINT;

		case KING_PALACE:
			return MarchTargetPointType.CAPITAL_POINT;
		case CAPITAL_TOWER:
			return MarchTargetPointType.CAPITAL_TOWER_POINT;
		case GUILD_TERRITORY: {
			TerritoryType type = TerritoryType.valueOf(point.getBuildingId());
			switch (type) {
			case GUILD_BASTION:
				return MarchTargetPointType.GUILD_BASTION_POINT;
			case GUILD_MINE:
				return MarchTargetPointType.GUILD_SUPER_MINE;
			case GUILD_BARTIZAN:
				return MarchTargetPointType.GUILD_TOWER;
			case GUILD_STOREHOUSE:
				return MarchTargetPointType.GUILD_WARE_HOUSE;

			default:
				break;
			}

		}
		case STRONG_POINT:
			return MarchTargetPointType.WORLD_STRONGPOINT;
		case SUPER_WEAPON:
			return MarchTargetPointType.SUPER_WEAPON_POINT;
		case XIAO_ZHAN_QU:
			return MarchTargetPointType.XZQ_POINT;
		case WAR_FLAG_POINT:
			IFlag flag = FlagCollection.getInstance().getFlag(point.getGuildBuildId());
			if (flag == null || !flag.isCenter()) {
				return MarchTargetPointType.WAR_FLAG_MAP_POIT;
			} else {
				return MarchTargetPointType.CENTER_FLAG_POIT;
			}
		case CROSS_FORTRESS:
			return MarchTargetPointType.CROSS_FORTRESS_POINT;
		default:
			return MarchTargetPointType.QUARTERED_POINT;
		}

	}

	/**
	 * 警报触发点
	 * 
	 * @return
	 */
	default int[] alermPoint() {
		return GameUtil.splitXAndY(getMarchEntity().getAlarmPointId());
	}

	default List<IWorldMarch> alarmPointMarches() {
		int[] alarmPoint = this.alermPoint();
		return pointMarches(alarmPoint[0], alarmPoint[1]);
	}

	default List<IWorldMarch> pointMarches(int x, int y) {
		Collection<IWorldMarch> terminalPtMarchs = WorldMarchService.getInstance().getWorldPointMarch(x, y);

		if (terminalPtMarchs == null || terminalPtMarchs.isEmpty()) {
			return Collections.emptyList();
		}

		Set<IWorldMarch> result = new HashSet<>();
		for (IWorldMarch targetMarch : terminalPtMarchs) {
			if (targetMarch == null || targetMarch.getMarchId().equals(this.getMarchId())) {
				continue;
			}
			if (targetMarch.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
				continue;
			}
			
			// 加入集结的
			if (targetMarch.isMassMarch()) {
				Set<IWorldMarch> massJoinMarchs = WorldMarchService.getInstance().getMassJoinMarchs(targetMarch, true);
				for (IWorldMarch massJoin : massJoinMarchs) {
					if (massJoin.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
						continue;
					}
					result.add(massJoin);
				}
			}
			
			result.add(targetMarch);
		}
		return new ArrayList<>(result);
	}

	/**
	 * 警报点上的乱对行军 (不包括回程中行军)
	 * 
	 * @return
	 */
	default List<IWorldMarch> alarmPointEnemyMarches() {
		List<IWorldMarch> result = new ArrayList<>();
		//boolean nationMassMarch = isNationMassMarch();
		for (IWorldMarch march : alarmPointMarches()) {
			if (GuildService.getInstance().isInTheSameGuild(this.getPlayerId(), march.getPlayerId())) {
				continue;
			}
//			if (nationMassMarch && GuildService.getInstance().isSameCamp(getPlayer(), march.getPlayer().getGuildId())) {
//				continue;
//			}
			result.add(march);
		}
		return result;
	}

	/**
	 * 收集进攻型行军报告信息 原雷达
	 * 
	 * @param enemyPlayer
	 *            被进攻者玩家
	 * @param leaderMarch
	 *            集结队伍中队长的行军信息
	 * @param memberMarchs
	 *            集结队伍中队员的行军信息
	 * @param marchReport
	 *            组装行军信息的PB实体
	 */
	default AttackMarchReportPB.Builder assembleEnemyMarchInfo(Player enemyPlayer, Set<IWorldMarch> memberMarchs) {
		AttackMarchReportPB.Builder marchReport = AttackMarchReportPB.newBuilder();
		marchReport.setMarchUUID(this.getMarchId());
		WorldMarchType marchType = this.getMarchType();
		marchReport.setMarchType(marchType.getNumber());

		if (this instanceof YuriRevengeMonsterMarch) {
			YuriRevengeMonsterMarch yuriRevengeMonsterMarch = (YuriRevengeMonsterMarch) this;
			marchReport.setYuriLastRound(yuriRevengeMonsterMarch.isLastRound());
			marchReport.setYuriNextPushTime(yuriRevengeMonsterMarch.getNextPushTime());
			marchReport.setYuriRound(yuriRevengeMonsterMarch.getRound());
		}

		Player leaderMarchplayer = this.getPlayer();
		if (this.isNationMassMarch()) {
			marchReport.setSameGuild(GuildService.getInstance().isSameCamp(leaderMarchplayer, enemyPlayer.getGuildId()));
		} else if (leaderMarchplayer.hasGuild()) {
			marchReport.setSameGuild(Objects.equals(enemyPlayer.getGuildId(), leaderMarchplayer.getGuildId()));
		}

		BuildingCfg buildingCfg = enemyPlayer.getData().getBuildingCfgByType(BuildingType.RADAR);
		if (buildingCfg == null) {
			return marchReport;
		}
		int radarLevel = buildingCfg.getLevel();

		marchReport.setMarchStartTime(this.getStartTime());
		// 行军到达时间
		if (radarLevel >= GsConst.RadarLevel.LV5) {
			marchReport.setArrivalTime(this.getEndTime());
		}
		if (this.isMassMarch()) { // 出发时间大于当前时间, 是集结
			marchReport.setMassReadyTime(this.getMassReadyTime());
		}

		if (marchType == WorldMarchType.ASSISTANCE_RES) {
			String assistantStr = this.getAssistantStr();
			if (StringUtils.isNotEmpty(assistantStr)) {
				marchReport.setAssistant(assistantStr);
			}
		}

		if (radarLevel >= GsConst.RadarLevel.LV9 && Objects.nonNull(memberMarchs)) { // 告知所有领主确切等级
			for (IWorldMarch march : memberMarchs) {
				Player mplayer = GlobalData.getInstance().makesurePlayer(march.getPlayerId());
				GeneralPB.Builder general = assembleGeneralPB(mplayer, march, radarLevel);
				marchReport.addMember(general);
			}
		}
		if (radarLevel >= GsConst.RadarLevel.LV7) {
			List<IWorldMarch> allMarch = new ArrayList<IWorldMarch>();
			allMarch.add(this);
			Optional.ofNullable(memberMarchs).ifPresent(allMarch::addAll);
			int totalSoldierPopulation = allMarch.stream().flatMap(m -> m.getArmys().stream()).mapToInt(e -> e.getTotalCount()).sum();
			marchReport.setArmyTotalAbout(totalSoldierPopulation);// 准确数
		}

		// 发起行军的出兵位置
		if (radarLevel >= GsConst.RadarLevel.LV3) {
			int[] xy = GameUtil.splitXAndY(this.getOrigionId());
			marchReport.setOriginalX(xy[0]);
			marchReport.setOriginalY(xy[1]);
		}
		marchReport.setTargetX(this.getTerminalX());
		marchReport.setTargetY(this.getTerminalY());

		// 发起行军的玩家的id
		if (radarLevel >= GsConst.RadarLevel.LV1) {
			GeneralPB.Builder general = assembleGeneralPB(leaderMarchplayer, this, radarLevel);
			marchReport.setLeader(general);
		}
		return marchReport;
	}

	/**
	 * 
	 * @param player
	 * @param march
	 * @param radarLevel
	 * @param randomRange
	 * @param isleader
	 *            是否进攻发起者
	 * @return
	 */
	default GeneralPB.Builder assembleGeneralPB(Player player, IWorldMarch march, int radarLevel) {
		GeneralPB.Builder general = GeneralPB.newBuilder().setName(player.getName()).setId(player.getId()).setIcon(player.getIcon())
				.setPfIcon(Optional.ofNullable(player.getPfIcon()).orElse(""));
		if (player.hasGuild()) {
			general.setGuildName(player.getGuildName()).setGuildId(player.getGuildId())
					.setGuildTag(GuildService.getInstance().getGuildTag(player.getGuildId()) == null ? "" : GuildService.getInstance().getGuildTag(player.getGuildId()));
		}

		if (radarLevel >= GsConst.RadarLevel.LV9) {
			general.setLevel(player.getCityLevel());
		}
		if (radarLevel < GsConst.RadarLevel.LV11) {
			return general;
		}
		for (ArmyInfo armyInfo : march.getArmys()) {
			ArmySoldierPB.Builder builder = ArmySoldierPB.newBuilder();
			builder.setArmyId(armyInfo.getArmyId());
			builder.setStar(player.getSoldierStar(armyInfo.getArmyId()));
			builder.setPlantStep(player.getSoldierStep(armyInfo.getArmyId()));
			builder.setPlantSkillLevel(player.getSoldierPlantSkillLevel(armyInfo.getArmyId()));
			builder.setPlantMilitaryLevel(player.getSoldierPlantMilitaryLevel(armyInfo.getArmyId()));
			if (radarLevel >= GsConst.RadarLevel.LV13) {
				builder.setCount(armyInfo.getTotalCount());
			}
			general.addArmyInfo(builder);
		}
		// 天赋
		if (radarLevel >= GsConst.RadarLevel.LV29) {
			int talentType = player.getData().getPlayerEntity().getTalentType();
			List<TalentEntity> talentEntities = player.getData().getTalentEntities();
			for (TalentEntity talentEntity : talentEntities) {
				if (talentType != talentEntity.getType()) {
					continue;
				}
				if (talentEntity.getLevel() > 0 && !talentEntity.isInvalid()) {
					general.addTalent(TaltentPB.newBuilder().setLevel(talentEntity.getLevel()).setTalentId(talentEntity.getTalentId()));
				}
			}
		}

		// 科技
		if (radarLevel >= GsConst.RadarLevel.LV26) {
			List<Integer> allTech = player.getData().getTechnologyEntities().stream().filter(e -> e.getLevel() > 0).map(TechnologyEntity::getCfgId).collect(Collectors.toList());
			general.addAllTech(allTech);
		}

		for (PlayerHero hero : march.getHeros()) {
			general.addHeroInfo(hero.toPBobj());
		}
		if (march.getSuperSoldierId() > 0) {
			Optional<SuperSoldier> ssoldierOp = player.getSuperSoldierByCfgId(march.getSuperSoldierId());
			if (ssoldierOp.isPresent()) {
				general.setSsoldier(ssoldierOp.get().toPBobj());
			}
		}
		ArmourBriefInfo armour = player.genArmourBriefInfo( ArmourSuitType.valueOf(march.getMarchEntity().getArmourSuit()));
		general.setArmourBrief(armour);
		general.setArmourSuit(ArmourSuitType.valueOf(march.getMarchEntity().getArmourSuit()));
		return general;
	}
	
	/**接收到警报的玩家*/
	default Set<String> reportSend(){
		return getMarchEntity().getReportSend();
	}
}
