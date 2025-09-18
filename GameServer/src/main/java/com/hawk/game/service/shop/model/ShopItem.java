package com.hawk.game.service.shop.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.protocol.Shop.*;

public class ShopItem {
    public int id;
    public int cnt;
    public int tip;
    public long time;

    public ShopItem(){

    }

    public ShopItem(int id){
        this.id = id;
        this.tip = 1;
    }

    public Object serialize() {
        return JSON.toJSON(this);
    }

    public static ShopItem unSerialize(JSONObject json) {
        if(json == null){
            return null;
        }
        return JSON.parseObject(json.toJSONString(), ShopItem.class);
    }

    public ShopItemPB.Builder toPB(){
        ShopItemPB.Builder builder = ShopItemPB.newBuilder();
        builder.setId(id);
        builder.setCount(cnt);
        builder.setTip(tip);
        return builder;
    }
}
