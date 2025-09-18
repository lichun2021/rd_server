package com.hawk.robot.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Const.ToolSpeedUpType;

/**
 * 物品配置
 *
 * @author david
 *
 */
@HawkConfigManager.XmlResource(file = "xml/item.xml")
public class ItemCfg extends HawkConfigBase {
	@Id
	protected final int id;
	// 类型
	protected final int itemType;
	// 大本等级
	protected final int cityLevel;
	// 背包格子上限
	protected final int pile_max;
	// 是否可使用
	protected final int use;
	// 是否可全部使用
	protected final int useAll;
	// 加速类型
	protected final String speedUpType;
	// 加速时间（秒）
	protected final int speedUpTime;
	// 作用号
	protected final int effect;
	// 作用值类型（1：百分比，2：数值）
	protected final int numType;
	// 作用值
	protected final int num;
	// 行军加速倍数
	protected final int marchSpeedMultiple;
	// 状态ID
	protected final int buffId;
	// 奖励ID
	protected final int rewardId;
	// 加属性
	protected final String addAttr;
	// 价格
	protected final int sellPrice;

	// 加属性类型
	protected int attrType;
	// 加属性值
	protected int attrVal;
	// 野怪id
	protected final int worldEnemy;
	
	// 迷雾宝箱随机事件roundTime
	protected final int roundTime;
	// 迷雾宝箱等级
	protected final int boxLevel;
	
	//士兵装备独有 攻
	private final int attack;
	private final int defence;
	private final int hp;
	private final int crit;
	private final int hit;
	private final int percentAttack;
	private final int percentDefence;
	private final int percentCrit;
	private final int percentHit;
	private final int percentHp;
	private final int percentAntiCrit;
	private final int percentMiss;

	// 英雄卡独有
	private final String resolveList;
	private final int heroExp;
	private final int skillExp; //英雄技能经验
	
	private final int addGiftLevelExp;//="60" 大礼包等级
	private final int addBigGiftExp;//="1"  领取累计
	// 英雄技能
	private final String skillComposeItem;//="30000_13220001_10" 
	private final String skillResolveItem;//="30000_13200001_3" 
	private final int skillGet;//="20001" 
	private final int exchangeItem;//="13200002" // 万能碎片兑换
	private List<Integer> speedUpTypeList;

	// 装扮
	private final int dressId;
	// 开启迷雾要塞宝箱所需的时间：单位s
	private final String unlockTime;
	
	// 值为1时，使用后不发使用成功通知，为0则需要发送通知
	private final int useSuccessOpen;
	
	private static Map<Integer, List<ItemCfg>> resItemMap = new HashMap<>();

	public ItemCfg() {
		id = 0;
		skillComposeItem = "";
		skillResolveItem = "";
		skillGet = 0;
		itemType = 0;
		skillExp = 0;
		pile_max = 0;
		cityLevel = 0;
		use = 0;
		useAll = 0;
		speedUpType = "";
		speedUpTime = 0;
		effect = 0;
		numType = 0;
		num = 0;
		marchSpeedMultiple = 0;
		buffId = 0;
		rewardId = 0;
		addAttr = "";
		sellPrice = 0;
		this.attack = 0;
		this.defence = 0;
		this.hp = 0;
		this.crit = 0;
		this.hit = 0;
		this.percentAttack = 0;
		this.percentDefence = 0;
		this.percentCrit = 0;
		this.percentHit = 0;
		this.percentHp = 0;
		this.percentAntiCrit = 0;
		this.percentMiss = 0;
		this.resolveList = "";
		this.heroExp = 0;
		addGiftLevelExp = 0;
		addBigGiftExp = 0;
		worldEnemy = 0;
		exchangeItem = 0;
		dressId = 0;
		unlockTime = "";
		roundTime = 1800;
		boxLevel = 0;
		useSuccessOpen = 0;
	}
	
	public int getBoxLevel() {
		return boxLevel;
	}

	/**
	 * 是否可用于加速
	 * 
	 * @return
	 */
	public boolean speedUpAble(int queueType) {
		if (Objects.isNull(this.speedUpTypeList)) {
			return false;
		}

		if (speedUpTypeList.contains(ToolSpeedUpType.TOOL_SPEED_COMMON_VALUE)) {
			return true;
		}

		return speedUpTypeList.contains(queueType);
	}

	public int getId() {
		return id;
	}

	public int getItemType() {
		return itemType;
	}

