package com.twitter.finagle.channel

import org.jboss.netty.channel._

class BrokeredChannelSink extends AbstractChannelSink {
  override def eventSunk(p: ChannelPipeline, e: ChannelEvent) {
    e match {
      case e: ChannelStateEvent =>
        handleChannelStateEvent(p, e)
      case e: MessageEvent =>
        handleMessageEvent(p, e)
    }
  }

  private def handleChannelStateEvent(p: ChannelPipeline, e: ChannelStateEvent) {
    val ch = e.getChannel.asInstanceOf[BrokeredChannel]
    val value = e.getValue
    val future = e.getFuture

    e.getState match {
      case ChannelState.OPEN =>
        if (java.lang.Boolean.FALSE eq value)
          ch.realClose(future)
      case ChannelState.BOUND =>
        if (value ne null) {
          future.setSuccess()
          Channels.fireChannelBound(ch, value.asInstanceOf[Broker])
        } else {
          ch.realClose(future)
        }
      case ChannelState.CONNECTED =>
        if (value ne null)
          ch.realConnect(value.asInstanceOf[Broker], future)
        else
          ch.realClose(future)
      case ChannelState.INTEREST_OPS =>
        future.setSuccess()
    }
  }

  private def handleMessageEvent(p: ChannelPipeline, e: MessageEvent) {
    val ch = e.getChannel.asInstanceOf[BrokeredChannel]
    ch.realWrite(e)
  }

}