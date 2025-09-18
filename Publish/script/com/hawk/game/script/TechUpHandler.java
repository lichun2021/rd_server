package com.hawk.game.script;

import java.util.Map;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;

import com.hawk.game.config.TechnologyCfg;
import com.hawk.game.entity.TechnologyEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.log.LogConst.PowerChangeReason;

/**
 * 科技一键升级
 * 
 * localhost:8080/script/techLvlUp?playerId=1aat-2awl7h-1&playerName=111
 * 
 * @author lating
 *
 */
public class TechUpHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "player not exist");
			}
			
			if (player.isActiveOnline()) {
				int threadIdx = player.getXid().getHashThread(HawkTaskManager.getInstance().getThreadNum());
				HawkTaskManager.getInstance().postTask(new HawkTask() {
					@Override
					public Object run() {
						ConfigIterator<TechnologyCfg> cfgs = HawkConfigManager.getInstance().getConfigIterator(TechnologyCfg.class);
						for(TechnologyCfg cfg : cfgs){
							techLevelUp(player, cfg);
						}
						return null;
					}
				}, threadIdx);
			} else {
				ConfigIterator<TechnologyCfg> cfgs = HawkConfigManager.getInstance().getConfigIterator(TechnologyCfg.class);
				for(TechnologyCfg cfg : cfgs){
					techLevelUp(player, cfg);
				}
			}
			
			return successResponse("success");

		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "");
	}
	/**
	 * 科技升级
	 * @param techId
	 * @return
	 */
	private boolean techLevelUp(Player player, TechnologyCfg cfg) {
		int techId = cfg.getTechId();
		TechnologyEntity entity = player.getData().getTechEntityByTechId(techId);
		if (entity == null) {
			entity = player.getData().createTechnologyEntity(cfg);
		}

		player.getData().getPlayerEffect().addEffectTech(player, entity);
		entity.setLevel(cfg.getLevel());
		entity.setResearching(false);
		player.getPush().syncTechnologyLevelUpFinish(entity.getCfgId());
		player.refreshPowerElectric(PowerChangeReason.TECH_LVUP);

		// 如果科技解锁技能,则推送科技技能信息
		if (cfg.getTechSkill() > 0) {
			player.getPush().syncTechSkillInfo();
		}

		return true;
	}
}
