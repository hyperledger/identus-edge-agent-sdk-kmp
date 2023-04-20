config.resolve = {
    fallback: {
        stream: require.resolve("stream-browserify")
    }
};
var webpack = require('webpack');

config.plugins.push(new webpack.ProvidePlugin({
    Buffer: ['buffer', 'Buffer'],
}));
