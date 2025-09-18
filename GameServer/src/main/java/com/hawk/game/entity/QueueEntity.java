package com.hawk.game.entity;

import org.hawk.annotation.IndexProp;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.game.GsConfig;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.util.GsConst;

/**
 * 队列实体对象
 *
 * @author julia
 */
@Entity
@Table(name = "queue")
public class QueueEntity extends HawkDBEntity {
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
    @GeneratedValue(generator = "uuid")
	@Column(name = "id", unique = true, nullable = false)
    @IndexProp(id = 1)
	private String id = null;

	@Column(name = "playerId", nullable = false)
    @IndexProp(id = 2)
	private String playerId = "";

	// 队列类型，比如建筑升级或者科技升级 队列类型，建筑升级，科技升级等
	@Column(name = "queueType", nullable = false)
    @IndexProp(id = 3)
	private int queueType;

	@Column(name = "buildingType")
    @IndexProp(id = 4)
	private int buildingType;

	@Column(name = "itemId", nullable = false)
    @IndexProp(id = 5)
	private String itemId = null;

	@Column(name = "startTime", nullable = false)
    @IndexProp(id = 6)
	private long startTime;

	@Column(name = "endTime", nullable = false)
    @IndexProp(id = 7)
	private long endTime;

	@Column(name = "totalQueueTime", nullable = false)
    @IndexProp(id = 8)
	private long totalQueueTime;

	@Column(name = "totalReduceTime", nullable = false)
    @IndexProp(id = 9)
	private long totalReduceTime;

	@Column(name = "status", nullable = false)
    @IndexProp(id = 10)
	private int status;

	@Column(name = "helpTimes", nullable = false)
    @IndexProp(id = 11)
	private int helpTimes;

	@Column(name = "cancelBackRes", nullable = true)
    @IndexProp(id = 12)
	private String cancelBackRes;

	@Column(name = "reusage")
    @IndexProp(id = 13)
	private int reusage = -1;
	
	/**
	 * 可用状态结束时间，付费队列值不等于0
	 */
	@Column(name = "enableEndTime")
    @IndexProp(id = 14)
	private long enableEndTime; 

	@Column(name = "createTime", nullable = false)
    @IndexProp(id = 15)
	protected long createTime = 0;

	@Column(name = "updateTime")
    @IndexProp(id = 16)
	protected long updateTime = 0;

	@Column(name = "invalid")
    @IndexProp(id = 17)
	protected boolean invalid;

	@Column(name = "multiply")
    @IndexProp(id = 18)
	private int multiply;
	
	
	public QueueEntity() {
		asyncLandPeriod(GsConfig.getInstance().getEntitySyncPeriod());
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getHelpTimes() {
		return helpTimes;
	}

	public void setHelpTimes(int helpTimes) {
		this.helpTimes = helpTimes;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public int getQueueType() {
		return queueType;
	}

	public void setQueueType(int queueType) {
		this.queueType = queueType;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public long getTotalQueueTime() {
		return totalQueueTime;
	}

	public void setTotalQueueTime(long totalQueueTime) {
		this.totalQueueTime = totalQueueTime;
	}

	public long getTotalReduceTime() {
		return totalReduceTime;
	}

	public void setTotalReduceTime(long totalReduceTime) {
		this.totalReduceTime = totalReduceTime;
	}

	public int getBuildingType() {
		return buildingType;
	}

	public void setBuildingType(int buildingType) {
		this.buildingType = buildingType;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getCancelBackRes() {
		return cancelBackRes;
	}

	public void setCancelBackRes(String cancelBackRes) {
		this.cancelBackRes = cancelBackRes;
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

	public int getReusage() {
		return reusage;
	}

	public void setReusage(int reusage) {
		this.reusage = reusage;
	}
	
	public long getEnableEndTime() {
		return enableEndTime;
	}

	public void setEnableEndTime(long enableEndTime) {
		this.enableEndTime = enableEndTime;
	}

	public int getMultiply() {
		//此字段是后加的
		//考虑线上的数据有的是0，所以加此判断
		if(this.multiply <=1){
			return 1;
		}
		return multiply;
	}
	
	public void setMultiply(int multiply) {
		this.multiply = multiply;
	}
	
	
	/**
	 * 可重用队列重置
	 */
	public void remove() {
		setReusage(GsConst.QueueReusage.FREE.intValue());
		setQueueType(-1);
		setStatus(-1);
		setItemId("");
		setBuildingType(0);
		setStartTime(0);
		setEndTime(0);
		setTotalQueueTime(0);
		setHelpTimes(0);
		setTotalReduceTime(0);
		setCancelBackRes("");
		setMultiply(0);
	}

	/**
	 * 更新可重用队列
	 * 
	 * @param itemId
	 * @param buildingType
	 * @param newBuildCfgId
	 * @param startTime
	 * @param buildTime
	 * @param cancelBackRes
	 */
	public void update(int queueType, int queueStatus, String itemId, int buildingType,
			long startTime, double buildTime, List<ItemInfo> cancelBackRes, GsConst.QueueReusage reusage,int multiply) {
		setQueueType(queueType);
		setStatus(queueStatus);
		setStartTime(startTime);
		setEndTime(startTime + (long) Math.ceil(buildTime / 1000) * 1000);
		setTotalQueueTime(getEndTime() - startTime);
		setItemId(itemId);
		setBuildingType(buildingType);
		setReusage(reusage.intValue()); // 可重用队列
		setHelpTimes(0);
		setTotalReduceTime(0);
		if (cancelBackRes != null && cancelBackRes.size() > 0) {
			AwardItems items = AwardItems.valueOf();
			items.addItemInfos(cancelBackRes);
			setCancelBackRes(items.toDbString());
		}
		setMultiply(multiply);
	}
	

	
	
	
	@Override
	public String getPrimaryKey() {
		return id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		id = primaryKey;
	}
	
	public String getOwnerKey() {
		return playerId;
	}
}
