package com.hawk.game.script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.crossactivity.resourcespree.ResourceSpreeBoxDelete;
import com.hawk.game.crossactivity.resourcespree.ResourceSpreeBoxRefersh;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.world.thread.WorldThreadScheduler;

/**
 * localhost:8080/script/crossBox?action=gen&boxCount =1_10,2_10,3_10,4_10&term=1&winner=60017,60018
 * localhost:8080/script/crossBox?action=remove
 * 
 * 
 *
 */
public class ResourceSpreeBoxGenHandler extends HawkScript {
	
	
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			//QA用得
			String action = params.get("action");
			String boxCountStr = params.get("boxCount");
			String winner = params.get("winner");
			String termIdStr = params.get("term");
			if(HawkOSOperator.isEmptyString(action)){
				return "paramFail";
			}
			
			if(action.equals("gen")){
				if(HawkOSOperator.isEmptyString(boxCountStr) ||
						HawkOSOperator.isEmptyString(winner)||
						HawkOSOperator.isEmptyString(termIdStr)){
					return "paramFail";
				}
				Map<Integer,Integer> map = new HashMap<>();
				List<String> winners = new ArrayList<>();
				String[] arr = boxCountStr.split(",");
				for(String str : arr){
					String[] parr = str.split("_");
					int id = Integer.parseInt(parr[0]);
					int num = Integer.parseInt(parr[1]);
					map.put(id, num);
				}
				String[] warr = winner.split(",");
				for(String str : warr){
					winners.add(str.trim());
				}
				int termId = Integer.parseInt(termIdStr);
				
				ResourceSpreeBoxRefersh boxRefersh = new ResourceSpreeBoxRefersh(termId, winners, 
						1000, map, 50, 3000);
				WorldThreadScheduler.getInstance().postDelayWorldTask(boxRefersh);
				return successResponse("genBoxSuccese");
			}
			
			if(action.equals("remove")){
				ResourceSpreeBoxDelete delete = new ResourceSpreeBoxDelete();
				WorldThreadScheduler.getInstance().postDelayWorldTask(delete);
				return successResponse("removeSuccese");
			}
			return successResponse("succese");

		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "");
	}

	
	
}
