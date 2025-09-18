package com.hawk.game.module.college.cfg;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.cfgElement.EffectObject;
import com.hawk.game.item.ItemInfo;

/**
 * 跨服基础配置
 * 
 * @author Codej
 *
 */
@HawkConfigManager.KVResource(file = "xml/college_const.xml")
public class CollegeConstCfg extends HawkConfigBase {
	
	/**
	 * 实例
	 */
	private static CollegeConstCfg instance = null;
	
	/**
	 * 获取实例
	 * @return
	 */
	public static CollegeConstCfg getInstance() {
		return instance;
	}
	
	/** # 模块开关*/
	private final int isSysterOpen;
	
	/** 教官大本等级限制*/
	private final int coachCityLvlLimit;

	/** 教官等级限制*/
	private final int coachLevelLimit;

	/** 创建学院消耗*/
	private final String createCost;

	/** 学院人数限制*/
	private final int collegeMemberMaxCnt;

	/** 在线时长领取数量限制*/
	private final int onlineRewardLimit;

	/** 教官列表显示数量*/
	private final int coachListSize;

	/** 申请列表显示数量限制*/
	private final int applyListSize;

	/** 每个在线成员提供的作用号加成*/
	private final String memberEffect;
	
	
	/** 提醒上线冷却*/
	private final int remindTimeLimitCD;

	/** 离线多长时间之后可以提醒上线*/
	private final int remindTime;
	
	/** 加入申请有效期*/
	private final int applyEffectTime;
	
	
	
	
	//学员主动退出学院扣除积分百分比
	private final int collegeDeductRatio;
	
	private final int collegeScoreItem;
	
	// 学院可存储体力上限
	private final int maxStrength;
	
	// 每日可分配体力上限
	private final int dayMaxStrength;
	
	// 重新命名消耗
	private final String renameExpend;
	
	// 换教官的时间：天
	private final int changeCoach;
	
	// 线上已有学院乱码名称
	private final String randomName;
	
	// 体力补充比例:万分比
	private final int strengthBack;

	//名字长度限制
	private final int nameLenLimitMax;
	private final int nameLenLimitMin;
	
	//联盟邀请冷却时间
	private final int letterSendCD;
	//联盟邀请有效时间
	private final int letterContinuedCD;
	//被踢加回冷却时间
	private final int letterAgainJoinCD;
	// -----------------------------自己组装的---------------------------------
	

	/**
	 * 创建学院消耗列表
	 */
	private List<ItemInfo> createCostList;	

	private List<EffectObject> effectList;
	// -----------------------------------------------------------------------
	
	/**
	 * 构造
	 */
	public CollegeConstCfg() {
		instance = this;
		isSysterOpen = 0;
		coachCityLvlLimit = 0;
		coachLevelLimit = 0;
		createCost = "";
		collegeMemberMaxCnt = 0;
		onlineRewardLimit = 0;
		coachListSize = 10;
		applyListSize = 30;
		memberEffect = "";
		remindTimeLimitCD = 0;
		remindTime = 0;
		applyEffectTime = 86400;
		
		collegeDeductRatio =0;
		maxStrength = 0;
		dayMaxStrength = 0;
		renameExpend = "";
		changeCoach = 0;
		randomName = "";
		strengthBack= 0;
		nameLenLimitMax= 0;
		nameLenLimitMin= 0;
		
		letterSendCD = 0;
		letterContinuedCD = 0;
		letterAgainJoinCD = 0;
		collegeScoreItem= 0;
		
	}
	
	public boolean getIsSysterOpen() {
		return isSysterOpen == 1;
	}

	public int getCoachCityLvlLimit() {
		return coachCityLvlLimit;
	}

	public int getCoachLevelLimit() {
		return coachLevelLimit;
	}

	public String getCreateCost() {
		return createCost;
	}

	public int getCollegeMemberMaxCnt() {
		return collegeMemberMaxCnt;
	}

	public int getOnlineRewardLimit() {
		return onlineRewardLimit;
	}

	public int getCoachListSize() {
		return coachListSize;
	}

	public int getApplyListSize() {
		return applyListSize;
	}

	public String getMemberEffect() {
		return memberEffect;
	}

	public long getRemindTimeLimitCD() {
		return remindTimeLimitCD * 1000l;
	}

	public long getRemindTime() {
		return remindTime * 1000l;
	}

	public long getApplyEffectTime() {
		return applyEffectTime * 1000l;
	}

	public List<ItemInfo> getCreateCostList() {
		return createCostList.stream().map(e -> e.clone()).collect(Collectors.toList());
	}

	public List<EffectObject> getEffectList() {
		return effectList;
	}
	
	

	public int getCollegeDeductRatio() {
		return collegeDeductRatio;
	}

	public int getMaxStrength() {
		return maxStrength;
	}

	public int getDayMaxStrength() {
		return dayMaxStrength;
	}

	public ItemInfo getRenameExpendItem(int time) {
		String[] costArr = this.renameExpend.split(",");
		ItemInfo cost = null;
		if(time >= costArr.length){
			cost = ItemInfo.valueOf(costArr[costArr.length-1]);
		}else{
			cost = ItemInfo.valueOf(costArr[time-1]);
		}
		if(cost.getCount() > 0){
			return cost;
		}
		return null;
	}

	public int getChangeCoach() {
		return changeCoach;
	}

	public String getRandomName() {
		return randomName;
	}

	public int getStrengthBack() {
		return strengthBack;
	}

	public int getNameLenLimitMax() {
		return nameLenLimitMax;
	}
	
	public int getNameLenLimitMin() {
		return nameLenLimitMin;
	}
	
	public int getLetterAgainJoinCD() {
		return letterAgainJoinCD;
	}
	
	public int getLetterContinuedCD() {
		return letterContinuedCD;
	}
	
	public int getLetterSendCD() {
		return letterSendCD;
	}
	
	public int getCollegeScoreItem() {
		return collegeScoreItem;
	}

	@Override
	protected boolean assemble() {
		this.createCostList = ItemInfo.valueListOf(createCost);
		
		List<EffectObject> effectList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(memberEffect)) {
			String[] array = memberEffect.split(",");
			for (String val : array) {
				String[] info = val.split("_");
				EffectObject effect = new EffectObject(Integer.parseInt(info[0]), Integer.parseInt(info[1]));
				effectList.add(effect);
			}
		}
		this.effectList = effectList;
		
		return super.assemble();
	}
}
