package com.hawk.robot.action.march;

import java.util.List;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.WorldDataManager;
import com.hawk.robot.RobotLog;

import org.hawk.annotation.RobotAction;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;

import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.WorldMarchPB;
import com.hawk.game.protocol.World.WorldMarchRelation;
import com.hawk.game.protocol.World.WorldMarchSpeedUpReq;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;

/**
 * 
 * 行军加速
 * 
 * @author lating
 *
 */
@RobotAction(valid = true)
public class WorldMarchSpeedAction extends HawkRobotAction {

	@Override
	public void doAction(HawkRobotEntity robotEntity) {
		GameRobotEntity gameRobotEntity = (GameRobotEntity) robotEntity;
		List<String> marchIdList = gameRobotEntity.getWorldData().getMarchIdList();
		if(marchIdList == null || marchIdList.size() <= 0) {
			return;
		}
		
		for(String marchId : marchIdList) {
			WorldMarchPB worldMarch = WorldDataManager.getInstance().getMarch(marchId);
			if (worldMarch == null) {
				RobotLog.worldErrPrintln("fetch wolrd march failed, playerId: {}, marchId: {}", gameRobotEntity.getPlayerId(), marchId);
				continue;
			}
			
			if (worldMarch.getRelation() != WorldMarchRelation.SELF) {
				continue;
			}
			
			WorldMarchStatus marchStatus = worldMarch.getMarchStatus();
			// 非去程或回程行军，不予处理
			if( marchStatus != WorldMarchStatus.MARCH_STATUS_MARCH && marchStatus != WorldMarchStatus.MARCH_STATUS_RETURN_BACK) {
				continue;
			}
			
			WorldMarchType marchType = worldMarch.getMarchType();
			// 非集结类行军剩余时间小于1分钟，不予处理
			if(worldMarch.getEndTime() - HawkTime.getMillisecond() <= 60000 && !isMassJoinMarch(marchType)) {
				continue;
			}
			
			// 进攻玩家基地的去程行军
			if (marchStatus == WorldMarchStatus.MARCH_STATUS_MARCH && isAtkPlayerMarch(marchType) && HawkRand.randPercentRate(60)) {
				sendProtocol(gameRobotEntity, worldMarch);
				continue;
			}
			
			// 参与集结的去程行军
			if (marchStatus == WorldMarchStatus.MARCH_STATUS_MARCH && isMassJoinMarch(marchType) && HawkRand.randPercentRate(70)) {
				sendProtocol(gameRobotEntity, worldMarch);
				continue;
			}
			
			// 国王战相关的去程行军
			if (marchStatus == WorldMarchStatus.MARCH_STATUS_MARCH && isPresidentMarch(marchType) && HawkRand.randPercentRate(85)) {
				sendProtocol(gameRobotEntity, worldMarch);
				continue;
			}
			
			// 其它行军
			if (HawkRand.randPercentRate(15)) {
				sendProtocol(gameRobotEntity, worldMarch);
			}
		}
	}
	
	/**
	 * 发送行军加速协议
	 * @param gameRobot
	 * @param worldMarch
	 */
	private void sendProtocol(GameRobotEntity gameRobot, WorldMarchPB worldMarch) {
		WorldMarchSpeedUpReq.Builder builder = WorldMarchSpeedUpReq.newBuilder();
		builder.setMarchId(worldMarch.getMarchId());
		builder.setItemId(Const.ItemId.ITEM_WORLD_MARCH_SPEEDUPH_VALUE);
		gameRobot.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MARCH_SPEEDUP_C_VALUE, builder));
		RobotLog.worldPrintln("worldMarch speed, playerId: {}, marchId: {}, marchStatus: {}, time: {}", gameRobot.getPlayerId(), worldMarch.getMarchId(), worldMarch.getMarchStatus().name(), (worldMarch.getEndTime() - HawkTime.getMillisecond())/1000);
	}
	
	private boolean isAtkPlayerMarch(WorldMarchType marchType) {
		return marchType == WorldMarchType.ATTACK_PLAYER || marchType == WorldMarchType.MASS;
	}
	
	private boolean isMassJoinMarch(WorldMarchType marchType) {
		return marchType == WorldMarchType.MASS_JOIN 
				|| marchType == WorldMarchType.MANOR_MASS_JOIN 
				|| marchType == WorldMarchType.MANOR_ASSISTANCE_MASS_JOIN
				|| marchType == WorldMarchType.PRESIDENT_MASS_JOIN
			    || marchType == WorldMarchType.PRESIDENT_ASSISTANCE_MASS_JOIN
				|| marchType == WorldMarchType.MONSTER_MASS_JOIN
				|| marchType == WorldMarchType.PRESIDENT_TOWER_MASS_JOIN
				|| marchType == WorldMarchType.FOGGY_FORTRESS_MASS_JOIN
				|| marchType == WorldMarchType.SUPER_WEAPON_MASS_JOIN;
	}
	
	private boolean isPresidentMarch(WorldMarchType marchType) {
		return marchType == WorldMarchType.PRESIDENT_SINGLE 
				|| marchType == WorldMarchType.PRESIDENT_MASS 
				|| marchType == WorldMarchType.PRESIDENT_ASSISTANCE
				|| marchType == WorldMarchType.PRESIDENT_ASSISTANCE_MASS
				|| marchType == WorldMarchType.PRESIDENT_TOWER_SINGLE
				|| marchType == WorldMarchType.PRESIDENT_TOWER_MASS;
	}
}
