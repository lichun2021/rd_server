package com.hawk.activity.type.impl.pddActivity.entity;

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.pddActivity.cfg.PDDKVCfg;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkTime;
import org.hibernate.annotations.GenericGenerator;

import org.hawk.annotation.IndexProp;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "activity_pdd")
public class PDDActivityEntity extends HawkDBEntity implements IActivityDataEntity {
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
    @Column(name = "resetTime", nullable = false)
    private long resetTime;

    /** 购买数量信息 */
    @IndexProp(id = 9)
    @Column(name = "buyInfo", nullable = false)
    private String buyInfo;

    @IndexProp(id = 10)
    @Column(name = "failNum", nullable = false)
    private int failNum;

    @IndexProp(id = 11)
    @Column(name = "isFirst", nullable = false)
    private boolean isFirst;

    @IndexProp(id = 12)
    @Column(name = "shareTime", nullable = false)
    private long shareTime;

    @IndexProp(id = 13)
    @Column(name = "shareCount", nullable = false)
    private int shareCount;

    /** 活动成就 */
    @Transient
    private List<AchieveItem> itemList = new ArrayList<AchieveItem>();

    /** 兑换数量 */
    @Transient
    private Map<Integer, Integer> buyNumMap = new HashMap<>();

    public PDDActivityEntity(){

    }

    public PDDActivityEntity(String playerId, int termId) {
        this.playerId = playerId;
        this.termId = termId;
        this.achieveItems = "";
    }

    @Override
    public void beforeWrite() {
        //成就数据转换成字符串
        this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
        //兑换数据转字符串
        this.buyInfo = SerializeHelper.mapToString(buyNumMap);
    }

    @Override
    public void afterRead() {
    	this.itemList.clear();
        //字符串转换成成就数据
        SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
        //字符串转兑换数据
        buyNumMap = SerializeHelper.stringToMap(buyInfo, Integer.class, Integer.class);
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

    public long getResetTime() {
        return resetTime;
    }

    public void setResetTime(long resetTime) {
        this.resetTime = resetTime;
    }

    public Map<Integer, Integer> getBuyNumMap() {
        return buyNumMap;
    }

    public int getShareCount() {
        return shareCount;
    }

    public void setShareCount(int shareCount) {
        this.shareCount = shareCount;
    }

    public long getShareTime() {
        return shareTime;
    }

    public void setShareTime(long shareTime) {
        this.shareTime = shareTime;
    }

    public boolean addShare(){
        long now = HawkTime.getMillisecond();
        PDDKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PDDKVCfg.class);
        if(cfg == null){
            return false;
        }
        if(now - shareTime < cfg.getShareCdTime()){
            return false;
        }
        if(shareCount >= cfg.getShareDailyNum()){
            return false;
        }
        shareTime = now;
        shareCount += 1;
        notifyUpdate();
        return true;
    }
}
