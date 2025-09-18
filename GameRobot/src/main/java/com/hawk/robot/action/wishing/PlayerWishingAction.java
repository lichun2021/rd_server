package com.hawk.robot.action.wishing;

import java.util.List;

import org.hawk.annotation.RobotAction;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;

import com.hawk.game.protocol.Building.BuildingPB;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Wishing.PlayerWishingReq;
import com.hawk.robot.GameRobotEntity;

/**
 * 许愿池机器人
 * @author golden
 *
 */
@RobotAction(valid = false)
public class PlayerWishingAction extends HawkRobotAction {
	
	static final PlayerAttr[] RES_TYPES = {PlayerAttr.GOLDORE_UNSAFE, 
			PlayerAttr.OIL_UNSAFE,
			PlayerAttr.STEEL_UNSAFE, 
			PlayerAttr.TOMBARTHITE_UNSAFE,
			PlayerAttr.GOLDORE,
			PlayerAttr.OIL,
			PlayerAttr.STEEL,
			PlayerAttr.TOMBARTHITE};

	@Override
	public void doAction(HawkRobotEntity entity) {
		GameRobotEntity robot = (GameRobotEntity) entity;
		List<BuildingPB> wishBuilding = robot.getBuildingByType(BuildingType.WISHING_WELL_VALUE);
		if (wishBuilding.isEmpty()) {
			return;
		}
		PlayerWishingReq.Builder builder = PlayerWishingReq.newBuilder();
		builder.setResourceType(rendomResourceType());
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.WISHING_C_VALUE, builder));
	}

	/**
	 * 随机资源类型
	 * @return
	 */
	private PlayerAttr rendomResourceType() {
		return RES_TYPES[HawkRand.randInt(RES_TYPES.length - 1)];
	}
}
