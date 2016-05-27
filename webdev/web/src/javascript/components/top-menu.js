
$(function() {
    
    /**
     * Provides javascript functionality for setting up the top menu.
     * @requires jquery
     * @author Alan Holt (aholt@venturetech.net)
     */
    function setupTopMenuActive() {
        var host = window.location.host;
        var path = window.location.pathname;
        $('.lr-menu').find('a[href]').each(function(idx, item) {
            var href = item.href, $li = $(item).parent();
            if($li.data('path')) href = $li.data('path');
            const hostIndex = href.indexOf(host);
            if(hostIndex > -1) {
                href = href.slice(hostIndex + host.length);
            }
            if(href !== '/') { // Not doing home page ref
                if (path.indexOf(href) === 0) {
                    $li.addClass('mi-active').removeClass('mi-inactive');
                }
                else {
                    $li.addClass('mi-inactive').removeClass('mi-active');
                }
            } else if(href === path) {
                if (path.indexOf(href) === 0) {
                    $li.addClass('mi-active').removeClass('mi-inactive');
                }
                else {
                    $li.addClass('mi-inactive').removeClass('mi-active');
                }
            }
        });
    }
    
    setupTopMenuActive();
});