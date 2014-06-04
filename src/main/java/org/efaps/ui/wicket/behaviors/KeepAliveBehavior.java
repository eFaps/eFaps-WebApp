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


package org.efaps.ui.wicket.behaviors;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnLoadHeaderItem;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.ui.wicket.util.Configuration.ConfigAttribute;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class KeepAliveBehavior
    extends Behavior
{
    /**
     * Message send to keep websocket alive.
     */
    public static final String MSG = "keepAlive";

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    @Override
    public void renderHead(final Component _component,
                           final IHeaderResponse _response)
    {
        if (Configuration.getAttributeAsBoolean(ConfigAttribute.WEBSOCKET_ACTVATE)) {
            super.renderHead(_component, _response);
            _response.render(OnLoadHeaderItem.forScript(
                            "top.Wicket.WebSocket.send(\"" + KeepAliveBehavior.MSG + "\")"));
        }
    }
}
