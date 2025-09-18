package com.hawk.game.module.lianmenxhjz.battleroom.player.according;

import com.hawk.game.module.lianmenxhjz.battleroom.worldpoint.XHJZBuildType;
import com.hawk.game.protocol.XHJZ.PBXHJZBuildTime;

public class XHJZBuildStayTime {
	private XHJZBuildType buildType;
	private long times;

	public PBXHJZBuildTime toPBObj() {
		PBXHJZBuildTime.Builder builder = PBXHJZBuildTime.newBuilder();
		builder.setBuildTimes(times);
		builder.setBuildType(buildType.intValue());
		return builder.build();
	}

	public XHJZBuildType getBuildType() {
		return buildType;
	}

	public void setBuildType(XHJZBuildType buildType) {
		this.buildType = buildType;
	}

	public long getTimes() {
		return times;
	}

	public void setTimes(long times) {
		this.times = times;
	}

}
