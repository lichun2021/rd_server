package com.hawk.game.gacha;

import java.util.ArrayList;
import java.util.List;
import org.hawk.os.HawkTime;
import com.hawk.game.config.GachaCfg;
import com.hawk.game.entity.PlayerGachaEntity;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.module.mechacore.cfg.MechaCoreConstCfg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Const.GachaType;

/**
 * 机甲核心模块一次
 * 
 * @author lating
 *
 */
public class GachaModuleOnceImpl implements GachaOprator {

	@Override
	public CheckAndConsumResult checkAndConsum(GachaCfg gachaCfg, PlayerGachaEntity gachaEntity, Player player) {
		if (isFreeTurn(gachaCfg, gachaEntity)) {
			return CheckAndConsumResult.create(true);
		}
		
		PlayerGachaEntity gachaEntityTen = player.getData().getGachaEntityByType(GachaType.MODULE_TEN);
		int daycnt = gachaEntityTen.getDayCount() * 10 + gachaEntity.getDayCount();
		int limit = MechaCoreConstCfg.getInstance().getDrawTimesLimit() + player.getPlayerMechaCore().getGachaAddProductCount();
		if (daycnt >= limit) {
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

	private boolean isFreeTurn(GachaCfg gachaCfg, PlayerGachaEntity gachaEntity) {
		return gachaEntity.getFreeTimesUsed() < gachaCfg.getFreeTimesLimit() && gachaEntity.getNextFree() < HawkTime.getMillisecond();
	}

	@Override
	public List<String> gacha(GachaCfg gachaCfg, PlayerGachaEntity gachaEntity, Player player) {
		// 抽1次的
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
			
			gachaEntity.setCount(gachaEntity.getCount() + 1);
			return gachaOnce(player, gachaCfg, gachaEntity, gachaPool);
		}
		
		if (gachaEntity.getFirstGachaUsed() == 0) {
			int gachaPool = gachaCfg.getFirstTimeGachaPool();
			gachaEntity.setFirstGachaUsed(1);
			return gachaOnce(player, gachaCfg, gachaEntity, gachaPool);
		}

		gachaEntity.setCount(gachaEntity.getCount() + 1);
		gachaEntity.setDayCount(gachaEntity.getDayCount() + 1);
		PlayerGachaEntity gachaEntityTen = player.getData().getGachaEntityByType(GachaType.MODULE_TEN);
		int daycnt = gachaEntityTen.getDayCount() * 10 + gachaEntity.getDayCount();
		int limit = MechaCoreConstCfg.getInstance().getDrawTimesLimit();
		if (daycnt > limit) {
			player.getPlayerMechaCore().decGachaAddProduct(1);
		}
		
		return gachaOnce(player, gachaCfg, gachaEntity, gachaCfg.getNormalGachaPoolA());
	}
	
	private List<String> gachaOnce(Player player, GachaCfg gachaCfg, PlayerGachaEntity gachaEntity, int gachaPool) {
		PlayerGachaEntity gachaTenEntity = player.getData().getGachaEntityByType(GachaType.MODULE_TEN);
		int finalCount = gachaTenEntity.getCount() * 10 + gachaEntity.getCount();
		int pseudoDropTimes = gachaCfg.getPseudoDropTimes(),
			pseudoDropGachaPool = gachaCfg.getPseudoDropGachaPool();
		if (finalCount / pseudoDropTimes > 0 && finalCount % pseudoDropTimes == 0) {
			gachaPool = pseudoDropGachaPool > 0 ? pseudoDropGachaPool : gachaCfg.getNormalGachaPoolA();
		}
		
		return gachaOnce(player.getCityLevel(), gachaPool);
	}

	private List<String> gachaOnce(final int homeLevel, int gachaPool) {
		List<String> result = new ArrayList<>(1);
		result.add(gacha(gachaPool, homeLevel));
		return result;
	}
	
	public boolean isGachaModule() {
		return true;
	}

	@Override
	public int getGachaCount() {
		return 1;
	}
	
	@Override
	public void setGachaCount(int num) {
		
	}
}