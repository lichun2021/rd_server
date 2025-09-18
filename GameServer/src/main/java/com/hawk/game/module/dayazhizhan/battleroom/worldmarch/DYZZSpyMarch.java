package com.hawk.game.module.dayazhizhan.battleroom.worldmarch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.city.CityManager;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.config.TechnologyCfg;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.entity.TalentEntity;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.dayazhizhan.battleroom.IDYZZWorldPoint;
import com.hawk.game.module.dayazhizhan.battleroom.player.IDYZZPlayer;
import com.hawk.game.module.dayazhizhan.battleroom.worldmarch.submarch.IDYZZReportPushMarch;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.IDYZZBuilding;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.IDYZZFuelBank;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Mail.DefenceBuilding;
import com.hawk.game.protocol.Mail.DetectMail;
import com.hawk.game.protocol.Mail.MailArmyInfo;
import com.hawk.game.protocol.Mail.MailPlayerInfo;
import com.hawk.game.protocol.Mail.PBDetectDYZZBuilding;
import com.hawk.game.protocol.Mail.PBDetectData;
import com.hawk.game.protocol.Mail.SpyTaltentPB;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.mail.DungeonMailType;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.MailBuilderUtil;
import com.hawk.game.world.march.IWorldMarch;

public class DYZZSpyMarch extends IDYZZWorldMarch implements IDYZZReportPushMarch {

