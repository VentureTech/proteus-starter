var gulp = require('gulp');
var gutil = require('gulp-util');
var sass = require('gulp-sass');
var rename = require('gulp-rename');
var minifyCss = require('gulp-clean-css');
var autoprefixer = require('gulp-autoprefixer');
var clip = require('gulp-clip-empty-files');
var sourcemaps = require("gulp-sourcemaps");
var uglify = require('gulp-uglify');
var del = require('del');
var include = require("gulp-include");
var favicons = require('gulp-favicons');
var runSequence = require('run-sequence');
var flatten = require('gulp-flatten');

gulp.task('default', ['build']);

gulp.task('build', ['styles', 'design', 'javascript', 'favicons']);
gulp.task('clean', function(callback) {
    del(['./build/']).then(function(data) {
        callback();
    });
});
gulp.task('styles', function (callback) {
    runSequence('styles:clean', ['styles:build', 'styles:vendor'], callback);
});
gulp.task('styles:build', function() {
    return gulp.src('./web/src/stylesheets/**/*.scss')
        .pipe(clip())
        .pipe(sass({
            outputStyle: 'expanded'
        }))
        .pipe(autoprefixer({
            browsers: ['> 1%', 'last 2 versions', 'ie > 9'],
            cascade: false
        }))
        .pipe(gulp.dest('./build/stylesheets'))
        .pipe(minifyCss())
        .pipe(rename({ suffix: '.min' }))
        .pipe(gulp.dest('./build/stylesheets'))
        ;
});
gulp.task('styles:vendor', function() {
    return gulp.src(['./bower_components/select2/dist/css/select2.css'
    ])
        .pipe(clip())
        .pipe(gulp.dest('./build/stylesheets/vendor'))
        .pipe(minifyCss())
        .pipe(rename({ suffix: '.min' }))
        .pipe(gulp.dest('./build/stylesheets/vendor'))
        ;
});
gulp.task('styles:clean', function(callback) {
    del(['./build/stylesheets']).then(function(data) {
        callback();
    });
});

gulp.task('javascript', function(callback){
    runSequence('javascript:clean', ['javascript:build', 'javascript:vendor'], callback);
});
gulp.task('javascript:build', function() {
    return gulp.src(['./web/src/javascript/**/*.js', '!./web/src/javascript/components/*.js'])
        .pipe(clip())
        .pipe(include())
            .on('error', console.log)
        .pipe(gulp.dest('./build/javascript'))
        .pipe(sourcemaps.init({ debug: true }))
        .pipe(uglify())
        .pipe(rename({ suffix: '.min' }))
        .pipe(sourcemaps.write('../sourcemaps/', {
            sourceMappingURL: function(file) {
                var fn = file.path.lastIndexOf('/');
                if(fn > -1 && file.path.length > ++fn) fn = file.path.substring(fn);
                else fn = file.path;
                return '/_sourcemaps/' + fn + '.map';
            }
        }))
		.pipe(flatten({includeParents: 2}))
        .pipe(gulp.dest('./build/javascript'))
        ;
});
gulp.task('javascript:vendor', function() {
    return gulp.src(['./bower_components/jquery/dist/jquery.js',
            './bower_components/tether/dist/js/tether.js',
            './bower_components/select2/dist/js/select2.js',
            './bower_components/select2/dist/js/**/i18n/*.js', // globing for directory creation
            './bower_components/bootstrap/dist/js/bootstrap.js'
    ])
        .pipe(clip())
        .pipe(gulp.dest('./build/javascript/vendor'))
        .pipe(sourcemaps.init({ debug: true }))
        .pipe(uglify())
        .pipe(rename({ suffix: '.min' }))
        .pipe(sourcemaps.write('../../sourcemaps', {
            sourceMappingURL: function(file) {
                var fn = file.path.lastIndexOf('/');
                if(fn > -1 && file.path.length > ++fn) fn = file.path.substring(fn);
                else fn = file.path;
                return '/_sourcemaps/' + fn + '.map';
            }
        }))
        .pipe(flatten({includeParents: 3}))
        .pipe(gulp.dest('./build/javascript/vendor'))
        ;
});
gulp.task('javascript:clean', function(callback) {
    del(['./build/javascript', './build/sourcemaps']).then(function(data) {
        callback();
    });
});

gulp.task('design', function (callback) {
    runSequence('design:clean', ['design:build'], callback);
});
gulp.task('design:build', function() {
    return gulp.src('./web/src/design/**/*')
        .pipe(gulp.dest('./build/design'));
});
gulp.task('design:clean', function(callback) {
    del(['./build/design']).then(function(data) {
        callback();
    });
});

gulp.task('favicons', function (callback) {
    runSequence('favicons:clean', ['favicons:build'], callback);
});
gulp.task('favicons:build', function() {
    return gulp.src("./web/src/favicons/logo.png")
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
        .pipe(gulp.dest('./build/favicons'));
});
gulp.task('favicons:clean', function(callback) {
    del(['./build/favicons']).then(function(data) {
        callback();
    });
});

require('gulp-task-list')(gulp);