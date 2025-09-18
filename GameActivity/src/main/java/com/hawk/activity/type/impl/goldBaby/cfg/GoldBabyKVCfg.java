package com.hawk.activity.type.impl.goldBaby.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

@HawkConfigManager.KVResource(file = "activity/gold_baby/gold_baby_cfg.xml")
public class GoldBabyKVCfg extends HawkConfigBase {

	private final String findCost1;

	private final String findCost2;

	private final String findCost3;

	private final String lockCost1;

	private final String lockCost2;

	private final String lockCost3;

	private final double lockTopGrade;

	private final int pool1FindTimes;

	private final int pool2FindTimes;

	private final int pool3FindTimes;

	private final int pool1ResetTimes;

	private final int pool2ResetTimes;

	private final int pool3ResetTimes;

	private final String ticketCost;

	private final int serverDelay;

	private final String startDate;

	private final String price;

	private final String item;

	private final int limit;

	private List<RewardItem.Builder> findCostList1;

	private List<RewardItem.Builder> findCostList2;

	private List<RewardItem.Builder> findCostList3;

	private List<RewardItem.Builder> lockCostList1;

	private List<RewardItem.Builder> lockCostList2;

	private List<RewardItem.Builder> lockCostList3;

	private List<RewardItem.Builder> ticketCostList;

	public GoldBabyKVCfg() {
		findCost1="";
		findCost2="";
		findCost3="";
		lockCost1="";
		lockCost2="";
		lockCost3="";
		lockTopGrade=0;
		pool1FindTimes=0;
		pool2FindTimes=0;
		pool3FindTimes=0;
		pool1ResetTimes=0;
		pool2ResetTimes=0;
		pool3ResetTimes=0;
		ticketCost="";
		serverDelay=0;
		startDate="";
		price="";
		item="";
		limit=0;
	}

	public String getPrice() {
		return price;
	}

	public String getStartDate() {
		return startDate;
	}

	public String getLockCost1() {
		return lockCost1;
	}

	public String getLockCost2() {
		return lockCost2;
	}

	public String getLockCost3() {
		return lockCost3;
	}

	public String getCost() {
		return ticketCost;
	}

	public String getFindCost1() {
		return findCost1;
	}

	public String getFindCost2() {
		return findCost2;
	}

	public String getFindCost3() {
		return findCost3;
	}

	public double getLockTopGrade() {
		return lockTopGrade;
	}

	public int getPool1FindTimes() {
		return pool1FindTimes;
	}

	public int getPool2FindTimes() {
		return pool2FindTimes;
	}

	public int getPool3FindTimes() {
		return pool3FindTimes;
	}


	public int getPool1ResetTimes() {
		return pool1ResetTimes;
	}

	public int getPool2ResetTimes() {
		return pool2ResetTimes;
	}

	public int getPool3ResetTimes() {
		return pool3ResetTimes;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}


	public int getLimit() {
		return limit;
	}

	@Override
	protected boolean assemble() {
		try {
			findCostList1 = RewardHelper.toRewardItemImmutableList(findCost1);
			findCostList2 = RewardHelper.toRewardItemImmutableList(findCost2);
			findCostList3 = RewardHelper.toRewardItemImmutableList(findCost3);
			lockCostList1 = RewardHelper.toRewardItemImmutableList(lockCost1);
			lockCostList2 = RewardHelper.toRewardItemImmutableList(lockCost2);
			lockCostList3 = RewardHelper.toRewardItemImmutableList(lockCost3);
			ticketCostList = RewardHelper.toRewardItemImmutableList(ticketCost);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}

	@Override
	protected final boolean checkValid() {

		return super.checkValid();
	}

	public List<RewardItem.Builder> getFindCostList1() {
		return findCostList1;
	}

	public List<RewardItem.Builder> getFindCostList2() {
		return findCostList2;
	}

	public List<RewardItem.Builder> getFindCostList3() {
		return findCostList3;
	}

	public void setFindCostList1(List<RewardItem.Builder> findCostList1) {
		this.findCostList1 = findCostList1;
	}

	public void setFindCostList2(List<RewardItem.Builder> findCostList2) {
		this.findCostList2 = findCostList2;
	}

	public void setFindCostList3(List<RewardItem.Builder> findCostList3) {
		this.findCostList3 = findCostList3;
	}

	public List<RewardItem.Builder> getTicketCostList() {
		return ticketCostList;
	}

	public void setTicketCostList(List<RewardItem.Builder> ticketCostList) {
		this.ticketCostList = ticketCostList;
	}

	public String getTicketCost() {
		return ticketCost;
	}

	public List<RewardItem.Builder> getLockCostList1() {
		return lockCostList1;
	}

	public void setLockCostList1(List<RewardItem.Builder> lockCostList1) {
		this.lockCostList1 = lockCostList1;
	}

	public List<RewardItem.Builder> getLockCostList2() {
		return lockCostList2;
	}

	public void setLockCostList2(List<RewardItem.Builder> lockCostList2) {
		this.lockCostList2 = lockCostList2;
	}

	public List<RewardItem.Builder> getLockCostList3() {
		return lockCostList3;
	}

	public void setLockCostList3(List<RewardItem.Builder> lockCostList3) {
		this.lockCostList3 = lockCostList3;
	}

	public String getItem() {
		return item;
	}

	/**
	 * 根据奖池id获取奖池可重置次数
	 * @param poolId
	 * @return
	 */
	public int getResetTimesById(int poolId) {
		if (poolId == 1) {
			return pool1ResetTimes;
		}
		if (poolId == 2) {
			return pool2ResetTimes;
		}
		if (poolId == 3) {
			return pool3ResetTimes;
		}
		return 0;
	}

	/**
	 * 根据奖池id获取奖池可抽取次数
	 * @param poolId
	 * @return
	 */
	public int getPoolFindTimesById (int poolId) {
		if (poolId == 1) {
			return pool1FindTimes;
		}
		if (poolId==2) {
			return pool2FindTimes;
		}
		if (poolId==3) {
			return pool3FindTimes;
		}
		return 0;
	}

	/**
	 * 根据奖池id和奖池已抽取次数
	 * 获取当前奖池抽奖的消耗
	 * @param poolId
	 * @param times
	 * @return
	 */
	public RewardItem.Builder getFindCostByIdAndTimes(int poolId, int times) {
		if (poolId==1) {
			return findCostList1.get(times);
		}
		if (poolId==2) {
			return findCostList2.get(times);
		}
		if (poolId==3) {
			return findCostList3.get(times);
		}
		return null;
	}


	/**
	 * 锁定最高档时
	 * 根据奖池id和奖池已抽取次数
	 * 获取当前奖池抽奖的消耗
	 * @param poolId
	 * @param times
	 * @return
	 */
	public RewardItem.Builder getLockCostByIdAndTimes(int poolId, int times) {
		if (poolId==1) {
			return lockCostList1.get(times);
		}
		if (poolId==2) {
			return lockCostList2.get(times);
		}
		if (poolId==3) {
			return lockCostList3.get(times);
		}
		return null;
	}
}
