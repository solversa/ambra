/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.rating.action;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.action.BaseActionSupport;
import org.plos.article.service.FetchArticleService;
import org.plos.model.article.ArticleType;
import org.plos.models.Article;
import org.plos.models.Rating;
import org.plos.models.RatingContent;
import org.plos.models.RatingSummary;
import org.plos.models.RatingSummaryContent;
import org.plos.rating.service.RatingsPEP;
import org.plos.user.PlosOneUser;
import org.plos.util.ProfanityCheckingService;
import org.springframework.beans.factory.annotation.Required;
import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.criterion.Restrictions;
import org.topazproject.otm.util.TransactionHelper;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;


/**
 * General Rating action class to store and retrieve a users's rating
 *
 * @author Stephen Cheng
 */
@SuppressWarnings("serial")
public class RateAction extends BaseActionSupport {
  private double           insight;
  private double           reliability;
  private double           style;
  private double           singleRating;
  private String           articleURI;
  private boolean          isResearchArticle;
  private String           commentTitle;
  private String           comment;
  private Session          session;
  private RatingsPEP       pep;
  private FetchArticleService fetchArticleService;

  private ProfanityCheckingService profanityCheckingService;
  private static final Log log = LogFactory.getLog(RateAction.class);

  private RatingsPEP getPEP() {
    try {
      if (pep == null)
        pep                      = new RatingsPEP();
    } catch (Exception e) {
      throw new Error("Failed to create Ratings PEP", e);
    }
    return pep;
  }
  
  /**
   * @param fetchArticleService the fetchArticleService to set
   */
  @Required
  public void setFetchArticleService(FetchArticleService fetchArticleService) {
    this.fetchArticleService = fetchArticleService;
  }

