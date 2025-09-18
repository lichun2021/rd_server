package com.hawk.activity.type.impl.deepTreasure.entity;

import com.alibaba.fastjson.JSONArray;
import com.hawk.activity.type.ActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.deepTreasure.cfg.DeepTreasureBoxAchieveCfg;
import com.hawk.activity.type.impl.deepTreasure.cfg.DeepTreasureBuffCfg;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.annotation.IndexProp;
import org.hawk.config.HawkConfigManager;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Entity
@Table(name = "activity_deep_treasure")
public class DeepTreasureEntity extends ActivityDataEntity {
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
     * 活动成就项数据
     */
    @IndexProp(id = 4)
    @Column(name = "achieveItems", nullable = false)
    private String achieveItems = "";

    @IndexProp(id = 5)
    @Column(name = "nineBoxStr", nullable = false)
    private String nineBoxStr = "";

    @IndexProp(id = 6)
    @Column(name = "loginDays", nullable = false)
    private String loginDays = "";

    /**
     * 免费刷新
     */
    @IndexProp(id = 7)
    @Column(name = "nextFree", nullable = false)
    private long nextFree;

    /**
     * 兑换次数
     */
    @IndexProp(id = 8)
    @Column(name = "purchaseItemTimes", nullable = false)
    private long purchaseItemTimes;

    @IndexProp(id = 9)
    @Column(name = "exchangeMsg", nullable = false)
    private String exchangeMsg = "";

    /**
     * 刷新次数
     */
    @IndexProp(id = 10)
    @Column(name = "refreshtimes", nullable = false)
    private long refreshtimes;

    /**
     * 开箱子总数
     */
    @IndexProp(id = 11)
    @Column(name = "lotteryCount", nullable = false)
    private int lotteryCount;

    @IndexProp(id = 12)
    @Column(name = "createTime", nullable = false)
    private long createTime;

    @IndexProp(id = 13)
    @Column(name = "updateTime", nullable = false)
    private long updateTime;

    @IndexProp(id = 14)
    @Column(name = "invalid", nullable = false)
    private boolean invalid;

    @IndexProp(id = 15)
    @Column(name = "lotteryBuff", nullable = false)
    private String lotteryBuff;

    @Transient
    private List<AchieveItem> itemList = new ArrayList<AchieveItem>();
    @Transient
    private List<DeepTreasureBox> nineBox = new ArrayList<>();

    @Transient
    private Map<Integer, Integer> exchangeNumMap = new ConcurrentHashMap<>();

    @Transient
    private Map<Integer, DeepTreasureBuff> lotteryBuffMap = new ConcurrentHashMap<>();

    public DeepTreasureEntity() {
    }

    public DeepTreasureEntity(String playerId) {
        this.playerId = playerId;
        this.achieveItems = "";
        this.loginDays = "";
    }

    public DeepTreasureEntity(String playerId, int termId) {
        this.playerId = playerId;
        this.termId = termId;
        this.achieveItems = "";
        this.loginDays = "";
    }

    public int getExchangeCount(int exchangeId) {
        return this.exchangeNumMap.getOrDefault(exchangeId, 0);
    }

    public void addExchangeCount(int eid, int count) {
        if (count <= 0) {
            return;
        }
        count += this.getExchangeCount(eid);
        this.exchangeNumMap.put(eid, count);
        this.notifyUpdate();
    }

