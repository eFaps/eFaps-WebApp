/*
 * Copyright 2003 - 2016 The eFaps Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.efaps.ui.wicket.components.gridx.behaviors;

import java.io.File;
import java.util.List;
import org.efaps.ui.wicket.components.modalwindow.*;
import org.apache.wicket.Page;
import org.apache.wicket.PageReference;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;

import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.Command;
import org.efaps.db.Instance;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.objects.UICmdObject;
import org.efaps.ui.wicket.models.objects.grid.UIGrid;
import org.efaps.ui.wicket.pages.AbstractMergePage;
import org.efaps.ui.wicket.pages.content.grid.GridPage;
import org.efaps.ui.wicket.pages.dialog.DialogPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.util.ParameterUtil;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class SubmitBehavior.
 *
 * @author The eFaps Team
 */
public class SubmitBehavior
    extends AjaxFormSubmitBehavior
    implements IAjaxIndicatorAware
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SubmitBehavior.class);

    /**
     * Instantiates a new menu item submit ajax behavior.
     */
    public SubmitBehavior()
    {
        super("click");
    }

    @Override
    protected void onSubmit(final AjaxRequestTarget _target)
    {
        super.onSubmit(_target);

        final IRequestParameters para = getComponent().getRequest().getRequestParameters();
        final StringValue rid = para.getParameterValue("rid");
        final UIGrid uiGrid = (UIGrid) getComponent().getPage().getDefaultModelObject();
        final Long cmdId = uiGrid.getID4Random(rid.toString());

        try {
            final Command cmd = Command.get(cmdId);
            final List<StringValue> oidValues = para.getParameterValues("selectedRow");
            final String[] oids = ParameterUtil.parameter2Array(para, "selectedRow");
            boolean check = false;
            if (cmd.getSubmitSelectedRows() > -1) {
                if (cmd.getSubmitSelectedRows() > 0) {
                    check = oidValues == null ? false : oidValues.size() == cmd.getSubmitSelectedRows();
                } else {
                    check = oidValues == null ? false : !oidValues.isEmpty();
                }
            } else {
                check = true;
            }

            if (check) {
                if (cmd.isAskUser()) {
                    getComponent().getPage().visitChildren(ModalWindowContainer.class,
                                    new IVisitor<ModalWindowContainer, Void>()
                                    {

                                        @Override
                                        public void component(final ModalWindowContainer _modal,
                                                              final IVisit<Void> _visit)
                                        {
                                            _modal.setPageCreator(new AskDialogPageCreator(getComponent().getPage()
                                                            .getPageReference(), cmdId, oids,
                                                            ((UIGrid) getComponent().getPage().getDefaultModelObject())
                                                                .getCallInstance()));
                                            _modal.setInitialHeight(150);
                                            _modal.setInitialWidth(350);
                                            _modal.show(_target);
                                            _visit.stop();
                                        }
                                    });
                } else {
                    boolean updatePage = true;
                    if (cmd.hasEvents(EventType.UI_COMMAND_EXECUTE)) {
                        try {
                            final List<Return> rets;
                            if (oidValues != null) {
                                rets = cmd.executeEvents(EventType.UI_COMMAND_EXECUTE, ParameterValues.OTHERS, oids);
                            } else {
                                rets = cmd.executeEvents(EventType.UI_COMMAND_EXECUTE);
                            }
                            if (cmd.isTargetShowFile() && rets != null && !rets.isEmpty()) {
                                final Object object = rets.get(0).get(ReturnValues.VALUES);
                                if (object instanceof File) {
                                    ((EFapsSession) getComponent().getSession()).setFile((File) object);
                                    ((AbstractMergePage) getComponent().getPage()).getDownloadBehavior().initiate(
                                                    _target);
                                    updatePage = false;
                                }
                            }
                        } catch (final EFapsException e) {
                            throw new RestartResponseException(new ErrorPage(e));
                        }
                    }
                    if (updatePage) {
                        uiGrid.reload();
                        getComponent().setResponsePage(new GridPage(Model.of(uiGrid)));
                    }
                }
            } else {
                getComponent().getPage().visitChildren(ModalWindowContainer.class,
                                new IVisitor<ModalWindowContainer, Void>()
                                {

                                    @Override
                                    public void component(final ModalWindowContainer _modal,
                                                          final IVisit<Void> _visit)
                                    {
                                        _modal.setPageCreator(new WarnDialogPageCreator(getComponent().getPage()
                                                        .getPageReference(), cmd.getSubmitSelectedRows()));
                                        _modal.setInitialHeight(150);
                                        _modal.setInitialWidth(350);
                                        _modal.show(_target);
                                        _visit.stop();
                                    }
                                });
            }
        } catch (final EFapsException e) {
            SubmitBehavior.LOG.error("Catched", e);
        }
    }

    @Override
    public String getAjaxIndicatorMarkupId()
    {
        return "eFapsVeil";
    }

    /**
     * The Class DialogPageCreate.
     */
    static class AskDialogPageCreator
        implements LegacyModalWindow.PageCreator
    {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The page ref. */
        private final PageReference pageRef;

        /** The rows. */
        private final long cmdId;

        /** The oids. */
        private final String[] oids;

        /** The instance. */
        private final Instance instance;

        /**
         * Instantiates a new ask dialog page creator.
         *
         * @param _pageReference the page reference
         * @param _cmdId the cmd id
         * @param _oids the oids
         * @param _instance the instance
         */
        AskDialogPageCreator(final PageReference _pageReference,
                             final Long _cmdId,
                             final String[] _oids,
                             final Instance _instance)
        {
            this.pageRef = _pageReference;
            this.cmdId = _cmdId;
            this.oids = _oids;
            this.instance = _instance;
        }

        @Override
        public Page createPage()
        {
            Page ret;
            try {
                ret = new DialogPage(this.pageRef, UICmdObject.getModel(this.cmdId, this.instance), this.oids);
            } catch (final CacheReloadException e) {
                ret = new ErrorPage(e);
            }
            return ret;
        }
    }


    /**
     * The Class DialogPageCreate.
     */
    static class WarnDialogPageCreator
        implements LegacyModalWindow.PageCreator
    {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The page ref. */
        private final PageReference pageRef;

        /** The rows. */
        private final int rows;

        /**
         * Instantiates a new dialog page create.
         *
         * @param _pageRef the page ref
         * @param _rows the rows
         */
        WarnDialogPageCreator(final PageReference _pageRef,
                              final int _rows)
        {
            this.pageRef = _pageRef;
            this.rows = _rows;
        }

        @Override
        public Page createPage()
        {
            return new DialogPage(this.pageRef, "SubmitSelectedRows.fail" + this.rows, false, false);
        }
    }
}
