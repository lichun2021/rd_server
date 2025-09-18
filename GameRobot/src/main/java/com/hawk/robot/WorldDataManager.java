package com.hawk.robot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;

import com.hawk.game.protocol.GuildManager.GetGuildMemeberInfoResp;
import com.hawk.game.protocol.GuildManager.GuildMemeberInfo;
import com.hawk.game.protocol.GuildManager.HPGetGuildShopInfoResp;
import com.hawk.game.protocol.GuildManager.HPGuildShopItem;
import com.hawk.game.protocol.GuildManor.GuildManorBase;
import com.hawk.game.protocol.GuildManor.GuildManorList;
import com.hawk.game.protocol.GuildManor.GuildSuperMineBase;
import com.hawk.game.protocol.GuildManor.GuildTowerBase;
import com.hawk.game.protocol.GuildManor.GuildWarehouseBase;
import com.hawk.game.protocol.GuildScience.GuildScienceInfo;
import com.hawk.game.protocol.President.PresidentInfo;
import com.hawk.game.protocol.SuperWeapon.AllSuperWeaponInfo;
import com.hawk.game.protocol.World.WorldMarchPB;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.robot.util.GuildUtil;
import com.hawk.robot.util.WorldUtil;

public class WorldDataManager {
	
	private static WorldDataManager instance = null;
	
	/**
	 * 集结行军数据 <guildId, worldMarch>
	 */
	private Map<String, WorldMarchPB> massMarchs = new ConcurrentHashMap<>();
	
	/**
	 * 领地集结行军数据 <guildId, worldMarch>
	 */
	private Map<String, WorldMarchPB> manorMassMarchs = new ConcurrentHashMap<>();
	
	/**
	 * 领地援助集结行军数据 <guildId, worldMarch>
	 */
	private Map<String, WorldMarchPB> manorAssitanceMassMarchs = new ConcurrentHashMap<>();
	
	/**
	 * 集结迷雾要塞行军
	 */
	private Map<String, WorldMarchPB> foggyMassMarchs = new ConcurrentHashMap<>();
	
	/**
	 * 怪物集结行军数据 <guildId, worldMarch>
	 */
	private Map<String, WorldMarchPB> monsterMassMarchs = new ConcurrentHashMap<>();
	
	/**
	 * 王城集结数据
	 */
	private Map<String, WorldMarchPB> presidentMassMarchs = new ConcurrentHashMap<>();
	
	/**
	 * 箭塔集结数据
	 */
	private Map<String, WorldMarchPB> presidentTowerMassMarchs = new ConcurrentHashMap<>();
	
	/**
	 * 世界所有行军<marchId, playerId>
	 */
	private Map<String, WorldMarchPB> worldMarchs = new ConcurrentHashMap<>();
	
	/**
	 * 联盟商店信息
	 */
	protected Map<String, List<HPGuildShopItem>> guildShopItem = new HashMap<String, List<HPGuildShopItem>>();
	
	/**
	 * 所有联盟名字
	 */
	protected List<String> guildIds = new ArrayList<String>();
	
	/**
	 * 联盟成员信息
	 */
	protected Map<String, List<GuildMemeberInfo>> guildMemberInfos = new HashMap<String, List<GuildMemeberInfo>>();
	/**
	 * 联盟科技信息
	 */
	protected Map<String, List<GuildScienceInfo>> guildScienceInfo = new HashMap<>();
	/**
	 * 联盟堡垒列表
	 */
	protected Map<String, BlockingQueue<GuildManorBase>> guildManorInfos = new ConcurrentHashMap<String, BlockingQueue<GuildManorBase>>();
	
	/**
	 * 联盟箭塔信息
	 */
	protected Map<String, BlockingQueue<GuildTowerBase>> guildTowerInfos = new HashMap<String, BlockingQueue<GuildTowerBase>>();
	
	/**
	 * 联盟超级矿信息
	 */
	protected Map<String, BlockingQueue<GuildSuperMineBase>> guildMineInfos = new HashMap<String, BlockingQueue<GuildSuperMineBase>>();
	
	/**
	 * 联盟仓库信息
	 */
	protected Map<String, GuildWarehouseBase> guildWareHouseInfos = new HashMap<String, GuildWarehouseBase>();
	
	/**
	 * 联盟领地占用点信息
	 */
	protected Map<Integer, Set<Integer>> allManorPoints = new HashMap<Integer, Set<Integer>>();
	
	/**
	 * 联盟建筑点上对应的行军数量
	 */
	protected Map<Integer, Set<String>> pointMarchCount = new ConcurrentHashMap<Integer, Set<String>>();
	
