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

package org.efaps.ui.wicket.models;

import java.io.Serializable;
import java.util.Formatter;

import org.apache.commons.lang3.RandomStringUtils;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class EmbeddedLink
    implements Serializable
{

    /**
     * The Parameter Key if used for with Jasper Element Handler.
     */
    public static final String JASPER_PARAMETERKEY = EmbeddedLink.class + ".Key";

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Predefined tag elements that can be used for rendering.
     */
    enum TAG
    {
        JASPER("<span id=\"%s\" class=\"eFapsLink\"></span>");

        private String html;

        TAG(final String _html)
        {
            this.html = _html;
        }

        public String getHtml()
        {
            return this.html;
        }
    }

    /**
     * The instance key for the link.
     */
    private String instanceKey;

    /**
     * The id of this embedded link.
     */
    private final String id = RandomStringUtils.randomAlphanumeric(8);

    /**
     * The html tag to be used.
     */
    private String tag;

    /**
     * @param _instanceKey instanc key
     */
    public EmbeddedLink(final String _instanceKey)
    {
        this.instanceKey = _instanceKey;
    }

    /**
     * Getter method for the instance variable {@link #instanceKey}.
     *
     * @return value of instance variable {@link #instanceKey}
     */
    public String getInstanceKey()
    {
        return this.instanceKey;
    }

    /**
     * Setter method for instance variable {@link #instanceKey}.
     *
     * @param _instanceKey value for instance variable {@link #instanceKey}
     */
    public void setInstanceKey(final String _instanceKey)
    {
        this.instanceKey = _instanceKey;
    }

    /**
     * Getter method for the instance variable {@link #id}.
     *
     * @return value of instance variable {@link #id}
     */
    public String getId()
    {
        return this.id;
    }

    /**
     * Getter method for the instance variable {@link #tag}.
     *
     * @return value of instance variable {@link #tag}
     */
    public String getTag()
    {
        String ret = this.tag;
        final Formatter formatter = new Formatter();
        formatter.format(ret, getId());
        ret = formatter.toString();
        formatter.close();
        return ret;
    }

    /**
     * Setter method for instance variable {@link #tag}.
     *
     * @param _tag value for instance variable {@link #tag}
     */
    public void setTagHtml(final String _tag)
    {
        this.tag = _tag;
    }

    /**
     * @param _instanceKey instance key the link is wanted for
     * @return new Embeded Link
     */
    public static EmbeddedLink getJasperLink(final String _instanceKey)
    {
        final EmbeddedLink ret = new EmbeddedLink(_instanceKey);
        ret.setTagHtml(TAG.JASPER.getHtml());
        return ret;
    }
}
