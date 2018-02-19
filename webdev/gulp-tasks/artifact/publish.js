/**
 * Created by aholt on 1/10/17.
 */
var gulp = require('gulp');
var gafu = require('gulp-artifactory-upload');
var projectInfo = require('../../package.json');
var stream = require('stream');
var gutil = require('gulp-util');
var slack = require('../slack-send');
var stp = require('gulp-stream-to-promise');

//noinspection JSUnresolvedVariable
var SERVER_URL = process.env.publish_venturetech_url || process.env.publish_venture_tech_url,
    PUBLISH_USERNAME = process.env.publish_venturetech_username,
    PUBLISH_PASSWORD = process.env.publish_venturetech_password,
    parsedGroup = projectInfo.group.replace('.', '/');

var VERSION = `${projectInfo.version}`;
var REPO_KEY = "release";
if(projectInfo.versionStatus === "SNAPSHOT") {
    VERSION += "-SNAPSHOT";
    REPO_KEY = "snapshot";
}
var PUBLISH_METADATA_URL = `${SERVER_URL}/vt-${REPO_KEY}-local/${parsedGroup}/${projectInfo.name}`;
var PUBLISH_URL = `${SERVER_URL}/vt-${REPO_KEY}-local/${parsedGroup}/${projectInfo.name}/${VERSION}`;

gulp.task('publish', ['zip', 'publish:maven-metadata'], function() {
    return stp(gulp.src(`./artifact/${projectInfo.name}-${VERSION}.zip`)
            .pipe(gafu({
                url: PUBLISH_URL,
                username: PUBLISH_USERNAME,
                password: PUBLISH_PASSWORD
            }))
        ).then(function() {
            slack.send(`${projectInfo.name}-${VERSION} has been published to artifactory.`);
    });
});

gulp.task('publish:maven-metadata', ['zip'], function() {
    return templateAsFile('maven-metadata.xml',
        `<?xml version="1.0" encoding="UTF-8"?>
<metadata>
  <groupId>${projectInfo.group}</groupId>
  <artifactId>${projectInfo.name}</artifactId>
  <version>${VERSION}</version>
  <versioning>
    <latest>${VERSION}</latest>
    <versions>
      <version>${VERSION}</version>
    </versions>
    <lastUpdated>${new Date().toISOString()}</lastUpdated>
  </versioning>
</metadata>`).pipe(gafu({
        url: PUBLISH_METADATA_URL,
        username: PUBLISH_USERNAME,
        password: PUBLISH_PASSWORD
    }))
});

function templateAsFile(filename, template) {
    var src = stream.Readable({ objectMode: true });
    src._read = function() {
        this.push(new gutil.File({
            cwd: "",
            base: "",
            path: filename,
            contents: new Buffer(template)
        }));
        this.push(null);
    };
    return src;
}
