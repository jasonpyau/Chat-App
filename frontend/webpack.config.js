const path = require('path');
const target = path.resolve(__dirname, '../backend/src/main/resources/static/built');

module.exports = {
    entry: './src/index.tsx',
    devtool: 'source-map',
    cache: true,
    mode: 'production', //'development',//'production',
    module: {
        rules: [
            {
                test: /\.tsx?$/,
                use: 'ts-loader',
                exclude: /node_modules/
            },
            {
                test: /\.css$/i,
                use: ["style-loader", "css-loader"]
            }
        ]
    },
    resolve: {
        extensions: ['.tsx', '.ts', '.js'],
        fallback: {
            net: false
        }
    },
    output: {
        path: target,
        filename: 'bundle.js'
    }
};