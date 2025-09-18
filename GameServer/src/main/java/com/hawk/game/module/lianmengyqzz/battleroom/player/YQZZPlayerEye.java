package com.hawk.game.module.lianmengyqzz.battleroom.player;

public class YQZZPlayerEye {
	/**城点*/
	private int aoiObjId = 0;
	private final IYQZZPlayer parent;
	
	public YQZZPlayerEye(IYQZZPlayer parent){
		this.parent = parent;
	}
	
	public int getAoiObjId() {
		return aoiObjId;
	}

	public void setAoiObjId(int aoiObjId) {
		this.aoiObjId = aoiObjId;
	}

	public IYQZZPlayer getParent() {
		return parent;
	}
}
