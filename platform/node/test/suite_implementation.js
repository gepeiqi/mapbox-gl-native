'use strict';

var mbgl = require('../index');
var request = require('request');
var PNG = require('pngjs').PNG;
var fs = require('fs');
var path = require('path');

mbgl.on('message', function(msg) {
    console.log('%s (%s): %s', msg.severity, msg.class, msg.text);
});

module.exports = function (style, options, callback) {
    var map = new mbgl.Map({
        ratio: options.pixelRatio,
        request: function(req, callback) {
            request(req.url, {encoding: null}, function (err, response, body) {
                if (err) {
                    callback(err);
                } else if (response.statusCode == 404) {
                    callback();
                } else if (response.statusCode != 200) {
                    callback(new Error(response.statusMessage));
                } else {
                    callback(null, {data: body});
                }
            });
        }
    });

    var timedOut = false;
    var watchdog = setTimeout(function () {
        timedOut = true;
        map.dumpDebugLogs();
        callback(new Error('timed out after 20 seconds'));
    }, 20000);

    options.debug = {
        tileBorders: options.debug,
        collision: options.collisionDebug,
        overdraw: options.showOverdrawInspector,
    };

    options.center = style.center || [0, 0];
    options.zoom = style.zoom || 0;
    options.bearing = style.bearing || 0;
    options.pitch = style.pitch || 0;

    map.load(style);

    applyOperations(options.operations, function() {
        map.render(options, function (err, pixels) {
            var results = options.queryGeometry ?
              map.queryRenderedFeatures(options.queryGeometry, options.queryOptions || {}) :
              [];
            map.release();
            if (timedOut) return;
            clearTimeout(watchdog);
            callback(err, pixels, results.map(prepareFeatures));
        });
    });

    function applyOperations(operations, callback) {
        var operation = operations && operations[0];
        if (!operations || operations.length === 0) {
            callback();

        } else if (operation[0] === 'wait') {
            map.render(options, function () {
                applyOperations(operations.slice(1), callback);
            });

        } else if (operation[0] === 'addImage' || operation[0] === 'updateImage') {
            var img = PNG.sync.read(fs.readFileSync(path.join(__dirname, '../../../mapbox-gl-js/test/integration', operation[2])));

            map.addImage(operation[1], img.data, {
                height: img.height,
                width: img.width,
                pixelRatio: operation[3] || 1
            });

            applyOperations(operations.slice(1), callback);

        } else if (operation[0] === 'setStyle') {
            map.load(operation[1]);
            applyOperations(operations.slice(1), callback);

        } else {
            // Ensure that the next `map.render(options)` does not overwrite this change.
            if (operation[0] === 'setCenter') {
                options.center = operation[1];
            } else if (operation[0] === 'setZoom') {
                options.zoom = operation[1];
            } else if (operation[0] === 'setBearing') {
                options.bearing = operation[1];
            } else if (operation[0] === 'setPitch') {
                options.pitch = operation[1];
            }

            map[operation[0]].apply(map, operation.slice(1));
            applyOperations(operations.slice(1), callback);
        }
    }

    function prepareFeatures(r) {
        delete r.layer;
        return r;
    }
};
