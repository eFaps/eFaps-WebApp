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


package org.efaps.ui.wicket.components.form.chart;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.wicket.markup.html.image.resource.DynamicImageResource;
import org.efaps.ui.wicket.models.cell.UIFormCellChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ChartResource
    extends DynamicImageResource
{
    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ChartResource.class);

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Underlying uiObject for this resource.
     */
    private final UIFormCellChart uiObject;

    /**
     * @param _uiObject UIFormCellChart
     */
    public ChartResource(final UIFormCellChart _uiObject)
    {
        this.uiObject = _uiObject;
    }

    /**
     * Get the Image Data as Byte Array.
     * @return image data as byte array
     * @see org.apache.wicket.markup.html.image.resource.DynamicImageResource#getImageData()
     */
    @Override
    protected byte[] getImageData()
    {
        byte[] bytes = null;
        try {
            final FileInputStream fis = new FileInputStream(this.uiObject.getImage());
            //InputStream in = resource.openStream();
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            final byte[] buf = new byte[1024];
            for (int len = fis.read(buf); len > 0; len = fis.read(buf)) {
                bos.write(buf, 0, len);
            }
            fis.close();
            bytes = bos.toByteArray();
        } catch (final FileNotFoundException e) {
            ChartResource.LOG.error("FileNotFoundException", e);
        } catch (final IOException e) {
            ChartResource.LOG.error("IOException", e);
        }
        return bytes;
    }
}
