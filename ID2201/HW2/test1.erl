% module for running the program

-module(test1).
-compile(export_all).

start() ->

  Com1 = 'sweden@127.0.0.1',

    % start the routers

    routy:start(stockholm),
    routy:start(lund),
    routy:start(malmo),
    routy:start(lulea),
    routy:start(boras), %% r5
    routy:start(broo),

    timer:sleep(100),

    % connect them to each other

    %add sthlm to malmö
    stockholm ! {add, malmo, {malmo, Com1}},
    timer:sleep(100),
    malmo ! {add, stockholm, {stockholm, Com1}},
    timer:sleep(100),

    %add malmö to borås
    malmo ! {add, boras, {boras, Com1}},
    timer:sleep(100),
    boras ! {add, malmo, {malmo, Com1}},
    timer:sleep(100),

    %add luleå to borås
    boras ! {add, lulea, {lulea, Com1}},
    timer:sleep(100),
    lulea ! {add, boras, {boras, Com1}},
    timer:sleep(100),

    %add lund to luleå
    lund ! {add, lulea, {lulea, Com1}},
    timer:sleep(100),
    lulea ! {add, lund, {lund, Com1}},
    timer:sleep(100),

    %add lund to stockholm),
    lund ! {add, stockholm, {stockholm, Com1}},
    timer:sleep(100),
    stockholm ! {add, lund, {lund, Com1}},
    timer:sleep(100),

    %add lund to broo
    lund ! {add, broo, {broo, Com1}},
    timer:sleep(100),
    broo ! {add, lund, {lund, Com1}},
    timer:sleep(100),

    %add broo to stockholm
    stockholm ! {add, broo, {broo, Com1}},
    timer:sleep(100),
    broo ! {add, stockholm, {stockholm, Com1}},
    timer:sleep(100),

    %broadcast and update

    stockholm ! broadcast,
    timer:sleep(100),
    lund ! broadcast,
    timer:sleep(100),
    malmo ! broadcast,
    timer:sleep(100),
    lulea ! broadcast,
    timer:sleep(100),
    boras ! broadcast,
    timer:sleep(100),
    broo ! broadcast,
    timer:sleep(100),

    stockholm ! update,
    timer:sleep(100),
    lund ! update,
    timer:sleep(100),
    malmo ! update,
    timer:sleep(100),
    lulea ! update,
    timer:sleep(100),
    boras ! update,
    timer:sleep(100),
    broo ! update.

stop() ->
    routy:stop(stockholm),
    routy:stop(lund),
    routy:stop(malmo),
    routy:stop(lulea),
    routy:stop(boras),
    routy:stop(broo).
