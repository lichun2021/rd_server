package com.hawk.robot.action.march.type.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.hawk.robot.annotation.RobotMarch;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.SearchType;
import com.hawk.game.protocol.World.WorldMarchReq;
import com.hawk.game.protocol.World.WorldSearchReq;
import com.hawk.game.protocol.World.WorldSearchResp;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.RobotLog;
import com.hawk.robot.action.march.March;
import com.hawk.robot.action.march.type.MarchType;
import com.hawk.robot.config.WorldResourceCfg;
import com.hawk.robot.util.WorldUtil;

/**
 * 机器人行军 - 采集
 * @author golden
 *
 */
@RobotMarch(marchType = "COLLECT_RESOUREC")
public class CollectResource implements March {

	@Override
	public void startMarch(GameRobotEntity robot) {
		WorldSearchReq.Builder req = WorldSearchReq.newBuilder();
		req.setType(SearchType.SEARCH_RESOURCE);
		
		Random random = new Random();
		
		List<Integer> keys = new ArrayList<Integer>();
		ConfigIterator<WorldResourceCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(WorldResourceCfg.class);
		while (iterator.hasNext()) {
			WorldResourceCfg cfg = iterator.next();
			keys.add(cfg.getId());
		}
		if (!keys.isEmpty()) {
			int resourceId = keys.get(random.nextInt(keys.size()));
			req.setId(resourceId);
			robot.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_SEARCH_C_VALUE, req));
		}
	}

	@Override
	public int getMarchType() {
		return MarchType.COLLECT_RESOUREC.intVal();
	}
	
	public static void startMarch(GameRobotEntity robot, WorldSearchResp resp) {
		if(resp == null) {
			return;
		}
		int x = resp.getTargetX();
		int y = resp.getTargetY();
		
		WorldMarchReq.Builder builder = WorldUtil.generatorMarchBuilder(robot, x, y, null, true);
		if (builder == null) {
			return;
		}
		
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_COLLECTRESOURCE_C_VALUE, builder));

		RobotLog.worldPrintln("start march action, playerId: {}, marchType: {}", robot.getPlayerId(), MarchType.COLLECT_RESOUREC.name());
	}
}
