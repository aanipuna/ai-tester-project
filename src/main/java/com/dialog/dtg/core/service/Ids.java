package com.dialog.dtg.core.service;

import java.util.UUID;

public final class Ids {

    private Ids() {
    }

    public static String nextSpecId() {
        return "spec-" + shortId();
    }

    public static String nextPlanId() {
        return "plan-" + shortId();
    }

    public static String nextRunId() {
        return "run-" + shortId();
    }

    public static String nextWorkflowId() {
        return "wf-" + shortId();
    }

    public static String nextWorkflowRunId() {
        return "wfrun-" + shortId();
    }

    public static String nextStepId() {
        return "step-" + shortId();
    }

    private static String shortId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
