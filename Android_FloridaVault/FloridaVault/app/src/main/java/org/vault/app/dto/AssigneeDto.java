package org.vault.app.dto;

/**
 * Created by aqeeb.pathan on 03-07-2015.
 */
public class AssigneeDto {
    private long id;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
