package com.hawk.game.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import com.hawk.game.protocol.World.PresetMarchManhattan;
import org.apache.commons.lang.StringUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.game.battle.Battle;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.BattleUnity;
import com.hawk.game.battle.battleIncome.IBattleIncome;
import com.hawk.game.battle.guarder.GuarderPlayer;
import com.hawk.game.city.CityManager;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.config.HeroOfficeCfg;
import com.hawk.game.config.TechnologyCfg;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.config.WorldMarchConstProperty;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.entity.TalentEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.lianmengstarwars.player.ISWPlayer;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.staffofficer.StaffOfficerSkill;
import com.hawk.game.module.staffofficer.StaffOfficerType;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Armour.ArmourBriefInfo;
import com.hawk.game.protocol.Armour.ArmourSuitType;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.FightResult;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.protocol.Mail.CollectMail;
import com.hawk.game.protocol.Mail.CollectResource;
import com.hawk.game.protocol.Mail.CollectType;
import com.hawk.game.protocol.Mail.CureMail;
import com.hawk.game.protocol.Mail.DefenceBuilding;
import com.hawk.game.protocol.Mail.DetectMail;
import com.hawk.game.protocol.Mail.FightMail;
import com.hawk.game.protocol.Mail.FighteInfo;
import com.hawk.game.protocol.Mail.MailArmyInfo;
import com.hawk.game.protocol.Mail.MailDefBuildingPB;
import com.hawk.game.protocol.Mail.MailEffVal;
import com.hawk.game.protocol.Mail.MailPlayerInfo;
import com.hawk.game.protocol.Mail.MailSoldierPB;
import com.hawk.game.protocol.Mail.MonsterMail;
import com.hawk.game.protocol.Mail.PBDetectData;
import com.hawk.game.protocol.Mail.ResAssistanceMail;
import com.hawk.game.protocol.Mail.ResourceAssistance;
import com.hawk.game.protocol.Mail.SoilderAssistanceMail;
import com.hawk.game.protocol.Mail.SpyTaltentPB;
import com.hawk.game.protocol.MechaCore.MechaCoreSuitType;
import com.hawk.game.protocol.Player.EffectPB;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldPlayerService;

/**
 * 邮件体构建
 * 
 * @author Nannan.Gao
 * @date 2016-12-10 15:14:54
 */
public class MailBuilderUtil {

	/**
	 * 采集报告邮件体构建
	 * 
	 * @param march
	 * @param mailId
	 * @param isSuccess
	 *            是否采集成功
	 * @return
	 */
	public static CollectMail.Builder createCollectMail(WorldMarch march, int mailId, boolean isSuccess) {
		CollectMail.Builder builder = CollectMail.newBuilder();

		builder.setMailId(mailId);
		int[] pos = GameUtil.splitXAndY(march.getOrigionId());
		builder.setX(pos[0]);
		builder.setY(pos[1]);
		builder.setCollectTime((int) (march.getResEndTime() - march.getResStartTime()));
		if (march.getMarchType() == WorldMarchType.MANOR_COLLECT_VALUE) {
			builder.setType(CollectType.SUPER_RES);
		} else if (march.getMarchType() == WorldMarchType.YURI_EXPLORE_VALUE) {
			builder.setType(CollectType.YURI_RES);
		} else if (march.getMarchType() == WorldMarchType.STRONGPOINT_VALUE) {
			builder.setBeginTime(march.getResStartTime());
			builder.setEndTime(march.getResEndTime());
			builder.setCfgId(Integer.parseInt(march.getTargetId()));
			builder.setType(CollectType.STRONGPOINT);
		}  else if (march.getMarchType() == WorldMarchType.TREASURE_HUNT_RESOURCE_VALUE) {
			builder.setBeginTime(march.getResStartTime());
			builder.setEndTime(march.getResEndTime());
			builder.setCfgId(Integer.parseInt(march.getTargetId()));
			builder.setType(CollectType.TH_RES);
		} else if (march.getMarchType() == WorldMarchType.PYLON_MARCH_VALUE) {
			builder.setBeginTime(march.getResStartTime());
			builder.setEndTime(march.getResEndTime());
			builder.setCfgId(Integer.parseInt(march.getTargetId()));
			builder.setType(CollectType.PYLON);
		} else {
			builder.setType(CollectType.NORMAL_RES);
		}
		// 采集成功
		if (isSuccess) {
			AwardItems award = march.getAwardItems();
			if (award != null) {
				for (ItemInfo item : award.getAwardItems()) {
					CollectResource.Builder cBuilder = CollectResource.newBuilder();
					cBuilder.setResCount((int) item.getCount());
					cBuilder.setResId(item.getItemId());
					cBuilder.setResType(item.getType());
					//如果是资源点, 传入资源点Id
					if(march.getMarchType() == WorldMarchType.COLLECT_RESOURCE_VALUE){
						cBuilder.setWorldResId(Integer.parseInt(march.getTargetId()));
					}
					builder.addRes(cBuilder.build());
				}
			}
			
			if (!HawkOSOperator.isEmptyString(march.getAwardExtraStr())) {
				AwardItems extraAward = AwardItems.valueOf(march.getAwardExtraStr());
				for (ItemInfo item : extraAward.getAwardItems()) {
					CollectResource.Builder cBuilder = CollectResource.newBuilder();
					cBuilder.setResCount((int) item.getCount());
					cBuilder.setResId(item.getItemId());
					cBuilder.setResType(item.getType());
					builder.addRes(cBuilder.build());
				}
			}
		}
		
		return builder;
	}

	/**
	 * 治伤兵邮件体构建
	 * 
	 * @param hospitalCap
	 *            医院空位人口
	 * @param deadPopulation
	 *            死亡士兵人口
	 * @return
	 */
	public static CureMail.Builder createCureMail(int hospitalCap, int deadPopulation,List<ArmyInfo> wound2DeadList) {
		CureMail.Builder builder = CureMail.newBuilder();

		builder.setHospitalCap(hospitalCap);
		builder.setTotalHurt(hospitalCap + deadPopulation);
		builder.setDead(deadPopulation);
		for(ArmyInfo ainfo : wound2DeadList){
			builder.addDetail(buildMailSoldierPB(ainfo));
		}

		return builder;
	}

	/**
	 * 资源援助邮件体构建
	 * 
	 * @param march
	 * @param player
	 * @param taxRate
	 *            援助税率
	 * @return
	 */
	public static ResAssistanceMail.Builder createResAssistanceMail(WorldMarch march, Player player, int taxRate) {
		ResAssistanceMail.Builder builder = ResAssistanceMail.newBuilder();
		builder.setAtime(HawkTime.getMillisecond());
		builder.setTradeTaxRate(taxRate);

		if (march == null || player == null) {
			return builder;
		}

		builder.setGuildTag(player.getGuildTag());
		builder.setPlayerIcon(player.getIcon());
		builder.setName(player.getName());
		builder.setId(player.getId());
		if (!HawkOSOperator.isEmptyString(player.getPfIcon())) {
			builder.setPfIcon(player.getPfIcon());
		}

		// 坐标
		int[] point = WorldPlayerService.getInstance().getPlayerPosXY(player.getId());
		builder.setX(point[0]);
		builder.setY(point[1]);

		// 援助的资源信息
		AwardItems award = AwardItems.valueOf(march.getAssistantStr());
		if (award != null) {
			for (ItemInfo itemInfo : award.getAwardItems()) {
				ResourceAssistance.Builder resource = ResourceAssistance.newBuilder();
				resource.setItemId(itemInfo.getItemId());
				resource.setType(itemInfo.getType());
				resource.setNum((int) itemInfo.getCount());
				builder.addResource(resource);
			}
		}

		return builder;
	}

