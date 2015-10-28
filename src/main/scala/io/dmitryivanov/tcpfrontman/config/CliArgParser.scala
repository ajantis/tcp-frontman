package io.dmitryivanov.tcpfrontman.config

import java.net.URL
import scopt.Read

trait CliArgParser { this: DefaultConfigs =>

  import CliArgParser._

  private[config] def createParser = new scopt.OptionParser[ServerConfig]("tcp-frontman") {
    private[this] def portValidation: Int => Either[String, Unit] = { value =>
      if (1 < value && value < 65535)
        success
      else
        failure("Value <port> must be within 1-65535 range")
    }

    head("tcp-frontman", "0.1")

    opt[Int]('b', "bind-port") valueName "<port>" action { (value, config) =>
      config.copy(bindPort = value)
    } validate { portValidation } text s"Port that the tcp proxy should run on. [Default: $DefaultBindPort]"

    opt[URL]('t', "target") valueName "<host>" action { (value, config) =>
      val port = if (value.getPort != -1) value.getPort else getDefaultPort(value.getProtocol)

      config.copy(remoteHost = value.getHost, remotePort = port)
    } text s"Location of the server the proxy will target. [Default: $DefaultRemoteHost:$DefaultRemotePort]"

    help("help") text "Prints this usage text"
  }

  def parse(args: Array[String]): Option[ServerConfig] = {
    createParser.parse(args, ServerConfig(bindPort = DefaultBindPort, remoteHost = DefaultRemoteHost, remotePort = DefaultRemotePort))
  }

  private def getDefaultPort(schema: String): Int = {
    schema match {
      case "https" => 443
      case _       => 80
    }
  }

  private def isSsl(schema: String): Boolean = schema == "https"
}

object CliArgParser {
  import scopt.Read._
  implicit val urlRead: Read[URL] = reads { value => new URL(value) }
}