	public int getCityLevel() {
		return cityLevel;
	}

	public int getUseAll() {
		return useAll;
	}

	public String getSpeedUpType() {
		return speedUpType;
	}

	public int getSpeedUpTime() {
		return speedUpTime;
	}

	public int getPercentMiss() {
		return percentMiss;
	}

	public int getEffect() {
		return effect;
	}

	public int getNumType() {
		return numType;
	}

	public int getNum() {
		return num;
	}

	public int getMarchSpeedMultiple() {
		return marchSpeedMultiple;
	}

	public int getPercentAntiCrit() {
		return percentAntiCrit;
	}

	public int getBuffId() {
		return buffId;
	}

	public int getRewardId() {
		return rewardId;
	}

	public int getUse() {
		return use;
	}

	public int getAttrType() {
		return attrType;
	}

	public int getAttrVal() {
		return attrVal;
	}

	public int getPile_max() {
		return pile_max;
	}

	public int getSellPrice() {
		return sellPrice;
	}

	public int getAttack() {
		return attack;
	}

	public int getDefence() {
		return defence;
	}

	public int getHp() {
		return hp;
	}

	public int getCrit() {
		return crit;
	}

	public int getHit() {
		return hit;
	}

	public int getPercentAttack() {
		return percentAttack;
	}

	public int getPercentDefence() {
		return percentDefence;
	}

	public int getPercentCrit() {
		return percentCrit;
	}

	public int getPercentHit() {
		return percentHit;
	}

	public int getPercentHp() {
		return percentHp;
	}

	public String getResolveList() {
		return resolveList;
	}

	public int getHeroExp() {
		return heroExp;
	}

	public int getWorldEnemy() {
		return worldEnemy;
	}

	/**
	 * 判断此道具是否为资源类型的道具
	 * @return
	 */
	public boolean isResItem() {
		int type = getAttrType();
		return type == PlayerAttr.GOLDORE_VALUE
				|| type == PlayerAttr.GOLDORE_UNSAFE_VALUE
				|| type == PlayerAttr.OIL_VALUE
				|| type == PlayerAttr.OIL_UNSAFE_VALUE
				|| type == PlayerAttr.STEEL_VALUE
				|| type == PlayerAttr.STEEL_UNSAFE_VALUE
				|| type == PlayerAttr.TOMBARTHITE_VALUE
				|| type == PlayerAttr.TOMBARTHITE_UNSAFE_VALUE;
	}
	
	public int getSkillExp() {
		return skillExp;
	}

	public String getSkillComposeItem() {
		return skillComposeItem;
	}

	public String getSkillResolveItem() {
		return skillResolveItem;
	}

	public int getSkillGet() {
		return skillGet;
	}

	@Override
	protected boolean assemble() {
		if (itemType == Const.ToolType.ADD_ATTR_VALUE) {
			String[] attr = addAttr.split("_");
			if (attr.length == 2) {
				attrType = Integer.valueOf(attr[0]);
				attrVal = Integer.valueOf(attr[1]);
				List<ItemCfg> cfgList = resItemMap.get(attrType);
				if(cfgList == null) {
					cfgList = new LinkedList<>();
					resItemMap.put(attrType, cfgList);
				}
				cfgList.add(this);
			}
		}
		
		if (StringUtils.isNotEmpty(speedUpType)) {
			List<Integer> list = Splitter.on(",").omitEmptyStrings().splitToList(speedUpType).stream()
					.map(Integer::valueOf)
					.collect(Collectors.toList());
			speedUpTypeList = ImmutableList.copyOf(list);
		}
		return true;
	}
	
	public static List<Integer> getItemCfgIdByResType(int resType) {
		List<Integer> cfgIds = new ArrayList<Integer>();
		if(resItemMap.containsKey(resType)) {
			List<ItemCfg> cfgList = resItemMap.get(resType);
			cfgIds = cfgList.stream().map(e -> e.getId()).collect(Collectors.toList());
		}
		return cfgIds;
	}

	public int getExchangeItem() {
		return exchangeItem;
	}

	public int getDressId() {
		return dressId;
	}

	public String getUnlockTime() {
		return unlockTime;
	}

	public int getAddGiftLevelExp() {
		return addGiftLevelExp;
	}

	public int getAddBigGiftExp() {
		return addBigGiftExp;
	}

	public int getUseSuccessOpen() {
		return useSuccessOpen;
	}
}
