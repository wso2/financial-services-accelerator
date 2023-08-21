/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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
const HtmlWebpackPlugin = require("html-webpack-plugin");
const CopyPlugin = require("copy-webpack-plugin");
const isProduction = process.env.NODE_ENV === "production";

const config = {
  entry: {
    index: "./frontend/accelerator/src/index.js",
  },
  output: {
    path: path.resolve(__dirname, "site/public/dist"),
    filename: "[name].[contenthash].bundle.js",
    chunkFilename: "[name].[contenthash].bundle.js",
    clean: true,
    globalObject: "this",
    ignoreBrowserWarnings: true,
  },
  watchOptions: {
    poll: 1000,
    ignored: ["node_modules", "META-INF", "WEB-INF"]
  },
  devServer: {
    client: {
      overlay: true,
      progress: true,
      reconnect: 3,
    },
    compress: true,
    historyApiFallback: true,
    hot: true,
    open: {
      target: ["/consentmgr"],
      app: {
        name: "google-chrome",
        arguments: ["--incognito", "--new-window"],
      },
    },
    server: "https",
    port: 3000,
  },
  module: {
    rules: [
      {
        test: /\.(js|jsx)$/,
        exclude: /node_modules/,
        use: [
          {
            loader: "babel-loader",
          },
          {
            loader: path.resolve("loader.js"),
          },
        ],
      },
      {
        test: /\.css$/i,
        use: ["style-loader", "css-loader"],
      },
      {
        test: /\.s[ac]ss$/i,
        use: ["style-loader", "css-loader", "sass-loader"],
      },
      {
        test: /\.(png|svg|jpg|jpeg|gif|ico|icon)$/i,
        type: "asset",
        generator: {
          filename: "images/[name][ext]",
        },
      },
    ],
  },
  plugins: [
    new HtmlWebpackPlugin({
      hash: true,
      minify: true,
      title: "Consent Manager",
      filename: path.resolve(__dirname, "site/public/dist/index.html"),
      favicon: path.resolve(__dirname, "site/public/images/favicon.ico"),
      template: path.resolve(__dirname, "site/public/pages/index.template.html"),
    }),
    new CopyPlugin({
      patterns: [
        {from: "../../lib/auth/lib", to: "../libs/auth/lib", noErrorOnMissing: true},
        {from: "../../lib/auth/package.json", to: "../libs/auth", noErrorOnMissing: true},
        {from: "../../lib/i18n/lib", to: "../libs/i18n/lib", noErrorOnMissing: true },
        {from: "../../lib/i18n/package.json", to: "../libs/i18n", noErrorOnMissing: true},
      ],
    }),
  ],
};

module.exports = () => {
  if (isProduction) {
    config.mode = "production";
    config.output.publicPath = "/consentmgr/site/public/dist/";
  } else {
    config.mode = "development";
    config.output.publicPath = "/";
  }
  return config;
};
