package com.hawk.game.module.lianmengtaiboliya;

import java.util.LinkedList;
import java.util.List;

import org.hawk.os.HawkException;

import com.hawk.game.module.lianmengtaiboliya.cfg.TBLYFuelBankCfg;
import com.hawk.game.module.lianmengtaiboliya.worldpoint.TBLYFuelBank;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.service.chat.ChatParames;

public class TBLYFuBankRefesh {
	private List<Integer> fuelBankRefreshTimes = new LinkedList<>();
	private long nextRefreshFuelBank;
	private int fubankCount;
	private TBLYFuelBankCfg fubankcfg;
	private TBLYBattleRoom parent;

	private TBLYFuBankRefesh() {
	}

	public static TBLYFuBankRefesh create(TBLYBattleRoom parent, TBLYFuelBankCfg cfg) {
		TBLYFuBankRefesh result = new TBLYFuBankRefesh();
		result.parent = parent;
		result.fubankcfg = cfg;

		result.fuelBankRefreshTimes.addAll(cfg.getRefreshTimeList());
		result.nextRefreshFuelBank = parent.getCreateTime() + result.fuelBankRefreshTimes.remove(0) * 1000;

		return result;
	}

	public void onTick() {
		long curTimeMil = parent.getCurTimeMil();
		if (curTimeMil > nextRefreshFuelBank) {
			if (!fuelBankRefreshTimes.isEmpty()) {
				nextRefreshFuelBank = fuelBankRefreshTimes.remove(0) * 1000 + parent.getCreateTime();
			} else {
				nextRefreshFuelBank = Long.MAX_VALUE;
			}
			ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.TBLY_126).build();
			parent.addWorldBroadcastMsg(parames);
			fubankCount += fubankcfg.getRefreshCount();
		}

		if (fubankCount > 0) {
			fubankCount--;
			try {
				TBLYFuelBank res = TBLYFuelBank.create(parent, fubankcfg.getId());
				int[] xy = parent.randomFreePoint(popFuelBankPoint(), res.getPointType());
				if (xy != null) {
					res.setX(xy[0]);
					res.setY(xy[1]);
					parent.addViewPoint(res);
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}

	public int[] popFuelBankPoint() {
		return fubankcfg.randomBoinPoint();
	}
}
