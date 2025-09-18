package com.hawk.game.service.shop;

import com.hawk.game.config.ShopItemBaseCfg;
import com.hawk.game.config.ShopKVBaseCfg;
import com.hawk.game.entity.PlayerShopEntity;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Shop.*;
import com.hawk.game.service.shop.model.ShopItem;
import com.hawk.log.Action;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public abstract class ShopBase<K extends ShopKVBaseCfg, T extends ShopItemBaseCfg> {
    public final Logger logger = LoggerFactory.getLogger("Server");

    public abstract ShopType getType();

    public Action getCostAction(){
        return Action.XHJZ_SHOP_COST;
    }
    public Action getGetAction(){
        return Action.XHJZ_SHOP_GET;
    }

    public Class<K> getKClass()
    {
        Class<K> tClass = (Class<K>)((ParameterizedTypeImpl)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        return tClass;
    }

    public Class<T> getTClass()
    {
        Class<T> tClass = (Class<T>)((ParameterizedTypeImpl)getClass().getGenericSuperclass()).getActualTypeArguments()[1];
        return tClass;
    }

    public K getKvCfg(){
        return HawkConfigManager.getInstance().getKVInstance(getKClass());
    }

    public T getItem(int cfgId){
        return HawkConfigManager.getInstance().getConfigByKey(getTClass(), cfgId);
    }

    public ConfigIterator<T> getIterator(){
        return HawkConfigManager.getInstance().getConfigIterator(getTClass());
    }

    public int calShopCurTerm(){
        ShopKVBaseCfg kvCfg = getKvCfg();
        long now = HawkTime.getMillisecond();
        long dur = now - kvCfg.getStartTimeValue();
        int term = (int)(dur / kvCfg.getRefreshTime()) + 1;
        return Math.max(1, term);
    }

    public void check(Player player, ShopType type){
        Optional<PlayerShopEntity> opEntity = player.getShopById(type.getNumber());
        if(opEntity.isPresent()) {
            PlayerShopEntity entity = opEntity.get();
            boolean isReset = isReset(entity);
            if(isReset){
                reset(entity);
            }else {
                checkItemAdd(entity);
            }
        }else {
            PlayerShopEntity entity = createEntity(player.getId());
            HawkDBManager.getInstance().create(entity);
            player.getData().getShopEntityList().add(entity);
        }
    }

    public ShopDataPB.Builder info(Player player, PlayerShopEntity entity){
        ShopDataPB.Builder shopData = ShopDataPB.newBuilder();
        shopData.setType(getType());
        shopData.setRefreshTime(getRefreshTime(getCurTerm()));
        for(ShopItem shopItem : entity.getShopItemMap().values()){
            shopData.addItems(shopItem.toPB());
        }
        return shopData;
    }

    public long getRefreshTime(int term){
        ShopKVBaseCfg kvCfg = getKvCfg();
        if(kvCfg == null){
            return HawkTime.getMillisecond() + TimeUnit.DAYS.toMillis(30);
        }
        return kvCfg.getStartTimeValue() + kvCfg.getRefreshTime() * term;
    }

    public int getCurTerm(){
        return ShopService.getInstance().getTerm(getType());
    }

    public PlayerShopEntity createEntity(String playerId){
        PlayerShopEntity entity = new PlayerShopEntity(playerId, getType().getNumber());
        for(ShopItemBaseCfg cfg : getIterator()){
            entity.getShopItemMap().put(cfg.getId(), new ShopItem(cfg.getId()));
        }
        entity.setTerm(getCurTerm());
        return entity;
    }

    public boolean isReset(PlayerShopEntity entity){
        return entity.getTerm() != getCurTerm();
    }

    public void reset(PlayerShopEntity entity){
        Map<Integer, ShopItem> shopItemMap = new HashMap<>();
        for(ShopItemBaseCfg cfg : getIterator()){
            shopItemMap.put(cfg.getId(), new ShopItem(cfg.getId()));
        }
        entity.setTerm(getCurTerm());
        entity.setShopItemMap(shopItemMap);
    }

    public void checkItemAdd(PlayerShopEntity entity){
        boolean isUpdate = false;
        for(ShopItemBaseCfg cfg : getIterator()){
            if(!entity.getShopItemMap().containsKey(cfg.getId())){
                entity.getShopItemMap().put(cfg.getId(), new ShopItem(cfg.getId()));
                isUpdate = true;
            }
        }
        if(isUpdate){
            entity.notifyUpdate();
        }
    }

    public boolean canExchange(ShopItemBaseCfg itemCfg){
        return true;
    }

    public int getMaxCount(Player player, ShopItemBaseCfg itemCfg){
        return itemCfg.getTimes();
    }

    public void exchange(Player player, ShopExchangeReq req){
        ShopItemBaseCfg itemCfg = getItem(req.getId());
        if(itemCfg == null){
            return;
        }
        if(!canExchange(itemCfg)){
            return;
        }
        Optional<PlayerShopEntity> opEntity = player.getShopById(req.getType().getNumber());
        if(opEntity.isPresent()){
            PlayerShopEntity entity = opEntity.get();
            ShopItem shopItem = entity.getShopItemMap().getOrDefault(req.getId(), new ShopItem(req.getId()));
            if(shopItem.cnt + req.getCount() > getMaxCount(player, itemCfg)){
                return;
            }
            ConsumeItems consume = ConsumeItems.valueOf();
            consume.addConsumeInfo(ItemInfo.valueListOf(itemCfg.getNeedItem(), req.getCount()));
            if(!consume.checkConsume(player)){
                return;
            }
            shopItem.cnt += req.getCount();
            entity.notifyUpdate();
            consume.consumeAndPush(player, getCostAction());
            AwardItems awardItems = AwardItems.valueOf();
            awardItems.addItemInfos(ItemInfo.valueListOf(itemCfg.getGainItem(), req.getCount()));
            awardItems.rewardTakeAffectAndPush(player, getGetAction(), true);
            ShopExchangeResp.Builder resp = ShopExchangeResp.newBuilder();
            resp.setType(req.getType());
            resp.setItem(shopItem.toPB());
            player.sendProtocol(HawkProtocol.valueOf(HP.code2.SHOP_EXCHANGE_RESP, resp));
        }
    }

    public void tip(Player player, ShopTipReq req){
        Optional<PlayerShopEntity> opEntity = player.getShopById(req.getType().getNumber());
        if(opEntity.isPresent()) {
            PlayerShopEntity entity = opEntity.get();
            ShopTipResp.Builder resp = ShopTipResp.newBuilder();
            resp.setType(req.getType());
            if(req.hasIsAll() && req.getIsAll()){
                for(ShopItem shopItem : entity.getShopItemMap().values()){
                    shopItem.tip = req.getTip();
                    resp.addItem(shopItem.toPB());
                }
            }else {
                ShopItem shopItem = entity.getShopItemMap().get(req.getId());
                if(shopItem != null){
                    shopItem.tip = req.getTip();
                    resp.addItem(shopItem.toPB());
                }
            }
            entity.notifyUpdate();
            player.sendProtocol(HawkProtocol.valueOf(HP.code2.SHOP_TIP_RESP, resp));
        }
    }
}
