package com.hawk.game.aop;

import org.hawk.db.HawkDBEntity;
import org.hawk.log.HawkLog;

public aspect DBEntityAspect {	
	/*
	public pointcut setMethods(): (execution(public * com.hawk.game.entity.*.set*(..))) && 
								  !execution(public * com.hawk.game.entity.*.setUpdateTime(..)) &&
								  !execution(public * com.hawk.game.entity.*.setCreateTime(..));
	*/
	
	public pointcut setMethods(): (execution(* org.hawk.db.HawkDBEntity+.set*(..))) && 
	  							  !execution(* org.hawk.db.HawkDBEntity+.setUpdateTime(..)) &&
	  							  !execution(* org.hawk.db.HawkDBEntity+.setCreateTime(..)) &&
	  							  !execution(* org.hawk.db.HawkDBEntity+.setInvalid(..));
	
	after() returning() : setMethods() {		
		HawkDBEntity obj = (HawkDBEntity) thisJoinPoint.getTarget();
		if (obj.getUpdateTime() > 0) {
			if (obj.isInvalid()) {
				HawkLog.warnPrintln("db entity update illegality, entity: {}, fileName: {}, line: {}", 
						obj, thisJoinPoint.getSourceLocation().getFileName(), thisJoinPoint.getSourceLocation().getLine());
			}
			obj.notifyUpdate();
		}
	}
}