  /**
   * Rates an article for the currently logged in user.  Will look to see if there are
   * existing rating. If so, will update the ratings, otherwise will insert new ones.
   *
   * @return WebWork action status
   */
  public String rateArticle() {
    final PlosOneUser       user               = PlosOneUser.getCurrentUser();
    final Date              now                = new Date(System.currentTimeMillis());
    final URI               annotatedArticle;


    try {
      annotatedArticle = new URI(articleURI);
    } catch (URISyntaxException ue) {
      log.info("Could not construct article URI: " + articleURI, ue);

      return ERROR;
    }

    if (user == null) {
      log.info("User is null for rating articles");
      addActionError("Must be logged in");

      return ERROR;
    }

    getPEP().checkObjectAccess(RatingsPEP.SET_RATINGS, URI.create(user.getUserId()), annotatedArticle);

    if(isResearchArticle) {
      // must rate at least one rating category
      if (insight == 0 && reliability == 0 && style == 0) {
        addActionError("At least one category must be rated");
        return INPUT;
      }
    }
    else {
      // ensure the single rating specified
      if(singleRating == 0) {
        addActionError("A rating must be specified.");
        return INPUT;
      }
    }

    // reject profanity in content
    final List<String> profaneWordsInCommentTitle = profanityCheckingService.validate(commentTitle);
    final List<String> profaneWordsInComment      = profanityCheckingService.validate(comment);
    if (profaneWordsInCommentTitle.size() != 0
      || profaneWordsInComment.size() != 0) {
      addProfaneMessages(profaneWordsInCommentTitle, "commentTitle", "title");
      addProfaneMessages(profaneWordsInComment, "comment", "comment");
      return INPUT;
    }

    return TransactionHelper.doInTx(session,
                              new TransactionHelper.Action<String>() {
      @SuppressWarnings("unchecked")
      public String run(Transaction tx) {
        Rating        articleRating        = null;
        RatingSummary articleRatingSummary = null;
        boolean       newRating            = false;

        if (log.isDebugEnabled()) {
          log.debug("Retrieving user Ratings for article: " + articleURI + " and user: "
                  + user.getUserId());
        }

        // Ratings by this User for Article
        List<Rating> ratingsList = session
          .createCriteria(Rating.class)
          .add(Restrictions.eq("annotates", articleURI))
          .add(Restrictions.eq("creator", user.getUserId()))
          .list();
        if (ratingsList.size() == 0) {
          newRating = true;
          articleRating = new Rating();
          articleRating.setAnnotates(annotatedArticle);
          articleRating.setContext("");
          articleRating.setCreator(user.getUserId());
          articleRating.setCreated(now);
          articleRating.setBody(new RatingContent());
        } else if (ratingsList.size() == 1) {
          articleRating = ratingsList.get(0);
        } else {
          // should never happen
          String errorMessage = "Multiple Ratings, " + ratingsList.size() + ", for Article, " + articleURI + ", for user, " + user.getUserId();
          log.error(errorMessage);
          throw new RuntimeException(errorMessage);
        }

        // RatingsSummary for Article
        List<RatingSummary> summaryList = session
          .createCriteria(RatingSummary.class)
          .add(Restrictions.eq("annotates", articleURI))
          .list();
        if (summaryList.size() == 0) {
          articleRatingSummary = new RatingSummary();
          articleRatingSummary.setAnnotates(annotatedArticle);
          articleRatingSummary.setContext("");
          articleRatingSummary.setCreated(now);
          articleRatingSummary.setBody(new RatingSummaryContent());
        } else if (summaryList.size() == 1) {
          articleRatingSummary = summaryList.get(0);
        } else {
          // should never happen
          String errorMessage = "Multiple RatingsSummary, " + summaryList.size() + ", for Article " + articleURI;
          log.error(errorMessage);
          throw new RuntimeException(errorMessage);
        }

        // update Rating + RatingSummary with new values
        articleRating.setCreated(now);
        articleRatingSummary.setCreated(now);

        // if they had a prior insight Rating, remove it from the RatingSummary
        if (articleRating.getBody().getInsightValue() > 0) {
          articleRatingSummary.getBody().removeRating(Rating.INSIGHT_TYPE, articleRating.getBody().getInsightValue());
        }

        // update insight Article Rating, don't care if new, update or 0
        articleRating.getBody().setInsightValue((int) insight);

        // if user rated insight, add to to Article RatingSummary
        if (insight > 0) {
          articleRatingSummary.getBody().addRating(Rating.INSIGHT_TYPE, (int) insight);
        }

        // if they had a prior reliability Rating, remove it from the RatingSummary
        if (articleRating.getBody().getReliabilityValue() > 0) {
          articleRatingSummary.getBody().removeRating(Rating.RELIABILITY_TYPE, articleRating.getBody().getReliabilityValue());
        }

        // update reliability Article Rating, don't care if new, update or 0
        articleRating.getBody().setReliabilityValue((int) reliability);

        // if user rated reliability, add to to Article RatingSummary
        if (reliability > 0) {
          articleRatingSummary.getBody().addRating(Rating.RELIABILITY_TYPE, (int) reliability);
        }

        // if they had a prior style Rating, remove it from the RatingSummary
        if (articleRating.getBody().getStyleValue() > 0) {
          articleRatingSummary.getBody().removeRating(Rating.STYLE_TYPE, articleRating.getBody().getStyleValue());
        }

        // update style Article Rating, don't care if new, update or 0
        articleRating.getBody().setStyleValue((int) style);

        // if user rated style, add to to Article RatingSummary
        if (style > 0) {
          articleRatingSummary.getBody().addRating(Rating.STYLE_TYPE, (int) style);
        }

        // if they had a prior single Rating, remove it from the RatingSummary
        if (articleRating.getBody().getSingleRatingValue() > 0) {
          articleRatingSummary.getBody().removeRating(Rating.SINGLE_RATING_TYPE, articleRating.getBody().getSingleRatingValue());
        }

        // update single Article Rating, don't care if new, update or 0
        articleRating.getBody().setSingleRatingValue((int) singleRating);

        // if user rated single, add to to Article RatingSummary
        if (singleRating > 0) {
          articleRatingSummary.getBody().addRating(Rating.SINGLE_RATING_TYPE, (int) singleRating);
        }

        // Rating comment
        articleRating.getBody().setCommentTitle(commentTitle);
        articleRating.getBody().setCommentValue(comment);

        // if this is a new Rating, the summary needs to know
        if (newRating) {
          articleRatingSummary.getBody().setNumUsersThatRated(articleRatingSummary.getBody().getNumUsersThatRated() + 1);
        }

        // PLoS states that ratings can never be deleted once created,
        // so always do a Session.saveOrUpdate(), no delete
        session.saveOrUpdate(articleRating);
        session.saveOrUpdate(articleRatingSummary);
        return SUCCESS;
      }
    });
  }

  /**
   * Get the ratings and comment for the logged in user
   *
   * @return WebWork action status
   */
  public String retrieveRatingsForUser() {
    final PlosOneUser user = PlosOneUser.getCurrentUser();

    if (user == null) {
      log.info("User is null for retrieving user ratings");
      addActionError("Must be logged in");

      return ERROR;
    }

    getPEP().checkObjectAccess(RatingsPEP.GET_RATINGS, URI.create(user.getUserId()),
                                                    URI.create(articleURI));

    return TransactionHelper.doInTx(session,
                              new TransactionHelper.Action<String>() {
      @SuppressWarnings("unchecked")
      public String run(Transaction tx) {

        List<Rating> ratingsList = session
          .createCriteria(Rating.class)
          .add(Restrictions.eq("annotates", articleURI))
          .add(Restrictions.eq("creator", user.getUserId())).list();

        if (ratingsList.size() < 1) {
          log.debug("didn't find any matching ratings for user: " + user.getUserId());
          addActionError("No ratings for user");
          return ERROR;
        }

        Rating rating = ratingsList.get(0);

        setInsight(rating.getBody().getInsightValue());
        setReliability(rating.getBody().getReliabilityValue());
        setStyle(rating.getBody().getStyleValue());
        setSingleRating(rating.getBody().getSingleRatingValue());

        setCommentTitle(rating.getBody().getCommentTitle());
        setComment(rating.getBody().getCommentValue());

        return SUCCESS;
      }
    });

  }