    public void addLotteryBuff(DeepTreasureBoxAchieveCfg cfg) {
        DeepTreasureBuffCfg buffCfg = HawkConfigManager.getInstance().getConfigByKey(DeepTreasureBuffCfg.class, cfg.getBuffId());
        if (buffCfg == null) {
            return;
        }
        this.lotteryBuffMap.put(cfg.getBuffId(), DeepTreasureBuff.valueOf(cfg.getBuffId(), buffCfg.getTimes()));
        this.notifyUpdate();
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

    public String getAchieveItems() {
        return achieveItems;
    }

    public void setAchieveItems(String achieveItems) {
        this.achieveItems = achieveItems;
    }

    public List<AchieveItem> getItemList() {
        return itemList;
    }

    public void setItemList(List<AchieveItem> itemList) {
        this.itemList = itemList;
    }

    @Override
    public void afterRead() {
        SerializeHelper.stringToMap(this.exchangeMsg, Integer.class, Integer.class, this.exchangeNumMap);
        this.itemList.clear();
        SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
        loadNineBox();
        loadBuff();
    }

    @Override
    public void beforeWrite() {
        this.exchangeMsg = SerializeHelper.mapToString(exchangeNumMap);
        this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
        this.nineBoxStr = nineBoxSerialize();
        this.lotteryBuff = buffSerialize();
    }

    private void loadNineBox() {
        JSONArray arr = JSONArray.parseArray(nineBoxStr);
        if (arr == null) {
            return;
        }
        arr.forEach(str -> {
            DeepTreasureBox crack = new DeepTreasureBox();
            crack.mergeFrom(str.toString());
            nineBox.add(crack);
        });
    }

    private void loadBuff() {
        JSONArray arr = JSONArray.parseArray(lotteryBuff);
        if (arr == null) {
            return;
        }
        arr.forEach(str -> {
            DeepTreasureBuff buff = new DeepTreasureBuff();
            buff.mergeFrom(str.toString());
            lotteryBuffMap.put(buff.getId(), buff);
        });
    }

    public String nineBoxSerialize() {
        JSONArray arr = new JSONArray();
        nineBox.stream().map(DeepTreasureBox::serialize).forEach(arr::add);
        return arr.toJSONString();
    }

    public String buffSerialize() {
        JSONArray arr = new JSONArray();
        lotteryBuffMap.values().stream().map(DeepTreasureBuff::serialize).forEach(arr::add);
        return arr.toJSONString();
    }

    @Override
    public long getCreateTime() {
        return createTime;
    }

    @Override
    public String getPrimaryKey() {
        return this.id;
    }

    @Override
    public long getUpdateTime() {
        return updateTime;
    }

    @Override
    public boolean isInvalid() {
        return invalid;
    }

    @Override
    protected void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    @Override
    protected void setInvalid(boolean invalid) {
        this.invalid = invalid;

    }

    @Override
    public void setPrimaryKey(String primaryKey) {
        this.id = primaryKey;
    }

    @Override
    protected void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public void resetItemList(List<AchieveItem> itemList) {
        this.itemList = itemList;
        this.notifyUpdate();
    }

    @Override
    public void setLoginDaysStr(String loginDays) {
        this.loginDays = loginDays;
    }

    @Override
    public String getLoginDaysStr() {
        return loginDays;
    }

    @Override
    public void recordLoginDay() {
        super.recordLoginDay();
    }

    public String getLoginDays() {
        return loginDays;
    }

    public void setLoginDays(String loginDays) {
        this.loginDays = loginDays;
    }

    public long getNextFree() {
        return nextFree;
    }

    public void setNextFree(long nextFree) {
        this.nextFree = nextFree;
    }

    public long getRefreshtimes() {
        return refreshtimes;
    }

    public void setRefreshtimes(long refreshtimes) {
        this.refreshtimes = refreshtimes;
    }

    public List<DeepTreasureBox> getNineBox() {
        return nineBox;
    }

    public void setNineBox(List<DeepTreasureBox> nineBox) {
        this.nineBox = nineBox;
    }

    public String getNineBoxStr() {
        return nineBoxStr;
    }

    public void setNineBoxStr(String nineBoxStr) {
        this.nineBoxStr = nineBoxStr;
    }

    public long getPurchaseItemTimes() {
        return purchaseItemTimes;
    }

    public void setPurchaseItemTimes(long purchaseItemTimes) {
        this.purchaseItemTimes = purchaseItemTimes;
    }

    public Map<Integer, Integer> getExchangeNumMap() {
        return exchangeNumMap;
    }

    public Map<Integer, DeepTreasureBuff> getLotteryBuffMap() {
        return lotteryBuffMap;
    }

    public int getLotteryCount() {
        return lotteryCount;
    }

    public void setLotteryCount(int lotteryCount) {
        this.lotteryCount = lotteryCount;
    }

}
