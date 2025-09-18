package com.hawk.game.service.xqhxWar.state;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.GsConfig;
import com.hawk.game.config.XQHXWarTimeCfg;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.service.xqhxWar.XQHXWarResidKey;
import com.hawk.game.service.xqhxWar.XQHXWarService;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import java.util.concurrent.TimeUnit;

/**
 * 先驱回响状态机
 */
public class XQHXWarStateData {
    //期数
    private int termId;
    //状态
    private XQHXWarStateEnum state = XQHXWarStateEnum.PEACE;
    //状态切换标记位
    private boolean isNext = false;

    public int getTermId() {
        return termId;
    }

    public void setTermId(int termId) {
        this.termId = termId;
    }

    public XQHXWarStateEnum getState() {
        return state;
    }

    /**
     * 从redis里面加载数据
     */
    public void load(){
        String value = RedisProxy.getInstance().getRedisSession().hGet(XQHXWarResidKey.XQHX_WAR_STATE , GsConfig.getInstance().getServerId());
        //为空直接返回
        if(HawkOSOperator.isEmptyString(value)){
            return;
        }
        //解析
        JSONObject obj = JSONObject.parseObject(value);
        //期数
        termId = obj.getIntValue("termId");
        //状态
        state = XQHXWarStateEnum.valueOf(obj.getIntValue("state"));
    }

    /**
     * 更新数据到redis
     */
    public void update(){
        JSONObject obj = new JSONObject();
        obj.put("termId", termId);
        obj.put("state", state.getIndex());
        RedisProxy.getInstance().getRedisSession().hSet(XQHXWarResidKey.XQHX_WAR_STATE, GsConfig.getInstance().getServerId(), obj.toJSONString());
    }

    @Override
    public String toString() {
        return "XQHXWarStateDate{" +
                "termId=" + termId +
                ", state=" + state +
                '}';
    }

    /**
     * 状态机tick
     */
    public void tick(){
        //当前时间
        long now = HawkTime.getMillisecond();
        //结束时间
        long endTime = getEndTime();
        //超过时间设置切换状态
        if(now >= endTime){
            //设置切换状态标记
            toNext();
        }
        //切换状态
        if(isNext){
            try {
                next();
            }catch (Exception e){
                HawkException.catchException(e);
            }
            isNext = false;
        }
    }

    /**
     * 状态结束时间
     * @return 结束时间戳
     */
    public long getEndTime(){
        try {
            //当前配置，和平阶段需要计算配置
            XQHXWarTimeCfg cfg = state == XQHXWarStateEnum.PEACE ?
                    XQHXWarService.getInstance().calCfg() : XQHXWarService.getInstance().getTimeCfg();
            //如果为空直接返回未来时间
            if(cfg == null){
                return HawkTime.getMillisecond() + TimeUnit.DAYS.toMillis(1);
            }
            //通过状态不同读不同配置
            switch (state){
                //和平阶段
                case PEACE:{
                    return cfg.getSignupTime();
                }
                //报名阶段
                case SIGNUP:{
                    return cfg.getMatchWaitTime();
                }
                //匹配等待
                case MATCH_WAIT:{
                    return cfg.getMatchTime();
                }
                //匹配
                case MATCH:{
                    return cfg.getMatchEndTime();
                }
                //匹配结束
                case MATCH_END:{
                    return cfg.getBattleTime();
                }
                //战斗
                case BATTLE:{
                    return cfg.getSettleTime();
                }
                //结束展示
                case FINISH:{
                    return cfg.getEndTime();
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
     * 设置状态切换标记
     */
    public void toNext(){
        isNext = true;
    }

    /**
     * 切换状态
     */
    public void next(){
        //离开老状态
        leave();
        //切换状态
        changeState();
        //进入新状态
        enter();
        //更新数据
        update();
        //记录状态切换日志
        HawkLog.logPrintln(toString());
        //同步给在线玩家
        XQHXWarService.getInstance().syncAllPLayer();
    }

    /**
     * 切换状态 PEACE -> SIGNUP -> MATCH_WAIT -> MATCH -> MATCH_END -> BATTLE -> FINISH -> PEACE
     */
    public void changeState(){
        try {
            switch (state){
                //和平到报名
                case PEACE:{
                    state = XQHXWarStateEnum.SIGNUP;
                }
                break;
                //报名到匹配等待
                case SIGNUP:{
                    state = XQHXWarStateEnum.MATCH_WAIT;
                }
                break;
                //匹配等待到匹配
                case MATCH_WAIT:{
                    state = XQHXWarStateEnum.MATCH;
                }
                break;
                //匹配到匹配结束
                case MATCH:{
                    state = XQHXWarStateEnum.MATCH_END;
                }
                break;
                //匹配结束到战斗
                case MATCH_END:{
                    state = XQHXWarStateEnum.BATTLE;
                }
                break;
                //战斗到结束展示
                case BATTLE:{
                    state = XQHXWarStateEnum.FINISH;
                }
                break;
                //结束展示到和平
                case FINISH:{
                    state = XQHXWarStateEnum.PEACE;
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
                //报名
                case SIGNUP:{
                    XQHXWarService.getInstance().onSignup();
                }
                break;
                //匹配等待
                case MATCH_WAIT:{
                    XQHXWarService.getInstance().onMatchWait();
                }
                break;
                //匹配
                case MATCH:{
                    XQHXWarService.getInstance().onMatch();
                }
                break;
                //匹配结束
                case MATCH_END:{
                    XQHXWarService.getInstance().onMatchEnd();
                }
                break;
                //战斗
                case BATTLE:{
                    XQHXWarService.getInstance().onBattle();
                }
                break;
            }
        } catch (Exception e) {
            HawkException.catchException(e);
        }
    }

    /**
     * 离开状态
     */
    public void leave(){
        try {
            switch (state){
                //和平状态
                case PEACE:{
                    XQHXWarService.getInstance().outPeace();
                }
                break;
                //结束展示
                case FINISH:{
                    XQHXWarService.getInstance().outEnd();
                }
                break;
            }
        } catch (Exception e) {
            HawkException.catchException(e);
        }
    }
}
