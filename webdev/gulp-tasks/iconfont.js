var _ = require('underscore');
var gulp = require('gulp');
var codepoints = require('codepoints');
var consolidate = require('gulp-consolidate');
var rename = require('gulp-rename');
var iconfont = require('gulp-iconfont');

gulp.task('iconfont', function(){
    return gulp.src('./web/src/FontGlyphs/src/icons/*.svg')
        .pipe(iconfont({
            fontName: 'neptune-glyph-font', // required
            appendCodepoints: true, // recommended option
            startCodepoint: 0xF101,
            fontHeight: 1001,
            normalize: true
        }))
        .on('glyphs', function(glyphs, options) {
            glyphs = _.map(glyphs, function(glyph) {
                return {
                    name: glyph.name,
                    glyph: glyph.unicode[0].charCodeAt(0).toString(16).toUpperCase()
                };
            });

            gulp.src('./web/src/FontGlyphs/src/templates/_iconfont.scsstpl')
                .pipe(consolidate('lodash', {
                    glyphs: glyphs
                }))
                .pipe(rename('_font-glyph-entities.scss'))
                .pipe(gulp.dest('./web/src/stylesheets/default/config/'));
        })
        .pipe(gulp.dest('./web/src/design/default/fonts'));
});