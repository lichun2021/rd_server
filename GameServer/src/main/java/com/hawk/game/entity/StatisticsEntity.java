package com.hawk.game.entity;

import org.hawk.annotation.IndexProp;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkOSOperator;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.WorldMarchConstProperty;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst.StatisticDataType;

/**
 * 玩家统计数据
 *
 * @author
 *
 */
@Entity
@Table(name = "statistics")
public class StatisticsEntity extends HawkDBEntity {
	@Id
	@Column(name = "playerId", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String playerId = null;

	@Column(name = "loginCnt")
    @IndexProp(id = 2)
	private int loginCnt;

	@Column(name = "loginDay")
    @IndexProp(id = 3)
	private int loginDay;

	@Column(name = "warWinCnt")
    @IndexProp(id = 4)
	private int warWinCnt;

	@Column(name = "warLoseCnt")
    @IndexProp(id = 5)
	private int warLoseCnt;

	@Column(name = "atkWinCnt")
    @IndexProp(id = 6)
	private int atkWinCnt;

	@Column(name = "atkLoseCnt")
    @IndexProp(id = 7)
	private int atkLoseCnt;

	@Column(name = "atkInProtectCnt")
    @IndexProp(id = 8)
	private int atkInProtectCnt;

	@Column(name = "defWinCnt")
    @IndexProp(id = 9)
	private int defWinCnt;

	@Column(name = "defLoseCnt")
    @IndexProp(id = 10)
	private int defLoseCnt;

	@Column(name = "spyCnt")
    @IndexProp(id = 11)
	private int spyCnt;

	@Column(name = "atkMonsterCnt")
    @IndexProp(id = 12)
	private int atkMonsterCnt;

	@Column(name = "atkMonsterWinCnt")
    @IndexProp(id = 13)
	private int atkMonsterWinCnt;

	@Column(name = "armyAddCnt")
    @IndexProp(id = 14)
	private long armyAddCnt;

	@Column(name = "armyKillCnt")
    @IndexProp(id = 15)
	private long armyKillCnt;

	@Column(name = "armyLoseCnt")
    @IndexProp(id = 16)
	private long armyLoseCnt;

	@Column(name = "armyCureCnt")
    @IndexProp(id = 17)
	private long armyCureCnt;

	@Column(name = "joinGuildCnt")
    @IndexProp(id = 18)
	private int joinGuildCnt;

	// 战败被打飞
	@Column(name = "loseFightCnt")
    @IndexProp(id = 19)
	private int loseFightCnt;

	// 是否被打
	@Column(name = "isBeating")
    @IndexProp(id = 20)
	private int isBeating;

	// cdk类型
	@Column(name = "cdkType")
    @IndexProp(id = 21)
	private String cdkType;
	/**
	 * 玩家迁城时间记录
	 */
	@Column(name = "cityMoveRecord")
    @IndexProp(id = 22)
	private String cityMoveRecord;
	
	/**
	 * 其它统计数据
	 */
	@Column(name = "commonStatisData")
    @IndexProp(id = 23)
	private String commonStatisData;

	// 记录创建时间
	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 24)
	private long createTime = 0;

	// 最后一次更新时间
	@Column(name = "updateTime")
    @IndexProp(id = 25)
	private long updateTime;

	// 记录是否有效
	@Column(name = "invalid")
    @IndexProp(id = 26)
	private boolean invalid;

	@Transient
	private Map<Long, Integer> cityMoveTimeMap = new ConcurrentHashMap<>();
	
	// 其它统计数据
	@Transient
	private Map<Integer, AtomicLong> commonStatisDataMap = new ConcurrentHashMap<>();

	public StatisticsEntity() {
		
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String puid) {
		this.playerId = puid;
	}

	public int getLoginCnt() {
		return loginCnt;
	}

	public void setLoginCnt(int loginCnt) {
		this.loginCnt = loginCnt;
	}

	public int getLoginDay() {
		return loginDay;
	}

	public void setLoginDay(int loginDay) {
		this.loginDay = loginDay;
	}

	public int getWarWinCnt() {
		return warWinCnt;
	}

	public void setWarWinCnt(int warWinCnt) {
		this.warWinCnt = warWinCnt;
	}

	public int getWarLoseCnt() {
		return warLoseCnt;
	}

	public void setWarLoseCnt(int warLoseCnt) {
		this.warLoseCnt = warLoseCnt;
	}

	public int getAtkWinCnt() {
		return atkWinCnt;
	}

	public void setAtkWinCnt(int atkWinCnt) {
		this.atkWinCnt = atkWinCnt;
	}

	public int getAtkLoseCnt() {
		return atkLoseCnt;
	}

	public void setAtkLoseCnt(int atkLoseCnt) {
		this.atkLoseCnt = atkLoseCnt;
	}

	public int getAtkInProtectCnt() {
		return atkInProtectCnt;
	}

