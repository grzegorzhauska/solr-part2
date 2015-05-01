package controllers

import libs.SearchLib
import libs.solr.scala.SolrClient
import play.api._
import play.api.mvc._

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import play.api.Play.current

object Application extends Controller {

  def index = Action {

    var client = new SolrClient("http://" + play.Play.application().configuration().getString("solr.engine.host")
      + ":" + play.Play.application().configuration().getString("solr.engine.port") +
       play.Play.application().configuration().getString("solr.engine.indexPath") +
        play.Play.application().configuration().getString("solr.engine.collection"))

    val fileLines = io.Source.fromFile("geo.txt").getLines.toList

    fileLines.map {
      line => {
          val data = line.split("\\s")
          //val file = io.Source.fromFile("tikadataset\\" + data(0))
          val file = play.Play.application().classloader().getResource("tikadataset\\" + data(0));


          val file1 = Play.getFile("conf/tikadataset/" + data(0))




        try {
          println("TRYING FILE...")
          val is = new FileInputStream(file1);
          val contenthandler = new BodyContentHandler(10*1024*1024);
          val metadata = new Metadata();
          val pdfparser = new PDFParser();
          pdfparser.parse(is, contenthandler, metadata, new ParseContext());
          //println(contenthandler.toString());
          client
          .add(Map("id"-> data(0).toString(), "text"->contenthandler.toString(), "latitude" -> data(1).toString(), "longitude" -> data(2).toString()))
          .commit
        }
        catch  {

          case e: Exception => {
            println("FILE EXCEPTION!")
            e.printStackTrace();
          }
        }
        finally {
            //if (is != null) is.close();
        }
      }
    }





    Ok(views.html.index(""))
  }

  def get(query: String): Action[AnyContent] = Action { implicit request =>
    //var results = SearchLib.get(query,request)
    //Ok(results)

     var client = new SolrClient("http://" + play.Play.application().configuration().getString("solr.engine.host")
      + ":" + play.Play.application().configuration().getString("solr.engine.port") +
       play.Play.application().configuration().getString("solr.engine.indexPath") +
        play.Play.application().configuration().getString("solr.engine.collection"))


    val documents = client.query(query)
      //.sortBy("id", Order.asc)
      .rows(100).getResultAsMap().documents //return only 100 rows


    val x = documents map {
        doc => {
          //doc.get("id").get + " " + doc.get("latitude").get + " " + doc.get("longitude").get
          //if(doc.get("latitude").get != "dd") {
            doc.get("id").get + " " + doc.get("latitude").get + " " + doc.get("longitude").get
          //}
        }
      }

      Ok(views.html.index(x.mkString(",")))
  }

}