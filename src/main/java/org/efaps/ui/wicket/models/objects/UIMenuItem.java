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

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.apache.wicket.RestartResponseException;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.AbstractMenu;
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
    private DefaultMutableTreeNode ancestor;

    /**
     * in the case that the MenuItem is used for a submit, setting this
     *  to true opens a Dialog to ask the user
     * "do you really want to..?".
     */
    private boolean askUser = false;

    /**
     * All childs of this menu item.
     */
    private final List<UIMenuItem> childs = new ArrayList<UIMenuItem>();

    /**
     * this instance variable stores in the case that this MenuItem is
     * part of a {@link #org.efaps.ui.wicket.components.menutree.MenuTree}
     *  if it is selected by default and therefore the Form or Tabel
     *  connected to this MenuItem must be opened.
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
     * @see #childs
     * @see #getChilds()
     * @return true if this MenuItem has childs, else false
     */
    public boolean hasChilds()
    {
        return !this.childs.isEmpty();
    }

    /**
     * This is the getter method for the instance variable {@link #childs}.
     *
     * @return value of instance variable {@link #childs}
     */
    public List<UIMenuItem> getChilds()
    {
        return this.childs;
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
                        this.childs.add(new UIMenuItem(subCmd.getUUID(), getInstanceKey()));
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
     */
    public void setHeader(final boolean _header)
    {
        this.header = _header;
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
     * get a TreeModel which used in the Components to construct the actuall tree.
     *
     * @see #getNode()
     * @return TreeModel of this MenuItemModel including the ChildNodes
     */
    public TreeModel getTreeModel()
    {
        return new DefaultTreeModel(getNode());
    }

    /**
     * get a Node of this MenuItemModel including the Childs.
     *
     * @see #addNode(DefaultMutableTreeNode, List)
     * @return DefaultMutableTreeNode of this MenuItemModel including the ChildNodes
     */
    public DefaultMutableTreeNode getNode()
    {
        final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(this);
        setHeader(true);
        addNode(rootNode, this.childs);
        return rootNode;
    }

    /**
     * recursive method used to fill the TreeModel.
     *
     * @see #getTreeModel()
     * @param _parent ParentNode children schould be added
     * @param _childs List<StructurBrowserModel>to be added as childs
     */
    private void addNode(final DefaultMutableTreeNode _parent,
                         final List<UIMenuItem> _childs)
    {
        for (int i = 0; i < _childs.size(); i++) {
            final DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(_childs.get(i));
            _parent.add(childNode);
            if (_childs.get(i).hasChilds()) {
                addNode(childNode, _childs.get(i).childs);
            }
        }
    }

    /**
     * This is the getter method for the instance variable {@link #ancestor}.
     *
     * @return value of instance variable {@link #ancestor}
     */
    public DefaultMutableTreeNode getAncestor()
    {
        return this.ancestor;
    }

    /**
     * This is the setter method for the instance variable {@link #ancestor}.
     *
     * @param _node the ancestor to set
     */
    public void setAncestor(final DefaultMutableTreeNode _node)
    {
        this.ancestor = _node;
    }

    /**
     * @see org.efaps.ui.wicket.models.objects.AbstractUIObject#execute()
     */
    @Override
    public void execute()
    {
        // TODO Auto-generated method stub
    }

}
