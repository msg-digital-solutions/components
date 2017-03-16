// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.components.marketo.tmarketoinput;

import static org.slf4j.LoggerFactory.getLogger;
import static org.talend.components.marketo.tmarketoinput.TMarketoInputProperties.InputOperation.CustomObject;
import static org.talend.daikon.properties.presentation.Widget.widget;
import static org.talend.daikon.properties.property.PropertyFactory.newBoolean;
import static org.talend.daikon.properties.property.PropertyFactory.newEnum;
import static org.talend.daikon.properties.property.PropertyFactory.newInteger;
import static org.talend.daikon.properties.property.PropertyFactory.newString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.avro.Schema;
import org.slf4j.Logger;
import org.talend.components.api.component.Connector;
import org.talend.components.api.component.ISchemaListener;
import org.talend.components.api.component.PropertyPathConnector;
import org.talend.components.marketo.MarketoComponentProperties;
import org.talend.components.marketo.MarketoConstants;
import org.talend.components.marketo.helpers.IncludeExcludeTypesTable;
import org.talend.components.marketo.helpers.MarketoColumnMappingsTable;
import org.talend.components.marketo.tmarketoconnection.TMarketoConnectionProperties.APIMode;
import org.talend.daikon.i18n.GlobalI18N;
import org.talend.daikon.i18n.I18nMessages;
import org.talend.daikon.properties.ValidationResult;
import org.talend.daikon.properties.ValidationResult.Result;
import org.talend.daikon.properties.presentation.Form;
import org.talend.daikon.properties.presentation.Widget;
import org.talend.daikon.properties.property.Property;

public class TMarketoInputProperties extends MarketoComponentProperties {

    private static final Logger LOG = getLogger(TMarketoInputProperties.class);

    private static final I18nMessages messages = GlobalI18N.getI18nMessageProvider()
            .getI18nMessages(TMarketoInputProperties.class);

    public enum InputOperation {
        getLead, // retrieves basic information of leads and lead activities in Marketo DB. getLead:
        getMultipleLeads, // retrieves lead records in batch.
        getLeadActivity, // retrieves the history of activity records for a single lead identified by the provided key.
        getLeadChanges, // checks the changes on Lead data in Marketo DB.
        CustomObject // CO Operation
    }

    public enum LeadSelector {
        LeadKeySelector,
        StaticListSelector,
        LastUpdateAtSelector
    }

    public enum LeadKeyTypeREST {
        id,
        cookie,
        email,
        twitterId,
        facebookId,
        linkedInId,
        sfdcAccountId,
        sfdcContactId,
        sfdcLeadId,
        sfdcLeadOwnerId,
        sfdcOpptyId
    }

    public enum LeadKeyTypeSOAP {

        IDNUM, // The Marketo ID (e.g. 64)
        COOKIE, // The value generated by the Munchkin Javascript.
        EMAIL, // The email address associated with the lead. (e.g. rufus@marketo.com)
        SFDCLEADID, // The lead ID from SalesForce
        LEADOWNEREMAIL, // The Lead Owner Email
        SFDCACCOUNTID, // The Account ID from SalesForce
        SFDCCONTACTID, // The Contact ID from SalesForce
        SFDCLEADOWNERID, // The Lead owner ID from SalesForce
        SFDCOPPTYID, // The Opportunity ID from SalesForce
    }

    public enum ListParam {
        STATIC_LIST_NAME,
        STATIC_LIST_ID
    }