  /**
   * Gets the URI of the article being rated.
   *
   * @return Returns the articleURI.
   */
  @RequiredStringValidator(message = "Article URI is a required field")
  public String getArticleURI() {
    return articleURI;
  }

  /**
   * Sets the URI of the article being rated.
   *
   * @param articleURI The articleUri to set.
   */
  public void setArticleURI(String articleURI) throws Exception {
    if(articleURI != null && articleURI.equals(this.articleURI)) {
      return;
    }
    this.articleURI = articleURI;
    
    // resolve article type and supported properties
    Article artInfo = fetchArticleService.getArticleInfo(articleURI); 
    assert artInfo != null : "artInfo is null (Should have already been cached.)  Is the articleURI correct?)";
    ArticleType articleType = ArticleType.getKnownArticleTypeForURI(URI.create(articleURI));
    assert articleType != null;
    articleType = ArticleType.getDefaultArticleType();
    for (URI artTypeUri : artInfo.getArticleType()) {
      if (ArticleType.getKnownArticleTypeForURI(artTypeUri)!= null) {
        articleType = ArticleType.getKnownArticleTypeForURI(artTypeUri);
        break;
      }
    }
    isResearchArticle = ArticleType.isResearchArticle(articleType);
  }

  /**
   * @return the isResearchArticle
   */
  public boolean getIsResearchArticle() {
    return isResearchArticle;
  }

  /**
   * Gets the style rating.
   *
   * @return Returns the style.
   */

  //@DoubleRangeFieldValidator(message = "Elegance must be <= 5 and greater than 0", key = "i18n.key", shortCircuit = true, minInclusive = "1.0", maxInclusive = "5.0")
  public double getStyle() {
    return style;
  }

  /**
   * Sets the style rating.
   *
   * @param style The elegance to set.
   */
  public void setStyle(double style) {
    this.style = style;
  }

  /**
   * Gets the insight rating.
   *
   * @return Returns the insight.
   */

  //@DoubleRangeFieldValidator(message = "Insight must be <= 5 and greater than 0", key = "i18n.key", shortCircuit = true, minInclusive = "0.0", maxInclusive = "5.0")
  public double getInsight() {
    return insight;
  }

  /**
   * Sets the insight rating.
   *
   * @param insight The insight to set.
   */
  public void setInsight(double insight) {
    if (log.isDebugEnabled())
      log.debug("setting insight to: " + insight);

    this.insight = insight;
  }

  /**
   * Gets the reliability rating.
   *
   * @return Returns the security.
   */

  //@DoubleRangeFieldValidator(message = "Reliability must be <= 5 and greater than 0", key = "i18n.key", shortCircuit = true, minInclusive = "1.0", maxInclusive = "5.0")
  public double getReliability() {
    return reliability;
  }

  /**
   * Sets the reliability rating.
   *
   * @param reliability The security to set.
   */
  public void setReliability(double reliability) {
    this.reliability = reliability;
  }

  /**
   * Gets the overall rating.
   *
   * @return Returns the overall.
   */
  public double getOverall() {

    return RatingContent.calculateOverall(getInsight(), getReliability(), getStyle());
  }

  /**
   * @return the singleRating
   */
  public double getSingleRating() {
    return singleRating;
  }

  /**
   * @param singleRating the singleRating to set
   */
  public void setSingleRating(double singleRating) {
    this.singleRating = singleRating;
  }

  /**
   * Gets the rating comment.
   *
   * @return Returns the comment.
   */
  public String getComment() {
    return comment;
  }

  /**
   * Sets the ratings comment.
   *
   * @param comment The comment to set.
   */
  public void setComment(String comment) {
    this.comment = comment;
  }

  /**
   * Gets the rating comment title.
   *
   * @return Returns the commentTitle.
   */
  public String getCommentTitle() {
    return commentTitle;
  }

  /**
   * Sets the rating comment title.
   *
   * @param commentTitle The commentTitle to set.
   */
  public void setCommentTitle(String commentTitle) {
    this.commentTitle = commentTitle;
  }

  /**
   * Sets the otm util.
   *
   * @param session The otm session to set.
   */
  @Required
  public void setOtmSession(Session session) {
    this.session = session;
  }

  /**
   * Set the profanityCheckingService.
   *
   * @param profanityCheckingService profanityCheckingService
   */
  public void setProfanityCheckingService(final ProfanityCheckingService profanityCheckingService) {

    this.profanityCheckingService = profanityCheckingService;
  }
}
