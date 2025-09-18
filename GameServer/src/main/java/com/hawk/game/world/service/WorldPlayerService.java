package com.hawk.game.world.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.hawk.annotation.MessageHandler;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple3;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.config.WorldEnemyCfg;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.global.GlobalData;
import com.hawk.game.invoker.CityRecoverMsgInvoker;
import com.hawk.game.invoker.WorldRemoveCityMsgInvoker;
import com.hawk.game.module.PlayerWorldModule;
import com.hawk.game.msg.MigrateOutPlayerMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Mail.SoilderAssistanceMail;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.CityMoveType;
import com.hawk.game.protocol.World.WorldInfoPush;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.mail.GuildMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.MailBuilderUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.object.AreaObject;
import com.hawk.game.world.object.Point;
import com.hawk.gamelib.GameConst.MsgId;

/**
 *
 * @author zhenyu.shang
 * @since 2017年8月15日
 */
public class WorldPlayerService extends HawkAppObj {
	/**
	 * 日志对象
	 */
	private static Logger logger = LoggerFactory.getLogger("Server");
	
	/**
	 * 玩家的坐标信息
	 */
	private Map<String, Integer> playerPos;
	/**
	 * 玩家城堡随机坐落区域 (区域id, 可占用点)
	 */
	private Map<Long, BlockingQueue<Point>> bornCityArea;
	/**
	 * 玩家城堡随机坐落区域玩家数量 (区域id, 玩家数量)
	 */
	private Map<Long, AtomicInteger> areaPlayerCount;

	/**
	 * 检测过期城点
	 */
	private long checkTimeOutCity;
	
	
	private Map<Integer,List<Point>> guildAutoMoveArea;
	private AtomicLong guildAutomoveParam;
	/**
	 * 单例对象
	 */
	private static WorldPlayerService instance = null;

	/**
	 * 获取单例对象
	 * 
	 * @return
	 */
	public static WorldPlayerService getInstance() {
		return instance;
	}

	/**
	 * 对象构造函数
	 * 
	 * @param xid
	 */
	public WorldPlayerService(HawkXID xid) {
		super(xid);
		instance = this;
		
		playerPos = new ConcurrentHashMap<String, Integer>();
		areaPlayerCount = new ConcurrentHashMap<Long, AtomicInteger>();
		bornCityArea = new ConcurrentHashMap<Long, BlockingQueue<Point>>();
	}

