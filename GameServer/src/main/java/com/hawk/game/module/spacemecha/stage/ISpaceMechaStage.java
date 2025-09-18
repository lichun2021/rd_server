package com.hawk.game.module.spacemecha.stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import com.hawk.game.module.spacemecha.MechaSpaceConst.SpacePointIndex;
import com.hawk.game.battle.NpcPlayer;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.spacemecha.MechaSpaceInfo;
import com.hawk.game.module.spacemecha.SpaceMechaService;
import com.hawk.game.protocol.Activity.MechaSpaceGuardResult;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.SpaceMecha.EnemyAtkInfo;
import com.hawk.game.protocol.SpaceMecha.MechaSpacePB;
import com.hawk.game.protocol.SpaceMecha.SpaceMechaInfoPB;
import com.hawk.game.protocol.SpaceMecha.SpaceMechaStage;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.module.spacemecha.config.SpaceMechaEnemyCfg;
import com.hawk.game.module.spacemecha.config.SpaceMechaStageCfg;
import com.hawk.game.module.spacemecha.worldpoint.SpaceWorldPoint;
import com.hawk.game.player.hero.NPCHero;
import com.hawk.game.player.hero.NPCHeroFactory;

/**
 * 星甲召唤舱体阶段抽象类
 * 
 *  lating
 */
public abstract class ISpaceMechaStage {
	/**
	 * 本阶段开始时间
	 */
	private long startTime;
	/**
	 * 舱体所属联盟
	 */
	private String guildId;
	/**
	 * 阶段持续时长
	 */
	private long period;
	
