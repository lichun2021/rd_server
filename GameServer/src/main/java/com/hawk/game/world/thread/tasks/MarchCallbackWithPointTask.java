package com.hawk.game.world.thread.tasks;

import java.util.ArrayList;
import java.util.List;

import org.hawk.os.HawkOSOperator;

import com.hawk.game.util.GsConst;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.thread.WorldTask;

/**
 * 针对点上的行军召回
 * @author zhenyu.shang
 * @since 2018年1月19日
 */
public class MarchCallbackWithPointTask extends WorldTask{
	
	private List<IWorldMarch> marchs;
	
	private int x;
	
	private int y;
	
	public MarchCallbackWithPointTask(IWorldMarch march, int x, int y) {
		super(GsConst.WorldTaskType.WORLD_MARCH_POINT_CALLBACK);
		this.marchs = new ArrayList<IWorldMarch>();
		this.marchs.add(march);
		
		this.x = x;
		this.y = y;
	}

	public MarchCallbackWithPointTask(List<IWorldMarch> marchs, int x, int y) {
		super(GsConst.WorldTaskType.WORLD_MARCH_POINT_CALLBACK);
		this.marchs = marchs;
		this.x = x;
		this.y = y;
	}

	@Override
	public boolean onInvoke() {
		if(marchs == null || marchs.isEmpty()){
			WorldMarchService.logger.error("[MarchCallbackWithPointTask] add world march list is null or empty , marchs : {}", marchs == null ? marchs : 0);
			return false;
		}
		for (IWorldMarch march : marchs) {
			if (march != null && !HawkOSOperator.isEmptyString(march.getMarchId())) {
				WorldMarchService.logger.info("marchCallBack, marchId:{}", march.getMarchId());
			}
			WorldMarchService.getInstance().onMarchCallBack(march);
		}
		if(x > 0 && y > 0){
			WorldPointService.getInstance().notifyPointUpdate(x, y);
		}
		return true;
	}

}
