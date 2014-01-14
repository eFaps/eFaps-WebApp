/*
 * Copyright 2003 - 2014 The eFaps Team
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

package org.efaps.ui.wicket.components.button;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.efaps.ui.wicket.components.efapscontent.StaticImageComponent;
import org.efaps.ui.wicket.resources.EFapsContentReference;

/**
 * @author The eFaps Team
 * @version $Id$
 */
public class Button
    extends Panel
{

   /**
     * Wicket id that must be used for the link component.
     */
    public static final String LINKID = "buttonLink";

    /**
     * Reference to an icon in the eFaps Database.
     */
    public enum ICON {
        /** accept.png. */
        ACCEPT("accept.png"),
        /** add.png. */
        ADD("add.png"),
        /** cancel.png. */
        CANCEL("cancel.png"),
        /** delete.png. */
        DELETE("delete.png"),
        /** next.png. */
        NEXT("next.png"),
        /** previous. */
        PREVIOUS("previous.png");

        /**
         * reference.
         */
        private final EFapsContentReference reference;

        /**
         * @param _image image
         */
        private ICON(final String _image)
        {
            this.reference = new EFapsContentReference(Button.class, _image);
        }

        /**
         * Getter method for the instance variable {@link #reference}.
         *
         * @return value of instance variable {@link #reference}
         */
        public EFapsContentReference getReference()
        {
            return this.reference;
        }
    }

    /**
     * Needed foer serialization.
     */
    private static final long serialVersionUID = 1L;


    private final ButtonImage imagediv = new ButtonImage("icon");

    public Button(final String _wicketId,
                  final WebMarkupContainer _link,
                  final String _label)
    {
        this(_wicketId, _link, _label, null);
    }

    public Button(final String _wicketId,
                  final WebMarkupContainer _link,
                  final String _label,
                  final EFapsContentReference _icon)
    {
        super(_wicketId);
        this.add(_link);
        _link.add(new ButtonStyleBehavior());
        final Label buttonlabel = new Label("buttonLabel", _label);
        buttonlabel.add(new ButtonStyleBehavior());
        _link.add(buttonlabel);

        _link.add(this.imagediv);

        if (_icon != null) {
            this.imagediv.setReference(_icon);
        }
    }

    public String getLinkWicketId()
    {
        return Button.LINKID;
    }

    public void setIconReference(final EFapsContentReference _icon)
    {
        this.imagediv.setReference(_icon);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.wicket.Component#onBeforeRender()
     */
    @Override
    protected void onBeforeRender()
    {
        if (!this.imagediv.hasReference()) {
            this.imagediv.setVisible(false);
        }
        super.onBeforeRender();
    }

    /**
     * Image for the Button.
     */
    public static class ButtonImage
        extends StaticImageComponent
    {

        /**
         * Needed for serialization.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Has a reference or not.
         */
        private boolean reference = false;

        /**
         * @param _wicketId wicketid for this component
         */
        public ButtonImage(final String _wicketId)
        {
            super(_wicketId);
        }

        /**
         * @return true if has reference, else false
         */
        public boolean hasReference()
        {
            return this.reference;
        }

        @Override
        public void setReference(final EFapsContentReference _reference)
        {
            super.setReference(_reference);
            this.reference = true;
        }

        @Override
        protected void onComponentTag(final ComponentTag _tag)
        {
            _tag.put("style", "background-repeat: no-repeat; "
                            + "background-position: left top; "
                            + "background-image:url(" + super.getUrl() + ")");
        }
    }
}
