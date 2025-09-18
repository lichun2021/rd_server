package com.hawk.game.world.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple3;
import org.hawk.xid.HawkXID;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.CakeShareRefreshEvent;
import com.hawk.activity.event.impl.DragonBoatRefreshEvent;
import com.hawk.activity.type.impl.cakeShare.CakeShareActivity;
import com.hawk.activity.type.impl.cakeShare.cfg.CakeShareKVCfg;
import com.hawk.activity.type.impl.drogenBoatFestival.gift.DragonBoatGiftActivity;
import com.hawk.activity.type.impl.drogenBoatFestival.gift.cfg.DragonBoatGiftKVCfg;
import com.hawk.game.config.GhostTowerCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.object.AreaObject;
import com.hawk.game.world.object.CakeShareInfo;
import com.hawk.game.world.object.DragonBoatInfo;
import com.hawk.game.world.object.Point;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;

/**
 * 资源宝库
 */
public class WorldResTreasurePointService extends HawkAppObj {

	/**
	 * tick周期
	 */
	private static final long TICKPERIOD = 3000L;

	/**
	 * 上次tick时间
	 */
	private long lastTickTime = 0L;

	/**
	 * 幽灵工厂上传tick时间点
	 */
	private long lastGhostTowerTickTime = 0L;
	/**
	 * 幽灵怪点缓存
	 */
	private Map<String,Integer> ghostPoints = new ConcurrentHashMap<String, Integer>();
	
	
	private WorldPoint dragonBoatPoint;
	
	private WorldPoint cakePoint;
	
	private static WorldResTreasurePointService instance = null;

	public static WorldResTreasurePointService getInstance() {
		return instance;
	}

	public WorldResTreasurePointService(HawkXID xid) {
		super(xid);
		instance = this;
		long currentTime = HawkTime.getMillisecond();
		lastTickTime = currentTime;
	}

	@Override
	public boolean onTick() {
		long currentTime = HawkTime.getMillisecond();
		if (currentTime - lastTickTime < TICKPERIOD) {
			return true;
		}
		long lastTickTimeTemp = this.lastTickTime;
		lastTickTime = currentTime;

		List<WorldPoint> resTrPoints = WorldPointService.getInstance().getWorldPointsByType(WorldPointType.RESOURC_TRESURE);
		for (WorldPoint point : resTrPoints) {
			if (point.getProtectedEndTime() < currentTime) {
				deletePoint(point);
			}
		}
		//龙船tick
		this.dragonBoatTick(lastTickTimeTemp,currentTime);
		//幽灵工厂tick
		this.ghostTowerMonsterTick(currentTime);
		//共享蛋糕tick
		this.cakeShareTick(lastTickTimeTemp, currentTime);
		return true;
	}

	public void recordGhostPoint(String playerId,int pointId){
		this.ghostPoints.put(playerId, pointId);
	}
	
	public int getGhostPoint(String playerId){
		if(!this.ghostPoints.containsKey(playerId)){
			return 0;
		}
		return this.ghostPoints.get(playerId);
	}
	public void ghostTowerMonsterTick(long currentTime){
		int tickInterval = 60 * 1000;   //每分钟tick一次
		if(currentTime  -  this.lastGhostTowerTickTime <tickInterval){
			return;
		}
		this.lastGhostTowerTickTime = currentTime;
		List<WorldPoint> points = WorldPointService.getInstance().getWorldPointsByType(WorldPointType.GHOST_TOWER_MONSTER);
		for(WorldPoint point : points){
			if(point.getPointType() != WorldPointType.GHOST_TOWER_MONSTER_VALUE){
				continue;
			}
			GhostTowerCfg cfg = HawkConfigManager.getInstance().
					getConfigByKey(GhostTowerCfg.class, point.getMonsterId());
			int ghostLiftTime = 0;
			if(cfg != null){
				ghostLiftTime = cfg.getGhostLiftTime() * 1000;
			}
			//如果达到移除时间，查看是否有正在前往的行军，如果有则保留
			if(currentTime > point.getLifeStartTime() + ghostLiftTime){
				String playerId = point.getOwnerId();
				int ghostId = point.getMonsterId();
				boolean hasMarch = false;
				BlockingQueue<IWorldMarch> playerMarchs = WorldMarchService.getInstance().getPlayerMarch(playerId);
				for (IWorldMarch iWorldMarch : playerMarchs) {
					if(iWorldMarch.getMarchType() == WorldMarchType.GHOST_TOWER_MARCH &&
							!iWorldMarch.isReturnBackMarch()){
						hasMarch = true;
						break;
					}
				}
				if(!hasMarch){
					SystemMailService.getInstance().sendMail(MailParames.newBuilder()
							.setPlayerId(playerId)
							.setMailId(MailId.GHOST_TOWER_MONSTER_ATTACK_FAILED_CHANGED)
							.addContents(ghostId)
							.build());
					deletePoint(point);
				}
			}
			
		}
		
		
	}
	
