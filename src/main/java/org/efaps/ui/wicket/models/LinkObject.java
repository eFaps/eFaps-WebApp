/*
 * Copyright 2003 - 2013 The eFaps Team
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


package org.efaps.ui.wicket.models;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class LinkObject
{

    /**
     * The Parameter Key if used for with Jasper Element Handler.
     */
    public static String JASPER_PARAMETERKEY = LinkObject.class + ".Key";

    /**
     * The instance key for the link.
     */
    private String instanceKey;
    /**
     * @param _oid
     */
    public LinkObject(final String _instanceKey)
    {
       this.instanceKey = _instanceKey;
    }

    /**
     * Getter method for the instance variable {@link #instanceKey}.
     *
     * @return value of instance variable {@link #instanceKey}
     */
    protected String getInstanceKey()
    {
        return this.instanceKey;
    }

    /**
     * Setter method for instance variable {@link #instanceKey}.
     *
     * @param _instanceKey value for instance variable {@link #instanceKey}
     */
    protected void setInstanceKey(final String _instanceKey)
    {
        this.instanceKey = _instanceKey;
    }

    }
