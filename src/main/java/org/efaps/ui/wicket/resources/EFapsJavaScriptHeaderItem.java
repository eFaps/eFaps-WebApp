/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.ui.wicket.resources;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.core.util.string.JavaScriptUtils;
import org.apache.wicket.request.Response;
import org.apache.wicket.util.value.AttributeMap;
import org.efaps.ui.wicket.EFapsApplication;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class EFapsJavaScriptHeaderItem
    extends AbstractEFapsHeaderItem
{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _reference reference for this HeaderItem
     */
    public EFapsJavaScriptHeaderItem(final EFapsContentReference _reference)
    {
        super(_reference);
    }

    /**
     * @return empty Collection because no rendering will be donw
     */
    @Override
    public Iterable<?> getRenderTokens()
    {
        final List<String> ret = new ArrayList<>();
        ret.add(getReference().getName());
        return ret;
    }

    @Override
    public void render(final Response response)
    {
        final AttributeMap attributes = new AttributeMap();
        attributes.putAttribute(JavaScriptUtils.ATTR_TYPE, "text/javascript");
        attributes.putAttribute(JavaScriptUtils.ATTR_SCRIPT_SRC,
                        EFapsApplication.get().getServletContext().getContextPath()
                                        + "/servlet/static/" + getReference().getName());
        JavaScriptUtils.writeScript(response, attributes);
    }
}