    public enum IncludeExcludeFieldsSOAP {
        VisitWebpage,
        FillOutForm,
        ClickLink,
        RegisterForEvent,
        AttendEvent,
        SendEmail,
        EmailDelivered,
        EmailBounced,
        UnsubscribeEmail,
        OpenEmail,
        ClickEmail,
        NewLead,
        ChangeDataValue,
        LeadAssigned,
        NewSFDCOpprtnty,
        Wait,
        RunSubflow,
        RemoveFromFlow,
        PushLeadToSales,
        CreateTask,
        ConvertLead,
        ChangeScore,
        ChangeOwner,
        AddToList,
        RemoveFromList,
        SFDCActivity,
        EmailBouncedSoft,
        PushLeadUpdatesToSales,
        DeleteLeadFromSales,
        SFDCActivityUpdated,
        SFDCMergeLeads,
        MergeLeads,
        ResolveConflicts,
        AssocWithOpprtntyInSales,
        DissocFromOpprtntyInSales,
        UpdateOpprtntyInSales,
        DeleteLead,
        SendAlert,
        SendSalesEmail,
        OpenSalesEmail,
        ClickSalesEmail,
        AddtoSFDCCampaign,
        RemoveFromSFDCCampaign,
        ChangeStatusInSFDCCampaign,
        ReceiveSalesEmail,
        InterestingMoment,
        RequestCampaign,
        SalesEmailBounced,
        ChangeLeadPartition,
        ChangeRevenueStage,
        ChangeRevenueStageManually,
        ComputeDataValue,
        ChangeStatusInProgression,
        ChangeFieldInProgram,
        EnrichWithDatacom,
        ChangeSegment,
        ComputeSegmentation,
        ResolveRuleset,
        SmartCampaignTest,
        SmartCampaignTestTrigger
    }

    public enum IncludeExcludeFieldsREST {
        VisitWebpage(1),
        FillOutForm(2),
        ClickLink(3),
        SendEmail(6),
        EmailDelivered(7),
        EmailBounced(8),
        UnsubscribeEmail(9),
        OpenEmail(10),
        ClickEmail(11),
        NewLead(12),
        ChangeDataValue(13),
        SyncLeadToSFDC(19),
        ConvertLead(21),
        ChangeScore(22),
        ChangeOwner(23),
        AddToList(24),
        RemoveFromList(25),
        SFDCActivity(26),
        EmailBouncedSoft(27),
        DeleteLeadFromSFDC(29),
        SFDCActivityUpdated(30),
        MergeLeads(32),
        AddToOpportunity(34),
        RemoveFromOpportunity(35),
        UpdateOpportunity(36),
        DeleteLead(37),
        SendAlert(38),
        SendSalesEmail(39),
        OpenSalesEmail(40),
        ClickSalesEmail(41),
        AddToSFDCCampaign(42),
        RemoveFromSFDCCampaign(43),
        ChangeStatusInSFDCCampaign(44),
        ReceiveSalesEmail(45),
        InterestingMoment(46),
        RequestCampaign(47),
        SalesEmailBounced(48),
        ChangeLeadPartition(100),
        ChangeRevenueStage(101),
        ChangeRevenueStageManually(102),
        ChangeStatusInProgression(104),
        EnrichWithDataCom(106),
        ChangeSegment(108),
        CallWebhook(110),
        SentForwardToFriendEmail(111),
        ReceivedForwardToFriendEmail(112),
        AddToNurture(113),
        ChangeNurtureTrack(114),
        ChangeNurtureCadence(115),
        ShareContent(400),
        VoteInPoll(401),
        ClickSharedLink(405);

        public int fieldVal;

        private static Map<Integer, IncludeExcludeFieldsREST> map = new HashMap<Integer, IncludeExcludeFieldsREST>();

        static {
            for (IncludeExcludeFieldsREST fv : IncludeExcludeFieldsREST.values()) {
                map.put(fv.fieldVal, fv);
            }
        }

        IncludeExcludeFieldsREST(final int fv) {
            fieldVal = fv;
        }

        public static IncludeExcludeFieldsREST valueOf(int fv) {
            return map.get(fv);
        }

    }

    public Property<InputOperation> inputOperation = newEnum("inputOperation", InputOperation.class).setRequired();

    public MarketoColumnMappingsTable mappingInput = new MarketoColumnMappingsTable("mappingInput");

    public Property<LeadSelector> leadSelectorSOAP = newEnum("leadSelectorSOAP", LeadSelector.class).setRequired();