	/**
	 * 龙船tick
	 * @param lastTickTimeTemp
	 * @param currentTime
	 */
	public void dragonBoatTick(long lastTickTimeTemp,long currentTime){
		List<WorldPoint> drogenBoat = WorldPointService.getInstance().getWorldPointsByType(WorldPointType.DRAGON_BOAT);
		Optional<ActivityBase> dbGiftboatActivityOP = ActivityManager.getInstance().getActivity(Activity.ActivityType.DRAGON_BOAT_GIFT_VALUE);
		if (dbGiftboatActivityOP.isPresent()) {
			DragonBoatGiftActivity activity = (DragonBoatGiftActivity) dbGiftboatActivityOP.get();
			boolean opening = activity.isOpening("");
			//创建龙船
			if (opening && drogenBoat.isEmpty()) {
				long boatId = activity.getDragonBoatId();
				Map<String,String> awardMap = activity.getDragonBoatGiftRecord(boatId);
				this.dragonBoatPoint = createDragonBoat(boatId,awardMap);
				Set<String> players = GlobalData.getInstance().getOnlinePlayerIds();
				for(String pid : players){
					DragonBoatRefreshEvent event = new DragonBoatRefreshEvent(pid,boatId);
					ActivityManager.getInstance().postEvent(event);
				}
			}
			//更新龙船
			if(opening && !drogenBoat.isEmpty()){
				this.updateDragonBoat(lastTickTimeTemp, currentTime, drogenBoat,activity);
			}
			//删除龙船
			if (!opening && !drogenBoat.isEmpty()) {
				drogenBoat.forEach(this::deletePoint);
				this.dragonBoatPoint = null;
			}
		}
	}
	
	
	private void updateDragonBoat(long lastUpdateTime,long curTime,List<WorldPoint> points,DragonBoatGiftActivity activity){
		if(this.dragonBoatPoint == null && points.size() > 0){
			for(int i=0;i<points.size();i++ ){
				if(i == 0){
					this.dragonBoatPoint = points.get(i);
					continue;
				}
				this.deletePoint(points.get(i));
			}
		}
		if(this.dragonBoatPoint == null){
			return;
		}
		DragonBoatGiftKVCfg config = HawkConfigManager.getInstance().
				getKVInstance(DragonBoatGiftKVCfg.class);
		boolean update = false;
		long zeroTime = HawkTime.getAM0Date().getTime();
		for(int hour : config.getRefreshTimeList()){
			long refreshTime = zeroTime + HawkTime.HOUR_MILLI_SECONDS *hour;
			if(lastUpdateTime<= refreshTime && refreshTime < curTime){
				update = true;
				break;
			}
		}
		if(update){
			long boatId = activity.getDragonBoatId();
			DragonBoatInfo info = new DragonBoatInfo();
			info.setBoatId(boatId);
			info.addAwardRecord(null);
			this.dragonBoatPoint.setDragonBoatInfo(info);
			Set<String> players = GlobalData.getInstance().getOnlinePlayerIds();
			for(String pid : players){
				DragonBoatRefreshEvent event = new DragonBoatRefreshEvent(pid,boatId);
				ActivityManager.getInstance().postEvent(event);
			}
			//世界广播
			ChatParames.Builder chatParames = ChatParames.newBuilder();
			chatParames.setKey(NoticeCfgId.DRAGON_BOAT_GIFT_REFRESH);
			chatParames.setChatType(ChatType.SPECIAL_BROADCAST);
			ChatService.getInstance().addWorldBroadcastMsg(chatParames.build());
		}
	}
	
	public WorldPoint getDragonBoatPoint(){
		return this.dragonBoatPoint;
	}
	
	
	
