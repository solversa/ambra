/* $HeadURL$
 * $Id$
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

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import org.ambraproject.service.annotation.AnnotationService;
import org.ambraproject.service.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * Action to create an annotation. It also does profanity validation on the user content.
 */
@SuppressWarnings("serial")
public class CreateAnnotationAction extends DiscussionAction {

  private String target;
  private String mimeType = "text/plain";
  private String annotationId;
  private boolean isPublic = false;
  private String supercedes;

  protected AnnotationService annotationService;
  private Cache articleHtmlCache;
  private static final Logger log = LoggerFactory.getLogger(CreateAnnotationAction.class);

  @Override
  protected void create() {
    annotationId = annotationService.createComment(
        getCurrentUser(),
        target,
        commentTitle,
        comment,
        ciStatement).toString();

    articleHtmlCache.remove(target);
  }

  @Override
  protected void error(Exception e) {
    log.error("Could not create annotation", e);
    addActionError("Annotation creation failed with error message: " + e.getMessage());
  }

  @Override
  protected void success() {
    addActionMessage("Annotation created with id:" + annotationId);
  }

  /**
   * Set the target that it annotates.
   *
   * @param target target
   */
  public void setTarget(final String target) {
    this.target = target;
  }

  /**
   * @param isPublic set the visibility of annotation
   */
  public void setIsPublic(final boolean isPublic) {
    this.isPublic = isPublic;
  }

  /**
   * Set the mimeType of the annotation
   *
   * @param mimeType mimeType
   */
  public void setMimeType(final String mimeType) {
    this.mimeType = mimeType;
  }

  /**
   * Get the id of the newly created annotation
   *
   * @return annotation id
   */
  public String getAnnotationId() {
    return annotationId;
  }

  /**
   * @return the target
   */
  @RequiredStringValidator(message = "You must specify the target that this annotation is applied on")
  public String getTarget() {
    return target;
  }

  /**
   * @return the commentTitle
   */
  public String getCommentTitle() {
    return commentTitle;
  }

  /**
   * @return the annotation content
   */
  public String getComment() {
    return comment;
  }

  /**
   * @return the mime type
   */
  public String getMimeType() {
    return mimeType;
  }

  /**
   * @return whether the annotation is public
   */
  public boolean getIsPublic() {
    return isPublic;
  }

  /**
   * @return the older annotation that it supersedes
   */
  public String getSupercedes() {
    return supercedes;
  }

  /**
   * @param supercedes the older annotation that it supersedes
   */
  public void setSupercedes(final String supercedes) {
    this.supercedes = supercedes;
  }

  @Required
  public void setAnnotationService(final AnnotationService annotationService) {
    this.annotationService = annotationService;
  }

  /**
   * @param articleHtmlCache The Article(transformed) cache to use
   */
  @Required
  public void setArticleHtmlCache(Cache articleHtmlCache) {
    this.articleHtmlCache = articleHtmlCache;
  }
}
