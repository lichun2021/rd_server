package com.hawk.game.module.dayazhizhan.battleroom;

import java.util.LinkedList;
import java.util.List;

import com.hawk.game.module.dayazhizhan.battleroom.DYZZConst.DYZZState;
import com.hawk.game.module.dayazhizhan.battleroom.player.rogue.DYZZRogueType;

public class DYZZRogueRefesh {
	private DYZZBattleRoom parent;
	private List<Integer> rogueSelectTimesList = new LinkedList<>();
	private long lasttick;

	private DYZZRogueRefesh() {
	}

	public static DYZZRogueRefesh create(DYZZBattleRoom parent) {
		DYZZRogueRefesh result = new DYZZRogueRefesh();
		result.parent = parent;
		result.lasttick = parent.getCollectStartTime();
		result.rogueSelectTimesList.addAll(parent.getCfg().getRogueSelectTimesList());
		return result;
	}

	public void onTick() {
		if (rogueSelectTimesList.isEmpty() || parent.getCurTimeMil() < lasttick) {
			return;
		}
		lasttick = parent.getCurTimeMil() + 3000;

		if (parent.getCurTimeMil() > parent.getCreateTime() + rogueSelectTimesList.get(0) * 1000) {
			int param = rogueSelectTimesList.remove(0);
			parent.getPlayerList(DYZZState.GAMEING).forEach(p -> p.getRogueCollec().rogueOnce(DYZZRogueType.TIME, param));
		}

	}

}
