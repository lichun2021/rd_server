package com.hawk.robot.action.guild;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.hawk.annotation.RobotAction;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;

import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Const.TerritoryType;
import com.hawk.game.protocol.GuildManor.CreateGuildManor;
import com.hawk.game.protocol.GuildManor.CreateGuildManorBuilding;
import com.hawk.game.protocol.GuildManor.GuildBuildingNorStat;
import com.hawk.game.protocol.GuildManor.GuildManorBase;
import com.hawk.game.protocol.GuildManor.GuildSuperMineBase;
import com.hawk.game.protocol.GuildManor.GuildTowerBase;
import com.hawk.game.protocol.GuildManor.GuildWarehouseBase;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.World.WorldMarchPB;
import com.hawk.game.protocol.World.WorldMarchRelation;
import com.hawk.game.protocol.World.WorldMarchReq;
import com.hawk.game.protocol.World.WorldMarchServerCallBackReq;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointSync;
import com.hawk.robot.GameRobotApp;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.WorldDataManager;
import com.hawk.robot.RobotLog;
import com.hawk.robot.util.GuildUtil;
import com.hawk.robot.util.WorldUtil;

/**
 * 联盟领地
 * @author zhenyu.shang
 * @since 2017年8月10日
 */
@RobotAction(valid = true)
public class PlayerGuildManorAction extends HawkRobotAction{
	
	/**
	 * 成员随机行为Map
	 */
	private Map<NorActionType, Integer> norActMap = new HashMap<NorActionType, Integer>();
	
	
	public PlayerGuildManorAction() {
		norActMap.put(NorActionType.COLLECT, NorActionType.COLLECT.getRand());
		norActMap.put(NorActionType.STORE, NorActionType.STORE.getRand());
		norActMap.put(NorActionType.NONE, NorActionType.NONE.getRand());
		norActMap.put(NorActionType.CALLBACK, NorActionType.CALLBACK.getRand());
		norActMap.put(NorActionType.ASSITANCE_JOIN, NorActionType.ASSITANCE_JOIN.getRand());
	}
	
	private enum NorActionType{
		COLLECT(55),//采集资源
		STORE(55),//仓库操作
		CALLBACK(10),//召回行军
		ASSITANCE_JOIN(10), //集结加入
		NONE(15);//什么都不做
		
		private final int rand;
		
		private NorActionType(int rand){
			this.rand = rand;
		}

		public int getRand() {
			return rand;
		}
	}

	/**
	 * 策略：
	 * 没有公会的：
	 * 执行创建或者加入
	 * 有公会的：
	 * 判断R4以上的，执行放置，回收，建设，挖矿，仓库存储
	 * R4及以下的，执行建设，挖矿，仓库存储
	 * 具体流程：
	 * 	先判断R4，如果是则判断当前领地状态，1, 未放置-放置，2未完成-建设，3已完成-拆除（10），挖矿（40），仓库存取（40）， 最后在判断其他建筑状态，然后依据情况，放置和建造其他建筑
	 *  不是R4的，判断当前领地状态，未放置-什么也不做，未完成-建设，已完成-挖矿（50），仓库存取（50）
	 */
	@Override
	public void doAction(HawkRobotEntity robotEntity) {
		GameRobotEntity gameRobotEntity = (GameRobotEntity) robotEntity;
		String guildId = gameRobotEntity.getGuildData().getGuildId();
		if(!HawkOSOperator.isEmptyString(guildId)){
			hasGuildAction(gameRobotEntity, guildId);
		} else {
			if(!GameRobotApp.getInstance().getConfig().getBoolean("auto")){
				notGuildAction(gameRobotEntity);
			}
		}
	}
	
