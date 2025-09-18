package com.hawk.activity.msg;

import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.gamelib.GameConst.MsgId;
import org.hawk.msg.HawkMsg;

import java.util.List;

public class PlayerAchieveUpdateMsg extends HawkMsg {
    private List<AchieveItem> items;

    private PlayerAchieveUpdateMsg() {
        super(MsgId.PLAYER_ACHIEVE_UPDATE);
    }

    public static PlayerAchieveUpdateMsg valueOf(List<AchieveItem> items){
        PlayerAchieveUpdateMsg msg = new PlayerAchieveUpdateMsg();
        msg.items = items;
        return msg;
    }

    public List<AchieveItem> getItems() {
        return items;
    }
}
