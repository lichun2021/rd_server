package com.hawk.game.service.shop.impl;

import com.hawk.game.config.CyborgShopItemCfg;
import com.hawk.game.config.CyborgShopKVCfg;
import com.hawk.game.config.ShopItemBaseCfg;
import com.hawk.game.entity.PlayerShopEntity;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Shop.*;
import com.hawk.game.service.cyborgWar.CLWActivityData;
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
public class CyborgShop extends ShopBase<CyborgShopKVCfg, CyborgShopItemCfg>{

    public CyborgShop(){

    }

    @Override
    public ShopType getType(){
        return ShopType.CYBORG_SHOP;
    }

    // Custom Code Begin
    // 此处为自定义代码
    public Action getCostAction(){
        return Action.CYBORG_BUY_ITEM;
    }
    public Action getGetAction(){
        return Action.CYBORG_BUY_ITEM;
    }

    public static final String EXT_PARAM_FORCE_TIME = "forceTime";

    @Override
    public PlayerShopEntity createEntity(String playerId) {
        PlayerShopEntity entity = super.createEntity(playerId);
        entity.getExtParamObj().put(EXT_PARAM_FORCE_TIME, HawkTime.getMillisecond());
        return entity;
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
                item.setIsHide(!canExchange(itemCfg));
            }
            shopData.addItems(item);
        }
        return shopData;
    }

    @Override
    public boolean isReset(PlayerShopEntity entity) {
        if(super.isReset(entity)){
            return true;
        }
        ShopServerData serverData = ShopService.getInstance().getServerDate(ShopType.CYBORG_SHOP);
        if(serverData == null){
            return false;
        }
        long now = HawkTime.getMillisecond();
        if(serverData.forceTime < now){
            long lastRefreshTime = entity.getExtParamObj().getLongValue(EXT_PARAM_FORCE_TIME);
            if(lastRefreshTime < serverData.forceTime){
                return true;
            }
        }
        return false;
    }

    @Override
    public void reset(PlayerShopEntity entity) {
        super.reset(entity);
        entity.getExtParamObj().put(EXT_PARAM_FORCE_TIME, HawkTime.getMillisecond());
        entity.notifyUpdate();
    }

    @Override
    public boolean canExchange(ShopItemBaseCfg itemCfg) {
        if(itemCfg.getLimitSeason() > 0){
            if(!CyborgLeaguaWarService.getInstance().isInSeason()){
                return false;
            }
            CLWActivityData clwData = CyborgLeaguaWarService.getInstance().getActivityData();
            if(itemCfg.getLimitSeason() != clwData.getSeason()){
                return false;
            }
        }
        return true;
    }

    // Custom Code End
}
