package com.hawk.activity.type.impl.timeLimitBuy.entity;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.AchieveActivityEntity;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 限时抢购
 * @author Golden
 *
 */
@Entity
@Table(name="activity_time_limit_buy")
public class TimeLimitBuyEntity extends AchieveActivityEntity implements IActivityDataEntity{
	
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
    @IndexProp(id = 1)
	@Column(name = "id", unique = true, nullable = false)
	private String id;
	
    @IndexProp(id = 2)
	@Column(name = "playerId", nullable = false)
	private String playerId = null;
	
    @IndexProp(id = 3)
	@Column(name = "termId", nullable = false)
	private int termId;
	
	/**
	 * 已购买
	 */
    @IndexProp(id = 4)
	@Column(name = "buyStr", nullable = false)
	private String buyStr;
	
	/**
	 * 提醒0开启状态1关闭状态
	 */
    @IndexProp(id = 5)
	@Column(name = "closeRemind", nullable = false)
	private int closeRemind;
	
    @IndexProp(id = 6)
	@Column(name = "createTime", nullable = false)
	private long createTime;
	
    @IndexProp(id = 7)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;
	
    @IndexProp(id = 8)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	public TimeLimitBuyEntity() {
		
	}
	
	public TimeLimitBuyEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
	}

	/**
	 * 已购买集合
	 */
	@Transient
	private Map<Integer, Integer> buyInfo = new HashMap<>();
	
	@Override
	public String getPrimaryKey() {
		return id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
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
		return this.invalid;
	}

	@Override
	protected void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}
	
	/**
	 * 获取购买次数
	 * @param goodsId
	 * @return
	 */
	public int getBuyTimes(int goodsId) {
		if (!buyInfo.containsKey(goodsId)) {
			return 0;
		}
		return buyInfo.get(goodsId);
	}
	
	/**
	 * 添加已购买
	 * @param goodsId
	 */
	public void addBuy(int goodsId) {
		if (!buyInfo.containsKey(goodsId)) {
			buyInfo.put(goodsId, 1);
		} else {
			int beforeTimes = buyInfo.get(goodsId);
			buyInfo.put(goodsId, beforeTimes + 1);
		}
		
		notifyUpdate();
	}

	public int getCloseRemind() {
		return closeRemind;
	}

	public void setCloseRemind(int closeRemind) {
		this.closeRemind = closeRemind;
	}

	@Override
	public void beforeWrite() {
		buyStr = SerializeHelper.mapToString(buyInfo);
	}
	
	@Override
	public void afterRead() {
		buyInfo = SerializeHelper.stringToMap(buyStr);
	}
}
