package com.hawk.game.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.hawk.game.item.ItemInfo;
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
	// 消耗时不能使用金币替代
	protected final int cannotConvert;

	// 加属性类型
	protected int attrType;
	// 加属性值
	protected int attrVal;
	// 野怪id
	protected final int worldEnemy;
	// 资源宝库id
	protected final int resTreasure;
	
	// 迷雾宝箱随机事件roundTime
	protected final int roundTime;
	// 迷雾宝箱等级
	protected final int boxLevel;
	// 超级兵四个技能部位
	protected final int partType;
	
	// 国家建筑id
	protected final int nationBuild;
	// 增加建筑值
	protected final int buildValue;
	
	protected final int nationTech;
	
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
	
	private final int tblyUse;

	// 英雄卡独有
	private final String resolveList;
	private final int heroExp;
	private final int skillExp; //英雄技能经验
	
	// 超级那啥
	private final String superLabCompose;//="30000_1480001_1" 
	private final String superLabDecompose;//="30000_1480001_1"
	
	private final int addGiftLevelExp;//="60" 大礼包等级
	private final int addBigGiftExp;//="1"  领取累计
	// 英雄技能
	private final String skillComposeItem;//="30000_13220001_10" 
	private final String skillResolveItem;//="30000_13200001_3" 
	private final int skillGet;//="20001" 
	private final String exchangeItem;//="30000_13200002_1" // 万能碎片兑换
	// 装扮
	private final int dressId;
	//装板能不能赠送
	private final int canGive;
	// 开启迷雾要塞宝箱所需的时间：单位s
	private final String unlockTime;
	// 值为1时，使用后不发使用成功通知，为0则需要发送通知
	private final int useSuccessOpen;
	// 自选奖励宝箱奖励内容
	private final String chooseAward;
	
	private Integer[] unlockTimeRange;
	
	private List<Integer> speedUpTypeList;
	
	private List<ItemInfo> chooseAwardList;
	
	//代金券过期时间
	private final String voucherTime;
	//代金券使用的礼包
	private final String voucherUse;
	//代金券使用额度下限
	private final String voucherLimit;
	
	// 赠送倒计时
	private final int protectionPeriod;

	// 赠送保护期额外消耗道具
	private final int protectionConsume;

	// 英雄档案碎片兑换
	private final String heroArchivesExchange;
	
	// 英雄档案碎片反向兑换
	private final String heroArchivesFullExchange;

	//家园建筑
	private final int buildId;

	private List<ItemInfo> voucherLimitList;
	
	// 英雄档案碎片兑换
	private Map<Integer, ItemInfo> heroArchivesExchangeItem;
	
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
		cannotConvert = 0;
		tblyUse = 0;
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
		resTreasure = 0;
		exchangeItem = "";
		dressId = 0;
		unlockTime = "";
		roundTime = 1800;
		boxLevel = 0;
		useSuccessOpen = 0;
		partType = 0;
		superLabCompose = "";
		superLabDecompose = "";
		chooseAward = "";
		canGive = 0;
		voucherTime = "";
		voucherUse = "";
		voucherLimit = "";
		nationBuild = 0;
		buildValue = 0;
		nationTech = 0;
		protectionPeriod = 0;
		protectionConsume = 3;
		heroArchivesExchange = "";
		heroArchivesFullExchange = "";
		buildId = 0;
	}
	
	public int getBoxLevel() {
		return boxLevel;
	}
	
	/**
	 * 使用后是否发送使用成功通知
	 * @return
	 */
	public boolean isNotify() {
		return useSuccessOpen == 0;
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
		return Integer.MAX_VALUE;
	}

	public int getSellPrice() {
		return sellPrice;
	}
	
	/**
	 * 消耗时能否使用金币替代
	 * @return
	 */
	public boolean consumeCanConvert() {
		return cannotConvert != 1;
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

	public int getNationBuild() {
		return nationBuild;
	}

	public int getBuildValue() {
		return buildValue;
	}

	@Override
	protected boolean assemble() {
		if (itemType == Const.ToolType.ADD_ATTR_VALUE || itemType == Const.ToolType.NATION_RESOURCE_ITEM_VALUE) {
			String[] attr = addAttr.split("_");
			if (attr.length == 2) {
				attrType = Integer.valueOf(attr[0]);
				attrVal = Integer.valueOf(attr[1]);
			}
		}
		
		if (StringUtils.isNotEmpty(speedUpType)) {
			List<Integer> list = Splitter.on(",").omitEmptyStrings().splitToList(speedUpType).stream()
					.map(Integer::valueOf)
					.collect(Collectors.toList());
			speedUpTypeList = ImmutableList.copyOf(list);
		}
		
		if (!HawkOSOperator.isEmptyString(unlockTime)) {
			String[] unlockTimeStr = unlockTime.split("_");
			unlockTimeRange = new Integer[unlockTimeStr.length];
			for (int i = 0; i < unlockTimeStr.length; i++) {
				unlockTimeRange[i] = Integer.valueOf(unlockTimeStr[i]);
			}
		}
		
		if (!HawkOSOperator.isEmptyString(chooseAward)) {
			chooseAwardList = new ArrayList<ItemInfo>();
			String[] awardArray = chooseAward.split(";");
			for (String award : awardArray) {
				chooseAwardList.add(ItemInfo.valueOf(award));
			}
		} else {
			chooseAwardList = Collections.emptyList();
		}
		this.voucherLimitList = ItemInfo.valueListOf(this.voucherLimit);
		
		Map<Integer, ItemInfo> heroArchivesExchangeItem = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(heroArchivesExchange)) {
			String[] exchangeItem = heroArchivesExchange.split(";");
			for (int i = 0; i < exchangeItem.length; i++) {
				ItemInfo item = ItemInfo.valueOf(exchangeItem[i]);
				heroArchivesExchangeItem.put(item.getItemId(), item);
			}
		}
		this.heroArchivesExchangeItem = heroArchivesExchangeItem;
		return true;
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

	public String getExchangeItem() {
		return exchangeItem;
	}

	public static boolean isExistItemId (int itemId) {
		return Objects.nonNull(HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemId));
	}

	public int getDressId() {
		return dressId;
	}

	public int getUnlockTime() {
		int unlockBase = HawkRand.randInt(unlockTimeRange[0], unlockTimeRange[1]);
		return (((unlockBase - 1) / roundTime + 1) * roundTime);
	}

	public int getAddGiftLevelExp() {
		return addGiftLevelExp;
	}

	public int getAddBigGiftExp() {
		return addBigGiftExp;
	}

	public int getPartType() {
		return partType;
	}

	public String getSuperLabCompose() {
		return superLabCompose;
	}

	public String getSuperLabDecompose() {
		return superLabDecompose;
	}

	public String getChooseAward() {
		return chooseAward;
	}
	
	public int getResTreasure() {
		return resTreasure;
	}

	public ItemInfo getChooseAward(int awardIndex) {
		if (chooseAwardList.isEmpty()) {
			return null;
		}
		
		ItemInfo itemInfo = chooseAwardList.get(awardIndex);
		if (itemInfo != null) {
			itemInfo = itemInfo.clone();
		}
		
		return itemInfo;
	}

	public int getCanGive() {
		return canGive;
	}

	public String getVoucherTime() {
		return voucherTime;
	}

	public String getVoucherUse() {
		return voucherUse;
	}

	public String getVoucherLimit() {
		return voucherLimit;
	}
	
	public List<ItemInfo> getVoucherLimitList() {
		return voucherLimitList;
	}

	public void setVoucherLimitList(List<ItemInfo> voucherLimitList) {
		this.voucherLimitList = voucherLimitList;
	}
	//根据类型获取代金券条件数量
	public long getVoucherLimitByType(int type){
		for (ItemInfo itemInfo : this.voucherLimitList) {
			if (itemInfo.getItemId() == type ) {
				return itemInfo.getCount();
			}
		}
		return 0;
	}

	public int getTblyUse() {
		return tblyUse;
	}

	public int getNationTech() {
		return nationTech;
	}

	public long getProtectionPeriod() {
		return protectionPeriod * 1000L;
	}

	public int getProtectionConsume() {
		return protectionConsume;
	}

	public ItemInfo getHeroArchivesExchangeItem(int toItemId) {
		return heroArchivesExchangeItem.get(toItemId);
	}
	
	public ItemInfo getHeroArchivesFullExchange() {
		return ItemInfo.valueOf(heroArchivesFullExchange);
	}

	public int getBuildId() {
		return buildId;
	}
}
