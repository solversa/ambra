/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2010 by Topaz, Inc.
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

package org.topazproject.ambra.search2.service;

import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.search2.SearchParameters;
import org.topazproject.ambra.search2.SearchResultSinglePage;

/**
 * Search service interface.
 *
 * @author Dragisa Krsmanovic
 */
public interface SearchService {

  SearchResultSinglePage simpleSearch(SearchParameters searchParameters) throws ApplicationException;

  SearchResultSinglePage advancedSearch(SearchParameters searchParameters) throws ApplicationException;
  
  SearchResultSinglePage findAnArticleSearch(SearchParameters searchParameters) throws ApplicationException;

  SearchResultSinglePage getFilterData(SearchParameters searchParameters) throws ApplicationException;
}
