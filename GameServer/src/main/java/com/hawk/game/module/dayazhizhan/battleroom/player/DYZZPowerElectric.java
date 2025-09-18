package com.hawk.game.module.dayazhizhan.battleroom.player;

import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.PowerElectric;
import com.hawk.log.LogConst.PowerChangeReason;

public class DYZZPowerElectric extends PowerElectric{

	public DYZZPowerElectric(PlayerData playerData) {
		super(playerData);
	}

	
	/**
	 * 战斗力\电力刷新，如果有变化推送前台
	 * @param player
	 * @param isSoliderCure 是否伤兵治疗恢复
	 * @param needSync
	 */
	@Override
	public void refreshPowerElectric(Player player, boolean isSoliderCure, boolean needSync, PowerChangeReason reason) {
		IDYZZPlayer dyzzPlayer = (IDYZZPlayer) player;
		// 计算当前战力属性
		calcElectric();
		
		long afterPoint = getPowerData().getTotalPoint();
		
		getPowerData().setBattlePoint(afterPoint);

		// 保存战力值
		dyzzPlayer.setPower(afterPoint);

		// 属性同步
		if (dyzzPlayer.isActiveOnline() && needSync) {
			dyzzPlayer.getPush().syncPlayerInfo();
		}
	
	}
}
