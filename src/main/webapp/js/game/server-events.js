var ServerEvents = function (game) {

    var eshq = new ESHQ("forkpin-game-" + game.id);

    // callback called when the connection is made
    eshq.onopen = function (e) {
        console.log("Open event", game, e);
    };

    // called when a new message with no specific type has been received
    eshq.onmessage = function (e) {
        var gameData = JSON.parse(e.data);
        console.log("game update", game, gameData);
        game.update(gameData);
    };

    // callback called on error
    eshq.onerror = function (e) {
        console.log("Error event", game, e);
    };
};