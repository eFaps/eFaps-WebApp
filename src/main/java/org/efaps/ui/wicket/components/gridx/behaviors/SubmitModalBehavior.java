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

import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.efaps.admin.ui.Command;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.objects.UIGrid;
import org.efaps.ui.wicket.pages.dialog.DialogPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class SubmitModalBehavior.
 *
 * @author The eFaps Team
 */
public class SubmitModalBehavior
    extends AjaxFormSubmitBehavior
    implements IAjaxIndicatorAware
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SubmitModalBehavior.class);

    /**
     * Instantiates a new submit modal behavior.
     */
    public SubmitModalBehavior()
    {
        super("click");
    }

    @Override
    protected void onSubmit(final AjaxRequestTarget _target)
    {
        super.onSubmit(_target);

        try {
            final IRequestParameters para = getComponent().getRequest().getRequestParameters();
            final StringValue rid = para.getParameterValue("rid");
            final UIGrid uiGrid = (UIGrid) getComponent().getPage().getDefaultModelObject();
            final Long cmdId = uiGrid.getID4Random(rid.toString());
            final Command cmd = Command.get(cmdId);
            final List<StringValue> oidValues = para.getParameterValues("selectedRow");
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
                final ModalWindowContainer modal = getComponent().getPage().visitChildren(ModalWindowContainer.class,
                                new ModalVisitor());
                modal.show(_target);
            } else {
                getComponent().getPage().visitChildren(ModalWindowContainer.class,
                                new IVisitor<ModalWindowContainer, Void>()
                                {

                                    @Override
                                    public void component(final ModalWindowContainer _modal,
                                                          final IVisit<Void> _visit)
                                    {
                                        _modal.setPageCreator(new ModalWindow.PageCreator()
                                        {

                                            private static final long serialVersionUID = 1L;

                                            @Override
                                            public Page createPage()
                                            {
                                                return new DialogPage(getComponent().getPage().getPageReference(),
                                                                "SubmitSelectedRows.fail" + cmd.getSubmitSelectedRows(),
                                                                false, false);
                                            }
                                        });
                                        _modal.setInitialHeight(150);
                                        _modal.setInitialWidth(350);
                                        _modal.show(_target);
                                        _visit.stop();
                                    }
                                });
            }
        } catch (final Exception e) {
            SubmitModalBehavior.LOG.error("Catched", e);
        }
    }

    @Override
    public String getAjaxIndicatorMarkupId()
    {
        return "eFapsVeil";
    }

}
