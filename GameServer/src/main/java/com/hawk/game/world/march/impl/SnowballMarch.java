package com.hawk.game.world.march.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.SnowballGoalAssistanceEvent;
import com.hawk.activity.event.impl.SnowballGoalEvent;
import com.hawk.activity.event.impl.SnowballKickEvent;
import com.hawk.activity.type.impl.snowball.cfg.SnowballCfg;
import com.hawk.activity.type.impl.snowball.cfg.SnowballStageCfg;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.KickSnowballDirection;
import com.hawk.game.protocol.World.SnowballGoalInfo;
import com.hawk.game.protocol.World.SnowballKickInfo;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.BasedMarch;
import com.hawk.game.world.object.Point;
import com.hawk.game.world.proxy.WorldPointProxy;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.service.WorldSnowballService;

/**
 * 踢雪球行军
 * @author golden
 *
 */
public class SnowballMarch extends PlayerMarch implements BasedMarch {

	// 雪球起始点
	private int fromX;
	private int fromY;
	
	// 雪球路遇障碍点
	private int obstaceleX;
	private int obstaceleY;
	
	// 雪球目标点
	private int targetX;
	private int targetY;
	
	// 雪球出现时间
	private long createTime;
	
	public SnowballMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.SNOWBALL_MARCH;
	}

	@Override
	public void onMarchReach(Player player) {

		try {
			// 目标点雪球
			WorldPoint point = WorldPointService.getInstance().getWorldPoint(this.getTerminalId());
			
			// 踢雪球方向
			KickSnowballDirection direction = KickSnowballDirection.valueOf(this.getMarchEntity().getAttackTimes());
			
			// 踢雪球距离
			int distance = getKickDistance();
			
			// 雪球的号码
			int snowballNumber = Integer.valueOf(this.getMarchEntity().getTargetId());
			
			// 目标点已经改变
			if (point == null || point.getPointType() != WorldPointType.SNOWBALL_VALUE || snowballNumber != point.getMonsterId()) {
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(player.getId())
						.setMailId(MailId.SNOWBALL_MAIL_2)
						.build());
				
			} else {
				fromX = 0;
				fromY = 0;
				obstaceleX = 0;
				obstaceleY = 0;
				createTime = 0L;
				
				WorldSnowballService.getInstance().putPlayerLakKick(getPlayerId(), point.getMonsterId());

				// 击球事件 踢一个球次数达到限制了就不发此事件了
				List<String> kickBallRecord = WorldSnowballService.getInstance().getKickBallRecord(point.getMonsterId());
				ActivityManager.getInstance().postEvent(new SnowballKickEvent(getPlayerId(), point.getX(), point.getY(), 0, kickBallRecord));
				
				// 击球记录
				WorldSnowballService.getInstance().kickBall(point.getMonsterId(), getPlayerId());
				
				// 计算雪球之后的位置
				calcSnowballAfterPos(point, direction, distance);
				
				WorldMarchService.getInstance().sendBattleResultInfo(this, true, new ArrayList<>(), new ArrayList<>(), true, false, direction);
			}
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		// 行军返回
		WorldMarchService.getInstance().onPlayerNoneAction(this, getMarchEntity().getReachTime());
	}

	/**
	 * 获取踢球距离
	 */
	public int getKickDistance() {
		// 士兵数量
		int count = 0;
		List<ArmyInfo> armyInfo = this.getArmys();
		for (ArmyInfo army : armyInfo) {
			count += army.getTotalCount();
		}

		int distance = 0;
		Map<Integer, Integer> marchDistanceMap = SnowballCfg.getInstance().getMarchDistanceMap();
		for (Entry<Integer, Integer> marchDistance : marchDistanceMap.entrySet()) {
			if (count >= marchDistance.getKey() && marchDistance.getValue() > distance) {
				distance = marchDistance.getValue();
			}
		}
		return distance;
	}

	/**
	 * 计算雪球之后的位置
	 */
	public void calcSnowballAfterPos(WorldPoint point, KickSnowballDirection direction, int distance) {

		fromX = point.getX();
		fromY = point.getY();
		
		int[] afterPos = new int[]{point.getX(), point.getY()};
		
		for (int i = 0; i < distance; i++) {
			// 走一个格子
			afterPos = onceStep(afterPos[0], afterPos[1], direction);
			// 进球
			if (checkGoal(point, afterPos)) {
				return;
			}
			// 出界
			if (chekcOutOfBounds(point, afterPos)) {
				return;
			}
		}
		
		// 球到达目标点
		if (doSeat(point, afterPos)) {
			return;
		}
		
		int times = 0;
		
		// 到达目标点以后发现不能落座，继续向前进方向滚动
		while(true) {
			
			times++;
			
			// 走一个格子
			afterPos = onceStep(afterPos[0], afterPos[1], direction);
			// 进球
			if (checkGoal(point, afterPos)) {
				return;
			}
			// 出界
			if (chekcOutOfBounds(point, afterPos)) {
				return;
			}
			// 球到达目标点
			if (doSeat(point, afterPos)) {
				return;
			}
			// 做个容错
			if (times > 1000) {
				return;
			}
		}
	}

	/**
	 * 检测进球
	 */
	public boolean checkGoal(WorldPoint worldPoint, int[] afterPos) {
		// 进球坐标,没进的话为0
		int goalPointId = getGoalPointId(afterPos);
		
		if (goalPointId > 0) {
			doGoal(worldPoint, goalPointId);
		}
		
		return goalPointId > 0;
	}
	
	/**
	 * 获取进球的建筑坐标
	 */
	public int getGoalPointId(int[] pos) {
		// 进球的坐标
		int goalPointId = 0;
		
		// 当前阶段
		int stage = WorldSnowballService.getInstance().getCurrentStage();
		
		// 开放的进球点
		SnowballStageCfg stageCfg = HawkConfigManager.getInstance().getConfigByKey(SnowballStageCfg.class, stage);
		Set<Integer> openPos = stageCfg.getOpenPosSet();
		
		Map<Integer, Integer> torRangeMap = SnowballCfg.getInstance().getTorRangeMap();
		for (Entry<Integer, Integer> entry : torRangeMap.entrySet()) {
			int torPointId = entry.getKey();
			int[] torPos = GameUtil.splitXAndY(torPointId);
			int range = entry.getValue();
			
			if (!openPos.contains(torPointId)) {
				continue;
			}
			double distance = WorldUtil.distance(pos[0], pos[1], torPos[0], torPos[1]);
			if (distance <= range) {
				goalPointId = torPointId;
			}
		}
		
		return goalPointId;
	}
	
	/**
	 * 处理进球
	 */
	public void doGoal(WorldPoint snowball, int goalPointId) {
		
		int[] goalPos = GameUtil.splitXAndY(goalPointId);
		
		int snowballNumber = snowball.getMonsterId();
		// 进球事件
		List<String> kickBallRecord = WorldSnowballService.getInstance().getKickBallRecord(snowball.getMonsterId());
		ActivityManager.getInstance().postEvent(new SnowballGoalEvent(getPlayerId(), goalPos[0], goalPos[1], 0, kickBallRecord));

		List<String> sendAssis = new ArrayList<>();
		// 进球助攻事件
		for (String record : kickBallRecord) {
			if (record.equals(getPlayerId())) {
				continue;
			}
			if (!GuildService.getInstance().isInTheSameGuild(record, getPlayerId())) {
				continue;
			}
			if (sendAssis.contains(record)) {
				continue;
			}
			sendAssis.add(record);
			ActivityManager.getInstance().postEvent(new SnowballGoalAssistanceEvent(record, goalPos[0], goalPos[1], 0, kickBallRecord, snowballNumber));
		}

		createTime = HawkTime.getMillisecond();
		targetX = goalPos[0];
		targetY = goalPos[1];
		
		SnowballGoalInfo.Builder builder = SnowballGoalInfo.newBuilder();
		builder.setFromX(snowball.getX());
		builder.setFromY(snowball.getY());
		builder.setTargetX(goalPos[0]);
		builder.setTargetY(goalPos[1]);
		builder.setNumber(snowballNumber);
		snowball.addSnowballGoalInfo(builder);
		snowball.addSnowballKickInfo(getSnowballKickInfo());
		
		// 删除击球记录
		WorldSnowballService.getInstance().removeKickRecord(snowballNumber);
		
		WorldSnowballService.getInstance().removeSnowball(snowballNumber);
		
		// 删除旧点
		WorldPointService.getInstance().removeWorldPoint(snowball.getId());
		
		// 生成新点
		WorldPoint newPoint = WorldSnowballService.getInstance().genSnowball(snowballNumber);
		WorldSnowballService.getInstance().addSnowball(newPoint);
		
		WorldPointService.getInstance().notifyPointUpdate(goalPos[0], goalPos[1]);
		
		// 日志
		LogUtil.logSnowballGoal(getPlayer(), goalPos[0], goalPos[1]);
	}

	/**
	 * 球到达目标点
	 */
	public boolean doSeat(WorldPoint oldPoint, int[] afterPos) {
		
		// 是否可以落座
		if (canSnowballSeat(afterPos[0], afterPos[1])) {
			
			createTime = HawkTime.getMillisecond();
			targetX = afterPos[0];
			targetY = afterPos[1];
			
			int snowballNumber = oldPoint.getMonsterId();
			
			// 删除旧点
			oldPoint.addSnowballKickInfo(getSnowballKickInfo());
			WorldPointService.getInstance().removeWorldPoint(oldPoint.getId());
			
			// 创建新点
			Point genPoint = WorldPointService.getInstance().getAreaPoint(afterPos[0], afterPos[1], true);
			WorldPoint worldPoint = new WorldPoint(genPoint.getX(), genPoint.getY(), genPoint.getAreaId(), genPoint.getZoneId(), WorldPointType.SNOWBALL_VALUE);
			worldPoint.setMonsterId(snowballNumber);
			worldPoint.addSnowballKickInfo(getSnowballKickInfo());
			
			WorldPointService.getInstance().addPoint(worldPoint);
			WorldSnowballService.getInstance().addSnowball(worldPoint);
			WorldPointProxy.getInstance().create(worldPoint);
			WorldSnowballService.logger.info("genSnowball doSeat, x:{}, y:{}, areaId:{}, number:{}", oldPoint.getX(), oldPoint.getY(), oldPoint.getAreaId(), worldPoint.getMonsterId());
			
			return true;
		} else {
			if (obstaceleX == 0) {
				obstaceleX = afterPos[0];
			}
			if (obstaceleY == 0) {
				obstaceleY = afterPos[1];
			}
		}
		
		return false;
	}

	/**
	 * 雪球点是否可以落座
	 */
	public boolean canSnowballSeat(int x, int y) {
		Point point = WorldPointService.getInstance().getAreaPoint(x, y, true);
		if (point == null) {
			return false;
		}

		if (!point.canRMSeat()) {
			return false;
		}

		if (WorldPointService.getInstance().getWorldPoint(point.getId()) != null) {
			return false;
		}

		return true;
	}
	
	/**
	 * 检测出界
	 */
	public boolean chekcOutOfBounds(WorldPoint point, int[] afterPos) {
		int worldMaxX = WorldMapConstProperty.getInstance().getWorldMaxX();
		int worldMaxY = WorldMapConstProperty.getInstance().getWorldMaxY();
		if (afterPos[0] <= 1 || afterPos[0] >= worldMaxX -1 || afterPos[1] <= 1 || afterPos[0] >= worldMaxY - 1) {
			return true;
		}
		return false;
	}

	/**
	 * 前进一步
	 */
	public int[] onceStep(int x, int y, KickSnowballDirection direction) {

		switch (direction) {

		case SNOWBALL_UP:
			y = y - 2;
			break;

		case SNOWBALL_UPLEFT:
			x--;
			y--;
			break;

		case SNOWBALL_UPRIGHT:
			x++;
			y--;
			break;

		case SNOWBALL_LEFT:
			x = x -2;
			break;

		case SNOWBALL_RIGHT:
			x = x + 2;
			break;

		case SNOWBALL_DOWN:
			y = y + 2;
			break;

		case SNOWBALL_DOWNLEFT:
			x--;
			y++;
			break;

		case SNOWBALL_DOWNRIGHT:
			x++;
			y++;
			break;

		default:
			break;
		}

		return new int[] { x, y };
	}
	
	public SnowballKickInfo.Builder getSnowballKickInfo() {
		if (createTime != 0L) {
			SnowballKickInfo.Builder builder = SnowballKickInfo.newBuilder();
			builder.setFromX(fromX);
			builder.setFromY(fromY);
			builder.setObstaceleX(obstaceleX);
			builder.setObstaceleY(obstaceleY);
			builder.setTargetX(targetX);
			builder.setTargetY(targetY);
			builder.setCreateTime(createTime);
			builder.setPlayerId(this.getPlayerId());
			return builder;
		}
		return null;
	}
}
