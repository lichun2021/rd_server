package com.hawk.game.gacha;

import java.util.ArrayList;
import java.util.List;
import com.hawk.game.config.GachaCfg;
import com.hawk.game.entity.PlayerGachaEntity;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.module.mechacore.cfg.MechaCoreConstCfg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Const.GachaType;

/**
 * 机甲核心模块十次
 * 
 * @author lating
 *
 */
public class GachaModuleTenImpl implements GachaOprator {
	private int gachaCount = DEFAULT_BATCH;

	@Override
	public CheckAndConsumResult checkAndConsum(GachaCfg gachaCfg, PlayerGachaEntity gachaEntity, Player player) {
		GachaType gachaType = GachaType.MODULE_ONE;
		PlayerGachaEntity gachaEntityOne = player.getData().getGachaEntityByType(gachaType);
		int daycnt = gachaEntity.getDayCount() * 10 + gachaEntityOne.getDayCount();
		int limit = MechaCoreConstCfg.getInstance().getDrawTimesLimit() + player.getPlayerMechaCore().getGachaAddProductCount();
		if (daycnt + gachaCount > limit) {
			return CheckAndConsumResult.create(false);
		}
		
		ItemInfo ticket = ItemInfo.valueOf(gachaCfg.getTicketExpend());
		int ticketCount = player.getData().getItemNumByItemId(ticket.getItemId());
		int notHave = getGachaCount() - ticketCount;
		//不能用金条抽取
		if (notHave > 0) {
			player.sendError(HP.code.GACHA_C_VALUE, Status.Error.ITEM_NOT_ENOUGH_VALUE, 0);
			return CheckAndConsumResult.create(false);
		}
		
		return GachaOprator.super.checkAndConsum(gachaCfg, gachaEntity, player);
	}
	
	@Override
	public List<String> gacha(GachaCfg gachaCfg, PlayerGachaEntity gachaEntity, Player player) {
		List<String> rewards = new ArrayList<>(getGachaCount());
		PlayerGachaEntity gachaOneEntity = player.getData().getGachaEntityByType(GachaType.MODULE_ONE);
		int oldDaycnt = gachaEntity.getDayCount() * 10 + gachaOneEntity.getDayCount();
		for (int i = 0; i < getGachaCount() / 10; i++) {
			rewards.addAll(gacheTen(gachaCfg, gachaEntity, gachaOneEntity, player));
		}
		
		int oldCount = gachaEntity.getCount() * 10;
		int onCnt = getGachaCount() % 10;
		if (onCnt > 0) {
			oldCount += gachaOneEntity.getCount();
			gachaOneEntity.setCount(gachaOneEntity.getCount() + onCnt);
			gachaOneEntity.setDayCount(gachaOneEntity.getDayCount() + onCnt);
			int pseudoDropTimes = gachaCfg.getPseudoDropTimes(), pseudoDropGachaPool = gachaCfg.getPseudoDropGachaPool();
			for (int i = 0; i < onCnt; i++) {
				oldCount++;
				int poolId = gachaCfg.getNormalGachaPoolA();
				if (oldCount / pseudoDropTimes > 0 && oldCount % pseudoDropTimes == 0) {
					poolId = pseudoDropGachaPool > 0 ? pseudoDropGachaPool : poolId;
				}
				rewards.add(this.gacha(poolId, player.getCityLevel()));
			}
		}
		
		int daycnt = gachaEntity.getDayCount() * 10 + gachaOneEntity.getDayCount();
		int limit = MechaCoreConstCfg.getInstance().getDrawTimesLimit();
		if (daycnt > limit) {
			int decCount = oldDaycnt < limit ? (daycnt - limit) : (daycnt - oldDaycnt);
			player.getPlayerMechaCore().decGachaAddProduct(decCount);
		}
		
		return rewards;
	}

	private List<String> gacheTen(GachaCfg gachaCfg, PlayerGachaEntity gachaEntity, PlayerGachaEntity gachaOneEntity, Player player) {
		int oldCount = gachaEntity.getCount() * 10 + gachaOneEntity.getCount();
		gachaEntity.setCount(gachaEntity.getCount() + 1);
		gachaEntity.setDayCount(gachaEntity.getDayCount() + 1);

		final int homeLevel = player.getCityLevel(), pseudoDropTimes = gachaCfg.getPseudoDropTimes(), pseudoDropGachaPool = gachaCfg.getPseudoDropGachaPool();
		List<String> rewards = new ArrayList<>(11);
		for (int i = 0; i < 9; i++) {
			oldCount++;
			int poolId = gachaCfg.getNormalGachaPoolA();
			if (oldCount / pseudoDropTimes > 0 && oldCount % pseudoDropTimes == 0) {
				poolId = pseudoDropGachaPool > 0 ? pseudoDropGachaPool : poolId;
			}
			rewards.add(this.gacha(poolId, homeLevel));
		}

		oldCount++;
		int poolId = gachaCfg.getNormalGachaPoolB();
		if (oldCount / pseudoDropTimes > 0 && oldCount % pseudoDropTimes == 0) {
			poolId = pseudoDropGachaPool;
		}
		if (poolId <= 0) {
			poolId = gachaCfg.getNormalGachaPoolA();
		}
		rewards.add(this.gacha(poolId, homeLevel));
		return rewards;
	}
	
	public boolean isGachaModule() {
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