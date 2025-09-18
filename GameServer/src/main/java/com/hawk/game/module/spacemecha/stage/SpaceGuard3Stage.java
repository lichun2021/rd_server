package com.hawk.game.module.spacemecha.stage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.xid.HawkXID;

import com.hawk.game.battle.NpcPlayer;
import com.hawk.game.entity.CustomDataEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.spacemecha.MechaSpaceInfo;
import com.hawk.game.module.spacemecha.SpaceMechaService;
import com.hawk.game.module.spacemecha.MechaSpaceConst;
import com.hawk.game.module.spacemecha.MechaSpaceConst.SpacePointIndex;
import com.hawk.game.module.spacemecha.config.SpaceMechaCabinCfg;
import com.hawk.game.module.spacemecha.config.SpaceMechaConstCfg;
import com.hawk.game.module.spacemecha.config.SpaceMechaEnemyCfg;
import com.hawk.game.module.spacemecha.worldmarch.SpaceMechaMonsterAttackMarch;
import com.hawk.game.module.spacemecha.worldpoint.SpaceWorldPoint;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.NPCHeroFactory;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.protocol.Activity.GuardRecordPB;
import com.hawk.game.protocol.Activity.GuardRecordStagePB;
import com.hawk.game.protocol.Activity.MechaSpaceGuardResult;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.SpaceMecha.EnemyAtkInfo;
import com.hawk.game.protocol.SpaceMecha.MechaSpacePB;
import com.hawk.game.protocol.SpaceMecha.MechaSpaceType;
import com.hawk.game.protocol.SpaceMecha.SpaceMechaInfoPB;
import com.hawk.game.protocol.SpaceMecha.SpaceMechaStage;
import com.hawk.game.protocol.SpaceMecha.Stage3DetailPB;
import com.hawk.game.protocol.World.MonsterType;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldPointService;

/**
 * 防守阶段3
 * 
 * 1.前一阶段未被击破的敌军据点消失，在原位置刷出普通敌军攻击主舱
 * 2.刷出BOSS，进攻主舱，刷出规则同据点，刷出后即显示对防守机甲的行军线
 * 3.当成功防守最终boss后：
 *   - 参与防守的玩家，播放防守成功动效；  
 *   - 防守成功后，舱体消失，部队回退至主城； 
 *   - 地上爆出随机数量宝箱，刷新方式同据点
 * 4.倒计时结束，成功防守BOSS，则继续进入防守阶段4
 * 
 * @author lating
 *
 */
public class SpaceGuard3Stage extends ISpaceMechaStage {
	
	private boolean pushMonster;
	
	private Map<Integer, Integer> enemyMap = new HashMap<>();
	
	private long firstWaveMarchReachTime;
	
	/**
	 * 记录信息
	 */
	GuardRecordStagePB.Builder stageBuilder;
	
	public SpaceGuard3Stage(String guildId) {
		super(guildId, SpaceMechaStage.SPACE_GUARD_3_VALUE);
	}

	@Override
	public boolean onTick() {
		if (checkMainSpaceDefenceFailed()) {
			return false;
		}
		
		long timeNow = HawkTime.getMillisecond();
		if (timeNow >= this.getEndTime()) {
			stageEnd();
		} else {
			SpaceMechaConstCfg cfg = HawkConfigManager.getInstance().getKVInstance(SpaceMechaConstCfg.class);
			if (!pushMonster && timeNow - this.getStartTime() >= cfg.getBossMarchTime()) {
				pushMonster = true;
				pushMonsterMarch();
			}
		}
		
		return true;
	}
	
	@Override
	public SpaceMechaStage getStageVal() {
		return SpaceMechaStage.SPACE_GUARD_3;
	}
	
	/**
	 * 阶段初始化
	 * 
	 * @param preStage
	 */
	public void init(SpaceGuard2Stage preStage) {
		recordInfo();
		HawkLog.logPrintln("spaceMecha SPACE_GUARD_3 stage init, guildId: {}", getGuildId());
		stageChangeSync(NoticeCfgId.SPACE_MECHA_STAGE_3_START);
		// 倒计时结束时，未被击破的据点，将在阶段3消失并生成1支普通敌军进攻主舱
		MechaSpaceInfo obj = getGuildSpaceInfoObj();
		firstWaveMarchReachTime = preStage.pushLastWaveMonster(obj);
		// 刷出boss点
		createBossPoint();
	}
	