	/**
	 * 在线机器人id
	 */
	protected List<String> onlineRobotIds = new CopyOnWriteArrayList<String>();
	
	/**
	 * 服务器删除行军的总量
	 */
	private AtomicLong delMarchTotal = new AtomicLong(0);
	/**
	 * 服务器成功发起行军的总量
	 */
	private AtomicLong successMarchTotal = new AtomicLong(0);
	/**
	 * 客户端发起行军的总量
	 */
	private AtomicLong startMarchTotal = new AtomicLong(0);
	
	/**
	 * 国王战状态
	 */
	private int presidentStatus = 0;
	
	/**
	 * 超级武器状态
	 */
	private int superWeaponStatus = 0;
	
	private String presidentId = null;
	
	/**
	 * 超级武器报名信息
	 */
	private Map<String, Set<Integer>> superWeaponSignUpData = new HashMap<String, Set<Integer>>();
	
	private WorldDataManager() {
		
	}
	
	public static WorldDataManager getInstance() {
		if(instance == null) {
			instance = new WorldDataManager();
			instance.onTick();
		}
		return instance;
	}
	
	public void addStartMarchCount() {
		startMarchTotal.incrementAndGet();
	}
	
	public void addSuccessMarchCount() {
		successMarchTotal.incrementAndGet();
	}
	
	public void addDelMarchCount() {
		delMarchTotal.incrementAndGet();
	}
	
	public long getStartMarchCount() {
		return startMarchTotal.get();
	}
	
	public long getSuccessMarchCount() {
		return successMarchTotal.get();
	}
	
	public long getDelMarchCount() {
		return delMarchTotal.get();
	}
	
	public void addRobot(String playerId) {
		onlineRobotIds.add(playerId);
	}
	
	public void removeRobot(String playerId) {
		onlineRobotIds.remove(playerId);
	}
	
	public boolean isRobotOnline(String playerId) {
		return onlineRobotIds.indexOf(playerId) >= 0;
	}
	
	public List<String> getRobotOnline() {
		return onlineRobotIds;
	}
	
