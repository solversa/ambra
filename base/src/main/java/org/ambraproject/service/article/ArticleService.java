/*
 * Copyright (c) 2006-2014 by Public Library of Science
 *
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ambraproject.service.article;

import org.ambraproject.ApplicationException;
import org.ambraproject.models.Article;
import org.ambraproject.models.ArticleRelationship;
import org.ambraproject.models.Category;
import org.ambraproject.views.CitedArticleView;
import org.ambraproject.views.SearchHit;
import org.ambraproject.views.TOCArticle;
import org.ambraproject.views.article.ArticleInfo;
import org.ambraproject.views.article.BaseArticleInfo;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;
import java.net.URI;
import java.text.ParseException;
import java.util.List;
import java.util.Set;
import java.util.Map;

/**
 * @author Joe Osowski
 */

public interface ArticleService {
  /**
   * Determines if the set contains a type of research article
   *
   * @param types The set of types to search
   * @return True if the article is a research article
   * @throws ApplicationException
   *                                  if there was a problem talking to the OTM
   * @throws NoSuchArticleIdException When the article does not exist
   */
  public boolean containsResearchType(final Set<String> types)
    throws NoSuchArticleIdException, ApplicationException;

  /**
   * Determines if the set contains a type of retraction
   *
   * @param types The set of types to search
   * @return True if the article is a retraction article
   * @throws NoSuchArticleIdException
   * @throws ApplicationException
   */
  public boolean containsRetractionType(final Set<String> types)
    throws NoSuchArticleIdException, ApplicationException;

  /**
   * Determines if the set contains a type of expression of concern
   *
   * @param types The set of types to search
   * @return True if the article is a eoc article
   * @throws ApplicationException
   * @throws NoSuchArticleIdException When the article does not exist
   */
  public boolean containsEocType(final Set<String> types)
    throws NoSuchArticleIdException, ApplicationException;


  /**
   * Determines if the set contains a type of Correction
   *
   * @param types The set of types to search
   * @return True if the article is a correction article
   * @throws ApplicationException
   * @throws NoSuchArticleIdException When the article does not exist
   */
  public boolean containsCorrectionType(final Set<String> types)
    throws NoSuchArticleIdException, ApplicationException;

  /**
   * Determines if the articleURI is of type researchArticle
   *
   * @param article The article object
   * @return True if the article is a research article
   * @throws ApplicationException
   *                                  if there was a problem talking to the OTM
   * @throws NoSuchArticleIdException When the article does not exist
   */
  public boolean isResearchArticle(final Article article)
    throws NoSuchArticleIdException, ApplicationException;

  /**
   * Determines if the articleURI is of type researchArticle
   *
   * @param articleInfo The articleInfo object
   * @return True if the article is a research article
   * @throws ApplicationException
   *                                  if there was a problem talking to the OTM
   * @throws NoSuchArticleIdException When the article does not exist
   */
  public boolean isResearchArticle(final ArticleInfo articleInfo)
      throws NoSuchArticleIdException, ApplicationException;

  /**
   * Determines if the article is of type retraction
   *
   * @param articleInfo The ArticleInfo object
   * @return True if the article is a retraction article
   * @throws NoSuchArticleIdException
   * @throws ApplicationException
   */
  public boolean isRetractionArticle(final BaseArticleInfo articleInfo)
          throws NoSuchArticleIdException, ApplicationException;

  /**
   * Determines if the articleURI is of type expression of concern
   *
   * @param articleInfo The ArticleType object
   * @return True if the article is a eoc article
   * @throws ApplicationException
   * @throws NoSuchArticleIdException When the article does not exist
   */
  public boolean isEocArticle(final BaseArticleInfo articleInfo)
      throws NoSuchArticleIdException, ApplicationException;


  /**
   * Determines if the article is of type Correction
   *
   * @param articleInfo The ArticleInfo object
   * @return True if the article is a correction article
   * @throws ApplicationException
   * @throws NoSuchArticleIdException When the article does not exist
   */
  public boolean isCorrectionArticle(final BaseArticleInfo articleInfo)
          throws NoSuchArticleIdException, ApplicationException;

  /**
   * Check the type of the article for taxonomy classification using the article object
   * @param article the article
   * @return true if the article is an amendment (correction, eoc or retraction)
   * @throws ApplicationException
   * @throws NoSuchArticleIdException
   */
  public boolean isAmendment(Article article) throws ApplicationException, NoSuchArticleIdException;
  /**
   * Get a List of all of the Journal/Volume/Issue combinations that contain the <code>articleURI</code> which was
   * passed in. Each primary List element contains a secondary List of six Strings which are, in order: <ul>
   * <li><strong>Element 0: </strong> Journal URI</li> <li><strong>Element 1: </strong> Journal key</li>
   * <li><strong>Element 2: </strong> Volume URI</li> <li><strong>Element 3: </strong> Volume name</li>
   * <li><strong>Element 4: </strong> Issue URI</li> <li><strong>Element 5: </strong> Issue name</li> </ul> A Journal
   * might have multiple Volumes, any of which might have multiple Issues that contain the <code>articleURI</code>. The
   * primary List will always contain one element for each Issue that contains the <code>articleURI</code>.
   *
   * @param articleDoi Article DOI that is contained in the Journal/Volume/Issue combinations which will be returned
   * @return All of the Journal/Volume/Issue combinations which contain the articleURI passed in
   */
  public List<List<String>> getArticleIssues(final String articleDoi);

