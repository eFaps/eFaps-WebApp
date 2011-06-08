/*
 * Copyright 2003 - 2011 The eFaps Team
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

package org.efaps.ui.wicket.components.form.command;

import java.util.Map;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;
import org.efaps.ui.wicket.models.cell.UIFormCellCmd;
import org.efaps.ui.wicket.models.objects.AbstractUIPageObject;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class EsjpAjaxComponent
    extends WebComponent
{

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AjaxCmdBehavior.class);

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId wicket id for this component
     * @param _model model for this component
     */
    public EsjpAjaxComponent(final String _wicketId,
                             final IModel<?> _model)
    {
        super(_wicketId, _model);
    }

    @Override
    protected void onComponentTagBody(final MarkupStream _markupstream,
                                      final ComponentTag _componenttag)
    {
        super.onComponentTagBody(_markupstream, _componenttag);
        final String script = ((AjaxCmdBehavior) getBehaviors().get(0)).getJavaScript();
        final UIFormCellCmd uiObject = (UIFormCellCmd) getDefaultModelObject();
        try {
            final AbstractUIPageObject pageObject = (AbstractUIPageObject) (getPage().getDefaultModelObject());
            final Map<String, String> uiID2Oid = pageObject == null ? null : pageObject.getUiID2Oid();
            final String content = uiObject.getRenderedContent(script, uiID2Oid);
            replaceComponentTagBody(_markupstream, _componenttag, content);
        } catch (final EFapsException e) {
            EsjpAjaxComponent.LOG.error("onComponentTagBody", e);
        }
    }
}