	/**
	 * tick删除离线玩家的已结束行军
	 */
	private void onTick() {
		ScheduledExecutorService pool = Executors.newSingleThreadScheduledExecutor();
		int tickPeriod = 2;
		try {
			tickPeriod = GameRobotApp.getInstance().getConfig().getInt("robot.tickPeriod");
		} catch(Exception e) {
			HawkException.catchException(e);
		}
		
		final int period = tickPeriod;
		pool.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				long startTime = HawkTime.getMillisecond();
				try {
					Iterator<Entry<String, WorldMarchPB>> iterator = worldMarchs.entrySet().iterator();
					while (iterator.hasNext()) {
						WorldMarchPB march = iterator.next().getValue();
						if (isRobotOnline(march.getPlayerId())) {
							continue;
						}
						
						if (march.getEndTime() > HawkTime.getMillisecond()) {
							continue;
						}
						
						// 回程的行军直接删除
						if (march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_RETURN_BACK) {
							if (!removeMassMarch(march)) {
								removeOfflineMarch(march);
							}
							iterator.remove();
							continue;
						}
						
						WorldMarchType marchType = march.getMarchType();
						switch (marchType.getNumber()) {
							// 打玩家、打怪、侦查、集结PVP、集结打怪、randombox
							case WorldMarchType.ATTACK_PLAYER_VALUE:
							case WorldMarchType.ATTACK_MONSTER_VALUE:
							case WorldMarchType.SPY_VALUE:
							case WorldMarchType.RANDOM_BOX_VALUE: {
								removeOfflineMarch(march);
								iterator.remove();
								break;
							}
							default: {
								if (removeMassMarch(march)) {
									iterator.remove();
								}
								break;
							}
						}
					}
				} catch (Exception e) {
					HawkException.catchException(e);
				} finally {
					long costtime = HawkTime.getMillisecond() - startTime;
					if (costtime > period * 1000) {
						RobotLog.worldPrintln("world march ontick timeout, costtime: {}", costtime);
					}
				}
			}
			
		}, 300, tickPeriod, TimeUnit.SECONDS);
	}
	
	/**
	 * 移除集结行军
	 * @param march
	 * @return
	 */
	private boolean removeMassMarch(WorldMarchPB march) {
		WorldMarchType marchType = march.getMarchType();
		if (marchType == WorldMarchType.MASS || marchType == WorldMarchType.MONSTER_MASS 
				| marchType == WorldMarchType.MANOR_MASS || marchType == WorldMarchType.MANOR_ASSISTANCE_MASS) {
			removeOfflineMarch(march);
			// 参与集结PVP、参与集结打怪
			worldMarchs.values().stream()
			.filter( e -> e.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_JOIN_MARCH 
			&& march.getMarchId().equals(e.getTargetId())).forEach(e -> removeOfflineMarch(e));
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * 移除离线玩家的行军
	 * @param march
	 */
	private void removeOfflineMarch(WorldMarchPB march) {
		addDelMarchCount();
		// TODO 联盟ID获取问题
		removeMarch(null, march.getMarchId());
		RobotLog.worldPrintln("remove march, robot offline, playerId: {}", march.getPlayerId());
	}
	
	public void addMarch(String guildId, WorldMarchPB march) {
		if(!worldMarchs.containsKey(march.getMarchId())) {
			RobotLog.worldPrintln("add march, playerId: {}, relation: {}, marchId: {}", march.getPlayerId(), march.getRelation().name(), march.getMarchId());
		}
		
		worldMarchs.put(march.getMarchId(), march);
		if(!HawkOSOperator.isEmptyString(guildId)) {
			if(march.getMarchType() == WorldMarchType.MASS){
				if(!massMarchs.containsKey(guildId)) {
					RobotLog.worldPrintln("add mass march, playerId: {}, marchType: {}, guildId: {}", march.getPlayerId(), march.getMarchType(), guildId);
				}
				massMarchs.put(guildId, march);
			} else if(march.getMarchType() == WorldMarchType.MONSTER_MASS){
				if(!monsterMassMarchs.containsKey(guildId)) {
					RobotLog.worldPrintln("add mass march, playerId: {}, marchType: {}, guildId: {}", march.getPlayerId(), march.getMarchType(), guildId);
				}
				monsterMassMarchs.put(guildId, march);
			} else if(march.getMarchType() == WorldMarchType.MANOR_MASS){
				if(!manorMassMarchs.containsKey(guildId)) {
					RobotLog.worldPrintln("add mass march, playerId: {}, marchType: {}, guildId: {}", march.getPlayerId(), march.getMarchType(), guildId);
				}
				manorMassMarchs.put(guildId, march);
			} else if(march.getMarchType() == WorldMarchType.MANOR_ASSISTANCE_MASS){
				if(!manorAssitanceMassMarchs.containsKey(guildId)) {
					RobotLog.worldPrintln("add mass march, playerId: {}, marchType: {}, guildId: {}", march.getPlayerId(), march.getMarchType(), guildId);
				}
				manorAssitanceMassMarchs.put(guildId, march);
			} else if(march.getMarchType() == WorldMarchType.PRESIDENT_MASS){
				if(!presidentMassMarchs.containsKey(guildId)){
					RobotLog.worldPrintln("add mass march, playerId: {}, marchType: {}, guildId: {}", march.getPlayerId(), march.getMarchType(), guildId);
				}
				presidentMassMarchs.put(guildId, march);
			} else if(march.getMarchType() == WorldMarchType.PRESIDENT_TOWER_MASS){
				if(!presidentTowerMassMarchs.containsKey(guildId)){
					RobotLog.worldPrintln("add mass march, playerId: {}, marchType: {}, guildId: {}", march.getPlayerId(), march.getMarchType(), guildId);
				}
				presidentTowerMassMarchs.put(guildId, march);
			} else if(march.getMarchType() == WorldMarchType.FOGGY_FORTRESS_MASS) {
				if(!foggyMassMarchs.containsKey(guildId)){
					RobotLog.worldPrintln("add mass march, playerId: {}, marchType: {}, guildId: {}", march.getPlayerId(), march.getMarchType(), guildId);
				}
				foggyMassMarchs.put(guildId, march);
			}
		}
	}
	
	public WorldMarchPB removeMarch(String guildId, String marchId) {
		if(worldMarchs.containsKey(marchId)) {
			RobotLog.worldPrintln("remove march, guild: {}, marchId: {}", guildId, marchId);
		}
		if (!HawkOSOperator.isEmptyString(guildId)) {
			massMarchs.remove(guildId);
			monsterMassMarchs.remove(guildId);
			manorAssitanceMassMarchs.remove(guildId);
			manorMassMarchs.remove(guildId);
			presidentMassMarchs.remove(guildId);
			presidentTowerMassMarchs.remove(guildId);
			foggyMassMarchs.remove(guildId);
		}
		return worldMarchs.remove(marchId);
	}
	
	public WorldMarchPB getMarch(String marchId) {
		return worldMarchs.get(marchId);
	}
	
	public WorldMarchPB getMassMarch(String guildId) {
		return massMarchs.get(guildId);
	}
	
	public WorldMarchPB getManorMassMarch(String guildId) {
		return manorMassMarchs.get(guildId);
	}
	
	public WorldMarchPB getManorAssitanceMassMarch(String guildId) {
		return manorAssitanceMassMarchs.get(guildId);
	}
	
	public WorldMarchPB getMonsterMassMarch(String guildId) {
		return monsterMassMarchs.get(guildId);
	}
	
	public WorldMarchPB getPresidentMassMarch(String guildId){
		return presidentMassMarchs.get(guildId);
	}
	
	public WorldMarchPB getPresidentTowerMassMarch(String guildId){
		return presidentTowerMassMarchs.get(guildId);
	}

	public WorldMarchPB getFoggyMassMarchs(String guildId) {
		return foggyMassMarchs.get(guildId);
	}

	public void setFoggyMassMarchs(Map<String, WorldMarchPB> foggyMassMarchs) {
		this.foggyMassMarchs = foggyMassMarchs;
	}

	public int getWorldMarchCount() {
		return worldMarchs.size();
	}
	
	public List<GuildMemeberInfo> getGuildMemberInfo(String guildId){
		return guildMemberInfos.get(guildId);
	}
	
	public List<HPGuildShopItem> getGuildShopItem(String guildId){
		return guildShopItem.get(guildId);
	}
	
	public GuildWarehouseBase getWareHouse(String guildId){
		return guildWareHouseInfos.get(guildId);
	}
	
	public BlockingQueue<GuildManorBase> getManor(String guildId){
		return guildManorInfos.get(guildId);
	}
	
	public BlockingQueue<GuildSuperMineBase> getSuperMine(String guildId){
		return guildMineInfos.get(guildId);
	}
	
	public BlockingQueue<GuildTowerBase> getTower(String guildId){
		return guildTowerInfos.get(guildId);
	}
	
	public Set<Integer> getManorPoints(String guildId){
		BlockingQueue<GuildManorBase> manors = guildManorInfos.get(guildId);
		if(manors == null){
			return null;
		}
		Set<Integer> all = new HashSet<Integer>();
		for (GuildManorBase base : manors) {
			if(!GuildUtil.isManorComplete(base.getStat())){
				continue;
			}
			Set<Integer> arroundPoint = this.allManorPoints.get(WorldUtil.combineXAndY(base.getX(), base.getY()));
			if(arroundPoint != null){
				all.addAll(arroundPoint);
			}
		}
		return all;
	}
	
	/**
	 * 刷新联盟商店信息
	 */
	public void refreshGuildShopInfo(String guildId, HPGetGuildShopInfoResp guildShopInfo){
		List<HPGuildShopItem> list = guildShopItem.get(guildId);
		if(list == null){
			list = new ArrayList<HPGuildShopItem>();
			guildShopItem.put(guildId, list);
		}
		list.clear();
		list.addAll(guildShopInfo.getShopItemList());
	}
	
	/**
	 * 获取联盟科技信息
	 * @param guildId
	 * @return
	 */
	public List<GuildScienceInfo> getGuildScienceInfo(String guildId){
		return guildScienceInfo.get(guildId);
	}
	
	/**
	 * 刷新联盟科技信息
	 */
	public void refreshGuildScienceInfo(String guildId, List<GuildScienceInfo> scienceInfos){
		guildScienceInfo.put(guildId, new ArrayList<>(scienceInfos));
	}
	
	/**
	 * 刷新联盟成员
	 * @param guildListResp
	 */
	public void refreshGuildMemberList(String guildId, GetGuildMemeberInfoResp guildMemeberInfoResp){
		List<GuildMemeberInfo> list = guildMemberInfos.get(guildId);
		if(list == null){
			list = new ArrayList<GuildMemeberInfo>();
			guildMemberInfos.put(guildId, list);
		}
		list.clear();
		list.addAll(guildMemeberInfoResp.getInfoList());
	}
	
	/**
	 * 刷新联盟领地列表
	 * @param guildId
	 * @param list
	 */
	public void refreshGuildManorList(String guildId, GuildManorList list){
		if(list.getAllManorCount() > 0){
			BlockingQueue<GuildManorBase> manors = guildManorInfos.get(guildId);
			if(manors == null){
				manors = new LinkedBlockingQueue<GuildManorBase>();
				guildManorInfos.put(guildId, manors);
			}
			manors.clear();
			manors.addAll(list.getAllManorList());
		}
		if(list.getAllSuperMineCount() > 0){
			BlockingQueue<GuildSuperMineBase> mines = guildMineInfos.get(guildId);
			if(mines == null){
				mines = new LinkedBlockingQueue<GuildSuperMineBase>();
				guildMineInfos.put(guildId, mines);
			}
			mines.clear();
			mines.addAll(list.getAllSuperMineList());
		}
		if(list.getAllTowerCount() > 0){
			BlockingQueue<GuildTowerBase> towers = guildTowerInfos.get(guildId);
			if(towers == null){
				towers = new LinkedBlockingQueue<GuildTowerBase>();
				guildTowerInfos.put(guildId, towers);
			}
			towers.clear();
			towers.addAll(list.getAllTowerList());
		}
		if(list.hasAllWarehouse()){
			guildWareHouseInfos.put(guildId, list.getAllWarehouse());
		}
	}
	
	/**
	 * 添加新的联盟领地占用点
	 * @param x
	 * @param y
	 */
	public void addManorPoints(int x, int y){
		Integer manorPos = WorldUtil.combineXAndY(x, y);
		if(this.allManorPoints.containsKey(manorPos)){
			return;
		}
		Set<Integer> all = new HashSet<Integer>();
		//先将当前中心点加入
		all.add(manorPos);
		//再计算占用点(占用半径17 + 领地本身3)
		List<Integer> roundPoints = WorldUtil.getRadiusAllPoints(x, y, 20);
		all.addAll(roundPoints);
		this.allManorPoints.put(manorPos, all);
	}
	
	public void addGuildId(String name){
		this.guildIds.add(name);
	}
	
	public void removeGuildId(String name){
		this.guildIds.remove(name);
	}

	public List<String> getGuildIds() {
		return guildIds;
	}

	public boolean isGuildNumLimit(){
		return guildIds.size() >= RobotAppConfig.getInstance().getGuildNumLimit();
	}
	
	public Map<String, BlockingQueue<GuildManorBase>> getGuildManorInfos() {
		return guildManorInfos;
	}
	
	public void addPointMarch(int pointId, String marchId){
		synchronized (pointMarchCount) {
			if(pointMarchCount.containsKey(pointId)){
				pointMarchCount.get(pointId).add(marchId);
			} else {
				Set<String> set = new HashSet<String>();
				set.add(marchId);
				pointMarchCount.put(pointId, set);
			}
		}
	}
	
	public void removePointMarch(int pointId, String marchId){
		synchronized (pointMarchCount) {
			if(pointMarchCount.containsKey(pointId)){
				pointMarchCount.get(pointId).remove(marchId);
			}
		}
	}
	
	public int getPointMarch(int pointId){
		int count = 0;
		if(pointMarchCount.containsKey(pointId)){
			count = pointMarchCount.get(pointId).size();
		} 
		return count;
	}

	public int getPresidentStatus() {
		return presidentStatus;
	}
	
	public String getPresidentId() {
		return presidentId;
	}

	public int getSuperWeaponStatus() {
		return superWeaponStatus;
	}

	/**
	 * 设置国王战状态
	 * @param presidentStatus
	 */
	public void setPresidentInfo(PresidentInfo presidentInfo) {
		int status = presidentInfo.getPeriodType();
		String president = presidentInfo.getPresidentId();
		if(status != this.presidentStatus){
			this.presidentStatus = status;
		}
		this.presidentId = president;
	}
	
	/**
	 * 设置超级武器状态
	 * @param allSuperWeaponInfo
	 */
	public void setSuperWeaponInfo(AllSuperWeaponInfo allSuperWeaponInfo){
		this.superWeaponStatus = allSuperWeaponInfo.getPeriodType();
	}
	
	/**
	 * 增加报名信息
	 * @param guildId
	 * @param pointId
	 */
	public void addSuperWeaponSignUpData(String guildId, int pointId){
		Set<Integer> data = superWeaponSignUpData.get(guildId);
		if(data == null){
			data = new HashSet<Integer>();
			data.add(pointId);
			superWeaponSignUpData.put(guildId, data);
		} else {
			data.add(pointId);
		}
	}
	
	public boolean canSignUp(String guildId, int pointId){
		Set<Integer> data = superWeaponSignUpData.get(guildId);
		if(data != null && data.contains(pointId)){
			return false;
		}
		return true;
	}
	
	/**
	 * 从报名信息中随机一个点
	 * @param guildId
	 * @return
	 */
	public int randomSuperWeaponPointId(String guildId){
		Set<Integer> data = superWeaponSignUpData.get(guildId);
		if(data == null || data.isEmpty()){
			return -1;
		}
		List<Integer> list = new ArrayList<Integer>();
		for (Integer integer : data) {
			list.add(integer);
		}
		return HawkRand.randomObject(list);
	}
	
	/**
	 * 清空报名信息
	 */
	public void clearSignUpData(){
		superWeaponSignUpData.clear();
	}
}