	public boolean init() {
		// 初始化出生点区域
		initBornArea();
		
		// 遍历玩家城点, 构建区域信息和出生信息
		Set<Integer> timeoutPoints = new HashSet<Integer>();
		
		// 获取所有玩家城点
		List<WorldPoint> playerPoint = WorldPointService.getInstance().getWorldPointsByType(WorldPointType.PLAYER);
		
		// 初始化所有玩家城点
		for (WorldPoint worldPoint : playerPoint) {
			try {
				// 存储玩家位置相关信息
				if (HawkOSOperator.isEmptyString(worldPoint.getPlayerId())) {
					logger.error("player world point error, pointId: {}", worldPoint.getId());
					continue;
				}

				for (long areaId : WorldMapConstProperty.getInstance().getBirthAreas()) {
					int[] bornAreaId = GameUtil.splitFromAndTo(areaId);
					if (WorldUtil.isPointInArea(worldPoint.getId(), bornAreaId[0], bornAreaId[1])) {
						addAreaPlayerCount(areaId, 1);
					}
				}
				addPlayerPos(worldPoint);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		// 移除过期的世界点坐标
		WorldPointService.getInstance().removeWorldPoints(timeoutPoints, true);
		
		//
		this.initGuildAutoMoveArea();
		return true;
	}

	@Override
	public boolean onTick() {
		// 检测过期城点
		chickTimeoutCity();
		return true;
	}
	
	/**
	 * 初始化出生点区域
	 */
	private void initBornArea() {
		long startTime = HawkTime.getMillisecond();
		logger.info("start load born city areas ...");
		startTime = HawkTime.getMillisecond();
		// 初始化玩家新手城点区域
		for (long areaRange : WorldMapConstProperty.getInstance().getBirthAreas()) {
			int[] bornAreaId = GameUtil.splitFromAndTo(areaRange);

			// 玩家城点区域起始点
			int[] fromPoint = GameUtil.splitXAndY(bornAreaId[0]);
			int[] toPoint = GameUtil.splitXAndY(bornAreaId[1]);

			int areaCenterX = (fromPoint[0] + toPoint[0]) / 2;
			int areaCenterY = (fromPoint[1] + toPoint[1]) / 2;
			int radiuX = (toPoint[0] - fromPoint[0]) / 2;
			int radiuY = (toPoint[1] - fromPoint[1]) / 2;

			Map<Integer, Point> aroundPoints = WorldPointService.getInstance().getAroundPoints(areaCenterX, areaCenterY, radiuX, radiuY);
			ArrayList<Point> aroundPointsList = new ArrayList<Point>(aroundPoints.values());
			Collections.shuffle(aroundPointsList);
			bornCityArea.put(areaRange, new LinkedBlockingQueue<Point>(aroundPointsList));
			areaPlayerCount.put(areaRange, new AtomicInteger());
		}
		logger.info("born areas load finish, costtime: {}", HawkTime.getMillisecond() - startTime);
	}

	/**
	 * 获取玩家城点位置
	 * 
	 * @param playerId
	 * @return
	 */
	public int getPlayerPos(String playerId) {
		if (!playerPos.containsKey(playerId)) {
			return 0;
		}
		return playerPos.get(playerId);
	}

	/**
	 * 获取玩家坐标xy
	 * 
	 * @param playerId
	 * @return
	 */
	public int[] getPlayerPosXY(String playerId) {
		if (HawkOSOperator.isEmptyString(playerId)) {
			return new int[] { 0, 0 };
		}

		int pointId = getPlayerPos(playerId);
		if (pointId > 0) {
			return GameUtil.splitXAndY(pointId);
		}
		return new int[] { 0, 0 };
	}

	/**
	 * 获取玩家的城点对象
	 * 
	 * @param playerId
	 * @return
	 */
	public WorldPoint getPlayerWorldPoint(String playerId) {
		Integer pointId = playerPos.get(playerId);
		if (WorldRobotService.getInstance().isRobotId(playerId)) {
			Player robot = GlobalData.getInstance().makesurePlayer(playerId);
			if (robot == null) {
				return null;
			}
			pointId = robot.getPlayerPos();
		}
		
		if (pointId != null) {
			return WorldPointService.getInstance().getWorldPoint(pointId);
		}
		return null;
	}

	/**
	 * 新增玩家城点位置
	 * 
	 * @param point
	 */
	public void addPlayerPos(WorldPoint point) {
		playerPos.put(point.getPlayerId(), point.getId());
	}

	/**
	 * 删除玩家城点位置
	 * 
	 * @param playerId
	 */
	public void rmFromPlayerPos(String playerId) {
		if (HawkOSOperator.isEmptyString(playerId)) {
			return;
		}
		playerPos.remove(playerId);
	}

	/**
	 * 获取玩家出生城点区域玩家数量
	 * 
	 * @param areaId
	 * @return
	 */
	protected int getAreaPlayerCount(long areaId) {
		if (!areaPlayerCount.containsKey(areaId)) {
			return 0;
		}
		return areaPlayerCount.get(areaId).intValue();
	}

	/**
	 * 添加玩家出生城点区域玩家数量
	 * 
	 * @param areaId
	 * @param count
	 */
	public int addAreaPlayerCount(long areaId, int count) {
		if (!areaPlayerCount.containsKey(areaId)) {
			areaPlayerCount.put(areaId, new AtomicInteger());
		}
		return areaPlayerCount.get(areaId).addAndGet(count);
	}

	/**
	 * 设置区域玩家数
	 * 
	 * @param areaId
	 * @param count
	 */
	protected void setAreaPlayerCount(long areaId, int count) {
		if (!areaPlayerCount.containsKey(areaId)) {
			areaPlayerCount.put(areaId, new AtomicInteger());
		}
		areaPlayerCount.get(areaId).set(count);
	}

	/**
	 * 获取玩家出生城点区域
	 * 
	 * @param areaId
	 * @return
	 */
	protected BlockingQueue<Point> getBornCityArea(long areaId) {
		if (!bornCityArea.containsKey(areaId)) {
			return null;
		}
		return bornCityArea.get(areaId);
	}

	/**
	 * 更新世界点的玩家信息
	 * 
	 * @param playerId
	 * @param playerName
	 * @param cityLevel
	 * @param playerIcon
	 */
	public void updatePlayerPointInfo(String playerId, String playerName, int cityLevel, int playerIcon, String playerProtectInfo) {
		try {
			List<WorldPoint> needUpdates = new ArrayList<WorldPoint>();
			// 基地点
			WorldPoint worldPoint = getPlayerWorldPoint(playerId);
			if (worldPoint != null) {
				needUpdates.add(worldPoint);
			}
			// 驻扎点
			List<IWorldMarch> quarteredMarchs = WorldMarchService.getInstance().getPlayerMarch(playerId, WorldMarchType.ARMY_QUARTERED_VALUE);
			if (quarteredMarchs != null && !quarteredMarchs.isEmpty()) {
				for (IWorldMarch march : quarteredMarchs) {
					WorldPoint terminalPoint = WorldPointService.getInstance().getWorldPoint(march.getMarchEntity().getTerminalId());
					if (terminalPoint != null && playerId.equals(terminalPoint.getPlayerId())) {
						needUpdates.add(terminalPoint);
					}
				}
			}
			
			// 资源点
			List<IWorldMarch> resMarchs = WorldMarchService.getInstance().getPlayerMarch(playerId, WorldMarchType.COLLECT_RESOURCE_VALUE);
			if (resMarchs != null && !resMarchs.isEmpty()) {
				for (IWorldMarch march : resMarchs) {
					WorldPoint terminalPoint = WorldPointService.getInstance().getWorldPoint(march.getMarchEntity().getTerminalId());
					if (terminalPoint != null && playerId.equals(terminalPoint.getPlayerId())) {
						needUpdates.add(terminalPoint);
					}
				}
			}
			
			for (WorldPoint needUpdate : needUpdates) {
				needUpdate.setPlayerName(playerName);
				needUpdate.setCityLevel(cityLevel);
				needUpdate.setPlayerIcon(playerIcon);
				needUpdate.setPersonalProtectInfo(playerProtectInfo);
				// 通知场景本点数据更新
				WorldPointService.getInstance().getWorldScene().update(needUpdate.getAoiObjId());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 城防值降到0强制迁城
	 * 
	 * @param player
	 * @return
	 */
	public boolean moveCity(Player player) {
		boolean isFirst = player.getData().getPlayerBaseEntity().getOnFireEndTime() <= 0;
		removeCity(player.getId(), true);
		Point newPoint = randomSettlePoint(player, isFirst);
		if (newPoint != null) {
			// 生成玩家城堡占用点
			WorldPoint targetPoint = new WorldPoint(newPoint.getX(), newPoint.getY(), newPoint.getAreaId(), newPoint.getZoneId(), WorldPointType.PLAYER_VALUE);
			targetPoint.initPlayerInfo(player.getData());
			targetPoint.setProtectedEndTime(0);

			// 创建玩家使用的世界点信息
			if (!WorldPointService.getInstance().createWorldPoint(targetPoint)) {
				logger.error("random settle point failed, playerId: {}, pos: ({}, {}), isBornPos: {}", player.getId(), targetPoint.getX(), targetPoint.getY(), isFirst);
				return false;
			}
			
			// 判断是否在联盟领地中，获取增益
			GuildManorService.getInstance().notifyManorBuffChange(player);
			
			int cityDef = player.getPlayerBaseEntity().getCityDefVal();
			// 投递回玩家线程执行
			player.dealMsg(MsgId.CITY_DEF_RECOVER, new CityRecoverMsgInvoker(player));
			
			if (!isFirst) {
				player.sendProtocol(HawkProtocol.valueOf(cityDef > 0 ? HP.code.NEWLY_MOVE_CITY_NOTIFY_PUSH : HP.code.MOVE_CITY_NOTIFY_PUSH));
				sendCityWorldPoint(player, newPoint.getX(), newPoint.getY());
			}

			// 新手保护时间
			long cityShieldTime = player.getData().getCityShieldTime();
			if (cityShieldTime > HawkTime.getMillisecond()) {
				updateWorldPointProtected(player.getId(), cityShieldTime);
			}
			logger.info("player random world point success, playerId: {}, x: {} , y: {}, isBornPos: {}", player.getId(), newPoint.getX(), newPoint.getY(), isFirst);
		} else {
			logger.error("player random world point failed, playerId: {}, isBornPos: {}", player.getId(), isFirst);
		}
		return true;
	}
	
	/**
	 * 世界迁城
	 * 
	 * @param playerId
	 * @param deleteEntity
	 * @param stayInPlace   原地高迁标识
	 * @return
	 */
	public WorldPoint moveCity(String playerId, boolean deleteEntity, boolean stayInPlace) {
		WorldPoint point = getPlayerWorldPoint(playerId);
		if (point == null || HawkOSOperator.isEmptyString(playerId) || !playerId.equals(point.getPlayerId())) {
			return null;
		}
		
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player == null) {
			HawkLog.logPrintln("WorldPlayerService move city failed, makesure player result null, playerId: {}", playerId);
			return point;
		}
		
		// 处理援助类型的行军
		beHitFlyMarchProcess(player);
		
		// 处理玩家的行军
		WorldMarchService.getInstance().mantualMoveCityProcessMarch(player);
		
		// 非原地高迁的情况下需要移除原来的城点
		if (!stayInPlace) {
			if (WorldRobotService.getInstance().isRobotId(playerId)) {
				deleteEntity = false;
			}
			// 删除世界点
			WorldPointService.getInstance().removeWorldPoint(point.getId(), deleteEntity);
			// 投递回玩家线程执行
			player.dealMsg(MsgId.CITY_REMOVE, new WorldRemoveCityMsgInvoker(player));
		}
		
		logger.info("move player city, playerId: {}, point: {}, delete point: {}, stayInPlace: {}", player.getId(), point, deleteEntity, stayInPlace);

		return point;
	}

	/**
	 * 移除世界城点
	 * 
	 * @param playerId
	 * @return
	 */
	public WorldPoint removeCity(String playerId, boolean deleteEntity) {
		WorldPoint point = moveCity(playerId, deleteEntity, false);
		return point;
	}

	/**
	 * 发送城点被清除
	 * 
	 * @param player
	 * @param isRemoved
	 * @param x
	 * @param y
	 */
	public void sendCityWorldPoint(Player player, int x, int y) {
		// 自己世界信息通报
		WorldInfoPush.Builder worldBuilder = WorldInfoPush.newBuilder();
		worldBuilder.setTargetX(x);
		worldBuilder.setTargetY(y);
		worldBuilder.setIsRecreate(false);
		Integer guildPointId = GuildManorService.getInstance().getGuildManorPointId(player.getGuildId());
		if (guildPointId != null) {
			int[] guildPos = GameUtil.splitXAndY(guildPointId.intValue());
			worldBuilder.setGuildPosX(guildPos[0]);
			worldBuilder.setGuildPosX(guildPos[1]);
		}
			
		player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_PLAYER_WORLD_INFO_PUSH, worldBuilder));
	}

	/**
	 * 被打飞之后处理援助类型的行军
	 */
	private void beHitFlyMarchProcess(Player defPlayer) {
		// 玩家发起的行军
		BlockingQueue<IWorldMarch> playerMarchs = WorldMarchService.getInstance().getPlayerMarch(defPlayer.getId());
		
		for (IWorldMarch march : playerMarchs) {
			
			if (march.getMarchEntity().getMarchStatus() != WorldMarchStatus.MARCH_STATUS_MARCH_VALUE) {
				continue;
			}

			int icon = GuildService.getInstance().getGuildFlagByPlayerId(march.getPlayerId());

			if (march.getMarchType() == WorldMarchType.ASSISTANCE_RES) {

				// 向玩家援助
				Player tarPlayer = GlobalData.getInstance().makesurePlayer(march.getMarchEntity().getTargetId());
				if (tarPlayer == null) {
					continue;
				}

				// 发送邮件---资源援助者被打飞
				GuildMailService.getInstance().sendMail(MailParames
						.newBuilder()
						.setPlayerId(march.getPlayerId())
						.setMailId(MailId.RES_ASSISTANCE_FAILED_FROMER_CHANGED)
						.addSubTitles(tarPlayer.getName())
						.setIcon(icon)
						.build());
				
				logger.info("world service bore to die assiatance_res disturbed marchData:{}" + march);
				
			} else if (march.getMarchType() == WorldMarchType.ASSISTANCE) {

				// 向玩家援助
				final Player tarPlayer = GlobalData.getInstance().makesurePlayer(march.getMarchEntity().getTargetId());
				if (tarPlayer == null) {
					continue;
				}

				// 发邮件---士兵援助者被打飞
				List<PlayerHero> hero = defPlayer.getHeroByCfgId(march.getMarchEntity().getHeroIdList());
				SoilderAssistanceMail.Builder builder = MailBuilderUtil.createSoilderAssistanceMail(march.getMarchEntity(), hero, tarPlayer);
				GuildMailService.getInstance().sendMail(MailParames
						.newBuilder()
						.setPlayerId(march.getPlayerId())
						.setMailId(MailId.SOILDER_ASSISTANCE_FAILED_FROMER_CHANGED)
						.addSubTitles(tarPlayer.getName())
						.addContents(builder).setIcon(icon).build());
				
				logger.info("world service bore to die assiatance_soilder disturbed marchData:{}" + march);
			}
		}
	}

	/**
	 * 检测世界点是否可被玩家占用
	 * 
	 * @param player
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean checkPlayerCanOccupy(Player player, int x, int y) {
		try {
			AreaObject areaObj = WorldPointService.getInstance().getArea(x, y);
			Point centerPoint = areaObj.getAreaPoint(x, y);
			if (centerPoint == null) {
				logger.error("move city failed, check player occupy failed, centerPoint is null");
				return false;
			}

			// 计算玩家当前所占点坐标集合(包括城点中心点)
			Set<Integer> currPointIds = new HashSet<Integer>();

			int playerPos = getPlayerPos(player.getId());
			currPointIds.add(playerPos);
			List<Point> currOccupyPoints = WorldPointService.getInstance().getRhoAroundPointsAll(playerPos, GsConst.PLAYER_POINT_RADIUS);
			for (Point point : currOccupyPoints) {
				currPointIds.add(point.getId());
			}

			// 检验是否可占用
			return tryPlayerOccupied(centerPoint, WorldPointType.PLAYER, currPointIds);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}

	/**
	 * 玩家城点尝试占用世界地图点
	 * 
	 * @param areaObj
	 * @param centerPoint
	 * @param type
	 * @param currOccupyIds
	 * @return
	 */
	protected boolean tryPlayerOccupied(Point centerPoint, WorldPointType type, Set<Integer> currOccupyIds) {
		// 占位距离
		int distance = GsConst.PLAYER_POINT_RADIUS;
		// 距离边界的长度
		int boundary = GsConst.WORLD_BOUNDARY_SIZE;

		// 边界坐标点不可用的检测
		if (centerPoint.getX() <= boundary || centerPoint.getY() < boundary || centerPoint.getX() >= (WorldMapConstProperty.getInstance().getWorldMaxX() - boundary)
				|| centerPoint.getY() >= WorldMapConstProperty.getInstance().getWorldMaxY() - boundary) {
			return false;
		}
		// 中心点是否被占用
		WorldPoint currPoint = WorldPointService.getInstance().getWorldPoint(centerPoint.getX(), centerPoint.getY());
		if (currPoint != null && (currOccupyIds == null || !currOccupyIds.contains(currPoint.getId()))) {
			return false;
		}
		// 此处注意！！！
		// 由于直接取空闲点，可能涉及当前点的重合，导致空闲点不足，所以此处需要取 （范围内点∩当前点）∪空闲点
		List<Point> allAroundPoints = WorldPointService.getInstance().getRhoAroundPointsAll(centerPoint.getX(), centerPoint.getY(), distance);
		// 城点距离限制范围内的所有点(1距离为4个点, 2距离为12个点)
		List<Point> freeAroundPoints = WorldPointService.getInstance().getRhoAroundPointsFree(centerPoint.getX(), centerPoint.getY(), distance);

		// 空闲点
		List<Integer> freePoints = new ArrayList<Integer>();
		for (Point freeAroundPoint : freeAroundPoints) {
			freePoints.add(freeAroundPoint.getId());
		}
		
		// 原占用点加入空闲点
		for (int currOccupyId : currOccupyIds) {
			for (Point allAroundPoint : allAroundPoints) {
				if (allAroundPoint.getId() == currOccupyId) {
					freePoints.add(currOccupyId);
					break;
				}
			}
		}

		// 必须为4个, 否则就是有阻挡点存在
		if (freePoints.size() != 2 * distance * (distance - 1)) {
			return false;
		}
		return true;
	}

	/**
	 * 更新玩家城点的保护数据
	 * 
	 * @param protectedEndTime
	 */
	public void updateWorldPointProtected(String playerId, long protectedEndTime) {

		WorldPoint worldPoint = getPlayerWorldPoint(playerId);
		if (worldPoint == null) {
			return;
		}
		worldPoint.setProtectedEndTime(protectedEndTime);
		// 通知场景本点数据更新
		WorldPointService.getInstance().getWorldScene().update(worldPoint.getAoiObjId());
	}

	/**
	 * 清除玩家的世界城点
	 * 
	 * @param playerId
	 * @return
	 */
	public void rmPlayerWorldPoint(String playerId) {
		int pointId = getPlayerPos(playerId);
		if (pointId <= 0) {
			return;
		}
		int[] posInfo = GameUtil.splitXAndY(pointId);
		WorldPointService.getInstance().removeWorldPoint(posInfo[0], posInfo[1]);
	}

	/**
	 * 玩家创建，加入，离开联盟都需要调用
	 * 
	 * @param player
	 */
	public void noticeAllianceChange(String playerId) {
		WorldPoint worldPoint = getPlayerWorldPoint(playerId);
		if (worldPoint == null) {
			return;
		}
		// 通知场景本点数据更新
		WorldPointService.getInstance().getWorldScene().update(worldPoint.getAoiObjId());

		BlockingQueue<IWorldMarch> marchs = WorldMarchService.getInstance().getPlayerMarch(playerId);
		if (marchs == null) {
			return;
		}
		// 更新采集驻扎点的信息
		for (IWorldMarch worldMarch : marchs) {
			WorldMarch march = worldMarch.getMarchEntity();
			if (march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_COLLECT_VALUE
					|| march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_QUARTERED_VALUE
					|| march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MANOR_BREAK_VALUE
					|| march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MANOR_BUILD_VALUE
					|| march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MANOR_GARRASION_VALUE
					|| march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MANOR_REPAIR_VALUE
					|| march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_EXPLORE_VALUE
					|| march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_OCCUPY_VALUE) {
				WorldPoint tarPoint = WorldPointService.getInstance().getWorldPoint(march.getTerminalId());
				if (tarPoint != null) {
					WorldPointService.getInstance().getWorldScene().update(tarPoint.getAoiObjId());
				}
			}
		}
	}

	/**
	 * 随机生成玩家城堡坐标
	 * 
	 * @param player
	 * @param protectedEndTime
	 * @param isBornPos
	 * @return
	 */
	public Point randomSettlePoint(Player player, boolean isBornPos) {
		long startTime = HawkTime.getMillisecond();

		// 随机到的世界点以及周边占用点信息
		Point pointInfo = null;
		try {
			// 计算玩家当前所占点坐标集合(包括城点中心点)
			int beforePos = getPlayerPos(player.getId());

			Set<Integer> currPointIds = new HashSet<Integer>();

			// 如果是出生城点
			if (isBornPos) {
				pointInfo = randomCityBornPoint();
			} else {// 非出生点需要忽略当前占用的点
				currPointIds.add(beforePos);
				List<Point> currOccupyPoints = WorldPointService.getInstance().getRhoAroundPointsAll(beforePos, GsConst.PLAYER_POINT_RADIUS);
				for (Point point : currOccupyPoints) {
					currPointIds.add(point.getId());
				}
			}

			if (pointInfo == null) {
				if (isBornPos) {
					logger.warn("can not find born point, start all map search !!!");
				}
				// 全地图随机
				List<AreaObject> areaList = new LinkedList<AreaObject>();
				areaList.addAll(WorldPointService.getInstance().getAreaVales());
				Collections.shuffle(areaList);
				for (AreaObject areaObj : areaList) {
					// 区域已满
					if (areaObj.getFreePointCount() <= 0) {
						continue;
					}
					// 随机一个点
					pointInfo = randomFreePoint(areaObj, WorldPointType.PLAYER, currPointIds, GameConstCfg.getInstance().getRandMinPt(), GameConstCfg.getInstance().getRandMaxPt());
					if (pointInfo != null) {
						break;
					}
				}
			}
			
			player.getData().updatePlayerPos(pointInfo.getX(), pointInfo.getY());
			
			return pointInfo;
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			long cost = HawkTime.getMillisecond() - startTime;
			if (pointInfo != null) {
				logger.info("player random settle point success, playerId: {}, pos: ({}, {}), isBornPos: {}, costtime: {}", player.getId(), pointInfo.getX(), pointInfo.getY(), isBornPos, cost);
			} else {
				logger.error("player random settle point failed, playerId: {}, isBornPos: {}, costtime: {}", player.getId(), isBornPos, cost);
			}
		}
		return null;
	}
	
	
	/**
	 * 盟主周围随机生成玩家城堡坐标
	 * 
	 * @param player
	 * @param protectedEndTime
	 * @param isBornPos
	 * @return
	 */
	public Point guildSettlePoint(Player player) {
		long startTime = HawkTime.getMillisecond();

		Point pointInfo = null;
		try {
			//获取盟主ID
			String leaderId = GuildService.getInstance().getGuildLeaderId(player.getGuildId());
			//盟主位置
			int leaderPos = WorldPlayerService.getInstance().getPlayerPos(leaderId);
			
			if(leaderPos <= 0){
				logger.info("[guildSettlePoint] leader posiont is 0, leaderId : {}", leaderId);
				return null;
			}
			
			// 计算玩家当前所占点坐标集合(包括城点中心点)
			int beforePos = getPlayerPos(player.getId());
			
			Set<Integer> currPointIds = new HashSet<Integer>();
			// 要忽略当前占用的点
			currPointIds.add(beforePos);
			List<Point> currOccupyPoints = WorldPointService.getInstance().getRhoAroundPointsAll(beforePos, GsConst.PLAYER_POINT_RADIUS);
			for (Point point : currOccupyPoints) {
				currPointIds.add(point.getId());
			}
			
			//按半径获取盟主周围所有的空闲点
			List<Point> points = WorldPointService.getInstance().getRhoAroundPointsFree(leaderPos, ConstProperty.getInstance().getAllianceTransfer());
			//打乱顺序
			Collections.shuffle(points);
			//遍历查找
			for (Point point : points) {
				if(!point.canPlayerSeat()){
					continue;
				}
				// 不能在危险区域
				if (WorldPointService.getInstance().isInCapitalArea(point.getId())) {
					continue;
				}
				if (tryPlayerOccupied(point, WorldPointType.PLAYER, currPointIds)) {
					pointInfo = point;
					break;
				}
			}
			
			return pointInfo;
		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			long cost = HawkTime.getMillisecond() - startTime;
			if (pointInfo != null) {
				logger.info("player random settle point success, playerId: {}, pos: ({}, {}), costtime: {}", player.getId(), pointInfo.getX(), pointInfo.getY(), cost);
			} else {
				logger.error("player random settle point failed, playerId: {}, costtime: {}", player.getId(), cost);
			}
		}
		return null;
	}
	

	/**
	 * 随机玩家出生城点
	 * 
	 * @return
	 */
	private Point randomCityBornPoint() {
		long startTime = HawkTime.getMillisecond();

		int areaPeopleLimit = WorldMapConstProperty.getInstance().getAreaPeopleLimit();
		int birthAreaNum = WorldMapConstProperty.getInstance().getBirthAreaNumber();

		// 城点随机刷新的区域
		List<Long> randomAreas = new ArrayList<Long>();
		for (long areaId : WorldMapConstProperty.getInstance().getBirthAreas()) {
			// 区域内玩家数量
			int playerCountInArea = getAreaPlayerCount(areaId);
			// 数量大于xx则不在此区域坐落
			if (playerCountInArea < areaPeopleLimit) {
				randomAreas.add(areaId);
			}
			// 随机<=四个区域
			if (randomAreas.size() >= birthAreaNum) {
				break;
			}
		}
		// 乱序
		Collections.shuffle(randomAreas);
		for (long areaId : randomAreas) {
			// 区域内可用点集合
			BlockingQueue<Point> areaPoints = getBornCityArea(areaId);
			// 占用或者不可占用的移除列表, 这里移除主要是为了后续城点占用效率考虑
			List<Point> removeList = new ArrayList<Point>();
			Point findPoint = null;
			// 遍历区域内所有城点, 寻找城点
			for (Point point : areaPoints) {
				removeList.add(point);
				// 城点占用 x + y为奇数
				if ((point.getX() + point.getY()) % 2 != 1) {
					continue;
				}
				// 若可以占领城点
				if (tryBornCityOccupied(point)) {
					addAreaPlayerCount(areaId, 1);
					findPoint = point;
					break;
				}
			}
			// 删除此区域不可用的点
			removeList.forEach(point -> areaPoints.remove(point));
			// 判断点
			if (findPoint != null) {
				int[] posAreaId = GameUtil.splitFromAndTo(areaId);
				long fromX = GameUtil.splitXAndY(posAreaId[0])[0];
				long fromY = GameUtil.splitXAndY(posAreaId[0])[1];
				long toX = GameUtil.splitXAndY(posAreaId[1])[0];
				long toY = GameUtil.splitXAndY(posAreaId[1])[1];
				int playerCount = getAreaPlayerCount(areaId);

				logger.info("born city in area, fromX: {}, fromY: {}, toX: {}, toY: {}, playerCount: {}, x: {}, y: {}, costtime: {}", fromX, fromY, toX, toY, playerCount, findPoint.getX(),
						findPoint.getY(), HawkTime.getMillisecond() - startTime);
				return findPoint;
			}
			// 此区域没有找到出生城点，把人数设置为最大，下次就不进这个区域找了
			setAreaPlayerCount(areaId, areaPeopleLimit);
		}
		return null;
	}

	/**
	 * 迁城到目标坐标点
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public WorldPoint mantualSettleCity(Player player, int x, int y, long protectedEndTime) {
		WorldPoint worldPoint = null;
		long startTime = HawkTime.getMillisecond();
		try {
			if (!checkPlayerCanOccupy(player, x, y)) {
				return null;
			}
			AreaObject areaObj = WorldPointService.getInstance().getArea(x, y);
			// 计算玩家当前所占点坐标集合(包括城点中心点)
			int[] playerPos = getPlayerPosXY(player.getId());
			// 生成玩家城堡占用点
			worldPoint = new WorldPoint(x, y, areaObj.getId(), WorldUtil.getPointResourceZone(x, y), WorldPointType.PLAYER_VALUE);

			worldPoint.initPlayerInfo(player.getData());
			worldPoint.setProtectedEndTime(protectedEndTime);

			// 删除原来的老位置
			WorldPointService.getInstance().removeWorldPoint(playerPos[0], playerPos[1]);
			// 创建玩家使用的世界点信息
			if (!WorldPointService.getInstance().createWorldPoint(worldPoint)) {
				logger.error("mantual settle point failed,create new pos failed, playerId: {}, pos: ({}, {})", player.getId(), x, y);
				return null;
			}

			// 如果在玩家出生城点区域
			for (Long areaId : WorldMapConstProperty.getInstance().getBirthAreas()) {
				int[] bornAreaId = GameUtil.splitFromAndTo(areaId);
				if (WorldUtil.isPointInArea(worldPoint.getId(), bornAreaId[0], bornAreaId[1])) {
					addAreaPlayerCount(areaId, 1);
				}
			}

			GuildManorService.getInstance().notifyManorBuffChange(player);
			return worldPoint;

		} catch (Exception e) {
			HawkException.catchException(e);
		} finally {
			long cost = HawkTime.getMillisecond() - startTime;
			if (worldPoint != null) {
				logger.info("player mantual settle point success, playerId: {}, pos: ({}, {}), costtime: {}", player.getId(), x, y, cost);
			} else {
				logger.warn("player mantual settle point failed, playerId: {}, pos: ({}, {}), costtime: {}", player.getId(), x, y, cost);
			}
		}
		return null;
	}

	/**
	 * 获得一个随即的空余点, 带着距离， 资源和怪物1个菱格，城堡4个，联盟塔9个
	 * 
	 * @return
	 */
	public Point randomFreePoint(AreaObject areaObj, WorldPointType type, Set<Integer> ignoreIds, Point minPt, Point maxPt) {
		// 临时循环列表，不满足拿出
		List<Point> validPoints = areaObj.getValidPoints(type.getNumber(), null);
		Collections.shuffle(validPoints);
		for (Point validPoint : validPoints) {
			// 随机的时候不在危险区域
			if (WorldPointService.getInstance().isInCapitalArea(validPoint.getId())) {
				continue;
			}
			// 最小点限制
			if (minPt != null) {
				if (validPoint.getX() < minPt.getX() || validPoint.getY() < minPt.getY()) {
					continue;
				}
			}
			// 最大点限制
			if (maxPt != null) {
				if (validPoint.getX() > maxPt.getX() || validPoint.getY() > maxPt.getY()) {
					continue;
				}
			}
			if (tryPlayerOccupied(validPoint, type, ignoreIds)) {
				return validPoint;
			}
		}
		return null;
	}

	/**
	 * 玩家出生城点尝试占用
	 * 
	 * @param centerPoint
	 * @return
	 */
	private boolean tryBornCityOccupied(Point centerPoint) {
		// 中心点是否被占用
		WorldPoint currPoint = WorldPointService.getInstance().getWorldPoint(centerPoint.getX(), centerPoint.getY());
		if (currPoint != null) {
			return false;
		}

		// 城点和资源点等的距离范围内 (1距离为4个点, 2距离为12个点, n距离为2n(n+1)个点)
		int peopleResourceDistance = WorldMapConstProperty.getInstance().getPeopleResourceDistance();
		List<Point> aroundPoints = WorldPointService.getInstance().getRhoAroundPointsFree(centerPoint.getX(), centerPoint.getY(), peopleResourceDistance);
		// 必须为2n(n+1)个点, 否则就是有阻挡点存在
		if (aroundPoints.size() != 2 * peopleResourceDistance * (peopleResourceDistance - 1)) {
			return false;
		}
		for (Point point : aroundPoints) {
			// 不在危险区域
			if (WorldPointService.getInstance().isInCapitalArea(point.getId())) {
				return false;
			}
		}

		// 每个玩家城点之间的距离范围内城点
		int peopleDistance = WorldMapConstProperty.getInstance().getPeopleDistance();
		aroundPoints = WorldPointService.getInstance().getRhoAroundPointsAll(centerPoint.getX(), centerPoint.getY(), peopleDistance);
		for (Point point : aroundPoints) {
			WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(point.getId());
			if (worldPoint != null && (worldPoint.getPointType() == WorldPointType.PLAYER_VALUE)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 创建玩家的新手世界资源
	 * 
	 * @param playerId
	 */
	public WorldPoint createNewlyData(String playerId) {
		int playerPointId = getPlayerPos(playerId);
		if (playerPointId <= 0) {
			logger.error("createNewlyData error, playerPointId is 0!");
			return null;
		}
		// 范围内寻找。 如果已经有专属野怪，则不生成，直接返回
		int maxViewRadius = Math.max(GameConstCfg.getInstance().getViewXRadius(), GameConstCfg.getInstance().getViewYRadius());
		List<Point> aroundPoints = WorldPointService.getInstance().getRhoAroundPointsAll(playerPointId, maxViewRadius);
		for (Point point : aroundPoints) {
			WorldPoint wp = WorldPointService.getInstance().getWorldPoint(point.getId());
			if (wp == null) {
				continue;
			}
			String ownerId = wp.getOwnerId();
			if (!HawkOSOperator.isEmptyString(ownerId) && ownerId.equals(playerId)) {
				return wp;
			}
		}

		WorldEnemyCfg enemyCfg = HawkConfigManager.getInstance().getConfigByIndex(WorldEnemyCfg.class, 0);
		Point point = randomNewlyDataPoint(playerId, WorldPointType.ROBOT);
		if (point == null) {
			logger.error("createNewlyData error, point is null!");
			return null;
		}

		WorldPoint worldPoint = new WorldPoint(point.getX(), point.getY(), point.getAreaId(), point.getZoneId(), WorldPointType.ROBOT_VALUE);
		worldPoint.setOwnerId(playerId);
		worldPoint.setMonsterId(enemyCfg.getId());
		worldPoint.initMonsterBlood(enemyCfg.randomHp());
		worldPoint.setLifeStartTime(HawkTime.getMillisecond());

		if (!WorldPointService.getInstance().createWorldPoint(worldPoint)) {
			logger.error("createNewlyData error, create point failed!");
			return null;
		}
		logger.info("createNewlyData, playerId:{}, x:{}, y:{}", playerId, worldPoint.getX(), worldPoint.getY());
		return worldPoint;
	}

	/**
	 * 随机一个新手数据点
	 * 
	 * @param playerId
	 * @param pointType
	 * @return
	 */
	public Point randomYuriStrikePoint(String playerId, WorldPointType pointType, int enemyDistance) {
		int playerPosId = getPlayerPos(playerId);
		int[] playerPos = GameUtil.splitXAndY(playerPosId);
		
		// 距离判断
		int[] disArr = WorldMapConstProperty.getInstance().getWorldSearchRadius();
		int maxViewRadius = disArr[disArr.length - 1];
		
		List<Point> aroundPoints = WorldPointService.getInstance().getRhoAroundPointsFree(playerPosId, maxViewRadius);
		for (Point validPoint : aroundPoints) {
			int validId = validPoint.getId();
			if (WorldPointService.getInstance().isInCapitalArea(validId)) {
				continue;
			}
			int[] validPos = GameUtil.splitXAndY(validId);
			if (Math.abs(validPos[0] - playerPos[0]) <= enemyDistance || Math.abs(validPos[1] - playerPos[1]) <= enemyDistance) {
				continue;
			}
			if (!validPoint.canRMSeat()) {
				continue;
			}
			return validPoint;
		}
		return null;
	}
	
	/**
	 * 随机一个新手数据点
	 * 
	 * @param playerId
	 * @param pointType
	 * @return
	 */
	public Point randomNewlyDataPoint(String playerId, WorldPointType pointType) {
		int playerPosId = getPlayerPos(playerId);
		int[] playerPos = GameUtil.splitXAndY(playerPosId);
		int maxViewRadius = Math.max(GameConstCfg.getInstance().getViewXRadius(), GameConstCfg.getInstance().getViewYRadius());
		List<Point> aroundPoints = WorldPointService.getInstance().getRhoAroundPointsFree(playerPosId, maxViewRadius);
		for (Point validPoint : aroundPoints) {
			int validId = validPoint.getId();
			if (WorldPointService.getInstance().isInCapitalArea(validId)) {
				continue;
			}
			int[] validPos = GameUtil.splitXAndY(validId);
			if (Math.abs(validPos[0] - playerPos[0]) <= 3 || Math.abs(validPos[1] - playerPos[1]) <= 3) {
				continue;
			}
			if (!validPoint.canRMSeat()) {
				continue;
			}
			return validPoint;
		}
		return null;
	}

	/**
	 * 设置玩家点活跃状态
	 * 
	 * @param playerId
	 * @return
	 */
	public void setPlayerPointActive(String playerId) {
		int pointId = getPlayerPos(playerId);
		if (pointId > 0) {
			WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(pointId);
			if (worldPoint != null) {
				worldPoint.setLastActiveTime(HawkTime.getMillisecond());
			}
		}
	}
	
	/**
	 * 检测过期城点
	 */
	public void chickTimeoutCity() {
		// 检测过期城点周期(s)
		long checkTimeOutCityPeroid = GameConstCfg.getInstance().getCheckTimeOutCityPeroid();
		// 检测过期城点起始时间
		int checkTimeOutCityHourA = GameConstCfg.getInstance().getCheckTimeOutCityHourA();
		// 检测过期城点结束时间
		int checkTimeOutCityHourB = GameConstCfg.getInstance().getCheckTimeOutCityHourB();
		// 检测过期城点数量
		int checkTimeOutCityCount = GameConstCfg.getInstance().getCheckTimeOutCityCount();
		
		// 3分钟检测一次
		long currentTime = HawkTime.getMillisecond();
		if (currentTime - checkTimeOutCity < checkTimeOutCityPeroid) {
			return;
		}
		checkTimeOutCity = HawkTime.getMillisecond();
		
		// 只在每天2~7点这个时间段清理
		int currentHour = HawkTime.getHour();
		if (currentHour < checkTimeOutCityHourA || currentHour > checkTimeOutCityHourB) {
			return;
		}
		
		Set<Integer> timeoutPoints = new HashSet<Integer>();
		try {
			// 遍历计算过期世界点信息
			List<WorldPoint> worldPointList = WorldPointService.getInstance().getWorldPointsByType(WorldPointType.PLAYER);
			for (WorldPoint worldPoint : worldPointList) {
				if (!HawkOSOperator.isEmptyString(worldPoint.getPlayerId()) && checkRemoveTimeoutCity(worldPoint)) {
					timeoutPoints.add(worldPoint.getId());
					// 每次清理30个
					if (timeoutPoints.size() >= checkTimeOutCityCount) {
						break;
					}
					logger.info("player world point timeout, playerId: {}, pointId: {}", worldPoint.getPlayerId(), worldPoint.getId());
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		// 移除过期的世界点坐标
		WorldPointService.getInstance().removeWorldPoints(timeoutPoints, true);

		// 记录日志信息
		logger.info("remove timeout city success, count: {}, costtime: {}", timeoutPoints.size(), HawkTime.getMillisecond() - currentTime);
	}
	
	/**
	 * 判断城点是否可以移除
	 * @param worldPoint
	 * @return
	 */
	private boolean checkRemoveTimeoutCity(WorldPoint worldPoint) {
		long offlineTime = HawkTime.getMillisecond() - worldPoint.getLastActiveTime();
		
		// 对城堡等级和清理时间进行判断
		int cityLevel = ConstProperty.getInstance().getRebuildLevel();
		long cityCleanTime = ConstProperty.getInstance().getRebuildTime() * 1000L;
		if (worldPoint.getCityLevel() < cityLevel && offlineTime >= cityCleanTime) {
			removeCity(worldPoint.getPlayerId(), true);
			return true;
		}
		
		return false;
	}
	
	/**
	 * 玩家迁移
	 * @param playerId
	 */
	@MessageHandler
	public void migratePlayer(MigrateOutPlayerMsg msg) {
		msg.setResult(Boolean.FALSE);
		WorldPoint wp = this.getPlayerWorldPoint(msg.getPlayer().getId());
		wp.delete(false);
		msg.setResult(Boolean.TRUE);
	}
	

	/**
	 * 重置玩家城点着火状态
	 */
	public void resetCityFireStatus(Player player, long endTime) {
		int pointId = getPlayerPos(player.getId());
		if (pointId <= 0) {
			return;
		}
		
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(pointId);
		if (worldPoint == null || !WorldUtil.isPlayerPoint(worldPoint)) {
			logger.error("reset city fire status error, playerId: {}, pointId: {}, point: {}", player.getId(), pointId, worldPoint);
			return;
		}
		
		worldPoint.setCommonHurtEndTime(endTime);
		WorldPointService.getInstance().getWorldScene().update(worldPoint.getAoiObjId());
	}
	
	
	public void initGuildAutoMoveArea(){
		Map<Integer,List<Point>> map = new ConcurrentHashMap<Integer, List<Point>>();
		List<HawkTuple3<Integer, Integer, Integer>>  list = WorldMapConstProperty.getInstance().getGuildAutoMoveArea();
		for(int i=0;i<list.size();i++){
			HawkTuple3<Integer, Integer, Integer> tuple = list.get(i);
			List<Point> plist = new ArrayList<>();
			List<Point> aroundPoints = WorldPointService.getInstance().getRhoAroundPointsAll(tuple.first,tuple.second,tuple.third);
			for(Point point : aroundPoints){
				if ((point.getX() + point.getY()) % 2 == 0) {
					continue;
				}
				plist.add(point);
			}
			map.put(i, plist);
		}
		this.guildAutoMoveArea = map;
		this.guildAutomoveParam = new AtomicLong(1);
	}
	
	
	public void randomGuildCreatePoint(Player player){
		long curTime = HawkTime.getMillisecond();
		long serverOpenTime = GameUtil.getServerOpenTime();
		if(curTime > serverOpenTime + 
				WorldMapConstProperty.getInstance().getGuildAutoMoveOpenTimeLimt() * 1000l){
			return;
		}
		
		if(player.getCityLevel() >= WorldMapConstProperty.getInstance().getGuildAutoMoveLevelLimt()){
			return;
		}
		
		int cnt = player.getData().getPlayerOtherEntity().getAutoGuildCityMoveCnt();
		if(cnt >= WorldMapConstProperty.getInstance().getGuildAutoMoveCount()){
			return;
		}
		if(this.guildAutoMoveArea.isEmpty()){
			return;
		}
		long param = this.guildAutomoveParam.incrementAndGet();
		long size = this.guildAutoMoveArea.size();
		int areaId = (int) (param % size);
		List<Point> list = this.guildAutoMoveArea.get(areaId);
		List<Point> randoms = new ArrayList<>();
		for(Point point : list){
			if(point.canPlayerSeat()){
				randoms.add(point);
			}
		}
		if(randoms.size() <= 0){
			return;
		}
		
		Collections.shuffle(randoms);
		for(Point point : randoms){
			if(!point.canPlayerSeat()){
				continue;
			}
			if(!WorldPlayerService.getInstance().checkPlayerCanOccupy(player, point.getX(), point.getY())) {
				continue;
			}
			PlayerWorldModule worldModule = player.getModule(GsConst.ModuleType.WORLD_MODULE);
			worldModule.moveCity(CityMoveType.GUILD_CREATE_AUTO_MOVE_VALUE, point.getX(), point.getY(), false, true);
			return;
		}
		
		
	}
	
	
	
	public void randomGuildJoinPoint(String leaderId,Player player){
		long curTime = HawkTime.getMillisecond();
		long serverOpenTime = GameUtil.getServerOpenTime();
		if(curTime > serverOpenTime + 
				WorldMapConstProperty.getInstance().getGuildAutoMoveOpenTimeLimt() * 1000l){
			return;
		}
		
		if(player.getCityLevel() >= WorldMapConstProperty.getInstance().getGuildAutoMoveLevelLimt()){
			return;
		}
		
		int leaderPos = this.getPlayerPos(leaderId);
		if(leaderPos <= 0){
			return;
		}
		int cnt = player.getData().getPlayerOtherEntity().getAutoGuildCityMoveCnt();
		if(cnt >= WorldMapConstProperty.getInstance().getGuildAutoMoveCount()){
			return;
		}
		List<Point> aroundPoints = WorldPointService.getInstance().getRhoAroundPointsFree(leaderPos,
				WorldMapConstProperty.getInstance().getGuildAutoMoveLeaderRadius());
		
		int[] leaderPosArr = GameUtil.splitXAndY(leaderPos);
		Point targetPoint = null;
		double disMax = Double.MAX_VALUE;
		for(Point point : aroundPoints){
			if(!point.canPlayerSeat()){
				continue;
			}
			if(!WorldPlayerService.getInstance().checkPlayerCanOccupy(player, point.getX(), point.getY())) {
				continue;
			}
			double dis = WorldUtil.distance(leaderPosArr[0], leaderPosArr[1], point.getX(), point.getY());
			if(dis < disMax ){
				targetPoint = point;
				disMax = dis;
			}
		}
		if(Objects.nonNull(targetPoint)){
			PlayerWorldModule worldModule = player.getModule(GsConst.ModuleType.WORLD_MODULE);
			worldModule.moveCity(CityMoveType.GUILD_JOIN_AUTO_MOVE_VALUE, targetPoint.getX(), targetPoint.getY(), false, true);
		}
		
	}
}
