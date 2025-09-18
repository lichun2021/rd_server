package com.hawk.activity.type.impl.starLightSign.cfg;

import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import java.util.List;

@HawkConfigManager.XmlResource(file = "activity/starlight_sign/starlight_buy.xml")
public class StarLightSignPayGiftCfg extends HawkConfigBase {
    @Id
    private final int id;

    private final int type;

    private final String buy;

    private List<Integer> buyList;

    public StarLightSignPayGiftCfg(){
        id = 0;
        type = 0;
        buy = "";
    }

    @Override
    protected boolean assemble() {
        try {
            buyList = SerializeHelper.cfgStr2List(buy, SerializeHelper.BETWEEN_ITEMS);
        }catch (Exception e){
            HawkException.catchException(e);
            return false;
        }
        return true;
    }

    public int getType() {
        return type;
    }

    public List<Integer> getBuyList() {
        return buyList;
    }
}
