package com.hawk.game.config;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.helper.HawkAssert;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.type.impl.shareGlory.ShareGloryActivity;
import com.hawk.activity.type.impl.stronestleader.StrongestLeaderActivity;
import com.hawk.activity.type.impl.strongestGuild.StrongestGuildActivity;
import com.hawk.activity.type.impl.yurirevenge.YuriRevengeActivity;
import com.hawk.game.GsApp;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.module.lianmengyqzz.march.cfg.YQZZTimeCfg;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZMatchService;
import com.hawk.game.player.Player;
import com.hawk.game.president.PresidentCity;
import com.hawk.game.president.PresidentFightService;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.CrossActivity.CrossActivityState;
import com.hawk.game.protocol.President.PresidentPeriod;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.XmlResource(file = "xml/gift_group.xml")
public class GiftGroupCfg extends HawkConfigBase {
	/**
	 *
	 */
	@Id
	private final int id;

	/**
	 *
	 */
	private final int type;

	/**
	 *
	 */
	private final String groupName;

	/**
	 *解锁条件类型
	 */
	private final int unlockConType;

	/**
	 *解锁条件参数
	 */
	private final String unlockConValue;

	/**
	 *解锁条件类型
	 */
	private final int unlockConType2;

	/**
	 *解锁条件参数
	 */
	private final String unlockConValue2;
	/**
	 *有效
	 */
	private final int onSale;

	/**
	 *后置礼包
	 */
	private final String postGift;

	/**
	 *限购类型
	 */
	private final int limitType;

	/**
	 *限购次数
	 */
	private final int limitCnt;
	
	/**
	 * 是否需要推荐
	 */
	private final int recommend;
	
	/**
	 * 每日推荐次数
	 */
	private final int dayRecommendTimes;
	
	/**
	 * 总推荐次数
	 */
	private final int totalRecommendTimes;
	
	/**
	 * 推荐vip最小等级
	 */
	private final int vipLevelMin;
	
	/**
	 * 推荐vip最大等级
	 */
	private final int vipLevelMax;
	
	/**
	 * 推荐大本最小等级
	 */
	private final int buildLevelMin;
	
	/**
	 * 推荐大本最大等级
	 */
	private final int buildLevelMax;
	
	/**
	 * 推荐间隔时间(s)
	 */
	private final int intervalTime;
	
	/**
	 * 推荐优先级
	 */
	private final int recommendPriority;
	
	/**
	 * 当日有购买行为后，是否移除当日推荐列表
	 */
	private final int isRemove;
	
	
	private List<int[]> unlockConValueList;
	private List<int[]> unlockConValueList2;

	private Map<Integer, Integer> postGiftMap;

	//类型代金券用
	private final int voucherType;
	
	public GiftGroupCfg() {
		this.id = 0;
		this.type = 0;
		this.groupName = "";
		this.unlockConType = 0;
		this.unlockConValue = "";
		this.unlockConType2 = 0;
		this.unlockConValue2 = "";
		this.onSale = 0;
		this.postGift = "";
		this.limitType = 0;
		this.limitCnt = 0;
		this.voucherType = 0;
		
		this.recommend = 0;
		this.dayRecommendTimes = 0;
		this.totalRecommendTimes = 0;
		this.vipLevelMin = 0;
		this.vipLevelMax = 0;
		this.buildLevelMin = 0;
		this.buildLevelMax = 0;
		this.intervalTime = 0;
		this.recommendPriority = 0;
		this.isRemove = 0;
	}
	
    public boolean checkUnlockConValue(Player player){
        boolean first = checkUnlockConValue(player, unlockConValueList, unlockConType);
        boolean second = checkUnlockConValue(player, unlockConValueList2, unlockConType2);
        return first && second;
    }
	
