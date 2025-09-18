package com.hawk.game.player.strength.imp.bonus;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.config.AllianceOfficialCfg;
import com.hawk.game.config.SoldierStrengthTypeCfg;
import com.hawk.game.entity.GuildMemberObject;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.strength.imp.PlayerStrengthCell;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.GsConst.GuildOffice;

/**
 * 联盟官员
 * @author Golden
 *
 */
@StrengthType(strengthType = 200)
public class StrengthImp200 implements StrengthBonusImp {
	
	@Override
	public void calc(Player player, SoldierType soldierType, PlayerStrengthCell cell) {
		PlayerData playerData = player.getData();
		SoldierStrengthTypeCfg typeCfg = getStrengthTypeCfg();
		GuildMemberObject member = GuildService.getInstance().getGuildMemberObject(playerData.getPlayerId());
		if (member != null && member.getOfficeId() != GuildOffice.NONE.value()) {
			AllianceOfficialCfg officialCfg = HawkConfigManager.getInstance().getConfigByKey(AllianceOfficialCfg.class, member.getOfficeId());
			cell.setAtk(Math.min(officialCfg.getAtkAttr(soldierType.getNumber()), typeCfg.getAtkAttrMax()));
			cell.setHp(Math.min(officialCfg.getHpAttr(soldierType.getNumber()), typeCfg.getHpAttrMax()));
		}
	}
}