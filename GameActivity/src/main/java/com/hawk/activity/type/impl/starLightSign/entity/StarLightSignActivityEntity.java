package com.hawk.activity.type.impl.starLightSign.entity;

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import org.hawk.annotation.IndexProp;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "activity_star_light_sign")
public class StarLightSignActivityEntity  extends HawkDBEntity implements IActivityDataEntity {
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
    @Column(name = "achieveItems", nullable = false)
    private String achieveItems;

    @IndexProp(id = 8)
    @Column(name = "signItems", nullable = false)
    private String signItems;

    @IndexProp(id = 9)
    @Column(name = "signDays", nullable = false)
    private String signDays;

    @IndexProp(id = 10)
    @Column(name = "score", nullable = false)
    private int score;

    @IndexProp(id = 11)
    @Column(name = "scoreBox", nullable = false)
    private String scoreBox;

    @IndexProp(id = 12)
    @Column(name = "isMultiple", nullable = false)
    private boolean isMultiple;

    @IndexProp(id = 13)
    @Column(name = "isAdMultiple", nullable = false)
    private boolean isAdMultiple;

    @IndexProp(id = 14)
    @Column(name = "signRedeemCnt", nullable = false)
    private int signRedeemCnt;


    @Transient
    private List<AchieveItem> itemList = new ArrayList<>();

    @Transient
    private List<StarLightSignItem> signList = new ArrayList<>();

    @Transient
    private List<Integer> signDayList = new ArrayList<>();

    @Transient
    private List<Integer> scoreBoxList = new ArrayList<>();

    public StarLightSignActivityEntity(){

    }

    public StarLightSignActivityEntity(String playerId, int termId){
        this.playerId = playerId;
        this.termId = termId;
        this.achieveItems = "";
        this.signItems = "";
        this.signDays = "";
        this.scoreBox = "";
    }

    @Override
    public void beforeWrite() {
        //成就数据转换成字符串
        this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
        this.signItems = SerializeHelper.collectionToString(this.signList, SerializeHelper.ELEMENT_DELIMITER);
        signDays = SerializeHelper.collectionToString(signDayList, SerializeHelper.ATTRIBUTE_SPLIT);
        scoreBox = SerializeHelper.collectionToString(scoreBoxList, SerializeHelper.ATTRIBUTE_SPLIT);
    }

    @Override
    public void afterRead() {
        //字符串转换成成就数据
    	this.itemList.clear();
    	this.signList.clear();
        SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
        SerializeHelper.stringToList(StarLightSignItem.class, this.signItems, this.signList);
        signDayList = SerializeHelper.cfgStr2List(signDays);
        scoreBoxList = SerializeHelper.cfgStr2List(scoreBox);
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

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isMultiple() {
        return isMultiple;
    }

    public void setMultiple(boolean multiple) {
        isMultiple = multiple;
    }

    public boolean isAdMultiple() {
        return isAdMultiple;
    }

    public void setAdMultiple(boolean adMultiple) {
        isAdMultiple = adMultiple;
    }

    public int getSignRedeemCnt() {
        return signRedeemCnt;
    }

    public void setSignRedeemCnt(int signRedeemCnt) {
        this.signRedeemCnt = signRedeemCnt;
    }

    public List<StarLightSignItem> getSignList() {
        return signList;
    }

    public void setSignList(List<StarLightSignItem> signList) {
        this.signList = signList;
    }

    public List<Integer> getSignDayList() {
        return signDayList;
    }

    public void setSignDayList(List<Integer> signDayList) {
        this.signDayList = signDayList;
    }

    public List<Integer> getScoreBoxList() {
        return scoreBoxList;
    }

    public void setScoreBoxList(List<Integer> scoreBoxList) {
        this.scoreBoxList = scoreBoxList;
    }
}