	/**
	 * 士兵援助邮件体构建
	 * 
	 * @param march
	 * @param player
	 * @return
	 */
	public static SoilderAssistanceMail.Builder createSoilderAssistanceMail(WorldMarch march, List<PlayerHero> heroList , Player player) {
		SoilderAssistanceMail.Builder builder = SoilderAssistanceMail.newBuilder();
		builder.setAtime(HawkTime.getMillisecond());
		if (Objects.nonNull(heroList)) {
			for(PlayerHero hero: heroList){
				builder.addHero(hero.toPBobj());
			}
		}
		// 援助的军队信息
		if (march != null && march.getArmys() != null) {
			Player marchPlayer = GlobalData.getInstance().makesurePlayer(march.getPlayerId());
			for (ArmyInfo armyInfo : march.getArmys()) {
				builder.addSoldier(armyInfo.toArmySoldierPB(marchPlayer));
			}
			if (Objects.nonNull(marchPlayer)) {
				SuperSoldier ssoldier = marchPlayer.getSuperSoldierByCfgId(march.getSuperSoldierId()).orElse(null);
				if (Objects.nonNull(ssoldier)) {
					builder.setSsoldier(ssoldier.toPBobj());
				}
			}
			ArmourBriefInfo armour = marchPlayer.genArmourBriefInfo( ArmourSuitType.valueOf(march.getArmourSuit()));
			builder.setArmourBrief(armour);
		}

		if (player != null) {
			builder.setPlayerIcon(player.getIcon());
			builder.setGuildTag(player.getGuildTag());
			if (!HawkOSOperator.isEmptyString(player.getPfIcon())) {
				builder.setPfIcon(player.getPfIcon());
			}
			builder.setName(player.getName());
			builder.setId(player.getId());
			// 获取玩家坐标
			int[] point = WorldPlayerService.getInstance().getPlayerPosXY(player.getId());
			builder.setX(point[0]);
			builder.setY(point[1]);
		}

		return builder;
	}