  /**
   * Change an articles state.
   *
   * @param articleDoi uri
   * @param authId the authorization ID of the current user
   * @param state   state
   *
   * @throws NoSuchArticleIdException NoSuchArticleIdException
   */
  public void setState(final String articleDoi, final String authId, final int state) throws NoSuchArticleIdException;

  /**
   * Get the ids of all articles satisfying the given criteria.
   * <p/>
   * This method calls <code>getArticles(...)</code> then parses the Article IDs from that List.
   * <p/>
   *
   * @param params
   * @return the (possibly empty) list of article ids.
   * @throws java.text.ParseException if any of the dates could not be parsed
   */
  public List<String> getArticleDOIs(final ArticleServiceSearchParameters params) throws ParseException;

  /**
   * Return a list of articles representing articles published within the 30 days ignoring image articles
   * The list returned is randomized.  If there are less then articleCount articles in the results, go back in time
   * up to 30 days to fill the list
   *
   * @param journal_eIssn the journal to filter on
   * @param articleTypesToShow the list of URLS to filter on
   * @param numDaysInPast the number of days into the past to look back
   * @param articleCount the minimum number of articles to return
   *
   * @return a list randomized search results
   */
  @Transactional(readOnly = true)
  public List<SearchHit> getRandomRecentArticles(String journal_eIssn, List<URI>articleTypesToShow, int numDaysInPast, int articleCount);

  /**
   * Get all of the articles satisfying the given criteria.

   * @param params
   *
   * @return all of the articles satisfying the given criteria (possibly null) Key is the Article DOI.  Value is the
   *         Article itself.
   */
  public List<Article> getArticles(final ArticleServiceSearchParameters params);

  /**
   * Get an Article by URI.
   *
   * @param articleDoi URI of Article to get.
   * @param authId the authorization ID of the current user
   * @return Article with specified URI or null if not found.
   * @throws NoSuchArticleIdException NoSuchArticleIdException
   */
  public Article getArticle(final String articleDoi, final String authId)
    throws NoSuchArticleIdException;

  /**
   * Get an Article by ID.
   *
   * @param articleID ID of Article to get.
   * @param authId the authorization ID of the current user
   * @return Article with specified URI or null if not found.
   * @throws NoSuchArticleIdException NoSuchArticleIdException
   */
  public Article getArticle(final Long articleID, final String authId)
      throws NoSuchArticleIdException;

  /**
   * Get articles based on a list of Article id's.
   *
   * If an article is requested that the user does not have access to, it will not be returned
   *
   * @param articleDois list of article id's
   * @param authId the authorization ID of the current user
   * @return <code>List&lt;Article&gt;</code> of articles requested
   */
  public List<Article> getArticles(final List<String> articleDois, final String authId);

  /**
   * Get the articleInfo object for an article
   * @param articleDoi the ID of the article
   * @param authId the authorization ID of the current user
   * @return articleInfo
   */
  public ArticleInfo getArticleInfo(final String articleDoi, final String authId) throws NoSuchArticleIdException;

  /**
   * Get the articleInfo object for an article
   * @param articleID the back-end primary key of the article
   * @param authId the authorization ID of the current user
   * @return articleInfo
   */
  public ArticleInfo getArticleInfo(final Long articleID, final String authId) throws NoSuchArticleIdException;

  /**
   * Get a table of Contents style list of articles
   *
   * @param articleDois the list of articles to fetch
   * @param authId the authorization ID of the current user
   *
   * @return
   */
  public List<TOCArticle> getArticleTOCEntries(final List<String> articleDois, final String authId);

  /**
   * Get a basic view object for the article with the corresponding id
   *
   * @param articleID the id of the article to get
   * @return a simple wrapper around string properties of the article
   */
  public ArticleInfo getBasicArticleView(Long articleID) throws NoSuchArticleIdException;


  /**
   * Get a basic view object for the article by doi
   * @param articleDoi the doi of the article
   * @return a simple wrapper around the article with only title and doi set
   * @throws NoSuchArticleIdException if the article doesn't exist
   */
  public ArticleInfo getBasicArticleView(String articleDoi) throws NoSuchArticleIdException;

  // TODO: consider moving these two methods into a new CitedArticleService.  Seems like overkill for now.
  /**
   * Loads a CitedArticle from the DB.
   *
   * @param citedArticleID primary key
   * @return the CitedArticle instance
   */
  public CitedArticleView getCitedArticle(long citedArticleID);

  /**
   * Populates DB objects as necessary to assign the given categories to the given article.
   * This only selects the top 8 categories given to it and saves those categories.
   *
   * Sorted by weight.  The items with the lowest weights beyond 8 are dropped
   *
   * @param article article to update
   * @param categories List of category strings
   *
   * @return The list of categories applied to the article
   */
  public Map<Category, Integer> setArticleCategories(Article article, Map<String, Integer> categories);

  /**
   * Throw a NoSuchArticleIdException exception if the article doesn't exist or the user does not have permission
   * to see the article
   *
   * @param articleDoi article doi
   * @param authId the authorization ID of the current user
   * @throws NoSuchArticleIdException
   */
  public void checkArticleState(final String articleDoi, final String authId) throws NoSuchArticleIdException;

  /**
   * Returns an article's amendments of type Retraction or Expression of Concern
   * Used in displaying the search results
   *
   * @param articleDoi the article doi
   * @return
   * @throws Exception
   */
  public List<ArticleRelationship> getArticleAmendments(String articleDoi) throws DataAccessException;
}
