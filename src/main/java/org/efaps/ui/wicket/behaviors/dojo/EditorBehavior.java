/*
 * Copyright 2003 - 2009 The eFaps Team
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

package org.efaps.ui.wicket.behaviors.dojo;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.ComponentTag;

/**
 * Class renders a dojo editor.This widget provides basic WYSIWYG editing
 * features, based on the browser’s underlying rich text editing capability,
 * accompanied by a toolbar (dijit.Toolbar). A plugin model is available
 * to extend the editor’s capabilities as well as the the options available
 * in the toolbar. Content generation may vary across browsers, and clipboard
 * operations may have different results, to name a few limitations.
 * Note: this widget should not be used with the HTML &lt;TEXTAREA&gt; tag.
 * The default menu from dojo is:
 * UNDO,REDO,SEPERATOR,CUT,COPY,PASTE,SEPERATOR,BOLD,ITALIC,UNDERLINE,
 * STRIKETROUGH,SEPERATOR,ORDEREDLIST,UNORDERLIST,INDENT,OUTENT,
 * SEPERATOR,JUSTIFYLEFT,JUSTIFYRIGHT,JUSTIFYCENTER,JUSTIFYFULL
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class EditorBehavior extends AbstractDojoBehavior
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Enum for the different MenuCommands of a dojo Editor.
     */
    public enum MenuCommands
    {
        /** "undo. */
        UNDO("undo"),
        /** "redo. */
        REDO("redo"),
        CUT("cut"),
        COPY("paste"),
        PASTE("paste"),
        SELECTALL("selectAll"),
        BOLD("bold"),
        ITALIC("italic"),
        UNDERLINE("underline"),
        STRIKETHROUGH("strikethrough"),
        SUBSRCIPT("subscript"),
        SUPERSCRIPT("superscript"),
        REMOVEFORMAT("removeFormat"),
        ORDEREDLIST("insertOrderedList"),
        UNORDERLIST("insertUnorderedList"),
        HR("insertHorizontalRule"),
        INDENT("indent"),
        OUTENT("outdent"),
        JUSTIFYLEFT("justifyLeft"),
        JUSTIFYRIGHT("justifyRight"),
        JUSTIFYCENTER("justifyCenter"),
        JUSTIFYFULL("justifyFull"),
        CREATELINK("createLink"),
        UNLINK("unlink"),
        DELETE("delete"),
        SEPERATOR("|");

        /**
         * Stores the key of the MenuCommands.
         */
        private final String key;

        /**
         * Private Constructor.
         *
         * @param _key Key
         */
        private MenuCommands(final String _key)
        {
            this.key = _key;
        }

        /**
         * Getter method for instance variable {@link #key}.
         *
         * @return value of instance variable {@link #key}
         */
        public String getKey()
        {
            return this.key;
        }
    }

    /**
     * Stores the design of this BorderBehavior.
     */
    private final List<EditorBehavior.MenuCommands> cmds = new ArrayList<EditorBehavior.MenuCommands>();

    /**
     * Constructor.
     *
     * @param _cmds List of cmds to be used in this editor, null if default
     *              from dojo should be used
     */
    public EditorBehavior(final List<EditorBehavior.MenuCommands> _cmds)
    {
        if (_cmds != null) {
            this.cmds.addAll(_cmds);
        }
    }

    /**
     * The tag of the related component must be set, so that a dojo
     * BorderContainer will be rendered.
     *
     * @param _component component this Behavior belongs to
     * @param _tag Tag to write to
     */
    @Override
    public void onComponentTag(final Component _component, final ComponentTag _tag)
    {
        super.onComponentTag(_component, _tag);
        _tag.put("dojoType", "dijit.Editor");
        if (this.cmds.size() > 0) {
            final StringBuilder str = new StringBuilder();
            str.append("[");
            boolean first = true;
            for (final MenuCommands cmd : this.cmds) {
                if (first) {
                    first = false;
                } else {
                    str.append(",");
                }
                str.append("'").append(cmd.key).append("'");
            }
            str.append("]");
            _tag.put("plugins", str.toString());
        }
    }
}
