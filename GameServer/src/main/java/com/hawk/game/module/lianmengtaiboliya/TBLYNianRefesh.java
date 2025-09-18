package com.hawk.game.module.lianmengtaiboliya;

import java.util.LinkedList;
import java.util.List;

import org.hawk.os.HawkException;

import com.hawk.game.module.lianmengtaiboliya.worldpoint.TBLYNian;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.service.chat.ChatParames;

public class TBLYNianRefesh {
	private TBLYBattleRoom parent;
	private long nextRefreshNian;
	private List<Integer> nianRefreshTimes = new LinkedList<>();
	private int nianCount;

	private TBLYNianRefesh() {
	}

	public static TBLYNianRefesh create(TBLYBattleRoom parent) {
		TBLYNianRefesh result = new TBLYNianRefesh();
		result.parent = parent;

		result.nianRefreshTimes.addAll(TBLYNian.getCfg().getRefreshTimeList());
		result.nextRefreshNian = parent.getCreateTime() + result.nianRefreshTimes.remove(0) * 1000;

		return result;
	}

	public void onTick() {
		long curTimeMil = parent.getCurTimeMil();
		// 刷机甲
		if (curTimeMil > nextRefreshNian) {
			if (!nianRefreshTimes.isEmpty()) {
				nextRefreshNian = nianRefreshTimes.remove(0) * 1000 + parent.getCreateTime();
			} else {
				nextRefreshNian = Long.MAX_VALUE;
			}

			ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(Const.NoticeCfgId.TBLY_NIAN_CHUXIAN).build();
			parent.addWorldBroadcastMsg(parames);
			nianCount += TBLYNian.getCfg().getRefreshCount();
		}

		if (nianCount > 0) {
			nianCount--;
			try {
				TBLYNian res = new TBLYNian(parent);
				int[] xy = parent.randomFreePoint(TBLYNian.getCfg().randomBoinPoint(), res.getPointType());
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

}
