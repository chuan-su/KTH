-module(loggera).
-export([start/1, stop/1]).

start(Nodes) ->
  spawn_link(fun() -> init(Nodes) end).

stop(Logger) ->
  Logger ! stop.

init(Nodes) ->
  MsgQueue = mq:new(),
  Clock = time:clock(Nodes), %% Counter for each nodes
  loop(Clock, MsgQueue).

loop(Clock, Mq) ->
  receive
    {log, From, Vector, Msg} ->
      NewMq = mq:offer({From, Vector, Msg}, Mq), %% put msg into queue
      NewClock = time:update(From, Vector, Clock), %% update counter for corresponding Node.

      {SafeMsg, Rest} = mq:partition(fun({_, V, _}) -> time:safe(V, NewClock) end, NewMq),

      mq:iterate(fun({F,T,M}) -> log(F, T, M) end, mq:sorted(SafeMsg)),

      loop(NewClock, Rest);
    stop ->
      ok
  end.

log(From, Time, Msg) ->
  io:format("log: ~w ~w ~p ~n", [Time, From, Msg]).
