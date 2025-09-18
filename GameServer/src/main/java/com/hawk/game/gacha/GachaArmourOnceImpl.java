package com.hawk.game.gacha;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hawk.os.HawkTime;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.game.config.ArmourConstCfg;
import com.hawk.game.config.GachaCfg;
import com.hawk.game.entity.PlayerGachaEntity;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Activity;

/**
 * 抽铠甲一次
 * @author golden
 *
 */
public class GachaArmourOnceImpl implements GachaOprator {

	@Override
	public CheckAndConsumResult checkAndConsum(GachaCfg gachaCfg, PlayerGachaEntity gachaEntity, Player player) {
		int armourCount = player.getData().getArmourEntityList().size();
		if (armourCount >= ArmourConstCfg.getInstance().getArmourMaxCount()) {
			return CheckAndConsumResult.create(false);
		}
		if (isFreeTurn(gachaCfg, gachaEntity)) {
			return CheckAndConsumResult.create(true);
		}
		if (gachaEntity.getDayCount() >= ArmourConstCfg.getInstance().getGachaLimit()) {
			return CheckAndConsumResult.create(false);
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
		Optional<ActivityBase> optionalActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.EQUIP_TECH_VALUE);
		boolean isActOpen = optionalActivity.isPresent() ? optionalActivity.get().isOpening(player.getId()) : false;
		if (isFreeTurn(gachaCfg, gachaEntity)) {// 免费
			gachaEntity.setFreeTimesUsed(gachaEntity.getFreeTimesUsed() + 1);
			gachaEntity.setNextFree(HawkTime.getMillisecond() + gachaCfg.getFreeTime() * 1000);
			int gachaPool = gachaCfg.getFreeGachaPool();
			if (gachaEntity.getFirstGachaUsed() == 0) {
				gachaPool = isActOpen ? gachaCfg.getFirstTimeGachaPoolActivity() : gachaCfg.getFirstTimeGachaPool();
				gachaEntity.setFirstGachaUsed(1);
			}
			if (gachaPool == 0) {
				gachaPool = isActOpen ? gachaCfg.getNormalGachaPoolAActivity() : gachaCfg.getNormalGachaPoolA();
			}
			
			gachaEntity.setCount(gachaEntity.getCount() + 1);
			
			// 免费次数不计入每日上限
			// gachaEntity.setDayCount(gachaEntity.getDayCount() + 1);
			
			return gachaOnce(homeLevel, gachaPool);
		}
		
		if (gachaEntity.getFirstGachaUsed() == 0) {
			int gachaPool = isActOpen ? gachaCfg.getFirstTimeGachaPoolActivity() : gachaCfg.getFirstTimeGachaPool();
			gachaEntity.setFirstGachaUsed(1);
			return gachaOnce(homeLevel, gachaPool);
		}

		gachaEntity.setCount(gachaEntity.getCount() + 1);
		gachaEntity.setDayCount(gachaEntity.getDayCount() + 1);
		// poolA1次或 pseudopool1次
		final boolean pseudoDrop = gachaEntity.getCount() % gachaCfg.getPseudoDropTimes() == 0;
		int extraPool;
		//活动
		if (isActOpen) {
			extraPool = pseudoDrop ? gachaCfg.getPseudoDropGachaPoolActivity() : gachaCfg.getNormalGachaPoolAActivity();
		}else{
			extraPool = pseudoDrop ? gachaCfg.getPseudoDropGachaPool() : gachaCfg.getNormalGachaPoolA();
		}
		if (extraPool == 0) {
			extraPool = isActOpen ? gachaCfg.getNormalGachaPoolAActivity() : gachaCfg.getNormalGachaPoolA();
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
	public boolean isGachArmour() {
		return true;
	}
	
	@Override
	public void setGachaCount(int num) {
		// TODO Auto-generated method stub
		
	}
}