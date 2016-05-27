var gulp = require('gulp');
var sass = require('gulp-sass');
var rename = require('gulp-rename');
var minifyCss = require('gulp-clean-css');
var autoprefixer = require('gulp-autoprefixer');
var clip = require('gulp-clip-empty-files');
var sourcemaps = require("gulp-sourcemaps");
var uglify = require('gulp-uglify');
var concat = require('gulp-concat');
var del = require('del');
var fs = require('fs');
var stream = require('stream');
var path = require('path');
var merge = require('merge-stream');

var JS_SOURCE_MAPS = function setupSourceMaps() {
	var commonScripts = {
		id: "common-scripts",
		src: "/_resources/dyn/files/1079664z7254cb9a/_fn"
	};
	var lrMenu = {
		id: "lr-menu",
		src: "/_resources/dyn/files/1079662z9b376eaf/_fn"
	};
	var defaultSrcMap = '';
	var list = [commonScripts, lrMenu];
	function getSourceMap(id) {
		var result = list.filter(function(srcMap) {
			return srcMap.id == id;
		}).map(function(srcMap) {
			return srcMap.src;
		}).pop();

		if(!result)
			result = defaultSrcMap;
		return result;
	}

	return {
		getSourceMap: getSourceMap
	};
}();

function getFolders(dir) {
	return fs.readdirSync(dir)
		.filter(function(file) {
			return fs.statSync(path.join(dir, file)).isDirectory();
		});
}

function getJSFiles(dir) {
	return fs.readdirSync(dir)
		.filter(function (file) {
			return !fs.statSync(path.join(dir, file)).isDirectory();
		})
		.filter(function (file) {
			return !file.startsWith('_');
		})
		.filter(function (file) {
			return file.endsWith('.js');
		});
}

gulp.task('default', ['build']);

gulp.task('build', ['styles', 'design', 'javascript', 'favicons']);
gulp.task('clean', ['styles:clean', 'design:clean', 'javascript:clean', 'favicons:clean']);

gulp.task('styles', ['styles:build']);
gulp.task('styles:build', ['styles:clean'], function() {
	return gulp.src('./web/src/stylesheets/**/*.scss')
		.pipe(clip())
		.pipe(sass({
			outputStyle: 'expanded'
		}))
		.pipe(autoprefixer({
			browsers: ['> 1%', 'last 2 versions', 'ie > 9'],
			cascade: false
		}))
		.pipe(gulp.dest('./web/build/stylesheets'))
		.pipe(minifyCss())
		.pipe(rename({ suffix: '.min' }))
		.pipe(gulp.dest('./web/build/stylesheets'))
		;
});
gulp.task('styles:clean', function(callback) {
	del(['./web/build/stylesheets']).then(function(data) {
		callback();
	});
});

gulp.task('javascript', ['javascript:build']);
gulp.task('javascript:build', ['javascript:minify']);
gulp.task('javascript:minify', ['javascript:concat'], function() {
	var folders = getFolders('./web/src/javascript');
	var tasks = folders.map(function (folder) {
		var folderPath = path.join('./web/src/javascript', folder);
		var destFolderPath = path.join('./web/build/javascript', folder);
		return getJSFiles(folderPath).map(function(file) {
			return gulp.src(path.join(folderPath, '/', file))
				.pipe(sourcemaps.init({ debug: true }))
				.pipe(gulp.dest(destFolderPath))
				.pipe(uglify())
				.pipe(rename({ suffix: '.min' }))
				.pipe(sourcemaps.write('./', {
					sourceMappingURLPrefix: JS_SOURCE_MAPS.getSourceMap(file.replace('.min', '').replace('.js', ''))
				}))
				.pipe(gulp.dest(destFolderPath));
		});
	});
	return merge(tasks, getJSFiles('./web/src/javascript').map(function(file) {
		return gulp.src(path.join('./web/src/javascript', '/', file))
			.pipe(sourcemaps.init({ debug: true }))
			.pipe(gulp.dest('./web/build/javascript'))
			.pipe(uglify())
			.pipe(rename({ suffix: '.min' }))
			.pipe(sourcemaps.write('./', {
				sourceMappingURLPrefix: JS_SOURCE_MAPS.getSourceMap(file.replace('.min', '').replace('.js', ''))
			}))
			.pipe(gulp.dest('./web/build/javascript'));
	}));
});
gulp.task('javascript:concat', ['javascript:clean'], function() {
	var folders = getFolders('./web/src/javascript');

	return folders.map(function (folder) {
		var folderPath = path.join('./web/src/javascript', folder);
		var destFolderPath = path.join('./web/build/javascript', folder);
		var subFolders = getFolders(folderPath);
		return subFolders.map(function (subFolder) {
			return gulp.src(path.join(folderPath, subFolder, '/_*.js'))
				.pipe(sourcemaps.init())
				.pipe(concat(subFolder + '.js'))
				.pipe(gulp.dest(destFolderPath))
				.pipe(uglify())
				.pipe(rename({suffix: '.min'}))
				.pipe(sourcemaps.write('./', {
					sourceMappingURLPrefix: JS_SOURCE_MAPS.getSourceMap(subFolder)
				}))
				.pipe(gulp.dest(destFolderPath));
		});
	});
});
gulp.task('javascript:clean', function(callback) {
	del(['./web/build/javascript']).then(function(data) {
		callback();
	});
});

gulp.task('design', ['design:build']);
gulp.task('design:build', ['design:clean'], function() {
	return gulp.src('./web/src/design/**/*')
		.pipe(gulp.dest('./web/build/design'));
});
gulp.task('design:clean', function(callback) {
	del(['./web/build/design']).then(function(data) {
		callback();
	});
});

gulp.task('favicons', ['favicons:build']);
gulp.task('favicons:build', ['favicons:clean'], function() {
	return gulp.src('./web/src/favicons/**/*')
		.pipe(gulp.dest('./web/build/favicons'));
});
gulp.task('favicons:clean', function(callback) {
	del(['./web/build/favicons']).then(function(data) {
		callback();
	});
});