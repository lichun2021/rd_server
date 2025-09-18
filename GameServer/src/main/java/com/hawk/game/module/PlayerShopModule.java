package com.hawk.game.module;

import com.hawk.game.entity.PlayerShopEntity;
import com.hawk.game.msg.shop.ShopRefreshMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Shop.*;
import com.hawk.game.service.shop.ShopService;
import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class PlayerShopModule extends PlayerModule {
    static Logger logger = LoggerFactory.getLogger("Server");
    /**
     * 构造函数
     *
     * @param player
     */
    public PlayerShopModule(Player player) {
        super(player);
    }

    @Override
    protected boolean onPlayerLogin() {
        for(ShopType type : ShopType.values()){
            ShopService.getInstance().check(player, type);
        }
        ShopInfoResp.Builder resp = ShopInfoResp.newBuilder();
        for(PlayerShopEntity entity : player.getData().getShopEntityList()){
            resp.addDatas(ShopService.getInstance().info(player, ShopType.valueOf(entity.getShopId()), entity));
        }
        player.sendProtocol(HawkProtocol.valueOf(HP.code2.SHOP_INFO_RESP, resp));
        return super.onPlayerLogin();
    }

    @ProtocolHandler(code = HP.code2.SHOP_INFO_REQ_VALUE)
    public void info(HawkProtocol hawkProtocol) {
        ShopInfoReq req = hawkProtocol.parseProtocol(ShopInfoReq.getDefaultInstance());
        ShopInfoResp.Builder resp = ShopInfoResp.newBuilder();
        for(ShopType type : req.getTypesList()){
            Optional<PlayerShopEntity> opEntity = player.getShopById(type.getNumber());
            opEntity.ifPresent(playerShopEntity -> resp.addDatas(ShopService.getInstance().info(player, type, playerShopEntity)));
        }
        player.sendProtocol(HawkProtocol.valueOf(HP.code2.SHOP_INFO_RESP, resp));
    }

    @ProtocolHandler(code = HP.code2.SHOP_EXCHANGE_REQ_VALUE)
    public void exchange(HawkProtocol hawkProtocol) {
        ShopExchangeReq req = hawkProtocol.parseProtocol(ShopExchangeReq.getDefaultInstance());
        ShopService.getInstance().exchange(player, req);
    }

    @ProtocolHandler(code = HP.code2.SHOP_TIP_REQ_VALUE)
    public void tip(HawkProtocol hawkProtocol) {
        ShopTipReq req = hawkProtocol.parseProtocol(ShopTipReq.getDefaultInstance());
        ShopService.getInstance().tip(player, req);
    }

    @MessageHandler
    private boolean onShopRefresh(ShopRefreshMsg msg){
        ShopInfoResp.Builder resp = ShopInfoResp.newBuilder();
        for(ShopType type : ShopType.values()){
            ShopService.getInstance().check(player, type);
            Optional<PlayerShopEntity> opEntity = player.getShopById(type.getNumber());
            opEntity.ifPresent(playerShopEntity -> resp.addDatas(ShopService.getInstance().info(player, type, playerShopEntity)));
        }
        player.sendProtocol(HawkProtocol.valueOf(HP.code2.SHOP_INFO_RESP, resp));
        return true;
    }
}
