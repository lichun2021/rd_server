package com.hawk.game.module.lianmenxhjz.battleroom.player;

public class XHJZPlayerEye {
	/**城点*/
	private int aoiObjId = 0;
	private final IXHJZPlayer parent;
	
	public XHJZPlayerEye(IXHJZPlayer parent){
		this.parent = parent;
	}
	
	public int getAoiObjId() {
		return aoiObjId;
	}

	public void setAoiObjId(int aoiObjId) {
		this.aoiObjId = aoiObjId;
	}

	public IXHJZPlayer getParent() {
		return parent;
	}
}
