package com.hawk.game.gacha;

import java.util.ArrayList;
import java.util.List;

import com.hawk.game.config.GachaCfg;
import com.hawk.game.entity.PlayerGachaEntity;
import com.hawk.game.player.Player;

/**
 * 
 * @author luwentao
 *
 */
class GachaTenImpl implements GachaOprator {
	private int gachaCount = DEFAULT_BATCH;

	@Override
	public List<String> gacha(GachaCfg gachaCfg, PlayerGachaEntity gachaEntity, Player player) {
		List<String> rewards = new ArrayList<>(getGachaCount());
		for (int i = 0; i < getGachaCount() / 10; i++) {
			rewards.addAll(gacheTen(gachaCfg, gachaEntity, player));
		}
		
		for (int i = 0; i < getGachaCount() % 10; i++) {
			rewards.add(this.gacha(gachaCfg.getNormalGachaPoolA(), player.getCityLevel()));
		}
		
		return rewards;
	}

	private List<String> gacheTen(GachaCfg gachaCfg, PlayerGachaEntity gachaEntity, Player player) {
		gachaEntity.setCount(gachaEntity.getCount() + 1);
		gachaEntity.setDayCount(gachaEntity.getDayCount() + 1);

		final int homeLevel = player.getCityLevel();
		// 抽10次的
		List<String> rewards = new ArrayList<>(11);
		for (int i = 0; i < 9; i++) {
			rewards.add(this.gacha(gachaCfg.getNormalGachaPoolA(), homeLevel));
		}

		// poolB1次或 pseudopool1次
		final boolean pseudoDrop = gachaEntity.getCount() % gachaCfg.getPseudoDropTimes() == 0;
		int extraPool = pseudoDrop ? gachaCfg.getPseudoDropGachaPool() : gachaCfg.getNormalGachaPoolB();
		if (extraPool == 0) {
			extraPool = gachaCfg.getNormalGachaPoolA();
		}
		rewards.add(this.gacha(extraPool, homeLevel));
		return rewards;
	}

	@Override
	public int getGachaCount() {
		return gachaCount;
	}

	@Override
	public void setGachaCount(int num) {
		gachaCount = num;
	}

}
