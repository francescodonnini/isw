/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.syncope.client.console.wicket.markup.html.form;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.syncope.client.console.wicket.ajax.markup.html.ClearIndicatingAjaxLink;
import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.authroles.authorization.strategies.role.metadata.MetaDataRoleAuthorizationStrategy;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * This empty class must exist because there not seems to be alternative to provide specialized HTML for edit links.
 *
 * @param <T> model object type.
 */
public final class ActionLinksPanel<T extends Serializable> extends Panel {

    private static final long serialVersionUID = 322966537010107771L;

    private final PageReference pageRef;

    private final IModel<T> model;

    private boolean disableIndicator = false;

    private ActionLinksPanel(final String componentId, final IModel<T> model, final PageReference pageRef) {
        super(componentId, model);
        this.model = model;
        this.pageRef = pageRef;

        setOutputMarkupId(true);

        super.add(new Fragment("panelClaim", "emptyFragment", this));
        super.add(new Fragment("panelManageResources", "emptyFragment", this));
        super.add(new Fragment("panelManageUsers", "emptyFragment", this));
        super.add(new Fragment("panelManageGroups", "emptyFragment", this));
        super.add(new Fragment("panelMapping", "emptyFragment", this));
        super.add(new Fragment("panelAccountLink", "emptyFragment", this));
        super.add(new Fragment("panelResetTime", "emptyFragment", this));
        super.add(new Fragment("panelClone", "emptyFragment", this));
        super.add(new Fragment("panelCreate", "emptyFragment", this));
        super.add(new Fragment("panelEdit", "emptyFragment", this));
        super.add(new Fragment("panelReset", "emptyFragment", this));
        super.add(new Fragment("panelUserTemplate", "emptyFragment", this));
        super.add(new Fragment("panelGroupTemplate", "emptyFragment", this));
        super.add(new Fragment("panelEnable", "emptyFragment", this));
        super.add(new Fragment("panelSearch", "emptyFragment", this));
        super.add(new Fragment("panelDelete", "emptyFragment", this));
        super.add(new Fragment("panelExecute", "emptyFragment", this));
        super.add(new Fragment("panelDryRun", "emptyFragment", this));
        super.add(new Fragment("panelSelect", "emptyFragment", this));
        super.add(new Fragment("panelExport", "emptyFragment", this));
        super.add(new Fragment("panelSuspend", "emptyFragment", this));
        super.add(new Fragment("panelReactivate", "emptyFragment", this));
        super.add(new Fragment("panelReload", "emptyFragment", this));
        super.add(new Fragment("panelChangeView", "emptyFragment", this));
        super.add(new Fragment("panelUnlink", "emptyFragment", this));
        super.add(new Fragment("panelLink", "emptyFragment", this));
        super.add(new Fragment("panelUnassign", "emptyFragment", this));
        super.add(new Fragment("panelAssign", "emptyFragment", this));
        super.add(new Fragment("panelDeprovision", "emptyFragment", this));
        super.add(new Fragment("panelProvision", "emptyFragment", this));
        super.add(new Fragment("panelZoomIn", "emptyFragment", this));
        super.add(new Fragment("panelZoomOut", "emptyFragment", this));
    }

