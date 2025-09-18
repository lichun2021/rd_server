package com.hawk.game.service.shop.impl;

import com.hawk.game.config.LmjyShopItemCfg;
import com.hawk.game.config.LmjyShopKVCfg;
import com.hawk.game.config.ShopItemBaseCfg;
import com.hawk.game.entity.PlayerShopEntity;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Shop.*;
import com.hawk.game.service.shop.ShopBase;
import com.hawk.game.service.shop.ShopService;
import com.hawk.game.service.shop.model.ShopItem;
import com.hawk.game.service.shop.model.ShopServerData;
import com.hawk.log.Action;
import org.hawk.os.HawkTime;
import com.hawk.game.util.GsConst;
import com.hawk.game.service.cyborgWar.CyborgLeaguaWarService;

/**
 * 本文件通过AutoGenShop自动生成，请不要修改自定义代码以外的部分，重新生成会被覆盖，如果要修改请修改模板
 */
public class LmjyShop extends ShopBase<LmjyShopKVCfg, LmjyShopItemCfg>{

    public LmjyShop(){

    }

    @Override
    public ShopType getType(){
        return ShopType.LMJY_SHOP;
    }

    // Custom Code Begin
    // 此处为自定义代码
    public Action getCostAction(){
        return Action.BUY_MILITARY_ITEM;
    }
    public Action getGetAction(){
        return Action.BUY_MILITARY_ITEM;
    }

    @Override
    public ShopDataPB.Builder info(Player player, PlayerShopEntity entity) {
        ShopDataPB.Builder shopData = ShopDataPB.newBuilder();
        shopData.setType(getType());
        shopData.setRefreshTime(getRefreshTime(getCurTerm()));
        for(ShopItem shopItem : entity.getShopItemMap().values()){
            ShopItemPB.Builder item = shopItem.toPB();
            ShopItemBaseCfg itemCfg = getItem(shopItem.id);
            if(itemCfg != null){
                item.setMaxCount(getMaxCount(player, itemCfg));
            }
            shopData.addItems(item);
        }
        return shopData;
    }

    @Override
    public int getMaxCount(Player player, ShopItemBaseCfg itemCfg) {
        double eff645 = player.getEffect().getEffVal(Const.EffType.LIFE_TIME_CARD_645) * GsConst.EFF_PER + 1;
        return (int)(itemCfg.getTimes() * eff645);
    }

    // Custom Code End
}
