package com.hawk.game.module.dayazhizhan.battleroom;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.DYZZFuelBankSmall;
import com.hawk.game.util.GameUtil;

public class DYZZFuBankSmallRefesh {
	private DYZZBattleRoom parent;
	private List<Integer> collectPoints = new LinkedList<>();
	private long lasttick;
	private boolean orderCreate;

	private DYZZFuBankSmallRefesh() {
	}

	public static DYZZFuBankSmallRefesh create(DYZZBattleRoom parent) {
		DYZZFuBankSmallRefesh result = new DYZZFuBankSmallRefesh();
		result.parent = parent;
		result.lasttick = parent.getCollectStartTime();
		return result;
	}

	public void onTick() {
		if (parent.getCurTimeMil() < lasttick) {
			return;
		}
		lasttick = parent.getCurTimeMil() + 3000;

		long count = parent.getViewPoints().stream().filter(p -> p.getClass() == DYZZFuelBankSmall.class).count();
		if (collectPoints.size() > 0 && count < DYZZFuelBankSmall.getCfg().getMinNum()) {
			Collections.shuffle(collectPoints);
			int[] xy = GameUtil.splitXAndY(collectPoints.remove(0));

			DYZZFuelBankSmall icd = DYZZFuelBankSmall.create(parent);
			icd.setX(xy[0]);
			icd.setY(xy[1]);
			parent.addViewPoint(icd);
		}
	}

	public void onFubankRemove(DYZZFuelBankSmall fuSmall) {
		if (!isOrderCreate()) {
			collectPoints.add(fuSmall.getPointId());
		}
	}

	public boolean isOrderCreate() {
		return orderCreate;
	}

	public void setOrderCreate(boolean orderCreate) {
		this.orderCreate = orderCreate;
	}

}
