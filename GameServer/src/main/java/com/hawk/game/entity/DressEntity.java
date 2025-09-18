package com.hawk.game.entity;

import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkTime;

import com.hawk.game.entity.item.DressItem;
import com.hawk.game.protocol.Dress.DressType;
import com.hawk.game.util.GsConst;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 装扮
 * @author golden
 *
 */
@Entity
@javax.persistence.Table(name = "dress")
public class DressEntity extends HawkDBEntity {
	@Id
	@Column(name = "playerId", unique = true, nullable = false)
	@IndexProp(id = 1)
	private String playerId = "";
	
	/**
	 * 装扮信息
	 */
	@Column(name = "dressInfo", nullable = false)
	@IndexProp(id = 2)
	private String dressInfo;
	
	@Column(name = "createTime", nullable = false)
	@IndexProp(id = 3)
	protected long createTime = 0;

	@Column(name = "updateTime")
	@IndexProp(id = 4)
	protected long updateTime = 0;

	@Column(name = "invalid")
	@IndexProp(id = 5)
	protected boolean invalid;
	
	@Transient
	BlockingDeque<DressItem> dressDeque = new LinkedBlockingDeque<>();

	public DressEntity() {
		
	}
	
	public DressEntity(String playerId) {
		this.playerId = playerId;
	}
	
	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	public boolean isInvalid() {
		return invalid;
	}

	public void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	@Override
	public void beforeWrite() {
		dressInfo = SerializeHelper.collectionToString(dressDeque, SerializeHelper.ELEMENT_DELIMITER, SerializeHelper.ATTRIBUTE_SPLIT);
	}

	@Override
	public void afterRead() {
		dressDeque = new LinkedBlockingDeque<>();
		List<DressItem> list = SerializeHelper.stringToList(DressItem.class, dressInfo);
		for (DressItem item : list) {
			dressDeque.add(item);
		}
		super.afterRead();
	}
	
	/**
	 * 获取装扮信息
	 * @return
	 */
	public BlockingDeque<DressItem> getDressInfo() {
		return dressDeque;
	}
	
	 /**
	  * 是否拥有装扮
	  * @param dressType
	  * @param modelType
	  */
	public boolean hasDress(int dressType, int modelType) {
		for (DressItem dressInfo : getDressInfo()) {
			if (dressInfo.getDressType() == dressType && dressInfo.getModelType() == modelType) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 添加装扮信息
	 * @return
	 */
	public void addDressInfo(DressItem dressInfo) {
		dressDeque.add(dressInfo);
	}
	
	/**
	 * 获取装扮信息
	 * @return
	 */
	public DressItem getDressInfo(int dressType, int modelType) {
		for (DressItem dressInfo : getDressInfo()) {
			if (dressInfo.getDressType() != dressType || dressInfo.getModelType() != modelType) {
				continue;
			}
			return dressInfo;
		}
		return null;
	}
	
	/**
	 * 添加或更新装扮信息
	 * @param dressType
	 * @param modelType
	 * @param continueTime
	 */
	public void addOrUpdateDressInfo(int dressType, int modelType, long continueTime) {
		// 永久的道具，策划配置的0。服务器按照100年来计算
		continueTime = continueTime == 0L ? GsConst.PERPETUAL_MILL_SECOND: continueTime;
		DressItem dressInfo = getDressInfo(dressType, modelType);
		long now = HawkTime.getMillisecond();
		if (dressInfo == null) {
			dressInfo = new DressItem();
			dressInfo.setDressType(dressType);
			dressInfo.setModelType(modelType);
			dressInfo.setStartTime(now);
			dressInfo.setContinueTime(continueTime);
			addDressInfo(dressInfo);
		} else {
			if (continueTime != GsConst.PERPETUAL_MILL_SECOND) {
				long beforeContinueTime = dressInfo.getContinueTime();
				dressInfo.setContinueTime(beforeContinueTime + continueTime);
			} else {
				dressInfo.setContinueTime(continueTime);
			}
		}
		
		if (dressType == DressType.MARCH_DRESS_VALUE || dressType == DressType.DERMA_VALUE) {
			dressInfo.setShowType(modelType);
			dressInfo.setShowEndTime(now + continueTime);
		}
		
		notifyUpdate();
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