	/**
	 * 阶段结束处理
	 */
	public void stageEnd() {
		try {
			defSuccessHandle();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		updateAndRecord(false);
		MechaSpaceInfo spaceObj = getGuildSpaceInfoObj();
		// 记录阶段变化
		SpaceMechaService.getInstance().logSpaceMechaStageEnd(spaceObj, HawkTime.getMillisecond() - this.getStartTime());
		HawkLog.logPrintln("spaceMecha SPACE_GUARD_3 stage end, guildId: {}, enemy points: {}", getGuildId(), spaceObj.getEnemyPointIds());
		// 行军遣返
		spaceObj.forceAllSpaceMarchBack();
		forceMarchBack(spaceObj.getSpaceWorldPoint(SpacePointIndex.MAIN_SPACE));
		// 是否要移除世界点
		spaceObj.clearPoint();
		
		SpaceGuard4Stage stage = new SpaceGuard4Stage(spaceObj.getGuildId()); 
		spaceObj.setStage(stage);
		stage.init();
	}
	
	/**
	 * 行军遣返
	 * @param worldPoint
	 */
	private void forceMarchBack(WorldPoint worldPoint) {
		Collection<IWorldMarch> marchs = WorldMarchService.getInstance().getWorldPointMarch(worldPoint.getId());
		for (IWorldMarch march : marchs) {	
			if (march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
				continue;
			}
			
			try {
				Set<IWorldMarch> list = WorldMarchService.getInstance().getMassJoinMarchs(march, true);
				for (IWorldMarch march1 : list) {
					WorldMarchService.getInstance().onPlayerNoneAction(march1, HawkTime.getMillisecond());
				}
				
				if (march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
					WorldMarchService.getInstance().onMarchReturnImmediately(march, march.getArmys());
				} else {
					WorldMarchService.getInstance().onPlayerNoneAction(march, HawkTime.getMillisecond());
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	/**
	 * 刷出boss
	 */
	private void createBossPoint() {
		MechaSpaceInfo obj = getGuildSpaceInfoObj();
		HawkLog.debugPrintln("spaceMecha SPACE_GUARD_3 stage create enemy point before, guildId: {}, enemy points: {}", getGuildId(), obj.getEnemyPointIds());
		obj.getEnemyPointIds().stream().forEach(e -> WorldPointService.getInstance().removeWorldPoint(e, true));
		obj.getEnemyPointIds().clear();
		
		int[] xy = GameUtil.splitXAndY(obj.getSpacePointId(SpacePointIndex.MAIN_SPACE));
		WorldPoint point = SpaceMechaService.getInstance().createMonster(xy[0], xy[1], getGuildId(), MonsterType.TYPE_15_VALUE, 0, WorldPointType.SPACE_MECHA_MONSTER_VALUE);
		obj.addEnemyPointId(point.getId());
		enemyMap.put(point.getId(), point.getMonsterId());
		
		HawkLog.logPrintln("spaceMecha SPACE_GUARD_3 stage create enemy point after, guildId: {}, enemy points: {}", getGuildId(), obj.getEnemyPointIds());
	}
	
	/**
	 * boss发起行军
	 */
	private void pushMonsterMarch(){
		MechaSpaceInfo obj = getGuildSpaceInfoObj();
		for (int pointId : obj.getEnemyPointIds()) {
			WorldPoint point = WorldPointService.getInstance().getWorldPoint(pointId);
			if (point.getPointType() != WorldPointType.SPACE_MECHA_MONSTER_VALUE) {
				HawkLog.logPrintln("spaceMecha SPACE_GUARD_3 stage push march, pointType error, guildId: {}, pointId: {}, pointType: {}", getGuildId(), pointId, point.getPointType()); 
				continue;
			}
			
			SpaceMechaEnemyCfg monsterCfg = HawkConfigManager.getInstance().getConfigByKey(SpaceMechaEnemyCfg.class, point.getMonsterId());
			if(monsterCfg == null || monsterCfg.getAttackAim() != MechaSpaceType.MAIN_SPACE_VALUE){
				HawkLog.logPrintln("spaceMecha SPACE_GUARD_3 stage push march, monsterCfg error, guildId: {}, pointId: {}, monsterId: {}", getGuildId(), pointId, point.getMonsterId());
				continue;
			}
			
			try {
				int subVal = obj.getSpaceEffVal(EffType.SPACE_MECHA_BOSS_SOLDIER_SUB_VALUE);
				int addVal = obj.getBossEffVal(EffType.SPACE_MECHA_BOSS_SOLDIER_ADD_VALUE);
				List<ArmyInfo> armyList = monsterCfg.getArmyList();
				if (subVal > 0) {
					armyList.forEach(e -> e.setTotalCount(e.getTotalCount() - (int)(subVal * 1D / 10000 * e.getTotalCount())));
				} else if (addVal > 0) {
					armyList.forEach(e -> e.setTotalCount(e.getTotalCount() + (int)(addVal * 1D / 10000 * e.getTotalCount())));
				}
				
				NpcPlayer npcPlayer = new NpcPlayer(HawkXID.nullXid());
				npcPlayer.setPlayerId(SpaceMechaStage.SPACE_GUARD_3_VALUE + "_" + point.getX() + "_" + point.getY());
				npcPlayer.setPfIcon(monsterCfg.getIcon());
				npcPlayer.setName(monsterCfg.getName());
				List<PlayerHero> heroList = new ArrayList<>();
				monsterCfg.getHeroIdList().stream().forEach(heroId -> heroList.add(NPCHeroFactory.getInstance().get(heroId)));
				npcPlayer.setHeros(heroList);
				obj.getSpaceWorldPoint(SpacePointIndex.MAIN_SPACE).addNpcPlayer(pointId, npcPlayer);
				
				//发起行军开始
				SpaceMechaMonsterAttackMarch march = (SpaceMechaMonsterAttackMarch) WorldMarchService.getInstance().startMonsterMarch(obj.getGuildId(), WorldMarchType.SPACE_MECHA_MONSTER_ATTACK_MARCH_VALUE, 
						pointId, obj.getSpacePointId(SpacePointIndex.MAIN_SPACE), null, obj.getGuildId(), armyList, monsterCfg.getHeroIdList(), false);
				WorldMarchService.getInstance().addGuildMarch(march);
				HawkLog.logPrintln("spaceMecha SPACE_GUARD_3 stage push march, guildId: {}, monsterId: {}, marchId: {}, npcPlayer: {}, posX: {}, posY: {}, subVal: {}, addVal: {}, armyList: {}, hero: {}", 
						getGuildId(), point.getMonsterId(), march.getMarchId(), npcPlayer.getId(), march.getOrigionX(), march.getOrigionY(), subVal, addVal, armyList, monsterCfg.getHeroIdList());
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			
			WorldPointService.getInstance().removeWorldPoint(pointId, true);
		}
	}
	
	/**
	 * 防守成功处理
	 */
	private void defSuccessHandle() {
		MechaSpaceInfo obj = getGuildSpaceInfoObj();
		HawkLog.logPrintln("spaceMecha SPACE_GUARD_3 stage defence win, guildId: {}, space maxLevel before: {}", getGuildId(), obj.getMaxLevel());
		// 难度结算
		int maxLevel = Math.max(obj.getLevel() + 1, obj.getMaxLevel());
		obj.setNewMaxLevel(maxLevel);

		SpaceWorldPoint spacePoint = obj.getSpaceWorldPoint(SpacePointIndex.MAIN_SPACE);
		SpaceMechaCabinCfg cfg = SpaceMechaCabinCfg.getCfgByLevel(obj.getLevel());
		// 发通知
		Object[] objects = new Object[] { spacePoint.getX(), spacePoint.getY(), cfg.getId() };
		ChatService.getInstance().addWorldBroadcastMsg(ChatParames.newBuilder().setChatType(ChatType.CHAT_ALLIANCE).setKey(NoticeCfgId.SPACE_MECHA_MAIN_DEF_WIN_LAST).setGuildId(obj.getGuildId()).addParms(objects).build());

		SpaceMechaConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(SpaceMechaConstCfg.class);
		Object[] object = new Object[] { obj.getLevel() };
		// 发参与奖（参与主舱防守成员）
		for (String playerId : spacePoint.getDefenceMembers()) {
			HawkLog.debugPrintln("spaceMecha SPACE_GUARD_3 stage defence win participate award, guildId: {}, playerId: {}, space maxLevel after: {}", getGuildId(), playerId, obj.getMaxLevel());
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			if (player != null) {
				CustomDataEntity customData = SpaceMechaService.getInstance().getCustomDataEntity(player, MechaSpaceConst.PERSONAL_MAINSPACE_PARTI_AWARD_TOTAL);
				if (customData.getValue() >= constCfg.getCabinPartiAwardPersonLimit()) {
					continue;
				}
				customData.setValue(customData.getValue() + 1);
			}
			
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
			        .setPlayerId(playerId)
			        .setMailId(MailId.SPACE_MECHA_WIN_REWARD_MEMBER)
			        .addContents(object)
			        .setAwardStatus(MailRewardStatus.NOT_GET)
			        .setRewards(cfg.getWinPartiAward())
			        .build());
		}
		
		// 防守成功全员邮件
		for (String memberId : GuildService.getInstance().getGuildMembers(obj.getGuildId())) {
			HawkLog.debugPrintln("spaceMecha SPACE_GUARD_3 stage defence win all award, guildId: {}, playerId: {}, space maxLevel after: {}", getGuildId(), memberId, obj.getMaxLevel());
			Player player = GlobalData.getInstance().makesurePlayer(memberId);
			if (player != null) {
				CustomDataEntity customData = SpaceMechaService.getInstance().getCustomDataEntity(player, MechaSpaceConst.PERSONAL_MAINSPACE_ALL_AWARD_TOTAL);
				if (customData.getValue() >= constCfg.getCabinwinAwardPersonLimit()) {
					continue;
				}
				customData.setValue(customData.getValue() + 1);
			}
			
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(memberId)
					.setMailId(MailId.SPACE_MECHA_WIN_REWARD_ALL)
					.setRewards(cfg.getWinAward())
					.addContents(object)
					.setAwardStatus(MailRewardStatus.NOT_GET)
					.build());
		}
	}
	
	@Override
	public void buildSpacePointInfo(SpaceWorldPoint spacePoint, MechaSpacePB.Builder builder) {
		if (spacePoint.getSpaceIndex() != SpacePointIndex.MAIN_SPACE) {
			return;
		}
		
		if (firstWaveMarchReachTime > HawkApp.getInstance().getCurrentTime()) {
			builder.setNearestTime(firstWaveMarchReachTime);
			return;
		}
		
		SpaceMechaConstCfg cfg = HawkConfigManager.getInstance().getKVInstance(SpaceMechaConstCfg.class);
		long reachTime = this.getStartTime() + cfg.getBossMarchTime() + cfg.getEnemyMarchTime();
		if (reachTime > HawkTime.getMillisecond()) {
			builder.setNearestTime(reachTime);
		}
	}

	@Override
	public void buildStageInfo(SpaceMechaInfoPB.Builder builder) {
		MechaSpaceInfo obj = getGuildSpaceInfoObj();
		Stage3DetailPB.Builder detailBuilder = Stage3DetailPB.newBuilder();
		SpaceMechaConstCfg cfg = HawkConfigManager.getInstance().getKVInstance(SpaceMechaConstCfg.class);
		long reachTime = this.getStartTime() + cfg.getBossMarchTime() + cfg.getEnemyMarchTime();
		detailBuilder.setReachTime(reachTime);
		detailBuilder.setBroken(obj.getSpStrongHoldBroken());
		detailBuilder.setStrongHoldId(obj.getSpStrongHoldCfgId());
		for (Entry<Integer, Integer> entry : enemyMap.entrySet()) {
			EnemyAtkInfo.Builder enemyBuilder = buildEnemyAtkInfo(entry.getKey(), entry.getValue(), reachTime);
			detailBuilder.addMainSpaceEnemy(enemyBuilder);
		}
		
		builder.setStage3Detail(detailBuilder);
	}

	private void recordInfo() {
		MechaSpaceInfo obj = getGuildSpaceInfoObj();
		stageBuilder = GuardRecordStagePB.newBuilder();
		stageBuilder.setStage(SpaceMechaStage.SPACE_GUARD_3_VALUE);
		stageBuilder.setGuardResult(0);
		stageBuilder.setMainSpaceVal(obj.getSpaceBlood(SpacePointIndex.MAIN_SPACE));
	}

	@Override
	public void updateRecord() {
		MechaSpaceInfo obj = getGuildSpaceInfoObj();
		int mainSpaceBlood = obj.getSpaceBlood(SpacePointIndex.MAIN_SPACE);
		stageBuilder.setGuardResult(mainSpaceBlood > 0 ? 0 : 1);
		stageBuilder.setMainSpaceVal(mainSpaceBlood);
	}

	@Override
	public void updateAndRecord(boolean flush) {
		updateAndRecord(flush, null);
	}
	
	@Override
	public void updateAndRecord(boolean flush, MechaSpaceGuardResult result) {
		try {
			MechaSpaceInfo obj = getGuildSpaceInfoObj();
			int mainSpaceBlood = obj.getSpaceBlood(SpacePointIndex.MAIN_SPACE);
			if (result == null) {
				result = mainSpaceBlood > 0 ? MechaSpaceGuardResult.SPACE_GUARD_SUCC : MechaSpaceGuardResult.SPACE_GUARD_FAILED;
			}
			stageBuilder.setGuardResult(result.getNumber());
			stageBuilder.setMainSpaceVal(mainSpaceBlood);
			GuardRecordPB.Builder builder = SpaceMechaService.getInstance().getLatestSpaceRecord(obj.getGuildId());
			builder.addStageInfo(stageBuilder);
			if (flush) {
				builder.setGuardResult(stageBuilder.getGuardResult());
				SpaceMechaService.getInstance().flushSpaceRecordToRedis(obj.getGuildId());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
}
