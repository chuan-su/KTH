-module(test).
-export([start_n1/0, start_n2/0, add/3, lookup/2, start/2, add_pair/2, evaluate/2]).

start_n1() ->
  Pid = node:start(1, nil),
  node:start(2, Pid),
  node:start(3, Pid),
  node:start(4, Pid),
  node:start(5, Pid),
  Pid.

start_n2() ->
  Pid = node2:start(1, nil),
  node2:start(2, Pid),
  node2:start(3, Pid),
  node2:start(4, Pid),
  node2:start(5, Pid),
  Pid.


add(Key, Value, Node) ->
  Qref = make_ref(),
  Client = self(),
  Node ! {add, Key, Value, Qref, Client},
  receive
    {Qref, ok} ->
      ok
  after 3000 ->
      io:format("Node not replying to message {add ~w,~w,~w,~w}~n", [Key, Value, Qref, Client])
  end.

lookup(Key, Node) ->
  Qref = make_ref(),
  Client = self(),
  Node ! {lookup, Key, Qref, Client},
  receive
    {Qref, Result} ->
      io:format("Received lookup result for key {~w}~n", [Key]),
      Result
  after 3000 ->
      io:format("Node not replying to message {lookup ~w,~w,~w}", [Key, Qref, Client])
  end.

start(0, Pid) -> Pid;
start(N, Pid) ->
  NewPid = node2:start(key:generate(), Pid),
  start(N-1, NewPid).

add_pair(N, Node) ->
  Pairs = lists:map(fun(_) -> {key:generate(), key:generate()} end, lists:seq(1,N)),
  lists:foreach(fun({K, V}) -> spawn(fun() -> add(K, V, Node) end) end, Pairs),
  Pairs.

%%  N: Number of elemements to add,
%% P: node id
evaluate(N, P) ->
  L = add_pair(N, P),
  Start = erlang:system_time(micro_seconds),
  lists:foreach(fun({K, _V}) -> lookup(K, P) end, L),
  End = erlang:system_time(micro_seconds),
  io:format("Time took for lookup ~w elements is: ~w ms ~n", [N, (End - Start) div 1000]).