    public ActionLinksPanel<T> add(
            final ActionLink<T> link,
            final ActionLink.ActionType type,
            final String entitlements,
            final boolean enabled) {

        switch (type) {
            case CLAIM:
                return buildFragment(link, entitlements, enabled, "panelClaim", "fragmentClaim", "claimLink", false, null);
            case MANAGE_RESOURCES:
                return buildFragment(link, entitlements, enabled, "panelManageResources", "fragmentManageResources", "manageResourcesLink", false, null);
            case MANAGE_USERS:
                return buildFragment(link, entitlements, enabled, "panelManageUsers", "fragmentManageUsers", "manageUsersLink", false, null);
            case MANAGE_GROUPS:
                return buildFragment(link, entitlements, enabled, "panelManageGroups", "fragmentManageGroups", "manageGroupsLink", false, null);
            case MAPPING:
                return buildFragment(link, entitlements, enabled, "panelMapping", "fragmentMapping", "mappingLink", false, null);
            case ACCOUNT_LINK:
                return buildFragment(link, entitlements, enabled, "panelAccountLink", "fragmentAccountLink", "accountLinkLink", false, null);
            case RESET_TIME:
                return buildFragment(link, entitlements, enabled, "panelResetTime", "fragmentResetTime", "resetTimeLink", false, null);
            case CLONE:
                return buildFragment(link, entitlements, enabled, "panelClone", "fragmentClone", "cloneLink", false, null);
            case CREATE:
                return buildFragment(link, entitlements, enabled, "panelCreate", "fragmentCreate", "createLink", false, null);
            case RESET:
                return buildFragment(link, entitlements, enabled, "panelReset", "fragmentReset", "resetLink", false, null);
            case EDIT:
                return buildFragment(link, entitlements, enabled, "panelEdit", "fragmentEdit", "editLink", false, null);
            case USER_TEMPLATE:
                return buildFragment(link, entitlements, enabled, "panelUserTemplate", "fragmentUserTemplate", "userTemplateLink", false, null);
            case GROUP_TEMPLATE:
                return buildFragment(link, entitlements, enabled, "panelGroupTemplate", "fragmentGroupTemplate", "groupTemplateLink", false, null);
            case ENABLE:
                return buildFragment(link, entitlements, enabled, "panelEnable", "fragmentEnable", "enableLink", false, null);
            case SEARCH:
                return buildFragment(link, entitlements, enabled, "panelSearch", "fragmentSearch", "searchLink", false, null);
            case EXECUTE:
                return buildFragment(link, entitlements, enabled, "panelExecute", "fragmentExecute", "executeLink", false, null);
            case DRYRUN:
                return buildFragment(link, entitlements, enabled, "panelDryRun", "fragmentDryRun", "dryRunLink", false, null);
            case DELETE:
                return buildFragment(link, entitlements, enabled, "panelDelete", "fragmentDelete", "deleteLink", true, null);
            case SELECT:
                return buildFragment(link, entitlements, enabled, "panelSelect", "fragmentSelect", "selectLink", false, null);
            case EXPORT:
                return buildFragment(link, entitlements, enabled, "panelExport", "fragmentExport", "exportLink", false, null);
            case SUSPEND:
                return buildFragment(link, entitlements, enabled, "panelSuspend", "fragmentSuspend", "suspendLink", false, null);
            case REACTIVATE:
                return buildFragment(link, entitlements, enabled, "panelReactivate", "fragmentReactivate", "reactivateLink", false, null);
            case RELOAD:
                return buildFragment(link, entitlements, enabled, "panelReload", "fragmentReload", "reloadLink", false, null);
            case CHANGE_VIEW:
                return buildFragment(link, entitlements, enabled, "panelChangeView", "fragmentChangeView", "changeViewLink", false, null);
            case UNLINK:
                return buildFragment(link, entitlements, enabled, "panelUnlink", "fragmentUnlink", "unlinkLink", true, "confirmUnlink");
            case LINK:
                return buildFragment(link, entitlements, enabled, "panelLink", "fragmentLink", "linkLink", false, null);
            case UNASSIGN:
                return buildFragment(link, entitlements, enabled, "panelUnassign", "fragmentUnassign", "unassignLink", true, "confirmUnassign");
            case ASSIGN:
                return buildFragment(link, entitlements, enabled, "panelAssign", "fragmentAssign", "assignLink", false, null);
            case DEPROVISION:
                return buildFragment(link, entitlements, enabled, "panelDeprovision", "fragmentDeprovision", "deprovisionLink", true, "confirmDeprovision");
            case PROVISION:
                return buildFragment(link, entitlements, enabled, "panelProvision", "fragmentProvision", "provisionLink", false, null);
            case ZOOM_IN:
                return buildFragment(link, entitlements, enabled, "panelZoomIn", "fragmentZoomIn", "zoomInLink", false, null);
            case ZOOM_OUT:
                return buildFragment(link, entitlements, enabled, "panelZoomOut", "fragmentZoomOut", "zoomOutLink", false, null);
            default:
                return this;
        }
    }

