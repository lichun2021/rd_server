package com.hawk.robot.action.plot;

import org.hawk.annotation.RobotAction;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.enums.EnumUtil;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.PlotBattle;
import com.hawk.robot.config.PlotLevelCfg;

@RobotAction(valid = true)
public class PlotAction extends HawkRobotAction{

	@Override
	public void doAction(HawkRobotEntity entity) {
		PlotType plotType = EnumUtil.random(PlotType.class);
		switch(plotType) {
		case UP_LOAD:
			doUpLoad(entity);
			break;
		
		}
	}
	
	private void doUpLoad(HawkRobotEntity entity) {
		PlotBattle.UploadBattleReq.Builder reqBuilder = PlotBattle.UploadBattleReq.newBuilder();
		int levelsId = getRandomLevelsId();
		reqBuilder.setLevelsId(levelsId);
		reqBuilder.setCostTime(0);
		
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.UPLOAD_BATTLE_REQ_VALUE, reqBuilder);
		entity.sendProtocol(protocol);
	}
	
	private int getRandomLevelsId() {
		ConfigIterator<PlotLevelCfg> plotLevelCfgIterator = HawkConfigManager.getInstance().getConfigIterator(PlotLevelCfg.class);
		int index = HawkRand.randInt(0, plotLevelCfgIterator.size() - 1);
		
		int count=0;
		while(plotLevelCfgIterator.hasNext()) {
			PlotLevelCfg levelCfg = plotLevelCfgIterator.next();
			if (count == index) {
				return levelCfg.getMissionId(); 
			}
			count++;
		}
		
		return 0;
	}

	private static enum PlotType {
		UP_LOAD;
	} 
}
