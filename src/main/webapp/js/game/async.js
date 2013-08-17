var async = (function () {

    var eshq = new ESHQ("forkpin");

    // callback called when the connection is made
    eshq.onopen = function(e) {
        console.log("Open event", e);
    };

    // called when a new message with no specific type has been received
    eshq.onmessage = function(e) {
        console.log("Message type: %s, message data: %s", e.type, e.data);
    };

    // callback called on error
    eshq.onerror = function(e) {
        console.log("Error event", e);
    };

    return eshq; // todo - if it's useful. Otherwise???

})();