package kubernetes

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.core.Predef.Simulation

import scala.concurrent.duration.DurationInt
class AKS extends Simulation{

  def hostName: String = getProperty("HOST_NAME", "http://localhost:8090/").toString

  def userCount: Int = getProperty("USERS", "10").toInt

  def rampDuration: Int = getProperty("RAMP_DURATION", "10").toInt

  def testDuration: Int = getProperty("DURATION", "60").toInt

  def apiPath: String = "loadtest/1"

  private def getProperty(propertyName: String, defaultValue: String) = {
    Option(System.getenv(propertyName))
      .orElse(Option(System.getProperty(propertyName)))
      .getOrElse(defaultValue)
  }

  val httpProtocol = http
    .baseUrl(hostName)
    .header("Accept", "application/json")

  before {
    println(s"Running our test with ${userCount} users")
    println(s"Ramping users over ${rampDuration} seconds")
    println(s"Total Test duration: ${testDuration} seconds")
  }

  def kubernetesLoadTest() = {
    forever() {
      exec(
        http("Kubernetes Load Test")
          .get(apiPath)
          .check(status.is(200)))
    }
  }

  val scn = scenario("Get Latest Offers")
    .forever() {
      exec(kubernetesLoadTest())
        .pause(2)
    }

  setUp(
    scn.inject(
      nothingFor(5 seconds),
      //rampUsers(userCount) during (rampDuration seconds))
      //constantUsersPerSec(userCount) during(rampDuration seconds)
      rampUsersPerSec(50) to(userCount) during(rampDuration)
    )).protocols(httpProtocol)
    .maxDuration(testDuration seconds)
    .assertions(
      global.responseTime.max.lt(30000),
      global.successfulRequests.percent.gt(95))

  after {
    println("Kubernetes test completed")
  }
}
