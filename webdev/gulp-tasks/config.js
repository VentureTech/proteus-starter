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
    runSequence('config:templates', cb);
});

gulp.task('config:templates', function() {
    return gulp.src('./web/src/config/templates/**/*')
        .pipe(gulp.dest('./build/ConfigurationFiles/MIWT Templates'))
});

gulp.task('config:clean', function(cb) {
    del(['./build/ConfigurationFiles']).then(function(data) {
        cb();
    });
});