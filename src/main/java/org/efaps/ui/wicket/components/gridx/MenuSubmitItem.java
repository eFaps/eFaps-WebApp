package org.efaps.ui.wicket.components.gridx;

import java.io.File;
import java.util.List;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.html.WebComponent;
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
import org.efaps.ui.wicket.components.confirmation.ConfirmationPanel;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.objects.UICmdObject;
import org.efaps.ui.wicket.models.objects.UIGrid;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.util.ParameterUtil;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

public class MenuSubmitItem
    extends WebComponent
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new menu submit item.
     *
     * @param _wicketId the wicket id
     */
    public MenuSubmitItem(final String _wicketId)
    {
        super(_wicketId);
        add(new MenuItemSubmitAjaxBehavior());
    }

    /**
     * Gets the bavior.
     *
     * @return the bavior
     */
    public MenuItemSubmitAjaxBehavior getBavior()
    {
        return getBehaviors(MenuItemSubmitAjaxBehavior.class).get(0);
    }

    public static class MenuItemSubmitAjaxBehavior
        extends AjaxFormSubmitBehavior
    {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /**
         * Instantiates a new menu item submit ajax behavior.
         */
        public MenuItemSubmitAjaxBehavior()
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
                                                _modal.setContent(new ConfirmationPanel(_modal.getContentId(),
                                                                UICmdObject.getModel(cmdId)));
                                                _modal.setInitialHeight(150);
                                                _modal.setInitialWidth(350);
                                                _modal.setTop(false);
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
                                    rets = cmd.executeEvents(EventType.UI_COMMAND_EXECUTE, ParameterValues.OTHERS,
                                                    oids);
                                } else {
                                    rets = cmd.executeEvents(EventType.UI_COMMAND_EXECUTE);
                                }
                                if (cmd.isTargetShowFile() && rets != null && !rets.isEmpty()) {
                                    final Object object = rets.get(0).get(ReturnValues.VALUES);
                                    if (object instanceof File) {
                                        ((EFapsSession) getComponent().getSession()).setFile((File) object);
                                       // ((AbstractMergePage) getPage()).getDownloadBehavior().initiate(_target);
                                        updatePage = false;
                                    }
                                }
                            } catch (final EFapsException e) {
                                throw new RestartResponseException(new ErrorPage(e));
                            }
                        }
                        if (updatePage) {
                           /** final AbstractUIObject uiObject = (AbstractUIObject) getPage().getDefaultModelObject();
                            uiObject.resetModel();

                            Page page = null;
                            try {
                                if (uiObject instanceof UITable) {
                                    page = new TablePage(Model.of((UITable) uiObject),
                                                    ((AbstractContentPage) getPage()).getCalledByPageReference());
                                } else if (uiObject instanceof UIForm) {
                                    page = new FormPage(Model.of((UIForm) uiObject),
                                                    ((AbstractContentPage) getPage()).getCalledByPageReference());
                                }
                            } catch (final EFapsException e) {
                                page = new ErrorPage(e);
                            }
                            setResponsePage(page);
                            */
                        }
                    }
                }
            } catch (final CacheReloadException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
