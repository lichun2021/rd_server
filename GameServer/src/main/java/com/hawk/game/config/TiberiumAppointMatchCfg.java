package com.hawk.game.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;
import org.hawk.tuple.HawkTuple2;

/**
 * 泰伯利亚之战指定匹配服
 * 
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/tiberium_appoint_match.xml")
public class TiberiumAppointMatchCfg extends HawkConfigBase {
	/** 活动期数 */
	@Id
	private final int termId;
	
	/** 指定匹配联盟id列表 */
	private final String appointGuildId;


	private List<HawkTuple2<String, String>> appointList;
	

	public TiberiumAppointMatchCfg() {
		termId = 0;
		appointGuildId = "";
	}

	public int getTermId() {
		return termId;
	}
	
	public List<HawkTuple2<String, String>> getAppointList() {
		return appointList;
	}

	protected boolean assemble() {
		List<HawkTuple2<String, String>> matchList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(this.appointGuildId)) {
			String[] tupleStrs = this.appointGuildId.split(";");
			for (String tupleStr : tupleStrs) {
				String[] guildArr = tupleStr.split("_");
				if (guildArr.length != 2) {
					return false;
				}
				HawkTuple2<String, String> tuple = new HawkTuple2<String, String>(guildArr[0], guildArr[1]);
				matchList.add(tuple);
			}
		}
		this.appointList = matchList;
		return true;
	}

	@Override
	protected boolean checkValid() {
		HashSet<String> set = new HashSet<>();
		for (HawkTuple2<String, String> tuple : appointList) {
			set.add(tuple.first);
			set.add(tuple.second);
		}
		if (set.size() != appointList.size() * 2) {
			HawkLog.logPrintln("tiberium_appoint_match.xml duplicate appoint guildid!");
			return false;
		}
		return true;
	}
}
