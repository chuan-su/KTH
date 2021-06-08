-module(dijkstra).
-export([table/2, route/2]).
-import(map, [update/3, reachable/2, all_node/1]).

entry(Node, Sorted) ->
  case lists:keyfind(Node, 1, Sorted) of
    {_, Length, _} ->
      Length;
    false  ->
      0
  end.

replace(Node, N, Gateway, Sorted) ->
  L = lists:keyreplace(Node, 1, Sorted, {Node, N, Gateway}),
  lists:keysort(2, L).

update(Node, N, Gateway, Sorted) ->
  update(Node, N, entry(Node, Sorted), Gateway, Sorted).


update(Node, N, EN, Gateway, Sorted) when N < EN  ->
  replace(Node, N, Gateway, Sorted);
update(_, _, _, _, Sorted)  ->
  Sorted.

iterate([], _Map, Table) ->
  Table;
iterate([{_, inf, unknown}|_Rest], _Map, Table) ->
  Table;
iterate([{Node, N, Gateway}|Rest], Map, Table) ->
  ReachableNodes =  map:reachable(Node, Map),
  NSorted = lists:foldl(fun(Curr, Prev) -> update(Curr, N + 1, Gateway, Prev) end, Rest, ReachableNodes),
  iterate(NSorted, Map, [{Node, Gateway} | Table]).

table(Gateways, Map) ->
  Nodes = map:all_node(Map),

  UnknownNodes = lists:map(fun(Node) -> {Node, inf, unknown } end, Nodes -- Gateways),
  KnownNodes = lists:map(fun(Gateway) -> {Gateway, 0, Gateway} end, Gateways),

  SortedList = KnownNodes ++ UnknownNodes,
  iterate(SortedList, Map, []).

route(Node, Table) ->
  case lists:keyfind(Node, 1, Table) of
    {_, Gateway} ->
      {ok, Gateway};
    false  ->
      notfound
  end.
