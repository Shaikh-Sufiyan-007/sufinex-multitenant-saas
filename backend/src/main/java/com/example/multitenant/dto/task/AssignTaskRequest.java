package com.example.multitenant.dto.task;

public class AssignTaskRequest {

    private Long assignedToUserId;

    public AssignTaskRequest() {
    }

    public AssignTaskRequest(Long assignedToUserId) {
        this.assignedToUserId = assignedToUserId;
    }

    public Long getAssignedToUserId() {
        return assignedToUserId;
    }

    public void setAssignedToUserId(Long assignedToUserId) {
        this.assignedToUserId = assignedToUserId;
    }
}
