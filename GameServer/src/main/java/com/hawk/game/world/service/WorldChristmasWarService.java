package com.hawk.game.world.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.uuid.HawkUUIDGenerator;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.type.ActivityState;
import com.hawk.activity.type.impl.christmaswar.ChristmasWarActivity;
import com.hawk.game.config.WorldChristmasWarBossCfg;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.World.ChristmasInfoResp;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.object.AreaObject;
import com.hawk.game.world.object.Point;

/**
 * 圣诞大战.
 * @author jm
 *
 */
public class WorldChristmasWarService extends HawkAppObj {
	/**
	 * 日志
	 */
	public static Logger logger = LoggerFactory.getLogger("Server");

	/**
	 * 实例
	 */
	private static WorldChristmasWarService instance = null;
	
	/**
	 * 圣诞怪物.
	 */
	private Map<String, WorldPoint> boss;

	/**
	 * 刷新boss的时间
	 */
	private volatile long refreshAllTime;
	/**
	 * 上次的公告时间.
	 */
	private long lastOptTime;
		
	/**
	 * 刷新的UUID，代表当前刷的哪个批次的怪.
	 */
	private String refreshUuid;
	/**
	 * 获取实例
	 */
	public static WorldChristmasWarService getInstance() {
		return instance;
	}
	
	public WorldChristmasWarService(HawkXID xid) {
		super(xid);
		instance = this;
	}
	
