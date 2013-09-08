    var serverEvents = (function () {

        var eshq = new ESHQ("forkpin");

        // callback called when the connection is made
        eshq.onopen = function(e) {
            console.log("Open event", e);
        };

        // called when a new message with no specific type has been received
        eshq.onmessage = function(e) {
            var gameData = JSON.parse(e.data);
            console.log("game update", gameData);
            // todo - replace below with gameControls.update(game);
            var game = gameControls.games[gameData.id];
            game.load(gameData.fen());
            gameControls.focus.board.position(gameData.fen());
            gameControls.games[gameData.id] = {meta: gameData, game: game};
        };

        // callback called on error
        eshq.onerror = function(e) {
            console.log("Error event", e);
        };

        return eshq; // todo - if it's useful. Otherwise???

    })();