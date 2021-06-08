-module(stress_test).
-import(test, [request/2]).
-export([bench/3]).
-export([init/1]).

%% P = number of parallel requests
bench(Host, Port, P) ->
  register(benchmark, spawn(fun() -> init(P) end)),
  run_test(Host, Port, P).

init(P) ->
  Start = erlang:system_time(micro_seconds),
  wait(Start, [], P).

%% wait for all tests to be finished.
wait(Start,Pids, P) ->
  receive
    {done, Pid} ->
      FT = Pids ++ [Pid], %% collect finished tests.
      check(Start, FT, P), %% check if all tests have finished.
      wait(Start, FT, P);
    {stop} ->
      ok
  end.

%% check all test processes have finished running.
check(Start, Pids, P) when length(Pids) == P -> finish(Start);
check(_,_,_) -> ok.

finish(Start) ->
  Finish = erlang:system_time(micro_seconds),
  io:format("test result: ~w~n", [Finish-Start]),
  exit(whereis(benchmark),"finished stress tests").

run_test(_Host, _Port, 0) -> ok;
run_test(Host, Port, P) ->
  spawn(fun() -> test:request(Host, Port), benchmark ! { done, self() } end), %% run each test in parallel and report on complete.
  run_test(Host, Port, P - 1).
