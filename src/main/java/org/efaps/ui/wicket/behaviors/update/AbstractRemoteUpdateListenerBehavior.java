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

package org.efaps.ui.wicket.behaviors.update;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class AbstractRemoteUpdateListenerBehavior
    extends Behavior
    implements IRemoteUpdateListener
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Random key to uniquely identify this listener.
     */
    private final String key = RandomStringUtils.randomAlphanumeric(8);

    /**
     * Component this behavior belongs to.
     */
    private final Component component;

    /**
     * @param _component    Component this behavior belongs to
     */
    public AbstractRemoteUpdateListenerBehavior(final Component _component)
    {
        this.component = _component;
    }

    /**
     * Getter method for the instance variable {@link #component}.
     *
     * @return value of instance variable {@link #component}
     */
    public Component getComponent()
    {
        return this.component;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey()
    {
        return this.key;
    }
}
