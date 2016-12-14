package org.efaps.ui.wicket.components.confirmation;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.components.button.Button;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.objects.ICmdUIObject;
import org.efaps.ui.wicket.pages.dialog.DialogPage;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.util.EFapsException;

public class ConfirmationPanel
    extends GenericPanel<ICmdUIObject>
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new confirmation panel.
     *
     * @param _wicketId the wicket id
     * @param _model the model
     */
    public ConfirmationPanel(final String _wicketId,
                             final IModel<ICmdUIObject> _model)
    {
        super(_wicketId, _model);
        try {
            final String cmdName = getModelObject().getCommand().getName();
            add(new Label("textLabel", DBProperties.getProperty(cmdName + ".Question")).setOutputMarkupId(true));

            add(new Button("submitButton", new AjaxSubmitLink(Button.LINKID), DialogPage.getLabel(cmdName, "Submit"),
                            Button.ICON.ACCEPT.getReference()).setOutputMarkupId(true));

            add(new Button("closeButton", new AjaxCloseLink(Button.LINKID), DialogPage.getLabel(cmdName, "Cancel"),
                            Button.ICON.CANCEL.getReference()));

        } catch (final Exception e) {
            // TODO: handle exception
        }
    }

    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(DialogPage.CSS));
    }

    /**
     * AjaxLink that closes the ModalWindow this Page was opened in.
     */
    public class AjaxCloseLink
        extends AjaxLink<Object>
    {

        /** Needed for serialization. */
        private static final long serialVersionUID = 1L;

        /**
         * @param _wicketId wicket id of this component
         */
        public AjaxCloseLink(final String _wicketId)
        {
            super(_wicketId);
        }

        /**
         * @see org.apache.wicket.ajax.markup.html.AjaxLink#onClick(org.apache.wicket.ajax.AjaxRequestTarget)
         * @param _target request target
         */
        @Override
        public void onClick(final AjaxRequestTarget _target)
        {
            visitParents(ModalWindowContainer.class, new IVisitor<ModalWindowContainer, Void>()
            {

                @Override
                public void component(final ModalWindowContainer modal,
                                      final IVisit<Void> _visit)
                {
                    modal.close(_target);
                    _visit.stop();
                }
            });
        }
    }

    /**
     * AjaxLink that closes the ModalWindow this Page was opened in.
     */
    public class AjaxSubmitLink
        extends WebMarkupContainer
    {

        /** Needed for serialization. */
        private static final long serialVersionUID = 1L;

        /**
         * @param _wicketId wicket id of this component
         */
        public AjaxSubmitLink(final String _wicketId)
        {
            super(_wicketId);
            add(new AjaxFormSubmitBehavior("click")
            {

                /** The Constant serialVersionUID. */
                private static final long serialVersionUID = 1L;

                @Override
                protected Form<?> findForm()
                {
                    return getComponent().getPage().visitChildren(FormContainer.class,
                                    new IVisitor<FormContainer, FormContainer>()
                                    {

                                        @Override
                                        public void component(final FormContainer _object,
                                                              final IVisit<FormContainer> visit)
                                        {
                                            visit.stop(_object);
                                        }
                                    });
                }

                /**
                 * Override this method to provide special submit handling in a
                 * multi-button form. This method
                 * will be called <em>before</em> the form's onSubmit method.
                 */
                @Override
                protected void onSubmit(final AjaxRequestTarget _target)
                {
                    final List<StringValue> selectedRows = getRequest().getRequestParameters().getParameterValues(
                                    "selectedRow");

                    final ICmdUIObject cmdUI = (ICmdUIObject) findParent(ConfirmationPanel.class)
                                    .getDefaultModelObject();
                    final String[] oids;
                    if (selectedRows == null) {
                        oids = ArrayUtils.EMPTY_STRING_ARRAY;
                    } else {
                        oids = new String[selectedRows.size()];
                        int i = 0;
                        for (final StringValue value : selectedRows) {
                            oids[i] = value.toString();
                            i++;
                        }
                    }

                    try {
                        cmdUI.executeEvents(ParameterValues.OTHERS, oids);
                    } catch (final EFapsException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    visitParents(ModalWindowContainer.class, new IVisitor<ModalWindowContainer, Void>()
                    {

                        @Override
                        public void component(final ModalWindowContainer modal,
                                              final IVisit<Void> _visit)
                        {
                            modal.close(_target, cmdUI);
                            _visit.stop();
                        }
                    });
                }
            });
        }
    }
}
