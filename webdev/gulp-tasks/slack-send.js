/**
 * Created by aholt on 2/2/17.
 */
var rp = require('request-promise');
var projectInfo = require('../package.json');

var getOptions = function(message) {
    return {
        method: 'POST',
        uri: encodeURI(`https://slack.com/api/chat.postMessage?`
            + `token=${projectInfo.slack_token}`
            + `&channel=${projectInfo.slack_channel}`
            + `&text=${message}`
            + `&username=${projectInfo.slack_user}`),
        form: {
            token: projectInfo.slack_token,
            channel: projectInfo.slack_channel,
            text: message,
            username: projectInfo.slack_user
        }
    }
};

module.exports = {
    send: function(message) {
        if(!!projectInfo.slack_token && projectInfo.slack_token != ''
        && !!projectInfo.slack_channel && projectInfo.slack_channel != ''
        && !!projectInfo.slack_user && projectInfo.slack_user != '')
            return rp(getOptions(message));
        else
            return Promise.resolve(true);
    }
};