	public void setAtkInProtectCnt(int atkInProtectCnt) {
		this.atkInProtectCnt = atkInProtectCnt;
	}

	public int getDefWinCnt() {
		return defWinCnt;
	}

	public void setDefWinCnt(int defWinCnt) {
		this.defWinCnt = defWinCnt;
	}

	public int getDefLoseCnt() {
		return defLoseCnt;
	}

	public void setDefLoseCnt(int defLoseCnt) {
		this.defLoseCnt = defLoseCnt;
	}

	public int getSpyCnt() {
		return spyCnt;
	}

	public void setSpyCnt(int spyCnt) {
		this.spyCnt = spyCnt;
	}

	public int getAtkMonsterCnt() {
		return atkMonsterCnt;
	}

	public void setAtkMonsterCnt(int atkMonsterCnt) {
		this.atkMonsterCnt = atkMonsterCnt;
	}

	public int getAtkMonsterWinCnt() {
		return atkMonsterWinCnt;
	}

	public void setAtkMonsterWinCnt(int atkMonsterWinCnt) {
		this.atkMonsterWinCnt = atkMonsterWinCnt;
	}

	public long getArmyAddCnt() {
		return armyAddCnt;
	}

	public void setArmyAddCnt(long armyAddCnt) {
		this.armyAddCnt = armyAddCnt;
	}

	public long getArmyKillCnt() {
		return armyKillCnt;
	}

	public void setArmyKillCnt(long armyKillCnt) {
		this.armyKillCnt = armyKillCnt;
	}

	public long getArmyLoseCnt() {
		return armyLoseCnt;
	}

	public void setArmyLoseCnt(long armyLoseCnt) {
		this.armyLoseCnt = armyLoseCnt;
	}

	public long getArmyCureCnt() {
		return armyCureCnt;
	}

	public void setArmyCureCnt(long armyCureCnt) {
		this.armyCureCnt = armyCureCnt;
	}

	public int getJoinGuildCnt() {
		return joinGuildCnt;
	}

	public void setJoinGuildCnt(int joinGuildCnt) {
		this.joinGuildCnt = joinGuildCnt;
	}

	public long getLoseFightCnt() {
		return loseFightCnt;
	}

	public void setLoseFightCnt(int loseFightCnt) {
		this.loseFightCnt = loseFightCnt;
	}

	public int getIsBeating() {
		return isBeating;
	}

	public void setIsBeating(int isBeating) {
		this.isBeating = isBeating;
	}

	/**
	 *CDK类型
	 * @return
	 */
	public String getCdkType() {
		return cdkType;
	}