	public DYZZSpyMarch(IDYZZPlayer parent) {
		super(parent);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.SPY;
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
	public void heartBeats() {
		// 当前时间
		long currTime = getParent().getParent().getCurTimeMil();
		// 行军或者回程时间未结束
		if (getMarchEntity().getEndTime() > currTime) {
			return;
		}
		// 行军返回到达
		if (getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
			onMarchBack();
			return;
		}

		// 行军到达
		onMarchReach(getParent());

	}

	@Override
	public void onMarchReach(Player parent) {
		IDYZZWorldPoint point = getParent().getParent().getWorldPoint(getMarchEntity().getTerminalX(), getMarchEntity().getTerminalY()).orElse(null);
		// 路点为空
		if (point == null) {
			onMarchReturn(getMarchEntity().getTerminalId(), getMarchEntity().getOrigionId(), Collections.emptyList());
			return;
		}

		if (point instanceof IDYZZPlayer) {
			doPlayerSpy(point);
			return;
		}
		if (point instanceof IDYZZFuelBank) {
			doFuelBankSpy(point);
			onMarchReturn(getMarchEntity().getTerminalId(), getMarchEntity().getOrigionId(), Collections.emptyList());
			return;
		}

		if (point instanceof IDYZZBuilding) {
			doBuildSpy(point);
			onMarchReturn(getMarchEntity().getTerminalId(), getMarchEntity().getOrigionId(), Collections.emptyList());
			return;
		}

		onMarchReturn(getMarchEntity().getTerminalId(), getMarchEntity().getOrigionId(), Collections.emptyList());
	}

	private void doBuildSpy(IDYZZWorldPoint point) {
		// DYZZ_SPY_BUILD_SUCCESS = 2020009;//泰伯利亚建筑侦察成功 2020009
		// DYZZ_SPY_BUILD_FAIL = 2020010;//泰伯利亚建筑侦察失败 2020010
		// DYZZ_SPY_BUILD_BE = 2020011; //泰伯利亚建筑被侦察 2020011
		IDYZZBuilding build = (IDYZZBuilding) point;
		List<IDYZZWorldMarch> quarteredMarchs = getParent().getParent().getPointMarches(getMarchEntity().getTerminalId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);

		if (quarteredMarchs.isEmpty()) {
			return;
		}

		if (quarteredMarchs.get(0).getParent().getCamp() == getParent().getCamp()) {
			// FightMailService.getInstance().sendMail(MailParames.newBuilder()
			// .setPlayerId(getPlayerId())
			// .addSubTitles(point.getX(), point.getY())
			// .addContents(point.getX(), point.getY())
			// .setMailId(MailId.DYZZ_SPY_BUILD_FAIL)
			// .build());
			return;
		}

		// 确认侦查类型
		final int radarLevel = radaLevel(getParent());

		PBDetectDYZZBuilding.Builder builder = PBDetectDYZZBuilding.newBuilder().setLevel(radarLevel).setPointX(point.getX()).setPointY(point.getY());
		builder.setBuildId(point.getPointType().getNumber());
		builder.setIndex(build.getIndex());
		if (radarLevel >= GsConst.RadarLevel.LV10) {
			for (IWorldMarch ma : quarteredMarchs) {
				MailArmyInfo.Builder defenceArmy = MailBuilderUtil.getDefenceArmy(radarLevel, ma, radarLevel < GsConst.RadarLevel.LV18);
				builder.addDefenceArmy(defenceArmy);
			}
		}
		if (radarLevel >= GsConst.RadarLevel.LV4 && radarLevel < GsConst.RadarLevel.LV10) {
			int assistTotal = quarteredMarchs.stream().flatMap(m -> m.getMarchEntity().getArmys().stream()).mapToInt(a -> a.getFreeCnt()).sum();
			builder.setDefenceArmyAboutNum(GameUtil.getProbablyNum(assistTotal, GsConst.RADA_RANDOM_RANGE));
		}

		// 侦查邮件
		MailParames.Builder playerParamesBuilder = MailParames.newBuilder().setPlayerId(getParent().getId()).setMailId(MailId.DYZZ_SPY_BUILD_SUCCESS)
				.addSubTitles(point.getX(), point.getY())
				.setDuntype(DungeonMailType.DYZZ)
				.addContents(DetectMail.newBuilder().setDyzzBuild(builder)).addTips(point.getX()).addTips(point.getY());

		MailParames mparames = playerParamesBuilder.build();
		FightMailService.getInstance().sendMail(mparames);
		build.getSpyMailList().add(0, mparames.getUuid());

		// 向守军推送被侦查
		IDYZZPlayer player = getParent();
		for (IWorldMarch tempMarch : quarteredMarchs) {
			FightMailService.getInstance()
					.sendMail(MailParames.newBuilder().setPlayerId(tempMarch.getPlayerId()).setMailId(MailId.DYZZ_SPY_BUILD_BE).addSubTitles(point.getX(), point.getY())
							.addTitles(player.getName())
							.addMidTitles(player.getGuildTag(), player.getName())
							.setDuntype(DungeonMailType.DYZZ)
							.addContents(player.getIcon(), player.getName(), player.getPower(), player.getX(), player.getY(), player.getPfIcon(), point.getX(), point.getY(),
									player.getGuildTag())
							.build());
		}

	}

	private void doFuelBankSpy(IDYZZWorldPoint point) {
		// DYZZ_SPY_RES_SUCCESS = 2020005;//泰伯利亚侦察资源点 2020005
		// DYZZ_SPY_RES_BE = 2020006;//泰伯利亚被侦察资源点 2020006
		// DYZZ_SPY_RES_FAIL = 2020007;//泰伯利亚资源点侦察失败（撤离） 2020007
		// DYZZ_SPY_RES_FAIL_SAME_GUILD = 2020008;//泰伯利亚资源点侦察失败（联盟变更） 2020008
		IDYZZFuelBank resPoint = (IDYZZFuelBank) point;
		if (resPoint.getMarch() == null) {
			return;
		}

		DYZZCollectFuelMarch enemy = resPoint.getMarch();
		if (enemy.getParent().getCamp() == getParent().getCamp()) {
			return;
		}

		// 首都上驻扎的行军
		List<IWorldMarch> quarteredMarchs = new ArrayList<>();
		quarteredMarchs.add(enemy);

		// 确认侦查类型
		final int radarLevel = radaLevel(getParent());

		PBDetectDYZZBuilding.Builder builder = PBDetectDYZZBuilding.newBuilder().setLevel(radarLevel).setPointX(point.getX()).setPointY(point.getY());
		builder.setBuildId(resPoint.getPointType().getNumber());
		if (radarLevel >= GsConst.RadarLevel.LV10) {
			for (IWorldMarch ma : quarteredMarchs) {
				MailArmyInfo.Builder defenceArmy = MailBuilderUtil.getDefenceArmy(radarLevel, ma, radarLevel < GsConst.RadarLevel.LV18);
				builder.addDefenceArmy(defenceArmy);
			}
		}
		if (radarLevel >= GsConst.RadarLevel.LV4 && radarLevel < GsConst.RadarLevel.LV10) {
			int assistTotal = quarteredMarchs.stream().flatMap(m -> m.getMarchEntity().getArmys().stream()).mapToInt(a -> a.getFreeCnt()).sum();
			builder.setDefenceArmyAboutNum(GameUtil.getProbablyNum(assistTotal, GsConst.RADA_RANDOM_RANGE));
		}
		builder.setPlayer(MailBuilderUtil.createMailPlayerInfo(quarteredMarchs.get(0).getPlayer()));
		// 侦查邮件
		MailParames.Builder playerParamesBuilder = MailParames.newBuilder().setPlayerId(getParent().getId()).setMailId(MailId.DYZZ_SPY_RES_SUCCESS)
				.addSubTitles(point.getX(), point.getY())
				.addMidTitles(enemy.getParent().getGuildTag(), enemy.getParent().getName())
				.setDuntype(DungeonMailType.DYZZ)
				.addTips(point.getX(),point.getY(),enemy.getParent().getName())
				.addContents(DetectMail.newBuilder().setDyzzBuild(builder));
		MailParames mparames = playerParamesBuilder.build();
		FightMailService.getInstance().sendMail(mparames);

		// 向守军推送被侦查
		int[] playerPos = getParent().getPos();
		IDYZZPlayer player = getParent();
		for (IWorldMarch tempMarch : quarteredMarchs) {
			FightMailService.getInstance()
					.sendMail(MailParames.newBuilder().setPlayerId(tempMarch.getPlayerId()).setMailId(MailId.DYZZ_SPY_RES_BE).addSubTitles(point.getX(), point.getY())
							.setDuntype(DungeonMailType.DYZZ)
							.addMidTitles(player.getGuildTag(), player.getName())
							.addContents(player.getIcon(), player.getName(), player.getPower(), playerPos[0], playerPos[1], player.getPfIcon(), point.getX(), point.getY(),
									player.getGuildTag())
							.build());
		}

	}

	private void doPlayerSpy(IDYZZWorldPoint point) {
		IDYZZPlayer tarPlayer = (IDYZZPlayer) point;

		sendDetectMail(getParent(), tarPlayer, point);

		onMarchReturn(getMarchEntity().getTerminalId(), getMarchEntity().getOrigionId(), Collections.emptyList());
	}

	@Override
	public void onMarchBack() {

		this.remove();

	}

	/**
	 * 发送侦察成功邮件
	 * 
	 * @param player
	 * @param targetPlayer
	 * @param point
	 * @return
	 */
	public boolean sendDetectMail(IDYZZPlayer player, IDYZZPlayer targetPlayer, IDYZZWorldPoint point) {
		if (player == null || targetPlayer == null || point == null) {
			return false;
		}

		// 对方使用了部队镜像
		boolean isMirror = false;
		if (point.getPointType() == WorldPointType.PLAYER && targetPlayer.getData().getEffVal(EffType.CITY_SCORT_MIRROR) > 0) {
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
		DetectMail.Builder builder = createDetectMail(buildCfg.getLevel(), player, targetPlayer, point, isMirror);
		builder.setSelfPower(player.getPower());

		int[] pos = player.getPosXY();
		MailPlayerInfo pbtplayer = builder.getDetectData().getPlayer();
		Object[] subTitle = new Object[] { pbtplayer.getName(), pbtplayer.getX(), pbtplayer.getY() };
		MailParames.Builder playerParamesBuilder = MailParames.newBuilder().setPlayerId(player.getId()).addSubTitles(subTitle).addContents(builder)
				.setOppPfIcon(targetPlayer.getPfIcon())
				.addTips(targetPlayer.getName()).setDuntype(DungeonMailType.DYZZ)
				.addMidTitles(targetPlayer.getGuildTag(), targetPlayer.getName());
		MailParames.Builder targetParamesBuilder = MailParames.newBuilder().setPlayerId(targetPlayer.getId()).setOppPfIcon(player.getPfIcon()).addTips(player.getName())
				.setDuntype(DungeonMailType.DYZZ)
				.addMidTitles(player.getGuildTag(), player.getName());
		int targetConstructionFactoryLevel = targetPlayer.getCityLevel();
		int constructionFactoryLevel = player.getCityLevel();
		if (point.getPointType() == WorldPointType.PLAYER) {
			// 发送邮件---侦查玩家基地成功（侦察方）
			playerParamesBuilder.setMailId(MailId.DETECT_BASE_SUCC_TO_FROM);
			// 发送邮件---侦查玩家基地成功（被侦察方）
			targetParamesBuilder.setMailId(MailId.DETECT_BASE_SUCC_TO_TARGET).addContents(player.getIcon()).addContents(player.getName()).addContents(player.getPower())
					.addContents(pos[0])
					.addContents(pos[1]).addContents(player.getPfIcon()).addContents(targetConstructionFactoryLevel).addContents(constructionFactoryLevel)
					.addContents(player.getGuildTag());

		}
		FightMailService.getInstance().sendMail(playerParamesBuilder.setDuntype(DungeonMailType.DYZZ).build());

		FightMailService.getInstance().sendMail(targetParamesBuilder.setDuntype(DungeonMailType.DYZZ).build());
		return true;
	}

	public DetectMail.Builder createDetectMail(final int radarLevel, IDYZZPlayer player, IDYZZPlayer targetPlayer, IDYZZWorldPoint point, boolean isMirror) {
		PBDetectData.Builder builder = PBDetectData.newBuilder();
		MailPlayerInfo.Builder playerInfo = MailPlayerInfo.newBuilder();

		builder.setLevel(radarLevel);

		// 侦查点为空
		if (point == null) {
			return DetectMail.newBuilder();
		}

		// 侦查类型过滤
		int pointType = point.getPointType().getNumber();
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
				playerInfo.setName(GameUtil.getPlayerNameWithGuildTag(targetPlayer.getDYZZGuildId(), targetPlayer.getName()));
				playerInfo.setIcon(targetPlayer.getIcon());
				playerInfo.setX(point.getX());
				playerInfo.setY(point.getY());
				playerInfo.setPower(targetPlayer.getPower());
				playerInfo.setPfIcon(targetPlayer.getPfIcon());
				builder.setPlayer(playerInfo);
			}
		}

		if (pointType == WorldPointType.RESOURCE_VALUE || pointType == WorldPointType.QUARTERED_VALUE) {
			// 资源点 驻扎点
			List<IDYZZWorldMarch> marchs = getParent().getParent().getPointMarches(point.getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED);
			if (!marchs.isEmpty()) {
				leaderMarch = marchs.get(0);
			}
		} else if (pointType == WorldPointType.PLAYER_VALUE) {
			// 玩家城堡---基地
			List<IDYZZWorldMarch> massMarches = getParent().getParent().getPlayerMarches(targetPlayer.getId(), WorldMarchStatus.MARCH_STATUS_WAITING);
			if (!massMarches.isEmpty()) {
				leaderMarch = massMarches.get(0);
			}
			List<IDYZZWorldMarch> marchs = getParent().getParent().getPointMarches(point.getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST);
			if (marchs != null && marchs.size() > 0) {
				marches = new ArrayList<IWorldMarch>(marchs);
			}
		}

		if (pointType == WorldPointType.PLAYER_VALUE && !targetPlayer.getData().getArmyEntities().isEmpty()) {
			if (radarLevel >= GsConst.RadarLevel.LV18) {
				// 防守部队和所有部队的准确数量
				MailArmyInfo.Builder defenceArmy = MailBuilderUtil.getDefenceArmy(radarLevel, targetPlayer, false, isMirror);
				builder.setDefenceArmy(defenceArmy);
			} else if (radarLevel >= GsConst.RadarLevel.LV10 && radarLevel < GsConst.RadarLevel.LV18) {
				// 防守部队的的部队组成和各部队大致数量
				MailArmyInfo.Builder defenceArmy = MailBuilderUtil.getDefenceArmy(radarLevel, targetPlayer, true, isMirror);
				builder.setDefenceArmy(defenceArmy);
			} else if (radarLevel >= GsConst.RadarLevel.LV4 && radarLevel < GsConst.RadarLevel.LV10) {
				// 防守部队总数的大致数量
				int soldierTotal = targetPlayer.marchArmy().stream().mapToInt(e -> e.getFree()).sum();
				soldierTotal = GameUtil.getProbablyNum(soldierTotal, GsConst.RADA_RANDOM_RANGE);
				soldierTotal = isMirror ? soldierTotal * 2 : soldierTotal;
				builder.setDefenceArmyAboutNum(soldierTotal);
			}

			// 集结行军
			if (radarLevel >= GsConst.RadarLevel.LV21 && leaderMarch != null) {
				List<IDYZZWorldMarch> massMarches = getParent().getParent().getPointMarches(point.getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH,
						WorldMarchStatus.MARCH_STATUS_WAITING,
						WorldMarchType.MASS_JOIN);

				List<IWorldMarch> unReachList = massMarches.stream().filter(march -> march.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE)
						.collect(Collectors.toList());
				List<IWorldMarch> reachList = massMarches.stream().filter(march -> march.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE)
						.collect(Collectors.toList());
				reachList.add(leaderMarch);

				ToIntFunction<List<IWorldMarch>> armyCountFunc = list -> list.stream().flatMap(march -> march.getMarchEntity().getArmys().stream()).mapToInt(ArmyInfo::getFreeCnt)
						.sum();

				int unreach = armyCountFunc.applyAsInt(unReachList);
				int reach = armyCountFunc.applyAsInt(reachList);
				builder.setMassNum(unreach);
				if (radarLevel >= GsConst.RadarLevel.LV23) { // 集结行军详情
					builder.addAllMassArmy(MailBuilderUtil.createHelpArmy(GsConst.RadarLevel.LV30, isMirror, unReachList));
				}
				if (radarLevel >= GsConst.RadarLevel.LV25) {
					builder.setMassNumReach(reach);
				}
				if (radarLevel >= GsConst.RadarLevel.LV27) {
					builder.addAllMassArmyReach(MailBuilderUtil.createHelpArmy(GsConst.RadarLevel.LV30, isMirror, reachList));
				}
			}

		} else if (leaderMarch != null) {
			if (radarLevel >= GsConst.RadarLevel.LV18) {
				// 防守部队和所有部队的准确数量
				MailArmyInfo.Builder defenceArmy = MailBuilderUtil.getDefenceArmy(radarLevel, leaderMarch, false);
				builder.setDefenceArmy(defenceArmy);
			} else if (radarLevel >= GsConst.RadarLevel.LV10 && radarLevel < GsConst.RadarLevel.LV18) {
				// 防守部队的的部队组成和各部队大致数量
				MailArmyInfo.Builder defenceArmy = MailBuilderUtil.getDefenceArmy(radarLevel, leaderMarch, true);
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
					builder.addAllHelpArmy(MailBuilderUtil.createHelpArmy(radarLevel, isMirror, marches));
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
			// 对方城防设施大至数量 (城防兵的大至数) sb策划文档到处让人猜,文档和UI也对不上.
			if (radarLevel >= GsConst.RadarLevel.LV6) {
				builder.setDefenceNum(GameUtil.getProbablyNum(all, GsConst.RADA_RANDOM_RANGE));

				Consumer<? super BuildingBaseEntity> action = buld -> {
					DefenceBuilding.Builder debuild = DefenceBuilding.newBuilder().setId(buld.getBuildingCfgId()).setNum(1);
					if (buld.getType() == BuildingType.PRISM_TOWER_VALUE) {
						debuild.setType(1);
					} else if (buld.getType() == BuildingType.CITY_WALL_VALUE) {
						debuild.setType(3);
						if (targetPlayer.getPlayerBaseEntity().getOnFireEndTime() > getParent().getParent().getCurTimeMil()) {
							debuild.setFireSpeed((int) CityManager.getInstance().getWallFireSpeed(targetPlayer));
						}
						int cityDefVal = CityManager.getInstance().getRealCityDef(targetPlayer);
						// if (radarLevel >= GsConst.RadarLevel.LV24) {
						debuild.setCityDefVal(cityDefVal);
						// }else{
						// debuild.setCityDefVal(GameUtil.getProbablyNum(cityDefVal,
						// GsConst.RADA_RANDOM_RANGE));
						// }
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
					if (radarLevel >= GsConst.RadarLevel.LV20) {
						debuild.setNum(sod.getFree());
					} else {
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
					if (talentEntity.getLevel() > 0 && !talentEntity.isInvalid()) {
						builder.addTalent(SpyTaltentPB.newBuilder().setLevel(talentEntity.getLevel()).setTalentId(talentEntity.getTalentId()));
					}
				}
			}

			// 科技
			if (radarLevel >= GsConst.RadarLevel.LV26) {
				List<Integer> allTech = targetPlayer.getData().getTechnologyEntities().stream().filter(e -> e.getLevel() > 0)
						.map(e -> HawkConfigManager.getInstance().getConfigByKey(TechnologyCfg.class, e.getCfgId())).filter(Objects::nonNull).map(TechnologyCfg::getId)
						.collect(Collectors.toList());
				builder.addAllTech(allTech);
			}

			// 藏兵洞
			List<IDYZZWorldMarch> hiddenMarchs = getParent().getParent().getPointMarches(point.getPointId(), WorldMarchType.HIDDEN_MARCH);
			if (radarLevel >= GsConst.RadarLevel.LV10) {
				for (IWorldMarch ma : hiddenMarchs) {
					MailArmyInfo.Builder defenceArmy = MailBuilderUtil.getDefenceArmy(radarLevel, ma, radarLevel < GsConst.RadarLevel.LV18);
					builder.addHiddenArmy(defenceArmy);
				}
			}
			if (radarLevel >= GsConst.RadarLevel.LV4 && radarLevel < GsConst.RadarLevel.LV10) {
				int hiddenTotal = hiddenMarchs.stream().flatMap(m -> m.getArmys().stream()).mapToInt(a -> a.getFreeCnt()).sum();
				builder.setHiddenArmyAboutNum(GameUtil.getProbablyNum(hiddenTotal, GsConst.RADA_RANDOM_RANGE));
			}

		}

		if (leaderMarch != null) {
			builder.setLeaderMarchId(leaderMarch.getMarchId());
		}
		return DetectMail.newBuilder().setDetectData(builder);
	}

	@Override
	public Set<String> attackReportRecipients() {
		// 防守方援军
		List<IDYZZWorldMarch> helpMarchList = getParent().getParent().getPointMarches(getMarchEntity().getTerminalId(), WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST,
				WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED, WorldMarchStatus.MARCH_STATUS_MARCH_COLLECT);
		Set<String> result = new HashSet<>();
		result.add(getMarchEntity().getTargetId());
		for (IDYZZWorldMarch march : helpMarchList) {
			result.add(march.getPlayerId());
		}
		return result;
	}

	private int radaLevel(Player player) {
//		// 确认侦查类型
//		BuildingBaseEntity buildingEntity = player.getData().getBuildingEntityByType(BuildingType.RADAR);
//		if (Objects.isNull(buildingEntity)) {
//			return 0;
//		}
//		BuildingCfg buildCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
//		if (buildCfg == null) {
//			return 0;
//		}
//
//		return buildCfg.getLevel();
		return GsConst.RadarLevel.LV35;
	}
}