    public Property<LeadSelector> leadSelectorREST = newEnum("leadSelectorREST", LeadSelector.class).setRequired();

    public Property<LeadKeyTypeREST> leadKeyTypeREST = newEnum("leadKeyTypeREST", LeadKeyTypeREST.class);

    public Property<LeadKeyTypeSOAP> leadKeyTypeSOAP = newEnum("leadKeyTypeSOAP", LeadKeyTypeSOAP.class);

    public Property<String> leadKeyValue = newString("leadKeyValue");

    public Property<String> leadKeyValues = newString("leadKeyValues");

    public Property<Integer> leadKeysSegmentSize = newInteger("leadKeysSegmentSize");

    public Property<ListParam> listParam = newEnum("listParam", ListParam.class);

    public Property<String> listParamValue = newString("listParamValue");

    public Property<String> fieldList = newString("fieldList");

    public Property<String> oldestCreateDate = newString("oldestCreateDate");

    public Property<String> oldestUpdateDate = newString("oldestUpdateDate");

    public Property<String> latestUpdateDate = newString("latestUpdateDate");

    public Property<String> latestCreateDate = newString("latestCreateDate");

    public Property<String> sinceDateTime = newString("sinceDateTime");

    public Property<Boolean> setIncludeTypes = newBoolean("setIncludeTypes");

    public IncludeExcludeTypesTable includeTypes = new IncludeExcludeTypesTable("includeTypes");

    public Property<Boolean> setExcludeTypes = newBoolean("setExcludeTypes");

    public IncludeExcludeTypesTable excludeTypes = new IncludeExcludeTypesTable("excludeTypes");

    /**
     * Custom objects
     */
    public enum CustomObjectAction {
        describe,
        list,
        get
    }

    public Property<String> customObjectName = newString("customObjectName");

    public Property<CustomObjectAction> customObjectAction = newEnum("customObjectAction", CustomObjectAction.class);

    public Property<String> customObjectNames = newString("customObjectNames");

    public Property<String> customObjectFilterType = newString("customObjectFilterType");

    public Property<String> customObjectFilterValues = newString("customObjectFilterValues");

    //
    private static final long serialVersionUID = 3335746787979781L;

    public TMarketoInputProperties(String name) {
        super(name);
    }

    @Override
    public Schema getSchema(Connector connector, boolean isOutputConnection) {
        if (isOutputConnection)
            return schemaFlow.schema.getValue();
        return schemaInput.schema.getValue();
    }

    @Override
    protected Set<PropertyPathConnector> getAllSchemaPropertiesConnectors(boolean isOutputConnection) {
        if (isOutputConnection)
            return Collections.singleton(FLOW_CONNECTOR);
        else
            return Collections.singleton(MAIN_CONNECTOR);
    }

    @Override
    public void setupProperties() {
        super.setupProperties();
        //
        inputOperation.setPossibleValues((Object[]) InputOperation.values());
        inputOperation.setValue(InputOperation.getLead);
        leadSelectorSOAP.setPossibleValues((Object[]) LeadSelector.values());
        leadSelectorSOAP.setValue(LeadSelector.LeadKeySelector);
        leadSelectorREST.setPossibleValues(LeadSelector.LeadKeySelector, LeadSelector.StaticListSelector);
        leadSelectorREST.setValue(LeadSelector.LeadKeySelector);

        setSchemaListener(new ISchemaListener() {

            @Override
            public void afterSchema() {
                updateMappings();
            }
        });
        schemaInput.schema.setValue(MarketoConstants.getRESTSchemaForGetLeadOrGetMultipleLeads());
        schemaFlow.schema.setValue(MarketoConstants.getRESTSchemaForGetLeadOrGetMultipleLeads());
        updateMappings();

        setIncludeTypes.setValue(false);
        includeTypes.type.setPossibleValues((Object[]) IncludeExcludeFieldsREST.values());
        setExcludeTypes.setValue(false);
        excludeTypes.type.setPossibleValues((Object[]) IncludeExcludeFieldsREST.values());
        fieldList.setValue("");
        leadKeysSegmentSize.setValue(50);
        sinceDateTime.setValue("yyyy-MM-dd HH:mm:ss Z");
        //
        // Custom Objects
        //
        customObjectAction.setPossibleValues((Object[]) CustomObjectAction.values());
        customObjectAction.setValue(CustomObjectAction.describe);
        customObjectName.setValue("");
        customObjectNames.setValue("");
        customObjectFilterType.setValue("");
        customObjectFilterValues.setValue("");
    }

