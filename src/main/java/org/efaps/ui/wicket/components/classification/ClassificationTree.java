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

import java.util.Iterator;
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
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Renders the tree for selecting a clqssification.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ClassificationTree
    extends NestedTree<UIClassification>
{

    /**
     * /** Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ClassificationTree.class);

    /**
     * @param _wicketId wicketId of this component
     * @param _model model for this component
     * @throws CacheReloadException on error
     */
    public ClassificationTree(final String _wicketId,
                              final IModel<UIClassification> _model)
        throws CacheReloadException
    {
        super(_wicketId, new ClassificationTreeProvider(_model));
        setOutputMarkupId(true);
        if ("human".equals(Configuration.getAttribute(ConfigAttribute.CLASSTREE_CLASS))) {
            add(new HumanTheme());
        } else if ("windows".equals(Configuration.getAttribute(ConfigAttribute.CLASSTREE_CLASS))) {
            add(new WindowsTheme());
        }

        Properties properties;
        try {
            properties = Configuration.getAttributeAsProperties(ConfigAttribute.CLASSTREE_EXPAND);
        } catch (final EFapsException e) {
            ClassificationTree.LOG.error("cannot read Properties from Configuration.");
            properties = new Properties();
        }
        final Iterator<? extends UIClassification> iter = getProvider().getRoots();
        while (iter.hasNext()) {
            final UIClassification clazz = iter.next();
            final String expand = properties.getProperty(Type.get(clazz.getClassificationUUID()).getName(), "false");
            addChildren(clazz, "true".equalsIgnoreCase(expand));
        }
    }

    /**
     * Recursive method to add child classifications.
     *
     * @param _uiClass  classification to add
     * @param _force    force expanded
     */
    private void addChildren(final UIClassification _uiClass,
                             final boolean _force)
    {
        if (_force || _uiClass.isExpanded()) {
            getModelObject().add(_uiClass);
            for (final UIClassification child : _uiClass.getChildren()) {
                addChildren(child, _force);
            }
        }
    }

    @Override
    public void expand(final UIClassification _uiStrBrws)
    {
        super.expand(_uiStrBrws);
        _uiStrBrws.setExpanded(true);
    }

    /**
     * Collapse the given node, tries to update the affected branch if the
     * change happens on an {@link AjaxRequestTarget}.
     *
     * @param _uiStrBrws    the object to collapse
     */
    @Override
    public void collapse(final UIClassification _uiStrBrws)
    {
        super.collapse(_uiStrBrws);
        _uiStrBrws.setExpanded(false);
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
                final ClassificationTreePanel parent = findParent(ClassificationTreePanel.class);
                // Don't do that if it is a filter
                if (parent != null && !getModelObject().isMultipleSelect() && !selected) {
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
                if (parent != null) {
                    parent.modelChanged();
                }
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
