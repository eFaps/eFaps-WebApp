package org.efaps.ui.wicket.components.gridx.behaviors;

import java.io.File;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
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
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.objects.UICmdObject;
import org.efaps.ui.wicket.models.objects.UIGrid;
import org.efaps.ui.wicket.pages.AbstractMergePage;
import org.efaps.ui.wicket.pages.content.grid.GridPage;
import org.efaps.ui.wicket.pages.dialog.DialogPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.util.ParameterUtil;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                                            _modal.setPageCreator(new ModalWindow.PageCreator()
                                            {

                                                private static final long serialVersionUID = 1L;

                                                @Override
                                                public Page createPage()
                                                {
                                                    Page page = null;
                                                    try {
                                                        page = new DialogPage(getComponent().getPage()
                                                                        .getPageReference(), UICmdObject.getModel(
                                                                                        cmdId), oids);
                                                    } catch (final EFapsException e) {
                                                        page = new ErrorPage(e);
                                                    }
                                                    return page;
                                                }
                                            });
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
        } catch (final EFapsException e) {
            SubmitBehavior.LOG.error("Catched", e);
        }
    }

    @Override
    public String getAjaxIndicatorMarkupId()
    {
        return "eFapsVeil";
    }
}
