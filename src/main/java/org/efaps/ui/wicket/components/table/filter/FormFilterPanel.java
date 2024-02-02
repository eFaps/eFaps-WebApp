/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.ui.wicket.components.table.filter;

import org.apache.wicket.PageReference;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.efaps.admin.ui.Command;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UITableHeader;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class FormFilterPanel
    extends Panel
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * Id of the Iframe.
     */
    public static final String IFRAME_ID = "eFapsFilterFrame";

    /**
     * @param _id
     */
    public FormFilterPanel(final String _wicketId,
                           final IModel<UITableHeader> _model,
                           final PageReference _pageReference)
    {
        super(_wicketId);

        final String cmdName = _model.getObject().getField().getProperty("FilterCmd");
        try {
            final Command cmd = Command.get(cmdName);
            final UIForm uiform = new UIForm(cmd.getUUID(), null);
            uiform.setCallingCommandUUID(_model.getObject().getUiHeaderObject().getCommand().getUUID());
            final FormContainer form = new FormContainer("form");
            add(form);
            FormPage.updateFormContainer(_pageReference.getPage(), form, uiform);
        } catch (final EFapsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
