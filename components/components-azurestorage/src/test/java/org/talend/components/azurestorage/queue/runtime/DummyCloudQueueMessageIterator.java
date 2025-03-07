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
package org.talend.components.azurestorage.queue.runtime;

import java.util.Iterator;
import java.util.List;

import com.microsoft.azure.storage.queue.CloudQueueMessage;

public class DummyCloudQueueMessageIterator implements Iterator<CloudQueueMessage> {

    private Iterator<CloudQueueMessage> it;

    public DummyCloudQueueMessageIterator(List<CloudQueueMessage> list) {
        super();
        this.it = list.iterator();
    }

    @Override
    public boolean hasNext() {

        return it.hasNext();
    }

    @Override
    public CloudQueueMessage next() {
        return it.next();
    }

}
