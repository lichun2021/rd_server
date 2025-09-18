package com.hawk.activity.type.impl.cnyExam.entity;

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
@Table(name = "activity_cny_exam")
public class CnyExamEntity extends HawkDBEntity implements IActivityDataEntity {
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

    @IndexProp(id = 7)
    @Column(name = "buyItems", nullable = false)
    private String buyItems;

    @IndexProp(id = 8)
    @Column(name = "takeItems", nullable = false)
    private String takeItems;

    @IndexProp(id = 9)
    @Column(name = "score", nullable = false)
    private int score;

    @IndexProp(id = 10)
    @Column(name = "achieveItems", nullable = false)
    private String achieveItems;
    @IndexProp(id = 11)
    @Column(name = "level", nullable = false)
    private int level;

    @IndexProp(id = 12)
    @Column(name = "chooseItems1", nullable = false)
    private String chooseItems1;

    @IndexProp(id = 13)
    @Column(name = "chooseItems2", nullable = false)
    private String chooseItems2;

    @IndexProp(id = 14)
    @Column(name = "loginDays", nullable = false)
    private int loginDays;

    @IndexProp(id = 15)
    @Column(name = "loginTime", nullable = false)
    private long loginTime;

    @Transient
    private Map<Integer,Integer> buyMap = new HashMap<>();
    @Transient
    private Map<Integer,Integer> takeMap = new HashMap<>();
    @Transient
    private Map<Integer,Integer> chooseMap1 = new HashMap<>();
    @Transient
    private Map<Integer,Integer> chooseMap2 = new HashMap<>();


    /** 活动成就 */
    @Transient
    private List<AchieveItem> itemList = new ArrayList<>();

    public CnyExamEntity(){

    }

    public CnyExamEntity(String playerId, int termId){
        this.playerId = playerId;
        this.termId = termId;
    }


    @Override
    public void beforeWrite() {
        buyItems = SerializeHelper.mapToString(buyMap);
        takeItems = SerializeHelper.mapToString(takeMap);
        chooseItems1 = SerializeHelper.mapToString(chooseMap1);
        chooseItems2 = SerializeHelper.mapToString(chooseMap2);
        this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
    }

    @Override
    public void afterRead() {
        buyMap = SerializeHelper.stringToMap(buyItems, Integer.class, Integer.class);
        takeMap = SerializeHelper.stringToMap(takeItems, Integer.class, Integer.class);
        chooseMap1 = SerializeHelper.stringToMap(chooseItems1, Integer.class, Integer.class);
        chooseMap2 = SerializeHelper.stringToMap(chooseItems2, Integer.class, Integer.class);
        this.itemList = SerializeHelper.stringToList(AchieveItem.class, this.achieveItems);
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

    public List<AchieveItem> getItemList() {
        return itemList;
    }

    public void setItemList(List<AchieveItem> itemList) {
        this.itemList = itemList;
    }

    public Map<Integer, Integer> getBuyMap() {
        return buyMap;
    }

    public Map<Integer, Integer> getTakeMap() {
        return takeMap;
    }

    public Map<Integer, Integer> getChooseMap1() {
        return chooseMap1;
    }

    public Map<Integer, Integer> getChooseMap2() {
        return chooseMap2;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
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
