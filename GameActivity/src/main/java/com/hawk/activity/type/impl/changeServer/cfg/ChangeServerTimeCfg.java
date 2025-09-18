package com.hawk.activity.type.impl.changeServer.cfg;

import com.hawk.activity.config.IActivityTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@HawkConfigManager.XmlResource(file = "activity/changeserver/changeserver_time.xml")
public class ChangeServerTimeCfg extends HawkConfigBase implements IActivityTimeCfg {
    /** 活动期数*/
    @Id
    private final int termId;

    private final String termServerId;
    /** 预览时间*/
    private final String showTime;

    /** 开启时间*/
    private final String startTime;

    /** 结束时间*/
    private final String endTime;

    /** 消失时间*/
    private final String hiddenTime;

    private final String startTimeConut;
    private final String endTimeConut;
    private final String startTimeChange;
    private final String endTimeChange;

    private final String startTimeShowList;
    private final String endTimeShowList;


    private final String startTimeOfficialChange;
    private final String endTimeOfficialChange;
    private final String startTimeConutChange;
    private final String endTimeConutChange;

    private final String startTimeSpChange;
    private final String endTimeSpChange;

    private final String separateServerTime;



    /** 预览时间戳*/
    private long showTimeValue;

    /** 开启时间戳*/
    private long startTimeValue;

    /** 结束时间戳*/
    private long endTimeValue;

    /** 消失时间戳*/
    private long hiddenTimeValue;

    private long startTimeConutValue;
    private long endTimeConutValue;
    private long startTimeChangeValue;
    private long endTimeChangeValue;
    private long startTimeShowListValue;
    private long endTimeShowListValue;
    private long startTimeOfficialChangeValue;
    private long endTimeOfficialChangeValue;
    private long startTimeConutChangeValue;
    private long endTimeConutChangeValue;
    private long startTimeSpChangeValue;
    private long endTimeSpChangeValue;

    private Map<String, List<String>> toServerIds;
    private Map<String, String> toServerIdMap;

    public ChangeServerTimeCfg(){
        termId = 0;
        termServerId = "";
        showTime = "";
        startTime = "";
        endTime = "";
        hiddenTime = "";
        startTimeConut = "";
        endTimeConut = "";
        startTimeChange = "";
        endTimeChange = "";
        startTimeShowList = "";
        endTimeShowList = "";
        startTimeOfficialChange = "";
        endTimeOfficialChange = "";
        startTimeConutChange = "";
        endTimeConutChange = "";
        startTimeSpChange = "";
        endTimeSpChange = "";
        separateServerTime = "";
    }

    @Override
    protected boolean assemble() {
        showTimeValue = HawkTime.parseTime(showTime);
        startTimeValue = HawkTime.parseTime(startTime);
        endTimeValue = HawkTime.parseTime(endTime);
        hiddenTimeValue = HawkTime.parseTime(hiddenTime);
        startTimeConutValue = HawkTime.parseTime(startTimeConut);
        endTimeConutValue = HawkTime.parseTime(endTimeConut);
        startTimeChangeValue = HawkTime.parseTime(startTimeChange);
        endTimeChangeValue = HawkTime.parseTime(endTimeChange);
        startTimeShowListValue = HawkTime.parseTime(startTimeShowList);
        endTimeShowListValue = HawkTime.parseTime(endTimeShowList);
        startTimeOfficialChangeValue = HawkTime.parseTime(startTimeOfficialChange);
        endTimeOfficialChangeValue = HawkTime.parseTime(endTimeOfficialChange);
        startTimeConutChangeValue = HawkTime.parseTime(startTimeConutChange);
        endTimeConutChangeValue = HawkTime.parseTime(endTimeConutChange);
        startTimeSpChangeValue = HawkTime.parseTime(startTimeSpChange);
        endTimeSpChangeValue = HawkTime.parseTime(endTimeSpChange);
        Map<String, String> tmpMap = new HashMap<>();
        Map<String, List<String>> tmpListMap = new HashMap<>();
        for(String group : termServerId.split(";")){
            List<String> tmpList = new ArrayList<>();
            for(String serverIds : group.split(",")){
                String [] serverIdArr = serverIds.split("_");
                if(serverIdArr.length > 0){
                    String mainServer = serverIdArr[0];
                    tmpList.add(mainServer);
                    for(String serverId : serverIdArr){
                        tmpMap.put(serverId, mainServer);
                        tmpListMap.put(serverId, tmpList);
                    }
                }
            }
        }
        toServerIds = tmpListMap;
        toServerIdMap = tmpMap;
        return true;
    }

    @Override
    public int getTermId() {
        return termId;
    }

    @Override
    public long getShowTimeValue() {
        return showTimeValue;
    }

    @Override
    public long getStartTimeValue() {
        return startTimeValue;
    }

    @Override
    public long getEndTimeValue() {
        return endTimeValue;
    }

    @Override
    public long getHiddenTimeValue() {
        return hiddenTimeValue;
    }

    public String getShowTime() {
        return showTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getHiddenTime() {
        return hiddenTime;
    }

    public long getStartTimeConutValue() {
        return startTimeConutValue;
    }

    public long getEndTimeConutValue() {
        return endTimeConutValue;
    }

    public long getStartTimeChangeValue() {
        return startTimeChangeValue;
    }

    public long getEndTimeChangeValue() {
        return endTimeChangeValue;
    }

    public long getStartTimeShowListValue() {
        return startTimeShowListValue;
    }

    public long getEndTimeShowListValue() {
        return endTimeShowListValue;
    }

    public long getStartTimeOfficialChangeValue() {
        return startTimeOfficialChangeValue;
    }

    public long getEndTimeOfficialChangeValue() {
        return endTimeOfficialChangeValue;
    }

    public long getStartTimeConutChangeValue() {
        return startTimeConutChangeValue;
    }

    public long getEndTimeConutChangeValue() {
        return endTimeConutChangeValue;
    }

    public long getStartTimeSpChangeValue() {
        return startTimeSpChangeValue;
    }

    public long getEndTimeSpChangeValue() {
        return endTimeSpChangeValue;
    }

    public String getMainServer(String serverId){
        return toServerIdMap.getOrDefault(serverId, "");
    }

    public Map<String, String> getToServerIdMap() {
        return toServerIdMap;
    }

    public List<String> getToServerIds(String serverId) {
        return toServerIds.getOrDefault(serverId, new ArrayList<>());
    }

    @Override
    protected boolean checkValid() {
        return checkTimeCfgValid(this.getClass());
    }

    public String getSeparateServerTime() {
        return separateServerTime;
    }
}
