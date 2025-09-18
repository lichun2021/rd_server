package com.hawk.game.gacha;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.game.config.ArmourConstCfg;
import com.hawk.game.config.GachaCfg;
import com.hawk.game.entity.PlayerGachaEntity;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Const.GachaType;

/**
 * 抽铠甲十次
 * @author golden
 *
 */
public class GachaArmourTenImpl implements GachaOprator {
	private int gachaCount = DEFAULT_BATCH;

	@Override
	public CheckAndConsumResult checkAndConsum(GachaCfg gachaCfg, PlayerGachaEntity gachaEntity, Player player) {
		int armourCount = player.getData().getArmourEntityList().size();
		if (armourCount >= ArmourConstCfg.getInstance().getArmourMaxCount()) {
			return CheckAndConsumResult.create(false);
		}
		GachaType gachaType = GachaType.ARMOUR_ONE;
		PlayerGachaEntity gachaEntityOne = player.getData().getGachaEntityByType(gachaType);
		int daycnt = gachaEntity.getDayCount() * 10 + gachaEntityOne.getDayCount();
		if (daycnt + gachaCount > ArmourConstCfg.getInstance().getGachaLimit()) {
			return CheckAndConsumResult.create(false);
		}
		return GachaOprator.super.checkAndConsum(gachaCfg, gachaEntity, player);
	}
	
	@Override
	public List<String> gacha(GachaCfg gachaCfg, PlayerGachaEntity gachaEntity, Player player) {
		List<String> rewards = new ArrayList<>(getGachaCount());
		for (int i = 0; i < getGachaCount() / 10; i++) {
			rewards.addAll(gacheTen(gachaCfg, gachaEntity, player));
		}
		
		int onCnt = getGachaCount() % 10;
		if (onCnt > 0) {
			GachaType gachaType = GachaType.ARMOUR_ONE;
			PlayerGachaEntity gachaEntityOne = player.getData().getGachaEntityByType(gachaType);
			gachaEntityOne.setCount(gachaEntityOne.getCount() + onCnt);
			gachaEntityOne.setDayCount(gachaEntityOne.getDayCount() + onCnt);
			for (int i = 0; i < onCnt; i++) {
				rewards.add(this.gacha(gachaCfg.getNormalGachaPoolA(), player.getCityLevel()));
			}
		}
		
		return rewards;
	}

	private List<String> gacheTen(GachaCfg gachaCfg, PlayerGachaEntity gachaEntity, Player player) {
		gachaEntity.setCount(gachaEntity.getCount() + 1);
		gachaEntity.setDayCount(gachaEntity.getDayCount() + 1);

		final int homeLevel = player.getCityLevel();
		Optional<ActivityBase> optionalActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.EQUIP_TECH_VALUE);
		boolean isActOpen = optionalActivity.isPresent() ? optionalActivity.get().isOpening(player.getId()) : false;
		int poolId = isActOpen ? gachaCfg.getNormalGachaPoolAActivity() : gachaCfg.getNormalGachaPoolA();
		// 抽10次的
		List<String> rewards = new ArrayList<>(11);
		for (int i = 0; i < 9; i++) {
			rewards.add(this.gacha(poolId, homeLevel));
		}

		// poolB1次或 pseudopool1次
		final boolean pseudoDrop = gachaEntity.getCount() % gachaCfg.getPseudoDropTimes() == 0;
		int extraPool;
		if (isActOpen) {
			extraPool = pseudoDrop ? gachaCfg.getPseudoDropGachaPoolActivity() : gachaCfg.getNormalGachaPoolBActivity();
		}else{
			extraPool = pseudoDrop ? gachaCfg.getPseudoDropGachaPool() : gachaCfg.getNormalGachaPoolB();
		}
		if (extraPool == 0) {
			extraPool = isActOpen ? gachaCfg.getNormalGachaPoolAActivity() : gachaCfg.getNormalGachaPoolA();
		}
		rewards.add(this.gacha(extraPool, homeLevel));
		return rewards;
	}

	@Override
	public boolean isGachArmour() {
		return true;
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