package com.kineplan.cabinet.infrastructure;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kineplan.subscription-plans")
public class SubscriptionPlanProperties {
    private Map<String, Quota> plans = new HashMap<>();

    public Map<String, Quota> getPlans() { return plans; }
    public void setPlans(Map<String, Quota> plans) { this.plans = plans; }

    public Quota quotaFor(String plan) {
        Quota quota = plans.get(plan);
        if (quota == null) {
            throw new IllegalArgumentException("Unknown subscription plan");
        }
        return quota;
    }

    public static class Quota {
        private long maxPractitioners;
        private long maxSmsPerMonth;

        public long getMaxPractitioners() { return maxPractitioners; }
        public void setMaxPractitioners(long maxPractitioners) { this.maxPractitioners = maxPractitioners; }
        public long getMaxSmsPerMonth() { return maxSmsPerMonth; }
        public void setMaxSmsPerMonth(long maxSmsPerMonth) { this.maxSmsPerMonth = maxSmsPerMonth; }
    }
}
