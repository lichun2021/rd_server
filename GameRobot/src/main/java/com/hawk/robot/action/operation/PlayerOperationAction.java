package com.hawk.robot.action.operation;

import java.util.HashMap;
import java.util.Map;

import org.hawk.annotation.RobotAction;
import org.hawk.enums.EnumUtil;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Player.GetPlayerBasicInfoReq;
import com.hawk.game.protocol.Player.PlayerChangeIconReq;
import com.hawk.game.protocol.Player.PlayerChangeNameReq;
import com.hawk.game.protocol.Player.PlayerDetailReq;
import com.hawk.game.protocol.Player.RemoveShieldPlayerReq;
import com.hawk.game.protocol.Player.ShieldPlayerReq;
import com.hawk.robot.GameRobotApp;
import com.hawk.robot.GameRobotEntity;

/**
 * 玩家操作action类
 * 
 * @author lating
 *
 */
@RobotAction(valid = false)
public class PlayerOperationAction extends HawkRobotAction {
	
	private Map<String, Integer> map;
	
	public PlayerOperationAction() {
		super();
		map = new HashMap<String, Integer>();
	}
	
	/**
	 *	操作类型
	 */
	private static enum OperationType {
		CHANGE_NAME,         // 玩家改名
		CHANGE_ICON,         // 玩家改头像
		FETCH_PLAYER_INFO,   // 获取玩家信息
		OPEN_BOARD,          // 打开指挥官信息面板
		SHIELD,              // 屏蔽玩家
		SHIEED_REMOVE        // 解除屏蔽
	}

	@Override
	public void doAction(HawkRobotEntity entity) {
		GameRobotEntity robotEntity = (GameRobotEntity) entity;
		OperationType operType = EnumUtil.random(OperationType.class);
		
		switch (operType) {
		case CHANGE_NAME:
			doChangeNameAction(robotEntity);
			break;
		case CHANGE_ICON:
			doChangeIconAction(robotEntity);
			break;
		case FETCH_PLAYER_INFO:
			doFetchPlayerInfoAction(robotEntity);
			break;
		case OPEN_BOARD:
			doOpenBoardAction(robotEntity);
			break;
		case SHIELD:
			doShieldPlayerAction(robotEntity);
			break;
		case SHIEED_REMOVE:
			doRemoveShieldAction(robotEntity);
			break;
		default:
			break;
		}
		
	}
	
	/**
	 * 玩家改名
	 * 
	 * @param robotEntity
	 */
	public static void doChangeNameAction(GameRobotEntity robotEntity) {
		PlayerChangeNameReq.Builder builder = PlayerChangeNameReq.newBuilder();
		String name = getName();
		builder.setName(name);
		if(HawkRand.randPercentRate(30)) {
			builder.setUseGold(true);
		} else {
			builder.setUseGold(false);
			builder.setItemId(0);
		}
		
		robotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_CHANGE_NAME_C_VALUE, builder));
	}
	
	public static String getName() {
		String name = HawkOSOperator.randomUUID();
		return name.substring(name.indexOf("-") + 1, name.length()).replaceAll("-", "");
	}
	
	/**
	 * 玩家改头像
	 * 
	 * @param robotEntity
	 */
	private void doChangeIconAction(GameRobotEntity robotEntity) {
		PlayerChangeIconReq.Builder builder = PlayerChangeIconReq.newBuilder();
		if(HawkRand.randPercentRate(30)) {
			builder.setUseGold(true);
		} else {
			builder.setUseGold(false);
		}
		
		builder.setIconId(HawkRand.randInt(0, 1) == 0 ? 0 : 10);
		robotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_CHANGE_ICON_C_VALUE, builder));
	}
	
	/**
	 * 获取玩家信息
	 * 
	 * @param robotEntity
	 */
	private void doFetchPlayerInfoAction(GameRobotEntity robotEntity) {
		int index = 0;
		if(map.containsKey(robotEntity.getPlayerId())){
			index = map.get(robotEntity.getPlayerId());
		}
		
		// 领主详情
		if(index++ == 0) {
			robotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_DETAIL_C_VALUE));
			map.put(robotEntity.getPlayerId(), index);
			return;
		}
		
		GameRobotEntity randRobotEntity = (GameRobotEntity) GameRobotApp.getInstance().randRobotEntity(robotEntity.getPlayerId());
		if(randRobotEntity == null) {
			map.put(robotEntity.getPlayerId(), index);
			return;
		}
		
		// 获取其他领主详情
		if(index++ == 1) {
			PlayerDetailReq.Builder builder = PlayerDetailReq.newBuilder();
			builder.setPlayerId(randRobotEntity.getPlayerId());
			robotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_DETAIL_OTHER_C_VALUE, builder));
			map.put(robotEntity.getPlayerId(), index);
			return;
		}
		
		// 获得本地玩家基本信息
		if(index++ == 2) {
			GetPlayerBasicInfoReq.Builder builder1 = GetPlayerBasicInfoReq.newBuilder();
			builder1.setName(randRobotEntity.getName());
			robotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_GETLOCALPLAYERINFOBYNAME_C_VALUE, builder1));
			map.put(robotEntity.getPlayerId(), index);
			return;
		}
		
		// 获得全局玩家基本信息
		if(index++ == 3) {
			index = 0;
			GetPlayerBasicInfoReq.Builder builder2 = GetPlayerBasicInfoReq.newBuilder();
			builder2.setName(randRobotEntity.getName());
			robotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_GETGLOBALPLAYERINFOBYNAME_C_VALUE, builder2));
			map.put(robotEntity.getPlayerId(), index);
		}
	}
	
	/**
	 * 打开指挥官信息面板操作
	 * 
	 * @param robotEntity
	 */
	private void doOpenBoardAction(GameRobotEntity robotEntity) {
		robotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.OPEN_PLAYER_BOARD_C_VALUE));
	}
	
	/**
	 * 屏蔽玩家操作
	 * 
	 * @param robotEntity
	 */
	private void doShieldPlayerAction(GameRobotEntity robotEntity) {
		GameRobotEntity randRobotEntity = (GameRobotEntity) GameRobotApp.getInstance().randRobotEntity(robotEntity.getPlayerId());
		if(randRobotEntity != null) {
			ShieldPlayerReq.Builder builder = ShieldPlayerReq.newBuilder();
			builder.setPlayerId(randRobotEntity.getPlayerId());
			robotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.SHIELD_PLAYER_C_VALUE, builder));
		}
	}
	
	/**
	 * 解除屏蔽操作
	 * 
	 * @param robotEntity
	 */
	private void doRemoveShieldAction(GameRobotEntity robotEntity) {
		GameRobotEntity randRobotEntity = (GameRobotEntity) GameRobotApp.getInstance().randRobotEntity(robotEntity.getPlayerId());
		if(randRobotEntity != null) {
			RemoveShieldPlayerReq.Builder builder = RemoveShieldPlayerReq.newBuilder();
			builder.setPlayerId(randRobotEntity.getPlayerId());
			robotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.REMOVE_SHIELD_C_VALUE, builder));
		}
	}

}
