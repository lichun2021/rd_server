package com.hawk.game.crossactivity.resourcespree;

import java.util.List;

import org.hawk.log.HawkLog;

import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.thread.WorldDelayTask;

/**
 * 资源狂欢宝箱刷出
 * @author chechangda
 *
 */
public class ResourceSpreeBoxDelete extends WorldDelayTask{
	
	
	
	public ResourceSpreeBoxDelete() {
		super(GsConst.WorldTaskType.RESOURCE_SPREE_BOX_DELETE, 1000, 1000, 1);
	}


	@Override
	public boolean onInvoke() {
		List<WorldPoint> plist = WorldPointService.getInstance().getWorldPointsByType(WorldPointType.RESOURCE_SPREE_BOX);
		HawkLog.logPrintln("CrossActivityService removeResourceSpreeBox,size:{}",plist.size());
		if(plist.size() <= 0){
			return true;
		}
		for(WorldPoint point : plist){
			HawkLog.logPrintln("CrossActivityService removeResourceSpreeBox,pointX:{},pointY:{}",point.getX(),point.getY());
			WorldPointService.getInstance().removeWorldPoint(point.getId());
		}
		return true;
	}
	
	
}
