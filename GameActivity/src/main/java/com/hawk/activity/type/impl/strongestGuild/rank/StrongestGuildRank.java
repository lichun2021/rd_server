package com.hawk.activity.type.impl.strongestGuild.rank;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityConfig;
import com.hawk.activity.type.impl.strongestGuild.StrongestGuildActivity;
import com.hawk.game.protocol.Activity.ActivityType;

import redis.clients.jedis.Tuple;

/***
 * 王者联盟排行接口
 * @author yang.rao
 *
 */
public interface StrongestGuildRank {
	
	public String key();
	
	public Set<Tuple> getRankList();
	
	/** 执行排行 **/
	public void doRank();
	
	public boolean addScore(double score, String member);
	
	public double getScore(String member);
	
	public void clear();
	
	/****
	 * 删除无效的元素（比如联盟被解散）
	 * @param element
	 */
	public void remove(String element);
	
	/***
	 * 获取当前活动期数
	 * @return
	 */
	default int getTermId(){
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(ActivityType.STRONGEST_GUILD_VALUE);
		if(opActivity.isPresent()){
			StrongestGuildActivity activity = (StrongestGuildActivity)opActivity.get();
			return activity.getActivityTermId();
		}
		return 0;
	}
	
	/***
	 * 获取本期活动的阶段
	 * @return
	 */
	default int getStageId(){
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(ActivityType.STRONGEST_GUILD_VALUE);
		if(opActivity.isPresent()){
			StrongestGuildActivity activity = (StrongestGuildActivity)opActivity.get();
			return activity.getCurStageId();
		}
		return 0;
	}
	
	/***
	 * 获取显示排名个数
	 * @return
	 */
	default int getRankSize(){
		return ActivityConfig.getInstance().getActivityCircularRankSize();
	}
	
	/***
	 * 积分转换
	 * @param resource
	 * @return
	 */
	default Set<Tuple> exchangeSet(Set<Tuple> resource){
		Set<Tuple> newSet = new LinkedHashSet<>();
		for(Tuple tu : resource){
			Tuple t = new Tuple(tu.getElement(), tu.getScore());
			newSet.add(t);
		}
		return newSet;
	}
}
