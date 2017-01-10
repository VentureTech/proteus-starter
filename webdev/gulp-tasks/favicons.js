/**
 * Created by aholt on 1/10/17.
 */
var gulp = require('gulp');
var gutil = require('gulp-util');
var del = require('del');
var favicons = require('gulp-favicons');
var runSequence = require('run-sequence');

gulp.task('favicons', function (callback) {
    runSequence('favicons:clean', ['favicons:build'], callback);
});
gulp.task('favicons:build', function() {
    return gulp.src("./web/src/**/favicons/logo.png")
        .pipe(favicons({
            appName: "My App",
            appDescription: "This is my application",
            developerName: "Your Name Here OR Null",
            developerURL: "http://venturetech.net/",
            background: "#FFFFFF",
            path: "/_favicons/",
            url: "/",
            display: "standalone",
            orientation: "portrait",
            version: 1.0,
            logging: false,
            online: false,
            html: "index.html",
            pipeHTML: true,
            replace: true
        }))
        .on('error', gutil.log)
        .pipe(gulp.dest('./build/FavIcons'));
});
gulp.task('favicons:clean', function(callback) {
    del(['./build/FavIcons']).then(function(data) {
        callback();
    });
});