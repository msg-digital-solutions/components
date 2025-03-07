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
package org.talend.components.marketo.runtime;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.talend.components.api.component.runtime.Sink;
import org.talend.components.api.component.runtime.WriteOperation;
import org.talend.components.api.container.RuntimeContainer;
import org.talend.components.marketo.tmarketocampaign.TMarketoCampaignProperties;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties;
import org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.LeadSelector;
import org.talend.components.marketo.tmarketolistoperation.TMarketoListOperationProperties;
import org.talend.components.marketo.tmarketooutput.TMarketoOutputProperties;
import org.talend.components.marketo.wizard.MarketoComponentWizardBaseProperties.InputOperation;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.ValidationResultMutable;

import static org.slf4j.LoggerFactory.getLogger;
import static org.talend.components.marketo.MarketoConstants.REST_API_LIMIT;

public class MarketoSink extends MarketoSourceOrSink implements Sink {

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider().getI18nMessages(MarketoSink.class);

    private transient static final Logger LOG = getLogger(MarketoSink.class);

    @Override
    public WriteOperation<?> createWriteOperation() {
        return new MarketoWriteOperation(this);
    }

    @Override
    public ValidationResult validate(RuntimeContainer container) {
        ValidationResultMutable vr = new ValidationResultMutable(super.validate(container));
        if (vr.getStatus().equals(Result.ERROR)) {
            return vr;
        }
        // output
        if (properties instanceof TMarketoOutputProperties) {
            switch (((TMarketoOutputProperties) properties).outputOperation.getValue()) {
            case syncLead:
                break;
            case syncMultipleLeads:
                break;
            case deleteLeads:
                break;
            case syncCustomObjects:
                if (StringUtils.isEmpty(((TMarketoOutputProperties) properties).customObjectName.getValue())) {
                    vr.setStatus(Result.ERROR);
                    vr.setMessage(messages.getMessage("error.validation.customobject.customobjectname"));
                    return vr;
                }
                break;
            case deleteCustomObjects:
                if (StringUtils.isEmpty(((TMarketoOutputProperties) properties).customObjectName.getValue())) {
                    vr.setStatus(Result.ERROR);
                    vr.setMessage(messages.getMessage("error.validation.customobject.customobjectname"));
                    return vr;
                }
                break;
            }
            Integer bsize = ((TMarketoOutputProperties) properties).batchSize.getValue();
            if (bsize == null) {
                vr.setStatus(Result.ERROR);
                vr.setMessage(messages.getMessage("error.validation.batchSize.empty"));
                return vr;
            }
            if (bsize < 1 || bsize > REST_API_LIMIT) {
                ((TMarketoOutputProperties) properties).batchSize.setValue(REST_API_LIMIT);
                LOG.info(messages.getMessage("error.validation.batchSize.range", REST_API_LIMIT));
            }
        }
        // check list operations
        if (properties instanceof TMarketoListOperationProperties) {
            // nothing to check for now.
        }
        // check getMultipleLeads with an input
        if (properties instanceof TMarketoInputProperties) {
            // operation must be getMultipleLeads
            if (!((TMarketoInputProperties) properties).inputOperation.getValue().equals(InputOperation.getMultipleLeads)) {
                vr.setStatus(Result.ERROR);
                vr.setMessage(messages.getMessage("error.validation.sink.getmultipleleads.only"));
                return vr;
            }
            // lead selector must be LeadKeySelector
            LeadSelector selector;
            if (APIMode.SOAP.equals(properties.getConnectionProperties().apiMode.getValue())) {
                selector = ((TMarketoInputProperties) properties).leadSelectorSOAP.getValue();
            } else {
                selector = ((TMarketoInputProperties) properties).leadSelectorREST.getValue();
            }
            if (!selector.equals(LeadSelector.LeadKeySelector)) {
                vr.setStatus(Result.ERROR);
                vr.setMessage(messages.getMessage("error.validation.sink.leadkeyselector.only"));
                return vr;
            }
            // lead key values must be defined
            if (StringUtils.isEmpty(((TMarketoInputProperties) properties).leadKeyValues.getValue())) {
                vr.setStatus(Result.ERROR);
                vr.setMessage(messages.getMessage("error.validation.leadkeyvalues"));
                return vr;
            }
        }
        // Campaign
        if (properties instanceof TMarketoCampaignProperties) {
            TMarketoCampaignProperties p = (TMarketoCampaignProperties) properties;
            switch (p.campaignAction.getValue()) {
            case trigger:
                if (StringUtils.isEmpty(p.campaignId.getStringValue())) {
                    vr.setStatus(Result.ERROR);
                    vr.setMessage(messages.getMessage("error.validation.campaign.byid"));
                    return vr;
                }
                if (p.triggerCampaignForLeadsInBatch.getValue()) {
                    if (p.batchSize.getValue() < 1 || p.batchSize.getValue() > REST_API_LIMIT) {
                        p.batchSize.setValue(REST_API_LIMIT);
                        LOG.info(messages.getMessage("error.validation.batchSize.range", REST_API_LIMIT));
                    }
                }
                break;
            default:
                vr.setStatus(Result.ERROR);
                vr.setMessage(messages.getMessage("error.validation.campaign.operation"));
                return vr;
            }
        }

        return ValidationResult.OK;
    }

}
