package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 战区赛季积分排名
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/super_barrack_section.xml")
public class SuperWeaponSection extends HawkConfigBase {

	@Id
	protected final int id;

	protected final String score;

	private int scoreLow;
	
	public SuperWeaponSection() {
		id = 0;
		score = "";
	}

	public int getId() {
		return id;
	}

	public String getScore() {
		return score;
	}

	public int getScoreLow() {
		return scoreLow;
	}
	
	@Override
	protected boolean assemble() {
		if (score.contains("_")) {
			scoreLow = Integer.parseInt(score.split("_")[0]);
		} else {
			scoreLow = Integer.parseInt(score);
		}
		
		return true;
	}
}
