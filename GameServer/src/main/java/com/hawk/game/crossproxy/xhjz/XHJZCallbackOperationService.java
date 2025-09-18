package com.hawk.game.crossproxy.xhjz;

import com.hawk.game.GsConfig;
import com.hawk.game.crossproxy.CrossProxy;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Cross;
import com.hawk.game.protocol.Cross.XHJZCrossMsg;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Login;
import com.hawk.game.protocol.Status;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.log.LogConst;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;

public class XHJZCallbackOperationService {
    private static final XHJZCallbackOperationService instance = new XHJZCallbackOperationService();

    public static XHJZCallbackOperationService getInstance() {
        return instance;
    }

    private XHJZCallbackOperationService() {

    }

    /**
     * 预处理返回.
     *
     * @param player
     * @param hawkProtocol
     * @param enterCrossMsg
     */
    public void onPrepareCrossBack(Player player, HawkProtocol hawkProtocol, XHJZCrossMsg enterCrossMsg) {
        Cross.RpcCommonResp rpcCommonResp = hawkProtocol.parseProtocol(Cross.RpcCommonResp.getDefaultInstance());
        Player.logger.info("xhjz playerId:{} received inner enter cross errorCode:{}", player.getId(), rpcCommonResp.getErrorCode());

        if (rpcCommonResp.getErrorCode() == Status.SysError.SUCCESS_OK_VALUE) {
            onPrepareCrossOk(player, enterCrossMsg);
        } else {
            onPrepareCrossFail(player);
        }
    }

    /**
     * 预跨服成功之后把玩家的信息
     * @param crossMsg
     */
    public void onPrepareCrossOk(Player player, XHJZCrossMsg crossMsg) {
        String targetServerId = crossMsg.getServerId();
        //添加到本服的其它跨服玩家
        CrossService.getInstance().addEmigrationPlayer(player.getId(), targetServerId);
        // 通知客户端清理数据
        player.sendProtocol(HawkProtocol.valueOf(HP.code2.XHJZ_WAR_CROSS_BEGIN));

        //记录日志
        LogUtil.logPlayerCross(player, targetServerId, LogConst.CrossStateType.CROSS_START, Cross.CrossType.XHJZ);

        //构建一条带跨服信息的登录协议发送到目标服.
        Login.HPLogin.Builder loginBuilder = player.getHpLogin();
        Cross.InnerEnterCrossMsg.Builder innerBuilder = loginBuilder.getInnerEnterCrossMsgBuilder();
        if (innerBuilder == null) {
            innerBuilder = Cross.InnerEnterCrossMsg.newBuilder();
            loginBuilder.setInnerEnterCrossMsg(innerBuilder);
        }
        innerBuilder.setXhjzCrossMsg(crossMsg);
        innerBuilder.setPlayerNmae(player.getName());
        String guildId = player.getGuildId();
        guildId = guildId == null ? "" : guildId;
        innerBuilder.setGuildId(guildId);
        innerBuilder.setCrossType(Cross.CrossType.XHJZ_VALUE);
        innerBuilder.setGuildAuth(player.getGuildAuthority());


        //玩家发起跨服的时候其它协议是不能处理的，所以这里走rpc
        Login.HPLogin.Builder cloneLoginBuilder = loginBuilder.clone();
        cloneLoginBuilder.setFlag(1);
        HawkProtocol hawkProtocol = HawkProtocol.valueOf(HP.code.LOGIN_C_VALUE, cloneLoginBuilder);
        CrossProxy.getInstance().rpcRequest(hawkProtocol, new XHJZEnterCallback(player), targetServerId, player.getId(), player.getId());
    }


    /** 在玩家线程.
     *  预跨服失败的时候需要把玩家给找回来.
     */
    public void onPrepareCrossFail(Player player) {
        Player.logger.info("xhjz prepare cross fail playerId:{}", player.getId());
        player.setCrossStatus(GsConst.PlayerCrossStatus.NOTHING);
        //放在最前,防止后面的逻辑判断有问题.
        String toServerId = CrossService.getInstance().removeEmigrationPlayer(player.getId());
        toServerId = toServerId == null ? "NULL" : toServerId;
        // 通知客户端清理数据
        player.sendProtocol(HawkProtocol.valueOf(HP.code2.XHJZ_WAR_CROSS_FINISH));

        //记录泰伯利亚失败.
        LogUtil.logPlayerCross(player, toServerId, LogConst.CrossStateType.CROSS_FAIL, Cross.CrossType.XHJZ);

        //模拟login协议需要的数据.
        AccountInfo accoutnInfo = GlobalData.getInstance().getAccountInfoByPlayerId(player.getId());
        accoutnInfo.setLoginTime(HawkTime.getMillisecond());
        HawkProtocol loginProtocol = HawkProtocol.valueOf(HP.code.LOGIN_C_VALUE, player.getHpLogin());
        player.getSession().setUserObject("account", accoutnInfo);
        loginProtocol.bindSession(player.getSession());
        player.onProtocol(loginProtocol);

        //在login的时候会加，所以这减掉
        GlobalData.getInstance().changePfOnlineCnt(player, false);

        //如果失败了，就需要把玩家的状态给清理掉.
        boolean setCrossStatus = RedisProxy.getInstance().setPlayerCrossStatus(GsConfig.getInstance().getServerId(), player.getId(), GsConst.PlayerCrossStatus.NOTHING);
        if (!setCrossStatus) {
            Player.logger.error("playerId:{} prepare cross fail fix player set cross status fail", player.getId());
        }
    }
}
