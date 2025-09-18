package com.hawk.activity.type.impl.aftercompetition.cfg;

import org.hawk.collection.ConcurrentHashTable;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.google.common.collect.Table;
import com.hawk.activity.type.impl.aftercompetition.AfterCompetitionConst;

@HawkConfigManager.XmlResource(file = "activity/after_competition_party/after_competition_party_union.xml")
public class AfterCompetitionUnionCfg extends HawkConfigBase {

	@Id 
	private final int id;
	
	/** 渠道：1微信，2手Q */
	private final int channel;
	
	/** 所属联赛：1.泰伯 2.霸主 3.联赛  */
	private final int race;
	
	/** 联盟排名  */
	private final int rank;
	
	/** 联盟id */
	private final String winnerUnion;
	
	private final String serverId;
	private final String unionName;
	
	
	private static Table<String, Integer, AfterCompetitionUnionCfg> rankUnionTable = ConcurrentHashTable.create();

	public AfterCompetitionUnionCfg() {
		this.id = 0;
		this.channel = 1;
		this.race = 1;
		this.rank = 1;
		this.winnerUnion = "";
		this.serverId = "";
		this.unionName = "";
	}
	
	@Override
	protected boolean assemble() {
		if (channel != AfterCompetitionConst.CHANNEL_WX && channel != AfterCompetitionConst.CHANNEL_QQ) {
			return false;
		}
		if (race < AfterCompetitionConst.RACE_TYPE_1 || race > AfterCompetitionConst.RACE_TYPE_3) {
			return false;
		}
		if (HawkOSOperator.isEmptyString(winnerUnion)) {
			return false;
		}
		
		String key = channel + "_" + race;
		rankUnionTable.put(key, rank, this);
		return true;
	}

	public int getId() {
		return id;
	}

	public int getChannel() {
		return channel;
	}

	public int getRace() {
		return race;
	}

	public int getRank() {
		return rank;
	}

	public String getWinnerUnion() {
		return winnerUnion;
	}
	
	public String getServerId() {
		return serverId;
	}

	public String getUnionName() {
		return unionName;
	}

	public static AfterCompetitionUnionCfg getUnion(int channel, int race, int rank) {
		String key = channel + "_" + race;
		return rankUnionTable.get(key, rank);
	}

}
