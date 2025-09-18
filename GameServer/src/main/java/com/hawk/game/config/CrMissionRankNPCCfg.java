package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/***
 * 英雄试炼npc表
 * @author yang.rao
 *
 */
@HawkConfigManager.XmlResource(file = "xml/cr_rank_npc.xml")
public class CrMissionRankNPCCfg extends HawkConfigBase {
	
	@Id
	private final int id;
	
	private final String name;
	
	private final String icon;
	
	private final int score;
	
	public CrMissionRankNPCCfg(){
		id = 0;
		name = "";
		icon = "";
		score = 0;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getIcon() {
		return icon;
	}

	public int getScore() {
		return score;
	}

	@Override
	protected boolean checkValid() {
		if(score < 0){
			throw new RuntimeException(String.format("cr_rank_npc.xml配置积分出错，id:%d, score:%d", id, score));
		}
		return super.checkValid();
	}
	
	
}
