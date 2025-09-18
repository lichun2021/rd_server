package com.hawk.game.lianmengjunyan.worldmarch.submarch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;

import com.hawk.game.config.BuildingCfg;
import com.hawk.game.entity.TalentEntity;
import com.hawk.game.entity.TechnologyEntity;
import com.hawk.game.lianmengjunyan.ILMJYWorldPoint;
import com.hawk.game.lianmengjunyan.LMJYRoomManager;
import com.hawk.game.lianmengjunyan.player.ILMJYPlayer;
import com.hawk.game.lianmengjunyan.worldmarch.ILMJYWorldMarch;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Armour.ArmourBriefInfo;
import com.hawk.game.protocol.Armour.ArmourSuitType;
import com.hawk.game.protocol.Army.ArmySoldierPB;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.AttackMarchReportPB;
import com.hawk.game.protocol.World.GeneralPB;
import com.hawk.game.protocol.World.MarchReportPB;
import com.hawk.game.protocol.World.MarchTargetPointType;
import com.hawk.game.protocol.World.TaltentPB;
import com.hawk.game.protocol.World.WorldMarchRefreshPB;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.march.IWorldMarch;

/** @author lwt
 * @date 2017年10月6日 */
public interface ILMJYReportPushMarch extends IWorldMarch {

	/** 发送攻击报告 */
	default void pushAttackReport() {
		try {
			removeAttackReport();

			if (!isEvident()) {
				return;
			}

			ILMJYWorldMarch worldMarch = (ILMJYWorldMarch) this;
			Set<String> attackReportRecipients = attackReportRecipients();
			reportSend().addAll(attackReportRecipients);

			ILMJYWorldPoint point = worldMarch.getParent().getParent().getWorldPoint(this.getTerminalId()).orElse(null);
			Set<ILMJYWorldMarch> memberMarchs = Collections.emptySet();
			if (this instanceof ILMJYMassMarch) {
				ILMJYMassMarch massMarch = (ILMJYMassMarch) this;
				memberMarchs = massMarch.getMassJoinMarchs(true);
			}

			for (String playerId : attackReportRecipients) {
				doSendReport(playerId, memberMarchs, point);
			}
		} catch (Exception e) {
			e.printStackTrace();
			HawkException.catchException(e);
		}
	}

	/** 发送攻击报告给特定玩家 */
	default void pushAttackReport(String playerId) {
		if (!isEvident()) { // 不发送战报
			return;
		}
		Set<String> attackReportRecipients = attackReportRecipients();
		if (!attackReportRecipients.contains(playerId)) { // 未达到接受条件
			return;
		}
		reportSend().add(playerId);

		ILMJYWorldMarch worldMarch = (ILMJYWorldMarch) this;
		ILMJYWorldPoint point = worldMarch.getParent().getParent().getWorldPoint(this.getTerminalId()).orElse(null);
		Set<ILMJYWorldMarch> memberMarchs = Collections.emptySet();
		if (this instanceof ILMJYMassMarch) {
			ILMJYMassMarch massMarch = (ILMJYMassMarch) this;
			memberMarchs = massMarch.getMassJoinMarchs(true);
		}

		doSendReport(playerId, memberMarchs, point);

	}

