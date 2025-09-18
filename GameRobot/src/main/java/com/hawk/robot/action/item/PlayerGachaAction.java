package com.hawk.robot.action.item;

import org.hawk.annotation.RobotAction;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;

/**
 * 
 * 扭蛋
 * 
 * @author lating
 *
 */
@RobotAction(valid = false)
public class PlayerGachaAction extends HawkRobotAction {
	

	@Override
	public void doAction(HawkRobotEntity robotEntity) {
		// 这个版本不需要招募攻能
//		GameRobotEntity gameRobotEntity = (GameRobotEntity) robotEntity;
//		if(HawkRand.randPercentRate(50)) {
//			// 扭蛋
//			List<Integer> gachaTypeList = GachaCfg.getIdlist();
//			Collections.shuffle(gachaTypeList);
//			HPGachaReq.Builder builder = HPGachaReq.newBuilder();
//			builder.setGachaType(gachaTypeList.get(0));
//			gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.GACHA_C_VALUE, builder));
//		} else {
//			// 主动请求同步扭蛋信息
//			gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.GACHA_SYNC_C_VALUE));
//		}
	}
}
