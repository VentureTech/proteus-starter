/**
 * Created by aholt on 2/6/17.
 */
var gulp = require('gulp');
var del = require('del');
var runSequence = require('run-sequence');
var inlineCss = require('gulp-inline-css');
var removeHtmlComments = require('gulp-remove-html-comments');


gulp.task('config', function(cb) {
    runSequence('config:clean', ['config:build'], cb);
    inlineCss('config:clean', ['config:build'], cb);
});

gulp.task('config:build', function(cb) {
    runSequence('config:files', cb);
    inlineCss('config:files', cb);
});

gulp.task('config:files', function() {
    gulp.src('./web/src/config/Email Templates/*')
        .pipe(inlineCss({
            applyStyleTags: true,
            applyLinkTags: true,
            removeStyleTags: false,
            removeLinkTags: false
        }))
        .pipe(removeHtmlComments())
        .pipe(gulp.dest('./build/ConfigurationFiles/Email Templates/'));

    gulp.src('./web/src/config/MIWT Templates/**')
        .pipe(gulp.dest('./build/ConfigurationFiles/MIWT Templates/'));
});

gulp.task('config:clean', function(cb) {
    del(['./build/ConfigurationFiles']).then(function(data) {
        cb();
    });
});