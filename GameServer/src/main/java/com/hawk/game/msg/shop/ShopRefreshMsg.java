package com.hawk.game.msg.shop;

import com.hawk.game.protocol.Shop.*;
import org.hawk.msg.HawkMsg;

import java.util.List;

public class ShopRefreshMsg extends HawkMsg {
    private List<ShopType> updateList;

    public ShopRefreshMsg() {
    }

    public static ShopRefreshMsg valueOf(List<ShopType> updateList){
        ShopRefreshMsg msg = new ShopRefreshMsg();
        msg.updateList = updateList;
        return msg;
    }

    public List<ShopType> getUpdateList() {
        return updateList;
    }
}
