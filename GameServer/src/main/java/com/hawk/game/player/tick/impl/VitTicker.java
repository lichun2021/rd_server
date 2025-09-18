package com.hawk.game.player.tick.impl;

import org.hawk.app.HawkApp;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import com.hawk.game.config.ConstProperty;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.player.Player;
import com.hawk.game.player.tick.PlayerTickLogic;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.util.GsConst;
import com.hawk.log.Action;
import com.hawk.log.Source;

public class VitTicker implements PlayerTickLogic {

	@Override
	public void onTick(Player player) {
		if (player.getLevel() <= 0) {
			HawkLog.errPrintln("player vitTick, level error, playerId: {}", player.getId());
			return;
		}
		
		long currentTime = HawkApp.getInstance().getCurrentTime();
		try {
			long lastTime = player.getEntity().getVitTime();
			int curVit = player.getVit();
			int maxVit = player.getMaxVit();
			if (lastTime <= 0 && curVit < maxVit) {
				lastTime = currentTime;
				player.getEntity().setVitTime(lastTime);
			}

			int effVal = player.getData().getEffVal(EffType.PLAYER_VIT_PER) + player.getData().getEffVal(EffType.EFF_523);
			effVal += player.getData().getEffVal(EffType.PLAYER_VIT_PER_CARD);
			effVal += player.getData().getEffVal(EffType.BACK_PRIVILEGE_VIT_PER);
			long edgeTime = ConstProperty.getInstance().getVitPointAddTime() * 1000L;
			edgeTime /= 1 + effVal * GsConst.EFF_PER;

			int count = (int) Math.floor((currentTime - lastTime) / edgeTime);
			if (lastTime <= 0 || count <= 0) {
				return;
			}

			lastTime += count * edgeTime;
			player.getEntity().setVitTime(lastTime);

			player.increaseVit(count, Action.VIT_AUTO_ADD, true);

			player.getPush().syncPlayerInfo();

			BehaviorLogger.log4Service(player, Source.SYSTEM_OPERATION, Action.VIT_AUTO_ADD,
					Params.valueOf("vit", player.getVit()));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

}
