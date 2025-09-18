package com.hawk.activity.type.impl.rank;

/**
 * 排行榜抽象
 * @author PhilChen
 *
 * @param <T>
 */
public abstract class AbstractActivityRankProvider<T extends ActivityRank> implements ActivityRankProvider<T> {
	
	/**
	 * 是否可以加入排行榜
	 * @param rankInfo
	 * @return
	 */
	protected abstract boolean canInsertIntoRank(T rankInfo);
	
	/**
	 * 插入/更新排行榜
	 * @param rankInfo
	 * @return
	 */
	protected abstract boolean insertRank(T rankInfo);
	
	/**
	 * 获取排行榜大小
	 * @return
	 */
	protected abstract int getRankSize();
	
	@Override
	public boolean insertIntoRank(T rankInfo) {
		if (canInsertIntoRank(rankInfo) == false) {
			return false;
		}
		insertRank(rankInfo);
		return true;
	}
	
}
