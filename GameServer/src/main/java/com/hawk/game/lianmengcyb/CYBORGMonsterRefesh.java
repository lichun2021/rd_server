package com.hawk.game.lianmengcyb;

import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;

import com.hawk.game.config.CYBORGMonsterCfg;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.service.chat.ChatParames;

public class CYBORGMonsterRefesh {

	private TreeMap<Integer, CYBORGMonsterCfg> fuelBankRefreshTimes = new TreeMap<>();
	private CYBORGBattleRoom parent;

	private boolean yugaoNotSend = true;

	private List<CYBORGMonsterCreater> createrList = new CopyOnWriteArrayList<>();
	private int refreshTurn; // 已经刷新的波数
	
	private CYBORGMonsterRefesh() {
	}

	public static CYBORGMonsterRefesh create(CYBORGBattleRoom parent) {
		CYBORGMonsterRefesh result = new CYBORGMonsterRefesh();
		result.parent = parent;

		ConfigIterator<CYBORGMonsterCfg> cfgs = HawkConfigManager.getInstance().getConfigIterator(CYBORGMonsterCfg.class);
		for (CYBORGMonsterCfg cfg : cfgs) {
			for (Integer reftime : cfg.getRefreshTimeList()) {
				result.fuelBankRefreshTimes.put(reftime, cfg);
			}
		}

		return result;
	}
	
	long lastTest ;

	public void onTick() {
		createrList.forEach(CYBORGMonsterCreater::onTick);
		if (fuelBankRefreshTimes.isEmpty()) {
			return;
		}

		long curTimeMil = parent.getCurTimeMil();
		// 预告
		if (yugaoNotSend) {
			long yugaoTime = parent.getCreateTime() + parent.getCfg().getMonsterHerald() * 1000;
			if (curTimeMil > yugaoTime) {
				Integer next = fuelBankRefreshTimes.firstKey();

				int p1 = (next - parent.getCfg().getMonsterHerald()) / 60;
				ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).addParms(p1).setKey(NoticeCfgId.CYBORG_MONSTER_HERALD).build();
				parent.addWorldBroadcastMsg(parames);
				yugaoNotSend = false;
			}
		}

		if (curTimeMil < nextRefreshTime()) {
			return;
		}

		Integer next = fuelBankRefreshTimes.firstKey();
		CYBORGMonsterCfg mcfg = fuelBankRefreshTimes.remove(next);
		createrList.add(CYBORGMonsterCreater.create(this, mcfg));
		refreshTurn ++;
		if (!fuelBankRefreshTimes.isEmpty()) {
			int p1 = (fuelBankRefreshTimes.firstKey() - next) / 60;
			ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).addParms(p1).setKey(NoticeCfgId.CYBORG_MONSTER_REFRASH).build();
			parent.addWorldBroadcastMsg(parames);
		} else {

			ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.CYBORG_MONSTER_REFRASH_OVER).build();
			parent.addWorldBroadcastMsg(parames);
		}

	}

	public CYBORGBattleRoom getParent() {
		return parent;
	}

	public long nextRefreshTime() {
		if (fuelBankRefreshTimes.isEmpty()) {
			return 0;
		}
		int next = fuelBankRefreshTimes.firstKey();
		long nextRefreshFuelBank = parent.getCreateTime() + next * 1000;
		return nextRefreshFuelBank;
	}
	
	public int monsterCount(){
		return getParent().worldMonsterCount();
	}
	
	public int getRefreshTurn() {
		return refreshTurn;
	}
	
	
}
