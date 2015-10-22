package io.dmitryivanov.tcpfrontman

import io.netty.channel._

class TcpProxyBackendHandler(inboundChannel: Channel) extends ChannelInboundHandlerAdapter {

  import TcpProxyFrontendHandler._

  override def channelRead(ctx: ChannelHandlerContext, msg: scala.Any): Unit = {
    inboundChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
      override def operationComplete(future: ChannelFuture) {
        if (future.isSuccess) {
          ctx.channel().read()
        } else {
          future.channel().close()
        }
      }
    })
  }

  override def channelInactive(ctx: ChannelHandlerContext): Unit = closeOnFlush(inboundChannel)

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    cause.printStackTrace()
    closeOnFlush(ctx.channel())
  }

  override def channelActive(ctx: ChannelHandlerContext): Unit = ctx.read()
}
