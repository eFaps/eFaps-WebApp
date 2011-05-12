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

package org.efaps.ui.wicket.components.form.set;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.efaps.ui.wicket.components.efapscontent.StaticImageComponent;
import org.efaps.ui.wicket.models.cell.UIFormCell;
import org.efaps.ui.wicket.models.cell.UIFormCellSet;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.util.EFapsException;

/**
 * Class is responsible for rendering the y-coordinates for an AttributeSet.
 *
 * @author The eFasp Team
 * @version $Id$
 */
public class YPanel
    extends Panel
{

    /**
     * Content reference for the add icon.
     */
    public static final EFapsContentReference ICON_ADD = new EFapsContentReference(YPanel.class, "add.png");

    /**
     * Content reference for the delete icon.
     */
    public static final EFapsContentReference ICON_DELETE = new EFapsContentReference(YPanel.class, "delete.png");

    /**
     * Static variable used as the class name for the table.
     */
    public static final String STYLE_CLASS = "eFapsFieldSet";

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId wicketId for the component
     * @param _model model for the component
     * @param _formmodel FormModel
     * @throws EFapsException on error
     */
    public YPanel(final String _wicketId,
                  final IModel<UIFormCellSet> _model,
                  final UIForm _formmodel)
        throws EFapsException
    {
        super(_wicketId, _model);
        final UIFormCellSet set = (UIFormCellSet) super.getDefaultModelObject();
        setOutputMarkupId(true);

        final RepeatingView repeater = new RepeatingView("yRepeater", _model);
        add(repeater);
        // only in edit mode we need visible components
        if (set.isEditMode()) {
            final AjaxAddNew addNew = new AjaxAddNew("addNew", _model, repeater, _formmodel);
            add(addNew);
            final StaticImageComponent image = new StaticImageComponent("add");
            image.setReference(YPanel.ICON_ADD);
            addNew.add(image);
        } else {
            final Component invisible = new WebMarkupContainer("addNew").setVisible(false);
            add(invisible);
        }

        for (final Entry<Integer, Map<Integer, UIFormCell>> entry : set.getYX2value().entrySet()) {
            repeater.add(new ValuePanel(repeater.newChildId(), set, entry.getKey(), entry.getValue(), _formmodel));
        }
    }
}