	/**
	 * 有联盟操作
	 * @param gameRobotEntity
	 * @param guildId
	 */
	private void hasGuildAction(GameRobotEntity gameRobotEntity, String guildId){
		// 判断是否可切世界
		if (!WorldUtil.checkSwitchIntoWorld(gameRobotEntity)) {
			return;
		}
				
		//检查是否有视野
		WorldPointSync resp = gameRobotEntity.getWorldData().getWorldPointSync();
		if(resp == null){
			WorldUtil.enterWorldMap(gameRobotEntity); //进入世界地图
			WorldUtil.move(gameRobotEntity); //移动(通过传回point list, 查找可攻击玩家)
			return;
		}
		//获取领地列表,没有先请求
		BlockingQueue<GuildManorBase> list = WorldDataManager.getInstance().getManor(guildId);
		if(list == null){
			gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_MANOR_LIST_C_VALUE));
			return;
		}
		int power = gameRobotEntity.getGuildData().getGuildInfoSync().getGuildAuthority();
		//先处理堡垒相关, 没放就放, 放了就建, 建成则继续往下执行
		if(!doSome4Manor(gameRobotEntity, guildId, resp, power)){
			return;
		}
		//再处理建筑相关
		if(!doSome4Building(gameRobotEntity, guildId, resp, power)){
			return;
		}
		//如果领地建造好了, 则执行随机操作
		randomAction(gameRobotEntity, guildId, list);
	}
	
	/**
	 * 处理堡垒
	 * @param gameRobotEntity
	 * @param guildId
	 * @return
	 */
	private boolean doSome4Manor(GameRobotEntity gameRobotEntity, String guildId, WorldPointSync resp, int power){
		// 判断是否可切世界
		if (!WorldUtil.checkSwitchIntoWorld(gameRobotEntity)) {
			return false;
		}
				
		boolean res = false;
		//获取领地列表,没有先请求
		BlockingQueue<GuildManorBase> list = WorldDataManager.getInstance().getManor(guildId);
		if(list == null){
			gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_MANOR_LIST_C_VALUE));
			return res;
		}
		for (GuildManorBase guildManorBase : list) {
			switch (guildManorBase.getStat()) {
			case LOCKED_M:
				break; //锁定状态直接跳过
			case OPENED_M: //放置
				if(power >= Const.GuildAuthority.L4_VALUE){ //R4及盟主执行放置操作
					List<WorldPointPB> points = resp.getPointsList();
					List<WorldPointPB> pointPBs = new ArrayList<WorldPointPB>();
					pointPBs.addAll(points);
					Collections.shuffle(pointPBs);
					int[] pos = WorldUtil.searchPointsWithDiamond(pointPBs, gameRobotEntity, 2);
					if(pos == null || gameRobotEntity.getGuildData().isMoveSee()){
						WorldUtil.enterWorldMap(gameRobotEntity); //进入世界地图
						WorldUtil.move(gameRobotEntity); //移动(通过传回point list, 查找可攻击玩家)
						gameRobotEntity.getGuildData().setMoveSee(false);
						RobotLog.guildPrintln("guild: {} , manor :{} check pos is null, move the world!", guildId, guildManorBase.getManorIdx());
						return res;
					}
					CreateGuildManor.Builder builder = CreateGuildManor.newBuilder();
					builder.setX(pos[0]);
					builder.setY(pos[1]);
					builder.setManorIdx(guildManorBase.getManorIdx());
					gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.CREATE_GUILD_TOWER_C_VALUE, builder));
					RobotLog.guildPrintln("put down guild manor, pos: {}, manorIndex: {}, point:{}", pos[0] + "," + pos[1], guildManorBase.getManorIdx(), pos[2] + "," + pos[3]);
					return res;
				}
				break;
			case BUILDING_M: //建造
			case UNCOMPELETE_M:
				startBuildMarch(gameRobotEntity, guildManorBase.getX(), guildManorBase.getY(), WorldMarchType.MANOR_BUILD);
				break;
			case BREAKING_M:
				if(HawkRand.randPercentRate(10)){
					startMassMarch(gameRobotEntity, guildManorBase.getX(), guildManorBase.getY(), WorldMarchType.MANOR_ASSISTANCE_MASS);
				} else {
					startBuildMarch(gameRobotEntity, guildManorBase.getX(), guildManorBase.getY(), WorldMarchType.MANOR_ASSISTANCE);
				}
				res = true;
			case DAMAGED_M:
			case REPAIRING_M:
				startBuildMarch(gameRobotEntity, guildManorBase.getX(), guildManorBase.getY(), WorldMarchType.MANOR_REPAIR);
				res = true;
				break;
			case GARRISON_M:
				//建成了就直接召回, 不做其他处理
				List<String> marchIdList = gameRobotEntity.getWorldData().getMarchIdList();
				if(marchIdList != null && marchIdList.size() > 0) {
					for(String marchId : marchIdList) {
						WorldMarchPB worldMarch = WorldDataManager.getInstance().getMarch(marchId);
						if (worldMarch == null) {
							RobotLog.worldErrPrintln("fetch wolrd march failed, playerId: {}, marchId: {}", gameRobotEntity.getPlayerId(), marchId);
							continue;
						}
						if(worldMarch.getRelation() != WorldMarchRelation.SELF) {
							continue;
						}
						if(worldMarch.getMarchType() == WorldMarchType.MANOR_BUILD && worldMarch.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MANOR_GARRASION){
							callbackMarch(gameRobotEntity, worldMarch);
						}
					}
				}
				res = true;
				break;
			default: 
				res = true; //其他状态则可以往下执行
				break;
			}
		}
		return res;
	}
	
	/**
	 * 其他建筑物操作
	 * @param gameRobotEntity
	 * @param guildId
	 * @param resp
	 * @param power
	 */
	private boolean doSome4Building(GameRobotEntity gameRobotEntity, String guildId, WorldPointSync resp, int power){
		boolean a = false;
		boolean b = false;
		boolean c = false;
		//建造其他建筑
		BlockingQueue<GuildSuperMineBase> mines = WorldDataManager.getInstance().getSuperMine(guildId);
		for (GuildSuperMineBase guildSuperMineBase : mines) {
			if(guildSuperMineBase.getStat() == GuildBuildingNorStat.LOCKED_N){//锁定直接跳过
				continue;
			} else if(guildSuperMineBase.getStat() == GuildBuildingNorStat.OPENED_N){
				if(power >= Const.GuildAuthority.L4_VALUE){ //R4及盟主执行放置操作
					putDownBuilding(gameRobotEntity, guildId, resp, guildSuperMineBase.getType().getNumber(), TerritoryType.GUILD_MINE);
				}
			} else if(guildSuperMineBase.getStat() == GuildBuildingNorStat.BUILDING_N || guildSuperMineBase.getStat() == GuildBuildingNorStat.UNCOMPELETE_N){
				//没有完成则建设
				startBuildMarch(gameRobotEntity, guildSuperMineBase.getX(), guildSuperMineBase.getY(), WorldMarchType.MANOR_BUILD);
			} else { //其他状态
				a = true;
			}
		}
		BlockingQueue<GuildTowerBase> towers = WorldDataManager.getInstance().getTower(guildId);
		for (GuildTowerBase tower : towers) {
			if(tower.getStat() == GuildBuildingNorStat.LOCKED_N){//锁定直接跳过
				continue;
			} else if(tower.getStat() == GuildBuildingNorStat.OPENED_N){
				if(power >= Const.GuildAuthority.L4_VALUE){ //R4及盟主执行放置操作
					putDownBuilding(gameRobotEntity, guildId, resp, tower.getTowerId(), TerritoryType.GUILD_BARTIZAN);
				}
			} else if(tower.getStat() == GuildBuildingNorStat.BUILDING_N || tower.getStat() == GuildBuildingNorStat.UNCOMPELETE_N){
				//没有完成则建设
				startBuildMarch(gameRobotEntity, tower.getX(), tower.getY(), WorldMarchType.MANOR_BUILD);
			} else {
				b = true;
			}
		}
		GuildWarehouseBase house = WorldDataManager.getInstance().getWareHouse(guildId);
		if(house.getStat() == GuildBuildingNorStat.OPENED_N){
			if(power >= Const.GuildAuthority.L4_VALUE){ //R4及盟主执行放置操作
				putDownBuilding(gameRobotEntity, guildId, resp, 1, TerritoryType.GUILD_STOREHOUSE);
			}
		} else if(house.getStat() == GuildBuildingNorStat.BUILDING_N || house.getStat() == GuildBuildingNorStat.UNCOMPELETE_N){
			//没有完成则建设
			startBuildMarch(gameRobotEntity, house.getX(), house.getY(), WorldMarchType.MANOR_BUILD);
		} else if(house.getStat() != GuildBuildingNorStat.LOCKED_N){
			c = true;
		}
		return a && b && c; //三个必须每个建筑起码有一个建成才往下执行
	}

	/**
	 * 放置建筑物
	 * 
	 * 建筑需要在领地的点里随机一个
	 * 
	 * @param gameRobotEntity
	 * @param resp
	 * @param index
	 * @param territoryType
	 */
	private void putDownBuilding(GameRobotEntity gameRobotEntity, String guildId, WorldPointSync resp, int index, TerritoryType territoryType) {
		if(!HawkRand.randPercentRate(20)){
			return;
		}
		Set<Integer> points = WorldDataManager.getInstance().getManorPoints(guildId);
		if(points == null || points.isEmpty()){
			gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_MANOR_LIST_C_VALUE));
			return;
		}
		List<Integer> list = new ArrayList<Integer>(points);
		Integer position = HawkRand.randomObject(list);
		int[] pos = WorldUtil.splitXAndY(position);
		if(pos != null){
			CreateGuildManorBuilding.Builder builder = CreateGuildManorBuilding.newBuilder();
			builder.setX(pos[0]);
			builder.setY(pos[1]);
			builder.setType(territoryType);
			builder.setManorIdx(index);
			gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.CREATE_GUILD_BUILDING_C_VALUE, builder));
		}
		RobotLog.guildPrintln("put down guild building, pos {}, type: {}, manorIndex: {}", pos[0] + "," + pos[1], territoryType, index);
	}
	
	/**
	 * 发起领地行军
	 * @param gameRobotEntity
	 * @param x
	 * @param y
	 */
	private void startBuildMarch(GameRobotEntity gameRobotEntity, int x, int y, WorldMarchType worldMarchType) {
//		long marchTotal = PublicDataManager.getInstance().getWorldMarchCount();
//		if(marchTotal >= GameRobotApp.getInstance().getConfig().getInt("marchCount")) {
//			RobotLog.worldErrPrintln("start manor march failed, total march count touch limit");
//			return;
//		}
		
		//获取行军信息, 如果有行军了, 则不执行
		if(WorldDataManager.getInstance().getPointMarch(WorldUtil.combineXAndY(x, y)) >= 8){
			return;
		}
		//每个人只能发出3条领地行军
		if (gameRobotEntity.getWorldData().getMarchCount() > 3) {
			return;
		}
		//判断一下坐标,有可能没有及时刷新
		if(x == 0 || y == 0){
			gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_MANOR_LIST_C_VALUE));
			return;
		}
		
		WorldMarchReq.Builder builder = WorldUtil.generatorMarchBuilder(gameRobotEntity, x, y, worldMarchType, true);
		if (builder == null) {
			return;
		}
		
		gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_MANOR_MARCH_C_VALUE, builder));
		RobotLog.guildPrintln("start march action manor, playerId: {}, marchType: {}, pos : {}", gameRobotEntity.getPlayerId(), worldMarchType, x + "," + y);
	}
	
	/**
	 * 发起领地捐助集结行军
	 * @param gameRobotEntity
	 * @param x
	 * @param y
	 */
	private void startMassMarch(GameRobotEntity gameRobotEntity, int x, int y, WorldMarchType worldMarchType) {
//		long marchTotal = PublicDataManager.getInstance().getWorldMarchCount();
//		if(marchTotal >= GameRobotApp.getInstance().getConfig().getInt("marchCount")) {
//			RobotLog.worldErrPrintln("start manor march failed, total march count touch limit");
//			return;
//		}
		
		//获取行军信息, 领地行军, 每个点有10个行军就够了
		if(WorldDataManager.getInstance().getPointMarch(WorldUtil.combineXAndY(x, y)) >= 10){
			return;
		}
		//每个人只能发出3条领地行军
		if (gameRobotEntity.getWorldData().getMarchCount() > 3) {
			return;
		}
		//判断一下坐标,有可能没有及时刷新
		if(x == 0 || y == 0){
			gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_MANOR_LIST_C_VALUE));
			return;
		}
		
		WorldMarchReq.Builder builder = WorldUtil.generatorMarchBuilder(gameRobotEntity, x, y, worldMarchType, true);
		if (builder == null) {
			return;
		}
		builder.setMassTime(600);
		gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MASS_C_VALUE, builder));
		RobotLog.guildPrintln("start march action manor, playerId: {}, marchType: {}, pos : {}", gameRobotEntity.getPlayerId(), worldMarchType, x + "," + y);
	}

	/**
	 * 概率执行其他操作
	 */
	private void randomAction(GameRobotEntity robot, String guildId, BlockingQueue<GuildManorBase> list){
		//其他情况概率执行收回(5), 挖矿(20), 仓库存储(20), 什么都不做（55）
		NorActionType type = HawkRand.randomWeightObject(norActMap);
		RobotLog.guildPrintln("now random action is {}, playerId: {}", type, robot.getPuid());
		switch (type) {
		case COLLECT:
			BlockingQueue<GuildSuperMineBase> mines = WorldDataManager.getInstance().getSuperMine(guildId);
			for (GuildSuperMineBase guildSuperMineBase : mines) {
				if(guildSuperMineBase.getStat() == GuildBuildingNorStat.COMPELETE_N || guildSuperMineBase.getStat() == GuildBuildingNorStat.ACTIVE_N){
					WorldMarchReq.Builder builder = WorldUtil.generatorMarchBuilder(robot, guildSuperMineBase.getX(), guildSuperMineBase.getY(), WorldMarchType.MANOR_COLLECT, true);
					if (builder != null) {
						robot.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_MANOR_MARCH_C_VALUE, builder));
						RobotLog.guildPrintln("start march action, playerId: {}, marchType: {}", robot.getPlayerId(), WorldMarchType.MANOR_COLLECT);
					}
				}
			}
			break;
		case STORE:
			GuildWarehouseBase house = WorldDataManager.getInstance().getWareHouse(guildId);
			if(house.getStat() == GuildBuildingNorStat.COMPELETE_N || house.getStat() == GuildBuildingNorStat.ACTIVE_N){
				if(HawkRand.randPercentRate(50)){
					WorldMarchReq.Builder builder = WorldUtil.generatorMarchBuilder(robot, house.getX(), house.getY(), null, true);
					if (builder != null) {
						RewardItem.Builder reBuilder = RewardItem.newBuilder();
						reBuilder.setItemId(PlayerAttr.GOLDORE_UNSAFE_VALUE);
						reBuilder.setItemCount(1);
						reBuilder.setItemType(Const.ItemType.PLAYER_ATTR_VALUE);
						builder.addAssistant(reBuilder.build());
						robot.sendProtocol(HawkProtocol.valueOf(HP.code.WARE_HOUSE_STORE_MARCH_C_VALUE, builder));
						RobotLog.guildPrintln("start march action, playerId: {}, marchType: {}", robot.getPlayerId(), WorldMarchType.WAREHOUSE_STORE);
					}
				} else {
					WorldMarchReq.Builder builder = WorldUtil.generatorMarchBuilder(robot, house.getX(), house.getY(), null, true);
					if (builder != null) {
						RewardItem.Builder reBuilder = RewardItem.newBuilder();
						reBuilder.setItemId(PlayerAttr.GOLDORE_UNSAFE_VALUE);
						reBuilder.setItemCount(1);
						reBuilder.setItemType(Const.ItemType.PLAYER_ATTR_VALUE);
						builder.addAssistant(reBuilder.build());
						robot.sendProtocol(HawkProtocol.valueOf(HP.code.WARE_HOUSE_TAKE_MARCH_C_VALUE, builder));
						RobotLog.guildPrintln("start march action, playerId: {}, marchType: {}", robot.getPlayerId(), WorldMarchType.WAREHOUSE_GET);
					}
				}
			}
			break;
		case CALLBACK:
			List<String> marchIdList = robot.getWorldData().getMarchIdList();
			if(marchIdList == null || marchIdList.size() <= 0) {
				return;
			}
			for(String marchId : marchIdList) {
				WorldMarchPB worldMarch = WorldDataManager.getInstance().getMarch(marchId);
				if (worldMarch == null) {
					RobotLog.worldErrPrintln("fetch wolrd march failed, playerId: {}, marchId: {}", robot.getPlayerId(), marchId);
					continue;
				}
				
				if(worldMarch.getRelation() != WorldMarchRelation.SELF) {
					continue;
				}
				
				if(worldMarch.getMarchType() == WorldMarchType.MANOR_COLLECT && worldMarch.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_COLLECT){
					callbackMarch(robot, worldMarch);
				}
			}
			break;
		case ASSITANCE_JOIN:
			joinAssitanceMass(robot);
			break;
		default:
			break;
		}
	}
	
	private void joinAssitanceMass(GameRobotEntity robot){
		String guildId = robot.getGuildId();
		if(HawkOSOperator.isEmptyString(guildId)) {
			return;
		}
		WorldMarchPB massMarch = WorldDataManager.getInstance().getManorAssitanceMassMarch(guildId);
		if(massMarch == null) {
			return;
		}
		if (massMarch.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_WAITING) {
			RobotLog.worldErrPrintln("start march failed, mass march status not waiting, playerId: {}", robot.getPlayerId());
			return;
		}
		if(massMarch.getPlayerId().equals(robot.getPlayerId())){
			return;
		}
		WorldMarchReq.Builder builder = WorldUtil.generatorMarchBuilder(robot, massMarch.getOrigionX(), massMarch.getOrigionY(), null, true);
		if (builder == null) {
			return;
		}
		builder.setMarchId(massMarch.getMarchId());
		builder.setType(WorldMarchType.MANOR_ASSISTANCE_MASS_JOIN);
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MASS_JOIN_C_VALUE, builder));
		RobotLog.worldPrintln("start march action, playerId: {}, marchType: {}", robot.getPlayerId(), WorldMarchType.MANOR_ASSISTANCE_MASS_JOIN.name());
	}
	
	private void callbackMarch(GameRobotEntity robot, WorldMarchPB worldMarch){
		WorldMarchServerCallBackReq.Builder builder = WorldMarchServerCallBackReq.newBuilder();
		builder.setMarchId(worldMarch.getMarchId());
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_SERVER_CALLBACK_C_VALUE, builder));
		RobotLog.worldPrintln("worldMarch callback, playerId: {}, marchId: {}, marchStatus: {}, type :{}", robot.getPlayerId(), worldMarch.getMarchId(), worldMarch.getMarchStatus(), worldMarch.getMarchType());
	}
	
	/**
	 * 无联盟操作
	 * @param gameRobotEntity
	 */
	private void notGuildAction(GameRobotEntity gameRobotEntity){
		if(HawkRand.randPercentRate(5) && !WorldDataManager.getInstance().isGuildNumLimit()){
			GuildUtil.createGuild(gameRobotEntity);
		} else {
			GuildUtil.searchOrJoinGuild(gameRobotEntity);
		}
	}
}
