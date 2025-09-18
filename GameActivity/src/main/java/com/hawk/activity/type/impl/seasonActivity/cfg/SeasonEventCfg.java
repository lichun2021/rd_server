package com.hawk.activity.type.impl.seasonActivity.cfg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 荣耀殿堂赛季坑位、赛事对应关系配置
 */
@HawkConfigManager.XmlResource(file = "activity/season/season_event_type.xml")
public class SeasonEventCfg extends HawkConfigBase {
	/**
	 * 赛季
	 */
    @Id
    private final int id;

    /** 坑位1对应赛事 */
    private final int event1;

    /** 坑位2对应赛事 */
    private final int event2;

    /** 坑位3对应赛事 */
    private final int event3;
    
    private List<Integer> eventList = new ArrayList<>();

    public SeasonEventCfg(){
        id = 0;
        event1 = 0;
        event2 = 0;
        event3 = 0;
    }
    
    public boolean assemble() {
    	eventList.add(event1);
    	eventList.add(event2);
    	eventList.add(event3);
    	return true;
    }

	public int getId() {
		return id;
	}

	public int getEvent1() {
		return event1;
	}

	public int getEvent2() {
		return event2;
	}

	public int getEvent3() {
		return event3;
	}

	public List<Integer> getEventList() {
		return eventList;
	}
}
