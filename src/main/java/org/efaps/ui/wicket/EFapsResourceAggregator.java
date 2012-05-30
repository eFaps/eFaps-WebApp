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

package org.efaps.ui.wicket;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.DecoratingHeaderResponse;
import org.efaps.ui.wicket.behaviors.dojo.OnDojoReadyHeaderItem;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class EFapsResourceAggregator
    extends DecoratingHeaderResponse
{
    private final List<OnDojoReadyHeaderItem> dojoReadyItemsToBeRendered = new ArrayList<OnDojoReadyHeaderItem>();

    /**
     * @param _real
     */
    public EFapsResourceAggregator(final IHeaderResponse _real)
    {
        super(_real);
    }

    @Override
    public void render(final HeaderItem _item)
    {
         if (_item instanceof OnDojoReadyHeaderItem) {
            this.dojoReadyItemsToBeRendered.add((OnDojoReadyHeaderItem) _item);
        } else {
            getRealResponse().render(_item);
        }
    }
    /* (non-Javadoc)
     * @see org.apache.wicket.markup.html.DecoratingHeaderResponse#close()
     */
    @Override
    public void close()
    {
        renderCombinedEventScripts();
        super.close();
    }


    /**
     * Combines all DOM ready and onLoad scripts and renders them as 2 script
     * tags.
     */
    private void renderCombinedEventScripts()
    {
        final StringBuilder combinedScript = new StringBuilder();

        for (final OnDojoReadyHeaderItem curItem : this.dojoReadyItemsToBeRendered) {
            combinedScript.append("\n");
            combinedScript.append(curItem.getJavaScript());
            combinedScript.append(";");
        }

        if (combinedScript.length() > 0) {
            getRealResponse().render(OnDojoReadyHeaderItem.forScript(combinedScript.append("\n").toString()));
        }
    }
}