	private static boolean checkUnlockConValue(Player player ,List<int[]> unlockConValueList , int unlockConType){
		long curTime = HawkTime.getMillisecond();
		int curSeconds = HawkTime.getSeconds();
		int areaId = GameUtil.getWorldId();
		Iterator<int[]> condIterator = unlockConValueList.iterator();
		boolean rlt = false;
		if (unlockConValueList.isEmpty()) {
			rlt = true;
			
			return rlt;
		}
		
		while(condIterator.hasNext()) {
			int [] valueArray = condIterator.next();
			rlt = false;
			switch(valueArray[0]) {
			case GsConst.GiftConst.UNLOCK_PLAYER_REGISTER:				
				rlt = player.getCreateTime() + valueArray[1] <= curTime && curTime < player.getCreateTime() + valueArray[2]; 
			break;
			case GsConst.GiftConst.UNLOCK_PLAYER_BUILDING_LEVEL:
				int buildingLevel = player.getData().getBuildingMaxLevel(valueArray[1]);
				rlt = buildingLevel >= valueArray[2] && buildingLevel < valueArray[3];
			break;
			case GsConst.GiftConst.UNLOCK_PLAYER_COMMANDER_LEVEL:
				rlt = player.getLevel() >= valueArray[1] && player.getLevel() < valueArray[2];
				break;
			case GsConst.GiftConst.UNLOCK_PRESIDENT:
				//战争状态是开启的.
				if (!PresidentFightService.getInstance().isFightPeriod()) {
					PresidentCity presidentCity = PresidentFightService.getInstance().getPresidentCity();
					if (presidentCity.getStatus() == PresidentPeriod.INIT_VALUE) {
						rlt = (presidentCity.getStartTime() - curTime) <= valueArray[1] * 1000l;
					} else if(presidentCity.getStatus() == PresidentPeriod.PEACE_VALUE) {
						rlt = (presidentCity.getStartTime() - curTime) <= valueArray[1] * 1000l || 
								presidentCity.getEndTime() + valueArray[2] * 1000l >= curTime;
					}
				} else {
					rlt = true;
				}
				break;
			case GsConst.GiftConst.UNLOCK_SERVER_OPEN:
				long serverOpenTime = GsApp.getInstance().getServerOpenAM0Time();
				long time = ((curTime - serverOpenTime) / 1000);
				rlt =  time >= valueArray[1] && time < valueArray[2];
				break;
			case GsConst.GiftConst.UNLOCK_WEEK:
				int dayOfWeek = HawkTime.getDayOfWeek();
				int day = 0;
				for (int i = 1; i < valueArray.length; i++) {
					day = valueArray[i];
					if (day == dayOfWeek) {
						rlt = true;
						break;
					}
				}
				break;
			case GsConst.GiftConst.UNLOCK_STRONGEST_STAGE:
				Optional<StrongestLeaderActivity> strongestLeader = ActivityManager.getInstance().getGameActivityByType(Activity.ActivityType.STRONEST_LEADER_VALUE);
				if (strongestLeader.isPresent()  && strongestLeader.get().isOpening(player.getId()) ) {
					for (int j = 1; j < valueArray.length; j++) {
						if (valueArray[j] == strongestLeader.get().getStageId()) {
							rlt =true;
							break;
						}
					}					
				}
				break;
			case GsConst.GiftConst.UNLOCK_YURI:
				Optional<YuriRevengeActivity> yuri = ActivityManager.getInstance().getGameActivityByType(Activity.ActivityType.YURI_REVENGE_VALUE);
				if (yuri.isPresent() && yuri.get().isShow(player.getId())) {
					rlt = true;
				}
				break;
			case GsConst.GiftConst.UNLOCK_NATURE_TIME:
				rlt = valueArray[1] <= curSeconds && curSeconds < valueArray[2];
				break;
			case GsConst.GiftConst.UNLOCK_AREA:
				for (int j = 1; j < valueArray.length; j++) {
					if (valueArray[j] == areaId) {
						rlt = true;
						break;
					}
				}
				break;
			case GsConst.GiftConst.UNLOCK_PLATFORM:
				for (int j = 1; j < valueArray.length; j++) {
					if (valueArray[j] == player.getPlatId()) {
						rlt = true;
						break;
					}
				}
				break;
			case GsConst.GiftConst.UNLOCK_CROSS_ACTIVITY:
				//非隐藏阶段则为开启.
				if (CrossActivityService.getInstance().isState(CrossActivityState.C_OPEN)
						|| CrossActivityService.getInstance().isState(CrossActivityState.C_SHOW)) {
					rlt = true;
				}
				break;			
			case GsConst.GiftConst.UNLOCK_KING_GUILD:
				Optional<StrongestGuildActivity> strongestGuild = ActivityManager.getInstance().getGameActivityByType(Activity.ActivityType.STRONGEST_GUILD_VALUE);
				if (strongestGuild.isPresent()  && strongestGuild.get().isOpening(player.getId()) ) {
					for (int j = 1; j < valueArray.length; j++) {
						if (valueArray[j] == strongestGuild.get().getCurStageId()) {
							rlt =true;
							break;
						}
					}					
				}
			case GsConst.GiftConst.UNLOCK_COMMON_ACTIVITY:
				Optional<ActivityBase> opActivityBase = ActivityManager.getInstance().getActivity(valueArray[1]);
				if (!opActivityBase.isPresent()) {
					break;
				}
				ActivityBase activityBase = opActivityBase.get();
				if (activityBase.isOpening(player.getId())) {
					rlt =true;
				}
				break;
			case GsConst.GiftConst.YQZZ:
				long currentTime = HawkTime.getMillisecond();
				int termId =YQZZMatchService.getInstance().getDataManger().getStateData().getTermId();
				YQZZTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(YQZZTimeCfg.class, termId);
				if (timeCfg != null) {
					if (currentTime >= timeCfg.getShowTimeValue() && currentTime <= timeCfg.getRewardTimeValue()) {
						rlt = true;
					}
				}
				break;
			case GsConst.GiftConst.SHARE_GLORY_GIFT:
				Optional<ActivityBase> activityOp = ActivityManager.getInstance().getActivity(ActivityType.SHARE_GLORY_VALUE);
				if (!activityOp.isPresent()) {
					break;
				}
				ShareGloryActivity activity = (ShareGloryActivity)activityOp.get();
				if (activity != null && !activity.isHidden(player.getId())) {
					rlt = true;
				}
				break;
			default:
				rlt = true;
				break;
			}
            // 移除逐条件打印，避免日志过量
			
			//逻辑与的情况下,非真即中断  逻辑或的情况,真即中断。
			if (unlockConType == GsConst.GiftConst.LOGIC_AND && !rlt ||
					(unlockConType == GsConst.GiftConst.LOGIC_OR && rlt)) {
				break;
			}			
		}
		return rlt;
	}