	/**
	 * 生成野怪
	 */
	private WorldPoint createDragonBoat(long boatId,Map<String,String> awardPlayers) {
		DragonBoatGiftKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(DragonBoatGiftKVCfg.class);
		if(cfg == null){
			return null;
		}
		List<Point> points = WorldPointService.getInstance().
				getRhoAroundPointsFree(cfg.getDragonPointX(), cfg.getDragonPointY(), cfg.getAreaRadius());
		for (Point point : points) {
			AreaObject area = WorldPointService.getInstance().getArea(point.getAreaId());
			
			if (!point.canYuriSeat()) {
				continue;
			}
			
			if (!WorldPointService.getInstance().tryOccupied(area, point, GsConst.PLAYER_POINT_RADIUS)) {
				continue;
			}
			
			if (WorldPointService.getInstance().getWorldPoint(point.getId()) != null) {
				continue;
			}
			
			if (WorldPointService.getInstance().isInCapitalArea(point.getId())) {
				continue;
			}
			DragonBoatInfo info = new DragonBoatInfo();
			info.setBoatId(boatId);
			info.addAwardRecord(awardPlayers);
			
			WorldPoint worldPoint = new WorldPoint(point.getX(), point.getY(), 
					point.getAreaId(), point.getZoneId(), WorldPointType.DRAGON_BOAT_VALUE);
			worldPoint.setResourceId(1);
			worldPoint.setPersistable(false);
			worldPoint.setDragonBoatInfo(info);
			WorldPointService.getInstance().addPoint(worldPoint);
			HawkLog.logPrintln("createDrogenBoatGiftboat, x:{}, y:{}, areaId:{}, blood:{}", point.getX(), point.getY(), point.getAreaId(), worldPoint.getRemainBlood());
			return worldPoint;
		}
		return null;
	}

	public void deletePoint(WorldPoint point) {
		WorldTask task = new WorldTask(GsConst.WorldTaskType.RES_TREASURE_DEL) {
			@Override
			public boolean onInvoke() {
				WorldPointService.getInstance().removeWorldPoint(point.getX(), point.getY());
				return true;
			}
		};
		long worldThreadId = WorldThreadScheduler.getInstance().getThreadState().getIntValue("threadId");
		long currThreadId = HawkOSOperator.getThreadId();
		if (currThreadId != worldThreadId) {
			WorldThreadScheduler.getInstance().postWorldTask(task);
		} else {
			task.onInvoke();
		}
	}
	
	
	////蛋糕同享 野怪
	
	
	/**
	 * 蛋糕同享 tick
	 */
	public void cakeShareTick(long lastTickTimeTemp,long currentTime){
		List<WorldPoint> cakeShare = WorldPointService.getInstance().getWorldPointsByType(WorldPointType.CAKE_SHARE);
		Optional<ActivityBase> cakeShareActivityOp = ActivityManager.getInstance().getActivity(Activity.ActivityType.CAKE_SHARE_VALUE);
		if (cakeShareActivityOp.isPresent()) {
			CakeShareActivity activity = (CakeShareActivity) cakeShareActivityOp.get();
			boolean opening = activity.isOpening("");
			//创建蛋糕
			if (opening && cakeShare.isEmpty()) {
				HawkTuple3<Integer, Long, Long> cakeInfo = activity.getAwardTurn();
				Map<String,String> awardMap = activity.getCakeShareRecord(cakeInfo.first);
				this.cakePoint = createCakeShare(cakeInfo.first,cakeInfo.second,cakeInfo.third,awardMap);
				Set<String> players = GlobalData.getInstance().getOnlinePlayerIds();
				for(String pid : players){
					CakeShareRefreshEvent event = new CakeShareRefreshEvent(pid,cakeInfo.first);
					ActivityManager.getInstance().postEvent(event);
				}
			}
			//更新蛋糕
			if(opening && !cakeShare.isEmpty()){
				this.updateCakeShare(lastTickTimeTemp, currentTime, cakeShare,activity);
			}
			//删除龙船
			if (!opening && !cakeShare.isEmpty()) {
				cakeShare.forEach(this::deletePoint);
				this.cakePoint = null;
			}
		}
	}
	
	
	/**更新蛋糕信息
	 * @param lastUpdateTime
	 * @param curTime
	 * @param points
	 * @param activity
	 */
	private void updateCakeShare(long lastUpdateTime,long curTime,List<WorldPoint> points,CakeShareActivity activity){
		if(this.cakePoint == null && points.size() > 0){
			for(int i=0;i<points.size();i++ ){
				if(i == 0){
					this.cakePoint = points.get(i);
					continue;
				}
				this.deletePoint(points.get(i));
			}
		}
		if(this.cakePoint == null){
			return;
		}
		//刷新
		HawkTuple3<Integer, Long, Long> cakeInfo = activity.getAwardTurn();
		if(this.cakePoint.getCakeShareInfo() == null || 
				this.cakePoint.getCakeShareInfo().getCakeId() != cakeInfo.first){
			CakeShareInfo info = new CakeShareInfo();
			info.setCakeId(cakeInfo.first);
			info.setStartTime(cakeInfo.second);
			info.setEndTime(cakeInfo.third);
			info.addAwardRecord(null);
			this.cakePoint.setCakeShareInfo(info);
			Set<String> players = GlobalData.getInstance().getOnlinePlayerIds();
			for(String pid : players){
				CakeShareRefreshEvent event = new CakeShareRefreshEvent(pid,cakeInfo.first);
				ActivityManager.getInstance().postEvent(event);
			}
		}
		//蛋糕开始拿奖励
		if(lastUpdateTime < this.cakePoint.getCakeShareInfo().getStartTime() &&
				curTime >= this.cakePoint.getCakeShareInfo().getStartTime()){
			//世界广播  
			ChatParames.Builder chatParames = ChatParames.newBuilder();
			chatParames.setKey(NoticeCfgId.CAKE_SHARE_GIFT_REFRESH);
			chatParames.setChatType(ChatType.SPECIAL_BROADCAST);
			ChatService.getInstance().addWorldBroadcastMsg(chatParames.build());
			
			Set<String> players = GlobalData.getInstance().getOnlinePlayerIds();
			for(String pid : players){
				CakeShareRefreshEvent event = new CakeShareRefreshEvent(pid,cakeInfo.first);
				ActivityManager.getInstance().postEvent(event);
			}
		}
	}
	
