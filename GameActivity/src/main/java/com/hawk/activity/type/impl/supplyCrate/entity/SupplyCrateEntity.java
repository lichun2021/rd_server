package com.hawk.activity.type.impl.supplyCrate.entity;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.supplyCrate.cfg.SupplyCrateKVCfg;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.annotation.IndexProp;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkOSOperator;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "activity_supply_crate")
public class SupplyCrateEntity extends HawkDBEntity implements IActivityDataEntity {
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

    @IndexProp(id = 9)
    @Column(name = "round", nullable = false)
    private int round;

    @IndexProp(id = 10)
    @Column(name = "isCanOPen", nullable = false)
    private boolean isCanOPen;

    @IndexProp(id = 11)
    @Column(name = "isCanNext", nullable = false)
    private boolean isCanNext;

    @IndexProp(id = 12)
    @Column(name = "mult", nullable = false)
    private int mult;

    @IndexProp(id = 13)
    @Column(name = "crateItems", nullable = false)
    private String crateItems = "";

    @IndexProp(id = 14)
    @Column(name = "boxProg", nullable = false)
    private int boxProg;

    @IndexProp(id = 15)
    @Column(name = "customIndex", nullable = false)
    private int customIndex;

    @IndexProp(id = 16)
    @Column(name = "openItems", nullable = false)
    private String openItems = "";

    @IndexProp(id = 17)
    @Column(name = "isCanDouble", nullable = false)
    private boolean isCanDouble;

    @IndexProp(id = 18)
    @Column(name = "guildBoxProg", nullable = false)
    private int guildBoxProg;

    @IndexProp(id = 19)
    @Column(name = "boxCount", nullable = false)
    private int boxCount;


    /** 活动成就 */
    @Transient
    private List<AchieveItem> itemList = new ArrayList<AchieveItem>();

    @Transient
    private List<SupplyCrateItemObj> crateItemList = new ArrayList<>();

    @Transient
    private List<SupplyCrateItemObj> openItemList = new ArrayList<>();

    public SupplyCrateEntity() {

    }

    public SupplyCrateEntity(String playerId, int termId) {
        this.playerId = playerId;
        this.termId = termId;
        this.round = 1;
    }

    @Override
    public void beforeWrite() {
        //成就数据转换成字符串
        this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
        JSONArray crateArr = new JSONArray();
        for(SupplyCrateItemObj obj : crateItemList){
            crateArr.add(obj.serialize());
        }
        this.crateItems = crateArr.toJSONString();
        JSONArray openArr = new JSONArray();
        for(SupplyCrateItemObj obj : openItemList){
            openArr.add(obj.serialize());
        }
        this.openItems = openArr.toJSONString();
    }

    @Override
    public void afterRead() {
        //字符串转换成成就数据
        this.itemList = SerializeHelper.stringToList(AchieveItem.class, this.achieveItems);
        if (!HawkOSOperator.isEmptyString(crateItems)) {
            List<SupplyCrateItemObj> tmpList = new ArrayList<>();
            JSONArray crateArr = JSONArray.parseArray(crateItems);
            for (int i = 0; i < crateArr.size(); i++) {
                JSONObject obj = crateArr.getJSONObject(i);
                tmpList.add(SupplyCrateItemObj.unSerialize(obj));
            }
            this.crateItemList = tmpList;
        }
        if (!HawkOSOperator.isEmptyString(openItems)) {
            List<SupplyCrateItemObj> tmpList = new ArrayList<>();
            JSONArray openArr = JSONArray.parseArray(openItems);
            for (int i = 0; i < openArr.size(); i++) {
                JSONObject obj = openArr.getJSONObject(i);
                tmpList.add(SupplyCrateItemObj.unSerialize(obj));
            }
            this.openItemList = tmpList;
        }
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

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public boolean isCanNext() {
        return isCanNext;
    }

    public void setCanNext(boolean canNext) {
        isCanNext = canNext;
    }

    public boolean isCanOPen() {
        return isCanOPen;
    }

    public void setCanOPen(boolean canOPen) {
        isCanOPen = canOPen;
    }

    public int getMult() {
        return mult;
    }

    public void setMult(int mult) {
        this.mult = mult;
    }

    public int getBoxProg() {
        return boxProg;
    }

    public void setBoxProg(int boxProg) {
        this.boxProg = boxProg;
    }

    public void addBoxProg(int add){
        SupplyCrateKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(SupplyCrateKVCfg.class);
        if(boxCount >= kvCfg.getBoxMax()){
            return;
        }
        int newCount = boxCount + add;
        if(newCount > kvCfg.getBoxMax()){
            newCount = kvCfg.getBoxMax();
        }
        int realAdd = newCount - boxCount;
        this.boxProg = this.boxProg + realAdd;
        this.boxCount = newCount;
        notifyUpdate();
    }

    public int getCustomIndex() {
        return customIndex;
    }

    public void setCustomIndex(int customIndex) {
        this.customIndex = customIndex;
    }

    public List<SupplyCrateItemObj> getCrateItemList() {
        return crateItemList;
    }

    public void setCrateItemList(List<SupplyCrateItemObj> crateItemList) {
        this.crateItemList = crateItemList;
    }

    public List<SupplyCrateItemObj> getOpenItemList() {
        return openItemList;
    }

    public void setOpenItemList(List<SupplyCrateItemObj> openItemList) {
        this.openItemList = openItemList;
    }

    public boolean isCanDouble() {
        return isCanDouble;
    }

    public void setCanDouble(boolean canDouble) {
        isCanDouble = canDouble;
    }

    public int getGuildBoxProg() {
        return guildBoxProg;
    }

    public void setGuildBoxProg(int guildBoxProg) {
        this.guildBoxProg = guildBoxProg;
    }

    public int getBoxCount() {
        return boxCount;
    }

    public void setBoxCount(int boxCount) {
        this.boxCount = boxCount;
    }
}
