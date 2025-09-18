package com.hawk.activity.type.impl.diffInfoSave;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.HP;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;

public class DiffInfoSaveHandler extends ActivityProtocolHandler {

    @ProtocolHandler(code = HP.code2.DIFF_INFO_SAVE_RED_DOT_VALUE)
    public void reaInfo(HawkProtocol protocol, String playerId){
        //获得活动实例
        DiffInfoSaveActivity activity = getActivity(ActivityType.DIFF_INFO_SAVE);
        Result<?> result = activity.click(playerId);
        //如果没有成功执行，返回对应错误码
        if(result.isFail()){
            sendErrorAndBreak(playerId, protocol.getType(), result.getStatus());
        }
    }
}
