package com.hawk.game.crossactivity;

import java.util.Comparator;

public class OpenServerCompartor implements Comparator<CrossServerInfo> {

	@Override
	public int compare(CrossServerInfo o1, CrossServerInfo o2) {
		if (o1.getOpenServerTime() == o2.getOpenServerTime()) {
			return o1.getServerId().compareTo(o2.getServerId());
		} else {
			return o1.getOpenServerTime() - o2.getOpenServerTime() > 0 ? 1 : -1;
		}
	}
}
