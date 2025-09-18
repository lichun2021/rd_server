package com.hawk.activity.helper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.hawk.os.HawkException;

import com.hawk.activity.event.ActivityEvent;

public class ActivityEventSerialize {
	/**
	 * 序列化活动事件
	 * @param obj
	 * @return
	 */
	public static byte[] serialize(ActivityEvent obj) {
		byte[] bytes = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(obj);
			bytes = baos.toByteArray();
			baos.close();
			oos.close();
		} catch (IOException e) {
			HawkException.catchException(e);
			return null;
		}
		return bytes;
	}
	
	/**
	 * 反序列化活动事件
	 * @param bytes
	 * @return
	 */
	public static ActivityEvent deSerialize(byte[] bytes) {
		Object obj = null;
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			ObjectInputStream ois = new ObjectInputStream(bais);
			obj = ois.readObject();
		} catch (Exception e) {
			HawkException.catchException(e);
			return null;
		}
		return (ActivityEvent) obj;
	}
}
