package com.shippable.samples.openshift;

import com.shippable.samples.openshift.model.Score;
import com.shippable.samples.openshift.rest.JaxRsActivator;
import com.shippable.samples.openshift.rest.ScoreRestService;
import com.shippable.samples.openshift.util.Resources;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class ScoresTest {
  @Deployment(testable = false)
  public static Archive<?> createTestArchive() {
    return ShrinkWrap.create(WebArchive.class, "test.war")
      .addClasses(Score.class, ScoreRestService.class, JaxRsActivator.class, Resources.class)
      .addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml")
      .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
      .addAsWebInfResource("test-ds.xml", "test-ds.xml");
  }

  @Test
  public void scoresTest(@ArquillianResource URL baseUri) throws IOException, URISyntaxException, ParserConfigurationException, XPathExpressionException, SAXException {
    HttpClient client = new DefaultHttpClient();
    HttpPost post = new HttpPost(baseUri.toURI().resolve("rest/scores/add/1234"));
    HttpResponse response = client.execute(post);
    assertEquals(200, response.getStatusLine().getStatusCode());
    response.getEntity().writeTo(System.out);

    HttpGet get = new HttpGet(baseUri.toURI().resolve("rest/scores"));
    HttpResponse secondResponse = client.execute(get);
    assertEquals(200, secondResponse.getStatusLine().getStatusCode());

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    secondResponse.getEntity().writeTo(out);
    String scores = out.toString("UTF-8");
    // simple, crude check
    assertTrue(scores.contains("1234"));

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.parse(new InputSource(new StringReader(scores)));
    XPathFactory xPathFactory = XPathFactory.newInstance();
    XPath xpath = xPathFactory.newXPath();
    XPathExpression expr = xpath.compile("//score/score/text()");
    NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

    boolean scoreFound = false;
    for (int i = 0; i < nl.getLength(); i++) {
      String content = nl.item(i).getNodeValue();
      if (Integer.parseInt(content) == 1234) {
        scoreFound = true;
        break;
      }
    }
    assertTrue(scoreFound);
  }
}
