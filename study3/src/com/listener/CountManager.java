package com.listener;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

// HttpSessionListener : 세션이 생성되거나 소멸될때 발생하는 이벤트를 처리하는 리스너

@WebListener
public class CountManager implements HttpSessionListener {
	
	private static int currentCount;
	private static long toDayCount, yesterDayCount, totalCount;
	
	public CountManager() {
		// 자정이되면 오늘 인원은 어제인원으로 할당하고 오늘 인원은 0
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
		// 세션이 만들어질때
		// HttpSession session = evt.getSession();
		
		synchronized (this) {
			currentCount++;
			toDayCount++;
			totalCount++;
		}
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent evt) {
		// 세션이 소멸될때
		synchronized (this) {
			currentCount--;
			if(currentCount < 0) {
				currentCount = 0;
			}
		}
	}
}
