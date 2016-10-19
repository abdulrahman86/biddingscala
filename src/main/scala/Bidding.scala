import java.io.{FileNotFoundException, InputStream}

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

object Bidding
{
  def main(args: Array[String]) {

    val start = System.currentTimeMillis()

    val campaignsJSON = Option(getClass.getResourceAsStream("campaigns.json")).map(scala.io.Source.fromInputStream)
      .map(_.getLines.toList)
      .getOrElse(throw new FileNotFoundException("campaigns.json"))

    val bidRequestsJSON = Option(getClass.getResourceAsStream("bid_requests.json")).map(scala.io.Source.fromInputStream)
      .map(_.getLines.toList)
      .getOrElse(throw new FileNotFoundException("bid_requests.json"))

    val mapper: ObjectMapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)

    val requests: Array[BidRequest] = mapper.readValue(bidRequestsJSON.head, classOf[Array[BidRequest]])
    val campaignArray: Array[Campaign] = mapper.readValue(campaignsJSON.head, classOf[Array[Campaign]])

    val matchedValues: Array[MatchValue] = for {
      bidRequest <- requests
      campaign <- campaignArray
      if(bidRequest.country == campaign.targetedCountry && campaign.getDimensions.contains(bidRequest.getDimensions))
    } yield MatchValue(bidRequest requestId, campaign campaignId)

    println(mapper.writeValueAsString(ProgramOutput(matchedValues, System.currentTimeMillis - start)))

  }

  case class BidRequest(requestId: String, pageUrl: String, country: String, dimensions: String) {

    def getDimensions: (Int, Int)= {

      val dimensionArray: Array[String] = dimensions.split("x")
      (dimensionArray(0).toInt, dimensionArray(1).toInt)
    }
  }

  case class Campaign(campaignId: String, targetedCountry: String, targetedDomain: String, adDimensions: Array[String]){

    def getDimensions: List[(Int, Int)] = {

      val dimensionArray: Array[Array[String]] = adDimensions.map(_ split ("x"))
      dimensionArray collect{case Array(x: String, y: String, _*) => (x.toInt,y.toInt)} toList
    }
  }

  case class MatchValue(bidRequestId: String, campaignId: String)

  case class ProgramOutput(results: Array[MatchValue], evaluationTimeInMillis: Long)

}

