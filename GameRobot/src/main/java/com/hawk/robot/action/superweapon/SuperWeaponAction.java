package com.hawk.robot.action.superweapon;

import java.util.ArrayList;
import java.util.List;

import org.hawk.annotation.RobotAction;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponPeriod;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponSignUp;
import com.hawk.game.protocol.World.WorldMarchPB;
import com.hawk.game.protocol.World.WorldMarchReq;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.robot.GameRobotApp;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.RobotLog;
import com.hawk.robot.WorldDataManager;
import com.hawk.robot.util.GuildUtil;
import com.hawk.robot.util.WorldUtil;

/**
 * 超级武器机器人行为
 * @author zhenyu.shang
 * @since 2018年5月5日
 */
@RobotAction(valid = true)
public class SuperWeaponAction extends HawkRobotAction {
	
	private static List<Integer> sw = new ArrayList<Integer>();
	
	static {
		sw.add(WorldUtil.combineXAndY(350,700));
		sw.add(WorldUtil.combineXAndY(250,500));
		sw.add(WorldUtil.combineXAndY(250,700));
		sw.add(WorldUtil.combineXAndY(350,500));
		sw.add(WorldUtil.combineXAndY(400,600));
		sw.add(WorldUtil.combineXAndY(300,800));
		sw.add(WorldUtil.combineXAndY(300,400));
		sw.add(WorldUtil.combineXAndY(200,600));
	}

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
		//策略：如果超级武器开战的情况下，向各个超级武器随机发起行军
		//先判断是否为超级武器开战期间
		int status = WorldDataManager.getInstance().getSuperWeaponStatus();
		if(status == SuperWeaponPeriod.WARFARE_VALUE){
			//先随机一个超武
			int pointId = WorldDataManager.getInstance().randomSuperWeaponPointId(guildId);
			if(pointId <= 0){
				RobotLog.guildErrPrintln("guild has not sign up data, guildId : {}", guildId);
				return ;
			}
			int pos[] = WorldUtil.splitXAndY(pointId);
			if(HawkRand.randPercentRate(5)){ //25%的几率发起集结
				if(HawkOSOperator.isEmptyString(guildId)) {
					return;
				}
				//判断当前是否有发起集结
				WorldMarchPB massMarch = WorldDataManager.getInstance().getPresidentMassMarch(guildId);
				if(massMarch == null) {
					startMassMarch(gameRobotEntity, pos[0], pos[1], WorldMarchType.SUPER_WEAPON_MASS);
				} else {
					startMassJoinMarch(massMarch, gameRobotEntity, pos[0], pos[1], WorldMarchType.SUPER_WEAPON_MASS_JOIN);
				}
			} else {
				//攻击王城
				startMarch(gameRobotEntity, pos[0], pos[1], WorldMarchType.SUPER_WEAPON_SINGLE, HP.code.SUPER_WEAPON_SINGLE_MARCH_C_VALUE);
			}
		} else if(status == SuperWeaponPeriod.SIGNUP_VALUE){
			//报名阶段先报名
			//先随机一个超武
			int pointId = HawkRand.randomObject(sw);
			if(gameRobotEntity.getGuildData().isLeader() && WorldDataManager.getInstance().canSignUp(guildId, pointId)){ //4级以上可以报名
				int[] pos = WorldUtil.splitXAndY(pointId);
				SuperWeaponSignUp.Builder builder = SuperWeaponSignUp.newBuilder();
				builder.setPosX(pos[0]);
				builder.setPosY(pos[1]);
				gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.SUPER_WEAPON_WAR_SIGN_UP_C_VALUE, builder));
				RobotLog.guildPrintln("guild has sign up the point ({}, {}), guildId : {}", pos[0], pos[1], guildId);
			}
		} else if(status == SuperWeaponPeriod.PEACE_VALUE){
			WorldDataManager.getInstance().clearSignUpData();
		}
	}
	
	/**
	 * 发起领地行军
	 * @param gameRobotEntity
	 * @param x
	 * @param y
	 */
	private void startMarch(GameRobotEntity gameRobotEntity, int x, int y, WorldMarchType worldMarchType, int code) {
		//每个人只能发出1条超级武器行军
		if (gameRobotEntity.getWorldData().getMarchCount() > 0) {
			return;
		}
		WorldMarchReq.Builder builder = WorldUtil.generatorMarchBuilder(gameRobotEntity, x, y, worldMarchType, true);
		if (builder == null) {
			return;
		}
		gameRobotEntity.sendProtocol(HawkProtocol.valueOf(code, builder));
		RobotLog.guildPrintln("start march action president, playerId: {}, marchType: {}, pos : {}", gameRobotEntity.getPlayerId(), worldMarchType, x + "," + y);
	}
	
	/**
	 * 发起集结行军
	 * @param robot
	 * @param x
	 * @param y
	 * @param worldMarchType
	 */
	private void startMassMarch(GameRobotEntity robot, int x, int y, WorldMarchType worldMarchType){
		if (robot.getWorldData().getMarchCount() > 0) {
			return;
		}
		//发起行军
		WorldMarchReq.Builder builder = WorldUtil.generatorMarchBuilder(robot, x, y, worldMarchType, true);
		if (builder == null) {
			return;
		}
		builder.setMassTime(600);
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MASS_C_VALUE, builder));
		RobotLog.worldPrintln("start march action, playerId: {}, marchType: {}", robot.getPlayerId(), worldMarchType);
	}
	
	/**
	 * 发起集结加入行军
	 * @param massMarch
	 * @param robot
	 * @param x
	 * @param y
	 * @param worldMarchType
	 */
	private void startMassJoinMarch(WorldMarchPB massMarch, GameRobotEntity robot, int x, int y, WorldMarchType worldMarchType){
		if (robot.getWorldData().getMarchCount() > 0) {
			return;
		}
		if (massMarch.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_WAITING) {
			RobotLog.worldErrPrintln("start march failed, mass march status not waiting, playerId: {}", robot.getPlayerId());
			return;
		}
		WorldMarchReq.Builder builder = WorldUtil.generatorMarchBuilder(robot, massMarch.getOrigionX(), massMarch.getOrigionY(), null, true);
		if (builder == null) {
			return;
		}
		builder.setMarchId(massMarch.getMarchId());
		builder.setType(worldMarchType);
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MASS_JOIN_C_VALUE, builder));
		RobotLog.worldPrintln("start march action, playerId: {}, marchType: {}", robot.getPlayerId(), worldMarchType);
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
