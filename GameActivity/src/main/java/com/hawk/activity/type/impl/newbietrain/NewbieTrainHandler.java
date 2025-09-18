package com.hawk.activity.type.impl.newbietrain;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.NoviceGiftBuyReq;
import com.hawk.game.protocol.Activity.NoviceTrainRecordReq;
import com.hawk.game.protocol.Activity.NoviceTrainReq;
import com.hawk.game.protocol.Activity.NoviceTrainSelectReq;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

/**
 * 新兵作训
 */
public class NewbieTrainHandler extends ActivityProtocolHandler {

	/**
	 * 请求活动信息
	 * @param hawkProtocol
	 * @param playerId
	 */
    @ProtocolHandler(code = HP.code2.NOVICE_TRAIN_ACTIVITY_INFO_REQ_VALUE)
    public void onActivityInfoReq(HawkProtocol hawkProtocol, String playerId) {
        NewbieTrainActivity activity = this.getActivity(ActivityType.NEWBIE_TRAIN_ACTIVITY);
        if(activity == null || !activity.isOpening(playerId)){
        	PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, hawkProtocol.getType(), Status.Error.ACTIVITY_NOT_OPEN_VALUE);
            return;
        }
        
        activity.syncActivityDataInfo(playerId);
    }
    
    /**
     * 选择作训的英雄或装备兵种
     * @param hawkProtocol
     * @param playerId
     */
    @ProtocolHandler(code = HP.code2.NOVICE_TRAIN_SELECT_REQ_VALUE)
    public void onTrainSelectReq(HawkProtocol hawkProtocol, String playerId) {
        NewbieTrainActivity activity = this.getActivity(ActivityType.NEWBIE_TRAIN_ACTIVITY);
        if(activity == null || !activity.isOpening(playerId)){
        	PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, hawkProtocol.getType(), Status.Error.ACTIVITY_NOT_OPEN_VALUE);
            return;
        }
        
        NoviceTrainSelectReq req = hawkProtocol.parseProtocol(NoviceTrainSelectReq.getDefaultInstance());
        activity.selectTrainObject(playerId, req.getType().getNumber(), req.getSelectId(), hawkProtocol.getType());
    }
    
    /**
     * 作训请求
     * @param hawkProtocol
     * @param playerId
     */
    @ProtocolHandler(code = HP.code2.NOVICE_TRAIN_REQ_VALUE)
    public void onTrainReq(HawkProtocol hawkProtocol, String playerId) {
        NewbieTrainActivity activity = this.getActivity(ActivityType.NEWBIE_TRAIN_ACTIVITY);
        if(activity == null || !activity.isOpening(playerId)){
        	PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, hawkProtocol.getType(), Status.Error.ACTIVITY_NOT_OPEN_VALUE);
            return;
        }
        
        NoviceTrainReq req = hawkProtocol.parseProtocol(NoviceTrainReq.getDefaultInstance());
        activity.doTrain(playerId, req.getType().getNumber(), req.getTimes(), hawkProtocol.getType());
    }
    
    /**
     * 购买礼包
     * @param hawkProtocol
     * @param playerId
     */
    @ProtocolHandler(code = HP.code2.NOVICE_TRAIN_GIFT_BUY_REQ_VALUE)
    public void onBuyGiftReq(HawkProtocol hawkProtocol, String playerId) {
        NewbieTrainActivity activity = this.getActivity(ActivityType.NEWBIE_TRAIN_ACTIVITY);
        if(activity == null || !activity.isOpening(playerId)){
        	PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, hawkProtocol.getType(), Status.Error.ACTIVITY_NOT_OPEN_VALUE);
            return;
        }
        
        NoviceGiftBuyReq req = hawkProtocol.parseProtocol(NoviceGiftBuyReq.getDefaultInstance());
        activity.buyGift(playerId, req.getType().getNumber(), req.getGiftId(), hawkProtocol.getType());
    }
    
    /**
     * 作训记录数据请求
     * @param hawkProtocol
     * @param playerId
     */
    @ProtocolHandler(code = HP.code2.NOVICE_TRAIN_RECORD_REQ_VALUE)
    public void onTrainRecordReq(HawkProtocol hawkProtocol, String playerId) {
        NewbieTrainActivity activity = this.getActivity(ActivityType.NEWBIE_TRAIN_ACTIVITY);
        if(activity == null || !activity.isOpening(playerId)){
        	PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, hawkProtocol.getType(), Status.Error.ACTIVITY_NOT_OPEN_VALUE);
            return;
        }
        
        NoviceTrainRecordReq req = hawkProtocol.parseProtocol(NoviceTrainRecordReq.getDefaultInstance());
        activity.trainRecordReq(playerId, req.getType().getNumber(), hawkProtocol.getType());
    }

}
