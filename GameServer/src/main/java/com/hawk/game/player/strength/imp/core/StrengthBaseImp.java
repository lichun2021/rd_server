package com.hawk.game.player.strength.imp.core;

import com.hawk.game.player.PlayerData;
import com.hawk.game.player.strength.imp.PlayerStrengthCell;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * 战力实现
 * @author Golden
 *
 */
public interface StrengthBaseImp {

	/**
	 * 计算战力
	 * @param playerData
	 * @param soldierType
	 * @param cell
	 */
	void calc(PlayerData playerData, SoldierType soldierType, PlayerStrengthCell cell);
	
	default String getStrengthType(){
		return "";
	};
}
