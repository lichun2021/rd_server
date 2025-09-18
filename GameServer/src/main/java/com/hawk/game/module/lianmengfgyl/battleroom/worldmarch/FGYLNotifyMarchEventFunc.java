package com.hawk.game.module.lianmengfgyl.battleroom.worldmarch;

import java.util.function.Function;

import com.hawk.game.protocol.World.MarchEvent;

public class FGYLNotifyMarchEventFunc implements Function<Object, Object> {
	private IFGYLWorldMarch march;
	private MarchEvent eventType;

	@Override
	public Object apply(Object t) {
		march.pushMarchEvent(eventType);
		return null;
	}

	public IFGYLWorldMarch getMarch() {
		return march;
	}

	public void setMarch(IFGYLWorldMarch march) {
		this.march = march;
	}

	public MarchEvent getEventType() {
		return eventType;
	}

	public void setEventType(MarchEvent eventType) {
		this.eventType = eventType;
	}

}
