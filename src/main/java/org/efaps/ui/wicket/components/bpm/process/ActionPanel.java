/*
 * Copyright 2003 - 2013 The eFaps Team
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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.ui.wicket.components.bpm.process;

import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.iterator.ComponentHierarchyIterator;
import org.efaps.ui.wicket.models.objects.UIProcessInstanceLog;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ActionPanel
    extends Panel
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketID component id
     * @param _model model for contact
     * @param _pageReference reference to the calling page
     */
    public ActionPanel(final String _wicketID,
                       final IModel<UIProcessInstanceLog> _model,
                       final PageReference _pageReference)
    {
        super(_wicketID, _model);
        final AjaxLink<UIProcessInstanceLog> select = new AjaxLink<UIProcessInstanceLog>("select", _model)
        {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(final AjaxRequestTarget _target)
            {
                final UIProcessInstanceLog processinstance = (UIProcessInstanceLog) getDefaultModelObject();
                if (processinstance != null) {
                    final ComponentHierarchyIterator iter = getPage().visitChildren(AjaxFallbackDefaultDataTable.class);
                    while (iter.hasNext()) {
                        final AjaxFallbackDefaultDataTable<?, ?> table = (AjaxFallbackDefaultDataTable<?, ?>) iter
                                        .next();
                        final IDataProvider<?> provider = table.getDataProvider();
                        if (provider instanceof NodeInstanceProvider) {
                            ((NodeInstanceProvider) provider).setProcessInstanceId(processinstance
                                            .getProcessInstanceId());
                            ((NodeInstanceProvider) provider).requery();
                            _target.add(table);
                            break;
                        }
                    }

                }
            }
        };
        add(select);
    }
}
