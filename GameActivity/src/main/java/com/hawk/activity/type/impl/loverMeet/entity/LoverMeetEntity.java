package com.hawk.activity.type.impl.loverMeet.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name="activity_lover_meet")
public class LoverMeetEntity extends HawkDBEntity implements IActivityDataEntity {

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
	
	/** 问卷*/
    @IndexProp(id = 4)
	@Column(name = "questionStr", nullable = false)
	private String questionStr;
	
	/** 结局*/
    @IndexProp(id = 5)
	@Column(name = "endingStr", nullable = false)
	private String endingStr;
	
	
	/** 活动成就项数据 */
    @IndexProp(id = 6)
	@Column(name = "achieveItems", nullable = false)
	private String achieveItems;
	

    @IndexProp(id = 7)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 8)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 9)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	@Transient
	private List<AchieveItem> itemList = new ArrayList<AchieveItem>();
	
	@Transient
	private Map<Long, Integer> endingMap = new ConcurrentHashMap<>();
	
	@Transient
	private LoverMeetQuestion question = new LoverMeetQuestion();
	
	
	public LoverMeetEntity() {
	}
	
	public LoverMeetEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.achieveItems = "";
		this.questionStr = "";
		this.endingStr = "";
	}
	
	
	@Override
	public void beforeWrite() {
		this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
		this.endingStr = SerializeHelper.mapToString(endingMap);
		this.questionStr = this.question.serializ();
	}

	@Override
	public void afterRead() {
		this.itemList.clear();
		SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
		SerializeHelper.stringToMap(this.endingStr, Long.class, Integer.class,this.endingMap);
		this.question.mergeFrom(this.questionStr);
	}

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
		return invalid;
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
	
	
	
	public List<AchieveItem> getItemList() {
		return itemList;
	}

	public void setItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
	}
	
	public LoverMeetQuestion getQuestion() {
		return question;
	}
	
	
	public void addEnding(long time,int endingId){
		this.endingMap.put(time, endingId);
		this.notifyUpdate();
	}
	
	public int getEndingCount(){
		return this.endingMap.size();
	}
	
	public Set<Integer> getEndings(){
		Set<Integer> set = new HashSet<>();
		set.addAll(this.endingMap.values());
		return set;
	}
	
	public void resetQuestion(){
		LoverMeetQuestion newQuestion = new LoverMeetQuestion();
		this.question = newQuestion;
		this.notifyUpdate();
	}
	
}