	/**
	 * 侦查报告内容 原科技相关
	 * 
	 * @param radarLevel
	 * @param targetPlayer
	 *            行军侦查的目标玩家(非必须参数)
	 * @param point
	 * @param guildId
	 *            联盟ID(非必须参数)
	 * @param isMirror
	 *            对方是否使用了部队镜像
	 * @return
	 */
	public static DetectMail.Builder createDetectMail(final int radarLevel,Player player, Player targetPlayer, WorldPoint point, String guildId, boolean isMirror) {
		PBDetectData.Builder builder = PBDetectData.newBuilder();
		MailPlayerInfo.Builder playerInfo = MailPlayerInfo.newBuilder();

		builder.setLevel(radarLevel);

		// 侦查点为空
		if (point == null) {
			return DetectMail.newBuilder();
		}

		// 侦查类型过滤
		int pointType = point.getPointType();
		if (pointType == WorldPointType.MONSTER_VALUE || pointType == WorldPointType.OCCUPIED_VALUE || pointType == WorldPointType.EMPTY_VALUE) {
			return DetectMail.newBuilder();
		}

		IWorldMarch leaderMarch = null;
		List<IWorldMarch> marches = null;

		// 数据不合法
		if (pointType == WorldPointType.PLAYER_VALUE || pointType == WorldPointType.RESOURCE_VALUE || pointType == WorldPointType.QUARTERED_VALUE) {
			if (targetPlayer == null) {
				return DetectMail.newBuilder();
			} else {
				// 玩家数据构建
				playerInfo.setPlayerId(targetPlayer.getId());
				playerInfo.setName(GameUtil.getPlayerNameWithGuildTag(targetPlayer.getGuildId(), targetPlayer.getName()));
				playerInfo.setIcon(targetPlayer.getIcon());
				playerInfo.setX(point.getX());
				playerInfo.setY(point.getY());
				playerInfo.setPower(targetPlayer.getPower());
				playerInfo.setPfIcon(targetPlayer.getPfIcon());
				builder.setPlayer(playerInfo);
			}
		}

		// 数据采集
		if (pointType == WorldPointType.KING_PALACE_VALUE) {
			// 首都
			marches = WorldMarchService.getInstance().getPresidentQuarteredMarchs();
			if (marches != null && marches.size() > 0) {
				leaderMarch = marches.remove(0);
				// 玩家数据构建
				Player leaderSnapshot = GlobalData.getInstance().makesurePlayer(leaderMarch.getPlayerId());
				playerInfo.setPlayerId(leaderMarch.getPlayerId());
				playerInfo.setName(leaderMarch.getMarchEntity().getPlayerName());
				playerInfo.setGuildTag(leaderSnapshot.getGuildTag());
				playerInfo.setIcon(leaderSnapshot.getIcon());
				playerInfo.setPfIcon(leaderSnapshot.getPfIcon());
				playerInfo.setX(WorldMapConstProperty.getInstance().getWorldCenterX());
				playerInfo.setY(WorldMapConstProperty.getInstance().getWorldCenterY());
				builder.setPlayer(playerInfo);
			}
		} else if (pointType == WorldPointType.RESOURCE_VALUE || pointType == WorldPointType.QUARTERED_VALUE) {
			// 资源点 驻扎点
			leaderMarch = WorldMarchService.getInstance().getPlayerMarch(targetPlayer.getId(), point.getMarchId());
		} else if (pointType == WorldPointType.PLAYER_VALUE) {
			// 玩家城堡---基地
			List<IWorldMarch> massMarches = WorldMarchService.getInstance().getPlayerMassMarch(targetPlayer.getId());
			if(!massMarches.isEmpty()){
				leaderMarch = massMarches.get(0);
			}
			Set<IWorldMarch> marchs = WorldMarchService.getInstance().getPlayerPassiveMarchs(targetPlayer.getId(), WorldMarchType.ASSISTANCE_VALUE, WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST_VALUE);
			if (marchs != null  && marchs.size() > 0) {
				marches = new ArrayList<IWorldMarch>(marchs);
			}
		}

		if (pointType == WorldPointType.PLAYER_VALUE && !targetPlayer.getData().getArmyEntities().isEmpty()) {
			if (radarLevel >= GsConst.RadarLevel.LV18) {
				// 防守部队和所有部队的准确数量
				MailArmyInfo.Builder defenceArmy = getDefenceArmy(radarLevel, targetPlayer, false, isMirror);
				builder.setDefenceArmy(defenceArmy);
			} else if (radarLevel >= GsConst.RadarLevel.LV10 && radarLevel < GsConst.RadarLevel.LV18) {
				// 防守部队的的部队组成和各部队大致数量
				MailArmyInfo.Builder defenceArmy = getDefenceArmy(radarLevel, targetPlayer, true, isMirror);
				builder.setDefenceArmy(defenceArmy);
			} else if (radarLevel >= GsConst.RadarLevel.LV4 && radarLevel < GsConst.RadarLevel.LV10) {
				// 防守部队总数的大致数量
				int soldierTotal = targetPlayer.marchArmy().stream().mapToInt(e -> e.getFree()).sum();
				soldierTotal = GameUtil.getProbablyNum(soldierTotal, GsConst.RADA_RANDOM_RANGE);
				soldierTotal = isMirror ? soldierTotal * 2 : soldierTotal;
				builder.setDefenceArmyAboutNum(soldierTotal);
			}
			
			// 集结行军
			if(radarLevel >= GsConst.RadarLevel.LV21 && leaderMarch!=null){
				Set<IWorldMarch> massMarches = WorldMarchService.getInstance().getMassJoinMarchs(leaderMarch, false);
				
				List<IWorldMarch> unReachList = massMarches.stream()
						.filter(march -> march.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE)
						.collect(Collectors.toList());
				List<IWorldMarch> reachList = massMarches.stream()
						.filter(march -> march.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE)
						.collect(Collectors.toList());
				reachList.add(leaderMarch);
				
				ToIntFunction<List<IWorldMarch>> armyCountFunc = list -> list.stream() 
						.flatMap(march -> march.getMarchEntity().getArmys().stream())
						.mapToInt(ArmyInfo::getFreeCnt)
						.sum();
				
				int unreach = armyCountFunc.applyAsInt(unReachList);
				int reach = armyCountFunc.applyAsInt(reachList);
				builder.setMassNum(unreach);
				if(radarLevel >= GsConst.RadarLevel.LV23){ //集结行军详情
					builder.addAllMassArmy(createHelpArmy(GsConst.RadarLevel.LV30, isMirror, unReachList));
				}
				if(radarLevel >= GsConst.RadarLevel.LV25){
					builder.setMassNumReach(reach);
				}
				if(radarLevel >= GsConst.RadarLevel.LV27){
					builder.addAllMassArmyReach(createHelpArmy(GsConst.RadarLevel.LV30, isMirror, reachList));
				}
			}
			
		} else if (leaderMarch !=null) {
			if (radarLevel >= GsConst.RadarLevel.LV18) {
				// 防守部队和所有部队的准确数量
				MailArmyInfo.Builder defenceArmy = getDefenceArmy(radarLevel, leaderMarch, false);
				builder.setDefenceArmy(defenceArmy);
			} else if (radarLevel >= GsConst.RadarLevel.LV10 && radarLevel < GsConst.RadarLevel.LV18) {
				// 防守部队的的部队组成和各部队大致数量
				MailArmyInfo.Builder defenceArmy = getDefenceArmy(radarLevel, leaderMarch, true);
				builder.setDefenceArmy(defenceArmy);
			} else if (radarLevel >= GsConst.RadarLevel.LV4 && radarLevel < GsConst.RadarLevel.LV10) {
				// 防守部队总数的大致数量
				int soldierTotal = leaderMarch.getMarchEntity().getArmys().stream().mapToInt(e -> e.getFreeCnt()).sum();
				soldierTotal = GameUtil.getProbablyNum(soldierTotal, GsConst.RADA_RANDOM_RANGE);
				soldierTotal = isMirror ? soldierTotal * 2 : soldierTotal;
				builder.setDefenceArmyAboutNum(soldierTotal);
			}
		}

		// 援军部队构建
		if (marches != null && marches.size() > 0) {
			if (pointType == WorldPointType.PLAYER_VALUE || pointType == WorldPointType.KING_PALACE_VALUE) {
				if (radarLevel >= GsConst.RadarLevel.LV14) {
					// 可以侦查到每支援军部队组成和各自的准确数量
					builder.addAllHelpArmy(createHelpArmy(radarLevel, isMirror, marches));
				}
				// 可以侦查援军部队总数的大致数量
				if (radarLevel >= GsConst.RadarLevel.LV8) {
					int assistTotal = 0;
					for (IWorldMarch march : marches) {
						for (ArmyInfo army : march.getMarchEntity().getArmys()) {
							assistTotal += army.getFreeCnt();
						}
					}
					int assistAbout = GameUtil.getProbablyNum(assistTotal, GsConst.RADA_RANDOM_RANGE);
					assistAbout = isMirror ? assistAbout * 2 : assistAbout;
					builder.setHelpArmyAboutNum(assistAbout);
				}
			}
		}

		// 可以侦查防御武器---玩家城堡
		if (pointType == WorldPointType.PLAYER_VALUE) {
			// 取得所有的城防设施 (广陵塔, SoldierType 士兵)
			List<BuildingBaseEntity> towers = targetPlayer.getData().getBuildingListByType(BuildingType.PRISM_TOWER);
			List<BuildingBaseEntity> walls = targetPlayer.getData().getBuildingListByType(BuildingType.CITY_WALL);

			List<ArmyEntity> defSoldiers = targetPlayer.defArmy();
			int all = defSoldiers.stream().mapToInt(ArmyEntity::getFree).sum();
			// 对方城防设施大至数量   (城防兵的大至数) sb策划文档到处让人猜,文档和UI也对不上.
			if (radarLevel >= GsConst.RadarLevel.LV6) {
				builder.setDefenceNum(GameUtil.getProbablyNum(all, GsConst.RADA_RANDOM_RANGE));

				Consumer<? super BuildingBaseEntity> action = buld -> {
					DefenceBuilding.Builder debuild = DefenceBuilding.newBuilder().setId(buld.getBuildingCfgId()).setNum(1);
					if(buld.getType() == BuildingType.PRISM_TOWER_VALUE){
						debuild.setType(1);
					}else if(buld.getType() == BuildingType.CITY_WALL_VALUE){
						debuild.setType(3);
						if(targetPlayer.getPlayerBaseEntity().getOnFireEndTime() > HawkTime.getMillisecond()){
							debuild.setFireSpeed((int) CityManager.getInstance().getWallFireSpeed(targetPlayer));
						}
						int cityDefVal = CityManager.getInstance().getRealCityDef(targetPlayer);
//						if (radarLevel >= GsConst.RadarLevel.LV24) {
							debuild.setCityDefVal(cityDefVal);
//						}else{
//							debuild.setCityDefVal(GameUtil.getProbablyNum(cityDefVal, GsConst.RADA_RANDOM_RANGE));
//						}
					}
					if (radarLevel >= GsConst.RadarLevel.LV24) {
						BuildingCfg bcfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buld.getBuildingCfgId());
						debuild.setLevel(bcfg.getLevel());
					}
					
					builder.addDefenceBuildings(debuild);
				};
				towers.forEach(action);
				walls.forEach(action);
			}
			if (radarLevel >= GsConst.RadarLevel.LV20) {
				builder.setDefenceNum(all);
			}

			// 对方防御武器的组成和大至数量
			if (radarLevel >= GsConst.RadarLevel.LV12) {

				defSoldiers.stream().forEach(sod -> {
					DefenceBuilding.Builder debuild = DefenceBuilding.newBuilder().setId(sod.getArmyId()).setType(2);
					debuild.setHonor(targetPlayer.getSoldierStar(sod.getArmyId()));
					if (radarLevel >= GsConst.RadarLevel.LV20) {
						debuild.setNum(sod.getFree());
					}else{
						debuild.setNum(GameUtil.getProbablyNum(sod.getFree(), GsConst.RADA_RANDOM_RANGE));
					}
					builder.addDefenceBuildings(debuild);
				});
			}

			// 天赋
			if (radarLevel >= GsConst.RadarLevel.LV30) {
				int talentType = targetPlayer.getData().getPlayerEntity().getTalentType();
				List<TalentEntity> talentEntities = targetPlayer.getData().getTalentEntities();
				for (TalentEntity talentEntity : talentEntities) {
					if (talentType != talentEntity.getType()) {
						continue;
					}
					if (talentEntity.getLevel() > 0) {
						builder.addTalent(SpyTaltentPB.newBuilder().setLevel(talentEntity.getLevel()).setTalentId(talentEntity.getTalentId()));
					}
				}
			}

			// 科技
			if (radarLevel >= GsConst.RadarLevel.LV26) {
				List<Integer> allTech = targetPlayer.getData().getTechnologyEntities().stream()
						.filter(e -> e.getLevel() > 0)
						.map(e ->  HawkConfigManager.getInstance().getConfigByKey(TechnologyCfg.class, e.getCfgId()))
						.filter(Objects::nonNull)
						.map(TechnologyCfg::getId).collect(Collectors.toList());
				builder.addAllTech(allTech);
			}
			
			// 藏兵洞
			List<IWorldMarch> hiddenMarchs = WorldMarchService.getInstance().getPlayerMarch(targetPlayer.getId(), WorldMarchType.HIDDEN_MARCH_VALUE);
			if (radarLevel >= GsConst.RadarLevel.LV10) {
				for (IWorldMarch ma : hiddenMarchs) {
					MailArmyInfo.Builder defenceArmy = MailBuilderUtil.getDefenceArmy(radarLevel, ma, radarLevel < GsConst.RadarLevel.LV18);
					builder.addHiddenArmy(defenceArmy);
				}
			}
			if (radarLevel >= GsConst.RadarLevel.LV4 && radarLevel < GsConst.RadarLevel.LV10) {
				int hiddenTotal = hiddenMarchs.stream()
						.flatMap(m -> m.getArmys().stream())
						.mapToInt(a -> a.getFreeCnt())
						.sum();
				builder.setHiddenArmyAboutNum( GameUtil.getProbablyNum(hiddenTotal, GsConst.RADA_RANDOM_RANGE));
			}

		}

