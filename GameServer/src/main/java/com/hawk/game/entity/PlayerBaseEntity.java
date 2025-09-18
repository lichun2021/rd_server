package com.hawk.game.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.GsConfig;

/**
 * 玩家基础数据
 *
 * @author hawk
 *
 */
@Entity
@Table(name = "player_base")
public class PlayerBaseEntity extends HawkDBEntity {
	@Id
	@Column(name = "playerId", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String playerId = null;

	@Column(name = "level")
    @IndexProp(id = 2)
	private int level;

	@Column(name = "exp")
    @IndexProp(id = 3)
	private int exp;
	
	@Column(name = "expDec")
    @IndexProp(id = 4)
	private int expDec;

	@Column(name = "gold")
    @IndexProp(id = 5)
	private int gold = 0;

	@Column(name = "coin")
    @IndexProp(id = 6)
	private int coin = 0;

	// 金矿
	@Column(name = "goldore")
    @IndexProp(id = 7)
	private long goldore = 0;

	// 石油
	@Column(name = "oil")
    @IndexProp(id = 8)
	private long oil = 0;

	// 铀矿
	@Column(name = "steel")
    @IndexProp(id = 9)
	private long steel = 0;

	// 铀矿
	@Column(name = "steelUnsafe")
    @IndexProp(id = 10)
	private long steelUnsafe = 0;
	
	// 合金
	@Column(name = "tombarthite")
    @IndexProp(id = 11)
	private long tombarthite = 0;
	
	// 联盟贡献
	@Column(name = "guildContribution")
    @IndexProp(id = 12)
	private long guildContribution = 0;
	
	@Column(name = "recharge")
    @IndexProp(id = 13)
	private int recharge = 0;

	// 托管第三方货币余额
	@Column(name = "diamonds")
    @IndexProp(id = 14)
	private int diamonds = 0;

	// 第三方历史充值总额
	@Column(name = "saveAmt")
    @IndexProp(id = 15)
	private int saveAmt = 0;

	// 记账数额
	@Column(name = "chargeAmt")
    @IndexProp(id = 16)
	private int chargeAmt = 0;
		
	// 金矿非受保护
	@Column(name = "goldoreUnsafe")
    @IndexProp(id = 17)
	private long goldoreUnsafe = 0;
	// 石油非受保护
	@Column(name = "oilUnsafe")
    @IndexProp(id = 18)
	private long oilUnsafe = 0;
	// 合金非受保护
	@Column(name = "tombarthiteUnsafe")
    @IndexProp(id = 19)
	private long tombarthiteUnsafe = 0;

	// 已解锁的区块
	@Column(name = "unlockedArea")
    @IndexProp(id = 20)
	private String unlockedArea = "";

	// 玩家城墙着火状态结束时刻
	@Column(name = "onFireEndTime")
    @IndexProp(id = 21)
	private long onFireEndTime = 0;

	// 可修复城防的时刻
	@Column(name = "cityDefRepairTime")
    @IndexProp(id = 22)
	private long cityDefRepairTime = 0;

	// 上一次消耗城防值的时刻
	@Column(name = "cityDefConsumeTime")
    @IndexProp(id = 23)
	private long cityDefConsumeTime = 0;

	// 玩家实际城防值
	@Column(name = "cityDefVal")
    @IndexProp(id = 24)
	private int cityDefVal = 0;

	// 战争狂热结束时间
	@Column(name = "warFeverEndTime")
    @IndexProp(id = 25)
	private long warFeverEndTime = 0;
	
	// 记录创建时间
	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 26)
	private long createTime = 0;

	// 最后一次更新时间
	@Column(name = "updateTime")
    @IndexProp(id = 27)
	private long updateTime;

	// 记录是否有效
	@Column(name = "invalid")
    @IndexProp(id = 28)
	private boolean invalid;
	
	/**
	 *记录玩家完成的一些标志
	 */
	@Column(name="flag")
    @IndexProp(id = 29)
	private int flag;
	
