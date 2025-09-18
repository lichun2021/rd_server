package com.hawk.game.module.homeland.rank;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.game.crossproxy.CrossProxy;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisKey;
import com.hawk.game.module.homeland.PlayerHomeLandModule;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.HomeLand;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SysProtocol;
import com.hawk.game.util.GsConst;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkAppObj;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.redis.HawkRedisSession;
import org.hawk.xid.HawkXID;

public class HomeLandService extends HawkAppObj {
    public static HomeLandService instance;
    //本服排行榜
    HomeLandLocalRankProvider localRank = new HomeLandLocalRankProvider();
    //跨服排行榜
    HomeLandCrossRankProvider crossRank = new HomeLandCrossRankProvider();
    //联盟排行榜
    HomeLandGuildRankProvider guildRank = new HomeLandGuildRankProvider();


    public HomeLandService(HawkXID xid) {
        super(xid);
        instance = this;
    }

    @Override
    public boolean onTick() {
        localRank.onTick();
        crossRank.onTick();
        guildRank.onTick();
        return super.onTick();
    }

    public static HomeLandService getInstance() {
        return instance;
    }

    public boolean init() {
        localRank.init();
        crossRank.init();
        guildRank.init();
        return true;
    }

    public void updatePlayerInfo(HomeLandPlayerRankInfo info) {
        HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
        redisSession.hSet(RedisKey.HOME_LAND_RANK_PLAYER, info.getPlayerId(), SerializeHelper.toJson(info));
    }

    public HomeLandRankImpl getRankByType(HomeLandRankType rankType, HomeLandRankServerType serverType, String guildId) {
        HomeLandRankImpl rank = null;
        switch (serverType) {
            case LOCAL:
                rank = localRank.getRankByType(rankType);
                break;
            case CROSS:
                rank = crossRank.getRankByType(rankType);
                break;
            case GUILD:
                rank = guildRank.getRankByType(rankType, guildId);
                break;
            default:
                break;
        }
        return rank;
    }

    public void updateRank(HomeLandRankType rankType, HomeLandRank param, HomeLandPlayerRankInfo playerInfo) {
        localRank.updateRank(rankType, param);
        crossRank.updateRank(rankType, param);
        guildRank.updateRank(rankType, param);
        updatePlayerInfo(playerInfo);
    }

    /**
     * A->跨服拜访家园
     *
     * @param crossReq
     */
    public void visitHomeView(HomeLand.HomeLandBuildingInfoReq.Builder crossReq) {
        HomeLand.PBHomeLandServiceReq.Builder csReq = HomeLand.PBHomeLandServiceReq.newBuilder();
        csReq.setProtoclType(HP.code2.HOME_BUILDING_INFO_C_VALUE);
        csReq.setReq(crossReq.build().toByteString());
        HawkProtocol sendProto = HawkProtocol.valueOf(HP.code2.HOME_BUILDING_SERVICE_REQ_VALUE, csReq);
        CrossProxy.getInstance().sendNotify(sendProto, crossReq.getTargetServerId(), null);
    }

    /**
     * A->跨服拜访家园
     *
     * @param crossReq
     */
    public void likeHomeView(HomeLand.HomeLandThemeLikeReq.Builder crossReq) {
        HomeLand.PBHomeLandServiceReq.Builder csReq = HomeLand.PBHomeLandServiceReq.newBuilder();
        csReq.setProtoclType(HP.code2.HOME_BUILDING_THEME_LIKE_C_VALUE);
        csReq.setReq(crossReq.build().toByteString());
        HawkProtocol sendProto = HawkProtocol.valueOf(HP.code2.HOME_BUILDING_SERVICE_REQ_VALUE, csReq);
        CrossProxy.getInstance().sendNotify(sendProto, crossReq.getTargetServerId(), null);
    }


    /**
     * B->接收家园
     *
     * @param protocol
     * @return
     */
    @ProtocolHandler(code = HP.code2.HOME_BUILDING_SERVICE_REQ_VALUE)
    public boolean onHomeInfo(HawkProtocol protocol) {
        HomeLand.PBHomeLandServiceReq req = protocol.parseProtocol(HomeLand.PBHomeLandServiceReq.getDefaultInstance());
        final int protoType = req.getProtoclType();
        switch (protoType) {
            case HP.code2.HOME_BUILDING_INFO_C_VALUE:
                HomeLand.HomeLandBuildingInfoReq.Builder homeInfoReq = HomeLand.HomeLandBuildingInfoReq.newBuilder();
                try {
                    homeInfoReq.mergeFrom(req.getReq());
                } catch (InvalidProtocolBufferException e) {
                    HawkException.catchException(e);
                }
                Player targetPlayer = GlobalData.getInstance().makesurePlayer(homeInfoReq.getTargetPlayerId());
                if (targetPlayer == null) {
                    String mainServerId = GlobalData.getInstance().getMainServerId(homeInfoReq.getServerId());
                    CrossProxy.getInstance().sendProtocol(HawkProtocol.valueOf(HP.sys.ERROR_CODE, SysProtocol.HPErrorCode.newBuilder().setHpCode(protocol.getType())
                            .setErrCode(Status.SysError.PLAYER_INVALID_VALUE)
                            .setErrFlag(0)), mainServerId, homeInfoReq.getPlayerId());
                    return false;
                }
                PlayerHomeLandModule module = targetPlayer.getModule(GsConst.ModuleType.HOME_LAND_MODULE);
                module.onHomeInfo(HawkProtocol.valueOf(protoType, homeInfoReq));
                break;
            case HP.code2.HOME_BUILDING_THEME_LIKE_C_VALUE:
                HomeLand.HomeLandThemeLikeReq.Builder likeInfoReq = HomeLand.HomeLandThemeLikeReq.newBuilder();
                try {
                    likeInfoReq.mergeFrom(req.getReq());
                } catch (InvalidProtocolBufferException e) {
                    HawkException.catchException(e);
                }
                Player likeInfoPlayer = GlobalData.getInstance().makesurePlayer(likeInfoReq.getTargetPlayerId());
                if (likeInfoPlayer == null) {
                    String mainServerId = GlobalData.getInstance().getMainServerId(likeInfoReq.getServerId());
                    CrossProxy.getInstance().sendProtocol(HawkProtocol.valueOf(HP.sys.ERROR_CODE, SysProtocol.HPErrorCode.newBuilder().setHpCode(protocol.getType())
                            .setErrCode(Status.SysError.PLAYER_INVALID_VALUE)
                            .setErrFlag(0)), mainServerId, likeInfoReq.getPlayerId());
                    return false;
                }
                PlayerHomeLandModule likeInfoModule = likeInfoPlayer.getModule(GsConst.ModuleType.HOME_LAND_MODULE);
                likeInfoModule.onHomeLike(HawkProtocol.valueOf(protoType, likeInfoReq));
            default:
                break;
        }
        return true;
    }
}
