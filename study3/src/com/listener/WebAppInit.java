package com.listener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class WebAppInit implements ServletContextListener {
	private String pathname = "/WEB-INF/count.txt";

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// 서버가 종료될 때
		saveFile();
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		// 서바가 초기화 될 때
		pathname = sce.getServletContext().getRealPath(pathname);
		loadFile();
	}
	
	protected void loadFile() {
		try {
			long toDay = 0, yesterDay = 0, total = 0;
			
			File f = new File(pathname);
			
			if(!f.exists()) {
				return;
			}
			
			BufferedReader br = new BufferedReader(new FileReader(pathname));
			
			String s;
			
			s = br.readLine();
			
			if(s != null) {
				String []ss = s.split(":");
				if(ss.length == 3) {
					toDay = Long.parseLong(ss[0]);
					yesterDay = Long.parseLong(ss[1]);
					total = Long.parseLong(ss[2]);
				}
			}
			
			br.close();
			
			CountManager.init(toDay, yesterDay, total);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void saveFile() {
		try {
			long toDay = 0, yesterDay = 0, total = 0;
			
			toDay=CountManager.getToDayCount();
			yesterDay=CountManager.getYesterDayCount();
			total=CountManager.getTotalCount();
			
			String s = toDay+":"+yesterDay+":"+total;
			
			PrintWriter out = new PrintWriter(new FileWriter(pathname));
			out.println(s);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
