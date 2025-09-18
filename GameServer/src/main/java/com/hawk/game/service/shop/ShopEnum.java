package com.hawk.game.service.shop;

import com.hawk.game.protocol.Shop.*;
import com.hawk.game.service.shop.impl.XhjzShop;
import com.hawk.game.service.shop.impl.CyborgShop;
import com.hawk.game.service.shop.impl.DyzzShop;
import com.hawk.game.service.shop.impl.LmjyShop;
import com.hawk.game.service.shop.impl.FgylShop;
import com.hawk.game.service.shop.impl.CrossShop;

/**
 * 本文件通过AutoGenShop自动生成，请不要手动修改，重新生成会被覆盖，如果要修改请修改模板
 */
public enum ShopEnum {
    XHJZ_SHOP(ShopType.XHJZ_SHOP, new XhjzShop()),
    CYBORG_SHOP(ShopType.CYBORG_SHOP, new CyborgShop()),
    DYZZ_SHOP(ShopType.DYZZ_SHOP, new DyzzShop()),
    LMJY_SHOP(ShopType.LMJY_SHOP, new LmjyShop()),
    FGYL_SHOP(ShopType.FGYL_SHOP, new FgylShop()),
    CROSS_SHOP(ShopType.CROSS_SHOP, new CrossShop()),
    ;

    private ShopType type;
    private ShopBase shop;

    ShopEnum(ShopType type, ShopBase shop){
        this.type = type;
        this.shop = shop;
    }

    public ShopType getType() {
        return type;
    }

    public ShopBase getShop() {
        return shop;
    }
}