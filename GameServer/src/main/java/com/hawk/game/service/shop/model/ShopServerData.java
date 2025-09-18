package com.hawk.game.service.shop.model;

import com.alibaba.fastjson.JSON;
import com.hawk.game.GsConfig;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.service.shop.ShopRedisKey;
import org.hawk.os.HawkOSOperator;

public class ShopServerData {
    public int shopId;
    public int term;
    public long forceTime;

    public String serialize() {
        return JSON.toJSONString(this);
    }

    public static ShopServerData unSerialize(String json) {
        if (HawkOSOperator.isEmptyString(json)) {
            return null;
        }
        return JSON.parseObject(json, ShopServerData.class);
    }

    public void save(){
        String key = String.format(ShopRedisKey.SHOP_SERVER, GsConfig.getInstance().getServerId());
        RedisProxy.getInstance().getRedisSession().hSet(key, String.valueOf(shopId), serialize());
    }
}
