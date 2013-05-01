oidrelay
========

A Java servlet to act as a public relay for OpenID to Relying Parties behind firewalls

> A server which is behind a firewall or NAT may wish to authenticate a user via OpenID.
Without leveraging another server publicly accessible on the internet, this is not possible.
A simple relay service running on a trusted server enables this use-case.

## Specification

A first-cut of the specification [is available](http://nwilson.github.com/oidrelay). All
comments gladly received.

## Code

The code in this project shall implement the Relay Server described in the specification.
The client-side (Indirect Relying Party) is to be implemented separately in a product
which is not product. The Relay Server however can be happily tested with `curl`.

## Status

1. ~~Learn Java~~
2. ~~Implement main servlet~~
3. Integrate the OpenID bit
