var loginMod = (function () {

    var authResult = undefined;

    var divs = {
        'userName': $('#userName'),
        'selfPanel': $('#selfPanel'),
        'opponentPanel': $('#opponentPanel')
    };

    return {

        player: undefined,

        /**
         * Hides the sign-in button and connects the server-side app after
         * the user successfully signs in.
         *
         * @param {Object} authResult An Object which contains the access token and
         *   other authentication information.
         */
        onSignInCallback: function (authResult) {
            if (authResult['access_token']) {
                // The user is signed in
                this.authResult = authResult;
                gapi.client.load('plus', 'v1', this.renderProfile);
                $('#gDisconnect').show();
                $('#newGame-loggedIn').show();
                $('#newGame-notLoggedIn').hide();
            } else if (authResult['error']) {
                // The user is not signed in.
                $('#gConnect').show();
                $('#newGame-loggedIn').hide();
                $('#newGame-notLoggedIn').show();
            }
        },

        renderProfile: function () {
            var request = gapi.client.plus.people.get({'userId': 'me'});
            request.execute(function (profile) {
                loginMod.player = profile;
                divs.userName.empty();
                divs.selfPanel.empty();
                if (profile.error) {
                    divs.selfPanel.append(profile.error);
                    return;
                }
                loginMod.connectServer(profile);
                divs.userName.append(profile.displayName);
                $('#gConnect').hide();
                $('#userMenu').show();
            });
        },

        /**
         * Calls the server endpoint to disconnect the app for the user.
         */
        disconnectServer: function () {
            // Revoke the server tokens
            console.log(window.location.origin + '/disconnect');
            $.ajax({
                type: 'POST',
                url: window.location.origin + '/disconnect',
                async: false,
                success: function (result) {
                    $('#visiblePeople').empty();
                    $('#gConnect').show();
                    $('#userMenu').hide();
                },
                error: function (e) {
                    console.log(e);
                }
            });
        },

        connectServer: function (profile) {
            var gplusId = profile.id;
            var url = window.location.origin + '/connect?state=' + gplusOneTimeToken + '&gplus_id=' + gplusId;
            console.log('connectServer -> ', url);
            $.ajax({
                type: 'POST',
                url: url,
                contentType: 'application/octet-stream; charset=utf-8',
                success: function (result) {
                    gameControls.loadGamesForUser();
                    chessboard.renderPlayer(profile);
                },
                error: function (e) {
                    console.log('error connecting:', e.status, e.statusText);
                },
                processData: false,
                data: this.authResult.code
            });
        },

        profile: function(gPlusId, callback) {
            $.ajax({
                type: 'GET',
                url: window.location.origin + '/profile/' + gPlusId,
                contentType: 'application/octet-stream; charset=utf-8',
                success: callback,
                error: function(e) {
                    console.log('error getting profile', gPlusId, e);
                }
            });
        }
    };
})();

/**
 * Calls the helper method that handles the authentication flow.
 *
 * @param {Object} authResult An Object which contains the access token and
 *   other authentication information.
 */
function onSignInCallback(authResult) {
    loginMod.onSignInCallback(authResult);
}

$(document).ready(function () {
    $('#gDisconnect').click(loginMod.disconnectServer);
});