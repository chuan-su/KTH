-module(map).
-export([new/0, update/3, reachable/2, all_node/1]).

new() ->
  [].

update(Node, Links, Map) ->
  %% keystore is used to upsert a tuple to a tuple list.
  lists:keystore(Node, 1, Map, {Node, Links}).

reachable(Node, Map) ->
  case lists:keyfind(Node, 1, Map) of
    false ->
      [];
    {_, Links} ->
      Links
  end.

all_node(Map) ->
  L = lists:foldl(fun({Node, Links}, Prev) -> lists:flatten([Node,Links]) ++ Prev end, [], Map),
  S = sets:from_list(L),
  sets:to_list(S).
