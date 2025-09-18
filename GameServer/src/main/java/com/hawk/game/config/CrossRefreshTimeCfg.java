package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

@HawkConfigManager.XmlResource(file = "xml/cross_refresh_time.xml")
public class CrossRefreshTimeCfg extends HawkConfigBase {
    @Id
    protected final int id;
    protected final String refreshTime;

    protected long refreshTimeValue;

    public CrossRefreshTimeCfg(){
        id = 0;
        refreshTime = "";
    }

    @Override
    protected boolean assemble() {
        refreshTimeValue = HawkTime.parseTime(refreshTime);
        return true;
    }

    public long getRefreshTimeValue() {
        return refreshTimeValue;
    }
}
