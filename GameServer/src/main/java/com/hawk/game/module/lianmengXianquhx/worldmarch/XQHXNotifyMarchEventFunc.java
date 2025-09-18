package com.hawk.game.module.lianmengXianquhx.worldmarch;

import java.util.function.Function;

import com.hawk.game.protocol.World.MarchEvent;

public class XQHXNotifyMarchEventFunc implements Function<Object, Object> {
	private IXQHXWorldMarch march;
	private MarchEvent eventType;

	@Override
	public Object apply(Object t) {
		march.pushMarchEvent(eventType);
		return null;
	}

	public IXQHXWorldMarch getMarch() {
		return march;
	}

	public void setMarch(IXQHXWorldMarch march) {
		this.march = march;
	}

	public MarchEvent getEventType() {
		return eventType;
	}

	public void setEventType(MarchEvent eventType) {
		this.eventType = eventType;
	}

}
