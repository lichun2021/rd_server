package com.hawk.game.invoker;

import java.util.ArrayList;
import java.util.List;

import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.config.MissionCfg;
import com.hawk.game.entity.MissionEntity;
import com.hawk.game.module.PlayerLifetimeCardModule;
import com.hawk.game.player.Player;
import com.hawk.game.service.StoryMissionService;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.MissionState;

public class CityLevelUpRefreshMissionInvoker extends HawkMsgInvoker {
	
	private Player player;
	private int cityLevel;
	
	public CityLevelUpRefreshMissionInvoker(Player player, int cityLevel) {
		this.player = player;
		this.cityLevel = cityLevel;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		List<MissionEntity> missions = player.getData().getMissionEntities();
		List<MissionEntity> removeMissions = new ArrayList<>();
		for (MissionEntity entity : missions) {
			if(entity.getState() == MissionState.STATE_NOT_OPEN) {
				MissionCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MissionCfg.class, entity.getCfgId());
				if(cfg.getCastleClass() <= cityLevel) {
					removeMissions.add(entity);
				}
			}
		}
		
		missions.removeAll(removeMissions);
		HawkDBEntity.batchDelete(removeMissions);
		StoryMissionService.getInstance().upLevelRefresh(player);
		
		PlayerLifetimeCardModule module = player.getModule(GsConst.ModuleType.LIFETIME_CARD);
		module.onCityLevelUp();
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public int getCityLevel() {
		return cityLevel;
	}
	
}