	public int getId() {
		return id;
	}

	public int getType() {
		return type;
	}

	public String getGroupName() {
		return groupName;
	}

	public int getUnlockConType() {
		return unlockConType;
	}

	public String getUnlockConValue() {
		return unlockConValue;
	}

	public int getOnSale() {
		return onSale;
	}

	public String getPostGift() {
		return postGift;
	}

	public int getLimitType() {
		return limitType;
	}

	public int getLimitCnt() {
		return limitCnt;
	}
	
	@Override
	public boolean checkValid() {
		for (Integer groupId : this.getPostGiftMap().keySet()) {
			GiftGroupCfg groupCfg = HawkConfigManager.getInstance().getConfigByKey(GiftGroupCfg.class, groupId);
			if (groupCfg == null) {
				throw new InvalidParameterException("GiftGroupCfg 配置的后置礼包组ID找不到 id=>"+this.id+" 后置礼包Id=>"+groupId);
			}
			
			if (groupCfg.getType() != GsConst.GiftConst.GROUP_TYPE_POST) {
				throw new InvalidParameterException("GiftGroupCfg 配置的后置礼包ID不是后置礼包类型 id=>"+this.id+" 后置礼包Id=>"+groupId);
			}
		}
		
		Iterator<int[]> condIterator = this.getUnlockConValueList().iterator();
		while(condIterator.hasNext()) {
			int [] valueArray = condIterator.next();
			switch(valueArray[0]) {
			case GsConst.GiftConst.UNLOCK_PLAYER_REGISTER:				 
				HawkAssert.isTrue(valueArray[2] > valueArray[1], "giftGroup 结束时间必须大于开始时间 id:"+id);
			break;
			case GsConst.GiftConst.UNLOCK_PLAYER_BUILDING_LEVEL:
				BuildingType bt = BuildingType.valueOf(valueArray[1]);
				HawkAssert.notNull(bt, "giftGroup 建筑类型不对 id:"+id);
				HawkAssert.isTrue(valueArray[3] > valueArray[2], "giftGroup 最大建筑等级要大于最小建筑等级 id:"+id);
			break;
			case GsConst.GiftConst.UNLOCK_PLAYER_COMMANDER_LEVEL:
				HawkAssert.isTrue(valueArray[2] > valueArray[1], "giftGroup 最大玩家等级要大于最小玩家等级  id:"+id);
				break;
			case GsConst.GiftConst.UNLOCK_PRESIDENT:				
				break;
			case GsConst.GiftConst.UNLOCK_SERVER_OPEN:
				HawkAssert.isTrue(valueArray[2] >= valueArray[1], "giftGroup最大时间要大于最小时间id:"+id);
				break;
			case GsConst.GiftConst.UNLOCK_WEEK:
				HawkAssert.isTrue(valueArray[1] >=1 && valueArray[1] <= 7, "星期必须配置在1-7之间 id:"+id);
				break;
			case GsConst.GiftConst.UNLOCK_STRONGEST_STAGE:			
				break;
			case GsConst.GiftConst.UNLOCK_YURI:				
				break;
			case GsConst.GiftConst.UNLOCK_NATURE_TIME:
				HawkAssert.isTrue(valueArray[2] > valueArray[1], "giftGroup最大时间要大于最小时间id:"+id);
				break;
			case GsConst.GiftConst.UNLOCK_AREA:
				break;
			case GsConst.GiftConst.UNLOCK_PLATFORM:				
				break;
			case GsConst.GiftConst.UNLOCK_COMMON_ACTIVITY:
				ActivityType activityType = ActivityType.valueOf(valueArray[1]);
				HawkAssert.isTrue(activityType != null, "活动ID不存在 id:"+valueArray[1]);
				break;
			default:
				break;
			}					
		}			
		
		return true;
	}

