package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.google.common.base.Splitter;

/**
 * 年兽宝箱配置
 *
 */
@HawkConfigManager.XmlResource(file = "xml/world_christmas_war_box.xml")
public class WorldChristmasWarBoxCfg extends HawkConfigBase {

	@Id
	protected final int id;
	
	/**
	 * 奖励
	 */
	protected final String award;
	
	/**
	 * 是否需要公告
	 */
	protected final boolean needNotice;
	
	/**
	 * 奖励
	 */
	private List<Integer> awards;
	/**
	 * 占领需要的时间(秒)
	 */
	private final int time;
	
	public WorldChristmasWarBoxCfg() {
		id = 0;
		award = "";
		needNotice = false;
		this.time = 60;
	}

	public int getId() {
		return id;
	}

	public String getAward() {
		return award;
	}
	
	public List<Integer> getAwards() {
		return awards;
	}

	@Override
	protected boolean assemble() {
		
		List<Integer> awards = new ArrayList<Integer>();
		if (!HawkOSOperator.isEmptyString(award)) {
			for (String award : Splitter.on(";").split(award)) {
				awards.add(Integer.parseInt(award));
			}
		}
		this.awards = awards;
		
		return true;
	}

	public int getTime() {
		return time;
	}

	public boolean isNeedNotice() {
		return needNotice;
	}
}
