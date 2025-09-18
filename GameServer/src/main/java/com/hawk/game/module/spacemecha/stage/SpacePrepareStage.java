package com.hawk.game.module.spacemecha.stage;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkTime;
import com.hawk.game.module.spacemecha.MechaSpaceInfo;
import com.hawk.game.module.spacemecha.MechaSpaceConst.SpacePointIndex;
import com.hawk.game.module.spacemecha.config.SpaceMechaConstCfg;
import com.hawk.game.module.spacemecha.config.SpaceMechaLevelCfg;
import com.hawk.game.module.spacemecha.worldpoint.SpaceWorldPoint;
import com.hawk.game.protocol.SpaceMecha.SpaceMechaInfoPB.Builder;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.protocol.Activity.MechaSpaceGuardResult;
import com.hawk.game.protocol.SpaceMecha.EnemyAtkInfo;
import com.hawk.game.protocol.SpaceMecha.MechaSpacePB;
import com.hawk.game.protocol.SpaceMecha.SpaceMechaStage;
import com.hawk.game.protocol.SpaceMecha.Stage1DetailPB;
import com.hawk.game.protocol.SpaceMecha.WaveEnemyAtkInfo;

/**
 * 预热阶段
 * 
 * 1.主舱可驻守（单人、集结），每人上限1队
 * 2.子舱可驻守（单人、集结），每个子舱每人上限1队，每个子舱分别最多容纳N名成员
 * 3.刷出阶段1首波敌军，并显示预进攻行军线
 * 
 * @author lating
 *
 */
public class SpacePrepareStage extends ISpaceMechaStage {
	
	public SpacePrepareStage(String guildId) {
		super(guildId, SpaceMechaStage.SPACE_PREPARE_VALUE);
	}

	@Override
	public boolean onTick() {
		long timeNow = HawkTime.getMillisecond();
		if (timeNow >= this.getEndTime()) {
			stageEnd(timeNow);
		}
		return true;
	}

	@Override
	public SpaceMechaStage getStageVal() {
		return SpaceMechaStage.SPACE_PREPARE;
	}
	
	public void stageEnd() {
		stageEnd(HawkTime.getMillisecond());
	}
	
	/**
	 * 阶段结束处理
	 * @param timeNow
	 */
	private void stageEnd(long timeNow) {
		HawkLog.logPrintln("spaceMecha prepare stage end, guildId: {}", getGuildId());
		MechaSpaceInfo obj = getGuildSpaceInfoObj();
		SpaceGuard1Stage stage = new SpaceGuard1Stage(obj.getGuildId());
		// 阶段自动切换
		obj.setStage(stage); 
		stage.init(timeNow);
	}
	
	@Override
	public void updateRecord() {
	}

	@Override
	public void updateAndRecord(boolean flush) {
	}
	
	@Override
	public void updateAndRecord(boolean flush, MechaSpaceGuardResult result) {
	}
	
	@Override
	public void buildSpacePointInfo(SpaceWorldPoint spacePoint, MechaSpacePB.Builder builder) {
		SpaceMechaConstCfg cfg = HawkConfigManager.getInstance().getKVInstance(SpaceMechaConstCfg.class);
		long marchTime = cfg.getEnemyMarchTime();		
		if (spacePoint.getSpaceIndex() == SpacePointIndex.MAIN_SPACE) {
			long spaceMainStartTime = this.getEndTime() + cfg.getCabinFirstWaveTime();
			long reachTime = spaceMainStartTime + marchTime;
			builder.setNearestTime(reachTime);
		} else {
			SpaceMechaLevelCfg levelCfg = HawkConfigManager.getInstance().getConfigByKey(SpaceMechaLevelCfg.class, spacePoint.getSpaceLevel());
			long wavePushMarchCd = levelCfg.getStage1WaveCd();
			long createMonsterCd = levelCfg.getStage1RefreshCd();
			long monsterMarchTime = cfg.getEnemyMarchTime();
			long roundWholeTime = wavePushMarchCd + monsterMarchTime + createMonsterCd;
			long startTime = this.getEndTime() + cfg.getSubcabinFirstWaveTime();
			if (levelCfg.getSpEnemyWaveMin() > 0 && cfg.getSubcabinFirstWaveTime() <= 0) {
				startTime = this.getEndTime() + (levelCfg.getSpEnemyWaveMin() - 1) * roundWholeTime + wavePushMarchCd;
			}
			long reachTime = startTime + marchTime;
			builder.setNearestTime(reachTime);
		}
	}

	@Override
	public void buildStageInfo(Builder builder) {
		SpaceMechaConstCfg cfg = HawkConfigManager.getInstance().getKVInstance(SpaceMechaConstCfg.class);
		long spaceMainStartTime = this.getEndTime() + cfg.getCabinFirstWaveTime();
		long marchTime = cfg.getEnemyMarchTime();		
		long reachTime = spaceMainStartTime + marchTime;
		MechaSpaceInfo spaceObj = getGuildSpaceInfoObj();
		
		Stage1DetailPB.Builder detailBuilder = Stage1DetailPB.newBuilder();
		detailBuilder.setWave(1);
		WaveEnemyAtkInfo.Builder waveEnemyBuilder = WaveEnemyAtkInfo.newBuilder();
		waveEnemyBuilder.setWave(1);
		waveEnemyBuilder.setReachTime(reachTime);
		for (int pointId : spaceObj.getEnemyPointIds()) {
			WorldPoint point = WorldPointService.getInstance().getWorldPoint(pointId);
			if (point == null) {
				continue;
			} 
			
			EnemyAtkInfo.Builder enemyBuilder = buildEnemyAtkInfo(pointId, point.getMonsterId(), reachTime);
			waveEnemyBuilder.addMainSpaceEnemy(enemyBuilder);
		}
		
		detailBuilder.addWaveEnemy(waveEnemyBuilder);
		builder.setPrepareDetail(detailBuilder);
	}

}
