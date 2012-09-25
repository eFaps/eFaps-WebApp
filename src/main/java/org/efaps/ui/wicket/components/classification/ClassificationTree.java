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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.ui.wicket.components.classification;

import java.util.Properties;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.content.CheckedFolder;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.HumanTheme;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.WindowsTheme;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.admin.datamodel.Type;
import org.efaps.ui.wicket.models.objects.UIClassification;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.ui.wicket.util.Configuration.ConfigAttribute;

/**
 * Renders the tree for selecting a clqssification.
 *
 * @author The eFaps Team
 * @version $Id: ClassificationTree.java 7534 2012-05-19 09:32:04Z
 *          jan@moxter.net $
 */
public class ClassificationTree
    extends NestedTree<UIClassification>
{

    /**
     * /** Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId wicketId of this component
     * @param _model model for this component
     */
    public ClassificationTree(final String _wicketId,
                              final IModel<UIClassification> _model)
    {
        super(_wicketId, new ClassificationTreeProvider(_model));
        setOutputMarkupId(true);
        if ("human".equals(Configuration.getAttribute(ConfigAttribute.CLASSTREE_CLASS))) {
            add(new HumanTheme());
        } else if ("windows".equals(Configuration.getAttribute(ConfigAttribute.CLASSTREE_CLASS))) {
            add(new WindowsTheme());
        }

        final Properties properties = Configuration.getAttributeAsProperties(ConfigAttribute.CLASSTREE_EXPAND);

        final String expand = properties.getProperty(Type.get(_model.getObject().getClassificationUUID()).getName(),
                        "true");
        if ("true".equalsIgnoreCase(expand)) {
            addAll(getProvider().getRoots().next());
        }
    }

    private void addAll(final UIClassification _uiClass)
    {
        getModelObject().add(_uiClass);
        for (final UIClassification child : _uiClass.getChildren()) {
            addAll(child);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.apache.wicket.extensions.markup.html.repeater.tree.AbstractTree#
     * newContentComponent(java.lang.String, org.apache.wicket.model.IModel)
     */
    @Override
    protected Component newContentComponent(final String _id,
                                            final IModel<UIClassification> _model)
    {
        return new CheckedFolder<UIClassification>(_id, this, _model)
        {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(final AjaxRequestTarget _target)
            {
                final boolean selected = getModelObject().isSelected();

                if (!getModelObject().isMultipleSelect() && !selected) {
                    // select event ensure that there are no other objects selected by cleaning it totally
                    UIClassification uiClass = getModelObject();
                    while (!uiClass.isRoot()) {
                        uiClass = uiClass.getParent();
                    }
                    toggleDown(uiClass, false);
                }

                // unselect event ensure that no children are selected
                if (selected) {
                    toggleDown(getModelObject(), false);
                } else {
                    toggleUp(getModelObject(), true);
                }
                _target.add(ClassificationTree.this);
                findParent(ClassificationTreePanel.class).modelChanged();
            }

            private void toggleUp(final UIClassification _uiClass,
                                  final boolean _selected)
            {
                UIClassification uiClass = _uiClass;
                uiClass.setSelected(_selected);
                while (!uiClass.isRoot()) {
                    uiClass = uiClass.getParent();
                    uiClass.setSelected(_selected);
                }
            }

            private void toggleDown(final UIClassification _uiClass,
                                    final boolean _selected)
            {
                _uiClass.setSelected(_selected);
                for (final UIClassification child : _uiClass.getChildren()) {
                    toggleDown(child, _selected);
                }
            }

            @Override
            protected IModel<Boolean> newCheckBoxModel(final IModel<UIClassification> _model)
            {
                return Model.of(_model.getObject().isSelected());
            }
        };
    }
}
