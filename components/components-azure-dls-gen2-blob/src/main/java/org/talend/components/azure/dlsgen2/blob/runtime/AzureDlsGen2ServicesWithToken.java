//==============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
//==============================================================================

package org.talend.components.azure.dlsgen2.blob.runtime;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;

public class AzureDlsGen2ServicesWithToken implements AzureDlsGen2Services {

    private final String accountName;

    private final String tenantId;

    private final String clientId;

    private final String clientSecret;

    private final String authority = "https://login.microsoftonline.com/";

    private ClientSecretCredential clientSecretCredential;

    public AzureDlsGen2ServicesWithToken(String accountName, String tenantId, String clientId, String clientSecret) {
        this.accountName = accountName;
        this.tenantId = tenantId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        clientSecretCredential = new ClientSecretCredentialBuilder()
                .authorityHost(authority + this.tenantId)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .tenantId(tenantId)
                .build();
    }

    private AzureDlsGen2ServicesWithToken(Builder builder) {
        this.accountName = builder.accountName;
        this.tenantId = builder.tenantId;
        this.clientId = builder.clientId;
        this.clientSecret = builder.clientSecret;
        clientSecretCredential = new ClientSecretCredentialBuilder()
                .authorityHost(authority + tenantId)
                .clientId(builder.clientId)
                .clientSecret(builder.clientSecret)
                .tenantId(builder.tenantId)
                .build();
    }

    public String getAccountName() {
        return this.accountName;
    }

    public String getTenantId() {
        return this.tenantId;
    }

    public String getClientId() {
        return this.clientId;
    }

    public String getClientSecret() {
        return this.clientSecret;
    }

    public ClientSecretCredential getClientSecretCredential() {
        return this.clientSecretCredential;
    }

    @Override
    public BlobServiceClient getBlobServiceClient() {
        String endpoint = String.format("https://%s.blob.core.windows.net", accountName);
        return new BlobServiceClientBuilder()
                .endpoint(endpoint)
                .credential(clientSecretCredential)
                .buildClient();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String accountName;

        private String tenantId;

        private String clientId;

        private String clientSecret;


        public Builder accountName(String accountName) {
            this.accountName = accountName;
            return this;
        }

        public Builder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder clientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        public AzureDlsGen2ServicesWithToken build() {
            return new AzureDlsGen2ServicesWithToken(this);
        }
    }


}
