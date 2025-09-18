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
@HawkConfigManager.XmlResource(file = "xml/world_nian_box.xml")
public class WorldNianBoxCfg extends HawkConfigBase {

	@Id
	protected final int id;
	
	/**
	 * 奖励
	 */
	protected final String award;
	
	/**
	 * 是否需要公告
	 */
	protected final int needNotice;
	
	/**
	 * 奖励
	 */
	private List<Integer> awards;
	
	public WorldNianBoxCfg() {
		id = 0;
		award = "";
		needNotice = 0;
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

	public boolean needNotice() {
		return needNotice == 1;
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
}
