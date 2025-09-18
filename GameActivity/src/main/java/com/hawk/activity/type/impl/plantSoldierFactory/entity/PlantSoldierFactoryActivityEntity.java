package com.hawk.activity.type.impl.plantSoldierFactory.entity;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.plantSoldierFactory.PlantSoldierFactoryActivity;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkOSOperator;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "activity_plant_soldier_factory")
public class PlantSoldierFactoryActivityEntity extends HawkDBEntity implements IActivityDataEntity {
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
    private String achieveItems = "";

    @IndexProp(id = 8)
    @Column(name = "resetTime", nullable = false)
    private long resetTime;

    /** 购买数量信息 */
    @IndexProp(id = 9)
    @Column(name = "buyInfo", nullable = false)
    private String buyInfo = "";

    /** 奖励信息 */
    @IndexProp(id = 10)
    @Column(name = "awardInfo", nullable = false)
    private String awardInfo = "";

    /** 奖励信息 */
    @IndexProp(id = 11)
    @Column(name = "bigAwardInfo", nullable = false)
    private String bigAwardInfo = "";

    @IndexProp(id = 12)
    @Column(name = "drawCount", nullable = false)
    private int drawCount;

    @IndexProp(id = 13)
    @Column(name = "drawTotalCount", nullable = false)
    private int drawTotalCount;


    /** 活动成就 */
    @Transient
    private List<AchieveItem> itemList = new ArrayList<AchieveItem>();

    /** 商店商品购买数量  */
    @Transient
    private Map<Integer, Integer> shopItemMap = new HashMap<>();

    @Transient
    private Map<Integer, PlantSoldierFactoryItemObj> awardMap = new HashMap<>();

    @Transient
    private Map<Integer, PlantSoldierFactoryItemObj> bigAwardMap = new HashMap();

    public PlantSoldierFactoryActivityEntity(){

    }

    public PlantSoldierFactoryActivityEntity(String playerId, int termId){
        this.playerId = playerId;
        this.termId = termId;
    }

    @Override
    public void beforeWrite() {
        this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
        if(awardMap.size() == PlantSoldierFactoryActivity.AWARD_COUNT){
            JSONArray awardArr = new JSONArray();
            for(int i=1; i<= PlantSoldierFactoryActivity.AWARD_COUNT; i++){
                PlantSoldierFactoryItemObj obj = awardMap.get(i);
                awardArr.add(obj.serialize());
            }
            this.awardInfo = awardArr.toJSONString();
        }
        if(bigAwardMap.size() == PlantSoldierFactoryActivity.BIG_AWARD_COUNT){
            JSONArray bigAwardArr = new JSONArray();
            for(int i=1; i<= PlantSoldierFactoryActivity.BIG_AWARD_COUNT; i++){
                PlantSoldierFactoryItemObj obj = bigAwardMap.get(i);
                bigAwardArr.add(obj.serialize());
            }
            this.bigAwardInfo = bigAwardArr.toJSONString();
        }
        this.buyInfo = SerializeHelper.mapToString(shopItemMap);
    }

    @Override
    public void afterRead() {
        //字符串转换成成就数据
        this.itemList = SerializeHelper.stringToList(AchieveItem.class, this.achieveItems);
        if (!HawkOSOperator.isEmptyString(awardInfo)) {
            Map<Integer, PlantSoldierFactoryItemObj> tmp = new HashMap<>();
            JSONArray crateArr = JSONArray.parseArray(awardInfo);
            for (int i = 0; i < crateArr.size(); i++) {
                JSONObject obj = crateArr.getJSONObject(i);
                PlantSoldierFactoryItemObj itemObj = PlantSoldierFactoryItemObj.unSerialize(obj);
                tmp.put(itemObj.getPos(), itemObj);
            }
            this.awardMap = tmp;
        }
        if (!HawkOSOperator.isEmptyString(bigAwardInfo)) {
            Map<Integer, PlantSoldierFactoryItemObj> tmp = new HashMap<>();
            JSONArray openArr = JSONArray.parseArray(bigAwardInfo);
            for (int i = 0; i < openArr.size(); i++) {
                JSONObject obj = openArr.getJSONObject(i);
                PlantSoldierFactoryItemObj itemObj = PlantSoldierFactoryItemObj.unSerialize(obj);
                tmp.put(itemObj.getPos(), itemObj);
            }
            this.bigAwardMap = tmp;
        }
        shopItemMap = SerializeHelper.stringToMap(buyInfo, Integer.class, Integer.class);
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

    public Map<Integer, PlantSoldierFactoryItemObj> getAwardMap() {
        return awardMap;
    }

    public void setAwardMap(Map<Integer, PlantSoldierFactoryItemObj> awardMap) {
        this.awardMap = awardMap;
    }

    public Map<Integer, PlantSoldierFactoryItemObj> getBigAwardMap() {
        return bigAwardMap;
    }

    public void setBigAwardMap(Map<Integer, PlantSoldierFactoryItemObj> bigAwardMap) {
        this.bigAwardMap = bigAwardMap;
    }

    public Map<Integer, Integer> getShopItemMap() {
        return shopItemMap;
    }

    public int getDrawCount() {
        return drawCount;
    }

    public void setDrawCount(int drawCount) {
        this.drawCount = drawCount;
    }

    public int getDrawTotalCount() {
        return drawTotalCount;
    }

    public void setDrawTotalCount(int drawTotalCount) {
        this.drawTotalCount = drawTotalCount;
    }
}