    @Override
    public void setupLayout() {
        super.setupLayout();

        Form mainForm = getForm(Form.MAIN);
        mainForm.addRow(inputOperation);
        // Custom Objects
        mainForm.addColumn(customObjectAction);
        mainForm.addRow(customObjectName);
        mainForm.addRow(customObjectNames);
        mainForm.addRow(customObjectFilterType);
        mainForm.addColumn(customObjectFilterValues);
        //
        mainForm.addRow(widget(mappingInput).setWidgetType(Widget.TABLE_WIDGET_TYPE));
        // leadSelector
        mainForm.addRow(leadSelectorSOAP);
        mainForm.addRow(leadSelectorREST);
        mainForm.addColumn(leadKeyTypeREST);
        mainForm.addColumn(leadKeyTypeSOAP);
        mainForm.addColumn(leadKeyValue);
        mainForm.addColumn(widget(leadKeyValues).setWidgetType(Widget.ENUMERATION_WIDGET_TYPE));
        //
        mainForm.addRow(listParam);
        mainForm.addColumn(listParamValue);
        //
        mainForm.addRow(oldestCreateDate);
        mainForm.addColumn(latestCreateDate);
        //
        mainForm.addRow(oldestUpdateDate);
        mainForm.addColumn(latestUpdateDate);
        //
        mainForm.addRow(setIncludeTypes);
        mainForm.addRow(widget(includeTypes).setWidgetType(Widget.TABLE_WIDGET_TYPE));
        mainForm.addRow(setExcludeTypes);
        mainForm.addRow(widget(excludeTypes).setWidgetType(Widget.TABLE_WIDGET_TYPE));
        //
        mainForm.addRow(fieldList);
        mainForm.addRow(sinceDateTime);
        //
        mainForm.addRow(batchSize);
        mainForm.addRow(dieOnError);
    }

