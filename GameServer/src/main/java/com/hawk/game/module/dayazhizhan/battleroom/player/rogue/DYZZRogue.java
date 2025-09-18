package com.hawk.game.module.dayazhizhan.battleroom.player.rogue;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.module.dayazhizhan.battleroom.cfg.DYZZRogueBaseCfg;
import com.hawk.game.protocol.DYZZ.PBdYZZRogue;

public class DYZZRogue {
	private int index;
	private List<Integer> rogueIds = new ArrayList<>();
	private int selected;

	public DYZZRogueBaseCfg getCfg() {
		return HawkConfigManager.getInstance().getConfigByKey(DYZZRogueBaseCfg.class, selected);
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public List<Integer> getRogueIds() {
		return rogueIds;
	}

	public void setRogueIds(List<Integer> rogueIds) {
		this.rogueIds = rogueIds;
	}

	public int getSelected() {
		return selected;
	}

	public void setSelected(int selected) {
		this.selected = selected;
	}

	public PBdYZZRogue genPBObj() {
		return PBdYZZRogue.newBuilder().setIndex(index).setRogueselected(selected).addAllRogueIds(rogueIds).build();
	}

}
