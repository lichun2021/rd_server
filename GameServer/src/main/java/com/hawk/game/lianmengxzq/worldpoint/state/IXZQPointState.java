package com.hawk.game.lianmengxzq.worldpoint.state;

import com.hawk.game.lianmengxzq.worldpoint.XZQWorldPoint;
import com.hawk.game.player.hero.SerializJsonStrAble;
import com.hawk.game.protocol.XZQ.PBXZQBuildStatus;

public abstract class IXZQPointState implements SerializJsonStrAble {
	
	private final XZQWorldPoint parent;

	public IXZQPointState(XZQWorldPoint parent) {
		this.parent = parent;
	}

	public abstract void init();
	
	public abstract void ontick();

	
	public abstract String getOccupyGuild();
	
	public long getControlStartTime(){
		return 0;
	}
	
	public long getControlEndTime(){
		return 0;
	}

		
	
	
	public final XZQWorldPoint getParent() {
		return parent;
	}

	public static IXZQPointState valueOf(XZQWorldPoint parent, int state) {
		PBXZQBuildStatus statues = PBXZQBuildStatus.valueOf(state);
		switch (statues) {
		case XZQ_BUILD_INIT:
		case XZQ_BUILD_SIGNUP:
		case XZQ_BUILD_WAIT_OPEN:
		case XZQ_BUILD_CONTROL:
			return new XZQStatePeace(parent);
		case XZQ_BUILD_BATTLE:
			return new XZQStateBattle(parent);
		default:
			break;
		}
		return null;
	}

	/** 处于交战期*/
	public boolean isPeace() {
		return false;
	}
}
