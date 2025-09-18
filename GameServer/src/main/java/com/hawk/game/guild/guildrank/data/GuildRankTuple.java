package com.hawk.game.guild.guildrank.data;

import java.util.Set;
import java.util.TreeSet;

import redis.clients.jedis.Tuple;

public class GuildRankTuple extends Tuple {
	public GuildRankTuple(String element, Double score) {
		super(element, score);
	}

	@Override
	public int compareTo(Tuple o) {
		if (this.getScore() == o.getScore()) {
			//因为现在都是从大到小的排行榜 这里做个patch 暂时修复问题， 如果以后有从小到大的排行榜再做代码处理
			// 0 -> 0 , 1->-1 , -1 -> 1
			return (0 - this.getElement().compareTo(o.getElement())); 
		} else {
			if (this.getScore() < o.getScore()) {
				return -1;
			}
			return 1;
		}
	}
	
	static public Set<Tuple> descendingRankSet( Set<Tuple> paramSet ){
		TreeSet<Tuple> tmpSet = new TreeSet<>();
		for(Tuple tp : paramSet ){
			tmpSet.add(new GuildRankTuple(tp.getElement(), tp.getScore()));
		}
		return tmpSet.descendingSet();
	}
}
