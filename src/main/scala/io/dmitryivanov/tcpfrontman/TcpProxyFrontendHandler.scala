package io.dmitryivanov.tcpfrontman

import io.netty.bootstrap.Bootstrap
import io.netty.buffer.Unpooled
import io.netty.channel._
import io.netty.channel.socket.nio.NioDatagramChannel
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import io.netty.resolver.dns.{ DnsServerAddresses, DnsNameResolverGroup }

class TcpProxyFrontendHandler(remoteHost: String, remotePort: Int) extends ChannelInboundHandlerAdapter {
  import TcpProxyFrontendHandler._

  private val sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build()

  @volatile
  private var outboundChannel: Channel = _

  override def channelActive(ctx: ChannelHandlerContext): Unit = {

    val inboundChannel = ctx.channel()

    // Start the connection attempt.
    val bootstrap = new Bootstrap()

    bootstrap.group(inboundChannel.eventLoop())
      .channel(ctx.channel().getClass)
      .handler(new TcpProxyBackendHandler(inboundChannel))
      .resolver(new DnsNameResolverGroup(classOf[NioDatagramChannel], DnsServerAddresses.defaultAddresses()))
      //.resolver()
      .option[java.lang.Boolean](ChannelOption.AUTO_READ, false)

    val channelFuture = bootstrap.connect(remoteHost, remotePort)
    outboundChannel = channelFuture.channel()

    val outboundPipeline = outboundChannel.pipeline()

    outboundPipeline.addFirst(sslCtx.newHandler(outboundChannel.alloc(), remoteHost, remotePort))

    channelFuture.addListener(new ChannelFutureListener() {
      @Override
      def operationComplete(channelFuture: ChannelFuture): Unit = {
        if (channelFuture.isSuccess) {
          // connection complete start to read first data
          inboundChannel.read()
        } else {
          // Close the connection if the connection attempt has failed.
          inboundChannel.close()
        }
      }
    })
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    cause.printStackTrace()
    closeOnFlush(ctx.channel())
  }

  override def channelRead(ctx: ChannelHandlerContext, msg: scala.Any): Unit = {
    if (outboundChannel.isActive) {
      outboundChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
        override def operationComplete(future: ChannelFuture): Unit = {
          if (future.isSuccess) {
            // was able to flush out data, start to read the next chunk
            ctx.channel().read()
          } else {
            future.channel().close()
          }
        }
      })
    }
  }

  override def channelInactive(ctx: ChannelHandlerContext) {
    if (outboundChannel != null) {
      closeOnFlush(outboundChannel)
    }
  }
}

object TcpProxyFrontendHandler {

  /**
   * Closes the specified channel after all queued write requests are flushed.
   */
  def closeOnFlush(ch: Channel): Unit = {
    if (ch.isActive) {
      ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE)
    }
  }
}
