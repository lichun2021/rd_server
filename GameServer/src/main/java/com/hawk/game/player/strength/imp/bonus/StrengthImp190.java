package com.hawk.game.player.strength.imp.bonus;

import java.util.List;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.config.GuildScienceLevelCfg;
import com.hawk.game.config.SoldierStrengthTypeCfg;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.entity.GuildScienceEntity;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.strength.imp.PlayerStrengthCell;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.service.GuildService;

/**
 * 联盟科技
 * @author Golden
 *
 */
@StrengthType(strengthType = 190)
public class StrengthImp190 implements StrengthBonusImp {
	
	@Override
	public void calc(Player player, SoldierType soldierType, PlayerStrengthCell cell) {
		PlayerData playerData = player.getData();
		SoldierStrengthTypeCfg typeCfg = getStrengthTypeCfg();
		
		int atkAttr = 0;
		int hpAttr = 0;
		
		List<GuildScienceEntity> scienceList = GuildService.getInstance().getGuildScienceList(getGuildId(playerData.getPlayerId()));
		for (GuildScienceEntity entity : scienceList) {
			GuildScienceLevelCfg cfg = HawkConfigManager.getInstance().getCombineConfig(GuildScienceLevelCfg.class, entity.getLevel(), entity.getScienceId());
			if (cfg == null) {
				continue;
			}
			atkAttr += cfg.getAtkAttr(soldierType.getNumber());
			hpAttr += cfg.getHpAttr(soldierType.getNumber());
		}
		
		cell.setAtk(Math.min(atkAttr, typeCfg.getAtkAttrMax()));
		cell.setHp(Math.min(hpAttr, typeCfg.getHpAttrMax()));
	}
	
	private String getGuildId(String playerId) {
		String guildId = GuildService.getInstance().getPlayerGuildId(playerId);
		GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(guildId);
		return guild == null ? "" : guild.getId();
	}
}