		// 可以侦查到资源的拥有数量
		if (radarLevel >= GsConst.RadarLevel.LV2) {
			final int[] RES_LV = WorldMapConstProperty.getInstance().getResLv();
			// 侦查点是玩家城堡
			if (pointType == WorldPointType.PLAYER_VALUE) {
				builder.setMyCityLevel(player.getCityLv());
				long[] hasResAry = targetPlayer.getPlunderResAry(GsConst.RES_TYPE);
//				Map<Integer, Long> resStore = targetPlayer.getPlunderResStore();
				for (int i = 0; i < GsConst.RES_TYPE.length; i++) {
					if (targetPlayer.getCityLv() >= RES_LV[i] && player.getCityLv() >= RES_LV[i]) {
						int resType = GsConst.RES_TYPE[i];
						builder.addCanPlunderItem(ItemInfo.toRewardItem(ItemType.PLAYER_ATTR_VALUE, resType, hasResAry[i]));
//						builder.addCanPlunderItemStore(ItemInfo.toRewardItem(ItemType.PLAYER_ATTR_VALUE, resType, resStore.getOrDefault(resType, 0L).intValue()));
					}
				}
			}
		}
		if(leaderMarch!=null){
			builder.setLeaderMarchId(leaderMarch.getMarchId());
		}
		return DetectMail.newBuilder().setDetectData(builder);
	}

	/**
	 * 获取援军信息
	 * 
	 * @param isArmy
	 *            true获取援军兵种信息，false只获取援军领主的基本信息
	 * @param isAbout
	 *            false获取每一兵种的准确信息，true获取每一兵种的大致信息
	 * @param isMirror
	 * @return
	 */
	public static List<MailArmyInfo> createHelpArmy(int radarLevel, boolean isMirror, Collection<IWorldMarch> marchs) {

		List<MailArmyInfo> helpArmys = new ArrayList<MailArmyInfo>();
		if (marchs == null) {
			return helpArmys;
		}

		double randomRange = (double) WorldMarchConstProperty.getInstance().getScoutSoldierRandomRange() / 1000.0f;
		String[] playerIds = new String[marchs.size()];
		int index = 0;
		for (IWorldMarch march : marchs) {
			playerIds[index] = march.getPlayerId();
			index++;
		}
		Map<String, Player> snapshotMap = GlobalData.getInstance().getPlayerMap(playerIds);

		boolean isAbout = radarLevel < GsConst.RadarLevel.LV22;
		for (IWorldMarch march : marchs) {
			// 获取玩家快照信息
			Player playerInfo = snapshotMap.get(march.getPlayerId());
			if (null == playerInfo) {
				continue;
			}

			// 部队数据
			MailArmyInfo.Builder builder = MailArmyInfo.newBuilder();
			builder.setPlantStep(playerInfo.getCityPlantLv());
			builder.addAllSuperWeponEff(march.getMarchEntity().getEffectList());
			if (radarLevel >= GsConst.RadarLevel.LV10) {
				for (PlayerHero hero : march.getHeros()) {
					builder.addHero(hero.toPBobj());
				}
				SuperSoldier ssoldier = playerInfo.getSuperSoldierByCfgId(march.getSuperSoldierId()).orElse(null);
				if(Objects.nonNull(ssoldier)){
					builder.setSsoldier(ssoldier.toPBobj());
				}
				ArmourBriefInfo armour = playerInfo.genArmourBriefInfo(ArmourSuitType.valueOf(march.getMarchEntity().getArmourSuit()));
				builder.setArmourBrief(armour);
				addMailEffVal(builder, playerInfo);
				//超武已解锁
				if(playerInfo.checkManhattanFuncUnlock()) {
					PresetMarchManhattan.Builder  marchManhattan = PresetMarchManhattan.newBuilder();
					marchManhattan.setManhattanAtkSwId(march.getMarchEntity().getManhattanAtkSwId());
					marchManhattan.setManhattanDefSwId(march.getMarchEntity().getManhattanDefSwId());
					builder.setManhattan(playerInfo.buildManhattanInfo(marchManhattan.build()));
				}
				//机甲核心功能已解锁
				if(playerInfo.checkMechacoreFuncUnlock()) {
					MechaCoreSuitType suit = MechaCoreSuitType.valueOf(march.getMarchEntity().getMechacoreSuit());
					builder.setMechacore(playerInfo.buildMechacoreInfo(suit));
				}
			}
			int assistTotal = 0;
			for (ArmyInfo army : march.getMarchEntity().getArmys()) {
				assistTotal += army.getFreeCnt();
				if (radarLevel >= GsConst.RadarLevel.LV16) {
					MailSoldierPB.Builder soldierBuilder = MailSoldierPB.newBuilder();
					soldierBuilder.setSoldierId(army.getArmyId());
					soldierBuilder.setStar(playerInfo.getSoldierStar(army.getArmyId()));
					soldierBuilder.setPlantStep(playerInfo.getSoldierStep(army.getArmyId()));
					soldierBuilder.setPlantSkillLevel(playerInfo.getSoldierPlantSkillLevel(army.getArmyId()));
					soldierBuilder.setPlantMilitaryLevel(playerInfo.getSoldierPlantMilitaryLevel(army.getArmyId()));
					soldierBuilder.setWoundedCnt(army.getWoundedCount());
					int count = 0;
					if (isAbout) {
						count = GameUtil.getProbablyNum(army.getFreeCnt(), randomRange);
					} else {
						count = army.getFreeCnt();
					}
					count = isMirror ? count * 2 : count;
					soldierBuilder.setDefencedCount(count);

					builder.addSoldier(soldierBuilder);
				}
			}
			int totalNum = isAbout ? GameUtil.getProbablyNum(assistTotal, randomRange) : assistTotal;
			builder.setTotalNum(isMirror ? totalNum * 2 : totalNum);

			builder.setIsAboutValue(isAbout);
			builder.setPlayerName(march.getMarchEntity().getPlayerName());
			builder.setLevel(playerInfo.getLevel());
			builder.setIcon(playerInfo.getIcon());
			if (StringUtils.isNotEmpty(playerInfo.getGuildName())) {
				builder.setGuildName(playerInfo.getGuildName());
			}
			if (StringUtils.isNotEmpty(playerInfo.getGuildTag())) {
				builder.setGuildTag(playerInfo.getGuildTag());
			}
			helpArmys.add(builder.build());
		}

		return helpArmys;
	}

	/**
	 * 获取驻扎点或资源点的防守部队
	 * 
	 * @param armyList
	 * @param isAbout
	 * @return
	 */
	public static MailArmyInfo.Builder getDefenceArmy(int radarLevel, IWorldMarch leaderMarch, boolean isAbout) {
		Player leader = leaderMarch.getPlayer();
		MailArmyInfo.Builder builder = MailArmyInfo.newBuilder();
		builder.setPlantStep(leader.getCityPlantLv());
		builder.addAllSuperWeponEff(leaderMarch.getMarchEntity().getEffectList());
		if (radarLevel >= GsConst.RadarLevel.LV10) {
			for (PlayerHero hero : leaderMarch.getHeros()) {
				builder.addHero(hero.toPBobj());
			}
			SuperSoldier ssoldier = leader.getSuperSoldierByCfgId(leaderMarch.getSuperSoldierId()).orElse(null);
			if(Objects.nonNull(ssoldier)){
				builder.setSsoldier(ssoldier.toPBobj());
			}
			ArmourBriefInfo armour = leader.genArmourBriefInfo( ArmourSuitType.valueOf(leaderMarch.getMarchEntity().getArmourSuit()));
			builder.setArmourBrief(armour);
			addMailEffVal(builder, leader);
			//超武已解锁
			if(leader.checkManhattanFuncUnlock()) {
				PresetMarchManhattan.Builder  marchManhattan = PresetMarchManhattan.newBuilder();
				marchManhattan.setManhattanAtkSwId(leaderMarch.getMarchEntity().getManhattanAtkSwId());
				marchManhattan.setManhattanDefSwId(leaderMarch.getMarchEntity().getManhattanDefSwId());
				builder.setManhattan(leader.buildManhattanInfo(marchManhattan.build()));
			}
			//机甲核心功能已解锁
			if(leader.checkMechacoreFuncUnlock()) {
				MechaCoreSuitType suit = MechaCoreSuitType.valueOf(leaderMarch.getMarchEntity().getMechacoreSuit());
				builder.setMechacore(leader.buildMechacoreInfo(suit));
			}
		}
		// 防守部队
		List<ArmyInfo> armyList = leaderMarch.getArmys();

		if (armyList == null || armyList.size() == 0) {
			return builder;
		}

		double randomRange = (double) WorldMarchConstProperty.getInstance().getScoutSoldierRandomRange() / 1000.0f;
		for (ArmyInfo army : armyList) {
			if (army.getFreeCnt() <= 0) {
				continue;
			}
			MailSoldierPB.Builder soldierBuilder = MailSoldierPB.newBuilder();
			soldierBuilder.setSoldierId(army.getArmyId());
			soldierBuilder.setStar(leader.getSoldierStar(army.getArmyId()));
			soldierBuilder.setPlantStep(leader.getSoldierStep(army.getArmyId()));
			soldierBuilder.setPlantSkillLevel(leader.getSoldierPlantSkillLevel(army.getArmyId()));
			soldierBuilder.setPlantMilitaryLevel(leader.getSoldierPlantMilitaryLevel(army.getArmyId()));
			if (isAbout) {
				soldierBuilder.setDefencedCount(GameUtil.getProbablyNum(army.getFreeCnt(), randomRange));
			} else {
				soldierBuilder.setDefencedCount(army.getFreeCnt());
			}

			builder.addSoldier(soldierBuilder);
		}

		builder.setIsAboutValue(isAbout);
		builder.setPlayerName(leader.getName());
		builder.setPlayerId(leader.getId());
		if (StringUtils.isNotEmpty(leader.getGuildName())) {
			builder.setGuildName(leader.getGuildName());
		}
		if (StringUtils.isNotEmpty(leader.getGuildTag())) {
			builder.setGuildTag(leader.getGuildTag());
		}

		return builder;
	}

	/**
	 * 获取防御兵力信息
	 * 
	 * @param armyList
	 * @param isAbout
	 *            false获取具体数量，true获取大致数量
	 * @param isMirror
	 * @return
	 */
	public static MailArmyInfo.Builder getDefenceArmy(int radarLevel, Player targetPlayer, boolean isAbout, boolean isMirror) {
		List<ArmyEntity> armyList = targetPlayer.marchArmy();
		MailArmyInfo.Builder builder = MailArmyInfo.newBuilder();
		builder.setPlantStep(targetPlayer.getCityPlantLv());
		builder.setIsAboutValue(isAbout);
		
		if (radarLevel >= GsConst.RadarLevel.LV10) {
			List<PlayerHero> defHeros = BattleService.getInstance().defCityHeros(targetPlayer);
			for (PlayerHero hero : defHeros) {
				builder.addHero(hero.toPBobj());
			}
			Optional<SuperSoldier> ssoldier = BattleService.getInstance().defCitySuperSoldier(targetPlayer);
			if(ssoldier.isPresent()){
				builder.setSsoldier(ssoldier.get().toPBobj());
			}
			ArmourBriefInfo armour = targetPlayer.genArmourBriefInfo(ArmourSuitType.valueOf(targetPlayer.getEntity().getArmourSuit()));
			builder.setArmourBrief(armour);
			addMailEffVal(builder, targetPlayer);
			//超武已解锁
			if(targetPlayer.checkManhattanFuncUnlock()) {
				builder.setManhattan(targetPlayer.buildManhattanInfo());
			}
			//机甲核心功能已解锁
			if(targetPlayer.checkMechacoreFuncUnlock()) {
				builder.setMechacore(targetPlayer.buildMechacoreInfo(null));
			}
		}

		// 部队数据详情
		for (ArmyEntity army : armyList) {
			if (army.getFree() > 0) {
				MailSoldierPB.Builder soldierBuilder = MailSoldierPB.newBuilder();
				soldierBuilder.setSoldierId(army.getArmyId());
				soldierBuilder.setStar(targetPlayer.getSoldierStar(army.getArmyId()));
				soldierBuilder.setPlantStep(targetPlayer.getSoldierStep(army.getArmyId()));
				soldierBuilder.setPlantSkillLevel(targetPlayer.getSoldierPlantSkillLevel(army.getArmyId()));
				soldierBuilder.setPlantMilitaryLevel(targetPlayer.getSoldierPlantMilitaryLevel(army.getArmyId()));
				int defencedCount = army.getFree();
				if (isAbout) {
					double randomRange = (double) WorldMarchConstProperty.getInstance().getScoutSoldierRandomRange() / 1000.0f;
					defencedCount = GameUtil.getProbablyNum(defencedCount, randomRange);
				}
				defencedCount = isMirror ? defencedCount * 2 : defencedCount;
				soldierBuilder.setDefencedCount(defencedCount);

				builder.addSoldier(soldierBuilder);
			}
		}
		builder.setPlayerName(targetPlayer.getName());
		builder.setPlayerId(targetPlayer.getId());
		if (StringUtils.isNotEmpty(targetPlayer.getGuildName())) {
			builder.setGuildName(targetPlayer.getGuildName());
		}
		if (StringUtils.isNotEmpty(targetPlayer.getGuildTag())) {
			builder.setGuildTag(targetPlayer.getGuildTag());
		}

		return builder;
	}

	/**
	 * 战斗邮件
	 * @param point 
	 * @param battleIncome
	 * @param battleOutcome
	 * @param isAtk
	 * @return
	 */
	public static FightMail.Builder createFightMail(IBattleIncome battleIncome, BattleOutcome battleOutcome, boolean isAtk) {
		FightMail.Builder builder = FightMail.newBuilder();
		List<Player> players = new ArrayList<>();
		// 本次参战部队信息
		Map<String, List<ArmyInfo>> battleArmyMap;
		
		// 敌方本次参战部队信息
		Map<String, List<ArmyInfo>> oppArmyMap;
		// 己方黑洞导致敌方死兵信息
		Map<String, Integer> blackHoleMap = new HashMap<>();
		if (isAtk) {
			players = battleIncome.getAtkPlayers();
			battleArmyMap = battleOutcome.getBattleArmyMapAtk();
			oppArmyMap = battleOutcome.getBattleArmyMapDef();
		} else {
			players = battleIncome.getDefPlayers();
			battleArmyMap = battleOutcome.getBattleArmyMapDef();
			oppArmyMap = battleOutcome.getBattleArmyMapAtk();
		}
		if (battleArmyMap == null || battleArmyMap.size() == 0) {
			return builder;
		}
		
		// 汇总敌方部队因黑洞导致的死兵信息
		if (oppArmyMap != null && !oppArmyMap.isEmpty()) {
			for (Entry<String, List<ArmyInfo>> entry : oppArmyMap.entrySet()) {
				List<ArmyInfo> oppArmys = entry.getValue();
				if (oppArmys == null) {
					continue;
				}
				for (ArmyInfo armyInfo : oppArmys) {
					Map<String, Integer> blackHoleInfo = armyInfo.getBlackHoleDead();
					if (blackHoleInfo == null) {
						continue;
					}
					for (Entry<String, Integer> blackEntry : blackHoleInfo.entrySet()) {
						String key = blackEntry.getKey();
						int dcnt = blackEntry.getValue();
						if (blackHoleMap.containsKey(key)) {
							blackHoleMap.put(key, blackHoleMap.get(key) + dcnt);
						} else {
							blackHoleMap.put(key, dcnt);
						}
					}
				}
			}
		}

		int[] battlePos = GameUtil.splitXAndY(battleIncome.getBattle().getPointId());
		builder.setBattleX(battlePos[0]);
		builder.setBattleY(battlePos[1]);

		// 己方部队数据整理(单位为人口)
		int totalSoldier = 0;
		int hurtSoldier = 0;
		int deadSoldier = 0;
		int killSoldier = 0;
		int defBuildLose = 0;
		int nationMilitary = 0;
		double totalDisBattlePoint = 0;
		int nationalHospital = 0;
		int eff12111Cnt = 0;
		// 部队信息列表
		List<MailArmyInfo> mailArmyList = new ArrayList<>();
		Battle battle = battleIncome.getBattle();
		if (players != null) {
			// 玩家数据
			for (Player player : players) {
				if (player instanceof GuarderPlayer) {
					continue;
				}
				String playerId = player.getId();
			
				MailArmyInfo.Builder mailArmyBuilder = MailArmyInfo.newBuilder();
				mailArmyBuilder.setPlantStep(player.getCityPlantLv());
				mailArmyBuilder.setNationMilitary(battleOutcome.getPlayerNationMilitary(player.getId()));
				nationMilitary += battleOutcome.getPlayerNationMilitary(player.getId());
				// 战前战力
				double battlePoint = 0;
				// 损失战力
				double disBattlePoint = 0;
				int selfnationalHospital = 0;
				if (StringUtils.isNotEmpty(player.getGuildName())) {
					mailArmyBuilder.setGuildName(player.getGuildName());
				}
				if (StringUtils.isNotEmpty(player.getGuildTag())) {
					mailArmyBuilder.setGuildTag(player.getGuildTag());
				}
				// 添加部队信息
				mailArmyBuilder.setPlayerName(player.getName());
				mailArmyBuilder.setPlayerId(player.getId());
				mailArmyBuilder.setLevel(player.getLevel());
				mailArmyBuilder.setIcon(player.getIcon());
				mailArmyBuilder.setFgylSkill(battleOutcome.getFgylSkill(player.getId()));
				if (!HawkOSOperator.isEmptyString(player.getPfIcon())) {
					mailArmyBuilder.setPfIcon(player.getPfIcon());
				}
				
				List<Integer> heroIds = isAtk ? battleIncome.getAtkPlayerHeros(player.getId()) : battleIncome.getDefPlayerHeros(player.getId());
				List<PlayerHero> heroOp = player.getHeroByCfgId(heroIds);
				for (PlayerHero herp : heroOp) {
					mailArmyBuilder.addHero(herp.toPBobj());
				}
				int ssoldierId = isAtk ? battleIncome.getAtkPlayerSuperSoldier(player.getId()) : battleIncome.getDefPlayerSuperSoldier(player.getId());
				SuperSoldier ssoldier = player.getSuperSoldierByCfgId(ssoldierId).orElse(null);
				if (Objects.nonNull(ssoldier)) {
					mailArmyBuilder.setSsoldier(ssoldier.toPBobj());
				}
				ArmourSuitType armourSuit = ArmourSuitType.valueOf(isAtk?battleIncome.getAtkPlayerArmourSuitId(player.getId()): battleIncome.getDefPlayerArmourSuitId(player.getId()));
				if(armourSuit != ArmourSuitType.ARMOUR_NONE){
					ArmourBriefInfo armourBuilder = player.genArmourBriefInfo(ArmourSuitType.valueOf(isAtk?battleIncome.getAtkPlayerArmourSuitId(player.getId()): battleIncome.getDefPlayerArmourSuitId(player.getId())));
					mailArmyBuilder.setArmourBrief(armourBuilder);
					addMailEffVal(mailArmyBuilder, player);
				}
				//超武已解锁
				if(player.checkManhattanFuncUnlock()) {
					PresetMarchManhattan presetManhattan = isAtk ? battleIncome.getAtkPlayerManhattan(playerId)
							: battleIncome.getDefPlayerManhattan(playerId);
					mailArmyBuilder.setManhattan(player.buildManhattanInfo(presetManhattan));
				}
				//机甲核心功能已解锁
				if(player.checkMechacoreFuncUnlock()) {
					MechaCoreSuitType suit = isAtk ? battleIncome.getAtkPlayerMechacoreSuit(player.getId()) : battleIncome.getDefPlayerMechacoreSuit(player.getId());
					mailArmyBuilder.setMechacore(player.buildMechacoreInfo(suit));
				}
				
				List<ArmyInfo> playerArmy = battleArmyMap.get(player.getId());
				if (playerArmy != null) {
					if (battleArmyMap.containsKey(GuarderPlayer.guarderPlayerId(player.getId()))) {
						playerArmy = new ArrayList<>(playerArmy);
						playerArmy.addAll(battleArmyMap.get(GuarderPlayer.guarderPlayerId(player.getId())));
					}
					// 己方军队详细数据
					for (ArmyInfo armyInfo : playerArmy) {
						BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyInfo.getArmyId());
						if (cfg == null) {
							continue;
						}
						// 防御建筑
						if (cfg.getType() == SoldierType.BARTIZAN_100_VALUE) {
							mailArmyBuilder.addDefBuilding(buildMailDefBuildingPB(armyInfo));
						}
						// 陷阱
						else if (cfg.getType() == SoldierType.WEAPON_LANDMINE_101_VALUE || cfg.getType() == SoldierType.WEAPON_ACKACK_102_VALUE
								|| cfg.getType() == SoldierType.WEAPON_ANTI_TANK_103_VALUE) {
							mailArmyBuilder.addSoldier(buildMailSoldierPB(armyInfo));
							defBuildLose += armyInfo.getDeadCount();
						}
						// 普通兵种
						else {
							mailArmyBuilder.addSoldier(buildMailSoldierPB(armyInfo));
							if(Objects.nonNull(armyInfo.getSssSLMPet())){
								mailArmyBuilder.addSoldier(buildMailSoldierPB(armyInfo.getSssSLMPet()));
							}
							// 数据统计
							totalSoldier += armyInfo.getTotalCount() + armyInfo.getShadowCnt();
							hurtSoldier += armyInfo.getWoundedCount();
							deadSoldier += armyInfo.getDeadCount() + armyInfo.getShadowDeadCnt();
							battlePoint += cfg.getPower() * (armyInfo.getTotalCount() + armyInfo.getShadowCnt());
							selfnationalHospital += armyInfo.getTszzNationalHospital();
							eff12111Cnt += armyInfo.getEff12111Cnt();
						}

						killSoldier += armyInfo.getKillCount();
						disBattlePoint += armyInfo.getDisBattlePoint();
					}
				}

				mailArmyBuilder.setBattlePoint((int) Math.ceil(battlePoint));
				mailArmyBuilder.setDisBattlePoint((int) Math.ceil(disBattlePoint));
				mailArmyBuilder.setNationalHospital(selfnationalHospital);
				if(player instanceof ISWPlayer){
					mailArmyBuilder.setSwDeadCount(((ISWPlayer) player).getDeadCnt());
				}
				if (blackHoleMap.containsKey(playerId)) {
					mailArmyBuilder.setBlackHoleDeadCnt(blackHoleMap.get(playerId));
				}
				if (battleIncome.getBattle().getEff12541map().containsKey(playerId)) {
					mailArmyBuilder.setManhattan12541(battleIncome.getBattle().getEff12541map().get(playerId));
				}
				mailArmyList.add(mailArmyBuilder.build());
				totalDisBattlePoint += disBattlePoint;
				nationalHospital += selfnationalHospital;
			}
		}
		// 己方战斗数据
		FighteInfo.Builder fighteInfo = FighteInfo.newBuilder();

		// 己方玩家信息
		MailPlayerInfo.Builder playerInfo = null;
		if (players != null && players.size() > 0) {
			Player leaderPlayer = players.get(0);
			playerInfo = createMailPlayerInfo(leaderPlayer);
		} else {
			playerInfo = createMailPlayerInfo(null);
		}
		// 损失的战力
		playerInfo.setDisBattlePoint((int) Math.ceil(totalDisBattlePoint));
		fighteInfo.setTotalSoldier(totalSoldier);
		fighteInfo.setDeadSoldier(deadSoldier);  // 损失
		fighteInfo.setHurtSoldier(hurtSoldier);  // 受伤
		fighteInfo.setSurvivalSoldier(totalSoldier - deadSoldier - hurtSoldier); // 存活
		fighteInfo.setKillSoldier(killSoldier);  // 消灭
		fighteInfo.setBattlefieldCure(0);
		fighteInfo.setDefBuildLose(defBuildLose);
		fighteInfo.setNationalHospital(nationalHospital);
		fighteInfo.setEff12111Cnt(eff12111Cnt);
		if(nationMilitary>0){
			fighteInfo.setNationMilitary(nationMilitary);
		}
		HawkLog.logPrintln("MailBuilderUtil fighteInfo, playerId: {}, isAtk: {}, DisBattlePoint: {}, TotalSoldier: {}, DeadSoldier: {}, HurtSoldier: {}, SurvivalSoldier: {}, KillSoldier: {}, DefBuildLose: {}", 
				playerInfo.getPlayerId(), isAtk, playerInfo.getDisBattlePoint(), fighteInfo.getTotalSoldier(), fighteInfo.getDeadSoldier(),
				fighteInfo.getHurtSoldier(), fighteInfo.getSurvivalSoldier(), fighteInfo.getKillSoldier(), fighteInfo.getDefBuildLose());
		Map<EffType, Integer> effs = new HashMap<>();
		BattleUnity leaderUnit = null;
		if (isAtk) {
			effs = battle.getAttacker().getEffMap();
			leaderUnit = battleIncome.getAtkCalcParames().getleaderUnity();
		} else {
			effs = battle.getDefencer().getEffMap();
			leaderUnit = battleIncome.getDefCalcParames().getleaderUnity();
		}
		effs.entrySet().stream().forEach(e -> {
			builder.addSelfEffs(EffectPB.newBuilder().setEffId(e.getKey().getNumber()).setEffVal(e.getValue()));
		});
		
		if (Objects.nonNull(leaderUnit)) {
			try {
				fighteInfo.setStaffPoint(leaderUnit.getUnitStatic().getStaffOfficePoint());
				for (StaffOfficerSkill staffSkill : leaderUnit.getPlayer().getStaffOffic().getSkillList()) {
					if (!staffSkill.isUnLock()) {
						continue;
					}
					StaffOfficerType type = staffSkill.getCfg().getType();
					if (type == StaffOfficerType.SOType4 || type == StaffOfficerType.SOType3) {
						if (!leaderUnit.getEffectParams().isStaffPointGreat()) {
							continue;
						}
					}
					fighteInfo.addStaffSkills(staffSkill.toPBObj());
				}
				for (PlayerHero hero : leaderUnit.getPlayer().getAllHero()) {
					HeroOfficeCfg offCfg = HawkConfigManager.getInstance().getConfigByKey(HeroOfficeCfg.class, hero.getOffice());
					if (Objects.nonNull(offCfg) && offCfg.getStaffOfficer() == 1) {
						fighteInfo.addStaffHeros(hero.toPBobj());
					}
				}
				fighteInfo.setForceFieldMax((long) Math.ceil(leaderUnit.getSolider().getTroop().forceFieldMax/1000.0));
				fighteInfo.setForceField((long) Math.ceil(leaderUnit.getSolider().getTroop().getForceField()/1000.0));
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

		// 战斗数据添加
		builder.setSelfPlayer(playerInfo);
		builder.setSelfFight(fighteInfo);
		builder.addAllSelfArmy(mailArmyList);
		builder.setBattleRound(battleIncome.getBattle().getBattleRound());
		return builder;
	}
	
	/**
	 * 创建野怪邮件
	 * @return
	 */
	public static MonsterMail.Builder createMonsterMail(BattleOutcome battleOutcome, List<Integer> heroId, int monsterId, WorldPoint point, 
			List<ItemInfo> normalRewards, List<ItemInfo> fistKillRewards, float remainBlood, float atkValue, int woundCount) {
		return createMonsterMail(battleOutcome, heroId, monsterId, point, normalRewards, fistKillRewards, remainBlood, atkValue, woundCount, 1);
	}
	/**
	 * 创建野怪邮件
	 * @param afterArmyList 进攻方部队列表
	 * @param isWin	是否胜利
	 * @param monsterId	怪物id
	 * @param point	世界点
	 * @param normalReward 普通奖励
	 * @param fistKillReward 首杀奖励
	 * @return
	 */
	public static MonsterMail.Builder createMonsterMail(BattleOutcome battleOutcome, List<Integer> heroId, int monsterId, WorldPoint point, 
			List<ItemInfo> normalRewards, List<ItemInfo> fistKillRewards, float remainBlood, float atkValue, int woundCount, int atkTotalTimes) {
		MonsterMail.Builder monsterBuilder = MonsterMail.newBuilder();
		monsterBuilder.setMonsterId(monsterId);
		monsterBuilder.setResult(battleOutcome.isAtkWin() ? FightResult.ATTACK_SUCC : FightResult.ATTACK_FAIL);
		if (fistKillRewards != null && !fistKillRewards.isEmpty()) {
			monsterBuilder.setFirstKill(true);
			fistKillRewards.forEach(fistKillReward -> monsterBuilder.addFirstKillReward(fistKillReward.toRewardItem()));
		}
		if (normalRewards != null) {
			normalRewards.forEach(normalReward -> monsterBuilder.addRewards(normalReward.toRewardItem()));
		}
		monsterBuilder.setX(point.getX());
		monsterBuilder.setY(point.getY());

		int totalCnt = 0;
		int hurtCnt = 0;
		int survivalCnt = 0;
		double disBattlePoint = 0;
		// 本次战斗部队信息
		Map<String, List<ArmyInfo>> battleArmyMapAtk = battleOutcome.getBattleArmyMapAtk();
		for (ArmyInfo info : WorldUtil.mergAllPlayerArmy(battleArmyMapAtk)) {
			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, info.getArmyId());
			totalCnt += info.getTotalCount();
			survivalCnt += info.getFreeCnt();
			hurtCnt += info.getWoundedCount();
			disBattlePoint += info.getWoundedCount() * cfg.getPower();
		}
		FighteInfo.Builder figntInfo = FighteInfo.newBuilder();
		figntInfo.setTotalSoldier(totalCnt);
		figntInfo.setHurtSoldier(hurtCnt);
		figntInfo.setSurvivalSoldier(survivalCnt);
		figntInfo.setDisBattlePoint((int) Math.ceil(disBattlePoint));

		monsterBuilder.setAtkFight(figntInfo);

		monsterBuilder.setRemainBlood(remainBlood);
		monsterBuilder.setAtkValue(atkValue);
		monsterBuilder.setWoundCount(woundCount);
		monsterBuilder.setTotalTimes(atkTotalTimes);
		return monsterBuilder;
	}
	/**
	 * 构建参战士兵信息
	 * 
	 * @param armyInfo
	 * @return
	 */
	public static MailSoldierPB.Builder buildMailSoldierPB(ArmyInfo armyInfo) {
		BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyInfo.getArmyId());
		MailSoldierPB.Builder soldierBuilder = MailSoldierPB.newBuilder();
		soldierBuilder.setSoldierId(armyInfo.getArmyId());
		soldierBuilder.setDeadCnt(armyInfo.getDeadCount());
		soldierBuilder.setKillCnt(armyInfo.getMailShowKillCount());
		soldierBuilder.setWoundedCnt(armyInfo.getWoundedCount());
		soldierBuilder.setSurvivedCnt(armyInfo.getFreeCnt());
		soldierBuilder.setRealLoseCnt(armyInfo.getRealLoseCount());
		soldierBuilder.setSave1634(armyInfo.getSave1634());
		soldierBuilder.setHelp1635TarCount(armyInfo.getHelp1635TarCount());
		soldierBuilder.setHelp1635Count(armyInfo.getHelp1635Count());
		soldierBuilder.setShadowLoseCnt(armyInfo.getShadowDeadCnt());
		soldierBuilder.setShadowVanishCnt(armyInfo.getShadowCnt() - armyInfo.getShadowDeadCnt());
		soldierBuilder.setStar(armyInfo.getStar());
		soldierBuilder.setPlantStep(armyInfo.getPlantStep());
		soldierBuilder.setPlantSkillLevel(armyInfo.getPlantSkillLevel());
		soldierBuilder.setPlantMilitaryLevel(armyInfo.getPlantMilitaryLevel());
		soldierBuilder.setDodgeCnt(armyInfo.getDodgeCnt());
		soldierBuilder.setExtrAtktimes(armyInfo.getExtrAtktimes());
		soldierBuilder.setLiao1645Cnt(armyInfo.getLiao1645Cnt());
		soldierBuilder.setLiao1645Kill(armyInfo.getLiao1645Kill());
		soldierBuilder.setNationalHospital(armyInfo.getTszzNationalHospital());
		soldierBuilder.setKunNa1652Help(armyInfo.getKunNa1652Help());
		soldierBuilder.setKunNa1653Kill(armyInfo.getKunNa1653Kill());
		soldierBuilder.setSssKaiEn1656Cnt(armyInfo.getSssKaiEn1656Cnt());
		soldierBuilder.setSssSLM1667Kill(armyInfo.isSssSLM1667Kill() ? 1 : 0);
		soldierBuilder.setEff12086Zhuan(armyInfo.getEff12086Zhuan());
		soldierBuilder.setEff12086ZhuanAll(armyInfo.getEff12086ZhuanAll());
		soldierBuilder.setDeadPower((int) (armyInfo.getDeadCount() * cfg.getPower()));
		soldierBuilder.setWoundedPower((int) (armyInfo.getWoundedCount() * cfg.getPower()));
		soldierBuilder.setSurvivedPower((int) (armyInfo.getFreeCnt() * cfg.getPower()));
		soldierBuilder.setRealLosePower((int) (armyInfo.getRealLoseCount() * cfg.getPower()));
		soldierBuilder.setShadowLosePower((int) (armyInfo.getShadowDeadCnt()* cfg.getPower()));
		soldierBuilder.setKillPower((int) (armyInfo.getKillPower()));
		soldierBuilder.setEff12339Cnt(armyInfo.getEff12339Cnt());
		soldierBuilder.setEff12339Power((int) armyInfo.getEff12339Power());
		soldierBuilder.setEff11044Kill(GuarderPlayer.isGuarderPlayer(armyInfo.getPlayerId()) ? 1 : 0);
		return soldierBuilder;
	}
	
	/**
	 * 构建防御建筑(箭塔)信息
	 * @param armyInfo
	 * @return
	 */
	private static MailDefBuildingPB.Builder buildMailDefBuildingPB(ArmyInfo armyInfo){
		MailDefBuildingPB.Builder builder = MailDefBuildingPB.newBuilder();
		builder.setBuildingId(AssembleDataManager.getInstance().getBuildingCfgId(armyInfo.getArmyId()));
		builder.setKillCnt(armyInfo.getKillCount());
		builder.setKillPower((int) (armyInfo.getKillPower()));
		return builder;
	}

	/**
	 * 玩家数据
	 * 
	 * @param player
	 * @param point
	 * @param commanderState
	 * @return
	 */
	public static MailPlayerInfo.Builder createMailPlayerInfo(Player player) {
		MailPlayerInfo.Builder playerInfo = MailPlayerInfo.newBuilder();

		if (player != null) {
			playerInfo.setPlayerId(player.getId());
			playerInfo.setName(player.getName());
			playerInfo.setIcon(player.getIcon());

			int[] pos = player.getPosXY();
			playerInfo.setX(pos[0]);
			playerInfo.setY(pos[1]);
			playerInfo.setPower(player.getPower());
			String guildName = player.getGuildName();
			if (!HawkOSOperator.isEmptyString(guildName)) {
				playerInfo.setGuildName(guildName);
			}
			String guildTag = player.getGuildTag();
			if (!HawkOSOperator.isEmptyString(guildTag)) {
				playerInfo.setGuildTag(guildTag);
			}

			if (!HawkOSOperator.isEmptyString(player.getPfIcon())) {
				playerInfo.setPfIcon(player.getPfIcon());
			}
		}
		return playerInfo;
	}
	
	/**
	 * 添加作用号信息
	 * 
	 * @param builder
	 * @param player
	 */
	public static void addMailEffVal(MailArmyInfo.Builder builder, Player player) {
		try {
			EffType[] types = new EffType[] {Const.EffType.ARMOUR_11001, Const.EffType.ARMOUR_11002,
					Const.EffType.ARMOUR_STAR_EXPLORE_11014,Const.EffType.ARMOUR_STAR_EXPLORE_11015,
					Const.EffType.ARMOUR_STAR_EXPLORE_11016,Const.EffType.ARMOUR_STAR_EXPLORE_11017,
					Const.EffType.ARMOUR_STAR_EXPLORE_11018,Const.EffType.ARMOUR_STAR_EXPLORE_11019,
					Const.EffType.ARMOUR_STAR_EXPLORE_11020,Const.EffType.ARMOUR_STAR_EXPLORE_11021,
					Const.EffType.ARMOUR_STAR_EXPLORE_11022,Const.EffType.ARMOUR_STAR_EXPLORE_11023,
					Const.EffType.ARMOUR_STAR_EXPLORE_11024,Const.EffType.ARMOUR_STAR_EXPLORE_11025,
					Const.EffType.ARMOUR_STAR_EXPLORE_11026,Const.EffType.ARMOUR_STAR_EXPLORE_11027,
					Const.EffType.ARMOUR_STAR_EXPLORE_11028,Const.EffType.ARMOUR_STAR_EXPLORE_11029,
					Const.EffType.ARMOUR_STAR_EXPLORE_11030,Const.EffType.ARMOUR_STAR_EXPLORE_11031,
					Const.EffType.ARMOUR_STAR_EXPLORE_11032,Const.EffType.ARMOUR_STAR_EXPLORE_11034,
					Const.EffType.ARMOUR_STAR_EXPLORE_11035};
			int[] vals = player.getEffect().getEffValArr(types);
			for (int i=0; i<types.length; i++) {
				MailEffVal.Builder effBuilder = MailEffVal.newBuilder();
				effBuilder.setEffId(types[i].getNumber());
				effBuilder.setEffVal(vals[i]);
				builder.addEffVal(effBuilder);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

}