	/**
	 * 构造函数
	 * @param guildId
	 * @param stage
	 */
	public ISpaceMechaStage(String guildId, int stage) {
		this.startTime = HawkTime.getMillisecond();
		this.guildId = guildId;
		SpaceMechaStageCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SpaceMechaStageCfg.class, stage);
		period = cfg != null ? cfg.getTimeLong() : 300000L;
	}
	
	/**
	 * 获取当前阶段的起始时间
	 * @return
	 */
	public long getStartTime() {
		return startTime;
	}
	
	/**
	 * 获取当前舱体所属联盟ID
	 * @return
	 */
	public String getGuildId() {
		return guildId;
	}
	
	/**
	 * 获取当前阶段的时长
	 * @return
	 */
	public long getPeriod() {
		return period;
	}
	
	/**
	 * 获取阶段内敌军进攻波次
	 * @return
	 */
	public int getRound() {
		return 1;
	}
	
	/**
	 * 获取当前阶段的结束时间
	 * @return
	 */
	public long getEndTime() {
		return this.startTime + this.period;
	}
	
	/**
	 * 获取舱体信息
	 * @return
	 */
	public MechaSpaceInfo getGuildSpaceInfoObj() {
		return SpaceMechaService.getInstance().getGuildSpace(guildId);
	}
	
	/**
	 * 阶段变更同步
	 * @param stage
	 */
	protected void stageChangeSync(NoticeCfgId key) {
		MechaSpaceInfo spaceObj = getGuildSpaceInfoObj();
		int[] pos = GameUtil.splitXAndY(spaceObj.getSpacePointId(SpacePointIndex.MAIN_SPACE));
		int stage = getStageVal().getNumber();
		Object[] objects = new Object[] { pos[0], pos[1], stage };
		ChatService.getInstance().addWorldBroadcastMsg(ChatParames.newBuilder().setChatType(ChatType.CHAT_ALLIANCE).setKey(key).setGuildId(getGuildId()).addParms(objects).build());
		//ChatService.getInstance().addWorldBroadcastMsg(ChatParames.newBuilder().setChatType(ChatType.SYS_BROADCAST).setKey(NoticeCfgId.SPACE_MECHA_STAGE_CHANGE_PERSON).addParms(objects).build());
		spaceObj.syncSpaceMechaInfo();
	}
	
	/**
	 * 判断主舱防守失败
	 * 
	 * @return
	 */
	public boolean checkMainSpaceDefenceFailed() {
		MechaSpaceInfo spaceObj = getGuildSpaceInfoObj();
		SpaceWorldPoint spacePoint = spaceObj.getSpaceWorldPoint(SpacePointIndex.MAIN_SPACE);
		boolean failed = spacePoint.getSpaceBlood() <= 0;
		if (failed) {
			HawkLog.logPrintln("spaceMecha mainspace defence failed, guildId: {}, stage: {}", spaceObj.getGuildId(), getStageVal());
			// 行军遣返
			spaceObj.forceAllSpaceMarchBack();
			dismissFutureMarch(spaceObj);
			// 是否要移除世界点
			spaceObj.clearPoint();
			updateAndRecord(true);
			spaceObj.setStage(null);
			spaceObj.syncSpaceMechaInfo();
		}
		return failed;
	}
	
	/**
	 * 解散路上的行军
	 */
	private void dismissFutureMarch(MechaSpaceInfo spaceObj) {
		try {
			SpaceWorldPoint spacePoint = spaceObj.getSpaceWorldPoint(SpacePointIndex.MAIN_SPACE);
			List<IWorldMarch> marchs = new ArrayList<IWorldMarch>(WorldMarchService.getInstance().getWorldPointMarch(spacePoint.getId()));
			spacePoint = spaceObj.getSpaceWorldPoint(SpacePointIndex.SUB_SPACE_1);
			marchs.addAll(WorldMarchService.getInstance().getWorldPointMarch(spacePoint.getId()));
			spacePoint = spaceObj.getSpaceWorldPoint(SpacePointIndex.SUB_SPACE_2);
			marchs.addAll(WorldMarchService.getInstance().getWorldPointMarch(spacePoint.getId()));
			for (IWorldMarch march : marchs) {	
				if(march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
					continue;
				}
				
				// 处理集结的行军
				if (march.getMarchType() == WorldMarchType.SPACE_MECHA_MAIN_MARCH_MASS) {
					Set<IWorldMarch> list = WorldMarchService.getInstance().getMassJoinMarchs(march, true);
					for (IWorldMarch march1 : list) {
						WorldMarchService.getInstance().onPlayerNoneAction(march1, HawkTime.getMillisecond());
					}
					
					if (march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
						WorldMarchService.getInstance().onMarchReturnImmediately(march, march.getArmys());
					} else {
						WorldMarchService.getInstance().onPlayerNoneAction(march, HawkTime.getMillisecond());
					}
				} else {
					// 非集结行军
					WorldMarchService.getInstance().onPlayerNoneAction(march, HawkTime.getMillisecond());
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 构建敌军信息
	 * 
	 * @param pointId
	 * @param enemyId
	 * @param reachTime
	 * @return
	 */
	protected EnemyAtkInfo.Builder buildEnemyAtkInfo(int pointId, int enemyId, long reachTime) {
		EnemyAtkInfo.Builder enemyBuilder = EnemyAtkInfo.newBuilder();
		if (pointId > 0) {
			int[] xy = GameUtil.splitXAndY(pointId);
			enemyBuilder.setSourceX(xy[0]);
			enemyBuilder.setSourceY(xy[1]);
		}
		enemyBuilder.setEnemyId(enemyId);
		enemyBuilder.setReachTime(reachTime);
		
		buildEnemyInfo(enemyId, enemyBuilder);
		
		return enemyBuilder;
	}
	
	/**
	 * 构建敌军信息
	 * 
	 * @param enemyId
	 * @param enemyBuilder
	 */
	protected void buildEnemyInfo(int enemyId, EnemyAtkInfo.Builder enemyBuilder) {
		SpaceMechaEnemyCfg enemyCfg = HawkConfigManager.getInstance().getConfigByKey(SpaceMechaEnemyCfg.class, enemyId);
		//组装部队信息
		NpcPlayer npcPlayer = SpaceMechaService.getInstance().getNpcPlayer(enemyCfg);
		List<ArmyInfo> armylist = enemyCfg.getArmyList();
		for (ArmyInfo armyInfo : armylist) {
			enemyBuilder.addArmyInfo(armyInfo.toArmySoldierPB(npcPlayer));
		}
		
		//组装英雄信息
		List<Integer> heroInfoIds = enemyCfg.getHeroIdList();
		for (Integer heroInfoId : heroInfoIds) {
			NPCHero npcHero = NPCHeroFactory.getInstance().get(heroInfoId);
			enemyBuilder.addHeros(npcHero.toPBobj());
		}
	}
	
	/**
	 * 对阶段进行周期tick
	 * @return
	 */
	public abstract boolean onTick();
	
	/**
	 * 获取阶段对应的阶段值
	 * @return
	 */
	public abstract SpaceMechaStage getStageVal();
	
	/**
	 * 阶段结束处理
	 */
	public abstract void stageEnd();
	
	/**
	 * 构建星甲召唤舱体阶段相关的信息
	 * @param builder
	 */
	public abstract void buildStageInfo(SpaceMechaInfoPB.Builder builder);
	
	/**
	 * 构建舱体世界点针对阶段相关的信息
	 * @param spacePoint
	 * @param builder
	 */
	public abstract void buildSpacePointInfo(SpaceWorldPoint spacePoint, MechaSpacePB.Builder builder);
	
	/**
	 * 更新阶段记录信息（只更新不入库，敌军到达主舱血量变化时调用）
	 */
	public abstract void updateRecord();
	
	/**
	 * 更新阶段信息并入库（是否入库取决于flush参数，只有在停服、主舱最终防守失败、主舱最终防守成功舱体结束时才存redis）
	 * 
	 * @param flush
	 */
	public abstract void updateAndRecord(boolean flush);
	public abstract void updateAndRecord(boolean flush, MechaSpaceGuardResult result);
	
}
