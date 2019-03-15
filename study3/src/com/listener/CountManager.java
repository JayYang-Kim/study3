package com.listener;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

// HttpSessionListener : ������ �����ǰų� �Ҹ�ɶ� �߻��ϴ� �̺�Ʈ�� ó���ϴ� ������

@WebListener
public class CountManager implements HttpSessionListener {
	
	private static int currentCount;
	private static long toDayCount, yesterDayCount, totalCount;
	
	public CountManager() {
		// �����̵Ǹ� ���� �ο��� �����ο����� �Ҵ��ϰ� ���� �ο��� 0
		TimerTask cron = new TimerTask() {
			
			@Override
			public void run() {
				yesterDayCount = toDayCount;
				toDayCount = 0;
				
			}
		};
		
		Timer t = new Timer();
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, 1);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		t.schedule(cron, cal.getTime(), 1000*60*60*24);
	}
	
	public static void init(long toDay, long yesterDay, long total) {
		toDayCount = toDay;
		yesterDayCount = yesterDay;
		totalCount = total;
	}
	
	public static int getCurrentCount() {
		return currentCount;
	}

	public static long getToDayCount() {
		return toDayCount;
	}

	public static long getYesterDayCount() {
		return yesterDayCount;
	}

	public static long getTotalCount() {
		return totalCount;
	}
	
	@Override
	public void sessionCreated(HttpSessionEvent evt) {
		// ������ ���������
		// HttpSession session = evt.getSession();
		
		synchronized (this) {
			currentCount++;
			toDayCount++;
			totalCount++;
		}
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent evt) {
		// ������ �Ҹ�ɶ�
		synchronized (this) {
			currentCount--;
			if(currentCount < 0) {
				currentCount = 0;
			}
		}
	}
}
