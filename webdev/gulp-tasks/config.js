/**
 * Created by aholt on 2/6/17.
 */
var gulp = require('gulp');
var del = require('del');
var runSequence = require('run-sequence');

gulp.task('config', function(cb) {
    runSequence('config:clean', ['config:build'], cb);
});

gulp.task('config:build', function(cb) {
    runSequence('config:files', cb);
});

gulp.task('config:files', function() {
    return gulp.src('./web/src/config/**/*')
        .pipe(gulp.dest('./build/ConfigurationFiles/'))
});

gulp.task('config:clean', function(cb) {
    del(['./build/ConfigurationFiles']).then(function(data) {
        cb();
    });
});