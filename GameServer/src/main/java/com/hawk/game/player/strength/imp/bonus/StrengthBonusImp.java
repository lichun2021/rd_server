package com.hawk.game.player.strength.imp.bonus;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.config.SoldierStrengthTypeCfg;
import com.hawk.game.player.Player;
import com.hawk.game.player.strength.imp.PlayerStrengthCell;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * 战力实现
 * @author Golden
 *
 */
public interface StrengthBonusImp {

	/**
	 * 计算战力
	 * @param playerData
	 * @param soldierType
	 * @param cell
	 */
	void calc(Player player, SoldierType soldierType, PlayerStrengthCell cell);
	
	/**
	 * 获取战力计算配置
	 * @return
	 */
	default SoldierStrengthTypeCfg getStrengthTypeCfg() {
		return HawkConfigManager.getInstance().getConfigByKey(SoldierStrengthTypeCfg.class, getStrengthType());
	}
	
	/**
	 * 获取战力类型
	 * @return
	 */
	default int getStrengthType() {
		return getClass().getAnnotation(StrengthType.class).strengthType();
	}
}
