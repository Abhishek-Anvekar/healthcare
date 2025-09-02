package com.healthcare.messaging_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "messaging.reminder")
public class ReminderProperties {
    private List<Integer> remindersMinutes;
    private String cron;
    private boolean enabled;

    public List<Integer> getRemindersMinutes() { return remindersMinutes; }
    public void setRemindersMinutes(List<Integer> remindersMinutes) { this.remindersMinutes = remindersMinutes; }

    public String getCron() { return cron; }
    public void setCron(String cron) { this.cron = cron; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
