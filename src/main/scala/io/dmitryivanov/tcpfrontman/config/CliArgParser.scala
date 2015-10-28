package io.dmitryivanov.tcpfrontman.config

trait CliArgParser { this: DefaultConfigs =>

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

    opt[String]('h', "target-host") valueName "<host>" action { (value, config) =>
      config.copy(remoteHost = value)
    } text s"Host or IP address of the server the proxy will target. [Default: $DefaultRemoteHost]"

    opt[Int]('p', "target-port") valueName "<port>" action { (value, config) =>
      config.copy(remotePort = value)
    } validate { portValidation } text s"Port of the server the proxy will target. [Default: $DefaultRemotePort]"

    help("help") text "Prints this usage text"
  }

  def parse(args: Array[String]): Option[ServerConfig] = {
    createParser.parse(args, ServerConfig(bindPort = DefaultBindPort, remoteHost = DefaultRemoteHost, remotePort = DefaultRemotePort))
  }
}
