/**
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

const path = require("path");

module.exports = {
  entry: "./accelerator/index.js",
  output: {
    path: path.join(__dirname, "/../dist"),
    filename: "index_bundle.js",
    publicPath: "/",
  },
  devtool: 'source-map',
  devServer: {
    historyApiFallback: true,
    open: true,
    server: 'https',
  },
  module: {
    rules: [
      {
        test: /\.(js|jsx)$/,
        exclude: /node_modules/,
        use: [
          {
              loader: 'babel-loader',
          },
          {
              loader: path.resolve('loader.js'),
          },
      ],
      },
      {
        test: /\.css$/,
        use: ['style-loader', 'css-loader'],
      },
      {
        test: /\.(png|svg|jpg|jpeg|gif)$/i,
        type: 'asset/resource',
        generator: {
          filename: 'images/[name][ext]',
          publicPath: '/consentmgr/dist/',
        },
      },
      {
        test: /\.json$/,
        loader: 'json-loader',
      }
    ]
  }
};
