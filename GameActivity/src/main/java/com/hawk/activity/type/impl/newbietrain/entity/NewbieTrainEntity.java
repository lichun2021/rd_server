package com.hawk.activity.type.impl.newbietrain.entity;

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.serialize.string.SerializeHelper;

import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;
import org.hawk.annotation.IndexProp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.*;

/**
 * 新兵作训
 */
@Entity
@Table(name = "activity_newbie_train")
public class NewbieTrainEntity extends HawkDBEntity implements IActivityDataEntity {
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
     * 每日首次登录时间
     */
    @IndexProp(id = 4)
    @Column(name = "dailyLoginTime", nullable = false)
    private long dailyLoginTime;
    
    /** 活动成就项数据 */
    @IndexProp(id = 5)
	@Column(name = "achieveItems", nullable = false)
	private String achieveItems;
    
    @IndexProp(id = 6)
   	@Column(name = "trainInfos", nullable = false)
   	private String trainInfos;
    
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
	private Map<Integer, NewbieTrainInfo> trainInfoMap = new HashMap<>();
    
    public NewbieTrainEntity(){
    }

    public NewbieTrainEntity(String playerId, int termId){
        this.playerId = playerId;
        this.termId = termId;
    }
    
    @Override
    public void beforeWrite() {
    	this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
    	this.trainInfos = SerializeHelper.collectionToString(this.trainInfoMap.values(), ";", "|");
    }

    @Override
    public void afterRead() {
    	this.itemList.clear();
    	SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
    	List<NewbieTrainInfo> list = new ArrayList<>();
    	SerializeHelper.stringToList(NewbieTrainInfo.class, this.trainInfos, ";", "\\|", list);
    	for (NewbieTrainInfo info : list) {
    		trainInfoMap.put(info.getTrainType(), info);
    	}
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

	@Override
    public int getTermId() {
        return termId;
    }

    public void setTermId(int termId) {
        this.termId = termId;
    }
    
	public long getDailyLoginTime() {
		return dailyLoginTime;
	}

	public void setDailyLoginTime(long dailyLoginTime) {
		this.dailyLoginTime = dailyLoginTime;
	}

	public String getAchieveItems() {
		return achieveItems;
	}
	
	public void setAchieveItems(String achieveItems) {
		this.achieveItems = achieveItems;
	}
	
	public void setItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
	}
	
	public List<AchieveItem> getItemList() {
		return itemList;
	}
	
	public Map<Integer, NewbieTrainInfo> getTrainInfoMap() {
		return trainInfoMap;
	}

    public NewbieTrainInfo getTrainInfo(int trainType) {
    	if (trainInfoMap.containsKey(trainType)) {
    		return trainInfoMap.get(trainType);
    	}
    	
    	NewbieTrainInfo info = new NewbieTrainInfo();
    	info.setTrainType(trainType);
    	trainInfoMap.put(trainType, info);
    	notifyUpdate();
    	return info;
    }

    @Override
    public long getCreateTime() {
        return createTime;
    }

    @Override
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    @Override
    public long getUpdateTime() {
        return updateTime;
    }

    @Override
    public void setUpdateTime(long updateTime) {
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
