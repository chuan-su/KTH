-module(worker).
-export([start/5, stop/1, peers/2]).

start(Name, Logger, Seed, Sleep, Jitter) ->
  spawn_link(fun() -> init(Name, Logger, Seed, Sleep, Jitter) end).

stop(Worker) ->
  Worker ! stop.

init(Name, Log, Seed, Sleep, Jitter) ->
  rand:seed(exsplus, {Seed, Seed, Seed}),
  receive
    {peers, Peers} ->
      Clock = time:clock([johan, paul, ringo, george]),
      loop(Name, Log, Peers, Sleep, Jitter, Clock);
    stop ->
      ok
  end.

peers(Wrk, Peers) ->
  Wrk ! {peers, Peers}.

loop(Name, Log, Peers, Sleep, Jitter, Clock) ->
  Wait = rand:uniform(Sleep),
  receive
    {msg, Time, Msg} ->
      NewClock = time:inc(Name, time:merge(Time, Clock)),
      Log ! {log, Name, NewClock, {received, Msg}},
      loop(Name, Log, Peers, Sleep, Jitter, NewClock);
    stop ->
      ok;
    Error  ->
      Log ! {log, Name, time, {error, Error}}
  after Wait ->
      Selected = select(Peers),
      NewClock = time:inc(Name, Clock),
      Message = {hello, rand:uniform(100)},
      Selected ! {msg, NewClock, Message},
      jitter(Jitter),
      Log ! {log, Name, NewClock, {sending, Message}},
      loop(Name, Log, Peers, Sleep, Jitter, NewClock)
  end.

select(Peers) ->
  lists:nth(rand:uniform(length(Peers)), Peers).

jitter(0) -> ok;
jitter(Jitter) ->  timer:sleep(rand:uniform(Jitter)).
