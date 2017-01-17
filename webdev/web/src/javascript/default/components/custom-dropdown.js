/**
 * Created by aholt on 6/29/16.
 */
function setupCustomDropdowns(ctx) {
    var $ctx = !!ctx ? $(ctx.parentNode || document) : $(ctx || document);
    //Main logo only
    $ctx.find('.dropdown .toggler-wrapper  [data-background-image]').each(function(idx, target) {
        var backgroundurl = target.getAttribute('data-background-image');
        target.style.cssText += ';overflow: hidden;'
            + 'text-indent: -75rem;'
            + 'white-space: nowrap;'
            + 'margin-left: 7px;'
            + 'width: 12rem;'
            + 'display: inline-block;'
            + 'vertical-align: middle;'
            + 'font-size: 2rem;'
            + 'color: #2e2e2e;'
            + 'text-transform: uppercase;'
            + 'text-align: start;'
            + 'padding: 0;'
            + 'background: url(' + backgroundurl + ') center center/contain no-repeat;';
    });

    //Dropdown menu items only
    $ctx.find('.dropdown .dropdown-menu [data-background-image]').each(function(idx, target) {
        var backgroundurl = target.getAttribute('data-background-image');
        target.style.cssText += ';overflow: hidden;'
            + 'text-indent: 0;'
            + 'margin: .5rem 0;'
            + 'white-space: nowrap;'
            + 'width: 100%;'
            + 'display: block;'
            + 'vertical-align: middle;'
            + 'font-size: .85rem;'
            + 'color: #2e2e2e;'
            + 'text-transform: none;'
            + 'text-align: start;'
            + 'padding: .25rem 0 .25rem 4.25rem;'
            + 'background: url(' + backgroundurl + ') left center/contain no-repeat;';
    });
}