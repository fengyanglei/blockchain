package com.example.blockchain.config;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.UUID;

/**
 * Created by Layne on 2018-3-29.
 */
@WebListener
public class InitialID implements ServletContextListener{
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext servletContext = servletContextEvent.getServletContext();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        servletContext.setAttribute("uuid", uuid);
        System.out.println("uuid ===== " + uuid);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
