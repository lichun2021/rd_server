package com.hawk.robot.action.president;

import java.util.ArrayList;
import java.util.List;

import org.hawk.annotation.RobotAction;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.President.PresidentPeriod;
import com.hawk.game.protocol.World.PlayerEnterWorld;
import com.hawk.game.protocol.World.PlayerWorldMove;
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
 * 国王战机器人
 * @author zhenyu.shang
 * @since 2018年1月1日
 */
@RobotAction(valid = true)
public class PresidentAction extends HawkRobotAction{
	
	private static List<Integer> towers =  new ArrayList<Integer>();
	
	static {
		towers.add(WorldUtil.combineXAndY(300,591));
		towers.add(WorldUtil.combineXAndY(300,609));
		towers.add(WorldUtil.combineXAndY(291,600));
		towers.add(WorldUtil.combineXAndY(309,600));
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
		//策略：如果国王战开启的情况下，向箭塔和王城随机发起行军
		//先判断是否为国王战开战期间
		int status = WorldDataManager.getInstance().getPresidentStatus();
		if(status == PresidentPeriod.WARFARE_VALUE || status == PresidentPeriod.OVERTIME_VALUE){
			//判断王城到城点的距离，如果在一定范围外，则不攻击
			int radius = GameRobotApp.getInstance().getConfig().getInt("presidentAttackRadius");
			double distance = WorldUtil.lineDistance(gameRobotEntity.getWorldData().getWorldInfo().getTargetX(), gameRobotEntity.getWorldData().getWorldInfo().getTargetY(), 300, 600);
			
			RobotLog.guildPrintln("president distance info, radius: {}, distance : {}", radius, distance);
			if(distance > radius){
				return;
			}
			if(HawkRand.randPercentRate(70)){
				if(HawkRand.randPercentRate(25)){ //25%的几率发起集结
					if(HawkOSOperator.isEmptyString(guildId)) {
						return;
					}
					//判断当前是否有发起集结
					WorldMarchPB massMarch = WorldDataManager.getInstance().getPresidentMassMarch(guildId);
					if(massMarch == null) {
						startMassMarch(gameRobotEntity, 300, 600, WorldMarchType.PRESIDENT_MASS);
					} else {
						startMassJoinMarch(massMarch, gameRobotEntity, 300, 600, WorldMarchType.PRESIDENT_MASS_JOIN);
					}
				} else {
					//攻击王城
					startMarch(gameRobotEntity, 300, 600, WorldMarchType.PRESIDENT_SINGLE, HP.code.PRESIDENT_SINGLE_MARCH_C_VALUE);
				}
			} else {
				//攻击箭塔
				int pointId = HawkRand.randomObject(towers);
				int[] xy = WorldUtil.splitXAndY(pointId);
				if(HawkRand.randPercentRate(25)){ //25%的几率发起集结
					if(HawkOSOperator.isEmptyString(guildId)) {
						return;
					}
					//判断当前是否有发起集结
					WorldMarchPB massMarch = WorldDataManager.getInstance().getPresidentTowerMassMarch(guildId);
					if(massMarch == null) {
						startMassMarch(gameRobotEntity, xy[0], xy[1], WorldMarchType.PRESIDENT_TOWER_MASS);
					} else {
						startMassJoinMarch(massMarch, gameRobotEntity, xy[0], xy[1], WorldMarchType.PRESIDENT_TOWER_MASS_JOIN);
					}
				} else {
					startMarch(gameRobotEntity, xy[0], xy[1], WorldMarchType.PRESIDENT_TOWER_SINGLE, HP.code.PRESIDENT_TOWER_SINGLE_MARCH_C_VALUE);
				}
			}
			
			if(gameRobotEntity.getWorldViewPosition() > 0){
				int[] p = WorldUtil.splitXAndY(gameRobotEntity.getWorldViewPosition());
				int[] pos = new int[2];
				if(p[0] == 300 && p[1] == 600){
					pos[0] = HawkRand.randInt(100, 200);
					pos[1] = HawkRand.randInt(700, 1100);
				} else {
					pos[0] = 300;
					pos[1] = 600;
				}
				PlayerWorldMove.Builder req = PlayerWorldMove.newBuilder();
				req.setX(pos[0]);
				req.setY(pos[1]);
				gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_WORLD_MOVE_VALUE, req));
				gameRobotEntity.setWorldViewPosition(WorldUtil.combineXAndY(pos[0], pos[1]));
			} else {
				int x = 300;
				int y = 600;
				PlayerEnterWorld.Builder enterWorld = PlayerEnterWorld.newBuilder();
				enterWorld.setX(x);
				enterWorld.setY(y);
				gameRobotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_ENTER_WORLD_VALUE, enterWorld));
				gameRobotEntity.setWorldViewPosition(WorldUtil.combineXAndY(x, y));
			}
		}
	}
	
	/**
	 * 发起领地行军
	 * @param gameRobotEntity
	 * @param x
	 * @param y
	 */
	private void startMarch(GameRobotEntity gameRobotEntity, int x, int y, WorldMarchType worldMarchType, int code) {
		//每个人只能发出3条国王战行军
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
