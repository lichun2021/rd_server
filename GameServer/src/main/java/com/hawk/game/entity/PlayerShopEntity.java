package com.hawk.game.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.protocol.Shop.*;
import com.hawk.game.service.shop.ShopService;
import com.hawk.game.service.shop.model.ShopItem;
import org.hawk.annotation.IndexProp;
import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Entity
@Table(name = "shop")
public class PlayerShopEntity extends HawkDBEntity {
    @Id
    @GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
    @GeneratedValue(generator = "uuid")
    @Column(name = "id", unique = true, nullable = false)
    @IndexProp(id = 1)
    private String id;

    @Column(name = "playerId", nullable = false)
    @IndexProp(id = 2)
    private String playerId = "";

    @Column(name = "shopId", nullable = false)
    @IndexProp(id = 3)
    private int shopId;

    @Column(name = "term", nullable = false)
    @IndexProp(id = 4)
    private int term;

    @Column(name = "createTime", nullable = false)
    @IndexProp(id = 5)
    protected long createTime = 0;

    @Column(name = "updateTime")
    @IndexProp(id = 6)
    protected long updateTime = 0;

    @Column(name = "invalid")
    @IndexProp(id = 7)
    protected boolean invalid;

    @Column(name = "itemData")
    @IndexProp(id = 8)
    private String itemData="";

    @Column(name = "extParam")
    @IndexProp(id = 9)
    private String extParam="";

    public PlayerShopEntity(){

    }

    public PlayerShopEntity(String playerId, int shopId){
        this.playerId = playerId;
        this.shopId = shopId;
    }

    @Transient
    private Map<Integer, ShopItem> shopItemMap = new HashMap<>();

    @Transient
    private JSONObject extParamObj = new JSONObject();

    @Override
    public void beforeWrite() {
        JSONArray itemArr = new JSONArray();
        for(ShopItem shopItem : shopItemMap.values()){
            itemArr.add(shopItem.serialize());
        }
        itemData = itemArr.toJSONString();
        extParam = extParamObj.toJSONString();
        super.beforeWrite();
    }

    @Override
    public void afterRead() {
        if (!HawkOSOperator.isEmptyString(itemData)) {
            JSONArray itemArr = JSONArray.parseArray(itemData);
            Map<Integer, ShopItem> tmp = new HashMap<>();
            for (int i = 0; i < itemArr.size(); i++) {
                JSONObject itemObj = itemArr.getJSONObject(i);
                ShopItem shopItem = ShopItem.unSerialize(itemObj);
                tmp.put(shopItem.id, shopItem);
            }
            shopItemMap = tmp;
        }
        if (!HawkOSOperator.isEmptyString(extParam)) {
            extParamObj = JSON.parseObject(extParam);
        }
        super.afterRead();
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
    public String getOwnerKey() {
        return playerId;
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

    public int getShopId() {
        return shopId;
    }

    public void setShopId(int shopId) {
        this.shopId = shopId;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
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

    public Map<Integer, ShopItem> getShopItemMap() {
        return shopItemMap;
    }

    public void setShopItemMap(Map<Integer, ShopItem> shopItemMap) {
        this.shopItemMap = shopItemMap;
    }

    public JSONObject getExtParamObj() {
        return extParamObj;
    }
}
