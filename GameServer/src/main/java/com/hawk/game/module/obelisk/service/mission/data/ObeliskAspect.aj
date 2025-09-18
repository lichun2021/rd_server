package com.hawk.game.module.obelisk.service.mission.data;

public aspect ObeliskAspect {

	public pointcut setMethods(): (execution(* com.hawk.game.module.obelisk.service.mission.data.ObeliskMissionItem+.set*(..))) && 
	  !execution(* com.hawk.game.module.obelisk.service.mission.data.ObeliskMissionItem+.setChanged(..)) ;

	after() returning() : setMethods() {
		ObeliskMissionItem obj = (ObeliskMissionItem) thisJoinPoint.getTarget();
		obj.setChanged(true);
	}
}
