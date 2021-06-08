-module(time).
-export([zero/0, inc/2, merge/2, leq/2, clock/1, update/3, safe/2]).

zero() ->
  0.

inc(Name, V) ->
  lists:map(fun({Node, Time}) ->
                case Node of
                  Name -> {Node, Time + 1};
                  _ -> {Node, Time}
                end
            end, V).

%% When a node receives a vector Vi from remote node.
%% It merge its own vector by setting V[j] = max(V[j], V[i]), for j = 1,2...,N.
merge(Vi, Vj) ->
  lists:zipwith(fun({Pi,Ti}, {_Pj, Tj}) -> {Pi, max(Ti, Tj)}  end, Vi, Vj).

%% V <= V' if V[j] <= V'[j] for j = 1,2...,N
leq(Vi, Vj) ->
  V = lists:zipwith(fun({P, Ti}, {P, Tj}) -> {P, Ti =< Tj} end, Vi, Vj),
  lists:all(fun({_, Leq}) -> Leq end, V).

%% A vector clock.
clock(Nodes) ->
  lists:map(fun(Node) -> {Node, zero()} end, Nodes).

update(Node, Vector, Clock) ->
  NodeClock = lists:keyfind(Node, 1, Vector), %% find the time counter from the vector node send to us.
  %% update our clock with this node's time counter and keep other nodes counter unchanged.
  lists:keyreplace(Node, 1, Clock, NodeClock).

safe(Vi, Vj) ->
  leq(Vi,Vj).