	public boolean init() {
		try {
			boss = new ConcurrentHashMap<>();
			
			List<WorldPoint> wpList = WorldPointService.getInstance().getWorldPointsByType(WorldPointType.CHRISTMAS_BOSS);
			for (WorldPoint wp : wpList) {
				String uuid = this.getBossUuid(wp);
				boss.put(uuid, wp);
				logger.info("init christmas boss uuid:{}, x:{}, y:{}", uuid, wp.getX(), wp.getY());
				
				//如果没有了怪 refreshAllTime为0也是可以的,
				if (refreshAllTime <= 0) {
					refreshAllTime = wp.getCreateTime();
				}
			}					
			
			this.refreshUuid = LocalRedis.getInstance().getChristmasWarRefreshUuid();
			
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return false;
	}
	
	@Override
	public boolean onTick() {
		if (refreshAllTime > 0 && this.hasChristmasMonsterInWorld()) {
			long curTime = HawkTime.getMillisecond();
			WorldMapConstProperty constProperty = WorldMapConstProperty.getInstance();
			long removeTime = constProperty.getChristmasRemoveTime();
			long expectRemoveTime = refreshAllTime + removeTime;
			//删除.
			if (curTime > expectRemoveTime && lastOptTime < expectRemoveTime) {
				this.removeAllMonster();
				ChatParames chatPara = ChatParames.newBuilder().setChatType(ChatType.SPECIAL_BROADCAST)
						.setKey(Const.NoticeCfgId.WORLD_CHRISTMAS_BOSS_RUN_AWAY).build();
				ChatService.getInstance().addWorldBroadcastMsg(chatPara);
				logger.info("remove christmas by timeout");
				lastOptTime = curTime;
			}
			
			//逃跑公告
			for (int noticeTime : constProperty.getChristmasRemoveNoticeTimeList()) {
				long noticeTimeL = refreshAllTime + removeTime - noticeTime * 1000l;
				if (curTime >= noticeTimeL && lastOptTime < noticeTimeL) {
					ChatParames chatPara = ChatParames.newBuilder().setChatType(ChatType.SPECIAL_BROADCAST)
							.setKey(Const.NoticeCfgId.WORLD_CHRISTMAS_BOSS_PREPARE_RUN_AWAY).addParms(noticeTime / 60).build();
					ChatService.getInstance().addWorldBroadcastMsg(chatPara);
					lastOptTime = curTime;
				}
			}
		}
		return super.onTick();
	}
	
	private boolean hasChristmasMonsterInWorld() {
		return !boss.isEmpty();
	}	
	
	/**
	 * 获取剩余的血量.
	 * @return
	 */
	public long getAllRemainBlood() {
		long blood = 0;
		for (WorldPoint wp : boss.values()) {
			blood += wp.getRemainBlood();
		}
		
		return blood;
	}	
	
	/**
	 * 获取单个初始化的血量.
	 * @return
	 */
	public int getInitBlood() {				
		WorldChristmasWarBossCfg cfg = this.getBossCfg(); 
		int blood = 0;
		List<ArmyInfo> armyList = cfg.getArmyList();
		for (ArmyInfo army : armyList) {
			blood += army.getTotalCount();
		}			
		
		int hpNumber = cfg.getHpNumber();
		
		// 用年兽的k值
		blood = (int)(1L * (blood * WorldNianService.getInstance().getLastNianBloodK()) / GsConst.RANDOM_MYRIABIT_BASE);
		
		// 返回血量自适应，可以整除血条数量
		return ((((blood - 1) / hpNumber) + 1) * hpNumber);			
	}
	
	/**
	 * 刷新怪物
	 */
	public void refreshBoss() {
		removeAllMonster();
		if (!isActivityOpen()) {
			return;
		}
		
		Const.NoticeCfgId noticeId = Const.NoticeCfgId.WORLD_CHRISTMAS_BOSS_REFRESH;
		ChatParames chat = ChatParames.newBuilder().setChatType(Const.ChatType.SPECIAL_BROADCAST).setKey(noticeId).build(); 
		ChatService.getInstance().addWorldBroadcastMsg(chat);
		
		generateAllBoss();
	}
	
	private void generateAllBoss() {
		WorldChristmasWarBossCfg bossCfg = this.getBossCfg();
		int bossId = bossCfg.getId();
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.CHRISTMAS_WAR_VALUE);
		if (!opActivity.isPresent()) {
			return;
		}
		
		logger.info("generate all boos");
		ChristmasWarActivity activity = (ChristmasWarActivity)opActivity.get();
		int summonNum = activity.getSummonNum();
		if (summonNum <= 0) {
			logger.info("summoned boos less than 0");
		}
		activity.addSummonedNum(summonNum);
		
		WorldMapConstProperty mapConst = WorldMapConstProperty.getInstance(); 
		List<int[]> posList = new ArrayList<>(mapConst.getChristmasRefreshPosList());
		List<int[]> realPosList = new ArrayList<>(summonNum);
		while(realPosList.size() < summonNum) {
			if (posList.isEmpty()) {
				posList.addAll(mapConst.getChristmasRefreshPosList());
			}
			
			realPosList.add(posList.get(HawkRand.randInt(0, posList.size() - 1)));
		}
		for (int i = 0; i < summonNum; i++) {
			int[] posArray = realPosList.get(i);
			generateBoss(bossId, GameUtil.combineXAndY(posArray[0], posArray[1]));
		}
		
		this.refreshAllTime = HawkTime.getMillisecond();
		this.refreshUuid = HawkUUIDGenerator.genUUID();
		LocalRedis.getInstance().setChristmasWarRefreshUuid(refreshUuid);
	}
	
	private void generateBoss(int bossId, int pos) {
		WorldMapConstProperty mapConst = WorldMapConstProperty.getInstance(); 
		int radius = mapConst.getChristmasRefreshAreaRadius();
		List<Point> freePointList = WorldPointService.getInstance().getRhoAroundPointsFree(pos, radius);
		Collections.shuffle(freePointList);
		
		for (Point freePoint : freePointList) {
			if (!freePoint.canChristmasWarSeat()) {
				continue;
			}
			
			AreaObject areaObj = WorldPointService.getInstance().getArea(freePoint.getX(), freePoint.getY());
			boolean isFree = WorldPointService.getInstance().tryOccupied(areaObj, freePoint, GsConst.CHRISTMAS_BOSS_SIZE);
			if (!isFree) {
				continue;
			}					
			
			WorldPoint wp = new WorldPoint(freePoint.getX(), freePoint.getY(), areaObj.getId(), freePoint.getZoneId(), WorldPointType.CHRISTMAS_BOSS_VALUE);
			wp.setMonsterId(bossId);
			wp.setRemainBlood(this.getInitBlood());
			
			WorldPointService.getInstance().createWorldPoint(wp);
			
			this.putBossToCache(wp);
			logger.info("generate christmas boos pos:({}, {})", wp.getX(), wp.getY());
			
			break;
		}
	}
	
