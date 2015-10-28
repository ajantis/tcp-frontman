package io.dmitryivanov.tcpfrontman

import io.dmitryivanov.tcpfrontman.config.{ ServerConfig, DefaultConfigs, CliArgParser }
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.logging.{ LogLevel, LoggingHandler }

object Boot extends App with CliArgParser with DefaultConfigs {

  // TODO get it from application.conf
  val DefaultBindPort = 8080
  val DefaultRemoteHost = "localhost"
  val DefaultRemotePort = 80

  parse(args) match {
    case Some(config) => startServer(config)
    case None =>
      // error msg will be displayed automatically
      // but we want to send a non-zero exit code to be shell-friendly
      System.exit(-1)
  }

  private def startServer(config: ServerConfig): Unit = {
    val bossGroup = new NioEventLoopGroup(1)
    val workerGroup = new NioEventLoopGroup()

    try {
      val bootstrap = new ServerBootstrap()

      bootstrap.group(bossGroup, workerGroup)
        .channel(classOf[NioServerSocketChannel])
        .handler(new LoggingHandler(LogLevel.INFO))
        .childHandler(new TcpProxyInitializer(config.remoteHost, config.remotePort))
        .childOption[java.lang.Boolean](ChannelOption.AUTO_READ, false)
        .bind(config.bindPort).sync().channel().closeFuture().sync()

    } finally {
      bossGroup.shutdownGracefully()
      workerGroup.shutdownGracefully()
    }
  }
}
