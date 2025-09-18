package com.hawk.game.service.shop;

import com.hawk.game.GsConfig;
import com.hawk.game.entity.PlayerShopEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.msg.shop.ShopRefreshMsg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Shop.*;
import com.hawk.game.service.shop.impl.XhjzShop;
import com.hawk.game.service.shop.model.ShopServerData;
import com.hawk.game.util.GsConst;
import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppObj;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tickable.HawkPeriodTickable;
import org.hawk.xid.HawkXID;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ShopService extends HawkAppObj {
    Map<ShopType, ShopBase> shopMap = new ConcurrentHashMap<>();
    Map<ShopType, ShopServerData> shopServerDataMap = new ConcurrentHashMap<>();

    private static ShopService instance = null;

    public static ShopService getInstance() {
        return instance;
    }

    public ShopService(HawkXID xid) {
        super(xid);
        instance = this;
    }

    public boolean init(){
        loadServerDate();
        for(ShopEnum shopEnum : ShopEnum.values()){
            shopMap.put(shopEnum.getType(), shopEnum.getShop());
            if(!shopServerDataMap.containsKey(shopEnum.getType())){
                ShopServerData serverData = new ShopServerData();
                serverData.shopId = shopEnum.getType().getNumber();
                serverData.term = shopEnum.getShop().calShopCurTerm();
                serverData.save();
                shopServerDataMap.put(shopEnum.getType(), serverData);
            }
        }
        addTickable(new HawkPeriodTickable(1000) {
            @Override
            public void onPeriodTick() {
                List<ShopType> updateList = new ArrayList<>();
                for(ShopEnum shopEnum : ShopEnum.values()){
                    ShopServerData serverData = shopServerDataMap.get(shopEnum.getType());
                    if(serverData == null){
                        continue;
                    }
                    int oldTerm = serverData.term;
                    int curTerm = shopEnum.getShop().calShopCurTerm();
                    if(curTerm != oldTerm){
                        serverData.term = curTerm;
                        serverData.save();
                        updateList.add(shopEnum.getType());
                    }
                }
                if(!updateList.isEmpty()){
                    Set<String> playerIds = GlobalData.getInstance().getOnlinePlayerIds();
                    for (String playerId : playerIds) {
                        HawkApp.getInstance().postMsg(HawkXID.valueOf(GsConst.ObjType.PLAYER, playerId), ShopRefreshMsg.valueOf(updateList));
                    }
                }

            }
        });
        return true;
    }

    public void loadServerDate(){
        String serverKey = String.format(ShopRedisKey.SHOP_SERVER, GsConfig.getInstance().getServerId());
        Map<String, String> map = RedisProxy.getInstance().getRedisSession().hGetAll(serverKey);
        for(String str : map.values()){
            if (HawkOSOperator.isEmptyString(str)) {
                continue;
            }
            try {
                ShopServerData serverData = ShopServerData.unSerialize(str);
                if(serverData == null){
                    continue;
                }
                shopServerDataMap.put(ShopType.valueOf(serverData.shopId), serverData);
            } catch (Exception e) {
                HawkException.catchException(e);
            }
        }
    }

    public int getTerm(ShopType type){
        ShopServerData serverData = shopServerDataMap.get(type);
        return serverData == null ? 0 : serverData.term;
    }

    public ShopServerData getServerDate(ShopType type){
        return shopServerDataMap.get(type);
    }

    public void forceRefresh(ShopType type, long forceTime){
        ShopServerData serverData =  getServerDate(type);
        serverData.forceTime = forceTime;
        serverData.save();
        List<ShopType> updateList = new ArrayList<>();
        updateList.add(type);
        Set<String> playerIds = GlobalData.getInstance().getOnlinePlayerIds();
        for (String playerId : playerIds) {
            HawkApp.getInstance().postMsg(HawkXID.valueOf(GsConst.ObjType.PLAYER, playerId), ShopRefreshMsg.valueOf(updateList));
        }
    }

    public ShopDataPB.Builder info(Player player, ShopType type, PlayerShopEntity entity){
        return shopMap.get(type).info(player, entity);
    }

    public void check(Player player, ShopType type){
        shopMap.get(type).check(player, type);
    }

    public void exchange(Player player, ShopExchangeReq req){
        shopMap.get(req.getType()).exchange(player, req);
    }

    public void tip(Player player, ShopTipReq req){
        shopMap.get(req.getType()).tip(player, req);
    }
}
