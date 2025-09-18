package com.hawk.game.module.lianmengyqzz.march.cfg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.google.common.collect.ImmutableList;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZConst.YQZZAchieveType;


/**
 * 跨服活动时间配置
 * @author  che
 *
 */
@HawkConfigManager.XmlResource(file = "xml/moon_war_nation_achieve.xml")
public class YQZZCountryAchieveCfg extends HawkConfigBase implements YQZZAchieveCfg{

	/** 成就id*/
	@Id
	private final int achieveId;
	/** 条件类型*/
	private final int conditionType1;
	/** 条件值*/
	private final String conditionValue1;
	/** 条件类型*/
	private final int conditionType2;
	/** 条件值*/
	private final String conditionValue2;
	/** 奖励列表*/
	private final String rewards;
	
	private List<Long> conditionValueList1;
	private List<Long> conditionValueList2;
	private long targetValue1;
	private long targetValue2;
	private List<ItemInfo> rewardItems;
	
	
	public YQZZCountryAchieveCfg() {
		achieveId = 0;
		conditionType1 = 0;
		conditionValue1 = "";
		conditionType2 = 0;
		conditionValue2= "";
		rewards= "";
	}
	
	public int getAchieveId() {
		return achieveId;
	}
	
	public int getConditionType1() {
		return conditionType1;
	}
	
	public int getConditionType2() {
		return conditionType2;
	}
	
	public List<Long> getConditionValueList1() {
		return conditionValueList1;
	}
	
	public List<Long> getConditionValueList2() {
		return conditionValueList2;
	}
	
	@Override
	public long getTargetValue1() {
		return this.targetValue1;
	}
	
	@Override
	public long getTargetValue2() {
		return this.targetValue2;
	}
	
	public List<ItemInfo> getRewardList(){
		List<ItemInfo> ret = new ArrayList<>();
		for (ItemInfo item : rewardItems) {
			ret.add(item.clone());
		}
		return ret;
	}
	
	protected boolean assemble() {
		List<Long> conList1 = new ArrayList<>();
		long traget1 = 0;
		if(!HawkOSOperator.isEmptyString(this.conditionValue1)){
			String[] conArr = this.conditionValue1.split(",");
			for(int i=0;i<conArr.length;i++){
				String str =  conArr[i];
				if(i == conArr.length -1){
					traget1 = Long.parseLong(str.trim());
				}else{
					conList1.add(Long.parseLong(str.trim()));
				}
			}
		}
		this.conditionValueList1 = ImmutableList.copyOf(conList1);
		this.targetValue1 = traget1;
		
		List<Long> conList2 = new ArrayList<>();
		long traget2 = 0;
		if(!HawkOSOperator.isEmptyString(this.conditionValue2)){
			String[] conArr = this.conditionValue2.split(",");
			for(int i=0;i<conArr.length;i++){
				String str =  conArr[i];
				if(i == conArr.length -1){
					traget2 = Long.parseLong(str.trim());
				}else{
					conList2.add(Long.parseLong(str.trim()));
				}
			}
		}
		this.conditionValueList2 = ImmutableList.copyOf(conList2);
		this.targetValue2 = traget2;
		
		rewardItems = ItemInfo.valueListOf(this.rewards);
		return true;
	}

	@Override
	protected boolean checkValid() {
		if(this.conditionType1 != 0){
			if(YQZZAchieveType.valueOf(this.conditionType1) == null){
				return false;
			}
		}
		if(this.conditionType2 != 0){
			if(YQZZAchieveType.valueOf(this.conditionType2) == null){
				return false;
			}
		}
		return true;
	}
}
