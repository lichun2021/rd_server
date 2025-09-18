package com.hawk.robot.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;

import com.hawk.game.protocol.Army.ArmyInfoPB;
import com.hawk.game.protocol.Army.ArmySoldierPB;
import com.hawk.game.protocol.Building.BuildingPB;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.TerritoryType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.GenWorldPointReq;
import com.hawk.game.protocol.World.PlayerEnterWorld;
import com.hawk.game.protocol.World.PlayerWorldMove;
import com.hawk.game.protocol.World.WorldInfoPush;
import com.hawk.game.protocol.World.WorldMarchPB;
import com.hawk.game.protocol.World.WorldMarchReq;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.robot.GameRobotApp;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.RobotAppConfig;
import com.hawk.robot.RobotAppHelper;
import com.hawk.robot.RobotLog;
import com.hawk.robot.WorldDataManager;
import com.hawk.robot.action.army.PlayerArmyAction;
import com.hawk.robot.action.march.WorldMarchCallbackAction;
import com.hawk.robot.config.BuildingCfg;
import com.hawk.robot.data.WorldData;

/**
 *
 * @author zhenyu.shang
 * @since 2017年8月10日
 */
public class WorldUtil {
	
	/**
	 * 生成出征builder
	 * @param robot
	 * @param x
	 * @param y
	 * @param isArmyMarch 是否带部队出征(例如：侦查不带兵)
	 * @return
	 */
	public static WorldMarchReq.Builder generatorMarchBuilder(GameRobotEntity robot, int x, int y, WorldMarchType type, boolean isArmyMarch) {
		WorldMarchReq.Builder builder = WorldMarchReq.newBuilder();
		builder.setPosX(x);
		builder.setPosY(y);
		if(type != null){
			builder.setType(type);
		}
		
		//不需要部队出征,直接返回builder
		if (!isArmyMarch) {
			return builder;
		}
		
		List<BuildingPB> list = robot.getBuildingByType(BuildingType.FIGHTING_COMMAND_VALUE);
		if (list.isEmpty()) {
			RobotLog.worldErrPrintln("start march failed, robot has no fight command building, playerId: {}", robot.getPlayerId());
			return null;
		}
		
		BuildingCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, list.get(0).getBuildCfgId());
		int amount = cfg == null ? 0 : cfg.getAttackUnitLimit();
		int total = 0;
		boolean success = false;
		int armyId = 0;
		for (ArmyInfoPB army : robot.getArmyObjects()) {
			if (army.getFreeCount() <= 0) {
				armyId = army.getArmyId();
				continue;
			}
			
			success = true;
			int count = HawkRand.randInt(1, army.getFreeCount());
			if (total + count > amount) {
				count = amount - total;
			}
			
			if (count <= 0) {
				break;
			}
			total += count;
			ArmySoldierPB.Builder armySoldier = ArmySoldierPB.newBuilder();
			armySoldier.setArmyId(army.getArmyId());
			armySoldier.setCount(count);
			builder.addArmyInfo(armySoldier);
		}
		
		if(!success) {
			final int soldierId = armyId;
			GameRobotApp.getInstance().executeTask(new Runnable() {
				@Override
				public void run() {
					PlayerArmyAction.trainSoldier(robot, soldierId);
				}
			});
			
			RobotLog.worldErrPrintln("start march failed, robot has not free army, playerId: {}", robot.getPlayerId());
			return null;
		}
		
		WorldDataManager.getInstance().addStartMarchCount();
		
