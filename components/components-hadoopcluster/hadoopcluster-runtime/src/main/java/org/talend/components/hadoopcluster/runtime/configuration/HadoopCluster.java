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
package org.talend.components.hadoopcluster.runtime.configuration;

import java.util.List;
import java.util.Map;

public interface HadoopCluster {

	public void setBlacklistParams(List<String> names);
	
    public Map<HadoopHostedService, HadoopClusterService> getHostedServices();

}
