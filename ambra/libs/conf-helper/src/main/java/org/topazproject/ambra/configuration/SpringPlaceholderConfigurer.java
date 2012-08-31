/* $HeadURL::                                                                                    $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
 * http://topazproject.org
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
package org.topazproject.ambra.configuration;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.apache.commons.configuration.Configuration;
import java.util.Properties;

/**
 * A Spring Configurer that uses the commons-configuration singleton
 * <code>ConfigurationStore</code> in this package to get its configuration.
 *
 * @author Eric Brown
 */
public class SpringPlaceholderConfigurer extends PropertyPlaceholderConfigurer {
  protected String resolvePlaceholder(String placeholder, Properties properties) {
    Configuration conf = ConfigurationStore.getInstance().getConfiguration();
    String value = null;

    if (conf != null)
      value = conf.getString(placeholder);
    if (value == null)
      value = super.resolvePlaceholder(placeholder, properties);

    return value;
  }
}