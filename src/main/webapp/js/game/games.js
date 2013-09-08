var gameControls = (function () {

    return {



        games: {}, // gameId -> {id, board, chessJsGame: Chess}

        focus: {
            gameId: undefined,
            board: undefined,
            game: undefined,
            validDestinations: []
        },

        loadGamesForUser: function () {
            $.ajax({
                type: 'GET',
                url: window.location.origin + '/games',
                success: function (games) {
                    console.log('games loaded', games);
                },
                error: function (e) {
                    console.log('error loading games', e.error);
                }
            })
        },

        issueChallenge: function () {
            $.ajax({
                type: 'POST',
                url: window.location.origin + '/challenge',
                contentType: 'application/x-www-form-urlencoded; charset=utf-8',
                success: function (result) {
                    if (result.hasOwnProperty('fen')) {
                        gameControls.loadGame(result);
                    } else {
                        console.log('challenge created', result);
                    }
                },
                error: function (e) {
                    console.log('error issuing challenge', e.error);
                }
            })
        },

        loadGame: function (gameFromServer) {
            console.log('loading game', gameFromServer);
            var game = new Chess(gameFromServer.fen);
            game.san = function () {
                var fen = this.fen();
                return fen.substring(0, fen.indexOf(' '));
            };
            this.games[gameFromServer.id] = {meta: gameFromServer, game: game};
            $('#chessboard').fadeTo('slow', 1.0);
            this.focus.board.position(game.fen());
            this.focus.game = game;
            this.focus.gameId = gameFromServer.id;
        },

        isUsersTurn: function () {
            return (this.focus.game.turn() == 'w' && this.games[this.focus.gameId].meta.white == loginMod.player.id) ||
                (this.focus.game.turn() == 'b' && this.games[this.focus.gameId].meta.black == loginMod.player.id);
        },

        event: {

            dragStart: function (source) {
                if (!gameControls.isUsersTurn()) return false;
                var moves = this.focus.game.moves({square: source});
                var toCoordinates = function (s) {
                    return s.substring(s.length - 2);
                };
                this.focus.validDestinations = $.map(moves, toCoordinates);
                return this.focus.validDestinations.length > 0;
            },

            drop: function (from, to) {
                var result = this.focus.game.move({from: from, to: to});
                var validMove = result != null;
                if (validMove) {
                    $.ajax({
                        type: 'POST',
                        url: window.location.origin + '/move',
                        contentType: 'application/x-www-form-urlencoded; charset=utf-8',
                        success: function (result) {
                            console.log('ok posting move', result);
                        },
                        error: function (e) {
                            console.log('error posting move', e);
                        },
                        data: {gameId: this.focus.gameId, from: from.toUpperCase(), to: to.toUpperCase()}
                    });
                    return false;
                }
                return 'snapback';
            },

            snapEnd: function () {
                if (this.focus.board.fen() !== this.focus.game.san()) {
                    this.focus.board.position(this.focus.game.san());
                }
            }

        }
    };
})();

gameControls.focus.board = new ChessBoard('chessboard', {
    draggable: true,
    snapbackSpeed: 'fast',
    onDragStart: gameControls.event.dragStart,
    onDrop: gameControls.event.drop,
    onSnapEnd: gameControls.event.snapEnd,
    focus: gameControls.focus,
    showErrors: 'true'
});

$(document).ready(function () {
    $('#chessboard').fadeTo('slow', 0.25);
    $('#playButton').click(gameControls.issueChallenge);
});

