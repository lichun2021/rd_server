package com.hawk.game.world.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.app.HawkAppObj;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.type.ActivityState;
import com.hawk.activity.type.impl.snowball.SnowballActivity;
import com.hawk.activity.type.impl.snowball.cfg.SnowballCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.SnowballAttackResp;
import com.hawk.game.protocol.World.SnowballLastAtkPush;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.AlgorithmPoint;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.object.Point;
import com.hawk.game.world.proxy.WorldPointProxy;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;

/**
 * 雪球大战
 * @author golden
 *
 */
public class WorldSnowballService extends HawkAppObj {

	/**
	 * 生成雪球随机次数
	 */
	public static final int RANDTIMES = 100;

	/**
	 * 日志对象
	 */
	public static Logger logger = LoggerFactory.getLogger("Server");

	/**
	 * 单例对象
	 */
	private static WorldSnowballService instance = null;

	/**
	 * 所有雪球
	 */
	private Map<Integer, WorldPoint> snowballs = new ConcurrentHashMap<>();

	/**
	 * 击球记录
	 */
	private Map<Integer, List<String>> kickRecord = new ConcurrentHashMap<>();

	/**
	 * 玩家上次踢球
	 */
	private Map<String, Integer> lastKickMap = new ConcurrentHashMap<>();
	
	/**
	 * 上次tick时间
	 */
	private long lastRefreshTickTime;

	/**
	 * 获取单例对象
	 */
	public static WorldSnowballService getInstance() {
		return instance;
	}

	/**
	 * 对象构造函数
	 */
	public WorldSnowballService(HawkXID xid) {
		super(xid);
		instance = this;
	}

	/**
	 * 初始化
	 */
	public boolean init() {
		List<WorldPoint> points = WorldPointService.getInstance().getWorldPointsByType(WorldPointType.SNOWBALL);
		for (WorldPoint point : points) {
			snowballs.put(point.getMonsterId(), point);
			logger.info("initSnowballs, x:{}, y:{}, pylonId:{}", point.getX(), point.getY(), point.getMonsterId());
		}
		
		return true;
	}

