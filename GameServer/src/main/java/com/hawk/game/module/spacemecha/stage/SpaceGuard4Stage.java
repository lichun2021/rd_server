package com.hawk.game.module.spacemecha.stage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;

import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.module.spacemecha.MechaSpaceInfo;
import com.hawk.game.module.spacemecha.SpaceMechaService;
import com.hawk.game.module.spacemecha.MechaSpaceConst.SpaceMechaGrid;
import com.hawk.game.module.spacemecha.MechaSpaceConst.SpacePointIndex;
import com.hawk.game.module.spacemecha.config.SpaceMechaBoxCfg;
import com.hawk.game.module.spacemecha.config.SpaceMechaConstCfg;
import com.hawk.game.module.spacemecha.config.SpaceMechaLevelCfg;
import com.hawk.game.module.spacemecha.worldpoint.MechaBoxWorldPoint;
import com.hawk.game.module.spacemecha.worldpoint.SpaceWorldPoint;
import com.hawk.game.protocol.Activity.GuardRecordPB;
import com.hawk.game.protocol.Activity.GuardRecordStagePB;
import com.hawk.game.protocol.Activity.MechaSpaceGuardResult;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.SpaceMecha.MechaBoxPB;
import com.hawk.game.protocol.SpaceMecha.MechaSpacePB.Builder;
import com.hawk.game.protocol.SpaceMecha.SpaceMechaInfoPB;
import com.hawk.game.protocol.SpaceMecha.SpaceMechaStage;
import com.hawk.game.protocol.SpaceMecha.Stage4DetailPB;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.object.AreaObject;
import com.hawk.game.world.object.Point;
import com.hawk.game.world.service.WorldPointService;

/**
 * 防守阶段4
 * 
 * 1.爆出的随机数量宝箱可以通过采集获取
 * 2.阶段4倒计时结束后，本轮玩法完全结束，主界面活动板消失
 * 
 * @author lating
 *
 */
public class SpaceGuard4Stage extends ISpaceMechaStage {
	/**
	 * 宝箱点
	 */
	private List<Integer> boxPointList = new ArrayList<>();
	/**
	 * 记录信息
	 */
	GuardRecordStagePB.Builder stageBuilder;
	
	public SpaceGuard4Stage(String guildId) {
		super(guildId, SpaceMechaStage.SPACE_GUARD_4_VALUE);
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
		return SpaceMechaStage.SPACE_GUARD_4;
	}
	
	/**
	 * 阶段初始化
	 */
	public void init() {
		recordInfo();
		MechaSpaceInfo obj = getGuildSpaceInfoObj();
		HawkLog.logPrintln("spaceMecha SPACE_GUARD_4 stage init, guildId: {}, enemyPoint: {}", getGuildId(), obj.getEnemyPointIds());
		// 阶段3倒计时结束，boss防守成功即刷新出宝箱，刷新后再继续进入防守阶段4
		dropBox();
		// 联盟发送消息，提醒阶段4开始
		stageChangeSync(NoticeCfgId.SPACE_MECHA_STAGE_4_START);
	}
	
	public void stageEnd() {
		stageEnd(HawkTime.getMillisecond());
	}
	
	/**
	 * 阶段结束处理
	 */
	private void stageEnd(long timeNow) {
		updateAndRecord(true);
		MechaSpaceInfo spaceObj = getGuildSpaceInfoObj();
		// 记录阶段变化
		SpaceMechaService.getInstance().logSpaceMechaStageEnd(spaceObj, timeNow - this.getStartTime());
		HawkLog.logPrintln("spaceMecha SPACE_GUARD_4 stage end, guildId: {}, enemyPoint: {}", getGuildId(), spaceObj.getEnemyPointIds());

		forcdMarchBack(spaceObj, timeNow);
		spaceObj.setMaxLevel(Math.max(spaceObj.getMaxLevel(), spaceObj.getNewMaxLevel()));
		spaceObj.setStage(null);
		spaceObj.syncSpaceMechaInfo();
	}
	
