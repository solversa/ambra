/* $HeadURL::                                                                            $
 * $Id:ListReplyAction.java 722 2006-10-02 16:42:45Z viru $
 *
 * Copyright (c) 2006-2010 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
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
package org.ambraproject.action.annotation;

import org.ambraproject.action.BaseActionSupport;
import org.ambraproject.service.annotation.AnnotationService;
import org.ambraproject.service.article.ArticleService;
import org.ambraproject.service.article.FetchArticleService;
import org.ambraproject.views.AnnotationView;
import org.ambraproject.views.AuthorExtra;
import org.ambraproject.views.article.ArticleInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.w3c.dom.Document;

import java.util.List;

/**
 * Action class to get a list of replies to annotations.
 */
@SuppressWarnings("serial")
public class ListReplyAction extends BaseActionSupport {
  private static final Logger log = LoggerFactory.getLogger(ListReplyAction.class);

  protected AnnotationService annotationService;
  private ArticleService articleService;
  private FetchArticleService fetchArticleService;

  private Long root;
  private AnnotationView baseAnnotation;
  private ArticleInfo articleInfo;
  private List<AuthorExtra> authorExtras;

  @Override
  public String execute() throws Exception {
    try {
      baseAnnotation = annotationService.getFullAnnotationView(root);
      articleInfo = articleService.getBasicArticleView(baseAnnotation.getArticleID());

      Document doc = fetchArticleService.getArticleDocument(articleInfo);
      authorExtras = fetchArticleService.getAuthorAffiliations(doc);
    } catch (Exception ae) {
      log.error("Could not list all replies for root: " + root, ae);
      addActionError("Reply fetching failed with error message: " + ae.getMessage());
      return ERROR;
    }

    return SUCCESS;
  }

  public void setRoot(final Long root) {
    this.root = root;
  }

  /**
   * @return Returns the baseAnnotation.
   */
  public AnnotationView getBaseAnnotation() {
    return baseAnnotation;
  }

  public ArticleInfo getArticleInfo() {
    return articleInfo;
  }

  public List<AuthorExtra> getAuthorExtras() {
    return authorExtras;
  }

  @Required
  public void setArticleService(ArticleService articleService) {
    this.articleService = articleService;
  }

  @Required
  public void setAnnotationService(final AnnotationService annotationService) {
    this.annotationService = annotationService;
  }

  @Required
  public void setFetchArticleService(FetchArticleService fetchArticleService) {
    this.fetchArticleService = fetchArticleService;
  }

}
