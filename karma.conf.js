module.exports = function (config) {
    config.set({
        browsers: ['ChromiumHeadlessNoSandbox'],
        customLaunchers: {
        // This will allow CI pipelines to run karma tests as root
        ChromiumHeadlessNoSandbox: {
            base: 'ChromiumHeadless',
            flags: ['--no-sandbox']
          }
        },
        // The directory where the output file lives
        basePath: 'target',
        // The file itself
        files: ['ci.js'],
        frameworks: ['cljs-test'],
        plugins: ['karma-cljs-test', 'karma-chrome-launcher'],
        colors: true,
        logLevel: config.LOG_INFO,
        client: {
            args: ["shadow.test.karma.init"],
            singleRun: true
        }
    })
};
