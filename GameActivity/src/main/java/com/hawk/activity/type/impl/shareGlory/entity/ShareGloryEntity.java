package com.hawk.activity.type.impl.shareGlory.entity;

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.shareGlory.cfg.ShareGloryKVCfg;
import com.hawk.activity.type.impl.shareGlory.cfg.ShareGloryKVCfg.DonateItemType;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import org.hawk.annotation.IndexProp;
import javax.persistence.*;
import java.util.*;

/**
 * 荣耀同享数据库对象
 *
 * @author Richard
 */
@Entity
@Table(name = "activity_alliance_share_glory")
public class ShareGloryEntity extends HawkDBEntity implements IActivityDataEntity {

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
    @Column(name = "createTime", nullable = false)
    private long createTime;
    @IndexProp(id = 4)
    @Column(name = "updateTime", nullable = false)
    private long updateTime;
    @IndexProp(id = 5)
    @Column(name = "invalid", nullable = false)
    private boolean invalid;
    @IndexProp(id = 6)
    @Column(name = "termId", nullable = false)
    private int termId;
    /**
     * 监控指定活动获得的道具已返还的数量
     * 格式道具30000_itemID_数量,格式道具30000_itemID_数量 ......
     */
    @IndexProp(id = 7)
    @Column(name = "rewardInfoA", nullable = false)
    private String rewardInfoA;
    /**
     * 监控指定活动获得的道具
     * 格式：格式道具30000_itemID_数量,格式道具30000_itemID_数量 ......
     */
    @IndexProp(id = 8)
    @Column(name = "rewardActivityA", nullable = false)
    private String rewardActivityA;

    /**
     * 监控指定活动获得的道具已返还的数量
     * 格式道具30000_itemID_数量,格式道具30000_itemID_数量 ......
     */
    @IndexProp(id = 9)
    @Column(name = "rewardInfoB", nullable = false)
    private String rewardInfoB;
    /**
     * 监控指定活动获得的道具
     * 格式：格式道具30000_itemID_数量,格式道具30000_itemID_数量 ......
     */
    @IndexProp(id = 10)
    @Column(name = "rewardActivityB", nullable = false)
    private String rewardActivityB;
    /**
     * 当前已经领取到的A能量柱升级奖励的等级
     */
    @IndexProp(id = 11)
    @Column(name = "rewardEnergyLevelA", nullable = false)
    private int rewardEnergyLevelA;
    /**
     * A道具已捐献总数量
     */
    @IndexProp(id = 12)
    @Column(name = "donateCountA", nullable = false)
    private int donateCountA;
    /**
     * B道具已捐献总数量
     */
    @IndexProp(id = 13)
    @Column(name = "donateCountB", nullable = false)
    private int donateCountB;
    /**
     * 当前已经领取到的B能量柱升级奖励的等级
     */
    @IndexProp(id = 14)
    @Column(name = "rewardEnergyLevelB", nullable = false)
    private int rewardEnergyLevelB;
    /**
     * 活动期间所在的工会
     */
    @IndexProp(id = 15)
    @Column(name = "guildid", nullable = false)
    private String guildid="";
    /**
     * 玩家已经领取过的能量A奖励等级
     */
    @IndexProp(id = 16)
    @Column(name = "rewardEnergyA", nullable = false)
    private String rewardEnergyA;
    /**
     *玩家已经领取过的能量B奖励等级
     */
    @IndexProp(id = 17)
    @Column(name = "rewardEnergyB", nullable = false)
    private String rewardEnergyB;
    @Transient
    private Set<Integer> rewardEnergySetA = new HashSet<>();
    @Transient
    private Set<Integer> rewardEnergySetB = new HashSet<>();
    @Transient
    private Map<Integer, Integer> rewardInfoMapA = new HashMap<>();
    @Transient
    private Map<Integer, Integer> rewardActivityMapA = new HashMap<>();
    @Transient
    private Map<Integer, Integer> rewardInfoMapB = new HashMap<>();
    @Transient
    private Map<Integer, Integer> rewardActivityMapB = new HashMap<>();

    public ShareGloryEntity() {
    }

    public ShareGloryEntity(String playerId, int termId) {
        this.playerId = playerId;
        this.termId = termId;
    }

    /**
     * 应该返还的道具列表
     * rewardActivity数量 根据公式： 赠送数量=max(int（累计获得数量*赠送比例）-已赠送数量,0)
     * 计算得来
     *
     * @return 格式：格式道具30000_itemID_数量,格式道具30000_itemID_数量 ......
     */
    public String getRewardActivityA() {
        return rewardActivityA;
    }

