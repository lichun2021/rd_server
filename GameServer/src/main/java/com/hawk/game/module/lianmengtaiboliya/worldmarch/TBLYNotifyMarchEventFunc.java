package com.hawk.game.module.lianmengtaiboliya.worldmarch;

import java.util.function.Function;

import com.hawk.game.protocol.World.MarchEvent;

public class TBLYNotifyMarchEventFunc implements Function<Object, Object> {
	private ITBLYWorldMarch march;
	private MarchEvent eventType;

	@Override
	public Object apply(Object t) {
		march.pushMarchEvent(eventType);
		return null;
	}

	public ITBLYWorldMarch getMarch() {
		return march;
	}

	public void setMarch(ITBLYWorldMarch march) {
		this.march = march;
	}

	public MarchEvent getEventType() {
		return eventType;
	}

	public void setEventType(MarchEvent eventType) {
		this.eventType = eventType;
	}

}
