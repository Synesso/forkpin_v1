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
                // After we load the Google+ API, render the profile data from Google+.
                gapi.client.load('plus', 'v1', this.renderProfile);
                $('#gDisconnect').show();
            } else if (authResult['error']) {
                // There was an error, which means the user is not signed in.
                // As an example, you can troubleshoot by writing to the console:
                console.log('There was an error: ' + authResult['error']);
                $('#gConnect').show();
            }
            console.log('authResult', authResult);
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
                loginMod.connectServer(profile.id);
                divs.userName.append(profile.displayName);
                divs.selfPanel.append('<img src="' + profile.image.url + '" class="img-circle"/>');
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

        connectServer: function (gplusId) {
            url = window.location.origin + '/connect?state=' + gplusOneTimeToken + '&gplus_id=' + gplusId;
            console.log('connectServer -> ', url);
            $.ajax({
                type: 'POST',
                url: url,
                contentType: 'application/octet-stream; charset=utf-8',
                success: function (result) {
                    // helper.people();
                },
                error: function (e) {
                    console.log('error connecting:', e.status, e.statusText);
                },
                processData: false,
                data: this.authResult.code
            });
        }

        /**
         * Calls the server endpoint to get the list of people visible to this app.
         * todo - this is not used but could be for the challenge-friend functionality
         */
        /*
         people: function() {
         $.ajax({
         type: 'GET',
         url: window.location.origin + '/people',
         contentType: 'application/octet-stream; charset=utf-8',
         success: function(result) {
         loginMod.appendCircled(result);
         },
         error: function(e) {
         console.log('error getting people list', e);
         }
         });
         },
         */
        /**
         * Displays visible People retrieved from server.
         *
         * @param {Object} people A list of Google+ Person resources.
         * todo - just an example of how to get people's details
         */
        /*
         appendCircled: function(people) {
         $('#visiblePeople').empty().append('Number of people visible to this app: ' + people.totalItems + '<br/>');
         for (var personIndex in people.items) {
         person = people.items[personIndex];
         $('#visiblePeople').append('<img src="' + person.image.url + '" title="' + person.displayName + '">');
         }
         },
         */

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