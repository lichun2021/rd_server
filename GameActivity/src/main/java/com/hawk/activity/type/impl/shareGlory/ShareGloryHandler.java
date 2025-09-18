package com.hawk.activity.type.impl.shareGlory;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.shareGlory.cfg.ShareGloryKVCfg;
import com.hawk.activity.type.impl.shareGlory.cfg.ShareGloryKVCfg.DonateItemType;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.HP;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;

/**
 * 联盟欢庆消息处理
 *
 * @author richard
 */
public class ShareGloryHandler extends ActivityProtocolHandler {

    /**
     * 道具捐献请求
     *
     * @param protocol
     * @param playerId
     */
    @ProtocolHandler(code = HP.code2.SHARE_GLORY_DONATE_REQ_VALUE)
    public void shareGloryDonateReq(HawkProtocol protocol, String playerId) {
        Activity.PBShareGloryDonateReq req =
                protocol.parseProtocol(Activity.PBShareGloryDonateReq.getDefaultInstance());

        ShareGloryActivity activity = this.getActivity(ActivityType.SHARE_GLORY_ACTIVITY);
        if (activity == null) {
            return;
        }

        if(!activity.isOpening(playerId)){
            return;
        }

        if (req.getCount() <= 0) {
            return;
        }

        ShareGloryKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(ShareGloryKVCfg.class);

        int itemId = 0;
        if (DonateItemType.typeA.VAL == req.getEnergyType()) {
            itemId = cfg.getItemA();
        } else if (DonateItemType.typeB.VAL == req.getEnergyType()) {
            itemId = cfg.getItemB();
        }
        //捐献类型不对，不处理
        if (0 == itemId) {
            return;
        }

        /**同步界面*/
        activity.onDonate(playerId, itemId, req.getCount(),activity.getActivityTermId());
    }

    /**
     * 本活动联盟数据请求
     *
     * @param protocol
     * @param playerId
     */
    @ProtocolHandler(code = HP.code2.SHARE_GLORY_ALLIANCE_ENERGY_REQ_VALUE)
    public void allianceInfoReq(HawkProtocol protocol, String playerId) {
        ShareGloryActivity activity = this.getActivity(ActivityType.SHARE_GLORY_ACTIVITY);
        if (activity == null) {
            return;
        }
        if(!activity.isOpening(playerId)){
            return;
        }
        activity.onAllianceInfoReq(playerId,activity.getActivityTermId());
    }

    /**
     * 玩家在本活动中的个人数据请求
     *
     * @param protocol
     * @param playerId
     */
    @ProtocolHandler(code = HP.code2.SHARE_GLORY_PLAYER_INFO_REQ_VALUE)
    public void shareGloryPlayerInfoReq(HawkProtocol protocol, String playerId) {
        ShareGloryActivity activity = this.getActivity(ActivityType.SHARE_GLORY_ACTIVITY);
        if (activity == null) {
            return;
        }
        if(!activity.isOpening(playerId)){
            return;
        }
        activity.onPlayerInfoReq(playerId,activity.getActivityTermId());
    }

    /**
     * 玩家在本活动中的个人数据请求
     *
     * @param protocol
     * @param playerId
     */
    @ProtocolHandler(code = HP.code2.SHARE_GLORY_DONATE_RANK_REQ_VALUE)
    public void shareGloryDonateRankReqReq(HawkProtocol protocol, String playerId) {
        ShareGloryActivity activity = this.getActivity(ActivityType.SHARE_GLORY_ACTIVITY);
        if (activity == null) {
            return;
        }
        if(!activity.isOpening(playerId)){
            return;
        }
        activity.onShareGloryDonateRankReq(playerId);
    }
}