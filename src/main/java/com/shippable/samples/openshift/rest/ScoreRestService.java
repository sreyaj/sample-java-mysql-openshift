package com.shippable.samples.openshift.rest;

import java.util.List;

import javax.ejb.Stateless;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.*;

import com.shippable.samples.openshift.model.Score;

@Path("/scores")
@Stateless
public class ScoreRestService {
  @Inject
  private EntityManager em;

  @GET
  @Produces("text/xml")
  public List<Score> listScores() {
    return em.createQuery("select s from Score s order by s.timestamp").getResultList();
  }

  @POST
  @Path("/add/{score:[0-9][0-9]*}")
  @Produces("text/xml")
  public Score addScore(@PathParam("score") int score) {
    Score newScore = new Score();
    newScore.setScore(score);
    em.persist(newScore);
    return newScore;
  }
}
