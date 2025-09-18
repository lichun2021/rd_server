package com.hawk.game.march;

import java.util.ArrayList;
import java.util.List;
import org.hawk.os.HawkTime;

import com.hawk.game.config.ConstProperty;

/**
 * 自动打野行军参数
 * 
 * @author lating
 *
 */
public class AutoMonsterMarchParam {
	// 野怪最低等级
	private int minLevel;
	// 野怪最高等级
	private int maxLevel;
	// 搜索野怪类型
	private List<Integer> searchType;
	// 打野出征部队信息
	private List<AutoMarchInfo> autoMarchList;
	// 自动打野开启时间
	private long openTime;
	// 打野周卡是否处于生效状态
	private boolean monthCardBuffEnable;
	// 处于迁城状态
	private volatile boolean cityMoving;
	
	/**
	 *	 打野出征部队信息
	 *
	 */
	public static class AutoMarchInfo {
		// 打野出征部队信息
		private List<ArmyInfo> army;
		// 打野出征英雄ID
		private List<Integer> heroIds;
		// 打野出征机甲ID
		private int superSoldierId;
		// 优先级
		private int priority;
		// 状态，1已出征，0未出征
		private int status;
		// 编队信息 0-默认，1-编队1,2-编队2，3-编队3， 4-编队4
		private int troops;
		// 铠甲
		private int armourSuitType;
		// 天赋
		private int talent;
		// 超能实验室
		private int superLab;
		// 机甲核心
		private int mechacoreSuit;
		
		public List<ArmyInfo> getArmy() {
			return army;
		}
		
		public void setArmy(List<ArmyInfo> army) {
			this.army = army;
		}
		
		public List<Integer> getHeroIds() {
			return heroIds;
		}
		
		public void setHeroIds(List<Integer> heroIds) {
			this.heroIds = heroIds;
		}
		
		public int getSuperSoldierId() {
			return superSoldierId;
		}
		
		public void setSuperSoldierId(int superSoldierId) {
			this.superSoldierId = superSoldierId;
		}

		public int getPriority() {
			return priority;
		}

		public void setPriority(int priority) {
			this.priority = priority;
		}

		public int getStatus() {
			return status;
		}

		public void setStatus(int status) {
			this.status = status;
		}

		public int getTroops() {
			return troops;
		}

		public void setTroops(int troops) {
			this.troops = troops;
		}
		
		public int getId() {
			return priority * 100 + troops;
		}

		public int getArmourSuitType() {
			return armourSuitType;
		}

		public void setArmourSuitType(int armourSuitType) {
			this.armourSuitType = armourSuitType;
		}
		
		public int getMechacoreSuit() {
			return mechacoreSuit;
		}

		public void setMechacoreSuit(int mechacoreSuit) {
			this.mechacoreSuit = mechacoreSuit;
		}

		public int getTalent() {
			return talent;
		}

		public void setTalent(int talent) {
			this.talent = talent;
		}

		public int getSuperLab() {
			return superLab;
		}

		public void setSuperLab(int superLab) {
			this.superLab = superLab;
		}
		
	}
	
	public AutoMonsterMarchParam() {
		this.openTime = HawkTime.getMillisecond();
		this.autoMarchList = new ArrayList<AutoMarchInfo>();
	}

	public int getMinLevel() {
		return minLevel;
	}

	public void setMinLevel(int minLevel) {
		this.minLevel = minLevel;
	}

	public int getMaxLevel() {
		return maxLevel;
	}

	public void setMaxLevel(int maxLevel) {
		this.maxLevel = maxLevel;
	}

	public List<Integer> getSearchType() {
		return searchType;
	}

	public void setSearchType(List<Integer> searchType) {
		this.searchType = searchType;
	}

	public long getOpenTime() {
		return openTime;
	}

	@Override
	public String toString() {
		return "[minLevel=" + minLevel + ", maxLevel=" + maxLevel + ", searchType=" + searchType + ", openTime=" + openTime + ", monthCardBuffEnable=" + monthCardBuffEnable + "]";
	}

	public boolean isCityMoving() {
		return cityMoving;
	}

	public void setCityMoving(boolean cityMoving) {
		this.cityMoving = cityMoving;
	}

	public synchronized void addAutoMarchInfo(List<AutoMarchInfo> autoMarchInfos) {
		this.autoMarchList.clear();
		this.autoMarchList.addAll(autoMarchInfos);
	}

	public boolean isMonthCardBuffEnable() {
		return monthCardBuffEnable;
	}

	public void setMonthCardBuffEnable(boolean monthCardBuffEnable) {
		this.monthCardBuffEnable = monthCardBuffEnable;
	}
	
	/**
	 * 获取可开启的自动打野行军队列数
	 * @return
	 */
	public synchronized int getAutoMarchCount() {
		return autoMarchList.size();
	}
	
	/**
	 * 重置自动打野行军队列状态
	 * @param autoMarchIdentify
	 */
	public synchronized void resetAutoMarchStatus(int autoMarchIdentify) {
		for (AutoMarchInfo info : autoMarchList) {
			if (info.getId() == autoMarchIdentify) {
				info.setStatus(0);
				break;
			}
		}
	}
	
	/**
	 * 选择可出征的自动打野行军队列
	 * 
	 * @return
	 */
	public synchronized AutoMarchInfo getAutoMarchByPriority() {
		AutoMarchInfo autoMarchInfo = null;
		// 根据优先级选择出征队列
		for (AutoMarchInfo info : autoMarchList) {
			if (info.getStatus() > 0) {
				continue;
			}
			
			if (autoMarchInfo == null || info.getPriority() > autoMarchInfo.getPriority()) {
				autoMarchInfo = info;
			}
		}
		
		return autoMarchInfo;
	}
	
	/**
	 * 移除自动打野行军队列
	 * @param autoMarchIdentify
	 */
	public synchronized void removeAutoMarch(int autoMarchIdentify) {
		AutoMarchInfo autoMarch = null;
		for (AutoMarchInfo info : autoMarchList) {
			if (info.getId() == autoMarchIdentify) {
				autoMarch = info;
				break;
			}
		}
		
		if (autoMarch != null) {
			autoMarchList.remove(autoMarch);
		}
	}
	
	/**
	 * 判断自动打野CD是否已结束
	 * 
	 * @return
	 */
	public boolean isAutoMarchCDEnd() {
		return HawkTime.getMillisecond() - openTime >= ConstProperty.getInstance().getAutoAtkMonsterCD();
	}
	
}
