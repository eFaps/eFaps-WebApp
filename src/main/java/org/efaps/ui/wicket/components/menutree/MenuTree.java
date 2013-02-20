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

package org.efaps.ui.wicket.components.menutree;

import java.util.UUID;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.theme.HumanTheme;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.iterator.ComponentHierarchyIterator;
import org.efaps.ui.wicket.behaviors.update.IRemoteUpdateListener;
import org.efaps.ui.wicket.behaviors.update.IRemoteUpdateable;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author The eFaps Team
 * @version $Id$
 */
public class MenuTree
    extends NestedTree<UIMenuItem>
    implements IRemoteUpdateable
{

    /**
     * Reference to icon for go into button.
     */
    public static final EFapsContentReference ICON_GOINTO = new EFapsContentReference(MenuTree.class, "GoInto.gif");

    /**
     * Reference to icon for go up button.
     */
    public static final EFapsContentReference ICON_GOUP = new EFapsContentReference(MenuTree.class, "GoUp.gif");

    /**
     * Reference to icon for closed child button.
     */
    public static final EFapsContentReference ICON_CHILDCLOSED = new EFapsContentReference(MenuTree.class,
                    "ChildClosed.gif");

    /**
     * Reference to icon for open child button.
     */
    public static final EFapsContentReference ICON_CHILDOPENED = new EFapsContentReference(MenuTree.class,
                    "ChildOpened.gif");

    /**
     * Reference to style sheet for the menutree.
     */
    public static final EFapsContentReference CSS = new EFapsContentReference(MenuTree.class, "MenuTree.css");

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(MenuTree.class);

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Selected Component.
     */
    private Component selected = null;

    /**
     * Constructor used for a new MenuTree.
     *
     * @param _wicketId wicket id of the component
     * @param _commandUUID uuid of the command
     * @param _oid oid
     * @param _selectCmdUUID UUID of the selected Command
     */
    public MenuTree(final String _wicketId,
                    final UUID _commandUUID,
                    final String _oid,
                    final UUID _selectCmdUUID)
        throws CacheReloadException
    {
        super(_wicketId, new TreeMenuModel(_commandUUID, _oid));

        add(new HumanTheme());
        add(AttributeModifier.append("class", "eFapsTreeMenu"));
        add(new MenuUpdateBehavior());

        setDefault(_selectCmdUUID);
    }

    /**
     * Render to the web response the eFapsContentReference.
     *
     * @param _response Response object
     */
    @Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(MenuTree.CSS));
    }

    /**
     * Set the default selected item.
     *
     * @param _selectCmdUUID UUID of the selected Command
     */
    public void setDefault(final UUID _selectCmdUUID)
    {
        final UIMenuItem menuItem = getProvider().getRoots().next();
        menuItem.setHeader(true);
        boolean hasDefault = false;
        for (final UIMenuItem childItem : menuItem.getChildren()) {
            if ((_selectCmdUUID == null && childItem.isDefaultSelected())
                            || (_selectCmdUUID != null && _selectCmdUUID.equals(childItem.getCommandUUID()))) {
                hasDefault = true;
                childItem.setSelected(true);
            }
        }
        if (!hasDefault) {
            menuItem.setSelected(true);
        }
        expand(menuItem);
    }

    /*
     * (non-Javadoc)
     * @see org.apache.wicket.extensions.markup.html.repeater.tree.AbstractTree#
     * newContentComponent(java.lang.String, org.apache.wicket.model.IModel)
     */
    @Override
    protected Component newContentComponent(final String _id,
                                            final IModel<UIMenuItem> _model)
    {
        final MenuItem menuitem = new MenuItem(_id, this, _model);
        if (_model.getObject().isSelected()) {
            this.selected = menuitem;
        }
        return menuitem;
    }

    @Override
    public Component newNodeComponent(final String _wicketId,
                                      final IModel<UIMenuItem> _model)
    {
        return new AbstractMenuNode(_wicketId, this, _model)
        {

            private static final long serialVersionUID = 1L;

            @Override
            protected Component createContent(final String _id,
                                              final IModel<UIMenuItem> _model)
            {
                return newContentComponent(_id, _model);
            }
        };
    }

    /**
     * Getter method for the instance variable {@link #selected}.
     *
     * @return value of instance variable {@link #selected}
     */
    public Component getSelected()
    {
        return this.selected;
    }

    /**
     * Setter method for instance variable {@link #selected}.
     *
     * @param _selected value for instance variable {@link #selected}
     */

    public void setSelected(final Component _selected)
    {
        this.selected = _selected;
    }

    /**
     * @param _commandUUID UUID of the command
     * @param _instanceKey instance key
     * @param _target the ajax target to use
     */
    public void addChildMenu(final UUID _commandUUID,
                             final String _instanceKey,
                             final AjaxRequestTarget _target)
        throws CacheReloadException
    {
        final UIMenuItem menuItem = (UIMenuItem) getSelected().getDefaultModelObject();
        boolean old = false;
        for (final UIMenuItem child : menuItem.getChildren()) {
            if (child.getInstanceKey().equals(_instanceKey)
                            && child.getCommandUUID().equals(_commandUUID)) {
                old = true;
                child.setSelected(true);
                menuItem.setSelected(false);
                final ComponentHierarchyIterator iterator = visitChildren(MenuItem.class);
                while (iterator.hasNext()) {
                    final Component comp = iterator.next();
                    final Object object = comp.getDefaultModelObject();
                    if (object != null && object instanceof UIMenuItem) {
                        if (((UIMenuItem) object).isSelected()) {
                            _target.add(getSelected());
                            setSelected(comp);
                            _target.add(getSelected());
                            break;
                        }
                    }
                }
                break;
            }
        }

        if (!old) {
            final UIMenuItem newMenuItem = new UIMenuItem(_commandUUID, _instanceKey);
            newMenuItem.setSelected(true);
            newMenuItem.setAncestor(menuItem);
            menuItem.getChildren().add(newMenuItem);
            expand(menuItem);
            expand(newMenuItem);
            menuItem.setSelected(false);
            _target.add(getSelected());
        }
    }

    /**
     * @param _menuItem child to be removed
     * @param _target Ajaxtarget
     */
    public void removeChild(final UIMenuItem _menuItem,
                            final AjaxRequestTarget _target)
    {
        final UIMenuItem selectedItem = (UIMenuItem) getSelected().getDefaultModelObject();

        final UIMenuItem ancestor = _menuItem.getAncestor();
        boolean nested = false;
        if (selectedItem.isChild(_menuItem) || selectedItem.equals(_menuItem)) {
            nested = true;
        }
        ancestor.getChildren().remove(_menuItem);
        if (nested) {
            expand(ancestor);
            ancestor.setSelected(true);
            final ComponentHierarchyIterator visitor = visitChildren(MenuItem.class);
            while (visitor.hasNext()) {
                final Component component = visitor.next();
                if (component.getDefaultModelObject().equals(ancestor)) {
                    setSelected(component);
                    break;
                }
            }
        } else {
            expand(ancestor);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerListener(final IRemoteUpdateListener _listener)
    {
        final MenuUpdateBehavior behavior = getBehaviors(MenuUpdateBehavior.class).get(0);
        behavior.register(_listener);
    }
}
