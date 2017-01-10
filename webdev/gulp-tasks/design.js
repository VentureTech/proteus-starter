/**
 * Created by aholt on 1/10/17.
 */
var gulp = require('gulp');
var del = require('del');
var runSequence = require('run-sequence');

gulp.task('design', function (callback) {
    runSequence('design:clean', ['design:build'], callback);
});
gulp.task('design:build', function() {
    return gulp.src('./web/src/design/**/*')
        .pipe(gulp.dest('./build/Design'));
});
gulp.task('design:clean', function(callback) {
    del(['./build/Design']).then(function(data) {
        callback();
    });
});
