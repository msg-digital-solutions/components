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

package org.talend.components.netsuite.client.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Descriptor of NetSuite data object model type.
 */
public class TypeDesc {

    /** Short name of data object type. */
    private String typeName;

    /** Class of data object type. */
    private Class<?> typeClass;

    /** List of field descriptors for this data object type. */
    private List<FieldDesc> fields;

    /** Map of field descriptors by names, for faster access. */
    private Map<String, FieldDesc> fieldMap;

    public TypeDesc(String typeName, Class<?> typeClass, List<FieldDesc> fields) {
        this.typeName = typeName;
        this.typeClass = typeClass;
        this.fields = fields;

        fieldMap = new HashMap<>(fields.size());
        for (FieldDesc fieldDesc : fields) {
            fieldMap.put(fieldDesc.getName(), fieldDesc);
        }
    }

    public String getTypeName() {
        return typeName;
    }

    public Class<?> getTypeClass() {
        return typeClass;
    }

    public FieldDesc getField(String name) {
        return fieldMap.get(name);
    }

    public Map<String, FieldDesc> getFieldMap() {
        return Collections.unmodifiableMap(fieldMap);
    }

    public List<FieldDesc> getFields() {
        return Collections.unmodifiableList(fields);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TypeDesc{");
        sb.append("typeName='").append(typeName).append('\'');
        sb.append(", typeClass=").append(typeClass);
        sb.append(", fields=").append(fields);
        sb.append('}');
        return sb.toString();
    }
}
