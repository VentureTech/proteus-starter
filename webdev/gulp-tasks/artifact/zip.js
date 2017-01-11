/**
 * Created by aholt on 1/10/17.
 */
var gulp = require('gulp');
var gzip = require('gulp-zip');
var del = require('del');
var projectInfo = require('../../package.json');

gulp.task('clean:zip', function() {
    return del(['./artifact/*']);
});

gulp.task('zip', ['build', 'clean:zip'], function() {
    var version = `${projectInfo.name}-${projectInfo.version}`;
    if(projectInfo.versionStatus == "SNAPSHOT")
        version += "-SNAPSHOT";
    return gulp.src('./build/**/*')
        .pipe(gzip(`${version}.zip`))
        .pipe(gulp.dest('./artifact'));
});