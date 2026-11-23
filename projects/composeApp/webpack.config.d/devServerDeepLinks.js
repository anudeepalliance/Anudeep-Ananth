// Local dev server only: serve index.html for page addresses such as /ocrolus/runs/abc,
// so refreshing a deep link works the same way it does on the Ktor server.
if (config.devServer) {
    config.devServer.historyApiFallback = true;
}
