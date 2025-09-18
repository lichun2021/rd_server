package com.hawk.activity.type.impl.rank;

import java.util.List;

/**
 * 活动通用排行榜接口
 * @author PhilChen
 *
 * @param <T>
 */
public interface ActivityRankProvider<T extends ActivityRank> {

	/**
	 * 获取排行类型
	 * @return
	 */
	ActivityRankType getRankType();
	
	/**
	 * 是否进行定时调用
	 * @return
	 */
	boolean isFixTimeRank();
	
	/**
	 * 初始加载排行榜数据
	 */
	void loadRank();
	
	/**
	 * 执行排名
	 */
	void doRankSort();
	
	/**
	 * 加入/更新排行
	 * @param rank
	 * @return
	 */
	boolean insertIntoRank(T rank);
	/**
	 * 获取排行列表
	 * @return
	 */
	List<T> getRankList();
	
	/**
	 * 获取指定id的排名信息
	 * @param id
	 * @return
	 */
	T getRank(String id);

	/**
	 * 获取指定名次区间的玩家列表
	 * @param start
	 * @param end
	 * @return
	 */
	List<T> getRanks(int start, int end);

	/**
	 * 清理排行榜
	 */
	void clean();

	/**
	 * 添加排行积分
	 * @param id
	 * @param score
	 */
	void addScore(String id, int score);
	
	/**
	 * 从排行中移除
	 * @param id
	 */
	void remMember(String id);

}
