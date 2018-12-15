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

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Return;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.Command;
import org.efaps.db.Instance;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowAjaxPageCreator;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.objects.ICmdUIObject;
import org.efaps.ui.wicket.models.objects.PagePosition;
import org.efaps.ui.wicket.models.objects.grid.UIGrid;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

/**
 * The Class ModalVisitor.
 *
 * @author The eFaps Team
 */
public class ModalVisitor
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
        final PagePosition pagePosition;

        switch(uiGrid.getPagePosition()) {
            case TREE:
                pagePosition = PagePosition.TREEMODAL;
                break;
            case CONTENT:
            default:
                pagePosition = PagePosition.CONTENTMODAL;
        }

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
                return uiGrid.getCallInstance();
            }

            @Override
            public List<Return> executeEvents(final EventType _eventType,
                                              final Object... _objectTuples)
                throws EFapsException
            {
                return null;
            }
        }, _modal, pagePosition);
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
