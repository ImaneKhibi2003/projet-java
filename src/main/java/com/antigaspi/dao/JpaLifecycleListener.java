package com.antigaspi.dao;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/** Démarre JPA au lancement de l'application et le ferme proprement à l'arrêt. */
@WebListener
public class JpaLifecycleListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        JpaUtil.getEntityManagerFactory(); // échoue tout de suite si la config est mauvaise
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        JpaUtil.close();
    }
}