    private ActionLinksPanel<T> buildFragment(
            final ActionLink<T> link,
            final String entitlements,
            final boolean enabled,
            final String panelId,
            final String fragmentId,
            final String linkId,
            final boolean isConfirm,
            final String confirmMessage) {

        Fragment fragment = new Fragment(panelId, fragmentId, this);

        if (isConfirm) {
            IndicatingOnConfirmAjaxLink<Void> confirmLink = confirmMessage == null
                    ? new IndicatingOnConfirmAjaxLink<Void>(linkId, pageRef) {
                private static final long serialVersionUID = 1L;
                @Override protected void onClickInternal(final AjaxRequestTarget target) { link.onClick(target, model.getObject()); }
                @Override public String getAjaxIndicatorMarkupId() { return disableIndicator ? StringUtils.EMPTY : super.getAjaxIndicatorMarkupId(); }
            }
                    : new IndicatingOnConfirmAjaxLink<Void>(linkId, pageRef, confirmMessage) {
                private static final long serialVersionUID = 1L;
                @Override protected void onClickInternal(final AjaxRequestTarget target) { link.onClick(target, model.getObject()); }
                @Override public String getAjaxIndicatorMarkupId() { return disableIndicator ? StringUtils.EMPTY : super.getAjaxIndicatorMarkupId(); }
            };
            confirmLink.feedbackPanelAutomaticReload(link.feedbackPanelAutomaticReload());
            fragment.addOrReplace(confirmLink);
        } else {
            ClearIndicatingAjaxLink<Void> standardLink = new ClearIndicatingAjaxLink<Void>(linkId, pageRef) {
                private static final long serialVersionUID = 1L;
                @Override protected void onClickInternal(final AjaxRequestTarget target) { link.onClick(target, model.getObject()); }
                @Override public String getAjaxIndicatorMarkupId() { return disableIndicator ? StringUtils.EMPTY : super.getAjaxIndicatorMarkupId(); }
            };
            standardLink.feedbackPanelAutomaticReload(link.feedbackPanelAutomaticReload());
            fragment.addOrReplace(standardLink);
        }

        fragment.setEnabled(enabled);
        MetaDataRoleAuthorizationStrategy.authorize(fragment, ENABLE, entitlements);
        super.addOrReplace(fragment);

        return this;
    }

    public void remove(final ActionLink.ActionType type) {
        switch (type) {
            case CLAIM:
                super.addOrReplace(new Fragment("panelClaim", "emptyFragment", this));
                break;

            case MANAGE_RESOURCES:
                super.addOrReplace(new Fragment("panelManageResources", "emptyFragment", this));
                break;

            case MANAGE_USERS:
                super.addOrReplace(new Fragment("panelManageUsers", "emptyFragment", this));
                break;

            case MANAGE_GROUPS:
                super.addOrReplace(new Fragment("panelManageGroups", "emptyFragment", this));
                break;

            case MAPPING:
                super.addOrReplace(new Fragment("panelMapping", "emptyFragment", this));
                break;

            case ACCOUNT_LINK:
                super.addOrReplace(new Fragment("panelAccountLink", "emptyFragment", this));
                break;

            case RESET_TIME:
                super.addOrReplace(new Fragment("panelResetTime", "emptyFragment", this));
                break;

            case CLONE:
                super.addOrReplace(new Fragment("panelClone", "emptyFragment", this));
                break;

            case CREATE:
                super.addOrReplace(new Fragment("panelCreate", "emptyFragment", this));
                break;

            case EDIT:
                super.addOrReplace(new Fragment("panelEdit", "emptyFragment", this));
                break;

            case USER_TEMPLATE:
                super.addOrReplace(new Fragment("panelUserTemplate", "emptyFragment", this));
                break;

            case SEARCH:
                super.addOrReplace(new Fragment("panelSearch", "emptyFragment", this));
                break;

            case EXECUTE:
                super.addOrReplace(new Fragment("panelExecute", "emptyFragment", this));
                break;

            case DRYRUN:
                super.addOrReplace(new Fragment("panelDryRun", "emptyFragment", this));
                break;

            case DELETE:
                super.addOrReplace(new Fragment("panelDelete", "emptyFragment", this));
                break;

            case SELECT:
                super.addOrReplace(new Fragment("panelSelect", "emptyFragment", this));
                break;

            case EXPORT:
                super.addOrReplace(new Fragment("panelExport", "emptyFragment", this));
                break;

            case SUSPEND:
                super.addOrReplace(new Fragment("panelSuspend", "emptyFragment", this));
                break;

            case REACTIVATE:
                super.addOrReplace(new Fragment("panelReactivate", "emptyFragment", this));
                break;

            case RELOAD:
                super.addOrReplace(new Fragment("panelReload", "emptyFragment", this));
                break;

            case CHANGE_VIEW:
                super.addOrReplace(new Fragment("panelChangeView", "emptyFragment", this));
                break;

            case UNLINK:
                super.addOrReplace(new Fragment("panelUnlink", "emptyFragment", this));
                break;

            case LINK:
                super.addOrReplace(new Fragment("panelLink", "emptyFragment", this));
                break;

            case UNASSIGN:
                super.addOrReplace(new Fragment("panelUnassign", "emptyFragment", this));
                break;

            case ASSIGN:
                super.addOrReplace(new Fragment("panelAssign", "emptyFragment", this));
                break;

            case DEPROVISION:
                super.addOrReplace(new Fragment("panelDeprovision", "emptyFragment", this));
                break;

            case PROVISION:
                super.addOrReplace(new Fragment("panelProvision", "emptyFragment", this));
                break;
            case ZOOM_IN:
                super.addOrReplace(new Fragment("panelZoomIn", "emptyFragment", this));
                break;
            case ZOOM_OUT:
                super.addOrReplace(new Fragment("panelZoomOut", "emptyFragment", this));
                break;
            default:
                // do nothing
        }
    }

