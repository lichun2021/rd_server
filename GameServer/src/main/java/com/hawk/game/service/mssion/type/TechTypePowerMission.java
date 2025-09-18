package com.hawk.game.service.mssion.type;

import java.util.List;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.config.TechnologyCfg;
import com.hawk.game.entity.TechnologyEntity;
import com.hawk.game.item.mission.MissionCfgItem;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.mssion.Mission;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.event.EventTechTypePower;

/**
 * 科技{1}战力达到{2
 * @author golden
 *
 */
@Mission(missionType = MissionType.MISSION_TECH_TYPE_POWER)
public class TechTypePowerMission implements IMission {

	@Override
	public <T extends MissionEvent> void refreshGeneralMission(Player player, T missionEvent) {
		
	}

	@Override
	public <T extends MissionEvent> void refreshMission(PlayerData playerData, T missionEvent, MissionEntityItem entityItem, MissionCfgItem cfg) {
		EventTechTypePower event = (EventTechTypePower)missionEvent;
		int conditionType = cfg.getIds().get(0);
		if (event.getType() != conditionType) {
			return;
		}
		entityItem.setValue(event.getPower());
		checkMissionFinish(entityItem, cfg);
	}

	@Override
	public void initMission(PlayerData playerData, MissionEntityItem entityItem, MissionCfgItem cfg) {
		int conditionType = cfg.getIds().get(0);
		int power = getTechBattlePointByType(playerData, conditionType);
		entityItem.setValue(power);
		checkMissionFinish(entityItem, cfg);
	}
	
	/**
	 * 获取某类科技的总战力
	 * @param techType
	 * @return
	 */
	private int getTechBattlePointByType(PlayerData playerData, int techType){
		int techBattlePoint = 0;
		List<TechnologyEntity> technologyEntities = playerData.getTechnologyEntities();
		for (TechnologyEntity technologyEntity : technologyEntities) {
			if (technologyEntity.getLevel() > 0) {
				TechnologyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(TechnologyCfg.class, technologyEntity.getCfgId());
				if (cfg != null && cfg.getTechType() == techType) {
					techBattlePoint += cfg.getBattlePoint();
				}
			}
		}
		return techBattlePoint;
	}
}
