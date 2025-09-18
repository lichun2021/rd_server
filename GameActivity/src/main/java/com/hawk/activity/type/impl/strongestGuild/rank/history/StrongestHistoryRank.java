package com.hawk.activity.type.impl.strongestGuild.rank.history;

import java.util.Set;

/***
 * 王者联盟历史榜单
 * @author yang.rao
 *
 */
public interface StrongestHistoryRank {
	
	/** 返回从0到high的历史榜单 **/
	public Set<String> getHistoryRank(int high, int expireSeconds);
}
