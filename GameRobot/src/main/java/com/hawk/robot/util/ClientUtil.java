package com.hawk.robot.util;

import java.util.List;
import java.util.Map;
import org.hawk.os.HawkTime;

import com.hawk.game.protocol.World.WorldMarchPB;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.WorldDataManager;
import com.hawk.robot.config.ConstProperty;

/**
 *
 * @author lating
 * @since 2017年10月9日
 */
public class ClientUtil {
	
	public static boolean isExecuteAllowed(GameRobotEntity robot, String actionName, long peroid) {
		Map<String, Long> lastExecuteTime = robot.getCityData().getLastExecuteTime();
		if (!lastExecuteTime.containsKey(actionName)) {
			return true;
		}
		
		if (HawkTime.getMillisecond() - lastExecuteTime.get(actionName) > peroid) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * 判断一个作用号是否是资源增产作用号
	 * @param effectId
	 * @return
	 */
	public static boolean isResProduceUpEffect(Integer effectId) {
		List<Integer> effects = ConstProperty.getInstance().getResUpEffectList();
		return effects != null && effects.contains(effectId);
	}
	
	/**
	 * 判断一个行军类型是否是攻击性行军
	 * @param playerId
	 * @param marchType
	 * @param terminalId
	 * @return
	 */
	public static boolean isOffensiveAction(WorldMarchType marchType) {
		
		if (marchType.equals(WorldMarchType.ATTACK_PLAYER)
				
				|| marchType.equals(WorldMarchType.MASS)
				|| marchType.equals(WorldMarchType.MASS_JOIN)
				
				|| marchType.equals(WorldMarchType.SPY)

				|| marchType.equals(WorldMarchType.MANOR_SINGLE)
				|| marchType.equals(WorldMarchType.MANOR_MASS)
				|| marchType.equals(WorldMarchType.MANOR_MASS_JOIN)
				
				|| marchType.equals(WorldMarchType.PRESIDENT_SINGLE)
				|| marchType.equals(WorldMarchType.PRESIDENT_MASS)
				|| marchType.equals(WorldMarchType.PRESIDENT_MASS_JOIN)
				
				|| marchType.equals(WorldMarchType.PRESIDENT_TOWER_SINGLE)
				|| marchType.equals(WorldMarchType.PRESIDENT_TOWER_MASS)
				|| marchType.equals(WorldMarchType.PRESIDENT_TOWER_MASS_JOIN)) {
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * 判断一个玩家身上是否有攻击性行军
	 * @param robot
	 * @return
	 */
	public static boolean hasOffensiveMarch(GameRobotEntity robot) {
		for (String marchId : robot.getWorldData().getMarchIdList()) {
			WorldMarchPB march = WorldDataManager.getInstance().getMarch(marchId);
			if (march != null && isOffensiveAction(march.getMarchType())) {
				return true;
			}
		}
		
		return false;
	}
}