    /**
     * 应该返还的道具列表
     * rewardActivity数量 根据公式： 赠送数量=max(int（累计获得数量*赠送比例）-已赠送数量,0)
     * 计算得来
     *
     * @return 格式：格式道具30000_itemID_数量,格式道具30000_itemID_数量 ......
     */
    public String getRewardActivityB() {
        return rewardActivityB;
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
    public void beforeWrite() {
        this.rewardInfoA = SerializeHelper.mapToString(this.rewardInfoMapA);
        this.rewardActivityA = SerializeHelper.mapToString(this.rewardActivityMapA);
        this.rewardInfoB = SerializeHelper.mapToString(this.rewardInfoMapB);
        this.rewardActivityB = SerializeHelper.mapToString(this.rewardActivityMapB);

        this.rewardEnergyA = this.convert2String(rewardEnergySetA);
        this.rewardEnergyB = this.convert2String(rewardEnergySetB);
    }

    @Override
    public void afterRead() {
        this.rewardInfoMapA = SerializeHelper.stringToMap(this.rewardInfoA, Integer.class, Integer.class);
        this.rewardActivityMapA = SerializeHelper.stringToMap(this.rewardActivityA, Integer.class, Integer.class);
        this.rewardInfoMapB = SerializeHelper.stringToMap(this.rewardInfoB, Integer.class, Integer.class);
        this.rewardActivityMapB = SerializeHelper.stringToMap(this.rewardActivityB, Integer.class, Integer.class);

        this.rewardEnergySetA = SerializeHelper.stringToSet(Integer.class,
                this.rewardEnergyA, "_");
        this.rewardEnergySetB = SerializeHelper.stringToSet(Integer.class,
                this.rewardEnergyB, "_");
    }

    @Override
    public String getPrimaryKey() {
        return this.id;
    }

    @Override
    public void setPrimaryKey(String primaryKey) {
        this.id = primaryKey;
    }

    public Map<Integer, Integer> getRewardInfoMapA() {
        return rewardInfoMapA;
    }

    public void setRewardInfoListA(Map<Integer, Integer> rewardInfoMap) {
        this.rewardInfoMapA = rewardInfoMap;
    }

    public Map<Integer, Integer> getRewardInfoMapB() {
        return rewardInfoMapB;
    }

    public void setRewardInfoListB(Map<Integer, Integer> rewardInfoMap) {
        this.rewardInfoMapB = rewardInfoMap;
    }

    public void addRewardInfoA(int itemId, int number) {
        int count = rewardInfoMapA.containsKey(itemId) ? rewardInfoMapA.get(itemId) + number :
                number;
        this.rewardInfoMapA.put(itemId, count);
        this.notifyUpdate();
    }

    public void addRewardInfoB(int itemId, int number) {
        int count = rewardInfoMapB.containsKey(itemId) ? rewardInfoMapB.get(itemId) + number :
                number;
        this.rewardInfoMapB.put(itemId, count);
        this.notifyUpdate();
    }

    public Map<Integer, Integer> getRewardActivityMapA() {
        return rewardActivityMapA;
    }

    public void setRewardActivityMapA(Map<Integer, Integer> rewardActivityMap) {
        this.rewardActivityMapA = rewardActivityMap;
    }

    public void addRewardActivityA(int itemId, int number) {
        int count = this.rewardActivityMapA.containsKey(itemId) ?
                this.rewardActivityMapA.get(itemId) + number : number;

        this.rewardActivityMapA.put(itemId, count);
        this.notifyUpdate();
    }

    public void addRewardActivity(DonateItemType type, int itemId, int number) {
        if (type == DonateItemType.typeA) {
            addRewardActivityA(itemId, number);
        }else if(type == DonateItemType.typeB){
            addRewardActivityB(itemId, number);
        }
    }

    public Map<Integer, Integer> getRewardActivityMapB() {
        return rewardActivityMapB;
    }

    public void setRewardActivityMapB(Map<Integer, Integer> rewardActivityMap) {
        this.rewardActivityMapB = rewardActivityMap;
    }

    public void addRewardActivityB(int itemId, int number) {
        int count = this.rewardActivityMapB.containsKey(itemId) ?
                this.rewardActivityMapB.get(itemId) + number : number;

        this.rewardActivityMapB.put(itemId, count);
        this.notifyUpdate();
    }

    public Set<Integer> getRewardEnergyLevel(int type) {
        return type == DonateItemType.typeA.VAL ?
                this.rewardEnergySetA : this.rewardEnergySetB;
    }

    public void setRewardEnergyLevel(int level, int type) {
        if (type == DonateItemType.typeA.VAL) {
            this.rewardEnergySetA.add(level);
        } else {
            this.rewardEnergySetB.add(level);
        }
        this.notifyUpdate();
    }

    public void addDonateCount(int itemId, int number) {
        ShareGloryKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(ShareGloryKVCfg.class);
        if(itemId == cfg.getItemA()){
            this.donateCountA += number;
        }else if(itemId == cfg.getItemB()){
            this.donateCountB += number;
        }
        else{
            return;
        }
        this.notifyUpdate();
    }

    public int getDonateCount(int itemId) {
        ShareGloryKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(ShareGloryKVCfg.class);
        if(itemId == cfg.getItemA()){
            return this.donateCountA;
        }else if(itemId == cfg.getItemB()){
            return this.donateCountB;
        }
        return Integer.MAX_VALUE;
    }

    @Override
    public int getTermId() {
        return termId;
    }

    public void setTermId(int termId) {
        this.termId = termId;
    }

    public String getGuildid() {
        return guildid;
    }

    public void setGuildid(String guildid) {
        this.guildid = guildid;
        this.notifyUpdate();
    }

    private String convert2String(Set<Integer> list){

        if (list == null || list.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        String str = new String();
        for (Integer iLevel : list) {
            str = iLevel.toString() + "_";
            builder.append(str);
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }
}
