package com.hawk.activity.type.impl.evolution.entity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 英雄进化之路活动数据存储
 * 
 * @author lating
 *
 */
@Entity
@Table(name = "activity_evolution")
public class ActivityEvolutionEntity extends HawkDBEntity implements IActivityDataEntity {

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
	
	/** 经验积分 */
    @IndexProp(id = 4)
	@Column(name = "exp", nullable = false)
	private int exp;
	
	/** 奖池等级 */
    @IndexProp(id = 5)
	@Column(name = "level", nullable = false)
	private int level;
	
	/** 等级奖励领取状态 */
    @IndexProp(id = 6)
	@Column(name = "status", nullable = false)
	private int status;

	/** 任务信息 */
    @IndexProp(id = 7)
	@Column(name = "taskItems", nullable = false)
	private String taskItems;
	
	/** 已兑换的奖励信息  */
    @IndexProp(id = 8)
	@Column(name = "finishedExchange", nullable = false)
	private String finishedExchange;

    @IndexProp(id = 9)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 10)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 11)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;

	@Transient
	private List<TaskItem> taskList = new CopyOnWriteArrayList<TaskItem>();
	
	@Transient
	private List<Integer> exchangeList = new CopyOnWriteArrayList<Integer>();

	public ActivityEvolutionEntity() {
	}

	public ActivityEvolutionEntity(String playerId) {
		this.playerId = playerId;
	}

	public ActivityEvolutionEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
	}

	@Override
	public void beforeWrite() {
		this.taskItems = SerializeHelper.collectionToString(this.taskList, SerializeHelper.ELEMENT_DELIMITER);
		this.finishedExchange = SerializeHelper.collectionToString(this.exchangeList, SerializeHelper.ELEMENT_DELIMITER);
	}


	@Override
	public void afterRead() {
		this.taskList.clear();
		this.exchangeList.clear();
		SerializeHelper.stringToList(TaskItem.class, this.taskItems, this.taskList);
		SerializeHelper.stringToList(Integer.class, this.finishedExchange, this.exchangeList);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public int getExp() {
		return exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
	
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getTaskItems() {
		return taskItems;
	}

	public void setTaskItems(String taskItems) {
		this.taskItems = taskItems;
	}
	
	public String getFinishedExchange() {
		return finishedExchange;
	}

	public void setFinishedExchange(String finishedExchange) {
		this.finishedExchange = finishedExchange;
	}

	public List<TaskItem> getTaskList() {
		return taskList;
	}
	
	public TaskItem getTaskItem(int taskId) {
		for (TaskItem item : taskList) {
			if (item.getTaskId() == taskId) {
				return item;
			}
		}
		
		return null;
	}
	
	public List<Integer> getExchangeList() {
		return exchangeList;
	}

	/**
	 * 初始化活动任务列表
	 * 
	 * @param orderList
	 */
	public void resetTaskList(List<TaskItem> taskList) {
		this.taskList = taskList;
		this.notifyUpdate();
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

	@Override
	public String getPrimaryKey() {
		return this.id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}

}
