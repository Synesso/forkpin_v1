/* An object that represents the chess board and player profile bar on the page.
   It can focus on a particular game, or show the challenge button, or a splash screen. */
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

    var nextPageToken = '';

    var friendHtml = function(person, i) {
        var el = $('<div class="frRw"><div class="frAvCl">' +
            '<img src="' + person.image.url + '" title="' + person.displayName + '" class="frAv"/>' +
            '</div><div class="frNm">' + person.displayName + '</div><div class="frCh">' +
            '<a id="frCr' + i + '" href="javascript:void(0);" class="frCr btn">Challenge</a></div></div>');
        var link = el.find('#frCr' + i);
        link.click(function(){
            gameControls.issueChallenge(person.id, link);
            link.attr('disabled', true);
            link.text('');
            link.append($('<img src="img/ajax-loader.gif" height="15" width="15"/>'));
            link.addClass('noClicky');
        });
        return el;
    };

    var moreFriends = function(pageToken) {
        nextPageToken = undefined;
        $('#moreFriendsSpinner').show();
        $.ajax({
            type: 'GET',
            url: window.location.origin + '/people',
            contentType: 'application/octet-stream; charset=utf-8',
            success: function(people) {
                $('#moreFriendsSpinner').hide();
                for (var personIndex in people.items) {
                    var person = people.items[personIndex];
                    $('#newGame-friends').append(friendHtml(person, personIndex));
                }
                nextPageToken = people.nextPageToken;
            },
            error: function(e) {
                $('#moreFriendsSpinner').hide();
                console.log('error getting people list', e);
            },
            data: {pageToken: pageToken}
        });
    };

    $("#newGame-loggedIn").scroll(function() {
        if ($(this)[0].scrollHeight - $(this).scrollTop() == $(this).outerHeight()) {
            if (nextPageToken == undefined) { return; }
            moreFriends(nextPageToken);
        }
    });

    $('#chEmBtn').click(function() {
        var form = document.forms['chEmFrm'];
        var emailAddress = $("#chEmTxt").val();
        if (form.elements['email'].checkValidity()) {
            $('#chEmBtn').attr('disabled', true).addClass('noClicky');
            $.ajax({
                type: 'POST',
                url: window.location.origin + '/challenge/email',
                contentType: 'application/x-www-form-urlencoded; charset=utf-8',
                success: function (result) {
                    $('#chEmHlp').text('Challenged ' + emailAddress + '. Challenge someone else?');
                    $("#chEmTxt").val('');
                    $('#chEmBtn').attr('disabled', false).removeClass('noClicky');
                    console.log('ok issuing email challenge to', emailAddress, result);
                },
                error: function (e) {
                    $('#chEmHlp').text('Error challenging ' + emailAddress + '. Sorry!');
                    $('#chEmBtn').attr('disabled', false).removeClass('noClicky');
                    console.error('error challenging user', emailAddress, e.responseText, e);
                },
                always: function () {
                },
                data: $('#chEmFrm').serialize()
            });
        } else {
            console.log("todo - how to show field is not valid?");
        }
    });

    $('#chEmTxt').keypress(function (event) {
        if (event.which == 13) {
            $("#chEmBtn").click();
            return false;
        }
        return true;
    });

    return {

        focus: game,

        boardDiv: $('#chessboard'),

        alert: function(heading, message) {
            $('#alertModalHeader').empty().text(heading);
            $('#alertModalBody').empty().text(message);
            $('#alertModal').modal('show');
        },

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
            // moreFriends(''); // this loads people followed from g+.
        },

        renderOpponent: function(player) { renderProfile(player, $('#opponentPanel'), false); }
    }

})();

/* A collection of functions that fetch information from the server (and other resources) and
   triggers events on the chessboard object as a result. */
var gameControls = (function () {

    var waitModeOn = function(button) {
        button.attr('disabled', true).addClass("noClicky");
    };

    var waitModeOff = function(button) {
        button.attr('disabled', false).removeClass("noClicky");
    };

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
        },

        loadChallenge: function(id, key) {
            $.ajax({
                type: 'GET',
                url: window.location.origin + '/challenge/' + id,
                contentType: 'application/octet-stream; charset=utf-8',
                success: function (challenge) {
                    /* Accept the challenge presented in the modal box #acceptChallengeModal */
                    $("#accChYes").click(function() {
                        waitModeOn($("#accChYes"));
                        $.ajax({
                            type: 'POST',
                            url: window.location.origin + '/challenge/accept',
                            contentType: 'application/x-www-form-urlencoded; charset=utf-8',
                            success: function(accepted) {
                                console.log('accepted challenge', accepted);
                                waitModeOff($("#accChYes"));
                                $('#acceptChallengeModal').modal('hide');
                                if (accepted.action === 'onLoginActionCreated') {
                                    chessboard.alert("Success!", "Now log in to see your game.");
                                } else {
                                    chessboard.focusOn(accepted.game);
                                }
                            },
                            error: function(e) {
                                console.log('error accepting challenge', e);
                            },
                            data: {key: key, id: id}
                        });
                    });
                    $('#challengerProfilePic').attr("src", challenge.challenger.profilePictureUrl);
                    $('#challengerName').append(challenge.challenger.displayName);
                    $('#acceptChallengeModal').modal('show');
                }, error: function (e) {
                    console.error('Unable to load challenge', id, e);
                },
                data: {key: key}
            });
        },

        issueChallenge: function(gPlusId, button) {
            $.ajax({
                type: 'POST',
                url: window.location.origin + '/challenge',
                contentType: 'application/x-www-form-urlencoded; charset=utf-8',
                success: function (challenge) {
                    console.log("Challenge issued:", challenge);
                    button.addClass('btn-success');
                    button.text('Issued');
                },
                error: function (e) {
                    console.log('error loading games', e);
                },
                data: {gPlusId: gPlusId}
            })
        }
    }
})();

/* returns an object that encapsulates:
   1. a chess.js game instance,
   2. an eshq channel
   3. some additional functions

   meta is structured as per Game.scala#forClient
   existingEngine is the chess.js game instance. If not present a new one in created.
*/
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

$(document).ready(function () {
    if (displayAction.view === "game") {
        console.log("game", displayAction.id);
    } else if (displayAction.view === "challenge") {
        gameControls.loadChallenge(displayAction.id, displayAction.key);
    }
});
