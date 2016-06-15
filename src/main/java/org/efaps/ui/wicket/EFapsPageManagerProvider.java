/*
 * Copyright 2003 - 2016 The eFaps Team
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
 */
package org.efaps.ui.wicket;

import org.apache.wicket.Application;
import org.apache.wicket.DefaultPageManagerProvider;
import org.apache.wicket.pageStore.IDataStore;
import org.apache.wicket.pageStore.IPageStore;
import org.apache.wicket.serialize.ISerializer;
import org.efaps.ui.wicket.store.EFapsPageStore;

/**
 * The Class EFapsPageManagerProvider.
 *
 * @author The eFaps Team
 */
public class EFapsPageManagerProvider
    extends DefaultPageManagerProvider
{

    /**
     * Instantiates a new e faps page manager provider.
     *
     * @param _application the application
     */
    public EFapsPageManagerProvider(final Application _application)
    {
        super(_application);
    }

    @Override
    protected IPageStore newPageStore(final IDataStore _dataStore)
    {
        final int inmemoryCacheSize = this.application.getStoreSettings().getInmemoryCacheSize();
        final ISerializer pageSerializer = this.application.getFrameworkSettings().getSerializer();
        return new EFapsPageStore(pageSerializer, _dataStore, inmemoryCacheSize);
    }
}
