package com.hawk.game.player.strength.imp.bonus;

import java.util.List;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

import com.hawk.activity.type.impl.monthcard.cfg.MonthCardActivityCfg;
import com.hawk.activity.type.impl.monthcard.entity.ActivityMonthCardEntity;
import com.hawk.activity.type.impl.monthcard.entity.MonthCardItem;
import com.hawk.game.config.SoldierStrengthTypeCfg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.strength.imp.PlayerStrengthCell;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.util.GameUtil;

/**
 * 月卡
 * @author Golden
 *
 */
@StrengthType(strengthType = 260)
public class StrengthImp260 implements StrengthBonusImp {
	
	@Override
	public void calc(Player player, SoldierType soldierType, PlayerStrengthCell cell) {
		PlayerData playerData = player.getData();
		SoldierStrengthTypeCfg typeCfg = getStrengthTypeCfg();
		
		ActivityMonthCardEntity entity = GameUtil.getMonthCardEntity(playerData.getPlayerId());
		if (entity == null) {
			return;
		}
		
		int atkAttr = 0;
		int hpAttr = 0;
		
		long millisecond = HawkTime.getMillisecond();
		List<MonthCardItem> cardList = entity.getEfficientCardList();
		for (MonthCardItem card : cardList) {
			MonthCardActivityCfg cardCfg = HawkConfigManager.getInstance().getConfigByKey(MonthCardActivityCfg.class, card.getCardId());
			if (cardCfg == null) {
				continue;
			}
			long endTime = cardCfg.getValidEndTime(card.getPucharseTime());
			if (millisecond < endTime) {
				atkAttr += cardCfg.getAtkAttr(soldierType.getNumber());
				hpAttr += cardCfg.getHpAttr(soldierType.getNumber());
			}
		}
		
		cell.setAtk(Math.min(atkAttr, typeCfg.getAtkAttrMax()));
		cell.setHp(Math.min(hpAttr, typeCfg.getHpAttrMax()));
	}
}