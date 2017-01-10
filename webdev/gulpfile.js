//noinspection JSUnresolvedVariable
require('dotenv').config({
    path: `${process.env.HOME}/.gradle/gradle.properties`
});
var gulp = require('gulp');
var del = require('del');

require('./gulp-tasks/styles');
require('./gulp-tasks/javascript');
require('./gulp-tasks/design');
require('./gulp-tasks/favicons');
require('./gulp-tasks/artifact/zip');
require('./gulp-tasks/artifact/publish');

gulp.task('default', ['build']);

gulp.task('build', ['styles', 'design', 'javascript', 'favicons']);
gulp.task('clean', function(callback) {
    del(['./build/']).then(function(data) {
        callback();
    });
});

require('gulp-task-list')(gulp);