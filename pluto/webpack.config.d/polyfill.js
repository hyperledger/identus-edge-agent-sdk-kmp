/* eslint-disable no-undef */
config.resolve = {
    fallback: {
        "crypto": require.resolve("crypto-browserify"),
        "path": require.resolve("path-browserify"),
        "stream": require.resolve("stream-browserify"),
        "fs": false
    }
};