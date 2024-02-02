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
package org.efaps.ui.wicket;

/**
 * This interface is used to trigger the isInstantiationAuthorized Method in
 * {@link org.efaps.ui.wicket.EFapsApplication
 * .EFapsFormBasedAuthorizationStartegy}.
 * This has the effect that only a Page which implements this Interface, will
 * <b>not</b> be checked if a User is checked in and so can be accessed.
 *
 * @author Jan Moxter
 * @version $Id$
 */
public interface EFapsNoAuthorizationNeededInterface
{

}
