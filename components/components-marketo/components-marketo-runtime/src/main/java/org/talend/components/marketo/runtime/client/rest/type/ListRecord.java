//============================================================================
//
// Copyright (C) 2006-2023 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
//============================================================================
package org.talend.components.marketo.runtime.client.rest.type;

import java.util.Date;

public class ListRecord {

    private Integer id;

    private String name;

    private String description;

    private String programName;

    private Date createdAt;

    private Date updatedAt;

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return this.id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public String getProgramName() {
        return this.programName;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getCreatedAt() {
        return this.createdAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getUpdatedAt() {
        return this.updatedAt;
    }

    @Override
    public String toString() {
        return "ListRecord [id=" + id + ", name=" + name + ", description=" + description + ", programName=" + programName
                + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + "]";
    }

}
