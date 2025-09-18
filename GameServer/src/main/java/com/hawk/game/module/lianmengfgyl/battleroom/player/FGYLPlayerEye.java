package com.hawk.game.module.lianmengfgyl.battleroom.player;

public class FGYLPlayerEye {
	/**城点*/
	private int aoiObjId = 0;
	private final IFGYLPlayer parent;
	
	public FGYLPlayerEye(IFGYLPlayer parent){
		this.parent = parent;
	}
	
	public int getAoiObjId() {
		return aoiObjId;
	}

	public void setAoiObjId(int aoiObjId) {
		this.aoiObjId = aoiObjId;
	}

	public IFGYLPlayer getParent() {
		return parent;
	}
}
