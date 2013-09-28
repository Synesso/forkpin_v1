var chessboard = (function() {

    var config = {
        draggable: true,
        snapbackSpeed: 'fast',
        showErrors: 'true'
    };

    var board = new ChessBoard('chessboard', config);

    var renderProfile = function(profile, div, leftAligned) {
        div.hide();
        div.empty();
        var htmlAvatar = '<img src="' + profile.image.url + '" class="img-circle"/>';
        var htmlName = '<span style="vertical-align: bottom; font-size: 24.5px; font-weight: bold;">' + profile.name.givenName + '</span>';
        if (leftAligned) {
            div.append(htmlAvatar + htmlName);
        } else {
            div.append(htmlName + htmlAvatar);
        }
        div.show('slow');
    };

    return {

        focus: game,

        boardDiv: $('#chessboard'),

        // the game is dissociated from the board and the board is overlayed with "new game" control.
        removeFocus: function() {
            this.focus = null;
            this.boardDiv.children().children().addClass('challengemode');
        },

        focusOn: function(game) {
            this.focus = game;
            this.boardDiv.children().children().removeClass('challengemode');
            $('#challengeOverlay').hide();
            board.position(game.san());
            config.onDragStart = function(source) {
                config.validMovesForPiece = game.validMovesFrom(source);
                return config.validMovesForPiece.length > 0;
            };
            config.onDrop = function(from, to) {
                var result = game.move({from: from, to: to});
                if (result != null) {
                    console.log("This user moved. Next player is ", game.engine.turn());
                    //noinspection JSUnusedGlobalSymbols
                    $.ajax({
                        type: 'POST',
                        url: window.location.origin + '/move',
                        contentType: 'application/x-www-form-urlencoded; charset=utf-8',
                        success: function (result) {
                            console.log('ok posting move', result);
                        },
                        error: function (e) {
                            // todo - here the client has moved, but server doesn't know. rollback/report.
                            console.log('error posting move', e.responseText);
                        },
                        data: {gameId: game.id, from: from.toUpperCase(), to: to.toUpperCase()}
                    });
                    return false;
                }
                return 'snapback';
            };
            config.onSnapEnd = function () {
                if (game.fen !== game.san()) {
                    board.position(game.san());
                }
            };
        },

        update: function(game) {
            if (game.id === this.focus.id) {
                var fen = game.fen;
                var san = fen.substring(0, fen.indexOf(' '));
                if (board.fen() !== san) {
                    board.position(san);
                }
            }
        },

        renderPlayer: function(player) {
            renderProfile(player, $('#selfPanel'), true);
            $.ajax({
                type: 'GET',
                url: window.location.origin + '/people',
                contentType: 'application/octet-stream; charset=utf-8',
                success: function(people) {
                    for (var personIndex in people.items) {
                     person = people.items[personIndex];
                        $('#newGame-friends').append(
                            '<img src="' + person.image.url + '" title="' + person.displayName + '" class="friendAvatar">');
                     }
                },
                error: function(e) {
                    console.log('error getting people list', e);
                }
            });
        },

        renderOpponent: function(player) { renderProfile(player, $('#opponentPanel'), false); }
    }

})();

chessboard.removeFocus();

var gameControls = (function () {
    return {
        loadGamesForUser: function () {
            //noinspection JSUnusedGlobalSymbols
            $.ajax({
                type: 'GET',
                url: window.location.origin + '/games',
                success: function (games) {
                    console.log("Games for current user:", games);
                    if (games.length > 0) {
                        var firstGame = game(games[0]);
                        chessboard.focusOn(firstGame);
                        loginMod.profile(firstGame.opponentId(), chessboard.renderOpponent);
                    }
                },
                error: function (e) {
                    console.log('error loading games', e);
                }
            })
        }
    }
})();


var game = function(meta, existingEngine) {

    var engine = existingEngine || (function() {
        var e = new Chess(meta.fen);
        e.san = function() {
            var fen = e.fen();
            return fen.substring(0, fen.indexOf(' '));
        };
        e.update = function(game) {
            e.move(game.moves[0]);
        };
        return e;
    })();

    return {

        id: meta.id,

        engine: engine,

        playerColour: function() {
            if (loginMod.player.id == meta.white) {
                return "w";
            } else if (loginMod.player.id == meta.black) {
                return "b";
            }
            return undefined;
        },

        opponentId: function() {
            if (loginMod.player.id == meta.white) {
                return meta.black;
            } else if (loginMod.player.id == meta.black) {
                return meta.white;
            }
            return undefined;
        },

        validMovesFrom: function(source) {
            if (this.engine.turn() == this.playerColour()) {
                var moves = this.engine.moves({square: source, verbose: true});
                return $.map(moves, function (m) { return m.to });
            } else {
                return [];
            }
        },

        move: function(from, to) {
            return this.engine.move(from, to);
        },

        san: engine.san,

        eventSource: (function() {
            var eshq = new ESHQ("forkpin-game-" + meta.id);
            eshq.onmessage = function (e) {
                var gameData = JSON.parse(e.data);
                console.log("game update", gameData);
                engine.update(gameData);
                chessboard.update(gameData);
            };
            return eshq;
        })()

    };
};