	@Override
	public boolean onTick() {

		if (HawkTime.getMillisecond() - lastRefreshTickTime > SnowballCfg.getInstance().getTickPeriod()) {
			WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.REFRESH_SNOWBALL) {
				@Override
				public boolean onInvoke() {
					checkSnowballsRefresh();
					return true;
				}
			});
			lastRefreshTickTime = HawkTime.getMillisecond();
		}

		return true;
	}

	/**
	 * 活动是否开启
	 */
	public boolean isActivityOpen() {
		Optional<ActivityBase> activity = ActivityManager.getInstance().getGameActivityByType(ActivityType.SNOWBALL_VALUE);
		return activity.isPresent() && activity.get().getActivityEntity().getActivityState() == ActivityState.OPEN;
	}

	/**
	 * 获取当前阶段
	 */
	public int getCurrentStage() {
		int stage = 1;
		if (isActivityOpen()) {
			Optional<ActivityBase> activity = ActivityManager.getInstance().getGameActivityByType(ActivityType.SNOWBALL_VALUE);
			SnowballActivity activityOp = (SnowballActivity)activity.get();
			stage = activityOp.getCurrentStage();
		}
		return stage;
	}
	
	/**
	 * 
	 * 是否是刷新雪球的时间
	 */
	public boolean isRefreshSnowballTime() {
		Optional<ActivityBase> activity = ActivityManager.getInstance().getGameActivityByType(ActivityType.SNOWBALL_VALUE);
		if (!activity.isPresent()) {
			return false;
		}
		SnowballActivity snowballActivity = (SnowballActivity)activity.get();
		return snowballActivity.isInProgress();
	}

	/**
	 * 检测雪球刷新
	 */
	public void checkSnowballsRefresh() {
		if (isRefreshSnowballTime()) {
			refreshSnowballs();
		} else {
			removeSnowballs();
		}
	}

	/**
	 * 移除雪球
	 */
	public void removeSnowballs() {
		
		kickRecord.clear();
		
		lastKickMap.clear();
		
		if (snowballs.isEmpty()) {
			return;
		}

		int refreshBallCount = SnowballCfg.getInstance().getRefreshBallCount();
		for (int i = 0; i < refreshBallCount; i++) {
			// 雪球变化
			int number = i + 1;

			// 有这个编号的雪球
			WorldPoint point = snowballs.get(number);
			if (point == null) {
				continue;
			}

			WorldPointService.getInstance().removeWorldPoint(point.getId());

			snowballs.remove(number);

			logger.info("removeSnowball, x:{}, y:{}, areaId:{}, number:{}", point.getX(), point.getY(), point.getAreaId(), number);
		}
		
		if (!snowballs.isEmpty()) {
			for (WorldPoint point : snowballs.values()) {
				logger.info("removeSnowball, x:{}, y:{}, areaId:{}, number:{}", point.getX(), point.getY(), point.getAreaId(), point.getMonsterId());
				WorldPointService.getInstance().removeWorldPoint(point.getId());
			}
			snowballs.clear();
		}
	}

	/**
	 * 刷新
	 */
	public void refreshSnowballs() {

		// 生成点列表
		List<WorldPoint> createPoints = new ArrayList<>();
		
		int refreshBallCount = SnowballCfg.getInstance().getRefreshBallCount();
		for (int i = 0; i < refreshBallCount; i++) {

			// 雪球变化
			int number = i + 1;

			// 有这个编号的雪球
			if (snowballs.get(number) != null) {
				continue;
			}

			// 生成雪球
			WorldPoint snowball = genSnowball(number);
			if (snowball != null) {
				addSnowball(snowball);
				createPoints.add(snowball);
			}
		}

		if (!createPoints.isEmpty()) {
			WorldPointProxy.getInstance().batchCreate(createPoints);
		}
	}

	/**
	 * 生成雪球
	 */
	public void addSnowball(WorldPoint snowball) {
		snowballs.put(snowball.getMonsterId(), snowball);
	}
	
	/**
	 * 生成雪球
	 */
	public WorldPoint genSnowball(int number) {
		
		try {
			
			int randTimes = 0;

			while (true) {

				randTimes++;

				if (randTimes > RANDTIMES) {
					break;
				}

				int[] pos = randomSnowballPos();
				int randX = pos[0];
				int randY = pos[1];

				Point point = WorldPointService.getInstance().getAreaPoint(randX, randY, true);
				if (point == null) {
					continue;
				}

				if (!point.canRMSeat()) {
					continue;
				}

				if (WorldPointService.getInstance().getWorldPoint(point.getId()) != null) {
					continue;
				}

				// 创建世界点对象
				WorldPoint worldPoint = new WorldPoint(point.getX(), point.getY(), point.getAreaId(), point.getZoneId(), WorldPointType.SNOWBALL_VALUE);
				worldPoint.setMonsterId(number);
				WorldPointService.getInstance().addPoint(worldPoint);

				logger.info("genSnowball, x:{}, y:{}, areaId:{}, number:{}", point.getX(), point.getY(), point.getAreaId(), number);

				return worldPoint;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return null;
	}

	/**
	 * 随机雪球生成坐标
	 */
	public int[] randomSnowballPos() {
		int[] randPos = new int[2];

		// 三级资源带左上点
		int[] refreshRangePoint1 = SnowballCfg.getInstance().getRefreshRangePoint1();
		// 三级资源带右下点
		int[] refreshRangePoint2 = SnowballCfg.getInstance().getRefreshRangePoint2();
		// 四级资源带左上点
		int[] refreshRangePoint3 = SnowballCfg.getInstance().getRefreshRangePoint3();
		// 四级资源带右下点
		int[] refreshRangePoint4 = SnowballCfg.getInstance().getRefreshRangePoint4();

		// 先把x随机出来
		int randX = HawkRand.randInt(refreshRangePoint1[0], refreshRangePoint2[0]);

		int randY = 0;
		if (randX >= refreshRangePoint3[0] && randX <= refreshRangePoint4[0]) {

			if (HawkRand.randInt(1) > 0) {
				randY = HawkRand.randInt(refreshRangePoint1[1], refreshRangePoint3[1]);
			} else {
				randY = HawkRand.randInt(refreshRangePoint4[1], refreshRangePoint2[1]);
			}

		} else {
			randY = HawkRand.randInt(refreshRangePoint1[1], refreshRangePoint2[1]);
		}

		randPos[0] = randX;
		randPos[1] = randY;

		return randPos;
	}

	/**
	 * 是否可以出征
	 */
	public boolean canMarch(List<ArmyInfo> armyInfo) {
		// 士兵数量
		int count = 0;
		for (ArmyInfo army : armyInfo) {
			count += army.getTotalCount();
		}
		
		Map<Integer, Integer> marchDistanceMap = SnowballCfg.getInstance().getMarchDistanceMap();
		for (Integer marchDistance : marchDistanceMap.keySet()) {
			if (count >= marchDistance) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 获取踢球记录
	 */
	public List<String> getKickBallRecord(int number) {
		List<String> playerIds = kickRecord.get(number);
		if (playerIds == null) {
			playerIds = new ArrayList<>();
			List<String> oldValue = kickRecord.putIfAbsent(number, playerIds);
			if (oldValue != null) {
				playerIds = oldValue;
			}
		}
		return playerIds;
	}
	
	/**
	 * 获取踢球记录
	 */
	public String getLastKickBall(int number) {
		List<String> kickBallRecord = getKickBallRecord(number);
		if (kickBallRecord.isEmpty()) {
			return null;
		}
		return kickBallRecord.get(kickBallRecord.size() - 1);
	}
	
	/**
	 * 踢球
	 */
	public void kickBall(int number, String playerId) {
		List<String> playerIds = getKickBallRecord(number);
		playerIds.add(playerId);
	}
	
	/**
	 * 移除击球记录 
	 */
	public void removeKickRecord(int num) {
		List<String> list = kickRecord.get(num);
		for (String playerId : list) {
			removePlayerLakKick(playerId);
		}
		
		kickRecord.remove(num);
	}

	/**
	 * 获取建筑进球展示信息
	 * @return ret[0] 进球数 ret[1] 目标数
	 */
	public int[] getBuildingGoalShowInfo(WorldPoint point) {
		
		if (point.getPointType() != WorldPointType.KING_PALACE_VALUE
				&& point.getPointType() != WorldPointType.CROSS_FORTRESS_VALUE
				&& point.getPointType() != WorldPointType.SUPER_WEAPON_VALUE) {
			return null;
		}
		
		if (!isActivityOpen()) {
			return null;
		}
		
		Optional<ActivityBase> activity = ActivityManager.getInstance().getGameActivityByType(ActivityType.SNOWBALL_VALUE);
		SnowballActivity activityOp = (SnowballActivity)activity.get();
		
		int[] ret = new int[2];
		
		List<String> goalInfo = activityOp.getGoalInfo(point.getId());
		ret[0] = goalInfo.size();
		ret[1] = 0;
		return ret;
	}
	
	/**
	 * 获取建筑自己的进球数量
	 */
	public int getOwnGoalCount(WorldPoint point, String playerId) {
		
		if (point.getPointType() != WorldPointType.KING_PALACE_VALUE
				&& point.getPointType() != WorldPointType.CROSS_FORTRESS_VALUE
				&& point.getPointType() != WorldPointType.SUPER_WEAPON_VALUE) {
			return 0;
		}
		
		if (!isActivityOpen()) {
			return 0;
		}
		
		Optional<ActivityBase> activity = ActivityManager.getInstance().getGameActivityByType(ActivityType.SNOWBALL_VALUE);
		SnowballActivity activityOp = (SnowballActivity)activity.get();
		
		int count = 0;
		List<String> goalInfo = activityOp.getGoalInfo(point.getId());
		for (String info : goalInfo) {
			if (info.equals(playerId)) {
				count++;
			}
		}
		return count;
	}
	
	/**
	 * 获取加速作用号值
	 */
	public int getSpeedUpEffValue(String playerId, int pointId) {
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(pointId);
		if (point == null || point.getPointType() != WorldPointType.SNOWBALL_VALUE) {
			return 0;
		}
		
		// 击球次数
		int kickTimes = kickTimes(playerId, point.getMonsterId());
		
		if (kickTimes == 0) {
			return 0;
		}
		
		int onceValue = SnowballCfg.getInstance().getMarchSpeedValue();
		int maxValue = SnowballCfg.getInstance().getMarchSpeedValueMax();
		int value = Math.min(onceValue * kickTimes, maxValue);
		return value;
	}

	/**
	 * 获取玩家击球次数
	 */
	public int kickTimes(String playerId, int monsterId) {
		int kickTimes = 0;
		List<String> kickRecord = getKickBallRecord(monsterId);
		for (String record : kickRecord) {
			if (!record.equals(playerId)) {
				continue;
			}
			kickTimes++;
		}
		return kickTimes;
	}
	
	/**
	 * 盟友是否踢过球
	 */
	public boolean hasGuildKicked(String playerId, int monsterId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (!player.hasGuild()) {
			return false;
		}
		
		List<String> kickRecord = getKickBallRecord(monsterId);
		for (String record : kickRecord) {
			if (!GuildService.getInstance().isInTheSameGuild(record, playerId)) {
				continue;
			}
			return true;
		}
		
		return false;
	}
	
	/**
	 * 设置玩家上次击球
	 */
	public void putPlayerLakKick(String playerId, int snowball) {
		lastKickMap.put(playerId, snowball);
		pushPlayerLastKick(playerId);
	}
	
	/**
	 * 删除
	 */
	public void removePlayerLakKick(String playerId) {
		lastKickMap.remove(playerId);
		if (GlobalData.getInstance().isOnline(playerId)) {
			pushPlayerLastKick(playerId);
		}
	}
	
	/**
	 * 获取玩家上次踢的球
	 */
	public int getPlayerLastKick(String playerId) {
		Integer snowball = lastKickMap.get(playerId);
		if (snowball == null) {
			return 0;
		}
		return snowball;
	}
	
	/**
	 * 玩家上次击球推送
	 */
	public void pushPlayerLastKick(String playerId) {
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		SnowballLastAtkPush.Builder builder = SnowballLastAtkPush.newBuilder();
		if (isActivityOpen() && lastKickMap.containsKey(playerId)) {
			builder.setHasLastAtk(true);
			builder.setLastAtkNum(lastKickMap.get(playerId));
			builder.setAtkTimes(kickTimes(playerId, lastKickMap.get(playerId)));
		} else {
			builder.setHasLastAtk(false);
			builder.setLastAtkNum(0);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_SNOWBALL_LAST_ATK_PUSH_VALUE, builder));
	}
	
	public WorldPoint getSnowball(int snowball) {
		return snowballs.get(snowball);
	}
	
	public void removeSnowball(int number) {
		snowballs.remove(number);
	}
	
	/**
	 * 雪球进球推送
	 */
	public void notifySnowballMove(Player player, WorldPoint point) {
		if (point.getSnowballGoalInfo() != null) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_SNOWBALL_GOBAL_PUSH, point.getSnowballGoalInfo()));
		}
		if (point.getSnowballKickInfo() != null) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_SNOWBALL_REMOVE, point.getSnowballKickInfo()));
		}
	}
	
	/**
	 * 搜索雪球
	 */
	public WorldPoint searchSnowball(int[] pos, int index) {
		// 中心点
		AlgorithmPoint centerPoint = new AlgorithmPoint(pos[0], pos[1]);

		// 查找到的点集合
		TreeSet<WorldPoint> searchPoints = new TreeSet<>(new Comparator<WorldPoint>() {
			@Override
			public int compare(WorldPoint o1, WorldPoint o2) {
				double distance1 = centerPoint.distanceTo(new AlgorithmPoint(o1.getX(), o1.getY()));
				double distance2 = centerPoint.distanceTo(new AlgorithmPoint(o2.getX(), o2.getY()));
				return distance1 >= distance2 ? 1 : -1;
			}
		});
		
		Collection<WorldPoint> snowballCollection = snowballs.values();
		for (WorldPoint nian : snowballCollection) {
			searchPoints.add(nian);
		}
		
		if (searchPoints.isEmpty()) {
			return null;
		}
		
		List<WorldPoint> searchPointList = new ArrayList<WorldPoint>(searchPoints.size());
		searchPointList.addAll(searchPoints);
		
		return searchPointList.get(index % searchPointList.size());
	}
	
	/**
	 * 通知雪球攻击 
	 */
	public void noticeSnowballAttack(int fromX, int fromY, int toX, int toY) {
		Set<String> viewerIds = WorldUtil.calcPointViewers(fromX, fromY, 0, 0);
		viewerIds.addAll(WorldUtil.calcPointViewers(toX, toY, 0, 0));
		
		for (String playerId : viewerIds) {
			try {
				Player player = GlobalData.getInstance().getActivePlayer(playerId);
				if (player == null) {
					continue;
				}
				SnowballAttackResp.Builder builder = SnowballAttackResp.newBuilder();
				builder.setFromX(fromX);
				builder.setFromY(fromY);
				builder.setToX(toX);
				builder.setToY(toY);
				player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_SNOWBALL_ATTACK_RESP_VALUE, builder));
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
}
