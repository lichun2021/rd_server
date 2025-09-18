package com.hawk.robot.response.world;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.SearchType;
import com.hawk.game.protocol.World.WorldSearchResp;
import com.hawk.robot.GameRobotApp;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.action.march.type.impl.AttackMonster;
import com.hawk.robot.action.march.type.impl.AttackNewMonster;
import com.hawk.robot.action.march.type.impl.CollectResource;
import com.hawk.robot.action.march.type.impl.MassFoggy;
import com.hawk.robot.action.march.type.impl.Strongpoint;
import com.hawk.robot.annotation.RobotResponse;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = HP.code.WORLD_SEARCH_S_VALUE)
public class WorldSearchResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		// 查找资源、野怪
		WorldSearchResp resp = protocol.parseProtocol(WorldSearchResp.getDefaultInstance());
		//robotEntity.getData().getWorldData().setWorldSearchResp(resp);
		
		switch (resp.getType().getNumber()) {
		case SearchType.SEARCH_MONSTER_VALUE:
			GameRobotApp.getInstance().executeTask(new Runnable() {
				@Override
				public void run() {
					AttackMonster.startMarch(robotEntity, resp);
				}
			});
			break;
		case SearchType.SEARCH_RESOURCE_VALUE:
			GameRobotApp.getInstance().executeTask(new Runnable() {
				@Override
				public void run() {
					CollectResource.startMarch(robotEntity, resp);
				}
			});
			break;
		case SearchType.SEARCH_STRONGPOINT_VALUE:
			GameRobotApp.getInstance().executeTask(new Runnable() {
				@Override
				public void run() {
					Strongpoint.startMarch(robotEntity, resp);
				}
			});
			break;
		case SearchType.SEARCH_NEW_MONSTER_VALUE:
			GameRobotApp.getInstance().executeTask(new Runnable() {
				@Override
				public void run() {
					AttackNewMonster.startMarch(robotEntity, resp);
				}
			});
			break;
		case SearchType.SEARCH_FOGGY_VALUE:
			GameRobotApp.getInstance().executeTask(new Runnable() {
				@Override
				public void run() {
					MassFoggy.startMarch(robotEntity, resp);
				}
			});
			break;
		default:
			break;
		}
	}

}
