package com.hawk.activity.type.impl.snowball.cfg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

/**
 * 雪球大战阶段配置
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "activity/snowball/snowball_stage.xml")
public class SnowballStageCfg extends HawkConfigBase {
	
	/**
	 * 阶段id
	 */
	@Id
	private final int stageId;

	/**
	 * 解锁条件 x1,y1,10;x2,y2,10
	 */
	private final String unlock;

	/**
	 * 开放坐标 x1,y1;x2,y2
	 */
	private final String openPos;
	
	/**
	 * 到达该阶段发送的公告id
	 */
	private final int noticeId;
	
	/**
	 * 解锁条件
	 */
	private Map<Integer, Integer> unlockMap;
	
	/**
	 * 开放坐标
	 */
	private Set<Integer> openPosSet;
	
	/**
	 * 构造
	 */
	public SnowballStageCfg() {
		stageId = 0;
		unlock = "";
		openPos = "";
		noticeId = 0;
	}

	public int getStageId() {
		return stageId;
	}
	
	public Map<Integer, Integer> getUnlockMap() {
		return unlockMap;
	}

	public Set<Integer> getOpenPosSet() {
		return openPosSet;
	}

	public int getNoticeId() {
		return noticeId;
	}

	@Override
	protected boolean assemble() {
		
		Map<Integer, Integer> unlockMap = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(unlock)) {
			String[] split = unlock.split(";");
			for (int i = 0; i < split.length; i++) {
				String[] vlaue = split[i].split(",");
				int x = Integer.parseInt(vlaue[0]);
				int y = Integer.parseInt(vlaue[1]);
				int value = Integer.parseInt(vlaue[2]);
				int pointId = (y << 16) | x;
				unlockMap.put(pointId, value);
			}
		}
		this.unlockMap = unlockMap;
		
		Set<Integer> openPosSet = new HashSet<>();
		if (!HawkOSOperator.isEmptyString(openPos)) {
			String[] split = openPos.split(";");
			for (int i = 0; i < split.length; i++) {
				String[] pos = split[i].split(",");
				int x = Integer.parseInt(pos[0]);
				int y = Integer.parseInt(pos[1]);
				int pointId = (y << 16) | x;
				openPosSet.add(pointId);
			}
		}
		this.openPosSet = openPosSet;
		
		return true;
	}
}
