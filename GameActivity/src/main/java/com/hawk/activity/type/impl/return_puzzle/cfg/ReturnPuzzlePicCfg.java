package com.hawk.activity.type.impl.return_puzzle.cfg;

import java.security.InvalidParameterException;
import java.util.List;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.serialize.string.SerializeHelper;


/**
 * 武者拼图活动积分奖励配置
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "activity/return_puzzle/return_puzzle_pic.xml")
public class ReturnPuzzlePicCfg extends AchieveConfig {
	/** */
	@Id
	private final int achieveId;
	
	/**
	 * 索引
	 */
	private final int order;
	
	/**
	 * 时间
	 */
	private final int time;
	/** 条件值*/
	private final String puzzleAchieve;	
	/** 条件类型*/
	private final int conditionType;
	/** 条件值*/
	private final String conditionValue;
	/** 奖励列表*/
	private final String rewards;
	
	private List<RewardItem.Builder> rewardList;
	private List<Integer> puzzleAchieveValueList;
	private AchieveType achieveType;
	private List<Integer> conditionValueList;
	
	public ReturnPuzzlePicCfg() {
		achieveId = 0;
		order = 0;
		time =0;
		puzzleAchieve="";
		conditionType = 0;
		conditionValue = "";
		rewards = "";
	}
	
	@Override
	protected boolean assemble() {
		try {
			achieveType = AchieveType.getType(conditionType);
			if (achieveType == null) {
				HawkLog.errPrintln("achieve type not found! type: {}", conditionType);
				return false;
			}
			rewardList = RewardHelper.toRewardItemImmutableList(rewards);
			conditionValueList = SerializeHelper.stringToList(Integer.class, conditionValue, SerializeHelper.ATTRIBUTE_SPLIT);
			puzzleAchieveValueList = SerializeHelper.stringToList(Integer.class, puzzleAchieve, SerializeHelper.ATTRIBUTE_SPLIT);
			
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}
	
	public String getRewards() {
		return rewards;
	}

	public List<Integer> getPuzzleAchieveValueList() {
		return puzzleAchieveValueList;
	}

	public void setPuzzleAchieveValueList(List<Integer> puzzleAchieveValueList) {
		this.puzzleAchieveValueList = puzzleAchieveValueList;
	}

	public int getOrder() {
		return order;
	}

	public int getTime() {
		return time;
	}

	public String getPuzzleAchieve() {
		return puzzleAchieve;
	}

	public void setRewardList(List<RewardItem.Builder> rewardList) {
		this.rewardList = rewardList;
	}

	@Override
	public int getAchieveId() {
		return achieveId;
	}

	@Override
	public AchieveType getAchieveType() {
		return achieveType;
	}
	
	@Override
	public List<Integer> getConditionValues() {
		return conditionValueList;
	}
	
	@Override
	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}

	@Override
	public String getReward() {
		// TODO Auto-generated method stub
		return rewards;
	}	
	
	@Override
	protected boolean checkValid() {
		int size = HawkConfigManager.getInstance().getConfigIterator(ReturnPuzzlePicCfg.class).size();
		List<ReturnPuzzleDateCfg> list = HawkConfigManager.getInstance().getConfigIterator(ReturnPuzzleDateCfg.class).toList(); 
		ReturnPuzzleDateCfg cfg = list.get(list.size()-1);
		if(cfg.getDuration() < size){
			throw new InvalidParameterException("ReturnPuzzleDateCfg config id="+cfg.getId()+",Duration="+cfg.getDuration()+",ReturnPuzzlePicCfg row size="+size+",check file");
		}
		return super.checkValid();
	}
	
}