	public WorldPoint getCakeSharePoint(){
		return this.cakePoint;
	}
	
	
	/**
	 * 生成野怪
	 */
	private WorldPoint createCakeShare(int cakeId,long startTime,long endTime,Map<String,String> awardPlayers) {
		CakeShareKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(CakeShareKVCfg.class);
		if(cfg == null){
			return null;
		}

		List<HawkTuple2<Integer, Integer>> poingList = cfg.getPointList();
		//圆心坐标点配置多个,以确保能生成蛋糕
		for (HawkTuple2<Integer, Integer> cfgPoint : poingList) {
			List<Point> points = WorldPointService.getInstance().getRhoAroundPointsFree(cfgPoint.first, cfgPoint.second, cfg.getAreaRadius());
			for (Point point : points) {
				AreaObject area = WorldPointService.getInstance().getArea(point.getAreaId());

				if (!point.canYuriSeat()) {
					continue;
				}
				if (!WorldPointService.getInstance().tryOccupied(area, point, GsConst.CAKE_SHARE_RADIUS)) {
					continue;
				}
				if (WorldPointService.getInstance().getWorldPoint(point.getId()) != null) {
					continue;
				}
				if (WorldPointService.getInstance().isInCapitalArea(point.getId())) {
					continue;
				}
				CakeShareInfo info = new CakeShareInfo();
				info.setCakeId(cakeId);
				info.setStartTime(startTime);
				info.setEndTime(endTime);
				info.addAwardRecord(awardPlayers);

				WorldPoint worldPoint = new WorldPoint(point.getX(), point.getY(), point.getAreaId(), point.getZoneId(), WorldPointType.CAKE_SHARE_VALUE);
				worldPoint.setResourceId(1);
				worldPoint.setPersistable(false);
				worldPoint.setCakeShareInfo(info);
				WorldPointService.getInstance().addPoint(worldPoint);
				HawkLog.logPrintln("createCakeShare, x:{}, y:{}, areaId:{}, blood:{}", point.getX(), point.getY(), point.getAreaId(), worldPoint.getRemainBlood());
				return worldPoint;
			}
		}
		return null;
	}

}
