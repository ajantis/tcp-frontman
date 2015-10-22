package io.dmitryivanov.tcpfrontman

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.logging.{ LogLevel, LoggingHandler }

object Boot extends App {

  val BindPort = 8090

  val remoteHost = "localhost"
  val remotePort = 443

  val bossGroup = new NioEventLoopGroup(1)
  val workerGroup = new NioEventLoopGroup()

  try {
    val bootstrap = new ServerBootstrap()

    bootstrap.group(bossGroup, workerGroup)
      .channel(classOf[NioServerSocketChannel])
      .handler(new LoggingHandler(LogLevel.INFO))
      .childHandler(new TcpProxyInitializer(remoteHost, remotePort))
      .childOption[java.lang.Boolean](ChannelOption.AUTO_READ, false)
      .bind(BindPort).sync().channel().closeFuture().sync()

  } finally {
    bossGroup.shutdownGracefully()
    workerGroup.shutdownGracefully()
  }
}