    @Override
    public void refreshLayout(Form form) {
        super.refreshLayout(form);

        boolean useSOAP = connection.apiMode.getValue().equals(APIMode.SOAP);
        //
        if (form.getName().equals(Form.MAIN)) {
            // first hide everything
            form.getWidget(leadSelectorSOAP.getName()).setVisible(false);
            form.getWidget(leadSelectorREST.getName()).setVisible(false);
            form.getWidget(leadKeyTypeSOAP.getName()).setVisible(false);
            form.getWidget(leadKeyTypeREST.getName()).setVisible(false);
            form.getWidget(leadKeyValue.getName()).setVisible(false);
            form.getWidget(leadKeyValues.getName()).setVisible(false);
            form.getWidget(listParam.getName()).setVisible(false);
            form.getWidget(listParamValue.getName()).setVisible(false);
            form.getWidget(oldestUpdateDate.getName()).setVisible(false);
            form.getWidget(latestUpdateDate.getName()).setVisible(false);
            form.getWidget(setIncludeTypes.getName()).setVisible(false);
            form.getWidget(setExcludeTypes.getName()).setVisible(false);
            form.getWidget(includeTypes.getName()).setVisible(false);
            form.getWidget(excludeTypes.getName()).setVisible(false);
            form.getWidget(fieldList.getName()).setVisible(false);
            form.getWidget(sinceDateTime.getName()).setVisible(false);
            form.getWidget(oldestCreateDate.getName()).setVisible(false);
            form.getWidget(latestCreateDate.getName()).setVisible(false);
            form.getWidget(batchSize.getName()).setVisible(false);
            // custom objects
            form.getWidget(customObjectAction.getName()).setVisible(false);
            form.getWidget(customObjectName.getName()).setVisible(false);
            form.getWidget(customObjectNames.getName()).setVisible(false);
            form.getWidget(customObjectFilterType.getName()).setVisible(false);
            form.getWidget(customObjectFilterValues.getName()).setVisible(false);
            //
            // enable widgets according params
            //
            if (useSOAP) {
                inputOperation.setPossibleValues(InputOperation.getLead, InputOperation.getMultipleLeads,
                        InputOperation.getLeadActivity, InputOperation.getLeadChanges);
            } else {
                inputOperation.setPossibleValues(InputOperation.values());
            }
            //
            form.getWidget(mappingInput.getName()).setVisible(true);
            // getLead
            if (inputOperation.getValue().equals(InputOperation.getLead)) {
                form.getWidget(leadKeyValue.getName()).setVisible(true);
                if (useSOAP) {
                    form.getWidget(leadKeyTypeSOAP.getName()).setVisible(true);
                } else {
                    form.getWidget(leadKeyTypeREST.getName()).setVisible(true);
                }
            }
            // getMultipleLeads
            if (inputOperation.getValue().equals(InputOperation.getMultipleLeads)) {
                if (useSOAP) {
                    form.getWidget(leadSelectorSOAP.getName()).setVisible(true);
                    switch (leadSelectorSOAP.getValue()) {
                    case LeadKeySelector:
                        form.getWidget(leadKeyTypeSOAP.getName()).setVisible(true);
                        form.getWidget(leadKeyValues.getName()).setVisible(true);
                        break;
                    case StaticListSelector:
                        form.getWidget(listParam.getName()).setVisible(true);
                        form.getWidget(listParamValue.getName()).setVisible(true);
                        break;
                    case LastUpdateAtSelector:
                        form.getWidget(oldestUpdateDate.getName()).setVisible(true);
                        form.getWidget(latestUpdateDate.getName()).setVisible(true);
                        break;
                    }
                } else {
                    form.getWidget(leadSelectorREST.getName()).setVisible(true);
                    switch (leadSelectorREST.getValue()) {
                    case LeadKeySelector:
                        form.getWidget(leadKeyTypeREST.getName()).setVisible(true);
                        form.getWidget(leadKeyValues.getName()).setVisible(true);
                        break;
                    case StaticListSelector:
                        form.getWidget(listParam.getName()).setVisible(true);
                        form.getWidget(listParamValue.getName()).setVisible(true);
                        break;
                    }
                }
            }
            // getLeadActivity
            if (inputOperation.getValue().equals(InputOperation.getLeadActivity)) {
                if (useSOAP) {
                    form.getWidget(leadKeyTypeSOAP.getName()).setVisible(true);
                    form.getWidget(leadKeyValue.getName()).setVisible(true);
                } else {
                    form.getWidget(sinceDateTime.getName()).setVisible(true);
                }
                form.getWidget(setIncludeTypes.getName()).setVisible(true);
                form.getWidget(includeTypes.getName()).setVisible(setIncludeTypes.getValue());
                form.getWidget(setExcludeTypes.getName()).setVisible(true);
                form.getWidget(excludeTypes.getName()).setVisible(setExcludeTypes.getValue());
                form.getWidget(batchSize.getName()).setVisible(true);
            }
            // getLeadChanges
            if (inputOperation.getValue().equals(InputOperation.getLeadChanges)) {
                if (useSOAP) {
                    form.getWidget(setIncludeTypes.getName()).setVisible(true);
                    form.getWidget(includeTypes.getName()).setVisible(setIncludeTypes.getValue());
                    form.getWidget(setExcludeTypes.getName()).setVisible(true);
                    form.getWidget(excludeTypes.getName()).setVisible(setExcludeTypes.getValue());
                    form.getWidget(oldestCreateDate.getName()).setVisible(true);
                    form.getWidget(latestCreateDate.getName()).setVisible(true);
                } else {
                    form.getWidget(fieldList.getName()).setVisible(true);
                    form.getWidget(sinceDateTime.getName()).setVisible(true);
                }
                form.getWidget(batchSize.getName()).setVisible(true);
            }
            // Custom Objects
            if (inputOperation.getValue().equals(CustomObject)) {
                form.getWidget(mappingInput.getName()).setVisible(false); // don't need mappings for CO.
                form.getWidget(customObjectAction.getName()).setVisible(true);
                switch (customObjectAction.getValue()) {
                case describe:
                    form.getWidget(customObjectName.getName()).setVisible(true);
                    break;
                case list:
                    form.getWidget(customObjectNames.getName()).setVisible(true);
                    break;
                case get:
                    form.getWidget(customObjectName.getName()).setVisible(true);
                    form.getWidget(customObjectFilterType.getName()).setVisible(true);
                    form.getWidget(customObjectFilterValues.getName()).setVisible(true);
                    form.getWidget(batchSize.getName()).setVisible(true);
                    break;
                }
            }
        }
    }

