package com.hawk.ms.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class MergeServerThreadFactory implements ThreadFactory {
	private String name;
	private AtomicInteger threadNumber = new AtomicInteger(1);
	private ThreadGroup tg;
	public MergeServerThreadFactory(String name) {
		this.name = name;
		tg = new ThreadGroup(name);
	}
	@Override
	public Thread newThread(Runnable runnable) {
		 Thread t = new Thread(tg, runnable);
		 t.setName(name + "-" + threadNumber.getAndIncrement());
		 t.setDaemon(true);
		 
		 return t;
	}

}
