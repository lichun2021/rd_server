package com.hawk.game.player.tick.impl;

import java.util.List;

import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.player.Player;
import com.hawk.game.player.tick.PlayerTickLogic;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.util.GsConst;
import com.hawk.log.Action;

public class OilConsumeTicker implements PlayerTickLogic {

	@Override
	public void onTick(Player player) {
		long currentTime = HawkApp.getInstance().getCurrentTime();
		// 军队消耗石油
		if (currentTime - player.getOilConsumeTime() > GsConst.MINUTE_MILLI_SECONDS) {
			try {
				List<ArmyEntity> armyEntities = player.getData().getArmyEntities();
				double consumePerHour = 0;
				for (ArmyEntity army : armyEntities) {
					int count = army.getFree() + army.getMarch();
					BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, army.getArmyId());
					consumePerHour += cfg.getConsume() * count;
				}

				int troopOilConsumeBuffPer = player.getEffect().getEffVal(EffType.CITY_TROOP_COST_ADD_PER) - player.getEffect().getEffVal(EffType.CITY_TROOP_COST_REDUCE);
				long timeGap = currentTime - player.getOilConsumeTime();
				timeGap = Math.max(0, timeGap);
				timeGap = Math.min(timeGap, HawkTime.DAY_MILLI_SECONDS * 30);  // 防止上一次记录的oilConsumeTime距离现在太远导致消耗超出上限出异常，这里要给时间设定一个上限，就一个月吧
				long consumeTotal = (long) (consumePerHour * timeGap / GsConst.HOUR_MILLI_SECONDS * (1 + troopOilConsumeBuffPer * GsConst.EFF_PER));
				consumeTotal = Math.max(consumeTotal, 0);

				long unsafeOil = player.getUnsafeOil();
				long consumeOil = Math.min(consumeTotal, unsafeOil);
				if (consumeOil <= 0) {
					return;
				}

				consumeOil = Math.min(consumeOil, Integer.MAX_VALUE - 1);
				ConsumeItems consume = ConsumeItems.valueOf();
				consume.addConsumeInfo(PlayerAttr.OIL_UNSAFE, consumeOil);
				if (consume.checkConsume(player)) {
					consume.consumeAndPush(player, Action.OIL_CONSUME_BY_ARMY);
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			player.getEntity().setOilConsumeTime(currentTime);
		}
	}

}
