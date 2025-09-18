package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.google.common.collect.ImmutableList;
import com.hawk.game.item.ItemInfo;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 建设处任务
 * @author zhenyu.shang
 * @since 2022年3月31日
 */
@HawkConfigManager.XmlResource(file = "xml/nation_construction_achieve.xml")
public class NationConstructionQuestCfg extends HawkConfigBase {

	@Id
	protected final int achieveId;
	
	protected final int buildingLvl;
	
	protected final int weight;
	
	protected final String baseCondition;
	
	protected final String advCondition;
	
	protected final long continueTime;
	
	protected final int nationalBuildAward;
	
	protected final String nationalBaseAward;
	
	protected final String nationalAdvAward;
	
	protected final String personalBaseAward;
	
	protected final String personalAdvAward;
	
	protected final int personalAwardNum;
	
	protected List<ItemInfo> nationalBaseAwardList;
	protected List<ItemInfo> nationalBaseAndAdvAward;
	
	protected List<ItemInfo> personalBaseAwardList;
	protected List<ItemInfo> personalBaseAndAdvAward;
	
	protected List<List<Integer>> baseConditionList;
	protected List<List<Integer>> advConditionList;
	
	
	public NationConstructionQuestCfg() {
		achieveId = 0;
		buildingLvl = 0;
		weight = 0;
		baseCondition = "";
		advCondition = "";
		continueTime = 0;
		nationalBaseAward = "";
		nationalAdvAward = "";
		personalBaseAward = "";
		personalAdvAward = "";
		nationalBuildAward = 0;
		personalAwardNum = 0;
	}


	public int getAchieveId() {
		return achieveId;
	}

	public int getBuildingLvl() {
		return buildingLvl;
	}

	public int getWeight() {
		return weight;
	}

	public String getBaseCondition() {
		return baseCondition;
	}

	public String getAdvCondition() {
		return advCondition;
	}

	public long getContinueTime() {
		return continueTime;
	}

	public String getNationalBaseAward() {
		return nationalBaseAward;
	}

	public String getNationalAdvAward() {
		return nationalAdvAward;
	}

	public String getPersonalBaseAward() {
		return personalBaseAward;
	}

	public String getPersonalAdvAward() {
		return personalAdvAward;
	}

	public List<ItemInfo> getNationalBaseAwardList() {
		return nationalBaseAwardList;
	}


	public List<ItemInfo> getNationalBaseAndAdvAward() {
		return nationalBaseAndAdvAward;
	}


	public List<ItemInfo> getPersonalBaseAwardList() {
		return personalBaseAwardList;
	}


	public List<ItemInfo> getPersonalBaseAndAdvAward() {
		return personalBaseAndAdvAward;
	}


	public List<List<Integer>> getBaseConditionList() {
		return baseConditionList;
	}

	public List<List<Integer>> getAdvConditionList() {
		return advConditionList;
	}

	public int getNationalBuildAward() {
		return nationalBuildAward;
	}

	public int getPersonalAwardNum() {
		return personalAwardNum;
	}


	@Override
	protected boolean assemble() {
		// 国家基础奖励
		List<ItemInfo> baseList = new ArrayList<ItemInfo>();
		if (!HawkOSOperator.isEmptyString(nationalBaseAward)) {
			String[] split1 = nationalBaseAward.split(SerializeHelper.BETWEEN_ITEMS);
			for (int i = 0; i < split1.length; i++) {
				ItemInfo item = new ItemInfo(split1[i]);
				baseList.add(item);
			}
		}
		nationalBaseAwardList = ImmutableList.copyOf(baseList);
		// 国家基础和进阶奖励总和
		List<ItemInfo> adList = new ArrayList<ItemInfo>();
		if (!HawkOSOperator.isEmptyString(nationalAdvAward)) {
			String[] split1 = nationalAdvAward.split(SerializeHelper.BETWEEN_ITEMS);
			for (int i = 0; i < split1.length; i++) {
				ItemInfo item = new ItemInfo(split1[i]);
				adList.add(item);
			}
		}
		adList.addAll(baseList);
		nationalBaseAndAdvAward = ImmutableList.copyOf(adList);
		
		// 个人基础奖励
		List<ItemInfo> personbaseList = new ArrayList<ItemInfo>();
		if (!HawkOSOperator.isEmptyString(personalBaseAward)) {
			String[] split1 = personalBaseAward.split(SerializeHelper.BETWEEN_ITEMS);
			for (int i = 0; i < split1.length; i++) {
				ItemInfo item = new ItemInfo(split1[i]);
				personbaseList.add(item);
			}
		}
		personalBaseAwardList = ImmutableList.copyOf(personbaseList);
		// 个人基础和进阶奖励总和
		List<ItemInfo> personAdList = new ArrayList<ItemInfo>();
		if (!HawkOSOperator.isEmptyString(personalAdvAward)) {
			String[] split1 = personalAdvAward.split(SerializeHelper.BETWEEN_ITEMS);
			for (int i = 0; i < split1.length; i++) {
				ItemInfo item = new ItemInfo(split1[i]);
				personAdList.add(item);
			}
		}
		personAdList.addAll(personbaseList);
		personalBaseAndAdvAward = ImmutableList.copyOf(personAdList);
		
		// 基础条件
		List<List<Integer>> baseConditionList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(baseCondition)) {
			String[] split1 = baseCondition.split(SerializeHelper.BETWEEN_ITEMS);
			for (int i = 0; i < split1.length; i++) {
				
				List<Integer> baseCondition = new ArrayList<>();
				String[] split2 = split1[i].split(SerializeHelper.ATTRIBUTE_SPLIT);
				for (int j = 0; j < split2.length; j++) {
					baseCondition.add(Integer.valueOf(split2[j]));
				}
				
				baseConditionList.add(baseCondition);
			}
		}
		this.baseConditionList = baseConditionList;
		
		// 进阶条件
		List<List<Integer>> advConditionList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(advCondition)) {
			String[] split1 = advCondition.split(SerializeHelper.BETWEEN_ITEMS);
			for (int i = 0; i < split1.length; i++) {
				
				List<Integer> advCondition = new ArrayList<>();
				String[] split2 = split1[i].split(SerializeHelper.ATTRIBUTE_SPLIT);
				for (int j = 0; j < split2.length; j++) {
					advCondition.add(Integer.valueOf(split2[j]));
				}
				
				advConditionList.add(advCondition);
			}
		}
		this.advConditionList = advConditionList;
		return true;
	}
}
