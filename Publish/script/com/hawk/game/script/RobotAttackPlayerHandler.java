package com.hawk.game.script;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.hawk.app.HawkApp;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.net.session.HawkSession;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.xid.HawkXID;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.global.GlobalData;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.msg.PlayerAssembleMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Login.HPLogin;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.object.Point;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;

/**
 * 机器人集结进攻玩家
 *
 * localhost:8080/script/robotatt?playerId=7py-gn54j-1&count=10&distance=100
 *
 * playerName: 被攻击玩家的名字
 * count: 参与战斗行军的机器人数量
 * distance: 发起行军的距离限制(可选)
 *
 * @author lating
 *
 */
public class RobotAttackPlayerHandler extends HawkScript {
	/**
	 * 自动行军的距离
	 */
	private static int AUTO_MARCH_DISTANCE = 100;
	/**
	 * 开启行军机器人个数
	 */
	private static int robotCount = 10;

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		if (!GsConfig.getInstance().isDebug()) {
			return HawkScript.failedResponse(SCRIPT_ERROR, "");
		}
		
		// 先确定目标玩家
		Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
		if (player == null) {
			return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, params.toString());
		}
		
		int[] targetPos = WorldPlayerService.getInstance().getPlayerPosXY(player.getId());
		if (targetPos[0] <= 0 || targetPos[1] <= 0) {
			return HawkScript.failedResponse(ScriptError.WORLD_POSITION_ERROR_VALUE, params.toString());
		}
		
		if (params.containsKey("count")) {
			robotCount = Integer.valueOf(params.get("count"));
		}
		
		List<AccountInfo> accountList = getRobotAccountList();

		if (params.containsKey("distance")) {
			AUTO_MARCH_DISTANCE = Integer.valueOf(params.get("distance"));
		}
		
		// 机器人迁城，迁到距离目标玩家distance范围内
		randomMoveCity(accountList, targetPos);
		
		// 开启行军
		startMarch(player, accountList);
		
		return HawkScript.successResponse("attack player robot count: " + accountList.size() + " distance: " + AUTO_MARCH_DISTANCE);
	}
	
	/**
	 * 随机迁城
	 * @param accountList
	 * @param targetPos
	 */
	public void randomMoveCity(List<AccountInfo> accountList, int[] targetPos) {
		try {
			for(AccountInfo accountInfo : accountList) {
				Player robot = GlobalData.getInstance().scriptMakesurePlayer(accountInfo.getPlayerId());
				if(robot == null) {
					continue;
				}
				int[] selfPos = WorldPlayerService.getInstance().getPlayerPosXY(robot.getId());
				if (Math.abs(selfPos[0] - targetPos[0]) > AUTO_MARCH_DISTANCE || Math.abs(selfPos[1] - targetPos[1]) > AUTO_MARCH_DISTANCE) {
					// 在给定范围内随机一个迁城坐标
					int[] pos = randomPoint(robot, targetPos);
					if(pos != null) {
						 WorldPlayerService.getInstance().mantualSettleCity(robot, pos[0], pos[1], 0);
					}
				}
			}
		} catch(Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 随机一个城点
	 * @param player
	 * @param targetPos
	 * @return
	 */
	private int[] randomPoint(Player player, int[] targetPos) {
		
		int aroundDistance = 0;
		do {
			aroundDistance++;
			Map<Integer, Point> aroundPoints = WorldPointService.getInstance().getAroundPoints(targetPos[0], targetPos[1], aroundDistance, aroundDistance);
			List<Point> points = new ArrayList<>(aroundPoints.values());
			Collections.shuffle(points);
			for (Point validPoint : points) {
				if(((validPoint.getX() + validPoint.getY()) % 2 == 1) && WorldPlayerService.getInstance().checkPlayerCanOccupy(player, validPoint.getX(), validPoint.getY())) {
					return new int[]{validPoint.getX(), validPoint.getY()};
				}
			}
		} while (aroundDistance <= AUTO_MARCH_DISTANCE);
		
		return null;
	}
	
	/**
	 * 获取机器人信息
	 * @return
	 */
	public List<AccountInfo> getRobotAccountList() {
		List<AccountInfo> accountList = new LinkedList<AccountInfo>();
		GlobalData.getInstance().getAccountList(accountList);
		Iterator<AccountInfo> iterator = accountList.iterator();
		// 记录机器人总数
		int robotTotal = 0;
		while (iterator.hasNext()) {
			AccountInfo accountInfo = iterator.next();
			if (accountInfo.getPuid().indexOf("robot") < 0) {
				iterator.remove();
				continue;
			}
			
			robotTotal++;
			// 已发起行军的机器人不能再发起行军
			if(WorldMarchService.getInstance().getPlayerMarchCount(accountInfo.getPlayerId()) > 0) {
				iterator.remove();
			}
		}

		Collections.shuffle(accountList);
		
		// 世纪存在可用的机器人数量大于要求数量
		if (robotCount <= accountList.size()) {
			accountList.subList(robotCount, accountList.size()).clear();
		} else {
			// 可用的机器人不足时，生成新的机器人
			for(int i= robotTotal + 1; i <= robotTotal + robotCount - accountList.size(); i++) {
				AccountInfo accountInfo = genRobot("robot_puid_" + (i + 1));
				if(accountInfo != null) {
					accountList.add(accountInfo);
				}
			}
		}
		
		return accountList;
	}
	
	/**
	 * 机器人向目标玩家发起行军
	 */
	public void startMarch(Player player, List<AccountInfo> accountList) {
		int[] targetPos = WorldPlayerService.getInstance().getPlayerPosXY(player.getId());
		player.removeCityShield();
		for (int i = 0; accountList.size() > 1 && i < accountList.size(); i++) {
			AccountInfo accountInfo = accountList.get(i);
			Player robot = GlobalData.getInstance().makesurePlayer(accountInfo.getPlayerId());
			if (robot == null) {
				continue;
			}

			int posId = GameUtil.combineXAndY(targetPos[0], targetPos[1]);	
			List<ArmyInfo> armyList = new ArrayList<ArmyInfo>();
			armyList.add(new ArmyInfo(100501, 1000));
			armyList.add(new ArmyInfo(100101, 1000));

			// 开启行军
			WorldMarchService.getInstance().startMarch(robot, WorldMarchType.ATTACK_PLAYER_VALUE, posId, player.getId(), new EffectParams());

			HawkLog.logPrintln("robot start march, robotId: {}, marchType: {}, targetId: {}, targetPos: ({}, {})",
					accountInfo.getPlayerId(), WorldMarchType.ATTACK_PLAYER_VALUE,
					player.getId(), targetPos[0], targetPos[1]);
		}
	}
	
	/**
	 * 自动生成机器人
	 * @param puid
	 */
	public AccountInfo genRobot(String openid) {
		try {
			// 构造登录协议对象
			HPLogin.Builder builder = HPLogin.newBuilder();
			builder.setCountry("cn");
			builder.setChannel("guest");
			builder.setLang("zh-CN");
			builder.setPlatform("android");
			builder.setVersion("1.0.0.0");
			builder.setPfToken("da870ef7cf996eb6");
			builder.setPhoneInfo("{\"deviceMode\":\"win32\",\"mobileNetISP\":\"0\",\"mobileNetType\":\"0\"}\n");
			builder.setPuid(openid);
			builder.setServerId(GsConfig.getInstance().getServerId());
			builder.setDeviceId(openid);
			
			HawkSession session = new HawkSession(null);
			session.setAppObject(new Player(null));
			
			if (GsApp.getInstance().doLoginProcess(session, HawkProtocol.valueOf(HP.code.LOGIN_C_VALUE, builder), HawkTime.getMillisecond())) {
				AccountInfo accountInfo = GlobalData.getInstance().getAccountInfo(openid +"#android", GsConfig.getInstance().getServerId());
				if (accountInfo != null) {
					// 加载数据
					accountInfo.setInBorn(false);
					HawkXID xid = HawkXID.valueOf(GsConst.ObjType.PLAYER, accountInfo.getPlayerId());
					Player player = (Player) GsApp.getInstance().queryObject(xid).getImpl();
					PlayerData playerData = GlobalData.getInstance().getPlayerData(accountInfo.getPlayerId(), true);
					player.updateData(playerData);
					
					// 投递消息
					HawkApp.getInstance().postMsg(player, PlayerAssembleMsg.valueOf(builder.build(), session));
				}
				
				return accountInfo;
			}
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return null;
	}

}