	// 联盟军演积分
	@Column(name = "guildMilitaryScore")
	@IndexProp(id = 30)
	private long guildMilitaryScore = 0;
	
	// 赛博之战积分
	@Column(name = "cyborgScore")
	@IndexProp(id = 31)
	private long cyborgScore;
	
	// 达雅之战积分
	@Column(name = "dyzzScore")
	@IndexProp(id = 32)
	private long dyzzScore;
	
	// 该player历史充值额度对应的金条数（区别于saveAmt, 考虑到转平台的影响，saveAmt不能用来表示该player历史充值额度了）
	// 默认值-1表示该玩家还没有转过平台（仅首次使用，不管是否转过平台，都将设置为真实的值）
	@Column(name = "saveAmtTotal")
	@IndexProp(id = 33)
	private int saveAmtTotal = -1;
	
	//玩家充值总额（统一按金条数累计，直购礼包购买按RechargeEntity中的payMoney累计）
	@Column(name = "rechargeTotal")
	@IndexProp(id = 34)
	private int rechargeTotal = -1;
	
	//指挥官等级升级时间
	@Column(name = "levelUpTime")
	@IndexProp(id = 35)
	private long levelUpTime;
	
	
	@Transient
	private Set<Integer> unlockedAreaSet = new HashSet<>();
	