	/**
	 *CDK类型
	 * @return
	 */
	public void setCdkType(String cdkType) {
		this.cdkType = cdkType;
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

	@Override
	public void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	public void addLoginCnt(int loginCnt) {
		setLoginCnt(this.loginCnt + loginCnt);
	}

	public void addLoginDay(int loginDay) {
		setLoginDay(this.loginDay + loginDay);
	}

	public void addWarWinCnt(int warWinCnt) {
		setWarWinCnt(this.warWinCnt + warWinCnt);
	}

	public void addWarLoseCnt(int warLoseCnt) {
		setWarLoseCnt(this.warLoseCnt + warLoseCnt);
	}

	public void addAtkWinCnt(int atkWinCnt) {
		setAtkWinCnt(this.atkWinCnt + atkWinCnt);
	}

	public void addAtkLoseCnt(int atkLoseCnt) {
		setAtkLoseCnt(this.atkLoseCnt + atkLoseCnt);
	}

	public void addAtkInProtectCnt(int cnt) {
		setAtkInProtectCnt(this.atkInProtectCnt + cnt);
	}

	public void addDefWinCnt(int defWinCnt) {
		setDefWinCnt(this.defWinCnt + defWinCnt);
	}

	public void addDefLoseCnt(int defLoseCnt) {
		setDefLoseCnt(this.defLoseCnt + defLoseCnt);
	}

	public void addSpyCnt(int spyCnt) {
		setSpyCnt(this.spyCnt + spyCnt);
	}

	public void addAtkMonsterCnt(int atkMonsterCnt) {
		setAtkMonsterCnt(this.atkMonsterCnt + atkMonsterCnt);
		addCommonStatisData(StatisticDataType.PVE_TOTAL_TODAY, 1);
	}

	public void addAtkMonsterWinCnt(int atkMonsterWinCnt) {
		setAtkMonsterWinCnt(this.atkMonsterWinCnt + atkMonsterWinCnt);
	}

	public void addArmyAddCnt(long armyAddCnt) {
		setArmyAddCnt(this.armyAddCnt + armyAddCnt);
	}

	public void addArmyKillCnt(long armyKillCnt) {
		setArmyKillCnt(this.armyKillCnt + armyKillCnt);
	}

	public void addArmyLoseCnt(long armyLoseCnt) {
		setArmyLoseCnt(this.armyLoseCnt + armyLoseCnt);
	}

	public void addArmyCureCnt(long armyCureCnt) {
		setArmyCureCnt(this.armyCureCnt + armyCureCnt);
	}

	public void addJoinGuildCnt(int joinGuildCnt) {
		setJoinGuildCnt(this.joinGuildCnt + joinGuildCnt);
	}

	public void addLoseFightTimes() {
		this.setLoseFightCnt(GameUtil.addValue(
				loseFightCnt,
				WorldMarchConstProperty.getInstance().getDaysOfDefeated(),
				WorldMarchConstProperty.getInstance().getNumsOfDefeated()));
	}

	public long getLoseFightTimes() {
		return GameUtil.getValue(
				loseFightCnt,
				WorldMarchConstProperty.getInstance().getDaysOfDefeated());
	}

	public void addCityMoveRecord(int moveType, long time) {
		int recordLimitNum = ConstProperty.getInstance().getCityMoveRecordLimit();
		if (recordLimitNum > 0 && cityMoveTimeMap.size() >= recordLimitNum) {
			Entry<Long, Integer> oldest = null;
			for (Entry<Long, Integer> entry : cityMoveTimeMap.entrySet()) {
				if (oldest == null) {
					oldest = entry;
					continue;
				}
				if (entry.getKey() < oldest.getKey()) {
					oldest = entry;
				}
			}
			cityMoveTimeMap.remove(oldest.getKey());
		}
		cityMoveTimeMap.put(time, moveType);
		notifyUpdate();
	}
	
	public void addCommonStatisData(StatisticDataType dataType, long value) {
		AtomicLong oldValue = commonStatisDataMap.get(dataType.intVal());
		if (oldValue == null) {
			commonStatisDataMap.putIfAbsent(dataType.intVal(), new AtomicLong(0));
			oldValue = commonStatisDataMap.get(dataType.intVal());
		}
		oldValue.addAndGet(value);
		notifyUpdate();
	}
	
	public long getStatisData(StatisticDataType dataType) {
		AtomicLong oldValue = commonStatisDataMap.get(dataType.intVal());
		if (oldValue == null) {
			return 0;
		}

		return oldValue.get();
	}
	
	public void setCommonStatisData(StatisticDataType dataType, long value) {
		AtomicLong oldValue = commonStatisDataMap.get(dataType.intVal());
		if (oldValue == null) {
			commonStatisDataMap.putIfAbsent(dataType.intVal(), new AtomicLong(0));
			oldValue = commonStatisDataMap.get(dataType.intVal());
		}
		oldValue.set(value);
		notifyUpdate();
	}

	public Map<Long, Integer> getCityMoveRecord() {
		return cityMoveTimeMap;
	}

	@Override
	public void beforeWrite() {
		StringBuilder builder = new StringBuilder();
		int index = 0;
		for (Entry<Long, Integer> entry : cityMoveTimeMap.entrySet()) {
			index++;
			builder.append(entry.getKey()).append("_").append(entry.getValue());
			if (index < cityMoveTimeMap.size()) {
				builder.append("|");
			}
		}
		this.cityMoveRecord = builder.toString();
		
		StringBuilder builder1 = new StringBuilder();
		int index1 = 0;
		for (Entry<Integer, AtomicLong> entry : commonStatisDataMap.entrySet()) {
			index1++;
			builder1.append(entry.getKey()).append("_").append(entry.getValue().get());
			if (index1 < commonStatisDataMap.size()) {
				builder1.append("|");
			}
		}
		this.commonStatisData = builder1.toString();
	}

	@Override
	public void afterRead() {
		cityMoveTimeMap = new ConcurrentHashMap<>();
		
		String[] split = this.cityMoveRecord.split("\\|");
		for (String entry : split) {
			if (HawkOSOperator.isEmptyString(entry)) {
				continue;
			}
			String[] split2 = entry.split("_");
			Long key = Long.valueOf(split2[0]);
			Integer value = Integer.valueOf(split2[1]);
			this.cityMoveTimeMap.put(key, value);
		}
		
		commonStatisDataMap = new ConcurrentHashMap<>();
		
		String[] split1 = this.commonStatisData.split("\\|");
		for (String entry : split1) {
			if (HawkOSOperator.isEmptyString(entry)) {
				continue;
			}
			String[] split2 = entry.split("_");
			Integer key = Integer.valueOf(split2[0]);
			AtomicLong value = new AtomicLong(Long.valueOf(split2[1]));
			this.commonStatisDataMap.put(key, value);
		}
	}
	
	@Override
	public String getPrimaryKey() {
		return playerId;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		playerId = primaryKey;
	}
	
	public String getOwnerKey() {
		return playerId;
	}
}
