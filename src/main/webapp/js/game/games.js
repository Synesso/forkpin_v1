var gameControls = (function () {

    return {

        games: {}, // gameId -> Game

        board: new ChessBoard('chessboard', {
            draggable: true,
            snapbackSpeed: 'fast',
            game: undefined,
            validMovesForPiece: [],

            focusOn: function(game) {
                this.game = game;
                this.position(game.fen);
            },

            onDragStart: function (source) {
                if (!this.game.playerUp) return false;
                var moves = this.game.moves({square: source});
                var toCoordinates = function (s) {
                    return s.substring(s.length - 2);
                };
                this.validMovesForPiece = $.map(moves, toCoordinates);
                return this.validMovesForPiece.length > 0;
            },

            onDrop: function (from, to) {
                var result = this.game.move({from: from, to: to});
                if (result != null) {
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
                        data: {gameId: this.game.id, from: from.toUpperCase(), to: to.toUpperCase()}
                    });
                    return false;
                }
                return 'snapback';
            },

            onSnapEnd: function () {
                if (this.fen() !== this.game.san()) {
                    this.position(this.game.san());
                }
            },

            showErrors: 'true'
        }),

        loadGamesForUser: function () {
            $.ajax({
                type: 'GET',
                url: window.location.origin + '/games',
                success: function (games) {
                    console.log("Games for current user:", games);
                    if (games.length > 0) {
                        var game = new Game(games[0]);
                        gameControls.games[game.id] = game;
                    }
                },
                error: function (e) {
                    console.log('error loading games', e);
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
                        var game = new Game(games[0]);
                        gameControls.games[game.id] = game;
                    } else {
                        console.log('challenge created', result);
                    }
                },
                error: function (e) {
                    console.log('error issuing challenge', e.error);
                }
            })
        }
    };
})();

$(document).ready(function () {
    $('#chessboard').fadeTo('slow', 0.25);
    $('#playButton').click(gameControls.issueChallenge);
});

function Game(meta) {
    this.id = meta.id;
    this.black = meta.black;
    this.white = meta.white;
    this.activeColour = meta.activeColour;
    this.fen = meta.fen;
    this.san = this.fen.substring(0, this.fen.indexOf(' '));
    this.moves = meta.moves;
    this.game = new Chess(this.fen);
    this.playerUp = this.activeColour == game.turn();
    this.update = function (meta) {
        this.activeColour = meta.activeColour;
        this.playerUp = this.activeColour == game.turn();
        this.fen = meta.fen;
        this.san = this.fen.substring(0, this.fen.indexOf(' '));
        this.moves = meta.moves;
        var lastMove = this.moves.slice(this.moves.length - 1);
        this.game.move(lastMove.from, lastMove.to);
        this.board.position(this.fen);
    };
    $('#chessboard').fadeTo('slow', 1.0);
    new ServerEvents(this);
    gameControls.board.focusOn(this);
}