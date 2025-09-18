package com.hawk.activity.type.impl.powercollect.rank;

import java.util.List;
import java.util.Optional;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.type.impl.powercollect.PowerCollectActivity;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.gamelib.rank.RankScoreHelper;

public interface PowerCollectRank<T extends PowerCollectRankData> {
	
	public String getKey();
	
	public void doRankSort();
	
	public boolean addScore(double score, String member);
	
	public T loadRankData(String element);
	
	public List<T> getRankList();
	
	/** 获取有奖励的排行列表 **/
	public List<T> getHasRewardRankList(int termId, int maxSize);
	
	public int getRankSize();
	
	/** 删掉一个参与排行的元素 **/
	public void remove(String element);
	
	public void clear();
	
	/***
	 * 积分转换
	 * @param score(真实积分)
	 * @return 转换之后的积分
	 */
	default long exchangeScore(long score){
		return RankScoreHelper.calcSpecialRankScore(score);
	}
	
	/***
	 * 计算真实积分
	 * @param score(从redis获取的数值)
	 * @return 真实积分
	 */
	default long calRealScore(long score){
		return RankScoreHelper.getRealScore(score);
	}
	
	default int getTermId(){
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(ActivityType.SUPER_POWER_LAB_VALUE);
		if(opActivity.isPresent()){
			PowerCollectActivity activity = (PowerCollectActivity)opActivity.get();
			return activity.getActivityTermId();
		}
		return 0;
	}
}