	default void doSendReport(String playerId, Set<ILMJYWorldMarch> memberMarchs, ILMJYWorldPoint point) {
		ILMJYPlayer to = LMJYRoomManager.getInstance().makesurePlayer(playerId);
		// 在线即推送给客户端
		if (to == null) {
			return;
		}

		AttackMarchReportPB.Builder attReportBuilder = this.assembleEnemyMarchInfo(to, memberMarchs);
		MarchReportPB.Builder marchReport = MarchReportPB.newBuilder();
		marchReport.setAttackReport(attReportBuilder);
		marchReport.setTargetType(getMarchTargetPointType(to, point).getNumber());
		to.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MARCH_REPORT_PUSH, marchReport));
	}

	/** 可见报告
	 * 
	 * @return */
	default boolean isEvident() {
		return this.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE;
	}

	/** 攻击报告接受者列表
	 * 
	 * @return */
	Set<String> attackReportRecipients();

	/** 删除上一次发出的攻击报告 */
	default void removeAttackReport() {
		Set<String> attackReportRecipients = reportSend();
		for (String playerId : attackReportRecipients) {
			Player to = LMJYRoomManager.getInstance().makesurePlayer(playerId);
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
		Player to = LMJYRoomManager.getInstance().makesurePlayer(tarPlayerId);
		// 在线即推送给客户端
		if (to == null || !to.isActiveOnline()) {
			return;
		}
		WorldMarchRefreshPB.Builder builder = WorldMarchRefreshPB.newBuilder();
		builder.setMarchId(getMarchId());
		to.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MARCH_END_PUSH, builder));

	}

	/** 行军速度改变 */
	default void reachTimeChange() {
		Set<String> attackReportRecipients = reportSend();
		for (String playerId : attackReportRecipients) {
			Player to = LMJYRoomManager.getInstance().makesurePlayer(playerId);
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

	/** 获取行军目标点通知类型
	 * 
	 * @param march
	 * @return */
	default MarchTargetPointType getMarchTargetPointType(ILMJYPlayer player, ILMJYWorldPoint point) {
		if (point == null) {
			return MarchTargetPointType.QUARTERED_POINT;
		}

		switch (point.getPointType()) {
		case PLAYER: {
			String pointPid = ((ILMJYPlayer) point).getId();
			boolean isSelfCity = Objects.equals(player.getId(), pointPid);
			return isSelfCity ? MarchTargetPointType.PLAYER_CITY_POINT : MarchTargetPointType.ALLY_CITY;
		}
		default:
			return MarchTargetPointType.QUARTERED_POINT;
		}

	}

	/** 警报触发点
	 * 
	 * @return */
	default int[] alermPoint() {
		return GameUtil.splitXAndY(getMarchEntity().getAlarmPointId());
	}

	default List<ILMJYWorldMarch> alarmPointMarches() {
		return pointMarches(getMarchEntity().getAlarmPointId());
	}

	default List<ILMJYWorldMarch> pointMarches(int pointId) {
		ILMJYWorldMarch worldMarch = (ILMJYWorldMarch) this;
		Collection<ILMJYWorldMarch> terminalPtMarchs = worldMarch.getParent().getParent().getPointMarches(pointId);

		if (terminalPtMarchs == null || terminalPtMarchs.isEmpty()) {
			return Collections.emptyList();
		}

		Set<ILMJYWorldMarch> result = new HashSet<>();
		for (ILMJYWorldMarch targetMarch : terminalPtMarchs) {
			if (targetMarch == null || targetMarch.getMarchId().equals(this.getMarchId())) {
				continue;
			}
			if (targetMarch.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
				continue;
			}
			result.add(targetMarch);
		}
		return new ArrayList<>(result);
	}

	/** 警报点上的乱对行军 (不包括回程中行军)
	 * 
	 * @return */
	default List<ILMJYWorldMarch> alarmPointEnemyMarches() {
		ILMJYWorldMarch worldMarch = (ILMJYWorldMarch) this;
		ILMJYPlayer parent = worldMarch.getParent();
		List<ILMJYWorldMarch> result = new ArrayList<>();
		for (ILMJYWorldMarch march : alarmPointMarches()) {
			if (parent.isInSameGuild(march.getParent())) {
				continue;
			}
			result.add(march);
		}
		return result;
	}

	/** 收集进攻型行军报告信息 原雷达
	 * 
	 * @param to
	 *            被进攻者玩家
	 * @param leaderMarch
	 *            集结队伍中队长的行军信息
	 * @param memberMarchs
	 *            集结队伍中队员的行军信息
	 * @param marchReport
	 *            组装行军信息的PB实体 */
	default AttackMarchReportPB.Builder assembleEnemyMarchInfo(ILMJYPlayer to, Set<ILMJYWorldMarch> memberMarchs) {
		AttackMarchReportPB.Builder marchReport = AttackMarchReportPB.newBuilder();
		marchReport.setMarchUUID(this.getMarchId());
		WorldMarchType marchType = this.getMarchType();
		marchReport.setMarchType(marchType.getNumber());

		ILMJYWorldMarch worldMarch = (ILMJYWorldMarch) this;
		ILMJYPlayer leaderMarchplayer = worldMarch.getParent();
		if (leaderMarchplayer.hasGuild()) {
			marchReport.setSameGuild(Objects.equals(to.getGuildId(), leaderMarchplayer.getGuildId()));
		}

		BuildingCfg buildingCfg = to.getData().getBuildingCfgByType(BuildingType.RADAR);
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
			for (ILMJYWorldMarch march : memberMarchs) {
				ILMJYPlayer mplayer = march.getParent();
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

	/** @param player
	 * @param march
	 * @param radarLevel
	 * @param randomRange
	 * @param isleader
	 *            是否进攻发起者
	 * @return */
	default GeneralPB.Builder assembleGeneralPB(Player player, IWorldMarch march, int radarLevel) {
		GeneralPB.Builder general = GeneralPB.newBuilder().setName(player.getName()).setId(player.getId()).setIcon(player.getIcon())
				.setPfIcon(Optional.ofNullable(player.getPfIcon()).orElse(""));
		if (player.hasGuild()) {
			general.setGuildName(player.getGuildName()).setGuildId(player.getGuildId())
					.setGuildTag(player.getGuildTag());
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
			List<TalentEntity> talentEntities = player.getData().getTalentEntities();
			for (TalentEntity talentEntity : talentEntities) {
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
		ArmourBriefInfo armour = player.genArmourBriefInfo(ArmourSuitType.valueOf(march.getMarchEntity().getArmourSuit()));
		general.setArmourBrief(armour);
		general.setArmourSuit(ArmourSuitType.valueOf(march.getMarchEntity().getArmourSuit()));
		return general;
	}

	/**接收到警报的玩家*/
	default Set<String> reportSend() {
		return getMarchEntity().getReportSend();
	}
}