	public PlayerBaseEntity() {
		asyncLandPeriod(GsConfig.getInstance().getEntitySyncPeriod());
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getExp() {
		return exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}
	
	public int getExpDec() {
		return expDec;
	}

	public void setExpDec(int expDec) {
		this.expDec = expDec;
	}

	public int getGold() {
		return gold;
	}

	public void setGold(int gold) {
		this.gold = gold;
	}

	public long getSteelUnsafe() {
		return steelUnsafe;
	}

	public void setSteelUnsafe(long steelUnsafe) {
		this.steelUnsafe = steelUnsafe;
	}

	public int getCoin() {
		return coin;
	}

	public void setCoin(int coin) {
		this.coin = coin;
	}

	public long getGoldore() {
		return goldore;
	}

	public void setGoldore(long goldore) {
		this.goldore = goldore;
	}

	public long getOil() {
		return oil;
	}

	public void setOil(long oil) {
		this.oil = oil;
	}

	public long getSteel() {
		return steel;
	}

	public void setSteel(long steel) {
		this.steel = steel;
	}

	public long getTombarthite() {
		return tombarthite;
	}

	public void setTombarthite(long tombarthite) {
		this.tombarthite = tombarthite;
	}

	public long getGuildContribution() {
		return guildContribution;
	}

	public void setGuildContribution(long guildContribution) {
		this.guildContribution = guildContribution;
	}

	public int getRecharge() {
		return recharge;
	}

	public int getDiamonds() {
		return diamonds;
	}

	public void setDiamonds(int diamonds) {
		this.diamonds = diamonds;
	}

	public void setRecharge(int recharge) {
		this.recharge = recharge;
	}

	public int getSaveAmt() {
		return saveAmt;
	}

	public void setSaveAmt(int saveAmt) {
		this.saveAmt = saveAmt;
	}
	
	public int getSaveAmtTotal() {
		return saveAmtTotal;
	}

	public void setSaveAmtTotal(int saveAmtTotal) {
		this.saveAmtTotal = saveAmtTotal;
	}

	/**
	 * 此字段是给特定用途使用的，游戏内逻辑不要去使用它
	 * @return
	 */
	public int _getChargeAmt() {
		return chargeAmt;
	}

	/**
	 * 此字段是给特定用途使用的，游戏内逻辑不要去使用它
	 * @return
	 */
	public void _setChargeAmt(int chargeAmt) {
		this.chargeAmt = chargeAmt;
	}

	public String getUnlockedArea() {
		return unlockedArea;
	}

	public void setUnlockedArea(String unlockedArea) {
		this.unlockedArea = unlockedArea;
	}

	public long getOnFireEndTime() {
		return onFireEndTime;
	}

	public void setOnFireEndTime(long onFireEndTime) {
		this.onFireEndTime = onFireEndTime;
	}

	public long getCityDefNextRepairTime() {
		return cityDefRepairTime;
	}

	public void setCityDefNextRepairTime(long cityDefRepairTime) {
		this.cityDefRepairTime = cityDefRepairTime;
	}

	public long getCityDefConsumeTime() {
		return cityDefConsumeTime;
	}

	public void setCityDefConsumeTime(long cityDefConsumeTime) {
		this.cityDefConsumeTime = cityDefConsumeTime;
	}

	public int getCityDefVal() {
		return cityDefVal;
	}

	public void setCityDefVal(int cityDefVal) {
		this.cityDefVal = cityDefVal;
	}
	
	public long getGoldoreUnsafe() {
		return goldoreUnsafe;
	}

	public void setGoldoreUnsafe(long goldoreUnsafe) {
		this.goldoreUnsafe = goldoreUnsafe;
	}

	public long getOilUnsafe() {
		return oilUnsafe;
	}

	public void setOilUnsafe(long oilUnsafe) {
		this.oilUnsafe = oilUnsafe;
	}

	public long getTombarthiteUnsafe() {
		return tombarthiteUnsafe;
	}

	public void setTombarthiteUnsafe(long tombarthiteUnsafe) {
		this.tombarthiteUnsafe = tombarthiteUnsafe;
	}
	
	public void addUnlockedArea(int areaId) {
		if (!unlockedAreaSet.contains(areaId)) {
			unlockedAreaSet.add(areaId);
			if (unlockedAreaSet.size() > 1) {
				setUnlockedArea(getUnlockedArea() + "," + areaId);
			} else {
				setUnlockedArea(String.valueOf(areaId));
			}
		}
	}

	public Set<Integer> getUnlockedAreaSet() {
		if (unlockedAreaSet.isEmpty()) {
			assemble();
		}
		
		return unlockedAreaSet;
	}
	
	@Override
	public void afterRead() {
		assemble();
	}
	
	public void assemble() {
		if (unlockedAreaSet.size() > 0 || HawkOSOperator.isEmptyString(unlockedArea)) {
			return;
		}

		String[] areas = unlockedArea.split(",");
		for (String area : areas) {
			unlockedAreaSet.add(Integer.valueOf(area));
		}
	}

	@Override
	public void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	@Override
	public long getCreateTime() {
		return createTime;
	}

	@Override
	protected void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	@Override
	public long getUpdateTime() {
		return updateTime;
	}

	@Override
	protected void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	@Override
	public boolean isInvalid() {
		return invalid;
	}
	
	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	public long getWarFeverEndTime() {
		return warFeverEndTime;
	}

	public void setWarFeverEndTime(long warFeverEndTime) {
		this.warFeverEndTime = warFeverEndTime;
	}
	
	public long getGuildMilitaryScore() {
		return guildMilitaryScore;
	}

	public void setGuildMilitaryScore(long guildMilitaryScore) {
		this.guildMilitaryScore = guildMilitaryScore;
	}

	public long getCyborgScore() {
		return cyborgScore;
	}

	public void setCyborgScore(long cyborgScore) {
		this.cyborgScore = cyborgScore;
	}

	
	public long getDyzzScore() {
		return dyzzScore;
	}

	public void setDyzzScore(long dyzzScore) {
		this.dyzzScore = dyzzScore;
	}

	@Override
	public String getPrimaryKey() {
		return playerId;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		throw new UnsupportedOperationException("player base entity primaryKey is playerId");
	}
	
	public String getOwnerKey() {
		return playerId;
	}
	
	public int getRechargeTotal() {
		return rechargeTotal;
	}

	public void setRechargeTotal(int rechargeTotal) {
		this.rechargeTotal = rechargeTotal;
	}
	
	public void rechargeTotalAdd(int add) {
		setRechargeTotal(this.rechargeTotal + add);
	}
	
	public long getLevelUpTime() {
		return levelUpTime;
	}

	public void setLevelUpTime(long levelUpTime) {
		this.levelUpTime = levelUpTime;
	}
}