	/**
	 * 获取圣诞大战的termId
	 * @return
	 */
	public int getTermId() {
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.CHRISTMAS_WAR_VALUE);
		if (opActivity.isPresent() ) {
			return opActivity.get().getActivityTermId();					
		}
		
		return 0;
	}
	
	public boolean isActivityOpen() {
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.CHRISTMAS_WAR_VALUE);
		if (opActivity.isPresent() ) {
			ActivityState activityState = opActivity.get().getActivityEntity().getActivityState();
			
			return activityState == ActivityState.SHOW || activityState == ActivityState.OPEN;
		}
		
		return false;
	}
	
	/**
	 * 从本地缓存删除,.
	 * @param wp
	 */
	public boolean removeBossFromCache(WorldPoint wp ) {
		String uuid = this.getBossUuid(wp);
		return this.boss.remove(uuid) != null;
	}
	
	/**
	 * 本地缓存
	 * @param wp
	 */
	public void putBossToCache(WorldPoint wp) {
		String uuid = this.getBossUuid(wp);
		this.boss.put(uuid, wp);
	}
	
	/**
	 * bossid
	 * @param point
	 * @return
	 */
	public String getBossUuid(WorldPoint point) {
		return point.getId() + ":" + point.getCreateTime();
	}
	/**
	 * 删除所有的monster
	 */
	public void removeAllMonster() {
		logger.info("remove all christmas boss");
		Map<String, WorldPoint> localMap = boss;
		for (WorldPoint point : localMap.values()) {
			logger.info("remove christmas monster isRemoveAll:{}, pointId:{}, x:{}, y:{}, blood:{}", true, point.getId(), point.getX(), point.getY(), point.getRemainBlood());
			WorldPointService.getInstance().removeWorldPoint(point.getId(), true);
		}
		
		boss = new ConcurrentHashMap<>();
		refreshAllTime = 0L;					
	}
	
	/**
	 * 获取boss配置
	 * @return
	 */
	private WorldChristmasWarBossCfg getBossCfg() {
		return HawkConfigManager.getInstance().getConfigByKey(WorldChristmasWarBossCfg.class, 1);
	}
	
	/**
	 * boss 被杀
	 * @param wp
	 */
	public void onBossKill(WorldPoint wp) {		
		try {
			boolean removeResult = this.removeBossFromCache(wp);
			WorldPointService.getInstance().removeWorldPoint(wp.getId());
			
			logger.info("christmas boss kill x:{}, y:{} uuid:{}, removeResult:{}", wp.getX(), wp.getY(), this.getBossUuid(wp), removeResult);
			
			Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(ActivityType.CHRISTMAS_WAR_VALUE);
			if (!opActivity.isPresent()) {
				return;
			}
			ChristmasWarActivity activity = (ChristmasWarActivity) opActivity.get();
			activity.addKillChristmasNum(1);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
	}
	
	public String getRefreshUuid() {
		return this.refreshUuid;
	}
	
	public void setRefreshUuid(String uuid) {
		this.refreshUuid = uuid;
		LocalRedis.getInstance().setChristmasWarRefreshUuid(uuid);
	}

	public Map<String, WorldPoint> getBoss() {
		return boss;
	}
	
	/**
	 * 推送圣诞的相关信息.
	 * @param player
	 */
	public void pushChristmasInfo(Player player) {
		ChristmasInfoResp.Builder respBuidler = ChristmasInfoResp.newBuilder();
		String uuid = this.getRefreshUuid();
		respBuidler.setAtkTimes(player.getAtkNianTimes(uuid));
		
		HawkProtocol hawkProtocol = HawkProtocol.valueOf(HP.code.CHRISTMAS_INFO_RESP_VALUE, respBuidler);
		player.sendProtocol(hawkProtocol);
	}
}