    public ValidationResult validateInputOperation() {
        if (connection.apiMode.getValue().equals(APIMode.SOAP)) {
            if (inputOperation.getValue().equals(CustomObject)) {
                ValidationResult vr = new ValidationResult();
                vr.setStatus(Result.ERROR);
                vr.setMessage(messages.getMessage("error.validation.customobjects.nosoap"));
                return vr;
            }
        }
        return ValidationResult.OK;
    }

    public void updateMappings() {
        List<String> fld = getSchemaFields();
        mappingInput.columnName.setValue(fld);
        // protect mappings...
        if (fld.size() != mappingInput.size()) {
            List<String> mcn = new ArrayList<>();
            for (String t : fld)
                mcn.add("");
            mappingInput.marketoColumnName.setValue(mcn);
        }
        leadKeyValues.setPossibleValues(fld);
    }

    public void updateSchemaRelated() {
        Schema s = null;
        if (connection.apiMode.getValue().equals(APIMode.SOAP)) {
            switch (inputOperation.getValue()) {
            case getLead:
            case getMultipleLeads:
                s = MarketoConstants.getSOAPSchemaForGetLeadOrGetMultipleLeads();
                break;
            case getLeadActivity:
                s = MarketoConstants.getSOAPSchemaForGetLeadActivity();
                break;
            case getLeadChanges:
                s = MarketoConstants.getSOAPSchemaForGetLeadChanges();
                break;
            }
        } else {
            switch (inputOperation.getValue()) {
            case getLead:
            case getMultipleLeads:
                s = MarketoConstants.getRESTSchemaForGetLeadOrGetMultipleLeads();
                break;
            case getLeadActivity:
                s = MarketoConstants.getRESTSchemaForGetLeadActivity();
                break;
            case getLeadChanges:
                s = MarketoConstants.getRESTSchemaForGetLeadChanges();
                break;
            case CustomObject:
                switch (customObjectAction.getValue()) {
                case describe:
                case list:
                    s = MarketoConstants.getCustomObjectDescribeSchema();
                    break;
                case get:
                    s = MarketoConstants.getCustomObjectRecordSchema();
                    break;
                }
                break;
            }
        }
        schemaInput.schema.setValue(s);
        schemaFlow.schema.setValue(s);
        updateMappings();
    }

    public void beforeLeadKeyValues() {
        leadKeyValues.setPossibleValues(getSchemaFields());
    }

    public void afterInputOperation() {
        updateSchemaRelated();
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterCustomObjectAction() {
        afterInputOperation();
    }

    public void afterLeadSelectorSOAP() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterLeadSelectorREST() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterSetIncludeTypes() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterSetExcludeTypes() {
        refreshLayout(getForm(Form.MAIN));
    }

    public void afterApiMode() {
        afterInputOperation();
    }

    public void afterConnectionApiMode() {
        afterInputOperation();
    }
}
