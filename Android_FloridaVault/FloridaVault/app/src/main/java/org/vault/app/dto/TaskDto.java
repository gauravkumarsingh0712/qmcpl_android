package org.vault.app.dto;

/**
 * Created by aqeeb.pathan on 03-07-2015.
 */
public class TaskDto {
    private String name;
    private String notes;
    private String assignee_status;
    private AssigneeDto assignee;
    private String projects;
    private long workspace;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public long getWorkspace() {
        return workspace;
    }

    public void setWorkspace(long workspace) {
        this.workspace = workspace;
    }

    public String getProjects() {
        return projects;
    }

    public void setProjects(String projects) {
        this.projects = projects;
    }

    public String getAssignee_status() {
        return assignee_status;
    }

    public void setAssignee_status(String assignee_status) {
        this.assignee_status = assignee_status;
    }

    public AssigneeDto getAssignee() {
        return assignee;
    }

    public void setAssignee(AssigneeDto assignee) {
        this.assignee = assignee;
    }
}
