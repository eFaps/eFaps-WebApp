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

package org.efaps.ui.wicket.behaviors;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.behavior.IBehaviorListener;
import org.efaps.ui.wicket.EFapsSession;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ShowFileCallBackBehavior
    extends Behavior
    implements IBehaviorListener
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The component the bahavior is binded to.
     */
    private Component component;

    /**
     * @see org.apache.wicket.behavior.IBehaviorListener#onRequest()
     */
    public void onRequest()
    {
        ((EFapsSession) Session.get()).getFile();
    }

    /**
     * @see org.apache.wicket.behavior.AbstractBehavior#bind(org.apache.wicket.Component)
     * @param _component Component to bind to
     */
    @Override
    public void bind(final Component _component)
    {
        super.bind(_component);
        this.component = _component;
    }

    /**
     * @return the callback script
     */
    public String getCallbackScript()
    {
        if (getComponent() == null) {
            throw new IllegalArgumentException("Behavior must be bound to a component to create the URL");
        }
        final StringBuilder script = new StringBuilder();
        script.append("top.frames[\"eFapsFrameHidden\"].location.href=\"")
                        .append(getComponent().urlFor(this, IBehaviorListener.INTERFACE, null)).append("\"");
        return script.toString();
    }

    /**
     * @return the binded component
     */
    protected Component getComponent()
    {
        return this.component;
    }
}