    private ActionLinksPanel<T> setDisableIndicator(final boolean disableIndicator) {
        this.disableIndicator = disableIndicator;
        return this;
    }

    public static <T extends Serializable> Builder<T> builder(final PageReference pageRef) {
        return new Builder<T>(pageRef);
    }

    /**
     * ActionLinksPanel builder.
     *
     * @param <T> model object type.
     */
    public static final class Builder<T extends Serializable> implements Serializable {

        private static final long serialVersionUID = 1L;

        private final Map<ActionLink.ActionType, Triple<ActionLink<T>, String, Boolean>> actions = new HashMap<>();

        private final PageReference pageRef;

        private boolean disableIndicator = false;

        private Builder(final PageReference pageRef) {
            this.pageRef = pageRef;
        }

        public Builder<T> setDisableIndicator(final boolean disableIndicator) {
            this.disableIndicator = disableIndicator;
            return this;
        }

        public Builder<T> add(
                final ActionLink<T> link,
                final ActionLink.ActionType type,
                final String entitlements) {

            return addWithRoles(link, type, entitlements, true);
        }

        public Builder<T> add(
                final ActionLink<T> link,
                final ActionLink.ActionType type,
                final String entitlement,
                final boolean enabled) {

            return addWithRoles(link, type, entitlement, enabled);
        }

        public Builder<T> addWithRoles(
                final ActionLink<T> link,
                final ActionLink.ActionType type,
                final String entitlements) {

            return addWithRoles(link, type, entitlements, true);
        }

        public Builder<T> addWithRoles(
                final ActionLink<T> link,
                final ActionLink.ActionType type,
                final String entitlements,
                final boolean enabled) {
            actions.put(type, Triple.of(link, entitlements, enabled));
            return this;
        }

        /**
         * Use this method to build an ation panel without any model reference.
         *
         * @param id Component id.
         * @return Action link panel.
         */
        public ActionLinksPanel<T> build(final String id) {
            return build(id, null);
        }

        /**
         * Use this methos to build an action panel including a model reference.
         *
         * @param id Component id.
         * @param modelObject model object.
         * @return Action link panel.
         */
        public ActionLinksPanel<T> build(final String id, final T modelObject) {
            final ActionLinksPanel<T> panel = modelObject == null
                    ? new ActionLinksPanel<T>(id, new Model<T>(), this.pageRef)
                    : new ActionLinksPanel<T>(id, new Model<T>(modelObject), this.pageRef);

            panel.setDisableIndicator(disableIndicator);

            for (Entry<ActionLink.ActionType, Triple<ActionLink<T>, String, Boolean>> action : actions.entrySet()) {
                panel.add(
                        action.getValue().getLeft(),
                        action.getKey(),
                        action.getValue().getMiddle(),
                        action.getValue().getRight());
            }
            return panel;
        }
    }
}