	/**
	 * 行军遣返
	 * @param spaceObj
	 * @param time
	 */
	private void forcdMarchBack(MechaSpaceInfo spaceObj, long time) {
		for (int pointId : spaceObj.getBoxPointIdSet()) {
			try {
				Collection<IWorldMarch> marchs = WorldMarchService.getInstance().getWorldPointMarch(pointId);
				if (marchs.isEmpty()) {
					spaceObj.removeBoxPoint(pointId);
					WorldPointService.getInstance().removeWorldPoint(pointId, true);
					continue;
				}
				
				for (IWorldMarch march : marchs) {
					// 对于已经在点上的行军，需要直接给玩家发奖励
					SpaceMechaBoxCfg boxCfg = HawkConfigManager.getInstance().getConfigByKey(SpaceMechaBoxCfg.class, Integer.parseInt(march.getMarchEntity().getTargetId()));
					if (march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_COLLECT_VALUE) {
						AwardItems tmpAward = AwardItems.valueOf();
						tmpAward.addItemInfos(ItemInfo.valueListOf(boxCfg.getReward()));
						SystemMailService.getInstance().sendMail(MailParames.newBuilder()
								.setPlayerId(march.getPlayerId())
								.setMailId(MailId.SPACE_MECHA_BOX_COLLECT_AWARD) 
								.setAwardStatus(MailRewardStatus.NOT_GET)
								.addContents(spaceObj.getLevel())
								.addRewards(tmpAward.getAwardItems())
								.build());
					} else if (march.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
						SystemMailService.getInstance().sendMail(MailParames.newBuilder()
								.setPlayerId(march.getPlayerId())
								.setMailId(MailId.SPACE_MECHA_BOX_DISPEAR) 
								.addContents(new Object[] { boxCfg.getLevel(), boxCfg.getId() })
								.build());
					}
					WorldMarchService.getInstance().onPlayerNoneAction(march, time);
				}
				
				spaceObj.removeBoxPoint(pointId);
				WorldPointService.getInstance().removeWorldPoint(pointId, true);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	/**
	 * 掉落宝箱
	 */
	private void dropBox() {
		MechaSpaceInfo spaceObj = getGuildSpaceInfoObj();
		int[] xy = GameUtil.splitXAndY(spaceObj.getSpacePointId(SpacePointIndex.MAIN_SPACE));
		int centerX = xy[0], centerY = xy[1];
		List<WorldPoint> createPoints = new ArrayList<>();
		SpaceMechaLevelCfg levelCfg = HawkConfigManager.getInstance().getConfigByKey(SpaceMechaLevelCfg.class, spaceObj.getLevel());
		int[] minMax = levelCfg.getStage4boxNumMinMax();
		int count = HawkRand.randInt(minMax[0], minMax[1]);
		
		final int radiusAddDelta = 10, roundMax = 5;
		SpaceMechaConstCfg cfg = HawkConfigManager.getInstance().getKVInstance(SpaceMechaConstCfg.class);
		int randomRadius = cfg.getMinRefreshDistance();
		int round = 0;
		do {
			round++;
			try {
				List<Point> pointList = WorldPointService.getInstance().getRhoAroundPointsFree(centerX, centerY, randomRadius);
				randomRadius += radiusAddDelta;
				List<Point> points = WorldPointService.getInstance().getRhoAroundPointsFree(centerX, centerY, randomRadius);
				points.removeAll(pointList);
				Collections.shuffle(points);
				for (Point point : points) {
					if (createPoints.size() >= count) {
						break;
					}
					if (!point.canSpaceMechaSeat(WorldPointType.SPACE_MECHA_BOX_VALUE)) {
						continue;
					}
					
					AreaObject area = WorldPointService.getInstance().getArea(point.getAreaId());
					if (!WorldPointService.getInstance().tryOccupied(area, point, SpaceMechaGrid.MECHA_BOX)) {
						continue;
					}
					if (WorldPointService.getInstance().getWorldPoint(point.getId()) != null) {
						continue;
					}
					
					int resId = levelCfg.getStage4box();
					// 创建世界点对象
					MechaBoxWorldPoint worldPoint = new MechaBoxWorldPoint(point.getX(), point.getY(), point.getAreaId(), point.getZoneId(), WorldPointType.SPACE_MECHA_BOX_VALUE);
					worldPoint.setResourceId(resId);
					worldPoint.setBoxId(resId);
					worldPoint.setGuildId(getGuildId());
					worldPoint.setLifeStartTime(HawkTime.getMillisecond());
					WorldPointService.getInstance().addPoint(worldPoint);
					createPoints.add(worldPoint);
					spaceObj.addBoxPoint(worldPoint.getId());
					boxPointList.add(worldPoint.getId());
					
					HawkLog.debugPrintln("spaceMecha SPACE_GUARD_4 stage create box point, guildId: {}, posX: {}, posY: {}, resId: {}", getGuildId(), worldPoint.getX(), worldPoint.getY(), worldPoint.getResourceId());
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		} while (round < roundMax && createPoints.size() < count);
	}

	@Override
	public void buildStageInfo(SpaceMechaInfoPB.Builder builder) {
		Stage4DetailPB.Builder detailBuilder = Stage4DetailPB.newBuilder();
		for (int pointId : boxPointList) {
			MechaBoxWorldPoint point = (MechaBoxWorldPoint) WorldPointService.getInstance().getWorldPoint(pointId);
			if (point == null) {
				continue;
			}
			
			MechaBoxPB.Builder boxBuilder = MechaBoxPB.newBuilder();
			boxBuilder.setId(point.getBoxId());
			boxBuilder.setPosX(point.getX());
			boxBuilder.setPosY(point.getY());
			if (HawkOSOperator.isEmptyString(point.getCollectPlayerId())) {
				boxBuilder.setStatus(0);
				detailBuilder.addBox(boxBuilder);
				continue;
			} else {
				boxBuilder.setStatus(1);
				boxBuilder.setPlayerId(point.getCollectPlayerId());
				boxBuilder.setPlayerName(point.getCollectPlayerName());
				boxBuilder.setEndTime(point.getCollectEndTime());
			}
			
			detailBuilder.addBox(boxBuilder);
		}
		
		builder.setStage4Detail(detailBuilder);
	}

	private void recordInfo() {
		MechaSpaceInfo obj = getGuildSpaceInfoObj();
		stageBuilder = GuardRecordStagePB.newBuilder();
		stageBuilder.setStage(SpaceMechaStage.SPACE_GUARD_4_VALUE);
		stageBuilder.setGuardResult(0);
		stageBuilder.setMainSpaceVal(obj.getSpaceBlood(SpacePointIndex.MAIN_SPACE));
	}

	@Override
	public void updateRecord() {
		MechaSpaceInfo obj = getGuildSpaceInfoObj();
		int mainSpaceBlood = obj.getSpaceBlood(SpacePointIndex.MAIN_SPACE);
		stageBuilder.setGuardResult(0);
		stageBuilder.setMainSpaceVal(mainSpaceBlood);
	}
	
	@Override
	public void buildSpacePointInfo(SpaceWorldPoint spacePoint, Builder builder) {
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
				result = MechaSpaceGuardResult.SPACE_GUARD_SUCC;
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
