package com.hawk.game.module.lianmengfgyl.battleroom.worldpoint.refresh;

import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;

import com.hawk.game.module.lianmengfgyl.battleroom.FGYLBattleRoom;
import com.hawk.game.module.lianmengfgyl.battleroom.cfg.FGYLMonsterCfg;
import com.hawk.game.module.lianmengfgyl.battleroom.worldpoint.FGYLMonster;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.service.chat.ChatParames;

public class FGYLMonsterRefesh {

	private TreeMap<Integer, FGYLMonsterCfg> fuelBankRefreshTimes = new TreeMap<>();
	private FGYLBattleRoom parent;

	private boolean yugaoNotSend = true;

	private List<FGYLMonsterCreater> createrList = new CopyOnWriteArrayList<>();
	private int refreshTurn; // 已经刷新的波数

	private FGYLMonsterRefesh() {
	}

	public static FGYLMonsterRefesh create(FGYLBattleRoom parent) {
		FGYLMonsterRefesh result = new FGYLMonsterRefesh();
		result.parent = parent;

		ConfigIterator<FGYLMonsterCfg> cfgs = HawkConfigManager.getInstance().getConfigIterator(FGYLMonsterCfg.class);
		for (FGYLMonsterCfg cfg : cfgs) {
			for (Integer reftime : cfg.getRefreshTimeList()) {
				result.fuelBankRefreshTimes.put(reftime, cfg);
			}
		}

		return result;
	}

	long lastTest;

	public void onTick() {
		createrList.forEach(FGYLMonsterCreater::onTick);
		if (fuelBankRefreshTimes.isEmpty()) {
			return;
		}

		long curTimeMil = parent.getCurTimeMil();
		// 预告
		if (yugaoNotSend) {
			// long yugaoTime = parent.getCreateTime() + parent.getCfg().getMonsterHerald() * 1000;
			// if (curTimeMil > yugaoTime) {
			// Integer next = fuelBankRefreshTimes.firstKey();
			//
			// int p1 = (next - parent.getCfg().getMonsterHerald()) / 60;
			// ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).addParms(p1).setKey(NoticeCfgId.FGYL_MONSTER_HERALD).build();
			// parent.addWorldBroadcastMsg(parames);
			// yugaoNotSend = false;
			// }
		}

		if (curTimeMil < nextRefreshTime()) {
			return;
		}

		Integer next = fuelBankRefreshTimes.firstKey();
		FGYLMonsterCfg mcfg = fuelBankRefreshTimes.remove(next);
		createrList.add(FGYLMonsterCreater.create(this, mcfg));
		refreshTurn++;
		if (!fuelBankRefreshTimes.isEmpty()) {
			int p1 = (fuelBankRefreshTimes.firstKey() - next) / 60;
			// ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).addParms(p1).setKey(NoticeCfgId.FGYL_MONSTER_REFRASH).build();
			// parent.addWorldBroadcastMsg(parames);
		} else {

			// ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.FGYL_MONSTER_REFRASH_OVER).build();
			// parent.addWorldBroadcastMsg(parames);
		}

	}

	public FGYLBattleRoom getParent() {
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

	public int monsterCount() {
		return (int) getParent().getWorldPointService().getViewPointsList().stream().filter(p -> p instanceof FGYLMonster).count();
	}

	public int getRefreshTurn() {
		return refreshTurn;
	}

}
