package org.efaps.ui.wicket.components.gridx.behaviors;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.efaps.admin.event.Return;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.Command;
import org.efaps.db.Instance;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowAjaxPageCreator;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.objects.ICmdUIObject;
import org.efaps.ui.wicket.models.objects.UIGrid;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

public class OpenModalBehavior
    extends AjaxEventBehavior
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public OpenModalBehavior()
    {
        super("click");
    }

    /**
     * Show the modal window.
     *
     * @param _target AjaxRequestTarget
     */
    @Override
    protected void onEvent(final AjaxRequestTarget _target)
    {
        final ModalWindowContainer modal = getComponent().getPage().visitChildren(ModalWindowContainer.class,
                        new ModalVisitor());

        modal.show(_target);
    }

    private static class ModalVisitor
        implements IVisitor<ModalWindowContainer, ModalWindowContainer>, Serializable
    {
        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        @Override
        public void component(final ModalWindowContainer _modal,
                              final IVisit<ModalWindowContainer> _visit)
        {
            _modal.reset();
            final IRequestParameters para = _modal.getRequest().getRequestParameters();
            final StringValue rid = para.getParameterValue("rid");
            final UIGrid uiGrid = (UIGrid) _modal.getPage().getDefaultModelObject();
            final Long cmdId = uiGrid.getID4Random(rid.toString());

            final ModalWindowAjaxPageCreator pageCreator = new ModalWindowAjaxPageCreator(new ICmdUIObject()
            {

                /**
                 * The Constant
                 * serialVersionUID.
                 */
                private static final long serialVersionUID = 1L;

                @Override
                public AbstractCommand getCommand()
                    throws EFapsException
                {
                    return Command.get(cmdId);
                }

                @Override
                public Instance getInstance()
                {
                    return null;
                }

                @Override
                public List<Return> executeEvents(final Object... _objectTuples)
                    throws EFapsException
                {
                    return null;
                }
            }, _modal);
            try {
                final Command cmd = Command.get(cmdId);
                _modal.setInitialHeight(cmd.getWindowHeight());
                _modal.setInitialWidth(cmd.getWindowWidth());
            } catch (final CacheReloadException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            _modal.setPageCreator(pageCreator);
            _visit.stop(_modal);
        }
    }
}
