-module(mq).
-export([new/0, offer/2, partition/2, sorted/1, iterate/2, len/1]).

new() -> [].

offer(Msg, Q) -> [Msg | Q].

partition(F, Q) -> lists:partition(F, Q).

iterate(F,Q) -> lists:foreach(F, Q).

sorted(Q) -> lists:keysort(2, Q).

len(Q) -> length(Q).
