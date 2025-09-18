package com.hawk.game.module.lianmengyqzz.battleroom;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

import com.hawk.game.module.lianmengyqzz.battleroom.cfg.YQZZResourceCfg;

public class YQZZResourceRefesh {

	private List<HawkTuple2<Integer, YQZZResourceCfg>> fuelBankRefreshTimes = new LinkedList<>();
	private YQZZBattleRoom parent;

	private boolean yugaoNotSend = true;

	private List<YQZZResourceCreater> createrList = new CopyOnWriteArrayList<>();
	private int refreshTurn; // 已经刷新的波数

	private YQZZResourceRefesh() {
	}

	public static YQZZResourceRefesh create(YQZZBattleRoom parent) {
		YQZZResourceRefesh result = new YQZZResourceRefesh();
		result.parent = parent;

		ConfigIterator<YQZZResourceCfg> cfgs = HawkConfigManager.getInstance().getConfigIterator(YQZZResourceCfg.class);
		for (YQZZResourceCfg cfg : cfgs) {
			for (Integer reftime : cfg.getRefreshTimeList()) {
				result.fuelBankRefreshTimes.add(HawkTuples.tuple(reftime, cfg));
			}
		}
		result.fuelBankRefreshTimes.sort(Comparator.comparingInt(t -> t.first));
		
		return result;
	}

	long lastTest;

	public void onTick() {
		createrList.forEach(YQZZResourceCreater::onTick);
		if (fuelBankRefreshTimes.isEmpty()) {
			return;
		}

		long curTimeMil = parent.getCurTimeMil();
		// // 预告
		// if (yugaoNotSend) {
		// long yugaoTime = parent.getCreateTime() + parent.getCfg().getResourceHerald() * 1000;
		// if (curTimeMil > yugaoTime) {
		// Integer next = fuelBankRefreshTimes.firstKey();
		//
		// int p1 = (next - parent.getCfg().getResourceHerald()) / 60;
		// ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).addParms(p1).setKey(NoticeCfgId.YQZZ_MONSTER_HERALD).build();
		// parent.addWorldBroadcastMsg(parames);
		// yugaoNotSend = false;
		// }
		// }

		if (curTimeMil < nextRefreshTime()) {
			return;
		}

		HawkTuple2<Integer, YQZZResourceCfg> mcfg = fuelBankRefreshTimes.remove(0);
		createrList.add(YQZZResourceCreater.create(this, mcfg.second));
		refreshTurn++;
		// if (!fuelBankRefreshTimes.isEmpty()) {
		// int p1 = (fuelBankRefreshTimes.firstKey() - next) / 60;
		// ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).addParms(p1).setKey(NoticeCfgId.YQZZ_MONSTER_REFRASH).build();
		// parent.addWorldBroadcastMsg(parames);
		// } else {
		//
		// ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.YQZZ_MONSTER_REFRASH_OVER).build();
		// parent.addWorldBroadcastMsg(parames);
		// }

	}

	public YQZZBattleRoom getParent() {
		return parent;
	}

	public long nextRefreshTime() {
		if (fuelBankRefreshTimes.isEmpty()) {
			return Long.MAX_VALUE;
		}
		int next = fuelBankRefreshTimes.get(0).first;
		long nextRefreshFuelBank = parent.getCreateTime() + next * 1000;
		return nextRefreshFuelBank;
	}

	// public int monsterCount(){
	// return getParent().worldResourceCount();
	// }

	public int getRefreshTurn() {
		return refreshTurn;
	}

}
