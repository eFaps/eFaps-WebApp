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

package org.efaps.ui.wicket.models.objects;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.wicket.RestartResponseException;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.AbstractMenu;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.admin.ui.Image;
import org.efaps.beans.ValueList;
import org.efaps.beans.valueparser.ParseException;
import org.efaps.beans.valueparser.ValueParser;
import org.efaps.db.PrintQuery;
import org.efaps.ui.wicket.components.modalwindow.ICmdUIObject;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.util.EFapsException;

/**
 * This class provides the Model for rendering MenuComponents in
 * {@link #org.efaps.ui.wicket.components.menu.MenuPanel} and
 * in {@link #org.efaps.ui.wicket.components.menutree.MenuTree}.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class UIMenuItem
    extends AbstractUIObject
    implements ICmdUIObject
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 505704924081527139L;

    /**
     * this instance variable stores in the case that this MenuItem
     * is part of a {@link #org.efaps.ui.wicket.components.menutree.MenuTree}
     * if it was steped into the
     * ancestor of this menuitem.
     *
     * @see #ancestor
     */
    private UIMenuItem ancestor;

    /**
     * in the case that the MenuItem is used for a submit, setting this
     *  to true opens a Dialog to ask the user
     * "do you really want to..?".
     */
    private boolean askUser = false;

    /**
     * All childs of this menu item.
     */
    private final List<UIMenuItem> children = new ArrayList<UIMenuItem>();

    /**
     * this instance variable stores in the case that this MenuItem is
     * part of a {@link #org.efaps.ui.wicket.components.menutree.MenuTree}
     * if it is selected by default and therefore the Form or Table
     * connected to this MenuItem must be opened.
     */
    private boolean defaultSelected = false;

    /** Description of this menu item. */
    private String description;

    /**
     * this instance variable stores in the case that this MenuItem is
     * part of a {@link #org.efaps.ui.wicket.components.menutree.MenuTree}
     * if it is a header. This is
     * needed beacuse the headers are displayed in a differen style.
     */
    private boolean header = false;

    /** Url to the image of this menu item. */
    private String image;

    /** Label of this menu item. */
    private String label;

    /** Reference of this menu item. */
    private String reference;

    /**
     * this instance variable stores in the case that this MenuItem is part
     * of a {@link #org.efaps.ui.wicket.components.menutree.MenuTree} if
     * it was steped into this MenuItem.
     *
     * @see #ancestor
     */
    private boolean stepInto;

    /** Url of this menu item. */
    private String url;

    /** height of the window which will be opened. */
    private int windowHeight;

    /** width of the window which will be opened. */
    private int windowWidth;

    /**
     * Number of rows that must be selected.
     */
    private int submitSelectedRows;

    /**
     * This menuitem is selected.
     */
    private boolean selected = false;

    /**
     * The parent for this item.
     */
    private UIMenuItem parent;


    /**
     * Constructor setting the UUID of this MenuItem.
     *
     * @param _uuid UUID
     */
    public UIMenuItem(final UUID _uuid)
    {
        this(_uuid, null);
    }

    /**
     * Constructor setting the UUID and the key for the instance of this MenuItem.
     *
     * @param _uuid UUID
     * @param _instanceKey instance Key
     */
    public UIMenuItem(final UUID _uuid,
                      final String _instanceKey)
    {
        super(_uuid, _instanceKey);
        initialize();
    }

    /**
     * this method returns, if this MenuItem has childs.
     *
     * @see #children
     * @see #getChildren()
     * @return true if this MenuItem has childs, else false
     */
    public boolean hasChildren()
    {
        return !this.children.isEmpty();
    }

    /**
     * This is the getter method for the instance variable {@link #children}.
     *
     * @return value of instance variable {@link #children}
     */
    public List<UIMenuItem> getChildren()
    {
        return this.children;
    }

    /**
     * This is the getter method for the instance variable {@link #description}.
     *
     * @return value of instance variable {@link #description}
     */
    public String getDescription()
    {
        return this.description;
    }

    /**
     * This is the getter method for the instance variable {@link #image}.
     *
     * @return value of instance variable {@link #image}
     */
    public String getImage()
    {
        return this.image;
    }

    /**
     * This is the getter method for the instance variable {@link #label}.
     *
     * @return value of instance variable {@link #label}
     */
    public String getLabel()
    {
        return this.label;
    }

    /**
     * This is the getter method for the instance variable {@link #reference}.
     *
     * @return value of instance variable {@link #reference}
     */
    public String getReference()
    {
        return this.reference;
    }

    /**
     * Getter method for the instance variable {@link #submitSelectedRows}.
     *
     * @return value of instance variable {@link #submitSelectedRows}
     */
    public int getSubmitSelectedRows()
    {
        return this.submitSelectedRows;
    }

    /**
     * This method returns the URL to the Image of this MenuItem.
     *
     * @return URL of the Image
     * @throws EFapsException on error
     */
    public String getTypeImage()
        throws EFapsException
    {
        String ret = null;
        if (getInstance() != null) {
            final Image imageTmp = Image.getTypeIcon(getInstance().getType());
            if (imageTmp != null) {
                ret = imageTmp.getUrl();
            }
        }
        return ret;
    }

    /**
     * This is the getter method for the instance variable {@link #url}.
     *
     * @return value of instance variable {@link #url}
     */

    public String getUrl()
    {
        return this.url;
    }

    /**
     * This is the setter method for the instance variable {@link #url}.
     *
     * @param _url the url to set
     */
    public void setURL(final String _url)
    {
        this.url = _url;
    }

    /**
     * This is the getter method for the instance variable {@link #windowHeight} .
     *
     * @return value of instance variable {@link #windowHeight}
     */

    public int getWindowHeight()
    {
        return this.windowHeight;
    }

    /**
     * This is the getter method for the instance variable {@link #windowWidth}.
     *
     * @return value of instance variable {@link #windowWidth}
     */

    public int getWindowWidth()
    {
        return this.windowWidth;
    }

    /**
     * this method initializes this MenuItem.
     */
    private void initialize()
    {
        final AbstractCommand command = super.getCommand();
        this.image = command.getIcon();
        this.reference = command.getReference();
        this.askUser = command.isAskUser();
        this.windowHeight = command.getWindowHeight();
        this.windowWidth = command.getWindowWidth();
        this.defaultSelected = command.isDefaultSelected();
        this.description = "";
        this.submitSelectedRows = command.getSubmitSelectedRows();
        requeryLabel();
        try {
            if (command instanceof AbstractMenu) {
                for (final AbstractCommand subCmd : ((AbstractMenu) command).getCommands()) {
                    if (subCmd != null && subCmd.hasAccess(getMode(), getInstance())) {
                        if (subCmd.getTargetMode().equals(TargetMode.SEARCH)) {
                            final UISearchItem child = new UISearchItem(subCmd.getUUID(), getInstanceKey());
                            child.setParent(this);
                            this.children.add(child);
                        } else {
                            final UIMenuItem child = new UIMenuItem(subCmd.getUUID(), getInstanceKey());
                            child.setParent(this);
                            this.children.add(child);
                        }
                    }
                }
            }
        } catch (final EFapsException e) {
            throw new RestartResponseException(new ErrorPage(e));
        }
    }

    /**
     * Requery the Label.
     */
    public void requeryLabel()
    {
        try {
            this.label = DBProperties.getProperty(getCommand().getLabel());
            if (getInstance() != null) {
                final ValueParser parser = new ValueParser(new StringReader(this.label));
                final ValueList list = parser.ExpressionString();
                if (list.getExpressions().size() > 0) {
                    final PrintQuery print = new PrintQuery(getInstance());
                    list.makeSelect(print);
                    if (print.execute()) {
                        this.label = list.makeString(getInstance(), print, getMode());
                    }
                }
            }
        } catch (final EFapsException e) {
            throw new RestartResponseException(new ErrorPage(e));
        } catch (final ParseException e) {
            throw new RestartResponseException(new ErrorPage(e));
        }
    }

    /**
     * This is the getter method for the instance variable {@link #askUser}.
     *
     * @return value of instance variable {@link #askUser}
     */

    public boolean isAskUser()
    {
        return this.askUser;
    }

    /**
     * This is the getter method for the instance variable {@link #defaultSelected}.
     *
     * @return value of instance variable {@link #defaultSelected}
     */
    public boolean isDefaultSelected()
    {
        return this.defaultSelected;
    }

    /**
     * This is the getter method for the instance variable {@link #header}.
     *
     * @return value of instance variable {@link #header}
     */

    public boolean isHeader()
    {
        return this.header;
    }

    /**
     * This is the setter method for the instance variable {@link #header}.
     *
     * @param _header the header to set
     * @return this Menuitem for chaining
     */
    public UIMenuItem setHeader(final boolean _header)
    {
        this.header = _header;
        return this;
    }

    /**
     * This is the getter method for the instance variable {@link #stepInto}.
     *
     * @return value of instance variable {@link #stepInto}
     */
    public boolean isStepInto()
    {
        return this.stepInto;
    }

    /**
     * This is the setter method for the instance variable {@link #stepInto}.
     *
     * @param _stepInto the stepInto to set
     */
    public void setStepInto(final boolean _stepInto)
    {
        this.stepInto = _stepInto;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetModel()
    {
        // not needed here
    }

    /**
     * This is the getter method for the instance variable {@link #ancestor}.
     *
     * @return value of instance variable {@link #ancestor}
     */
    public UIMenuItem getAncestor()
    {
        return this.ancestor;
    }

    /**
     * This is the setter method for the instance variable {@link #ancestor}.
     *
     * @param _ancestor the ancestor to set
     */
    public void setAncestor(final UIMenuItem _ancestor)
    {
        this.ancestor = _ancestor;
    }

    /**
     * @see org.efaps.ui.wicket.models.objects.AbstractUIObject#execute()
     */
    @Override
    public void execute()
    {
        // TODO Auto-generated method stub
    }

    /**
     * Getter method for the instance variable {@link #selected}.
     *
     * @return value of instance variable {@link #selected}
     */
    public boolean isSelected()
    {
        return this.selected;
    }

    /**
     * Setter method for instance variable {@link #selected}.
     *
     * @param _selected value for instance variable {@link #selected}
     */

    public void setSelected(final boolean _selected)
    {
        this.selected = _selected;
    }


    /**
     * Getter method for the instance variable {@link #parent}.
     *
     * @return value of instance variable {@link #parent}
     */
    public UIMenuItem getParent()
    {
        return this.parent;
    }

    /**
     * Setter method for instance variable {@link #parent}.
     *
     * @param _parent value for instance variable {@link #parent}
     */

    public void setParent(final UIMenuItem _parent)
    {
        this.parent = _parent;
    }

    /**
     * @param _parentItem parentitem to check
     * @return true if this UIMenuItem is child/grandchild of the given UIMenuItem
     */
    public boolean isChild(final UIMenuItem _parentItem)
    {
        return isChild(this, _parentItem);
    }

    /**
     * Is the Childitem a child/grandchild of the Parentitem.
     * @param _childItem    ChildItem
     * @param _parentItem   ParentItem
     * @return true if child or grandchild etc.
     */
    public boolean isChild(final UIMenuItem _childItem,
                           final UIMenuItem _parentItem)
    {
        boolean ret = _parentItem.getChildren().contains(_childItem);
        if (!ret) {
            for (final UIMenuItem child : _parentItem.getChildren()) {
                if (child.hasChildren()) {
                    ret = isChild(_childItem, child);
                }
                if (ret) {
                    break;
                }
            }
        }
        return ret;
    }
}
