package com.hawk.activity.type.impl.recoveryExchange.entity;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.game.protocol.Activity.PBItemRecycleResp;
import com.hawk.game.protocol.Activity.PBItemRecycleStruct;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;
import com.hawk.serialize.string.SerializeHelper;

/**
 * @author richard
 * 道具回收活动玩家数据
 */
@Entity
@Table(name = "activity_recovery_exchange")
public class RecoveryExchangeEntity extends HawkDBEntity implements IActivityDataEntity {

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

    /**
     * 积分兑换物品的次数，使用:itemId_times:itemId_times
     */
    @IndexProp(id = 7)
    @Column(name = "exchangeTimes", nullable = false)
    private String exchangeTimes;
    /**
     * 10连抽次数
     */
    @IndexProp(id = 8)
    @Column(name = "redTimes", nullable = false)
    private String redTimes;
    /**
     * 关注的兑换id列表
     **/
    @IndexProp(id = 9)
    @Column(name = "redHighTimes", nullable = false)
    private String redHighTimes;
    /**
     * 关注的兑换id列表 用积分兑换物品的勾选状态
     **/
    @IndexProp(id = 10)
    @Column(name = "playerPoint", nullable = false)
    private String playerPoint;
    /**
     * 已回收物品的列表，此处记录的是物品三段式列表
     **/
    @IndexProp(id = 11)
    @Column(name = "recycleItems", nullable = false)
    private String recycleItems;

    @Transient
    private Map<Integer, Integer> exchangeTimesMap = new ConcurrentHashMap<>();
    @Transient
    private Map<Integer, Integer> redTimesMap = new ConcurrentHashMap<>();
    @Transient
    private Map<Integer, Integer> redHighTimesMap = new ConcurrentHashMap<>();
    //    @Transient
//    private Map<Integer, Integer> exchangeItemsMap = new ConcurrentHashMap<>();
    @Transient
    private List<Integer> playerPoints = new CopyOnWriteArrayList<Integer>();
    @Transient
    private Map<Integer, Integer> recycleItemsMap = new ConcurrentHashMap<>();

    public RecoveryExchangeEntity() {
    }

    public RecoveryExchangeEntity(String playerId, int termId) {
        this.playerId = playerId;
        this.termId = termId;
        this.exchangeTimes = "";
        this.redTimes = "";
        this.redHighTimes = "";
        //this.exchangeItems = "";
        this.recycleItems = "";
    }

    @Override
    public void beforeWrite() {
        this.exchangeTimes = SerializeHelper.mapToString(exchangeTimesMap);
        this.redTimes = SerializeHelper.mapToString(redTimesMap);
        this.redHighTimes = SerializeHelper.mapToString(redHighTimesMap);
        this.playerPoint = SerializeHelper.collectionToString(playerPoints, SerializeHelper.ATTRIBUTE_SPLIT);
        this.recycleItems = SerializeHelper.mapToString(recycleItemsMap);
    }

    @Override
    public void afterRead() {
        exchangeTimesMap = SerializeHelper.stringToMap(exchangeTimes, Integer.class, Integer.class);
        redTimesMap = SerializeHelper.stringToMap(redTimes, Integer.class, Integer.class);
        redHighTimesMap = SerializeHelper.stringToMap(redHighTimes, Integer.class, Integer.class);
        playerPoints = SerializeHelper.cfgStr2List(playerPoint, SerializeHelper.ATTRIBUTE_SPLIT);
        recycleItemsMap = SerializeHelper.stringToMap(recycleItems, Integer.class, Integer.class);
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

    public void addExchangeTimes(int cfgId, int times) {
        int oldTimes = this.exchangeTimesMap.getOrDefault(cfgId, 0);
        this.exchangeTimesMap.put(cfgId, oldTimes + times);
        this.notifyUpdate();
    }

    public int getExchangeTimes(int cfgId) {
        return this.exchangeTimesMap.getOrDefault(cfgId, 0);
    }

    public void getExchangeTimes(PBItemRecycleResp.Builder builder) {
        for (Map.Entry<Integer, Integer> entry : this.exchangeTimesMap.entrySet()) {
            PBItemRecycleStruct.Builder itemsBuilder = builder.addExchangeTimesBuilder();
            itemsBuilder.setCfgId(entry.getKey());
            itemsBuilder.setCount(entry.getValue());
        }
    }

    public void addRedTimesMap(int exchangeId, int times) {
        int oldTimes = this.redTimesMap.getOrDefault(exchangeId, 0);
        this.redTimesMap.put(exchangeId, oldTimes + times);
        this.notifyUpdate();
    }

    public int getRedTimes(int cfgId) {
        return this.redTimesMap.getOrDefault(cfgId, 0);
    }

    public void getRedTimes(PBItemRecycleResp.Builder builder) {
        for (Map.Entry<Integer, Integer> entry : this.redTimesMap.entrySet()) {
            PBItemRecycleStruct.Builder itemsBuilder = builder.addRedundantTimesBuilder();
            itemsBuilder.setCfgId(entry.getKey());
            itemsBuilder.setCount(entry.getValue());
        }
    }

    public void addRedHighTimes(int cfgId, int times) {
        int oldTimes = this.redHighTimesMap.getOrDefault(cfgId, 0);
        this.redHighTimesMap.put(cfgId, oldTimes + times);
        this.notifyUpdate();
    }

    public int getRedHighTimes(int cfgId) {
        return this.redHighTimesMap.getOrDefault(cfgId, 0);
    }

    public void getRedHighTimes(PBItemRecycleResp.Builder builder) {
        for (Map.Entry<Integer, Integer> entry : this.redHighTimesMap.entrySet()) {
            PBItemRecycleStruct.Builder itemsBuilder = builder.addRedundantHighTimesBuilder();
            itemsBuilder.setCfgId(entry.getKey());
            itemsBuilder.setCount(entry.getValue());
        }
    }

    public void changeRecycleItem(int cfgId, int count) {
        int oldCount = this.recycleItemsMap.getOrDefault(cfgId, 0);
        int newCount = oldCount + count;
        if (newCount > 0) {
            this.recycleItemsMap.put(cfgId, newCount);
        } else {
            this.recycleItemsMap.remove(cfgId);
        }
        this.notifyUpdate();
    }

    public int getRecycleItemCount(int cfgId) {
        return this.recycleItemsMap.getOrDefault(cfgId, 0);
    }

    public void getRecycleItems(PBItemRecycleResp.Builder builder) {
        for (Map.Entry<Integer, Integer> entry : this.recycleItemsMap.entrySet()) {
            PBItemRecycleStruct.Builder itemsBuilder = builder.addRecycleItemsBuilder();
            itemsBuilder.setCfgId(entry.getKey());
            itemsBuilder.setCount(entry.getValue());
        }
    }

    public void addTips(int id) {
        if (!playerPoints.contains(id)) {
            playerPoints.add(id);
        }
        this.notifyUpdate();
    }

    public void removeTips(int id) {
        playerPoints.remove(new Integer(id));
        this.notifyUpdate();
    }

    public List<Integer> getPlayerPoints() {
        return playerPoints;
    }
}
