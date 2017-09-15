/*
 * Copyright 2003 - 2017 The eFaps Team
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

package org.efaps.ui.wicket.components.preferences;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ILabelProvider;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.api.ui.IPreferencesProvider;
import org.efaps.ui.wicket.behaviors.dojo.SwitchBehavior;
import org.efaps.ui.wicket.components.button.AjaxButton;
import org.efaps.ui.wicket.components.button.AjaxButton.ICON;
import org.efaps.ui.wicket.pages.main.MainPage;
import org.efaps.ui.wicket.resources.AbstractEFapsHeaderItem;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.util.Configuration;
import org.efaps.ui.wicket.util.Configuration.ConfigAttribute;
import org.efaps.util.EFapsBaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class PreferencesPanel.
 */
public class PreferencesPanel
    extends GenericPanel<Void>
{
    /**
     * Reference to the style sheet.
     */
    private static final EFapsContentReference CSS = new EFapsContentReference(PreferencesPanel.class, "Preferences.css");


    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(PreferencesPanel.class);

    /**
     * Instantiates a new preferences panel.
     *
     * @param _wicketId the wicket id
     */
    public PreferencesPanel(final String _wicketId)
    {
        super(_wicketId);
        final Map<String, String> prefMap = new HashMap<>();
        final String providerClass = Configuration.getAttribute(ConfigAttribute.PREF_PROVIDER);
        try {
            final Class<?> clazz = Class.forName(providerClass, false, EFapsClassLoader.getInstance());
            final IPreferencesProvider provider = (IPreferencesProvider) clazz.newInstance();
            prefMap.putAll(provider.getPreferences());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            LOG.error("Catched", e);
        } catch (final EFapsBaseException e) {
            LOG.error("Catched", e);
        }

        final Form<Void> form = new Form<Void>("preferencesForm")
        {

            /** The Constant serialVersionUID. */
            private static final long serialVersionUID = 1L;

            @Override
            protected CharSequence getActionUrl()
            {
                return "";
            }
        };
        add(form);

        final boolean slideIn = BooleanUtils.toBoolean(prefMap.get(ConfigAttribute.SLIDEINMENU.getKey()));
        final PreferenceComponent slideInMenu = new PreferenceComponent("slideInMenu")
                        .setLabel(DBProperties.getProperty(ConfigAttribute.SLIDEINMENU.getKey() + ".Label"));
        slideInMenu.add(new SwitchBehavior()
                        .setInputName(ConfigAttribute.SLIDEINMENU.getKey())
                        .setOn(slideIn)
                        .setLeftLabel("Si")
                        .setRightLabel("No"));
        form.add(slideInMenu);
        slideInMenu.setVisible(prefMap.containsKey(ConfigAttribute.SLIDEINMENU.getKey()));

        final boolean tdtB = BooleanUtils.toBoolean(prefMap.get(ConfigAttribute.TABLEDEFAULTTYPECONTENT.getKey()));
        final PreferenceComponent tdtContent = new PreferenceComponent("tableDefaultType4Content")
                    .setLabel(DBProperties.getProperty(ConfigAttribute.TABLEDEFAULTTYPECONTENT.getKey() + ".Label"));
        tdtContent.add(new SwitchBehavior()
                        .setInputName(ConfigAttribute.TABLEDEFAULTTYPECONTENT.getKey())
                        .setLeftLabel("Table")
                        .setRightLabel("Grid")
                        .setOn(tdtB));
        form.add(tdtContent);
        tdtContent.setVisible(prefMap.containsKey(ConfigAttribute.TABLEDEFAULTTYPECONTENT.getKey()));


        final boolean tdtS = BooleanUtils.toBoolean(prefMap.get(ConfigAttribute.TABLEDEFAULTTYPESEARCH.getKey()));
        final PreferenceComponent tdtSearch = new PreferenceComponent("tableDefaultType4Search")
                    .setLabel(DBProperties.getProperty(ConfigAttribute.TABLEDEFAULTTYPESEARCH.getKey() + ".Label"));
        tdtSearch.add(new SwitchBehavior()
                        .setInputName(ConfigAttribute.TABLEDEFAULTTYPESEARCH.getKey())
                        .setLeftLabel("Table")
                        .setRightLabel("Grid")
                        .setOn(tdtS));
        form.add(tdtSearch);
        tdtSearch.setVisible(prefMap.containsKey(ConfigAttribute.TABLEDEFAULTTYPESEARCH.getKey()));

        final boolean tdtT = BooleanUtils.toBoolean(prefMap.get(ConfigAttribute.TABLEDEFAULTTYPETREE.getKey()));
        final PreferenceComponent tdtTree = new PreferenceComponent("tableDefaultType4Tree")
                    .setLabel(DBProperties.getProperty(ConfigAttribute.TABLEDEFAULTTYPETREE.getKey() + ".Label"));
        tdtTree.add(new SwitchBehavior()
                        .setInputName(ConfigAttribute.TABLEDEFAULTTYPETREE.getKey())
                        .setLeftLabel("Table")
                        .setRightLabel("Grid")
                        .setOn(tdtT));
        form.add(tdtTree);
        tdtTree.setVisible(prefMap.containsKey(ConfigAttribute.TABLEDEFAULTTYPETREE.getKey()));

        final boolean tdtF = BooleanUtils.toBoolean(prefMap.get(ConfigAttribute.TABLEDEFAULTTYPEFORM.getKey()));
        final PreferenceComponent tdtForm = new PreferenceComponent("tableDefaultType4Form")
                    .setLabel(DBProperties.getProperty(ConfigAttribute.TABLEDEFAULTTYPEFORM.getKey() + ".Label"));
        tdtForm.add(new SwitchBehavior()
                        .setInputName(ConfigAttribute.TABLEDEFAULTTYPEFORM.getKey())
                        .setLeftLabel("Table")
                        .setRightLabel("Grid")
                        .setOn(tdtF));
        form.add(tdtForm);
        tdtForm.setVisible(prefMap.containsKey(ConfigAttribute.TABLEDEFAULTTYPEFORM.getKey()));

        final AjaxButton<Void> saveBtn = new AjaxButton<Void>("saveBtn", ICON.ACCEPT.getReference(), "Save")
        {

            /** The Constant serialVersionUID. */
            private static final long serialVersionUID = 1L;

            @Override
            public void onRequest(final AjaxRequestTarget _target)
            {
                final IRequestParameters parameters = RequestCycle.get().getRequest().getRequestParameters();
                final Map<String, String> prefMap = new HashMap<>();
                for (final String name : parameters.getParameterNames()) {
                    prefMap.put(name, parameters.getParameterValue(name).toString());
                }
                try {
                    final Class<?> clazz = Class.forName(providerClass, false, EFapsClassLoader.getInstance());
                    final IPreferencesProvider provider = (IPreferencesProvider) clazz.newInstance();
                    provider.updatePreferences(prefMap);
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    LOG.error("Catched", e);
                } catch (final EFapsBaseException e) {
                    LOG.error("Catched", e);
                }
                RequestCycle.get().setResponsePage(MainPage.class);
            }
        };
        form.add(saveBtn);
    }

    /**
     * Render to the web response the eFapsContentReference.
     *
     * @param _response Response object
     */@Override
    public void renderHead(final IHeaderResponse _response)
    {
        super.renderHead(_response);
        _response.render(AbstractEFapsHeaderItem.forCss(CSS));
    }

    /**
     * The Class PreferenceComponent.
     */
    public static class PreferenceComponent
        extends WebComponent
        implements ILabelProvider<String>
    {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /** The label. */
        private String label;

        /**
         * Instantiates a new preference component.
         *
         * @param _wicketId the wicket id
         */
        public PreferenceComponent(final String _wicketId)
        {
            super(_wicketId);
        }

        /**
         * Sets the label.
         *
         * @param _label the new label
         * @return the preference component
         */
        public PreferenceComponent setLabel(final String _label)
        {
            this.label = _label;
            return this;
        }

        @Override
        public IModel<String> getLabel()
        {
            return Model.of(this.label);
        }
    }
}
