package com.hawk.game.module.lianmengyqzz.battleroom.player.according;

import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZBuildType;
import com.hawk.game.protocol.YQZZ.PBYQZZBuildTime;

public class YQZZBuildStayTime {
	private YQZZBuildType buildType;
	private long times;

	public PBYQZZBuildTime toPBObj() {
		PBYQZZBuildTime.Builder builder = PBYQZZBuildTime.newBuilder();
		builder.setBuildTimes(times);
		builder.setBuildType(buildType.intValue());
		return builder.build();
	}

	public YQZZBuildType getBuildType() {
		return buildType;
	}

	public void setBuildType(YQZZBuildType buildType) {
		this.buildType = buildType;
	}

	public long getTimes() {
		return times;
	}

	public void setTimes(long times) {
		this.times = times;
	}

}