		return builder;
	}
	
	/**
	 * 进入世界地图
	 * @param robot
	 */
	public static void enterWorldMap(GameRobotEntity robot) {
		WorldInfoPush robotWorldInfo = robot.getWorldData().getWorldInfo();
		if(robotWorldInfo == null) {
			RobotLog.worldErrPrintln("enter world map failed, playerId: {}, position info: {}", robot.getPlayerId(), robotWorldInfo);
			return;
		}
		int x = robotWorldInfo.getTargetX();
		int y = robotWorldInfo.getTargetY();
		
		PlayerEnterWorld.Builder enterWorld = PlayerEnterWorld.newBuilder();
		enterWorld.setX(x);
		enterWorld.setY(y);
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_ENTER_WORLD_VALUE, enterWorld));
		robot.setInWorld(true);
	}
	
	/**
	 * 场景移动
	 * @param robot
	 */
	public static void move(GameRobotEntity robot) {
		int[] pos = randomReqPos(robot);
		PlayerWorldMove.Builder req = PlayerWorldMove.newBuilder();
		req.setX(pos[0]);
		req.setY(pos[1]);
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_WORLD_MOVE_VALUE, req));
	}
	
	/**
	 * 生成随机世界点
	 * @param robot
	 * @return
	 */
	private static int[] randomReqPos(GameRobotEntity robot) {
		int[] pos = new int[2];
		WorldInfoPush robotWorldInfo = robot.getWorldData().getWorldInfo();
		if(robotWorldInfo == null) {
			RobotLog.worldErrPrintln("world move failed, playerId: {}, position info: {}", robot.getPlayerId(), robotWorldInfo);
			return pos;
		}
		int x = robotWorldInfo.getTargetX();
		int y = robotWorldInfo.getTargetY();
		
		Random random = new Random();
		int	radius = GameRobotApp.getInstance().getConfig().getInt("worldMoveRadius");
		x = x + (random.nextInt(radius) * (random.nextBoolean() ? 1 : -1));
		y = y + (random.nextInt(radius) * (random.nextBoolean() ? 1 : -1));
		while (x <= 0) {
			x++;
		}
		
		int WORLD_MAX_X = GameRobotApp.getInstance().getConfig().getInt("worldMaxX");
		while (x >= WORLD_MAX_X) {
			x--;
		}
		
		while (y <= 0) {
			y++;
		}

		int WORLD_MAX_Y = GameRobotApp.getInstance().getConfig().getInt("worldMaxY");
		while (y >= WORLD_MAX_Y) {
			y--;
		}
		
		pos[0] = x;
		pos[1] = y;
		return pos;
	}
	
	/**
	 * 请求生成世界点 精英野怪
	 * @param robot
	 */
	public static void reqMonsterPoint(GameRobotEntity robot) {
		int[] pos = randomReqPos(robot);
		GenWorldPointReq.Builder req = GenWorldPointReq.newBuilder();
		req.setPosX(pos[0]);
		req.setPosY(pos[1]);
		req.setItemId(1400001);
		req.setType(WorldPointType.MONSTER);
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.GEN_WORLD_POINT_C_VALUE, req));
	}
	
	/**
	 * 把int值拆分高16低16,index = 0 x;index = 1 y
	 * @param value
	 * @return
	 */
	public static int[] splitXAndY(int value) {
		return new int[] { (value & 0x0000ffff), (value >> 16) & 0x0000ffff };
	}

	/**
	 * 两个int分作高地位组合一个int值
	 * 
	 * @param  坐标y
	 * @param  坐标x
	 * @return
	 */
	public static int combineXAndY(int x, int y) {
		return (y << 16) | x;
	}
	
	/**
	 * 获取点半径
	 * @param pointType
	 * @param terriId
	 * @return
	 */
	public static int getPointRadius(int pointType, int terriId){
		int radius = 0;
		switch (pointType) {
		case WorldPointType.GUILD_TERRITORY_VALUE:
			if(terriId == TerritoryType.GUILD_BARTIZAN_VALUE){
				radius = 1;
			} else {
				radius = 2;
			}
			break;
		case WorldPointType.PLAYER_VALUE:
			radius = 2;
			break;
		default:
			radius = 1;
			break;
		}
		return radius;
	}
	
	/**
	 * 获取点的占用点 (菱形算法)
	 * @param x
	 * @param y
	 * @param radius
	 * @return
	 */
	public static List<Integer> getRadiusAllPoints(int x, int y, int radius){
		List<Integer> points = new ArrayList<Integer>();
		for (int i = y; i <= y + radius; i++) {//下部
			int len = radius - (i - y);
			for (int j = x - len; j <= x + len; j++) {
				if(i > 1200 || i < 0 || j < 0 || j > 600){
					continue;
				}
				points.add(combineXAndY(j, i));
			}
		}
		for (int i = y - 1; i >= y - radius; i--) {//上部
			int len = radius + (i - y);
			for (int j = x - len; j <= x + len; j++) {
				if(i > 1200 || i < 0 || j < 0 || j > 600){
					continue;
				}
				points.add(combineXAndY(j, i));
			}
		}
		return points;
	}
	
	/**
	 * 利用输出绘处视野内的图
	 * @param map
	 */
	public static void testDrawMap(Map<Integer, WorldPointPB> map){
		int xmin = 600, xmax = 0;
		int ymin = 1200, ymax = 0;
		for (Integer pointId : map.keySet()) {
			int[] xy = splitXAndY(pointId);
			if(xy[0] > xmax){
				xmax = xy[0];
			}
			if(xy[0] < xmin){
				xmin = xy[0];
			}
			
			if(xy[1] > ymax){
				ymax = xy[1];
			}
			if(xy[1] < ymin){
				ymin = xy[1];
			}
		}
		for (int i = ymin; i <= ymax; i++) {
			StringBuffer sb = new StringBuffer();
			for (int j = xmin; j <= xmax; j++) {
				if(map.containsKey(combineXAndY(j, i))){
					WorldPointPB pb = map.get(combineXAndY(j, i));
					if(pb.getPointType() == WorldPointType.PLAYER){
						sb.append("5");
					} else {
						sb.append("1");
					}
				} else {
					sb.append("0");
				}
			}
			RobotLog.guildPrintln(sb.toString());
		}
	}
	
	/**
	 * 搜索视野内可以放置的点(正方形算法)
	 * <pre>
	 * 	具体算法：
	 * 	1.取出视野内所有存在的点
	 *  2.找出这个点的顶点（点 + 自己半径 + 放置物的半径）
	 *  3.然后以这一圈点为中心点，查找自己半径内是否有其他的点，如果没有，则可以放置，如果有，再下一个
	 * </pre>
	 * @param points
	 * @param gameRobotEntity
	 * @return
	 */
	public static int[] searchPointsWithSquare(List<WorldPointPB> points, GameRobotEntity gameRobotEntity, int buildRaduis){
		//查找所有点周围是否能放置
		for (WorldPointPB worldPointPB : points) {
			int x = worldPointPB.getPointX();
			int y = worldPointPB.getPointY();
			int pointRaduis = WorldUtil.getPointRadius(worldPointPB.getPointType().getNumber(), worldPointPB.getTerriId());
			for (int i = y - pointRaduis - buildRaduis; i <= y + pointRaduis + buildRaduis; i++) {
				if(checkOccSquare(x - pointRaduis - buildRaduis, i, buildRaduis, gameRobotEntity.getWorldData())){
					return new int[]{x - pointRaduis - buildRaduis, i};
				}
				if(checkOccSquare(x + pointRaduis + buildRaduis, i, buildRaduis, gameRobotEntity.getWorldData())){
					return new int[]{x + pointRaduis + buildRaduis, i};
				}
			}
			for (int i = x - pointRaduis - buildRaduis; i <= x + pointRaduis + buildRaduis; i++) {
				if(checkOccSquare(i, y - pointRaduis - buildRaduis, buildRaduis, gameRobotEntity.getWorldData())){
					return new int[]{i, y - pointRaduis - buildRaduis};
				}
				if(checkOccSquare(i, y + pointRaduis + buildRaduis, buildRaduis, gameRobotEntity.getWorldData())){
					return new int[]{i, y + pointRaduis + buildRaduis};
				}
			}
		}
		return null;
	}
	
	/**
	 * 检查点周围半径范围内是否有其他点(正方形算法)
	 * @param x
	 * @param y
	 * @param radius
	 */
	private static boolean checkOccSquare(int x, int y, int radius, WorldData data){
		if(x - radius < 0 || y - radius < 0){
			return false;
		}
		for (int i = x - radius; i <= x + radius; i++) {
			for (int j = y - radius; j <= y + radius; j++) {
				if(data.checkExsit(i, j)){
					return false;
				}
			}
		}
		return true;
	}
	
	
	/**
	 * 搜索视野内可以放置的点(正方形算法)
	 * <pre>
	 * 	具体算法：
	 * 	1.取出视野内所有存在的点
	 *  2.找出这个点的四个顶点（中心点 ± 自己半径）
	 *  3.以顶点为基准点, 算出每条边上的点±建筑物的半径
	 * </pre>
	 * @param points
	 * @param gameRobotEntity
	 * @return
	 */
	public static int[] searchPointsWithDiamond(List<WorldPointPB> points, GameRobotEntity gameRobotEntity, int buildRaduis){
		//查找所有点周围是否能放置
		for (WorldPointPB worldPointPB : points) {
			int x = worldPointPB.getPointX();
			int y = worldPointPB.getPointY();
			int pointRaduis = WorldUtil.getPointRadius(worldPointPB.getPointType().getNumber(), worldPointPB.getTerriId());
			//以中心点开始算出周长上所有点 
			for (int i = 0; i <= pointRaduis + buildRaduis - 1; i++) {//(上右)
				int a = x + i;
				int b = y - (pointRaduis - i) - buildRaduis;//按边的点加上建筑物的半径
//				RobotLog.guildPrintln("上右周长上的点, pos: {} , 原始点 {} ,半径 {}", a + "," + b, x + "," + y, pointRaduis);
				if(checkOccDiamond(a, b, buildRaduis - 1, gameRobotEntity.getWorldData())){
					return new int[]{a, b, x, y};
				}
			}
			for (int i = 0; i <= pointRaduis + buildRaduis - 1; i++) {//(下右)
				int a = x + (pointRaduis - i) + buildRaduis;
				int b = y + i;
//				RobotLog.guildPrintln("下右周长上的点, pos: {} , 原始点 {} ,半径 {}", a + "," + b, x + "," + y, pointRaduis);
				if(checkOccDiamond(a, b, buildRaduis - 1, gameRobotEntity.getWorldData())){
					return new int[]{a, b, x, y};
				}
			}
			
			for (int i = 0; i <= pointRaduis + buildRaduis - 1; i++) {//(下左)
				int a = x - i;
				int b = y + (pointRaduis - i) + buildRaduis;
//				RobotLog.guildPrintln("下左周长上的点, pos: {} , 原始点 {} ,半径 {}", a + "," + b, x + "," + y, pointRaduis);
				if(checkOccDiamond(a, b, buildRaduis - 1, gameRobotEntity.getWorldData())){
					return new int[]{a, b, x, y};
				}
			}
			
			for (int i = 0; i <= pointRaduis + buildRaduis - 1; i++) {//(上左)
				int a = x - (pointRaduis - i) - buildRaduis;
				int b = y - i;
//				RobotLog.guildPrintln("上左周长上的点, pos: {} , 原始点 {} ,半径 {}", a + "," + b, x + "," + y, pointRaduis);
				if(checkOccDiamond(a, b, buildRaduis - 1, gameRobotEntity.getWorldData())){
					return new int[]{a, b, x, y};
				}
			}
		}
		return null;
	}
	
	/**
	 * 检查点周围半径范围内是否有其他点(菱形算法)
	 * 
	 * 此处注意, 最外层一圈的点是可以重合的, 所以检测的时候需要半径-1，不需要检查最外一圈的点
	 * @param x
	 * @param y
	 * @param radius
	 */
	private static boolean checkOccDiamond(int x, int y, int radius, WorldData data){
		for (int i = y; i <= y + radius; i++) {//下部
			int len = radius - (i - y);
			for (int j = x - len; j <= x + len; j++) {
				if(i > 1200 || i < 0 || j < 0 || j > 600){
					return false;
				}
				if(data.checkExsit(j, i)){
					return false;
				}
//				RobotLog.guildPrintln("检查点下部, pos: {} , 原始点 {} ,半径 {}", j + "," + i, x + "," + y, radius);
			}
		}
		for (int i = y - 1; i >= y - radius; i--) {//上部
			int len = radius + (i - y);
			for (int j = x - len; j <= x + len; j++) {
				if(i > 1200 || i < 0 || j < 0 || j > 600){
					return false;
				}
				if(data.checkExsit(j, i)){
					return false;
				}
//				RobotLog.guildPrintln("检查点上部, pos: {} , 原始点 {} ,半径 {}", j + "," + i, x + "," + y, radius);
			}
		}
		return true;
	}
	
	/**
	 * 判断是否是需要停留的行军 
	 * @param marchType
	 * @return
	 */
	public static boolean isStopMarch(WorldMarchType marchType) {
		switch (marchType.getNumber()) {
		// 打玩家、打怪、侦查、集结PVP、集结打怪、randombox
		case WorldMarchType.ATTACK_PLAYER_VALUE:
		case WorldMarchType.ATTACK_MONSTER_VALUE:
		case WorldMarchType.SPY_VALUE:
		case WorldMarchType.RANDOM_BOX_VALUE:
		case WorldMarchType.MASS_VALUE:
		case WorldMarchType.MONSTER_MASS_VALUE:
		case WorldMarchType.MASS_JOIN_VALUE:
		case WorldMarchType.MONSTER_MASS_JOIN_VALUE:
			return false;
		default:
			return true;
		}
	}
	
	/**
	 * 玩家下线时召回停留类行军
	 */
	public static void worldMarchCallBack(GameRobotEntity robot) {
		List<String> marchIdList = robot.getWorldData().getMarchIdList();
		for (String marchId : marchIdList) {
			WorldMarchPB worldMarch = WorldDataManager.getInstance().getMarch(marchId);
			if (worldMarch == null) {
				continue;
			}
			// 停留类行军下线就遣返
			if (WorldUtil.isStopMarch(worldMarch.getMarchType())) {
				WorldMarchCallbackAction.marchCallBack(robot, worldMarch);
				robot.getWorldData().delWorldMarch(robot, worldMarch.getMarchId());
			}
		}
	}
	
	/**
	 * 求两点之间的距离
	 * @param x1
	 * @param y1
	 * @param x3
	 * @param y2
	 * @return
	 */
	public static double lineDistance(int x1, int y1, int x2, int y2) {
		double lineLen = 0;
		lineLen = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
		return lineLen;
	}
	
	/**
	 * 判断玩家是否可以切世界
	 * 
	 * @param robotEntity
	 * @return
	 */
	public static boolean checkSwitchIntoWorld(GameRobotEntity robotEntity) {
		// 本来就在世界，不用考虑切了
		if (robotEntity.isInWorld()) {
			return true;
		}
		
		// 军衔等级达少校前不能切世界
		if (robotEntity.getMilitaryLevel() < RobotAppConfig.getInstance().getMilitaryLevelBoundary()) {
			return false;
		}
		
		// 机器人没注册满前，25%的概率可切世界
		if (!RobotAppHelper.getInstance().isFullRegister()) {
			if (HawkRand.randPercentRate(25)) {
				return true;
			}
		} else {
			if (HawkRand.randPercentRate(75)) {
				return true;
			}
		}
		
		return false;
	}
}
