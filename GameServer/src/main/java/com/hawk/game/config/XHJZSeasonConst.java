package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import java.util.HashSet;
import java.util.Set;

@HawkConfigManager.KVResource(file = "xml/xhjz_league_const.xml")
public class XHJZSeasonConst extends HawkConfigBase {
    protected final int initPoint;
    protected final int winPoint;
    protected final int winPointPara;
    protected final int losrPoint;
    protected final int losrPointPara;
    protected final String groupS;
    protected final String groupA;
    protected final String groupB;
    private final String onlyOldServers;

    protected final int seasonTimeIndex;

    private int groupSmin;
    private int groupSmax;
    private int groupAmin;
    private int groupAmax;
    private int groupBmin;
    private int groupBmax;
    private Set<String> onlyOldServerSet;

    public XHJZSeasonConst() {
        this.initPoint = 0;
        this.winPoint = 0;
        this.winPointPara = 0;
        this.losrPoint = 0;
        this.losrPointPara = 0;
        this.groupS = "";
        this.groupA = "";
        this.groupB = "";
        this.seasonTimeIndex = 3;
        this.onlyOldServers = "";
    }

    @Override
    protected boolean assemble() {
        String [] sArr = groupS.split("_");
        String [] aArr = groupA.split("_");
        String [] bArr = groupB.split("_");

        this.groupSmin = Integer.parseInt(sArr[0]);
        this.groupSmax = Integer.parseInt(sArr[1]);
        this.groupAmin = Integer.parseInt(aArr[0]);
        this.groupAmax = Integer.parseInt(aArr[1]);
        this.groupBmin = Integer.parseInt(bArr[0]);
        this.groupBmax = Integer.parseInt(bArr[1]);
        Set<String> onlyOldServerSetTmp = new HashSet<>();
        if(!HawkOSOperator.isEmptyString(this.onlyOldServers)){
            for(String rangeStr : this.onlyOldServers.split(",")){
                // 如果包含'-'，说明是一个范围
                if (rangeStr.contains("-")) {
                    String[] range = rangeStr.split("-");
                    int start = Integer.parseInt(range[0]);
                    int end = Integer.parseInt(range[1]);

                    // 将范围内的所有数字转换为字符串并加入列表
                    for (int i = start; i <= end; i++) {
                        onlyOldServerSetTmp.add(String.valueOf(i));
                    }
                } else {
                    // 单个数字，直接加入列表
                    onlyOldServerSetTmp.add(rangeStr);
                }
            }
        }
        onlyOldServerSet = onlyOldServerSetTmp;
        return true;
    }

    public int getInitPoint() {
        return initPoint;
    }

    public int getWinPoint() {
        return winPoint;
    }

    public int getWinPointPara() {
        return winPointPara;
    }

    public int getLosrPoint() {
        return losrPoint;
    }

    public int getLosrPointPara() {
        return losrPointPara;
    }

    public String getGroupS() {
        return groupS;
    }

    public String getGroupA() {
        return groupA;
    }

    public String getGroupB() {
        return groupB;
    }

    public int getGroupSmin() {
        return groupSmin;
    }

    public int getGroupSmax() {
        return groupSmax;
    }

    public int getGroupAmin() {
        return groupAmin;
    }

    public int getGroupAmax() {
        return groupAmax;
    }

    public int getGroupBmin() {
        return groupBmin;
    }

    public int getGroupBmax() {
        return groupBmax;
    }

    public int getSeasonTimeIndex() {
        return seasonTimeIndex;
    }

    public Set<String> getOnlyOldServerSet() {
        return onlyOldServerSet;
    }
}
