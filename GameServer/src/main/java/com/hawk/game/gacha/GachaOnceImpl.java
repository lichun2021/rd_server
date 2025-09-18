package com.hawk.game.gacha;

import java.util.ArrayList;
import java.util.List;

import org.hawk.os.HawkTime;

import com.hawk.game.config.GachaCfg;
import com.hawk.game.entity.PlayerGachaEntity;
import com.hawk.game.player.Player;

/**
 * 
 * @author luwentao
 *
 */
class GachaOnceImpl implements GachaOprator {

	@Override
	public CheckAndConsumResult checkAndConsum(GachaCfg gachaCfg, PlayerGachaEntity gachaEntity, Player player) {
		if (isFreeTurn(gachaCfg, gachaEntity)) {
			return CheckAndConsumResult.create(true);
		}

		return GachaOprator.super.checkAndConsum(gachaCfg, gachaEntity, player);
	}

	private boolean isFreeTurn(GachaCfg gachaCfg, PlayerGachaEntity gachaEntity) {
		return gachaEntity.getFreeTimesUsed() < gachaCfg.getFreeTimesLimit() && gachaEntity.getNextFree() < HawkTime.getMillisecond();
	}

	@Override
	public List<String> gacha(GachaCfg gachaCfg, PlayerGachaEntity gachaEntity, Player player) {
		// 抽1次的
		final int homeLevel = player.getCityLevel();

		if (isFreeTurn(gachaCfg, gachaEntity)) {// 免费
			gachaEntity.setFreeTimesUsed(gachaEntity.getFreeTimesUsed() + 1);
			gachaEntity.setNextFree(HawkTime.getMillisecond() + gachaCfg.getFreeTime() * 1000);
			int gachaPool = gachaCfg.getFreeGachaPool();
			if (gachaEntity.getFirstGachaUsed() == 0) {
				gachaPool = gachaCfg.getFirstTimeGachaPool();
				gachaEntity.setFirstGachaUsed(1);
			}
			if (gachaPool == 0) {
				gachaPool = gachaCfg.getNormalGachaPoolA();
			}
			return gachaOnce(homeLevel, gachaPool);
		}
		
		if (gachaEntity.getFirstGachaUsed() == 0) {
			int gachaPool = gachaCfg.getFirstTimeGachaPool();
			gachaEntity.setFirstGachaUsed(1);
			return gachaOnce(homeLevel, gachaPool);
		}

		gachaEntity.setCount(gachaEntity.getCount() + 1);
		gachaEntity.setDayCount(gachaEntity.getDayCount() + 1);
		
		// poolA1次或 pseudopool1次
		final boolean pseudoDrop = gachaEntity.getCount() % gachaCfg.getPseudoDropTimes() == 0;
		int extraPool = pseudoDrop ? gachaCfg.getPseudoDropGachaPool() : gachaCfg.getNormalGachaPoolA();
		if (extraPool == 0) {
			extraPool = gachaCfg.getNormalGachaPoolA();
		}
		return gachaOnce(homeLevel, extraPool);
	}

	private List<String> gachaOnce(final int homeLevel, int gachaPool) {
		List<String> result = new ArrayList<>(1);
		result.add(gacha(gachaPool, homeLevel));
		return result;
	}

	@Override
	public int getGachaCount() {
		return 1;
	}

	@Override
	public void setGachaCount(int num) {
		// TODO Auto-generated method stub
		
	}
}
