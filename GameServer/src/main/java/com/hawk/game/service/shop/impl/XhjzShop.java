package com.hawk.game.service.shop.impl;

import com.hawk.game.config.XhjzShopItemCfg;
import com.hawk.game.config.XhjzShopKVCfg;
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
public class XhjzShop extends ShopBase<XhjzShopKVCfg, XhjzShopItemCfg>{

    public XhjzShop(){

    }

    @Override
    public ShopType getType(){
        return ShopType.XHJZ_SHOP;
    }

    // Custom Code Begin
    // 此处为自定义代码
    public Action getCostAction(){
        return Action.XHJZ_SHOP_COST;
    }
    public Action getGetAction(){
        return Action.XHJZ_SHOP_GET;
    }
    // Custom Code End
}
