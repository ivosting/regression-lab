package dev.regressionlab.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Logs a deploy marker so every running version is visible in the log stream. */
@Component
public class StartupLogger {

    private static final Logger log = LoggerFactory.getLogger("deploy");

    private final String version;
    private final String env;

    public StartupLogger(@Value("${app.version}") String version, @Value("${app.env}") String env) {
        this.version = version;
        this.env = env;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        log.atInfo()
                .addKeyValue("event", "deploy.started")
                .log("regression-lab version {} started in env {}", version, env);
    }
}
