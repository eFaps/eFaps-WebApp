/*
 * Copyright 2003 - 2012 The eFaps Team
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
 * Revision:        $Rev:1510 $
 * Last Changed:    $Date:2007-10-18 09:35:40 -0500 (Thu, 18 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.components.form;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Page;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.components.LabelComponent;
import org.efaps.ui.wicket.components.embeddedlink.LinkElementComponent;
import org.efaps.ui.wicket.components.embeddedlink.LinkElementLink;
import org.efaps.ui.wicket.components.form.row.RowPanel;
import org.efaps.ui.wicket.models.EmbeddedLink;
import org.efaps.ui.wicket.models.UIModel;
import org.efaps.ui.wicket.models.cell.UIHiddenCell;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UIForm.FormElement;
import org.efaps.ui.wicket.models.objects.UIForm.FormRow;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.main.MainPage;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.util.EFapsException;

/**
 * TODO description.
 *
 * @author The eFaps Team
 * @version $Id$
 *
 */
public class FormPanel
    extends Panel
{
    /**
     * Reference to the style sheet.
     */
    public static final EFapsContentReference CSS = new EFapsContentReference(FormPanel.class, "FormPanel.css");

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Map containing the required Components. Used for the check if this components are filled in.
     */
    private final Map<String, Label> requiredComponents = new HashMap<String, Label>();

    /**
     * @param _wicketId wicket id of this component
     * @param _page page this component is in
     * @param _model model of this component
     * @param _formelementmodel model of the form element
     * @param _form fom container
     * @throws EFapsException on error
     */
    public FormPanel(final String _wicketId,
                     final Page _page,
                     final IModel<UIForm> _model,
                     final FormElement _formelementmodel,
                     final FormContainer _form)
        throws EFapsException
    {
        super(_wicketId, _model);
        final UIForm uiForm = _model.getObject();
        if (!uiForm.isInitialized()) {
            uiForm.execute();
        }

        final RepeatingView rowRepeater = new RepeatingView("rowRepeater");
        this.add(rowRepeater);

        for (final FormRow uiRow : _formelementmodel.getRowModels()) {
            final RowPanel row = new RowPanel(rowRepeater.newChildId(), new UIModel<FormRow>(uiRow), uiForm, _page,
                                              this, _form, _formelementmodel);
            rowRepeater.add(row);
        }

        final RepeatingView hiddenRepeater = new RepeatingView("hiddenRepeater");
        this.add(hiddenRepeater);
        for (final UIHiddenCell cell : uiForm.getHiddenCells()) {
            if (!cell.isAdded()) {
                hiddenRepeater.add(new LabelComponent(hiddenRepeater.newChildId(), cell.getCellValue()));
                cell.setAdded(true);
            }
        }
        final Page callPage = ((AbstractContentPage)_page).getCalledByPageReference().getPage();
        boolean menuUpdate = true;
        if (callPage instanceof MainPage) {
            menuUpdate = false;
        }
        final List<EmbeddedLink> links = EFapsSession.get().getEmbededLinks();
        for (final EmbeddedLink link : links) {
            if (menuUpdate) {
                hiddenRepeater.add(new LinkElementComponent(hiddenRepeater.newChildId(), link));
            } else {
                hiddenRepeater.add(new LinkElementLink(hiddenRepeater.newChildId(), link));
            }
        }
        EFapsSession.get().getEmbededLinks().clear();
    }

    /**
     * Render to the web response the eFapsContentReference.
     *
     * @param _response Response object
     */@Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(FormPanel.CSS));
    }

    /**
     * This is the getter method for the instance variable {@link #requiredComponents}.
     *
     * @return value of instance variable {@link #requiredComponents}
     */

    public Map<String, Label> getRequiredComponents()
    {
        return this.requiredComponents;
    }

    /**
     * Add a required component.
     *
     * @param _name Name of the component
     * @param _label label of the component
     */
    public void addRequiredComponent(final String _name,
                                     final Label _label)
    {
        this.requiredComponents.put(_name, _label);
    }
}
