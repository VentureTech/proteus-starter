/**
 * Created by aholt on 1/10/17.
 */
var gulp = require('gulp');
var rename = require('gulp-rename');
var clip = require('gulp-clip-empty-files');
var babel = require('gulp-babel');
var sourcemaps = require("gulp-sourcemaps");
var uglify = require('gulp-uglify');
var del = require('del');
var include = require("gulp-include");
var runSequence = require('run-sequence');
var flatten = require('gulp-flatten');

gulp.task('javascript', function (callback) {
  runSequence('javascript:clean', ['javascript:build', 'javascript:vendor'], callback);
});
gulp.task('javascript:build', function () {
  return gulp.src(['./web/src/javascript/**/*.js'])
             .pipe(clip())
             .pipe(include())
             .pipe(babel({
               presets: ['env']
             }))
             .on('error', console.log)
             .pipe(gulp.dest('./build/Javascript'))
             .pipe(sourcemaps.init({debug: true}))
             .pipe(uglify())
             .pipe(rename({suffix: '.min'}))
             .pipe(sourcemaps.write('../Sourcemaps/', {
               sourceMappingURL: function (file) {
                 var fn = file.path.lastIndexOf('/');
                 if (fn > -1 && file.path.length > ++fn) {
                   fn = file.path.substring(fn);
                 } else {
                   fn = file.path;
                 }
                 var pathSplit = file.path.split('/');
                 var tn = null;
                 if (pathSplit.length > 2) {
                   tn = pathSplit[pathSplit.length - 3];
                 }
                 return '/_sourcemaps/' + (tn == null
                   ? ''
                   : tn + '/') + fn + '.map';
               }
             }))
             .pipe(flatten({includeParents: 3}))
             .pipe(gulp.dest('./build/Javascript'));
});
gulp.task('javascript:vendor', function () {
  return gulp.src([
    './node_modules/jquery/dist/jquery.js',
    './node_modules/popper.js/dist/umd/popper.js',
    './node_modules/select2/dist/js/select2.js',
    './node_modules/select2/dist/js/**/i18n/*.js',
    './node_modules/bootstrap/dist/js/bootstrap.js'
  ])
             .pipe(clip())
             .pipe(gulp.dest('./build/Javascript/vendor'))
             .pipe(sourcemaps.init({debug: true}))
             .pipe(uglify())
             .pipe(rename({suffix: '.min'}))
             .pipe(sourcemaps.write('../../Sourcemaps', {
               sourceMappingURL: function (file) {
                 var fn = file.path.lastIndexOf('/');
                 if (fn > -1 && file.path.length > ++fn) {
                   fn = file.path.substring(fn);
                 } else {
                   fn = file.path;
                 }
                 return '/_sourcemaps/' + fn + '.map';
               }
             }))
             .pipe(flatten({includeParents: 3}))
             .pipe(gulp.dest('./build/Javascript/vendor'));
});
gulp.task('javascript:clean', ['sourcemaps:clean'], function (callback) {
  del(['./build/Javascript'])
    .then(function (data) {
      callback();
    });
});
gulp.task('sourcemaps:clean', function (callback) {
  del(['./build/Sourcemaps'])
    .then(function (data) {
      callback();
    });
});
