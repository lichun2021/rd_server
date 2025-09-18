package com.hawk.game.module.dayazhizhan.battleroom;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.DYZZFuelBankBig;
import com.hawk.game.util.GameUtil;

public class DYZZFuBankBigRefesh {
	private DYZZBattleRoom parent;
	private List<Integer> collectPoints = new LinkedList<>();
	private long lasttick;

	private DYZZFuBankBigRefesh() {
	}

	public static DYZZFuBankBigRefesh create(DYZZBattleRoom parent) {
		DYZZFuBankBigRefesh result = new DYZZFuBankBigRefesh();
		result.parent = parent;
		result.lasttick = parent.getCollectStartTime();
		return result;
	}

	public void onTick() {
		if (parent.getCurTimeMil() < lasttick) {
			return;
		}
		lasttick = parent.getCurTimeMil() + 3000;

		long count = parent.getViewPoints().stream().filter(p -> p.getClass() == DYZZFuelBankBig.class).count();
		if (collectPoints.size() > 0 && count < DYZZFuelBankBig.getCfg().getMinNum()) {
			Collections.shuffle(collectPoints);
			int[] xy = GameUtil.splitXAndY(collectPoints.remove(0));

			DYZZFuelBankBig icd = DYZZFuelBankBig.create(parent);
			icd.setX(xy[0]);
			icd.setY(xy[1]);
			parent.addViewPoint(icd);
		}
	}

	public void onFubankRemove(DYZZFuelBankBig fuSmall) {
		collectPoints.add(fuSmall.getPointId());
	}
}
