# Java-dictionary.bot.Bot

A shell client build for Drawers. Now monitor your
machines without any pain of SSH. Works well on slow network :)

```
How to make it work:
1. Get your developer key and secret from Drawers and update it in Bot.java
2. Deploy it on your server. It is a simple maven project, just create a fatjar.
3. Find it by your key in the contacts window of your Drawer app. Start chatting.
```


It currently only replies messages with size < 4kb, chunking is in plan.
Doesn't work on commands with sudo, supporting it is in plan.
It is using Babbler XMPP client to connect to Drawers. System requirement involves Java-8.
We love and use RX-Java to dispatch the messages - http://blog.danlew.net/2014/09/15/grokking-rxjava-part-1/

