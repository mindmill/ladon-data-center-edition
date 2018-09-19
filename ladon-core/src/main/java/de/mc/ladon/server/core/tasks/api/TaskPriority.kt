/*
 * Copyright (c) 2015 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.core.tasks.api

/**
 * TaskPriority
 * Created by ralfulrich on 30.04.15.
 */
enum class TaskPriority {
    /**
     * only run the task when system is idle
     */
    WHEN_IDLE,
    /**
     * only run the task when the system load is under 30 %
     */
    WHEN_LOW,
    /**
     * only run the task when the system load is not over 80%
     */
    MEDIUM,
    /**
     * run the task with high priority
     */
    HIGH
}