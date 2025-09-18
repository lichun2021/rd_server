package com.hawk.game.world.service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.uuid.HawkUUIDGenerator;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.type.ActivityState;
import com.hawk.game.GsApp;
import com.hawk.game.config.WorldGundamCfg;
import com.hawk.game.config.WorldGundamRefreshCfg;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.GundamInfo;
import com.hawk.game.protocol.World.GundamPush;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.object.AreaObject;
import com.hawk.game.world.object.Point;

/**
 * 机甲-服务类
 * @author golden
 *
 */
public class WorldGundamService extends HawkAppObj {
	
	/**
	 * 日志
	 */
	public static Logger logger = LoggerFactory.getLogger("Server");

	/**
	 * 实例
	 */
	private static WorldGundamService instance = null;
	
	/**
	 * 世界上机甲点列表
	 */
	public Map<String, WorldPoint> gundams;

	/**
	 * 获取实例
	 */
	public static WorldGundamService getInstance() {
		return instance;
	}
	
	/**
	 * 构造器
	 */
	public WorldGundamService(HawkXID xid) {
		super(xid);
		instance = this;
	}
	
	/**
	 * 初始化
	 */
	public boolean init() {
		gundams = new ConcurrentHashMap<String, WorldPoint>();
		
		List<WorldPoint> points = WorldPointService.getInstance().getWorldPointsByType(WorldPointType.GUNDAM);
		for (WorldPoint point : points) {
			gundams.put(getGundamUuid(point), point);
			logger.info("initGundam, x:{}, y:{}, gundamId:{}", point.getX(), point.getY(), point.getMonsterId());
		}
		
		return true;
	}

	/**
	 * 通知机甲被击杀
	 */
	public void notifyGundamKilled(int pointId) {
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(pointId);
		gundams.remove(getGundamUuid(point));
		
		WorldPointService.getInstance().removeWorldPoint(pointId, true);
		
		logger.info("removegundams, isRemoveAll:{}, pointId:{}, x:{}, y:{}, blood:{}, gundamId:{}", false, point.getId(), point.getX(), point.getY(), point.getRemainBlood(), point.getMonsterId());
	}
	
	/**
	 * 活动是否开启
	 * @return
	 */
	public boolean isActivityOpen() {
		Optional<ActivityBase> activity = ActivityManager.getInstance().getGameActivityByType(ActivityType.MACHINE_AWAKE_VALUE);
		return activity.isPresent() && activity.get().getActivityEntity().getActivityState() == ActivityState.OPEN;
	}
	
	/**
	 * 刷新机甲
	 */
	public void refreshGundam() {
		removeAllGundam();
		
		if (isActivityOpen()) {
			genAllGundam();
		}
	}
	
	/**
	 * 移除所有机甲
	 */
	public void removeAllGundam() {
		if (!hasGundamInWorld()) {
			return;
		}
		
		for (WorldPoint point : gundams.values()) {
			logger.info("removegundams, isRemoveAll:{}, pointId:{}, x:{}, y:{}, blood:{}, gundamId:{}", true, point.getId(), point.getX(), point.getY(), point.getRemainBlood(), point.getMonsterId());
			WorldPointService.getInstance().removeWorldPoint(point.getId(), true);
		}
		
		gundams = new ConcurrentHashMap<>();
		
		logger.info("removeallgundams...");
	}
	
	/**
	 * 生成所有机甲
	 */
	public void genAllGundam() {
		List<int[]> posArr = WorldMapConstProperty.getInstance().getGundamRefreshPosArr();
		for (int[] pos : posArr) {
			int pointId = GameUtil.combineXAndY(pos[0], pos[1]);
			genGundam(pointId);
		}
		
		ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SPECIAL_BROADCAST, Const.NoticeCfgId.WORLD_GUNDAM_REFRESH, null);
		