	@Override
	public boolean assemble() {		
		List<int[]> intArrayList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(this.unlockConValue)) {
			String[] itemArray = this.unlockConValue.split(SerializeHelper.BETWEEN_ITEMS);
			for (String item : itemArray) {
				String[] columnArray = item.split(SerializeHelper.ATTRIBUTE_SPLIT);
				int[] columns = new int[columnArray.length];
				Integer unlockType = Integer.parseInt(columnArray[0]);				
				switch(unlockType) {
				case GsConst.GiftConst.UNLOCK_NATURE_TIME:
					columns[0] = unlockType;
					for (int i = 1; i < columnArray.length; i++) {
						columns[i] = (int)(HawkTime.parseTime(columnArray[i]) / 1000);
					}
					break;
				default:
					for (int i = 0; i < columnArray.length; i++) {
						columns[i] = Integer.parseInt(columnArray[i]);
					}
					break;
				}
				
				intArrayList.add(columns);
			}			
		}	
		this.unlockConValueList = intArrayList;
		
		List<int[]> intArrayList2 = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(this.unlockConValue2)) {
			String[] itemArray = this.unlockConValue2.split(SerializeHelper.BETWEEN_ITEMS);
			for (String item : itemArray) {
				String[] columnArray = item.split(SerializeHelper.ATTRIBUTE_SPLIT);
				int[] columns = new int[columnArray.length];
				Integer unlockType = Integer.parseInt(columnArray[0]);				
				switch(unlockType) {
				case GsConst.GiftConst.UNLOCK_NATURE_TIME:
					columns[0] = unlockType;
					for (int i = 1; i < columnArray.length; i++) {
						columns[i] = (int)(HawkTime.parseTime(columnArray[i]) / 1000);
					}
					break;
				default:
					for (int i = 0; i < columnArray.length; i++) {
						columns[i] = Integer.parseInt(columnArray[i]);
					}
					break;
				}
				
				intArrayList2.add(columns);
			}			
		}	
		this.unlockConValueList2 = intArrayList2;
		
		this.postGiftMap = SerializeHelper.cfgStr2Map(this.postGift);
		
		return true;
	}

	public List<int[]> getUnlockConValueList() {
		return unlockConValueList;
	}

	public Map<Integer, Integer> getPostGiftMap() {
		return postGiftMap;
	}

	public void setPostGiftMap(Map<Integer, Integer> postGiftMap) {
		this.postGiftMap = postGiftMap;
	}

	public int getVoucherType() {
		return voucherType;
	}

	public int getRecommend() {
		return recommend;
	}

	public int getDayRecommendTimes() {
		return dayRecommendTimes;
	}

	public int getTotalRecommendTimes() {
		return totalRecommendTimes;
	}

	public int getVipLevelMin() {
		return vipLevelMin;
	}

	public int getVipLevelMax() {
		return vipLevelMax;
	}

	public int getBuildLevelMin() {
		return buildLevelMin;
	}

	public int getBuildLevelMax() {
		return buildLevelMax;
	}

	public long getIntervalTime() {
		return intervalTime * 1000L;
	}

	public int getRecommendPriority() {
		return recommendPriority;
	}

	public int getIsRemove() {
		return isRemove;
	}

	public int getUnlockConType2() {
		return unlockConType2;
	}

	public String getUnlockConValue2() {
		return unlockConValue2;
	}

	public List<int[]> getUnlockConValueList2() {
		return unlockConValueList2;
	}
	
}