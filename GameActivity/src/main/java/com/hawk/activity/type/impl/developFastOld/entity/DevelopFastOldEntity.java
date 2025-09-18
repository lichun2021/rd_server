package com.hawk.activity.type.impl.developFastOld.entity;

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "activity_develop_fast_old")
public class DevelopFastOldEntity extends HawkDBEntity implements IActivityDataEntity {
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

    @IndexProp(id = 4)
    @Column(name = "createTime", nullable = false)
    private long createTime;

    @IndexProp(id = 5)
    @Column(name = "updateTime", nullable = false)
    private long updateTime;

    @IndexProp(id = 6)
    @Column(name = "invalid", nullable = false)
    private boolean invalid;

    /** 活动成就项数据 */
    @IndexProp(id = 7)
    @Column(name = "buyItems", nullable = false)
    private String buyItems;

    /** 活动成就项数据 */
    @IndexProp(id = 8)
    @Column(name = "scoreItems", nullable = false)
    private String scoreItems;

    /** 活动成就项数据 */
    @IndexProp(id = 9)
    @Column(name = "taskItems", nullable = false)
    private String taskItems;

    /** 活动成就项数据 */
    @IndexProp(id = 10)
    @Column(name = "achieveItems", nullable = false)
    private String achieveItems;

    @IndexProp(id = 11)
    @Column(name = "loginDays", nullable = false)
    private int loginDays;

    @IndexProp(id = 12)
    @Column(name = "loginTime", nullable = false)
    private long loginTime;

    @Transient
    private Map<Integer,Integer> buyMap = new HashMap<>();
    @Transient
    private Map<Integer,Integer> scoreMap = new HashMap<>();

    @Transient
    private List<DevelopFastOldTask> taskList = new ArrayList<>();
    /** 活动成就 */
    @Transient
    private List<AchieveItem> achieveItemList = new ArrayList<>();



    public DevelopFastOldEntity(){

    }

    public DevelopFastOldEntity(String playerId, int termId){
        this.playerId = playerId;
        this.termId = termId;
    }


    @Override
    public void beforeWrite() {
        buyItems = SerializeHelper.mapToString(buyMap);
        scoreItems = SerializeHelper.mapToString(scoreMap);
        this.taskItems = SerializeHelper.collectionToString(this.taskList, SerializeHelper.ELEMENT_DELIMITER);
        this.achieveItems = SerializeHelper.collectionToString(this.achieveItemList, SerializeHelper.ELEMENT_DELIMITER);
    }

    @Override
    public void afterRead() {
        buyMap = SerializeHelper.stringToMap(buyItems, Integer.class, Integer.class);
        scoreMap = SerializeHelper.stringToMap(scoreItems, Integer.class, Integer.class);
        this.taskList.clear();
        this.achieveItemList.clear();
        SerializeHelper.stringToList(DevelopFastOldTask.class, this.taskItems, this.taskList);
        SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.achieveItemList);

    }

    @Override
    public int getTermId() {
        return termId;
    }

    public void setTermId(int termId) {
        this.termId = termId;
    }

    public String getPlayerId() {
        return playerId;
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

    public Map<Integer, Integer> getBuyMap() {
        return buyMap;
    }

    public Map<Integer, Integer> getScoreMap() {
        return scoreMap;
    }

    public List<AchieveItem> getItemList() {
        return achieveItemList;
    }

    public void setItemList(List<AchieveItem> itemList) {
        this.achieveItemList = itemList;
    }

    public List<DevelopFastOldTask> getTaskList() {
        return taskList;
    }

    public void setTaskList(List<DevelopFastOldTask> taskList) {
        this.taskList = taskList;
    }

    public int getLoginDays() {
        return loginDays;
    }

    public void setLoginDays(int loginDays) {
        this.loginDays = loginDays;
    }

    public long getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(long loginTime) {
        this.loginTime = loginTime;
    }
}
