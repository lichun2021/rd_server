package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * 战斗单元信息配置
 *
 * @author Link
 *
 */
@HawkConfigManager.XmlResource(file = "xml/battle_soldier.xml")
public class BattleSoldierCfg extends HawkConfigBase {
	@Id
	protected final int id;
	// 兵种类型
	protected final int type;
	// 士兵等级
	protected final int level;
	// 占位优先级
	protected final int posPrior;
	// 几回合攻击一次 放大100倍 50/100/200/300
	protected final int attackRound;
	// 受攻击有限级
	protected final int weight;
	// 攻击半径
	protected final int attackDis;
	// 一回合可移动步数，大于等于0的整数，0表示不可移动
	protected final int moveStep;
	// 训练时间
	protected final int time;
	// 训练消耗资源
	protected final String res;
	// 泰能进化消耗资源
	protected final String plantSoldierRes;
	// 治疗消耗资源
	protected final int recoverRes;
	// 超时空急救站急救消耗资源
	protected final int superTimeRes;
	// 优先攻击类型
	protected final String firstTypes;
	// 负重
	protected final int load;
	// 兵种行军速度
	private final int speed;
	// 治疗时间
	protected final double recoverTime;
	// 战力
	protected final float power;
	// 兵营的建筑类型
	protected final int building;
	// 士兵石油消耗
	protected final int consume;
	protected final String promotion;// "100602_100603_100604_100605"
	// 泰能兵, 不能制造只能通过太能进化所来
	protected final int plantSoldier;
	// 从哪个兵进化而来
	protected final int advanceArmy;
	//太能兵治疗消耗
	protected final String plantSoldierHealRes;
	//泰能兵治疗时间需要
	protected final int plantSoldierHealTime;
	

	private List<ItemInfo> resList;
	private List<SoldierType> firstTypeList;
	private List<Integer> promotionList;

	private SoldierType soldierType;

	public BattleSoldierCfg() {
		id = 0;
		type = 0;
		level = 0;
		posPrior = 0;
		attackRound = 0;
		attackDis = 0;
		moveStep = 0;
		firstTypes = "";
		building = 0;
		load = 0;
		speed = 0;
		power = 0;
		time = 0;
		res = "";
		plantSoldierRes = "";
		recoverTime = 0d;
		recoverRes = 100;
		superTimeRes = 100;
		consume = 0;
		promotion = "";
		weight = 10;
		plantSoldier = 0;
		advanceArmy = 0;
		plantSoldierHealRes = "";
		plantSoldierHealTime = 0;
	}

	@Override
	protected boolean checkValid() {
		return super.checkValid();
	}

	public int getId() {
		return id;
	}

	public int getLevel() {
		return level;
	}

	public int getPosPrior() {
		return posPrior;
	}

	public int getAttackRound() {
		return attackRound;
	}

	public int getAttackDis() {
		return attackDis;
	}

	public int getMoveStep() {
		return moveStep;
	}

	public List<SoldierType> getFirstTypes() {
		return firstTypeList;
	}

	public int getType() {
		return type;
	}

	/**
	 * 防御武器类型
	 */
	public boolean isDefWeapon() {
		return type == SoldierType.WEAPON_ANTI_TANK_103_VALUE || type == SoldierType.WEAPON_ACKACK_102_VALUE || type == SoldierType.WEAPON_LANDMINE_101_VALUE;
	}

	public int getBuilding() {
		return building;
	}

	public int getLoad() {
		return load;
	}

	public float getPower() {
		return power;
	}

	public int getTime() {
		return time;
	}

	public String getRes() {
		return res;
	}

	public String getPlantSoldierRes() {
		return plantSoldierRes;
	}

	public double getRecoverTime() {
		return recoverTime / 1000d * time;
	}

	public int getRecoverRes(boolean firstAid) {
		if (firstAid) {
			return superTimeRes;
		}

		return recoverRes;
	}

	public double getConsume() {
		return consume / 100d;
	}

	public List<ItemInfo> getResList() {
		return resList.stream().map(ItemInfo::clone).collect(Collectors.toList());
	}

	public int getSpeed() {
		return speed;
	}

	@Override
	protected boolean assemble() {
		this.soldierType = SoldierType.valueOf(type);
		this.resList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(res)) {
			String[] resArray = res.split(",");
			for (String resInfo : resArray) {
				ItemInfo itemInfo = ItemInfo.valueOf(resInfo);
				if (itemInfo != null) {
					resList.add(itemInfo);
				}
			}
		}

		firstTypeList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(firstTypes)) {
			String[] typeArray = firstTypes.split("_");
			for (String typeStr : typeArray) {
				firstTypeList.add(SoldierType.valueOf(Integer.valueOf(typeStr)));
			}
		}

		// 晋升
		List<Integer> proArrList = Splitter.on("_").omitEmptyStrings().splitToList(promotion).stream().map(Integer::valueOf).collect(Collectors.toList());
		promotionList = ImmutableList.copyOf(proArrList);

		return true;
	}

	public SoldierType getSoldierType() {
		return soldierType;
	}

	public List<Integer> getPromotionList() {
		return promotionList;
	}

	public void setPromotionList(List<Integer> promotionList) {
		this.promotionList = promotionList;
	}

	public String getPromotion() {
		return promotion;
	}

	public int getWeight() {
		return weight;
	}

	public static boolean isCfgExist(int itemId) {
		return Objects.nonNull(HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, itemId));
	}

	/** 泰能兵, 不能制造只能通过太能进化所来*/
	public boolean isPlantSoldier() {
		return plantSoldier > 0;
	}

	public int getAdvanceArmy() {
		return advanceArmy;
	}

	public List<SoldierType> getFirstTypeList() {
		return firstTypeList;
	}

	public String getPlantSoldierHealRes() {
		return plantSoldierHealRes;
	}

	public int getPlantSoldierHealTime() {
		return plantSoldierHealTime;
	}
	
	

}
