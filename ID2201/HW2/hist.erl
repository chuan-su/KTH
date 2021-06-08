-module(hist).
-export([new/1, update/3]).

new(Name) ->
  [{Name, inf}].

update(Node, N, History) ->
  case lists:keyfind(Node, 1, History) of
    { _, Counter } when Counter < N -> {new, lists:keyreplace(Node, 1, History, {Node, N})};
    { _, _Counter } -> old;
    false -> {new, [{Node, N} | History]}
end.
