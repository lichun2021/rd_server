package com.hawk.game.gacha;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.game.config.ArmourConstCfg;
import com.hawk.game.config.GachaCfg;
import com.hawk.game.entity.PlayerGachaEntity;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Const.GachaType;
import com.hawk.game.protocol.Item.HPSyncGachaInfoResp;
import com.hawk.game.util.BuilderUtil;

/**
 * 铠甲宝箱
 * @author golden
 *
 */
public class GachaArmourBoxImpl implements GachaOprator {
	private int gachaCount = 1;
	@Override
	public CheckAndConsumResult checkAndConsum(GachaCfg gachaCfg, PlayerGachaEntity gachaEntity, Player player) {
		
		int armourOneCount = 0;
		for (PlayerGachaEntity entity : player.getData().getPlayerGachaEntities()) {
			if (entity.getGachaType() != GachaType.ARMOUR_ONE.getNumber()) {
				continue;
			}
			armourOneCount += entity.getCount();
		}
		
		int armourTenCount = 0;
		for (PlayerGachaEntity entity : player.getData().getPlayerGachaEntities()) {
			if (entity.getGachaType() != GachaType.ARMOUR_TEN.getNumber()) {
				continue;
			}
			armourTenCount += entity.getCount();
		}
		
		int allArmourCount = armourOneCount + (armourTenCount * 10);
		int freeTimes = allArmourCount / ArmourConstCfg.getInstance().getGachaTimesBox();
		if (gachaEntity.getCount() + gachaCount > freeTimes) {
			HPSyncGachaInfoResp.Builder resp = BuilderUtil.gachaInfoPB(player.getData());
			player.sendProtocol(HawkProtocol.valueOf(HP.code.GACHA_SYNC_S, resp));
			return CheckAndConsumResult.create(false);
		}

		return CheckAndConsumResult.create(true);
	}

	@Override
	public List<String> gacha(GachaCfg gachaCfg, PlayerGachaEntity gachaEntity, Player player) {
		// 抽1次的
		final int homeLevel = player.getCityLevel();

		gachaEntity.setCount(gachaEntity.getCount() + gachaCount);
		gachaEntity.setDayCount(gachaEntity.getDayCount() + gachaCount);
		
		List<String> result = new ArrayList<>(gachaCount);
		Optional<ActivityBase> optionalActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.EQUIP_TECH_VALUE);
		boolean isActOpen = optionalActivity.isPresent() ? optionalActivity.get().isOpening(player.getId()) : false;
		int poolId = isActOpen ? gachaCfg.getNormalGachaPoolAActivity() : gachaCfg.getNormalGachaPoolA();
		for (int i = 0; i < gachaCount; i++) {
			result.add(gacha(poolId, homeLevel));
		}
		
		return result;
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