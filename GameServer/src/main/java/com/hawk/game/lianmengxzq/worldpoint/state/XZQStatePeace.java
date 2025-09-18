package com.hawk.game.lianmengxzq.worldpoint.state;

import com.hawk.game.lianmengxzq.worldpoint.XZQWorldPoint;
import com.hawk.game.lianmengxzq.worldpoint.data.XZQCommander;

public class XZQStatePeace extends IXZQPointState{

	public XZQStatePeace(XZQWorldPoint parent) {
		super(parent);
	}
	
	@Override
	public void init() {
		
	}

	@Override
	public void ontick() {
		//如果没有NPC并且无人控制，还给NPC
		boolean hasNpc = this.getParent().hasNpc();
		XZQCommander control = this.getParent().getCommander();
		if(!hasNpc && control == null){
			this.getParent().createNpc();
			this.getParent().updateWorldScene();
		}
	}
	
	@Override
	public String getOccupyGuild() {
		return null;
	}

	@Override
	public String serializ() {
		return null;
	}

	@Override
	public void mergeFrom(String serialiedStr) {
		
	}

	@Override
	public boolean isPeace(){
		return true;
	}

	
}
