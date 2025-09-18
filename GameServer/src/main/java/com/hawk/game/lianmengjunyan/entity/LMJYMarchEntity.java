package com.hawk.game.lianmengjunyan.entity;

import com.hawk.game.protocol.World.WorldMarchPB;
import com.hawk.game.protocol.World.WorldMarchRelation;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.march.IWorldMarch;

public class LMJYMarchEntity extends WorldMarch {

	public LMJYMarchEntity() {
		setPersistable(false);
	}

	@Override
	public boolean isVisibleOnPos(int x, int y, int viewRadius) {
		return true;
	}

	public WorldMarchPB.Builder toBuilder(WorldMarchPB.Builder builder, WorldMarchRelation relation) {
		 throw new UnsupportedOperationException("use march obj instanted");
	}

	@Override
	public IWorldMarch wrapUp() {
		return null;
	}
}