		LocalRedis.getInstance().setGundamRefreshUuid(HawkUUIDGenerator.genUUID());
	}
	
	/**
	 * 生成机甲
	 */
	public void genGundam(int pointId) {
		
		// 机甲刷新区域半径(以坐标为中心，半径内区域随机生成点)
		int areaRadius = WorldMapConstProperty.getInstance().getGundamRefreshAreaRadius();
		
		List<Point> areaPoints = WorldPointService.getInstance().getRhoAroundPointsFree(pointId, areaRadius);
		
		Collections.shuffle(areaPoints);
		
		for (Point point : areaPoints) {
			
			AreaObject area = WorldPointService.getInstance().getArea(point.getAreaId());
			
			if (!point.canGundamSeat()) {
				continue;
			}
			
			if (!WorldPointService.getInstance().tryOccupied(area, point, GsConst.GUNDAM_RADIUS)) {
				continue;
			}
			
			if (WorldPointService.getInstance().getWorldPoint(point.getId()) != null) {
				continue;
			}
			
			WorldPoint worldPoint = new WorldPoint(point.getX(), point.getY(), point.getAreaId(), point.getZoneId(), WorldPointType.GUNDAM_VALUE);
			int gundamId = getRefresGundamId();
			worldPoint.setMonsterId(gundamId);
			worldPoint.setRemainBlood(getGundamInitBlood(gundamId));
			
			if (!WorldPointService.getInstance().createWorldPoint(worldPoint)) {
				logger.error("gengundam error, create failed, x:{}, y:{}, areaId:{}, gundamId:{}", point.getX(), point.getY(), point.getAreaId(), gundamId);
				continue;
			}
			
			gundams.put(getGundamUuid(worldPoint), worldPoint);
			
			logger.info("gengundam, x:{}, y:{}, areaId:{}, blood:{}, gundamId:{}", point.getX(), point.getY(), point.getAreaId(), worldPoint.getRemainBlood(), gundamId);
			
			break;
		}
	}
	
	/**
	 * 获取可以刷新的机甲Id
	 */
	public int getRefresGundamId() {
		long serverOpenTime = HawkApp.getInstance().getCurrentTime() - GsApp.getInstance().getServerOpenTime();
		
		WorldGundamRefreshCfg returnCfg = HawkConfigManager.getInstance().getConfigByIndex(WorldGundamRefreshCfg.class, 0);
		
		ConfigIterator<WorldGundamRefreshCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(WorldGundamRefreshCfg.class);
		while (configIterator.hasNext()) {
			WorldGundamRefreshCfg cfg = configIterator.next();
			if (serverOpenTime >= cfg.getServerOpenTime()) {
				returnCfg = cfg;
			}
		}
		
		return returnCfg.getGundamId();
	}
	
	/**
	 * 获取所有高达点
	 */
	public Collection<WorldPoint> getGundams() {
		return gundams.values();
	}
	
	/**
	 * 获取机甲初始血量
	 */
	public int getGundamInitBlood(int gundamId) {
		WorldGundamCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WorldGundamCfg.class, gundamId);
		if (cfg == null) {
			return 0;
		}
		
		int blood = 0;
		List<ArmyInfo> armyList = cfg.getArmyList();
		for (ArmyInfo army : armyList) {
			blood += army.getTotalCount();
		}
		
		return blood;
	}
	
	/**
	 * 世界上是否有高达
	 * @return
	 */
	public boolean hasGundamInWorld() {
		return gundams.size() > 0;
	}
	
	/**
	 * 高达id
	 * @param point
	 * @return
	 */
	public String getGundamUuid(WorldPoint point) {
		return point.getId() + ":" + point.getCreateTime();
	}
	
	/**
	 * 高达刷新uuid
	 * @param point
	 * @return
	 */
	public String getGundamRefreshUuid() {
		return LocalRedis.getInstance().getGundamRefreshUuid();
	}
	
	/**
	 * 推送高达信息
	 * @param player
	 */
	public void pushGundamInfo(Player player) {
		GundamPush.Builder builder = GundamPush.newBuilder();
		if (!isActivityOpen()) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.GUNDAM_INFO_RESP, builder));
		}
		
		Map<String, String> atkTimesMap = LocalRedis.getInstance().getAllAtkGundamTimes(player.getId());
		String gundamRefreshUuid = WorldGundamService.getInstance().getGundamRefreshUuid();
		
		for (WorldPoint gundam : gundams.values()) {
			GundamInfo.Builder gundamInfo = GundamInfo.newBuilder();
			gundamInfo.setPosX(gundam.getX());
			gundamInfo.setPosY(gundam.getY());
			
			String times = atkTimesMap.get(gundamRefreshUuid);
			if (HawkOSOperator.isEmptyString(times)) {
				gundamInfo.setAtkTimes(0);
			} else {
				gundamInfo.setAtkTimes(Integer.valueOf(times));
			}
			
			builder.addGundam(gundamInfo);
		}
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GUNDAM_INFO_RESP, builder));
	}
}
