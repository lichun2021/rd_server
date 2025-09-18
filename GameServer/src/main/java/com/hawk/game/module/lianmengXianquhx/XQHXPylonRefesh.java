package com.hawk.game.module.lianmengXianquhx;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

import com.hawk.game.module.lianmengXianquhx.cfg.XQHXPylonCfg;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.service.chat.ChatParames;

public class XQHXPylonRefesh {

	private List<HawkTuple2<Integer, XQHXPylonCfg>> fuelBankRefreshTimes = new LinkedList<>();
	private XQHXBattleRoom parent;

	private boolean yugaoNotSend = true;

	private List<XQHXPylonCreater> createrList = new CopyOnWriteArrayList<>();
	private int refreshTurn; // 已经刷新的波数

	private XQHXPylonRefesh() {
	}

	public static XQHXPylonRefesh create(XQHXBattleRoom parent) {
		XQHXPylonRefesh result = new XQHXPylonRefesh();
		result.parent = parent;

		ConfigIterator<XQHXPylonCfg> cfgs = HawkConfigManager.getInstance().getConfigIterator(XQHXPylonCfg.class);
		for (XQHXPylonCfg cfg : cfgs) {
			for (Integer reftime : cfg.getRefreshTimeList()) {
				result.fuelBankRefreshTimes.add(HawkTuples.tuple(reftime, cfg));
			}
		}
		result.fuelBankRefreshTimes.sort(Comparator.comparingInt(t -> t.first));
		return result;
	}

	long lastTest;

	public void onTick() {
		createrList.forEach(XQHXPylonCreater::onTick);
		if (fuelBankRefreshTimes.isEmpty()) {
			return;
		}

		long curTimeMil = parent.getCurTimeMil();
		// 预告
		if (yugaoNotSend) {
			long yugaoTime = parent.getCreateTime() + parent.getCfg().getMonsterHerald() * 1000;
			if (curTimeMil > yugaoTime) {
				Integer next = fuelBankRefreshTimes.get(0).first;

				int p1 = (next - parent.getCfg().getMonsterHerald()) / 60;
				ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).addParms(p1).setKey(NoticeCfgId.XQHX_PYLON_HERALD).build();
				parent.addWorldBroadcastMsg(parames);
				yugaoNotSend = false;
			}
		}

		if (curTimeMil < nextRefreshTime()) {
			return;
		}

		HawkTuple2<Integer, XQHXPylonCfg> refresh = fuelBankRefreshTimes.remove(0);
		int next = refresh.first;
		XQHXPylonCfg mcfg = refresh.second;
		createrList.add(XQHXPylonCreater.create(this, mcfg));
		refreshTurn++;
		if (!fuelBankRefreshTimes.isEmpty()) {
			int p1 = (fuelBankRefreshTimes.get(0).first - next) / 60;
			ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).addParms(p1).setKey(NoticeCfgId.XQHX_PYLON_REFRASH).build();
			parent.addWorldBroadcastMsg(parames);
		} else {

			ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(NoticeCfgId.XQHX_PYLON_REFRASH_OVER).build();
			parent.addWorldBroadcastMsg(parames);
		}

	}

	public XQHXBattleRoom getParent() {
		return parent;
	}

	public long nextRefreshTime() {
		if (fuelBankRefreshTimes.isEmpty()) {
			return 0;
		}
		int next = fuelBankRefreshTimes.get(0).first;
		long nextRefreshFuelBank = parent.getCreateTime() + next * 1000;
		return nextRefreshFuelBank;
	}

	public int getRefreshTurn() {
		return refreshTurn;
	}

}
