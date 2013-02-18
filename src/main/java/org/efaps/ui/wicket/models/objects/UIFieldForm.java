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

package org.efaps.ui.wicket.models.objects;

import java.util.UUID;

import org.efaps.admin.datamodel.Classification;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.ui.Form;
import org.efaps.util.EFapsException;

/**
 * Class serves as the model for a field form. Meaning a from that is
 * displayed inside another form. This system of
 * displaying a from inside another form is used for classification.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class UIFieldForm
    extends UIForm
    implements IFormElement
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Stores the uuid of the classification in case that the constructor
     * for create is used.
     */
    private UUID classificationUUID;

    /**
     * Constructor for the case that a instance is given e.g. view, edit.
     *
     * @param _commandUuid the uuid of the command that opened the parent form
     * @param _instanceKey instance key for this UIFieldForm
     * @throws EFapsException on error
     */
    public UIFieldForm(final UUID _commandUuid,
                       final String _instanceKey)
        throws EFapsException
    {
        super(_commandUuid, _instanceKey);
        if (getInstance().getType() instanceof Classification) {
            final Form form = Form.getTypeForm(getInstance().getType());
            this.classificationUUID = getInstance().getType().getUUID();
            setFormUUID(form.getUUID());
        }
    }

    /**
     * Constructor used during create.
     *
     * @param _commandUuid the uuid of the command that opened the parent form
     * @param _classification the classificationto be created
     */
    public UIFieldForm(final UUID _commandUuid,
                       final UIClassification _classification)
        throws EFapsException
    {
        super(_commandUuid, null);
        final Type type = Type.get(_classification.getClassificationUUID());
        this.classificationUUID = type.getUUID();
        final Form form = Form.getTypeForm(type);
        setFormUUID(form.getUUID());
    }

    /**
     * Must be overwritten to set the create type to the underlying classification.
     *
     * @see org.efaps.ui.wicket.models.objects.UIForm#getCreateTargetType()
     * @return the classification type
     */
    @Override
    protected Type getCreateTargetType()
    {
        return Type.get(this.classificationUUID);
    }

    /**
     * Getter method for instance variable {@link #classificationUUID}.
     *
     * @return value of instance variable {@link #classificationUUID}
     */
    public UUID getClassificationUUID()
    {
        return this.classificationUUID;
    }
}
