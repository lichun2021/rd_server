package com.hawk.game.service.xqhxWar.state;

import com.hawk.game.config.XQHXConstCfg;
import com.hawk.game.service.xqhxWar.XQHXWarService;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

import java.util.concurrent.TimeUnit;

/**
 * 战斗状态机
 */
public class XQHXWarBattleStateData {
    //期数
    private int termId;
    //时间索引
    private int timeIndex;
    //状态
    private XQHXWarStateEnum state = XQHXWarStateEnum.BATTLE_WAIT;
    //切换标记
    private boolean isNext = false;

    /**
     * 构造函数
     * @param termId 期数
     * @param timeIndex 时间索引
     */
    public XQHXWarBattleStateData(int termId, int timeIndex) {
        this.termId = termId;
        this.timeIndex = timeIndex;
    }

    public int getTermId() {
        return termId;
    }

    public int getTimeIndex() {
        return timeIndex;
    }

    public XQHXWarStateEnum getState() {
        return state;
    }

    @Override
    public String toString() {
        return "XQHXWarBattleStateDate{" +
                "termId=" + termId +
                ", timeIndex=" + timeIndex +
                ", state=" + state +
                '}';
    }

    /**
     * 状态机tick
     */
    public void tick(){
        //当前时间
        long now = HawkTime.getMillisecond();
        //本状态结束时间
        long endTime = getEndTime();
        //时间超过就切状态
        if(now >= endTime){
            //设置切状态标记位
            toNext();
        }
        //且状态逻辑
        if(isNext){
            try {
                next();
            }catch (Exception e){
                HawkException.catchException(e);
            }
            //恢复状态
            isNext = false;
        }
    }

    /**
     * 状态结束时间
     * @return 结束时间戳
     */
    public long getEndTime(){
        try {
            //战斗开始时间
            long battleStartTIme = XQHXWarService.getInstance().getBattleStartTime(termId, timeIndex);
            //常量配置
            XQHXConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(XQHXConstCfg.class);
            //根据不同状态计算结束时间
            switch (state){
                //战斗等待
                case BATTLE_WAIT:{
                    return battleStartTIme;
                }
                //战斗开始
                case BATTLE_OPEN:{
                    return battleStartTIme + constCfg.getBattleTime();
                }
                //战斗结束等待
                case BATTLE_END_WAIT:{
                    return battleStartTIme + constCfg.getBattleTime() + TimeUnit.MINUTES.toMillis(10);
                }
            }
            //意外情况
            return Long.MAX_VALUE;
        } catch (Exception e) {
            HawkException.catchException(e);
            return Long.MAX_VALUE;
        }
    }

    /**
     * 设置切状态标记位
     */
    public void toNext(){
        isNext = true;
    }

    /**
     * 切状态
     */
    public void next(){
        //离开老状态
        leave();
        //切换状态
        changeState();
        //进入新状态
        enter();
        //记录状态切换日志
        HawkLog.logPrintln(toString());
        //同步给在线玩家
        XQHXWarService.getInstance().syncAllPLayer();
    }

    /**
     * 切换状态 BATTLE_WAIT -> BATTLE_OPEN -> BATTLE_END_WAIT -> BATTLE_END_WAIT
     */
    public void changeState(){
        try {
            switch (state){
                //战斗开始等待到战斗开始
                case BATTLE_WAIT:{
                    state = XQHXWarStateEnum.BATTLE_OPEN;
                }
                break;
                //战斗开始到战斗结束等待
                case BATTLE_OPEN:{
                    state = XQHXWarStateEnum.BATTLE_END_WAIT;
                }
                break;
                //战斗结束等待到战斗结束
                case BATTLE_END_WAIT:{
                    state = XQHXWarStateEnum.BATTLE_END;
                }
                break;
            }
        } catch (Exception e) {
            HawkException.catchException(e);
        }
    }

    /**
     * 进入状态
     */
    public void enter(){
        try {
            switch (state){
                //战斗开始
                case BATTLE_OPEN:{
                    XQHXWarService.getInstance().onBattleOpen(timeIndex);
                }
                break;
                //战斗结束
                case BATTLE_END:{
                    XQHXWarService.getInstance().onBattleEnd(timeIndex);
                }
                break;
            }
        } catch (Exception e) {
            HawkException.catchException(e);
        }
    }

    /**
     *  离开状态
     */
    public void leave(){
        try {
            //暂时没有逻辑
        } catch (Exception e) {
            HawkException.catchException(e);
        }
    }
}
