package com.hawk.game.lianmengstarwars.player;

public class SWPlayerEye {
	/**城点*/
	private int aoiObjId = 0;
	private final ISWPlayer parent;
	
	public SWPlayerEye(ISWPlayer parent){
		this.parent = parent;
	}
	
	public int getAoiObjId() {
		return aoiObjId;
	}

	public void setAoiObjId(int aoiObjId) {
		this.aoiObjId = aoiObjId;
	}

	public ISWPlayer getParent() {
		return parent;
	}
}
