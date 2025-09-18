package com.hawk.game.script;

import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.type.ActivityState;
import com.hawk.game.protocol.Activity.ActivityType;
import org.hawk.db.HawkDBManager;
import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import java.util.Map;


/**
 * 查询活动数据
 * localhost:8080/script/queryActivity?activityId=
 */
public class QueryActivityHandler extends HawkScript {
    @Override
    public String action(Map<String, String> params, HawkScriptHttpInfo hawkScriptHttpInfo) {
        try {
        	int activityId = Integer.parseInt(params.get("activityId"));
        	ActivityType activityType = ActivityType.valueOf(activityId);
        	if (activityType == null){
        		return  HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "incorrect activityId");
        	}
        	
        	JSONObject result = queryDB(activityId);
        	return HawkScript.successResponse("success",result);
        }catch (Exception e){
            HawkException.catchException(e);
        }
        return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "query failed");
    }

    /**
     * 查询数据
     * @param activityId
     * @return
     */
    public JSONObject queryDB(int activityId){
        Session session = HawkDBManager.getInstance().getSession();
        SQLQuery query = session.createSQLQuery(String.format("select id, state, termId from activity where activityId = ?"));
        query.setParameter(0, activityId);
        Object[] obj = (Object[]) query.uniqueResult();
        int state = (int) obj[1];
        String stateDesc = "hidden";
        if(state == ActivityState.END.intValue()) {
        	stateDesc = "end now（next hidden）";
        } else if(state == ActivityState.OPEN.intValue()) {
        	 stateDesc = "opening now（next end）";
        } else if(state == ActivityState.SHOW.intValue()) {
        	 stateDesc = "showing now（next open）";
        }
        JSONObject rnt = new JSONObject();
        rnt.put("活动状态", stateDesc);
        rnt.put("活动期数", obj[2]);
        return rnt;
    }
}
